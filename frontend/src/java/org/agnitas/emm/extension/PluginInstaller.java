/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension;

import java.io.IOException;

import org.agnitas.emm.extension.exceptions.DatabaseScriptException;
import org.agnitas.emm.extension.exceptions.MissingPluginManifestException;

/**
 * Interface for component to install plugins.
 */
public interface PluginInstaller {
	/**
	 * Installs plugin from given ZIP file
	 * 
	 * @param filename name of ZIP file
	 * 
	 * @return ID of plugin
	 * 
	 * @throws IOException on errors install plugin
	 * @throws MissingPluginManifestException on errors with plugin manifest
	 * @throws DatabaseScriptException on errors executing database script
	 */
	String installPlugin( String filename) throws IOException, MissingPluginManifestException, DatabaseScriptException;

	/**
	 * Uninstalls plugin files.
	 * 
	 * @param pluginId ID of plugin to be removed
	 */
	void uninstallPlugin(String pluginId);
}
