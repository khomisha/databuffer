/*
 * Copyright 2011-2017 Mikhail Khodonov
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
import java.lang.reflect.Type;
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
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.homedns.mkh.sqlmodifier.SQLModifier;

import com.akiban.sql.StandardException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.sun.rowset.WebRowSetImpl;
import com.sun.rowset.internal.Row;

/**
 * DataBuffer
 *
 */
public class DataBuffer extends WebRowSetImpl {
	private static final long serialVersionUID = -2926317597083330248L;
	private static final Logger LOG = Logger.getLogger( DataBuffer.class );
	
	/**
	 * Specifies insert SQL query 
	 */
	public static final int INSERT 		= 0;
	/**
	 * Specifies update SQL query 
	 */
	public static final int UPDATE 		= 1;
	/**
	 * Specifies delete SQL query 
	 */
	public static final int DELETE 		= 2;
	/**
	 * Specifies retrieve SQL query 
	 */
	public static final int RETRIEVE 	= 3;
	/**
	 * Specifies undefined query 
	 */
	public static final int UNKNOWN		= 4;
	/**
	 * Emulates modification queries action 
	 */
	public static final int PERFORM		= 5;
	
	/**
	 * Specifies XML data format
	 */
	public static final int XML 				= 0;
	/**
	 * Specifies JSON data format
	 */
	public static final int JSON 				= 1;
	/**
	 * Specifies serializable array data format
	 */
	public static final int SERIALIZABLE_ARRAY 	= 2;
	/**
	 * Specifies serializable list data format
	 */
	public static final int SERIALIZABLE_LIST 	= 3;
	
	private MetaData metaData;
	private SQLQuery insert;
	private SQLQuery delete;
	private SQLQuery update;
	private SQLQuery sp;
	private String sPKCol;
	private int iPage = 1;
	private boolean bIsStoredProcedure = false;
	private ArrayList< String > returnValue;
	private SQLModifier sqlModifier;
	private Connection pagingConn;

	/**
	 * @param metaData the data buffer meta data object
	 * 
	 * @throws Exception
	 */
	public DataBuffer( MetaData metaData ) throws Exception {
		returnValue = new ArrayList< String >( );
		sqlModifier = new SQLModifier( );
		this.metaData = metaData;
		DataBufferDesc desc = metaData.getDescription( );
		setCommand( desc.getTable( ).getQuery( ) );
		setTableName( desc.getTable( ).getUpdateTableName( ) );
		setMetaData( metaData );
		setKeyColumn( desc.getTable( ).getPKcol( ) );
		setSQL( );
		setPageSize( desc.getTable( ).getPageSize( ) );
	}
	
	/**
	 * @see com.sun.rowset.CachedRowSetImpl#close()
	 */
	public void close( ) {
		try {
			closeConn( );
			super.close( );
		}
		catch( SQLException e ) {
			LOG.error( e );
		}
	}
	
	/**
	 * Closes connection, typically this method should be called when server
	 * paging on and it's need manually close data buffer connection
	 * 
	 * @throws SQLException
	 */
	public void closeConn( ) throws SQLException {
		if( getPageSize( ) > 0 && pagingConn != null ) {
			pagingConn.close( );
		}
	}

	/**
	 * Returns meta data object {@link org.homedns.mkh.databuffer.MetaData}
	 * 
	 * @return the meta data object
	 */
	public MetaData getMetaData( ) {
		return( metaData );
	}

	/**
	 * Returns data buffer records as string array.
	 * 
	 * @return the array of data or empty array if no records
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public String[][] getData( ) throws SQLException, InvalidDatabufferDesc {
		return( getData( metaData.getColList( ) ) );
	}

	/**
	 * Returns selected columns data buffer records as string array.
	 * 
	 * @param cols
	 *            the list of the selected columns which data will be move to
	 *            output array
	 * 
	 * @return the array of data or empty array if no records
	 * 
	 * @throws SQLException
	 */
	public String[][] getData( List< Column > cols ) throws SQLException {
		String[][] asData = new String[ size( ) ][ cols.size( ) ];
		int iRow = 0;
		beforeFirst( );
		while( next( ) ) {
			int iCol = 0;
			for( Column col : cols ) {
				int iType = metaData.getColumnType( col.getColNum( ) + 1 );
				if( iType == Types.TIMESTAMP ) {
					Date date = getDate( col.getColNum( ) + 1 );
					asData[ iRow ][ iCol ] = (
						( date == null ) ? 
						null : 
						getEnvironment( ).getServerDateFormat( ).format( date )
					);			
				} else {				
					asData[ iRow ][ iCol ] = getString( col.getColNum( ) + 1 );
				}
				iCol++;
			}
			iRow++;
		}
		return( asData );
	}
	
