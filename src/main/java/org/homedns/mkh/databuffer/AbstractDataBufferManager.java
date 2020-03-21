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

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.homedns.mkh.databuffer.DBTransaction;
import org.homedns.mkh.databuffer.DataBuffer;
import org.homedns.mkh.databuffer.DataBufferMetaData;
import org.homedns.mkh.databuffer.Environment;

/**
 * Abstract data buffer manager
 *
 */
public abstract class AbstractDataBufferManager implements Environment {	
	private static final Logger LOG = Logger.getLogger( AbstractDataBufferManager.class );

	private ConcurrentHashMap< String, ConcurrentHashMap< Long, DataBuffer > > dbs = new ConcurrentHashMap< >( );
	private DBTransaction transObj; 
	private Locale locale; 
	private SimpleDateFormat cliDateFormat;
	private SimpleDateFormat srvDateFormat;
	
	/**
	* Closes data buffer manager and all bound data buffers
	*/
	public void close( ) {
		for( String sDataBufferName : dbs.keySet( ) ) {
			ConcurrentHashMap< Long, DataBuffer > dbMap = dbs.get( sDataBufferName );
			for( Long lUID : dbMap.keySet( ) ) {
				dbMap.get( lUID ).close( );
			}
			dbMap.clear( );
		}
		dbs.clear( );
	}

	/**
	 * Returns data buffer (if it doesn't exist try to create it and adds to the
	 * databuffer map).
	 * 
	 * @param id
	 *            the identifier object
	 * 
	 * @return the data buffer object or null
	 * 
	 * @throws Exception
	 */
	public DataBuffer getDataBuffer( UUID id ) throws Exception {
		String sDataBufferName = getLocaleDBName( id.getName( ) );
		DataBuffer db = getDataBuffer( sDataBufferName, id.getUID( ) );
		if( db == null ) {
			db = createDataBuffer( sDataBufferName, id.getUID( ) );
		}
		return( db );
	}
		
	/**
	 * Creates data buffer.
	 * 
	 * @param sDataBufferName
	 *            the data buffer name
	 * @param lUID
	 *            the data buffer uid
	 * @param env
	 *            the application environment
	 * 
	 * @return data buffer object.
	 * 
	 * @throws Exception
	 */
	protected DataBuffer createDataBuffer( String sDataBufferName, Long lUID ) throws Exception {
		DataBuffer db = new DataBuffer( new DataBufferMetaData( sDataBufferName, this ) );
		put( sDataBufferName, lUID, db );
		LOG.debug( "data buffer created: " + sDataBufferName + "-" + String.valueOf( lUID ) );
		return( db );
	}

	/**
	 * @see org.homedns.mkh.databuffer.Environment#getDataBufferFilename(java.lang.String)
	 */
	@Override
	public abstract String getDataBufferFilename( String sDataBufferName );
	
	/**
	 * @see org.homedns.mkh.databuffer.Environment#getClientDateFormat()
	 */
	@Override
	public SimpleDateFormat getClientDateFormat( ) {
		return( cliDateFormat );
	}

	/**
	 * @see org.homedns.mkh.databuffer.Environment#getServerDateFormat()
	 */
	@Override
	public SimpleDateFormat getServerDateFormat( ) {
		return( ( srvDateFormat == null ) ? Environment.super.getServerDateFormat( ) : srvDateFormat );
	}

	/**
	 * @see org.homedns.mkh.databuffer.Environment#getLocale()
	 */
	@Override
	public Locale getLocale( ) {
		return( ( locale == null ) ? Environment.super.getLocale( ) : locale );
	}

	/**
	 * @see org.homedns.mkh.databuffer.Environment#getTransObject()
	 */
	@Override
	public DBTransaction getTransObject( ) {
		return( transObj );
	}

	/**
	 * Sets transaction object
	 * 
	 * @param sqlca the transaction object to set
	 */
	public void setTransObject( DBTransaction transObj ) {
		this.transObj = transObj;
	}

	/**
	 * Sets locale
	 * 
	 * @param locale the locale to set
	 */
	public void setLocale( Locale locale ) {
		this.locale = locale;
	}

	/**
	 * Sets client date time format
	 * 
	 * @param cliDateFormat the client date time format to set
	 */
	public void setClientDateFormat( SimpleDateFormat cliDateFormat ) {
		this.cliDateFormat = cliDateFormat;
	}

	/**
	 * Sets server date time format
	 * 
	 * @param srvDateFormat the server date time format to set
	 */
	public void setServerDateFormat( SimpleDateFormat srvDateFormat ) {
		this.srvDateFormat = srvDateFormat;
	}

	/**
	 * Closes specified data buffer and removes it from data buffer manager registry
	 * 
	 * @param id
	 *            the data buffer identifier
	 */
	public void closeDataBuffer( UUID id ) {
		String sDataBufferName = getLocaleDBName( id.getName( ) );
		ConcurrentHashMap< Long, DataBuffer > dbMap = get( sDataBufferName );
		if( dbMap != null ) {
			DataBuffer db = dbMap.get( id.getUID( ) );
			if( db != null ) {
				dbMap.remove( id.getUID( ) );
				db.close( );
				LOG.debug( 
					"data buffer removed: " + sDataBufferName + "-" + String.valueOf( id.getUID( ) ) 
				);
			}
		}		
	}
	
	/**
	 * Returns data buffer object if exists otherwise null.
	 * 
	 * @param sDataBufferName
	 *            the data buffer name
	 * @param lUID
	 *            the data buffer uid
	 * 
	 * @return data buffer object or null.
	 */
	protected DataBuffer getDataBuffer( String sDataBufferName, Long lUID ) {
		DataBuffer db = null;
		ConcurrentHashMap< Long, DataBuffer > dbMap = get( sDataBufferName );
		if( dbMap != null ) {
			db = dbMap.get( lUID );
		}
		return( db );	
	}

	/**
	 * Returns data buffers map with the same name
	 * 
	 * @param sDataBufferName
	 *            the data buffer name
	 * 
	 * @return the data buffers map
	 */
	protected ConcurrentHashMap< Long, DataBuffer > get( String sDataBufferName ) {
		return( dbs.get( sDataBufferName ) );
	}

	/**
	 * Returns data buffer name for current locale.
	 * 
	 * @param sDataBufferName
	 *            the data buffer name
	 * 
	 * @return data buffer name for current locale.
	 */
	protected String getLocaleDBName( String sDataBufferName ) {
		if( sDataBufferName == null || "".equals( sDataBufferName ) ) {
			throw new IllegalArgumentException( sDataBufferName );
		}
		return(
			Environment.DEFAULT_LOCALE.equals( locale ) ? 
			sDataBufferName : 
			sDataBufferName + "_" + locale.getLanguage( )
		);
	}

	/**
	 * Adds the databuffer object to the map.
	 * 
	 * @param sDataBufferName
	 *            the data buffer name
	 * @param lUID
	 *            the data buffer uid
	 * @param db
	 *            the data buffer object to add
	 */
	private void put( String sDataBufferName, Long lUID, DataBuffer db ) {
		ConcurrentHashMap< Long, DataBuffer > dbMap = get( sDataBufferName );
		if( dbMap == null ) {
			dbMap = new ConcurrentHashMap< Long, DataBuffer >( );
			ConcurrentHashMap< Long, DataBuffer > map = dbs.putIfAbsent( sDataBufferName, dbMap );
			if( map != null ) {
				dbMap = map;
			}
		}
		dbMap.putIfAbsent( lUID, db );
	}
}