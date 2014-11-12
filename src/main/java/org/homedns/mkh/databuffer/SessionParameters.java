/*
 * Copyright 2012-2014 Mikhail Khodonov
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

/**
* Database session parameters
*/
public interface SessionParameters {
	/**
	 * Login parameter
	 */
	public static final String CURRENT_LOGIN = "0";

	/**
	 * Sets DBMS specific parameters to the database session.
	 * 
	 * @param conn
	 *            the connection
	 * 
	 * @throws SQLException
	 */
	public void set2Session( Connection conn ) throws SQLException;
	
	/**
	 * Sets parameter name.
	 * 
	 * @param sKey
	 *            the parameter key
	 * @param sName
	 *            the parameter name to set
	 */
	public void setParameter( String sKey, String sName );
	
	/**
	 * Returns parameter name.
	 * 
	 * @param sKey
	 *            the parameter key
	 * 
	 * @return the parameter name or null if not found
	 */
	public String getParameter( String sKey );

	/**
	 * Sets parameter value.
	 * 
	 * @param sKey
	 *            the parameter key
	 * @param sValue
	 *            the parameter value to set
	 */
	public void setParameterValue( String sKey, String sValue );
	
	/**
	 * Returns parameter value.
	 * 
	 * @param sKey
	 *            the parameter key
	 * 
	 * @return the parameter value or null if not found
	 */
	public String getParameterValue( String sKey );
}
