/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;

import com.agnitas.beans.Mailing;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.action.operations.ActionOperationContentViewParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.mailing.preview.service.HtmlPreviewOptions;
import com.agnitas.mailing.preview.service.MailingPreviewService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionOperationContentViewImpl implements EmmActionOperation<ActionOperationContentViewParameters> {
	
	private static final Logger logger = LogManager.getLogger(ActionOperationContentViewImpl.class);

	private final MailingDao mailingDao;
	private final MailingPreviewService mailingPreviewService;

	public ActionOperationContentViewImpl(MailingDao mailingDao, MailingPreviewService mailingPreviewService) {
		this.mailingDao = mailingDao;
		this.mailingPreviewService = mailingPreviewService;
	}

	@Override
	public boolean execute(
            ActionOperationContentViewParameters op,
            Map<String, Object> params,
            EmmActionOperationErrors errors
    ) {
		int companyID = op.getCompanyId();
		String tagName = op.getTagName();

        if (!params.containsKey("customerID")) {
            errors.addErrorCode(EmmActionOperationErrors.ErrorCode.MISSING_CUSTOMER_ID);
            return false;
        }

        if (!params.containsKey("mailingID")) {
            errors.addErrorCode(EmmActionOperationErrors.ErrorCode.MISSING_MAILING_ID);
            return false;
        }

        Integer customerId;
        try {
            customerId = parseIntegerParam(params.get("customerID"));
        } catch (Exception e) {
            errors.addErrorCode(EmmActionOperationErrors.ErrorCode.INVALID_CUSTOMER_ID);
            return false;
        }

        Integer mailingId;
        try {
            mailingId = parseIntegerParam(params.get("mailingID"));
        } catch (Exception e) {
            errors.addErrorCode(EmmActionOperationErrors.ErrorCode.INVALID_MAILING_ID);
            return false;
        }

        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        if (mailing.getId() == 0) {
            errors.addErrorCode(EmmActionOperationErrors.ErrorCode.UNKNOWN_MAILING_ID);
            return false;
        }

        try {
            // Removing HTML tags and cut at line feed was also done by old preview implementation
            HtmlPreviewOptions options = HtmlPreviewOptions.createDefault()
                    .withRemoveHtmlTags(true)
                    .withLineFeed(mailing.getEmailParam().getLinefeed());

            String archiveHtml;
            if (StringUtils.isBlank(tagName)) {
                archiveHtml = mailingPreviewService.renderHtmlPreview(mailingId, customerId, options);
            } else {
                String fragment = String.format("[agnDYN name=\"%s\"/]", tagName);
                archiveHtml = mailingPreviewService.renderPreviewFor(mailingId, customerId, fragment);
            }

            params.put("archiveHtml", archiveHtml);
            params.put("archiveSender", mailingPreviewService.renderPreviewFor(mailingId, customerId, mailing.getEmailParam().getFromAdr()));
            params.put("archiveSubject", mailingPreviewService.renderPreviewFor(mailingId, customerId, mailing.getEmailParam().getSubject()));

            return true;
        } catch (Exception e) {
            logger.error("archive problem: {}", e.getMessage(), e);
            errors.addErrorCode(EmmActionOperationErrors.ErrorCode.GENERAL_ERROR);
            return false;
        }
    }

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.CONTENT_VIEW;
    }
}
