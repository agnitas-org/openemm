/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.dao.MailingComponentDao;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.dao.ComRecipientDao;

public class ShowComponent extends HttpServlet {

	/** Logger. */
	private static final transient Logger logger = LogManager.getLogger( ShowComponent.class);
	
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
        ServletOutputStream out;
        long len;
        int compId;
        
		if (!AgnUtils.isUserLoggedIn(req)) {
            return;
        }
	
		String compIDParam = req.getParameter("compID");
		compId = NumberUtils.toInt(compIDParam, -1);
		
		if (compId < 0) {
			logger.warn("Error converting " + (compIDParam != null ? "'" + compIDParam + "'" : compIDParam) + " to integer");
			return;
		}
        
        if(compId==0) {
            return;
        }
	
		String customerIDStr = req.getParameter("customerID");
		String targetGroupIDStr = req.getParameter("targetGroupID");
		int customerID = NumberUtils.toInt(customerIDStr);
		int targetGroupID = NumberUtils.toInt(targetGroupIDStr);
        
        MailingComponent comp = getComponentDao().getMailingComponent(compId, AgnUtils.getCompanyID(req));
        
		if (comp != null) {
            try {
				switch (comp.getType()) {
					case Image:
				    case HostedImage:
					case ThumbnailImage:
						if (comp.getBinaryBlock() != null) {
							response.setContentType(comp.getMimeType());
							out=response.getOutputStream();
							out.write(comp.getBinaryBlock());
							out.flush();
							out.close();
						}
						break;
					case Attachment:
				    case PersonalizedAttachment:
				        HttpUtils.setDownloadFilenameHeader(response, comp.getComponentName());
				        response.setContentType(comp.getMimeType());
				        out=response.getOutputStream();
				       
				        byte[] attachment;
				        if (MailingComponentType.PersonalizedAttachment == comp.getType()) {
				        	attachment = getPersonalizedAttachment(comp, customerID, targetGroupID);
				        } else {
				        	attachment = comp.getBinaryBlock();
				        }
				        
				        len = attachment.length;
				        response.setContentLength((int)len);
				        out.write(attachment);
				        out.flush();
				        out.close();
				        break;
				case PrecAAttachement:
					// do not show component
					break;
				case Template:
					// do not show component
					break;
				default:
					throw new Exception("Invalid component type");
				}
			} catch (Exception e) {
				logger.error("Invalid component found: " + AgnUtils.getCompanyID(req) + "/" + compId, e);
				// do not show component
			}
        }
    }
	
	private byte[] getPersonalizedAttachment(MailingComponent comp, int customerID, int targetGroupID) {
    	if (comp == null) {
    		return new byte[0];
		}
    	int mailingID = comp.getMailingID();
		Page page;
    	Preview preview = getPreviewFactory().createPreview();
    	if (customerID > 0) {
			page = preview.makePreview(mailingID, customerID, false);
		} else if (targetGroupID > 0) {
    		page = preview.makePreview(mailingID, 0, targetGroupID);
		} else {
			Map<Integer,String> recipientList = getRecipientDao().getAdminAndTestRecipientsDescription(comp.getCompanyID(), mailingID);
			customerID = recipientList.keySet().iterator().next();
			page = preview.makePreview(mailingID, customerID, false);
		}
		return page == null ? new byte[0] : page.getAttachment(comp.getComponentName());
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
