/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.extensions;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.agnitas.util.FileUtils;
import org.agnitas.util.FileUtils.ZipEntryNotFoundException;
import org.apache.log4j.Logger;

import com.agnitas.emm.extension.impl.ComExtensionConstants;

/**
 * Installer class to install BIRT plugins correctly.
 */
class BirtPluginInstaller {
	
	/** Logger.  */
	private static final transient Logger logger = Logger.getLogger( BirtPluginInstaller.class);
	
	/** Path where the ZIP files are located. */
	private final String pluginZipPath;
	
	/** Base path of the report designs. */
	private final String reportDesignPath;
	
	/** Path where the code is located. */
	private final String scriptlibPath;
	
	/**
	 * Creates an installer instance.
	 * 
	 * @param pluginZipPath path to plugin ZIP
	 * @param reportDesignPath path to report designs
	 * @param scriptlibPath path to script libs
	 */
	BirtPluginInstaller( String pluginZipPath, String reportDesignPath, String scriptlibPath) {
		this.pluginZipPath = FileUtils.removeTrailingSeparator( pluginZipPath) + "/";
		this.reportDesignPath = FileUtils.removeTrailingSeparator( reportDesignPath) + "/";
		this.scriptlibPath = FileUtils.removeTrailingSeparator( scriptlibPath) + "/";
		
		if (pluginZipPath == null) {
			throw new NullPointerException("Plugin ZIP path cannot be null");
		} else {
			File dir = new File(pluginZipPath);
		
			// Check, if directory does exist.
			if (!dir.exists()) {
				// If not, create complete path
				if(!dir.mkdirs()) {
					// Something went wrong. 
					throw new RuntimeException("Cannot create missing plugin ZIP path '" + pluginZipPath + "'");
				} else {
					// Finally, check existence of directory again (just to be sure)
					if(!dir.exists()) {
						throw new RuntimeException("Plugin ZIP path '" + pluginZipPath + "' does not exist.");
					}
				}
			} else {
				if(!dir.isDirectory()) {
					throw new RuntimeException("Plugin ZIP path '" + pluginZipPath + "' exists, but is not a directory.");					
				}
			}
		}

		if (reportDesignPath == null) {
			throw new NullPointerException("Reportdesign path cannot be null");
		} else {
			File dir = new File(reportDesignPath);

			// Check, if directory does exist.
			if (!dir.exists()) {
				// If not, create complete path
				if(!dir.mkdirs()) {
					// Something went wrong. 
					throw new RuntimeException("Cannot create missing reportdesign path '" + reportDesignPath + "'");
				} else {
					// Finally, check existence of directory again (just to be sure)
					if(!dir.exists()) {
						throw new RuntimeException("Reportdesign path '" + reportDesignPath + "' does not exist");
					}
				}
			} else {
				if(!dir.isDirectory()) {
					throw new RuntimeException("Reportdesign path '" + reportDesignPath + "' exists, but is not a directory.");					
				}
			}
		}

		if (scriptlibPath == null) {
			throw new NullPointerException("Scriptlib path cannot be null");
		} else if (!new File(pluginZipPath).exists()) {
			File dir = new File(scriptlibPath);

			// Check, if directory does exist.
			if (!dir.exists()) {
				// If not, create complete path
				if(!dir.mkdirs()) {
					// Something went wrong. 
					throw new RuntimeException("Cannot create missing scriptlib path '" + scriptlibPath + "'");
				} else {
					// Finally, check existence of directory again (just to be sure)
					if(!dir.exists()) {
						throw new RuntimeException("Scriptlib path '" + scriptlibPath + "' does not exist");
					}
				}
			} else {
				if(!dir.isDirectory()) {
					throw new RuntimeException("Scriptlib path '" + scriptlibPath + "' exists, but is not a directory.");					
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("plugin ZIP path: " + pluginZipPath);
			logger.debug("report design path: " + reportDesignPath);
			logger.debug("scriptlib path: " + scriptlibPath);
		}
	}
	
	/**
	 * Install plugins on BIRT instance.
	 */
	void installPlugins() {
		removeAllPluginInstallations();		// Required to remove files of uninstalled plugins
		
		File zipDirectory = new File( pluginZipPath);
		if (!zipDirectory.exists()) {
			logger.error("BIRT Plugin directory '" + pluginZipPath + "' is missing");
		}
		File[] zipFiles = zipDirectory.listFiles( new ZipFilenameFilter());
		
		for( File zipFile : zipFiles) {
			if( logger.isInfoEnabled()) 
				logger.info( "Installing plugin from ZIP file: " + zipFile.getAbsolutePath());
		
			try {
				installPlugin( zipFile);
				
				if( logger.isInfoEnabled()) 
					logger.info( "Plugin installed successfully");
			} catch( Exception e) {
				logger.error( "Error installing plugin from ZIP file: " + zipFile.getAbsolutePath(), e);
			}
		}
	}
	
	/**
	 * Removes all installed plugin files except the ZIP file.
	 * Calling this method is required to remove files for uninstalled plugins. That is the hard-but-easy way.
	 */
	void removeAllPluginInstallations() {
		if( logger.isInfoEnabled())
			logger.info( "Removing all report designs");
		FileUtils.removeRecursively( this.reportDesignPath);	// Remove all report designs
		
		if( logger.isInfoEnabled())
			logger.info( "Removing scriptlibs for plugins");
		
		File scriptlibDirectory = new File( this.scriptlibPath);
		File[] files = scriptlibDirectory.listFiles( new ScriptlibFilenameFilter());
		for( File file : files) {
			if( logger.isDebugEnabled())
				logger.debug( "Removing scriptlib " + file.getAbsolutePath());
			
			file.delete();
		}
	}
	
	/**
	 * Install single plugin on BIRT instance.
	 * 
	 * @param file ZIP file of BIRT plugin
	 * 
	 * @throws IOException on errors installing plugin
	 */
	private void installPlugin(File file) throws IOException {
		try (ZipFile zipFile = new ZipFile(file)) {
			String pluginName = extractPluginName( zipFile.getName());
			
			if (logger.isDebugEnabled()) {
				logger.info( "plugin name: " + pluginName);
			}
		
			List<ZipEntry> zipEntries = zipEntriesAsList( zipFile);
			
			try {
				installScriptlibs( zipFile, zipEntries, pluginName);
				installReportDesigns( zipFile, zipEntries, pluginName);
			} catch( Exception e) {
				logger.error( "Error installing plugin from file: " + file.getAbsolutePath());
				
				// TODO: Remove installed files here...
			}
		}
	}
	
	/**
	 * Install script libraries from plugin.
	 * 
	 * @param zipFile ZIP file containing BIRT plugin
	 * @param zipEntries archived files in plugin
	 * @param pluginName name of plugin
	 * 
	 * @throws ZipEntryNotFoundException on accessing ZIP entry that does not exist
	 * @throws IOException on errors installing scriptlib
	 */
	private void installScriptlibs( ZipFile zipFile, List<ZipEntry> zipEntries, String pluginName) throws ZipEntryNotFoundException, IOException {
		final String scriptlibBase = FileUtils.removeTrailingSeparator( ComExtensionConstants.PLUGIN_BIRT_INSTALLED_ZIP_SCRIPTLIB_BASE) + "/";
		
		for( ZipEntry zipEntry : zipEntries) {
			if( zipEntry.getName().startsWith( scriptlibBase + "birt-plugin-") && zipEntry.getName().endsWith( ".jar")) {
				installScriptlib( zipFile, zipEntry.getName(), pluginName);
			}
		}
	}
	
	/**
	 * Install single scriptlib file.
	 * 
	 * @param zipFile ZIP file containing BIRT plugin
	 * @param entryName name of scriptlib file to install
	 * @param pluginName name of plugin
	 * 
	 * @throws ZipEntryNotFoundException on accessing ZIP entry that does not exist
	 * @throws IOException on errors installing scriptlib file
	 */
	private void installScriptlib( ZipFile zipFile, String entryName, String pluginName) throws ZipEntryNotFoundException, IOException {
		String destinationFileName = translateScriptlibName( entryName);
		File destinationFile = new File( destinationFileName);
		
		if( logger.isInfoEnabled()) 
			logger.info( "Installing scriptlib " + entryName + " to " + destinationFileName);

		FileUtils.createPathToFile( destinationFile);		
		FileUtils.extractZipEntryToFile(zipFile, entryName, destinationFile);
	}
	
	/**
	 * Translate name of sciptlib file.
	 * 
	 * @param zipEntryName name of scriptlib file in plugin ZIP
	 * 
	 * @return translated scriptlib filename
	 */
	private String translateScriptlibName( String zipEntryName) {
		String zipBaseDirectory = FileUtils.removeTrailingSeparator( ComExtensionConstants.PLUGIN_BIRT_INSTALLED_ZIP_SCRIPTLIB_BASE) + "/";
		
		assert zipEntryName.startsWith( zipBaseDirectory);
		
		String zipRelativeName = zipEntryName.substring( zipBaseDirectory.length());
		
		return this.scriptlibPath + zipRelativeName;
	}
	
	/**
	 * Install report designs from BIRT plugin.
	 * 
	 * @param zipFile ZIP file containing BIRT plugin
	 * @param zipEntries files archvied in ZIP
	 * @param pluginName name of plugin
	 * 
	 * @throws IOException on errors installing reportdesigns
	 * @throws ZipEntryNotFoundException on accessing ZIP entry that does not exist
	 */
	private void installReportDesigns( ZipFile zipFile, List<ZipEntry> zipEntries, String pluginName) throws IOException, ZipEntryNotFoundException {
		final String rptDesignBase = FileUtils.removeTrailingSeparator( ComExtensionConstants.PLUGIN_BIRT_INSTALLED_ZIP_RPTDESIGN_BASE) + "/";
		
		for( ZipEntry zipEntry : zipEntries) {
			if( zipEntry.getName().startsWith( rptDesignBase) && zipEntry.getName().endsWith( ".rptdesign")) {
				installReportDesign( zipFile, zipEntry.getName(), pluginName);
			}
		}
	}
	
	/**
	 * Install single report design.
	 * 
	 * @param zipFile ZIP file containing BIRT plugin
	 * @param entryName name of file in ZIP to install
	 * @param pluginName name of plugin
	 * 
	 * @throws IOException on errors installing report design
	 * @throws ZipEntryNotFoundException on accessing ZIP entry, that does not exist
	 */
	private void installReportDesign( ZipFile zipFile, String entryName, String pluginName) throws IOException, ZipEntryNotFoundException {
		String destinationFileName = translateReportDesignName( entryName, pluginName);
		File destinationFile = new File( destinationFileName);
		
		if( logger.isInfoEnabled()) 
			logger.info( "Installing report design " + entryName + " to " + destinationFileName);

		FileUtils.createPathToFile( destinationFile);		
		FileUtils.extractZipEntryToFile(zipFile, entryName, destinationFile);
	}
	
	/**
	 * Translate name of report design.
	 * 
	 * @param zipEntryName name of report design file in ZIP file
	 * @param pluginName name of plugin
	 * 
	 * @return translated report design file name
	 */
	private String translateReportDesignName( String zipEntryName, String pluginName) {
		String zipBaseDirectory = FileUtils.removeTrailingSeparator( ComExtensionConstants.PLUGIN_BIRT_INSTALLED_ZIP_SCRIPTLIB_BASE) + "/";
		
		assert zipEntryName.startsWith( zipBaseDirectory);
		
		String zipRelativeName = zipEntryName.substring( zipBaseDirectory.length());
		
		return this.reportDesignPath + pluginName + "/" + zipRelativeName;
	}
	
	/**
	 * List all files archived in ZIP file.
	 * 
	 * @param zipFile ZIP file
	 * 
	 * @return list of ZIP entries contained in the ZIP file
	 */
	private static List<ZipEntry> zipEntriesAsList( ZipFile zipFile) {
		List<ZipEntry> list = new Vector<>();
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		
		while( entries.hasMoreElements())
			list.add( entries.nextElement());
		
		
		return list;
	}
	
	/**
	 * Extracts plugin name from filename.
	 * 
	 * @param filename filename
	 * 
	 * @return plugin name
	 */
	private static String extractPluginName( String filename) {
		int lastSeparatorIndex = filename.lastIndexOf( '/');
		
		String baseName = (lastSeparatorIndex == -1) ? filename : filename.substring( lastSeparatorIndex + 1);	// Cut off leading path
		String pluginName = baseName.substring( 0, baseName.length() - 4); // Cut off trailing ".zip"
		
		return pluginName;
	}
}
