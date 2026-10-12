/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailing.validation;

import static com.agnitas.emm.core.mailing.dao.MailingParameterDao.ReservedMailingParam.isReservedParam;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.DkimDao;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.archive.service.CampaignService;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.components.service.MailingSendService;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.dao.MailingParameterDao;
import com.agnitas.emm.core.mailing.dto.PatchMailingSettingsCommand;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.trackablelinks.web.LinkScanResultToMessages;
import com.agnitas.emm.core.validator.MailingValidationField;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.core.validator.ValidationField;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.preview.AgnTagError;
import com.agnitas.preview.PreviewHelper;
import com.agnitas.preview.TAGCheck;
import com.agnitas.preview.TAGCheckFactory;
import com.agnitas.service.AgnDynTagGroupResolverFactory;
import com.agnitas.service.AgnTagService;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.CharacterEncodingValidator;
import com.agnitas.util.DynTagException;
import com.agnitas.util.HtmlUtils;
import com.agnitas.web.MailingContentChecker;
import jakarta.mail.internet.InternetAddress;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
class MailingValidatorImpl implements MailingValidator {

    private static final Logger LOGGER = LogManager.getLogger(MailingValidatorImpl.class);

    protected final MailingService mailingService;
    protected final ConfigService configService;
    protected final TargetService targetService;
    private final EmmActionService triggerService;
    private final CampaignService archiveService;
    private final AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory;
    private final ApplicationContext applicationContext;
    private final TAGCheckFactory tagCheckFactory;
    private final WorkflowService workflowService;
    private final AgnTagService agnTagService;
    private final LinkService linkService;
    private final MailingSendService mailingSendService;
    private final DkimDao dkimDao;
    private final MailinglistService mailinglistService;
    private final CharacterEncodingValidator characterEncodingValidator;

    public MailingValidatorImpl(
            AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory,
            MailingService mailingService,
            TargetService targetService,
            EmmActionService triggerService,
            CampaignService archiveService,
            ApplicationContext applicationContext,
            TAGCheckFactory tagCheckFactory,
            WorkflowService workflowService,
            AgnTagService agnTagService,
            LinkService linkService,
            ConfigService configService,
            MailingSendService mailingSendService,
            DkimDao dkimDao,
            MailinglistService mailinglistService,
            CharacterEncodingValidator characterEncodingValidator
    ) {
        this.agnDynTagGroupResolverFactory = agnDynTagGroupResolverFactory;
        this.mailingService = mailingService;
        this.targetService = targetService;
        this.triggerService = triggerService;
        this.archiveService = archiveService;
        this.applicationContext = applicationContext;
        this.tagCheckFactory = tagCheckFactory;
        this.workflowService = workflowService;
        this.agnTagService = agnTagService;
        this.linkService = linkService;
        this.configService = configService;
        this.mailingSendService = mailingSendService;
        this.dkimDao = dkimDao;
        this.mailinglistService = mailinglistService;
        this.characterEncodingValidator = characterEncodingValidator;
    }

    @Override
    public void validateContainsOnlyAlwaysAllowedChanges(Mailing mailing, PatchMailingSettingsCommand command) {
        command.getEmailPatch().ifPresent(emailPatch -> {
            PatchField<String> textTemplate = emailPatch.textTemplate();
            PatchField<String> htmlTemplate = emailPatch.htmlTemplate();

            boolean templateChanged = textTemplate.present()
                    && !Strings.CS.equals(textTemplate.value(), mailing.getTextTemplate().getEmmBlock()
            );
            boolean htmlChanged = !mailing.isGridMailing()
                    && mailing.getEmailParam().getMailFormat() != 0
                    && htmlTemplate.present()
                    && !Strings.CS.equals(htmlTemplate.value(), mailing.getHtmlTemplate().getEmmBlock());

            if (templateChanged || htmlChanged) {
                throw new ValidationException(
                        mailingService.hasMailingStatus(mailing.getId(), MailingStatus.SENT, mailing.getCompanyID())
                                ? "error.sent.mailing.change.denied"
                                : "status_changed"
                );
            }
        });
    }

