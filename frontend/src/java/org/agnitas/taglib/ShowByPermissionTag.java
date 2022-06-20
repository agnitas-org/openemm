/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.taglib;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import org.agnitas.util.AgnUtils;

/**
 * Connect: Connect to a database Table
 *
 * <Connect table="..." />
 */

public class ShowByPermissionTag extends PermissionExceptionTagSupport {
    private static final long serialVersionUID = 2088220971349294443L;

    protected String token;
    
    //***************************************
    //* Implementations for Tag
    //***************************************
    
    public void setToken(String mode) {
        if(mode!=null) {
            token=mode;
        } else {
            token = "";
        }
    }
    
    /**
     * permission control
     */
    @Override
	public int doStartTag() throws JspException {
    	ComAdmin aAdmin = AgnUtils.getAdmin(pageContext);
		if (aAdmin != null) {
            try {
                if (aAdmin.permissionAllowed(Permission.getPermissionsByToken(token))) {
                    return TagSupport.EVAL_BODY_INCLUDE;
                }
            } catch (Exception e) {
                releaseException(e, token);
            }
        }

		return SKIP_BODY;
	}
}
