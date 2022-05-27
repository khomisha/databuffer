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

import java.nio.file.Path;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.sql.DataSource;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

/**
 * Data buffer manager
 *
 */
public interface DataBufferManager {
	public static final Locale DEFAULT_LOCALE = Locale.US;
	public static final SimpleDateFormat SERVER_DATE_FMT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	public static final String DEFAULT_DATASOURCE_NAME = "default";
	
	/**
	 * Returns rowset factory
	 * 
	 * @return the rowset factory
	 * 
	 * @throws SQLException
	 */
	public static RowSetFactory getRowSetFactory( ) throws SQLException {
		return( RowSetProvider.newFactory( ) );
	}
	
	/**
	 * Adds data source
	 * 
	 * @param ds the data source to add
	 */
	public void addDataSource( GenericDataSource ds );
	
	/**
	 * Returns specified data source
	 * 
	 * @param sName the data source name
	 * 
	 * @return the data source
	 */
	public DataSource getDataSource( String sName );
	
	/**
	 * Returns data buffer with using default data source 
	 * 
	 * @param sName the target data buffer name
	 * 
	 * @return the data buffer
	 * 
	 * @throws Exception
	 */
	public DataBuffer getDataBuffer( String sName ) throws Exception;

	/**
	 * Returns data buffer with specified data source
	 * 
	 * @param sName the target data buffer name
	 * @param sDataSourceName the data source name
	 * 
	 * @return the data buffer
	 * 
	 * @throws Exception
	 */
	public DataBuffer getDataBuffer( String sName, String sDataSourceName ) throws Exception;
	
	/**
	 * Sets locale
	 * 
	 * @param locale the locale to set
	 */
	public default void setLocale( Locale locale ) {
	}

	/**
	 * Returns locale
	 * 
	 * @return the locale
	 */
	public Locale getLocale( );
	
	/**
	 * Sets resource
	 * 
	 * @param path
	 *            the resource path to set
	 * @param bResource
	 *            the resource type flag, if true, then the resource is within
	 *            the executable jar, otherwise file
	 * @param type
	 *            the class relative to the package of which the path to the
	 *            resource is determined, if bResource false, then this
	 *            parameter is ignored
	 */
	public void setResource( Path path, boolean bResource, Class< ? > type );
}
