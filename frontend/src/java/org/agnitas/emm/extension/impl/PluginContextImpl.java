/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.extension.PluginContext;

public class PluginContextImpl implements PluginContext {

	private final String pluginId;
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	
	public PluginContextImpl( String pluginId, HttpServletRequest request, HttpServletResponse response) {
		this.pluginId = pluginId;
		this.request = request;
		this.response = response;
	}
	
	@Override
	public HttpServletRequest getServletRequest() {
		return this.request;
	}
	
	@Override
	public HttpServletResponse getServletResponse() {
		return this.response;
	}
	
	@Override
	public void includeJspFragment( String relativeUrl) throws IOException, ServletException {
		request.getRequestDispatcher( "plugins/" + this.pluginId + "/" + relativeUrl).include(  request, response);
	}

}