    @Override
    public SimpleServiceResult validateForSave(Mailing mailing, Admin admin) {
        Set<Message> globalErrors = new LinkedHashSet<>();
        Map<ValidationField, Message> fieldErrors = new HashMap<>();
        Set<Message> warnings = new HashSet<>();

        validateShortname(mailing.getShortname()).ifPresent(msg -> fieldErrors.put(MailingValidationField.NAME, msg));
        validateDescription(mailing.getDescription()).ifPresent(msg -> fieldErrors.put(MailingValidationField.DESCRIPTION, msg));
        validateEmailMediatype(mailing, fieldErrors);
        validateMailinglistId(mailing, admin);
        isMediaTypesPresent(mailing).ifPresent(globalErrors::add);
        containIllegalScriptElement(mailing.getEmailParam()).ifPresent(globalErrors::add);
        validateMailingParams(mailing.getParameters(), admin, warnings).ifPresent(globalErrors::add);
        validateTriggers(mailing, admin);
        validateArchive(mailing, admin);

        if (!mailingService.isActiveOrSent(mailing)) {
            validatePlanDate(mailing).ifPresent(msg -> fieldErrors.put(MailingValidationField.PLAN_DATE, msg));
            validateTargets(mailing, admin, globalErrors, fieldErrors, warnings);
            validateMailingMod(mailing, globalErrors);
            validateHtmlTemplate(mailing, globalErrors, warnings);
        }
        return SimpleServiceResult
                .of(warnings, globalErrors, fieldErrors)
                .merge(validateMailingTagsAndComponents(mailing, admin.getLocale()));
    }

    private void validateArchive(Mailing mailing, Admin admin) {
        int archiveId = mailing.getCampaignID();
        if (archiveId > 0 && !archiveService.exists(archiveId, admin.getCompanyID())) {
            throw ValidationException.notFound(MailingValidationField.ARCHIVE);
        }
    }

    private void validateTriggers(Mailing mailing, Admin admin) {
        validateTrigger(mailing.getOpenActionID(), MailingValidationField.OPEN_TRIGGER, admin);
        validateTrigger(mailing.getClickActionID(), MailingValidationField.CLICK_TRIGGER, admin);
    }

    private void validateTrigger(int triggerId, ValidationField field, Admin admin) {
        if (triggerId > 0) {
            triggerService.validateSelectionForMailing(triggerId, field, admin);
        }
    }

    private Optional<Message> validateMailingParams(List<MailingParameter> params, Admin admin, Set<Message> warnings) {
        if (!admin.permissionAllowed(Permission.MAILING_PARAMETER_CHANGE)) {
            return Optional.empty();
        }
        if (params.stream()
                .filter(Objects::nonNull)
                .anyMatch(param -> StringUtils.isBlank(param.getName()))) {
            return Optional.of(Message.of("error.mailing.parameter.emptyName"));
        }
        params.stream()
                .map(MailingParameter::getName)
                .filter(MailingParameterDao.ReservedMailingParam::isReservedParam)
                .forEach(param -> LOGGER.error("User tried to use reserved mailing parameter name: {}", param));
        if (params.stream()
                .filter(param -> !isReservedParam(param.getName()))
                .anyMatch(param -> StringUtils.isEmpty(param.getValue()))) {
            warnings.add(Message.of("warning.mailing.parameter.emptyValue"));
        }
        return Optional.empty();
    }

