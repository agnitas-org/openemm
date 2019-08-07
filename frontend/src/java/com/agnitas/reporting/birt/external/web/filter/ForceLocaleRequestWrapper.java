/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.web.filter;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * HttpServletRequestWrapper which rewrites the clients language settings
 */
public class ForceLocaleRequestWrapper extends HttpServletRequestWrapper {

	private Locale locale;
	private Vector<Locale> locales;
	public ForceLocaleRequestWrapper(HttpServletRequest request, String language) {
		super(request);
		this.locale = new Locale(language.toLowerCase());
		locales = new Vector<>();
		locales.add(locale);
	}
	
	@Override
	public Locale getLocale() {
		return locale;
	}
	
	@Override
	public Enumeration<Locale> getLocales() {
		return locales.elements();
	}
}
