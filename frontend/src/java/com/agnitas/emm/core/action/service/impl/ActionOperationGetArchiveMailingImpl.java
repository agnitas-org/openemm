/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import org.agnitas.dao.MailingDao;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.preview.PreviewHelper;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveMailingParameters;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.messages.I18nString;

public class ActionOperationGetArchiveMailingImpl implements EmmActionOperation {
	
	private static final Logger logger = Logger.getLogger(ActionOperationGetArchiveMailingImpl.class);

    private PreviewFactory previewFactory;
	private MailingDao mailingDao;

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors actionOperationErrors) {
		ActionOperationGetArchiveMailingParameters op =(ActionOperationGetArchiveMailingParameters) operation;
		int companyID = op.getCompanyId();
		int expireDay = op.getExpireDay();
		int expireMonth = op.getExpireMonth();
		int expireYear = op.getExpireYear();
		
        Integer tmpNum=null;
        int customerID=0;
        boolean returnValue=false;
        int tmpMailingID=0;
        String archiveHtml=null;
        String archiveSubject=null;
        String archiveSender=null;
        int mobileID = 0;

	if(expireDay != 0 && expireMonth != 0 && expireYear != 0) {
		GregorianCalendar	exp=new GregorianCalendar(expireYear, expireMonth-1, expireDay);
		GregorianCalendar	now=new GregorianCalendar();

		if(now.after(exp)) {
			return false;
		}	
	}
        if(params.get("customerID")!=null) {
            tmpNum=(Integer)params.get("customerID");
            customerID=tmpNum.intValue();
        } else {
            return returnValue;
        }

        if(params.get("mailingID")!=null) {
            tmpNum=(Integer)params.get("mailingID");
            tmpMailingID=tmpNum.intValue();
        } else {
            return returnValue;
        }

        // check for mobile device        
        @SuppressWarnings("unchecked")
		Map<String, Object> requestParams = (Map<String, Object>) params.get("requestParameters");       
        Object mobileDeviceObject = requestParams.get("mobileDevice");
        if (mobileDeviceObject == null ) {
        	mobileDeviceObject = params.get("mobileDevice");	// another way to set the mobile device.        	
        }
        if (mobileDeviceObject != null ) {
        	try {
        		mobileID = Integer.parseInt((String)mobileDeviceObject);
        	} catch (Exception e) {
        		logger.error("Error converting mobileDevice ID. Expected Number and got: " + params.get("mobileDevcie"));
        		logger.error("Setting mobile Device ID to 0");
        		mobileID = 0;
        	}        	
        }
    
        try {
        	Page previewResult = generateBackEndPreview(tmpMailingID, customerID);

        	boolean mobile = false;
        	if (mobileID >0 ) {
        		mobile = true;
        	}
            //Check if mailing deleted - if it deleted change preview to error message on success form
            if (mailingDao.exist(tmpMailingID, companyID)) {
                archiveHtml = generateHTMLPreview(tmpMailingID, customerID, mobile);
            } else {
            	Locale locale = (Locale) params.get("locale");
            	archiveHtml = I18nString.getLocaleString("mailing.content.not.avaliable", locale != null ? locale : Locale.getDefault());
            }
        	String header = previewResult.getHeader();
        	if (header != null) {
        		archiveSender = PreviewHelper.getFrom(header);
        		archiveSubject = PreviewHelper.getSubject(header);
        	}	
        	returnValue=true;
        } catch (Exception e) {
        	logger.error("archive problem: "+e, e);
        	returnValue=false;
        }

        if(returnValue) {
        	params.put("archiveHtml", archiveHtml);
        	params.put("archiveSender", archiveSender);
        	params.put("archiveSubject", archiveSubject);
        }
        return returnValue;
	}

    private Page generateBackEndPreview(int mailingID,int customerID) {
		Preview preview = previewFactory.createPreview();
		Page output = preview.makePreview (mailingID,customerID, false);
		preview.done();
		return output;
	}

    protected String generateHTMLPreview(int mailingID, int customerID, boolean mobile) throws Exception {
    	logger.debug("entering generateHTMLPreview in ActionOperationGetArchiveMailing.");
    	Preview preview = previewFactory.createPreview();
		Page page = preview.makePreview(mailingID, customerID, null, false, false);
		if (page.getError() != null)  {
			throw new Exception("ScriptHelperService::generateBackEndHTMLPreview: Error generating preview. mailingID: " + mailingID +
					" customerID: " + customerID + "\n previewError: " + page.getError());
		} 
		return page.getHTML();	
    }

	public void setPreviewFactory(PreviewFactory previewFactory) {
		this.previewFactory = previewFactory;
	}

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

}
