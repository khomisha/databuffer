/*
 * Copyright 2014 Mikhail Khodonov
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

import java.util.List;
import javax.sql.RowSetMetaData;

/**
 * Meta data interface
 *
 */
public interface MetaData extends RowSetMetaData {

	/**
	 * Returns environment object
	 * 
	 * @return the environment
	 */
	public Environment getEnvironment( );

	/**
	 * Returns data buffer name
	 * 
	 * @return the data buffer name
	 */
	public String getDataBufferName( );

	/**
	 * Returns data buffer description object
	 * 
	 * @return the data buffer description object
	 */
	public DataBufferDesc getDescription( );

	/**
	 * Returns description as json string
	 * 
	 * @return the description as json string
	 */
	public String getDescriptionAsJson( );
	
	/**
	 * Returns columns list
	 * 
	 * @return the columns list
	 */
	public List< Column > getColList( ) throws InvalidDatabufferDesc;

	/**
	 * Returns updatable columns list
	 * 
	 * @return the updatable columns list
	 */
	public List< Column > getUpdatableCols( );

	/**
	 * Returns updatable column names list
	 * 
	 * @return the updatable column names list
	 */
	public List< String > getUpdatableColNames( );
}
