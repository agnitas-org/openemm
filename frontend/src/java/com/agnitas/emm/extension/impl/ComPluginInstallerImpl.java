/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.extension.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.agnitas.emm.extension.exceptions.DatabaseScriptException;
import org.agnitas.emm.extension.impl.DatabaseScriptExecutor;
import org.agnitas.emm.extension.impl.ExtensionSystemConfiguration;
import org.agnitas.emm.extension.impl.JspRestoreUtil;
import org.agnitas.emm.extension.impl.PluginInstallerImpl;
import org.agnitas.util.FileUtils;
import org.agnitas.util.FileUtils.ZipEntryNotFoundException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.agnitas.util.ProcessUtils;

public class ComPluginInstallerImpl extends PluginInstallerImpl {

	private static final transient Logger logger = Logger.getLogger( ComPluginInstallerImpl.class);

	public ComPluginInstallerImpl(ExtensionSystemConfiguration configuration, JspRestoreUtil jspRestoreUtil, DatabaseScriptExecutor databaseScriptExecutor) {
		super( configuration, jspRestoreUtil, databaseScriptExecutor);
	}

	@Override
	protected void installFilesFromPluginZip(ZipFile zipFile, String pluginId,
			ExtensionSystemConfiguration configuration) throws IOException,
			ZipEntryNotFoundException, DatabaseScriptException {
		super.installFilesFromPluginZip(zipFile, pluginId, configuration);

		String birtPluginBaseDirectory = ((ComExtensionSystemConfiguration) configuration).getBirtPluginBaseDirectory();
		
		if( logger.isDebugEnabled()) {
			logger.debug( "BIRT plugin base directory: " + birtPluginBaseDirectory);
		}
		
		installRemoteBirtFiles( zipFile, pluginId, birtPluginBaseDirectory, ((ComExtensionSystemConfiguration) configuration).getBirtHost(), ((ComExtensionSystemConfiguration) configuration).getBirtHostUser());
	}
	
	@Override
	protected void uninstallPlugin( String pluginId, ExtensionSystemConfiguration configuration) {
		super.uninstallPlugin( pluginId, configuration);
		
		String birtHostUser = ((ComExtensionSystemConfiguration) configuration).getBirtHostUser();
		String birtHost = ((ComExtensionSystemConfiguration) configuration).getBirtHost();
		String birtPluginBaseDirectory = ((ComExtensionSystemConfiguration) configuration).getBirtPluginBaseDirectory();
		
		String filename = birtPluginBaseDirectory + "/" + pluginId + ".zip";
		
		try {
			sshExecuteCommand( birtHostUser, birtHost, "[ -e " + filename + " ] && rm " + filename);
		} catch( IOException e) {
			logger.warn( "Error removing remote plugin files: " + birtPluginBaseDirectory + "/" + pluginId + ".zip", e);
		}
	}
	
	private boolean hasFilesForBirt( ZipFile zipFile) {
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		
		if( logger.isInfoEnabled()) {
			logger.info( "Searching for files for BIRT");
		}
		
		ZipEntry entry;
		while( entries.hasMoreElements()) {
			entry = entries.nextElement();
			
			// Entry is a directory? Skip further processing and go to next entry
			if( entry.isDirectory())
				continue;
			
			if( entry.getName().startsWith( ComExtensionConstants.PLUGIN_BIRT_ZIP_BASE)) {
				if( logger.isInfoEnabled()) {	
					logger.info( "Found file to install to BIRT: " + entry.getName());
				}
				
				return true;
			} else {
				if( logger.isDebugEnabled()) {
					logger.debug( "Not a file for BIRT: " + entry.getName());
				}
			}
		}
		
		if( logger.isInfoEnabled()) {
			logger.info( "No files found for BIRT");	
		}
		
		return false;
	}
	
	private void installRemoteBirtFiles( ZipFile zipFile, String pluginId, String remoteDirectory, String host, String hostUser) throws IOException {
		// Check, if we have something to do here
		if( !hasFilesForBirt( zipFile)) {
			if( logger.isInfoEnabled())
				logger.info( "No files found to install to BIRT");
			
			return;
		}
		
		File temporaryFile = File.createTempFile( "birt-plugin-", ".zip");
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Temporary file for BIRT files is: " + temporaryFile.getAbsolutePath());
		}
		
