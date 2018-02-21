/*
 * Copyright 2013-2014 Mikhail Khodonov
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

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.RowSetMetaDataImpl;
import com.akiban.sql.StandardException;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * Data buffer metadata object
 *
 */
@SuppressWarnings( "serial" )
public class DataBufferMetaData extends RowSetMetaDataImpl implements MetaData {
	private DataBufferDesc _desc;
	private Environment _env;
	private List< Column > _updatableCols = new ArrayList< Column >( );
	List< String > _colUpdName = new ArrayList< String >( );
	private String _sDataBufferName; 
	
	/**
	 * @param sDataBufferName
	 *            the data buffer name
	 * @param env
	 *            the data buffer environment
	 * 
	 * @throws IOException
	 * @throws InvalidDatabufferDesc
	 */
	public DataBufferMetaData( 
		String sDataBufferName, 
		Environment env 
	) throws InvalidDatabufferDesc, IOException {
		_env = env;
		_sDataBufferName = sDataBufferName;
		JsonReader in = null;
		try {
			in = new JsonReader( new FileReader( env.getDataBufferFilename( sDataBufferName ) ) );
			_desc = new Gson( ).fromJson( in, DataBufferDesc.class );
			_desc.check( );
			setMetaData( );
		}
		catch( Exception e ) {
			InvalidDatabufferDesc ex = new InvalidDatabufferDesc( sDataBufferName );
			ex.initCause( e );
			throw ex;
		}
		finally {
			if( in != null ) {
				in.close( );
			}
		}		
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getColList()
	 */
	@Override
	public List< Column > getColList( ) throws InvalidDatabufferDesc {
		Column[] cols = _desc.getColumns( );
		return( Arrays.asList( cols ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getEnvironment()
	 */
	@Override
	public Environment getEnvironment( ) {
		return _env;
	}

	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getDataBufferName()
	 */
	@Override
	public String getDataBufferName( ) {
		return( _sDataBufferName );
	}

	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getDescription()
	 */
	@Override
	public DataBufferDesc getDescription( ) {
		return( _desc );
	}

	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getDescriptionAsJson()
	 */
	@Override
	public String getDescriptionAsJson( ) {
		Gson gson = new Gson( );
		return( gson.toJson( _desc ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getUpdatableCols()
	 */
	@Override
	public List< Column > getUpdatableCols( ) {
		return( _updatableCols );
	}

	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getUpdatableColNames()
	 */
	@Override
	public List< String > getUpdatableColNames( ) {
		return( _colUpdName );
	}
	
	/**
	 * Invokes on set data buffer metadata
	 * 
	 * @param col
	 *            the column
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc
	 */
	protected void onSetMetaData( Column col ) throws SQLException, InvalidDatabufferDesc {
	}
	
	/**
	 * Sets rowset metadata.
	 * 
	 * @throws StandardException 
	 * @throws IOException 
	 * @throws InvalidDatabufferDesc 
	 * @throws SQLException
	 */
	private void setMetaData( ) 
		throws SQLException, IOException, StandardException, InvalidDatabufferDesc 
	{
		int iCol = 0;
		String sTable = _desc.getTable( ).getUpdateTableName( );
		ArrayList< String > colNames = new ArrayList< String >( );
		setColumnCount( _desc.getColumns( ).length );
		for( Column col : _desc.getColumns( ) ) {
			col.setColNum( iCol );
			colNames.add( col.getName( ) );
			setColumnName( iCol + 1, col.getName( ) );
			setColumnType( iCol + 1, col.getType( ).getSQLType( ) );
			if( sTable != null && !"".equals( sTable ) ) {
				setTableName( iCol + 1, sTable );
			}
			setNullable( 
				iCol + 1, 
				col.isRequired( ) ? RowSetMetaData.columnNoNulls : RowSetMetaData.columnNullable 
			);
			onSetMetaData( col );
			if( col.isUpdate( ) ) {
				_colUpdName.add( col.getName( ) );
				_updatableCols.add( col );
			}
			if(	Column.DDDB.equals( col.getStyle( ) ) ) {
				setValues( col );
			}
			iCol++;
		}
		_desc.setColNames( colNames.toArray( new String[ colNames.size( ) ] ) );
	}

	/**
	 * Sets values for column with DDDB style.
	 * 
	 * @param col
	 *            the column
	 *            
	 * @throws StandardException 
	 * @throws SQLException 
	 * @throws IOException          
	 * @throws InvalidDatabufferDesc 
	 */
	private void setValues( 
		Column col 
	) throws InvalidDatabufferDesc, IOException, SQLException, StandardException {
		DataBuffer dddb = null;
		try {
			dddb = new DataBuffer( new DataBufferMetaData( col.getDDDBName( ), _env ) );
			dddb.retrieve( );
			String[] asColName = { col.getDisplayCol( ), col.getDataCol( ) };
			List< Value > values = new ArrayList< Value >( );
			for( String[] row : dddb.getData( asColName ) ) {
				Value value = new Value( );
				value.setDisplayValue( row[ 0 ] );
				value.setDataValue( row[ 1 ] );
				values.add( value );
			}
			col.setValues( values.toArray( new Value[ values.size( ) ] ) );
		}
		finally {
			if( dddb != null ) {
				dddb.close( );
			}
		}
	}
}
