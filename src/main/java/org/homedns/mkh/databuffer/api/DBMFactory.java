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

import java.util.List;

/**
 * Data buffer manager factory
 *
 */
public class DBMFactory {

	/**
	 * Creates data buffer manager
	 * 
	 * @param type the DataBufferManager type
	 * @param dsl the data sources list
	 * 
	 * @return the data buffer manager instance
	 *  
	 * @throws Exception 
	 */
	public static DataBufferManager create( Class< ? extends DataBufferManager > type, List< GenericDataSource > dsl ) throws Exception {
		DataBufferManager dbm = type.newInstance( );
		for( GenericDataSource ds : dsl ) {
			dbm.addDataSource( ds );
		}
		return( dbm );
	}
}
