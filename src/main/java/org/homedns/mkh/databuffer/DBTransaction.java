/*
 * Copyright 2007-2014 Mikhail Khodonov
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

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Transaction object for database connection
 */
public class DBTransaction {
	private SessionParameters _sessionParams;
	private DataSource _dataSource;

	
	/**
	 * @param dataSource
	 *            the database data source to connect
	 */
	public DBTransaction( DataSource dataSource ) {
		_dataSource = dataSource;
	}

	/**
	 * Opens connection.
	 * 
	 * @param iAction
	 *            the action (retrieve, insert, update, delete)
	 * 
	 * @return the database connection
	 * 
	 * @throws SQLException
	 */
	public Connection getConnection( int iAction ) throws SQLException {
		Connection conn = _dataSource.getConnection( );
		if( iAction != DataBuffer.RETRIEVE && _sessionParams != null ) {
			_sessionParams.set2Session( conn );
		}
		return( conn );
	}
	
	/**
	 * Opens connection.
	 * 
	 * @return the database connection
	 * 
	 * @throws SQLException
	 */
	public Connection getConnection( ) throws SQLException {
		return( _dataSource.getConnection( ) );
	}

	/**
	 * Sets database session parameters object.
	 * 
	 * @param sessionParms
	 *            the database session parameters object to set
	 */
	public void setSessionParameters( SessionParameters sessionParms ) {
		_sessionParams = sessionParms;
	}
	
	/**
	 * Returns database session parameters object.
	 * 
	 * @return the database session parameters object
	 */
	public SessionParameters getSessionParameters( ) {
		return( _sessionParams );
	}
}

