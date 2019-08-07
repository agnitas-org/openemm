/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.pluginmanager.web;

import org.agnitas.emm.extension.ExtensionSystem;
import org.agnitas.emm.extension.exceptions.MissingPluginManifestException;
import org.agnitas.emm.extension.exceptions.RemovingSystemPluginNotAllowedException;
import org.agnitas.emm.extension.exceptions.UnknownPluginException;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.java.plugin.PluginLifecycleException;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.pluginmanager.dto.PluginUploadDto;
import com.agnitas.emm.core.pluginmanager.form.PluginForm;
import com.agnitas.emm.core.pluginmanager.form.PluginListForm;
import com.agnitas.emm.core.pluginmanager.form.PluginUploadForm;
import com.agnitas.emm.core.pluginmanager.service.PluginManagerService;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping(value = "/administration/pluginmanager")
@PermissionMapping(value = "plugin.manager")
public class PluginManagerController {
	
	private static final Logger logger = Logger.getLogger(PluginManagerController.class);
	
	private static final String PLUGIN_LIST = "pluginList";
	private static final String PLUGIN_FORM = "pluginForm";
	
	private PluginManagerService pluginManagerService;
	private ConversionService conversionService;
	private WebStorage webStorage;
	private UserActivityLogService userActivityLogService;

	public PluginManagerController(PluginManagerService pluginManagerService, ConversionService conversionService, WebStorage webStorage, UserActivityLogService userActivityLogService) {
		this.pluginManagerService = pluginManagerService;
		this.conversionService = conversionService;
		this.webStorage = webStorage;
		this.userActivityLogService = userActivityLogService;
	}
	
	@RequestMapping(value = "/plugins.action")
	public String list(ComAdmin admin, ExtensionSystem extensionSystem, PluginListForm form, Model model) {
		FormUtils.syncNumberOfRows(webStorage, ComWebStorage.PLUGIN_MANAGER_OVERVIEW, form);

		model.addAttribute(PLUGIN_LIST,
				pluginManagerService.getAllPlugins(extensionSystem,
						form.getNumberOfRows(),
						form.getPage(),
						form.getSort(),
						form.getOrder()));
		
		userActivityLogService.writeUserActivityLog(admin, "plugin manager", "active tab item - overview");
		
		return "pluginmanager_list_plugins";
	}
	
	@GetMapping(value = "/plugin/{id}/view.action")
	public String view(ComAdmin admin, ExtensionSystem extensionSystem, @PathVariable String id, Model model, Popups popups) {
		if (StringUtils.isEmpty(id.trim())) {
			popups.alert("error.pluginmanager.missing_plugin_id");
			return "redirect:/administration/pluginmanager/plugins.action";
		}
		
		try {
			PluginForm form = conversionService.convert(pluginManagerService.getPlugin(extensionSystem, id), PluginForm.class);
			model.addAttribute(PLUGIN_FORM, form);
			userActivityLogService.writeUserActivityLog(admin, "view plugin", "Plugin " + form.getId());

			return "pluginmanager_plugin_view";
			
		} catch (UnknownPluginException e) {
			logger.error("Cannot obtain plugin. " + e.getMessage());
			
		}
		
		return "redirect:/administration/pluginmanager/plugins.action";
	}
	
	@RequestMapping(value = "/plugin/new.action")
	public String create() {
		return "pluginmanager_select";
	}

	@PostMapping(value = "/plugin/install.action", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String save(ComAdmin admin, ExtensionSystem extensionSystem, PluginUploadForm form, Popups popups) {
		logger.info("Handling uploaded plugin file");
		if(form.getUploadPluginFile().isEmpty() || StringUtils.isEmpty(form.getUploadPluginFile().getOriginalFilename())) {
			popups.alert("error.pluginmanager.installer.no_plugin_file");
			return "redirect:/administration/pluginmanager/plugin/new.action";
		}
		
		try {
			pluginManagerService.installPlugin(extensionSystem, conversionService.convert(form, PluginUploadDto.class));
			
			popups.success("pluginmanager.installer.installed");
			
			userActivityLogService.writeUserActivityLog(admin, "install plugin", "Plugin uploaded file " + form.getUploadPluginFile().getOriginalFilename());
			
		} catch (MissingPluginManifestException e) {
			logger.warn("Cannot install plugin - missing manifest", e);
			
			popups.alert("error.pluginmanager.installer.manifest");
		} catch (Exception e) {
			logger.warn("Cannot install plugin", e);
			
			popups.alert("error.pluginmanager.installer.general");
		}
		
		return "redirect:/administration/pluginmanager/plugins.action";
	}
	
	@GetMapping(value = "/plugin/{id}/activate.action")
	public String activate(ComAdmin admin, ExtensionSystem extensionSystem, @PathVariable String id, Popups popups) {
		return changeActiveStatus(admin, extensionSystem, id, false, popups);
	}
	
	@GetMapping(value = "/plugin/{id}/deactivate.action")
	public String deactivate(ComAdmin admin, ExtensionSystem extensionSystem, @PathVariable String id, Popups popups) {
		return changeActiveStatus(admin, extensionSystem, id, true, popups);
	}
	
	private String changeActiveStatus(ComAdmin admin, ExtensionSystem extensionSystem, String pluginId, boolean isActive, Popups popups) {
		try {
			if(isActive) {
				pluginManagerService.deactivatePlugin(extensionSystem, pluginId);
			} else{
				pluginManagerService.activatePlugin(extensionSystem, pluginId);
			}
			
			popups.success("default.changes_saved");
			
			userActivityLogService.writeUserActivityLog(admin, (isActive ? "deactivate" : "activate") + " plugin", "Plugin " + pluginId);

			return "redirect:/administration/pluginmanager/plugin/" + pluginId + "/view.action";
		} catch (UnknownPluginException e) {
			popups.alert("error.pluginmanager.missing_plugin_id");
		} catch (PluginLifecycleException e) {
			popups.alert("error.pluginmanager.activate_plugin");
		}
		
		return "redirect:/administration/pluginmanager/plugins.action";
	}
	
	@RequestMapping(value = "/plugin/{pluginId}/uninstall.action", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
	public String delete(ComAdmin admin, ExtensionSystem extensionSystem, @PathVariable String pluginId, Popups popups) {
		try {
			pluginManagerService.deletePlugin(extensionSystem, pluginId);
			popups.success("pluginmanager.plugin.uninstalled");
			
			userActivityLogService.writeUserActivityLog(admin, "delete plugin", "Plugin " + pluginId);
			
		} catch (RemovingSystemPluginNotAllowedException e) {
			popups.alert("error.pluginmanager.uninstall.systemplugin");
		} catch (UnknownPluginException e) {
			popups.alert("error.pluginmanager.missing_plugin_id");
		} catch (Exception e) {
			popups.alert("error.pluginmanager.uninstall");
		}
		
		return "redirect:/administration/pluginmanager/plugins.action";
	}
}
