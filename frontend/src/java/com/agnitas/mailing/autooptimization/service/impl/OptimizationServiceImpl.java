/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.service.impl;

import static com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_CLICKRATE;
import static com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_OPENRATE;
import static com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_REVENUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DeliveryStat;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.Target;
import com.agnitas.beans.impl.MaildropDeleteException;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.core.components.service.MailingTriggerService;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.service.CopyMailingService;
import com.agnitas.emm.core.mailing.service.MailingParameterService;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.mailing.autooptimization.beans.CampaignStatEntry;
import com.agnitas.mailing.autooptimization.beans.Optimization;
import com.agnitas.mailing.autooptimization.beans.impl.AutoOptimizationStatus;
import com.agnitas.mailing.autooptimization.dao.OptimizationDao;
import com.agnitas.mailing.autooptimization.service.OptimizationCommonService;
import com.agnitas.mailing.autooptimization.service.OptimizationService;
import com.agnitas.mailing.autooptimization.service.OptimizationStatService;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OptimizationServiceImpl implements OptimizationService {
	
	private static final Logger logger = LogManager.getLogger(OptimizationServiceImpl.class);
	
	/** This flag indicates, that finishOptimizationsSingle() is already running. */
	private volatile boolean optimizationInProgress;
	
	private final OptimizationDao optimizationDao;
	private final TargetDao targetDao;
	private final MailingDao mailingDao;
	private final OptimizationCommonService optimizationCommonService;
	private final OptimizationStatService optimizationStatService;
	private final MailingParameterService mailingParameterService;
	private final CopyMailingService copyMailingService;
	private final MailingTriggerService mailingTriggerService;

	public OptimizationServiceImpl(OptimizationDao optimizationDao, TargetDao targetDao, MailingDao mailingDao,
                                   OptimizationCommonService optimizationCommonService, OptimizationStatService optimizationStatService,
                                   MailingParameterService mailingParameterService, CopyMailingService copyMailingService, MailingTriggerService mailingTriggerService) {
		this.optimizationDao = optimizationDao;
		this.targetDao = targetDao;
		this.mailingDao = mailingDao;
		this.optimizationCommonService = optimizationCommonService;
		this.optimizationStatService = optimizationStatService;
		this.mailingParameterService = mailingParameterService;
		this.copyMailingService = copyMailingService;
        this.mailingTriggerService = mailingTriggerService;
    }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agnitas.mailing.autooptimization.service.OptimizationService#delete(com.agnitas.mailing.autooptimization.beans.Optimization)
	 */
	@Override
	public boolean delete(Optimization optimization) throws MaildropDeleteException {
		if (optimization.getStatus() == AutoOptimizationStatus.SCHEDULED.getCode()) {
			optimizationCommonService.unscheduleOptimization(optimization);
		}
		return optimizationDao.delete(optimization);
	}

	@Override
	public String findName(int optimizationId, int companyId) {
		return optimizationDao.findName(optimizationId, companyId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agnitas.mailing.autooptimization.service.OptimizationService#save(com.agnitas.mailing.autooptimization.beans.Optimization)
	 */
	@Override
	public int save(Optimization optimization) {
		List<Integer> testMailingIds = optimization.getTestmailingIDs();

		//if optimization is workflow driven - don't change mediatype email parameters
		if (optimization.getWorkflowId() <= 0) {
			for (Integer mailingId : testMailingIds) {
				Mailing testmailing = mailingDao.getMailing(mailingId, optimization.getCompanyID());
				MediatypeEmail emailParam = testmailing.getEmailParam();
				emailParam.setDoublechecking(optimization.isDoubleCheckingActivated());
				mailingDao.saveMailing(testmailing, false);
			}
		}

		return optimizationDao.save(optimization);
	}

    @Override
    public int getOptimizationIdByFinalMailing(int finalMailingId, int companyId) {
        if (finalMailingId > 0 && companyId > 0) {
        	return optimizationDao.getOptimizationByFinalMailingId(finalMailingId, companyId);
		}
		return 0;
    }
    
    private MaildropEntry getEffectiveMaildrop(Set<MaildropEntry> entries, boolean isTestRun) {
		for (MaildropEntry e : entries) {
			if (isTestRun) {
				if (e.getStatus() == MaildropStatus.TEST.getCode()) {
					return e;
				}
			} else {
				if (e.getStatus() == MaildropStatus.WORLD.getCode()) {
					return e;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.agnitas.mailing.autooptimization.service.OptimizationService#listWorkflowManaged(int, int)
	 */
	@Override
	public List<Optimization> listWorkflowManaged(int workflowId, int companyID) {
		return optimizationDao.listWorkflowManaged(workflowId, companyID);
	}

	private boolean sendFinalMailing(Mailing mailing, boolean testRun, int blockSize, int stepping) {
		logger.debug("sendFinalMailing(" + (testRun ? "test run" : "") + "), mailing ID " + mailing.getId() + ", hashCode(this) = " + hashCode());

		MaildropEntry drop = new MaildropEntryImpl();

		Date now = new Date();
		drop.setSendDate(now);
		drop.setGenDate(now);
		drop.setGenChangeDate(now);
		drop.setMailingID(mailing.getId());
		drop.setCompanyID(mailing.getCompanyID());

		if (testRun) {
			drop.setStatus(MaildropStatus.TEST.getCode());
			drop.setGenStatus(MaildropGenerationStatus.SCHEDULED.getCode());
		} else {
			drop.setBlocksize(blockSize);
			drop.setStepping(stepping);
			drop.setStatus(MaildropStatus.WORLD.getCode());
			drop.setGenStatus(MaildropGenerationStatus.NOW.getCode());
		}

		mailing.getMaildropStatus().add(drop);
		mailingDao.saveMailing(mailing, false);
		mailingDao.updateStatus(drop.getCompanyID(), drop.getMailingID(), testRun ? MailingStatus.TEST : MailingStatus.SCHEDULED, now);

		if (testRun) {
			return true;
		}

		return mailingTriggerService.triggerMailing(drop.getId(), mailing.getMailingType());
	}

	/**
	 * chooses the mailing with best open- or clickrate , clones it and sends it to the remaining recipients
	 */
	private boolean finishOptimization(Optimization optimization) {
		logger.debug("finishOptimization(), optimization ID {}, hashCode(this) = {}", optimization.getId(), this.hashCode());

		boolean result = true;
		Target splitPart = null;

		int status = getState(optimization);
		optimization.setStatus(status);

		if (optimization.getStatus() == AutoOptimizationStatus.TEST_SEND.getCode()) {
			logger.debug("finishOptimization(), optimization ID {}, status = STATUS_TEST_SEND, hashCode(this) = {}", optimization.getId(), this.hashCode());
			optimization.setStatus(AutoOptimizationStatus.EVAL_IN_PROGRESS.getCode());

			save(optimization);

			int bestMailing = calculateBestMailing(optimization);
			logger.debug("finishOptimization(), optimization ID {}, bestMailing = {}, hashCode(this) = {}", optimization.getId(), bestMailing, this.hashCode());

			if (bestMailing != 0) {
				optimization.setResultMailingID(bestMailing);

				Mailing orgMailing = mailingDao.getMailing(bestMailing, optimization.getCompanyID());
				
				// Read mailing parameters
				List<MailingParameter> mailingParameters = mailingParameterService.getMailingParameters(optimization.getCompanyID(), bestMailing);
				
				int copiedMailingID = copyMailing(orgMailing);
				Mailing mailing = mailingDao.getMailing(copiedMailingID, orgMailing.getCompanyID());

				mailing.setCampaignID(orgMailing.getCampaignID());
				mailing.setMailingType(orgMailing.getMailingType());
				mailing.setTargetID(orgMailing.getTargetID());
				mailing.setTargetExpression(orgMailing.getTargetExpression());
				mailing.setCampaignID(orgMailing.getCampaignID());
				mailing.setMediatypes(orgMailing.getMediatypes());

				mailing.setShortname(optimization.getShortname());
				mailing.setDescription("AutoOptMail");

				int i = 3;
				if (optimization.getGroup3() != 0) {
					i++;
				}
				if (optimization.getGroup4() != 0) {
					i++;
				}
				if (optimization.getGroup5() != 0) {
					i++;
				}

				splitPart = targetDao.getListSplitTarget(optimization.getSplitType(), i, optimization.getCompanyID());

				mailing.setSplitID(splitPart.getId());
				mailing.setMailTemplateID(bestMailing);
				
				// Create clone copy of mailing parameters
				if (mailingParameters != null) {
					List<MailingParameter> copiedMailingParameters = new ArrayList<>();
					for (MailingParameter templateMailingParameter : mailingParameters) {
						MailingParameter copiedMailingParameter = new MailingParameter();
						
						copiedMailingParameter.setName(templateMailingParameter.getName());
						copiedMailingParameter.setValue(templateMailingParameter.getValue());
						copiedMailingParameter.setDescription(templateMailingParameter.getDescription());
						copiedMailingParameter.setCreationDate(templateMailingParameter.getCreationDate());
						
						copiedMailingParameters.add(copiedMailingParameter);
					}
					mailing.setParameters(copiedMailingParameters);
				}

				if (mailingDao.saveMailing(mailing, false) != 0) {
					if (mailing.getParameters() != null) {
						// Save mailing parameters only if list of parameters is not null
						mailingParameterService.updateParameters(mailing.getCompanyID(), mailing.getId(), mailing.getParameters(), 0);
					}
					MaildropEntry orgDropEntry = getEffectiveMaildrop(orgMailing.getMaildropStatus(), optimization.isTestRun());
					int blockSize = 0;
					int stepping = 0;
					if (orgDropEntry != null) {
						blockSize = orgDropEntry.getBlocksize();
						stepping = orgDropEntry.getStepping();
					}
					result = sendFinalMailing(mailing, optimization.isTestRun(), blockSize, stepping);
					
					optimization.setFinalMailingId( mailing.getId());
				} else {
					result = false;
				}
				logger.debug("finishOptimization(), optimization ID {}, final mailing = {}, send result = {}, hashCode(this) = {}", optimization.getId(), mailing.getId(), result, this.hashCode());
			}
			if (result) {
				optimization.setStatus(AutoOptimizationStatus.FINISHED.getCode());
				logger.debug("finishOptimization(), optimization ID {}, state transition to STATUS_FINISHED, hashCode(this) = {}", optimization.getId(), this.hashCode());
			} else {
				optimization.setStatus(AutoOptimizationStatus.TEST_SEND.getCode());
				logger.debug("finishOptimization(), optimization ID {}, state transition to TEST_SEND, hashCode(this) = {}", optimization.getId(), this.hashCode());
			}
			save(optimization);
		}
		return result;
	}

	protected int copyMailing(Mailing mailing) {
		return copyMailingService.copyMailing(
				mailing.getCompanyID(),
				mailing.getId(),
				mailing.getCompanyID(),
				mailing.getShortname(),
				mailing.getDescription()
		);
	}
	
	protected int calculateBestMailing(Optimization optimization) {
		Hashtable<Integer , CampaignStatEntry> stats = optimizationStatService.getStat(optimization);

		if(MapUtils.isEmpty(stats)) {
			return -1;
		}

        Map<Integer, Double> mailingsToFactors = stats.entrySet().stream()
                .filter(entry -> optimization.getTestmailingIDs().contains(entry.getKey()))
                .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> calculateFactor(entry.getValue(), optimization.getEvalType())));

        Double bestFactor = Collections.max(mailingsToFactors.values());
        return MapUtils.invertMap(mailingsToFactors).get(bestFactor);
	}
	
	@Override
	public void finishOptimizationsSingle(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		
		/*
		 * This method implements a litte more complex logic to take control over execution.
		 * This method should terminate as soon as possible if another execution of this
		 * method is detected. This will release the thread taken from the thread pool as
		 * soon as possible and will not lock as longer as needed.
		 * 
		 * To ensure, that finish optimizations runs only once at a time, the "double-checked lock pattern" is used.
		 * (Note: This pattern does not work with Java before 1.5, due to a bug in "volatile"!)
		 */
		
		if( optimizationInProgress) { // This check is useful to get to synchronized() only if needed. (synchronized() is an expensive construct)
			logger.info("Finishing auto-optimizations is already in progress.");
			return;
		}
		
		synchronized( this) {
			if( optimizationInProgress) { // Do the check again. In the meantime, another thread could be come through synchronized() before the current thread. (-> Double-checked locking pattern)
				logger.info( "Finishing auto-optimizations is already in progress.");
				return;
			}

			this.optimizationInProgress = true;
		}
		
		
		try { // This try-finally block ensures, that the flag "optimizationInProgess" is reset to false in any case.
			logger.info( "Starting process to finish auto-optimizations");
			this.finishOptimizations(includedCompanyIds, excludedCompanyIds);
		} finally {
			logger.info( "finishing auto-optimizations is done");
			
			this.optimizationInProgress = false;
		}
	}

	private void finishOptimizations(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		logger.debug("finishOptimizations(), hashCode(this) = {}", this.hashCode());

		Set<Integer> checkedOptimizations = new HashSet<>();

		// date has been reached ...
		try {
			Map<Integer, Integer> map = optimizationDao.getDueOnDateOptimizations(includedCompanyIds, excludedCompanyIds);
			
			if (logger.isDebugEnabled()) {
				for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
					logger.debug("optimization (date): {}, company ID: {}, hashCode(this) = {}", entry.getKey(), entry.getValue(), this.hashCode());
				}
			}

			if (MapUtils.isNotEmpty(map)) {
				for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
					int optimizationId = entry.getKey();
					int companyId = entry.getValue();
					try {
						Optimization optimization = optimizationDao.get(optimizationId, companyId);
						finishOptimization(optimization);
						checkedOptimizations.add(optimizationId);
					} catch (Exception e) {
						logger.error( "Cannot finalize optimization, id: " + optimizationId, e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("finishOptimizations", e);
		}
		
		// threshold has been reached
		List<Optimization> dueOnThresholdOptimizations = getDueOnThresholdOptimizations(includedCompanyIds, excludedCompanyIds);

		if (logger.isDebugEnabled()) {
			for (Optimization optimization : dueOnThresholdOptimizations) {
				logger.debug("optimization (threshold): {}, company ID: {}, hashCode(this) = {}", optimization.getId(), optimization.getCompanyID(), this.hashCode());
			}
		}

		for( Optimization optimization : dueOnThresholdOptimizations ) {
			try {
				if (!checkedOptimizations.contains(optimization.getId())) {
					finishOptimization(optimization);
				}
			} catch(Exception e) {
				logger.error("Cannot finalize optimization: " + e.getMessage(), e);
			}
		}
		
		logger.debug("finishOptimizations() #DONE#, hashCode(this) = {}", this.hashCode());
	}

	/**
	 * get all the optimizations where the threshold has been reached
	 */
	private List<Optimization> getDueOnThresholdOptimizations(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		logger.debug( "getDueOnThresholdOptimizations(), hashCode(this) = " + this.hashCode());
		List<Optimization> optimizationCandidates = optimizationDao.getDueOnThresholdOptimizationCandidates(includedCompanyIds, excludedCompanyIds);
		List<Optimization> thresholdReachedOptimizations = new ArrayList<>();
		
		for (Optimization optimization:optimizationCandidates) {

			int threshold = optimization.getThreshold();

			Hashtable<Integer,CampaignStatEntry> mailingData = optimizationStatService.getStat(optimization);

			if (MapUtils.isNotEmpty(mailingData)) {
				for (CampaignStatEntry value : mailingData.values()) {
					// should clicks or openings compared with the threshold ?
					double mailingStatValue = 0;
					if (AO_CRITERIA_CLICKRATE == optimization.getEvalType()) {
						mailingStatValue = value.getClicks();
					}

					if (AO_CRITERIA_OPENRATE == optimization.getEvalType()) {
						mailingStatValue = value.getOpened();
					}

					if (AO_CRITERIA_REVENUE == optimization.getEvalType()) {
						mailingStatValue = value.getRevenue();
					}

					if (mailingStatValue >=  threshold ) { // threshold has been reached
						thresholdReachedOptimizations.add(optimization);
						logger.debug( "getDueOnThresholdOptimizations(), found optimization " + optimization.getId() + ", hashCode(this) = " + this.hashCode());
						break;
					}
				}
			}
		}

		return thresholdReachedOptimizations;
	}

	private double calculateFactor(CampaignStatEntry aEntry, WorkflowDecision.WorkflowAutoOptimizationCriteria evalType) {
		double result = 0.0;
		double subscribers = 0.0;

		subscribers = aEntry.getTotalMails() - aEntry.getBounces() - aEntry
				.getOptouts();

		if (subscribers >= 1.0) {
			switch (evalType) {
				case AO_CRITERIA_CLICKRATE:
					result = (aEntry.getClicks()) / subscribers;
					break;
	
				case AO_CRITERIA_OPENRATE:
					result = (aEntry.getOpened()) / subscribers;
					break;
	
	            case AO_CRITERIA_REVENUE:
					result = aEntry.getRevenue() / subscribers;
					break;
				
				default:
					break;
			}
		}

		return result;
	}

	@Override
	public List<Optimization> getAutoOptimizations(Admin admin, Date start, Date end) {
		return optimizationDao.getAutoOptimizations(admin.getCompanyID(), start, end);
	}

	/**
	 * Get the state from the optimization.
	 *
	 * Optimization.STATUS_NOT_STARTED :
	 * The test mailings are not scheduled, or the test mailings are scheduled and have not been created yet
	 *
	 * Optimization.STATUS_EVAL_IN_PROGRESS:
	 * The statistics for test mailings is generating ...
	 *
	 * Optimization.STATUS_TEST_SEND
	 * The test mailings started to generate or have been send
	 *
	 * Optimization.STATUS_FINISHED
	 * The optimization process is done
	 */
	private int getState(Optimization optimization) {
		
		// The state STATUS_FINISHED is the only state which is directly written to the database
		Optimization optimizationFromDB = optimizationDao.get(optimization.getId(), optimization.getCompanyID());
				
		if (optimizationFromDB.getStatus() == AutoOptimizationStatus.FINISHED.getCode()) {
			return AutoOptimizationStatus.FINISHED.getCode();
		}
		
		// ... all other optimization states depend on the states of the test mailings
		// assume the first test mailing represents the state of the others
		
		List<Integer> testMailingIDs = optimization.getTestmailingIDs();
		
		// the first testmailing ( group1 ) is the reference
		if (testMailingIDs.size() > 0) {

			int testMailingStatus = mailingDao.getLastGenstatus(testMailingIDs.get(0), optimization.isTestRun() ? MaildropStatus.TEST.getCode() : MaildropStatus.WORLD.getCode());

			//Method for selecting mailings throws no exception, but returns -1 as default, if no entry was found
			if (testMailingStatus == -1) {
				return AutoOptimizationStatus.NOT_STARTED.getCode();
			}
			
			if (testMailingStatus == DeliveryStat.STATUS_NOT_SENT) {
				return AutoOptimizationStatus.SCHEDULED.getCode();
			}
			
			if (testMailingStatus == DeliveryStat.STATUS_SENDING ||  testMailingStatus == DeliveryStat.STATUS_GENERATED || testMailingStatus == DeliveryStat.STATUS_GENERATING) {
				return AutoOptimizationStatus.TEST_SEND.getCode();
			}
			
			if (testMailingStatus == DeliveryStat.STATUS_SENT && optimization.getResultMailingID() == 0) {
				return AutoOptimizationStatus.EVAL_IN_PROGRESS.getCode();
			}
			
			if (testMailingStatus == DeliveryStat.STATUS_SENT && optimization.getResultMailingID() != 0) {
				return AutoOptimizationStatus.FINISHED.getCode();
			}
		}
		
		return AutoOptimizationStatus.NOT_STARTED.getCode();
		
	}

	@Override
	public int getFinalMailingId(int companyId, int workflowId) {
		return optimizationDao.getFinalMailingId(companyId, workflowId);
	}
}
