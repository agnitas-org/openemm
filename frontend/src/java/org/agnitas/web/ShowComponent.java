/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.MailingComponent;
import org.agnitas.dao.MailingComponentDao;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.dao.ComRecipientDao;

public class ShowComponent extends HttpServlet {

	/** Logger. */
	private static final transient Logger logger = Logger.getLogger( ShowComponent.class);
	
    private static final long serialVersionUID = 6640509099616089054L;

	protected MailingComponentDao mailingComponentDao;
	protected PreviewFactory previewFactory;
	protected ComRecipientDao recipientDao;

	public void setMailingComponentDao(MailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}

	public void setPreviewFactory(PreviewFactory previewFactory) {
		this.previewFactory = previewFactory;
	}

	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

	/**
     * Gets mailing components
     * TYPE_IMAGE: if component not empty, write it into response
     * <br><br>
     * TYPE_HOSTED_IMAGE: if component not empty, write it into response
     * <br><br>
     * TYPE_THUMBNAIL_IMAGE: if component not empty, write it into response
     * <br><br>
     * TYPE_ATTACHMENT: create preview <br>
     *          write component into response
     * <br><br>
     * TYPE_PERSONALIZED_ATTACHMENT: create preview <br>
     *          write component into response
     * <br><br>
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {
        ServletOutputStream out=null;
        long len=0;
        int compId=0;
        
		if (!AgnUtils.isUserLoggedIn(req)) {
            return;
        }
        
        try {
            compId=Integer.parseInt(req.getParameter("compID"));
        } catch (Exception e) {
        	logger.warn( "Error converting " + (req.getParameter("compID") != null ? "'" + req.getParameter("compID") + "'" : req.getParameter("compID")) + " to integer", e);
            return;
        }
        
        if(compId==0) {
            return;
        }
        
        
        int customerID = 0;
        
        String customerIDStr = req.getParameter("customerID");
        if( StringUtils.isNumeric(customerIDStr)) {
        	customerID = Integer.parseInt(customerIDStr);
        }
        
        MailingComponent comp = getComponentDao().getMailingComponent(compId, AgnUtils.getCompanyID(req));
        
        if(comp!=null) {
            
            switch(comp.getType()) {
                case MailingComponent.TYPE_IMAGE:
                case MailingComponent.TYPE_HOSTED_IMAGE:
                    if (comp.getBinaryBlock() != null) {
                    response.setContentType(comp.getMimeType());
                    out=response.getOutputStream();
                    out.write(comp.getBinaryBlock());
                    out.flush();
                    out.close();
                    }
                    break;
                case MailingComponent.TYPE_THUMBNAIL_IMAGE:
                    if (comp.getBinaryBlock() != null) {
                    response.setContentType(comp.getMimeType());
                    out=response.getOutputStream();
                    out.write(comp.getBinaryBlock());
                    out.flush();
                    out.close();
                    }
                    break;
                case MailingComponent.TYPE_ATTACHMENT:
                case MailingComponent.TYPE_PERSONALIZED_ATTACHMENT:
    	            HttpUtils.setDownloadFilenameHeader(response, comp.getComponentName());
                    response.setContentType(comp.getMimeType());
                    out=response.getOutputStream();     
                   
                    byte[] attachment = null;
                    int mailingID = comp.getMailingID(); 
                                        
                    if( comp.getType() == MailingComponent.TYPE_PERSONALIZED_ATTACHMENT) { 
                    	Page page = null;                        
                        if( customerID == 0 ){ // no customerID is available, take the 1st available test recipient
                        	ComRecipientDao recipientDao = getRecipientDao();
                              Map<Integer,String> recipientList = recipientDao.getAdminAndTestRecipientsDescription(comp.getCompanyID(), mailingID);
                              customerID = recipientList.keySet().iterator().next();
                        }
                        Preview preview = getPreviewFactory().createPreview();   
                        page = preview.makePreview(mailingID, customerID, false);
                        attachment = page.getAttachment(comp.getComponentName());
                        
                    } else {
                    	attachment = comp.getBinaryBlock();
                    }                                       
                    
                    len= attachment.length;
                    response.setContentLength((int)len);
                    out.write(attachment);
                    out.flush();
                    out.close();
                    break;
            }
        }
    }

	private MailingComponentDao getComponentDao() {
		if (mailingComponentDao == null) {
			mailingComponentDao = (MailingComponentDao) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("MailingComponentDao");
		}
		return mailingComponentDao;
	}

	private PreviewFactory getPreviewFactory() {
		if (previewFactory == null) {
			previewFactory = (PreviewFactory) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("PreviewFactory");
		}
		return previewFactory;
	}

	private ComRecipientDao getRecipientDao() {
		if (recipientDao == null) {
			recipientDao = (ComRecipientDao) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("RecipientDao");
		}
		return recipientDao;
	}
}
