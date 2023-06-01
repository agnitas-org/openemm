/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.target.ConditionalOperator;
import org.agnitas.util.SafeString;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.workflow.beans.ComWorkflowReaction;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowMailingAware;
import com.agnitas.emm.core.workflow.beans.WorkflowParameter;
import com.agnitas.emm.core.workflow.beans.WorkflowRecipient;
import com.agnitas.emm.core.workflow.beans.WorkflowStart;
import com.agnitas.emm.core.workflow.dao.ComWorkflowDao;
import com.agnitas.emm.core.workflow.graph.WorkflowGraph;
import com.agnitas.emm.core.workflow.graph.WorkflowNode;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;

public class ComWorkflowStatisticsService {

	private ComWorkflowDao workflowDao;
	private ComMailingDao mailingDao;
	private ComRecipientDao recipientDao;
	private ComWorkflowService workflowService;
	private ComWorkflowActivationService activationService;
	private ComTargetService targetService;
    private ComOptimizationService optimizationService;

	private Map<Integer, List<String>> resultMap;
	private Map<Integer, Integer> sentNumber;


	public Map<Integer, List<String>> getWorkflowStats(int workflowId, int companyId, Locale locale) {
		resultMap = new HashMap<>();

		WorkflowGraph workflowGraph = new WorkflowGraph(workflowService.getIcons(workflowId, companyId));

		List<WorkflowNode> startNodes = workflowGraph.getAllNodesByType(WorkflowIconType.START.getId());
		for (WorkflowNode startNode : startNodes) {
			WorkflowStart startIcon = (WorkflowStart) startNode.getNodeIcon();
			boolean allRecipientAffected = true;
			boolean needsActiveBinding = true;

			List<Integer> initialRecipients = new ArrayList<>();
			if (startIcon.getStartType() == WorkflowStart.WorkflowStartType.EVENT) {
				allRecipientAffected = false;
				switch (startIcon.getEvent()) {
					case EVENT_DATE:
						initialRecipients = getDateBasedMatchingRecipients(workflowId, companyId, startIcon);
						break;
					case EVENT_REACTION:
						ComWorkflowReaction reaction = workflowService.getWorkflowReaction(workflowId, companyId);
						if (reaction != null) {
							initialRecipients = workflowService.getReactedRecipients(reaction, false);
							needsActiveBinding = workflowService.checkReactionNeedsActiveBinding(reaction);
						}
						break;
					default:
						break;
				}
			}

			for (WorkflowNode nextNode : startNode.getNextNodes()) {
				processStatisticsForRecipients(companyId, nextNode, initialRecipients, allRecipientAffected, needsActiveBinding);
			}
		}

		// count stats for mailing icons (openers and clickers)
		generateMailingsStatistics(companyId, locale, workflowGraph, workflowId);

		// count stats for parameter icon
		generateParametersStatistics(companyId, workflowId, workflowGraph);

		return resultMap;
	}

	private List<Integer> getDateBasedMatchingRecipients(int workflowId, int companyId, WorkflowStart startIcon) {
		Workflow workflow = workflowDao.getWorkflow(workflowId, companyId);

		Calendar firstCalendar = GregorianCalendar.getInstance();
		firstCalendar.setTime(WorkflowUtils.getStartStopIconDate(startIcon));
		firstCalendar.add(Calendar.DATE, -4);

		Calendar lastCalendar = Calendar.getInstance();
		if (workflow.getGeneralEndDate() != null && lastCalendar.after(workflow.getGeneralEndDate())) {
			lastCalendar.setTime(workflow.getGeneralEndDate());
		}
		ArrayList<Date> allDates = new ArrayList<>();

		while (firstCalendar.before(lastCalendar)) {
			Date curDate = firstCalendar.getTime();
			allDates.add(curDate);
			firstCalendar.add(Calendar.DATE, 1);
		}

		String operator = "";
		for (ConditionalOperator targetOperator : ConditionalOperator.values()) {
			if (targetOperator.getOperatorCode() == startIcon.getDateFieldOperator()) {
				operator = targetOperator.getEqlSymbol();
			}
		}

		return recipientDao.getDateMatchingRecipients(companyId, allDates, startIcon.getDateProfileField(),
				operator, startIcon.getDateFormat());
	}

