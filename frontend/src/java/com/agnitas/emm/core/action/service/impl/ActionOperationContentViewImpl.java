/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;

import org.agnitas.beans.Mailing;
import org.agnitas.dao.MailingDao;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationContentViewParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.mailing.web.MailingPreviewHelper;

public class ActionOperationContentViewImpl implements EmmActionOperation, ApplicationContextAware {
	
	private static final Logger logger = Logger.getLogger(ActionOperationContentViewImpl.class);

	private MailingDao mailingDao;
	private ApplicationContext con;

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors actionOperationErrors) {
		ActionOperationContentViewParameters op =(ActionOperationContentViewParameters) operation;
		int companyID = op.getCompanyId();
		String tagName = op.getTagName();
		
        Integer tmpNum=null;
        int customerID=0;
        boolean returnValue=false;
        int tmpMailingID=0;
        String archiveHtml=null;
        String archiveSubject=null;
        String archiveSender=null;

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

        Mailing aMailing=mailingDao.getMailing(tmpMailingID, companyID);

        if(aMailing.getId() != 0) {

            try {
             	if(tagName.compareTo("") == 0) {
               		archiveHtml=aMailing.getPreview(aMailing.getHtmlTemplate().getEmmBlock(), MailingPreviewHelper.INPUT_TYPE_HTML, customerID, true, con);
               	} else {
               		archiveHtml=aMailing.getPreview("[agnDYN name=\""+tagName+"\"/]", MailingPreviewHelper.INPUT_TYPE_HTML, customerID, true, con);
               	}
                archiveSender=aMailing.getPreview(aMailing.getEmailParam().getFromAdr(), MailingPreviewHelper.INPUT_TYPE_HTML, customerID, con);
                archiveSubject=aMailing.getPreview(aMailing.getEmailParam().getSubject(), MailingPreviewHelper.INPUT_TYPE_HTML, customerID, con);
                returnValue=true;
            } catch (Exception e) {
              	logger.error("archive problem: "+e, e);
            }
        }

        if(returnValue) {
            params.put("archiveHtml", archiveHtml);
            params.put("archiveSender", archiveSender);
            params.put("archiveSubject", archiveSubject);
        }
        return returnValue;
	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.CONTENT_VIEW;
    }

    public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Override
	public void setApplicationContext(ApplicationContext con) throws BeansException {
		this.con = con;
	}

}
