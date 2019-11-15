/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.CompositeKey;
import com.agnitas.beans.MailingSendOptions;
import com.agnitas.beans.MailingSendingProperties;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.workflow.beans.ComWorkflowReaction;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.WorkflowArchive;
import com.agnitas.emm.core.workflow.beans.WorkflowDeadline;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowDecisionCriteria;
import com.agnitas.emm.core.workflow.beans.WorkflowExport;
import com.agnitas.emm.core.workflow.beans.WorkflowFollowupMailing;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowImport;
import com.agnitas.emm.core.workflow.beans.WorkflowMailing;
import com.agnitas.emm.core.workflow.beans.WorkflowMailingAware;
import com.agnitas.emm.core.workflow.beans.WorkflowParameter;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionStepDeclaration;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.beans.WorkflowRecipient;
import com.agnitas.emm.core.workflow.beans.WorkflowReport;
import com.agnitas.emm.core.workflow.beans.WorkflowStart;
import com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartEventType;
import com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartType;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowReactionStepDeclarationImpl;
import com.agnitas.emm.core.workflow.dao.ComWorkflowReactionDao;
import com.agnitas.emm.core.workflow.dao.ComWorkflowReportScheduleDao;
import com.agnitas.emm.core.workflow.graph.WorkflowGraph;
import com.agnitas.emm.core.workflow.graph.WorkflowNode;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils.Deadline;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.beans.impl.ComOptimizationImpl;
import com.agnitas.mailing.autooptimization.service.ComOptimizationScheduleService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import com.agnitas.messages.Message;
import com.agnitas.service.ComMailingSendService;
import com.agnitas.service.ComMailingSendService.DeliveryType;
import org.agnitas.beans.Mailing;
import org.agnitas.dao.exception.target.TargetGroupPersistenceException;
import org.agnitas.emm.core.autoexport.service.AutoExportService;
import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.target.TargetFactory;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import static com.agnitas.emm.core.workflow.service.util.WorkflowUtils.WORKFLOW_TARGET_NAME_PATTERN;

public class ComWorkflowActivationService {

	private static final transient Logger logger = Logger.getLogger(ComWorkflowActivationService.class);
    public static final int DEFAULT_STEPPING = 60;

	public static final int TESTING_MODE_DEADLINE_DURATION = 15;  // minutes
	public static final Deadline TESTING_MODE_DEADLINE = new Deadline(TimeUnit.MINUTES.toMillis(TESTING_MODE_DEADLINE_DURATION));

	public static final List<Integer> ALL_MAILING_TYPES = Arrays.asList(
			WorkflowIconType.MAILING.getId(),
			WorkflowIconType.ACTION_BASED_MAILING.getId(),
			WorkflowIconType.DATE_BASED_MAILING.getId(),
			WorkflowIconType.FOLLOWUP_MAILING.getId()
	);
	private static final String HOUR_EXPRESSION = "1/24 * ";
	private static final String MINUTE_EXPRESSION = "1/(24 * 60) * ";

	private ComMailingDao mailingDao;
	private ComWorkflowService workflowService;
	private ComMailingSendService mailingSendService;
	private ComOptimizationService optimizationService;
	private ComOptimizationScheduleService optimizationScheduleService;
	private TargetFactory targetFactory;

	/** DAO accessing target groups. */
	private ComTargetDao targetDao;
	private ComWorkflowReactionDao reactionDao;
	private ComWorkflowReportScheduleDao reportScheduleDao;
    private AutoImportService autoImportService;
    private AutoExportService autoExportService;
	private ComWorkflowEQLHelper eqlHelper;
	private ComRecipientDao recipientDao;

	private int companyId;
	private int workflowId;
	private Set<WorkflowNode> processedNodes = new HashSet<>();
	private Map<Integer, Date> reportsSendDates = new HashMap<>();
	private Map<Integer, Date> optimizationsTestSendDates = new HashMap<>();
    private Map<Integer, Date> importsActivationDates = new HashMap<>();
    private Map<Integer, Date> exportsActivationDates = new HashMap<>();
	private Map<Integer, ComMailing> mailingsToUpdate = new HashMap<>();

	private WorkflowGraph workflowGraph;

    public void init(int companyId, int workflowId) {
		this.companyId = companyId;
		this.workflowId = workflowId;
		processedNodes = new HashSet<>();
		workflowGraph = null;
		mailingsToUpdate = new HashMap<>();
	}

	// @todo: maybe it will make sense to put this in a separate Thread (not to block the input) as it can take quite a long time
	public boolean activateWorkflow(int workflowId, ComAdmin admin, boolean testing, ActionMessages messages, ActionErrors errors, List<UserAction> userActions) throws Exception {
		init(admin.getCompanyID(), workflowId);

		final boolean isMailTrackingAvailable = AgnUtils.isMailTrackingAvailable(admin);
		Workflow workflow = workflowService.getWorkflow(workflowId, companyId);
		Date activationDate = new Date();
		TimeZone adminTimeZone = TimeZone.getTimeZone(admin.getAdminTimezone());

		ActionErrors activationErrors = new ActionErrors();

		// get icons and connections of current workflow
		List<WorkflowIcon> workflowIcons = workflow.getWorkflowIcons();
		Map<Integer, MailingSendingProperties> mailingSendingPropertiesMap = null;

		// create workflow graph
		workflowGraph = new WorkflowGraph(workflowIcons);

		if (!testing) {
			mailingSendingPropertiesMap = workflowIcons.stream()
				.filter(icon -> icon.getType() == WorkflowIconType.MAILING.getId())
				.map(icon -> (MailingSendingProperties) icon)
				.collect(Collectors.toMap(MailingSendingProperties::getMailingId, icon -> icon));
		}

		// handle followup mailings - set the appropriate base-mailing-id and decision criteria
		handleFollowupMailings(workflowGraph.getAllNodesByType(WorkflowIconType.FOLLOWUP_MAILING.getId()));

		// handle start icons
		List<WorkflowNode> startNodes = workflowGraph.getAllNodesByType(WorkflowIconType.START.getId());
		for (WorkflowNode startNode : startNodes) {
			final WorkflowStart startIcon = (WorkflowStart) startNode.getNodeIcon();

			Map<Integer, Date> mailingsSendDates = new HashMap<>();
			optimizationsTestSendDates.clear();
			reportsSendDates.clear();
			importsActivationDates.clear();
			exportsActivationDates.clear();

			Map<Integer, Integer> ruleStartMailingTargets = new HashMap<>();
			// Any start-icon has start-date so we need to process it to apply it to mailings in campaign (paying
			// attention to deadline icons which we can meet).
			processStartDate(mailingsSendDates, startIcon, activationDate, testing, adminTimeZone);

			if (startIcon.getStartType() == WorkflowStartType.EVENT) {
				switch (startIcon.getEvent()) {
					// The action-based campaign.
					case EVENT_REACTION:
						// create trigger for reaction that will send action-based mailing(s) when reaction takes place
						processActionBasedStart(startIcon, workflowId, activationDate, testing, adminTimeZone);
						break;

					// The rule-based (date-based) campaign.
					case EVENT_DATE:
						processRuleBasedStart(startIcon, ruleStartMailingTargets, testing);
						break;
				}
			}

			Map<Integer, List<WorkflowRecipient>> assignedRecipients = new HashMap<>();
			// Collect target groups and mailings lists from recipient icons and associate with mailings.
			processRecipientIcons(startIcon, assignedRecipients);
			
			assignedRecipients.forEach((mailingId, recipientIcons) -> {
				ComMailing mailing = getMailingToUpdate(mailingId);
				mailing.setMailinglistID(recipientIcons.get(0).getMailinglistId());
			});

			Map<Integer, Integer> assignedArchives = new HashMap<>();
			// Collect archives (campaigns) and associate with mailings.
			processArchiveIcons(startIcon, assignedArchives);
			
			assignedArchives.forEach((mailingId, archiveId) -> {
				ComMailing mailing = getMailingToUpdate(mailingId);
				mailing.setCampaignID(archiveId);
			});

			Map<Integer, ConditionGroup> workflowConditions = new HashMap<>();
			// Collect workflow target groups (sequential mailings dependencies, decisions) and associate with mailings.
			// Sequential mailings dependencies: ensures that every mailing wont be sent until previous mailings are sent (guarantees that
			// a mailings order is kept and a new subscriber wont receive intermediate mailing).
			processMailingTargets(startIcon, workflowConditions, isMailTrackingAvailable);
			

			// Attention: lazy calculation
			Map<WorkflowRecipient, Set<Integer>> immediateRecipientsToMailings = new HashMap<>();

			if (isMailTrackingAvailable) {
				// For the following chain:
				//		Recipient -> Mailing_A -> Deadline -> Mailing_B
				// target expression of Mailing_B should not contain target groups from Recipient

				workflowGraph.getAllNodesByType(WorkflowIconType.RECIPIENT.getId())
						.stream()
						.map(node -> (WorkflowRecipient) node.getNodeIcon())
						.forEach(recipient -> {
							Set<Integer> mailingIds = workflowGraph.getAllNextParallelIconsByType(recipient, ALL_MAILING_TYPES, new ArrayList<>(), false)
									.stream()
									.map(WorkflowUtils::getMailingId)
									.collect(Collectors.toSet());
							immediateRecipientsToMailings.put(recipient, mailingIds);
						});
			}

			// Generate and assign target expressions for mailings
			for (Entry<Integer, List<WorkflowRecipient>> entry : assignedRecipients.entrySet()) {
				Integer mailingId = entry.getKey();
				ConditionGroup conditions = new ConditionGroup(true);

				// handle recipient icons
				List<WorkflowRecipient> recipientIcons = entry.getValue();
				if (CollectionUtils.isNotEmpty(recipientIcons)) {
					// Assign target groups from a recipient icon to the following mailings

					recipientIcons.stream()
							.filter(recipient -> {
								if (CollectionUtils.isEmpty(recipient.getTargets())) {
									return false;
								}

								// When mailtracking is available then each mailing (except the very first one) should simply
								// use mailtracking data of the previous mailing and ignore target groups supplied by recipient icon
								return !isMailTrackingAvailable || immediateRecipientsToMailings.get(recipient).contains(mailingId);
							})
							.map(this::createConditionGroupFromRecipientIcon)
							.forEach(conditions::add);
				}

				// handle target created for date based start
				Integer dateStartTargetId = ruleStartMailingTargets.get(mailingId);
				if (dateStartTargetId != null) {
					conditions.add(new TargetCondition(dateStartTargetId));
				}

				Condition mailingTargetCondition = workflowConditions.get(mailingId);
				if (mailingTargetCondition != null) {
					mailingTargetCondition = mailingTargetCondition.reduce();
					if (mailingTargetCondition != null) {
						conditions.add(mailingTargetCondition);
					}
				}

				Mailing mailing = getMailingToUpdate(mailingId);
				mailing.setTargetExpression(Condition.toReducedTargetExpression(conditions));
			}

			Map<Integer, List<WorkflowParameter>> assignedParameters = new LinkedHashMap<>();
			Map<WorkflowDecision, List<Integer>> optimizationMailings = new HashMap<>();
			// Collect list-split parameters and auto-optimizations and associate with mailings and decisions.
			processParameters(startIcon, assignedParameters, optimizationMailings);

			// process mailings to be handled by auto-optimization
			List<ComOptimization> optimizations = processAutoOptimizations(workflow, optimizationMailings, assignedParameters, testing);

			// process mailings to assign needed list-split (for mailings not handled by auto-optimizations)
			for (List<Integer> value : optimizationMailings.values()) {
				// exclude mailings that are handled by auto-optimization
				for (Integer mailingId : value) {
					assignedParameters.put(mailingId, Collections.emptyList());
					mailingsSendDates.remove(mailingId);
				}
			}

			// Process mailings using list splits without auto-optimizations
			applyListSplitsToMailings(assignedParameters, true);
			
			mailingsSendDates.forEach((mailingId, sendDate) -> {
				ComMailing mailing = getMailingToUpdate(mailingId);
				mailing.setPlanDate(sendDate);
			});

			// Notice that updated mailings have to be saved before auto-optimizations/mailings scheduled
			saveUpdatedMailings();

			for (ComOptimization optimization : optimizations) {
				try {
					optimizationScheduleService.scheduleOptimization(optimization, mailingSendingPropertiesMap);
				} catch (Exception e) {
					// @todo: AO wasn't successfully scheduled, we need to inform the user and handle this situation
					logger.error("Failed to schedule auto-optimization: " + e.getMessage(), e);
				}
			}

			// send/schedule mailings and reports
			sendMailings(mailingsSendDates, admin.getAdminID(), testing, messages, activationErrors, userActions);
			sendReports();

            updateAutoImportActivationDate(companyId);
            updateAutoExportActivationDate(companyId);
		}

		errors.add(activationErrors);

		return activationErrors.isEmpty();
	}

