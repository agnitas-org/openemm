/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.taglib;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ComTarget;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.target.service.RecipientTargetGroupMatcher;

/**
 * Connect: Connect to a database Table
 *
 * <Connect table="..." />
 */

public class CustomerMatchTargetTag extends TagSupport implements BodyTag {
	/** Serial version UID. */
    private static final long serialVersionUID = 5503535991822272855L;
	
    /** The logger. */
	private static final transient Logger logger = LogManager.getLogger(CustomerMatchTargetTag.class);
	
	private BodyContent bodyContent = null;
    
	private int customerID;
    private int targetID;
    
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
            final ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
	        final ComTargetService targetService = applicationContext.getBean("targetService", ComTargetService.class);
	        
	        try {
		        final RecipientTargetGroupMatcher matcher = targetService.createRecipientTargetGroupMatcher(customerID, getCompanyID());
	        	final ComTarget target = targetService.getTargetGroup(targetID, getCompanyID());
	        	
	        	return matcher.isInTargetGroup(target)
	        			? EVAL_BODY_BUFFERED
	        			: SKIP_BODY;
	        } catch(final Exception e) {
	        	logger.warn("Error checking if recipients matches target group", e);
	        	
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
