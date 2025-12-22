/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import static com.agnitas.emm.core.target.beans.ConditionalOperator.CONTAINS;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.EQ;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.GEQ;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.GT;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.IS;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.LEQ;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.LIKE;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.LT;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.MOD;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.NEQ;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.NO;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.NOT_CONTAINS;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.NOT_LIKE;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.NOT_STARTS_WITH;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.STARTS_WITH;
import static com.agnitas.emm.core.target.beans.ConditionalOperator.YES;
import static com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus.STATUS_PAUSED;
import static com.agnitas.emm.core.workflow.service.util.WorkflowUtils.MAILING_ICON_TYPE_IDS;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.TrackableLink;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.auto_import.bean.AutoImport;
import com.agnitas.emm.core.auto_import.service.AutoImportService;
import com.agnitas.emm.core.autoexport.beans.AutoExport;
import com.agnitas.emm.core.autoexport.service.AutoExportService;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.components.service.MailingSendService;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.dao.MaildropStatusDao;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.objectusage.common.ObjectUsage;
import com.agnitas.emm.core.objectusage.common.ObjectUsageType;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.beans.ConditionalOperator;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.WorkflowConnection;
import com.agnitas.emm.core.workflow.beans.WorkflowDeadline;
import com.agnitas.emm.core.workflow.beans.WorkflowDeadline.WorkflowDeadlineType;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowDecisionCriteria;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowDecisionType;
import com.agnitas.emm.core.workflow.beans.WorkflowExport;
import com.agnitas.emm.core.workflow.beans.WorkflowFollowupMailing;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowImport;
import com.agnitas.emm.core.workflow.beans.WorkflowMailingAware;
import com.agnitas.emm.core.workflow.beans.WorkflowParameter;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.beans.WorkflowRecipient;
import com.agnitas.emm.core.workflow.beans.WorkflowRule;
import com.agnitas.emm.core.workflow.beans.WorkflowStart;
import com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartEventType;
import com.agnitas.emm.core.workflow.beans.WorkflowStartStop;
import com.agnitas.emm.core.workflow.beans.WorkflowStop;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowDateBasedMailingImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowDeadlineImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowDecisionImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowRecipientImpl;
import com.agnitas.emm.core.workflow.graph.WorkflowGraph;
import com.agnitas.emm.core.workflow.graph.WorkflowNode;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils.StartType;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.DbColumnType.SimpleDataType;
import com.agnitas.web.mvc.Popups;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorkflowValidationService {
    private static final Logger logger = LogManager.getLogger(WorkflowValidationService.class);

    private static final String VALID_NUMBER_TOKEN_REGEX = "\\d+|\\(\\s*\\d+|\\d+\\s*\\)";

    private static final String NULL_VALUE = "NULL";
    private static final String NOT_NULL_VALUE = "NOT_NULL";
    private static final int MIN_HOURS_BETWEEN_START_AND_AUTOOPT = 2;

    private static final Map<SimpleDataType, Set<Integer>> ALLOWED_OPERATORS_BY_TYPE = new HashMap<>();
    private static final Set<Integer> ALLOWED_OPERATORS_CODE_FOR_NUMERIC_TYPE = new HashSet<>();
    private static final Set<Integer> ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE = new HashSet<>();
    private static final Set<Integer> ALLOWED_OPERATORS_CODE_FOR_DATE_TYPE = new HashSet<>();
    private static final Map<String, String> ALLOWED_DATE_FORMATS = new HashMap<>();

    static {
        // init allowed operators for Numeric type;
        ALLOWED_OPERATORS_CODE_FOR_NUMERIC_TYPE.add(EQ.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_NUMERIC_TYPE.add(NEQ.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_NUMERIC_TYPE.add(GT.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_NUMERIC_TYPE.add(LT.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_NUMERIC_TYPE.add(MOD.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_NUMERIC_TYPE.add(IS.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_NUMERIC_TYPE.add(LEQ.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_NUMERIC_TYPE.add(GEQ.getOperatorCode());

        // init allowed operators for Characters type;
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(EQ.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(NEQ.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(GT.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(LT.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(LIKE.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(NOT_LIKE.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(IS.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(LEQ.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(GEQ.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(CONTAINS.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(NOT_CONTAINS.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(STARTS_WITH.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE.add(NOT_STARTS_WITH.getOperatorCode());

        // init allowed operators for Date type;
        ALLOWED_OPERATORS_CODE_FOR_DATE_TYPE.add(EQ.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_DATE_TYPE.add(NEQ.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_DATE_TYPE.add(GT.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_DATE_TYPE.add(LT.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_DATE_TYPE.add(IS.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_DATE_TYPE.add(LEQ.getOperatorCode());
        ALLOWED_OPERATORS_CODE_FOR_DATE_TYPE.add(GEQ.getOperatorCode());

        // init operators by type map;
        ALLOWED_OPERATORS_BY_TYPE.put(SimpleDataType.Numeric, ALLOWED_OPERATORS_CODE_FOR_NUMERIC_TYPE);
        ALLOWED_OPERATORS_BY_TYPE.put(SimpleDataType.Float, ALLOWED_OPERATORS_CODE_FOR_NUMERIC_TYPE);
        ALLOWED_OPERATORS_BY_TYPE.put(SimpleDataType.Characters, ALLOWED_OPERATORS_CODE_FOR_CHARACTERS_TYPE);
        ALLOWED_OPERATORS_BY_TYPE.put(SimpleDataType.Date, ALLOWED_OPERATORS_CODE_FOR_DATE_TYPE);
        ALLOWED_OPERATORS_BY_TYPE.put(SimpleDataType.DateTime, ALLOWED_OPERATORS_CODE_FOR_DATE_TYPE);

        ALLOWED_DATE_FORMATS.put("yyyymmdd", "yyyyMMdd");
        ALLOWED_DATE_FORMATS.put("yyyymm", "yyyyMM");
        ALLOWED_DATE_FORMATS.put("yyyy", "yyyy");
        ALLOWED_DATE_FORMATS.put("mmdd", "MMdd");
        ALLOWED_DATE_FORMATS.put("mm", "MM");
        ALLOWED_DATE_FORMATS.put("dd", "dd");
    }

    public enum MailingTrackingUsageErrorType {
        BASE_MAILING_NOT_FOUND,
        DECISION_MAILING_INVALID,
        BASE_MAILING_DISORDERED,
        DECISION_MAILING_DISORDERED,
        MAILING_TRACKING_DISABLED,
        EXPIRATION_PERIOD_EXCEEDED
    }

    public static class MailingTrackingUsageError {
        private MailingTrackingUsageErrorType errorType;
        private int mailingId;
        private MailingType mailingType;
        private String mailingName;

        public MailingTrackingUsageError(MailingTrackingUsageErrorType errorType, int mailingId) {
            this(errorType, mailingId, MailingType.NORMAL);
        }

        public MailingTrackingUsageError(MailingTrackingUsageErrorType errorType, int mailingId, MailingType mailingType) {
            this.errorType = errorType;
            this.mailingId = mailingId;
            this.mailingType = mailingType;
        }

        public MailingTrackingUsageError(MailingTrackingUsageErrorType errorType, int mailingId, MailingType mailingType, String mailingName) {
            this.errorType = errorType;
            this.mailingId = mailingId;
            this.mailingType = mailingType;
            this.mailingName = mailingName;
        }

        public MailingTrackingUsageErrorType getErrorType() {
            return errorType;
        }

        public int getMailingId() {
            return mailingId;
        }

        public void setMailingId(int mailingId) {
            this.mailingId = mailingId;
        }

        public MailingType getMailingType() {
            return mailingType;
        }

        public void setMailingType(MailingType mailingType) {
            this.mailingType = mailingType;
        }

        public String getMailingName() {
            return mailingName;
        }

        public void setMailingName(String mailingName) {
            this.mailingName = mailingName;
        }
    }

    // Allowed statuses for base mailings (for follow-up)
    private static final Set<String> FOLLOWED_MAILING_STATUSES = new HashSet<>();

    private TrackableLinkService trackableLinkService;
    private WorkflowService workflowService;
    private AutoImportService autoImportService;
    private AutoExportService autoExportService;
    private MailingDao mailingDao;
    private MaildropStatusDao maildropStatusDao;
    private ProfileFieldDao profileFieldDao;
    private MailingSendService mailingSendService;
    private MailingService mailingService;
    private TargetService targetService;
    private CompanyService companyService;
    private ConfigService configService;

    static {
        FOLLOWED_MAILING_STATUSES.add(MailingStatus.ACTIVE.getDbKey());
        FOLLOWED_MAILING_STATUSES.add(MailingStatus.SCHEDULED.getDbKey());
        FOLLOWED_MAILING_STATUSES.add(MailingStatus.SENT.getDbKey());
        FOLLOWED_MAILING_STATUSES.add(MailingStatus.NORECIPIENTS.getDbKey());
        FOLLOWED_MAILING_STATUSES.add(MailingStatus.SENDING.getDbKey());
    }

    public SimpleServiceResult validateStatusTransition(Workflow.WorkflowStatus currentStatus, Workflow.WorkflowStatus newStatus) {
        switch (currentStatus) {
            case STATUS_COMPLETE:
                return SimpleServiceResult.simpleError(Message.of("error.workflow.campaignStatusHasNotBeUpdatedAfterCompleted"));

            case STATUS_ACTIVE:
                if (newStatus == Workflow.WorkflowStatus.STATUS_OPEN || newStatus == Workflow.WorkflowStatus.STATUS_INACTIVE || newStatus == STATUS_PAUSED) {
                    return SimpleServiceResult.simpleSuccess();
                }
                return SimpleServiceResult.simpleError(Message.of("error.workflow.SaveActivatedWorkflow"));
            case STATUS_TESTING:
                if (newStatus == Workflow.WorkflowStatus.STATUS_OPEN || newStatus == Workflow.WorkflowStatus.STATUS_INACTIVE) {
                    return SimpleServiceResult.simpleSuccess();
                }
                return SimpleServiceResult.simpleError(Message.of("error.workflow.SaveActivatedWorkflow"));
            default:
                switch (newStatus) {
                    case STATUS_OPEN, STATUS_INACTIVE, STATUS_ACTIVE, STATUS_TESTING:
                        return SimpleServiceResult.simpleSuccess();

                    default:
                        return SimpleServiceResult.simpleError();
                }
        }
    }

    public SimpleServiceResult validate(int workflowId, List<WorkflowIcon> icons, Workflow.WorkflowStatus oldStatus, Workflow.WorkflowStatus newStatus, Admin admin) {
        List<Message> messages = new ArrayList<>();

        final int companyId = admin.getCompanyID();
        final boolean isMailtrackingActive = companyService.isMailtrackingActive(companyId);
        final TimeZone timezone = TimeZone.getTimeZone(admin.getAdminTimezone());
        final boolean isTesting = newStatus == Workflow.WorkflowStatus.STATUS_TESTING;
        final boolean isDuringPause = WorkflowUtils.isDuringPause(oldStatus, newStatus);
        final boolean isUnpausing = WorkflowUtils.isUnpausing(oldStatus, newStatus);
        final WorkflowGraph workflowGraph = new WorkflowGraph(icons);

        if (!isAllIconsFilled(icons)) {
            messages.add(Message.of("error.workflow.nodesShouldBeFilled"));
            if (icons.isEmpty()) {
                return SimpleServiceResult.simpleError(messages);
            }
        }
        if (isStartIconMissing(icons) || isStopIconMissing(icons)) {
            messages.add(Message.of("error.workflow.campaignShouldHaveAtLeastOneStartAndEnd"));
        }
        if (!validateReminderRecipients(icons)) {
            messages.add(Message.of("error.email.invalid"));
        }
        if (!hasRecipient(icons)) {
            messages.add(Message.of("error.workflow.campaignShouldHaveRecipient"));
        }
        if (isStartDateInPast(icons, timezone) && !isUnpausing && !isDuringPause) {
            messages.add(Message.of("error.workflow.campaignShouldNotHaveStartDateInPast"));
        }
        if (!isNotStopDateInPast(icons, timezone)) {
            messages.add(Message.of("error.workflow.campaignShouldNotHaveStopDateInPast"));
        }
        if (!noStartDateLaterEndDate(icons, timezone)) {
            messages.add(Message.of("error.validation.enddate"));
        }
        if (isReminderDateInThePast(icons, timezone)) {
            messages.add(Message.of("error.workflow.reminderDateInPast"));
        }
        if (!hasIconsConnections(icons)) {
            messages.add(Message.of("error.workflow.nodesShouldHaveIncomingAndOutgoingArrows"));
        }
        if (isAutoImportMisused(icons)) {
            messages.add(Message.of("error.workflow.autoImport.start.event"));
        }
        if (isAutoImportHavingShortDeadline(icons)) {
            messages.add(Message.of("error.workflow.autoImport.delay.tooShort", WorkflowDeadlineImpl.DEFAULT_AUTOIMPORT_DELAY_LIMIT));
        }
        if (decisionsHaveTwoOutgoingConnections(icons)) {
            if (!isMailtrackingActive && !decisionNegativePathConnectionShouldLeadToStopIcon(icons)) {
                messages.add(Message.of("error.workflow.decisionNegativePathConnectionShouldLeadToStopIcon"));
            }
        } else {
            messages.add(Message.of("error.workflow.decisionsShouldHaveTwoOutgoingConnections"));
        }
        if (!parametersSumNotHigher100(icons)) {
            messages.add(Message.of("error.workflow.ParametersSumNotHigher100"));
        }
        if (moreThanOneStartPresented(icons)) {
            messages.add(Message.of("error.workflow.start.one"));
        }
        if (!noMailingsBeforeRecipient(icons)) {
            messages.add(Message.of("error.workflow.NoMailingsBeforeRecipient"));
        }
        if (!checkMailingTypesCompatible(icons)) {
            messages.add(Message.of("error.workflow.mixedMailingTypes"));
        }
        if (!isFixedDeadlineUsageCorrect(icons)) {
            messages.add(Message.of("error.workflow.fixedDeadlineIsNotPermitted"));
        }
        if (!isDeadlineDateCorrect(icons, timezone)) {
            messages.add(Message.of("error.workflow.deadlineDateTooEarly"));
        }
        if (!mailingHasOnlyOneRecipient(icons)) {
            messages.add(Message.of("error.workflow.mailingHasOneRecipient"));
        }
        if (!campaignHasOnlyOneMailingList(icons)) {
            messages.add(Message.of("error.workflow.oneMailingList"));
        }
        if (!campaignHasOnlyMailingsAssignedToThisWorkflow(icons, companyId, workflowId)) {
            messages.add(Message.of("error.workflow.mailingIsUsingInAnotherCampaign"));
        }

        if (!isImportIsNotActive(icons, companyId)) {
            messages.add(Message.of("error.workflow.importIsActive"));
        }

        if (!isExportIsNotActive(icons, companyId)) {
            messages.add(Message.of("error.workflow.exportIsActive"));
        }

        if (!isTesting && isInvalidDelayForDateBase(icons)) {
            messages.add(Message.of("error.workflow.dateBased.invalid.delay"));
        }

        if (!isDateBaseCampaign(icons) && !isAnyTargetForDateBased(icons)) {
            messages.add(Message.of("error.mailing.rulebased_without_target"));
        }

        if (workflowService.hasDeletedMailings(icons, companyId)) {
            messages.add(Message.of("error.workflow.containsDeletedContent"));
        }

        if (missingRecipientsLoadInSplitBranches(workflowGraph)) {
            messages.add(Message.of("error.workflow.mailing.split.recipients"));
        }

        int mailingTrackingDataExpirationPeriod = 0;
        if (isMailtrackingActive) {
            mailingTrackingDataExpirationPeriod = configService.getIntegerValue(ConfigValue.ExpireSuccess, companyId);
        }

        if (!noLoops(icons)) {
            messages.add(Message.of("error.workflow.NoLoops"));
            return SimpleServiceResult.simpleError(messages);
        }

        validateInvalidTargetGroups(companyId, icons, messages);

        messages.addAll(validateAutoOptimization(icons));
        messages.addAll(validateStartTrigger(icons, companyId));
        messages.addAll(validateMailingTrackingUsage(icons, companyId, mailingTrackingDataExpirationPeriod));
        messages.addAll(validateReferencedProfileFields(icons, companyId));
        messages.addAll(validateOperatorsInDecisions(icons, companyId));
        messages.addAll(validateMailingDataAndComponents(icons, admin));
        messages.addAll(validateSplits(workflowGraph));

        messages.addAll(validateDuplicatedMailings(icons, admin));

        if (messages.isEmpty()) {
            return validateStatusTransition(oldStatus, newStatus);
        }

        return SimpleServiceResult.simpleError(messages);
    }

    private List<Message> validateSplits(WorkflowGraph graph) {
        return containsInvalidSplit(graph)
            ? List.of(Message.of("error.workflow.split.icon"))
            : emptyList();
    }

    private static boolean containsInvalidSplit(WorkflowGraph graph) {
        return graph.getAllNodesByType(WorkflowIconType.PARAMETER.getId()).stream()
            .filter(node -> node.getNodeIcon().isEditable())
            .anyMatch(paramNode -> missingPreviousSplitIcon(paramNode, graph)
                                   && missingNextAutoOptIcon(paramNode, graph));
    }

    private static boolean missingNextAutoOptIcon(WorkflowNode node, WorkflowGraph graph) {
        WorkflowIcon decision = graph.getNextIconByType(
            node.getNodeIcon(), WorkflowIconType.DECISION.getId(), emptyList(), false);
        return decision == null || !WorkflowUtils.isAutoOptimizationIcon(decision);
    }

    private static boolean missingPreviousSplitIcon(WorkflowNode node, WorkflowGraph graph) {
        return graph.getPreviousIconByType(node.getNodeIcon(), WorkflowIconType.SPLIT.getId(), emptySet()) == null;
    }

    private List<Message> validateDuplicatedMailings(List<WorkflowIcon> icons, Admin admin) {
        List<Mailing> duplicatedMailings = mailingService.getDuplicateMailing(icons, admin.getCompanyID());
        if (duplicatedMailings.isEmpty()) {
            return emptyList();
        }

        List<ObjectUsage> usages = duplicatedMailings.stream()
                .map(m -> new ObjectUsage(ObjectUsageType.MAILING, m.getId(), m.getShortname()))
                .collect(Collectors.toList());

        return List.of(new ObjectUsages(usages).toMessage("error.workflow.mailingIsUsingInSeveralIcons", admin.getLocale()));
    }

    private void validateInvalidTargetGroups(int companyId, List<WorkflowIcon> icons, List<Message> messages) {
        final Set<Integer> invalidTargets = getInvalidTargetGroups(companyId, icons);
        if (CollectionUtils.isNotEmpty(invalidTargets)) {
            final List<String> names = targetService.getTargetNamesByIds(companyId, invalidTargets);
            messages.add(Message.of("error.workflow.targets.invalid", StringUtils.join(names, ", ")));
        }
    }

    private List<Message> validateMailingTrackingUsage(List<WorkflowIcon> icons, int companyId, int trackingDays) {
        List<Message> messages = new ArrayList<>();

        // It's possible to show a separate error message for each case (e.g. listing names of affected mailings)
        // but for now just get rid of duplicated messages.
        Set<WorkflowValidationService.MailingTrackingUsageErrorType> reportedErrors = new HashSet<>();

        checkFollowupMailing(icons, companyId, trackingDays).forEach(e -> {
            if (reportedErrors.add(e.getErrorType())) {
                messages.add(translateToActionMessage(e, trackingDays));
            }
        });

        checkMailingTrackingUsage(icons, trackingDays).forEach(e -> {
            if (reportedErrors.add(e.getErrorType())) {
                messages.add(translateToActionMessage(e, trackingDays));
            }
        });

        checkMailingsReferencedInDecisions(icons, companyId, trackingDays).forEach(e -> {
            if (reportedErrors.add(e.getErrorType())) {
                messages.add(translateToActionMessage(e, trackingDays));
            }
        });

        return messages;
    }

    private Message translateToActionMessage(WorkflowValidationService.MailingTrackingUsageError error, int trackingDays) {
        MailingType mailingType = error.getMailingType();
        switch (error.getErrorType()) {
            case BASE_MAILING_NOT_FOUND, DECISION_MAILING_INVALID:
                if (mailingType == MailingType.ACTION_BASED || mailingType == MailingType.DATE_BASED) {
                    return Message.of("error.workflow.baseMailingNeedActivated", error.getMailingName());
                } else {
                    return Message.of("error.workflow.baseMailingNeedsSent", error.getMailingName());
                }

            case BASE_MAILING_DISORDERED:
                return Message.of("error.workflow.baseMailingAtFirst", error.getMailingName());

            case DECISION_MAILING_DISORDERED:
                return Message.of("error.workflow.decision.requiresMailingBefore", error.getMailingName());

            case MAILING_TRACKING_DISABLED:
                return Message.of("error.workflow.trackingRequired");

            case EXPIRATION_PERIOD_EXCEEDED:
                return Message.of("error.workflow.trackingtime", trackingDays);

            default:
                return Message.of(ERROR_MSG);
        }
    }

    private List<Message> validateReferencedProfileFields(List<WorkflowIcon> workflowIcons, int companyId) {
        List<String> columns;
        List<Message> messages = new ArrayList<>();

        columns = checkTrackableProfileFields(workflowIcons, companyId);
        if (!columns.isEmpty()) {
            messages.add(Message.of("error.workflow.profiledb.missingTrackableColumns", "<br>" + StringUtils.join(columns, "<br>")));
        }

        columns = checkProfileFieldsUsedInConditions(workflowIcons, companyId);
        if (!columns.isEmpty()) {
            messages.add(Message.of("error.workflow.profiledb.missingColumnsForConditions", "<br>" + StringUtils.join(columns, "<br>")));
        }

        return messages;
    }

    private List<Message> validateOperatorsInDecisions(List<WorkflowIcon> icons, int companyId) {
        List<Message> messages = new ArrayList<>();

        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.DECISION.getId() && icon.isFilled()) {
                WorkflowDecision decision = (WorkflowDecision) icon;
                int criteriaId = decision.getDecisionCriteria().getId();
                if (criteriaId == WorkflowDecision.WorkflowDecisionCriteria.DECISION_PROFILE_FIELD.getId()) {
                    messages.addAll(validateDecisionRules(decision, companyId));
                }
                if (criteriaId == WorkflowDecision.WorkflowDecisionCriteria.DECISION_REACTION.getId()) {
                    messages.addAll(validateDecisionReaction(decision, companyId));
                }
            }
        }

        return messages;
    }

    public boolean validateBasicStructure(List<WorkflowIcon> icons) {
        return validateIconIds(icons) && noLoops(icons) && hasIconsConnections(icons);
    }

    public List<Message> validateAutoOptimization(List<WorkflowIcon> icons) {
        List<String> errorCodes  = new ArrayList<>(2);
        if (!validateAutoOptimizationStructure(icons)) {
            errorCodes.add("error.workflow.connection.notAllowed");
        }
        if (isInvalidAutoOptimizationTime(icons)) {
            errorCodes.add("error.workflow.autoopt.time.short");
        }
        return errorCodes.stream().map(Message::of).collect(Collectors.toList());
    }

    private boolean isInvalidAutoOptimizationTime(List<WorkflowIcon> icons) {
        WorkflowGraph graph = new WorkflowGraph(icons);
        return icons.stream()
                .filter(WorkflowIcon::isFilled)
                .filter(WorkflowUtils::isAutoOptimizationIcon)
                .map(WorkflowDecisionImpl.class::cast)
                .anyMatch(icon -> {
                    WorkflowIcon start = getLinkedStart(icon, graph);
                    if (!start.isFilled()) {
                        return false;
                    }
                    Date startTime = ((WorkflowStart) start).getDate();
                    Date optimizationTime = icon.getDecisionDate();

                    if (optimizationTime == null) {
                        return false;
                    }
                    return optimizationTime.before(DateUtils.addHours(startTime, MIN_HOURS_BETWEEN_START_AND_AUTOOPT));
                });
    }

    private WorkflowIcon getLinkedStart(WorkflowDecisionImpl icon, WorkflowGraph graph) {
        return graph.getPreviousIconByType(icon, WorkflowIconType.START.getId(), emptySet());
    }

    private boolean validateAutoOptimizationStructure(List<WorkflowIcon> icons) {
        WorkflowGraph graph = new WorkflowGraph(icons);

        try {
            for (WorkflowIcon icon : icons) {
                // Non-editable mailing is only allowed as a part of A/B-test campaign.
                if (!icon.isEditable() && icon.getType() == WorkflowIconType.MAILING.getId()) {
                    validateAutoOptimizationMailingConnections(graph.getNodeByIcon(icon));
                }

                if (WorkflowUtils.isAutoOptimizationIcon(icon)) {
                    validateAutoOptimizationDecisionConnection(graph.getNodeByIcon(icon));
                }
            }

            return true;
        } catch (WorkflowStructureException e) {
            logger.debug("Error occurred: {}", e.getMessage(), e);
            return false;
        }
    }

    private void validateAutoOptimizationMailingConnections(WorkflowNode mailingNode) throws WorkflowStructureException {
        expectType(mailingNode, WorkflowIconType.MAILING);

        WorkflowNode parameterNode = expectOneSibling(mailingNode, false, WorkflowIconType.PARAMETER);
        WorkflowNode decisionNode = expectOneSibling(parameterNode, false, WorkflowIconType.DECISION);

        if (!WorkflowUtils.isAutoOptimizationIcon(decisionNode.getNodeIcon())) {
            throw new WorkflowStructureException("Invalid decision icon type: auto-optimization was expected");
        }

        List<WorkflowNode> testMailingNodes = expectSiblings(decisionNode, false, 2, 5, WorkflowIconType.MAILING);

        for (WorkflowNode testMailingNode : testMailingNodes) {
            WorkflowNode testParameterNode = expectOneSibling(testMailingNode, false, WorkflowIconType.PARAMETER);
            expectOneSibling(testParameterNode, false, WorkflowIconType.ARCHIVE);
        }
    }

    private void validateAutoOptimizationDecisionConnection(WorkflowNode decisionNode) throws WorkflowStructureException {
        if (!WorkflowUtils.isAutoOptimizationIcon(decisionNode.getNodeIcon())) {
            throw new WorkflowStructureException("Invalid decision icon type: auto-optimization was expected");
        }

        WorkflowNode parameterNode = expectOneSibling(decisionNode, true, WorkflowIconType.PARAMETER);
        WorkflowNode mailingNode = expectOneSibling(parameterNode, true, WorkflowIconType.MAILING);

        WorkflowIcon mailingIcon = mailingNode.getNodeIcon();

        if (mailingIcon.isEditable()) {
            throw new WorkflowStructureException("Missing expected final mailing for auto-optimization (must be non-editable)");
        }
    }

    private boolean validateIconIds(List<WorkflowIcon> icons) {
        Set<Integer> iconIds = new HashSet<>();
        Set<Integer> connectionIds = new HashSet<>();

        for (WorkflowIcon icon : icons) {
            // An icon has an invalid id value.
            if (icon.getId() <= 0) {
                return false;
            }

            // An icon has non-unique id.
            if (!iconIds.add(icon.getId())) {
                return false;
            }

            List<WorkflowConnection> connections = icon.getConnections();
            // Collect all the referenced icon ids.
            if (connections != null) {
                for (WorkflowConnection connection : connections) {
                    connectionIds.add(connection.getTargetIconId());
                }
            }
        }

        return iconIds.containsAll(connectionIds);
    }

    public List<Message> validateStartTrigger(List<WorkflowIcon> icons, int companyId) {
        List<Message> messages = new ArrayList<>();

        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.START.getId() && icon.isFilled()) {
                WorkflowStart start = (WorkflowStart) icon;

                switch (StartType.of(start)) {
                    case REACTION:
                        int mailingListId = getMailingListId(icons);

                        if (WorkflowReactionType.CHANGE_OF_PROFILE == start.getReaction() && mailingListId > 0) {
                            for (Workflow workflow : getActiveWorkflowsDrivenByProfileChange(companyId, mailingListId, start)) {
                                String name = StringEscapeUtils.escapeHtml4(workflow.getShortname());
                                messages.add(Message.of("error.workflow.reaction.trigger.unique", name));
                            }
                        }
                        if (WorkflowReactionType.CLICKED_LINK == start.getReaction() && start.getLinkId() > 0) {
                            validateClickedLink(companyId, messages, start.getLinkId());
                        }
                        break;
                    case RULE:
                        String rule = start.getDateFieldValue();

                        if (rule != null && !validateStartRuleExpression(rule)) {
                            messages.add(Message.of("error.workflow.rule.invalid"));
                        }
                        break;
                    case REGULAR:
                    case UNKNOWN:
                    default:
                        break;
                }
            }
        }

        return messages;
    }

    private void validateClickedLink(int companyId, List<Message> messages, int linkId) {
        TrackableLink link = trackableLinkService.getTrackableLink(companyId, linkId);
        if (link == null) {
            messages.add(Message.of("error.workflow.link.removed"));
        }
    }

    public boolean isAutoImportMisused(List<WorkflowIcon> icons) {
        WorkflowGraph graph = new WorkflowGraph(icons);

        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.IMPORT.getId()) {
                // Find a start icon related to this import icon.
                WorkflowIcon start = graph.getPreviousIconByType(icon, WorkflowIconType.START.getId(), emptySet());

                // If a start icon is missing or isn't filled yet just ignore that (let's assume it's ok).
                if (start != null && start.isFilled()) {
                    StartType type = StartType.of((WorkflowStart) start);
                    // So far an import icon is only allowed in normal campaigns (normal date-based and follow-up mailings).
                    if (type != StartType.REGULAR && type != StartType.UNKNOWN && type != StartType.RULE) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isAutoImportHavingShortDeadline(List<WorkflowIcon> icons) {
        WorkflowGraph graph = new WorkflowGraph(icons);

        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.IMPORT.getId()) {
                // The deadline icon must always be connected here (an immediate outgoing connection).
                for (WorkflowNode node : graph.getNodeByIcon(icon).getNextNodes()) {
                    WorkflowIcon nextIcon = node.getNodeIcon();

                    // Make sure that a deadline is at least 1 hours.
                    if (nextIcon.getType() == WorkflowIconType.DEADLINE.getId() && nextIcon.isFilled()) {
                        WorkflowDeadline deadline = (WorkflowDeadline) nextIcon;
    
                        switch (deadline.getDeadlineType()) {
                            case TYPE_DELAY:
                                // Relative deadline: simply check time units and value.
                                switch (deadline.getTimeUnit()) {
                                    case TIME_UNIT_MINUTE:
                                        return true;
                                    case TIME_UNIT_HOUR:
                                        return deadline.getDelayValue() < WorkflowDeadlineImpl.DEFAULT_AUTOIMPORT_DELAY_LIMIT;
									case TIME_UNIT_DAY:
										break;
									case TIME_UNIT_MONTH:
										break;
									case TIME_UNIT_WEEK:
										break;
									default:
										break;
                                }
                                break;

                            case TYPE_FIXED_DEADLINE:
                                // Absolute deadline: compare to start icon + previous deadline(s).
                                Date minDate = workflowService.getMaxPossibleDate(icon, icons);
                                if (minDate != null) {
                                    minDate = DateUtils.addHours(minDate, WorkflowDeadlineImpl.DEFAULT_AUTOIMPORT_DELAY_LIMIT);

                                    // Use default timezone here since getMaxPossibleDate uses it as well.
                                    Date deadlineDate = WorkflowUtils.mergeIconDateAndTime(deadline.getDate(), deadline.getHour(), deadline.getMinute(), TimeZone.getDefault());

                                    if (deadlineDate.before(minDate)) {
                                        return true;
                                    }
                                }
                                break;
                                
							default:
								break;
                        }
                    }
                }
            }
        }

        return false;
    }

    private List<Workflow> getActiveWorkflowsDrivenByProfileChange(int companyId, int mailingListId, WorkflowStart start) {
        List<WorkflowRule> rules = null;

        if (start.isUseRules()) {
            rules = start.getRules();
        }

        return workflowService.getActiveWorkflowsDrivenByProfileChange(companyId, mailingListId, start.getProfileField(), rules);
    }

    private boolean validateStartRuleExpression(String expression) {
        return validateDateRuleExpression(expression, null);
    }

    private boolean validateDateRuleExpression(String expression, String format) {
        expression = expression.toLowerCase().trim();

        boolean startWithNowWorld = false;
        if(expression.startsWith("sysdate")){
            expression = expression.substring("sysdate".length());
            startWithNowWorld = true;
        } else if(expression.startsWith("today")){
            expression = expression.substring("today".length());
            startWithNowWorld = true;
        }

        if (startWithNowWorld) {
            if (StringUtils.countMatches(expression, "(") != StringUtils.countMatches(expression, ")")) {
                return false;
            }

            for (String token : expression.split("([+\\-*/])")) {
                if (StringUtils.isNoneBlank(token) && !token.trim().matches(VALID_NUMBER_TOKEN_REGEX)) {
                    return false;
                }
            }

            return true;
        }

        return StringUtils.isNotEmpty(format) && validateDateFormat(expression, format);
    }

    private boolean validateDateFormat(String expression, String format) {
        return DateUtilities.parse(expression, new SimpleDateFormat(format)) != null;
    }

    public List<Integer> collectSentMailings(List<WorkflowIcon> workflowIcons, Admin admin) {
        return workflowIcons.stream()
                .filter(WorkflowUtils::isMailingIcon)
                .map(WorkflowUtils::getMailingId)
                .filter(id -> isSentMailing(id, admin.getCompanyID())).collect(Collectors.toList());
    }

    private boolean isSentMailing(Integer id, int companyId) {
        return id != null && id > 0
                && maildropStatusDao.listMaildropStatus(id, companyId).stream().anyMatch(this::hasForbiddenStatus);
    }

    public List<Message> validateMailingDataAndComponents(List<WorkflowIcon> workflowIcons, Admin admin) {
        Locale locale = admin.getLocale();
        return workflowIcons.stream()
                .filter(WorkflowUtils::isMailingIcon)
                .map(WorkflowUtils::getMailingId)
                .filter(id -> id > 0)
                .map(id -> mailingService.getMailing(admin.getCompanyID(), id))
                .flatMap(mailing -> collectMailingDataAndComponentErrors(mailing, locale)).collect(Collectors.toList());
    }

    private Stream<Message> collectMailingDataAndComponentErrors(Mailing mailing, Locale locale) {
        return mailingSendService.isRequiredDataAndComponentsExists(mailing)
                .getErrorMessages().stream()
                .map(error -> getErrorMsgWithMailingName(error, locale, mailing.getShortname()));
    }

    private static Message getErrorMsgWithMailingName(Message error, Locale locale, String mailingName) {
        return Message.exact(I18nString.getLocaleString("referencingObject.MAILING", locale, mailingName) + ":<br>"
                + I18nString.getLocaleString(error.getCode(), locale, error.getArguments()));
    }

    private boolean hasForbiddenStatus(MaildropEntry entry) {
        boolean result = false;
        if (entry.getStatus() == MaildropStatus.WORLD.getCode()) {
            result = true;
        } else if (entry.getStatus() == MaildropStatus.TEST.getCode() || entry.getStatus() == MaildropStatus.ADMIN.getCode()) {
            if (!MaildropGenerationStatus.isFinalState(entry.getGenStatus())) {
                return true;
            }
        }
        return result;
    }

    public boolean isAllIconsFilled(List<WorkflowIcon> workflowIcons) {
        for (WorkflowIcon icon : workflowIcons) {
            if (!isIconFilled(icon)) {
                return false;
            }
        }
        return isAutoOptimizationDecisionDateFilled(workflowIcons);
    }

    private boolean isAutoOptimizationDecisionDateFilled(List<WorkflowIcon> workflowIcons) {
        boolean isAutoOptimization = false;
        boolean usedActionBasedOrDateBasedMailing = false;
        boolean decisionDateFilled = true;

        for (WorkflowIcon icon : workflowIcons) {
            switch (icon.getType()) {
                case WorkflowIconType.Constants.ACTION_BASED_MAILING_ID:
                case WorkflowIconType.Constants.DATE_BASED_MAILING_ID:
                    usedActionBasedOrDateBasedMailing = true;
                    break;

                case WorkflowIconType.Constants.DECISION_ID:
                    if (icon.isFilled()) {
                        WorkflowDecision decision = (WorkflowDecision) icon;
                        if (decision.getDecisionType() == WorkflowDecision.WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION) {
                            isAutoOptimization = true;
                            decisionDateFilled = decision.getDecisionDate() != null;
                        }
                    }
                    break;

				default:
					break;
            }
        }


        if (isAutoOptimization && !usedActionBasedOrDateBasedMailing) {
            //ignore if campaign contains date_based or action_based mailings
            //date_based and action_based mailings are not allowed in A/B campaign
            return decisionDateFilled;
        }

        return true;
    }

    private boolean isIconFilled(WorkflowIcon icon) {
        if (WorkflowUtils.isMailingIcon(icon)) {
            if (icon.getType() == WorkflowIconType.FOLLOWUP_MAILING.getId()) {
                WorkflowFollowupMailing mailing = (WorkflowFollowupMailing) icon;
                return mailing.getBaseMailingId() > 0 && mailing.getMailingId() > 0;
            } else {
                // Non-editable mailing icon represents a final mailing of an A/B test (auto-optimization) and it
                // doesn't require a valid mailingId.
                return WorkflowUtils.getMailingId(icon) > 0 || !icon.isEditable();
            }
        } else if (icon.getType() == WorkflowIconType.START.getId()) {
            WorkflowStart start = (WorkflowStart) icon;
            if (WorkflowUtils.is(start, WorkflowStartEventType.EVENT_DATE)) {
                return start.getDateFieldValue() != null;
            }
        }
        return icon.isFilled();
    }

    public boolean isInvalidDelayForDateBase(List<WorkflowIcon> workflowIcons) {
        if (isDateBaseCampaign(workflowIcons)) {
            return workflowIcons.stream()
                    .filter(icon -> WorkflowIconType.fromId(icon.getType()) == WorkflowIconType.DEADLINE)
                    .map(WorkflowDeadline.class::cast)
                    .filter(WorkflowIcon::isFilled)
                    .filter(deadline -> deadline.getTimeUnit() == WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_MINUTE)
                    .anyMatch(deadline -> deadline.getDelayValue() < Duration.ofHours(1).toMinutes());
        }

        return false;
    }

    public boolean isDateBaseCampaign(List<WorkflowIcon> workflowIcons) {
        return workflowIcons.stream()
                .filter(icon -> WorkflowIconType.fromId(icon.getType()) == WorkflowIconType.START)
                .map(WorkflowStart.class::cast)
                .anyMatch(start -> WorkflowUtils.is(start, WorkflowStartEventType.EVENT_DATE));
    }

    public boolean isAnyTargetForDateBased(List<WorkflowIcon> workflowIcons) {
        WorkflowGraph graph = new WorkflowGraph(workflowIcons);

        return workflowIcons.stream()
                .filter(icon -> WorkflowIconType.fromId(icon.getType()) == WorkflowIconType.DATE_BASED_MAILING)
                .map(WorkflowDateBasedMailingImpl.class::cast)
                .map(icon -> graph.getPreviousIconByType(icon, WorkflowIconType.RECIPIENT.getId(), new HashSet<>()))
                .map(WorkflowRecipientImpl.class::cast)
                .filter(Objects::nonNull)
                .allMatch(icon -> CollectionUtils.isNotEmpty(icon.getTargets()) || CollectionUtils.isNotEmpty(icon.getAltgs()));
    }
    
    public boolean checkMailingTypesCompatible(List<WorkflowIcon> icons) {
        Set<Integer> mailingTypes = new HashSet<>();

        for (WorkflowIcon icon : icons) {
            if (WorkflowUtils.isMailingIcon(icon)) {
                mailingTypes.add(icon.getType());
            }
        }

        if (mailingTypes.size() == 2) {
            // Normal and follow-up mailings are compatible with each other, all the others are strictly exclusive
            return mailingTypes.contains(WorkflowIconType.MAILING.getId()) && mailingTypes.contains(WorkflowIconType.FOLLOWUP_MAILING.getId());
        } else {
            return mailingTypes.size() <= 1;
        }
    }

    public boolean hasRecipient(List<WorkflowIcon> workflowIcons) {
        for (WorkflowIcon icon : workflowIcons) {
            if (icon.getType() == WorkflowIconType.RECIPIENT.getId()) {
                return true;
            }
        }
        return false;
    }

    public boolean isStartIconMissing(List<WorkflowIcon> workflowIcons) {
        for (WorkflowIcon icon : workflowIcons) {
            if (icon.getType() == WorkflowIconType.START.getId()) {
                return false;
            }
        }
        return true;
    }

    public boolean isStopIconMissing(List<WorkflowIcon> workflowIcons) {
        for (WorkflowIcon icon : workflowIcons) {
            if (icon.getType() == WorkflowIconType.STOP.getId()) {
                return false;
            }
        }
        return true;
    }

    public boolean validateReminderRecipients(List<WorkflowIcon> workflowIcons) {
        for (WorkflowIcon icon : workflowIcons) {
            // Ignore unfilled icons.
            if (!icon.isFilled()) {
                continue;
            }

            switch (icon.getType()) {
                case WorkflowIconType.Constants.START_ID:
                    WorkflowStart start = (WorkflowStart) icon;
                    if (start.isSendReminder() && !AgnUtils.isEmailsListValid(start.getRecipients(), false)) {
                        return false;
                    }
                    break;

                case WorkflowIconType.Constants.STOP_ID:
                    WorkflowStop stop = (WorkflowStop) icon;
                    if (WorkflowStop.WorkflowEndType.DATE == stop.getEndType() && stop.isSendReminder() && !AgnUtils.isEmailsListValid(stop.getRecipients(), false)) {
                        return false;
                    }
                    break;
                    
				default:
					break;
            }
        }

        return true;
    }

    public boolean isReminderDateInThePast(List<WorkflowIcon> icons, TimeZone timezone) {
        for (WorkflowIcon icon : icons) {
            // Ignore unfilled icons.
            if (!icon.isFilled()) {
                continue;
            }

            switch (icon.getType()) {
                case WorkflowIconType.Constants.START_ID:
                    WorkflowStart start = (WorkflowStart) icon;
                    if (start.isSendReminder() && isReminderDateInThePast(start, timezone)) {
                        return true;
                    }
                    break;

                case WorkflowIconType.Constants.STOP_ID:
                    WorkflowStop stop = (WorkflowStop) icon;
                    if (WorkflowStop.WorkflowEndType.DATE == stop.getEndType() && stop.isSendReminder() && isReminderDateInThePast(stop, timezone)) {
                        return true;
                    }
                    break;
                    
				default:
					break;
            }
        }

        return false;
    }

    private boolean isReminderDateInThePast(WorkflowStartStop icon, TimeZone timezone) {
        if (icon.isRemindSpecificDate()) {
            return DateUtilities.isPast(WorkflowUtils.getReminderSpecificDate(icon, timezone));
        }
        return false;
    }

    public boolean isStartDateInPast(List<WorkflowIcon> workflowIcons, TimeZone timezone) {
        for (WorkflowIcon icon : workflowIcons) {
            if (icon.getType() == WorkflowIconType.START.getId()) {
                WorkflowStart start = (WorkflowStart) icon;
                if (checkHasDateInThePast(start, timezone)) {
                    return true;
                }
                if (WorkflowUtils.is(start, WorkflowStartEventType.EVENT_DATE)) {
                    start.setMinute(0);
                }
            }
        }
        return false;
    }

    public boolean isNotStopDateInPast(List<WorkflowIcon> workflowIcons, TimeZone timezone) {
        for (WorkflowIcon icon : workflowIcons) {
            if (icon.getType() == WorkflowIconType.STOP.getId()) {
                WorkflowStop workflowStopIcon = (WorkflowStop) icon;
                if ((workflowStopIcon.getEndType() != WorkflowStop.WorkflowEndType.AUTOMATIC) && checkHasDateInThePast(workflowStopIcon, timezone)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean noStartDateLaterEndDate(List<WorkflowIcon> workflowIcons, TimeZone timezone) {
        if (isStartIconMissing(workflowIcons) || isStopIconMissing(workflowIcons)) {
            return true;
        }
        WorkflowStart latestStart = getLatestStartDate(workflowIcons, timezone);
        WorkflowStop earlyStop = getEarlyEndDate(workflowIcons, timezone);
        if ((latestStart != null) && (earlyStop != null)) {
                return compareDates(latestStart, earlyStop, timezone) == -1;
        } else {
           return true;
        }
    }

    private WorkflowStart getLatestStartDate(List<WorkflowIcon> workflowIcons, TimeZone timezone) {
        WorkflowStart resultIcon = null;
        for (WorkflowIcon icon : workflowIcons) {
            if (icon.isFilled() && icon.getType() == WorkflowIconType.START.getId()) {
                WorkflowStart start = (WorkflowStart) icon;

                if (resultIcon == null || compareDates(start, resultIcon, timezone) == 1) {
                    resultIcon = start;
                }
            }
        }
        return resultIcon;
    }

    private WorkflowStop getEarlyEndDate(List<WorkflowIcon> workflowIcons, TimeZone timezone) {
        WorkflowStop resultIcon = null;
        for (WorkflowIcon icon : workflowIcons) {
            if (icon.getType() == WorkflowIconType.STOP.getId()) {
                WorkflowStop workflowStopIcon = (WorkflowStop) icon;
                if (workflowStopIcon.getEndType() == WorkflowStop.WorkflowEndType.AUTOMATIC) {
                    continue;
                }
                if (resultIcon != null) {
                    if (compareDates(workflowStopIcon, resultIcon, timezone) == -1) {
                        resultIcon = workflowStopIcon;
                    }
                } else {
                    resultIcon = workflowStopIcon;
                }
            }
        }
        return resultIcon;
    }

    public boolean hasIconsConnections(List<WorkflowIcon> icons) {
        Set<Integer> incoming = new HashSet<>();

        WorkflowUtils.forEachConnection(icons, (sourceId, targetId) -> incoming.add(targetId));

        for (WorkflowIcon icon : icons) {
            if (icon.getType() != WorkflowIconType.START.getId() && !incoming.contains(icon.getId())) {
                return false;
            }
            if (icon.getType() != WorkflowIconType.STOP.getId() && CollectionUtils.isEmpty(icon.getConnections())) {
                return false;
            }
        }

        return true;
    }

    public boolean decisionsHaveTwoOutgoingConnections(List<WorkflowIcon> icons) {
        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.DECISION.getId()) {
                WorkflowDecision decision = (WorkflowDecision) icon;
                if (decision.getDecisionType() != WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION) {
                    if (CollectionUtils.size(icon.getConnections()) != 2) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Has to be called in case when {@link CompanyDao#isMailtrackingActive(int)} returns false
     * @param icons workflow icons to process.
     * @return true if no-path connection leads to stop-icon and false otherwise, also returns false when something is wrong with outgoing connections
     */
    public boolean decisionNegativePathConnectionShouldLeadToStopIcon(List<WorkflowIcon> icons) {
        WorkflowGraph graph = new WorkflowGraph();

        if (graph.build(icons)) {
            for (WorkflowNode decisionNode : graph.getAllNodesByType(WorkflowIconType.DECISION.getId())) {
                WorkflowDecision decision = (WorkflowDecision) decisionNode.getNodeIcon();
                if (decision.getDecisionType() != WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION) {
                    WorkflowNode noPathNode = graph.getDecisionNoBranch(decisionNode);
                    if (noPathNode == null || noPathNode.getNodeIcon().getType() != WorkflowIconType.STOP.getId()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Check whether a mailing (referenced in a decision icon) exists and placed somewhere before a decision icon. For an
     * external mailings check validity of a mailtracking use.
     * @param icons workflow icons to process.
     * @param trackingDays mailtracking data expiration period (or 0 when a mailtracking is disabled).
     */
    private List<MailingTrackingUsageError> checkMailingsReferencedInDecisions(List<WorkflowIcon> icons, int companyId, int trackingDays) {
        List<MailingTrackingUsageError> errors = new ArrayList<>();
        WorkflowGraph workflowGraph = new WorkflowGraph();
        if (!workflowGraph.build(icons)) {
            return errors;
        }

        List<WorkflowNode> decisionNodes = workflowGraph.getAllNodesByType(WorkflowIconType.DECISION.getId());
        if (CollectionUtils.isNotEmpty(decisionNodes)) {
            final Set<Integer> mailingTypes = new HashSet<>(Arrays.asList(
                    WorkflowIconType.MAILING.getId(),
                    WorkflowIconType.ACTION_BASED_MAILING.getId(),
                    WorkflowIconType.DATE_BASED_MAILING.getId(),
                    WorkflowIconType.FOLLOWUP_MAILING.getId()
            ));

            for (WorkflowNode decisionNode : decisionNodes) {
                WorkflowDecision decision = (WorkflowDecision) decisionNode.getNodeIcon();

                if (decision.isFilled()
                        && WorkflowDecisionType.TYPE_DECISION == decision.getDecisionType()
                        && WorkflowDecisionCriteria.DECISION_REACTION == decision.getDecisionCriteria()
                        && decision.getMailingId() > 0) {

                    Date decisionDate = workflowService.getMaxPossibleDate(decision, icons);
                    if (decisionDate != null) {
                        boolean mailingFound = false;

                        // All the mailing icons in the workflow
                        List<WorkflowMailingAware> mailingIcons = workflowGraph.getAllNodesByTypes(mailingTypes)
                                .stream()
                                .map(node -> (WorkflowMailingAware) node.getNodeIcon())
                                .collect(Collectors.toList());

                        // Mailing icons available through incoming connections of the decision icon
                        Set<WorkflowMailingAware> precedingMailingIcons = workflowGraph.getAllPreviousIconsByType(decision, mailingTypes, new ArrayList<>())
                                .stream()
                                .map(icon -> (WorkflowMailingAware) icon)
                                .collect(Collectors.toSet());

                        for (WorkflowMailingAware mailingIcon : mailingIcons) {
                            if (mailingIcon.getMailingId() == decision.getMailingId()) {
                                mailingFound = true;

                                // Action-based mailing icon could be placed wherever, but the rest of types of mailing icon must not follow a decision icon.
                                if (mailingIcon.getType() != WorkflowIconType.ACTION_BASED_MAILING.getId()) {
                                    if (precedingMailingIcons.contains(mailingIcon)) {
                                        Date mailingDate = workflowService.getMaxPossibleDate(mailingIcon, icons);
                                        if (mailingDate != null) {
                                            if (decisionDate.before(mailingDate)) {
                                                errors.add(errorMailingDisordered(mailingIcon, companyId));
                                            }
                                        } else {
                                            logger.debug("Unable to calculate send date for mailing icon #{} (mailingId: {})", mailingIcon.getId(), mailingIcon.getMailingId());
                                        }
                                    } else {
                                        errors.add(errorMailingDisordered(mailingIcon, companyId));
                                    }
                                }

                                // The mailing allowed to be used only in one icon, no sense to continue
                                break;
                            }
                        }

                        if (!mailingFound) {
                            // Such a mailingId is not found among mailing icons - it is an external mailing
                            Map<String, Object> baseMailing = mailingDao.getMailingWithWorkStatus(decision.getMailingId(), companyId);
                            if (baseMailing.isEmpty()) {
                                errors.add(new MailingTrackingUsageError(
                                    MailingTrackingUsageErrorType.DECISION_MAILING_INVALID,
                                    decision.getMailingId()
                                ));
                            } else {
                                String baseStatus = (String) baseMailing.get("work_status");
                                Date baseSendDate = (Date) baseMailing.get("senddate");
                                MailingType mailingType = MailingType.getByCode(((Number) baseMailing.get("mailing_type")).intValue());

                                if (baseSendDate != null && FOLLOWED_MAILING_STATUSES.contains(baseStatus)) {
                                    // Calculate the max possible send date for a mailings dependent on decision
                                    Optional<Date> followingMailingMaxDate = workflowGraph.getAllNextParallelIconsByType(decision, mailingTypes, new ArrayList<>(), false)
                                            .stream()
                                            .map(icon -> workflowService.getMaxPossibleDate(icon, icons))
                                            .filter(Objects::nonNull)
                                            .max(Date::compareTo);

                                    if (followingMailingMaxDate.isPresent()) {
                                        Date mailingDate = followingMailingMaxDate.get();

                                        if (trackingDays > 0) {
                                            Calendar expirationDate = Calendar.getInstance();
                                            expirationDate.setTime(baseSendDate);
                                            expirationDate.add(Calendar.DAY_OF_YEAR, trackingDays);

                                            //Task GWUA-3391.
                                            boolean skipExpirePeriodValidation =
                                                    isHasNotActionMailingAfter(decision, workflowGraph);

                                            if (!skipExpirePeriodValidation && mailingDate.after(expirationDate.getTime())) {
                                                errors.add(new MailingTrackingUsageError(
                                                        MailingTrackingUsageErrorType.EXPIRATION_PERIOD_EXCEEDED,
                                                        decision.getMailingId(),
                                                        mailingType
                                                ));
                                            }
                                        } else {
                                            errors.add(new MailingTrackingUsageError(
                                                MailingTrackingUsageErrorType.MAILING_TRACKING_DISABLED,
                                                decision.getMailingId(),
                                                mailingType
                                            ));
                                        }
                                    }
                                } else {
                                    errors.add(new MailingTrackingUsageError(
                                            MailingTrackingUsageErrorType.DECISION_MAILING_INVALID,
                                            decision.getMailingId(),
                                            mailingType,
                                            (String) baseMailing.get("shortname")
                                    ));
                                }
                            }
                        }
                    } else {
                        logger.debug("Unable to calculate date for decision icon #{}", decision.getId());
                    }
                }
            }
        }

        return errors;
    }

    public void validateDeadlineBeforeDecision(List<WorkflowIcon> icons, Popups popups) {
        if (anyDecisionIconWithReactionCriteriaMissesDelay(new WorkflowGraph(icons))) {
            popups.warning("warning.workflow.decision.delay");
        }
    }

    private static boolean anyDecisionIconWithReactionCriteriaMissesDelay(WorkflowGraph graph) {
        return graph.getAllNodesByTypes(List.of(WorkflowIconType.Constants.DECISION_ID)).stream()
                .map(decisionNode -> (WorkflowDecision) decisionNode.getNodeIcon())
                .filter(WorkflowDecision::hasReactionCriteria)
                .anyMatch(decisionIcon -> !containsDeadlineBetweenMailingAndDecision(decisionIcon, graph));
    }

    public boolean missingRecipientsLoadInSplitBranches(WorkflowGraph graph) {
        return graph.getAllNodesByType(WorkflowIconType.Constants.SPLIT_ID).stream()
                .map(WorkflowNode::getNodeIcon)
                .anyMatch(splitIcon -> missingRecipientsLoadInSplitBranches(graph, splitIcon));
    }

    private static boolean missingRecipientsLoadInSplitBranches(WorkflowGraph graph, WorkflowIcon splitIcon) {
        return graph.getAllNextParallelIconsByType(splitIcon, List.of(WorkflowIconType.Constants.PARAMETER_ID), emptyList(), false)
                .stream()
                .anyMatch(parameterIcon -> missingRecipientsLoadInParameterIconChain(graph, parameterIcon));
    }

    private static boolean missingRecipientsLoadInParameterIconChain(WorkflowGraph graph, WorkflowIcon parameterIcon) {
        return graph.getNextIconByType(parameterIcon, MAILING_ICON_TYPE_IDS, emptyList(), false) == null;
    }

    private static boolean containsDeadlineBetweenMailingAndDecision(WorkflowDecision decisionIcon, WorkflowGraph graph) {
        return CollectionUtils.isEmpty(graph.getAllNextParallelIconsByType(decisionIcon,
                WorkflowActivationService.ALL_MAILING_TYPES,
                List.of(WorkflowIconType.DEADLINE.getId()),
                true));
    }

    private MailingTrackingUsageError errorMailingDisordered(WorkflowMailingAware icon, int companyId) {
        Map<String, Object> data = mailingDao.getMailingWithWorkStatus(icon.getMailingId(), companyId);

        return new MailingTrackingUsageError(
                MailingTrackingUsageErrorType.DECISION_MAILING_DISORDERED,
                icon.getMailingId(),
                MailingType.getByCode(((Number) data.get("mailing_type")).intValue()),
                (String) data.get("shortname")
        );
    }

    private boolean isHasNotActionMailingAfter(WorkflowIcon icon, WorkflowGraph workflowGraph){
        final Set<Integer> notActionBasedMailings = new HashSet<>(Arrays.asList(
                WorkflowIconType.MAILING.getId(),
                WorkflowIconType.DATE_BASED_MAILING.getId(),
                WorkflowIconType.FOLLOWUP_MAILING.getId()
        ));
        return workflowGraph.getAllNextParallelIconsByType(icon, notActionBasedMailings, new ArrayList<>(), false).isEmpty();
    }

    /**
     * Check whether a mailtracking is enabled (if it's needed) and a maximum possible time period
     * between followed mailing and following one doesn't exceeds {@code trackingDays}.
     * @param icons workflow icons to process.
     * @param trackingDays mailtracking data expiration period (or 0 when a mailtracking is disabled).
     */
    public List<MailingTrackingUsageError> checkMailingTrackingUsage(List<WorkflowIcon> icons, int trackingDays) {
        List<MailingTrackingUsageError> errors = new ArrayList<>();
        WorkflowGraph graph = new WorkflowGraph();

        if (!graph.build(icons)) {
            return errors;
        }

        final Set<Integer> mailingTypes = new HashSet<>(Arrays.asList(
                WorkflowIconType.MAILING.getId(),
                WorkflowIconType.ACTION_BASED_MAILING.getId(),
                WorkflowIconType.DATE_BASED_MAILING.getId(),
                WorkflowIconType.FOLLOWUP_MAILING.getId()
        ));

        for (WorkflowNode mailingNode : graph.getAllNodesByTypes(mailingTypes)) {
            WorkflowMailingAware mailingIcon = (WorkflowMailingAware) mailingNode.getNodeIcon();

            // Find all available incoming chains for this mailing icon
            List<List<WorkflowNode>> chains = workflowService.getChains(mailingIcon, icons, false);

            for (List<WorkflowNode> chain : chains) {
                boolean valid = true;
                boolean isLeadMailingNormal = isNormalMailing(mailingIcon);

                Date mailingDate = workflowService.getChainDate(chain);
                if (mailingDate == null) {
                    continue;
                }

                // Skip mailing node itself (it's first in the chain)
                for (int i = 1; i < chain.size(); i++) {
                    WorkflowIcon icon = chain.get(i).getNodeIcon();

                    // A decision icon is treated as a termination one if it doesn't use a mailtracking data
                    if (icon.getType() == WorkflowIconType.DECISION.getId()) {
                        WorkflowDecision decision = (WorkflowDecision) icon;

                        if (icon.isFilled()) {
                            if (decision.getDecisionType() == WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION) {
                                // Auto optimization doesn't use a mailtracking data
                                break;
                            }
                            // Decision (reaction, yes-path only) doesn't use a mailtracking data
                            if (decision.getDecisionType() == WorkflowDecisionType.TYPE_DECISION &&
                                    decision.getDecisionCriteria() == WorkflowDecisionCriteria.DECISION_REACTION &&
                                    chain.get(i - 1) == graph.getDecisionYesBranch(chain.get(i))) {
                                // Whether the chain goes through decision's positive connection (yes-path)
                                break;
                            }
                        }
                    } else if (WorkflowUtils.isMailingIcon(icon)) {
                        WorkflowMailingAware followedMailingIcon = (WorkflowMailingAware) icon;

                        if (trackingDays > 0) {
                            Date followedMailingDate = workflowService.getChainDate(chain, icon);
                            if (followedMailingDate == null) {
                                break;
                            }

                            Calendar expirationDate = Calendar.getInstance();
                            expirationDate.setTime(followedMailingDate);
                            expirationDate.add(Calendar.DAY_OF_YEAR, trackingDays);

                            if (mailingDate.after(expirationDate.getTime())) {
                                valid = false;
                                errors.add(new MailingTrackingUsageError(
                                        MailingTrackingUsageErrorType.EXPIRATION_PERIOD_EXCEEDED,
                                        followedMailingIcon.getMailingId()
                                ));
                            }
                        } else if(!isLeadMailingNormal || !isNormalMailing(icon)){
                            //Checks if mailtracking is disabled only normal mailings can be in queue

                            valid = false;
                            errors.add(new MailingTrackingUsageError(
                                    MailingTrackingUsageErrorType.MAILING_TRACKING_DISABLED,
                                    mailingIcon.getMailingId()
                            ));
                        }
                        break;
                    }
                }

                // There's no sense to show multiple errors related to the same mailing icon
                if (!valid) {
                    // Skip other chains starting with this mailing icon
                    break;
                }
            }
        }

        return errors;
    }

    public boolean parametersSumNotHigher100(List<WorkflowIcon> icons) {
        // Maps iconId -> parameterValue (only parameter icons).
        Map<Integer, Integer> parameterIconValueMap = new HashMap<>();

        for (WorkflowIcon icon : icons) {
            // Exclude unfilled parameter icons.
            if (WorkflowIconType.PARAMETER.getId() == icon.getType() && icon.isFilled()) {
                parameterIconValueMap.put(icon.getId(), ((WorkflowParameter) icon).getValue());
            }
        }

        for (WorkflowIcon icon : icons) {
            if (icon.getType() != WorkflowIconType.START.getId() && icon.getType() != WorkflowIconType.STOP.getId()) {
                List<WorkflowConnection> connections = icon.getConnections();

                if (CollectionUtils.isNotEmpty(connections)) {
                    int sum = 0;

                    for (WorkflowConnection connection : connections) {
                        Integer value = parameterIconValueMap.get(connection.getTargetIconId());

                        if (value != null) {
                            sum += value;
                        }
                    }

                    if (sum > 100) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean checkHasDateInThePast(WorkflowStartStop icon, TimeZone timezone) {
        Date date = WorkflowUtils.getStartStopIconDate(icon, timezone);
        if (date == null) {
            return false;
        }
        return DateUtilities.isPast(date);
    }

    private int compareDates(WorkflowStartStop icon1, WorkflowStartStop icon2, TimeZone timezone) {
        Date date1 = WorkflowUtils.getStartStopIconDate(icon1, timezone);
        Date date2 = WorkflowUtils.getStartStopIconDate(icon2, timezone);

        if (date1 == null || date2 == null) {
            return -1;
        }

        return date1.getTime() > date2.getTime() ? 1 : -1;
    }

    public boolean moreThanOneStartPresented(List<WorkflowIcon> icons) {
        WorkflowGraph graph = new WorkflowGraph();
        if (!graph.build(icons)) {
            return false;
        }
        return graph.getAllNodesByType(WorkflowIconType.START.getId()).size() > 1;
    }

    public boolean noLoops(List<WorkflowIcon> icons) {
        WorkflowGraph workflowGraph = new WorkflowGraph();

        if (workflowGraph.build(icons)) {
            return noLoops(workflowGraph);
        }

        return true;
    }

    private boolean noLoops(WorkflowGraph graph) {
        List<WorkflowNode> starts = graph.getAllNodesByType(WorkflowIconType.START.getId());
        int countStarts = starts.size();
        if (countStarts < 1) {
            return true;
        }

        for (WorkflowNode start : starts) {
            List<Integer> sawingNodes = new ArrayList<>();
            if (searchLoops(sawingNodes, start)) {
                return false;
            }
        }

        return true;
    }

    public boolean noMailingsBeforeRecipient(List<WorkflowIcon> icons) {
        WorkflowGraph graph = new WorkflowGraph();

        if (!graph.build(icons)) {
            return true;
        }

        List<WorkflowNode> mailings = graph.getAllNodesByTypes(Arrays.asList(
                WorkflowIconType.MAILING.getId(),
                WorkflowIconType.FOLLOWUP_MAILING.getId(),
                WorkflowIconType.ACTION_BASED_MAILING.getId(),
                WorkflowIconType.DATE_BASED_MAILING.getId()
        ));

        for (WorkflowNode mailing : mailings) {
            if (checkHasNextRecipient(mailing)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkHasNextRecipient(WorkflowNode node) {
        List<WorkflowNode> nextNodes = node.getNextNodes();
        int nextNodesSize = nextNodes.size();
        for (int i = 0; i < nextNodesSize; i++) {
            WorkflowNode nextNode = nextNodes.get(i);
            WorkflowIcon nextNodeIcon = nextNode.getNodeIcon();
            if (nextNodeIcon.getType() == WorkflowIconType.RECIPIENT.getId()) {
                return true;
            }

            node.deleteNextNode(nextNode);
            nextNode.deletePrevNode(node);
            i--;

            if (checkHasNextRecipient(nextNode)) {
                return true;
            }

            nextNodesSize = nextNodes.size();
        }

        return false;
    }

    private boolean searchLoops(List<Integer> sawingNodes, WorkflowNode node) {
        boolean hasLoops;

        List<WorkflowNode> nextNodes = node.getNextNodes();
        int nextNodesSize = nextNodes.size();
        for (int i = 0; i < nextNodesSize; i++) {
            WorkflowNode nextNode = nextNodes.get(i);
            WorkflowIcon nextNodeIcon = nextNode.getNodeIcon();
            if (sawingNodes.contains(nextNodeIcon.getId())) {
                return true;
            }

            sawingNodes.add(nextNodeIcon.getId());
            node.deleteNextNode(nextNode);
            nextNode.deletePrevNode(node);
            i--;
            hasLoops = searchLoops(sawingNodes, nextNode);
            sawingNodes.remove(Integer.valueOf(nextNodeIcon.getId()));
            if (hasLoops) {
                return true;
            }

            nextNodesSize = nextNodes.size();
        }
        return false;
    }

    public boolean isFixedDeadlineUsageCorrect(List<WorkflowIcon> workflowIcons) {
        boolean usedFixedDeadline = false;
        boolean usedActionBasedOrDateBasedMailing = false;

        for (WorkflowIcon icon : workflowIcons) {
            switch (icon.getType()) {
                case WorkflowIconType.Constants.ACTION_BASED_MAILING_ID:
                case WorkflowIconType.Constants.DATE_BASED_MAILING_ID:
                    usedActionBasedOrDateBasedMailing = true;
                    if (usedFixedDeadline) {
                        return false;
                    }
                    break;

                case WorkflowIconType.Constants.DEADLINE_ID:
                    if (icon.isFilled()) {
                        WorkflowDeadline deadlineIcon = (WorkflowDeadline) icon;
                        if (deadlineIcon.getDeadlineType() == WorkflowDeadlineType.TYPE_FIXED_DEADLINE) {
                            usedFixedDeadline = true;
                            if (usedActionBasedOrDateBasedMailing) {
                                return false;
                            }
                        }
                    }
                    break;
                    
				default:
					break;
            }
        }

        return true;
    }

    public boolean isDeadlineDateCorrect(List<WorkflowIcon> icons, TimeZone timezone) {
        WorkflowStart start = getLatestStartDate(icons, timezone);

        if (start != null) {
            Date maxStartDate = WorkflowUtils.getStartStopIconDate(start, timezone);

            if (maxStartDate == null) {
                return true;
            }

            for (WorkflowIcon icon : icons) {
                if (icon.isFilled() && WorkflowIconType.fromId(icon.getType()) == WorkflowIconType.DEADLINE) {
                    WorkflowDeadline deadline = (WorkflowDeadline) icon;
                    if (deadline.getDeadlineType() == WorkflowDeadlineType.TYPE_FIXED_DEADLINE) {
                        Date deadlineDate = WorkflowUtils.mergeIconDateAndTime(deadline.getDate(), deadline.getHour(), deadline.getMinute(), timezone);
                        if (deadlineDate.before(maxStartDate)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public boolean mailingHasOnlyOneRecipient(List<WorkflowIcon> icons) {
        WorkflowGraph graph = new WorkflowGraph();

        if (!graph.build(icons)) {
            return true;
        }

        List<Integer> mailingTypes = Arrays.asList(WorkflowIconType.MAILING.getId(),
                WorkflowIconType.ACTION_BASED_MAILING.getId(),
                WorkflowIconType.DATE_BASED_MAILING.getId(),
                WorkflowIconType.FOLLOWUP_MAILING.getId());

        List<WorkflowIcon> mailingIcons = graph.getAllNodesByTypes(mailingTypes)
                .stream()
                .map(WorkflowNode::getNodeIcon)
                .collect(Collectors.toList());

        for (WorkflowIcon mailingIcon : mailingIcons) {
            WorkflowNode mailingNode = graph.getNodeByIcon(mailingIcon);
            if (countRecipientsIcon(mailingNode) > 1) {
                return false;
            }

            // We need rebuild this graph since countRecipientsIcon method deletes all the links it follows
            graph.build(icons);
        }

        return true;
    }

    public boolean campaignHasOnlyOneMailingList(List<WorkflowIcon> workflowIcons) {
        int mailinglistId = getMailingListId(workflowIcons);

        if (mailinglistId > 0) {
            return checkMailinglistId(workflowIcons, mailinglistId);
        }

        return true;
    }

    private boolean checkMailinglistId(List<WorkflowIcon> workflowIcons, int mailinglistId) {
        for (WorkflowIcon icon : workflowIcons) {
            if (icon.getType() == WorkflowIconType.RECIPIENT.getId()) {
                WorkflowRecipient recipient = (WorkflowRecipient) icon;
                if (recipient.getMailinglistId() > 0 && recipient.getMailinglistId() != mailinglistId) {
                    return false;
                }
            }
        }
        return true;
    }

    private int countRecipientsIcon(WorkflowNode node) {
        Set<Integer> countedIcons = new HashSet<>();
        return countRecipientsIcon(node, countedIcons);
    }

    private int countRecipientsIcon(WorkflowNode node, Set<Integer> countedIcons) {
        int recipientsSize = 0;
        List<WorkflowNode> prevNodes = node.getPrevNodes();
        int prevNodesSize = prevNodes.size();
        for (int i = 0; i < prevNodesSize; i++) {
            WorkflowNode prevNode = prevNodes.get(i);
            WorkflowIcon prevNodeIcon = prevNode.getNodeIcon();
            if (prevNodeIcon.getType() == WorkflowIconType.RECIPIENT.getId()) {
                if (!countedIcons.contains(prevNodeIcon.getId())) {
                    recipientsSize++;
                    countedIcons.add(prevNodeIcon.getId());
                }
            } else {
                node.deletePrevNode(prevNode);
                prevNode.deleteNextNode(node);
                i--;
                recipientsSize += countRecipientsIcon(prevNode, countedIcons);
                prevNodesSize = prevNodes.size();
            }
        }
        return recipientsSize;
    }

    public int getMailingListId(List<WorkflowIcon> workflowIcons) {
        for (WorkflowIcon icon : workflowIcons) {
            if (icon.getType() == WorkflowIconType.RECIPIENT.getId()) {
                WorkflowRecipient recipient = (WorkflowRecipient) icon;
                if (recipient.isFilled() && recipient.getMailinglistId() > 0) {
                    return recipient.getMailinglistId();
                }
            }
        }
        return 0;
    }

    public boolean campaignHasOnlyMailingsAssignedToThisWorkflow(List<WorkflowIcon> icons, int companyId, int workflowId) {
        for (WorkflowIcon icon : icons) {
            int mailingId = WorkflowUtils.getMailingId(icon);
            if (mailingId > 0) {
                List<Integer> workflowIds = workflowService.getWorkflowIdsByAssignedMailingId(companyId, mailingId);

                if (containsUnexpectedIds(workflowIds, workflowId)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean containsUnexpectedIds(List<Integer> ids, int expectedId) {
        for (int id : ids) {
            if (expectedId != id) {
                return true;
            }
        }
        return false;
    }

    public boolean isImportIsNotActive(List<WorkflowIcon> icons, int companyId) {
        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.IMPORT.getId()) {
                WorkflowImport importIcon = (WorkflowImport) icon;
                if (importIcon.getImportexportId() > 0) {
                    if (autoImportService != null) {
                        AutoImport autoImport = autoImportService.getAutoImport(importIcon.getImportexportId(), companyId);
                        if (autoImport.isActive()) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public boolean isExportIsNotActive(List<WorkflowIcon> icons, int companyId) {
        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.EXPORT.getId()) {
                WorkflowExport exportIcon = (WorkflowExport) icon;
                if (exportIcon.getImportexportId() > 0) {
                    if (autoExportService != null) {
                        AutoExport autoExport = autoExportService.getAutoExport(exportIcon.getImportexportId(), companyId);
                        if (autoExport.isActive()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private List<MailingTrackingUsageError> checkFollowupMailing(List<WorkflowIcon> workflowIcons, int companyId, int trackingDays) {
        List<MailingTrackingUsageError> errors = new ArrayList<>();
        List<WorkflowFollowupMailing> followupMailingIcons = workflowService.getFollowupMailingIcon(workflowIcons);
        Map<Integer, WorkflowMailingAware> workflowMailings = new HashMap<>();
        workflowIcons.forEach(icon -> {
            if (icon.getType() == WorkflowIconType.MAILING.getId() || icon.getType() == WorkflowIconType.ACTION_BASED_MAILING.getId() || icon.getType() == WorkflowIconType.DATE_BASED_MAILING.getId()) {
                WorkflowMailingAware wMailing = (WorkflowMailingAware) icon;
                workflowMailings.put(wMailing.getMailingId(), wMailing);
            }
        });

        for (WorkflowFollowupMailing icon : followupMailingIcons) {
            if (!icon.isFilled()) {
                continue;
            }

            Map<String, Object> baseMailing = mailingDao.getMailingWithWorkStatus(icon.getBaseMailingId(), companyId);
            String baseStatus = (String) baseMailing.get("work_status");
            Date baseSendDate = (Date) baseMailing.get("senddate");
            if (workflowMailings.containsKey(icon.getBaseMailingId()) && baseSendDate == null) {
                baseSendDate = workflowService.getMaxPossibleDate(workflowMailings.get(icon.getBaseMailingId()), workflowIcons);
            }
            Date followSendDate = workflowService.getMaxPossibleDate(icon, workflowIcons);

            if (FOLLOWED_MAILING_STATUSES.contains(baseStatus) || workflowMailings.containsKey(icon.getBaseMailingId())) {
                if (followSendDate != null && baseSendDate != null) {
                    if (followSendDate.before(baseSendDate)) {
                        errors.add(new MailingTrackingUsageError(
                                MailingTrackingUsageErrorType.BASE_MAILING_DISORDERED,
                                icon.getMailingId(),
                                MailingType.getByCode(((Number) baseMailing.get("mailing_type")).intValue()),
                                (String)baseMailing.get("shortname")
                        ));
                    } else {
                        if (trackingDays > 0) {
                            Calendar expirationDate = Calendar.getInstance();
                            expirationDate.setTime(baseSendDate);
                            expirationDate.add(Calendar.DAY_OF_YEAR, trackingDays);
                            if (followSendDate.after(expirationDate.getTime())) {
                                errors.add(new MailingTrackingUsageError(
                                        MailingTrackingUsageErrorType.EXPIRATION_PERIOD_EXCEEDED,
                                        icon.getMailingId()
                                ));
                            }
                        } else {
                            errors.add(new MailingTrackingUsageError(
                                    MailingTrackingUsageErrorType.MAILING_TRACKING_DISABLED,
                                    icon.getMailingId()
                            ));
                        }
                    }
                } else {
                    if (baseSendDate == null) {
                        logger.debug("Unable to calculate send date for followup mailing icon #{} (mailingId: {})", icon.getId(), icon.getBaseMailingId());
                    }
                    if (followSendDate == null) {
                        logger.debug("Unable to calculate send date for base mailing (mailingId: {})", icon.getBaseMailingId());
                    }
                }
            } else {
                errors.add(new MailingTrackingUsageError(
                        MailingTrackingUsageErrorType.BASE_MAILING_NOT_FOUND,
                        icon.getMailingId()
                ));
            }
        }
        return errors;
    }

    public List<String> checkTrackableProfileFields(List<WorkflowIcon> icons, int companyId) {
        Set<String> columns = new HashSet<>();

        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.START.getId()) {
                WorkflowStart start = (WorkflowStart) icon;

                if (WorkflowUtils.is(start, WorkflowReactionType.CHANGE_OF_PROFILE)) {
                    columns.add(start.getProfileField());
                }
            }
        }

        return columns.stream()
                .filter(column -> !profileFieldDao.isTrackableColumn(column, companyId))
                .collect(Collectors.toList());
    }

    public List<String> checkProfileFieldsUsedInConditions(List<WorkflowIcon> icons, int companyId) {
        Set<String> columns = new HashSet<>();

        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.START.getId()) {
                WorkflowStart start = (WorkflowStart) icon;

                if (WorkflowUtils.is(start, WorkflowStartEventType.EVENT_DATE)) {
                    columns.add(start.getDateProfileField());
                }
            } else if (icon.getType() == WorkflowIconType.DECISION.getId()) {
                WorkflowDecision decision = (WorkflowDecision) icon;

                if (WorkflowDecisionType.TYPE_DECISION == decision.getDecisionType()) {
                    if (WorkflowDecisionCriteria.DECISION_PROFILE_FIELD == decision.getDecisionCriteria()) {
                        columns.add(decision.getProfileField());
                    }
                }
            }
        }

        return columns.stream()
                .filter(column -> !profileFieldDao.exists(column, companyId))
                .collect(Collectors.toList());
    }

    public List<Message> validateDecisionReaction(WorkflowDecision decision, int companyId) {
        List<Message> messages = new ArrayList<>();
        if (WorkflowReactionType.CLICKED_LINK == decision.getReaction() && decision.getLinkId() > 0) {
            validateClickedLink(companyId, messages, decision.getLinkId());
        }
        return messages;
    }
    
    
    public List<Message> validateDecisionRules(WorkflowDecision decision, int companyId) {
        // checking is field exist in recipient table
        DbColumnType columnType = profileFieldDao.getColumnType(companyId, decision.getProfileField());
        List<Message> messages = new ArrayList<>();

        if (columnType == null) {
            messages.add(Message.of("error.workflow.field.missing", decision.getProfileField()));
            return messages;
        }

        SimpleDataType simpleColumnType = columnType.getSimpleDataType();

        for (WorkflowRule rule : decision.getRules()) {
            final int operatorCode = rule.getPrimaryOperator();
            final ConditionalOperator operator = ConditionalOperator.fromOperatorCode(operatorCode).orElse(null);
            final String operatorReadableName = Objects.nonNull(operator) ? operator.getEqlSymbol() : operatorCode + " code";

            // checking if special handling operator
            if (operator == YES || operator == NO) {
                // stop validation for special handling
                continue;
            }

            if (operator != null && isOperatorApplicable(simpleColumnType, operator)) {
                String value = rule.getPrimaryValue();

                if (operator == IS) {
                    if (!NULL_VALUE.equalsIgnoreCase(value) && !NOT_NULL_VALUE.equalsIgnoreCase(value)) {
                        messages.add(Message.of("error.workflow.value.operator", value, operatorReadableName));
                    }
                } else {
                    if (simpleColumnType == SimpleDataType.Numeric || simpleColumnType == SimpleDataType.Float) {
                        if (!AgnUtils.isDouble(value)) {
                            messages.add(Message.of("error.workflow.value.type", value, simpleColumnType));
                        }
                    } else if (simpleColumnType == SimpleDataType.Date || simpleColumnType == SimpleDataType.DateTime) {
                        String dateFormat = ALLOWED_DATE_FORMATS.get(StringUtils.lowerCase(decision.getDateFormat()));
                        if (!validateDateRuleExpression(value, dateFormat)) {
                            messages.add(Message.of("error.workflow.value.type", value, simpleColumnType));
                        }
                    }
                }
            } else {
                messages.add(Message.of("error.workflow.operator", operatorReadableName, simpleColumnType));
            }
        }

        return messages;
    }

    public Set<Integer> getInvalidTargetGroups(final int companyId, final List<WorkflowIcon> icons) {
        final Set<Integer> mailingIds = new HashSet<>();

        for (WorkflowIcon icon : icons) {
            if (WorkflowUtils.isMailingIcon(icon) || WorkflowUtils.isBranchingDecisionIcon(icon)) {
                int mailingId = ((WorkflowMailingAware)icon).getMailingId();
                if(mailingId > 0){
                    mailingIds.add(mailingId);
                }
            }
        }

        final Map<Integer, String> targetExpressions = mailingDao.getTargetExpressions(companyId, mailingIds);
        final Map<Integer, Set<Integer>> contentTargets = mailingDao.getTargetsUsedInContent(companyId, mailingIds);

        final Set<Integer> targets = new HashSet<>();
        targetExpressions.values().stream().map(TargetExpressionUtils::getTargetIds).forEach(targets::addAll);
        contentTargets.values().forEach(targets::addAll);

        return targetService.getInvalidTargets(companyId, targets);
    }

    // Make sure that given operator is applicable to data type that selected column belongs to.
    private boolean isOperatorApplicable(SimpleDataType columnType, ConditionalOperator operator) {
        return ALLOWED_OPERATORS_BY_TYPE.get(columnType).contains(operator.getOperatorCode());
    }

    private boolean isNormalMailing(WorkflowIcon icon){
        return icon.getType() == WorkflowIconType.MAILING.getId();
    }

    private List<WorkflowNode> expectSiblings(WorkflowNode node, boolean forward, int countMin, int countMax, WorkflowIconType... types) throws WorkflowStructureException {
        if (countMin > countMax) {
            throw new IllegalArgumentException("countMin > countMax");
        }

        if (countMin < 0) {
            throw new IllegalArgumentException("countMin < 0");
        }

        List<WorkflowNode> siblings = forward ? node.getNextNodes() : node.getPrevNodes();

        if (siblings.size() < countMin || siblings.size() > countMax) {
            throw new WorkflowStructureException(String.format("Invalid number of siblings: %d (expected: [%d; %d])", siblings.size(), countMin, countMax));
        }

        if (types.length > 0) {
            for (WorkflowNode sibling : siblings) {
                expectType(sibling, types);
            }
        }

        return siblings;
    }

    private WorkflowNode expectOneSibling(WorkflowNode node, boolean forward, WorkflowIconType type) throws WorkflowStructureException {
        return expectSiblings(node, forward, 1, 1, type).get(0);
    }

    private void expectType(WorkflowNode node, WorkflowIconType... types) throws WorkflowStructureException {
        if (types.length == 0) {
            throw new IllegalArgumentException("types.length == 0");
        }

        int type = node.getNodeIcon().getType();

        for (WorkflowIconType iconType : types) {
            if (type == iconType.getId()) {
                return;
            }
        }

        throw new WorkflowStructureException("Unexpected icon type : " + WorkflowIconType.fromId(type));
    }

    public boolean containNotAllowedPauseChanges(List<WorkflowIcon> oldIcons, List<WorkflowIcon> newIcons) {
        if (oldIcons.size() != newIcons.size()) {
            return true;
        }
        for (int i = 0; i < oldIcons.size(); i++) {
            WorkflowIcon oldIcon = oldIcons.get(i);
            WorkflowIcon newIcon = newIcons.get(i);
            if (!oldIcon.getConnections().equals(newIcon.getConnections())) {
                return true;
            }
            if (!oldIcon.equalsIgnoreI18n(newIcon) && !isMailingOrStopIconWithNotChangedType(oldIcon, newIcon)) {
                return true; // Only mailing and stop icons can be changed in a paused campaign
            }
        }
        return false;
    }

    private boolean isMailingOrStopIconWithNotChangedType(WorkflowIcon oldIcon, WorkflowIcon newIcon) {
        return (WorkflowUtils.isMailingIcon(oldIcon) && WorkflowUtils.isMailingIcon(newIcon))
                || (WorkflowUtils.isStopIcon(oldIcon) && WorkflowUtils.isStopIcon(newIcon));
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setAutoImportService(AutoImportService autoImportService) {
        this.autoImportService = autoImportService;
    }

    public void setAutoExportService(AutoExportService autoExportService) {
        this.autoExportService = autoExportService;
    }

    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    public void setMaildropStatusDao(MaildropStatusDao maildropStatusDao) {
        this.maildropStatusDao = maildropStatusDao;
    }

    public void setProfileFieldDao(ProfileFieldDao profileFieldDao) {
        this.profileFieldDao = profileFieldDao;
    }

    public void setMailingSendService(MailingSendService mailingSendService) {
        this.mailingSendService = mailingSendService;
    }

    public void setMailingService(MailingService mailingService) {
        this.mailingService = mailingService;
    }

    public void setTargetService(TargetService targetService) {
        this.targetService = targetService;
    }

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    public void setTrackableLinkService(TrackableLinkService trackableLinkService) {
        this.trackableLinkService = trackableLinkService;
    }
}
