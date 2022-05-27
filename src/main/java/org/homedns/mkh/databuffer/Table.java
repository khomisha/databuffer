/*
 * Copyright 2013-2022 Mikhail Khodonov
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
import java.util.Arrays;

import com.google.gson.annotations.SerializedName;

/**
 * Data buffer table
 *
 */
public class Table implements Serializable {
	private static final long serialVersionUID = 5579034046103844446L;
	
	private String updateTableName;
	@SerializedName( "select" ) private String query;
	@SerializedName( "key" ) private String pkCol;
	@SerializedName( "rowCountColumn" ) private String rowCountCol;
	private int pageSize;
	private String[] argType;
	private String reportData;

	public Table( ) {
	}
	
	/**
	 * Returns update table name
	 * 
	 * @return the update table name
	 */
	public String getUpdateTableName( ) {
		return( updateTableName );
	}
	
	/**
	 * Sets update table name
	 * 
	 * @param sUpdateTableName
	 *            the update table name to set
	 */
	public void setUpdateTableName( String sUpdateTableName ) {
		this.updateTableName = sUpdateTableName;
	}
	
	/**
	 * Returns data buffer retrieve query
	 * 
	 * @return the data buffer retrieve query
	 */
	public String getQuery( ) {
		if( query == null || "".equals( query ) ) {
			throw new IllegalArgumentException( "no query" );
		}
		return( query );
	}
	
	/**
	 * Sets data buffer retrieve query
	 * 
	 * @param query
	 *            the data buffer retrieve query to set
	 */
	public void setQuery( String query ) {
		this.query = query.replaceAll( "[\\n\\x0B\\f\\r]","" );
	}
	
	/**
	 * Returns primary key column name
	 * 
	 * @return the primary key column name 
	 */
	public String getPKcol( ) {
		if( pkCol == null || "".equals( pkCol ) ) {
			throw new IllegalArgumentException( "no primary key" );
		}
		return( pkCol );
	}
	
	/**
	 * Sets primary key column name
	 * 
	 * @param pkCol
	 *            the primary key column name to set
	 */
	public void setPKcol( String pkCol ) {
		this.pkCol = pkCol;
	}
	
	/**
	 * Returns row count column name
	 * 
	 * @return the row count column name
	 */
	public String getRowCountCol( ) {
		return( rowCountCol );
	}
	
	/**
	 * Sets row count column name
	 * 
	 * @param rowCountCol
	 *            the row count column name to set
	 */
	public void setRowCountCol( String rowCountCol ) {
		this.rowCountCol = rowCountCol;
	}
	
	/**
	 * Returns data buffer query arguments data types array
	 * 
	 * @return the data buffer query arguments types array 
	 */
	public String[] getArgType( ) {
		return( argType );
	}
	
	/**
	 * Sets data buffer query arguments data types array
	 * 
	 * @param argType
	 *            the data buffer query arguments data types array to set
	 */
	public void setArgType( String[] argType ) {
		this.argType = argType;
	}
	
	/**
	 * Returns report data anchor point
	 * 
	 * @return the report data anchor point
	 */
	public String getReportData( ) {
		return( reportData );
	}
	
	/**
	 * Sets report data anchor point
	 * 
	 * @param reportData
	 *            the report data anchor point to set
	 */
	public void setReportData( String reportData ) {
		this.reportData = reportData;
	}

	/**
	 * Returns page size
	 * 
	 * @return the page size
	 */
	public Integer getPageSize( ) {
		return( pageSize );
	}

	/**
	 * Sets page size
	 * 
	 * @param pageSize
	 *            the page size to set
	 */
	public void setPageSize( int pageSize ) {
		this.pageSize = pageSize;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return "Table [updateTableName=" + updateTableName + ", query=" + query + ", pkCol=" + pkCol + ", rowCountCol="
			+ rowCountCol + ", pageSize=" + pageSize + ", argType=" + Arrays.toString( argType ) + ", reportData="
			+ reportData + "]";
	}
}
