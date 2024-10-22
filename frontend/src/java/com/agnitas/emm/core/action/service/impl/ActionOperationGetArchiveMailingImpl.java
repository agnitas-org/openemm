/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import com.agnitas.dao.MailingDao;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.preview.PreviewHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.userform.service.UserFormExecutionService;
import com.agnitas.messages.I18nString;

public class ActionOperationGetArchiveMailingImpl implements EmmActionOperation {
	
	private static final Logger logger = LogManager.getLogger(ActionOperationGetArchiveMailingImpl.class);

	private PreviewFactory previewFactory;
	private MailingDao mailingDao;

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors actionOperationErrors) {
		ActionOperationGetArchiveMailingParameters getArchiveActionOperation = (ActionOperationGetArchiveMailingParameters) operation;
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

		int customerID = ((Number) params.get("customerID")).intValue();
		int mailingID = ((Number) params.get("mailingID")).intValue();
		ComExtensibleUID uid = (ComExtensibleUID) params.get ("_uid");
		long sendDate = uid != null ? uid.getSendDate () : 0L;

		// check for mobile device
		boolean mobile = false;
		@SuppressWarnings("unchecked")
		Map<String, Object> requestParams = (Map<String, Object>) params.get("requestParameters"); // suppress warning for this cast
		Object mobileDeviceObject = requestParams.get("mobileDevice");
		if (mobileDeviceObject == null) {
			// another way to set the mobile device.
			mobileDeviceObject = params.get("mobileDevice");
		}
		if (mobileDeviceObject != null) {
			try {
				mobile = Integer.parseInt((String) mobileDeviceObject) > 0;
			} catch (Exception e) {
				logger.error("Error converting mobileDevice ID. Expected Number and got: " + params.get("mobileDevcie"));
				logger.error("Setting mobile Device ID to 0");
			}
		}

		try {
			Page previewResult = generateBackEndPreview(mailingID, customerID, sendDate);
			
			String archiveHtml;
			// Check if mailing deleted - if it deleted change preview to error
			// message on success form
			if (mailingDao.exist(mailingID, companyID)) {
				archiveHtml = generateHTMLPreview(mailingID, customerID, sendDate, mobile);
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
			logger.error("archive problem: " + e, e);
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

	protected String generateHTMLPreview(int mailingID, int customerID, long sendDate, boolean mobile) throws Exception {
		logger.debug("entering generateHTMLPreview in ActionOperationGetArchiveMailing.");
		Preview preview = previewFactory.createPreview();
		Page page = preview.makePreview(mailingID, customerID, null, false, false, sendDate);
		if (page.getError() != null) {
			throw new Exception("ScriptHelperService::generateBackEndHTMLPreview: Error generating preview. mailingID: " + mailingID + " customerID: " + customerID
					+ "\n previewError: " + page.getError());
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
