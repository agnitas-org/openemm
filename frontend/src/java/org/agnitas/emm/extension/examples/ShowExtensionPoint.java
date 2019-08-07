/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.examples;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.emm.extension.AnnotatedDispatchingEmmFeatureExtension;
import org.agnitas.emm.extension.PluginContext;
import org.agnitas.emm.extension.exceptions.ExtensionException;
import org.java.plugin.registry.Extension;
import org.springframework.context.ApplicationContext;

/**
 * Plugin to show all extensions points in the UI.
 */
public class ShowExtensionPoint extends AnnotatedDispatchingEmmFeatureExtension {

	@Override
	public void setup( PluginContext pluginContext, Extension extension, ApplicationContext context) throws ExtensionException {
		HttpServletRequest request = pluginContext.getServletRequest();
		
		request.setAttribute( "sidemenu_active", "none");
		request.setAttribute( "sidemenu_sub_active", "none");
		request.setAttribute( "agnTitleKey", "tab");
		request.setAttribute( "agnSubtitleKey", "tab");
		request.setAttribute( "agnSubtitleValue", "XYZ");
		request.setAttribute( "agnNavigationKey", "tab");
		request.setAttribute( "agnHighlightKey", "tab");
		request.setAttribute( "agnNavHrefAppend", "EXTENSIONPOINT=" + extension.getExtendedPointId());
		/*
		request.setAttribute( "agnPluginId", extension.getDeclaringPluginDescriptor().getId());
		request.setAttribute( "agnExtensionId", extension.getId());
		*/
	}

	@Override
	public void unspecifiedTarget( PluginContext pluginContext, Extension extension, ApplicationContext context) throws Throwable {
		try(PrintWriter out = pluginContext.getServletResponse().getWriter()) {
			out.println( "<script type='text/javascript'>alert('Extension point ID: " + extension.getExtendedPointId() + "')</script>");
		}
	}

}
