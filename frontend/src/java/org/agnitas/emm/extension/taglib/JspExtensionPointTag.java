/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.agnitas.emm.extension.ExtensionSystem;
import org.agnitas.emm.extension.util.ExtensionUtils;
import org.apache.log4j.Logger;

public class JspExtensionPointTag extends TagSupport {
	private static final long serialVersionUID = 5858577309929024954L;
	
	private static final Logger logger = Logger.getLogger(JspExtensionPointTag.class);

	/** Name of plugin providing extension point. */
	private String plugin;
	
	/** Name of extension point provided by plugin. */
	private String point;
	
	public void setPlugin( String plugin) {
		this.plugin = plugin;
	}
	
	public void setPoint( String point) {
		this.point = point;
	}
	
	@Override
	public int doEndTag() throws JspException {
		try {
			ExtensionSystem extensionSystem = ExtensionUtils.getExtensionSystem(pageContext.getServletContext());
			if (extensionSystem != null) {
				extensionSystem.invokeJspExtension(plugin, point, this.pageContext);
			} else {
				logger.warn("No JspPoint extension for plugin '" + plugin + "' defined");
			}
		} catch (Exception e) {
			logger.error( "Error handling JspPoint for plugin '" + plugin + "', point '" + point + "'", e);
		}

		return TagSupport.EVAL_PAGE;
	}
}
