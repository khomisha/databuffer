/*
 * Copyright 2011-2022 Mikhail Khodonov
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * $Id$
 */

package org.homedns.mkh.databuffer;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import javax.sql.rowset.WebRowSet;
import org.apache.log4j.Logger;
import org.homedns.mkh.databuffer.api.DataBuffer;
import org.homedns.mkh.databuffer.api.DataBufferManager;
import org.homedns.mkh.sqlmodifier.SQLModifier;
import org.homedns.mkh.util.Util;
import com.akiban.sql.StandardException;

/**
 * DataBuffer
 *
 */
public class DataBufferImpl implements DataBuffer {	
	private static final Logger LOG = Logger.getLogger( DataBufferImpl.class );
	
	private boolean bIsStoredProcedure = false;
	private SQLQuery delete;
	private DataBufferDesc desc;
	private SQLQuery insert;
	private int iPage = 1;
	private Connection pagingConn;
	private ArrayList< String > returnValue;
	private SQLQuery sp;
	private String sPKCol;
	private SQLModifier sqlModifier;
	private SQLQuery update;
	private DBConnection dbConn;
	
	private WebRowSet wrs;
	
	public DataBufferImpl( DataBufferDesc desc, DataSource ds ) throws Exception {
		this.desc = desc;
		returnValue = new ArrayList< String >( );
		sqlModifier = new SQLModifier( );
		wrs = DataBufferManager.getRowSetFactory( ).createWebRowSet( );
		wrs.setMetaData( desc.getMetaData( ) );
		wrs.setCommand( desc.getTable( ).getQuery( ) );
		wrs.setTableName( desc.getTable( ).getUpdateTableName( ) );
		setKeyColumn( desc.getTable( ).getPKcol( ) );
		setSQL( );
		setPageSize( desc.getTable( ).getPageSize( ) );
		dbConn = ( DBConnection )ds;
	}
	
	/**
	 * @see com.sun.rowset.CachedRowSet#close()
	 */
	@Override
	public void close( ) {
		try {
			closeConn( );
			wrs.close( );
		}
		catch( SQLException e ) {
			LOG.error( e.getMessage( ), e );
		}
	}
	
	/**
	 * Closes connection, typically this method should be called when server
	 * paging on and it's need manually close data buffer connection
	 * 
	 * @throws SQLException
	 */
	@Override
	public void closeConn( ) throws SQLException {
		if( wrs.getPageSize( ) > 0 && pagingConn != null ) {
			pagingConn.close( );
		}
	}

	/**
	 * Executes query - stored procedure. To be sure to define right format in
	 * data buffer description file to call stored procedure (property
	 * 'updateTableName').
	 * 
	 * @param iQueryType
	 *            the sql modification query type
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#INSERT},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#UPDATE},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#DELETE}
	 * @param query
	 *            the sql query object
	 * 
	 * @throws SQLException
	 */
	private void execute( int iQueryType, SQLQuery query ) throws SQLException {
		String sQuery = "";
		try(
			Connection conn = dbConn.getConnection( iQueryType );
			CallableStatement stmt = conn.prepareCall( query.getQuery( ) )
		) {
			stmt.registerOutParameter( 1, Types.VARCHAR );
			stmt.setInt( 2, iQueryType );
			int iItem = 3;
			for( String sParm : query.getParmName( ) ) {
				stmt.setObject( iItem, wrs.getObject( sParm ) );
				iItem++;
			}
			sQuery = stmt.toString( );
			LOG.debug( "executing query: " + sQuery );
			stmt.execute( );
			returnValue.clear( );
			returnValue.add( stmt.getString( 1 ) );
			if( stmt.getWarnings( ) != null ) {
				String sWarn = stmt.getWarnings( ).getMessage( );
				returnValue.add( sWarn );				
				LOG.debug( "query message: " + sWarn );
			}
		}
		catch( SQLException e ) {
			throw new SQLException( sQuery, e );
		}
	}