    /**
     * If Mailing created from template with rule "Mailing should only be sent with Target group-selection",
     * validate if at least one Target Group is set for Mailing.
     */
    @SuppressWarnings("unused") // fieldErrors used in extended scope
    protected void validateTargets(
            Mailing mailing,
            Admin admin,
            Set<Message> globalErrors,
            Map<ValidationField, Message> fieldErrors,
            Set<Message> warnings
    ) {
        Set<Integer> targetIds = TargetExpressionUtils.getNotAltgTargetIds(mailing.getTargetExpression(), targetService);
        for (Integer targetId : targetIds) {
            if (!targetService.exist(targetId, admin.getCompanyID())) {
                throw ValidationException.notFound(MailingValidationField.TARGETS, targetId);
            }
        }
        warnIfMailingNeedsTarget(mailing, warnings);
        if (mailing.getMailingType() == MailingType.ACTION_BASED
            && !configService.getBooleanValue(ConfigValue.CampaignEnableTargetGroups, admin.getCompanyID())
            && (CollectionUtils.isNotEmpty(mailing.getTargetGroups()) || mailing.getSplitID() > 0)
        ) {
            globalErrors.add(Message.exact("Can't use target groups for action based mailing"));
        }
        warnIfTargetsHaveDisjunction(admin, mailing, warnings);
    }

    private void warnIfMailingNeedsTarget(Mailing mailing, Set<Message> warnings) {
        if (mailingNeedsTarget(mailing) && isNoTargets(mailing)) {
            warnings.add(Message.of("warning.mailing.rulebased_without_target"));
        }
    }

    private boolean isNoTargets(Mailing mailing) {
        return mailing.isWorkflowDriven()
                ? !workflowService.isAdditionalRuleDefined(mailing.getCompanyID(), mailing.getWorkflowId())
                : CollectionUtils.isEmpty(mailing.getTargetGroups());
    }

    private boolean mailingNeedsTarget(Mailing mailing) {
        return (!mailing.isIsTemplate() && mailing.getNeedsTarget())
               || mailing.getMailingType() == MailingType.DATE_BASED
               || mailing.getMailingType() == MailingType.INTERVAL;
    }

    protected void validateEmailMediatype(Mailing mailing, Map<ValidationField, Message> fieldErrors) {
        MediatypeEmail emailMediatype = mailing.getEmailParam();
        if (!Mediatype.isActive(emailMediatype)) {
            return;
        }
        validateFromAndReplyFullNames(emailMediatype, fieldErrors, mailing.getCompanyID());
        validateSubject(emailMediatype.getSubject(), fieldErrors, mailing.getCompanyID());
        validateSenderEmail(emailMediatype.getFromEmail(), fieldErrors);
        validateReplyEmail(emailMediatype.getReplyEmail(), fieldErrors);
        validateEnvelopeEmail(emailMediatype.getEnvelopeEmail(), fieldErrors);
    }

    private void validateSubjectDynTags(String subject, Map<ValidationField, Message> fieldErrors) {
        try {
            agnTagService.getDynTags(subject);
        } catch (DynTagException e) {
            fieldErrors.put(MailingValidationField.SUBJECT, Message.of("error.template.dyntags.subject"));
            LOGGER.error("Error while parsing dyn tags in subject", e);
        }
    }

    private void validateEnvelopeEmail(String envelopeEmail, Map<ValidationField, Message> fieldErrors) {
        try {
            InternetAddress adr = new InternetAddress(envelopeEmail);
            String email = adr.getAddress();
            if (!AgnUtils.isEmailValid(email)) {
                fieldErrors.put(MailingValidationField.ENVELOPE_EMAIL, Message.of("error.mailing.envelope_adress"));
            }
        } catch (Exception e) {
            // do nothing
        }
    }

    private void validateReplyEmail(String replyEmail, Map<ValidationField, Message> fieldErrors) {
        try {
            InternetAddress adr = new InternetAddress(replyEmail);
            String email = adr.getAddress();
            if (!AgnUtils.isEmailValid(email)) {
                fieldErrors.put(MailingValidationField.REPLY_EMAIL, Message.of("error.mailing.reply_adress"));
            }
        } catch (Exception exc) {
            if (!Strings.CS.contains(replyEmail, "[agn")) {
                fieldErrors.put(MailingValidationField.REPLY_EMAIL, Message.of("error.mailing.reply_adress"));
            }
        }
    }

