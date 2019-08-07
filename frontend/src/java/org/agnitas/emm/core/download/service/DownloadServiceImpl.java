/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.download.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.activation.MimeType;
import javax.servlet.http.HttpSession;

import org.agnitas.emm.core.download.model.FileData;
import org.apache.log4j.Logger;

/**
 * Implementation of service dealing with download data.
 */
public class DownloadServiceImpl implements DownloadService {
	
	/** Logger. */
	private static final transient Logger logger = Logger.getLogger( DownloadServiceImpl.class);
	
	@Override
	public String registerDownloadData( String temporaryFileName, MimeType mimeType, String downloadName, HttpSession session) {
		Map<String, FileData> downloadData = getOrCreateDownloadDataMap( session);
		
		// Create a random UUID
		UUID uuid = UUID.randomUUID();
		
		// For the very rare possibility of duplicate UUIDs in the map, verify UUID and create new one if necessary
		while( downloadData.containsKey( uuid.toString())) {
			if( logger.isInfoEnabled())
				logger.info( "UUID " + uuid.toString() + " already used - renewing UUID");
			
			uuid = UUID.randomUUID();
		}
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Registering download data (temp file: " + temporaryFileName + ", MIME type: " + mimeType + ", download name: " + downloadName + ", ID: " + uuid);
		}
		
		FileData fileData = new FileData( temporaryFileName, mimeType, downloadName);
		downloadData.put( uuid.toString(), fileData);
		
		return uuid.toString();
	}

	@Override
	public void removeDownloadData( String id, HttpSession session) {
		if( logger.isInfoEnabled())
			logger.info( "Removing download data for session (download ID: " + id + ", session: " + session.getId() + ")");
		
		Map<String, FileData> map = getDownloadDataMap( session);
		
		// Do nothing, if there are no download informations
		if( map == null) {
			if( logger.isInfoEnabled())
				logger.info( "No download informations - nothing to delete.");
			
			return;
		}
		
		// Get the download information for the given ID
		FileData data = map.get( id);
		
		// No data for given ID? Then do nothing
		if( data == null) {
			if( logger.isInfoEnabled()) 
				logger.info( "No download information for ID " + id + " - nothing to do.");
			
			return;
		}

		// Check if temporary file exists
		File file = new File( data.getTempFileName());
		// No file? No work!
		if( !file.exists()) {
			if( logger.isInfoEnabled()) 
				logger.info( "Temporary file " + data.getTempFileName() + " for ID " + id + " does not exist.");
			
			return;
		}
		
		// Try to delete temporary file
		file.delete();
		
		// Remove download information from session
		map.remove( id);
		
		// No further informations? Drop session attribute
		if( map.size() == 0) {
			if( logger.isInfoEnabled())
				logger.info( "No further download informations -> removing session attribute");
			
			session.removeAttribute( DownloadConstants.DOWNLOAD_DATA_SESSION_ATTRIBUTE_NAME);
		}
	}
	
	@Override
	public void removeAllDownloadData( HttpSession session) {
		if( logger.isInfoEnabled())
			logger.info( "Removing all download data for session (session: " + session.getId() + ")");

		Map<String, FileData> map = getDownloadDataMap( session);
		
		// Do we already have download informations? If not, do nothing
		if( map == null) {
			if( logger.isInfoEnabled())
				logger.info( "No download informations - nothing to delete.");
			
			return;
		}
		
		// Iterator over all download data and try to delete temporary file
		for( Map.Entry<String, FileData> entry : map.entrySet()) {
			File file = new File( entry.getValue().getTempFileName());
			
			// Temporary file exists? Kill it!
			if( file.exists()) {
				if( logger.isInfoEnabled())
					logger.info( "Removing temporary file for ID " + entry.getKey() + ": " + entry.getValue().getTempFileName());
				
				file.delete();
			}
		}

		// We have no download data, so we do not need the attribute any more
		session.removeAttribute( DownloadConstants.DOWNLOAD_DATA_SESSION_ATTRIBUTE_NAME);
	}
	
	/**
	 * Returns the download information for the given HTTP session. If the session does not contain
	 * download informations (session attribute does not exist), a new session attribute is created and
	 * filled.
	 * 
	 * @param session HTTP session
	 * 
	 * @return Map containing download informations
	 */
	private Map<String, FileData> getOrCreateDownloadDataMap( HttpSession session) {
		Map<String, FileData> map = getDownloadDataMap( session);
		
		// Do we already have such map?
		if( map == null) {
			if( logger.isInfoEnabled())
				logger.info( "No download information found in session, creating new data structure");
			
			// No create a new one and store it in session
			map = new HashMap<>();
			session.setAttribute( DownloadConstants.DOWNLOAD_DATA_SESSION_ATTRIBUTE_NAME, map);
		}
		
		return map;
	}
	
	/**
	 * Returns the download informations for the given HTTP session. If no such informations exist (session attribute
	 * does not exist), null is returned.
	 * 
	 * @param session HTTP session
	 * 
	 * @return Map containing download informations or null
	 */
	private Map<String, FileData> getDownloadDataMap( HttpSession session) {
		@SuppressWarnings("unchecked")
		Map<String, FileData> map = (Map<String, FileData>) session.getAttribute( DownloadConstants.DOWNLOAD_DATA_SESSION_ATTRIBUTE_NAME);
		
		return map;
	}

	@Override
	public FileData getDownloadData( String id, HttpSession session) throws DownloadIdNotFoundException {
		Map<String, FileData> map = getDownloadDataMap( session);
		
		if( map == null)
			throw new DownloadIdNotFoundException( id);
		
		FileData data = map.get( id);
		
		if( data == null)
			throw new DownloadIdNotFoundException( id);
		
		return data;
	}
}
