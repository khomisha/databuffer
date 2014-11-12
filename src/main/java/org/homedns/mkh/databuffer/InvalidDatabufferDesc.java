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

/**
 * Invalid data buffer description exception
 *
 */
@SuppressWarnings( "serial" )
public class InvalidDatabufferDesc extends Exception {

	/**
	 * @param msg
	 *            the detail message
	 */
    public InvalidDatabufferDesc( String msg ) {
        super( msg );
    }
    
	/**
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method).
	 */
    public InvalidDatabufferDesc( Throwable cause ) {
        super( cause );
    }
    
    /**
	 * @param msg
	 *            the detail message
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method).
     */
    public InvalidDatabufferDesc( String msg, Throwable cause ) {
        super( msg, cause );
    }
}
