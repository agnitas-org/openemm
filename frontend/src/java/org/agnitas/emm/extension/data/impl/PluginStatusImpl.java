/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.data.impl;

import java.net.URL;

import org.agnitas.emm.extension.data.PluginStatus;

public class PluginStatusImpl implements PluginStatus {

	private URL url;
	private String version;
	private String vendor;
	private String pluginName;
	private String pluginId;
	private String description;
	private boolean activated;
	
	@Override
	public URL getUrl() {
		return this.url;
	}
	
	public void setUrl( URL url) {
		this.url = url;
	}
	
	@Override
	public String getVersion() {
		return this.version;
	}
	
	public void setVersion( String version) {
		this.version = version;
	}
	
	@Override
	public String getVendor() {
		return this.vendor;
	}
	
	public void setVendor( String vendor) {
		this.vendor = vendor;
	}

	@Override
	public String getPluginName() {
		return this.pluginName;
	}
	
	public void setPluginName( String pluginName) {
		this.pluginName = pluginName;			
	}

	@Override
	public String getPluginId() {
		return this.pluginId;
	}
	
	public void setPluginId( String pluginId) {
		this.pluginId = pluginId;
	}
	
	@Override
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription( String description) {
		this.description = description;
	}

	@Override
	public boolean isActivated() {
		return this.activated;
	}
	
	public void setActivated( boolean activated) {
		this.activated = activated;
	}
}
