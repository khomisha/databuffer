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

import java.sql.Types;

/**
 * Data buffer data types
 *
 */
public enum Type {
	STRING( "STRING" ), BYTE( "BYTE" ), SHORT( "SHORT" ), INT( "INT" ), LONG( "LONG" ),
	TIMESTAMP( "TIMESTAMP" ), DOUBLE( "DOUBLE" ), FLOAT( "FLOAT" ), BOOLEAN( "BOOLEAN" );

	private String _sName;
	private int _iSQLType;
	private int _iLength;
	
	/**
	 * @param sName the type name
	 */
	private Type( String sName ) {
		_sName = sName;
		setSQLTypeAndLength( sName );
	}

	/**
	 * Returns type name
	 * 
	 * @return the type name
	 */
	public String getName( ) {
		return( _sName );
	}

	/**
	 * Returns sql type 
	 * @see java.sql.Types
	 * 
	 * @return the the sql type
	 */
	public int getSQLType( ) {
		return( _iSQLType );
	}
	
	/**
	 * Returns data type length in bytes, for STRING data type returns
	 * multiplier, actual length in bytes = multiplier * length in symbols
	 * 
	 * @return the length in bytes
	 */
	public int getLength( ) {
		return( _iLength );
	}

	/**
	 * Sets sql type and length in bytes for specified java data type
	 * 
	 * @param type
	 *            the java data type
	 */
	private void setSQLTypeAndLength( String sName ) {
		if( "STRING".equals( sName ) ) {
			_iSQLType = Types.VARCHAR;
			_iLength = 2;
		} else if( "BYTE".equals( sName ) ) {
			_iSQLType = Types.TINYINT;
			_iLength = 1;
		} else if( "SHORT".equals( sName ) ) {
			_iSQLType = Types.SMALLINT;
			_iLength = 2;
		} else if( "INT".equals( sName ) ) {
			_iSQLType = Types.INTEGER;
			_iLength = 4;
		} else if( "LONG".equals( sName ) ) {
			_iSQLType = Types.BIGINT;
			_iLength = 8;
		} else if( "TIMESTAMP".equals( sName ) ) {
			_iSQLType = Types.TIMESTAMP;
			_iLength = 8;
		} else if( "DOUBLE".equals( sName ) ) {
			_iSQLType = Types.DOUBLE;
			_iLength = 8;
		} else if( "FLOAT".equals( sName ) ) {
			_iSQLType = Types.FLOAT;
			_iLength = 8;
		} else if( "BOOLEAN".equals( sName ) ) {
			_iSQLType = Types.BOOLEAN;
			_iLength = 1;
		} 
	}
}
