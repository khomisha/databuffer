/*
 * Copyright 2017-2018 Mikhail Khodonov
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

import java.sql.SQLException;

/**
 * Database connection unavailable exception
 *
 */
@SuppressWarnings( "serial" )
public class DBConnectionUnavailableException extends SQLException {

	/**
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method).
	 */
	public DBConnectionUnavailableException( Throwable cause ) {
		super( cause );
	}

	/**
	 * @param reason a description of the exception
	 */
	public DBConnectionUnavailableException( String reason ) {
		super( reason );
	}

	/**
	 * @param reason a description of the exception
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method).
	 */
	public DBConnectionUnavailableException( String reason, Throwable cause ) {
		super( reason, cause );
	}
}
