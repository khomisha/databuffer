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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.RowSetMetaDataImpl;
import org.apache.log4j.Logger;
import org.homedns.mkh.databuffer.api.Context;
import org.homedns.mkh.databuffer.api.DataBuffer;

/**
 * Data buffer's description properties object. Data buffer description is plain text file in JSON format.
 * Description example:
 * <p>
 * <pre>
 * {
 *   "name": "test_mon",                 - data buffer name
 *   "title": "test_mon",                - data buffer title
 *   "table": {                          - table section
 *       "updateTableName": "test_mon",  - update table name or stored procedure call in
 *                                         following format "? = call save", necessary parameters:
 *                                         return value, action id (update, delete, insert) and updatable 
 *                                         column list will be added dynamically at data buffer constructor time
 *       "select": "SELECT               - query
 *           tem_id,
 *           tem_packet_type,
 *           tem_client_id,
 *           tem_timestamp,
 *           tem_packet_id
 *        FROM
 *           test_mon
 *        WHERE
 *        	tem_packet_type = ? and
 *        	tem_timestamp = ?
 *       ",
 *       "key":"tem_id",                 - primary key column
 *       "rowCountColumn": "",           - query column name which returns query row count
 *       "pageSize": 18,                 - if rowCountColumn is not null and query defines this special 
 *                                         column it specifies how many rows have to be retrieved at 
 *                                         a time from database {@link org.homedns.mkh.databuffer.DataBuffer#setPageSize(Integer)}
 *                                         otherwise this is indicator for client side - rows number displayed 
 *                                         on the page, default is 0 (optional)
 *       "argType": [                    - obsolete, query arguments data types {@link org.homedns.mkh.databuffer.Type}
 *       	"INT",
 *       	"TIMESTAMP"
 *       ],
 *       "reportData": "1,10"            - if this data buffer is use for reporting it indicates 
 *                                         start column and row indexes in excel sheet template where data buffer
 *                                         data should be inserted otherwise empty string (optional)
 *   },
 *   "columns": [                        - column section
 *        {
 *           "name": "tem_id",           - column name
 *           "caption": "tem_id",        - column caption
 *           "dbName": "test_mon.tem_id",- database column name
 *           "type": "INT",              - column type
 *           "update": false,            - updatable column flag
 *           "validationRule": "",       - column validation rule (regex expression), it's evaluate when data inputs, true - valid, false - invalid data
 *           "validationMsg": "",        - column validation message
 *           "style": "edit",            - column style specifies column data presentation, i.e.
 *                                           ddlb - dropdown listbox,
 *                                           dddb - dropdown databuffer nested (child) databuffer,
 *                                           checkbox,
 *                                           radiobutton,
 *                                           pwd - password,
 *                                           edit_date - only date,
 *                                           edit_time - only time, 
 *                                           edit_ts - timestamp format, 
 *                                           edit - the default style depending on data type,
 *                                           empty string for nonvisual columns
 *           "pattern": "",              - column format pattern (optional) @see com.google.gwt.i18n.client.NumberFormat
 *           "limit": 0,                 - allowable column length in characters, 0 means no limit
 *           "width": 0,				 - column width in pixels, 0 means undefined
 *           "required": true,           - mandatory flag (true|false)
 *           "mask": "",                 - the keystroke filter mask to be applied to the column input value type (regex expression),
 *           "values": [""],             - array contains pairs display value - actual value,
 *                                         applied only for dddb, ddlb, checkbox and radiobutton styles.
 *           "dddbName": "",             - dropdown databuffer name
 *           "dddbDisplayColumn": "",    - display column name
 *           "dddbDataColumn": "",       - data column name
 *           "reportParam" : "",         - indicates cell (column,row) in excel sheet template where column value 
 *                                         should be inserted otherwise empty string. Column value must be scalar
 *                                         for given result set
 *           "argument": false			 - argument flag (true|false), default false, 
 *           							   it specifies that the value in this column is used as a retrieve argument in non GUI applications
 *       },
 *       ......
 *       {
 *           "name": "tem_packet_type",
 *           "caption": "tem_packet_type",
 *           "dbName": "test_mon.tem_packet_type
 *           "type": "INT",
 *           "update": false,
 *           "validationRule": "",
 *           "validationMsg": "",
 *           "style": "ddlb",
 *           "limit": 0,
 *           "required": true,
 *           "mask": "",
 *           "values":[
 *           	{ "displayValue": "event", "dataValue": 1 },
 *           	{ "displayValue": "command", "dataValue": 2 },
 *           	{ "displayValue": "event with params", "dataValue": 3 }
 *           ],
 *           "dddbName": "packet_type",
 *           "dddbDisplayColumn": "pct_name",
 *           "dddbDataColumn": "pct_id",
 *           "reportParam" : "2,3",
 *           "argument": true
 *      }
 *   ]
 * }
 * </pre>
 */
public class DataBufferDesc implements Serializable {
	private static final long serialVersionUID = -8033236221812557557L;
	private static final Logger LOG = Logger.getLogger( DataBufferDesc.class );
	
