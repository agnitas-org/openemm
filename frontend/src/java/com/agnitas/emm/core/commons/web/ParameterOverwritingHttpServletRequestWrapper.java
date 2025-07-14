/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.web;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.collections4.iterators.IteratorEnumeration;

public class ParameterOverwritingHttpServletRequestWrapper extends HttpServletRequestWrapper {
	
	private final Map<String, String[]> parameters;
	private final Map<String, String[]> parametersReadOnly;
	
	public ParameterOverwritingHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		
		this.parameters = new HashMap<>(request.getParameterMap());
		this.parametersReadOnly = Collections.unmodifiableMap(this.parameters);
	}

	public final void setParameter(final String name, final String... values) {
		this.parameters.put(name, values);
		
		assert this.parametersReadOnly.get(name) == values;		// Collections.unmodifiableMap() creates a wrapper around underlaying map
	}
	
	@Override
	public final String getParameter(final String name) {
		final String[] values = getParameterValues(name);
		
		return values != null
				? values[0]
				: null;
	}
	
	@Override
	public final String[] getParameterValues(final String name) {
		return getParameterMap().get(name);
	}

	@Override
	public final Map<String, String[]> getParameterMap() {
		return this.parametersReadOnly;
	}

	@Override
	public final Enumeration<String> getParameterNames() {
		return new IteratorEnumeration<>(getParameterMap().keySet().iterator());
	}

}
