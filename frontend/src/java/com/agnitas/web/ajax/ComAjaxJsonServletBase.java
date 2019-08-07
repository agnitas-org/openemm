/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.ajax;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.agnitas.web.ComAjaxServletBase;

import net.sf.json.JSONObject;

public abstract class ComAjaxJsonServletBase extends ComAjaxServletBase {
	private static final long serialVersionUID = 7706861868556502679L;

	@Override
    protected final void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("json");
        response.setCharacterEncoding("UTF-8");

        JSONObject responseJson = processRequestJson(request, response);
        if (responseJson == null) {
            responseJson = new JSONObject(true);
        }

        // noinspection resource
        responseJson.write(response.getWriter());
    }

    protected abstract JSONObject processRequestJson(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
