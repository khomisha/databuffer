/*
 * Copyright 2011-2014 Mikhail Khodonov
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
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import com.google.gson.annotations.SerializedName;

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
 *       "argType": [                    - query arguments data types {@link org.homedns.mkh.databuffer.Type}
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
 *           "pattern": "",              - column format pattern (optional)
 *           "limit": 0,                 - column length in bytes depends on column type	{@link Type}, for {@link Type#STRING} length calculates as 2 * limit
 *           "required": true,           - mandatory flag (true|false)
 *           "mask": "",                 - the keystroke filter mask to be applied to the column input value type (regex expression),
 *                                         Applied only for dddb, ddlb, checkbox and radiobutton styles.
 *           "values": [""],             - array contains pairs display value - actual value
 *           "dddbName": "",             - dropdown databuffer name
 *           "dddbDisplayColumn": "",    - display column name
 *           "dddbDataColumn": "",       - data column name
 *           "reportParam" : ""          - indicates cell (column,row) in excel sheet template where column value 
 *                                         should be inserted otherwise empty string. Column value must be scalar
 *                                         for given result set
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
 *           "reportParam" : "2,3"
 *      }
 *   ]
 * }
 * </pre>
 */
public class DataBufferDesc implements Serializable {
	private static final long serialVersionUID = 5105992519051332303L;
	private static final Logger LOG = Logger.getLogger( DataBufferDesc.class );
	
	@SerializedName( "name" ) private String _sName;
	@SerializedName( "title" ) private String _sTitle;
	@SerializedName( "table" ) private Table _table;
	@SerializedName( "columns" ) private Column[] _cols;
	@SerializedName( "colsNames" ) private String[] _asColName;
	
	public DataBufferDesc( ) {
	}
	
	/**
	 * Returns data buffer name
	 * 
	 * @return the data buffer name
	 * 
	 * @throws InvalidDatabufferDesc
	 */
	public String getName( ) throws InvalidDatabufferDesc {
		if( _sName == null || "".equals( _sName ) ) {
			throw new InvalidDatabufferDesc( "no data buffer name" );
		}
		return( _sName );
	}
	
	/**
	 * Sets data buffer name
	 * 
	 * @param sName
	 *            the data buffer name to set
	 */
	public void setName( String sName ) {
		_sName = sName;
	}
	
	/**
	 * Returns data buffer title
	 * 
	 * @return the data buffer title
	 */
	public String getTitle( ) {
		return( _sTitle );
	}
	
	/**
	 * Sets data buffer title
	 * 
	 * @param sTitle
	 *            the data buffer title to set
	 */
	public void setTitle( String sTitle ) {
		_sTitle = sTitle;
	}
	
	/**
	 * Returns data buffer table
	 * 
	 * @return the data buffer table
	 * 
	 * @throws InvalidDatabufferDesc 
	 */
	public Table getTable( ) throws InvalidDatabufferDesc {
		if( _table == null ) {
			throw new InvalidDatabufferDesc( "no table" );
		}
		return( _table );
	}

	/**
	 * Sets data buffer table
	 * 
	 * @param table
	 *            the data buffer table to set
	 */
	public void setTable( Table table ) {
		_table = table;
	}
	
	/**
	 * Returns data buffer columns
	 * Don't change columns order defined in json description file
	 * 
	 * @return the data buffer columns
	 * 
	 * @throws InvalidDatabufferDesc 
	 */
	public Column[] getColumns( ) throws InvalidDatabufferDesc {
		if( _cols == null || _cols.length < 1 ) {
			throw new InvalidDatabufferDesc( "no columns" );
		}
		return( _cols );
	}
	
	/**
	 * Sets data buffer columns
	 * 
	 * @param cols the data buffer columns to set
	 */
	public void setColumns( Column[] cols ) {
		_cols = cols;
	}
	
	/**
	 * Returns column names array
	 * 
	 * @return the column names array
	 */
	public String[] getColNames( ) {
		return( _asColName );
	}

	/**
	 * Sets column names array
	 * 
	 * @param asColName
	 *            the column names array to set
	 */
	public void setColNames( String[] asColName ) {
		_asColName = asColName;
	}

	/**
	 * Returns column by it's name
	 * 
	 * @param sColName
	 *            the column name
	 * 
	 * @return the column
	 * 
	 * @throws InvalidDatabufferDesc 
	 */
	public Column getColumn( String sColName ) throws InvalidDatabufferDesc {
		List< String > colList = Arrays.asList( _asColName );
		int iIndex = colList.indexOf( sColName );
		if( iIndex == -1 ) {
			throw new InvalidDatabufferDesc( "column doesn't exist: " + sColName );
		}
		return( _cols[ iIndex ] );
	}
	
	/**
	 * Checks data buffer description incompleteness
	 * 
	 * @throws InvalidDatabufferDesc
	 */
	public void check( ) throws InvalidDatabufferDesc {
		String sUpdateTable = _table.getUpdateTableName( );
		if( "".equals( sUpdateTable ) ) {
			LOG.warn( getName( ) + ": no update table" );
		}
		for( Column col : _cols ) {
			String sColDBName = col.getDBName( );
			if( "".equals( sColDBName ) ) {
				LOG.warn( 
					getName( ) + ": " + col.getName( ) + ": no database column name" 
				);				
			}
			if( !"".equals( sUpdateTable ) && !sColDBName.contains( sUpdateTable ) ) {
				LOG.warn( 
					getName( ) + ": " + col.getName( ) + ": wrong update table" 
				);								
			}
		}
	}
}

