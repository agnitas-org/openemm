/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class ResourceBundleManager {
	private static final transient Logger logger = Logger.getLogger( ResourceBundleManager.class);
	
	private final ExtensionSystem extensionSystem;
	private final Map<String, Map<String, ResourceBundle>> pluginBundleCache;
	
	public ResourceBundleManager( ExtensionSystem extensionSystem) {
		this.extensionSystem = extensionSystem;
		this.pluginBundleCache = new HashMap<>();
	}
	
	public ResourceBundle getResourceBundle( String plugin, String bundleName) throws IOException {
		String bundleFileName = bundleName + ".properties";
		
		Map<String, ResourceBundle> bundleMap = this.pluginBundleCache.get( plugin);
		
		if( bundleMap == null) {
			bundleMap = new HashMap<>();
			
			this.pluginBundleCache.put( plugin, bundleMap);
		}
		
		ResourceBundle bundle = bundleMap.get( bundleFileName);
		
		if( bundle == null) {
			try (InputStream stream = this.extensionSystem.getPluginResource( plugin, bundleFileName)) {
				if (stream != null) {
	    			try {
	    				bundle = new PropertyResourceBundle( stream);
	    				
	   					bundleMap.put( bundleFileName, bundle);
	    			} catch( IOException e) {
	    				logger.error( "Error reading resource bundle '" + bundleFileName + "' for plugin '" + plugin + "'", e);
	    			}
				} else {
					logger.warn( "i18n bundle '" + bundleFileName + "' not found for plugin '" + plugin + "'");
				}
			}
		}

		return bundle;
	}
}
