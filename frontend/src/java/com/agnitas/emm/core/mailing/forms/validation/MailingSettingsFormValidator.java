/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.forms.validation;

import static com.agnitas.emm.core.mailing.dao.ComMailingParameterDao.ReservedMailingParam.isReservedParam;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import jakarta.mail.internet.InternetAddress;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.exceptions.CharacterEncodingValidationExceptionMod;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CharacterEncodingValidator;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DynTagException;
import org.agnitas.util.HtmlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mailing.dao.ComMailingParameterDao;
import com.agnitas.emm.core.mailing.forms.MailingSettingsForm;
import com.agnitas.emm.core.mailing.forms.mediatype.EmailMediatypeForm;
import com.agnitas.emm.core.mailing.forms.mediatype.MediatypeForm;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.web.MailingSettingsOptions;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.trackablelinks.web.LinkScanResultToMessages;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.service.AgnTagService;
import com.agnitas.web.mvc.Popups;

public class MailingSettingsFormValidator {

    private static final Logger LOGGER = LogManager.getLogger(MailingSettingsFormValidator.class);

    private static final String SHORTNAME_FIELD = "shortname";
    private static final String SUBJECT_FIELD = "subject";
    private static final String ENVELOPE_EMAIL = "envelopeEmail";
    private static final String REPLY_EMAIL = "replyEmail";
    private static final String FROM_EMAIL = "fromEmail";

    protected MailingService mailingService;
    protected ConfigService configService;
    private CharacterEncodingValidator characterEncodingValidator;
    private MailinglistService mailinglistService;
    private ComWorkflowService workflowService;
    private AgnTagService agnTagService;
    private LinkService linkService;

    public boolean isValidFormBeforeMailingSave(Mailing mailing, MailingSettingsForm form, MailingSettingsOptions options, Admin admin, Popups popups) {
        int mailingId = mailing.getId();
        validateShortname(form.getShortname(), popups);
        validateDescription(form.getDescription(), popups);
        validateEmailMediatype(form.getEmailMediatype(), options.getCompanyId(), popups);
        validateMailinglistId(options.getWorkflowId(), form, admin, popups);

        isMediaTypesPresent(form, popups, options);
        containIllegalScriptElement(form, popups);
        validateMailingParams(form, admin, popups);
        if (options.isEditable()) {
            validatePlanDate(mailingId, workflowDriven(options.getWorkflowId()), form, admin, popups);
            validateTargets(options, form, admin, popups);
            validateMailingMod(form, mailing, popups);
            validateHtmlTemplate(mailing, form, options, popups);
        }
        return !popups.hasAlertPopups();
    }

    private void validateMailingParams(MailingSettingsForm form, Admin admin, Popups popups) {
        if (!admin.permissionAllowed(Permission.MAILING_PARAMETER_CHANGE)) {
            return;
        }
        if (form.getParams().stream()
                .filter(Objects::nonNull)
                .anyMatch(param -> StringUtils.isBlank(param.getName()))) {
            popups.alert("error.mailing.parameter.emptyName");
        }
        form.getParams().stream()
                .map(ComMailingParameter::getName)
                .filter(ComMailingParameterDao.ReservedMailingParam::isReservedParam)
                .forEach(param -> LOGGER.error("User tried to use reserved mailing parameter name: {}", param));
        if (form.getParams().stream()
                .filter(param -> !isReservedParam(param.getName()))
                .anyMatch(param -> StringUtils.isEmpty(param.getValue()))) {
            popups.warning("warning.mailing.parameter.emptyValue");
        }
    }

    /**
     * If Mailing created from template with rule "Mailing should only be sent with Target group-selection",
     * validate if at least one Target Group is set for Mailing.
     */
    protected void validateTargets(MailingSettingsOptions options, MailingSettingsForm form,
                                   Admin admin, Popups popups) {
        warnIfMailingNeedsTarget(options, form, popups);
        if (form.getMailingType() == MailingType.ACTION_BASED
                && !configService.getBooleanValue(ConfigValue.CampaignEnableTargetGroups, admin.getCompanyID())) {
            form.clearTargetsData();
        }
        warnIfTargetsHaveDisjunction(admin, form, popups);
    }

