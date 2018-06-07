/*
 * Copyright 2013-2018 Mikhail Khodonov
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
import com.google.gson.annotations.SerializedName;

/**
 * Data buffer column object
 *
 */
public class Column implements Serializable {
	private static final long serialVersionUID = -4625992839511135960L;

	/**
	* Dropdown listbox style
	*/
	public static final String DDLB			= "ddlb";
	/**
	* Dropdown data buffer style
	 */
	public static final String DDDB			= "dddb";
	/**
	 * Checkbox style
	 */
	public static final String CHECKBOX		= "checkbox";
	/**
	 * Radiobutton style
	 */
	public static final String RADIOBUTTON	= "radiobutton";
	/**
	 * General purpose edit style
	 */
	public static final String EDIT			= "edit";
	/**
	 * Password style
	 */
	public static final String PWD			= "pwd";
	/**
	 * Date style
	 */
	public static final String EDIT_DATE	= "edit_date";
	/**
	 * Time style
	 */
	public static final String EDIT_TIME	= "edit_time";
	/**
	 * Timestamp style
	 */
	public static final String EDIT_TS		= "edit_ts";

	@SerializedName( "name" ) private String sName;
	@SerializedName( "caption" ) private String sCaption;
	@SerializedName( "dbName" ) private String sDBName;
	@SerializedName( "type" ) private String sType;
	@SerializedName( "update" ) private boolean bUpdate;
	@SerializedName( "validationRule" ) private String sValidationRule;
	@SerializedName( "validationMsg" ) private String sValidationMsg;
	@SerializedName( "style" ) private String sStyle;
	@SerializedName( "limit" ) private int iLimit;
	@SerializedName( "required" ) private boolean bRequired;
	@SerializedName( "mask" ) private String sMask;
	@SerializedName( "values" ) private Value[] values;
	@SerializedName( "dddbName" ) private String sDDDBName;
	@SerializedName( "dddbDisplayColumn" ) private String sDDDBDisplayCol;
	@SerializedName( "dddbDataColumn" ) private String sDDDBDataCol;
	@SerializedName( "reportParam" ) private String sReportParam;
	@SerializedName( "colNum" ) private int iColNum;
	@SerializedName( "pattern" ) private String sPattern = "";
	@SerializedName( "width" ) private int iWidth = 0;
	
	public Column( ) {
	}
	
	/**
	 * Returns column name
	 * 
	 * @return the column name
	 * 
	 * @throws InvalidDatabufferDesc 
	 */
	public String getName( ) throws InvalidDatabufferDesc {
		if( sName == null || "".equals( sName ) ) {
			throw new InvalidDatabufferDesc( "no column name" );
		}
		return( sName );
	}
	
	/**
	 * Sets column name
	 * 
	 * @param sName
	 *            the column name to set
	 */
	public void setName( String sName ) {
		this.sName = sName;
	}
	
	/**
	 * Returns column number
	 * 
	 * @return the column number
	 */
	public int getColNum( ) {
		return( iColNum );
	}
	
	/**
	 * Sets column number
	 * 
	 * @param iColNum the column number to set
	 */
	public void setColNum( int iColNum ) {
		this.iColNum = iColNum;
	}
	
	/**
	 * Returns column caption
	 * 
	 * @return the column caption
	 */
	public String getCaption( ) {
		return( sCaption );
	}
	
	/**
	 * Sets column caption
	 * 
	 * @param sCaption the column caption to set
	 */
	public void setCaption( String sCaption ) {
		this.sCaption = sCaption;
	}
	
	/**
	 * Returns column database name
	 * 
	 * @return the column database name
	 */
	public String getDBName( ) {
		if( sDBName == null || "".equals( sDBName ) ) {
			return( "" );
		}
		return( sDBName );
	}
	
	/**
	 * Sets column database name
	 * 
	 * @param sDBName the column database name to set
	 */
	public void setDBName( String sDBName ) {
		this.sDBName = sDBName;
	}
	
	/**
	 * Returns column type
	 * 
	 * @return the column type
	 * 
	 * @throws InvalidDatabufferDesc 
	 */
	public Type getType( ) throws InvalidDatabufferDesc {
		if( sType == null || "".equals( sType ) ) {
			throw new InvalidDatabufferDesc( getName( ) + ": no column type" );
		}
		return( Type.valueOf( sType ) );
	}
	
	/**
	 * Sets column type
	 * 
	 * @param sType the column type to set
	 */
	public void setType( String sType ) {
		this.sType = sType;
	}
	
	/**
	 * Returns column update flag
	 * 
	 * @return the column update flag
	 */
	public boolean isUpdate( ) {
		return( bUpdate );
	}
	
	/**
	 * Sets column update flag
	 * 
	 * @param bUpdate the column update flag to set
	 */
	public void setUpdate( boolean bUpdate ) {
		this.bUpdate = bUpdate;
	}
	
	/**
	 * Returns column validation rule (regex expression)
	 * 
	 * @return the column validation rule 
	 */
	public String getValidationRule( ) {
		return( sValidationRule );
	}
	
	/**
	 * Sets column validation rule
	 * 
	 * @param sValidationRule the column validation rule to set
	 */
	public void setValidationRule( String sValidationRule ) {
		this.sValidationRule = sValidationRule;
	}
	
