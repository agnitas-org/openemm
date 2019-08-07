/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.impl;

import java.io.File;
import java.io.IOException;

import org.agnitas.util.FileUtils;
import org.apache.log4j.Logger;

public class JspRestoreUtil {
	
	private static final transient Logger logger = Logger.getLogger( JspRestoreUtil.class);
	
	private final ExtensionSystemConfiguration configuration;
	
	JspRestoreUtil( ExtensionSystemConfiguration configuration) {
		this.configuration = configuration;
	}
	
	void createWorkingJspsFromBackupDirectory( String pluginId) throws IOException {
		if( logger.isInfoEnabled()) {
			logger.info( "creating working JSP for plugin " + pluginId);
		}

		// Check, if directory exists
		File jspBackupDir = new File( this.configuration.getJspBackupDirectory( pluginId));
		if( !jspBackupDir.exists()) {
			// Directory does not exist, so exit
			if( logger.isInfoEnabled())
				logger.info( "No backup directory found for JSPs of plugin " + pluginId);
			
			return;
		}
			
		
		copyDirectoryContentRecursively( this.configuration.getJspBackupDirectory( pluginId), this.configuration.getJspWorkingDirectory(pluginId));
	}
	
	private void copyDirectoryContentRecursively( String sourceBaseDirectory, String destinationBaseDirectory) throws IOException {
		copyDirectoryContentRecursively( new File( sourceBaseDirectory), new File( destinationBaseDirectory));
	}
	
	private void copyDirectoryContentRecursively( File sourceDirectory, File destinationDirectory) throws IOException {
		
		if( logger.isDebugEnabled()) {
			logger.debug( "copy recursivly from " + sourceDirectory.getAbsolutePath() + " to " + destinationDirectory.getAbsolutePath());
		}
		
		destinationDirectory.mkdirs();
		
		File[] content = sourceDirectory.listFiles();
	
		for( File file : content) {
			if( file.isFile()) {
				if( logger.isDebugEnabled()) {
					logger.debug( "copying file " + file.getAbsolutePath());
				}
				
				FileUtils.copyFileToDirectory( file, destinationDirectory);
			} else if( file.isDirectory()) {
				if( logger.isDebugEnabled()) {
					logger.debug( "visiting directory " + file.getAbsolutePath());
				}
				
				copyDirectoryContentRecursively( file.getAbsolutePath(), destinationDirectory.getAbsolutePath() + File.separator + file.getName());
				
				if( logger.isDebugEnabled()) {
					logger.debug( "left directory " + file.getAbsolutePath());
				}
			}
		}
	}
	
}
