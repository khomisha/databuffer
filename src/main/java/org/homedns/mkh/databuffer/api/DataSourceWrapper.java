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

import javax.sql.DataSource;

/**
 * Data source wrapper
 *
 */
public class DataSourceWrapper implements GenericDataSource {
	private String sName;
	private DataSource ds;
	
	/**
	 * @param sName the datasource name
	 * @param ds the datasource
	 */
	public DataSourceWrapper( String sName, DataSource ds ) {
		this.sName = sName;
		this.ds = ds;
	}

	/**
	 * @see org.homedns.mkh.databuffer.api.GenericDataSource#getDataSource()
	 */
	@Override
	public DataSource getDataSource( ) {
		return( ds );
	}

	/**
	 * @see org.homedns.mkh.databuffer.api.GenericDataSource#getName()
	 */
	@Override
	public String getName( ) {
		return( sName );
	}
}
