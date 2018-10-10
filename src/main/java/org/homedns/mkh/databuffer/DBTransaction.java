/*
 * Copyright 2007-2017 Mikhail Khodonov
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
import java.util.Locale;

import javax.sql.DataSource;

/**
 * Database transaction object
 */
public class DBTransaction {
	private SessionParameters sessionParams;
	private DataSource dataSource;

	
	/**
	 * @param dataSource
	 *            the database data source
	 */
	public DBTransaction( DataSource dataSource ) {
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
		Connection conn = getConnection( );
		if( iAction != DataBuffer.RETRIEVE && sessionParams != null ) {
			sessionParams.set2Session( conn );
		}
		return( conn );
	}
	
	/**
	 * Returns connection.
	 * 
	 * @return the database connection
	 * 
	 * @throws SQLException
	 */
	public Connection getConnection( ) throws SQLException {
		try {
			return( dataSource.getConnection( ) );			
		}
		catch( SQLException e ) {
			throw new DBConnectionUnavailableException( 
				Util.getBundle( Locale.getDefault( ) ).getString( "connUnavailable" ), e 
			);
		}
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
}