	public boolean activateWorkflow(int workflowId, ComAdmin admin, boolean testing, List<Message> warnings, List<Message> messages, List<UserAction> userActions) throws Exception {
		init(admin.getCompanyID(), workflowId);

		final boolean isMailTrackingAvailable = AgnUtils.isMailTrackingAvailable(admin);
		Workflow workflow = workflowService.getWorkflow(workflowId, companyId);
		Date activationDate = new Date();
		TimeZone adminTimeZone = TimeZone.getTimeZone(admin.getAdminTimezone());

		List<Message> messageList = new ArrayList<>();

		// get icons and connections of current workflow
		List<WorkflowIcon> workflowIcons = workflow.getWorkflowIcons();
		Map<Integer, MailingSendingProperties> mailingSendingPropertiesMap = null;

		// create workflow graph
		workflowGraph = new WorkflowGraph(workflowIcons);

		if (!testing) {
			mailingSendingPropertiesMap = workflowIcons.stream()
					.filter(icon -> icon.getType() == WorkflowIconType.MAILING.getId())
					.map(icon -> (MailingSendingProperties) icon)
					.collect(Collectors.toMap(MailingSendingProperties::getMailingId, icon -> icon));
		}

		// handle followup mailings - set the appropriate base-mailing-id and decision criteria
		handleFollowupMailings(workflowGraph.getAllNodesByType(WorkflowIconType.FOLLOWUP_MAILING.getId()));

		// handle start icons
		List<WorkflowNode> startNodes = workflowGraph.getAllNodesByType(WorkflowIconType.START.getId());
		for (WorkflowNode startNode : startNodes) {
			final WorkflowStart startIcon = (WorkflowStart) startNode.getNodeIcon();

			Map<Integer, Date> mailingsSendDates = new HashMap<>();
			optimizationsTestSendDates.clear();
			reportsSendDates.clear();
			importsActivationDates.clear();
			exportsActivationDates.clear();

			Map<Integer, Integer> ruleStartMailingTargets = new HashMap<>();
			// Any start-icon has start-date so we need to process it to apply it to mailings in campaign (paying
			// attention to deadline icons which we can meet).
			processStartDate(mailingsSendDates, startIcon, activationDate, testing, adminTimeZone);

			if (startIcon.getStartType() == WorkflowStartType.EVENT) {
				switch (startIcon.getEvent()) {
					// The action-based campaign.
					case EVENT_REACTION:
						// create trigger for reaction that will send action-based mailing(s) when reaction takes place
						processActionBasedStart(startIcon, workflowId, activationDate, testing, adminTimeZone);
						break;

					// The rule-based (date-based) campaign.
					case EVENT_DATE:
						processRuleBasedStart(startIcon, ruleStartMailingTargets, testing);
						break;
				}
			}

			Map<Integer, List<WorkflowRecipient>> assignedRecipients = new HashMap<>();
			// Collect target groups and mailings lists from recipient icons and associate with mailings.
			processRecipientIcons(startIcon, assignedRecipients);

			assignedRecipients.forEach((mailingId, recipientIcons) -> {
				ComMailing mailing = getMailingToUpdate(mailingId);
				mailing.setMailinglistID(recipientIcons.get(0).getMailinglistId());
			});

			Map<Integer, Integer> assignedArchives = new HashMap<>();
			// Collect archives (campaigns) and associate with mailings.
			processArchiveIcons(startIcon, assignedArchives);

			assignedArchives.forEach((mailingId, archiveId) -> {
				ComMailing mailing = getMailingToUpdate(mailingId);
				mailing.setCampaignID(archiveId);
			});

			Map<Integer, ConditionGroup> workflowConditions = new HashMap<>();
			// Collect workflow target groups (sequential mailings dependencies, decisions) and associate with mailings.
			// Sequential mailings dependencies: ensures that every mailing wont be sent until previous mailings are sent (guarantees that
			// a mailings order is kept and a new subscriber wont receive intermediate mailing).
			processMailingTargets(startIcon, workflowConditions, isMailTrackingAvailable);


			// Attention: lazy calculation
			Map<WorkflowRecipient, Set<Integer>> immediateRecipientsToMailings = new HashMap<>();

			if (isMailTrackingAvailable) {
				// For the following chain:
				//		Recipient -> Mailing_A -> Deadline -> Mailing_B
				// target expression of Mailing_B should not contain target groups from Recipient

				workflowGraph.getAllNodesByType(WorkflowIconType.RECIPIENT.getId())
						.stream()
						.map(node -> (WorkflowRecipient) node.getNodeIcon())
						.forEach(recipient -> {
							Set<Integer> mailingIds = workflowGraph.getAllNextParallelIconsByType(recipient, ALL_MAILING_TYPES, new ArrayList<>(), false)
									.stream()
									.map(WorkflowUtils::getMailingId)
									.collect(Collectors.toSet());
							immediateRecipientsToMailings.put(recipient, mailingIds);
						});
			}

			// Generate and assign target expressions for mailings
			for (Entry<Integer, List<WorkflowRecipient>> entry : assignedRecipients.entrySet()) {
				Integer mailingId = entry.getKey();
				ConditionGroup conditions = new ConditionGroup(true);

				// handle recipient icons
				List<WorkflowRecipient> recipientIcons = entry.getValue();
				if (CollectionUtils.isNotEmpty(recipientIcons)) {
					// Assign target groups from a recipient icon to the following mailings

					recipientIcons.stream()
							.filter(recipient -> {
								if (CollectionUtils.isEmpty(recipient.getTargets())) {
									return false;
								}

								// When mailtracking is available then each mailing (except the very first one) should simply
								// use mailtracking data of the previous mailing and ignore target groups supplied by recipient icon
								return !isMailTrackingAvailable || immediateRecipientsToMailings.get(recipient).contains(mailingId);
							})
							.map(this::createConditionGroupFromRecipientIcon)
							.forEach(conditions::add);
				}

				// handle target created for date based start
				Integer dateStartTargetId = ruleStartMailingTargets.get(mailingId);
				if (dateStartTargetId != null) {
					conditions.add(new TargetCondition(dateStartTargetId));
				}

				Condition mailingTargetCondition = workflowConditions.get(mailingId);
				if (mailingTargetCondition != null) {
					mailingTargetCondition = mailingTargetCondition.reduce();
					if (mailingTargetCondition != null) {
						conditions.add(mailingTargetCondition);
					}
				}

				Mailing mailing = getMailingToUpdate(mailingId);
				mailing.setTargetExpression(Condition.toReducedTargetExpression(conditions));
			}

			Map<Integer, List<WorkflowParameter>> assignedParameters = new LinkedHashMap<>();
			Map<WorkflowDecision, List<Integer>> optimizationMailings = new HashMap<>();
			// Collect list-split parameters and auto-optimizations and associate with mailings and decisions.
			processParameters(startIcon, assignedParameters, optimizationMailings);

			// process mailings to be handled by auto-optimization
			List<ComOptimization> optimizations = processAutoOptimizations(workflow, optimizationMailings, assignedParameters, testing);

			// process mailings to assign needed list-split (for mailings not handled by auto-optimizations)
			for (List<Integer> value : optimizationMailings.values()) {
				// exclude mailings that are handled by auto-optimization
				for (Integer mailingId : value) {
					assignedParameters.put(mailingId, Collections.emptyList());
					mailingsSendDates.remove(mailingId);
				}
			}

			// Process mailings using list splits without auto-optimizations
			applyListSplitsToMailings(assignedParameters, true);

			mailingsSendDates.forEach((mailingId, sendDate) -> {
				ComMailing mailing = getMailingToUpdate(mailingId);
				mailing.setPlanDate(sendDate);
			});

			// Notice that updated mailings have to be saved before auto-optimizations/mailings scheduled
			saveUpdatedMailings();

			for (ComOptimization optimization : optimizations) {
				try {
					optimizationScheduleService.scheduleOptimization(optimization, mailingSendingPropertiesMap);
				} catch (Exception e) {
					// @todo: AO wasn't successfully scheduled, we need to inform the user and handle this situation
					logger.error("Failed to schedule auto-optimization: " + e.getMessage(), e);
				}
			}

			// send/schedule mailings and reports
			sendMailings(mailingsSendDates, admin.getAdminID(), testing, messageList, warnings, userActions);
			sendReports();

			updateAutoImportActivationDate(companyId);
			updateAutoExportActivationDate(companyId);
		}

		messages.addAll(messageList);

		return messageList.isEmpty();
	}

