/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.agnitas.emm.extension.PluginInstaller;
import org.agnitas.emm.extension.exceptions.DatabaseScriptException;
import org.agnitas.emm.extension.exceptions.MissingPluginManifestException;
import org.agnitas.util.FileUtils;
import org.agnitas.util.FileUtils.ZipEntryNotFoundException;
import org.apache.log4j.Logger;
import org.java.plugin.registry.ManifestInfo;
import org.java.plugin.registry.ManifestProcessingException;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginRegistry;

public class PluginInstallerImpl implements PluginInstaller {

	private static final Logger logger = Logger.getLogger(PluginInstallerImpl.class);

	private final ExtensionSystemConfiguration configuration;
	private final JspRestoreUtil jspRestoreUtil;
	private final DatabaseScriptExecutor databaseScriptExecutor;
	
	public PluginInstallerImpl(ExtensionSystemConfiguration configuration, JspRestoreUtil jspRestoreUtil, DatabaseScriptExecutor databaseScriptExecutor) {
		this.configuration = configuration;
		this.jspRestoreUtil = jspRestoreUtil;
		this.databaseScriptExecutor = databaseScriptExecutor;
	}
	
	@Override
	public String installPlugin(String filename) throws IOException, MissingPluginManifestException, DatabaseScriptException {
		if(logger.isDebugEnabled()) {
			logger.debug("Plugin ZIP file: " + filename);
		}
		
		ZipFile zipFile = new ZipFile(filename);
		PluginRegistry registry = org.java.plugin.ObjectFactory.newInstance().createRegistry();
		
		Collection<PluginDescriptor> list = registry.getPluginDescriptors();
		for(PluginDescriptor desc : list) {
			logger.fatal("Plugin descriptor: " + desc.getId());
		}
		
		if(logger.isInfoEnabled()) {
			logger.info("Installing plugin from file " + filename);
		}
		
		ManifestInfo manifestInfo;
		try {
			manifestInfo = readPluginDataFromManifest(zipFile, registry);
		} catch(IOException e) {
			logger.warn("Error reading manifest", e);
			
			throw new MissingPluginManifestException("Error reading manifest", e);
		} catch(ManifestProcessingException e) {
			logger.warn("Error processing manifest", e);
			
			throw new MissingPluginManifestException("Error processing manifest", e);
		}


		try {
			installPlugin(zipFile, manifestInfo.getId());
		} catch(ZipEntryNotFoundException e) {
			logger.warn("Error reading file from ZIP", e);
			
			throw new IOException("Error reading file from ZIP", e);
		} catch(DatabaseScriptException e) {
			logger.error("Error executing database script", e);
			
			throw e;
		}
		
		return manifestInfo.getId();
	}

	private void installPlugin(ZipFile zipFile, String pluginId) throws IOException, ZipEntryNotFoundException, DatabaseScriptException {
		if(logger.isInfoEnabled()) {
			logger.info("Installing files for plugin " + pluginId);
		}
		
		
		try {
			installFilesFromPluginZip(zipFile, pluginId, configuration);

			postInstallationProcessing(pluginId, configuration);
		} catch(IOException e) {
			logger.error("Error installing plugin files", e);
			
			removePluginFiles(pluginId);
			
			throw e;
		} catch(ZipEntryNotFoundException e) {
			logger.error("Error reading file from ZIP", e);
			
			removePluginFiles(pluginId);
			
			throw e;
		} catch(DatabaseScriptException e) {
			logger.error("Error executing database script", e);
			
			removePluginFiles(pluginId);
			
			throw e;
		}
	}
	
