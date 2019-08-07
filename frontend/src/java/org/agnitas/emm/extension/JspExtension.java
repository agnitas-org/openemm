/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension;

import javax.servlet.jsp.PageContext;

import org.agnitas.emm.extension.exceptions.JspExtensionException;
import org.java.plugin.registry.Extension;

/**
 * Interface for an plugin registered at an JSP extension point.
 * 
 * JSP extension points allow multiple extension to register and are normally used
 * for additional output of an existing JSP.
 */
public interface JspExtension {
	/**
	 * Entry point. 
	 * 
	 * @param extension the Extension instance for the JspExtension
	 * @param pageContext pageContext from JSP tag
	 * 
	 * @throws JspExtensionException on errors
	 */
	public void invoke( Extension extension, PageContext pageContext) throws JspExtensionException;
}
