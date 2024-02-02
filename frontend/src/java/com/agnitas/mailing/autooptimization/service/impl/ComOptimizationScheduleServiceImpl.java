/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.service.impl;

import static com.agnitas.emm.core.workflow.service.ComWorkflowActivationService.DEFAULT_STEPPING;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.mailing.autooptimization.beans.impl.AutoOptimizationStatus;
import org.agnitas.beans.impl.MaildropDeleteException;
import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.core.mailing.MailingAllReadySentException;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComTarget;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingSendingProperties;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.service.ComOptimizationCommonService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationScheduleService;
import com.agnitas.mailing.autooptimization.service.OptimizationIsFinishedException;

public class ComOptimizationScheduleServiceImpl implements ComOptimizationScheduleService {
	/** DAO accessing mailings. */
	private ComMailingDao mailingDao;
	
	/** DAO accessing target groups. */
	private ComTargetDao targetDao;
	private ComOptimizationCommonService optimizationCommonService;
	private MaildropService maildropService;

	@Override
	public void scheduleOptimization(ComOptimization optimization) throws MailingAllReadySentException,
			OptimizationIsFinishedException, MaildropDeleteException {
		scheduleOptimization(optimization, null);
	}

	@Override
	public void scheduleOptimization(ComOptimization optimization, Map<Integer, MailingSendingProperties> properties)
			throws MailingAllReadySentException,
			OptimizationIsFinishedException, MaildropDeleteException {

		if (optimization.getStatus() == AutoOptimizationStatus.SCHEDULED.getCode()) { // re-schedule an
															// existing
															// optimization
			// keep the provided dates, unschedule will overwrite them
			Date testSendDate = optimization.getTestMailingsSendDate();
			Date sendDate = optimization.getSendDate();

			unscheduleOptimization(optimization);
			optimization.setSendDate(sendDate);
			optimization.setTestMailingsSendDate(testSendDate);
			optimization.setStatus(AutoOptimizationStatus.SCHEDULED.getCode());
		}

		boolean result = true;
		ComTarget splitPart = null;

		if (!(optimization.getStatus() == AutoOptimizationStatus.SCHEDULED.getCode() || optimization.getStatus() == AutoOptimizationStatus.NOT_STARTED.getCode())) {
			throw new OptimizationIsFinishedException("The optimization has with id: " + optimization.getId()
							+ " has state " + optimization.getStatus());
		}

		List<Mailing> allMailings = getTestMailings(optimization);

		// loop over mailings

		int i = 0;
		for (Mailing testMailing : allMailings) {
			i++;

			// check if mailingtype is 'normal' and mailing has not been sent as
			// a 'world-mailing' yet,
			// if it has been sent throw an Exception
			if (testMailing.getMailingType() != MailingType.NORMAL
					|| this.maildropService.isActiveMailing(testMailing.getId(), testMailing.getCompanyID())) {
				throw new MailingAllReadySentException(
						"Mailing has allready been sent ! Mailing-ID: "
								+ testMailing.getId());
			}

			// set mailinglist_id, target_id, split_id , doublecheck and save it
			testMailing.setMailinglistID(optimization.getMailinglistID());
			testMailing.setTargetID(0);

			String targetExpression = optimization.getTargetExpression();
			if (StringUtils.isNotEmpty(targetExpression)) {
				String[] targetGroups = targetExpression.split(",");
				String operator;

				// todo: auto-optimization doesn't seem to support "Subscriber isn't allowed to be in target-groups" target mode
				if (optimization.getTargetMode() == Mailing.TARGET_MODE_AND) {
					operator = " & ";
				} else {
					operator = " | ";
				}

				testMailing.setTargetExpression(StringUtils.join(targetGroups, operator));
			} else {
				testMailing.setTargetExpression("");
			}

			splitPart = targetDao.getListSplitTarget(optimization.getSplitType(), i, optimization.getCompanyID());
			testMailing.setSplitID(splitPart.getId());

			MediatypeEmail  emailParam = testMailing.getEmailParam();

			if (optimization.getWorkflowId() > 0 && properties != null) {
				//workflow driven auto optimization doesn't have such settings and is always false
				//so don't update such parameter from optimization fields
				MailingSendingProperties mailingSendingProperties = properties.get(testMailing.getId());
				if (mailingSendingProperties != null) {
					emailParam.setDoublechecking(mailingSendingProperties.isDoubleCheck());
					emailParam.setSkipempty(mailingSendingProperties.isSkipEmptyBlocks());
				}
			} else {
				emailParam.setDoublechecking(optimization.isDoubleCheckingActivated());
			}

			if (mailingDao.saveMailing(testMailing, false) <= 0) {
				result = false;
				break;
			}
		}

		if (result) {
			// schedule mailings by setting entries in maildrop_status_tbl
			for (Mailing testMailing : allMailings) {
				MailingSendingProperties mailingSendingProperties = null;
				if(properties != null){
					mailingSendingProperties = properties.get(testMailing.getId());
				}
				scheduleMailing(testMailing, optimization.getTestMailingsSendDate(), optimization.isTestRun(), mailingSendingProperties);
			}
			optimization.setStatus(AutoOptimizationStatus.SCHEDULED.getCode());
		} else {
			optimization.setStatus(AutoOptimizationStatus.NOT_STARTED.getCode());
		}
		optimizationCommonService.save(optimization);
	}