	private int createTargetWithDeadlineRespect(WorkflowStart start, List<WorkflowIcon> deadlineIcons) {
        List<WorkflowDeadline> deadlines = deadlineIcons.stream().map(icon -> ((WorkflowDeadline)icon)).collect(Collectors.toList());
        String eql = eqlHelper.generateDateEQL(companyId, start.getDateProfileField(),
				start.getDateFieldOperator(), start.getDateFormat(),
				start.getDateFieldValue() + getDeadlineExpression(deadlines));
        ComTarget target = createTarget(eql, String.format(WORKFLOW_TARGET_NAME_PATTERN, "start by event date"));
        return target.getId();
    }

    private String getDeadlineExpression(List<WorkflowDeadline> deadlines) {
		List<String> expressions = new ArrayList<>();

		for (WorkflowDeadline deadline : deadlines) {
			int delayValue = deadline.getDelayValue();
			if (deadline.getTimeUnit() == WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_DAY) {
				expressions.add(Integer.toString(delayValue));
				if (deadline.isUseTime()) {
					expressions.add(HOUR_EXPRESSION + deadline.getHour());
					expressions.add(MINUTE_EXPRESSION + deadline.getMinute());
				}
			} else if (deadline.getTimeUnit() == WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_HOUR) {
				expressions.add(HOUR_EXPRESSION + delayValue);
			} else if (deadline.getTimeUnit() == WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_MINUTE) {
				expressions.add(MINUTE_EXPRESSION + delayValue);
			}
		}

		if (CollectionUtils.isNotEmpty(expressions)) {
			return "-" + String.join("-", expressions);
		}

		return StringUtils.EMPTY;
	}

	private ComMailing getMailingToUpdate(int mailingId) {
    	return mailingsToUpdate.computeIfAbsent(mailingId, mId -> (ComMailing) mailingDao.getMailing(mId, companyId));
	}

	private void saveUpdatedMailings() {
		for (ComMailing mailing : mailingsToUpdate.values()) {
			mailingDao.saveMailing(mailing, false);
		}
		mailingsToUpdate.clear();
	}

	private void processArchiveIcons(WorkflowStart startIcon, Map<Integer, Integer> assignedArchives) {
		workflowGraph.getAllNextIconsByType(startIcon, WorkflowIconType.ARCHIVE.getId(), Collections.emptySet())
				.stream()
				.map(icon -> (WorkflowArchive) icon)
				.forEach(archiveIcon -> {
					int archiveId = archiveIcon.getCampaignId();
					workflowGraph.getAllNextParallelIconsByType(archiveIcon, ALL_MAILING_TYPES, Collections.emptySet(), false)
							.stream()
							.map(WorkflowUtils::getMailingId)
							.filter(mailingId -> mailingId > 0)
							.forEach(mailingId -> assignedArchives.put(mailingId, archiveId));
				});
	}

	private void sendReports() {
		for (Entry<Integer, Date> entry : reportsSendDates.entrySet()) {
			WorkflowReport reportIcon = (WorkflowReport) workflowGraph.getNodeByIconId(entry.getKey()).getNodeIcon();
			List<Integer> reports = reportIcon.getReports();
			for (Integer reportId : reports) {
				reportScheduleDao.scheduleWorkflowReport(reportId, companyId, workflowId, entry.getValue());
			}
		}
	}

	private void sendMailings(Map<Integer, Date> mailingsSendDates, int adminId, boolean testing, ActionMessages messages, ActionErrors errors, List<UserAction> userActions) {
    	Map<Integer, WorkflowMailingAware> mailingIconsMap = getMailingIconsMap();

		for (Entry<Integer, Date> entry : mailingsSendDates.entrySet()) {
			int mailingId = entry.getKey();
			Date sendDate = entry.getValue();

			WorkflowMailingAware icon = mailingIconsMap.get(mailingId);

			if (icon == null) {
				logger.error("Missing required mailing icon for mailing #" + mailingId);
				continue;
			}

			WorkflowIconType iconType = WorkflowIconType.fromId(icon.getType());
			DeliveryType deliveryType;

			if (iconType == WorkflowIconType.ACTION_BASED_MAILING) {
				// Always use WORLD delivery type for action-based mailing (let the workflow manage its delivery).
				deliveryType = DeliveryType.WORLD;
			} else {
				// Other mailing types could use TEST delivery type on workflow test run.
				deliveryType = testing ? DeliveryType.TEST : DeliveryType.WORLD;
			}

			if (iconType == WorkflowIconType.MAILING || iconType == WorkflowIconType.FOLLOWUP_MAILING) {
				WorkflowMailing mailing = (WorkflowMailing) icon;

				// Send normal or follow-up mailing.
				MailingSendOptions options = MailingSendOptions.builder()
						.setDate(sendDate)
						.setAdminId(adminId)
						.setMaxRecipients(mailing.getMaxRecipients())
						.setBlockSize(mailing.getBlocksize())
						.setDefaultStepping(DEFAULT_STEPPING)
						.setFollowupFor(getBaseMailingId(icon))
						.setDoubleChecking(mailing.isDoubleCheck())
						.setSkipEmpty(mailing.isSkipEmptyBlocks())
						.setReportSendDayOffset(mailing.getAutoReport())
						.setGenerateAtSendDate(true)
						.setDeliveryType(deliveryType)
						.build();

				mailingSendService.sendMailing(mailingId, companyId, options, messages, errors, userActions);
			} else {
				// Send action-based or date-based mailing.
				MailingSendOptions options = MailingSendOptions.builder()
						.setDate(sendDate)
						.setAdminId(adminId)
						.setGenerateAtSendDate(true)
						.setDeliveryType(deliveryType)
						.build();

				mailingSendService.sendMailing(mailingId, companyId, options, messages, errors, userActions);
				// Use "test" mailing status on workflow test run.
				mailingDao.updateStatus(mailingId, testing ? "test" : "active");
			}
		}
	}

	private void sendMailings(Map<Integer, Date> mailingsSendDates, int adminId, boolean testing, List<Message> warnings, List<Message> messages, List<UserAction> userActions) {
		Map<Integer, WorkflowMailingAware> mailingIconsMap = getMailingIconsMap();

		for (Entry<Integer, Date> entry : mailingsSendDates.entrySet()) {
			int mailingId = entry.getKey();
			Date sendDate = entry.getValue();

			WorkflowMailingAware icon = mailingIconsMap.get(mailingId);

			if (icon == null) {
				logger.error("Missing required mailing icon for mailing #" + mailingId);
				continue;
			}

			WorkflowIconType iconType = WorkflowIconType.fromId(icon.getType());
			DeliveryType deliveryType;

			if (iconType == WorkflowIconType.ACTION_BASED_MAILING) {
				// Always use WORLD delivery type for action-based mailing (let the workflow manage its delivery).
				deliveryType = DeliveryType.WORLD;
			} else {
				// Other mailing types could use TEST delivery type on workflow test run.
				deliveryType = testing ? DeliveryType.TEST : DeliveryType.WORLD;
			}

			if (iconType == WorkflowIconType.MAILING || iconType == WorkflowIconType.FOLLOWUP_MAILING) {
				WorkflowMailing mailing = (WorkflowMailing) icon;

				// Send normal or follow-up mailing.
				MailingSendOptions options = MailingSendOptions.builder()
						.setDate(sendDate)
						.setAdminId(adminId)
						.setMaxRecipients(mailing.getMaxRecipients())
						.setBlockSize(mailing.getBlocksize())
						.setDefaultStepping(DEFAULT_STEPPING)
						.setFollowupFor(getBaseMailingId(icon))
						.setDoubleChecking(mailing.isDoubleCheck())
						.setSkipEmpty(mailing.isSkipEmptyBlocks())
						.setReportSendDayOffset(mailing.getAutoReport())
						.setGenerateAtSendDate(true)
						.setDeliveryType(deliveryType)
						.build();

				mailingSendService.sendMailing(mailingId, companyId, options, warnings, messages, userActions);
			} else {
				// Send action-based or date-based mailing.
				MailingSendOptions options = MailingSendOptions.builder()
						.setDate(sendDate)
						.setAdminId(adminId)
						.setGenerateAtSendDate(true)
						.setDeliveryType(deliveryType)
						.build();

				mailingSendService.sendMailing(mailingId, companyId, options, warnings, messages, userActions);
				// Use "test" mailing status on workflow test run.
				mailingDao.updateStatus(mailingId, testing ? "test" : "active");
			}
		}
	}

