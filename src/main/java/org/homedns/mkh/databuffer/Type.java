/*
 * Copyright 2014-2018 Mikhail Khodonov
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

import java.sql.Types;

/**
 * Data buffer data types
 *
 */
public enum Type {
	/**
	 * HEXSTRING hexadecimal string contains hexadecimal digits symbols
	 * ASCIISTRING ascii string contains ascii character set symbols only 
	 */
	STRING( "STRING" ), BYTE( "BYTE" ), SHORT( "SHORT" ), INT( "INT" ), LONG( "LONG" ),
	TIMESTAMP( "TIMESTAMP" ), DOUBLE( "DOUBLE" ), FLOAT( "FLOAT" ), BOOLEAN( "BOOLEAN" ),
	HEXSTRING( "HEXSTRING" ), ASCIISTRING( "ASCIISTRING" );

	private String sName;
	private int iSQLType;
	private int iLength;
	
	/**
	 * @param sName the type name
	 */
	private Type( String sName ) {
		this.sName = sName;
		setSQLTypeAndLength( sName );
	}

	/**
	 * Returns type name
	 * 
	 * @return the type name
	 */
	public String getName( ) {
		return( sName );
	}

	/**
	 * Returns sql type 
	 * @see java.sql.Types
	 * 
	 * @return the the sql type
	 */
	public int getSQLType( ) {
		return( iSQLType );
	}
	
	/**
	 * Returns data type length in bytes, for STRING data type returns 0 because
	 * the length in bytes depends on string length in chars, content and
	 * encoding, for HEXSTRING and ASCIISTRING data types returns 0 because the
	 * length in bytes equals number of chars in hex string.
	 * 
	 * @return the length in bytes
	 */
	public int getLength( ) {
		return( iLength );
	}

	/**
	 * Sets sql type and length in bytes for specified java data type
	 * 
	 * @param sName the type name
	 */
	private void setSQLTypeAndLength( String sName ) {
		if( "STRING".equals( sName ) ) {
			iSQLType = Types.VARCHAR;
			iLength = 0;
		} else if( "HEXSTRING".equals( sName ) ) {
			iSQLType = Types.VARCHAR;
			iLength = 0;
		} else if( "ASCIISTRING".equals( sName ) ) {
			iSQLType = Types.VARCHAR;
			iLength = 0;
		} else if( "BYTE".equals( sName ) ) {
			iSQLType = Types.TINYINT;
			iLength = 1;
		} else if( "SHORT".equals( sName ) ) {
			iSQLType = Types.SMALLINT;
			iLength = 2;
		} else if( "INT".equals( sName ) ) {
			iSQLType = Types.INTEGER;
			iLength = 4;
		} else if( "LONG".equals( sName ) ) {
			iSQLType = Types.BIGINT;
			iLength = 8;
		} else if( "TIMESTAMP".equals( sName ) ) {
			iSQLType = Types.TIMESTAMP;
			iLength = 8;
		} else if( "DOUBLE".equals( sName ) ) {
			iSQLType = Types.DOUBLE;
			iLength = 8;
		} else if( "FLOAT".equals( sName ) ) {
			iSQLType = Types.FLOAT;
			iLength = 8;
		} else if( "BOOLEAN".equals( sName ) ) {
			iSQLType = Types.BOOLEAN;
			iLength = 1;
		} 
	}
}