    private void validateSenderEmail(String fromEmail, Map<ValidationField, Message> fieldErrors) {
        try {
            InternetAddress adr = new InternetAddress(fromEmail);
            String email = adr.getAddress();
            if (!AgnUtils.isEmailValid(email)) {
                fieldErrors.put(MailingValidationField.SENDER_EMAIL, Message.of("error.mailing.sender_adress"));
            }
        } catch (Exception e) {
            if (!Strings.CS.contains(fromEmail, "[agn")) {
                fieldErrors.put(MailingValidationField.SENDER_EMAIL, Message.of("error.mailing.sender_adress"));
            }
        }
    }

    private void validateSubject(String subject, Map<ValidationField, Message> fieldErrors, int companyId) {
        if (StringUtils.length(subject) < MailingConstraints.SUBJECT_MIN_LENGTH) {
            fieldErrors.put(MailingValidationField.SUBJECT, Message.of("error.mailing.subject.too_short"));
        }
        validateSubjectDynTags(subject, fieldErrors);
        try {
            agnTagService.resolveTags(subject, companyId, 0, 0, 0);
        } catch (Exception e) {
            fieldErrors.put(MailingValidationField.SUBJECT, Message.of("error.personalization_tag"));
        }
    }

    private void validateFromAndReplyFullNames(MediatypeEmail emailMediatype, Map<ValidationField, Message> fieldErrors, int companyId) {
        if (StringUtils.length(emailMediatype.getReplyFullname()) > MailingConstraints.REPLY_FULL_NAME_MAX_LENGTH) {
            fieldErrors.put(MailingValidationField.REPLY_FULLNAME, Message.of("error.reply_fullname_too_long"));
        }
        if (StringUtils.length(emailMediatype.getFromFullname()) > MailingConstraints.SENDER_FULL_NAME_MAX_LENGTH) {
            fieldErrors.put(MailingValidationField.SENDER_FULLNAME, Message.of("error.sender_fullname_too_long"));
        }
        try {
            agnTagService.getDynTags(emailMediatype.getFromFullname());
        } catch (Exception e) {
            fieldErrors.put(MailingValidationField.SENDER_FULLNAME, Message.of("error.template.dyntags"));
        }
        try {
            agnTagService.resolveTags(emailMediatype.getFromFullname(), companyId, 0, 0, 0);
        } catch (Exception e) {
            fieldErrors.put(MailingValidationField.SENDER_FULLNAME, Message.of("error.personalization_tag"));
        }
    }

    private void validateMailinglistId(Mailing mailing, Admin admin) {
        if (!mailinglistService.existAndEnabled(admin, mailing.getMailinglistID())) {
            throw ValidationException.invalidValue(MailingValidationField.MAILINGLIST, mailing.isWorkflowDriven()
                    ? "error.mailing.noMailinglistSetWithCampaignEditor"
                    : "error.mailing.noMailinglist"
            );
        }
    }

    private Optional<Message> validateShortname(String shortname) {
        if (StringUtils.trimToNull(shortname) == null) {
            return Optional.of(Message.of("error.name.is.empty"));
        }
        if (StringUtils.trimToNull(shortname).length() < 3) {
            return Optional.of(Message.of("error.name.too.short"));
        }
        if (shortname.length() > MailingConstraints.NAME_MAX_LENGTH) {
            return Optional.of(Message.of("error.shortname_too_long"));
        }
        return Optional.empty();
    }

    private Optional<Message> validateDescription(String description) {
        if (StringUtils.length(description) > MailingConstraints.DESCRIPTION_MAX_LENGTH) {
            return Optional.of(Message.of("error.description.too.long"));
        }
        return Optional.empty();
    }

    private Optional<Message> isMediaTypesPresent(Mailing mailing) {
        if (!mailing.isGridMailing() && mailing.getMediatypes().values().stream().filter(Objects::nonNull).noneMatch(Mediatype::isActive)) {
            return Optional.of(Message.of("error.mailing.mediatype.none"));
        }
        return Optional.empty();
    }

