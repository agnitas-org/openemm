/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import java.util.Objects;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.util.AgnUtils;
import jakarta.servlet.jsp.JspException;

public class ShowByPermissionTag extends PermissionExceptionTagSupport {

    private static final long serialVersionUID = 2088220971349294443L;

    protected String token;
    
    public void setToken(String mode) {
        token = Objects.requireNonNullElse(mode, "");
    }

    /**
     * permission control
     */
    @Override
	public int doStartTag() throws JspException {
    	Admin aAdmin = AgnUtils.getAdmin(pageContext);
		if (aAdmin != null) {
            try {
                if (aAdmin.permissionAllowed(Permission.getPermissionsByToken(token))) {
                    return EVAL_BODY_INCLUDE;
                }
            } catch (Exception e) {
                releaseException(e, token);
            }
        }

		return SKIP_BODY;
	}
}