	private void generateParametersStatistics(int companyId, int workflowID, WorkflowGraph workflowGraph) {
		List<WorkflowNode> parameterNodes = workflowGraph.getAllNodesByType(WorkflowIconType.PARAMETER.getId());
		List<Integer> mailingTypes = Arrays.asList(WorkflowIconType.MAILING.getId(),
				WorkflowIconType.FOLLOWUP_MAILING.getId(), WorkflowIconType.DATE_BASED_MAILING.getId(),
				WorkflowIconType.ACTION_BASED_MAILING.getId());
		HashMap<Integer, Integer> paramRecipientMap = new HashMap<>();
		for (WorkflowNode parameterNode : parameterNodes) {
			WorkflowParameter parameterIcon = (WorkflowParameter) parameterNode.getNodeIcon();
			WorkflowMailingAware mailingIcon = (WorkflowMailingAware) workflowGraph.getNextIconByType(parameterIcon,
					mailingTypes, Collections.singletonList(WorkflowIconType.PARAMETER.getId()), false);
			if (mailingIcon != null) {
				int mailingId = WorkflowUtils.getMailingId(mailingIcon);
				int sent = 0;
                if(mailingId == 0 && isFinalMailing(mailingIcon)){
                    mailingId = getFinalMailingID(companyId, workflowID, mailingIcon, workflowGraph);
                }
				if (sentNumber.containsKey(mailingId)) {
					sent = sentNumber.get(mailingId);
				}
				resultMap.put(parameterIcon.getId(), Arrays.asList(String.valueOf(sent)));
				paramRecipientMap.put(parameterIcon.getId(), sent);
			}
		}
		for (WorkflowNode parameterNode : parameterNodes) {
			WorkflowParameter parameterIcon = (WorkflowParameter) parameterNode.getNodeIcon();
			if (!paramRecipientMap.containsKey(parameterIcon.getId())) {
				List<WorkflowIcon> nextParams = workflowGraph.getAllNextParallelIconsByType(parameterIcon, WorkflowIconType.PARAMETER.getId(), mailingTypes, false);
				int totalValue = 0;
				for (WorkflowIcon nextParam : nextParams) {
					if (paramRecipientMap.containsKey(nextParam.getId())) {
						totalValue += paramRecipientMap.get(nextParam.getId());
					}
				}
				resultMap.put(parameterIcon.getId(), Arrays.asList(String.valueOf(totalValue)));
			}
		}
	}

	private void generateMailingsStatistics(int companyId, Locale locale, WorkflowGraph workflowGraph, int workflowId) {
		List<WorkflowNode> mailingNodes = workflowGraph.getAllNodesByTypes(Arrays.asList(
				WorkflowIconType.MAILING.getId(), WorkflowIconType.FOLLOWUP_MAILING.getId(),
				WorkflowIconType.DATE_BASED_MAILING.getId(), WorkflowIconType.ACTION_BASED_MAILING.getId()));

		if (mailingNodes.isEmpty()) {
			return;
		}

		Set<Integer> mailingIds = new HashSet<>();
		HashMap<Integer, WorkflowMailingAware> mailingIcons = new HashMap<>();
		for (WorkflowNode mailingNode : mailingNodes) {
			WorkflowMailingAware mailingIcon = (WorkflowMailingAware) mailingNode.getNodeIcon();
			int mailingId = WorkflowUtils.getMailingId(mailingIcon);
			if (mailingId != 0) {
				mailingIds.add(mailingId);
				mailingIcons.put(mailingId, mailingIcon);
			} else if(isFinalMailing(mailingIcon)){
                //Additional check to be sure that we have final mailing
                for(WorkflowNode node : mailingNode.getPrevNodes()){
                    if(node.getNodeIcon().getType() == WorkflowIconType.PARAMETER.getId()){
                        int finalMailingID = getFinalMailingID(companyId, workflowId, node.getNodeIcon(), workflowGraph);
                        if(finalMailingID > 0){
                            mailingIds.add(finalMailingID);
                            mailingIcons.put(finalMailingID, mailingIcon);
                        }
                        break;
                    }
                }
            }
		}

		Map<Integer, Integer> clickers = mailingDao.getClickers(companyId, mailingIds, true);
		Map<Integer, Integer> openers = mailingDao.getOpeners(companyId, mailingIds, true);
		sentNumber = mailingDao.getSentNumber(companyId, mailingIds);

		NumberFormat formatter = new DecimalFormat("#0.0");
		for (Integer mailingId : mailingIds) {
			WorkflowMailingAware mailingIcon = mailingIcons.get(mailingId);

			double clickerRatio = sentNumber.get(mailingId) == 0 ? 0.0 : (((double)clickers.get(mailingId)) / ((double) sentNumber.get(mailingId)))*100;
			String clickerString = SafeString.getLocaleString("statistic.clicker", locale) + ": " +
					clickers.get(mailingId) + " (" + formatter.format(clickerRatio) + "%)";

			double openerRatio = sentNumber.get(mailingId) == 0 ? 0.0 : (((double)openers.get(mailingId)) / ((double) sentNumber.get(mailingId)))*100;
			String openerString = SafeString.getLocaleString("calendar.Openers", locale) + ": " + openers.get(mailingId) +
					" (" + formatter.format(openerRatio) + "%)";
			String sentMailings = SafeString.getLocaleString("statistic.mails.sent",locale)+": "+ sentNumber.get(mailingId);

			resultMap.put(mailingIcon.getId(), Arrays.asList(openerString, clickerString, sentMailings));
		}
	}