	/**
	 * Returns specified row data
	 * 
	 * @param iRow the row index
	 * 
	 * @return the row data or null if not found the row
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public Serializable[] getRowData( 
		int iRow 
	) throws SQLException, InvalidDatabufferDesc {
		if( iRow > getRowCount( ) || iRow < 0 ) {
			return( null );
		}
		int iRowCount = 0;
		beforeFirst( );
		while( next( ) ) {
			if( iRowCount == iRow ) {
				break;
			}
			iRowCount++;
		}
		Object[] data = getCurrentRow( ).getOrigRow( );
		return ( Arrays.copyOf( data, data.length, Serializable[].class ) );
	}

	/**
	 * Returns selected columns data buffer records as string array.
	 * 
	 * @param asColName
	 *            the columns names array which data will be move to output
	 *            array
	 * 
	 * @return the array of data or null if no records
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public String[][] getData( 
		String[] asColName 
	) throws SQLException, InvalidDatabufferDesc {
		List< Column > cols = new ArrayList< Column >( );
		for( String sColName : asColName ) {
			cols.add( metaData.getDescription( ).getColumn( sColName ) );
		}
		return( getData( cols ) );
	}
	
	/**
	 * Returns data buffer records as list.
	 * 
	 * @return the data list
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public ArrayList< ArrayList< Serializable > > getDataAsList( ) throws SQLException, InvalidDatabufferDesc {
		return( getDataAsList( metaData.getColList( ) ) );
	}

	/**
	 * Returns selected columns data buffer records as list.
	 * 
	 * @param cols
	 *            the list of the selected columns which data will be move to
	 *            output list
	 * 
	 * @return the data list
	 * 
	 * @throws SQLException
	 */
	public ArrayList< ArrayList< Serializable > > getDataAsList( 
		List< Column > cols 
	) throws SQLException {
		ArrayList< ArrayList< Serializable > > list = new ArrayList< ArrayList< Serializable > >( );
		beforeFirst( );
		while( next( ) ) {
			ArrayList< Serializable > row = new ArrayList< Serializable >( );
			for( Column col : cols ) {
				row.add( ( Serializable )getObject( col.getColNum( ) + 1 ) );
			}
			list.add( row );
		}
		return( list );
	}
	
	/**
	 * Returns selected columns data buffer records as list.
	 * 
	 * @param asCols
	 *            the selected column name array which data will be move to
	 *            output list
	 * 
	 * @return the data list
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public ArrayList< ArrayList< Serializable > > getDataAsList( 
		String[] asCols 
	) throws SQLException, InvalidDatabufferDesc {
		List< Column > cols = new ArrayList< Column >( );
		for( String sColName : asCols ) {
			cols.add( metaData.getDescription( ).getColumn( sColName ) );
		}
		return( getDataAsList( cols ) );
	}
	
	/**
	 * Returns data buffer name
	 * 
	 * @return the data buffer name
	 */
	public String getDataBufferName( ) {
		return( metaData.getDataBufferName( ) );
	}

	/**
	 * {@link org.homedns.mkh.databuffer.DataBufferMetaData#getDescription()}
	 */
	public DataBufferDesc getDescription( ) {
		return( metaData.getDescription( ) );
	}

	/**
	 * {@link org.homedns.mkh.databuffer.DataBufferMetaData#getDescriptionAsJson()}
	 */
	public String getDescriptionAsJson( ) {
		return( metaData.getDescriptionAsJson( ) );		
	}

	/**
	 * Returns data buffer environment
	 * 
	 * @return the data buffer environment
	 */
	public Environment getEnvironment( ) {
		return( metaData.getEnvironment( ) );
	}

