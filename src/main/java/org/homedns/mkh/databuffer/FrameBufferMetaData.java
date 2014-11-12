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
	private List< Integer > _offset = new ArrayList< Integer >( );
	private int _iSize;

	/**
	 * @see org.homedns.mkh.databuffer.DataBufferMetaData#DataBufferMetaData(String, Environment)
	 */
	public FrameBufferMetaData( 
		String sDataBufferName, Environment env 
	) throws IOException, SQLException, StandardException, InvalidDatabufferDesc {
		super( sDataBufferName, env );
		_offset.add( 0 );
	}

	/**
	* Returns updatable column's offset in bytes.
	*
	* @return the columns offsets list
	*/
	public List< Integer > getOffset( ) {
		return( _offset );
	}

	/**
	* Returns updatable columns total size in bytes.
	*
	* @return the size in bytes
	*/
	public int getSize( ) {
		return( _iSize );
	}

	/**
	 * @see org.homedns.mkh.databuffer.DataBufferMetaData#onSetMetaData(org.homedns.mkh.databuffer.Column)
	 */
	@Override
	protected void onSetMetaData( 
		Column col 
	) throws SQLException, InvalidDatabufferDesc {
		int iOffset = _offset.get( _offset.size( ) - 1 ) + getColLen( col );
		_offset.add( iOffset );
		if( col.getColNum( ) == getColumnCount( ) - 1 ) {
			_iSize = _offset.get( _offset.size( ) - 1 );
			_offset.remove( _offset.size( ) - 1 );
		}
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
			if( iLimit == 0 ) {
				throw new InvalidDatabufferDesc( 
					Util.getBundle( getEnvironment( ).getLocale( ) ).getString( "invalidColLength" ) +
					" = 0: " + col.getName( )					
				);
			}
			iLen = iLen * iLimit;
		} else {
			iLen = ( iLimit == 0 ) ? iLen : iLimit;
		}
		return( iLen );
	}
}
