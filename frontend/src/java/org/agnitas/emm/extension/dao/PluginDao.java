/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.dao;

import org.agnitas.emm.extension.data.PluginData;
import org.agnitas.emm.extension.exceptions.UnknownPluginException;

/**
 * DAO interface to access plugin data.
 */
public interface PluginDao {
	
	/**
	 * Get plugin data from DB.
	 * 
	 * @param pluginId ID of the plugin
	 *
	 * @return data to given plugin ID
	 * 
	 * @throws UnknownPluginException if there is no data to the given ID
	 */
	public PluginData getPluginData( String pluginId) throws UnknownPluginException;
	
	/**
	 * Saves the plugin data to DB.
	 * 
	 * @param pluginData plugin data to save
	 */
	public void savePluginData( PluginData pluginData);

	/**
	 * Removes the plugin data from DB.
	 * 
	 * @param pluginId ID of plugin for removing data
	 */
	public void removePluginData(String pluginId);
}
