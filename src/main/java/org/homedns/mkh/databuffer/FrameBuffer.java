/*
 * Copyright 2012-2018 Mikhail Khodonov
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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

/**
 * DataBuffer special edition to send/receive data as byte set
 *
 */
public class FrameBuffer extends DataBuffer {
	private static final long serialVersionUID = 4187887851482446439L;

	/**
	 * @param metaData the frame buffer meta data object
	 * 
	 * @throws Exception
	 */
	public FrameBuffer( MetaData metaData ) throws Exception {
		super( metaData );
	}

	/**
	 * Puts frame into the frame buffer.
	 * 
	 * @param abPacket
	 *            the input byte array
	 * @param bBigEndian
	 *            the bigendian flag
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public void putFrame( byte[] abPacket, boolean bBigEndian ) throws SQLException, InvalidDatabufferDesc {
		FrameBufferMetaData metaData = ( FrameBufferMetaData )getMetaData( );
		Frame frame = new Frame( metaData.getColsLength( ), abPacket, bBigEndian );
		moveToInsertRow( );
		List< String > colNames = metaData.getUpdatableColNames( );
		int iCol = 0;
		for( String sColName : colNames ) {
			Type type = metaData.getDescription( ).getColumn( sColName ).getType( );
			if( Type.HEXSTRING == type ) {
				updateString( sColName, frame.getHexString( iCol ) );
			} else if( Type.ASCIISTRING == type ) {
				updateString( sColName, frame.getAsciiString( iCol ) );
			} else if( Type.BYTE == type ) {
				updateByte( sColName, frame.getByte( iCol ) );
			} else if( Type.SHORT == type ) {
				updateShort( sColName, frame.getShort( iCol ) );
			} else if( Type.INT == type ) {
				updateInt( sColName, frame.getInt( iCol ) );
			} else if( Type.LONG == type ) {
				updateLong( sColName, frame.getLong( iCol ) );
			} else if( Type.TIMESTAMP == type ) {
				updateTimestamp( sColName, frame.getTimestamp( iCol ) );
			} else if( Type.DOUBLE == type ) {
				updateDouble( sColName, frame.getDouble( iCol ) );
			} else if( Type.FLOAT == type ) {
				updateFloat( sColName, frame.getFloat( iCol ) );
			}
			iCol++;
		}
		insertRow( );
		moveToCurrentRow( );
		last( );
	}
	
	/**
	 * Puts string of fields separated by a delimiter into the frame buffer.
	 * 
	 * @param sLine
	 *            the input string
	 * @param sDelimiter
	 *            the field's delimiter
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public void putLine( String sLine, String sDelimiter ) throws SQLException, InvalidDatabufferDesc {
		String[] as = sLine.split( sDelimiter );
		insertData( new Serializable[][] { as } );
	}
}