	private String name;
	private String title;
	private Table table;
	private Column[] columns;
	private List< String > colNames;
	private transient RowSetMetaDataImpl metaData;
	private transient List< String > updatableColNames;
	
	public DataBufferDesc( ) {
	}
	
	/**
	 * Inits data buffer description object
	 * 
	 * @param context the context
	 * 
	 * @throws Exception
	 */
	public void init( Context context ) throws Exception {
		check( );
		colNames = new ArrayList< >( getColumns( ).length );
		updatableColNames = new ArrayList< >( );
		metaData = new RowSetMetaDataImpl( );
		metaData.setColumnCount( getColumns( ).length );
		int iCol = 0;
		for( Column col : getColumns( ) ) {
			col.setColNum( iCol );
			colNames.add( col.getName( ) );
			if( col.isUpdate( ) ) {
				updatableColNames.add( col.getName( ) );
			}
			metaData.setColumnName( iCol + 1, col.getName( ) );
			metaData.setColumnType( iCol + 1, col.getType( ).getSQLType( ) );
			metaData.setTableName( iCol + 1, getTable( ).getUpdateTableName( ) );
			metaData.setNullable( 
				iCol + 1, 
				col.isRequired( ) ? RowSetMetaData.columnNoNulls : RowSetMetaData.columnNullable 
			);
			if(	Column.DDDB.equals( col.getStyle( ) ) ) {
				try( DataBuffer dddb = context.getDataBuffer( col.getDDDBName( ) ) ) {
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
			}
			iCol++;
		}
	}
	
	/**
	 * Returns data buffer name
	 * 
	 * @return the data buffer name
	 */
	public String getName( ) {
		if( name == null || "".equals( name ) ) {
			throw new IllegalArgumentException( "no data buffer name" );
		}
		return( name );
	}
	
	/**
	 * Sets data buffer name
	 * 
	 * @param name
	 *            the data buffer name to set
	 */
	public void setName( String name ) {
		this.name = name;
	}
	
	/**
	 * Returns data buffer title
	 * 
	 * @return the data buffer title
	 */
	public String getTitle( ) {
		return( title );
	}
	
	/**
	 * Sets data buffer title
	 * 
	 * @param title
	 *            the data buffer title to set
	 */
	public void setTitle( String title ) {
		this.title = title;
	}
	
	/**
	 * Returns data buffer table
	 * 
	 * @return the data buffer table
	 */
	public Table getTable( ) {
		if( table == null ) {
			throw new IllegalArgumentException( name + ": no table section" );
		}
		return( table );
	}

	/**
	 * Sets data buffer table
	 * 
	 * @param table
	 *            the data buffer table to set
	 */
	public void setTable( Table table ) {
		this.table = table;
	}
	
	/**
	 * Returns data buffer columns
	 * Don't change columns order defined in json description file
	 * 
	 * @return the data buffer columns
	 */
	public Column[] getColumns( ) {
		if( columns == null || columns.length < 1 ) {
			throw new IllegalArgumentException( name + ": no columns section" );
		}
		return( columns );
	}
	
	/**
	 * Sets data buffer columns
	 * 
	 * @param columns the data buffer columns to set
	 */
	public void setColumns( Column[] columns ) {
		this.columns = columns;
	}
	
	/**
	 * Returns column names list
	 * 
	 * @return the column names list
	 */
	public List< String > getColNames( ) {
		return( colNames );
	}
	
	/**
	 * @param colNames the column names to set
	 */
	public void setColNames( List< String > colNames ) {
		this.colNames = colNames;
	}

	/**
	 * Returns updatable column names list
	 * 
	 * @return the updatable column names list
	 */
	public List< String > getUpdatableColNames( ) {
		return( updatableColNames );
	}

	/**
	 * Returns column by it's name
	 * 
	 * @param colName
	 *            the column name
	 * 
	 * @return the column
	 */
	public Column getColumn( String colName ) {
		int iIndex = colNames.indexOf( colName );
		if( iIndex == -1 ) {
			throw new IllegalArgumentException( name + ": column doesn't exist: " + colName );
		}
		return( columns[ iIndex ] );
	}
	
	/**
	 * Returns rowset metadata 
	 * 
	 * @return the rowset metadata
	 */
	public RowSetMetaDataImpl getMetaData( ) {
		return( metaData );
	}

	/**
	 * Checks data buffer description incompleteness
	 * 
	 * @throws Exception
	 */
	private void check( ) throws Exception {
		getName( );
		getTable( );
		table.getQuery( );
		table.getPKcol( );
		String sUptadeTable = table.getUpdateTableName( );
		getColumns( );
		for( Column col : columns ) {
			col.getName( );
			col.getDBName( );
			col.getType( );
			col.getStyle( );
			if( col.isUpdate( ) && ( sUptadeTable == null || "".equals( sUptadeTable ) ) ) {
				LOG.warn( "Updatable column " + col.getName( ) + " and no update table" );								
			}
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return "DataBufferDesc [name=" + name + ", title=" + title + ", table=" + table + ", columns="
			+ Arrays.toString( columns ) + "]";
	}
}

