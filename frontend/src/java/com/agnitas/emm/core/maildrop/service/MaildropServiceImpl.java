/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.maildrop.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.agnitas.dao.MaildropStatusDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.maildrop.InvalidMailingTypeException;
import com.agnitas.emm.core.maildrop.MaildropException;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.MailingAlreadySentException;
import com.agnitas.emm.core.maildrop.UnknownMailingException;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;

public class MaildropServiceImpl implements MaildropService {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MaildropServiceImpl.class);

	private SteppingAndBlocksizeComputer steppingAndBlocksizeComputer;
	
	private ComMailingDao mailingDao;
	private MaildropStatusDao maildropStatusDao;
	
	@Override
	public final boolean stopWorldMailingBeforeGeneration(final int companyID, final int mailingID) {
		return maildropStatusDao.delete(companyID, mailingID, MaildropStatus.WORLD, MaildropGenerationStatus.SCHEDULED);
	}

	@Override
	public final int scheduleAdminMailing(final int mailingID, final int companyID, final int adminTargetID) throws MaildropException {
		checkMailtype(mailingID, MailingType.NORMAL);

		final MaildropEntry entry = MaildropEntryFactory.newAdminMaildrop(mailingID, companyID, adminTargetID);
		
		return this.maildropStatusDao.saveMaildropEntry(entry);
	}

	@Override
	public final int scheduleTestMailing(final int mailingID, final int companyID, final int testTargetID) throws MaildropException {
		checkMailtype(mailingID, MailingType.NORMAL);

		final MaildropEntry entry = MaildropEntryFactory.newTestMaildrop(mailingID, companyID, testTargetID);
		
		return this.maildropStatusDao.saveMaildropEntry(entry);
	}

	@Override
	public final void scheduleWorldMailing(final int mailingID, final int companyID, final Date sendDate, final int stepping, final int blocksize) throws MaildropException {
		checkMailtype(mailingID, MailingType.NORMAL);
		
		if(hasMaildropStatus(mailingID, companyID, MaildropStatus.WORLD)) {
			if(logger.isInfoEnabled()) {
				logger.info(String.format("Cannot schedule mailing %d for world delivery. Mailing already sent", mailingID));
			}
			
			throw new MailingAlreadySentException(mailingID);
		}
		
		// TODO: Compute genstatus, gendate, etc.
	}

	@Override
	public final void scheduleWorldMailing(final int mailingID, final int companyID, final Date sendDate, final int mailsPerHour) throws MaildropException {
		final SteppingAndBlocksize sab = this.steppingAndBlocksizeComputer.computeFromMailingsPerHour(mailsPerHour);
		
		scheduleWorldMailing(mailingID, companyID, sendDate, sab.getStepping(), sab.getBlocksize());
	}

	@Override
	public final void activateDatebasedMailing(final int mailingID, final int companyID, final int hour, final int stepping, final int blocksize) throws MaildropException {
		checkMailtype(mailingID, MailingType.DATE_BASED);
	}

	@Override
	public final void activateDatebasedMailing(final int mailingID, final int companyID, final int hour, final int mailsPerHour) throws MaildropException {
		final SteppingAndBlocksize sab = this.steppingAndBlocksizeComputer.computeFromMailingsPerHour(mailsPerHour);

		activateDatebasedMailing(mailingID, companyID, hour, sab.getStepping(), sab.getBlocksize());
	}

	@Override
	public final void deactivateDatebasedMailing(final int mailingID, final int companyID) throws MaildropException {
		// Check for world mailing not required here. Either there is a "Date"-maildrop entry or not.
	}

	@Override
	public void activateActionbasedMailing(final int mailingID, final int companyID) throws MaildropException {
		checkMailtype(mailingID, MailingType.ACTION_BASED);
	}

	@Override
	public void deactivateActionbasedMailing(final int mailingID, final int companyID) throws MaildropException {
		// Check for world mailing not required here. Either there is a "Action"-maildrop entry or not.
	}

	@Override
	public final boolean isActiveMailing(final int mailingID, final int companyID) {
		return hasMaildropStatus(mailingID, companyID, MaildropStatus.ACTION_BASED, MaildropStatus.DATE_BASED, MaildropStatus.WORLD);		
	}

	@Override
	public final boolean hasMaildropStatus(final int mailingID, final int companyID, final MaildropStatus... statusList) {
		final Collection<MaildropEntry> entries = this.maildropStatusDao.listMaildropStatus(mailingID, companyID);
		
		for(final MaildropEntry entry : entries) {
			for(MaildropStatus status : statusList) {
				if(entry.getStatus() == status.getCode())
					return true;
			}
		}

		return false;
	}

	@Override
	@Transactional
	public void selectTestRecipients(@VelocityCheck int companyId, int maildropStatusId, List<Integer> customerIds) {
		if (companyId > 0 && maildropStatusId > 0 && CollectionUtils.isNotEmpty(customerIds)) {
			if (maildropStatusDao.setSelectedTestRecipients(companyId, maildropStatusId, true)) {
				maildropStatusDao.setTestRecipients(maildropStatusId, customerIds);
			}
		}
	}

	/**
	 * Checks, that the mailing type of given mailing ID matches excepted mailing type.
	 * 
	 * @param mailingID ID of mailing
	 * @param expectedMailingType expected mailing type
	 * 
	 * @throws UnknownMailingException if mailing ID is unknown
	 * @throws InvalidMailingTypeException if mailing type does not match
	 */
	private final void checkMailtype(final int mailingID, final MailingType expectedMailingType) throws UnknownMailingException, InvalidMailingTypeException {
		final int currentMailingTypeCode = this.mailingDao.getMailingType(mailingID);
		
		if(currentMailingTypeCode == -1) {
			throw new UnknownMailingException(mailingID);
		}
		
		final MailingType currentMailingType = MailingType.fromCode(currentMailingTypeCode);
		
		if(!expectedMailingType.equals(currentMailingType)) {
			throw new InvalidMailingTypeException(expectedMailingType, currentMailingType);
		}
	}
	
	@Required
	public void setMailingDao(final ComMailingDao dao) {
		this.mailingDao = dao;
	}
	
	@Required
	public void setMaildropStatusDao(final MaildropStatusDao dao) {
		this.maildropStatusDao = dao;
	}
}
