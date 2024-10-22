/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.IntEnum;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.TrackableLink;
import com.agnitas.dao.CampaignDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.dashboard.bean.DashboardWorkflow;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.MailingDeliveryBlockingService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingException;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.reminder.service.ComReminderService;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.workflow.beans.ComWorkflowReaction;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus;
import com.agnitas.emm.core.workflow.beans.WorkflowArchive;
import com.agnitas.emm.core.workflow.beans.WorkflowDeadline;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowExport;
import com.agnitas.emm.core.workflow.beans.WorkflowFollowupMailing;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowImport;
import com.agnitas.emm.core.workflow.beans.WorkflowMailing;
import com.agnitas.emm.core.workflow.beans.WorkflowMailingAware;
import com.agnitas.emm.core.workflow.beans.WorkflowParameter;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionStep;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionStepDeclaration;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.beans.WorkflowRecipient;
import com.agnitas.emm.core.workflow.beans.WorkflowReminder;
import com.agnitas.emm.core.workflow.beans.WorkflowRule;
import com.agnitas.emm.core.workflow.beans.WorkflowStart;
import com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartEventType;
import com.agnitas.emm.core.workflow.beans.WorkflowStartStop;
import com.agnitas.emm.core.workflow.beans.WorkflowStop;
import com.agnitas.emm.core.workflow.beans.WorkflowStop.WorkflowEndType;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowConnectionImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowRecipientImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowStartImpl;
import com.agnitas.emm.core.workflow.dao.ComWorkflowDao;
import com.agnitas.emm.core.workflow.dao.ComWorkflowReactionDao;
import com.agnitas.emm.core.workflow.dao.ComWorkflowStartStopReminderDao;
import com.agnitas.emm.core.workflow.dao.ComWorkflowStartStopReminderDao.ReminderType;
import com.agnitas.emm.core.workflow.graph.WorkflowGraph;
import com.agnitas.emm.core.workflow.graph.WorkflowNode;
import com.agnitas.emm.core.workflow.service.ChangingWorkflowStatusResult;
import com.agnitas.emm.core.workflow.service.ComWorkflowActivationService;
import com.agnitas.emm.core.workflow.service.ComWorkflowDataParser;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.emm.core.workflow.service.ComWorkflowValidationService;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils.Deadline;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils.StartType;
import com.agnitas.emm.core.workflow.web.forms.WorkflowForm;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.beans.impl.AutoOptimizationStatus;
import com.agnitas.mailing.autooptimization.service.ComOptimizationCommonService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import com.agnitas.messages.Message;
import com.agnitas.reporting.birt.external.dao.ComCompanyDao;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.userform.bean.UserForm;
import jakarta.mail.internet.InternetAddress;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.beans.impl.MaildropDeleteException;
import org.agnitas.dao.MaildropStatusDao;
import org.agnitas.dao.MailingStatus;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoexport.service.AutoExportService;
import org.agnitas.emm.core.autoimport.bean.AutoImport;
import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDaoException;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.EmmCalendar;
import org.agnitas.util.SafeString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.agnitas.emm.core.workflow.beans.WorkflowDependencyType.MAILING_DELIVERY;
import static com.agnitas.emm.core.workflow.beans.WorkflowDependencyType.MAILING_LINK;
import static com.agnitas.emm.core.workflow.beans.WorkflowDependencyType.MAILING_REFERENCE;
import static com.agnitas.emm.core.workflow.service.util.WorkflowUtils.TESTING_MODE_DEADLINE;
import static com.agnitas.emm.core.workflow.service.util.WorkflowUtils.TESTING_MODE_DEADLINE_DURATION;
import static com.agnitas.emm.core.workflow.service.util.WorkflowUtils.asDeadline;
import static com.agnitas.emm.core.workflow.service.util.WorkflowUtils.isUnpausing;
import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;
import static org.agnitas.util.Const.Mvc.ERROR_MSG;

public class ComWorkflowServiceImpl implements ComWorkflowService {

    //how many hours we will wait
    private static final int DELAY_FOR_SENDING_MAILING = 12; //hours

    private static final Logger logger = LogManager.getLogger(ComWorkflowServiceImpl.class);

	private ColumnInfoService columnInfoService;
    private ComWorkflowValidationService workflowValidationService;
    private AutoImportService autoImportService;
    private AutoExportService autoExportService;
    private MailingDeliveryBlockingService mailingDeliveryBlockingService;
    private MaildropStatusDao maildropStatusDao;

    private ProfileFieldDao profileFieldDao;
	protected ComWorkflowDao workflowDao;
	private MailingDao mailingDao;
	private ComTargetDao targetDao;
    private ComTargetService targetService;
    private UserFormDao userFormDao;
    private ComCompanyDao birtCompanyDao;
	private ComWorkflowReactionDao reactionDao;
	private ComWorkflowStartStopReminderDao reminderDao;
    private ComOptimizationService optimizationService;
    private ComOptimizationCommonService optimizationCommonService;
    private SendActionbasedMailingService sendActionbasedMailingService;
    private ComReminderService reminderService;
    private ComWorkflowDataParser workflowDataParser;
    private CampaignDao campaignDao;
    private MediatypesDao mediatypesDao;
    protected AdminService adminService;
    private MailingService mailingService;
    private ComWorkflowActivationService workflowActivationService;
    private BulkActionValidationService<Integer, Workflow> bulkActionValidationService;

    private ComWorkflowService selfReference;

    @Override
	public void saveWorkflow(Admin admin, Workflow workflow, List<WorkflowIcon> icons) {
        selfReference.saveWorkflow(admin, workflow, icons, false);
    }

    @Override
    @Transactional
    public void saveWorkflow(Admin admin, Workflow workflow, List<WorkflowIcon> icons, boolean duringPause) {
        TimeZone timezone = TimeZone.getTimeZone(admin.getAdminTimezone());

        // Make some changes to mailing entities that are not used in the workflow anymore.
        releaseObsoleteMailings(workflow.getWorkflowId(), admin.getCompanyID(), icons);
        // Assign senderAdminId to some icons (if applicable).
        setSenderAdminId(icons, admin.getAdminID());

        // Check mailing types and make sure they're compatible with start icon (change mailing types if required).
        setCompatibleMailingTypes(icons);

        // Serialize the workflow structure (schema).
        workflow.setWorkflowSchema(workflowDataParser.serializeWorkflowIcons(icons));
        workflow.setWorkflowIcons(new ArrayList<>(icons));

        // Calculate derived data based on workflow structure.
        calculateDerivedData(workflow, timezone);
        // Store workflow and its structure (schema).
        saveWorkflow(workflow);

        // Generate and store (if required) workflow dependencies list.
        saveDependencies(admin.getCompanyID(), workflow.getWorkflowId(), icons, duringPause);

        // Make some changes to external entities (mailings, auto-imports, auto-exports, etc) used in the workflow
        // (apply some changes defined by the workflow structure).
        updateEntities(workflow, admin);
    }

    /**
     * Generate and store workflow dependencies list — must be easily available to other EMM components without parsing
     * workflow schema. Managed and referenced mailings, archives, user forms, target groups, etc.
     * In case when workflow paused we still need to preserve origin dependencies that was stored during activation
     * so they can remain untouched. For example mailing id=1 can be changed to mailing id=2 during pause. But after
     * pause timeout expired origin id=1 will be restored. So we need to save id=1 and id=2 during pause in order to
     * protect both dependencies from deletion.
     *
     * @param companyId an admin who saves a workflow.
     * @param workflowId an identifier of a workflow to collect dependencies list for.
     * @param icons icons of a workflow to collect dependencies from.
     * @param duringPause whether or not dependencies saved during pause
     */
    private void saveDependencies(int companyId, int workflowId, List<WorkflowIcon> icons, boolean duringPause) {
        Set<WorkflowDependency> dependencies = getDependenciesFromIcons(icons);
        if (duringPause) {
            dependencies.addAll(getDependenciesBeforePause(companyId, workflowId));
        }
        workflowDao.setDependencies(companyId, workflowId, dependencies, false);
    }

    private Set<WorkflowDependency> getDependenciesBeforePause(int companyId, int workflowId) {
        List<WorkflowIcon> iconsBeforePause = getIcons(getSchemaBeforePause(workflowId, companyId));
        return getDependenciesFromIcons(iconsBeforePause);
    }

    private Set<WorkflowDependency> getDependenciesFromIcons(List<WorkflowIcon> icons) {
        Set<WorkflowDependency> dependencies = new HashSet<>();

        for (WorkflowIcon icon : icons) {
            dependencies.addAll(icon.getDependencies());
        }

        Set<Integer> mailingIds = new HashSet<>();

        for (WorkflowDependency dependency : dependencies) {
            if (MAILING_DELIVERY == dependency.getType()) {
                mailingIds.add(dependency.getEntityId());
            }
        }

        // Remove redundant dependencies -> mailing delivery overlaps mailing reference.
        dependencies.removeIf(dependency -> {
            // Referenced mailing may or may not be used in a mailing icon (to be delivered).
            return WorkflowDependencyType.MAILING_REFERENCE == dependency.getType() && mailingIds.contains(dependency.getEntityId());
        });
        return dependencies;
    }

    /**
     * Release mailing entities that are not part of the workflow anymore — reset some changes previously applied to those mailings.
     * In order to collect obsolete mailings the method compares given {@code icons} and structure stored in database.
     *
     * @param workflowId an identifier of the workflow being updated.
     * @param companyId an identifier of a company that owns the referenced workflow.
     * @param icons an object representation of the workflow icons to be stored.
     */
    private void releaseObsoleteMailings(int workflowId, int companyId, List<WorkflowIcon> icons) {
        List<WorkflowIcon> storedIcons = getIcons(workflowId, companyId);

        if (CollectionUtils.isNotEmpty(storedIcons)) {
            Set<Integer> oldMailingIds = collectMailingIds(storedIcons);
            Set<Integer> newMailingIds = collectMailingIds(icons);

            // Retain obsolete mailing ids (mailings that are not used anymore).
            oldMailingIds.removeAll(newMailingIds);

            for (int mailingId : oldMailingIds) {
                Mailing mailing = getMailingForUpdate(mailingId, companyId);

                if (mailing != null) {
                    // Some hidden WM-driven target groups (list splits) could be assigned to a mailing.
                    // So we should reset related properties:
                    mailing.setSplitID(Mailing.NONE_SPLIT_ID);
                    mailing.setTargetExpression(null);
                    mailingDao.saveMailing(mailing, false);
                }
            }
        }
    }

    @Override
    public boolean existsAtLeastOneFilledMailingIcon(List<WorkflowIcon> icons) {
        return !collectMailingIds(icons).isEmpty();
    }

    /**
     * Collect distinct set of mailings managed by given {@code icons}.
     *
     * @param icons a list of icons representing the workflow structure.
     * @return a set of mailing identifiers.
     */
    private Set<Integer> collectMailingIds(List<WorkflowIcon> icons) {
        Set<Integer> ids = new HashSet<>();

        for (WorkflowIcon icon : icons) {
            switch (icon.getType()) {
                case WorkflowIconType.Constants.MAILING_ID:
                case WorkflowIconType.Constants.FOLLOWUP_MAILING_ID:
                case WorkflowIconType.Constants.ACTION_BASED_MAILING_ID:
                case WorkflowIconType.Constants.DATE_BASED_MAILING_ID:
                    int mailingId = WorkflowUtils.getMailingId(icon);
                    if (mailingId > 0 && icon.isFilled()) {
                        ids.add(mailingId);
                    }
                    break;
				default:
					break;
            }
        }

        return ids;
    }

    /**
     * Assign senderAdminId value to start and stop icons (required to provide proper notification delivery).
     *
     * @param icons a list of icons representing the workflow structure.
     * @param adminId an identifier of an admin to be treated as notification sender.
     */
    private void setSenderAdminId(List<WorkflowIcon> icons, int adminId) {
        for (WorkflowIcon icon : icons) {
            switch (icon.getType()) {
                case WorkflowIconType.Constants.START_ID:
                case WorkflowIconType.Constants.STOP_ID:
                    ((WorkflowStartStop) icon).setSenderAdminId(adminId);
                    break;
				default:
					break;
            }
        }
    }

    private void setCompatibleMailingTypes(List<WorkflowIcon> icons) {
        StartType startType = getStartType(icons);

        // Start icon is missing or unfilled so it's nothing to do.
        if (startType == StartType.UNKNOWN) {
            return;
        }

        ListIterator<WorkflowIcon> iterator = icons.listIterator();

        while (iterator.hasNext()) {
            WorkflowIcon icon = iterator.next();

            switch (icon.getType()) {
                case WorkflowIconType.Constants.MAILING_ID:
                case WorkflowIconType.Constants.FOLLOWUP_MAILING_ID:
                    if (startType != StartType.REGULAR) {
                        iterator.set(createReplacementMailingIcon(startType, icon));
                    }
                    break;

                case WorkflowIconType.Constants.ACTION_BASED_MAILING_ID:
                    if (startType != StartType.REACTION) {
                        iterator.set(createReplacementMailingIcon(startType, icon));
                    }
                    break;

                case WorkflowIconType.Constants.DATE_BASED_MAILING_ID:
                    if (startType != StartType.RULE) {
                        iterator.set(createReplacementMailingIcon(startType, icon));
                    }
                    break;
                default:
                	break;
            }
        }
    }

