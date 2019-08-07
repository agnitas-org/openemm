/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.impl;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.PluginDescriptor;

class LocationTracker {
	private static final transient Logger logger = Logger.getLogger( LocationTracker.class);
	
	private final List<PluginLocation> pluginLocations;				// Contains *all* plugin locations
	private final List<PluginLocation> systemPluginLocations;		// Contains locations of system plugins only
	private final Map<URL, PluginLocation> manifestLocationMap;
	
	public LocationTracker() {
		if( logger.isDebugEnabled()) {
			logger.debug( "creating LocationTracker");
		}
		
		this.pluginLocations = new Vector<>();
		this.systemPluginLocations = new Vector<>();
		this.manifestLocationMap = new HashMap<>();
		
		if( logger.isDebugEnabled()) {
			logger.debug( "LocationTracker created");
		}
	}
	
	public void registerLocations( Collection<PluginLocation> locations, boolean systemPlugins) {
		for( PluginLocation location : locations) 
			registerLocation( location, systemPlugins);
	}
	
	public void registerLocation( PluginLocation location, boolean isSystemPlugin) {
		
		if( logger.isInfoEnabled()) {
			logger.info( "registering plugin location (context = " + location.getContextLocation() + ", manifest = " + location.getManifestLocation() + ", system plugin = " + (isSystemPlugin ? "yes" : "no") + ")");
		}
		
		if( manifestLocationMap.containsKey( location.getManifestLocation())) {
			if( logger.isInfoEnabled())
				logger.info( "Location already registered - plugin seems to be reinstalled)");
			
			return;
		}
		
		this.pluginLocations.add( location);
		this.manifestLocationMap.put( location.getManifestLocation(), location);
		if( isSystemPlugin) {
			this.systemPluginLocations.add( location);
		}
	}

	public Collection<PluginLocation> getPluginLocations() {
		return this.pluginLocations;
	}

	public void unregisterLocation( PluginDescriptor descriptor) {
		URL manifestLocation = descriptor.getLocation();
		
		if( logger.isInfoEnabled()) {
			logger.info( "Unregistering location for manifest " + manifestLocation);
		}

		PluginLocation pluginLocation = this.manifestLocationMap.get( manifestLocation);
		
		if( pluginLocation == null) {
			logger.warn( "No plugin location for manifest " + manifestLocation);
		} else {
			if( isSystemPluginLocation( pluginLocation)) {
				logger.warn( "Attempt to unregister location of system plugin: " + pluginLocation.getContextLocation());
			} else {
				this.pluginLocations.remove( pluginLocation);
				this.manifestLocationMap.remove( manifestLocation);
			}
		}
	}
	
	public boolean isSystemPluginLocation( PluginLocation location) {
		return this.systemPluginLocations.contains( location);
	}
	
	public boolean isSystemPlugin( PluginDescriptor descriptor) {
		URL manifestLocation = descriptor.getLocation();
		PluginLocation pluginLocation = this.manifestLocationMap.get( manifestLocation);

		return isSystemPluginLocation( pluginLocation);
	}
}
