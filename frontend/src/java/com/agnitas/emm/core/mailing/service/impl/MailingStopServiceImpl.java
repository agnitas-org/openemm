/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Company;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingStopService;
import com.agnitas.emm.core.mailing.service.MailingStopServiceException;
import com.agnitas.emm.core.mailing.service.MailtrackingNotEnabledException;
import com.agnitas.emm.core.serverprio.server.ServerPrioService;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.codegen.CodeGenerationFlags;
import com.agnitas.emm.core.target.eql.codegen.CodeGenerationFlags.Flag;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCode;
import com.agnitas.emm.common.MailingStatus;
import org.agnitas.emm.core.commons.util.CompanyInfoDao;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.emm.core.mailing.service.MailingModel;
import com.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MailingStopServiceImpl implements MailingStopService {

	private static final Logger LOGGER = LogManager.getLogger(MailingStopServiceImpl.class);

	private MailingService mailingService;
	private MaildropService maildropService;
	private ServerPrioService serverPrioService; // Service dealing with server prios.
	private CopyMailingService copyMailingService;
	private CompanyInfoDao companyInfoDao;
	private EqlFacade eqlFacade;
	private CompanyService companyService;

	@Override
	public boolean isStopped(final int mailingID) {
		return serverPrioService.isMailGenerationAndDeliveryPaused(mailingID);
	}

	@Override
	public boolean stopMailing(final int companyID, final int mailingID, final boolean includeUnscheduled) throws MailingStopServiceException {
		final LightweightMailing mailing = this.mailingService.getLightweightMailing(companyID, mailingID);

		// Check that designated mailing can be stopped
		requireStoppableMailing(mailing);

		return stopMailing(mailing, includeUnscheduled);
	}

	private boolean stopMailing(final LightweightMailing mailing, final boolean includeUnscheduled) {
		assert canStopMailing(mailing);		// Ensured by caller

		// Try to cancel mailing before starting generation
		final boolean mailingStoppedBeforeGeneration = this.maildropService.stopWorldMailingBeforeGeneration(mailing.getCompanyID(), mailing.getMailingID());

		if (mailingStoppedBeforeGeneration) {
            mailingService.updateStatus(mailing.getCompanyID(), mailing.getMailingID(), MailingStatus.CANCELED);
			stopFollowUpMailings(mailing, includeUnscheduled);
			return true;
		}
        // Stopping before generation was not successful. Generation or delivery is in progress.
        final boolean result = serverPrioService.pauseMailGenerationAndDelivery(mailing.getMailingID());

        if (result) {
            mailingService.updateStatus(mailing.getCompanyID(), mailing.getMailingID(), MailingStatus.CANCELED);
            stopFollowUpMailings(mailing, includeUnscheduled);
        }
        return result;
	}

	private void stopFollowUpMailings(final LightweightMailing mailing, final boolean includeUnscheduled) {
        final List<Integer> followupMailings = mailingService.listFollowupMailingIds(mailing.getCompanyID(), mailing.getMailingID(), includeUnscheduled);

        for (int followupMailingID : followupMailings) {
        	final LightweightMailing followupMailing = mailingService.getLightweightMailing(mailing.getCompanyID(), followupMailingID);

        	stopMailing(followupMailing, includeUnscheduled);
        }
	}

	@Override
	public boolean resumeMailing(int companyId, int mailingId) throws MailingStopServiceException {
		final LightweightMailing mailing = mailingService.getLightweightMailing(companyId, mailingId);
		requireResumableMailing(mailing); // Check that designated mailing can be resumed

		// Resuming mailing is only possible with a ServerPriorService set for this service
		final boolean result = serverPrioService.resumeMailGenerationAndDelivery(mailingId);

		if (result) {
    		mailingService.updateStatus(companyId, mailingId, MailingStatus.SCHEDULED);
		}
		return result;
	}

	@Override
	public int copyMailingForResume(Admin admin, int mailingID) throws MailingStopServiceException {
		final int companyId = admin.getCompanyID();

		checkMailtrackingEnabled(companyId);

		try {
			final LightweightMailing mailing = mailingService.getLightweightMailing(admin.getCompanyID(), mailingID);

		   	// Copy mailing
	    	final int newMailingId = copyMailingService.copyMailing(
					companyId,
	    			mailingID,
					companyId,
	    			mailing.getShortname(),
	    			AgnUtils.makeCloneName(admin.getLocale(), mailing.getShortname()));

	    	try {
		    	// Create and register exclusion SQL
		    	final String exclusionEQL = String.format("NOT RECEIVED MAILING %d", mailingID);
		    	final SqlCode exclusionSqlCode = eqlFacade.convertEqlToSql(
		    			exclusionEQL,
						companyId,
		    			CodeGenerationFlags.DEFAULT_FLAGS.setFlag(Flag.IGNORE_TRACKING_VETO));

		    	companyInfoDao.writeConfigValue(
						companyId,
		    			String.format("fixed-target-clause[%d]", newMailingId),
		    			exclusionSqlCode.getSql(),
		    			"Added by copying stopped mailing");

		    	// Mark original mailing as canceled and copied
		    	mailingService.updateStatus(mailing.getCompanyID(), mailing.getMailingID(), MailingStatus.CANCELED_AND_COPIED);

		    	return newMailingId;
	    	} catch (final Exception e) {
	    		// Clean up (delete copy of mailing)
		    	final MailingModel mailingToDelete = new MailingModel();
		    	mailingToDelete.setCompanyId(companyId);
		    	mailingToDelete.setMailingId(newMailingId);
		    	mailingToDelete.setTemplate(false);

	    		this.mailingService.deleteMailing(mailingToDelete);

	    		throw e;
	    	}
		} catch (final Exception e) {
			final String msg = String.format("Error creating copy of mailing %d", mailingID);
			LOGGER.error(msg, e);
			throw new MailingStopServiceException(msg, e);
		}
	}

	@Override
	public boolean canStopMailing(final int companyID, final int mailingID) {
		final LightweightMailing mailing = this.mailingService.getLightweightMailing(companyID, mailingID);
		return canStopMailing(mailing);
	}

	private boolean canStopMailing(final LightweightMailing mailing) {
		final boolean inProgress = this.maildropService.hasMaildropStatus(mailing.getMailingID(), mailing.getCompanyID(), MaildropStatus.WORLD)
				&& !this.mailingService.isDeliveryComplete(mailing);

		return inProgress && !isStopped(mailing);
	}

	@Override
	public boolean isStopped(final int companyID, final int mailingID) {
		final LightweightMailing mailing = this.mailingService.getLightweightMailing(companyID, mailingID);
		return isStopped(mailing);
	}

	@Override
	public Date getDeliveryPauseDate(int companyId, int mailingId) {
		return serverPrioService.getDeliveryPauseDate(companyId, mailingId);
	}

	private boolean isStopped(final LightweightMailing mailing) {
		return isStopped(mailing.getMailingID());
	}

	private void requireStoppableMailing(final LightweightMailing mailing) throws MailingStopServiceException {
		if (!canStopMailing(mailing)) {
			final String msg = String.format("Mailing %d cannot be stopped", mailing.getMailingID());
            LOGGER.info(msg);
			throw new MailingStopServiceException(msg);
		}
	}

	private void requireResumableMailing(final LightweightMailing mailing) throws MailingStopServiceException {
		if (!isStopped(mailing)) {
			final String msg = String.format("Mailing %d cannot be resumed", mailing.getMailingID());
            LOGGER.info(msg);
			throw new MailingStopServiceException(msg);
		}
	}

	private void checkMailtrackingEnabled(final int companyID) throws MailingStopServiceException {
		final Company company = this.companyService.getCompany(companyID);

		if (company == null) {
			throw new MailingStopServiceException("Company not found!");
		}

		if (company.getMailtracking() == 0) {
			throw new MailtrackingNotEnabledException(companyID);
		}
	}

	public void setMailingService(final MailingService service) {
		this.mailingService = Objects.requireNonNull(service, "Mailing service is null");
	}

	public void setMaildropService(final MaildropService service) {
		this.maildropService = Objects.requireNonNull(service, "Maildrop service is null");
	}

	public void setServerPrioService(final ServerPrioService service) {
		this.serverPrioService =Objects.requireNonNull(service, "Server prio service is null");
	}

	public void setCopyMailingService(final CopyMailingService service) {
		this.copyMailingService = Objects.requireNonNull(service, "Mailing copy service is null");
	}

	public void setCompanyInfoDao(final CompanyInfoDao dao) {
		this.companyInfoDao = Objects.requireNonNull(dao, "Company info DAO is null");
	}

	public void setEqlFacade(final EqlFacade facade) {
		this.eqlFacade = Objects.requireNonNull(facade, "EQL facade is null");
	}

	public void setCompanyService(final CompanyService service) {
		this.companyService = Objects.requireNonNull(service, "companyService");
	}
}
