/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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
import java.util.stream.Collectors;

import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.mailing.beans.MailingArchiveEntry;
import org.agnitas.emm.core.mailing.service.MailingArchiveService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Company;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveListParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.mailing.preview.service.MailingPreviewService;

public class ActionOperationGetArchiveListImpl implements EmmActionOperation {
	
	/** The logger. */
	private static final Logger logger = LogManager.getLogger(ActionOperationGetArchiveListImpl.class);

	private ExtensibleUIDService uidService;
	private MailingDao mailingDao;
	private ComCompanyDao companyDao;
	private ConfigService configService;
	private MailingPreviewService mailingPreviewService;

	private final MailingArchiveService mailingArchiveService;

	public ActionOperationGetArchiveListImpl(final MailingArchiveService mailingArchiveService) {
		this.mailingArchiveService = Objects.requireNonNull(mailingArchiveService, "mailing archive service");
	}

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors actionOperationErrors) {
		final ActionOperationGetArchiveListParameters op =(ActionOperationGetArchiveListParameters) operation;
		final int companyID = op.getCompanyId();
		final int campaignID = op.getCampaignID();

		Integer tmpNum;
		int customerID;

		if(params.get("customerID")!=null) {
            tmpNum=(Integer)params.get("customerID");
            customerID=tmpNum.intValue();
        } else {
        	actionOperationErrors.addErrorCode(ErrorCode.MISSING_CUSTOMER_ID);
	        return false;
        }

        final Company company = companyDao.getCompany(companyID);
 		if(company == null) {
        	actionOperationErrors.addErrorCode(ErrorCode.UNKNOWN_COMPANY_ID);
        	return false;
        }

		final int licenseID = this.configService.getLicenseID();
        ComExtensibleUID uid = UIDFactory.from(licenseID, companyID, customerID);

		try {
			final List<MailingArchiveEntry> mailings = this.mailingArchiveService.listMailingArchive(companyID, campaignID);

       		final Hashtable<String, String> shortnames = new Hashtable<>();
			final Hashtable<String, String> uids = new Hashtable<>();
			final Hashtable<String, String> subjects = new Hashtable<>();
			final List<String> mailingids = new LinkedList<>();

			for(final MailingArchiveEntry mailing : mailings) {
				uid = UIDFactory.copyWithNewMailingID(uid, mailing.getMailingId());
				final String uidString =  uidService.buildUIDString(uid);
				final String mailingIdString = Integer.toString(mailing.getMailingId());
				final String subject = mailingPreviewService.renderPreviewFor(mailing.getMailingId(), customerID, mailing.getEmailSubject());

				shortnames.put(mailingIdString, mailing.getShortname());
				uids.put(mailingIdString, uidString);
				subjects.put(mailingIdString, subject);
				mailingids.add(mailingIdString);
			}

			params.put("archiveListSubjects", subjects);
        	params.put("archiveListNames", shortnames);
        	params.put("archiveListUids", uids);
        	params.put("archiveListMailingIDs", mailingids);

        	if(logger.isInfoEnabled()) {
        		logger.info("generated feed");
        	}

			return true;
		} catch (Exception e) {
        	logger.error(String.format("Error creating list of mailng for archive %d", campaignID), e);
        	return false;
        }
	}

	public boolean __old__execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors actionOperationErrors) {
		ActionOperationGetArchiveListParameters op =(ActionOperationGetArchiveListParameters) operation;
		int companyID = op.getCompanyId();
		int campaignID = op.getCampaignID();

		Integer tmpNum = null;
		int customerID = 0;
		Mailing aMailing = null;
		int tmpMailingID = 0;

        Hashtable<String, String> shortnames = new Hashtable<>();
        Hashtable<String, String> uids = new Hashtable<>();
        Hashtable<String, String> subjects = new Hashtable<>();
        LinkedList<String> mailingids = new LinkedList<>();

        if(params.get("customerID")!=null) {
            tmpNum=(Integer)params.get("customerID");
            customerID=tmpNum.intValue();
        } else {
        	actionOperationErrors.addErrorCode(ErrorCode.MISSING_CUSTOMER_ID);

            return false;
        }

        final Company company = companyDao.getCompany(companyID);

        if(company == null) {
        	actionOperationErrors.addErrorCode(ErrorCode.UNKNOWN_COMPANY_ID);

        	return false;
        }

        final int licenseID = this.configService.getLicenseID();
        ComExtensibleUID uid = UIDFactory.from(licenseID, companyID, customerID);

        try {
            List<Map<String, Object>> list = mailingDao.getMailingsForActionOperationGetArchiveList(companyID, campaignID);
            for (Map<String, Object> map : list) {
                tmpMailingID = ((Number) map.get("mailing_id")).intValue();
                aMailing = mailingDao.getMailing(tmpMailingID, companyID);

                MediatypeEmail aType= aMailing.getEmailParam();

                if (aType != null) {
	                if (aType.getStatus() == MediaTypeStatus.Active.getCode()) {
	                    mailingids.add(Integer.toString(tmpMailingID));
	                    shortnames.put(Integer.toString(tmpMailingID), (String) map.get("shortname"));

	                    final String subject = mailingPreviewService.renderPreviewFor(tmpMailingID, customerID, aType.getSubject());

	                    subjects.put(Integer.toString(tmpMailingID), subject);

	                    uid = UIDFactory.copyWithNewMailingID(uid, tmpMailingID);

	                    try {
	                    	uids.put( Integer.toString(tmpMailingID), uidService.buildUIDString( uid));
	                    } catch (Exception e) {
	                    	logger.error("problem encrypt: "+e, e);
	                        return false;
	                    }
	                }
                }
            }
        } catch (Exception e) {
        	logger.error("problem: "+e, e);
        	return false;
        }

        params.put("archiveListSubjects", subjects);
        params.put("archiveListNames", shortnames);
        params.put("archiveListUids", uids);
        params.put("archiveListMailingIDs", mailingids);

        if(logger.isInfoEnabled()) {
        	logger.info("generated feed");
        }

        return true;

	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.GET_ARCHIVE_LIST;
    }

    @Required
	public final void setUidService(final ExtensibleUIDService service) {
		this.uidService = Objects.requireNonNull(service, "UID Service cannot be null");
	}

	@Required
	public final void setMailingDao(final MailingDao dao) {
		this.mailingDao = Objects.requireNonNull(dao, "Mailing DAO cannot be null");
	}

	@Required
	public final void setCompanyDao(final ComCompanyDao dao) {
		this.companyDao = Objects.requireNonNull(dao, "Company DAO cannot be null");
	}
	
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}
	
	@Required
	public final void setMailingPreviewService(final MailingPreviewService service) {
		this.mailingPreviewService = Objects.requireNonNull(service, "MailingPreviewService is null");
	}
}