		try (FileOutputStream fileOutputStream = new FileOutputStream(temporaryFile)) {
			fillBirtZipFile( fileOutputStream, zipFile, pluginId);
		}

		// Transfer ZIP to BIRT
		transferZipToBirt( temporaryFile, host, hostUser, remoteDirectory, pluginId);
		
		// Delete file
		temporaryFile.delete();
	}
	
	private void transferZipToBirt( File zipFile, String birtHost, String birtHostUser, String remoteDirectory, String pluginId) throws IOException {
		if( logger.isInfoEnabled()) {
			logger.info( "Transfering ZIP file to BIRT");
		}
		
		StringBuffer remotePart = new StringBuffer();
		remotePart.append( birtHostUser);
		remotePart.append( '@');
		remotePart.append( birtHost);
		remotePart.append( ':');
		remotePart.append( remoteDirectory);
		remotePart.append( '/');
		remotePart.append( pluginId);
		remotePart.append( ".zip");
		
		if( logger.isDebugEnabled()) {
			logger.debug( "ZIP file is: " + zipFile.getAbsolutePath());
			logger.debug( "Remote is: " + remotePart.toString());
		}

		scpToBirt( zipFile.getAbsolutePath(), remotePart.toString());
		
		// In some setups, we loose the file permissions, so we have to set them manually
		sshExecuteCommand( birtHostUser, birtHost, "chmod 660 " + remoteDirectory + "/" + pluginId + ".zip");
	}
	
	private int scpToBirt( String localFile, String remotePart) throws IOException {
		
		String[] scpCommand = new String[] { "scp", localFile, remotePart };
		
		try {
			Process scpProcess = Runtime.getRuntime().exec( scpCommand);
			
			int exitValue = scpProcess.waitFor();
			
			if( logger.isDebugEnabled()) {
				ProcessUtils.logProcessOutput(scpProcess, logger, Level.INFO, "stdout for scp", "stderr for scp");
				logger.info( "'scp' terminated with exit code " + exitValue);
			}
			
			return exitValue;
		} catch( InterruptedException e) {
			logger.error( "'scp' interrupted", e);
			
			throw new IOException( e);	// TODO: Bad hack!
		} catch( IOException e) {
			logger.error( "Error calling 'scp'", e);
			
			throw e;
		}
	}
	
	private int sshExecuteCommand( String username, String host, String command) throws IOException {
		String[] sshCommand = new String[] { "ssh", username + "@" + host, command };
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Executing remote command: " + command);
		}
		
		try {
			Process scpProcess = Runtime.getRuntime().exec( sshCommand);
			
			int exitValue = scpProcess.waitFor();
			
			if( logger.isDebugEnabled()) {
				ProcessUtils.logProcessOutput(scpProcess, logger, Level.INFO, "stdout for ssh", "stderr for ssh");
				logger.info( "'ssh' terminated with exit code " + exitValue);
			}
			
			return exitValue;
		} catch( InterruptedException e) {
			logger.error( "'ssh' interrupted", e);
			
			throw new IOException( e);	// TODO: Bad hack!
		} catch( IOException e) {
			logger.error( "Error calling 'ssh'", e);
			
			throw e;
		}
	}
	
	private void fillBirtZipFile( FileOutputStream fileOutputStream, ZipFile zipFile, String pluginId) throws IOException {
		final String subdirectoryName = ComExtensionConstants.PLUGIN_BIRT_ZIP_BASE;
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Creating BIRT zip file for plugin '" + pluginId + "'");
		}
		
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
			String entryNameStart = FileUtils.removeTrailingSeparator( subdirectoryName) + "/";
			
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			ZipEntry entry = null;
			String translatedName;

			while( entries.hasMoreElements()) {
				entry = entries.nextElement();
 
				if( !entry.getName().startsWith( entryNameStart))
					continue;
				
				translatedName = entry.getName().substring( entryNameStart.length());
				
				if( !entry.isDirectory()) {
					if( logger.isDebugEnabled()) {
						logger.debug( "Packing " + entry.getName() + " as " + translatedName);
					}
					
					// Transfer file to temporary ZIP file
					try (InputStream inputStream = zipFile.getInputStream(entry)) {
						zipOutputStream.putNextEntry( new ZipEntry( translatedName));
						try {
							FileUtils.streamToStream( inputStream, entry.getSize(), zipOutputStream);
						} finally {
							zipOutputStream.closeEntry();
						}
					}
				}
			}
		}
	}
}