	// Collect all the mailings used in campaign and create map (mailingId -> mailingIcon).
	private Map<Integer, WorkflowMailingAware> getMailingIconsMap() {
		Map<Integer, WorkflowMailingAware> map = new HashMap<>();

		for (WorkflowNode node : workflowGraph.getAllNodesByTypes(ALL_MAILING_TYPES)) {
			WorkflowMailingAware icon = (WorkflowMailingAware) node.getNodeIcon();

			if (icon.getMailingId() > 0) {
				map.put(icon.getMailingId(), icon);
			}
		}

		return map;
	}

	private int getBaseMailingId(WorkflowMailingAware icon) {
    	if (WorkflowIconType.FOLLOWUP_MAILING.getId() == icon.getType()) {
    		return ((WorkflowFollowupMailing) icon).getBaseMailingId();
		} else {
    		return 0;
		}
	}

	private List<ComOptimization> processAutoOptimizations(Workflow workflow,
										  Map<WorkflowDecision, List<Integer>> optimizationMailings,
										  Map<Integer, List<WorkflowParameter>> mailingsParameters,
										  boolean testRun) {
		List<ComOptimization> entitiesToSchedule = new ArrayList<>();
		List<ComOptimization> entitiesToReuse = optimizationService.listWorkflowManaged(workflow.getWorkflowId(), companyId);
		Iterator<ComOptimization> entitiesIterator = entitiesToReuse.iterator();

		for (Entry<WorkflowDecision, List<Integer>> entry : optimizationMailings.entrySet()) {
			WorkflowDecision decision = entry.getKey();
			WorkflowArchive archive = (WorkflowArchive) workflowGraph.getNextIconByType(decision, WorkflowIconType.ARCHIVE.getId(),
					new HashSet<>(), true);
			WorkflowRecipient recipient = (WorkflowRecipient) workflowGraph.getNextIconByType(decision,
					WorkflowIconType.RECIPIENT.getId(), new HashSet<>(), true);

			if (archive != null && recipient != null) {
				ComOptimization optimization = null;
				if (entitiesIterator.hasNext()) {
					optimization = entitiesIterator.next();
				}

				optimization = createOptimization(
						optimization,
						workflow,
						archive.getCampaignId(),
						decision,
						recipient,
						entry.getValue(),
						optimizationsTestSendDates.get(decision.getId()),
						mailingsParameters,
						testRun
				);

				entitiesToSchedule.add(optimization);
			}
		}
		return entitiesToSchedule;
	}

	private ComOptimization createOptimization(ComOptimization optimization, Workflow workflow, int campaignId,
											   WorkflowDecision decisionIcon, WorkflowRecipient recipientIcon,
											   List<Integer> testMailings, Date testSendDate,
											   Map<Integer, List<WorkflowParameter>> mailingsParameters, boolean testRun) {
		if (optimization == null) {
			optimization = new ComOptimizationImpl();
		} else {
			// Take care about reused entity
			optimization.setGroup1(0);
			optimization.setGroup2(0);
			optimization.setGroup3(0);
			optimization.setGroup4(0);
			optimization.setGroup5(0);

			optimization.setResultMailingID(0);
			optimization.setFinalMailingId(0);
		}

		optimization.setCampaignID(campaignId);
		optimization.setCompanyID(companyId);
		optimization.setWorkflowId(workflow.getWorkflowId());
		optimization.setShortname("[campaign: " + workflow.getShortname() + "]");
		optimization.setDescription("");
		optimization.setStatus(ComOptimization.STATUS_NOT_STARTED);

		optimization.setEvalType(decisionIcon.getAoDecisionCriteria());
		try {
			optimization.setThreshold(Integer.parseInt(decisionIcon.getThreshold()));
		} catch (NumberFormatException e) {
			optimization.setThreshold(0);
		}
		optimization.setSendDate(decisionIcon.getDecisionDate());
		optimization.setTestMailingsSendDate(testSendDate);
		// @todo: we don't have setting in decision-icon for that, so we just set that to false?
		optimization.setDoubleCheckingActivated(false);
		optimization.setTestRun(testRun);

        Map<Integer, List<WorkflowParameter>> optimizationParameters = new HashMap<>();
		for (Integer mailingId : testMailings) {
			optimizationParameters.put(mailingId, mailingsParameters.get(mailingId));
		}
		List<WorkflowParameter> workflowParameterList = new ArrayList<>();
		// create targets for params and set splitType for optimization
		WorkflowParameter finalParameter = (WorkflowParameter) workflowGraph.getNextIconByType(
				decisionIcon, WorkflowIconType.PARAMETER.getId(), new HashSet<>(), false
		);
		workflowParameterList.add(finalParameter);
		optimizationParameters.put(0, workflowParameterList);

		LinkedHashMap<Integer, Integer> splitTargets = applyListSplitsToMailings(optimizationParameters, false);

		String splitBase = "";
		if (!splitTargets.isEmpty()) {
			Collection<Integer> targetIds = splitTargets.values();
			String targetName = targetDao.getTargetSplitName(targetIds.iterator().next());
			splitBase = targetName.substring(TargetLight.LIST_SPLIT_CM_PREFIX.length(), targetName.lastIndexOf("_"));
		}
		optimization.setSplitType(splitBase);

		// set target expression and mailinglist id
		optimization.setMailinglistID(recipientIcon.getMailinglistId());
		if (CollectionUtils.isNotEmpty(recipientIcon.getTargets())) {
			optimization.setTargetExpression(StringUtils.join(recipientIcon.getTargets(), ","));
		} else {
			optimization.setTargetExpression("");
		}

        // todo: auto-optimization doesn't seem to support "Subscriber isn't allowed to be in target-groups" target mode
        if (recipientIcon.getTargetsOption() == WorkflowRecipient.WorkflowTargetOption.ALL_TARGETS_REQUIRED) {
            optimization.setTargetMode(Mailing.TARGET_MODE_AND);
        } else {
            optimization.setTargetMode(Mailing.TARGET_MODE_OR);
        }

		// set test mailings
		int order = 1;
		// The groups' order should match the order of list split targets
		for (Integer mailingId : splitTargets.keySet()) {
			if (mailingId <= 0) {
				continue;
			}

			switch (order) {
				case 1: optimization.setGroup1(mailingId); break;
				case 2: optimization.setGroup2(mailingId); break;
				case 3: optimization.setGroup3(mailingId); break;
				case 4: optimization.setGroup4(mailingId); break;
				case 5: optimization.setGroup5(mailingId); break;
				default:break;
			}
			order++;
		}

		optimizationService.save(optimization);
		return optimization;
	}

	private LinkedHashMap<Integer, Integer> applyListSplitsToMailings(Map<Integer, List<WorkflowParameter>> mailingsParameters, boolean assignToMailings) {
		Map<Integer, Double> mailingsParts = new HashMap<>();
		Set<Integer> mailingsWithoutSplitting = new HashSet<>();

		mailingsParameters.forEach((mailingId, parameters) -> {
			if (CollectionUtils.isNotEmpty(parameters)) {
				mailingsParts.put(mailingId, combineListSplitParams(parameters));
			} else {
				mailingsWithoutSplitting.add(mailingId);
			}
		});

		LinkedHashMap<Integer, Double> orderedMailingsParts = new LinkedHashMap<>();

		// Parameters have to be sorted in ascending order (but final mailing's part is always the last one)
		mailingsParts.entrySet()
				.stream()
				.sorted((p1, p2) -> {
					if (p1.getKey() == 0) {
						return 1;
					}
					if (p2.getKey() == 0) {
						return -1;
					}
					return Double.compare(p1.getValue(), p2.getValue());
				})
				.forEach(p -> orderedMailingsParts.put(p.getKey(), p.getValue()));

		LinkedHashMap<Integer, Integer> splitTargets = createListSplitTargets(orderedMailingsParts);

		if (assignToMailings) {
			splitTargets.forEach((mailingId, splitId) -> {
				if (mailingId > 0) {
					ComMailing mailing = getMailingToUpdate(mailingId);
					mailing.setSplitID(splitId);
				}
			});

			for (int mailingId : mailingsWithoutSplitting) {
				// No splitting required - reset splitId.
				ComMailing mailing = getMailingToUpdate(mailingId);
				mailing.setSplitID(0);
			}
		}

		return splitTargets;
	}

	private double combineListSplitParams(List<WorkflowParameter> parameters) {
		double value = 100.0;
		for (WorkflowParameter parameter : parameters) {
			value = (value * (parameter.getValue())) / 100.0;
		}
		return value;
	}

	private void handleFollowupMailings(List<WorkflowNode> followupMailings) throws Exception {
		for (WorkflowNode followupMailingNode : followupMailings) {
			WorkflowFollowupMailing followupMailingIcon = (WorkflowFollowupMailing) followupMailingNode.getNodeIcon();
			Mailing followupMailing = getMailingToUpdate(followupMailingIcon.getMailingId());
			String followUpMethod = WorkflowUtils.getFollowUpMethod(followupMailingIcon.getDecisionCriterion());
			if (followUpMethod != null) {
				int baseMailing = followupMailingIcon.getBaseMailingId();
				if (followupMailing.getId() == baseMailing) {
					throw new Exception("Cannot create cyclic followup mailing structure");
				}
				MediatypeEmail emailParam = followupMailing.getEmailParam();
				emailParam.setFollowUpMethod(followUpMethod);
				emailParam.setFollowupFor(String.valueOf(baseMailing));
			}
			followupMailing.setMailingType(Mailing.TYPE_FOLLOWUP);
		}
	}

