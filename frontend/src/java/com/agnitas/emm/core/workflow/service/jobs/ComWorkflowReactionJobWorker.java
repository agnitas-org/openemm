/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.beans.Mailing;
import org.agnitas.service.JobWorker;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingException;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.workflow.beans.ComWorkflowReaction;
import com.agnitas.emm.core.workflow.beans.WorkflowActionMailingDeferral;
import com.agnitas.emm.core.workflow.beans.WorkflowDeadline;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.dao.ComWorkflowReactionDao;
import com.agnitas.emm.core.workflow.graph.WorkflowGraph;
import com.agnitas.emm.core.workflow.graph.WorkflowNode;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;

/**
 * Handles start-icon reactions
 */
public class ComWorkflowReactionJobWorker extends JobWorker {
    private static final transient Logger logger = Logger.getLogger(ComWorkflowReactionJobWorker.class);

    private enum SendingType {
        NONE,
        IMMEDIATE,
        DEFERRED
    }

    private ComWorkflowReactionDao reactionDao;
    private ComRecipientDao recipientDao;
    private ComTargetService targetService;
    private ComMailingDao mailingDao;
    private ComWorkflowService workflowService;
    private SendActionbasedMailingService sendActionbasedMailingService;

    @Override
    public void runJob() throws Exception {
        initBeans();

        final CompaniesConstraints constraints = getCompaniesConstrains();

        // Get reactions we need to check (active reactions with start date in the past)
        for (final ComWorkflowReaction reaction : reactionDao.getReactionsToCheck(constraints)) {
            final List<Integer> reactedRecipients = workflowService.getReactedRecipients(reaction, true);

            if (CollectionUtils.isNotEmpty(reactedRecipients)) {
                processReaction(reaction, reactedRecipients);
            }
        }

        // For legacy mode.
        processDeferrals(constraints);

        workflowService.processPendingReactionSteps(constraints);
    }

    private void processReaction(final ComWorkflowReaction reaction, final List<Integer> reactedRecipients) {
        // The current date/time is considered a moment when user reaction occurred but this timestamp is not a precise reaction moment.
        // Keep in mind that the maximal inaccuracy equals to the longest schedule period of the job worker (when a user
        // reacted right after the previous reactions check).
        final Date reactionDate = GregorianCalendar.getInstance().getTime();

        if (reaction.isLegacyMode()) {
            boolean reacted = false;

            // Get all the action-based mailings from the workflow
            final List<Integer> mailings = reaction.getMailingsToSend();

            if (CollectionUtils.isNotEmpty(mailings)) {

                for (final Integer mailingId : mailings) {
                    final Mailing mailing = mailingDao.getMailing(mailingId, reaction.getCompanyId());

                    switch (getSendingType(mailing, reaction, reactionDate)) {
                    case IMMEDIATE:
                        // Mailing should be sent when the reaction occurs (is not preceded by deadline(s))
                        send(mailing, reactedRecipients, reaction);
                        reacted = true;
                        break;

                    case DEFERRED:
                        // Mailing should be sent some time after the reaction occurs (relative deadline) or at the exact moment (fixed deadline)
                        defer(mailing, reactedRecipients, reaction, reactionDate);
                        reacted = true;
                        break;

                    case NONE:
                        // Mailing is disabled (not activated) and should not be sent
                        break;
                    }
                }
            }

            if (reacted) {
                reactionDao.trigger(reaction, reactedRecipients);
            }
        } else {
            reactionDao.trigger(reaction, reactedRecipients);
        }
    }

    /**
     * Send deferred action-based mailings.
     */
    private void processDeferrals(final CompaniesConstraints constraints) {
        // Retrieve an entries having a send date in the past.
        final List<WorkflowActionMailingDeferral> deferrals = reactionDao.getDeferredActionMailings(constraints);

        if (CollectionUtils.isNotEmpty(deferrals)) {
            final List<Integer> deferralsIds = new ArrayList<>(deferrals.size());

            final Map<Integer, Mailing> mailingCache = new HashMap<>();
            final Map<Integer, ComWorkflowReaction> reactionCache = new HashMap<>();

            for (final WorkflowActionMailingDeferral d : deferrals) {
                deferralsIds.add(d.getId());

                final Mailing mailing = mailingCache.computeIfAbsent(d.getMailingId(), (id) -> mailingDao.getMailing(id, d.getCompanyId()));
                final ComWorkflowReaction reaction = reactionCache.computeIfAbsent(d.getReactionId(), (id) -> reactionDao.getReaction(id, d.getCompanyId()));

                send(mailing, Collections.singletonList(d.getCustomerId()), reaction);
            }

            // No matter if the mailing was sent or not - we remove these deferrals as the deadline was reached
            reactionDao.markDeferredActionMailingsAsSent(deferralsIds);
        }
    }

