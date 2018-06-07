/*
 * Copyright 2011-2018 Mikhail Khodonov
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
import java.util.ArrayList;
import java.util.List;

/**
 * Byte buffer wrapper. It allows to interpret the byte buffer as a set of fields.
 */
public class Frame {
	private ByteBuffer buffer;
	private List< FrameField > fields;
	
	/**
	 * @param fieldsLengths
	 *            the fields lengths in bytes
	 * @param abPacket
	 *            the input byte array
	 * @param bBigEndian
	 *            the bigendian flag
	 */
	public Frame( List< Integer > fieldsLengths, byte[] abPacket, boolean bBigEndian ) {
		setFields( fieldsLengths );
		if( getSize( ) > abPacket.length ) {
			throw new IllegalArgumentException( "Total fields length more than backing array length" );
		}
		setBuffer( ByteBuffer.wrap( abPacket ) );
		setOrder( bBigEndian );
	}

	/**
	 * @param fieldsLengths
	 *            the fields lengths in bytes
	 * @param bBigEndian
	 *            the bigendian flag
	 */
	public Frame( List< Integer > fieldsLengths, boolean bBigEndian ) {
		setFields( fieldsLengths );
		setBuffer( ByteBuffer.allocate( getSize( ) ) );
		setOrder( bBigEndian );
	}

	/**
	* Returns frame fields values as byte buffer.
	*
	* @return byte buffer
	*/
	public ByteBuffer getBuffer( ) {
		return( buffer );
	}

	/**
	 * Sets byte buffer with frame fields values.
	 * 
	 * @param bb
	 *            the byte buffer to set
	 */
	protected void setBuffer( ByteBuffer bb ) {
		buffer = bb;
	}

	/**
	* Returns bytes order in frame.
	*
	* @return true if bigendian and false if little endian
	*/
	public boolean isBigEndian( ) {
		return( buffer.order( ) == ByteOrder.BIG_ENDIAN );
	}

	/**
	 * Sets bytes order in frame.
	 * 
	 * @param bBigEndian
	 *            the bigendian flag
	 */
	public void setOrder( boolean bBigEndian ) {
		buffer.order( bBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN );
	}

	/**
	 * Returns field value as timestamp.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * 
	 * @return value as timestamp
	 */
	public Timestamp getTimestamp( int iFieldIndex ) {
		return( new Timestamp( buffer.getLong( fields.get( iFieldIndex ).getOffset( ) ) ) );
	}

	/**
	 * Sets timestamp value for specified field in frame.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * @param tm
	 *            the timestamp
	 */
	public void setTimestamp( int iFieldIndex, Timestamp tm ) {
		buffer.putLong( fields.get( iFieldIndex ).getOffset( ), tm.getTime( ) );
	}

	/**
	 * Returns field value as integer.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * 
	 * @return value as integer
	 */
	public Integer getInt( int iFieldIndex ) {
		return( buffer.getInt( fields.get( iFieldIndex ).getOffset( ) ) );
	}

	/**
	 * Returns field value as byte.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * 
	 * @return value as byte
	 */
	public Byte getByte( int iFieldIndex ) {
		return( buffer.get( fields.get( iFieldIndex ).getOffset( ) ) );
	}

	/**
	 * Sets byte value for specified field in frame.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * @param b
	 *            the value to set
	 */
	public void setByte( int iFieldIndex, byte b ) {
		buffer.put( fields.get( iFieldIndex ).getOffset( ), b );
	}

	/**
	 * Returns field value as short.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * 
	 * @return value as short
	 */
	public Short getShort( int iFieldIndex ) {
		return( buffer.getShort( fields.get( iFieldIndex ).getOffset( ) ) );
	}

	/**
	 * Sets short value for specified field in frame.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * @param sh
	 *            the value to set
	 */
	public void setShort( int iFieldIndex, short sh ) {
		buffer.putShort( fields.get( iFieldIndex ).getOffset( ), sh );
	}