    private Optional<Message> containIllegalScriptElement(MediatypeEmail emailMediatype) {
        if (HtmlUtils.containsElementByTag(emailMediatype.getHtmlTemplate(), "script")
            || HtmlUtils.containsElementByTag(emailMediatype.getTemplate(), "script")) {
            return Optional.of(Message.of("error.mailing.content.illegal.script"));
        }
        return Optional.empty();
    }

    private Optional<Message> validatePlanDate(Mailing mailing) {
        if (mailing.getPlanDate() != null && mailing.getPlanDate().before(new Date())) {
            return Optional.of(Message.of(mailing.isWorkflowDriven()
                    ? "error.mailing.plan.date.pastSetWithCampaignEditor"
                    : "error.mailing.plan.date.past"
            ));
        }
        return Optional.empty();
    }

    private void validateMailingMod(Mailing mailing, Set<Message> errors) {
        MediatypeEmail emailMediatype = mailing.getEmailParam();
        CharsetEncoder charsetEncoder = Charset.forName(emailMediatype.getCharset()).newEncoder();
        if (!characterEncodingValidator.validateMod(emailMediatype.getSubject(), charsetEncoder).isEmpty()) {
            errors.add(Message.of("error.charset.subject"));
        }
        characterEncodingValidator.validateDynamicTags(mailing, charsetEncoder)
                .forEach(dynTag -> errors.add(Message.of("error.charset.content", dynTag.getStrWithError(), dynTag.getLine())));

        if (!Mediatype.isActive(emailMediatype)) {
            return;
        }
        characterEncodingValidator.validateMod(emailMediatype.getTemplate(), charsetEncoder)
                .forEach(error -> errors.add(Message.of("error.charset.component", "agnText", error.getLine())));

        characterEncodingValidator.validateMod(emailMediatype.getHtmlTemplate(), charsetEncoder)
                .forEach(error -> errors.add(Message.of("error.charset.component", "agnHtml", error.getLine())));
    }

    private void warnIfTargetsHaveDisjunction(Admin admin, Mailing mailing, Set<Message> warnings) {
        if (mailing.getMailingType() == MailingType.DATE_BASED
            && CollectionUtils.size(mailing.getTargetGroups()) > 1
            && !isMailingTargetsHaveConjunction(admin, mailing)) {
            warnings.add(Message.of("warning.mailing.target.disjunction"));
        }
    }

    private void validateHtmlTemplate(Mailing mailing, Set<Message> errors, Set<Message> warnings) {
        // Grid-based mailings have generated HTML template (not user-defined).
        if (mailing.isGridMailing()) {
            return;
        }
        String htmlTemplate = mailing.getEmailParam().getHtmlTemplate();
        if (isNotEmpty(htmlTemplate)) {
            tryValidateTrackableLinks(mailing.getCompanyID(), htmlTemplate, mailing.getId(), mailing.getMailinglistID(), errors, warnings);
        }
    }

    private void tryValidateTrackableLinks(int companyId, String htmlTemplate, int mailingId, int mailinglistId, Set<Message> errors, Set<Message> warnings) {
        try {
            LinkService.LinkScanResult linkScanResult = linkService.scanForLinks(htmlTemplate, mailingId, mailinglistId, companyId);
            linkScanResult.getErroneousLinks().forEach(link -> errors.add(Message.of(link.getErrorMessageKey(), link.getLinkText())));
            linkScanResult.getLocalLinks().forEach(link -> warnings.add(Message.of(link.getErrorMessageKey(), link.getLinkText())));
            LinkScanResultToMessages.insecureLinksAsMessage(linkScanResult).ifPresent(warnings::add);
        } catch (Exception e) {
            LOGGER.warn("something went wrong while validating links in html template");
        }
    }

    protected boolean isMailingTargetsHaveConjunction(Admin admin, Mailing mailing) {
        return mailing.getTargetMode() == Mailing.TARGET_MODE_AND;
    }