	/**
	 * Executes query (insert, delete, update).
	 * 
	 * @param query
	 *            the sql query object
	 * 
	 * @throws SQLException
	 */
	private void execute( SQLQuery query ) throws SQLException {
		String sQuery = "";
		int iOperation = query.getOperation( );
		try(
			Connection conn = dbConn.getConnection( iOperation );
			PreparedStatement stmt = conn.prepareStatement( 
				query.getQuery( ), Statement.RETURN_GENERATED_KEYS 
			);
		) {
			int iItem = 1;
			for( String sParm : query.getParmName( ) ) {
				Object value = wrs.getObject( sParm );
				stmt.setObject( iItem, value );
				iItem++;
			}
			sQuery = stmt.toString( );
			LOG.debug( "executing query: " + sQuery );
			stmt.executeUpdate( );
			returnValue.clear( );
			if( iOperation == INSERT ) {
				ResultSet ids = stmt.getGeneratedKeys( );
				while( ids.next( ) ) { 
					returnValue.add( ids.getString( sPKCol ) );
				}
			} else if( iOperation == UPDATE ) {
				returnValue.add( wrs.getString( sPKCol ) );
			}
		}
		catch( SQLException e ) {
			throw new SQLException( sQuery, e );
		}
	}

	/**
	 * Executes batch query - stored procedure. To be sure to define right format in
	 * data buffer description file to call stored procedure (property
	 * 'updateTableName'). Postgresql feature stored procedure must return void 
	 * otherwise java.lang.NullPointerException at org.postgresql.core.v3.SimpleParameterList.getV3Length
	 * raises
	 * 
	 * @param iQueryType
	 *            the sql modification query type
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#INSERT},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#UPDATE},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#DELETE}
	 * @param query
	 *            the sql query object
	 * 
	 * @throws SQLException
	 */
	private void executeBatch( int iQueryType, SQLQuery query ) throws SQLException {
		String sQuery = "";
		try(
			Connection conn = dbConn.getConnection( iQueryType );
			CallableStatement stmt = conn.prepareCall( query.getQuery( ) );
		) {
			wrs.beforeFirst( );
			while( wrs.next( ) ) {
				stmt.setInt( 1, iQueryType );
				int iItem = 2;
				for( String sParm : query.getParmName( ) ) {
					stmt.setObject( iItem, wrs.getObject( sParm ) );
					iItem++;
				}
				stmt.addBatch( );
			}
			sQuery = stmt.toString( );
			LOG.debug( "executing query: " + sQuery );
			stmt.executeBatch( );
		}
		catch( SQLException e ) {
			SQLException ne = e.getNextException( );
			String sErrMsg = "";
			if( ne != null ) {
				sErrMsg = ( ne.getMessage( ) != null ) ? ne.getMessage( ) : sErrMsg;
			}
			throw new SQLException( sQuery + ": detailed message: " + sErrMsg, e );
		}
	}
	