	/**
	 * Returns data buffer data as json string
	 * 
	 * @return the data buffer data as json string
	 * 
	 * @throws SQLException
	 */
	public String getJson( ) throws SQLException {
		Gson gson = new GsonBuilder( ).registerTypeAdapter( 
			Timestamp.class, 
			new TimestampSerializer( ) 
		).create( );
		Collection< ? > collection = toCollection( );
		Row[] data = collection.toArray( new Row[ collection.size( ) ] );
		StringBuffer sb = new StringBuffer( );
		sb.append( "[" );
		int iRow = 0;
		for( Row row : data ) {
			sb.append( gson.toJson( row.getOrigRow( ) ) );
			if( iRow < data.length - 1 ) {
				sb.append( "," );
			}
			iRow++;
		}
		sb.append( "]" );
		LOG.debug( sb.toString( ) );
		return( sb.toString( ) );		
	}

	/**
	 * Returns current page number.
	 * 
	 * @return page number.
	 */
	public int getPage( ) {
		return( iPage );
	}

	/**
	 * Returns sql query execution result.
	 * 
	 * @return the sql query execution result.
	 */
	public ArrayList< String > getReturnValue( ) {
		return( returnValue );
	}
	
	/**
	 * Returns number of rows.
	 * 
	 * @return number of rows
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public int getRowCount( ) throws SQLException, InvalidDatabufferDesc {
		int iRowCount = size( );
		if( iRowCount > 0 ) {
			if( getPageSize( ) > 0 ) {
				iRowCount = getInt( getDescription( ).getTable( ).getRowCountCol( ) );
			}
		}
		return( iRowCount );
	}

	/**
	 * Returns data buffer data as xml.
	 * 
	 * @return data as xml string or empty string
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public String getXml( ) throws SQLException, IOException {
		StringWriter writer = new StringWriter( );
		String sXml = "";
		try {
			writeXml( writer );
			writer.flush( );
			sXml = writer.toString( );
		}
		finally {
			writer.close( );			
		}
		return( sXml );		
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
	 * @throws InvalidDatabufferDesc 
	 */
	private String modifyQuery( String sAddWhere ) throws StandardException, InvalidDatabufferDesc {
		if( !sqlModifier.isParsed( ) ) {
			sqlModifier.parseQuery( 
				metaData.getDescription( ).getTable( ).getQuery( ) 
			);			
		}
		return( sqlModifier.modifyQuery( sAddWhere ) );
	}

	/**
	 * @see com.sun.rowset.CachedRowSetImpl#nextPage()
	 */
	public boolean nextPage( ) throws SQLException {
		boolean bNext = super.nextPage( );
		if( bNext ) {
			iPage++;
		}
		return( bNext );
	}

	/**
	 * @see com.sun.rowset.CachedRowSetImpl#previousPage()
	 */
	public boolean previousPage( ) throws SQLException {
		boolean bPrevious = super.previousPage( );
		if( bPrevious ) {
			iPage--;
		}
		return( bPrevious );
	}

	/**
	 * Puts data as json string to the data buffer.
	 * 
	 * @param sJsonData
	 *            the data as json string to put
	 * 
	 * @throws SQLException
	 * @throws ParseException 
	 * @throws InvalidDatabufferDesc 
	 * @throws IOException 
	 */
	public void putJson( 
		String sJsonData 
	) throws SQLException, ParseException, InvalidDatabufferDesc, IOException {
		LOG.debug( sJsonData );
		String[][] data;
		try( 
			JsonReader reader = new JsonReader( new StringReader( sJsonData ) );
		) {
			reader.setLenient( true );
			Gson gson = new Gson( );
			data = gson.fromJson( reader, String[][].class );
		}
		for( String[] row : data ) {
			moveToInsertRow( );
			for( Column col : metaData.getColList( ) ) {
				Object value = null;
				int iColIndex = col.getColNum( ) + 1;
				String sValue = row[ col.getColNum( ) ];
				int iType = metaData.getColumnType( iColIndex );
				try {
					value = toSQLType( sValue, iType );				
				}
				catch( ParseException e ) {
					ParseException ex = new ParseException( col.getName( ) + ": " + sValue, 0 );
					ex.initCause( e );
					throw ex;
				}
				if( value == null ) {
					updateNull( iColIndex );
				} else {
					updateObject( iColIndex, value );
				}
			}
			insertRow( );
			moveToCurrentRow( );
			last( );
		}
		LOG.debug( "putJson: success" );
	}
	
