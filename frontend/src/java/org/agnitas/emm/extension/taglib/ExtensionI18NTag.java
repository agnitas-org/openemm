/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.taglib;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.agnitas.emm.extension.util.ExtensionUtils;
import org.agnitas.emm.extension.util.I18NResourceBundle;
import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.taglib.TagUtils;

import com.agnitas.messages.I18nString;

/**
 * Tag to create a adapter layer between the plugin and the MVC framework (here Struts is used).<br />
 * <br />
 * This Tag is used in jsps as "&lt;emm:message key=... plugin=...>"<br />
 * Do not mix up with "&lt;bean:message key=...>", which is the standard message tag used in EMM jsps.<br />
 */
public class ExtensionI18NTag extends TagSupport {
	private static final long serialVersionUID = -6720298363037671239L;

	private static final transient Logger logger = Logger.getLogger( ExtensionI18NTag.class);
	
	private String plugin;
	private String key;
	
	public void setPlugin( String plugin) {
		this.plugin = plugin;
	}
	
	public void setKey( String key) {
		this.key = key;
	}

	@Override
	public int doStartTag() throws JspException {
		try {
			I18NResourceBundle bundle = ExtensionUtils.getExtensionSystem( pageContext.getServletContext()).getPluginI18NResourceBundle( plugin);
			boolean fallback = false;

			if (bundle != null) {
				// This code uses Struts
				String translation = bundle.getMessage( key, getUserLocale());

				if (translation == null) {
					fallback = true;
					logger.warn("Key '" + key + "' not defined in i18n bundle for plugin '" + plugin + "'");
				} else {
					print(translation);
				}
			} else {
				fallback = true;
				logger.warn( "No i18n bundle for plugin '" + plugin + "' defined");
			}

			if (fallback) {
				if (I18nString.hasMessageForKey(key)) {
					print(I18nString.getLocaleString(key, getUserLocale()));
				} else {
					print("?? Missing key " + key + " for plugin " + plugin + " ??");
				}
			}
		} catch( Exception e) {
			logger.error( "Error handling i18n for plugin '" + plugin + "', key '" + key + "'", e);
		}
		
		return TagSupport.SKIP_BODY;
	}
	
	private Locale getUserLocale() {
		// This is (currently) MVC-framework specific code.
		return TagUtils.getInstance().getUserLocale(pageContext, Globals.LOCALE_KEY);
	}

	private void print(String s) throws IOException {
		// Do not close this writer
		pageContext.getOut().write(s);
	}
}