    private StartType getStartType(List<WorkflowIcon> icons) {
        StartType startType = StartType.UNKNOWN;

        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.START.getId() && icon.isFilled()) {
                StartType type = StartType.of((WorkflowStart) icon);

                // Skip invalid or unfilled start icons.
                if (type != StartType.UNKNOWN) {
                    // Multiple start types are not allowed in the same campaign.
                    if (startType != StartType.UNKNOWN) {
                        return StartType.UNKNOWN;
                    }

                    startType = type;
                }
            }
        }

        return startType;
    }

    private WorkflowIcon createReplacementMailingIcon(StartType startType, WorkflowIcon sample) {
        WorkflowIcon icon = WorkflowUtils.getEmptyIcon(createReplacementMailingIconType(startType));

        icon.setId(sample.getId());
        icon.setX(sample.getX());
        icon.setY(sample.getY());
        icon.setFilled(sample.isFilled());
        icon.setEditable(sample.isEditable());
        icon.setIconTitle(sample.getIconTitle());
        icon.setConnections(sample.getConnections());

        if (sample.isFilled()) {
            ((WorkflowMailingAware) icon).setMailingId(WorkflowUtils.getMailingId(sample));
        }

        return icon;
    }

    private WorkflowIconType createReplacementMailingIconType(StartType startType) {
        switch (startType) {
            case REGULAR:
                return WorkflowIconType.MAILING;

            case REACTION:
                return WorkflowIconType.ACTION_BASED_MAILING;

            case RULE:
                return WorkflowIconType.DATE_BASED_MAILING;

            default:
                throw new UnsupportedOperationException("Unexpected start type");
        }
    }

    @Override
	public void saveWorkflow(Workflow workflow) {
		if (workflow.getWorkflowId() > 0) {
		    // Create a new entity if updating failed (an entity seems to be deleted meanwhile).
            if (!workflowDao.updateWorkflow(workflow)) {
                workflowDao.createWorkflow(workflow);
            }
		} else {
            workflowDao.createWorkflow(workflow);
		}
    }

    /**
     * Calculate start/stop data — date range (if specified) and types. This data depends on workflow structure and
     * should be shown to user in the workflows overview list.
     *
     * @param workflow a workflow entity to update.
     * @param timezone a timezone to be used for local date processing.
     */
    private void calculateDerivedData(Workflow workflow, TimeZone timezone) {
        Date startDate = null;
        Date stopDate = null;
        WorkflowEndType stopType = null;
        WorkflowReactionType startReaction = null;
        WorkflowStartEventType startEvent = null;

        for (WorkflowIcon workflowIcon : workflow.getWorkflowIcons()) {
            if (!workflowIcon.isFilled()) {
                continue;
            }

            Date date;
            switch (workflowIcon.getType()) {
                case WorkflowIconType.Constants.START_ID:
                    WorkflowStart iconStart = (WorkflowStart) workflowIcon;
                    date = WorkflowUtils.getStartStopIconDate(iconStart, timezone);

                    // A start icon having the earliest date is the main one
                    if (startDate == null || (date != null && date.before(startDate))) {
                        startDate = date;
                        if (iconStart.getStartType() == WorkflowStart.WorkflowStartType.EVENT) {
                            startEvent = iconStart.getEvent();
                            if (iconStart.getEvent() == WorkflowStartEventType.EVENT_REACTION) {
                                startReaction = iconStart.getReaction();
                            }
                        }
                    }
                    break;

                case WorkflowIconType.Constants.STOP_ID:
                    WorkflowStop iconStop = (WorkflowStop) workflowIcon;
                    stopType = iconStop.getEndType();
                    if (stopType == WorkflowEndType.DATE) {
                        date = WorkflowUtils.getStartStopIconDate(iconStop, timezone);

                        // A stop icon having the latest date is the main one
                        if (stopDate == null || (date != null && date.after(stopDate))) {
                            stopDate = date;
                        }
                    }
                    break;

				default:
					break;
            }
        }

        workflow.setGeneralStartDate(startDate);
        workflow.setGeneralEndDate(stopDate);
        workflow.setEndType(stopType);
        workflow.setGeneralStartReaction(startReaction);
        workflow.setGeneralStartEvent(startEvent);
    }

    @Override
    public Workflow getWorkflow(int workflowId, int companyId) {
        Workflow workflow = workflowDao.getWorkflow(workflowId, companyId);

        if (workflow != null) {
            workflow.setWorkflowIcons(getIcons(workflow.getWorkflowSchema()));
        }

        return workflow;
    }

    @Override
    public ServiceResult<List<Workflow>> getAllowedForDeletion(Set<Integer> ids, int companyId) {
        return bulkActionValidationService.checkAllowedForDeletion(ids, id -> getWorkflowForDeletion(id, companyId));
    }

    @Override
    public Collection<Integer> bulkDelete(Set<Integer> ids, int companyId) {
        List<Integer> allowedIds = ids.stream()
                .map(id -> getWorkflowForDeletion(id, companyId))
                .filter(ServiceResult::isSuccess)
                .map(r -> r.getResult().getWorkflowId())
                .collect(Collectors.toList());

        allowedIds.forEach(id -> deleteWorkflow(id, companyId));

        return allowedIds;
    }

    private ServiceResult<Workflow> getWorkflowForDeletion(int id, int companyId) {
        Workflow workflow = getWorkflow(id, companyId);
        if (workflow == null) {
            return ServiceResult.errorKeys("error.general.missing");
        }

        if (Workflow.WorkflowStatus.STATUS_ACTIVE.equals(workflow.getStatus()) || Workflow.WorkflowStatus.STATUS_TESTING.equals(workflow.getStatus())) {
            return ServiceResult.errorKeys("error.workflow.nodesShouldBeDisabledBeforeDeleting");
        }

        return ServiceResult.success(workflow);
    }

    @Override
    public List<WorkflowIcon> getIcons(int workflowId, int companyId) {
        return getIcons(workflowDao.getSchema(workflowId, companyId));
    }

    @Override
    public List<WorkflowIcon> getIconsForClone(Admin admin, int workflowId, boolean isWithContent) {
        List<WorkflowIcon> icons = getIcons(workflowId, admin.getCompanyID());

        if (icons == null) {
            return null;
        }

        return cloneIcons(admin, icons, isWithContent);
    }

    @Override
    public List<WorkflowIcon> getIcons(String schema) {
        if (StringUtils.isBlank(schema)) {
            return new ArrayList<>();
        }

        return workflowDataParser.deSerializeWorkflowIconsList(schema);
    }

    @Override
    public boolean validateDependency(int companyId, int workflowId, WorkflowDependency dependency) {
        boolean strict = false;

        switch (dependency.getType()) {
            // Extend on demand.
            case AUTO_IMPORT:
            case AUTO_EXPORT:
                strict = true;
                break;
			default:
			    //nothing to do
        }

        return workflowDao.validateDependency(companyId, workflowId, dependency, strict);
    }

    @Override
    public void deleteWorkflow(int workflowId, int companyId) {
        reminderDao.deleteReminders(companyId, workflowId);
        reactionDao.deleteWorkflowReactions(workflowId, companyId);
        workflowDao.deleteDependencies(companyId, workflowId, true);
        removeMailingsTargetExpressions(workflowId, companyId);
        workflowDao.deleteWorkflow(workflowId, companyId);
    }

    private void removeMailingsTargetExpressions(int workflowId, int companyId) {
        Set<Integer> mailingIds = collectMailingIds(getWorkflow(workflowId, companyId).getWorkflowIcons());
        workflowDao.removeMailingsTargetExpressions(companyId, mailingIds);
    }

    @Override
    public List<Workflow> getWorkflowsOverview(Admin admin) {
        return workflowDao.getWorkflowsOverview(admin);
    }

    @Override
    public List<DashboardWorkflow> getWorkflowsForDashboard(Admin admin) {
        return workflowDao.getWorkflowsForDashboard(admin);
    }

    @Override
	public List<LightweightMailing> getAllMailings(Admin admin) {
        return mailingDao.getMailingsDateSorted(admin);
	}

    @Override
    public List<LightweightMailing> getAllMailingsSorted(Admin admin, String sortFiled, String sortDirection) {
        return mailingDao.getAllMailingsSorted(admin, sortFiled, sortDirection);
    }

    @Override
	public List<Map<String, Object>> getAllMailings(Admin admin, List<MailingType> mailingTypes, String status,
                                                    String mailingStatus, boolean takeMailsForPeriod, String sort,
                                                    String order) {
        if (StringUtils.equals(status, "all")) {
            status = null;
        }

        return mailingDao.getMailingsNamesByStatus(admin, mailingTypes, status, mailingStatus, takeMailsForPeriod, sort, order);
	}

    @Override
	public List<Map<String, Object>> filterWithRequiredMediaTypes(List<Map<String, Object>> mailings, List<Integer> mediaTypes) {
        List<Map<String, Object>> mailingsWithMediatypes = new ArrayList<>();

        for (Map<String, Object> mailing : mailings) {
            int mailingId = ((Number) mailing.get("MAILING_ID")).intValue();
            int companyId = ((Number) mailing.get("COMPANY_ID")).intValue();

            try {
                Map<Integer, Mediatype> activeMediatypes = mediatypesDao.loadMediatypesByStatus(MediaTypeStatus.Active, mailingId, companyId);

                boolean atLeastOneMediatypeExists = mediaTypes.stream()
                        .anyMatch(activeMediatypes::containsKey);

                if (atLeastOneMediatypeExists) {
                    mailingsWithMediatypes.add(mailing);
                }

            } catch (MediatypesDaoException e) {
                logger.error(format("Error occurred during load of mailing (ID - {0}) mediatypes!", mailingId), e);
            }
        }

        return mailingsWithMediatypes;
    }

    @Override
    public List<Map<String, Object>> getMailings(int companyId, String commaSeparatedMailingIds) {
        return mailingDao.getMailings(companyId, commaSeparatedMailingIds);
    }

    @Override
    public List<TargetLight> getAllTargets(int companyId) {
        return targetDao.getTargetLights(companyId);
    }

    @Override
	public List<ProfileField> getHistorizedProfileFields(int companyId) throws Exception {
		return columnInfoService.getHistorizedComColumnInfos(companyId);
	}

    @Override
    public List<ProfileField> getProfileFields(int companyId) throws Exception {
        return new ArrayList<>(profileFieldDao.getComProfileFieldsMap(companyId, false).values());
    }

    @Override
	public List<AdminEntry> getAdmins(int companyId) {
		return adminService.listAdminsByCompanyID(companyId);
	}

	@Override
    public List<UserForm> getAllUserForms(int companyId) {
        return userFormDao.getUserForms(companyId);
    }

	@Override
    public Mailing getMailing(int mailingId, int companyId) {
        return mailingDao.getMailing(mailingId, companyId);
    }

	@Override
    public Map<String, Object> getMailingWithWorkStatus(int mailingId, int companyId) {
        return mailingDao.getMailingWithWorkStatus(mailingId, companyId);
    }

    @Override
    public boolean isParentMailingIdExistsInList(int parentMailingId, List<Map<String, Object>> mailings) {
        return mailings.stream()
                .anyMatch(mailing -> ((Number) mailing.get("MAILING_ID")).intValue() == parentMailingId);
    }

    @Override
    public String getTargetSplitName(int splitId) {
        return targetDao.getTargetSplitName(splitId);
    }

    private void updateEntities(Workflow workflow, Admin admin) {
        List<WorkflowIcon> icons = workflow.getWorkflowIcons();

        // Make sure that a basic structure is ok so it can be processed to collect data and apply settings to managed entities.
        if (!workflowValidationService.validateBasicStructure(icons)) {
            // Don't even try to collect the data if schema is inconsistent (contains loops, detached icons, etc).
            return;
        }

        WorkflowGraph graph = new WorkflowGraph(icons);

        try (CachingEntitiesSupplier entitiesSupplier = new CachingEntitiesSupplier(admin.getCompanyID())) {
            for (WorkflowNode node : graph.getAllNodesByType(WorkflowIconType.START.getId())) {
                Map<WorkflowIcon, List<Chain>> chainMap = new HashMap<>();

                // Collect chains for icons that refer external WM-driven entities.
                collectChains(chainMap, node);

                // Assign collected data to referenced entities.
                chainMap.forEach((icon, chains) -> updateEntities(icon, chains, admin, entitiesSupplier));
            }
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage(), e);
        }
    }

    private void updateEntities(WorkflowIcon icon, List<Chain> chains, Admin admin, EntitiesSupplier entitiesSupplier) {
        if (icon.isFilled() && icon.isEditable()) {
            switch (icon.getType()) {
                // Extend the following list on demand.
                case WorkflowIconType.Constants.MAILING_ID:
                case WorkflowIconType.Constants.FOLLOWUP_MAILING_ID:
                case WorkflowIconType.Constants.ACTION_BASED_MAILING_ID:
                case WorkflowIconType.Constants.DATE_BASED_MAILING_ID:
                    WorkflowMailingAware mailingIcon = (WorkflowMailingAware) icon;
                    int mailingId = mailingIcon.getMailingId();
                    if (mailingId > 0) {
                        Mailing mailing = entitiesSupplier.getMailing(mailingId);
                        if (mailing != null) {
                            assignWorkflowDrivenSettings(mailing, chains, mailingIcon, admin);
                        }
                    }
                    break;

                case WorkflowIconType.Constants.IMPORT_ID:
                    int autoImportId = ((WorkflowImport) icon).getImportexportId();
                    if (autoImportId > 0) {
                        AutoImport autoImport = entitiesSupplier.getAutoImport(autoImportId);
                        if (autoImport != null && !autoImport.isActive()) {
                            assignWorkflowDrivenSettings(autoImport, chains, admin);
                        }
                    }
                    break;

				default:
					break;
            }
        }
    }

    private void assignWorkflowDrivenSettings(Mailing mailing, List<Chain> chains, WorkflowMailingAware mailingIcon, Admin admin) {
        TimeZone timezone = AgnUtils.getTimeZone(admin);
        Date date = getMinDate(chains, timezone);

        mailing.setMailingType(getMailingType(mailingIcon, mailing.getMailingType()));
        // Assign a planning date or null (if schema is incomplete).
        mailing.setPlanDate(DateUtilities.midnight(date, timezone));

        chains.stream()
            .map(Chain::getArchive)
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(archive -> {
                mailing.setCampaignID(archive.getCampaignId());
                mailing.setArchived(archive.isArchived() ? 1 : 0);
            });

        chains.stream()
            .map(Chain::getMailingListId)
            .filter(id -> id > 0)
            .findFirst()
            .ifPresent(mailing::setMailinglistID);

        String targetExpression = chains.stream()
            .map(Chain::getTargetExpression)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining("|"));

        mailing.setSplitID(Mailing.NONE_SPLIT_ID);
        mailing.setTargetExpression(targetExpression);
    }

    private void assignWorkflowDrivenSettings(AutoImport autoImport, List<Chain> chains, Admin admin) {
        TimeZone timezone = AgnUtils.getTimeZone(admin);

        autoImport.setDeactivateByCampaign(true);
        autoImport.setAutoActivationDate(getMinDate(chains, timezone));

        List<Integer> mailingLists = Collections.emptyList();
        for (Chain chain : chains) {
            // Find first valid mailing list.
            int mailingListId = chain.getMailingListId();
            if (mailingListId > 0) {
                mailingLists = Collections.singletonList(mailingListId);
                break;
            }
        }

        autoImport.setMailinglists(mailingLists);
    }

    private Date getMinDate(List<Chain> chains, TimeZone timezone) {
        Date minDate = null;

        for (Chain chain : chains) {
            Date date = chain.getDate(timezone);

            if (minDate == null || date != null && date.before(minDate)) {
                minDate = date;
            }
        }

        return minDate;
    }

    private MailingType getMailingType(WorkflowMailingAware mailingIcon, MailingType defaultType) {
        switch (mailingIcon.getType()) {
            case WorkflowIconType.Constants.MAILING_ID:
                return MailingType.NORMAL;

            case WorkflowIconType.Constants.FOLLOWUP_MAILING_ID:
                return MailingType.FOLLOW_UP;

            case WorkflowIconType.Constants.DATE_BASED_MAILING_ID:
                return MailingType.DATE_BASED;

            case WorkflowIconType.Constants.ACTION_BASED_MAILING_ID:
                return MailingType.ACTION_BASED;

			default:
		        return defaultType;
        }
    }

    private void collectChains(Map<WorkflowIcon, List<Chain>> chainMap, WorkflowNode node) {
        collectChains(chainMap, new Chain(), node);
    }

    private void collectChains(Map<WorkflowIcon, List<Chain>> chainMap, Chain chain, WorkflowNode node) {
        List<WorkflowNode> nextNodes = node.getNextNodes();

        while (true) {
            WorkflowIcon icon = node.getNodeIcon();

            switch (icon.getType()) {
                case WorkflowIconType.Constants.START_ID:
                    chain.append((WorkflowStart) icon);
                    break;

                case WorkflowIconType.Constants.RECIPIENT_ID:
                    chain.append((WorkflowRecipient) icon);
                    break;

                case WorkflowIconType.Constants.DEADLINE_ID:
                    chain.append((WorkflowDeadline) icon);
                    break;

                case WorkflowIconType.Constants.PARAMETER_ID:
                    chain.append((WorkflowParameter) icon);
                    break;

                case WorkflowIconType.Constants.ARCHIVE_ID:
                    chain.append((WorkflowArchive) icon);
                    break;

                // Extend the following list on demand.
                case WorkflowIconType.Constants.MAILING_ID:
                case WorkflowIconType.Constants.FOLLOWUP_MAILING_ID:
                case WorkflowIconType.Constants.ACTION_BASED_MAILING_ID:
                case WorkflowIconType.Constants.DATE_BASED_MAILING_ID:
                case WorkflowIconType.Constants.IMPORT_ID:
                case WorkflowIconType.Constants.EXPORT_ID:
                    chainMap.computeIfAbsent(icon, i -> new ArrayList<>()).add(new Chain(chain));
                    break;

                case WorkflowIconType.Constants.STOP_ID:
                    // It's the end.
                    return;

				default:
					break;
            }

            if (nextNodes.size() == 1) {
                node = nextNodes.get(0);
                nextNodes = node.getNextNodes();
            } else {
                break;
            }
        }

        for (WorkflowNode next : nextNodes) {
            collectChains(chainMap, new Chain(chain), next);
        }
    }

    private Mailing getMailingForUpdate(int mailingId, int companyId) {
        try {
            Mailing mailing = getMailing(mailingId, companyId);
            return mailing != null && mailing.getId() > 0 ? mailing : null;
        } catch (MailingNotExistException e) {
            return null;
        }
    }

    private void populateMailingByDataFromFoundChain(String timeZone, boolean withOwnNodes, List<WorkflowNode> foundNodes, Mailing mailing) {
        TimeZone aZone = TimeZone.getTimeZone(timeZone);
        Calendar aCalendar = Calendar.getInstance();
        boolean haveStartingTime = false;
        boolean isOwnIconProcessed = false;

        for (WorkflowNode workflowNode : foundNodes) {
            WorkflowIcon workflowIcon = workflowNode.getNodeIcon();
            if (!workflowIcon.isFilled()) {
                continue;
            }

            switch (workflowIcon.getType()) {
                case WorkflowIconType.Constants.PARAMETER_ID:
                    WorkflowParameter parameterIcon = (WorkflowParameter) workflowIcon;
                    if (parameterIcon.getValue() > 0) {
                        mailing.setSplitID(Mailing.YES_SPLIT_ID);
                    } else {
                        mailing.setSplitID(Mailing.NONE_SPLIT_ID);
                    }
                    break;

                case WorkflowIconType.Constants.DEADLINE_ID:
                    WorkflowDeadline deadlineIcon = (WorkflowDeadline) workflowIcon;

                    switch (deadlineIcon.getDeadlineType()) {
                        case TYPE_FIXED_DEADLINE:
                            aCalendar.setTime(deadlineIcon.getDate());
                            aCalendar.add(Calendar.HOUR_OF_DAY, deadlineIcon.getHour());
                            aCalendar.add(Calendar.MINUTE, deadlineIcon.getMinute());
                            aCalendar.setTimeZone(aZone);
                            mailing.setPlanDate(aCalendar.getTime());
                            haveStartingTime = true;
                            break;

                        case TYPE_DELAY:
                            if (haveStartingTime) {
                                switch (deadlineIcon.getTimeUnit()) {
                                    case TIME_UNIT_MINUTE:
                                        aCalendar.add(Calendar.MINUTE, deadlineIcon.getDelayValue());
                                        break;
                                    case TIME_UNIT_HOUR:
                                        aCalendar.add(Calendar.HOUR_OF_DAY, deadlineIcon.getDelayValue());
                                        break;
                                    case TIME_UNIT_DAY:
                                        aCalendar.add(Calendar.DAY_OF_YEAR, deadlineIcon.getDelayValue());
                                        if (deadlineIcon.isUseTime()) {
                                            aCalendar.add(Calendar.HOUR_OF_DAY, deadlineIcon.getHour());
                                            aCalendar.add(Calendar.MINUTE, deadlineIcon.getMinute());
                                        }
                                        break;
									case TIME_UNIT_MONTH:
										break;
									case TIME_UNIT_WEEK:
										break;
									default:
										break;
                                }

                                aCalendar.setTimeZone(aZone);
                                mailing.setPlanDate(aCalendar.getTime());
                            }
                            break;

						default:
							break;
                    }
                    isOwnIconProcessed = true;
                    break;

                case WorkflowIconType.Constants.ARCHIVE_ID:
                    if (!isOwnIconProcessed || !withOwnNodes) {
                        WorkflowArchive archiveIcon = (WorkflowArchive) workflowIcon;
                        mailing.setCampaignID(archiveIcon.getCampaignId());
                        mailing.setArchived(archiveIcon.isArchived() ? 1 : 0);
                    }
                    break;

                case WorkflowIconType.Constants.RECIPIENT_ID:
                    if (!isOwnIconProcessed || !withOwnNodes) {
                        WorkflowRecipient recipientIcon = (WorkflowRecipient) workflowIcon;
                        mailing.setMailinglistID(recipientIcon.getMailinglistId());
                        if (CollectionUtils.isNotEmpty(recipientIcon.getAltgs())) {
                            mailing.setTargetExpression(TargetExpressionUtils.makeTargetExpressionWithAltgs(recipientIcon.getAltgs(), recipientIcon.getTargets(), recipientIcon.getTargetsOption()));
                        } else {
                            mailing.setTargetExpression(TargetExpressionUtils.makeTargetExpression(recipientIcon.getTargets(), recipientIcon.getTargetsOption()));
                        }
                    }
                    break;

                case WorkflowIconType.Constants.START_ID:
                    WorkflowStart startIcon = (WorkflowStart) workflowIcon;
                    aCalendar.setTime(WorkflowUtils.getStartStopIconDate(startIcon, aZone));
                    haveStartingTime = true;
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public Workflow copyWorkflow(Admin admin, int workflowId, boolean isWithContent) {
        Workflow workflow = getWorkflow(workflowId, admin.getCompanyID());

        // First clone a workflow entity itself, stored workflow schema is invalid at this point.
        workflow.setWorkflowId(0);
        workflow.setShortname(SafeString.getLocaleString("mailing.CopyOf", admin.getLocale()) + " " + workflow.getShortname());
        workflow.setStatus(Workflow.WorkflowStatus.STATUS_OPEN);

        // Migrate structure (icons and connections) to a clone - reset identifiers, clone referenced mailings (if required).
        List<WorkflowIcon> icons = cloneIcons(admin, workflow.getWorkflowIcons(), isWithContent);

        // Store workflow and its structure.
        getSelfReference().saveWorkflow(admin, workflow, icons);

        return workflow;
    }

    private List<WorkflowIcon> cloneIcons(Admin admin, List<WorkflowIcon> icons, boolean isWithContent) {
        List<WorkflowIcon> newIcons = new ArrayList<>(icons.size());

        if (isWithContent) {
            Map<Integer, Integer> newMailingsMap = cloneMailings(admin, collectMailingIds(icons));

            for (WorkflowIcon icon : icons) {
                if (icon.isFilled()) {
                    assignCloneMailingId(icon, newMailingsMap);
                }
                newIcons.add(icon);
            }
        } else {
            for (WorkflowIcon icon : icons) {
                WorkflowIcon newIcon = getEmptyIcon(icon);
                newIcon.setId(icon.getId());
                newIcon.setConnections(icon.getConnections());
                newIcons.add(newIcon);
            }
        }

        return newIcons;
    }

    private void assignCloneMailingId(WorkflowIcon icon, Map<Integer, Integer> map) {
        switch (icon.getType()) {
            case WorkflowIconType.Constants.MAILING_ID:
            case WorkflowIconType.Constants.ACTION_BASED_MAILING_ID:
            case WorkflowIconType.Constants.DATE_BASED_MAILING_ID: {
                WorkflowMailingAware mailing = (WorkflowMailingAware) icon;

                // Migrate the mailing (if any) referenced from an icon.
                if (!migrateId(mailing::setMailingId, mailing::getMailingId, map)) {
                    mailing.setFilled(false);
                }

                mailing.setIconTitle("");
                break;
            }

            case WorkflowIconType.Constants.FOLLOWUP_MAILING_ID: {
                WorkflowFollowupMailing mailing = (WorkflowFollowupMailing) icon;

                // Migrate the follow-up mailing (if any) referenced from an icon.
                if (!migrateId(mailing::setMailingId, mailing::getMailingId, map)) {
                    mailing.setFilled(false);
                }

                // Migrate the base mailing (if any) referenced from an icon.
                if (!migrateId(mailing::setBaseMailingId, mailing::getBaseMailingId, map)) {
                    mailing.setFilled(false);
                }

                mailing.setIconTitle("");
                break;
            }

            case WorkflowIconType.Constants.DECISION_ID: {
                WorkflowDecision decision = (WorkflowDecision) icon;

                // Migrate the mailing (if any) referenced from an icon.
                if (!migrateId(decision::setMailingId, decision::getMailingId, map)) {
                    decision.setFilled(false);
                }

                decision.setIconTitle("");
                break;
            }

			default:
				break;
        }
    }

    private boolean migrateId(Consumer<Integer> consumer, Supplier<Integer> supplier, Map<Integer, Integer> map) {
        int oldMailingId = supplier.get();
        int newMailingId = map.getOrDefault(oldMailingId, 0);

        consumer.accept(newMailingId);

        return newMailingId > 0 || oldMailingId <= 0;
    }

    private Map<Integer, Integer> cloneMailings(Admin admin, Set<Integer> mailingIds) {
        Map<Integer, Integer> map = new HashMap<>();

        for (int mailingId : mailingIds) {
            map.put(mailingId, cloneMailing(admin, mailingId));
        }

        return map;
    }

    private int cloneMailing(Admin admin, int mailingId) {
        String newMailingNamePrefix = SafeString.getLocaleString("mailing.CopyOf", admin.getLocale()) + " ";
        try {
            return mailingService.copyMailing(mailingId, admin.getCompanyID(), newMailingNamePrefix);
        } catch (Exception e) {
            logger.error("Error while copying mailing for workflow", e);
            return 0;
        }
    }

    private WorkflowIcon getEmptyIcon(WorkflowIcon icon) {
        try {
            WorkflowIcon newIcon = icon.getClass().getConstructor().newInstance();
            newIcon.setX(icon.getX());
            newIcon.setY(icon.getY());
            return newIcon;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean assignWorkflowDrivenSettings(Admin admin, Mailing mailing, int workflowId, int iconId) {
        if (mailing == null || workflowId <= 0 || iconId <= 0) {
            return false;
        }

        Workflow workflow = getWorkflow(workflowId, admin.getCompanyID());

        if (workflow == null) {
            return false;
        }

        WorkflowIcon mailingIcon = null;

        for (WorkflowIcon icon : workflow.getWorkflowIcons()) {
            if (iconId == icon.getId()) {
                mailingIcon = icon;
                break;
            }
        }

        if (mailingIcon == null) {
            return false;
        }

        return assignWorkflowDrivenSettings(admin, mailing, workflow.getWorkflowIcons(), mailingIcon);
    }

    private boolean assignWorkflowDrivenSettings(Admin admin, Mailing mailing, List<WorkflowIcon> icons, WorkflowIcon mailingIcon) {
        if (!WorkflowUtils.isMailingIcon(mailingIcon) || !workflowValidationService.noLoops(icons)) {
            return false;
        }

        WorkflowGraph graph = new WorkflowGraph();

        if (!graph.build(icons)) {
            return false;
        }

        List<WorkflowNode> foundNodes = new ArrayList<>();
        HashSet<Integer> terminateTypes = new HashSet<>();

        // found recipient
        WorkflowIcon prevRecipientIcon = graph.getPreviousIconByType(mailingIcon, WorkflowIconType.RECIPIENT.getId(), terminateTypes);
        if (prevRecipientIcon != null) {
            foundNodes.add(graph.getNodeByIcon(prevRecipientIcon));
        }

        // found archive
        WorkflowIcon prevArchiveIcon = graph.getPreviousIconByType(mailingIcon, WorkflowIconType.ARCHIVE.getId(), terminateTypes);
        if (prevArchiveIcon != null) {
            foundNodes.add(graph.getNodeByIcon(prevArchiveIcon));
        }

        // found start
        // ensure that start icon is added before a deadline icons (that could break a planned date calculation unless a deadline is fixed)
        WorkflowIcon prevStartIcon = graph.getPreviousIconByType(mailingIcon, WorkflowIconType.START.getId(), terminateTypes);
        if (prevStartIcon != null) {
            foundNodes.add(graph.getNodeByIcon(prevStartIcon));
        }

        // found deadline
        WorkflowIcon prevDeadlineIcon = graph.getPreviousIconByType(mailingIcon, WorkflowIconType.DEADLINE.getId(), terminateTypes);
        Deque<WorkflowIcon> prevDeadlineIcons = new LinkedList<>();
        while (prevDeadlineIcon != null) {
            prevDeadlineIcons.addFirst(prevDeadlineIcon);
            if (((WorkflowDeadline)prevDeadlineIcon).getDeadlineType() == WorkflowDeadline.WorkflowDeadlineType.TYPE_FIXED_DEADLINE) {
                // We have to sum delays unless a deadline is fixed
                break;
            }
            prevDeadlineIcon = graph.getPreviousIconByType(prevDeadlineIcon, WorkflowIconType.DEADLINE.getId(), terminateTypes);
        }

        for (WorkflowIcon icon : prevDeadlineIcons) {
            foundNodes.add(graph.getNodeByIcon(icon));
        }

        // found parameter
        WorkflowIcon parameterIcon = graph.getPreviousIconByType(mailingIcon, WorkflowIconType.PARAMETER.getId(), terminateTypes);
        if (parameterIcon != null) {
            foundNodes.add(graph.getNodeByIcon(parameterIcon));
        }

        populateMailingByDataFromFoundChain(admin.getAdminTimezone(), false, foundNodes, mailing);

        return true;
    }

	@Override
    public boolean hasCompanyDeepTrackingTables(int companyId) {
        return birtCompanyDao.hasDeepTrackingTables(companyId);
    }

    @Override
    public Map<Integer, ChangingWorkflowStatusResult> bulkDeactivate(Set<Integer> workflowIds, int companyId) throws Exception {
        final Map<Integer, ChangingWorkflowStatusResult> results = new HashMap<>();
        for (int workflowId : workflowIds) {
            Workflow workflow = getWorkflow(workflowId, companyId);

            final ChangingWorkflowStatusResult changingResult;
            switch (workflow.getStatus()) {
                case STATUS_ACTIVE:
                    changingResult = changeWorkflowStatus(workflow, WorkflowStatus.STATUS_INACTIVE);
                    break;

                case STATUS_TESTING:
                    changingResult = changeWorkflowStatus(workflow, WorkflowStatus.STATUS_OPEN);
                    break;

                default:
                    changingResult = ChangingWorkflowStatusResult.notChanged();
                    break;
            }

            results.put(workflowId, changingResult);
        }
        return results;
    }

    @Override
    public Map<Integer, ServiceResult<ChangingWorkflowStatusResult>> setActiveness(Set<Integer> ids, Admin admin, boolean activeness) {
        return ids.stream().collect(Collectors.toMap(Function.identity(), id -> {
            Workflow workflow = getWorkflow(id, admin.getCompanyID());

            if (workflow == null) {
                return new ServiceResult<>(ChangingWorkflowStatusResult.notChanged(Message.of(ERROR_MSG)), false);
            }

            return activeness ? activateWorkflow(workflow, admin) : deactivateWorkflow(workflow, admin);
        }));
    }

    private ServiceResult<ChangingWorkflowStatusResult> activateWorkflow(Workflow workflow, Admin admin) {
        WorkflowStatus newStatus = WorkflowStatus.STATUS_ACTIVE;

        try {
            SimpleServiceResult validationResult = workflowValidationService.validate(workflow.getWorkflowId(), workflow.getWorkflowIcons(), workflow.getStatus(), newStatus, admin);
            if (!validationResult.isSuccess()) {
                return new ServiceResult<>(ChangingWorkflowStatusResult.notChanged(validationResult.getErrorMessages()), false);
            }

            boolean unpausing = WorkflowUtils.isUnpausing(workflow.getStatus(), newStatus);
            List<Message> activationErrors = new ArrayList<>();

            deleteWorkflowTargetConditions(admin.getCompanyID(), workflow.getWorkflowId());
            if (workflowActivationService.activateWorkflow(workflow.getWorkflowId(), admin, false, unpausing, false, new ArrayList<>(), activationErrors, new ArrayList<>())) {
                ChangingWorkflowStatusResult result = changeWorkflowStatus(workflow, newStatus);
                return new ServiceResult<>(result, result.isChanged());
            }

            return new ServiceResult<>(changeWorkflowStatus(workflow, WorkflowStatus.STATUS_FAILED), false, activationErrors);
        } catch (Exception e) {
            logger.error("Error occurred while activating the workflow! ID: " + workflow.getWorkflowId(), e);
            return new ServiceResult<>(ChangingWorkflowStatusResult.notChanged(Message.of(ERROR_MSG)), false);
        }
    }

    private ServiceResult<ChangingWorkflowStatusResult> deactivateWorkflow(Workflow workflow, Admin admin) {
        WorkflowStatus oldStatus = workflow.getStatus();

        try {
            if (WorkflowStatus.STATUS_ACTIVE.equals(oldStatus)) {
                ChangingWorkflowStatusResult result = changeWorkflowStatus(workflow, WorkflowStatus.STATUS_INACTIVE);
                return new ServiceResult<>(result, result.isChanged());
            }

            if (WorkflowStatus.STATUS_TESTING.equals(oldStatus)) {
                ChangingWorkflowStatusResult result = changeWorkflowStatus(workflow, WorkflowStatus.STATUS_OPEN);
                return new ServiceResult<>(result, result.isChanged());
            }

            if (WorkflowStatus.STATUS_PAUSED.equals(oldStatus)) {
                deletePauseUndoEntry(workflow.getWorkflowId(), admin.getCompanyID());
                ChangingWorkflowStatusResult result = changeWorkflowStatus(workflow, WorkflowStatus.STATUS_INACTIVE);
                return new ServiceResult<>(result, result.isChanged());
            }
        } catch (Exception e) {
            logger.error("Error occurred while deactivating the workflow! ID: " + workflow.getWorkflowId(), e);
        }

        return new ServiceResult<>(ChangingWorkflowStatusResult.notChanged(Message.of(ERROR_MSG)), false);
    }

    @Override
	public ChangingWorkflowStatusResult changeWorkflowStatus(int workflowId, int companyId, WorkflowStatus newStatus) throws Exception {
        Workflow workflow = getWorkflow(workflowId, companyId);
        return changeWorkflowStatus(workflow, newStatus);
	}

    private ChangingWorkflowStatusResult changeWorkflowStatus(Workflow workflow, WorkflowStatus newStatus) throws Exception {
        int workflowId = workflow.getWorkflowId();
        int companyId = workflow.getCompanyId();

        List<WorkflowIcon> workflowIcons = workflow.getWorkflowIcons();

        WorkflowGraph workflowGraph = new WorkflowGraph();
        if (!workflowGraph.build(workflowIcons)) {
            return ChangingWorkflowStatusResult.notChanged();
        }

        // Retrieve active (scheduled/working/finished) auto-optimizations
        final List<ComOptimization> optimizations = optimizationService.listWorkflowManaged(workflowId, companyId)
                .stream()
                .filter(optimization -> optimization.getStatus() != AutoOptimizationStatus.NOT_STARTED.getCode())
                .collect(toList());

        switch (newStatus) {
            case STATUS_COMPLETE:
                if (workflow.getEndType() == WorkflowEndType.AUTOMATIC) {
                    for (WorkflowIcon icon : workflowIcons) {
                        final int type = icon.getType();

                        // if workflow with automatic end contains action/date based mailing, import or export we can not update status to completed
                        if (type == WorkflowIconType.ACTION_BASED_MAILING.getId()
                                || type == WorkflowIconType.DATE_BASED_MAILING.getId()
                                || type == WorkflowIconType.IMPORT.getId()
                                || type == WorkflowIconType.EXPORT.getId()
                                ) {
                            return ChangingWorkflowStatusResult.notChanged();
                        }

                        List<Integer> mailingsToCheck = new ArrayList<>();

                        for (ComOptimization optimization : optimizations) {
                            if (optimization.getStatus() == AutoOptimizationStatus.FINISHED.getCode()) {
                                mailingsToCheck.add(optimization.getFinalMailingId());
                            } else {
                                return ChangingWorkflowStatusResult.notChanged();
                            }
                        }

                        // if either normal or followup mailing haven't sent yet we can not update status to completed
                        if (type == WorkflowIconType.MAILING.getId() || type == WorkflowIconType.FOLLOWUP_MAILING.getId()) {
                            int mailingId = ((WorkflowMailingAware) icon).getMailingId();

                            // AO-driven final mailings have no mailingId (icon's mailingId = 0)
                            if (mailingId != 0) {
                                mailingsToCheck.add(mailingId);
                            }
                        }

                        for (Integer mailingId : mailingsToCheck) {
                            Map<String, Object> mailingData = getMailingWithWorkStatus(mailingId, companyId);
                            Date sendDate = mailingDao.getLastSendDate(mailingId);

                            EmmCalendar sendCalendar = new EmmCalendar(TimeZone.getDefault());
                            if (sendDate != null) {
                                sendCalendar.setTime(sendDate);
                                sendCalendar.add(EmmCalendar.HOUR, DELAY_FOR_SENDING_MAILING);
                            }
                            // get current date and time
                            GregorianCalendar nowCal = new GregorianCalendar(TimeZone.getDefault());
                            if (!(MailingStatus.SENT.getDbKey().equals(mailingData.get("work_status"))
                                    || MailingStatus.NORECIPIENTS.getDbKey().equals(mailingData.get("work_status"))
                                    || (sendDate != null && nowCal.after(sendCalendar)))) {
                                return ChangingWorkflowStatusResult.notChanged();
                            }
                        }
                    }
                }
                break;

            case STATUS_TESTED:
                List<Integer> mailingTypes = Arrays.asList(
                        WorkflowIconType.MAILING.getId(),
                        WorkflowIconType.DATE_BASED_MAILING.getId(),
                        WorkflowIconType.ACTION_BASED_MAILING.getId(),
                        WorkflowIconType.FOLLOWUP_MAILING.getId()
                );

                List<WorkflowNode> mailingNodes = workflowGraph.getAllNodesByTypes(mailingTypes);
                Map<Integer, Boolean> sentMailings = new HashMap<>();

                // Build relations map connecting AO-test mailings and AO-final mailings
                final Map<Integer, Integer> aoTestToFinalMailingMap = new HashMap<>();
                for (ComOptimization optimization : optimizations) {
                    if (optimization.getStatus() == AutoOptimizationStatus.FINISHED.getCode()) {
                        Integer finalMailingId = optimization.getFinalMailingId();
                        for (Integer testMailingId : optimization.getTestmailingIDs()) {
                        	aoTestToFinalMailingMap.put(testMailingId, finalMailingId);
                        }
                    }
                }

                Map<Integer, Mailing> mailings = new HashMap<>();
                Map<Integer, WorkflowMailingAware> mailingIcons = new HashMap<>();
                for (WorkflowNode workflowNode : mailingNodes) {
                    WorkflowMailingAware icon = (WorkflowMailingAware) workflowNode.getNodeIcon();
                    int mailingId = icon.getMailingId();
                    mailingIcons.put(mailingId, icon);

                    // AO-driven final mailings have no mailingId (icon's mailingId = 0)
                    if (mailingId == 0) {
                        WorkflowMailing testMailing = getPreviousOptimizationTestMailing(workflowGraph, icon);
                        if (testMailing != null) {
                            Integer id = aoTestToFinalMailingMap.get(testMailing.getMailingId());
                            if (id != null) {
                                mailingId = id;
                            }
                        }
                    }

                    boolean sent = false;
                    if (mailingId != 0) {
                        Mailing mailing = mailingDao.getMailing(mailingId, companyId);
                        mailings.put(mailingId, mailing);
                        MaildropEntry maildropEntry = null;
                        Date maxSendDate = null;

                        for (MaildropEntry entry : mailing.getMaildropStatus()) {
                            if (entry.getStatus() == MaildropStatus.TEST.getCode()) {
                                if (maildropEntry == null || entry.getSendDate().after(maxSendDate)) {
                                    maildropEntry = entry;
                                    maxSendDate = entry.getSendDate();
                                }
                            }
                        }

                        if (maildropEntry != null && maildropEntry.getGenStatus() == MaildropGenerationStatus.FINISHED.getCode()) {
                            sent = true;
                        } else if (maxSendDate != null) {
                            TimeZone timezone = TimeZone.getDefault();

                            Calendar sendAttemptsDeadline = Calendar.getInstance(timezone);
                            sendAttemptsDeadline.setTime(maxSendDate);
                            sendAttemptsDeadline.add(Calendar.HOUR_OF_DAY, DELAY_FOR_SENDING_MAILING);

                            Calendar calendarNow = Calendar.getInstance(timezone);
                            if (sendAttemptsDeadline.before(calendarNow)) {
                                sent = true;
                            }
                        }
                    } else {
                        logger.debug("Unable to retrieve a mailingId for the mailing icon #" + icon.getId());
                    }
                    sentMailings.put(mailingId, sent);
                }

                List<WorkflowNode> startNodes = workflowGraph.getAllNodesByType(WorkflowIconType.START.getId());
                // The campaign should have exactly one start icon
                if (startNodes.size() != 1) {
                    return ChangingWorkflowStatusResult.notChanged();
                }
                if (!isCampaignTestFinished(workflowGraph, mailingTypes, mailingIcons, mailings, sentMailings, companyId)) {
                    return ChangingWorkflowStatusResult.notChanged();
                }
                break;
            default:break;
        }

        boolean anyMailingDeactivated = false;
        List<Message> errors = new ArrayList<>();
        WorkflowStatus oldStatus = workflow.getStatus();

        if (isDeactivationOrCompletion(oldStatus, newStatus)) {
            boolean isTestRun = newStatus == WorkflowStatus.STATUS_TESTED;
            boolean isPausing = WorkflowUtils.isPausing(oldStatus, newStatus);

            for (ComOptimization optimization : optimizations) {
                // Un-schedule not finished optimizations, delete finished ones
                try {
                    if (optimization.getStatus() == AutoOptimizationStatus.FINISHED.getCode()) {
                        optimizationService.delete(optimization);
                    } else {
                        optimizationCommonService.unscheduleOptimization(optimization, isTestRun);
                    }
                } catch (MaildropDeleteException e) {
                    logger.error("Error occurred during optimization un-scheduling: " + optimization.getId(), e);
                }
            }

            for (WorkflowIcon icon : workflowIcons) {
                switch (icon.getType()) {
                    case WorkflowIconType.Constants.MAILING_ID:
                    case WorkflowIconType.Constants.FOLLOWUP_MAILING_ID:
                    case WorkflowIconType.Constants.ACTION_BASED_MAILING_ID:
                    case WorkflowIconType.Constants.DATE_BASED_MAILING_ID:
                        anyMailingDeactivated |= deactivateMailing((WorkflowMailingAware) icon, isTestRun, companyId, errors);
                        break;

                    case WorkflowIconType.Constants.IMPORT_ID:
                        deactivateAutoImport((WorkflowImport) icon, companyId);
                        break;

                    case WorkflowIconType.Constants.EXPORT_ID:
                        deactivateAutoExport((WorkflowExport) icon, companyId);
                        break;

					default:
						break;
                }
            }

            // disable all triggers of workflow
            reactionDao.deactivateWorkflowReactions(workflowId, companyId, isPausing);

            // When campaign ends make sure that all scheduled reminders are sent.
            if (newStatus == WorkflowStatus.STATUS_COMPLETE) {
                // Send scheduled reminders (if any) before actual status change.
                reminderService.send();
            }

            if (newStatus == WorkflowStatus.STATUS_FAILED) {
                newStatus = WorkflowStatus.STATUS_INACTIVE;
            }

            if (newStatus == WorkflowStatus.STATUS_TESTING_FAILED) {
                newStatus = WorkflowStatus.STATUS_OPEN ;

            }
        }

        if (newStatus == WorkflowStatus.STATUS_COMPLETE || newStatus == WorkflowStatus.STATUS_INACTIVE) {
            workflowDao.setActualEndDate(workflowId, new Date(), companyId);
        }

        // Create/schedule or remove reminders depending on status.
        updateReminders(companyId, workflowId, newStatus, workflowIcons);

        // set workflow to new state
        workflowDao.changeWorkflowStatus(workflowId, companyId, newStatus);

        return new ChangingWorkflowStatusResult(oldStatus, newStatus, workflow, anyMailingDeactivated, errors);
    }

    private boolean isDeactivationOrCompletion(WorkflowStatus oldStatus, WorkflowStatus newStatus) {
        if (WorkflowStatus.STATUS_FAILED == newStatus || WorkflowUtils.isPausing(oldStatus, newStatus)) {
            return true;
        }

        if (isUnpausing(oldStatus, newStatus)) {
            return false;
        }

        switch (oldStatus) {
            case STATUS_ACTIVE:
            case STATUS_PAUSED:
            case STATUS_TESTING:
                return oldStatus != newStatus;

            default:
                return false;
        }
    }

    /**
     * Generate and store workflow reminders (if any) configured in start and stop icons — once scheduled must be
     * triggered by {@link com.agnitas.emm.core.workflow.service.ComWorkflowReminderServiceJobWorker} when it's time.
     *
     * @param companyId an identifier of a company that a referenced workflow belongs to.
     * @param workflowId an identifier of a workflow to generate and store reminders for.
     * @param newStatus a workflow status.
     * @param icons icons of a workflow to generate reminders for.
     */
    private void updateReminders(int companyId, int workflowId, WorkflowStatus newStatus, List<WorkflowIcon> icons) {
        AdminTimezoneSupplier adminTimezoneSupplier = new AdminTimezoneCachingSupplier(companyId);
        List<WorkflowReminder> reminders;

        switch (newStatus) {
            case STATUS_ACTIVE:
                reminders = getReminders(icons, true, adminTimezoneSupplier);
                break;

            case STATUS_INACTIVE:
                reminders = getReminders(icons, false, adminTimezoneSupplier);
                break;

            default:
                // Remove existing reminders.
                reminders = Collections.emptyList();
                break;
        }

        reminderDao.setReminders(companyId, workflowId, reminders);
    }

    private List<WorkflowReminder> getReminders(List<WorkflowIcon> icons, boolean isWorkflowActive, AdminTimezoneSupplier adminTimezoneSupplier) {
        List<WorkflowReminder> reminders = new ArrayList<>();

        for (WorkflowIcon icon : icons) {
            if (WorkflowUtils.isStartStopIcon(icon) && icon.isFilled()) {
                WorkflowReminder reminder = getReminder((WorkflowStartStop) icon, isWorkflowActive, adminTimezoneSupplier);

                if (reminder != null) {
                    reminders.add(reminder);
                }
            }
        }

        return reminders;
    }

    private WorkflowReminder getReminder(WorkflowStartStop icon, boolean isWorkflowActive, AdminTimezoneSupplier adminTimezoneSupplier) {
        if (icon.isScheduleReminder()) {
            switch (icon.getType()) {
                case WorkflowIconType.Constants.START_ID:
                    if (isWorkflowActive) {
                        return getReminder(icon, ReminderType.START, adminTimezoneSupplier);
                    } else {
                        return getReminder(icon, ReminderType.MISSING_START, adminTimezoneSupplier);
                    }

                case WorkflowIconType.Constants.STOP_ID:
                    if (isWorkflowActive) {
                        return getReminder(icon, ReminderType.STOP, adminTimezoneSupplier);
                    } else {
                        return null;
                    }

                default:
                    return null;
            }
        }

        return null;
    }

    private WorkflowReminder getReminder(WorkflowStartStop icon, ReminderType reminderType, AdminTimezoneSupplier adminTimezoneSupplier) {
        try {
            Date date = getReminderDate(icon, reminderType, adminTimezoneSupplier);

            // Never generate MISSING_START reminders in the past.
            if (reminderType == ReminderType.MISSING_START && DateUtilities.isPast(date)) {
                return null;
            }

            return WorkflowReminder.builder()
                    .type(reminderType)
                    .sender(icon.getSenderAdminId())
                    // For MISSING_START reminder a messages is always generated automatically.
                    .message(reminderType == ReminderType.MISSING_START ? null : icon.getComment())
                    .recipients(recipientsBuilder -> setReminderRecipients(recipientsBuilder, icon))
                    .date(date)
                    .build();
        } catch (Exception e) {
            logger.error("Cannot create reminder: " + e.getMessage(), e);
        }

        return null;
    }

    private Date getReminderDate(WorkflowStartStop icon, ReminderType reminderType, AdminTimezoneSupplier adminTimezoneSupplier) {
        TimeZone timezone = adminTimezoneSupplier.getTimezone(icon.getSenderAdminId());

        if (reminderType == ReminderType.MISSING_START || !icon.isRemindSpecificDate()) {
            return WorkflowUtils.getStartStopIconDate(icon, timezone);
        } else {
            return WorkflowUtils.getReminderSpecificDate(icon, timezone);
        }
    }

    private void setReminderRecipients(WorkflowReminder.RecipientsBuilder recipientsBuilder, WorkflowStartStop startStop) {
        String recipients = startStop.getRecipients();

        if (StringUtils.isEmpty(recipients)) {
            recipientsBuilder.recipient(startStop.getRemindAdminId()).end();
        } else {
            try {
                for (InternetAddress address : AgnUtils.getEmailAddressesFromList(recipients)) {
                    recipientsBuilder.recipient(address.getAddress());
                }
            } catch (Exception e) {
                logger.error("Error occurred: " + e.getMessage(), e);
            }
            recipientsBuilder.end();
        }
    }

    private boolean isCampaignTestFinished(WorkflowGraph workflowGraph, List<Integer> mailingTypes, Map<Integer, WorkflowMailingAware> mailingIconsByMailingId, Map<Integer, Mailing> mailings, Map<Integer, Boolean> sentMailings, int companyId) {
        Date now = new Date();
        Date startDate = getCampaignTestStartDate(workflowGraph, mailingTypes, mailings);

        if (startDate == null) {
            return false;
        }

        for (Mailing mailing: mailings.values()) {
            Date maxPossibleDateOfMailing = getMaxPossibleDateForTestRun(workflowGraph.findChains(mailingIconsByMailingId.get(mailing.getId()), false), startDate);
            if (now.before(maxPossibleDateOfMailing)) {
                return false;
            }

            if (!sentMailings.getOrDefault(mailing.getId(), false)) {
                String targetSQL = targetService.getSQLFromTargetExpression(mailing, false);
                int customersForMailing = workflowDao.countCustomers(companyId, mailing.getMailinglistID(), targetSQL);
                Date maxWaitForSendDate = DateUtils.addMinutes(maxPossibleDateOfMailing, TESTING_MODE_DEADLINE_DURATION);

                if (customersForMailing > 0 && now.before(maxWaitForSendDate)) {
                    return false;
                }
            }
        }

        return true;
    }

    private Date getCampaignTestStartDate(WorkflowGraph workflowGraph, List<Integer> mailingTypes, Map<Integer, Mailing> mailings) {
        WorkflowNode start = workflowGraph.getAllNodesByType(WorkflowIconType.START.getId()).get(0);
        WorkflowMailingAware mailingIcon = (WorkflowMailingAware) workflowGraph.getNextIconByType(start.getNodeIcon(), mailingTypes, Collections.emptySet(), false);
        Mailing mailing = mailings.get(mailingIcon.getMailingId());

        if (mailing == null) {
            return null;
        }

        return mailing.getPlanDate();
    }

    private Date getMaxPossibleDateForTestRun(List<List<WorkflowNode>> chains, Date testStartDate) {
        Date result = new Date(testStartDate.getTime());
        for (List<WorkflowNode> chain: chains) {
            Date chainMaxDate = getChainDateForTestRun(chain, testStartDate);
            if (chainMaxDate.after(result)) {
                result = chainMaxDate;
            }
        }
        return result;
    }

    private Date getChainDateForTestRun(List<WorkflowNode> chain, Date testStartDate) {
        Deadline deadline = new Deadline(testStartDate);
        if (CollectionUtils.isNotEmpty(chain)) {
            chain = new ArrayList<>(chain);
            Collections.reverse(chain);
            WorkflowIcon icon = chain.get(0).getNodeIcon();
            if (icon.getType() == WorkflowIconType.START.getId()) {
                for (WorkflowNode node: chain) {
                    if (node.getNodeIcon().getType() == WorkflowIconType.DEADLINE.getId()) {
                        deadline = deadline.add(TESTING_MODE_DEADLINE);
                    }
                }
            }
        }
        return new Date(deadline.getValue());
    }

    private WorkflowMailing getPreviousOptimizationTestMailing(WorkflowGraph graph, WorkflowIcon currentIcon) {
        final int decisionTypeId = WorkflowIconType.DECISION.getId();

        WorkflowDecision decision = (WorkflowDecision) graph.getPreviousIconByType(currentIcon, decisionTypeId, Collections.singleton(decisionTypeId));
        if (decision == null) {
            return null;
        }
        if (decision.getDecisionType() != WorkflowDecision.WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION) {
            return null;
        }
        return (WorkflowMailing) graph.getPreviousIconByType(decision, WorkflowIconType.MAILING.getId(), Collections.singleton(decisionTypeId));
    }

    @Override
    public List<List<WorkflowNode>> getChains(WorkflowIcon icon, List<WorkflowIcon> icons, boolean isForwardDirection) {
        WorkflowGraph graph = new WorkflowGraph();

        if (!graph.build(icons)) {
            return new ArrayList<>();
        }

        List<List<WorkflowNode>> chains = graph.findChains(icon, isForwardDirection);
        if (chains.size() == 0) {
            chains.add(Collections.singletonList(graph.getNodeByIcon(icon)));
        } else {
            for (List<WorkflowNode> chain : chains) {
                chain.add(0, graph.getNodeByIcon(icon));
            }
        }
        return chains;
    }

    @Override
    public Date getMaxPossibleDate(WorkflowIcon icon, List<WorkflowIcon> workflowIcons) {
        List<List<WorkflowNode>> chains = getChains(icon, workflowIcons, false);
        return getMaxPossibleDate(chains);
    }

    @Override
    public Date getMaxPossibleDate(List<List<WorkflowNode>> chains) {
        Date maxDate = null;
        if (CollectionUtils.isNotEmpty(chains)) {
            for (List<WorkflowNode> chain : chains) {
                if (CollectionUtils.isNotEmpty(chain)) {
                    Date date = getChainDate(chain);
                    if (date != null && (maxDate == null || date.after(maxDate))) {
                        maxDate = date;
                    }
                }
            }
        }
        return maxDate;
    }

    @Override
    public Date getChainDate(List<WorkflowNode> chain) {
        return getChainDate(chain, null);
    }

    @Override
    public Date getChainDate(List<WorkflowNode> chain, WorkflowIcon terminatingIcon) {
        TimeZone timezone = TimeZone.getDefault();
        Deadline deadline = null;
        if (CollectionUtils.isNotEmpty(chain)) {
            chain = new ArrayList<>(chain);
            Collections.reverse(chain);

            WorkflowIcon firstIcon = chain.get(0).getNodeIcon();
            // Required a filled start icon
            if (firstIcon.getType() != WorkflowIconType.START.getId() || !firstIcon.isFilled()) {
                return null;
            }

            WorkflowStart startIcon = (WorkflowStart) firstIcon;

            deadline = asDeadline(startIcon, timezone);

            for (WorkflowNode node : chain) {
                WorkflowIcon icon = node.getNodeIcon();

                if (icon == terminatingIcon) {
                    break;
                }

                if (icon.getType() == WorkflowIconType.DEADLINE.getId()) {
                    WorkflowDeadline deadlineIcon = (WorkflowDeadline) icon;

                    // Every deadline icon have to be filled
                    if (!icon.isFilled()) {
                        return null;
                    }

                    deadline = deadline.add(asDeadline(deadlineIcon, timezone));
                } else if (icon.getType() == WorkflowIconType.DECISION.getId()) {
                    WorkflowDecision decision = (WorkflowDecision) icon;

                    if (icon.isFilled() &&
                            decision.getDecisionType() == WorkflowDecision.WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION &&
                            decision.getDecisionDate() != null) {
                        Deadline decisionDeadline = new Deadline(decision.getDecisionDate());

                        // Ignore a decision if it's earlier than we already are
                        if (decisionDeadline.getValue() > deadline.getValue()) {
                            deadline = decisionDeadline;
                        }
                    }
                }
            }
        }
        return deadline == null ? null : new Date(deadline.getValue());
    }

	@Override
	public List<Workflow> getWorkflowsToDeactivate(CompaniesConstraints constraints) {
		return workflowDao.getWorkflowsToDeactivate(constraints);
	}

	@Override
	public List<Workflow> getWorkflowsToUnpause(CompaniesConstraints constraints) {
		return workflowDao.getWorkflowsToUnpause(constraints);
	}

    @Override
    public List<Workflow> getWorkflowsByIds(Set<Integer> workflowIds, int companyId) {
        return workflowDao.getWorkflows(workflowIds, companyId);
    }

    @Override
    public List<Integer> getWorkflowIdsByAssignedMailingId(int companyId, int mailingId) {
        return workflowDao.getWorkflowIdsByAssignedMailingId(companyId, mailingId);
    }

    @Override
    public boolean hasDeletedMailings(List<WorkflowIcon> icons, int companyId) {
        boolean hasDeleted = false;

        for (WorkflowIcon icon : icons) {
            if (WorkflowUtils.isMailingIcon(icon) || WorkflowUtils.isBranchingDecisionIcon(icon)) {
                WorkflowMailingAware mailingIcon = (WorkflowMailingAware) icon;
                int mailingId = mailingIcon.getMailingId();
                if (mailingId != 0 && !mailingDao.exist(mailingId, companyId)) {
                    mailingIcon.setMailingId(0);
                    mailingIcon.setIconTitle("");
                    mailingIcon.setFilled(false);
                    hasDeleted = true;
                }
            }
        }
        return hasDeleted;
    }

    @Override
    public List<WorkflowFollowupMailing> getFollowupMailingIcon(List<WorkflowIcon> workflowIcons) {
        List<WorkflowFollowupMailing> list = new ArrayList<>();
        for (WorkflowIcon icon : workflowIcons) {
            if (icon.getType() == WorkflowIconType.FOLLOWUP_MAILING.getId()) {
                list.add((WorkflowFollowupMailing) icon);
            }
        }
        return list;
    }

    @Override
    public boolean isAdditionalRuleDefined(int companyId, int mailingId, int workflowId) {
        boolean result = false;
        Workflow workflow = getWorkflow(workflowId, companyId);
        List<WorkflowIcon> icons = workflow.getWorkflowIcons();

        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.DATE_BASED_MAILING.getId()) {
                WorkflowGraph workflowGraph = new WorkflowGraph();
                if (workflowGraph.build(icons) && workflowValidationService.noLoops(icons)) {
                    HashSet<Integer> terminateTypes = new HashSet<>();

                    // found start
                    WorkflowIcon prevStartIcon = workflowGraph.getPreviousIconByType(icon, WorkflowIconType.START.getId(), terminateTypes);
                    if (prevStartIcon != null) {
                        WorkflowStart start = (WorkflowStart) prevStartIcon;
                        if (start.getStartType() == WorkflowStart.WorkflowStartType.EVENT && start.getEvent() == WorkflowStartEventType.EVENT_DATE) {
                            result = true;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean checkReactionNeedsActiveBinding(ComWorkflowReaction reaction) {
        switch (reaction.getReactionType()) {
            case OPT_OUT:
            case WAITING_FOR_CONFIRM:
                return false;
            default: return true;
        }
    }

    @Override
    public List<UserStatus> getProperUserStatusList(ComWorkflowReaction reaction) {
        switch (reaction.getReactionType()) {
            case OPT_OUT:
                return new ArrayList<>(Arrays.asList(UserStatus.UserOut, UserStatus.AdminOut));

            case WAITING_FOR_CONFIRM:
                return new ArrayList<>(Collections.singletonList(UserStatus.WaitForConfirm));

            default:
                return null;
        }
    }

    @Override
    public ComWorkflowReaction getWorkflowReaction(int workflowId, int companyId) {
        int reactionId = reactionDao.getReactionId(workflowId, companyId);
        if (reactionId > 0) {
            return reactionDao.getReaction(reactionId, companyId);
        }
        return null;
    }

    @Override
    public List<Integer> getReactedRecipients(ComWorkflowReaction reaction, boolean excludeLoggedReactions) {
        switch (reaction.getReactionType()) {
            case CLICKED:
            case CLICKED_LINK:
                return reactionDao.getClickedRecipients(reaction, excludeLoggedReactions);

            case OPENED:
                return reactionDao.getOpenedRecipients(reaction, excludeLoggedReactions);

            case CHANGE_OF_PROFILE:
                return reactionDao.getRecipientsWithChangedProfile(reaction, excludeLoggedReactions);

            case OPT_IN:
            case OPT_OUT:
            case WAITING_FOR_CONFIRM:
                return reactionDao.getRecipientsWithChangedBinding(reaction, excludeLoggedReactions);
            default:
                return Collections.emptyList();
        }
    }

    @Override
    public void processPendingReactionSteps(CompaniesConstraints constraints) {
        List<WorkflowReactionStep> stepsToMake = reactionDao.getStepsToMake(constraints);

        stepsToMake.stream()
            .collect(Collectors.groupingBy(ReactionId::new))
            .forEach((rc, steps) -> processReactionSteps(rc.getCompanyId(), rc.getReactionId(), steps));

        if (stepsToMake.size() > 0) {
            // Useless steps couldn't appear unless some steps has been processed.
            reactionDao.setUselessStepsDone(constraints);
        }
    }

    @Override
    public List<Workflow> getActiveWorkflowsTrackingProfileField(String column, int companyId) {
        return workflowDao.getActiveWorkflowsTrackingProfileField(column, companyId);
    }

    @Override
    public List<Workflow> getActiveWorkflowsDependentOnProfileField(String column, int companyId) {
        return workflowDao.getActiveWorkflowsUsingProfileField(column, companyId);
    }

    @Override
    public List<Workflow> getActiveWorkflowsDrivenByProfileChange(int companyId, int mailingListId, String column, List<WorkflowRule> rules) {
        boolean isUseRules = CollectionUtils.isNotEmpty(rules);

        List<Workflow> workflows = workflowDao.getActiveWorkflowsDrivenByProfileChange(companyId, mailingListId, column, isUseRules);

        if (isUseRules) {
            // Exclude workflows using different rules.
            workflows.removeIf(w -> !compareRules(w, rules));
        }

        // Order by id, descending
        workflows.sort((w1, w2) -> Integer.compare(w2.getWorkflowId(), w1.getWorkflowId()));

        return workflows;
    }

    private boolean loadIcons(Workflow workflow) {
        if (workflow.getWorkflowIcons() == null) {
            List<WorkflowIcon> icons = getIcons(workflow.getWorkflowSchema());
            workflow.setWorkflowIcons(icons);
            return icons.size() > 0;
        }

        return true;
    }

    private boolean compareRules(Workflow workflow, List<WorkflowRule> rules) {
        if (loadIcons(workflow)) {
            for (WorkflowIcon icon : workflow.getWorkflowIcons()) {
                if (icon.getType() == WorkflowIconType.START.getId()) {
                    WorkflowStart start = (WorkflowStart) icon;

                    if (WorkflowUtils.is(start, WorkflowReactionType.CHANGE_OF_PROFILE) && rules.equals(start.getRules())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public List<Campaign> getCampaignList(int companyId, String sort, int order) {
        return campaignDao.getCampaignList(companyId, sort, order);
    }

    @Override
    public void setTargetConditionDependency(ComTarget target, int companyId, int workflowId) {
        int targetId = target.getId();
        if(targetId > 0) {
            workflowDao.addDependency(companyId, workflowId,
                    WorkflowDependency.from(WorkflowDependencyType.TARGET_GROUP_CONDITION, targetId));
        }
    }

    @Override
    public void deleteWorkflowTargetConditions(int companyId, int workflowId) {
		targetDao.deleteWorkflowTargetConditions(companyId, workflowId);
		workflowDao.deleteTargetConditionDependencies(companyId, workflowId);
	}

    @Override
    public List<Workflow> getDependentWorkflowOnMailing(int companyId, int mailingId) {
        final Collection<WorkflowDependency> dependencies =
                Arrays.asList(MAILING_DELIVERY.forId(mailingId), MAILING_LINK.forId(mailingId), MAILING_REFERENCE.forId(mailingId));
        return workflowDao.getDependentWorkflows(companyId, dependencies, false);
    }

    @Override
    public JSONArray getWorkflowListJson(Admin admin) {
        JSONArray mailingListsJson = new JSONArray();
        String dateTimePattern = admin.getDateTimeFormat().toPattern();

        for (Workflow workflow : getWorkflowsOverview(admin)) {
            mailingListsJson.element(getWorkflowListJsonEntry(dateTimePattern, workflow, admin.isRedesignedUiUsed()));
        }
        return mailingListsJson;
    }

    private JSONObject getWorkflowListJsonEntry(String dateTimePattern, Workflow workflow, boolean redesign) {
        JSONObject entry = new JSONObject();
        List<WorkflowIcon> icons = workflowDataParser.deSerializeWorkflowIconsList(workflow.getWorkflowSchema());

        entry.element("id", workflow.getWorkflowId());
        if (redesign) {
            entry.element("status", WorkflowForm.WorkflowStatus.valueOf(workflow.getStatusString()).getName());
        } else {
            entry.element("status", getWorkflowStatusJson(workflow));
        }
        entry.element("shortname", workflow.getShortname());
        entry.element("description", workflow.getDescription());
        entry.element("startDate", getWorkflowDateJson(workflow.getGeneralStartEvent(), icons, dateTimePattern, true, redesign));
        entry.element("stopDate", getWorkflowDateJson(workflow.getEndType(), icons, dateTimePattern, false, redesign));
        entry.element("reaction", getWorkflowReactionJson(workflow));
        return entry;
    }

    private JSONObject getWorkflowReactionJson(Workflow workflow) {
        JSONObject reactionJson = new JSONObject();
        String iconClass = "";
        String name = "";
        if (workflow.getGeneralStartReaction() != null) {
            iconClass = workflow.getGeneralStartReaction().getIconClass();
            name = workflow.getGeneralStartReaction().getName();
        }
        reactionJson.element("iconClass", iconClass);
        reactionJson.element("name", name);
        return reactionJson;
    }

    // TODO: EMMGUI-714 remove after remove of old design
    private JSONObject getWorkflowStatusJson(Workflow workflow) {
        JSONObject statusJson = new JSONObject();
        WorkflowForm.WorkflowStatus status = WorkflowForm.WorkflowStatus.valueOf(workflow.getStatusString());
        statusJson.element("name", status.getName());
        statusJson.element("messageKey", status.getMessageKey());
        return statusJson;
    }

    private JSONObject getWorkflowDateJson(IntEnum type, List<WorkflowIcon> icons, String dateTimePattern, boolean start, boolean redesign) {
        JSONObject dateJson = new JSONObject();
        dateJson.element("date", redesign
                ? getDateStrFromIconsRedesigned(icons, start)
                : getDateStrFromIcons(icons, dateTimePattern, start));
        if (redesign) {
            dateJson.element("type", type);
        } else {
            dateJson.element((start ? "start" : "end") + "TypeId", type != null ? type.getId() : -1);
        }
        return dateJson;
    }

    private String getDateStrFromIcons(List<WorkflowIcon> icons, String pattern, boolean start) {
        Optional<WorkflowIcon> iconOptional = icons.stream()
                .filter(i -> i.getType() == (start ? WorkflowIconType.START.getId() : WorkflowIconType.STOP.getId()))
                .findFirst();
        if (iconOptional.isPresent()) {
            Date date = ((WorkflowStartStop) iconOptional.get()).getDate();
            return date != null ? new SimpleDateFormat(pattern).format(date.getTime()) : "";
        }
        return "";
    }

    private Object getDateStrFromIconsRedesigned(List<WorkflowIcon> icons, boolean start) {
        Optional<WorkflowIcon> iconOptional = icons.stream()
                .filter(i -> i.getType() == (start ? WorkflowIconType.START.getId() : WorkflowIconType.STOP.getId()))
                .findFirst();
        if (iconOptional.isPresent()) {
            Date date = ((WorkflowStartStop) iconOptional.get()).getDate();
            return date != null ? date.getTime() : "";
        }
        return "";
    }

    private void processReactionSteps(int companyId, int reactionId, List<WorkflowReactionStep> steps) {
        ComWorkflowReaction reaction = reactionDao.getReaction(reactionId, companyId);

        // Process steps, apply filters (decisions + mailing target expression + mailing list + list split) and collect mailings (and recipients) to send.
        Map<Integer, List<Integer>> schedule = processReactionSteps(reaction, steps);

        if (schedule.size() > 0) {
            // List of binding statuses (or empty list for default behavior defined by back-end) that the recipient binding
            // should have (otherwise recipient won't receive mails).
            List<UserStatus> userStatuses = getProperUserStatusList(reaction);

            // Send mails to recipients according to processed reaction steps.
            schedule.forEach((mailingId, recipientIds) -> send(reaction, mailingId, recipientIds, userStatuses));
        }
    }

    private Map<Integer, List<Integer>> processReactionSteps(ComWorkflowReaction reaction, List<WorkflowReactionStep> steps) {
        // Maps mailingId -> recipientIds[]
        Map<Integer, List<Integer>> schedule = new HashMap<>();

        // Whether or not inactive recipients (having inactive binding status) should be excluded.
        boolean ensureActive = checkReactionNeedsActiveBinding(reaction);

        // Follow pre-determined execution order (sort by stepId) to make sure that dependent step will be processed
        // after the step it depends on.
        // Keep in mind that all these steps belong to the same reaction but they may have different caseId.
        steps.sort(Comparator.comparingInt(WorkflowReactionStepDeclaration::getStepId));

        MailingTargetSupplier targetSupplier = new MailingTargetCachingSupplier();

        for (WorkflowReactionStep step : steps) {
            String sqlStepTargetExpression = null;

            // Target group assigned to step refers to decisions and sequence control (to make sure that previous mailing is sent).
            if (step.getTargetId() > 0) {
                sqlStepTargetExpression = targetService.getTargetSQL(step.getTargetId(), step.getCompanyId(), step.isTargetPositive());
                // If specified target group doesn't exist then campaign "stops" at this step (no recipients).
                if (sqlStepTargetExpression == null) {
                    logger.error("The target group #" + step.getTargetId() + " is invalid or missing, required at step #" + step.getStepId() + " (reactionId:" + step.getReactionId() + ")");
                    sqlStepTargetExpression = "1=0";
                }
            }

            if (step.getMailingId() > 0) {
                // If mailing should be sent at this step than we need to filter recipients first (mailing list + target expression + list split).
                String sqlMailingTargetExpression = targetSupplier.getTargetAndListSplitSqlCode(step.getMailingId(), step.getCompanyId());
                int mailingListId = targetSupplier.getMailingListId(step.getMailingId(), step.getCompanyId());

                reactionDao.setStepDone(step, mailingListId, ensureActive, combineTargetExpressions(sqlStepTargetExpression, sqlMailingTargetExpression));

                // Retrieve all the recipients who reached this step.
                List<Integer> recipientIds = reactionDao.getStepRecipients(step);

                if (recipientIds.size() > 0) {
                    // Schedule mailing to be sent.
                    schedule.computeIfAbsent(step.getMailingId(), mailingId -> new ArrayList<>())
                        .addAll(recipientIds);
                }
            } else {
                if (sqlStepTargetExpression == null) {
                    reactionDao.setStepDone(step);
                } else {
                    reactionDao.setStepDone(step, sqlStepTargetExpression);
                }
            }
        }

        return schedule;
    }

    private String combineTargetExpressions(String sqlStepTargetExpression, String sqlMailingTargetExpression) {
        if (sqlStepTargetExpression == null && sqlMailingTargetExpression == null) {
            return null;
        }

        if (sqlStepTargetExpression == null) {
            return sqlMailingTargetExpression;
        } else if (sqlMailingTargetExpression == null) {
            return sqlStepTargetExpression;
        } else {
            return String.format("(%s) AND (%s)", sqlStepTargetExpression, sqlMailingTargetExpression);
        }
    }

    private void send(ComWorkflowReaction reaction, int mailingId, List<Integer> recipientIds, List<UserStatus> allowedUserStatuses) {
        for (int recipientId : recipientIds) {
            send(reaction, mailingId, recipientId, allowedUserStatuses);
        }
    }

    private void send(ComWorkflowReaction reaction, int mailingId, int recipientId, List<UserStatus> allowedUserStatuses) {
        try {
            MailgunOptions options = new MailgunOptions();
            options.withAllowedUserStatus(allowedUserStatuses);
        	sendActionbasedMailingService.sendActionbasedMailing(reaction.getCompanyId(), mailingId, recipientId, 0, options);
        } catch (SendActionbasedMailingException e) {
            // todo #monitor?
            logger.error("WM Reaction: error (reactionId: " + reaction.getReactionId() + ", workflowId: " + reaction.getWorkflowId() + "): " + e.getMessage(), e);
        }
    }

    private boolean deactivateMailing(WorkflowMailingAware icon, boolean testing, int companyId, List<Message> errors) {
        int mailingId = WorkflowUtils.getMailingId(icon);
        boolean deactivated = false;

        if (mailingId > 0) {
            switch (WorkflowIconType.fromId(icon.getType(), false)) {
                case MAILING:
                case FOLLOWUP_MAILING:
                    tryDeactivateMailing(mailingId, companyId, errors);

                    // Leave status "test" for mailing if that was the test run.
                    if (!testing) {
                        deactivated = mailingDao.updateStatus(companyId, mailingId, MailingStatus.CANCELED, null);
                    }
                    break;

                case ACTION_BASED_MAILING:
                case DATE_BASED_MAILING:
                    deactivateMailing(mailingId, companyId);

                    // Leave status "test" for mailing if that was the test run.
                    if (!testing) {
                        deactivated = mailingDao.updateStatus(companyId, mailingId, MailingStatus.DISABLE, null);
                    }
                    break;
				case ARCHIVE:
					break;
				case DEADLINE:
					break;
				case DECISION:
					break;
				case EXPORT:
					break;
				case FORM:
					break;
				case IMPORT:
					break;
				case PARAMETER:
					break;
				case RECIPIENT:
					break;
				case START:
					break;
				case STOP:
					break;
				default:
					break;
            }
        }
        return deactivated;
    }

    private void deactivateAutoImport(WorkflowImport icon, int companyId) throws Exception {
        if (autoImportService != null) {
            AutoImport autoImport = autoImportService.getAutoImport(icon.getImportexportId(), companyId);
            if (autoImport.isDeactivateByCampaign()) {
                autoImportService.deactivateAutoImport(companyId, icon.getImportexportId());
            }
        }
    }

    private void deactivateAutoExport(WorkflowExport icon, int companyId) {
        if (autoExportService != null) {
            AutoExport autoExport = autoExportService.getAutoExport(icon.getImportexportId(), companyId);
            if (autoExport.isDeactivateByCampaign()) {
                autoExportService.changeAutoExportActiveStatus(icon.getImportexportId(), companyId, false);
            }
        }
    }

    private void tryDeactivateMailing(int mailingId, int companyId, List<Message> errors) {
        try {
            deactivateMailing(mailingId, companyId);
        } catch (MailingNotExistException ex) {
            errors.add(Message.of("error.workflow.containsDeletedContent"));
        }
    }

    private void deactivateMailing(int mailingId, int companyId) {
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);
        mailingDeliveryBlockingService.unblock(mailingId);
        maildropStatusDao.cleanup(mailing.getMaildropStatus());
    }

    @Required
    public void setWorkflowDao(ComWorkflowDao workflowDao) {
        this.workflowDao = workflowDao;
    }

    @Required
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

    public MailingDao getMailingDao() {
        return mailingDao;
    }

    @Required
	public void setColumnInfoService(ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}

    @Required
	public void setAdminService(AdminService service) {
		this.adminService = Objects.requireNonNull(service, "Admin service is null");
	}

    @Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}

    @Required
    public void setTargetService(ComTargetService targetService) {
        this.targetService = targetService;
    }

    @Required
    public void setUserFormDao(UserFormDao userFormDao) {
        this.userFormDao = userFormDao;
    }

    @Required
    public void setBirtCompanyDao(ComCompanyDao birtCompanyDao) {
        this.birtCompanyDao = birtCompanyDao;
    }

    @Required
	public void setReactionDao(ComWorkflowReactionDao reactionDao) {
		this.reactionDao = reactionDao;
	}

    @Required
    public void setWorkflowValidationService(ComWorkflowValidationService workflowValidationService) {
        this.workflowValidationService = workflowValidationService;
    }

    @Required
    public void setMailingDeliveryBlockingService(MailingDeliveryBlockingService mailingDeliveryBlockingService) {
        this.mailingDeliveryBlockingService = mailingDeliveryBlockingService;
    }

    public void setAutoImportService(AutoImportService autoImportService) {
        this.autoImportService = autoImportService;
    }

    public void setAutoExportService(AutoExportService autoExportService) {
        this.autoExportService = autoExportService;
    }

    @Required
    public void setMaildropStatusDao(MaildropStatusDao maildropStatusDao) {
        this.maildropStatusDao = maildropStatusDao;
    }

    @Required
    public void setOptimizationService(ComOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @Required
    public void setMediatypesDao(MediatypesDao mediatypesDao) {
        this.mediatypesDao = mediatypesDao;
    }

    @Required
    public void setOptimizationCommonService(ComOptimizationCommonService optimizationCommonService) {
        this.optimizationCommonService = optimizationCommonService;
    }

    @Required
    public void setSendActionbasedMailingService(SendActionbasedMailingService sendActionbasedMailingService) {
        this.sendActionbasedMailingService = sendActionbasedMailingService;
    }

    @Required
    public void setProfileFieldDao(ProfileFieldDao profileFieldDao) {
        this.profileFieldDao = profileFieldDao;
    }

    @Required
    public void setWorkflowDataParser(ComWorkflowDataParser workflowDataParser) {
        this.workflowDataParser = workflowDataParser;
    }

    @Required
    public void setMailingService(MailingService mailingService) {
        this.mailingService = mailingService;
    }

    @Required
    public void setBulkActionValidationService(BulkActionValidationService<Integer, Workflow> bulkActionValidationService) {
        this.bulkActionValidationService = bulkActionValidationService;
    }

    @Required
    public void setWorkflowActivationService(ComWorkflowActivationService workflowActivationService) {
        this.workflowActivationService = workflowActivationService;
    }

    @Required
    public void setWorkflowStartStopReminderDao(ComWorkflowStartStopReminderDao reminderDao) {
        this.reminderDao = reminderDao;
    }

    @Required
    public void setReminderService(ComReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Required
    public void setReminderDao(ComWorkflowStartStopReminderDao reminderDao) {
        this.reminderDao = reminderDao;
    }

    @Required
    public void setCampaignDao(CampaignDao campaignDao) {
        this.campaignDao = campaignDao;
    }

    @Required
    public final void setSelfReference(final ComWorkflowService service) {
    	this.selfReference = Objects.requireNonNull(service, "Self reference is null");
    }

    public final ComWorkflowService getSelfReference() {
    	return this.selfReference;
    }

    private static class ReactionId {
        private int companyId;
        private int reactionId;

        public ReactionId(WorkflowReactionStep step) {
            this.companyId = step.getCompanyId();
            this.reactionId = step.getReactionId();
        }

        public int getCompanyId() {
            return companyId;
        }

        public int getReactionId() {
            return reactionId;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }

            if (ReactionId.class == o.getClass()) {
                ReactionId other = (ReactionId) o;
                return companyId == other.companyId && reactionId == other.reactionId;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return (companyId + "@" + reactionId).hashCode();
        }
    }

    private static class Chain {
        private WorkflowStart start;
        private List<WorkflowRecipient> recipients;
        private List<WorkflowDeadline> deadlines;
        private List<WorkflowParameter> parameters;
        private WorkflowArchive archive;

        public Chain() {
            this.recipients = new ArrayList<>();
            this.deadlines = new ArrayList<>();
            this.parameters = new ArrayList<>();
        }

        public Chain(Chain chain) {
            this.start = chain.start;
            this.recipients = new ArrayList<>(chain.recipients);
            this.deadlines = new ArrayList<>(chain.deadlines);
            this.parameters = new ArrayList<>(chain.parameters);
            this.archive = chain.archive;
        }

        public void append(WorkflowStart startToAdd) {
            this.start = startToAdd.isFilled() ? startToAdd : null;
            this.recipients.clear();
            this.deadlines.clear();
            this.parameters.clear();
            this.archive = null;
        }

        public void append(WorkflowRecipient recipient) {
            if (recipient.isFilled()) {
                recipients.add(recipient);
            }
        }

        public void append(WorkflowDeadline deadline) {
            if (deadline.isFilled()) {
                deadlines.add(deadline);
            }
        }

        public void append(WorkflowParameter parameter) {
            if (parameter.isFilled()) {
                parameters.add(parameter);
            }
        }

        public void append(WorkflowArchive archiveToAdd) {
            if (archiveToAdd.isFilled()) {
                this.archive = archiveToAdd;
            }
        }

        public Date getDate(TimeZone timezone) {
            Deadline deadline = null;

            if (start != null) {
                deadline = WorkflowUtils.asDeadline(start, timezone);
            }

            for (WorkflowDeadline icon : deadlines) {
                Deadline nextDeadline = WorkflowUtils.asDeadline(icon, timezone);

                if (deadline == null) {
                    deadline = nextDeadline;
                } else {
                    deadline = deadline.add(nextDeadline);
                }
            }

            if (deadline == null || deadline.isRelative()) {
                return null;
            }

            return new Date(deadline.getValue());
        }

        public int getMailingListId() {
            int mailingListId = 0;

            for (WorkflowRecipient recipient : recipients) {
                if (recipient.getMailinglistId() > 0) {
                    mailingListId = recipient.getMailinglistId();
                    break;
                }
            }

            return mailingListId;
        }

        public String getTargetExpression() {
            List<String> expressions = new ArrayList<>(recipients.size());

            for (WorkflowRecipient recipient : recipients) {
                List<Integer> altgIds = recipient.getAltgs();
                List<Integer> targetIds = recipient.getTargets();
                if (CollectionUtils.isNotEmpty(altgIds)) {
                    expressions.add(TargetExpressionUtils.makeTargetExpressionWithAltgs(altgIds, targetIds, recipient.getTargetsOption()));
                } else {
                    if (CollectionUtils.isNotEmpty(targetIds)) {
                        expressions.add(TargetExpressionUtils.makeTargetExpression(targetIds, recipient.getTargetsOption()));
                    }
                }
            }

            if (expressions.size() > 1) {
                // Wrap separate expressions with brackets if they use OR operator.
                expressions.replaceAll(e -> e.contains("|") ? ("(" + e + ")") : (e));
            }

            return StringUtils.join(expressions, '&');
        }

        public WorkflowArchive getArchive() {
            return archive;
        }
    }

    private interface MailingTargetSupplier {
        String getTargetAndListSplitSqlCode(int mailingId, int companyId);
        int getMailingListId(int mailingId, int companyId);
    }

    private class MailingTargetCachingSupplier implements MailingTargetSupplier {
        private Map<Integer, String> sqlCodeMap = new HashMap<>();
        private Map<Integer, Integer> mailingListMap = new HashMap<>();

        @Override
        public String getTargetAndListSplitSqlCode(int mailingId, int companyId) {
            return sqlCodeMap.computeIfAbsent(mailingId, id -> targetService.getMailingSqlTargetExpression(id, companyId, true));
        }

        @Override
        public int getMailingListId(int mailingId, int companyId) {
            return mailingListMap.computeIfAbsent(mailingId, id -> mailingDao.getMailinglistId(id, companyId));
        }
    }

    private interface AdminTimezoneSupplier {
        TimeZone getTimezone(int adminId);
    }

    private class AdminTimezoneCachingSupplier implements AdminTimezoneSupplier {
        private final int companyId;
        private final Map<Integer, TimeZone> timezoneMap = new HashMap<>();

        public AdminTimezoneCachingSupplier(int companyId) {
            if (companyId <= 0) {
                throw new IllegalArgumentException("companyId <= 0");
            }

            this.companyId = companyId;
        }

        @Override
        public TimeZone getTimezone(int adminId) {
            return timezoneMap.computeIfAbsent(adminId, id -> {
            	final Admin admin = adminService.getAdmin(adminId, companyId);
                final String timezone = admin != null ? admin.getAdminTimezone() : null;
                return TimeZone.getTimeZone(timezone);
            });
        }
    }

    private interface EntitiesSupplier {
        Mailing getMailing(int mailingId);
        AutoImport getAutoImport(int autoImportId);
    }

    private class CachingEntitiesSupplier implements EntitiesSupplier, AutoCloseable {
        private final int companyId;
        private Map<Integer, Mailing> mailingMap = new HashMap<>();
        private Map<Integer, AutoImport> autoImportMap = new HashMap<>();

        public CachingEntitiesSupplier(int companyId) {
            this.companyId = companyId;
        }

        @Override
        public Mailing getMailing(int mailingId) {
            return mailingMap.computeIfAbsent(mailingId, this::loadMailing);
        }

        private Mailing loadMailing(int mailingId) {
            Mailing mailing = mailingDao.getMailing(mailingId, companyId);

            if (mailing != null && mailing.getId() > 0) {
                return mailing;
            }

            return null;
        }

        @Override
        public AutoImport getAutoImport(int autoImportId) {
            return autoImportMap.computeIfAbsent(autoImportId, this::loadAutoImport);
        }

        private AutoImport loadAutoImport(int autoImportId) {
            if (autoImportService != null) {
                return autoImportService.getAutoImport(autoImportId, companyId);
            } else {
                return null;
            }
        }

        @Override
        public void close() throws Exception {
            for (Mailing mailing : mailingMap.values()) {
                if (mailing != null) {
                    mailingDao.saveMailing(mailing, false);
                }
            }

            for (AutoImport autoImport : autoImportMap.values()) {
                if (autoImport != null) {
                    autoImportService.saveAutoImport(autoImport);
                }
            }

            mailingMap.clear();
            autoImportMap.clear();
        }
    }

    @Override
    public boolean isLinkUsedInActiveWorkflow(TrackableLink link) {
        return reactionDao.isLinkUsedInActiveWorkflow(link);
    }

    @Override
    public void savePausedSchemaForUndo(Workflow workflow, int adminId) {
        workflowDao.savePausedSchemaForUndo(workflow, adminId);
    }

    @Override
    public void savePausedSchemaForUndo(Workflow workflow) {
        savePausedSchemaForUndo(workflow, getWorkflowSenderId(workflow));
    }

    @Override
    public int getWorkflowSenderId(Workflow workflow) {
        String schema = workflow.getWorkflowSchema();
        List<WorkflowIcon> icons = getIcons(schema);

        Optional<WorkflowStartStop> startOrStopIcon = icons.stream()
                .filter(i -> i.getType() == WorkflowIconType.START.getId() || i.getType() == WorkflowIconType.STOP.getId())
                .map(i -> ((WorkflowStartStop) i))
                .findFirst();

        if (startOrStopIcon.isEmpty()) {
            throw new IllegalStateException("Can't find start or stop icons to pause workflow!");
        }

        return startOrStopIcon.get().getSenderAdminId();
    }

    @Override
    public String getSchemaBeforePause(int workflowId, int companyId) {
        return workflowDao.getSchemaBeforePause(workflowId, companyId);
    }

    @Override
    public Date getPauseDate(int workflowId, int companyId) {
        return workflowDao.getPauseDate(workflowId, companyId);
    }

    @Override
    public void deletePauseUndoEntry(int workflowId, int companyId) {
        workflowDao.deletePauseUndoEntry(workflowId, companyId);
    }

    @Override
    public Admin getPauseAdmin(int workflowId, int companyId) {
        int pauseAdminId = workflowDao.getPauseAdminId(workflowId, companyId);
        return adminService.getAdmin(pauseAdminId, companyId);
   }

    @Override
    public String getInitialWorkflowSchema() {
        WorkflowStart start = new WorkflowStartImpl();
        start.setId(1);
        start.setX(8);
        start.setY(6);

        WorkflowRecipient recipient = new WorkflowRecipientImpl();
        recipient.setId(2);
        recipient.setX(start.getX() + 5);
        recipient.setY(start.getY());

        start.setConnections(List.of(new WorkflowConnectionImpl(recipient.getId())));
        return workflowDataParser.serializeWorkflowIcons(List.of(start, recipient));
    }

    @Override
    public boolean isAutoExportManagedByWorkflow(int autoExportId) {
        return workflowDao.isDependencyExists(autoExportId, WorkflowDependencyType.AUTO_EXPORT);
    }

    @Override
    public boolean isAutoImportManagedByWorkflow(int autoImportId) {
        return workflowDao.isDependencyExists(autoImportId, WorkflowDependencyType.AUTO_IMPORT);
    }
}
