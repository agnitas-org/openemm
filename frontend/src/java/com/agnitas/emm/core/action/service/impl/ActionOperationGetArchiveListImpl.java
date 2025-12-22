/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.agnitas.beans.IntEnum;
import com.agnitas.dao.CompanyDao;
import com.agnitas.emm.core.action.bean.ArchiveOverviewActionLimitType;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveListParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.mailing.bean.MailingArchiveEntry;
import com.agnitas.emm.core.mailing.service.MailingArchiveService;
import com.agnitas.mailing.preview.service.MailingPreviewService;
import com.agnitas.emm.core.commons.util.ConfigService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionOperationGetArchiveListImpl implements EmmActionOperation {
	
	private static final Logger logger = LogManager.getLogger(ActionOperationGetArchiveListImpl.class);

	private final ExtensibleUIDService uidService;
	private final CompanyDao companyDao;
	private final ConfigService configService;
	private final MailingPreviewService mailingPreviewService;
	private final MailingArchiveService mailingArchiveService;

	public ActionOperationGetArchiveListImpl(MailingArchiveService mailingArchiveService, ExtensibleUIDService uidService, CompanyDao companyDao,
											 ConfigService configService, MailingPreviewService mailingPreviewService) {
		this.mailingArchiveService = Objects.requireNonNull(mailingArchiveService, "mailing archive service");
		this.uidService = Objects.requireNonNull(uidService, "UID service");
		this.companyDao = Objects.requireNonNull(companyDao, "company DAO");
		this.configService = Objects.requireNonNull(configService, "config service");
		this.mailingPreviewService = Objects.requireNonNull(mailingPreviewService, "mailing preview service");
	}

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, EmmActionOperationErrors actionOperationErrors) {
		final ActionOperationGetArchiveListParameters op =(ActionOperationGetArchiveListParameters) operation;
		final int companyID = op.getCompanyId();
		final int campaignID = op.getCampaignID();

		int customerID;

		if(params.get("customerID")!=null) {
            customerID = ((Integer)params.get("customerID"));
        } else {
        	actionOperationErrors.addErrorCode(ErrorCode.MISSING_CUSTOMER_ID);
	        return false;
        }

        if (companyDao.getCompany(companyID) == null) {
        	actionOperationErrors.addErrorCode(ErrorCode.UNKNOWN_COMPANY_ID);
        	return false;
        }

		final int licenseID = this.configService.getLicenseID();
        ExtensibleUID uid = UIDFactory.from(licenseID, companyID, customerID);

		try {
			final List<MailingArchiveEntry> mailings = this.mailingArchiveService.listMailingArchive(
					campaignID,
					IntEnum.fromId(ArchiveOverviewActionLimitType.class, op.getLimitType()),
					op.getLimitValue(),
					companyID
			);

       		final Map<String, String> shortnames = new Hashtable<>();
			final Map<String, String> uids = new Hashtable<>();
			final Map<String, String> subjects = new Hashtable<>();
			final List<String> mailingids = new LinkedList<>();

			for (MailingArchiveEntry mailing : mailings) {
				uid = UIDFactory.copyWithNewMailingID(uid, mailing.getMailingId());
				final String uidString =  uidService.buildUIDString(uid);
				final String mailingIdString = Integer.toString(mailing.getMailingId());

				final boolean renderSubjectPreview = mailing.getEmailSubject().contains("[");
				final String subject = renderSubjectPreview
						? mailingPreviewService.renderPreviewFor(mailing.getMailingId(), customerID, mailing.getEmailSubject())
						: mailing.getEmailSubject();

				shortnames.put(mailingIdString, mailing.getShortname());
				uids.put(mailingIdString, uidString);
				subjects.put(mailingIdString, subject);
				mailingids.add(mailingIdString);
			}

			params.put("archiveListSubjects", subjects);
        	params.put("archiveListNames", shortnames);
        	params.put("archiveListUids", uids);
        	params.put("archiveListMailingIDs", mailingids);

			logger.info("generated feed");

			return true;
		} catch (Exception e) {
        	logger.error(String.format("Error creating list of mailng for archive %d", campaignID), e);
        	return false;
        }
	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.GET_ARCHIVE_LIST;
    }

}
