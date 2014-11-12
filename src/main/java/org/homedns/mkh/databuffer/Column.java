/*
 * Copyright 2013-2014 Mikhail Khodonov
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

	@SerializedName( "name" ) private String _sName;
	@SerializedName( "caption" ) private String _sCaption;
	@SerializedName( "dbName" ) private String _sDBName;
	@SerializedName( "type" ) private String _sType;
	@SerializedName( "update" ) private boolean _bIsUpdate;
	@SerializedName( "validationRule" ) private String _sValidationRule;
	@SerializedName( "validationMsg" ) private String _sValidationMsg;
	@SerializedName( "style" ) private String _sStyle;
	@SerializedName( "limit" ) private int _iLimit;
	@SerializedName( "required" ) private boolean _bIsRequired;
	@SerializedName( "mask" ) private String _sMask;
	@SerializedName( "values" ) private Value[] _values;
	@SerializedName( "dddbName" ) private String _sDDDBName;
	@SerializedName( "dddbDisplayColumn" ) private String _sDDDBDisplayCol;
	@SerializedName( "dddbDataColumn" ) private String _sDDDBDataCol;
	@SerializedName( "reportParam" ) private String _sReportParam;
	@SerializedName( "colNum" ) private int _iColNum;
	
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
		if( _sName == null || "".equals( _sName ) ) {
			throw new InvalidDatabufferDesc( "no column name" );
		}
		return( _sName );
	}
	
	/**
	 * Sets column name
	 * 
	 * @param sName
	 *            the column name to set
	 */
	public void setName( String sName ) {
		_sName = sName;
	}
	
	/**
	 * Returns column number
	 * 
	 * @return the column number
	 */
	public int getColNum( ) {
		return( _iColNum );
	}
	
	/**
	 * Sets column number
	 * 
	 * @param iColNum the column number to set
	 */
	public void setColNum( int iColNum ) {
		_iColNum = iColNum;
	}
	
	/**
	 * Returns column caption
	 * 
	 * @return the column caption
	 */
	public String getCaption( ) {
		return( _sCaption );
	}
	
	/**
	 * Sets column caption
	 * 
	 * @param sCaption the column caption to set
	 */
	public void setCaption( String sCaption ) {
		_sCaption = sCaption;
	}
	
	/**
	 * Returns column database name
	 * 
	 * @return the column database name
	 */
	public String getDBName( ) {
		if( _sDBName == null || "".equals( _sDBName ) ) {
			return( "" );
		}
		return( _sDBName );
	}
	
	/**
	 * Sets column database name
	 * 
	 * @param sDBName the column database name to set
	 */
	public void setDBName( String sDBName ) {
		_sDBName = sDBName;
	}
	
	/**
	 * Returns column type
	 * 
	 * @return the column type
	 * 
	 * @throws InvalidDatabufferDesc 
	 */
	public Type getType( ) throws InvalidDatabufferDesc {
		if( _sType == null || "".equals( _sType ) ) {
			throw new InvalidDatabufferDesc( getName( ) + ": no column type" );
		}
		return( Type.valueOf( _sType ) );
	}
	
	/**
	 * Sets column type
	 * 
	 * @param sType the column type to set
	 */
	public void setType( String sType ) {
		_sType = sType;
	}
	
	/**
	 * Returns column update flag
	 * 
	 * @return the column update flag
	 */
	public boolean isUpdate( ) {
		return( _bIsUpdate );
	}
	
	/**
	 * Sets column update flag
	 * 
	 * @param bIsUpdate the column update flag to set
	 */
	public void setUpdate( boolean bIsUpdate ) {
		_bIsUpdate = bIsUpdate;
	}
	
	/**
	 * Returns column validation rule (regex expression)
	 * 
	 * @return the column validation rule 
	 */
	public String getValidationRule( ) {
		return( _sValidationRule );
	}
	
	/**
	 * Sets column validation rule
	 * 
	 * @param sValidationRule the column validation rule to set
	 */
	public void setValidationRule( String sValidationRule ) {
		_sValidationRule = sValidationRule;
	}
	
	/**
	 * Returns validation message
	 * 
	 * @return the validation message
	 */
	public String getValidationMsg( ) {
		return( _sValidationMsg );
	}
	
	/**
	 * Sets validation message
	 * 
	 * @param sValidationMsg the validation message to set
	 */
	public void setValidationMsg( String sValidationMsg ) {
		_sValidationMsg = sValidationMsg;
	}
	
	/**
	 * Returns column style
	 * 
	 * @return the column style
	 * 
	 * @throws InvalidDatabufferDesc 
	 */
	public String getStyle( ) throws InvalidDatabufferDesc {
		if( Column.DDDB.equals( _sStyle ) && "".equals( getDDDBName( ) ) ) {
			throw new InvalidDatabufferDesc( getName( ) + ": no dropdown data buffer" );			
		}
		return( _sStyle );
	}
	
	/**
	 * Sets column style
	 * 
	 * @param sStyle the column style to set
	 */
	public void setStyle( String sStyle ) {
		_sStyle = sStyle;
	}
	
	/**
	 * Returns column length in bytes, 0 means non dimensional
	 * 
	 * @return the column length in bytes
	 */
	public int getLimit( ) {
		return( _iLimit );
	}
	
	/**
	 * Sets column length in bytes
	 * 
	 * @param iLimit the column length to set
	 */
	public void setLimit( int iLimit ) {
		_iLimit = iLimit;
	}
	
	/**
	 * Returns column mandatory flag
	 * 
	 * @return the column mandatory flag
	 */
	public boolean isRequired( ) {
		return( _bIsRequired );
	}
	
	/**
	 * Sets column mandatory flag
	 * 
	 * @param bIsRequired the column mandatory flag to set
	 */
	public void setRequired( boolean bIsRequired ) {
		_bIsRequired = bIsRequired;
	}
	
	/**
	 * Returns column mask, the keystroke filter mask to be applied to the column
	 * input value type (regex expression)
	 * 
	 * @return the column mask
	 */
	public String getMask( ) {
		return( _sMask );
	}
	
	/**
	 * Sets column mask, the keystroke filter mask to be applied to the column
	 * input value type (regex expression)
	 * 
	 * @param sMask the column mask to set
	 */
	public void setMask( String sMask ) {
		_sMask = sMask;
	}
	
	/**
	 * Returns array which contains pairs display value - actual value
	 * 
	 * @return the values array
	 */
	public Value[] getValues( ) {
		return( _values );
	}
	
	/**
	 * Sets array which contains pairs display value - actual value
	 * 
	 * @param values the values array to set
	 */
	public void setValues( Value[] values ) {
		_values = values;
	}
	
	/**
	 * Returns dropdown databuffer name
	 * 
	 * @return the dropdown databuffer name
	 */
	public String getDDDBName( ) {
		return( _sDDDBName );
	}
	
	/**
	 * Sets dropdown databuffer name
	 * 
	 * @param sDDDBName the dropdown databuffer name to set
	 */
	public void setDDDBName( String sDDDBName ) {
		_sDDDBName = sDDDBName;
	}
	
	/**
	 * Returns dropdown data buffer display column name
	 * 
	 * @return the dropdown data buffer display column name
	 */
	public String getDisplayCol( ) {
		return( _sDDDBDisplayCol );
	}
	
	/**
	 * Sets dropdown data buffer display column name
	 * 
	 * @param sDDDBDisplayCol
	 *            the dropdown data buffer display column name to set
	 */
	public void setDisplayCol( String sDDDBDisplayCol ) {
		_sDDDBDisplayCol = sDDDBDisplayCol;
	}
	
	/**
	 * Returns dropdown data buffer data column name
	 * 
	 * @return the dropdown data buffer data column name
	 */
	public String getDataCol( ) {
		return( _sDDDBDataCol );
	}
	
	/**
	 * Sets dropdown data buffer data column name
	 * 
	 * @param sDDDBDataCol
	 *            the dropdown data buffer data column name to set
	 */
	public void setDataCol( String sDDDBDataCol ) {
		_sDDDBDataCol = sDDDBDataCol;
	}
	
	/**
	 * Returns report indicates cell (column,row) in excel sheet template
	 * 
	 * @return the indicates cell
	 */
	public String getReportParam( ) {
		return( _sReportParam );
	}
	
	/**
	 * Sets indicates cell (column,row) in excel sheet template
	 * 
	 * @param sReportParam
	 *            the cell (column,row) to set
	 */
	public void setReportParam( String sReportParam ) {
		_sReportParam = sReportParam;
	}
}
