/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.json.web;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

/**
 * Struts action that returns POJOs in JSON format
 */
public abstract class JsonAction<F extends ActionForm> extends Action {

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(JsonAction.class);
	
	@Override
	public final ActionForward execute(final ActionMapping mapping, final ActionForm form0, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		@SuppressWarnings("unchecked")
		F form = (F) form0;
		
		try {			
			final Object result = executeJson(form, request);
			
			sendJson(result, response);
		} catch(final Exception e) {
			logger.error("Caught exception during processing request", e);
			
			final Object result = handleException(e, form, request);
			
			sendJson(result, response);
		}
		
		// We do never forward to some JSP or Action
		return null;
	}
	
	public Object handleException(Exception e, F form, HttpServletRequest request) {
		return null;
	}

	public static <T> T fromJsonString(final String string, Class<T> rootClass) {
		final JsonConfig config = new JsonConfig();
		config.setRootClass(rootClass);
		
		final JSON json = JSONSerializer.toJSON(string);
		@SuppressWarnings("unchecked")
		final T result = (T)JSONSerializer.toJava(json, config);
		
		return result;
	}
	
	private final JSON toJson(final Object obj) {
		final JSON json = JSONSerializer.toJSON(obj);

		return json;
	}

	private final void sendJson(final Object obj, final HttpServletResponse response) throws IOException {
		final JSON json = toJson(obj);

		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		
		try(PrintWriter writer = response.getWriter()) {
			json.write(writer);
			
			writer.flush();
		}
	}
	
	public abstract Object executeJson(final F form, final HttpServletRequest request) throws Exception;
	
}
