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

package org.homedns.mkh.databuffer;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;
import org.homedns.mkh.databuffer.api.DataBuffer;
import org.homedns.mkh.databuffer.api.SessionParameters;

/**
 * Database connection object
 *
 */
public class DBConnection implements DataSource {
	private SessionParameters sessionParams;
	private DataSource dataSource;

	/**
	 * @param dataSource the data source
	 */
	public DBConnection( DataSource dataSource ) {
		this.dataSource = dataSource;
	}

	/**
	 * Returns connection.
	 * 
	 * @param iAction
	 *            the action (retrieve, insert, update, delete)
	 * 
	 * @return the database connection
	 * 
	 * @throws SQLException
	 */
	public Connection getConnection( int iAction ) throws SQLException {
		Connection conn = dataSource.getConnection( );
		if( iAction != DataBuffer.RETRIEVE && sessionParams != null ) {
			sessionParams.set2Session( conn );
		}
		return( conn );
	}
	
	/**
	 * Sets database session parameters object.
	 * 
	 * @param sessionParms
	 *            the database session parameters object to set
	 */
	public void setSessionParameters( SessionParameters sessionParms ) {
		this.sessionParams = sessionParms;
	}
	
	/**
	 * Returns database session parameters object.
	 * 
	 * @return the database session parameters object
	 */
	public SessionParameters getSessionParameters( ) {
		return( sessionParams );
	}

	/**
	 * Returns backend datasource
	 * 
	 * @return the datasource
	 */
	public DataSource getDataSource( ) {
		return( dataSource );
	}

	/**
	 * @see javax.sql.CommonDataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter( ) throws SQLException {
		return dataSource.getLogWriter( );
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	public < T > T unwrap( Class< T > iface ) throws SQLException {
		return dataSource.unwrap( iface );
	}

	/**
	 * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter( PrintWriter out ) throws SQLException {
		dataSource.setLogWriter( out );
	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor( Class< ? > iface ) throws SQLException {
		return dataSource.isWrapperFor( iface );
	}

	/**
	 * @see javax.sql.DataSource#getConnection()
	 */
	public Connection getConnection( ) throws SQLException {
		return dataSource.getConnection( );
	}

	/**
	 * @see javax.sql.CommonDataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout( int seconds ) throws SQLException {
		dataSource.setLoginTimeout( seconds );
	}

	/**
	 * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	public Connection getConnection( String username, String password ) throws SQLException {
		return dataSource.getConnection( username, password );
	}

	/**
	 * @see javax.sql.CommonDataSource#getLoginTimeout()
	 */
	public int getLoginTimeout( ) throws SQLException {
		return dataSource.getLoginTimeout( );
	}

	/**
	 * @see javax.sql.CommonDataSource#getParentLogger()
	 */
	public Logger getParentLogger( ) throws SQLFeatureNotSupportedException {
		return dataSource.getParentLogger( );
	}
}
