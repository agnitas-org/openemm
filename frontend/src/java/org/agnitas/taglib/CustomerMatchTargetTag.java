/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.TagSupport;

import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ComTarget;
import com.agnitas.dao.ComTargetDao;

/**
 * Connect: Connect to a database Table
 *
 * <Connect table="..." />
 */

public class CustomerMatchTargetTag extends TagSupport implements BodyTag {
	/** Serial version UID. */
    private static final long serialVersionUID = 5503535991822272855L;
	
	private static final transient Logger logger = Logger.getLogger(CustomerMatchTargetTag.class);
	
	private BodyContent bodyContent = null;
    
	protected int customerID;
    protected int targetID;
    
    @Override
    public void	doInitBody() {
    	// do nothing
    }
    
    @Override
    public void	setBodyContent(BodyContent bodyContent) {
    	this.bodyContent = bodyContent;
    }
    
     /**
     * Setter for property customerID.
     * 
     * @param custID New value of property customerID.
     */
    public void setCustomerID(int custID) {
        this.customerID = custID;
    }
    
     /**
     * Setter for property targetID.
     * 
     * @param targID New value of property targetID.
     */
    public void setTargetID(int targID) {
        this.targetID = targID;
    }
    
    /**
     * checks if customer belongs to target group
     */
    @Override
    public int doStartTag() throws JspException	{
        if (targetID == 0) {
            return EVAL_BODY_BUFFERED;
        } else {
            ApplicationContext aContext = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
            ComTargetDao tDao = (ComTargetDao) aContext.getBean("TargetDao");
	        ComTarget aTarget = tDao.getTarget(targetID, getCompanyID());
	        
	        if (aTarget == null) {
	        	return EVAL_BODY_BUFFERED;
	        } else if (aTarget.isCustomerInGroup(this.customerID, aContext)) {
            	return EVAL_BODY_BUFFERED;
	        } else {
	        	return SKIP_BODY;
	        }
        }
    }
	
    @Override
	public int doEndTag() throws JspException {
		try {
			if (bodyContent != null) {
				pageContext.getOut().print(bodyContent.getString());
			}
		} catch (IOException e) {
			throw new JspException(e.getMessage());
		}

		return EVAL_PAGE;
	}
    
    @Override
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }
    
    public int getCompanyID() {
        try {
        	return AgnUtils.getAdmin(pageContext).getCompany().getId();
        } catch (Exception e) {
            logger.error("CustomerMatchTargetTag - getCompanyID: no companyID: " + e.getMessage());
            return 0;
        }
    }
}
