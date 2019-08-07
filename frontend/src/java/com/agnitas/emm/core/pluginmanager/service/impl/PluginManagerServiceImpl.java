/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.pluginmanager.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.extension.ExtensionSystem;
import org.agnitas.emm.extension.data.PluginDetail;
import org.agnitas.emm.extension.data.PluginStatus;
import org.agnitas.emm.extension.data.PluginStatusReport;
import org.agnitas.emm.extension.exceptions.DatabaseScriptException;
import org.agnitas.emm.extension.exceptions.MissingPluginManifestException;
import org.agnitas.emm.extension.exceptions.RemovingSystemPluginNotAllowedException;
import org.agnitas.emm.extension.exceptions.UnknownPluginException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.java.plugin.JpfException;
import org.java.plugin.PluginLifecycleException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.agnitas.emm.core.pluginmanager.dto.PluginDto;
import com.agnitas.emm.core.pluginmanager.dto.PluginEntryDto;
import com.agnitas.emm.core.pluginmanager.dto.PluginUploadDto;
import com.agnitas.emm.core.pluginmanager.service.PluginManagerService;
import com.agnitas.service.ExtendedConversionService;

@Service("pluginManagerService")
public class PluginManagerServiceImpl implements PluginManagerService {
	
	private static final Logger logger = Logger.getLogger(PluginManagerServiceImpl.class);

    private ExtendedConversionService extendedConversionService;

    public PluginManagerServiceImpl(ExtendedConversionService extendedConversionService) {
        this.extendedConversionService = extendedConversionService;
    }

    @Override
    public PaginatedListImpl<PluginEntryDto> getAllPlugins(ExtensionSystem extensionSystem, int pageSize, int pageNumber, String sort, String order) {
        PluginStatusReport pluginStatusReport = extensionSystem.getPluginStatusReport();
        List<PluginEntryDto> pluginList = new ArrayList<>();
        if(Objects.nonNull(pluginStatusReport)) {
            pluginList = extendedConversionService.convert(new ArrayList<>(pluginStatusReport.getItems()), PluginStatus.class, PluginEntryDto.class);
        }
        return new PaginatedListImpl<>(pluginList, pluginList.size(), pageSize, pageNumber, sort, order);
    }

    @Override
    public PluginDto getPlugin(ExtensionSystem extensionSystem, String pluginId) throws UnknownPluginException {
        PluginDetail pluginDetails = extensionSystem.getPluginDetails(pluginId);
        return extendedConversionService.convert(pluginDetails, PluginDto.class);
    }
	
	@Override
	public void installPlugin(ExtensionSystem extensionSystem, PluginUploadDto plugin) throws DatabaseScriptException, JpfException, MissingPluginManifestException, IOException {
    	File uploadedFile = plugin.getFile();
		Assert.notNull(uploadedFile, "Plugin file was null");
		
		String absolutePath = uploadedFile.getAbsolutePath();
  
		try {
			logger.info("Installing plugin from file: " + absolutePath);
			 
			extensionSystem.installPlugin(absolutePath);
		} finally {
			logger.info("Releasing temporary file");
			uploadedFile.delete();
		}
	}
	
	@Override
	public void activatePlugin(ExtensionSystem extensionSystem, String pluginId) throws PluginLifecycleException, UnknownPluginException {
        checkPluginId(extensionSystem, pluginId);
        extensionSystem.activatePluginForStartup(pluginId);
	}
	
	@Override
	public void deactivatePlugin(ExtensionSystem extensionSystem, String pluginId) throws UnknownPluginException {
		checkPluginId(extensionSystem, pluginId);
        extensionSystem.deactivatePluginForStartup(pluginId);
	}
	
	@Override
	public void deletePlugin(ExtensionSystem extensionSystem, String pluginId) throws UnknownPluginException, RemovingSystemPluginNotAllowedException {
    	checkPluginId(extensionSystem, pluginId);
        extensionSystem.uninstallPlugin(pluginId);
	}
	
	private void checkPluginId(ExtensionSystem extensionSystem, String pluginId) throws UnknownPluginException {
    	if(StringUtils.isEmpty(pluginId) || !extensionSystem.isPluginExist(pluginId)) {
        	throw new UnknownPluginException(pluginId);
        }
	}
}
