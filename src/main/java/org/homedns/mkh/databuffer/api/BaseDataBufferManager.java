/* 
 * Copyright 2022 Mikhail Khodonov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.homedns.mkh.databuffer.api;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.homedns.mkh.databuffer.DataBufferImpl;
import org.apache.log4j.Logger;
import org.homedns.mkh.databuffer.DBConnection;
import org.homedns.mkh.databuffer.DataBufferDesc;
import org.homedns.mkh.util.Util;
import com.google.gson.stream.JsonReader;

/**
 * Base data buffer manager, DataBufferManager implementation example
 * It must exist data source with name 'default'
 */
public class BaseDataBufferManager implements DataBufferManager {
	private static final Logger LOG = Logger.getLogger( BaseDataBufferManager.class );

	private ConcurrentHashMap< String, DataSource > dataSources;
	private Locale locale;
	private Path path;
	private boolean bResource;
	private Class< ? > type;

	public BaseDataBufferManager( ) throws SQLException {
		dataSources = new ConcurrentHashMap< >( );
		locale = DEFAULT_LOCALE;
	}

	/**
	 * @see org.homedns.mkh.databuffer.api.DataBufferManager#getLocale()
	 */
	@Override
	public Locale getLocale( ) {
		return( locale );
	}

	/**
	 * @see org.homedns.mkh.databuffer.api.DataBufferManager#setLocale(java.util.Locale)
	 */
	@Override
	public void setLocale( Locale locale ) {
		this.locale = locale;
	}

	/**
	 * @see org.homedns.mkh.databuffer.api.DataBufferManager#addDataSource(org.homedns.mkh.databuffer.api.GenericDataSource)
	 */
	@Override
	public void addDataSource( GenericDataSource ds ) {
		dataSources.put( ds.getName( ), new DBConnection( ds.getDataSource( ) ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.api.DataBufferManager#getDataBuffer(java.lang.String)
	 */
	@Override
	public DataBuffer getDataBuffer( String sName ) throws Exception {
		return( getDataBuffer( sName, DEFAULT_DATASOURCE_NAME ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.api.DataBufferManager#getDataBuffer(java.lang.String, java.lang.String)
	 */
	@Override
	public DataBuffer getDataBuffer( String sName, String sDataSourceName ) throws Exception {
		DataBufferDesc desc = getDataBufferDesc( sName );
		desc.init( 
			new Context( ) {
				@Override
				public DataBuffer getDataBuffer( String sName ) throws Exception {
					return( BaseDataBufferManager.this.getDataBuffer( sName ) );
				} 
			} 
		);
		return( new DataBufferImpl( desc, getDataSource( sDataSourceName ) ) );
	}
	
	/**
	 * @see org.homedns.mkh.databuffer.api.DataBufferManager#getDataSource(java.lang.String)
	 */
	@Override
	public DataSource getDataSource( String sName ) {
		return( dataSources.get( sName ) );
	}

	/**
	 * @see org.homedns.mkh.databuffer.api.DataBufferManager#setResource(java.nio.file.Path, boolean, java.lang.Class)
	 */
	@Override
	public void setResource( Path path, boolean bResource, Class< ? > type ) {
		this.path = path;
		this.bResource = bResource;
		if( bResource ) {
			this.type =type;
		}
	}

	/**
	 * Returns data buffer description object
	 * 
	 * @param sName the data buffer description file name
	 * 
	 * @return the data buffer description object
	 * 
	 * @throws Exception
	 */
	private DataBufferDesc getDataBufferDesc( String sName ) throws Exception {
		DataBufferDesc desc = null;
		sName = (
			( DEFAULT_LOCALE.equals( locale ) ) ? 
			sName + ".dbuf" : 
			sName + "_" + locale.getLanguage( ) + ".dbuf"
		);
		String sPath = Paths.get( path.toString( ), sName ).toString( );
		LOG.debug( sPath );
		try( JsonReader in = 
			new JsonReader( 
				bResource ? new InputStreamReader( type.getResourceAsStream( sPath ) ) :  new FileReader( sPath )
			)
		) {
			desc = Util.getGson( ).fromJson( in, DataBufferDesc.class );
		}
		LOG.debug( desc );
		return( desc );
	}
}