	/**
	 * Sets integer value for specified field in frame.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * @param i
	 *            the value to set
	 */
	public void setInt( int iFieldIndex, int i ) {
		buffer.putInt( fields.get( iFieldIndex ).getOffset( ), i );
	}

	/**
	 * Returns field value as long.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * 
	 * @return value as long
	 */
	public Long getLong( int iFieldIndex ) {
		return( buffer.getLong( fields.get( iFieldIndex ).getOffset( ) ) );
	}

	/**
	 * Sets long value for specified field in frame.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * @param l
	 *            the long value
	 */
	public void setLong( int iFieldIndex, long l ) {
		buffer.putLong( fields.get( iFieldIndex ).getOffset( ), l );
	}

	/**
	 * Returns field value as hex string.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * 
	 * @return value as hex string
	 */
	public String getHexString( int iFieldIndex ) {
		return( Util.getHex( getByteArray( iFieldIndex ) ) );
	}

	/**
	 * Returns field value as ascii string.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * 
	 * @return value as ascii string
	 */
	public String getAsciiString( int iFieldIndex ) {
		return( new String( getByteArray( iFieldIndex ) ) );
	}
	
	/**
	 * Sets HEXSTRING or ASCIISTRING value for specified field in frame.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * @param s
	 *            the HEXSTRING or ASCIISTRING value to set
	 */
	public void setString( int iFieldIndex, String s ) {
		buffer = ( ByteBuffer )buffer.position( fields.get( iFieldIndex ).getOffset( ) );
		buffer.put( s.getBytes( ) );
	}

	/**
	 * Returns field value as byte array
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * 
	 * @return the field value as byte array
	 */
	public byte[] getByteArray( int iFieldIndex ) {
		byte[] ab = new byte[ fields.get( iFieldIndex ).getLength( ) ];
		ByteBuffer bb = ( ByteBuffer )buffer.position( fields.get( iFieldIndex ).getOffset( ) );
		bb.get( ab );
		return( ab );
	}
	
	/**
	 * Returns field value as float.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * 
	 * @return value as float
	 */
	public Float getFloat( int iFieldIndex ) {
		return( buffer.getFloat( fields.get( iFieldIndex ).getOffset( ) ) );
	}

	/**
	 * Sets float value for specified field in frame.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * @param fl
	 *            the value to set
	 */
	public void setFloat( int iFieldIndex, float fl ) {
		buffer.putFloat( fields.get( iFieldIndex ).getOffset( ), fl );
	}

	/**
	 * Returns field value as double.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * 
	 * @return value as double
	 */
	public Double getDouble( int iFieldIndex ) {
		return( buffer.getDouble( fields.get( iFieldIndex ).getOffset( ) ) );
	}

	/**
	 * Sets double value for specified field in frame.
	 * 
	 * @param iFieldIndex
	 *            the field index
	 * @param db
	 *            the value to set
	 */
	public void setDouble( int iFieldIndex, double db ) {
		buffer.putDouble( fields.get( iFieldIndex ).getOffset( ), db );
	}
	
	/**
	 * Returns frame size in bytes
	 * 
	 * @return the frame size in bytes
	 */
	public int getSize( ) {
		FrameField lastField = fields.get( fields.size( ) - 1 );
		return( lastField.getOffset( ) + lastField.getLength( ) );
	}
	
	/**
	 * Sets frame fields
	 */
	private void setFields( List< Integer > fieldsLengths ) {
		fields = new ArrayList< FrameField >( );
		for( int iField = 0; iField < fieldsLengths.size( ); iField++ ) {
			FrameField field = new FrameField( );
			field.setLength( fieldsLengths.get( iField ) );
			int iOffset = (
				iField == 0 ? 
				0 :
				fieldsLengths.get( fieldsLengths.size( ) - 1 ) + fieldsLengths.get( iField )
			);
			field.setOffset( iOffset );
			fields.add( field );
		}
	}
}