	private LinkedHashMap<Integer, Integer> createListSplitTargets(LinkedHashMap<Integer, Double> parts) {
		// mailingId -> splitId
		LinkedHashMap<Integer, Integer> targets = new LinkedHashMap<>();
		if (parts == null || parts.isEmpty()) {
			return targets;
		}

		List<Double> doubleParts = new ArrayList<>();
		parts.forEach((mailingId, doublePart) -> doubleParts.add(doublePart));
		String splitType = StringUtils.join(doubleParts, ";");

		Double granularity;
		if (doubleParts.size() > 1) {
			granularity = WorkflowUtils.calculateGCD(doubleParts);
		} else {
			Double value = doubleParts.get(0);
			Double rest = 100.0 - value;
			granularity = WorkflowUtils.calculateGCD(Arrays.asList(value, rest));
		}

		if (granularity == null) {
			// Default - 5%
			granularity = 5.0;
		}
		int base = (int) Math.round(100.0 / granularity);

		Iterator<Map.Entry<Integer, Double>> iterator = parts.entrySet().iterator();

		int partIndex = 1;
		int previousBound = 0;
		while (iterator.hasNext()) {
			Map.Entry<Integer, Double> p = iterator.next();

			int mailingId = p.getKey();
			int part = (int) Math.round(base * p.getValue() / 100.0);
			int nextBound = previousBound + part;

			String targetEql;
			if (iterator.hasNext()) {
				targetEql = "customer_id % " + base + " >= " + previousBound + " AND customer_id % " + base + " < " + nextBound;
			} else {
				if (previousBound > 0) {
					// Take the rest - in order to avoid customers missing.
					targetEql = "customer_id % " + base + " >= " + previousBound;
				} else {
					// Take the part up to X% if the split list consists of a single part.
					targetEql = "customer_id % " + base + " < " + nextBound;
				}
			}

			// if there's already target with that name - no need to create target again
			// as the existing one has the needed SQL expression
			ComTarget target = targetDao.getListSplitTarget(splitType, partIndex, companyId);
			if (target == null || !target.isValid() || StringUtils.isBlank(target.getEQL())) {
				target = createTarget(targetEql, TargetLight.LIST_SPLIT_CM_PREFIX + splitType + "_" + partIndex, false);
			}
			targets.put(mailingId, target.getId());

			previousBound = nextBound;
			partIndex++;
		}

		return targets;
	}

	private ConditionGroup createConditionGroupFromRecipientIcon(WorkflowRecipient icon) {
		ConditionGroup conditions;
		List<Integer> targets = icon.getTargets();

		switch (icon.getTargetsOption()) {
			case NOT_IN_TARGETS:
				conditions = new ConditionGroup(true, false);
				break;

			case ONE_TARGET_REQUIRED:
				conditions = new ConditionGroup(false);
				break;

			case ALL_TARGETS_REQUIRED:
			default:
				conditions = new ConditionGroup(true);
				break;
		}

		if (CollectionUtils.isNotEmpty(targets)) {
			for (Integer targetId : targets) {
				conditions.add(new TargetCondition(targetId));
			}
		}

		return conditions;
	}

	public String createRecipientIconTargetExpression(WorkflowRecipient recipientIcon) {
		Condition conditions = createConditionGroupFromRecipientIcon(recipientIcon);
		String targetExpression = Condition.toReducedTargetExpression(conditions);
		return targetExpression == null ? "" : targetExpression;
	}

	private void processParameters(WorkflowStart startIcon, Map<Integer, List<WorkflowParameter>> resultMap,
								   Map<WorkflowDecision, List<Integer>> optimizationMailings) {
		WorkflowNode startNode = workflowGraph.getNodeByIcon(startIcon);
		processParameters(startNode, new ArrayList<>(), resultMap, optimizationMailings);
	}

	private void processParameters(WorkflowNode currentNode, List<WorkflowParameter> parametersSoFar,
								   Map<Integer, List<WorkflowParameter>> resultMap,
								   Map<WorkflowDecision, List<Integer>> optimizationMailings) {
		WorkflowIcon currentIcon = currentNode.getNodeIcon();
		if (currentIcon.getType() == WorkflowIconType.PARAMETER.getId()) {
			WorkflowParameter parameter = (WorkflowParameter) currentIcon;
			parametersSoFar.add(parameter);
		}
		else if (WorkflowUtils.isMailingIcon(currentIcon)) {
			int mailingId = WorkflowUtils.getMailingId(currentIcon);
			if (mailingId > 0) {
				resultMap.put(mailingId, new ArrayList<>(parametersSoFar));
				parametersSoFar.clear();

				// if next icon is AO-decision - link this decision-icon to mailing
				// (as the mailing will be handled by auto-optimization)
				if (currentNode.getNextNodes().size() == 1) {
					WorkflowNode nextNode = currentNode.getNextNodes().get(0);
					WorkflowIcon nextIcon = nextNode.getNodeIcon();

					if (WorkflowUtils.isAutoOptimizationIcon(nextIcon)) {
						optimizationMailings.computeIfAbsent((WorkflowDecision) nextIcon, k -> new ArrayList<>())
								.add(mailingId);
					}
				}
			}
		}

		List<WorkflowNode> nextNodes = currentNode.getNextNodes();
		for (WorkflowNode nextNode : nextNodes) {
			ArrayList<WorkflowParameter> parameters = new ArrayList<>(parametersSoFar);
			processParameters(nextNode, parameters, resultMap, optimizationMailings);
		}
	}

	private List<WorkflowReactionStepDeclaration> processReactionSteps(WorkflowStart startIcon, boolean testRun, TimeZone timezone) {
    	ActionBasedCampaignProcessor processor = new ActionBasedCampaignProcessor(timezone, testRun);
    	return processor.process(startIcon);
	}

	private void processMailingTargets(WorkflowStart startIcon, Map<Integer, ConditionGroup> workflowConditions, boolean isMailTrackingAvailable) {
		final Set<Integer> activeIconTypes = new HashSet<>();
		activeIconTypes.add(WorkflowIconType.DECISION.getId());
		activeIconTypes.add(WorkflowIconType.IMPORT.getId());
		activeIconTypes.addAll(ALL_MAILING_TYPES);

		if (WorkflowUtils.is(startIcon, WorkflowStartEventType.EVENT_REACTION)) {
			// Action-based campaigns use separate mechanism driven by reaction trigger so deadlines, decisions and mailing
			// sending order are not managed by target groups assigned as target expression.
			return;
		}

		Map<Integer, List<TargetCondition>> immediateDependencies = new HashMap<>();

		workflowGraph.getAllNextIconsByType(startIcon, activeIconTypes, Collections.emptySet()).forEach(icon -> {
			WorkflowNode node = workflowGraph.getNodeByIcon(icon);
			int iconId = icon.getId();

			if (WorkflowUtils.isBranchingDecisionIcon(icon)) {
				WorkflowNode positiveNode = workflowGraph.getDecisionYesBranch(node);
				Set<Integer> positive = workflowGraph.getAllNextParallelIconsByType(positiveNode.getNodeIcon(), activeIconTypes, Collections.emptySet(), false)
						.stream()
						.map(WorkflowIcon::getId)
						.collect(Collectors.toSet());

				WorkflowNode negativeNode = workflowGraph.getDecisionNoBranch(node);
				Set<Integer> negative = workflowGraph.getAllNextParallelIconsByType(negativeNode.getNodeIcon(), activeIconTypes, Collections.emptySet(), false)
						.stream()
						.map(WorkflowIcon::getId)
						.collect(Collectors.toSet());

				Set<Integer> distinctIds = new HashSet<>();
				distinctIds.addAll(positive);
				distinctIds.addAll(negative);

				for (Integer dependentIconId : distinctIds) {
					boolean isBranchPositive = positive.contains(dependentIconId);
					boolean isBranchNegative = negative.contains(dependentIconId);

					// When positive and negative paths lead to the same icon (dependent icon belongs to both positive and negative branches),
					// ignore that collapsing dependencies
					if (isBranchPositive != isBranchNegative) {
						immediateDependencies.computeIfAbsent(dependentIconId, k -> new ArrayList<>())
								.add(new TargetCondition(iconId, isBranchPositive));
					}
				}
			} else {
				List<WorkflowNode> nextNodes = node.getNextNodes();
				if (CollectionUtils.isNotEmpty(nextNodes)) {
					for (WorkflowNode nextNode : nextNodes) {
						workflowGraph.getAllNextParallelIconsByType(nextNode.getNodeIcon(), activeIconTypes, Collections.emptySet(), false)
								.stream()
								.filter(followingIcon -> !WorkflowUtils.isAutoOptimizationIcon(followingIcon))
								.map(followingIcon -> immediateDependencies.computeIfAbsent(followingIcon.getId(), id -> new ArrayList<>()))
								.forEach(dependencies -> dependencies.add(new TargetCondition(iconId, true)));
					}
				}
			}
		});

		Map<CompositeKey, Condition> cache = new HashMap<>();

		for (WorkflowIcon icon : workflowGraph.getAllNextIconsByType(startIcon, ALL_MAILING_TYPES, Collections.emptySet())) {
			int mailingId = WorkflowUtils.getMailingId(icon);
			if (mailingId > 0) {
				workflowConditions.put(mailingId, new ConditionGroup(false, true) {{
					for (TargetCondition condition : immediateDependencies.getOrDefault(icon.getId(), Collections.emptyList())) {
						add(composeCondition(immediateDependencies, cache, new HashSet<>(), condition.getTargetId(), condition.isPositive(), isMailTrackingAvailable));
					}
				}});
			}
		}
	}

