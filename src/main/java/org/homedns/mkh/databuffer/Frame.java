/*
 * Copyright 2011-2014 Mikhail Khodonov
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.util.List;

/**
 * Frame object to send/receive data as byte set to/from remote agents.
 */
public class Frame {
	private List< Integer > _fieldsOffsets;
	private ByteBuffer _bb;

	/**
	 * @param fieldsOffsets
	 *            the fields offsets within byte buffer
	 * @param abPacket
	 *            the input byte array
	 * @param bBigEndian
	 *            the big endian flag
	 */
	public Frame( List< Integer > fieldsOffsets, byte[] abPacket, boolean bBigEndian ) {
		_fieldsOffsets = fieldsOffsets;
		setBuffer( ByteBuffer.wrap( abPacket ) );
		setOrder( bBigEndian );
	}

	/**
	 * @param fieldsOffsets
	 *            the fields offsets with byte buffer
	 * @param iSize
	 *            the frame size in bytes
	 * @param bBigEndian
	 *            the big endian flag
	 */
	public Frame( List< Integer > fieldsOffsets, int iSize, boolean bBigEndian ) {
		_fieldsOffsets = fieldsOffsets;
		setBuffer( ByteBuffer.allocate( iSize ) );
		setOrder( bBigEndian );
	}

	/**
	* Returns frame fields values as byte buffer.
	*
	* @return byte buffer
	*/
	public ByteBuffer getBuffer( ) {
		return( _bb );
	}

	/**
	 * Sets byte buffer with frame fields values.
	 * 
	 * @param bb
	 *            the byte buffer to set
	 */
	public void setBuffer( ByteBuffer bb ) {
		_bb = bb;
	}

	/**
	* Returns bytes order in frame.
	*
	* @return true if big endian and false if little endian
	*/
	public boolean isBigEndian( ) {
		return( _bb.order( ) == ByteOrder.BIG_ENDIAN );
	}

	/**
	 * Sets bytes order in frame.
	 * 
	 * @param bBigEndian
	 *            the big endian flag
	 */
	public void setOrder( boolean bBigEndian ) {
		_bb.order( bBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN );
	}

	/**
	 * Returns field value as timestamp.
	 * 
	 * @param iField
	 *            the field index
	 * 
	 * @return value as timestamp
	 */
	public Timestamp getTimestamp( int iField ) {
		return( new Timestamp( _bb.getLong( _fieldsOffsets.get( iField ) ) ) );
	}

	/**
	 * Sets timestamp value for field iField in frame.
	 * 
	 * @param iField
	 *            the field index
	 * @param tm
	 *            the timestamp
	 */
	public void setTimestamp( int iField, Timestamp tm ) {
		_bb.putLong( _fieldsOffsets.get( iField ), tm.getTime( ) );
	}

	/**
	 * Returns field value as int.
	 * 
	 * @param iField
	 *            the field index
	 * 
	 * @return value as int
	 */
	public Integer getInt( int iField ) {
		return( _bb.getInt( _fieldsOffsets.get( iField ) ) );
	}

	/**
	 * Returns field value as byte.
	 * 
	 * @param iField
	 *            the field index
	 * 
	 * @return value as byte
	 */
	public Byte getByte( int iField ) {
		return( _bb.get( _fieldsOffsets.get( iField ) ) );
	}

	/**
	 * Sets byte value for field iField in frame.
	 * 
	 * @param iField
	 *            the field index
	 * @param b
	 *            the byte value to set
	 */
	public void setByte( int iField, byte b ) {
		_bb.put( _fieldsOffsets.get( iField ), b );
	}

	/**
	 * Returns field value as short.
	 * 
	 * @param iField
	 *            the field index
	 * 
	 * @return value as short
	 */
	public Short getShort( int iField ) {
		return( _bb.getShort( _fieldsOffsets.get( iField ) ) );
	}

	/**
	 * Sets short value for field iField in frame.
	 * 
	 * @param iField
	 *            the field index
	 * @param sh
	 *            the short value to set
	 */
	public void setShort( int iField, short sh ) {
		_bb.putShort( _fieldsOffsets.get( iField ), sh );
	}

	/**
	 * Sets int value for field iField in frame.
	 * 
	 * @param iField
	 *            the field index
	 * @param i
	 *            the int value
	 */
	public void setInt( int iField, int i ) {
		_bb.putInt( _fieldsOffsets.get( iField ), i );
	}

	/**
	 * Returns field value as long.
	 * 
	 * @param iField
	 *            the field index
	 * 
	 * @return value as long
	 */
	public Long getLong( int iField ) {
		return( _bb.getLong( _fieldsOffsets.get( iField ) ) );
	}

	/**
	 * Sets long value for field iField in frame.
	 * 
	 * @param iField
	 *            the field index
	 * @param l
	 *            the long value
	 */
	public void setLong( int iField, long l ) {
		_bb.putLong( _fieldsOffsets.get( iField ), l );
	}

	/**
	 * Returns field value as string.
	 * 
	 * @param iField
	 *            the field index
	 * 
	 * @return value as string
	 */
	public String getString( int iField ) {
		byte[] ab = new byte[ getFieldLen( iField ) ];
		ByteBuffer bb = ( ByteBuffer )_bb.position( _fieldsOffsets.get( iField ) );
		bb.get( ab );
		return( new String( ab ) );
	}

	/**
	 * Sets string value for field iField in frame.
	 * 
	 * @param iField
	 *            the field index
	 * @param s
	 *            the string value to set
	 */
	public void setString( int iField, String s ) {
		_bb = ( ByteBuffer )_bb.position( _fieldsOffsets.get( iField ) );
		_bb.put( s.getBytes( ) );
	}

	/**
	 * Returns field value as float.
	 * 
	 * @param iField
	 *            the field index
	 * 
	 * @return value as float
	 */
	public Float getFloat( int iField ) {
		return( _bb.getFloat( _fieldsOffsets.get( iField ) ) );
	}

	/**
	 * Sets float value for field iField in frame.
	 * 
	 * @param iField
	 *            the field index
	 * @param fl
	 *            the float value to set
	 */
	public void setFloat( int iField, float fl ) {
		_bb.putFloat( _fieldsOffsets.get( iField ), fl );
	}

	/**
	 * Returns field value as double.
	 * 
	 * @param iField
	 *            the field index
	 * 
	 * @return value as double
	 */
	public Double getDouble( int iField ) {
		return( _bb.getDouble( _fieldsOffsets.get( iField ) ) );
	}

	/**
	 * Sets double value for field iField in frame.
	 * 
	 * @param iField
	 *            the field index
	 * @param db
	 *            the double value
	 */
	public void setDouble( int iField, double db ) {
		_bb.putDouble( _fieldsOffsets.get( iField ), db );
	}

	/**
	 * Returns field length in bytes.
	 * 
	 * @param iField
	 *            the field index
	 * 
	 * @return field length in bytes
	 */
	private int getFieldLen( int iField ) {
		int iLen = 0;
		if( _fieldsOffsets.size( ) == iField + 1 ) {
			iLen = _bb.capacity( ) - _fieldsOffsets.get( iField );
		} else {
			iLen = _fieldsOffsets.get( iField + 1 ) - _fieldsOffsets.get( iField );
		}
		return( iLen );
	}
}
