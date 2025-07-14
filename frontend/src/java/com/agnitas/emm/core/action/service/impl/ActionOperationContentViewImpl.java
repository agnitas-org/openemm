/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;
import java.util.Objects;

import com.agnitas.dao.MailingDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationContentViewParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.mailing.preview.service.HtmlPreviewOptions;
import com.agnitas.mailing.preview.service.MailingPreviewService;

public class ActionOperationContentViewImpl implements EmmActionOperation {
	
	/** The logger. */
	private static final Logger logger = LogManager.getLogger(ActionOperationContentViewImpl.class);

	private MailingDao mailingDao;
	private MailingPreviewService mailingPreviewService;

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
           		// Removing HTML tags and cut at line feed was also done by old preview implementation
           		final HtmlPreviewOptions options = HtmlPreviewOptions.createDefault()
           				.withRemoveHtmlTags(true)
           				.withLineFeed(aMailing.getEmailParam().getLinefeed());

           		if(tagName.compareTo("") == 0) {
               		archiveHtml = mailingPreviewService.renderHtmlPreview(tmpMailingID, customerID, options);
               	} else {
               		final String fragment = String.format("[agnDYN name=\"%s\"/]", tagName);

               		archiveHtml = mailingPreviewService.renderPreviewFor(tmpMailingID, customerID, fragment);
               	}
                archiveSender = mailingPreviewService.renderPreviewFor(tmpMailingID, customerID, aMailing.getEmailParam().getFromAdr());
                archiveSubject = mailingPreviewService.renderPreviewFor(tmpMailingID, customerID, aMailing.getEmailParam().getSubject());
                
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

	public final void setMailingPreviewService(final MailingPreviewService service) {
		this.mailingPreviewService = Objects.requireNonNull(service, "MailingPreviewService is null");
	}
}