    private void send(final Mailing mailing, List<Integer> recipients, final ComWorkflowReaction reaction) {
        final boolean checkForActive = workflowService.checkReactionNeedsActiveBinding(reaction);

        // Check if the recipient matches all conditions of TGs he could obtain during deadline time.
        final String sqlTargetExpression = targetService.getSQLFromTargetExpression(mailing, true);
        recipients = recipientDao.filterRecipientsByMailinglistAndTarget(recipients, reaction.getCompanyId(), mailing.getMailinglistID(), sqlTargetExpression, false, checkForActive);

        if (!recipients.isEmpty()) {
            final List<Integer> userStatusList = workflowService.getProperUserStatusList(reaction);
            for (final int recipientId : recipients) {
                try {
                	final MailgunOptions mailgunOptions = new MailgunOptions();
        			if (userStatusList != null) {
        				mailgunOptions.withAllowedUserStatus(userStatusList);
        			}
                    sendActionbasedMailingService.sendActionbasedMailing(mailing.getCompanyID(), mailing.getId(), recipientId, 0, mailgunOptions);
                } catch (final SendActionbasedMailingException e) {
                    // todo #monitor?
                    logger.error("WM Reaction: error (reactionId: " + reaction.getReactionId() + ", workflowId: " + reaction.getWorkflowId() + "): " + e.getMessage(), e);
                }
            }
        }
    }

    private void defer(final Mailing mailing, final List<Integer> recipients, final ComWorkflowReaction reaction, final Date reactionDate) {
        // If the mailing is deferred (affected by deadline) - save recipients and send the mailing when the time comes.
        Date sendDate = getSendDate(mailing);

        // Mailing is disabled (not activated) and should not be sent
        if (sendDate == null) {
            return;
        }

        if (isSendDateRelativeToReaction(mailing, reaction)) {
            // Reaction start date is simply the moment when reaction is started to be watched so the relative deadline should be
            // treated as a delay between actual reaction occurrence and the mailing sending.
            final long delayMs = sendDate.getTime() - reaction.getStartDate().getTime();
            sendDate = new Date(reactionDate.getTime() + delayMs);
            reactionDao.addDeferredActionMailings(reaction.getReactionId(), mailing.getId(), recipients, sendDate, reaction.getCompanyId());
        } else {
            // If the mailing is preceded by a fixed deadline (absolute send date) then check if customers have reacted
            // before the deadline. If so we're going to defer the sending of the mailing. Otherwise (if deadline is reached)
            // the reacted customers will not receive such a deferred mailing.
            if (reactionDate.before(sendDate)) {
                reactionDao.addDeferredActionMailings(reaction.getReactionId(), mailing.getId(), recipients, sendDate, reaction.getCompanyId());
            }
        }
    }

    private Date getSendDate(final Mailing mailing) {
        final Set<MaildropEntry> maildrops = mailing.getMaildropStatus();
        if (maildrops != null) {
            for (final MaildropEntry maildrop : maildrops) {
                if (maildrop.getStatus() == MaildropStatus.ACTION_BASED.getCode()) {
                    return maildrop.getSendDate();
                }
            }
        }
        return null;
    }

    private SendingType getSendingType(final Mailing mailing, final ComWorkflowReaction reaction, final Date reactionDate) {
        final Date mailingDate = getSendDate(mailing);

        if (mailingDate == null) {
            // The maildrop entry is invalid or doesn't exist
            return SendingType.NONE;
        } else {
            if (mailingDate.after(reaction.getStartDate())) {
                // There's a deadline icon preceding the mailing icon
                return SendingType.DEFERRED;
            } else {
                if (reactionDate.after(mailingDate)) {
                    // It's time to send the mailing
                    return SendingType.IMMEDIATE;
                } else {
                    // Invalid state (should normally never occur)
                    return SendingType.NONE;
                }
            }
        }
    }

    /**
     * Check if there's at least one fixed deadline icon somewhere in a chain before the mailing. The point is that FIXED
     * plus one or more RELATIVE(S) is FIXED anyway (so the mailing send date is not relative to user reaction but absolute).
     */
    private boolean isSendDateRelativeToReaction(final Mailing mailing, final ComWorkflowReaction reaction) {
        final WorkflowGraph workflowGraph = new WorkflowGraph(workflowService.getIcons(reaction.getWorkflowId(), reaction.getCompanyId()));

        final List<WorkflowNode> mailingNodes = workflowGraph.getAllNodesByType(WorkflowIconType.ACTION_BASED_MAILING.getId());
        for (final WorkflowNode node : mailingNodes) {
            if (WorkflowUtils.getMailingId(node.getNodeIcon()) == mailing.getId()) {
                final List<WorkflowIcon> deadlines  = workflowGraph.getAllPreviousIconsByType(node.getNodeIcon(), WorkflowIconType.DEADLINE.getId(), Collections.emptySet());

                for (final WorkflowIcon icon : deadlines) {
                    final WorkflowDeadline deadline = (WorkflowDeadline) icon;

                    // Fixed deadline is simply the moment when the mailing should be sent. It's an absolute value so it doesn't
                    // depend on the reaction moment.
                    // Although notice that a reactions occurred after fixed deadline are ignored.
                    if (deadline.getDeadlineType() == WorkflowDeadline.WorkflowDeadlineType.TYPE_FIXED_DEADLINE) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void initBeans() {
        reactionDao = daoLookupFactory.getBeanWorkflowReactionDao();
        recipientDao = daoLookupFactory.getBeanRecipientDao();
        targetService = serviceLookupFactory.getBeanTargetService();
        mailingDao = daoLookupFactory.getBeanMailingDao();
        workflowService = serviceLookupFactory.getBeanWorkflowService();
        sendActionbasedMailingService = serviceLookupFactory.getBeanSendActionbasedMailingService();
    }
}
