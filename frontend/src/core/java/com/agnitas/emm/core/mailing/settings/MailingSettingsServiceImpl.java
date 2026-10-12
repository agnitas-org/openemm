/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailing.settings;

import static com.agnitas.emm.core.mailing.dao.MailingParameterDao.ReservedMailingParam.isReservedParam;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediaTypeStatus;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.factory.MailingFactory;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.dto.PatchMailingSettingsCommand;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingModel;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.validation.MailingValidator;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.factory.MediatypeFactory;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.UserMessageException;
import com.agnitas.util.DynTagException;
import com.agnitas.util.MissingEndTagException;
import com.agnitas.util.UnclosedTagException;
import com.agnitas.web.perm.NotAllowedActionException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailingSettingsServiceImpl implements MailingSettingsService {

    private static final Logger LOGGER = LogManager.getLogger(MailingSettingsServiceImpl.class);

    protected final TargetService targetService;
    protected final ConfigService configService;
    private final MailingService mailingService;
    private final MailingBaseService mailingBaseService;
    private final ApplicationContext applicationContext;
    private final DynamicTagDao dynamicTagDao;
    private final MailingValidator mailingValidator;
    private final MailingFactory mailingFactory;
    private final MediatypeFactory mediatypeFactory;

    public MailingSettingsServiceImpl(
            MailingService mailingService,
            MailingBaseService mailingBaseService,
            ApplicationContext applicationContext,
            DynamicTagDao dynamicTagDao,
            MailingValidator mailingValidator,
            TargetService targetService,
            ConfigService configService,
            MailingFactory mailingFactory,
            MediatypeFactory mediatypeFactory
    ) {
        this.mailingService = mailingService;
        this.mailingBaseService = mailingBaseService;
        this.applicationContext = applicationContext;
        this.dynamicTagDao = dynamicTagDao;
        this.mailingValidator = mailingValidator;
        this.targetService = targetService;
        this.configService = configService;
        this.mailingFactory = mailingFactory;
        this.mediatypeFactory = mediatypeFactory;
    }

    @Override
    public ServiceResult<Integer> createMailing(PatchMailingSettingsCommand command, Admin admin) {
        return createMailing(command, admin, 0, false);
    }

    @Override
    public ServiceResult<Integer> createStandardTemplate(PatchMailingSettingsCommand command, Admin admin) {
        return createMailing(command, admin, 0, true);
    }

    @Override
    public ServiceResult<Integer> createMailing(
            PatchMailingSettingsCommand command,
            Admin admin,
            int workflowId,
            boolean isTemplate
    ) {
        validateAllowed(admin, isTemplate);

        Mailing mailing = generateRawMailing(admin);
        mailing.setIsTemplate(isTemplate);

        return ServiceResult.from(trySaveSettings(mailing, command, admin), mailing.getId());
    }

    public Mailing generateRawMailing(Admin admin) {
        Mailing mailing = mailingFactory.newMailing();
        mailing.init(admin.getCompanyID(), applicationContext);
        return mailing;
    }

    @Transactional
    @Override
    public SimpleServiceResult updateSettings(int mailingId, PatchMailingSettingsCommand command, Admin admin) {
        Mailing mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
        validateAllowed(admin, mailing.isIsTemplate());
        SimpleServiceResult result = trySaveSettings(mailing, command, admin);
        mailingService.updateStatus(admin.getCompanyID(), mailing.getId(), MailingStatus.EDIT);
        return result;
    }

    private void validateAllowed(Admin admin, boolean isTemplate) {
        if (mailingService.isSettingsReadonly(admin, isTemplate)) {
            throw new NotAllowedActionException();
        }
    }

    private void syncMediatypeTemplates(Mailing mailing) {
        for (Mediatype mediatype : mailing.getMediatypes().values()) {
            if (mediatype != null && mediatype.getStatus() == MediaTypeStatus.Active.getCode()) {
                mediatype.syncTemplate(mailing, applicationContext);
            }
        }
    }

    protected SimpleServiceResult trySaveSettings(Mailing mailing, PatchMailingSettingsCommand command, Admin admin) {
        try {
            if (mailingService.isActiveOrSent(mailing)) {
                mailingValidator.validateContainsOnlyAlwaysAllowedChanges(mailing, command);
                command.name().ifPresent(mailing::setShortname);
                command.description().ifPresent(mailing::setDescription);
                command.archiveId().ifPresent(mailing::setArchived);
                command.archived().ifPresent(b -> mailing.setArchived(Boolean.TRUE.equals(b) ? 1 : 0));
                mailingService.saveMailingDescriptiveData(mailing);
                return SimpleServiceResult.simpleSuccess();
            }
            setMailingProperties(mailing, command, admin);
            List<Message> warnings = new ArrayList<>();
            List<Message> errors = new ArrayList<>();

            List<String> dynNamesForDeletion = new ArrayList<>();
            mailing.buildDependencies(true, dynNamesForDeletion, applicationContext, warnings, errors, admin.getLocale());
            dynamicTagDao.markNamesAsDeleted(mailing.getId(), dynNamesForDeletion);
            if (Mediatype.isActive(mailing.getEmailParam())) {
                warnings.addAll(mailingBaseService.doTextTemplateFilling(mailing, admin));
            }
            SimpleServiceResult result = SimpleServiceResult
                    .of(warnings, errors)
                    .merge(mailingValidator.validateForSave(mailing, admin))
                    .throwIfFailed();

            saveMailing(mailing, admin);

            return result; // may contain warnings
        } catch (ValidationException e) {
            throw e;
        } catch (MissingEndTagException e) {
            LOGGER.info("Missing end tag", e);
            throw new ValidationException("error.template.dyntags.missing_end_tag", e.getLineNumber(), e.getTag());
        } catch (UnclosedTagException e) {
            LOGGER.info("Unclosed tag", e);
            throw new ValidationException("error.template.dyntags.unclosed_tag", e.getTag());
        } catch (DynTagException e) {
            throw new ValidationException("error.mailing.content");
        } catch (UserMessageException e) {
            throw new ValidationException(e.getErrorMessageKey(), e.getAdditionalErrorData());
        } catch (Exception e) {
            LOGGER.error("Error in save mailing id: {}", mailing.getId(), e);
            throw new ValidationException("error.mailing.content");
        }
    }

    protected void setMailingProperties(Mailing mailing, PatchMailingSettingsCommand command, Admin admin) {
        // When 'mailing.settings.hide' permission is set general settings should not be changed
        boolean notActiveOrSent = !mailingService.isActiveOrSent(mailing);

        if (notActiveOrSent && !mailing.isWorkflowDriven() && !isMailingWithHiddenSettings(mailing, admin)) {
            command.mailinglistId().ifPresent(mailing::setMailinglistID);
            command.mailingType().ifPresent(mailing::setMailingType);
        }
        if (!mailing.isWorkflowDriven() && !isMailingWithHiddenSettings(mailing, admin)) {
            command.archived().ifPresent(b -> mailing.setArchived(Boolean.TRUE.equals(b) ? 1 : 0));
        }
        if (!mailing.isWorkflowDriven()) {
            command.archiveId().ifPresent(id -> mailing.setCampaignID(id != null ? id : 0));
        }
        if (notActiveOrSent && !mailing.isWorkflowDriven()) {
            command.planDate().ifPresent(date -> mailing.setPlanDate(date == null ? null : Date.from(date)));
            mailing.setSplitID(getTargetListSplitIdToSet(mailing, command));
            // Only change target expressions, that are NOT complex and not managed by workflow manager
            if (!isTargetExpressionComplex(mailing)) {
                mailing.setTargetExpression(adaptTargetExpressionToSet(mailing, command, admin));
            }
        }
        if (notActiveOrSent) {
            setMediatypesToMailing(mailing, command, admin);
        }
        if (admin.permissionAllowed(Permission.MAILING_PARAMETER_CHANGE)) {
            command.parameters().ifPresent(params -> mailing.setParameters(collectMailingParams(mailing, params)));
        }
        command.name().ifPresent(mailing::setShortname);
        command.description().ifPresent(mailing::setDescription);
        command.contentType().ifPresent(mailing::setMailingContentType);
        command.needsTarget().ifPresent(nt -> mailing.setNeedsTarget(Boolean.TRUE.equals(nt)));
        command.useDynamicTemplate().ifPresent(mailing::setUseDynamicTemplate);
        command.openTriggerId().ifPresent(id -> mailing.setOpenActionID(id != null ? id : 0));
        command.clickTriggerId().ifPresent(id -> mailing.setClickActionID(id != null ? id : 0));
        mailing.setLocked(1);
    }

    private static boolean isMailingWithHiddenSettings(Mailing mailing, Admin admin) {
        return !mailing.isIsTemplate() && admin.permissionAllowed(Permission.MAILING_SETTINGS_HIDE);
    }

    private int getTargetListSplitIdToSet(Mailing mailing, PatchMailingSettingsCommand command) {
        if (!command.splitId().present()) {
            return mailing.getSplitID();
        }
        if (isActionBasedTargetGroupsDisabled(mailing, mailing.getCompanyID())) {
            return Mailing.NONE_SPLIT_ID;
        }
        return command.splitId().value();
    }

    private boolean isActionBasedTargetGroupsDisabled(Mailing mailing, int companyId) {
        return mailing.getMailingType() == MailingType.ACTION_BASED
               && !configService.getBooleanValue(ConfigValue.CampaignEnableTargetGroups, companyId);
    }

    private String adaptTargetExpressionToSet(Mailing mailing, PatchMailingSettingsCommand command, Admin admin) {
        if (isActionBasedTargetGroupsDisabled(mailing, admin.getCompanyID())) {
            return "";
        }
        if (!command.targetExpression().present()) {
            return mailing.getTargetExpression();
        }
        return adaptTargetExpressionToSet(command.targetExpression().value(), admin);
    }

    @SuppressWarnings("unused") // overridden in extended class
    protected String adaptTargetExpressionToSet(String targetExpression, Admin admin) {
        return targetExpression;
    }

    protected boolean isTargetExpressionComplex(Mailing mailing) {
        return mailing.hasComplexTargetExpression(); // overridden in extended class
    }

    protected void setMediatypesToMailing(Mailing mailing, PatchMailingSettingsCommand command, Admin admin) {
        int oldMailFormat = mailing.getEmailParam().getMailFormat();

        Map<Integer, Mediatype> current = mailing.getMediatypes();

        command.activeMediatypesOrder().ifPresent(order -> applyActiveOrder(current, order));

        command.mediatypes().ifPresent(patches -> patches.forEach((type, patch) -> {
            Mediatype mt = current.computeIfAbsent(type.getMediaCode(), c -> mediatypeFactory.create(type.getMediaCode()));
            // When 'mailing.settings.hide' permission is set, text template and HTML template should still be editable
            boolean restrictedToAlwaysAllowed = isMailingWithHiddenSettings(mailing, admin) && Mediatype.isActive(mt);
            if (restrictedToAlwaysAllowed) {
                patch.applyAlwaysAllowedFieldsTo(mt);
            } else {
                patch.applyTo(mt);
            }
        }));

        MediatypeEmail updatedEmailMediatype = mailing.getEmailParam();

        if (mailing.isGridMailing()) {
            updatedEmailMediatype.setStatus(MediaTypeStatus.Active.getCode());
        }
        if (mailing.getMailingType() != MailingType.DATE_BASED) {
            updatedEmailMediatype.deleteDateBasedParameters();
        }
        if (StringUtils.isBlank(updatedEmailMediatype.getReplyFullname())) {
            updatedEmailMediatype.setReplyFullname(updatedEmailMediatype.getFromFullname());
        }
        if (updatedEmailMediatype.getMailFormat() == MailingModel.Format.TEXT.getCode()) {
            updatedEmailMediatype.setHtmlTemplate("");
        } else if (Mediatype.isActive(updatedEmailMediatype)
                   && oldMailFormat == MailingModel.Format.TEXT.getCode()
                   && isEmpty(updatedEmailMediatype.getHtmlTemplate())) {
            updatedEmailMediatype.setHtmlTemplate("[agnDYN name=\"HTML-Version\"/]");
        }
        syncMediatypeTemplates(mailing);
    }

    private void applyActiveOrder(Map<Integer, Mediatype> target, List<MediaTypes> order) {
        target.values().forEach(mt -> mt.setStatus(MediaTypeStatus.Inactive.getCode()));
        for (int i = 0; i < order.size(); i++) {
            MediaTypes type = order.get(i);
            Mediatype mt = target.computeIfAbsent(type.getMediaCode(), c -> mediatypeFactory.create(type.getMediaCode()));
            mt.setStatus(MediaTypeStatus.Active.getCode());
            mt.setPriority(i);
        }
    }

    private List<MailingParameter> collectMailingParams(Mailing mailing, List<MailingParameter> newParams) {
        // Let's retrieve all the parameters currently stored.
        List<MailingParameter> intervalParams = CollectionUtils.emptyIfNull(mailing.getParameters()).stream()
                .filter(p -> isReservedParam(p.getName()))
                .toList();
        List<MailingParameter> params = CollectionUtils.emptyIfNull(newParams).stream()
                .filter(param -> isNotEmpty(param.getName()))
                .collect(Collectors.toList());
        params.addAll(intervalParams);
        return params;
    }

    private void saveMailing(Mailing mailing, Admin admin) {
        boolean isNew = mailing.getId() == 0;
        boolean approved = mailingService.isApproved(mailing.getId(), mailing.getCompanyID());
        mailingBaseService.saveUndoData(mailing.getId(), admin.getAdminID());
//        if (ctxt.isCopying()) { // todo recheck after decision in GWUA-7149
//            mailingService.saveMailing(mailing, admin.permissionAllowed(Permission.MAILING_TRACKABLELINKS_NOCLEANUP));
        mailingService.saveMailingWithNewContent(mailing, admin);
        if (mailing.isIsTemplate()) {
            mailingService.updateMailingsWithDynamicTemplate(mailing, applicationContext);
        }
        if (approved && !isNew) {
            mailingService.writeRemoveApprovalLog(mailing.getId(), admin);
        }
    }
}
