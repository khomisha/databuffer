/*
 * Copyright 2013-2018 Mikhail Khodonov
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
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * Data buffer metadata object
 *
 */
@SuppressWarnings( "serial" )
public class DataBufferMetaData extends RowSetMetaDataImpl implements MetaData {
	private DataBufferDesc desc;
	private Environment env;
	private List< Column > updatableCols = new ArrayList< Column >( );
	List< String > colUpdName = new ArrayList< String >( );
	private String sDataBufferName; 
	
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
		this.env = env;
		this.sDataBufferName = sDataBufferName;
		try(
			JsonReader in = new JsonReader( 
				new FileReader( env.getDataBufferFilename( sDataBufferName ) ) 
			);
		) {
			desc = new Gson( ).fromJson( in, DataBufferDesc.class );
			desc.check( );
			setMetaData( );
		}
		catch( Exception e ) {
			InvalidDatabufferDesc ex = new InvalidDatabufferDesc( sDataBufferName );
			ex.initCause( e );
			throw ex;
		}
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getColList()
	 */
	@Override
	public List< Column > getColList( ) throws InvalidDatabufferDesc {
		Column[] cols = desc.getColumns( );
		return( Arrays.asList( cols ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getEnvironment()
	 */
	@Override
	public Environment getEnvironment( ) {
		return env;
	}

	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getDataBufferName()
	 */
	@Override
	public String getDataBufferName( ) {
		return( sDataBufferName );
	}

	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getDescription()
	 */
	@Override
	public DataBufferDesc getDescription( ) {
		return( desc );
	}

	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getDescriptionAsJson()
	 */
	@Override
	public String getDescriptionAsJson( ) {
		Gson gson = new Gson( );
		return( gson.toJson( desc ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getUpdatableCols()
	 */
	@Override
	public List< Column > getUpdatableCols( ) {
		return( updatableCols );
	}

	/**
	 * @see org.homedns.mkh.databuffer.MetaData#getUpdatableColNames()
	 */
	@Override
	public List< String > getUpdatableColNames( ) {
		return( colUpdName );
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
	 * @throws Exception 
	 */
	private void setMetaData( ) throws Exception {
		int iCol = 0;
		String sTable = desc.getTable( ).getUpdateTableName( );
		ArrayList< String > colNames = new ArrayList< String >( );
		setColumnCount( desc.getColumns( ).length );
		for( Column col : desc.getColumns( ) ) {
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
				colUpdName.add( col.getName( ) );
				updatableCols.add( col );
			}
			if(	Column.DDDB.equals( col.getStyle( ) ) ) {
				setValues( col );
			}
			iCol++;
		}
		desc.setColNames( colNames.toArray( new String[ colNames.size( ) ] ) );
	}

	/**
	 * Sets values for column with DDDB style.
	 * 
	 * @param col
	 *            the column
	 *            
	 * @throws Exception 
	 */
	private void setValues( Column col ) throws Exception {
		DataBuffer dddb = null;
		try {
			dddb = new DataBuffer( new DataBufferMetaData( col.getDDDBName( ), env ) );
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
