/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.extension.ExtensionSystem;
import org.agnitas.emm.extension.exceptions.ExtensionException;
import org.agnitas.emm.extension.exceptions.PluginInstantiationException;
import org.agnitas.emm.extension.exceptions.UnknownPluginException;
import org.agnitas.emm.extension.util.ExtensionConstants;
import org.agnitas.emm.extension.util.ExtensionUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet for invoking EmmFeatureExtensions.
 * 
 * The servlet selects an EmmFeatureExtension and invokes its <i>invoke</i> method.
 */
public class ExtensionSetupServlet extends HttpServlet {
	private static final long serialVersionUID = -2880710001289980047L;
	
	private static final transient Logger logger = Logger.getLogger(ExtensionSetupServlet.class);
	
	@Override
	protected void service( HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
			ExtensionSystem extensionSystem = ExtensionUtils.getExtensionSystem( this.getServletContext());
			
			WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext( this.getServletContext());
			
			String feature = request.getParameter( ExtensionConstants.FEATURE_REQUEST_PARAMETER); 

			extensionSystem.invokeFeatureSetupExtension( feature, applicationContext, request, response);
		} catch (PluginInstantiationException e) {
			logger.error("Error occurred: " + e.getMessage(), e);
		} catch (ExtensionException e) {
			logger.error("Error occurred: " + e.getMessage(), e);
		} catch (UnknownPluginException e) {
			logger.error("Error occurred: " + e.getMessage(), e);
		}
	}


}