	@Override
	public void unscheduleOptimization(ComOptimization optimization)
			throws MaildropDeleteException {
		optimizationCommonService.unscheduleOptimization(optimization);
		
	}

	private void scheduleMailing(Mailing mailing, Date sendDate, boolean testRun, MailingSendingProperties properties) {
		MaildropEntry drop = new MaildropEntryImpl();

		drop.setStatus(testRun ? MaildropStatus.TEST.getCode() : MaildropStatus.WORLD.getCode());
		drop.setGenStatus(MaildropGenerationStatus.SCHEDULED.getCode());
		drop.setSendDate(sendDate);
		drop.setGenDate(sendDate);
		drop.setGenChangeDate(sendDate);

		drop.setMailingID(mailing.getId());
		drop.setCompanyID(mailing.getCompanyID());

		if(properties != null){
			drop.setMaxRecipients(properties.getMaxRecipients());
			Tuple<Integer, Integer> blocksizeAndStepping = AgnUtils.makeBlocksizeAndSteppingFromBlocksize(properties.getBlocksize(), DEFAULT_STEPPING);
			drop.setBlocksize(blocksizeAndStepping.getFirst());
			drop.setStepping(blocksizeAndStepping.getSecond());
		}

		mailing.getMaildropStatus().add(drop);
		mailingDao.saveMailing(mailing, false);
		mailingDao.updateStatus(drop.getCompanyID(), drop.getMailingID(), testRun ? MailingStatus.TEST : MailingStatus.SCHEDULED, sendDate);
	}

	private List<Mailing> getTestMailings(ComOptimization optimization) {

		List<Mailing> allMailings = new ArrayList<>();
		List<Integer> testMailingIDs = optimization.getTestmailingIDs();

		for (Integer mailingID : testMailingIDs) {
			Mailing mailing = mailingDao.getMailing(mailingID, optimization
					.getCompanyID());
			allMailings.add(mailing);
		}

		return allMailings;
	}

	/**
	 * Set DAO accessing mailings.
	 * 
	 * @param mailingDao DAO accessing mailings
	 */
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	/**
	 * Set DAO accessing target groups.
	 * 
	 * @param targetDao DAO accessing target groups
	 */
	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}

	@Required
	public void setMaildropService(final MaildropService service) {
		this.maildropService = service;
	}
	
	public void setOptimizationCommonService(
			ComOptimizationCommonService optimizationCommonService) {
		this.optimizationCommonService = optimizationCommonService;
	}
}
