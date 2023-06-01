/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import java.util.Locale;

import org.agnitas.util.AgnUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.struts.Globals;
import org.apache.struts.taglib.TagUtils;
import org.apache.struts.taglib.bean.MessageTag;
import org.apache.taglibs.standard.functions.Functions;

import com.agnitas.beans.Admin;
import com.agnitas.messages.I18nString;

import jakarta.servlet.jsp.JspException;

/**
 * The same as bean:message tag, but:
 * - doesn't throw exceptions (configurable)
 * - escapes single quotes (configurable)
 */
public class AgnMessageTag extends MessageTag {
	private static final long serialVersionUID = 7575045295420461217L;

    private static final String ESCAPE_MODE_NONE = "none";
    private static final String ESCAPE_MODE_HTML = "html";
    private static final String ESCAPE_MODE_JS = "js";

	protected String arg5 = null;
	protected String arg6 = null;
	protected String arg7 = null;
	protected String arg8 = null;
	protected String arg9 = null;

	protected boolean noExceptions = true;
	protected String escapeMode = ESCAPE_MODE_JS;

	@Override
	public int doStartTag() throws JspException {
		String code = resolveCode();
		Locale locale = resolveLocale();
		String message = translate(code, locale, new Object[] {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9});

		if (message == null) {
			if (noExceptions) {
				message = "";
			} else {
				String strLocale = (locale == null) ? "default locale" : locale.toString();
				throw exception(messages.getMessage("message.message", "\"" + code + "\"", "\"" + ((bundle == null) ? "(default bundle)" : bundle) + "\"", strLocale));
			}
		}

		TagUtils.getInstance().write(pageContext, escape(message));

		return SKIP_BODY;
	}

	public String getArg5() {
		return arg5;
	}

	public void setArg5(String arg5) {
		this.arg5 = arg5;
	}

	public String getArg6() {
		return arg6;
	}

	public void setArg6(String arg6) {
		this.arg6 = arg6;
	}

	public String getArg7() {
		return arg7;
	}

	public void setArg7(String arg7) {
		this.arg7 = arg7;
	}

	public String getArg8() {
		return arg8;
	}

	public void setArg8(String arg8) {
		this.arg8 = arg8;
	}

	public String getArg9() {
		return arg9;
	}

	public void setArg9(String arg9) {
		this.arg9 = arg9;
	}

    public boolean isNoExceptions() {
		return noExceptions;
	}

	public void setNoExceptions(boolean noExceptions) {
		this.noExceptions = noExceptions;
	}

    public String getEscapeMode() {
        return escapeMode;
    }

    public void setEscapeMode(String escapeMode) {
        this.escapeMode = escapeMode;
    }

	@Override
	public void release() {
		super.release();

		arg5 = null;
		arg6 = null;
		arg7 = null;
		arg8 = null;
		arg9 = null;
	}

	private String translate(String code, Locale locale, Object[] args) {
		return I18nString.getLocaleString(code, locale, args);
	}

	private String escape(String message) throws JspException {
		switch (resolveEscapeMode()) {
			case ESCAPE_MODE_HTML:
				return Functions.escapeXml(message);

			case ESCAPE_MODE_JS:
				return StringEscapeUtils.escapeEcmaScript(message);

			default:
				return message;
		}
	}

	private String resolveCode() throws JspException {
		if (key == null) {
			// Look up the requested property value
			Object code = TagUtils.getInstance().lookup(pageContext, name, property, scope);

			if (code instanceof String || code == null) {
				return (String) code;
			} else {
				throw exception(messages.getMessage("message.property", key));
			}
		}

		return key;
	}

	private Locale resolveLocale() {
		// Use admin's configured language if available, otherwise use the client's browser language
		Admin admin = AgnUtils.getAdmin(pageContext);

		if (admin != null) {
			Locale locale = admin.getLocale();
			if (locale != null) {
				return locale;
			}
		}

		// Use browser's locale as a fallback.
		return TagUtils.getInstance().getUserLocale(pageContext, Globals.LOCALE_KEY);
	}

	private String resolveEscapeMode() throws JspException {
	    if (escapeMode != null) {
            switch (escapeMode.toLowerCase()) {
                case ESCAPE_MODE_HTML:
                    return ESCAPE_MODE_HTML;
                case ESCAPE_MODE_JS:
                    return ESCAPE_MODE_JS;
                case ESCAPE_MODE_NONE:
                    return ESCAPE_MODE_NONE;
				default:
					throw exception("Invalid escapeMode attribute value: '" + escapeMode + "', expected 'js', 'html' or 'none'");
            }
        } else {
        	throw exception("Invalid escapeMode attribute value: '" + escapeMode + "', expected 'js', 'html' or 'none'");
        }
    }

    private JspException exception(String errorMessage) {
		JspException e = new JspException(errorMessage);
		TagUtils.getInstance().saveException(pageContext, e);
		return e;
	}
}
