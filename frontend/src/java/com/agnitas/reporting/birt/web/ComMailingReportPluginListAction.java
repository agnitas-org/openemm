/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.web;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.extension.ExtensionSystem;
import org.agnitas.emm.extension.util.ExtensionUtils;
import org.agnitas.emm.extension.util.I18NResourceBundle;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.FileUtils;
import org.agnitas.web.StrutsActionBase;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.java.plugin.registry.Extension;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.extension.impl.ComExtensionConstants;
import com.agnitas.reporting.birt.beans.BirtPluginData;

public class ComMailingReportPluginListAction extends StrutsActionBase {

	private static final transient Logger logger = Logger.getLogger( ComMailingReportPluginListAction.class);
    
	protected ConfigService configService;
	
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if( logger.isInfoEnabled()) 
			logger.info( "Creating list of all allowed BIRT plugins");
		
		ExtensionSystem extensionSystem = ExtensionUtils.getExtensionSystem( request);
		ComAdmin admin = AgnUtils.getAdmin( request);
		
		Collection<Extension> extensions = extensionSystem.getActiveExtensions( "emm_core", "birt.statistics.mailing");
		List<BirtPluginData> list = createPluginList( admin, extensions, extensionSystem, request);
		
		if( logger.isDebugEnabled())
			logger.debug( list.size() + " plugins found to display");
		
		request.setAttribute( "plugins", list);
		
		return mapping.findForward( "list");
	}

	private List<BirtPluginData> createPluginList(ComAdmin admin, Collection<Extension> extensions, ExtensionSystem extensionSystem, HttpServletRequest request) throws Exception {
		List<BirtPluginData> list = new Vector<>();
		
		for( Extension extension : extensions) {
			String permissions = extension.getParameter( "permissions").valueAsString();
			
			if( admin.permissionAllowed(Permission.getPermissionsByToken(permissions))) {
				if( logger.isInfoEnabled())
					logger.info( "Plugin " + extension.getId() + " added to list");
				
				I18NResourceBundle resourceBundle = extensionSystem.getPluginI18NResourceBundle( extension.getId());
				
				String titleKey = extension.getParameter( "titlekey").valueAsString();
				String reportDesign = extension.getParameter( "reportdesign").valueAsString();
				
				String title = resourceBundle.getMessage( titleKey, admin.getLocale());
				
				BirtPluginData pluginData = new BirtPluginData( title, createPluginUrl( extension.getId(), reportDesign, request));
				
				list.add( pluginData);
			} else {
				if( logger.isInfoEnabled())
					logger.info( "Plugin " + extension.getId() + " rejected due to insufficient user permissions");
			}
		}
		
		return list;
	}
	
	private String createPluginUrl( String pluginId, String reportDesign, HttpServletRequest request) {
		Set<String> appendedParameters = new HashSet<>();
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(configService.getValue(ConfigValue.BirtUrl));					// Base URL
		builder.append( "/run");												// Name of BIRT application
		builder.append( "?__report=").append( FileUtils.removeTrailingSeparator( ComExtensionConstants.PLUGIN_BIRT_RPTDESIGN_BASE)).append("/").append( pluginId).append( "/").append( reportDesign);	// Report design
		appendedParameters.add( "__report");
		
		Enumeration<String> paramNames = request.getParameterNames();
		while( paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			
			if( appendedParameters.contains( paramName)) {
				logger.warn( "Omitting dpulicate parameter: " + paramName);
			} else {
				builder.append( "&").append( paramName).append( "=").append( request.getParameter( paramName));
			}
		}
		
		return builder.toString();
	}
	
}
