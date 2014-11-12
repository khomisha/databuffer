/*
 * Copyright 2012-2014 Mikhail Khodonov
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
import java.sql.Timestamp;
import java.util.List;
import com.akiban.sql.StandardException;

/**
 * DataBuffer special edition to send/receive data as byte set
 *
 */
public class FrameBuffer extends DataBuffer {
	private static final long serialVersionUID = 4187887851482446439L;

	/**
	 * @param metaData the frame buffer meta data object
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws StandardException
	 * @throws InvalidDatabufferDesc
	 */
	public FrameBuffer( 
		MetaData metaData 
	) throws IOException, SQLException, StandardException, InvalidDatabufferDesc {
		super( metaData );
	}

	/**
	 * Puts frame into the data buffer.
	 * 
	 * @param abPacket
	 *            the input byte array
	 * @param bBigEndian
	 *            the big endian flag
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public void putFrame( 
		byte[] abPacket, 
		boolean bBigEndian 
	) throws SQLException, InvalidDatabufferDesc {
		FrameBufferMetaData metaData = ( FrameBufferMetaData )getMetaData( );
		Frame frame = new Frame( metaData.getOffset( ), abPacket, bBigEndian );
		moveToInsertRow( );
		List< String > colNames = metaData.getUpdatableColNames( );
		int iCol = 0;
		for( String sColName : colNames ) {
			Type type = metaData.getDescription( ).getColumn( sColName ).getType( );
			if( Type.STRING == type ) {
				updateString( sColName, frame.getString( iCol ) );
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
	 * Puts string of fields separated by a delimiter into the data buffer.
	 * 
	 * @param sLine
	 *            the input string
	 * @param sDelimiter
	 *            the field's delimiter
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public void putLine( 
		String sLine, 
		String sDelimiter 
	) throws SQLException, InvalidDatabufferDesc {
		String[] as = sLine.split( sDelimiter );
		FrameBufferMetaData metaData = ( FrameBufferMetaData )getMetaData( );
		List< String > colNames = metaData.getUpdatableColNames( );
		if( as.length < colNames.size( ) ) {
			throw new IllegalArgumentException( sLine );
		}
		moveToInsertRow( );
		int iCol = 0;
		for( String sColName : colNames ) {
			if( "".equals( as[ iCol ] ) ) {
				updateNull( sColName );
			} else {
				Type type = metaData.getDescription( ).getColumn( sColName ).getType( );
				if( Type.STRING == type ) {
					updateString( sColName, as[ iCol ] );
				} else if( Type.BYTE == type ) {
					updateByte( sColName, Byte.valueOf( as[ iCol ] ) );
				} else if( Type.SHORT == type ) {
					updateShort( sColName, Short.valueOf( as[ iCol ] ) );
				} else if( Type.INT == type ) {
					updateInt( sColName, Integer.valueOf( as[ iCol ] ) );
				} else if( Type.LONG == type ) {
					updateLong( sColName, Long.valueOf( as[ iCol ] ) );
				} else if( Type.TIMESTAMP == type ) {
					// input value must be in yyyy-mm-dd hh:mm:ss[.f...] format
					updateTimestamp( sColName, Timestamp.valueOf( as[ iCol ] ) );
				} else if( Type.DOUBLE == type ) {
					updateDouble( sColName, Double.valueOf( as[ iCol ] ) );
				} else if( Type.FLOAT == type ) {
					updateFloat( sColName, Float.valueOf( as[ iCol ] ) );
				}
			}
			iCol++;
		}
		insertRow( );
		moveToCurrentRow( );
		last( );
	}

	/**
	 * Returns current row (updatable columns only) as byte array.
	 * 
	 * @param bBigEndian
	 *            the big endian flag
	 * 
	 * @return byte array
	 * 
	 * @throws SQLException
	 * @throws InvalidDatabufferDesc 
	 */
	public byte[] getData( boolean bBigEndian ) throws SQLException, InvalidDatabufferDesc {
		int iCol = 0;
		FrameBufferMetaData metaData = ( FrameBufferMetaData )getMetaData( );
		Frame frame = new Frame( metaData.getOffset( ), metaData.getSize( ), bBigEndian );
		List< String > colNames = metaData.getUpdatableColNames( );
		for( String sColName : colNames ) {
			Type type = metaData.getDescription( ).getColumn( sColName ).getType( );
			if( Type.STRING == type ) {
				frame.setString( iCol, getString( sColName ) );
			} else if( Type.BYTE == type ) {
				frame.setByte( iCol, getByte( sColName ) );
			} else if( Type.SHORT == type ) {
				frame.setShort( iCol, getShort( sColName ) );
			} else if( Type.INT == type ) {
				frame.setInt( iCol, getInt( sColName ) );
			} else if( Type.LONG == type ) {
				frame.setLong( iCol, getLong( sColName ) );
			} else if( Type.TIMESTAMP == type ) {
				frame.setTimestamp( iCol, getTimestamp( sColName ) );
			} else if( Type.DOUBLE == type ) {
				frame.setDouble( iCol, getDouble( sColName ) );
			} else if( Type.FLOAT == type ) {
				frame.setFloat( iCol, getFloat( sColName ) );
			}
			iCol++;
		}
		return( frame.getBuffer( ).array( ) );
	}
	
	/**
	 * Returns empty frame.
	 * 
	 * @param bBigEndian
	 *            the big endian flag
	 * 
	 * @return frame
	 */
	public Frame getFrame( boolean bBigEndian ) {
		FrameBufferMetaData metaData = ( FrameBufferMetaData )getMetaData( );
		return( new Frame( metaData.getOffset( ), metaData.getSize( ), bBigEndian ) );
	}
}
