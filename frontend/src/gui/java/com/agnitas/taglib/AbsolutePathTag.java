/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import static com.agnitas.util.JspUtilities.absUrlPrefix;
import static java.util.regex.Pattern.compile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;

import org.apache.commons.lang3.StringUtils;
import org.apache.taglibs.standard.tag.common.core.SetSupport;

public class AbsolutePathTag extends SetSupport {
    private static final long serialVersionUID = 4854974831832193374L;
    private String path;

    @Override
    public int doStartTag() throws JspException {
        return super.doStartTag();
    }

    @Override
    protected boolean isValueSpecified() {
        return true;
    }

    @Override
    protected Object evalValue() {
        String contextUrl = absUrlPrefix(((HttpServletRequest) pageContext.getRequest()).getContextPath());

        if (StringUtils.isBlank(contextUrl) || compile("^" + contextUrl + "/.*").matcher(path).matches()) {
            return absUrlPrefix(path);
        }
        return contextUrl + absUrlPrefix(path);
    }

    @Override
    protected Object evalTarget() {
        return null;
    }

    @Override
    protected String evalProperty() {
        return null;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
