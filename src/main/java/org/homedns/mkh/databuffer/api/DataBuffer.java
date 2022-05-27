/* 
 * Copyright 2022 Mikhail Khodonov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.homedns.mkh.databuffer.api;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.rowset.WebRowSet;

import org.homedns.mkh.databuffer.Column;
import org.homedns.mkh.databuffer.DataBufferDesc;
import com.akiban.sql.StandardException;

/**
 * Data buffer interface
 *
 */
public interface DataBuffer extends AutoCloseable {

	/**
	 * Specifies delete SQL query 
	 */
	int DELETE = 2;
	/**
	 * Specifies insert SQL query 
	 */
	int INSERT = 0;
	/**
	 * Specifies JSON data format
	 */
	int JSON = 1;
	/**
	 * Emulates modification queries action 
	 */
	int PERFORM = 5;
	/**
	 * Specifies retrieve SQL query 
	 */
	int RETRIEVE = 3;
	/**
	 * Specifies serializable array data format
	 */
	int SERIALIZABLE_ARRAY = 2;
	/**
	 * Specifies serializable list data format
	 */
	int SERIALIZABLE_LIST = 3;
	/**
	 * Specifies undefined query 
	 */
	int UNKNOWN = 4;
	/**
	 * Specifies update SQL query 
	 */
	int UPDATE = 1;
	/**
	 * Specifies XML data format
	 */
	int XML = 0;

	/**
	 * Closes connection, typically this method should be called when server
	 * paging on and it's need manually close data buffer connection
	 * 
	 * @throws SQLException
	 */
	public void closeConn( ) throws SQLException;
	
	/**
	 * Returns data buffer records as string array.
	 * 
	 * @return the array of data or empty array if no records
	 * 
	 * @throws SQLException
	 */
	String[][] getData( ) throws SQLException;

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
	String[][] getData( List< Column > cols ) throws SQLException;

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
	 */
	String[][] getData( String[] asColName ) throws SQLException;

	/**
	 * Returns data buffer records as list.
	 * 
	 * @return the data list
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	List< List< Serializable > > getDataAsList( ) throws SQLException;

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
	List< List< Serializable > > getDataAsList( List< Column > cols ) throws SQLException;

	/**
	 * Returns selected columns data buffer records as list.
	 * 
	 * @param asColNames
	 *            the selected column name array which data will be move to
	 *            output list
	 * 
	 * @return the data list
	 * 
	 * @throws SQLException
	 */
	List< List< Serializable > > getDataAsList( String[] asColNames ) throws SQLException;

	/**
	 * Returns data buffer name
	 * 
	 * @return the data buffer name
	 */
	String getDataBufferName( );

	/**
	 * Returns data buffer description
	 * 
	 * @return the data buffer description
	 */
	DataBufferDesc getDescription( );

	/**
	 * Returns data buffer description in json format
	 * 
	 * @return the data buffer description in json format
	 */
	String getDescriptionAsJson( );

	/**
	 * Returns data buffer data as json string
	 * 
	 * @return the data buffer data as json string
	 * 
	 * @throws SQLException
	 */
	String getJson( ) throws SQLException;

	/**
	 * Returns current page number.
	 * 
	 * @return page number.
	 */
	int getPage( );
	
	/**
	 * Returns page size, {@see javax.sql.rowset.CachedRowSet#getPageSize()}
	 * 
	 * @return page size
	 */
	int getPageSize( );

	/**
	 * Returns parent web rowset
	 * 
	 * @return the web rowset
	 */
	public WebRowSet getParent( );

	/**
	 * Returns sql query execution result.
	 * 
	 * @return the sql query execution result.
	 */
	ArrayList< String > getReturnValue( );

	/**
	 * Returns number of rows.
	 * 
	 * @return number of rows
	 * 
	 * @throws SQLException
	 */
	int getRowCount( ) throws SQLException;

	/**
	 * Returns specified row data
	 * 
	 * @param iRow the row index
	 * 
	 * @return the row data
	 * 
	 * @throws SQLException
	 */
	Serializable[] getRowData( int iRow ) throws SQLException;