    private void warnIfMailingNeedsTarget(MailingSettingsOptions options, MailingSettingsForm form, Popups popups) {
        if (mailingNeedsTarget(options, form) && isNoTargets(options, form)) {
            popups.warning("warning.mailing.rulebased_without_target");
        }
    }

    private boolean isNoTargets(MailingSettingsOptions options, MailingSettingsForm form) {
        int workflowId = options.getWorkflowId();
        return workflowId > 0
                ? !workflowService.isAdditionalRuleDefined(options.getCompanyId(), options.getMailingId(), workflowId)
                : CollectionUtils.isEmpty(form.getTargetGroupIds());
    }

    private boolean mailingNeedsTarget(MailingSettingsOptions options, MailingSettingsForm form) {
        return (!options.isTemplate() && form.isNeedsTarget())
                || form.getMailingType() == MailingType.DATE_BASED
                || form.getMailingType() == MailingType.INTERVAL;
    }

    private void validateEmailMediatype(EmailMediatypeForm form, int companyId, Popups popups) {
        if (!form.isActive()) {
            return;
        }
        validateFromAndReplyFullNames(form, popups);
        validateSubject(form, popups);
        validateFromEmail(form, popups);
        validateReplyEmail(form, popups);
        validateEnvelopeEmail(form, popups);
        validateTags(form, companyId, popups);
    }

    private void validateTags(EmailMediatypeForm form, int companyId, Popups popups) {
        validateSubjectDynTags(form.getSubject(), popups);
        try {
            agnTagService.getDynTags(form.getFromFullname());
        } catch (Exception e) {
            popups.field(SUBJECT_FIELD, "error.template.dyntags");
        }
        try {
            agnTagService.resolveTags(form.getSubject(), companyId, 0, 0, 0);
            agnTagService.resolveTags(form.getFromFullname(), companyId, 0, 0, 0);
        } catch (Exception e) {
            popups.field(SUBJECT_FIELD, "error.personalization_tag");
        }
    }

    private void validateSubjectDynTags(String subject, Popups popups) {
        try {
            agnTagService.getDynTags(subject);
        } catch (DynTagException e) {
            popups.field(SUBJECT_FIELD, "error.template.dyntags.subject");
            e.printStackTrace();
        }
    }

    private void validateEnvelopeEmail(EmailMediatypeForm form, Popups popups) {
        try {
            InternetAddress adr = new InternetAddress(form.getEnvelopeEmail());
            String email = adr.getAddress();
            if (!AgnUtils.isEmailValid(email)) {
                popups.field(ENVELOPE_EMAIL, "error.mailing.envelope_adress");
            }
        } catch (Exception e) {
            // do nothing
        }
    }

    private void validateReplyEmail(EmailMediatypeForm form, Popups popups) {
        try {
            InternetAddress adr = new InternetAddress(form.getReplyEmail());
            String email = adr.getAddress();
            if (!AgnUtils.isEmailValid(email)) {
                popups.field(REPLY_EMAIL, "error.mailing.reply_adress");
            }
        } catch (Exception exc) {
            if (!StringUtils.contains(form.getReplyEmail(), "[agn")) {
                popups.field(REPLY_EMAIL, "error.mailing.reply_adress");
            }
        }
    }

    private void validateFromEmail(EmailMediatypeForm form, Popups popups) {
        try {
            InternetAddress adr = new InternetAddress(form.getFromEmail());
            String email = adr.getAddress();
            if (!AgnUtils.isEmailValid(email)) {
                popups.field(FROM_EMAIL, "error.mailing.sender_adress");
            }
        } catch (Exception e) {
            if (!StringUtils.contains(form.getFromEmail(), "[agn")) {
                popups.field(FROM_EMAIL, "error.mailing.sender_adress");
            }
        }
    }
    
