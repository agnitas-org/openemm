/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.userform.service.UserFormExecutionService;
import com.agnitas.messages.I18nString;
import com.agnitas.preview.Page;
import com.agnitas.preview.Preview;
import com.agnitas.preview.PreviewFactory;
import com.agnitas.preview.PreviewHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionOperationGetArchiveMailingImpl implements EmmActionOperation<ActionOperationGetArchiveMailingParameters> {
	
	private static final Logger logger = LogManager.getLogger(ActionOperationGetArchiveMailingImpl.class);

	private final PreviewFactory previewFactory;
	private final MailingDao mailingDao;

	public ActionOperationGetArchiveMailingImpl(PreviewFactory previewFactory, MailingDao mailingDao) {
		this.previewFactory = previewFactory;
		this.mailingDao = mailingDao;
	}

	@Override
	public boolean execute(
			ActionOperationGetArchiveMailingParameters getArchiveActionOperation,
			Map<String, Object> params,
			EmmActionOperationErrors actionOperationErrors
	) {
		int companyID = getArchiveActionOperation.getCompanyId();
		int expireDay = getArchiveActionOperation.getExpireDay();
		int expireMonth = getArchiveActionOperation.getExpireMonth();
		int expireYear = getArchiveActionOperation.getExpireYear();

		String archiveSubject = null;
		String archiveSender = null;

		if (expireDay != 0 && expireMonth != 0 && expireYear != 0) {
			GregorianCalendar exp = new GregorianCalendar(expireYear, expireMonth - 1, expireDay);
			GregorianCalendar now = new GregorianCalendar();

			if (now.after(exp)) {
				actionOperationErrors.addErrorCode(EmmActionOperationErrors.ErrorCode.ARCHIVE_EXPIRED);
				return false;
			}
		}

		if (params.get("customerID") == null) {
			actionOperationErrors.addErrorCode(EmmActionOperationErrors.ErrorCode.MISSING_CUSTOMER_ID);
			return false;
		}
		if (params.get("mailingID") == null) {
			actionOperationErrors.addErrorCode(EmmActionOperationErrors.ErrorCode.MISSING_MAILING_ID);
			return false;
		}

		Integer customerID;
		try {
            customerID = parseIntegerParam(params.get("customerID"));
        } catch (Exception e) {
			actionOperationErrors.addErrorCode(EmmActionOperationErrors.ErrorCode.INVALID_CUSTOMER_ID);
			return false;
		}

		Integer mailingID;
		try {
			mailingID = parseIntegerParam(params.get("mailingID"));
		} catch (Exception e) {
			actionOperationErrors.addErrorCode(EmmActionOperationErrors.ErrorCode.INVALID_MAILING_ID);
			return false;
		}

		ExtensibleUID uid = (ExtensibleUID) params.get ("_uid");
		long sendDate = uid != null ? uid.getSendDate () : 0L;

		try {
			Page previewResult = generateBackEndPreview(mailingID, customerID, sendDate);
			
			String archiveHtml;
			// Check if mailing deleted - if it deleted change preview to error
			// message on success form
			if (mailingDao.exist(mailingID, companyID)) {
				archiveHtml = generateHTMLPreview(mailingID, customerID, sendDate);
			} else {
				Locale locale = (Locale) params.get("locale");
				archiveHtml = I18nString.getLocaleString("mailing.content.not.avaliable", locale != null ? locale : Locale.getDefault());
			}
			String header = previewResult.getHeader();
			if (header != null) {
				archiveSender = PreviewHelper.getFrom(header);
				archiveSubject = PreviewHelper.getSubject(header);
			}

			
			params.put(UserFormExecutionService.FORM_MIMETYPE_PARAM_NAME, "text/html;charset=utf-8");
			params.put("archiveHtml", archiveHtml);
			params.put("archiveSender", archiveSender);
			params.put("archiveSubject", archiveSubject);
			
			return true;
		} catch (Exception e) {
			logger.error("archive problem: {}", e.getMessage(), e);
			actionOperationErrors.addErrorCode(EmmActionOperationErrors.ErrorCode.GENERAL_ERROR);
			return false;
		}
	}

	@Override
	public ActionOperationType processedType() {
        return ActionOperationType.GET_ARCHIVE_MAILING;
	}

	private Page generateBackEndPreview(int mailingID, int customerID, long sendDate) {
		Preview preview = previewFactory.createPreview();
		Page output = preview.makePreview(mailingID, customerID, false, sendDate);
		preview.done();
		return output;
	}

	protected String generateHTMLPreview(int mailingID, int customerID, long sendDate) throws Exception {
		logger.debug("entering generateHTMLPreview in ActionOperationGetArchiveMailing.");
		Preview preview = previewFactory.createPreview();
		Page page = preview.makePreview(mailingID, customerID, null, false, false, sendDate);
		if (page.getError() != null) {
			throw new Exception("ScriptHelperService::generateBackEndHTMLPreview: Error generating preview. mailingID: " + mailingID + " customerID: " + customerID
					+ "\n previewError: " + page.getError());
		}
		return page.getHTML();
	}
}
