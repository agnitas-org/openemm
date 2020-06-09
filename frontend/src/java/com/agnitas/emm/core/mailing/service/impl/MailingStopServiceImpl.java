/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.util.List;
import java.util.Objects;

import org.agnitas.emm.core.commons.util.CompanyInfoDao;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingStopService;
import com.agnitas.emm.core.mailing.service.MailingStopServiceException;
import com.agnitas.emm.core.serverprio.server.ServerPrioService;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.codegen.CodeGenerationFlags;
import com.agnitas.emm.core.target.eql.codegen.CodeGenerationFlags.Flag;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCode;

/**
 * Implementation of {@link MailingStopService} interface.
 */
public final class MailingStopServiceImpl implements MailingStopService {

	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(MailingStopServiceImpl.class);

	/** Service dealing with mailings. */
	private MailingService mailingService;
	
	/** Service deleaing with maildrops. */
	private MaildropService maildropService;
	
	/** Service dealing with server prios. */
	private ServerPrioService serverPrioService;
	
	private CopyMailingService copyMailingService;
	private CompanyInfoDao companyInfoDao;
	private EqlFacade eqlFacade;
	
	@Override
	public final boolean stopRegularMailing(final int companyID, final int mailingID, final boolean includeUnscheduled) throws MailingStopServiceException {
		final LightweightMailing mailing = this.mailingService.getLightweightMailing(companyID, mailingID);

		// Check that designated mailing is a regular mailing
		requireRegularMailing(mailing);
		
		return stopMailing(mailing, includeUnscheduled);
	}
	
	private final boolean stopMailing(final LightweightMailing mailing, final boolean includeUnscheduled) {
		// Try to cancel mailing before starting generation
		final boolean mailingStoppedBeforeGeneration = this.maildropService.stopWorldMailingBeforeGeneration(mailing.getCompanyID(), mailing.getMailingID());

		if(mailingStoppedBeforeGeneration) {
            mailingService.updateStatus(mailing.getCompanyID(), mailing.getMailingID(), "canceled");
			stopFollowUpMailings(mailing, includeUnscheduled);
			
			return true;
		} else {
			// Stopping before generation was not successful. Generation or delivery is in progress.
			final boolean result = serverPrioService != null && serverPrioService.pauseMailGenerationAndDelivery(mailing.getMailingID());
			
			if(result) {
                mailingService.updateStatus(mailing.getCompanyID(), mailing.getMailingID(), "canceled");
    			stopFollowUpMailings(mailing, includeUnscheduled);
			}
			
			return result;
		}
	}
	
	private final void stopFollowUpMailings(final LightweightMailing mailing, final boolean includeUnscheduled) {
        final List<Integer> followupMailings = mailingService.listFollowupMailingIds(mailing.getCompanyID(), mailing.getMailingID(), includeUnscheduled);
        
        for (int followupMailingID : followupMailings) {
        	final LightweightMailing followupMailing = mailingService.getLightweightMailing(mailing.getCompanyID(), followupMailingID);
        	
        	stopMailing(followupMailing, includeUnscheduled);
        }
	}

	@Override
	public final boolean resumeRegularMailing(int companyID, int mailingID) throws MailingStopServiceException {
		final LightweightMailing mailing = mailingService.getLightweightMailing(companyID, mailingID);
		
		// Check that designated mailing is a regular mailing
		requireRegularMailing(mailing);
		
		return serverPrioService != null && serverPrioService.resumeMailGenerationAndDelivery(mailingID);
	}