	/**
	 * Converts specified value to the sql data type 
	 * 
	 * @param sValue the value
	 * @param iType the target sql data type
	 * 
	 * @return the value or null
	 * 
	 * @throws ParseException
	 */
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
	 * Puts data from xml string to the data buffer
	 * 
	 * @param sXml
	 *            the data as xml string to put
	 * 
	 * @throws SQLException
	 */
	public void putXml( String sXml ) throws SQLException {
		StringReader reader = new StringReader( sXml );
		try {
			readXml( reader );
		}
		finally {
			reader.close( );			
		}
	}

	/**
	* {@inheritDoc}
	*/
	public void removeCurrentRow( ) {
		super.removeCurrentRow( );
	}
	
	/**
	 * Retrieves data from database to the data buffer.
	 * 
	 * @return number of retrieved rows
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public int retrieve( ) throws SQLException, InvalidDatabufferDesc {
		Connection conn = null;
		int iPageSize = getPageSize( );
		try { 
			if( iPageSize > 0 && pagingConn == null ) {
				// server paging switch on
				pagingConn = getEnvironment( ).getTransObject( ).getConnection( RETRIEVE );
			}
			conn = iPageSize > 0 ? pagingConn : getEnvironment( ).getTransObject( ).getConnection( RETRIEVE );
			LOG.debug( getDataBufferName( ) + ": " + getCommand( ) );
			execute( conn );
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
	 * Retrieves data from database to the data buffer.
	 * 
	 * @param args
	 *            the retrieval arguments list
	 * 
	 * @return number of retrieved rows
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public int retrieve( 
		List< Serializable > args 
	) throws SQLException, InvalidDatabufferDesc {
		setArgs( args );
		return( retrieve( ) );
	}

	/**
	 * Retrieves data from database to the data buffer.
	 * 
	 * @param args
	 *            the query arguments list
	 * @param sAddWhere
	 *            the additional conditions for the WHERE clause
	 * 
	 * @return number of retrieved rows
	 * 
	 * @throws SQLException
	 * @throws StandardException
	 * @throws InvalidDatabufferDesc 
	 */
	public int retrieve( 
		List< Serializable > args, 
		String sAddWhere 
	) throws SQLException, StandardException, InvalidDatabufferDesc {
		if( sAddWhere != null && !"".equals( sAddWhere ) ) {
			String sQuery = modifyQuery( sAddWhere );
			LOG.debug( "sAddWhere: " + sAddWhere );
			LOG.debug( "modifyQuery: " + sQuery );
			setCommand( sQuery );
		} else {
			setCommand( getDescription( ).getTable( ).getQuery( ) );
		}
		return( args == null ? retrieve( ) : retrieve( args ) );
	}
	
	/**
	 * Retrieves data from database to the data buffer.
	 * 
	 * @param sAddWhere
	 *            the additional conditions for the WHERE clause
	 * 
	 * @return number of retrieved rows
	 * 
	 * @throws SQLException
	 * @throws StandardException
	 * @throws InvalidDatabufferDesc 
	 */
	public int retrieve( 
		String sAddWhere 
	) throws SQLException, StandardException, InvalidDatabufferDesc {
		return( retrieve( null, sAddWhere ) );
	}