	protected void installFilesFromPluginZip(ZipFile zipFile, String pluginId, ExtensionSystemConfiguration configuration) throws IOException, ZipEntryNotFoundException, DatabaseScriptException {
		String pluginDirectory = this.configuration.getPluginDirectory(pluginId);
		String jspBackupDirectory = this.configuration.getJspBackupDirectory(pluginId);
		String jspDirectory = this.configuration.getJspWorkingDirectory(pluginId);
		String databaseScriptsDirectory = this.configuration.getDatabaseScriptsDirectory(pluginId);
		

		if(logger.isDebugEnabled()) {
			logger.debug("plugin directory: " + pluginDirectory);
			logger.debug("JSP backup directory: " + jspBackupDirectory);
			logger.debug("JSP working directory: " + jspDirectory);
			logger.debug("Directory for database scripts: " + databaseScriptsDirectory);
		}
		
		installFile(zipFile, "plugin.xml", pluginDirectory, "plugin.xml");
		installJsps(zipFile, jspBackupDirectory);
		installClasspath(zipFile, pluginDirectory);
		installDatabaseScripts(zipFile, databaseScriptsDirectory);
	}

	protected void postInstallationProcessing(String pluginId, ExtensionSystemConfiguration configuration) throws DatabaseScriptException, IOException {
		this.databaseScriptExecutor.execute(new File(this.configuration.getDatabaseInstallScript(pluginId)), pluginId);
		
		this.jspRestoreUtil.createWorkingJspsFromBackupDirectory(pluginId);
	}
	
	private ManifestInfo readPluginDataFromManifest(ZipFile zipFile, PluginRegistry registry) throws IOException, ManifestProcessingException, MissingPluginManifestException {
		File tempManifestFile = extractManifestToTemporaryFile(zipFile);
		
		try {
			URL tempManifestUrl = tempManifestFile.toURI().toURL();
			
			return registry.readManifestInfo(tempManifestUrl);
		} finally {
			tempManifestFile.delete();
		}
	}
	
	private File extractManifestToTemporaryFile(ZipFile zipFile) throws IOException, MissingPluginManifestException {
		
		try {
			return FileUtils.extractZipEntryToTemporaryFile(zipFile, "plugin.xml", "emm-jpf-");
		} catch(ZipEntryNotFoundException e) {
			logger.error("No plugin manifest found", e);
			
			throw new MissingPluginManifestException("No plugin manifest found", e);
		}
	}
	
	public void removePluginFiles(String pluginId) {
		if(logger.isInfoEnabled()) {
			logger.info("Removing files for plugin " + pluginId);
		}
		
		try {
			this.databaseScriptExecutor.execute(new File(this.configuration.getDatabaseDeinstallScript(pluginId)), pluginId);
		} catch(DatabaseScriptException e) {
			logger.error("Error executing deinstallation script", e);
		}

		FileUtils.removeRecursively(this.configuration.getJspBackupDirectory(pluginId));
		FileUtils.removeRecursively(this.configuration.getJspWorkingDirectory(pluginId));
		FileUtils.removeRecursively(this.configuration.getDatabaseScriptsDirectory(pluginId));
		FileUtils.removeRecursively(this.configuration.getPluginDirectory(pluginId));
	}

	private void installJsps(ZipFile zipFile, String baseDirectory) throws IOException, ZipEntryNotFoundException {
		if(logger.isInfoEnabled()) {
			logger.info("Installing JPSs");
		}

		installFilesFromSubdirectory(zipFile, "jsp", baseDirectory, false);
	}
	
	private void installClasspath(ZipFile zipFile, String baseDirectory) throws IOException, ZipEntryNotFoundException {
		if(logger.isInfoEnabled()) {
			logger.info("Installing class path");
		}
			
		installFilesFromSubdirectory(zipFile, "bin", baseDirectory, true);
	}
	
	private void installDatabaseScripts(ZipFile zipFile, String baseDirectory) throws IOException, ZipEntryNotFoundException {
		if(logger.isInfoEnabled()) {
			logger.info("Installing database scripts");
		}
			
		installFilesFromSubdirectory(zipFile, "db", baseDirectory, true);
	}
	
