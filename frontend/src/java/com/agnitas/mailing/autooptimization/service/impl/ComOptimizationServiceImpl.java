/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.service.impl;

import static com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_CLICKRATE;
import static com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_OPENRATE;
import static com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_REVENUE;
import static com.agnitas.mailing.autooptimization.beans.ComOptimization.STATUS_EVAL_IN_PROGRESS;
import static com.agnitas.mailing.autooptimization.beans.ComOptimization.STATUS_FINISHED;
import static com.agnitas.mailing.autooptimization.beans.ComOptimization.STATUS_NOT_STARTED;
import static com.agnitas.mailing.autooptimization.beans.ComOptimization.STATUS_SCHEDULED;
import static com.agnitas.mailing.autooptimization.beans.ComOptimization.STATUS_TEST_SEND;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.impl.MaildropDeleteException;
import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.stat.CampaignStatEntry;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.beans.impl.SelectOption;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.DeliveryStat;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mailing.service.ComMailingParameterService;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.beans.impl.AutoOptimizationLight;
import com.agnitas.mailing.autooptimization.dao.ComOptimizationDao;
import com.agnitas.mailing.autooptimization.service.ComOptimizationCommonService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationStatService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ComOptimizationServiceImpl implements ComOptimizationService, ApplicationContextAware { // TODO: Remove dependency to ApplicationContextAware
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComOptimizationServiceImpl.class);
	
	/** This flag indicates, that finishOptimizationsSingle() is already running. */
	private volatile boolean optimizationInProgress;
	
	private ComOptimizationDao optimizationDao;
	
	/** DAO accessing target groups. */
	private ComTargetDao targetDao;
	
	/** DAO accessing mailings. */
	private ComMailingDao mailingDao;

	private ApplicationContext applicationContext; // when we will get rid off that ???
	// you can't reuse anything from existing code without a huge refactoring and/or including side effects and/or work with that ugly stuff ..
	
	private ComOptimizationCommonService optimizationCommonService;
	private ComOptimizationStatService optimizationStatService;
	private ComMailingParameterService mailingParameterService;
	private CopyMailingService copyMailingService;

	public ComOptimizationServiceImpl() {
		if( logger.isDebugEnabled()) {
			logger.debug("created new instance of " + this.getClass().getCanonicalName() + ", hashCode(this) = " + this.hashCode());
		}
		
		optimizationInProgress = false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agnitas.mailing.autooptimization.service.ComOptimizationService#delete(com.agnitas.mailing.autooptimization.beans.ComOptimization)
	 */
	@Override
	public boolean delete(ComOptimization optimization) throws MaildropDeleteException {
		
		if(optimization.getStatus() ==  STATUS_SCHEDULED) {
			optimizationCommonService.unscheduleOptimization(optimization);
		}
		return optimizationDao.delete(optimization);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agnitas.mailing.autooptimization.service.ComOptimizationService#save(com.agnitas.mailing.autooptimization.beans.ComOptimization)
	 */
	@Override
	public int save(ComOptimization optimization) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agnitas.mailing.autooptimization.service.ComOptimizationService#get(int,
	 *      int)
	 */
	@Override
	public ComOptimization get(int optimizationID, @VelocityCheck int companyID) {
		ComOptimization comOptimization = optimizationDao.get(optimizationID, companyID);
		 
		// For the send date use the 1st testmailing and take the maildrop status entry where the status_field = 'W' or 'T'
		int firstTestMailingID = comOptimization.getGroup1();
		if( firstTestMailingID != 0) {
			Mailing mailing = mailingDao.getMailing(firstTestMailingID, companyID);
			MaildropEntry entry = getEffectiveMaildrop(mailing.getMaildropStatus(), comOptimization.isTestRun());

			// set the testmailings senddate
			if (entry != null) {
				comOptimization.setTestMailingsSendDate(entry.getGenDate());
			}
		}
	
		// refresh the state from maildrop_status_tbl;
		
		comOptimization.setStatus(getState(comOptimization));
		
		return comOptimization;
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
	 * @see com.agnitas.mailing.autooptimization.service.ComOptimizationService#list(int,
	 *      int)
	 */
	@Override
	public List<ComOptimization> list(int campaignID, @VelocityCheck int companyID) {
		return optimizationDao.list(campaignID, companyID);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.agnitas.mailing.autooptimization.service.ComOptimizationService#listWorkflowManaged(int, int)
	 */
	@Override
	public List<ComOptimization> listWorkflowManaged(int workflowId, @VelocityCheck int companyID) {
		return optimizationDao.listWorkflowManaged(workflowId, companyID);
	}

	private boolean sendFinalMailing(Mailing mailing, boolean testRun, int blockSize, int stepping) throws Exception {
		logger.debug( "sendFinalMailing(" + (testRun ? "test run" : "") + "), mailing ID " + mailing.getId() + ", hashCode(this) = " + hashCode());

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
		mailingDao.updateStatus(drop.getMailingID(), testRun ? MailingStatus.TEST : MailingStatus.SCHEDULED);

		if (testRun) {
			return true;
		} else {
			return mailing.triggerMailing(drop.getId(), new Hashtable<>(), applicationContext);
		}
	}

	@Override
	public boolean finishOptimization(ComOptimization optimization ) throws Exception {
		logger.debug( "finishOptimization(), optimization ID " + optimization.getId() + ", hashCode(this) = " + this.hashCode());

		boolean result = true;
		ComTarget splitPart = null;
		
		int status = getState(optimization);
		optimization.setStatus(status);
		
		if (optimization.getStatus() == STATUS_TEST_SEND) {
			logger.debug( "finishOptimization(), optimization ID " + optimization.getId() + ", status = STATUS_TEST_SEND, hashCode(this) = " + this.hashCode());
			optimization.setStatus(STATUS_EVAL_IN_PROGRESS);

			save(optimization);

			int bestMailing = calculateBestMailing(optimization);
			logger.debug( "finishOptimization(), optimization ID " + optimization.getId() + ", bestMailing = " + bestMailing + ", hashCode(this) = " + this.hashCode());

			if (bestMailing != 0) {
				
				optimization.setResultMailingID(bestMailing);

				Mailing orgMailing = mailingDao.getMailing(bestMailing, optimization.getCompanyID());
				
				// Read mailing parameters
				List<ComMailingParameter> orgMailingParameters = mailingParameterService.getMailingParameters(optimization.getCompanyID(), bestMailing);
				
				int copiedMailingID = copyMailingService.copyMailing(orgMailing.getCompanyID(), orgMailing.getId(), orgMailing.getCompanyID(), orgMailing.getShortname(), orgMailing.getDescription());
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
				if (orgMailingParameters != null) {
					List<ComMailingParameter> copiedMailingParameters = new ArrayList<>();
					for (ComMailingParameter templateMailingParameter : orgMailingParameters) {
						ComMailingParameter copiedMailingParameter = new ComMailingParameter();
						
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
				logger.debug( "finishOptimization(), optimization ID " + optimization.getId() + ", final mailing = " + mailing.getId() + ", send result = " + result + ", hashCode(this) = " + this.hashCode());

			}
			if (result) {
				optimization.setStatus(STATUS_FINISHED);
				logger.debug( "finishOptimization(), optimization ID " + optimization.getId() + ", state transition to STATUS_FINISHED, hashCode(this) = " + this.hashCode());
			} else {
				optimization.setStatus(STATUS_TEST_SEND);
				logger.debug( "finishOptimization(), optimization ID " + optimization.getId() + ", state transition to TEST_SEND, hashCode(this) = " + this.hashCode());
			}
			save(optimization);
		}
		return result;
	}

	
	@Override
	public int calculateBestMailing(ComOptimization optimization) {

		Hashtable<Integer , CampaignStatEntry> stats = optimizationStatService.getStat(optimization);

		if(MapUtils.isEmpty(stats)) {
			return -1;
		}
	
		int[] mailingIDs = { optimization.getGroup2(),
				optimization.getGroup3(), optimization.getGroup4(),
				optimization.getGroup5() };
		
		int bestMailingID = optimization.getGroup1();
		double bestRate = calculateFactor(stats.get(optimization.getGroup1()), optimization.getEvalType());
				
		for(Integer mailingID : mailingIDs) {
					
			if( stats.containsKey(mailingID)) {
				if( bestRate < calculateFactor(stats.get(mailingID),optimization.getEvalType())) {
					bestMailingID = mailingID;
				}
			}
			
		}
				
		return bestMailingID;
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

	@Override
	public void finishOptimizations(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		if( logger.isDebugEnabled()) {
			logger.debug( "finishOptimizations(), hashCode(this) = " + this.hashCode());
		}

		StringBuffer result = new StringBuffer("time: "+ System.currentTimeMillis() + " ");
		Set<Integer> checkedOptimizations = new HashSet<>();

		// date has been reached ...
		try {
			Map<Integer, Integer> map = optimizationDao.getDueOnDateOptimizations(includedCompanyIds, excludedCompanyIds);
			
			if (logger.isDebugEnabled()) {
				for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
					logger.debug( "  optimization (date): " + entry.getKey() + ", company ID: " + entry.getValue() + ", hashCode(this) = " + this.hashCode());
				}
			}

			if (MapUtils.isNotEmpty(map)) {
				for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
					int optimizationId = entry.getKey();
					int companyId = entry.getValue();
					try {
						ComOptimization optimization = optimizationDao.get(optimizationId, companyId);
						finishOptimization(optimization);
						checkedOptimizations.add(optimizationId);
					} catch (Exception e) {
						logger.error( "Cannot finalize optimization, id: " + optimizationId, e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("finishOptimizations", e);

			result.append(e.getMessage());
		}
		
		// threshold has been reached
		List<ComOptimization> dueOnThresholdOptimizations = getDueOnThresholdOptimizations(includedCompanyIds, excludedCompanyIds);

		if( logger.isDebugEnabled()) {
			for( ComOptimization optimization : dueOnThresholdOptimizations) {
				logger.debug( "  optimization (threshold): " + optimization.getId() + ", company ID: " + optimization.getCompanyID() + ", hashCode(this) = " + this.hashCode());
			}
		}

		for( ComOptimization optimization : dueOnThresholdOptimizations ) {
			try {
				if (!checkedOptimizations.contains(optimization.getId())) {
					finishOptimization(optimization);
				}
			} catch(Exception e) {
				logger.error("Cannot finalize optimization: " + e.getMessage(), e);
			}
		}
		
		if( logger.isDebugEnabled()) {
			logger.debug( "finishOptimizations() #DONE#, hashCode(this) = " + this.hashCode());
		}
	}

	
	/**
	 * get all the optimizations where the threshold has been reached
	 * @return
	 */
	@Override
	public List<ComOptimization> getDueOnThresholdOptimizations(List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		logger.debug( "getDueOnThresholdOptimizations(), hashCode(this) = " + this.hashCode());
		List<ComOptimization> optimizationCandidates = optimizationDao.getDueOnThresholdOptimizationCandidates(includedCompanyIds, excludedCompanyIds);
		List<ComOptimization> thresholdReachedOptimizations = new ArrayList<>();
		
		for (ComOptimization optimization:optimizationCandidates) {

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
	public List<String[]> getSplitTypeList(@VelocityCheck int companyID, String splitType, String language) {
		List<String> splitNames = targetDao.getSplitNames(companyID);

		Map<String, Integer> splitTypes = new HashMap<>();
		Map<String, Integer> decimalSplitTypes = new HashMap<>();

		for (String splitName : splitNames) {
			boolean decimal = false;

			if (splitName.startsWith(TargetLight.LIST_SPLIT_PREFIX)) {
				splitName = splitName.substring(TargetLight.LIST_SPLIT_PREFIX.length());
			} else if (splitName.startsWith(TargetLight.LIST_SPLIT_CM_PREFIX)) {
				splitName = splitName.substring(TargetLight.LIST_SPLIT_CM_PREFIX.length());
				decimal = true;
			} else {
				logger.error("Invalid split list name prefix: " + splitName);
				continue;
			}

			int splitPartPos = splitName.lastIndexOf('_');
			if (splitPartPos < 0) {
				logger.error("Invalid split list name format: " + splitName);
				continue;
			}

			String splitBase = splitName.substring(0, splitPartPos);

			if (decimal) {
				Integer entries = decimalSplitTypes.get(splitBase);
				decimalSplitTypes.put(splitBase, entries == null ? 1 : entries + 1);
			} else {
				Integer entries = splitTypes.get(splitBase);
				splitTypes.put(splitBase, entries == null ? 1 : entries + 1);
			}
		}

		Map<String, String> mergedSplitTypes = new HashMap<>();

		for (Entry<String, Integer> entry : splitTypes.entrySet()) {
			// Auto-optimization requires at least 3 targets - two test mailings and a final mailing
			if (entry.getValue() < 3) {
				continue;
			}
			String splitBase = entry.getKey();
			String decimalBase = convertSplitBaseToDecimal(splitBase);
			if (decimalBase != null) {
				mergedSplitTypes.put(decimalBase, splitBase);
			}
		}

		for (Entry<String, Integer> entry : decimalSplitTypes.entrySet()) {
			// Auto-optimization requires at least 3 targets - two test mailings and a final mailing
			if (entry.getValue() < 3) {
				continue;
			}
			String decimalSplitBase = entry.getKey();
			// Force to overwrite an existing non-custom split type
			if (!mergedSplitTypes.containsKey(decimalSplitBase) || decimalSplitBase.equals(splitType)) {
				mergedSplitTypes.put(decimalSplitBase, decimalSplitBase);
			}
		}

		return mergedSplitTypes.entrySet()
				.stream()
				.sorted((a, b) -> a.getKey().compareTo(b.getKey()))
				.map(e -> {
					String value = e.getValue();
					if (StringUtils.equals(e.getKey(), value)) {
						String label = createLabelForDecimalSplitBase(value);
						return new String[]{ value, null, label };
					} else {
						String labelKey = "listsplit." + value;
						return new String[]{ value, labelKey, null };
					}
				})
				.collect(Collectors.toList());
	}

	// XXYYZZ -> XX.0;YY.0;ZZ.0
	private String convertSplitBaseToDecimal(String splitBase) {
		String decimalSplitBase = null;
		try {
			List<String> groups = new ArrayList<>();
			for (int i = 0; (i + 1) < splitBase.length(); i += 2) {
				double value = new Integer(splitBase.substring(i, i + 2));
				groups.add(String.valueOf(value));
			}
			decimalSplitBase = StringUtils.join(groups, ';');
		} catch (NumberFormatException e) {
			logger.error("Invalid split list name format: " + splitBase, e);
		}
		return decimalSplitBase;
	}

	// XX.0;YY.0;ZZ.0 -> XX% / YY% / ZZ%
	private String createLabelForDecimalSplitBase(String decimalSplitBase) {
		List<String> groups = new ArrayList<>();
		for (String value : decimalSplitBase.split(";")) {
			Double v = new Double(value);
			groups.add(v.intValue() + "%");
		}
		return StringUtils.join(groups, " / ");
	}

	@Override
	public List<TargetLight> getTargetGroupList(@VelocityCheck int companyID) {
		return targetDao.getTargetLights(companyID);
	}

	@Override
    public List<TargetLight> getTargets(String targetExpression, @VelocityCheck int companyID){
        Collection<Integer> targetIds = new ArrayList<>();

        if (StringUtils.isNotBlank(targetExpression)) {
            for (String targetId : targetExpression.split(",")) {
				if (StringUtils.isNotEmpty(targetId)) {
					targetIds.add(Integer.parseInt(targetId));
				}
			}
        }

        return targetDao.getUnchoosenTargetLights(companyID, targetIds);
    }

	@Override
    public List<TargetLight> getChosenTargets(String targetExpression, final int companyID){
        return targetExpression != null && !targetExpression.equals("") ? targetDao.getChoosenTargetLights(targetExpression, companyID) : new ArrayList<>();
    }

	@Override
	public List<ComOptimization> getOptimizationsForCalendar(@VelocityCheck int companyId, Date startDate, Date endDate) {
		if (startDate != null && endDate != null) {
			return optimizationDao.getOptimizationsForCalendar(companyId, startDate, endDate);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public JSONArray getOptimizationsAsJson(ComAdmin admin, LocalDate startDate, LocalDate endDate, DateTimeFormatter formatter) {
		JSONArray result = new JSONArray();

		if (startDate.isAfter(endDate)) {
		    return result;
        }

        ZoneId zoneId = AgnUtils.getZoneId(admin);
		Date start = DateUtilities.toDate(startDate, zoneId);
		Date end = DateUtilities.toDate(endDate.plusDays(1).atStartOfDay().minusNanos(1), zoneId);

		List<ComOptimization> optimizations = optimizationDao.getOptimizationsForCalendar_New(admin.getCompanyID(), start, end);

		for (ComOptimization optimization : optimizations) {
            JSONObject object = new JSONObject();

            object.element("id", optimization.getId());
            object.element("shortname", optimization.getShortname());
            object.element("campaignID", optimization.getCampaignID());
            object.element("workflowId", optimization.getWorkflowId());
            object.element("autoOptimizationStatus", optimization.getAutoOptimizationStatus());
            object.element("sendDate", DateUtilities.format(optimization.getSendDate(), zoneId, formatter));

            result.add(object);
        }

		return result;
	}

	@Override
	public List<SelectOption> getTestMailingList(ComOptimization optimization) {
		Map<Integer, String> groups = optimizationDao.getGroups(optimization.getCampaignID(), optimization.getCompanyID(), optimization.getId());

		if (MapUtils.isEmpty(groups)) {
			return Collections.emptyList();
		}

		List<SelectOption> options = new ArrayList<>(groups.size());
		groups.forEach((mailingId, shortname) -> options.add(new SelectOption(Integer.toString(mailingId), shortname)));
		return options;
	}

	// helper methods
	@Override
	public int getSplitNumbers(@VelocityCheck int companyID, String splitType) {
		return targetDao.getSplits(companyID, splitType);
	}

	@Override
	public int getState(ComOptimization optimization) {
		
		// The state STATUS_FINISHED is the only state which is directly written to the database
		ComOptimization optimizationFromDB = optimizationDao.get(optimization.getId(), optimization.getCompanyID());
				
		if (optimizationFromDB.getStatus() == ComOptimization.STATUS_FINISHED) {
			return ComOptimization.STATUS_FINISHED;
		}
		
		// ... all other optimization states depend on the states of the test mailings
		// assume the first test mailing represents the state of the others
		
		List<Integer> testMailingIDs = optimization.getTestmailingIDs();
		
		// the first testmailing ( group1 ) is the reference
		if (testMailingIDs.size() > 0) {

			int testMailingStatus = mailingDao.getLastGenstatus(testMailingIDs.get(0), optimization.isTestRun() ? MaildropStatus.TEST.getCode() : MaildropStatus.WORLD.getCode());

			//Method for selecting mailings throws no exception, but returns -1 as default, if no entry was found
			if (testMailingStatus == -1) {
				return STATUS_NOT_STARTED;
			}
			
			if (testMailingStatus == DeliveryStat.STATUS_NOT_SENT) {
				return STATUS_SCHEDULED;
			}
			
			if (testMailingStatus == DeliveryStat.STATUS_SENDING ||  testMailingStatus == DeliveryStat.STATUS_GENERATED || testMailingStatus == DeliveryStat.STATUS_GENERATING) {
				return STATUS_TEST_SEND;
			}
			
			if (testMailingStatus == DeliveryStat.STATUS_SENT && optimization.getResultMailingID() == 0) {
				return STATUS_EVAL_IN_PROGRESS;
			}
			
			if (testMailingStatus == DeliveryStat.STATUS_SENT && optimization.getResultMailingID() != 0) {
				return STATUS_FINISHED;
			}
		}
		
		return STATUS_NOT_STARTED;
		
	}

	@Override
	public int getFinalMailingID(int companyID, int workflowID, int oneOfTheSplitMailingID){
		return optimizationDao.getFinalMailingID(companyID, workflowID, oneOfTheSplitMailingID);
	}
	
	@Override
	public int getFinalMailingId(@VelocityCheck int companyId, int workflowId) {
		return optimizationDao.getFinalMailingId(companyId, workflowId);
	}
	
	@Override
	public AutoOptimizationLight getOptimizationLight(int companyId, int workflowId) {
		AutoOptimizationLight autoOptimizationLight = optimizationDao.getAutoOptimizationLight(companyId, workflowId);
		if (autoOptimizationLight == null) {
			autoOptimizationLight = new AutoOptimizationLight();
		}
		return autoOptimizationLight;
	}
	
	// make properties 'injectable'
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}

	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setOptimizationDao(ComOptimizationDao optimizationDao) {
		this.optimizationDao = optimizationDao;
	}

	@Required
	public void setOptimizationCommonService(ComOptimizationCommonService optimizationCommonService) {
		this.optimizationCommonService = optimizationCommonService;
	}

	@Required
	public void setOptimizationStatService(ComOptimizationStatService optimizationStatService) {
		this.optimizationStatService = optimizationStatService;
	}

	@Required
	public void setMailingParameterService(ComMailingParameterService mailingParameterService) {
		this.mailingParameterService = mailingParameterService;
	}

	@Required
	public void setCopyMailingService(CopyMailingService copyMailingService) {
		this.copyMailingService = copyMailingService;
	}
}
