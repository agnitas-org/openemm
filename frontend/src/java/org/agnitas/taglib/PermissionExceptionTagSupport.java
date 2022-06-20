/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.taglib;

import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class PermissionExceptionTagSupport extends TagSupport {
	private static final long serialVersionUID = -1443531291551216008L;

	private static final Logger logger = LogManager.getLogger(ShowByPermissionTag.class);
    
    protected boolean ignoreException;

    public void setIgnoreException(boolean ignoreException) {
		this.ignoreException = ignoreException;
	}
    
    public boolean isIgnoreException() {
        return ignoreException;
    }
    
    protected void releaseException(Exception e, String token) throws JspTagException {
        if(isIgnoreException()) {
            logger.warn("Invalid permission token: " + token, e);
        } else {
            throw new JspTagException("Invalid permission token: " + token);
        }
    }
}
