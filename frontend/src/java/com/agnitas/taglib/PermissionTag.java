/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTag;
import com.agnitas.util.AgnUtils;

public class PermissionTag extends PermissionExceptionTagSupport implements BodyTag {
	private static final long serialVersionUID = 4189224412870199939L;
    
	private String permissionToken;
	
	@Override
    public void	doInitBody() {
    	// do nothing
    }
    
    @Override
    public void	setBodyContent(BodyContent bodyContent) {
    	// do nothing
    }
    
	/**
	 * Setter for property token.
	 * 
	 * @param mode
	 *            New value of property token.
	 */
	public void setToken(String mode) {
		permissionToken = mode;
	}

	/**
	 * permission control
	 */
	@Override
	public int doStartTag() throws JspTagException {
		Admin aAdmin = AgnUtils.getAdmin(pageContext);

		if (aAdmin == null) {
			throw new JspTagException("PermissionDenied$" + permissionToken);
		} else {
			boolean permissionGranted = false;
			try {
				permissionGranted = aAdmin.permissionAllowed(Permission.getPermissionsByToken(permissionToken));
			} catch (Exception e) {
				releaseException(e, permissionToken);
			}
			if (!permissionGranted) {
				throw new JspTagException("PermissionDenied$" + permissionToken);
			}

			return SKIP_BODY;
		}
	}
	
    @Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}
