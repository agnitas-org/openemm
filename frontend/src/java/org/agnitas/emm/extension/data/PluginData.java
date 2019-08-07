/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.data;

/**
 * Stored informations on a plugin.
 */
public interface PluginData {
	
	/**
	 * Returns the plugin ID
	 * 
	 * @return plugin ID
	 */
	public String getPluginId();
	
	/**
	 * Set the plugin ID
	 * 
	 * @param pluginId plugin ID
	 */
	public void setPluginId( String pluginId);
	
	/**
	 * Returns, if the plugin is to be activated on startup of the extension system.
	 *
	 * <b>Note: This plugin can be activated on startup, when another plugin depends on
	 * this plugin, even if this methods returns false.</b> 
	 * 
	 * @return true, if plugin is to be activated
	 */
	public boolean isActivatedOnStartup();
	
	/**
	 * Set if plugin is to be activated on startup of the extension system.
	 * 
	 * @param activatedOnStartup true, if plugin is to be activated.
	 */
	public void setActivatedOnStartup( boolean activatedOnStartup);
}
