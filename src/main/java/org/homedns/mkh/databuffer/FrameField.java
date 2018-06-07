/*
 * Copyright 2018 Mikhail Khodonov
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
 * Frame field
 *
 */
public class FrameField {
	private int iLength;
	private int iOffset;

	public FrameField( ) {
	}

	/**
	 * Returns the field length in bytes
	 * 
	 * @return the length
	 */
	public int getLength( ) {
		return( iLength );
	}

	/**
	 * Returns the offset in frame
	 * 
	 * @return the offset in frame
	 */
	public int getOffset( ) {
		return( iOffset );
	}

	/**
	 * Sets the field length in bytes
	 * 
	 * @param iLength the field length to set
	 */
	public void setLength( int iLength ) {
		this.iLength = iLength;
	}

	/**
	 * Sets the field offset in frame
	 * 
	 * @param iOffset the field offset to set
	 */
	public void setOffset( int iOffset ) {
		this.iOffset = iOffset;
	}
}