	@Override
	public final int copyMailingForResume(final int companyID, final int mailingID, final String shortnameOfCopy, final String descriptionOfCopy) throws MailingStopServiceException {
		try {
		   	// Copy mailing
	    	final int newMailingId = this.copyMailingService.copyMailing(
	    			companyID,
	    			mailingID,
	    			companyID,
	    			shortnameOfCopy,
	    			descriptionOfCopy);
	
	    	try {
		    	// Create and register exclusion SQL
		    	final String exclusionEQL = String.format("NOT RECEIVED MAILING %d", mailingID);
		    	final SqlCode exclusionSqlCode = this.eqlFacade.convertEqlToSql(
		    			exclusionEQL,
		    			companyID,
		    			CodeGenerationFlags.DEFAULT_FLAGS.setFlag(Flag.IGNORE_TRACKING_VETO));
		    	
		    	this.companyInfoDao.writeConfigValue(
		    			companyID,
		    			String.format("fixed-target-clause[%d]", newMailingId),
		    			exclusionSqlCode.getSql(),
		    			"Added by copying stopped mailing");
		    	
		    	
		    	// Delete original mailing
		    	final MailingModel mailingToDelete = new MailingModel();
		    	mailingToDelete.setCompanyId(companyID);
		    	mailingToDelete.setMailingId(mailingID);
		    	mailingToDelete.setTemplate(false);
		    	mailingService.deleteMailing(mailingToDelete);
		
		    	return newMailingId;
	    	} catch(final Exception e) {
	    		// Clean up
		    	final MailingModel mailingToDelete = new MailingModel();
		    	mailingToDelete.setCompanyId(companyID);
		    	mailingToDelete.setMailingId(newMailingId);
		    	mailingToDelete.setTemplate(false);
	    		
	    		this.mailingService.deleteMailing(mailingToDelete);
	    		
	    		throw e;
	    	}
		} catch(final Exception e) {
			final String msg = String.format("Error creating copy of mailing %d", mailingID);
			LOGGER.error(msg, e);
			
			throw new MailingStopServiceException(msg, e);
		}
	}
	
	@Override
	public final boolean canStopRegularMailing(final int companyID, final int mailingID) {
		final LightweightMailing mailing = this.mailingService.getLightweightMailing(companyID, mailingID);

		return canStopRegularMailing(mailing);
	}
	
	private final boolean canStopRegularMailing(final LightweightMailing mailing) {
		final boolean inProgress = this.maildropService.hasMaildropStatus(mailing.getMailingID(), mailing.getCompanyID(), MaildropStatus.WORLD)
				&& !this.mailingService.isDeliveryComplete(mailing);
		
		return inProgress
				&& !canResumeRegularMailing(mailing);
	}
	
	@Override
	public final boolean canResumeRegularMailing(final int companyID, final int mailingID) {
		final LightweightMailing mailing = this.mailingService.getLightweightMailing(companyID, mailingID);

		return canResumeRegularMailing(mailing);
	}
	
	private final boolean canResumeRegularMailing(final LightweightMailing mailing) {
		if(serverPrioService != null && checkIsRegularMailing(mailing)) {
			return this.serverPrioService.isMailGenerationAndDeliveryPaused(mailing.getMailingID());
		} else {
			return false;
		}
		
	}

	private final void requireRegularMailing(final LightweightMailing mailing) throws MailingStopServiceException {
		if(!checkIsRegularMailing(mailing)) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Mailing %d is not a WORLD mailing", mailing));
			}
			
			throw new MailingStopServiceException("Not a REGULAR mailing");
		}
	}
	
	private final boolean checkIsRegularMailing(final LightweightMailing mailing) {
		return mailing.getMailingType() == MailingType.NORMAL.getCode();
	}

	@Required
	public final void setMailingService(final MailingService service) {
		this.mailingService = Objects.requireNonNull(service, "Mailing service is null");
	}
	
	@Required
	public final void setMaildropService(final MaildropService service) {
		this.maildropService = Objects.requireNonNull(service, "Maildrop service is null");
	}
	
	public final void setServerPrioService(final ServerPrioService service) {
		this.serverPrioService = Objects.requireNonNull(service, "Server prio service is null");
	}
	
	@Required
	public final void setCopyMailingService(final CopyMailingService service) {
		this.copyMailingService = Objects.requireNonNull(service, "Mailing copy service is null");
	}
	
	@Required
	public final void setCompanyInfoDao(final CompanyInfoDao dao) {
		this.companyInfoDao = Objects.requireNonNull(dao, "Company info DAO is null");
	}
	
	@Required
	public final void setEqlFacade(final EqlFacade facade) {
		this.eqlFacade = Objects.requireNonNull(facade, "EQL facade is null");
	}
}