	/**
	 * Saves current data buffer row to the database.
	 * 
	 * @param iQueryType
	 *            the sql modification query type
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#INSERT},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#UPDATE},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#DELETE}
	 * 
	 * @throws SQLException
	 */
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
	 * Saves data in database.
	 * 
	 * @param iQueryType
	 *            the sql modification query type
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#INSERT},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#UPDATE},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#DELETE}
	 * @param iDataFormat
	 *            the data format
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#XML},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#JSON},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#SERIALIZABLE_ARRAY}
	 * @param data
	 *            the data to save
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings( "unchecked" )
	public void save( 
		int iQueryType, 
		int iDataFormat,
		boolean bBatch,
		Object data 
	) 	throws Exception {
		DataBuffer db = null;
		try {
			db = new DataBuffer( metaData );
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
				for( int iRow = 1; iRow < db.size( ) + 1; iRow++ ) {
					db.save( iQueryType, iRow );
				}
			}
			retrieve( );
			returnValue.clear( );
			returnValue.addAll( db.getReturnValue( ) );
		}
		finally {
			if( db != null ) {
				db.close( );
			}
		}
	}

	/**
	 * Saves data buffer row with specified index to the database.
	 * 
	 * @param iQueryType
	 *            the sql modification query type
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#INSERT},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#UPDATE},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#DELETE}
	 * @param iRow
	 *            the row index
	 * 
	 * @throws SQLException
	 */
	public void save( int iQueryType, int iRow ) throws SQLException {
		if( setRow( iRow ) ) {
			save( iQueryType );
		}
	}

	/**
	 * Submits a batch of modifying commands to the database to save data buffer data.
	 * 
	 * @param iQueryType
	 *            the sql modification query type
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#INSERT},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#UPDATE},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#DELETE}
	 * 
	 * @throws SQLException
	 */
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
	 * Inserts data to the data buffer immediately following the
	 * current row.
	 * 
	 * @param data
	 *            the data to insert
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public void insertData( Serializable[][] data ) throws SQLException, InvalidDatabufferDesc {
		for( Serializable[] row : data ) {
			insertDataRow( Arrays.asList( row ) );					
		}
	}

	/**
	 * Inserts data to the data buffer immediately following the
	 * current row.
	 * 
	 * @param data
	 *            the data to insert
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public void insertData( List< List< Serializable > > data ) throws SQLException, InvalidDatabufferDesc {
		for( List< Serializable > row : data ) {
			insertDataRow( row );		
		}
	}
	
	/**
	 * Inserts data to the data buffer immediately following the
	 * current row.
	 * 
	 * @param data
	 *            the data to insert
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public void insertData( ArrayList< ArrayList< Serializable > > data ) throws SQLException, InvalidDatabufferDesc {
		for( List< Serializable > row : data ) {
			insertDataRow( row );		
		}
	}

	/**
	 * Inserts the data row into this data buffer immediately following the
	 * current row.
	 * 
	 * @param row
	 *            the data row to insert
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public void insertDataRow( List< Serializable > row ) throws SQLException, InvalidDatabufferDesc {
		if( row.size( ) > metaData.getColList( ).size( ) ) {
			throw new IllegalArgumentException( 
				Util.getBundle( getEnvironment( ).getLocale( ) ).getString( "tooFewColumns" ) 
			);
		}
		moveToInsertRow( );
		int iItem = 1;
		for( Object value : row ) {
			if( value instanceof Date ) {
				updateObject( 
					iItem,
					new Timestamp( ( ( Date )value ).getTime( ) ) 
				);
			} else {
				updateObject( iItem, value );
			}
			iItem++;
		}
		insertRow( );
		moveToCurrentRow( );
		last( );
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
	 * @throws InvalidDatabufferDesc 
	 */
	protected void setPageSize( Integer iSize ) throws SQLException, InvalidDatabufferDesc {
		if( 
			iSize != null &&
			iSize > 0 &&
			!"".equals( getDescription( ).getTable( ).getRowCountCol( ) ) 
		) {
			super.setPageSize( iSize );
		} else {
			super.setPageSize( 0 );			
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
	public boolean setRow( int iRow ) throws SQLException {
		return( relative( iRow - getRow( ) ) );
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
		return( new Timestamp( getEnvironment( ).getServerDateFormat( ).parse( ( sValue ) ).getTime( ) ) );
	}

	/**
	 * Executes query - stored procedure. To be sure to define right format in
	 * data buffer description file to call stored procedure (property
	 * 'updateTableName').
	 * 
	 * @param iQueryType
	 *            the sql modification query type
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#INSERT},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#UPDATE},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#DELETE}
	 * @param query
	 *            the sql query object
	 * 
	 * @throws SQLException
	 */
	private void execute( int iQueryType, SQLQuery query ) throws SQLException {
		String sQuery = "";
		try(
			Connection conn = getEnvironment( ).getTransObject( ).getConnection( iQueryType );
			CallableStatement stmt = conn.prepareCall( query.getQuery( ) );
		) {
			stmt.registerOutParameter( 1, Types.VARCHAR );
			stmt.setInt( 2, iQueryType );
			int iItem = 3;
			for( String sParm : query.getParmName( ) ) {
				stmt.setObject( iItem, getObject( sParm ) );
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
	 * Executes batch query - stored procedure. To be sure to define right format in
	 * data buffer description file to call stored procedure (property
	 * 'updateTableName'). Postgresql feature stored procedure must return void 
	 * otherwise java.lang.NullPointerException at org.postgresql.core.v3.SimpleParameterList.getV3Length
	 * raises
	 * 
	 * @param iQueryType
	 *            the sql modification query type
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#INSERT},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#UPDATE},
	 *            {@link org.homedns.mkh.databuffer.DataBuffer#DELETE}
	 * @param query
	 *            the sql query object
	 * 
	 * @throws SQLException
	 */
	private void executeBatch( int iQueryType, SQLQuery query ) throws SQLException {
		String sQuery = "";
		try(
			Connection conn = getEnvironment( ).getTransObject( ).getConnection( iQueryType );
			CallableStatement stmt = conn.prepareCall( query.getQuery( ) );
		) {
			beforeFirst( );
			while( next( ) ) {
				stmt.setInt( 1, iQueryType );
				int iItem = 2;
				for( String sParm : query.getParmName( ) ) {
					stmt.setObject( iItem, getObject( sParm ) );
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
			Connection conn = getEnvironment( ).getTransObject( ).getConnection( iOperation );
			PreparedStatement stmt = conn.prepareStatement( 
				query.getQuery( ), Statement.RETURN_GENERATED_KEYS 
			);
		) {
			int iItem = 1;
			for( String sParm : query.getParmName( ) ) {
				Object value = getObject( sParm );
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
				returnValue.add( getString( sPKCol ) );
			}
		}
		catch( SQLException e ) {
			throw new SQLException( sQuery, e );
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
			Connection conn = getEnvironment( ).getTransObject( ).getConnection( query.getOperation( ) );
			PreparedStatement stmt = conn.prepareStatement( query.getQuery( ) );
		) {
			beforeFirst( );
			while( next( ) ) {
				int iItem = 1;
				for( String sParm : query.getParmName( ) ) {
					stmt.setObject( iItem, getObject( sParm ) );
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
				setTimestamp( iItem, new Timestamp( ( ( Date )arg ).getTime( ) ) );
			} else {
				setObject( iItem, arg );
			}
			iItem++;
		}
	}

	/**
	* Sets SQL modification queries for prepared statements.
	*/
	private void setSQL( ) throws SQLException {
		List< String > colNames = metaData.getUpdatableColNames( );
		if( colNames == null ) {
			return;
		}
		int iColCount = colNames.size( );
		if( iColCount < 1 ) {
			return;
		}
		String sTable = getTableName( );
		if( sTable == null || "".equals( sTable ) ) {
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
		aiPKey[ 0 ] = findColumn( sPKCol );
		setKeyColumns( aiPKey );
	}

	private class SQLQuery {
		private String sQuery;
		private List< String > parmName = new ArrayList< String >( );
		private int iOperation = UNKNOWN;

		/**
		 * @param sQuery
		 *            the query definition
		 * @param iOperation
		 *            the sql operation
		 *            {@link org.homedns.mkh.databuffer.DataBuffer#INSERT},
		 *            {@link org.homedns.mkh.databuffer.DataBuffer#UPDATE},
		 *            {@link org.homedns.mkh.databuffer.DataBuffer#DELETE}
		 */
		public SQLQuery( String sQuery, int iOperation ) {
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
		 *         {@link org.homedns.mkh.databuffer.DataBuffer#INSERT},
		 *         {@link org.homedns.mkh.databuffer.DataBuffer#UPDATE},
		 *         {@link org.homedns.mkh.databuffer.DataBuffer#DELETE}
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
	 * Custom timestamp serializer
	 *
	 */
	private class TimestampSerializer implements JsonSerializer< Timestamp > {
		/**
		 * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
		 */
		@Override
		public JsonElement serialize( 
			Timestamp timestamp, Type type, JsonSerializationContext context 
		) {
			return( new JsonPrimitive( getEnvironment( ).getServerDateFormat( ).format( timestamp ) ) );
		}
	}
}
