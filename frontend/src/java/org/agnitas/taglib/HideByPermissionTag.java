/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.taglib;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.agnitas.util.AgnUtils;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;

public class HideByPermissionTag extends PermissionExceptionTagSupport {
    private static final long serialVersionUID = 4305002485480895206L;

    protected String token;    

    //***************************************
    //* Implementations for Tag
    //***************************************

    public void setToken(String token) {
		if (token != null) {
			this.token = token;
		} else {
			this.token = "";
		}
    }

    /**
     * permission control
     */
    @Override
	public int doStartTag() throws JspException {
    	Admin aAdmin = AgnUtils.getAdmin(pageContext);
		try {
			if (aAdmin != null && aAdmin.permissionAllowed(Permission.getPermissionsByToken(token))) {
				return TagSupport.SKIP_BODY;
			}
		} catch (Exception e) {
			releaseException(e,token);
		}
		return EVAL_BODY_INCLUDE;
	}
}
