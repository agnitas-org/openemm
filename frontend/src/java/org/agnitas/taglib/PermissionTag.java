/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.taglib;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTag;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import org.agnitas.util.AgnUtils;

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
		ComAdmin aAdmin = AgnUtils.getAdmin(pageContext);

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
	public int doEndTag() throws JspException {
		return EVAL_PAGE;
	}
}
