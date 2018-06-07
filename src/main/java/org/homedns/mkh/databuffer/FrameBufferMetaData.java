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

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.akiban.sql.StandardException;

/**
 * Frame buffer metadata object
 *
 */
@SuppressWarnings( "serial" )
public class FrameBufferMetaData extends DataBufferMetaData {
	private List< Integer > colsLength;

	/**
	 * @see org.homedns.mkh.databuffer.DataBufferMetaData#DataBufferMetaData(String, Environment)
	 */
	public FrameBufferMetaData( 
		String sDataBufferName, Environment env 
	) throws IOException, SQLException, StandardException, InvalidDatabufferDesc {
		super( sDataBufferName, env );
		colsLength = new ArrayList< Integer >( );
	}

	/**
	* Returns updatable column's list of lengths in bytes.
	*
	* @return the column's list of lengths
	*/
	public List< Integer > getColsLength( ) {
		return( colsLength );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBufferMetaData#onSetMetaData(org.homedns.mkh.databuffer.Column)
	 */
	@Override
	protected void onSetMetaData( Column col ) throws SQLException, InvalidDatabufferDesc {
		colsLength.add( getColLen( col ) );
	}

	/**
	 * Returns column's length in bytes.
	 * 
	 * @param col
	 *            the column
	 * 
	 * @return column's length in bytes
	 * 
	 * @throws InvalidDatabufferDesc
	 */
	private int getColLen( Column col ) throws InvalidDatabufferDesc {
		Type type = col.getType( );
		int iLimit = col.getLimit( );
		Integer iLen = type.getLength( );
		if( type == Type.STRING ) {
			throw new InvalidDatabufferDesc( 
				Util.getBundle( getEnvironment( ).getLocale( ) ).getString( "unsupportedType" ) +
				": STRING"					
			);
		} else if( type == Type.HEXSTRING || type == Type.ASCIISTRING ) {
			iLen = iLimit;
		}
		return( iLen );
	}
}
