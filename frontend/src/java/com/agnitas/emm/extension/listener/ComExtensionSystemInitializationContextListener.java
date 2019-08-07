/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.extension.listener;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.extension.impl.ExtensionSystemBuilder;
import org.agnitas.emm.extension.listener.ExtensionSystemInitializationContextListener;
import org.springframework.web.context.WebApplicationContext;

import com.agnitas.emm.extension.impl.ComExtensionSystemBuilder;

public class ComExtensionSystemInitializationContextListener extends ExtensionSystemInitializationContextListener {

	@Override
	protected ExtensionSystemBuilder createExtensionSystemBuilder() {
		return new ComExtensionSystemBuilder();
	}

	@Override
	protected void setBuilderProperties( ExtensionSystemBuilder extensionSystemBuilder0, ServletContext servletContext, WebApplicationContext springContext) throws Exception {
		super.setBuilderProperties(extensionSystemBuilder0, servletContext, springContext);
		
		ComExtensionSystemBuilder extensionSystemBuilder = (ComExtensionSystemBuilder) extensionSystemBuilder0;
		
		// BIRT properties
		extensionSystemBuilder.setBirtHost(extractDomain(configService.getValue(ConfigValue.BirtUrl)));
		extensionSystemBuilder.setBirtHostUser(configService.getValue(ConfigValue.BirtHostUser));
		extensionSystemBuilder.setBirtPluginDirectory(configService.getValue(ConfigValue.BirtPluginDirectory));
	}
	
	private String extractDomain( String fullUrl) throws MalformedURLException {
		URL url = new URL( fullUrl);
		
		return url.getHost();
	}
}
