/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension;

import org.agnitas.emm.extension.exceptions.ExtensionException;
import org.java.plugin.registry.Extension;
import org.springframework.context.ApplicationContext;

/**
 * Interface for new features. Features are used to add complete new functionality are invoked
 * by the ExtensionServlet. Feature are normally accessed by GET or POST requests.
 */
public interface EmmFeatureExtension {
	// TODO: ApplicationContext is used as parameter here, that not good. Quick hack for PoC only!!!

	/**
	 * Entry point for EMM features.
	 * 
	 * @param pluginContext PluginContext
	 * @param extension the Extension instance for the feature
	 * @param context Application context from Spring to access EMM's DAOs and services
	 * 
	 * @throws ExtensionException on errors
	 */
	public void invoke( PluginContext pluginContext, Extension extension, ApplicationContext context) throws ExtensionException;

	public void setup( PluginContext pluginContext, Extension extension, ApplicationContext context) throws ExtensionException;
}
