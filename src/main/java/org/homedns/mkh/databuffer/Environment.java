/*
 * Copyright 2012-2020 Mikhail Khodonov
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

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Data buffer environment interface
 *
 */
public interface Environment {
	public static final Locale DEFAULT_LOCALE = Locale.US;
	
	/**
	 * Returns server date format.
	 * 
	 * @return the server date format
	 */
	public default SimpleDateFormat getServerDateFormat( ) {
		return( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) );
	}

	/**
	 * Returns client date format.
	 * 
	 * @return the client date format
	 */
	public default SimpleDateFormat getClientDateFormat( ) {
		return( null );
	}

	/**
	* Returns transaction object
	* 
	* @return transaction object
	*/
	public DBTransaction getTransObject( );

	/**
	 * Returns data buffer description filename
	 * 
	 * @param sClassname
	 *            the data buffer class name
	 * 
	 * @return the data buffer description filename
	 */
	public String getDataBufferFilename( String sClassname );
	
	/**
	 * Returns locale
	 * 
	 * @return the locale
	 */
	public default Locale getLocale( ) {
		return( DEFAULT_LOCALE );
	}
}
