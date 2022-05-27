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
 * The generic data source
 *
 */
public interface GenericDataSource {
	
	public DataSource getDataSource( );
	
	public default void setDataSource( DataSource ds ) {
	}
    
    /**
     * Sets data source name
     * 
     * @param sName the data source name to set
     */
    public default void setName( String sName ) {
    }
    
    /**
     * Returns data source name
     * 
     * @return the data source name
     */
    public String getName( );  
}