    @Override
    public SimpleServiceResult validateMailingTagsAndComponents(Mailing mailing, Locale locale) {
        List<Message> warnings = new ArrayList<>();
        List<Message> errors = new ArrayList<>();
        List<String[]> errorReports = new ArrayList<>();

        String rdirDomain = linkService.getRdirDomain(mailing.getCompanyID());

        Map<String, List<AgnTagError>> agnTagsValidationErrors = mailing.checkAgnTagSyntax(applicationContext);
        if (MapUtils.isNotEmpty(agnTagsValidationErrors)) {
            errors.addAll(validateMailingTags(errorReports, agnTagsValidationErrors, locale));
        } else {
            warnings.addAll(validateMailingComponents(mailing, locale, errorReports));
        }
        if (CollectionUtils.isNotEmpty(errorReports)) {
            String report = errorReports.stream()
                    .map(list -> String.join(", ", list))
                    .collect(Collectors.joining("\n"));
            errors.add(Message.exact(I18nString.getLocaleString("error.template.dyntags", locale) + "<br><br>" + report));
        }
        mailing.getComponents().forEach((name, component) -> {
            if (component.getEmmBlock() != null) {
                Integer rdirLinkLine = linkService.getLineNumberOfFirstRdirLink(rdirDomain, component.getEmmBlock());
                if (rdirLinkLine != null) {
                    warnings.add(Message.of("warning.mailing.link.encoded", name, rdirLinkLine));
                }
            }
        });
        MailingContentChecker.checkHtmlWarningConditions(mailing).ifPresent(warnings::add);
        return SimpleServiceResult.of(warnings, errors);
    }

    private List<Message> validateMailingTags(List<String[]> errorReports, Map<String, List<AgnTagError>> agnTagsValidationErrors, Locale locale) {
        List<Message> errors = new ArrayList<>();
        agnTagsValidationErrors.forEach((componentName, validationErrors) -> {
            // noinspection ThrowableResultOfMethodCallIgnored

            if (componentName.startsWith("agn")) {
                componentName = componentName.substring(3);
            }
            AgnTagError firstError = validationErrors.getFirst();
            errors.add(Message.of("error.agntag.mailing.component", componentName, firstError.getFullAgnTagText()));
            errors.add(Message.of(firstError.getErrorKey().getMessageKey(), firstError.getAdditionalErrorDataWithLineInfo()));

            for (AgnTagError error : validationErrors) {
                errorReports.add(new String[]{componentName, error.getFullAgnTagText(), error.getLocalizedMessage(locale)});
            }
        });
        return errors;
    }