	private boolean isFinalMailing(WorkflowMailingAware mailingBase){
        return mailingBase.getType() != WorkflowIconType.FOLLOWUP_MAILING.getId()
                && mailingBase.getMailingId() == 0
                && !mailingBase.isEditable();
    }

    private int getFinalMailingID(int companyID, int workflowID, WorkflowIcon icon, WorkflowGraph workflowGraph){
        List<WorkflowIcon> mailings = workflowGraph.getAllPreviousIconsByType(icon, WorkflowIconType.MAILING.getId(), new ArrayList<>());
        int finalMailingID = 0;
        for(WorkflowIcon workflowIcon : mailings){
            WorkflowMailingAware mailingIcon = (WorkflowMailingAware) workflowIcon;
            if(mailingIcon.getMailingId() > 0){
                finalMailingID = optimizationService.getFinalMailingID(companyID, workflowID, mailingIcon.getMailingId());
                if(finalMailingID > 0){
                    return finalMailingID;
                }
            }
        }
        return finalMailingID;
    }
    
    public int getFinalMailingID(int workflowId, int companyId){
		if (isTotalStatisticAvailable(workflowId, companyId)) {
			return optimizationService.getFinalMailingId(companyId, workflowId);
		}

        return 0;
    }
    
	public boolean isTotalStatisticAvailable(int workflowId, int companyId) {
		Workflow workflow = workflowService.getWorkflow(workflowId, companyId);
		
		return workflow != null &&
				isTotalStatisticAvailable(workflow.getStatus(), workflow.getWorkflowIcons());
    }
    
    public boolean isTotalStatisticAvailable(Workflow.WorkflowStatus status, List<WorkflowIcon> icons) {
        return status == Workflow.WorkflowStatus.STATUS_COMPLETE &&
                WorkflowUtils.isAutoOptWorkflow(icons);
    }

	private void processStatisticsForRecipients(@VelocityCheck int companyId, WorkflowNode currentNode, List<Integer> currentRecipients, boolean allRecipientsAffected, boolean needsActiveBinding) {
		WorkflowIcon icon = currentNode.getNodeIcon();

		// handle recipient icons
		if (icon.getType() == WorkflowIconType.RECIPIENT.getId()) {
			WorkflowRecipient recipient = (WorkflowRecipient) icon;
			String targetExpression = activationService.createRecipientIconTargetExpression(recipient);
			String sqlExpression = targetService.getSQLFromTargetExpression(targetExpression, companyId);
			if (!allRecipientsAffected && !currentRecipients.isEmpty()) {
				currentRecipients = recipientDao.filterRecipientsByMailinglistAndTarget(currentRecipients,
						companyId, recipient.getMailinglistId(), sqlExpression, false, needsActiveBinding);
				resultMap.put(recipient.getId(), Collections.singletonList(Integer.toString(currentRecipients.size())));
			}
			else if (allRecipientsAffected) {
				currentRecipients = recipientDao.filterRecipientsByMailinglistAndTarget(null, companyId,
						recipient.getMailinglistId(), sqlExpression, true, needsActiveBinding);
				resultMap.put(recipient.getId(), Collections.singletonList(Integer.toString(currentRecipients.size())));
			}
			else {
				resultMap.put(recipient.getId(), Collections.singletonList("0"));
			}
			List<WorkflowNode> nextNodes = currentNode.getNextNodes();
			for (WorkflowNode nextNode : nextNodes) {
				processStatisticsForRecipients(companyId, nextNode, currentRecipients, allRecipientsAffected, needsActiveBinding);
			}
		}
		// if that is any other type, just continue iteration
		else {
			List<WorkflowNode> nextNodes = currentNode.getNextNodes();
			for (WorkflowNode nextNode : nextNodes) {
				processStatisticsForRecipients(companyId, nextNode, currentRecipients, allRecipientsAffected, needsActiveBinding);
			}
		}
	}

	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

	@Required
	public void setWorkflowService(ComWorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@Required
	public void setActivationService(ComWorkflowActivationService activationService) {
		this.activationService = activationService;
	}

	@Required
	public void setTargetService(ComTargetService targetService) {
		this.targetService = targetService;
	}

	@Required
	public void setWorkflowDao(ComWorkflowDao workflowDao) {
		this.workflowDao = workflowDao;
	}

	@Required
    public void setOptimizationService(ComOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }
}