    private void validateSubject(EmailMediatypeForm form, Popups popups) {
        if (form.getSubject().length() < 2) {
            popups.field(SUBJECT_FIELD, "error.mailing.subject.too_short");
        }
    }

    private void validateFromAndReplyFullNames(EmailMediatypeForm form, Popups popups) {
        if (StringUtils.length(form.getReplyFullname()) > 255) {
            popups.field("replyFullname", "error.reply_fullname_too_long");
        }
        if (StringUtils.length(form.getFromFullname()) > 255) {
            popups.field("senderFullname", "error.sender_fullname_too_long");
        }
        if (StringUtils.isBlank(form.getReplyFullname())) {
            form.setReplyFullname(form.getFromFullname());
        }
    }

    private void validateMailinglistId(int workflowId, MailingSettingsForm form, Admin admin, Popups popups) {
        if (!mailinglistService.existAndEnabled(admin, form.getMailinglistId())) {
            popups.alert(workflowId == 0
                    ? "error.mailing.noMailinglist"
                    : "error.mailing.noMailinglistSetWithCampaignEditor");
        }
    }

    private void validateDescription(String description, Popups popups) {
        if (StringUtils.length(description) > 500) {
            popups.field("description", "error.description.too.long");
        }
    }

    private void validateShortname(String shortname, Popups popups) {
        if (StringUtils.trimToNull(shortname) == null) {
            popups.field(SHORTNAME_FIELD, "error.name.is.empty");
        } else if (StringUtils.trimToNull(shortname).length() < 3) {
            popups.field(SHORTNAME_FIELD, "error.name.too.short");
        } else if (shortname.length() >= 100) {
            popups.field(SHORTNAME_FIELD, "error.shortname_too_long");
        }
    }

    private void isMediaTypesPresent(MailingSettingsForm form, Popups popups, MailingSettingsOptions options) {
        if (options.getGridTemplateId() <= 0 && form.getMediatypes().values().stream().filter(Objects::nonNull).noneMatch(MediatypeForm::isActive)) {
            popups.alert("error.mailing.mediatype.none");
        }
    }

    private void containIllegalScriptElement(MailingSettingsForm form, Popups popups) {
        EmailMediatypeForm emailMediatype = form.getEmailMediatype();
        if (HtmlUtils.containsElementByTag(emailMediatype.getHtmlTemplate(), "script")
                || HtmlUtils.containsElementByTag(emailMediatype.getTextTemplate(), "script")) {
            popups.alert("error.mailing.content.illegal.script");
        }
    }

    private void validatePlanDate(int mailingId, boolean workflowDriven, MailingSettingsForm form, Admin admin, Popups popups) {
        if (isNotEmpty(form.getPlanDate())) {
            tryValidatePlanDate(mailingId, workflowDriven, form, admin, popups);
        }
    }

    private void tryValidatePlanDate(int mailingId, boolean workflowDriven, MailingSettingsForm form, Admin admin, Popups popups) {
        try {
            TimeZone timeZone = AgnUtils.getTimeZone(admin);
            Date planDate = admin.getDateFormat().parse(form.getPlanDate());
            Date today = DateUtilities.midnight(timeZone);

            if (planDate.before(today)) {
                // Untouched plan date should not be validated (whether or not it is in the past)
                Date originPlanDate = DateUtilities.midnight(mailingService.getMailingPlanDate(mailingId, admin.getCompanyID()), timeZone);
                if (!planDate.equals(originPlanDate)) {
                    form.setPlanDate(admin.getDateFormat().format(today));
                    popups.alert(workflowDriven
                            ? "error.mailing.plan.date.pastSetWithCampaignEditor"
                            : "error.mailing.plan.date.past");
                }
            }
        } catch (ParseException e) {
            popups.alert("error.mailing.wrong.plan.date.format");
        }
    }