    private List<Message> validateMailingComponents(Mailing mailing, Locale locale, List<String[]> errorReports) {
        List<Message> warnings = new ArrayList<>();
        Map<String, MailingComponent> components = mailing.getComponents();
        if (validateDeprecatedTags(mailing.getCompanyID(), components, warnings) && mailing.getId() > 0) {
            // Only use backend/preview agn syntax check if mailing was already stored in database before (= not new mailing)
            List<String> outFailures = new ArrayList<>();

            try (TAGCheck tagCheck = tagCheckFactory.createTAGCheck(mailing.getCompanyID(), mailing.getId(), mailing.getMailinglistID(), locale)) {
                components.forEach((name, component) -> {
                    StringBuffer reportContents = new StringBuffer();
                    if (component.getEmmBlock() != null && !tagCheck.checkContent(component.getEmmBlock(), reportContents, outFailures)) {
                        appendErrorsToList(name, errorReports, reportContents);
                    }
                });
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        return warnings;
    }

    /**
     * Creates report about errors in dynamic tags.
     *
     * @param blockName      name of content block with invalid content
     * @param errorReports   list of messages about parsing errors (is changing inside the method)
     * @param templateReport content with errors
     */
    private void appendErrorsToList(String blockName, List<String[]> errorReports, StringBuffer templateReport) {
        Map<String, String> tagsWithErrors = PreviewHelper.getTagsWithErrors(templateReport);
        for (Map.Entry<String, String> entry : tagsWithErrors.entrySet()) {
            String[] errorRow = new String[3];
            errorRow[0] = blockName; // block
            errorRow[1] = entry.getKey(); // tag
            errorRow[2] = entry.getValue(); // value

            errorReports.add(errorRow);
        }
        List<String> errorsWithoutATag = PreviewHelper.getErrorsWithoutATag(templateReport);
        for (String error : errorsWithoutATag) {
            String[] errorRow = new String[3];
            errorRow[0] = blockName;
            errorRow[1] = "";
            errorRow[2] = error;
            errorReports.add(errorRow);
        }
    }

    private boolean validateDeprecatedTags(final int companyId, final Map<String, MailingComponent> components, List<Message> warnings) {
        boolean valid = true;
        for (MailingComponent component : components.values()) {
            if (component.getEmmBlock() != null) {
                final Set<String> deprecatedTagsNames = agnTagService.parseDeprecatedTagNamesFromString(component.getEmmBlock(), companyId);
                if (CollectionUtils.isNotEmpty(deprecatedTagsNames)) {
                    deprecatedTagsNames.forEach(el -> warnings.add(Message.of("warning.mailing.agntag.deprecated", el)));
                    valid = false;
                }
            }
        }
        return valid;
    }

    /**
     * Validate agn-tags and dyn-tags.
     */
    @Override
    public SimpleServiceResult validateMailingAfterSave(Admin admin, Mailing mailing) {
        SimpleServiceResult result = SimpleServiceResult.of(validateNeedDkimKey(admin, mailing));
        validateMailingModules(mailing).ifPresent(msg -> result.getErrorMessages().add(msg));
        return result;
    }

    private Optional<Message> validateMailingModules(Mailing mailing) {
        try {
            validateMailingModule(mailing.getTextTemplate());
            validateMailingModule(mailing.getHtmlTemplate());
            return Optional.empty();
        } catch (DynTagException e) {
            LOGGER.info("General error in tag", e);
            return Optional.of(Message.of("error.template.dyntags.general_tag_error", e.getLineNumber(), e.getTag()));
        }
    }

    private void validateMailingModule(MailingComponent template) throws DynTagException {
        if (template != null) {
            agnTagService.getDynTags(template.getEmmBlock(), agnDynTagGroupResolverFactory.create(template.getCompanyID(), template.getMailingID()));
        }
    }

    @Override
    public SimpleServiceResult validateNeedDkimKey(Admin admin, Mailing mailing) {
        MediatypeEmail mediatypeEmail = mailing.getEmailParam();

        // No further check, if media type "Email" is not active
        if (!Mediatype.isActive(mediatypeEmail)) {
            return SimpleServiceResult.simpleSuccess();
        }

        int companyID = admin.getCompanyID();

        String fromAddress = mediatypeEmail.getFromEmail();
        String senderDomain = AgnUtils.getDomainFromEmail(fromAddress);

        if (!configService.getBooleanValue(ConfigValue.DkimGlobalActivation, companyID)
            || dkimDao.existsDkimKeyForDomain(companyID, senderDomain)) {
            return SimpleServiceResult.simpleSuccess();
        }

        String configValue = configService.getValue(ConfigValue.SendMailingWithoutDkimCheck, companyID);

        if ("warning".equalsIgnoreCase(configValue)) {
            mailingSendService.sendEmail(admin, senderDomain);
            return SimpleServiceResult.simpleWarning(Message.of("warning.mailing.mandatoryDkimKeyMissing", senderDomain));
        } else if ("error".equalsIgnoreCase(configValue)) {
            mailingSendService.sendEmail(admin, senderDomain);
            return SimpleServiceResult.simpleError(Message.of("error.mailing.mandatoryDkimKeyMissing", senderDomain));
        }
        return SimpleServiceResult.simpleSuccess();
    }
}
