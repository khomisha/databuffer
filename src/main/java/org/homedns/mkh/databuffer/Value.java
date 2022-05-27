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

import java.io.Serializable;

/**
 * Value contains display value - data value
 *
 */
public class Value  implements Serializable {
	private static final long serialVersionUID = -2064001953576612248L;
	
	private String displayValue;
	private String dataValue;
	
	/**
	 * Returns display value
	 * 
	 * @return the display value
	 */
	public String getDisplayValue( ) {
		return(  displayValue );
	}
	
	/**
	 * Returns data value
	 * 
	 * @return the data value
	 */
	public String getDataValue( ) {
		return( dataValue );
	}
	
	/**
	 * Sets display value
	 * 
	 * @param displayValue
	 *            the display value to set
	 */
	public void setDisplayValue( String displayValue ) {
		this.displayValue = displayValue;
	}
	
	/**
	 * Sets data value
	 * 
	 * @param dataValue
	 *            the data value to set
	 */
	public void setDataValue( String dataValue ) {
		this.dataValue = dataValue;
	}
}