	private Condition composeCondition(Map<Integer, List<TargetCondition>> dependencies, Map<CompositeKey, Condition> cache, Set<Integer> reactedMailings, int iconId, boolean positive, boolean isMailtrackingAvailable) {
		WorkflowIcon icon = workflowGraph.getNodeByIconId(iconId).getNodeIcon();

		// Use composite key to provide separate target groups for different decision branches.
		CompositeKey cacheKey = CompositeKey.of(iconId, positive);
		Condition condition = cache.get(cacheKey);

		if (WorkflowUtils.isMailingIcon(icon)) {
			int mailingId = WorkflowUtils.getMailingId(icon);
			if (mailingId > 0) {
				if (reactedMailings.contains(mailingId)) {
					// Return empty group (to be reduced)
					condition = new ConditionGroup(true);
				} else if (condition == null && isMailtrackingAvailable) {
					int mailingSentTargetId = createMailingSentTarget((WorkflowMailingAware) icon);
					condition = new TargetCondition(mailingSentTargetId);
					cache.put(cacheKey, condition);
				}
			} else {
				// AO final mailing
				condition = new ConditionGroup(false) {{
					for (TargetCondition target : dependencies.getOrDefault(iconId, Collections.emptyList())) {
						add(composeCondition(dependencies, cache, reactedMailings, target.getTargetId(), target.isPositive(), isMailtrackingAvailable));
					}
				}};
			}
		} else if (WorkflowUtils.isAutoOptimizationIcon(icon)) {
			WorkflowDecision decision = (WorkflowDecision) icon;
			if (condition == null) {
				int testMailingsSentTargetId = createAutoOptimizationTarget(decision);
				condition = new TargetCondition(testMailingsSentTargetId);
				cache.put(cacheKey, condition);
			}
		} else if (WorkflowUtils.isBranchingDecisionIcon(icon)) {
			WorkflowDecision decision = (WorkflowDecision) icon;

			if (condition == null) {
				int decisionTargetId = createDecisionTarget(decision, positive);
				condition = new TargetCondition(decisionTargetId);
				cache.put(cacheKey, condition);
			}

			Condition branching = condition.clone();
			return new ConditionGroup(true, true) {{
				// Current decision's condition
				branching.setPositive(positive);
				add(branching);

				// Previous mailings (required to be sent) and decisions
				add(new ConditionGroup(false, true) {{
					// "Mailing is sent" check is not required in addition to click/open/etc reaction
					boolean excludeReactedMailing = decision.getDecisionCriteria() == WorkflowDecisionCriteria.DECISION_REACTION && positive;
					int reactionMailingId = decision.getMailingId();

					if (excludeReactedMailing) {
						reactedMailings.add(reactionMailingId);
					}

					for (TargetCondition target : dependencies.getOrDefault(iconId, Collections.emptyList())) {
						add(composeCondition(dependencies, cache, reactedMailings, target.getTargetId(), target.isPositive(), isMailtrackingAvailable));
					}

					if (excludeReactedMailing) {
						reactedMailings.remove(reactionMailingId);
					}
				}});
			}};
		} else if (icon.getType() == WorkflowIconType.IMPORT.getId()) {
			WorkflowImport autoImport = (WorkflowImport) icon;

			ConditionGroup incomingConditions = new ConditionGroup(false, true) {{
				for (TargetCondition target : dependencies.getOrDefault(iconId, Collections.emptyList())) {
					add(composeCondition(dependencies, cache, reactedMailings, target.getTargetId(), target.isPositive(), isMailtrackingAvailable));
				}
			}};

			if (autoImport.isErrorTolerant()) {
				return incomingConditions;
			} else {
				if (condition == null) {
					int autoImportTargetId = createImportTarget(autoImport);
					condition = new TargetCondition(autoImportTargetId);
					cache.put(cacheKey, condition);
				}

				Condition importCondition = condition.clone();
				importCondition.setPositive(positive);

				return new ConditionGroup(true, true) {{
					add(importCondition);
					add(incomingConditions);
				}};
			}
		}

		if (condition == null) {
			condition = new ConditionGroup(true);
		} else {
			condition = condition.clone();
			condition.setPositive(positive);
		}

		return condition;
	}

	private void processActionBasedStart(WorkflowStart startIcon, int workflowId, Date activationDate, boolean testRun, TimeZone timezone) {
		ComWorkflowReaction reaction = new ComWorkflowReaction();

		reaction.setCompanyId(companyId);
		reaction.setWorkflowId(workflowId);

		if (testRun) {
			reaction.setActive(false);
			reaction.setStartDate(activationDate);
		} else {
			reaction.setActive(true);
			reaction.setStartDate(WorkflowUtils.getStartStopIconDate(startIcon, timezone));
		}

		reaction.setAdminTimezone(timezone);
		reaction.setReactionType(startIcon.getReaction());
		reaction.setOnce(startIcon.isExecuteOnce());
		reaction.setMailingsToSend(Collections.emptyList());

		reaction.setMailinglistId(getMailingListId(startIcon));
		reaction.setTriggerMailingId(startIcon.getMailingId());
		reaction.setTriggerLinkId(startIcon.getLinkId());
		reaction.setProfileColumn(StringUtils.isEmpty(startIcon.getProfileField()) ? null : startIcon.getProfileField());

		// if the reaction is "Change of Profile" and the rules are used - take rules
		// from the start icon (we don't need rules-sql for any other reaction)
		if (startIcon.getReaction() == WorkflowReactionType.CHANGE_OF_PROFILE && startIcon.isUseRules()) {
			String sqlRules = eqlHelper.generateRuleEQL(companyId, startIcon.getRules(), startIcon.getProfileField(), startIcon.getDateFormat(), false);
			reaction.setRulesSQL(sqlRules);
		}

		// Legacy mode should never be used for newly activated workflows.
		reaction.setLegacyMode(false);

        reactionDao.saveReaction(reaction);

		List<WorkflowReactionStepDeclaration> steps = processReactionSteps(startIcon, testRun, timezone);
		reactionDao.saveReactionStepDeclarations(steps, reaction.getReactionId(), companyId);

		if (testRun) {
			int mailingListId = getMailingListId(startIcon);
			reactionDao.trigger(reaction, recipientDao.getAdminAndTestRecipientIds(companyId, mailingListId));
		}
	}

	private void processRuleBasedStart(WorkflowStart startIcon, Map<Integer, Integer> targetsMap, boolean testRun) {
    	if (testRun) {
    		// In order to manage the test run for rule-based campaign it's enough to avoid assigning of root target (rule) to mailings.
    		return;
		}

		workflowGraph.getAllNextIconsByType(startIcon, ALL_MAILING_TYPES, Collections.emptySet())
			.stream()
			.filter(icon -> WorkflowUtils.getMailingId(icon) > 0)
			.forEach(icon -> {
				List<WorkflowIcon> deadlines = workflowGraph.getAllPreviousIconsByType(icon, WorkflowIconType.Constants.DEADLINE_ID, Collections.emptySet());
				int mailingId = WorkflowUtils.getMailingId(icon);
				int targetId = createTargetWithDeadlineRespect(startIcon, deadlines);
				targetsMap.put(mailingId, targetId);
			});
	}

	private int getMailingListId(WorkflowStart startIcon) {
		return getMailingListId(workflowGraph.getNodeByIcon(startIcon));
	}

	private int getMailingListId(WorkflowNode node) {
    	WorkflowIcon icon = node.getNodeIcon();

    	if (icon.getType() == WorkflowIconType.RECIPIENT.getId()) {
    		WorkflowRecipient recipient = (WorkflowRecipient) icon;
    		if (recipient.getMailinglistId() > 0) {
    			return recipient.getMailinglistId();
			}
		}

		for (WorkflowNode n : node.getNextNodes()) {
    		int mailingListId = getMailingListId(n);
    		if (mailingListId > 0) {
    			return mailingListId;
			}
		}

		return 0;
	}

	private void processRecipientIcons(WorkflowStart startIcon, Map<Integer, List<WorkflowRecipient>> assignedRecipients) {
		WorkflowNode startNode = workflowGraph.getNodeByIcon(startIcon);
		processedNodes.clear();
		processRecipientIcons(assignedRecipients, new ArrayList<>(), startNode.getNextNodes(), false);
	}

	private void processRecipientIcons(Map<Integer, List<WorkflowRecipient>> assignedRecipients, List<WorkflowRecipient> recipientIcons,
									   List<WorkflowNode> nodes, boolean appendNextRecipientIcon) {
		for (WorkflowNode node : nodes) {
			if (!processedNodes.contains(node)) {
				processedNodes.add(node);
				WorkflowIcon icon = node.getNodeIcon();
				if (icon.getType() == WorkflowIconType.RECIPIENT.getId()) {
                    List<WorkflowRecipient> recipientIconsSoFar = recipientIcons;
                    recipientIcons = new ArrayList<>();
                    recipientIcons.addAll(recipientIconsSoFar);
                    if (!appendNextRecipientIcon) {
                        recipientIcons.clear();
                    }
                    recipientIcons.add((WorkflowRecipient) icon);
                    appendNextRecipientIcon = false;
				}
				else if (WorkflowUtils.isMailingIcon(icon) && !recipientIcons.isEmpty()) {
					int mailingId = WorkflowUtils.getMailingId(icon);
					if (mailingId > 0) {
						assignedRecipients.put(mailingId, recipientIcons);
					}
				}
                else if (WorkflowUtils.isBranchingDecisionIcon(icon)) {
                    appendNextRecipientIcon = true;
                }
				processRecipientIcons(assignedRecipients, recipientIcons, node.getNextNodes(), appendNextRecipientIcon);
			}
		}
	}
	
	private ComTarget createTarget(String eql, String targetName) {
    	return createTarget(eql, targetName, true);
    }

	private ComTarget createTarget(String eql, String targetName, boolean isTrackable) {
        ComTarget target = targetFactory.newTarget();
		
		target.setTargetName(targetName);
		target.setTargetDescription("");
		target.setCompanyID(companyId);
		target.setComponentHide(true);
		
		target.setEQL(eql);
		
        try {
            int targetId = targetDao.saveHiddenTarget(target);
            target.setId(targetId);
            if(isTrackable) {
            	workflowService.setTargetConditionDependency(target, companyId, workflowId);
			}
        } catch (TargetGroupPersistenceException e) {
            logger.error("Error creating target group during campaign activation: " + e.getMessage(), e);
			// @todo: inform the user (#monitor?)
        }
		
		return target;
    }
	
