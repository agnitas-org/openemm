/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.core.SetSupport;
import org.apache.velocity.util.ClassUtils;

public class InstantiateTag extends SetSupport {
	private static final long serialVersionUID = -1912211576518240520L;
	
	private String className;
	private Object target;
	private String property;

    public InstantiateTag() {
        className = null;
        target = null;
        property = null;
    }

    @Override
    public int doStartTag() throws JspException {
        final int code = super.doStartTag();
        super.doEndTag();
        return code;
    }

    @Override
    public int doEndTag() {
        return EVAL_PAGE;
    }

    public void setType(String className) {
        this.className = className;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public void setProperty(String property) {
        this.property = property;
    }

	@Override
	protected boolean isValueSpecified() {
		return true;
	}

	@Override
	protected Object evalValue() throws JspException {
        try {
            return ClassUtils.getNewInstance(className);
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            throw new JspException(e);
        }
	}

	@Override
	protected Object evalTarget() {
		return target;
	}

	@Override
	protected String evalProperty() {
		return property;
	}
}