	/**
	 * Returns data buffer data as xml.
	 * 
	 * @return data as xml string or empty string
	 * 
	 * @throws SQLException, IOException
	 */
	String getXml( ) throws SQLException, IOException;

	/**
	 * Inserts data to the data buffer immediately following the
	 * current row.
	 * 
	 * @param data
	 *            the data to insert
	 * 
	 * @throws SQLException
	 */
	void insertData( List< List< Serializable > > data ) throws SQLException;

	/**
	 * Inserts data to the data buffer immediately following the
	 * current row.
	 * 
	 * @param data
	 *            the data to insert
	 * 
	 * @throws SQLException
	 */
	void insertData( Serializable[][] data ) throws SQLException;

	/**
	 * Inserts the data row into this data buffer immediately following the
	 * current row.
	 * 
	 * @param row
	 *            the data row to insert
	 * 
	 * @throws SQLException
	 */
	void insertDataRow( List< Serializable > row ) throws SQLException;

	/**
	 * @see com.sun.rowset.CachedRowSet#nextPage()
	 */
	boolean nextPage( ) throws SQLException;

	/**
	 * @see com.sun.rowset.CachedRowSet#previousPage()
	 */
	boolean previousPage( ) throws SQLException;

	/**
	 * Puts data as json string to the data buffer.
	 * 
	 * @param sJsonData
	 *            the data as json string to put
	 * 
	 * @throws SQLException, ParseException, IOException 
	 */
	void putJson( String sJsonData ) throws SQLException, ParseException, IOException;

	/**
	 * Puts data from xml string to the data buffer
	 * 
	 * @param sXml
	 *            the data as xml string to put
	 * 
	 * @throws SQLException
	 */
	void putXml( String sXml ) throws SQLException;

	/**
	 * Retrieves data from database to the data buffer.
	 * 
	 * @return number of retrieved rows
	 * 
	 * @throws SQLException
	 */
	int retrieve( ) throws SQLException;

	/**
	 * Retrieves data from database to the data buffer.
	 * 
	 * @param args
	 *            the retrieval arguments list
	 * 
	 * @return number of retrieved rows
	 * 
	 * @throws SQLException
	 */
	int retrieve( List< Serializable > args ) throws SQLException;

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
	 * @throws SQLException, StandardException
	 */
	int retrieve( List< Serializable > args, String sAddWhere ) throws SQLException, StandardException;

	/**
	 * Retrieves data from database to the data buffer.
	 * 
	 * @param sAddWhere
	 *            the additional conditions for the WHERE clause
	 * 
	 * @return number of retrieved rows
	 * 
	 * @throws SQLException, StandardException
	 */
	int retrieve( String sAddWhere ) throws SQLException, StandardException;

	/**
	 * Saves current data buffer row to the database.
	 * 
	 * @param iQueryType
	 *            the sql modification query type
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#INSERT},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#UPDATE},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#DELETE}
	 * 
	 * @throws SQLException
	 */
	void save( int iQueryType ) throws SQLException;

	/**
	 * Saves data in database.
	 * 
	 * @param iQueryType
	 *            the sql modification query type
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#INSERT},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#UPDATE},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#DELETE}
	 * @param iDataFormat
	 *            the data format
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#XML},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#JSON},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#SERIALIZABLE_ARRAY}
	 * @param data
	 *            the data to save
	 * 
	 * @throws Exception
	 */
	void save( int iQueryType, int iDataFormat, boolean bBatch, Object data ) throws Exception;

	/**
	 * Saves specified row
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
	public void save( int iQueryType, int iRow ) throws SQLException;

	/**
	 * Submits a batch of modifying commands to the database to save data buffer data.
	 * 
	 * @param iQueryType
	 *            the sql modification query type
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#INSERT},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#UPDATE},
	 *            {@link org.homedns.mkh.databuffer.DataBufferImpl#DELETE}
	 * 
	 * @throws SQLException
	 */
	void saveBatch( int iQueryType ) throws SQLException;

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
	Serializable toSQLType( String sValue, int iType ) throws ParseException;
}