    private void validateMailingMod(MailingSettingsForm form, Mailing mailing, Popups popups) {
        try {
            characterEncodingValidator.validateMod(form, mailing);
        } catch (CharacterEncodingValidationExceptionMod e) {
            if (!e.getSubjectErrors().isEmpty()) {
                popups.alert("error.charset.subject");
            }
            e.getFailedMailingComponents().forEach(component
                    -> popups.alert("error.charset.component", component.getStrWithError(), component.getLine()));
            e.getFailedDynamicTags().forEach(dynTag
                    -> popups.alert("error.charset.content", dynTag.getStrWithError(), dynTag.getLine()));
        }
    }

    private void warnIfTargetsHaveDisjunction(Admin admin, MailingSettingsForm form, Popups popups) {
        if (form.getMailingType() == MailingType.DATE_BASED
                && CollectionUtils.size(form.getTargetGroupIds()) > 1
                && !isMailingTargetsHaveConjunction(admin, form)) {
            popups.warning("warning.mailing.target.disjunction");
        }
    }

    private void validateHtmlTemplate(Mailing mailing, MailingSettingsForm form, MailingSettingsOptions options, Popups popups) {
        // Grid-based mailings have generated HTML template (not user-defined).
        if (options.getGridTemplateId() != 0) {
            return;
        }
        EmailMediatypeForm emailMediatypeFrom = form.getEmailMediatype();
        // Check if user selected "text only" e-mail format.
        if (emailMediatypeFrom.getMailFormat() == MailingModel.Format.TEXT.getCode()) {
            emailMediatypeFrom.setHtmlTemplate("");
        } else if (mailing.getEmailParam() != null
                && mailing.getEmailParam().getMailFormat() == MailingModel.Format.TEXT.getCode()
                && isEmpty(emailMediatypeFrom.getHtmlTemplate())) {
            emailMediatypeFrom.setHtmlTemplate("[agnDYN name=\"HTML-Version\"/]");
        }
        String htmlTemplate = emailMediatypeFrom.getHtmlTemplate();
        if (isNotEmpty(htmlTemplate)) {
            tryValidateTrackableLinks(mailing.getCompanyID(), htmlTemplate, mailing.getId(), form.getMailinglistId(), popups);
        }
    }

    private void tryValidateTrackableLinks(int companyId, String htmlTemplate, int mailingId, int mailinglistId, Popups popups) {
        try {
            LinkService.LinkScanResult linkScanResult = linkService.scanForLinks(htmlTemplate, mailingId, mailinglistId, companyId);
            linkScanResult.getErroneousLinks().forEach(link -> popups.alert(link.getErrorMessageKey(), link.getLinkText()));
            linkScanResult.getLocalLinks().forEach(link -> popups.warning(link.getErrorMessageKey(), link.getLinkText()));
            LinkScanResultToMessages.linkWarningsToPopups(linkScanResult, popups);
        } catch (Exception e) {
            LOGGER.warn("something went wrong while validating links in html template");
        }
    }

    protected boolean workflowDriven(int workflowId) {
        return workflowId > 0;
    }
    
    protected boolean isMailingTargetsHaveConjunction(Admin admin, MailingSettingsForm form) {
        return form.getTargetMode() == Mailing.TARGET_MODE_AND;
    }
    
    @Required
	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}
	
    @Required
    public void setCharacterEncodingValidator(CharacterEncodingValidator characterEncodingValidator) {
        this.characterEncodingValidator = characterEncodingValidator;
    }
    
    @Required
    public void setMailinglistService(MailinglistService mailinglistService) {
        this.mailinglistService = mailinglistService;
    }

    @Required
    public void setWorkflowService(ComWorkflowService workflowService) {
        this.workflowService = workflowService;
    }
    
    @Required
    public void setLinkService(LinkService linkService) {
        this.linkService = linkService;
    }
    
    @Required
    public void setAgnTagService(final AgnTagService agnTagService) {
        this.agnTagService = agnTagService;
    }
    
    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