	/**
	 * Returns validation message
	 * 
	 * @return the validation message
	 */
	public String getValidationMsg( ) {
		return( sValidationMsg );
	}
	
	/**
	 * Sets validation message
	 * 
	 * @param sValidationMsg the validation message to set
	 */
	public void setValidationMsg( String sValidationMsg ) {
		this.sValidationMsg = sValidationMsg;
	}
	
	/**
	 * Returns column style
	 * 
	 * @return the column style
	 * 
	 * @throws InvalidDatabufferDesc 
	 */
	public String getStyle( ) throws InvalidDatabufferDesc {
		if( Column.DDDB.equals( sStyle ) && "".equals( getDDDBName( ) ) ) {
			throw new InvalidDatabufferDesc( getName( ) + ": no dropdown data buffer" );			
		}
		return( sStyle );
	}
	
	/**
	 * Sets column style
	 * 
	 * @param sStyle the column style to set
	 */
	public void setStyle( String sStyle ) {
		this.sStyle = sStyle;
	}
	
	/**
	 * Returns column length in bytes, 0 means non dimensional
	 * 
	 * @return the column length in bytes
	 */
	public int getLimit( ) {
		return( iLimit );
	}
	
	/**
	 * Sets column length in bytes
	 * 
	 * @param iLimit the column length to set
	 */
	public void setLimit( int iLimit ) {
		this.iLimit = iLimit;
	}
	
	/**
	 * Returns column mandatory flag
	 * 
	 * @return the column mandatory flag
	 */
	public boolean isRequired( ) {
		return( bRequired );
	}
	
	/**
	 * Sets column mandatory flag
	 * 
	 * @param bRequired the column mandatory flag to set
	 */
	public void setRequired( boolean bRequired ) {
		this.bRequired = bRequired;
	}
	
	/**
	 * Returns column mask, the keystroke filter mask to be applied to the column
	 * input value type (regex expression)
	 * 
	 * @return the column mask
	 */
	public String getMask( ) {
		return( sMask );
	}
	
	/**
	 * Sets column mask, the keystroke filter mask to be applied to the column
	 * input value type (regex expression)
	 * 
	 * @param sMask the column mask to set
	 */
	public void setMask( String sMask ) {
		this.sMask = sMask;
	}
	
	/**
	 * Returns array which contains pairs display value - actual value
	 * 
	 * @return the values array
	 */
	public Value[] getValues( ) {
		return( values );
	}
	
	/**
	 * Sets array which contains pairs display value - actual value
	 * 
	 * @param values the values array to set
	 */
	public void setValues( Value[] values ) {
		this.values = values;
	}
	
	/**
	 * Returns dropdown databuffer name
	 * 
	 * @return the dropdown databuffer name
	 */
	public String getDDDBName( ) {
		return( sDDDBName );
	}
	
	/**
	 * Sets dropdown databuffer name
	 * 
	 * @param sDDDBName the dropdown databuffer name to set
	 */
	public void setDDDBName( String sDDDBName ) {
		this.sDDDBName = sDDDBName;
	}
	
	/**
	 * Returns dropdown data buffer display column name
	 * 
	 * @return the dropdown data buffer display column name
	 */
	public String getDisplayCol( ) {
		return( sDDDBDisplayCol );
	}
	
	/**
	 * Sets dropdown data buffer display column name
	 * 
	 * @param sDDDBDisplayCol
	 *            the dropdown data buffer display column name to set
	 */
	public void setDisplayCol( String sDDDBDisplayCol ) {
		this.sDDDBDisplayCol = sDDDBDisplayCol;
	}
	
	/**
	 * Returns dropdown data buffer data column name
	 * 
	 * @return the dropdown data buffer data column name
	 */
	public String getDataCol( ) {
		return( sDDDBDataCol );
	}
	
	/**
	 * Sets dropdown data buffer data column name
	 * 
	 * @param sDDDBDataCol
	 *            the dropdown data buffer data column name to set
	 */
	public void setDataCol( String sDDDBDataCol ) {
		this.sDDDBDataCol = sDDDBDataCol;
	}
	
	/**
	 * Returns report indicates cell (column,row) in excel sheet template
	 * 
	 * @return the indicates cell
	 */
	public String getReportParam( ) {
		return( sReportParam );
	}
	
	/**
	 * Sets indicates cell (column,row) in excel sheet template
	 * 
	 * @param sReportParam
	 *            the cell (column,row) to set
	 */
	public void setReportParam( String sReportParam ) {
		this.sReportParam = sReportParam;
	}

	/**
	 * Returns column format pattern
	 * 
	 * @return the column format pattern
	 */
	public String getPattern( ) {
		return( sPattern );
	}

	/**
	 * Sets column format pattern
	 * 
	 * @param sPattern the column format pattern to set
	 */
	public void setPattern( String sPattern ) {
		this.sPattern = sPattern;
	}

	/**
	 * Returns column width in pixels
	 * 
	 * @return the column width
	 */
	public int getWidth( ) {
		return( iWidth );
	}

	/**
	 * Sets column width in pixels
	 * 
	 * @param iWidth the column width to set
	 */
	public void setWidth( int iWidth ) {
		this.iWidth = iWidth;
	}
}
