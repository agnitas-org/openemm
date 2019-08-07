/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.taglib;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;

import org.agnitas.util.AgnUtils;

public class FormatDateTag extends org.apache.taglibs.standard.tag.rt.fmt.FormatDateTag {
    private static final long serialVersionUID = 282007090797936756L;

    public void setFormat(SimpleDateFormat format) throws JspTagException {
        setPattern(format.toPattern());
        setTimeZone(format.getTimeZone());
    }

    @Override
	public int doEndTag() throws JspException {
        resolveLocale();
        return super.doEndTag();
    }

    private void resolveLocale() {
        ServletRequest request = pageContext.getRequest();
        if (request instanceof HttpServletRequest) {
            Locale locale = AgnUtils.getLocale((HttpServletRequest) request);
            Config.set(pageContext, Config.FMT_LOCALE, locale, PageContext.PAGE_SCOPE);
        }
    }
}