	private int createMailingSentTarget(WorkflowMailingAware mailing) {
		int mailingId = mailing.getMailingId();
		
		if (mailingId > 0) {
			String eql = String.format("RECEIVED MAILING %d", mailingId);
			return createTarget(eql, String.format(WORKFLOW_TARGET_NAME_PATTERN, "previous mailing has to be sent")).getId();
		}
		
		return 0;
	}
	
	private int createDecisionTarget(WorkflowDecision decision, boolean isPositiveDecisionCase) {
		try {
			if(WorkflowUtils.isBranchingDecisionIcon(decision)) {
				String eql = eqlHelper.generateDecisionEQL(companyId, decision, isPositiveDecisionCase);
				return createTarget(eql, String.format(WORKFLOW_TARGET_NAME_PATTERN, "decision")).getId();
			} else if(WorkflowUtils.isAutoOptimizationIcon(decision)) {
				return createAutoOptimizationTarget(decision);
			}
		} catch (Exception e) {
			logger.error("Error while creating target group during campaign activation: " + e.getMessage(), e);
		}
		
		return 0;
	}
	
	private int createAutoOptimizationTarget(WorkflowDecision decision) {
		try {
			String eql = workflowGraph.getAllNextParallelIconsByType(decision, ALL_MAILING_TYPES, Collections.emptySet(), true).stream()
					.map(WorkflowUtils::getMailingId)
					.map(mailingId -> String.format("RECEIVED MAILING %d", mailingId))
					.collect(Collectors.joining(" AND "));
			
			return createTarget(eql, String.format(WORKFLOW_TARGET_NAME_PATTERN, "decision")).getId();
		} catch (Exception e) {
			logger.error("Error while creating target group during campaign activation: " + e.getMessage(), e);
		}
		return 0;
	}
	
	private int createImportTarget(WorkflowImport importIcon) {
		int autoImportId = importIcon.getImportexportId();

    	if (autoImportId > 0) {
			String eql = String.format("FINISHED AUTOIMPORT %d", autoImportId);
			return createTarget(eql, String.format(WORKFLOW_TARGET_NAME_PATTERN, "auto-import succeeded")).getId();
		}

		return 0;
	}

	private void processStartDate(Map<Integer, Date> mailingsSendDates, WorkflowStart startIcon, Date date, boolean testRun, TimeZone adminTimeZone) {
		WorkflowNode startNode = workflowGraph.getNodeByIcon(startIcon);
		if (!testRun) {
			date = WorkflowUtils.getStartStopIconDate(startIcon, adminTimeZone);
		}
		processStartDate(mailingsSendDates, startNode.getNextNodes(), date, testRun, adminTimeZone);
	}

	private void processStartDate(Map<Integer, Date> mailingsSendDates, List<WorkflowNode> nodes, Date date, boolean testRun, TimeZone adminTimeZone) {
		for (WorkflowNode node : nodes) {
			WorkflowIcon icon = node.getNodeIcon();
			int iconId = icon.getId();
			Date nextDate = date;

			// store mailing send date
			if (WorkflowUtils.isMailingIcon(icon)) {
				int mailingId = WorkflowUtils.getMailingId(icon);
				if (mailingId > 0) {
					mailingsSendDates.put(mailingId, min(mailingsSendDates.get(mailingId), date));
				}
			}
			// store report send date
			else if (icon.getType() == WorkflowIconType.REPORT.getId()) {
				reportsSendDates.put(iconId, min(reportsSendDates.get(iconId), date));
			}
			// store the date for send of optimization test mailings
			else if (WorkflowUtils.isAutoOptimizationIcon(icon)) {
				optimizationsTestSendDates.put(iconId, min(optimizationsTestSendDates.get(iconId), date));
			}
			// store auto import activation date
			else if (icon.getType() == WorkflowIconType.IMPORT.getId()) {
				importsActivationDates.put(iconId, min(importsActivationDates.get(iconId), date));
			}
			// store auto export activation date
			else if (icon.getType() == WorkflowIconType.EXPORT.getId()) {
				exportsActivationDates.put(iconId, min(exportsActivationDates.get(iconId), date));
			}
			// change the processing date according to deadline-icon settings
			else if (icon.getType() == WorkflowIconType.DEADLINE.getId()) {
				WorkflowDeadline deadline = (WorkflowDeadline) node.getNodeIcon();
				nextDate = applyDeadline(date, deadline, testRun, adminTimeZone);
			}
			processStartDate(mailingsSendDates, node.getNextNodes(), nextDate, testRun, adminTimeZone);
		}
	}

	private Date min(Date d1, Date d2) {
		if (d1 != null && d2 != null) {
			return d1.before(d2) ? d1 : d2;
		} else {
			if (d1 != null) {
				return d1;
			}
			if (d2 != null) {
				return d2;
			}
		}
		return null;
	}

	private Date applyDeadline(Date triggerDate, WorkflowDeadline deadline, boolean testing, TimeZone adminTimeZone) {
        Calendar calendar = DateUtilities.calendar(triggerDate, adminTimeZone);

		if (testing) {
			calendar.add(Calendar.MINUTE, TESTING_MODE_DEADLINE_DURATION);
		} else {
			if (deadline.getDeadlineType() == WorkflowDeadline.WorkflowDeadlineType.TYPE_FIXED_DEADLINE) {
				calendar.setTime(WorkflowUtils.mergeIconDateAndTime(deadline.getDate(), deadline.getHour(), deadline.getHour()));
			} else {
				if (deadline.getTimeUnit() == WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_DAY) {
					calendar.add(Calendar.DAY_OF_MONTH, deadline.getDelayValue());
					if (deadline.isUseTime()) {
						calendar.set(Calendar.HOUR_OF_DAY, deadline.getHour());
						calendar.set(Calendar.MINUTE, deadline.getMinute());
					}
				} else if (deadline.getTimeUnit() == WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_HOUR) {
					calendar.add(Calendar.HOUR_OF_DAY, deadline.getDelayValue());
				} else if (deadline.getTimeUnit() == WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_MINUTE) {
					calendar.add(Calendar.MINUTE, deadline.getDelayValue());
				}
			}
		}
		return calendar.getTime();
	}

    private void updateAutoImportActivationDate(@VelocityCheck int companyId) throws Exception {
        for (Entry<Integer, Date> entry : importsActivationDates.entrySet()) {
            WorkflowImport importIcon = (WorkflowImport) workflowGraph.getNodeByIconId(entry.getKey()).getNodeIcon();
            autoImportService.setAutoActivationDateAndActivate(companyId, importIcon.getImportexportId(), entry.getValue(), true);
        }
    }

	private void updateAutoExportActivationDate(@VelocityCheck int companyId) throws Exception {
        for (Entry<Integer, Date> entry : exportsActivationDates.entrySet()) {
            WorkflowExport exportIcon = (WorkflowExport) workflowGraph.getNodeByIconId(entry.getKey()).getNodeIcon();
            autoExportService.setAutoActivationDateAndActivate(companyId, exportIcon.getImportexportId(), entry.getValue(), true);
        }
    }