	/**
	 * Executes batch of sql queries (insert, delete, update).
	 * 
	 * @param query
	 *            the sql query object
	 * 
	 * @throws SQLException
	 */
	private void executeBatch( SQLQuery query ) throws SQLException {
		String sQuery = "";
		try(
			Connection conn = dbConn.getConnection( query.getOperation( ) );
			PreparedStatement stmt = conn.prepareStatement( query.getQuery( ) );
		) {
			wrs.beforeFirst( );
			while( wrs.next( ) ) {
				int iItem = 1;
				for( String sParm : query.getParmName( ) ) {
					stmt.setObject( iItem, wrs.getObject( sParm ) );
					iItem++;
				}
				stmt.addBatch( );
			}
			sQuery = stmt.toString( );
			LOG.debug( "executing query: " + sQuery );
			stmt.executeBatch( );
		}
		catch( SQLException e ) {
			SQLException ne = e.getNextException( );
			String sErrMsg = "";
			if( ne != null ) {
				sErrMsg = ( ne.getMessage( ) != null ) ? ne.getMessage( ) : sErrMsg;
			}
			throw new SQLException( sQuery + ": detailed message: " + sErrMsg, e );
		}
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getData()
	 */
	@Override
	public String[][] getData( ) throws SQLException {
		return( getData( Arrays.asList( desc.getColumns( ) ) ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getData(java.util.List)
	 */
	@Override
	public String[][] getData( List< Column > cols ) throws SQLException {
		String[][] asData = new String[ wrs.size( ) ][ cols.size( ) ];
		int iRow = 0;
		wrs.beforeFirst( );
		while( wrs.next( ) ) {
			int iCol = 0;
			for( Column col : cols ) {
				int iType = wrs.getMetaData( ).getColumnType( col.getColNum( ) + 1 );
				if( iType == Types.TIMESTAMP ) {
					Date date = wrs.getDate( col.getColNum( ) + 1 );
					asData[ iRow ][ iCol ] = ( date == null ) ? null : DataBufferManager.SERVER_DATE_FMT.format( date );			
				} else {				
					asData[ iRow ][ iCol ] = wrs.getString( col.getColNum( ) + 1 );
				}
				iCol++;
			}
			iRow++;
		}
		return( asData );
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getData(java.lang.String[])
	 */
	@Override
	public String[][] getData( String[] asColName ) throws SQLException {
		List< Column > cols = new ArrayList< Column >( );
		for( String sColName : asColName ) {
			cols.add( desc.getColumn( sColName ) );
		}
		return( getData( cols ) );
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getDataAsList()
	 */
	@Override
	public List< List< Serializable > > getDataAsList( ) throws SQLException {
		return( getDataAsList( Arrays.asList( desc.getColumns( ) ) ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getDataAsList(java.util.List)
	 */
	@Override
	public List< List< Serializable > > getDataAsList( List< Column > cols ) throws SQLException {
		List< List< Serializable > > list = new ArrayList< List< Serializable > >( );
		wrs.beforeFirst( );
		while( wrs.next( ) ) {
			ArrayList< Serializable > row = new ArrayList< Serializable >( );
			for( Column col : cols ) {
				row.add( ( Serializable )wrs.getObject( col.getColNum( ) + 1 ) );
			}
			list.add( row );
		}
		return( list );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getDataAsList(java.lang.String[])
	 */
	@Override
	public List< List< Serializable > > getDataAsList( String[] asColNames ) throws SQLException {
		List< Column > cols = new ArrayList< Column >( );
		for( String sColName : asColNames ) {
			cols.add( desc.getColumn( sColName ) );
		}
		return( getDataAsList( cols ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getDataBufferName()
	 */
	@Override
	public String getDataBufferName( ) {
		return( desc.getName( ) );
	}

	/**
	 * Parses input string value to the timestamp using setting date/time format 
	 * 
	 * @param sValue the input value
	 * 
	 * @return the timestamp
	 * 
	 * @throws ParseException
	 */
	protected Timestamp getDateTime( String sValue ) throws ParseException {
		return( new Timestamp( DataBufferManager.SERVER_DATE_FMT.parse( ( sValue ) ).getTime( ) ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getDescription()
	 */
	@Override
	public DataBufferDesc getDescription( ) {
		return( desc );
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getDescriptionAsJson()
	 */
	@Override
	public String getDescriptionAsJson( ) {
		return( Util.getGson( ).toJson( desc ) );		
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getJson()
	 */
	@Override
	public String getJson( ) throws SQLException {
		Row[] rows = getRawData( );
		StringBuffer sb = new StringBuffer( "[" );
		int iRow = 0;
		for( Row row : rows ) {
			sb.append( Util.getGson( ).toJson( row.getOriginalValues( ) ) );
			if( iRow < rows.length - 1 ) {
				sb.append( "," );
			}
			iRow++;
		}
		sb.append( "]" );
		LOG.debug( sb );
		return( sb.toString( ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getPage()
	 */
	@Override
	public int getPage( ) {
		return( iPage );
	}

	/**
	 * @see org.homedns.mkh.databuffer.api.DataBuffer#getPageSize()
	 */
	@Override
	public int getPageSize( ) {
		return( wrs.getPageSize( ) );
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.api.DataBuffer#getParent()
	 */
	@Override
	public WebRowSet getParent( ) {
		return( wrs );
	}

	/**
	 * Returns raw data
	 * 
	 * @return the raw data
	 * 
	 * @throws SQLException
	 */
	private Row[] getRawData( ) throws SQLException {
		Collection< ? > collection = wrs.toCollection( );
		String s = Util.getGson( ).toJson( collection.toArray( ) );
		return( Util.getGson( ).fromJson( s, Row[].class ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getReturnValue()
	 */
	@Override
	public ArrayList< String > getReturnValue( ) {
		return( returnValue );
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getRowCount()
	 */
	@Override
	public int getRowCount( ) throws SQLException {
		int iRowCount = wrs.size( );
		if( iRowCount > 0 && wrs.getPageSize( ) > 0 ) {
			iRowCount = wrs.getInt( desc.getTable( ).getRowCountCol( ) );
		}
		return( iRowCount );
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getRowData(int)
	 */
	@Override
	public Serializable[] getRowData( int iRow ) throws SQLException {
		Row[] rows = getRawData( );
		return ( rows[ iRow ].getOriginalValues( ) );
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#getXml()
	 */
	@Override
	public String getXml( ) throws SQLException, IOException {
		try( StringWriter writer = new StringWriter( ) ) {
			wrs.writeXml( writer );
			writer.flush( );
			return( writer.toString( ) );		
		}
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#insertData(java.util.List)
	 */
	@Override
	public void insertData( List< List< Serializable > > data ) throws SQLException {
		for( List< Serializable > row : data ) {
			insertDataRow( row );		
		}
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#insertData(java.io.Serializable[][])
	 */
	@Override
	public void insertData( Serializable[][] data ) throws SQLException {
		for( Serializable[] row : data ) {
			insertDataRow( Arrays.asList( row ) );					
		}
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#insertDataRow(java.util.List)
	 */
	@Override
	public void insertDataRow( List< Serializable > row ) throws SQLException {
		if( row.size( ) > desc.getColumns( ).length ) {
			throw new IllegalArgumentException( "Exceeds available columns count" );
		}
		wrs.moveToInsertRow( );
		int iItem = 1;
		for( Object value : row ) {
			if( value instanceof Date ) {
				wrs.updateObject( iItem, new Timestamp( ( ( Date )value ).getTime( ) ) );
			} else {
				wrs.updateObject( iItem, value );
			}
			iItem++;
		}
		wrs.insertRow( );
		wrs.moveToCurrentRow( );
		wrs.last( );
	}

	/**
	 * Modifies original data buffer retrieve query.
	 * 
	 * @param sAddWhere
	 *            the addition for the WHERE clause
	 * 
	 * @return the result query.
	 * 
	 * @throws StandardException
	 */
	private String modifyQuery( String sAddWhere ) throws StandardException {
		if( !sqlModifier.isParsed( ) ) {
			sqlModifier.parseQuery( desc.getTable( ).getQuery( ) );			
		}
		return( sqlModifier.modifyQuery( sAddWhere ) );
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#nextPage()
	 */
	@Override
	public boolean nextPage( ) throws SQLException {
		boolean bNext = wrs.nextPage( );
		if( bNext ) {
			iPage++;
		}
		return( bNext );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#previousPage()
	 */
	@Override
	public boolean previousPage( ) throws SQLException {
		boolean bPrevious = wrs.previousPage( );
		if( bPrevious ) {
			iPage--;
		}
		return( bPrevious );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#putJson(java.lang.String)
	 */
	@Override
	public void putJson( String sJsonData ) throws SQLException, ParseException, IOException {
		LOG.debug( sJsonData );
		String[][] data = Util.getGson( ).fromJson( sJsonData, String[][].class );
		for( String[] row : data ) {
			wrs.moveToInsertRow( );
			for( Column col : desc.getColumns( ) ) {
				Object value = null;
				int iCol = col.getColNum( );
				try {
					value = toSQLType( row[ iCol ], wrs.getMetaData( ).getColumnType( iCol + 1 ) );				
				}
				catch( ParseException e ) {
					ParseException ex = new ParseException( col.getName( ) + ": " + row[ iCol ], 0 );
					ex.initCause( e );
					throw ex;
				}
				if( value == null ) {
					wrs.updateNull( iCol + 1 );
				} else {
					wrs.updateObject( iCol + 1, value );
				}
			}
			wrs.insertRow( );
			wrs.moveToCurrentRow( );
			wrs.last( );
		}
		LOG.debug( "putJson: success" );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#putXml(java.lang.String)
	 */
	@Override
	public void putXml( String sXml ) throws SQLException {
		try( StringReader reader = new StringReader( sXml ) ) {
			wrs.readXml( reader );
		}
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#retrieve()
	 */
	@Override
	public int retrieve( ) throws SQLException {
		Connection conn = null;
		int iPageSize = wrs.getPageSize( );
		try { 
			if( iPageSize > 0 && pagingConn == null ) {
				// server paging switch on
				pagingConn = dbConn.getConnection( RETRIEVE );
			}
			conn = iPageSize > 0 ? pagingConn : dbConn.getConnection( RETRIEVE );
			LOG.debug( getDataBufferName( ) + ": " + wrs.getCommand( ) );
			wrs.execute( conn );
			if( iPageSize > 0 ) {
				iPage = 1;
			}
		}
		finally {
			// close connection if server paging switch off, otherwise connection is still
			// alive until data buffer is closed or closeConn() should be called manually 
			if( iPageSize <= 0 ) {
				conn.close( );
			}
		}
		return( getRowCount( ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#retrieve(java.util.List)
	 */
	@Override
	public int retrieve( List< Serializable > args ) throws SQLException {
		setArgs( args );
		return( retrieve( ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#retrieve(java.util.List, java.lang.String)
	 */
	@Override
	public int retrieve( List< Serializable > args, String sAddWhere ) throws SQLException, StandardException {
		if( sAddWhere != null && !"".equals( sAddWhere ) ) {
			String sQuery = modifyQuery( sAddWhere );
			LOG.debug( "sAddWhere: " + sAddWhere );
			LOG.debug( "modifyQuery: " + sQuery );
			wrs.setCommand( sQuery );
		} else {
			wrs.setCommand( desc.getTable( ).getQuery( ) );
		}
		return( args == null ? retrieve( ) : retrieve( args ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#retrieve(java.lang.String)
	 */
	@Override
	public int retrieve( String sAddWhere ) throws SQLException, StandardException {
		return( retrieve( null, sAddWhere ) );
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#save(int)
	 */
	@Override
	public void save( int iQueryType ) throws SQLException {
		if( bIsStoredProcedure ) {
			execute( iQueryType, sp );
		} else {
			if( iQueryType == INSERT ) {
				execute( insert );
			} else if( iQueryType == UPDATE ) {
				execute( update );
			} else if( iQueryType == DELETE ) {
				execute( delete );
			}
		}
	}

	/**
	 * Saves data buffer row with specified index to the database.
	 * 
	 * @param iQueryType
	 *            the sql modification query type
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#INSERT},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#UPDATE},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#DELETE}
	 * @param iRow
	 *            the row index
	 * 
	 * @throws SQLException
	 */
	@Override
	public void save( int iQueryType, int iRow ) throws SQLException {
		if( setRow( iRow ) ) {
			save( iQueryType );
		}
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#save(int, int, boolean, java.lang.Object)
	 */
	@Override
	@SuppressWarnings( "unchecked" )
	public void save( int iQueryType, int iDataFormat,boolean bBatch, Object data ) throws Exception {
		try( DataBuffer db = new DataBufferImpl( desc, dbConn ) ) {
			if( iDataFormat == XML ) {
				db.putXml( ( String )data );
			} else if( iDataFormat == JSON ) {
				db.putJson( ( String )data );
			} else if( iDataFormat == SERIALIZABLE_ARRAY ) {
				db.insertData( ( Serializable[][] )data );				
			} else if( iDataFormat == SERIALIZABLE_LIST ) {
				if( data instanceof List< ? > ) {
					db.insertData( ( List< List< Serializable > > )data );
				}
			}
			if( bBatch ) {
				db.saveBatch( iQueryType );				
			} else {
				for( int iRow = 1; iRow < db.getParent( ).size( ) + 1; iRow++ ) {
					db.save( iQueryType, iRow );
				}
			}
			retrieve( );
			returnValue.clear( );
			returnValue.addAll( db.getReturnValue( ) );
		}
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#saveBatch(int)
	 */
	@Override
	public void saveBatch( int iQueryType ) throws SQLException {
		if( bIsStoredProcedure ) {
			executeBatch( iQueryType, sp );
		} else {
			if( iQueryType == INSERT ) {
				executeBatch( insert );
			} else if( iQueryType == UPDATE ) {
				executeBatch( update );
			} else if( iQueryType == DELETE ) {
				executeBatch( delete );
			}
		}
	}

	/**
	 * Sets retrieval arguments for query.
	 * 
	 * @param args
	 *            the query arguments list
	 * 
	 * @throws SQLException
	 */
	private void setArgs( List< Serializable > args ) throws SQLException {
		int iItem = 1;
		for( Object arg : args ) {
			LOG.debug( "retrieval argument " + iItem + ": " + arg );
			if( arg instanceof Date ) {
				wrs.setTimestamp( iItem, new Timestamp( ( ( Date )arg ).getTime( ) ) );
			} else {
				wrs.setObject( iItem, arg );
			}
			iItem++;
		}
	}

	/**
	 * Sets primary key column for this data buffer.
	 * 
	 * @param sPKCol
	 *            the primary key column name
	 * 
	 * @throws SQLException
	 */
	private void setKeyColumn( String sPKCol ) throws SQLException {
		this.sPKCol = sPKCol;
		int[] aiPKey = new int[ 1 ];
		aiPKey[ 0 ] = wrs.findColumn( sPKCol );
		wrs.setKeyColumns( aiPKey );
	}

	/**
	 * Sets the size of the page for server paging @see javax.sql.rowset.CachedRowSet, 
	 * which specifies how many rows have to be retrieved at a time. 
	 * Two conditions should be performed: 1. Page size
	 * should be defined on client side as > 0 2. Query definition in data
	 * buffer description should be contain special column 'row_count', which
	 * return query row count. Here is DBMS depended solution for Postgresql
	 * only:
	 * <p>
	 * <pre>
	 * SELECT count(*)over() as row_count, row_number()over() as row_number, 
	 *     sms_event.mev_id, d_event.evt_code, d_event.evt_name, d_egm_type.egt_name, 
	 *     d_egm_state.sta_name, sms_egm.egm_num_in_floor, sms_event.mev_date,  
	 *     sms_event.mev_denom, sms_event.mev_bill_drop, sms_event.mev_coin_drop,  
	 *     sms_event.mev_wat_in, sms_event.mev_total_drop, sms_event.mev_current_credits,  
	 *     sms_event.mev_cancelled_credits, sms_event.mev_total_jackpot, sms_event.mev_wat_out,  
	 *     sms_event.mev_total_hand_paid, sms_event.mev_total_in,  sms_event.mev_total_out,  
	 *     sms_event.mev_games_played, sms_event.mev_total_bills_of_type_1,  
	 *     sms_event.mev_total_bills_of_type_2, sms_event.mev_total_bills_of_type_3,  
	 *     sms_event.mev_total_bills_of_type_4, sms_event.mev_total_bills_of_type_5,  
	 *     sms_event.mev_total_bills_of_type_6, sms_event.mev_total_bills_of_type_7,  
	 *     sms_event.mev_total_bills_of_type_8, sms_event.mev_total_bills_of_type_9,  
	 *     sms_event.mev_total_bills_of_type_10  
	 * FROM  sms_event, d_egm_state,  
	 * 		sms_egm, d_event, d_egm_type 
	 * WHERE  d_egm_state.sta_id = sms_event.sta_id AND sms_egm.egm_id = sms_event.egm_id AND 
	 * 		 d_event.evt_id = sms_event.evt_id AND d_egm_type.egt_id = sms_egm.egt_id
	 * </pre>
	 * <p>
	 * Therefore for others DBMS should be override. NOTE: this is for support
	 * paging on client side. In others cases you should use ascendant
	 * setPageSize()
	 * 
	 * @param iSize
	 *            the page size
	 * 
	 * @throws SQLException
	 */
	protected void setPageSize( Integer iSize ) throws SQLException {
		if( iSize != null && iSize > 0 && !"".equals( desc.getTable( ).getRowCountCol( ) ) ) {
			wrs.setPageSize( iSize );
		} else {
			wrs.setPageSize( 0 );			
		}
	}

	/**
	 * Sets specified row as current row in data buffer.
	 * 
	 * @param iRow
	 *            the row index to set
	 * 
	 * @return true if success and false if failure
	 * 
	 * @throws SQLException
	 */
	private boolean setRow( int iRow ) throws SQLException {
		return( wrs.relative( iRow - wrs.getRow( ) ) );
	}
	
	/**
	* Sets SQL modification queries for prepared statements.
	*/
	private void setSQL( ) throws SQLException {
		List< String > colNames = desc.getUpdatableColNames( );
		int iColCount = colNames.size( );
		if( iColCount < 1 ) {
			return;
		}
		String sTable = desc.getTable( ).getUpdateTableName( );
		if( "".equals( sTable ) ) {
			return;
		}
		bIsStoredProcedure = sTable.contains( "call" );
		if( bIsStoredProcedure ) {
			sp = new SQLQuery(
				"{ " + sTable + 
				"(?," + Util.fill( "?,", iColCount * 2 ).substring( 0, iColCount * 2 - 1 ) + ") }",
				UNKNOWN
			);
			sp.setParmName( colNames );
		} else {
			insert = new SQLQuery(
				"insert into " + sTable +
				"(" + Util.assemble( colNames, "," ) +
				") values(" + Util.fill( "?,", iColCount * 2 ).substring( 0, iColCount * 2 - 1 ) + ")",
				INSERT
			);
			insert.setParmName( colNames );
			delete = new SQLQuery(
				"delete from " + sTable + " where " + sPKCol + " = ?",
				DELETE
			);
			delete.addParmName( sPKCol );
			update = new SQLQuery(
				"update " + sTable +
				" set " + Util.assemble( colNames, " = ?," ) + 
				" = ?" +
				" where " + sPKCol + " = ?",
				UPDATE
			);
			update.setParmName( colNames );
			update.addParmName( sPKCol );
		}
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBuffer1#toSQLType(java.lang.String, int)
	 */
	@Override
	public Serializable toSQLType( String sValue, int iType ) throws ParseException {
		Serializable value = null;
		if( sValue != null ) {
			if( !"".equals( sValue ) ) {
				if( iType == Types.TIMESTAMP ) {
					value = getDateTime( sValue ); 
				} else if( 
					iType == Types.TINYINT || 
					iType == Types.SMALLINT || 
					iType == Types.INTEGER 
				) {
					value = new Integer( sValue );
				} else if( iType == Types.BIGINT ) {
					value = new Long( sValue );					
				} else if( iType == Types.DOUBLE ) {
					value = new Double( sValue );
				} else if( iType == Types.FLOAT ) {
					value = new Float( sValue );
				} else if( iType == Types.BOOLEAN ) {
					value = new Boolean( sValue );					
				}
			}
			if( iType == Types.VARCHAR ) {
				value = sValue;
			}
		}
		return( value );
	}

	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		String s = "";
		try {
			s = "DataBufferImpl [getJson()=" + getJson( ) + "]";
		}
		catch( SQLException e ) {
			LOG.error( e.getMessage( ), e );
		}
		return( s );
	}


	private class SQLQuery {
		private int iOperation = UNKNOWN;
		private List< String > parmName = new ArrayList< String >( );
		private String sQuery;

		/**
		 * @param sQuery
		 *            the query definition
		 * @param iOperation
		 *            the sql operation
		 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#INSERT},
		 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#UPDATE},
		 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#DELETE}
		 */
		private SQLQuery( String sQuery, int iOperation ) {
			setQuery( sQuery );
			this.iOperation = iOperation;
		}

		/**
		 * Adds parameter names to the list.
		 * 
		 * @param sParmName
		 *            the parameter name
		 */
		public void addParmName( String sParmName ) {
			parmName.add( sParmName );
		}

		/**
		 * Returns sql operation.
		 * 
		 * @return the sql operation
		 *         {@link org.homedns.mkh.databuffer.DataBufferImpl#INSERT},
		 *         {@link org.homedns.mkh.databuffer.DataBufferImpl#UPDATE},
		 *         {@link org.homedns.mkh.databuffer.DataBufferImpl#DELETE}
		 */
		public int getOperation( ) {
			return( iOperation );
		}

		/**
		 * Returns parameters names list.
		 * 
		 * @return parameters names list
		 */
		public List< String > getParmName( ) {
			return( parmName );
		}
		
		/**
		 * Returns modifying query.
		 * 
		 * @return query
		 */
		public String getQuery( ) {
			return( sQuery );
		}

		/**
		 * Sets parameters names list.
		 * 
		 * @param parmName
		 *            the parameters names list to set
		 */
		public void setParmName( List< String > parmName ) {
			this.parmName.addAll( parmName );
		}

		/**
		 * Sets modifying query.
		 * 
		 * @param sQuery
		 *            the query to set
		 */
		public void setQuery( String sQuery ) {
			this.sQuery = sQuery;
		}
	}
	
	/**
	 * Data buffer row
	 */
	@SuppressWarnings( "unused" )
	public class Row implements Serializable {
		private static final long serialVersionUID = 4280493623062346204L;
		
		private Serializable[] currentVals;
		private BitSet colsChanged;
		private boolean deleted;
		private boolean updated;
		private boolean inserted;
		private int numCols;
		private Serializable[] origVals;
		
		/**
		 * Returns current values
		 * 
		 * @return the current values
		 */
		public Serializable[] getCurrentValues( ) {
			return( currentVals );
		}
		
		/**
		 * Returns original value
		 * 
		 * @return the original value
		 */
		public Serializable[] getOriginalValues( ) {
			return( origVals );
		}
	}
}
