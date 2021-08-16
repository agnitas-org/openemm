/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.agnitas.util.AgnUtils;

public final class CheckLogonTag extends TagSupport {
     
     
    private static final long serialVersionUID = -4706642742651352150L;

	// TODO Move initialization to method (same init used below a second time) and use WebAppFileUtil.getWebInfDirectoryPath()
    private String page = "/WEB-INF/jsp/login.jsp";
       
    public String getPage() {
        return (this.page);   
    }
    
    public void setPage(String page) {
        this.page = page;  
    }
    
    @Override
    public int doStartTag() throws JspException { 
        return (SKIP_BODY);  
    }
    
    @Override
    public int doEndTag() throws JspException {
        // Is there a valid user logged on?        
        // Forward control based on the results
        if (AgnUtils.getAdmin(pageContext) != null)
            return (EVAL_PAGE);
        else {
            try {
                pageContext.forward(page);
            } catch (Exception e) {
                throw new JspException(e.toString());
            }
            return (SKIP_PAGE);
        }  
    }
    
    @Override
    public void release() {  
        super.release();

        // TODO Move initialization to method (same init used above a second time) and use WebAppFileUtil.getWebInfDirectoryPath()
        this.page = "/WEB-INF/jsp/logon/logon.jsp";
    }
}