	/**
	 * Install files from a subdirectory in a ZIP file to a directory in the file system.
	 * 
	 * With <i>excludeUnderscoredDirectories</i> set to true, a special handling is enabled:
	 * All directories directly located in the specified ZIP subdirectory are checked, if their
	 * names begin with an underscore ("_"). If so, this directory is skipped.
	 * 
	 * Here an example: <i>subDirectoryName</i> is set to "example". These files and directories where copied:
	 * <ul>
	 * 	<li>example/fileA.txt</li>
	 * 	<li>example/dirA/fileB.txt</li>
	 * 	<li>example/dirA/_dirB/fileC.txt (<b>Note:</b> <i>_dirB</i> is not directly located in <i>example</i></li>)
	 * </ul>
	 * 
	 * These files and directories where not copied:
	 * <ul>
	 * 	<li>example/_dirC	(and all files and directories in <i>_dirC</i>)
	 * </ul>
	 * 
	 * @param zipFile the ZIP file containing the files to install
	 * @param subdirectoryName name of the subdirectory in the ZIP file
	 * @param baseDirectory name of the base directory in the file system
	 * @param excludeUnderscoredDirectories when true, all directories beginning with an underscore and that are directly located in the subdirectory of the ZIP are ignored
	 * @throws IOException on errors reading or writing data
	 * @throws ZipEntryNotFoundException when a specified ZIP entry was not found
	 */
	private void installFilesFromSubdirectory(ZipFile zipFile, String subdirectoryName, String baseDirectory, boolean excludeUnderscoredDirectories) throws IOException, ZipEntryNotFoundException {
		if(logger.isDebugEnabled()) {
			logger.debug("installing files from ZIP subdirectory (subdirectory in ZIP: " + subdirectoryName + ", target base directory: " + baseDirectory + ", exclude underscored directories: " + (excludeUnderscoredDirectories ? "yes" : "no"));
		}
		
		String entryNameStart = FileUtils.removeTrailingSeparator(subdirectoryName) + "/";
		
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		ZipEntry entry = null;
		String translatedName;
		
		while(entries.hasMoreElements()) {
			entry = entries.nextElement();

			if(!entry.getName().startsWith(entryNameStart))
				continue;
			
			if(excludeUnderscoredDirectories && entry.getName().startsWith(entryNameStart + "_")) {
				if(logger.isInfoEnabled())
					logger.info("skipping underscored directory " + entry.getName());
				
				continue;
			}
			
			translatedName = entry.getName().substring(entryNameStart.length());
			
			if(!entry.isDirectory()) {
				if(logger.isDebugEnabled()) {
					logger.debug("Installing " + entry.getName() + " to " + baseDirectory);
				}
				
				translatedName = entry.getName().substring(entryNameStart.length());
				
				installFile(zipFile, entry.getName(), baseDirectory, translatedName);
			} else {
				String dirName = baseDirectory + File.separator + translatedName;
				
				if(logger.isDebugEnabled()) {
					logger.debug("Creating directory " + dirName);
				}
				
				FileUtils.createPath(dirName);
			}
		}
		
	}
	
	private void installFile(ZipFile zipFile, String entryName, String baseDirectory, String filename) throws IOException, ZipEntryNotFoundException {
		File destinationFile = new File(baseDirectory + File.separator + filename);
		
		if(logger.isDebugEnabled()) {
			logger.debug("Installing ZIP entry " + entryName + " to " + destinationFile.getAbsolutePath());
		}
		
		try {
			FileUtils.createPathToFile(destinationFile);
			FileUtils.extractZipEntryToFile(zipFile, entryName, destinationFile);
		} catch(ZipEntryNotFoundException e) {
			logger.error("Specified ZIP entry " + entryName + " not found", e);
			
			destinationFile.delete();
			
			throw e;
		} catch(IOException e) {
			logger.error("Error installing file " + filename, e);
			
			destinationFile.delete();
			
			throw e;
		}
	}

	@Override
	public void uninstallPlugin(String pluginId) {
		uninstallPlugin(pluginId, this.configuration);
	}
	
	protected void uninstallPlugin(String pluginId, ExtensionSystemConfiguration configuration) {
		removePluginFiles(pluginId);
	}
}