	public void setWorkflowService(ComWorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void setMailingSendService(ComMailingSendService mailingSendService) {
		this.mailingSendService = mailingSendService;
	}

	public void setTargetFactory(TargetFactory targetFactory) {
		this.targetFactory = targetFactory;
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

	public void setReactionDao(ComWorkflowReactionDao reactionDao) {
		this.reactionDao = reactionDao;
	}

	public void setReportScheduleDao(ComWorkflowReportScheduleDao reportScheduleDao) {
		this.reportScheduleDao = reportScheduleDao;
	}

    public void setAutoImportService(AutoImportService autoImportService) {
        this.autoImportService = autoImportService;
    }

    public void setAutoExportService(AutoExportService autoExportService) {
        this.autoExportService = autoExportService;
    }

    public void setOptimizationService(ComOptimizationService optimizationService) {
		this.optimizationService = optimizationService;
	}

	public void setOptimizationScheduleService(ComOptimizationScheduleService optimizationScheduleService) {
		this.optimizationScheduleService = optimizationScheduleService;
	}

	@Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}
	
	@Required
	public void setEqlHelper(ComWorkflowEQLHelper eqlHelper) {
		this.eqlHelper = eqlHelper;
	}
	
	private class ActionBasedCampaignProcessor {
		private TimeZone timezone;
		private boolean testing;

		private Map<CompositeKey, Integer> targetsCache;
		private Map<WorkflowReactionStepDeclaration, WorkflowIcon> steps;
		private int maxStepId;

		public ActionBasedCampaignProcessor(TimeZone timezone, boolean testing) {
			this.timezone = timezone;
			this.testing = testing;
		}

		public List<WorkflowReactionStepDeclaration> process(WorkflowStart start) {
			targetsCache = new HashMap<>();
			steps = new LinkedHashMap<>();
			maxStepId = 0;

			process(workflowGraph.getNodeByIcon(start), new WorkflowReactionStepDeclarationImpl(), null);
			assignDecisionTargets();

			return new ArrayList<>(steps.keySet());
		}

		private void process(WorkflowNode node, WorkflowReactionStepDeclaration step, WorkflowReactionStepDeclaration previousMailingStep) {
			WorkflowIcon icon = node.getNodeIcon();

			if (WorkflowUtils.isBranchingDecisionIcon(icon)) {
				WorkflowReactionStepDeclaration positiveStep = next(step, icon, true);
				process(workflowGraph.getDecisionYesBranch(node), positiveStep, processForPositiveDecision((WorkflowDecision) icon, previousMailingStep));

				WorkflowReactionStepDeclaration negativeStep = next(step, icon, false);
				process(workflowGraph.getDecisionNoBranch(node), negativeStep, previousMailingStep);
			} else {
				if (WorkflowUtils.isMailingIcon(icon)) {
					step = next(step, icon);
					assignPreviousMailingTarget(step, previousMailingStep);
					previousMailingStep = step;
				} else if (icon.getType() == WorkflowIconType.DEADLINE.getId()) {
					step = append(step, deadline(icon));
				}

				process(node.getNextNodes(), step, previousMailingStep);
			}
		}

		private void process(List<WorkflowNode> nodes, WorkflowReactionStepDeclaration step, WorkflowReactionStepDeclaration previousMailingStep) {
			if (CollectionUtils.isNotEmpty(nodes)) {
				for (WorkflowNode next : nodes) {
					process(next, step, previousMailingStep);
				}
			}
		}

		private void assignPreviousMailingTarget(WorkflowReactionStepDeclaration step, WorkflowReactionStepDeclaration mailingStep) {
			if (mailingStep != null) {
				step.setTargetId(getTargetId(steps.get(mailingStep), true));
			}
		}

		private WorkflowReactionStepDeclaration processForPositiveDecision(WorkflowDecision decision, WorkflowReactionStepDeclaration mailingStep) {
			if (mailingStep == null) {
				return null;
			}

			WorkflowMailingAware mailing = (WorkflowMailingAware) steps.get(mailingStep);
			// The target group 'previous mailing sent' is redundant for positive path of decision that references same mailingId.
			if (decision.getMailingId() == mailing.getMailingId()) {
				return null;
			}

			return mailingStep;
		}

		private void assignDecisionTargets() {
			Set<Integer> referencedSteps = new HashSet<>();

			// Collect all the 'referenced' steps — the steps that some other ones depend on.
			for (WorkflowReactionStepDeclaration step : steps.keySet()) {
				referencedSteps.add(step.getPreviousStepId());
			}

			steps.forEach((step, icon) -> {
				// Prevent target group creation for final steps.
				if (WorkflowUtils.isBranchingDecisionIcon(icon) && referencedSteps.contains(step.getStepId())) {
					step.setTargetId(getTargetId(icon, step.isTargetPositive()));
				}
			});
		}

		private WorkflowReactionStepDeclaration append(WorkflowReactionStepDeclaration step, Deadline deadline) {
			WorkflowReactionStepDeclaration s = new WorkflowReactionStepDeclarationImpl(step);
			s.setDeadline(s.getDeadline().add(deadline));
			return s;
		}

		private Deadline deadline(WorkflowIcon deadline) {
			if (testing) {
				return TESTING_MODE_DEADLINE;
			} else {
				return WorkflowUtils.asDeadline((WorkflowDeadline) deadline, timezone);
			}
		}

		private WorkflowReactionStepDeclaration next(WorkflowReactionStepDeclaration step, WorkflowIcon icon) {
			return next(step, icon, true);
		}

		private WorkflowReactionStepDeclaration next(WorkflowReactionStepDeclaration step, WorkflowIcon icon, boolean positive) {
			return next(step, ++maxStepId, icon, positive);
		}

		private WorkflowReactionStepDeclaration next(WorkflowReactionStepDeclaration previousStep, int stepId, WorkflowIcon icon, boolean positive) {
			WorkflowReactionStepDeclaration step = new WorkflowReactionStepDeclarationImpl();

			step.setStepId(stepId);
			step.setPreviousStepId(previousStep.getStepId());
			step.setDeadline(previousStep.getDeadline());
			step.setTargetPositive(positive);

			if (WorkflowUtils.isMailingIcon(icon)) {
				step.setMailingId(WorkflowUtils.getMailingId(icon));
			}

			steps.put(step, icon);

			return step;
		}

		private int getTargetId(WorkflowIcon icon, boolean positive) {
			return targetsCache.computeIfAbsent(CompositeKey.of(icon, positive), k -> createTargetFrom(icon, positive));
		}

		private int createTargetFrom(WorkflowIcon icon, boolean positive) {
			if (WorkflowUtils.isBranchingDecisionIcon(icon)) {
				return createDecisionTarget((WorkflowDecision) icon, positive);
			}

			if (WorkflowUtils.isMailingIcon(icon)) {
				return createMailingSentTarget((WorkflowMailingAware) icon);
			}

			return 0;
		}
	}

	private abstract static class Condition {
		private boolean positive = true;

		public static String toReducedTargetExpression(Condition condition) {
			if (condition == null) {
				return null;
			}
			condition = condition.reduce();
			if (condition == null) {
				return null;
			}
			return condition.toTargetExpression();
		}

		public Condition() {
		}

		public Condition(boolean positive) {
			this.positive = positive;
		}

		public boolean isPositive() {
			return positive;
		}

		public void setPositive(boolean positive) {
			this.positive = positive;
		}

		public boolean isNegative() {
			return !positive;
		}

        /**
         * Used by JSP
         */
        @SuppressWarnings("unused")
		public void setNegative(boolean negative) {
			this.positive = !negative;
		}

		public void negate() {
			positive = !positive;
		}

		public Condition reduce() {
			return reduce(false);
		}

		public final String toTargetExpression() {
			return toTargetExpression(false);
		}

		public abstract boolean isConjunction();
		public abstract boolean isDisjunction();
		public abstract boolean isEmpty();
		@Override
		public abstract Condition clone();
		protected abstract Condition reduce(boolean negate);
		protected abstract String toTargetExpression(boolean atomic);
	}

	private class TargetCondition extends Condition {
		private int targetId;

		public TargetCondition(int targetId) {
			this.targetId = targetId;
		}

		public TargetCondition(int targetId, boolean positive) {
			super(positive);
			this.targetId = targetId;
		}

		public int getTargetId() {
			return targetId;
		}

        /**
         * Used by JSP
         */
        @SuppressWarnings("unused")
		public void setTargetId(int targetId) {
			this.targetId = targetId;
		}

		@Override
		public boolean isConjunction() {
			// Singular (atomic) condition could be a conjunction operand
			return true;
		}

		@Override
		public boolean isDisjunction() {
			// Singular (atomic) condition could be a disjunction operand
			return true;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public Condition clone() {
			return new TargetCondition(targetId, isPositive());
		}

		@Override
		protected Condition reduce(boolean negate) {
			Condition clone = clone();
			if (negate) {
				clone.negate();
			}
			return clone;
		}

		@Override
		protected String toTargetExpression(boolean atomic) {
			String id = Integer.toString(targetId);
			if (isPositive()) {
				return id;
			} else {
				return "!" + id;
			}
		}
	}

	private static class ConditionGroup extends Condition {
		private List<Condition> conditions = new LinkedList<>();
		private boolean conjunction;

		public ConditionGroup(boolean conjunction) {
			this.conjunction = conjunction;
		}

		public ConditionGroup(boolean conjunction, boolean positive) {
			super(positive);
			this.conjunction = conjunction;
		}

        @SuppressWarnings("unused")
		public boolean contains(Condition condition) {
			return conditions.contains(condition);
		}

		public void add(Condition condition) {
			conditions.add(condition);
		}

		public void addAll(Collection<Condition> conditions) {
			this.conditions.addAll(conditions);
		}

        @SuppressWarnings("unused")
		public void remove(Condition condition) {
			conditions.remove(condition);
		}

        @SuppressWarnings("unused")
		public void removeAll(Collection<Condition> conditions) {
			this.conditions.removeAll(conditions);
		}

		@Override
		public boolean isConjunction() {
			return conjunction;
		}

        @SuppressWarnings("unused")
		public void setConjunction(boolean conjunction) {
			this.conjunction = conjunction;
		}

		@Override
		public boolean isDisjunction() {
			return !conjunction;
		}

        @SuppressWarnings("unused")
		public void setDisjunction(boolean disjunction) {
			this.conjunction = !disjunction;
		}

		@Override
		public boolean isEmpty() {
			for (Condition condition : conditions) {
				if (!condition.isEmpty()) {
					return false;
				}
			}
			return true;
		}

		@Override
		public Condition clone() {
			ConditionGroup clone = new ConditionGroup(conjunction, isPositive());
			clone.conditions = conditions.stream()
					.map(Condition::clone)
					.collect(Collectors.toList());
			return clone;
		}

		@Override
		protected Condition reduce(boolean negate) {
			if (!conditions.isEmpty()) {
				boolean isNewPositive = negate ? !isPositive() : isPositive();
				boolean isNewNegative = !isNewPositive;

				List<Condition> newConditions = conditions.stream()
						.map(condition -> condition.reduce(isNewNegative))
						.filter(condition -> condition != null)
						.collect(Collectors.toList());

				if (newConditions.size() > 0) {
					if (newConditions.size() > 1) {
						return new ConditionGroup(isNewPositive ? isConjunction() : !isConjunction(), true) {{
							addAll(newConditions);
						}};
					} else {
						return newConditions.iterator().next();
					}
				}
			}
			return null;
		}

		@Override
		protected String toTargetExpression(boolean atomic) {
			if (conditions.isEmpty()) {
				return null;
			}

			Set<String> expressions = conditions.stream()
					// Open expressions with the same operators when possible: a & (b & c) -> a & b & c
					.map(condition -> condition.toTargetExpression(condition.isConjunction() != conjunction))
					.filter(StringUtils::isNotBlank)
					.collect(Collectors.toSet());

			if (CollectionUtils.isEmpty(expressions)) {
				return null;
			}

			String expression = StringUtils.join(expressions, conjunction ? "&" : "|");

			boolean negation = false;
			boolean brackets = false;

			if (expressions.size() > 1) {
				negation = isNegative();
				// Negation always requires brackets when multiple conditions/groups are there
				brackets = atomic || negation;
			} else {
				// Single child condition/group
				if (isNegative()) {
					// Collapse double negation
					if (expression.startsWith("!")) {
						expression = expression.substring(1);
					} else {
						negation = true;
					}
				}
			}

			if (brackets) {
				expression = "(" + expression + ")";
			}

			return negation ? ("!" + expression) : expression;
		}
	}
}
