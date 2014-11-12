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
 * Data buffer table
 *
 */
public class Table implements Serializable {
	private static final long serialVersionUID = 5579034046103844446L;
	
	@SerializedName( "updateTableName" ) private String _sUpdateTableName;
	@SerializedName( "select" ) private String _sQuery;
	@SerializedName( "key" ) private String _sPKcol;
	@SerializedName( "rowCountColumn" ) private String _sRowCountCol;
	@SerializedName( "pageSize" ) private int _iPageSize;
	@SerializedName( "argType" ) private String[] _argType;
	@SerializedName( "reportData" ) private String _sReportData;

	public Table( ) {
	}
	
	/**
	 * Returns update table name
	 * 
	 * @return the update table name
	 */
	public String getUpdateTableName( ) {
		if( _sUpdateTableName == null || "".equals( _sUpdateTableName ) ) {
			return( "" );
		}
		return( _sUpdateTableName );
	}
	
	/**
	 * Sets update table name
	 * 
	 * @param sUpdateTableName
	 *            the update table name to set
	 */
	public void setUpdateTableName( String sUpdateTableName ) {
		_sUpdateTableName = sUpdateTableName;
	}
	
	/**
	 * Returns data buffer retrieve query
	 * 
	 * @return the data buffer retrieve query
	 * 
	 * @throws InvalidDatabufferDesc 
	 */
	public String getQuery( ) throws InvalidDatabufferDesc {
		if( _sQuery == null || "".equals( _sQuery ) ) {
			throw new InvalidDatabufferDesc( "no query" );
		}
		return( _sQuery );
	}
	
	/**
	 * Sets data buffer retrieve query
	 * 
	 * @param sQuery
	 *            the data buffer retrieve query to set
	 */
	public void setQuery( String sQuery ) {
		_sQuery = sQuery;
	}
	
	/**
	 * Returns primary key column name
	 * 
	 * @return the primary key column name 
	 * 
	 * @throws InvalidDatabufferDesc 
	 */
	public String getPKcol( ) throws InvalidDatabufferDesc {
		if( _sPKcol == null || "".equals( _sPKcol ) ) {
			throw new InvalidDatabufferDesc( "no primary key" );
		}
		return( _sPKcol );
	}
	
	/**
	 * Sets primary key column name
	 * 
	 * @param sPKcol
	 *            the primary key column name to set
	 */
	public void setPKcol( String sPKcol ) {
		_sPKcol = sPKcol;
	}
	
	/**
	 * Returns row count column name
	 * 
	 * @return the row count column name
	 */
	public String getRowCountCol( ) {
		return( _sRowCountCol );
	}
	
	/**
	 * Sets row count column name
	 * 
	 * @param sRowCountCol
	 *            the row count column name to set
	 */
	public void setRowCountCol( String sRowCountCol ) {
		_sRowCountCol = sRowCountCol;
	}
	
	/**
	 * Returns data buffer query arguments data types array
	 * 
	 * @return the data buffer query arguments types array 
	 */
	public String[] getArgType( ) {
		return( _argType );
	}
	
	/**
	 * Sets data buffer query arguments data types array
	 * 
	 * @param argType
	 *            the data buffer query arguments data types array to set
	 */
	public void setArgType( String[] argType ) {
		_argType = argType;
	}
	
	/**
	 * Returns report data anchor point
	 * 
	 * @return the report data anchor point
	 */
	public String getReportData( ) {
		return( _sReportData );
	}
	
	/**
	 * Sets report data anchor point
	 * 
	 * @param sReportData
	 *            the report data anchor point to set
	 */
	public void setReportData( String sReportData ) {
		_sReportData = sReportData;
	}

	/**
	 * Returns page size
	 * 
	 * @return the page size
	 */
	public Integer getPageSize( ) {
		return( _iPageSize );
	}

	/**
	 * Sets page size
	 * 
	 * @param iPageSize
	 *            the page size to set
	 */
	public void setPageSize( int iPageSize ) {
		_iPageSize = iPageSize;
	}
}
