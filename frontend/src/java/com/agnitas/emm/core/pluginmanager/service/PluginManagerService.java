/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.pluginmanager.service;

import java.io.IOException;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.extension.ExtensionSystem;
import org.agnitas.emm.extension.exceptions.DatabaseScriptException;
import org.agnitas.emm.extension.exceptions.MissingPluginManifestException;
import org.agnitas.emm.extension.exceptions.RemovingSystemPluginNotAllowedException;
import org.agnitas.emm.extension.exceptions.UnknownPluginException;
import org.java.plugin.JpfException;
import org.java.plugin.PluginLifecycleException;

import com.agnitas.emm.core.pluginmanager.dto.PluginDto;
import com.agnitas.emm.core.pluginmanager.dto.PluginEntryDto;
import com.agnitas.emm.core.pluginmanager.dto.PluginUploadDto;

public interface PluginManagerService {

    PaginatedListImpl<PluginEntryDto> getAllPlugins(ExtensionSystem extensionSystem, int pageSize, int pageNumber, String sort, String order);

    PluginDto getPlugin(ExtensionSystem extensionSystem, String pluginId) throws UnknownPluginException;
	
	void installPlugin(ExtensionSystem extensionSystem, PluginUploadDto plugin) throws DatabaseScriptException, JpfException, MissingPluginManifestException, IOException;
	
	void activatePlugin(ExtensionSystem extensionSystem, String pluginId) throws PluginLifecycleException, UnknownPluginException;
	
	void deactivatePlugin(ExtensionSystem extensionSystem, String pluginId) throws UnknownPluginException;
	
	void deletePlugin(ExtensionSystem extensionSystem, String pluginId) throws UnknownPluginException, RemovingSystemPluginNotAllowedException;
}
