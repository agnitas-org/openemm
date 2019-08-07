/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.agnitas.emm.extension.data.PluginDetail;
import org.agnitas.emm.extension.data.PluginStatusReport;
import org.agnitas.emm.extension.exceptions.DatabaseScriptException;
import org.agnitas.emm.extension.exceptions.ExtensionException;
import org.agnitas.emm.extension.exceptions.MissingPluginManifestException;
import org.agnitas.emm.extension.exceptions.PluginInstantiationException;
import org.agnitas.emm.extension.exceptions.RemovingSystemPluginNotAllowedException;
import org.agnitas.emm.extension.exceptions.UnknownPluginException;
import org.agnitas.emm.extension.util.I18NResourceBundle;
import org.java.plugin.JpfException;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.registry.Extension;
import org.springframework.context.ApplicationContext;

public interface ExtensionSystem {

	/**
	 * Invoke extension registered on a JSP extension point.
	 * 
	 * @param pluginName name of the plugin, where the extension point is defined in.
	 * @param extensionPointName name of the extension point
	 * @param pageContext PageContext from JSP tag
	 */
	void invokeJspExtension(String pluginName,
			String extensionPointName, PageContext pageContext);

	/**
	 * Invoke EmmFeatureExtension.
	 * 
	 * @param pluginId ID of the plugin
	 * @param context ApplicationContext of Spring for accessing EMM's DAOs and services.
	 * @param request servlet request
	 * @param response servlet response
	 * 
	 * @throws PluginInstantiationException on errors creating plugin instance
	 * @throws ExtensionException on errors during executing of the feature plugin
	 * @throws UnknownPluginException when specified plugin is unknown
	 */
	// TODO: Uses ApplicationContext in parameter list. PoC only!
	void invokeFeatureExtension(String pluginId,
			ApplicationContext context, HttpServletRequest request,
			HttpServletResponse response) throws PluginInstantiationException,
			ExtensionException, UnknownPluginException;

	/**
	 * Invoke setup of EmmFeatureExtension.
	 * 
	 * @param pluginId ID of the plugin
	 * @param context ApplicationContext of Spring for accessing EMM's DAOs and services.
	 * @param request servlet request
	 * @param response servlet response
	 * 
	 * @throws PluginInstantiationException on errors creating plugin instance
	 * @throws ExtensionException on errors during executing of the feature plugin
	 * @throws UnknownPluginException when specified plugin is unknown
	 */
	// TODO: Uses ApplicationContext in parameter list. PoC only!
	void invokeFeatureSetupExtension(String pluginId,
			ApplicationContext context, HttpServletRequest request,
			HttpServletResponse response) throws PluginInstantiationException,
			ExtensionException, UnknownPluginException;

	Collection<Extension> getActiveExtensions(String plugin, String extensionPoint);

	InputStream getPluginResource(String plugin, String resource);

	I18NResourceBundle getPluginI18NResourceBundle(String plugin);

	ResourceBundle getPluginResourceBundle(String plugin, String bundleName) throws Exception;

	Extension getExtension(String plugin, String extension);

	PluginStatusReport getPluginStatusReport();

	PluginDetail getPluginDetails(String pluginID) throws UnknownPluginException;

	void activatePluginForStartup(String pluginId) throws PluginLifecycleException;

	void deactivatePluginForStartup(String pluginId);

	void installPlugin(String pluginFilename) throws MissingPluginManifestException, IOException, JpfException, DatabaseScriptException;

	void uninstallPlugin(String pluginID) throws RemovingSystemPluginNotAllowedException;

	boolean isSystemPlugin(String pluginID);
	
	boolean isPluginExist(String pluginId);
}
