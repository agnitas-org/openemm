/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailing.service;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.settings.MailingSettingsForm;
import com.agnitas.emm.core.mailing.settings.mediatype.EmailMediatypeForm;
import com.agnitas.emm.core.mailing.web.MailingSettingsOptions;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.HtmlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;

@Service
public class MailingSettingsUalService {

    private final MailingService mailingService;
    private final MailingBaseService mailingBaseService;
    private final JavaMailService javaMailService;
    private final ConfigService configService;
    private final MailingParameterLogService parameterLogService;
    private final UserActivityLogService userActivityLogService;

    public MailingSettingsUalService(
            MailingService mailingService,
            JavaMailService javaMailService,
            ConfigService configService,
            UserActivityLogService userActivityLogService,
            MailingBaseService mailingBaseService,
            MailingParameterLogService parameterLogService
    ) {
        this.mailingService = mailingService;
        this.javaMailService = javaMailService;
        this.configService = configService;
        this.userActivityLogService = userActivityLogService;
        this.mailingBaseService = mailingBaseService;
        this.parameterLogService = parameterLogService;
    }

    public void writeCreateUal(Mailing mailing, int parentId, boolean isTemplate, Admin admin) {
        userActivityLogService.writeUserActivityLog(admin, "create " + getTypeForChangelog(isTemplate), parentId > 0
                ? "Copy from ID: %d, to ID: %d".formatted(parentId, mailing.getId())
                : mailing.getShortname() + " (" + mailing.getId() + ")"
        );
    }

    public void writeUpdateUal(Mailing before, MailingSettingsForm form, Admin admin, int newSplitId) {
        String mailingAsStr = form.getShortname() + " (" + before.getId() + ")";
        String type = getTypeForChangelog(before.isIsTemplate());
        int workflowId = mailingBaseService.getWorkflowId(before.getId(), admin.getCompanyID());
        String changelogs = collectChangelogs(before, form, workflowId > 0, newSplitId);

        userActivityLogService.writeUserActivityLog(admin, "edit " + type + " settings", changelogs + mailingAsStr);

        parameterLogService.getMailingParametersChangeLog(
                before.getId(),
                paramsAsMap(before.getParameters()),
                paramsAsMap(form.getParams())
        ).forEach(ua -> userActivityLogService.writeUserActivityLog(admin, ua));
    }

    private Map<Integer, MailingParameter> paramsAsMap(List<MailingParameter> params) {
        return params
                .stream()
                .collect(Collectors.toMap(MailingParameter::getMailingInfoID, Function.identity()));
    }


    public UserAction getSaveUserAction(Mailing mailing, MailingSettingsForm form, Admin admin, MailingSettingsOptions options) {
        final String type = getTypeForChangelog(options.isTemplate());
        final String shortnameEnding = form.getShortname() + " (" + mailing.getId() + ")";

        if (options.isNew()) {
            return new UserAction("create " + type, shortnameEnding);
        }
        String changelogs = collectChangelogs(mailing, form, options.isWorkflowDriven(), options.getNewSplitId());
        if (isNotEmpty(changelogs) && form.getParentId() == 0) {
            mailingService.updateStatus(admin.getCompanyID(), mailing.getId(), MailingStatus.EDIT);
        }
        return new UserAction("edit " + type + " settings", changelogs + shortnameEnding);
    }

    private String getTypeForChangelog(boolean isTemplate) {
        return isTemplate ? "template" : "mailing";
    }

    /**
     * Create list of fields changed in mailing
     *
     * @param mailing - mailing to change loaded from database
     * @param form    - from user side
     * @return - list of string description of changes scheduled
     */
    private String collectChangelogs(Mailing mailing, MailingSettingsForm form, boolean isWorkflowDriven, int newSplitId) {
        return getPropertyChangelog("Short name", mailing.getShortname(), form.getShortname()) +
               getPropertyChangelog("Description", trimToEmpty(mailing.getDescription()), trimToEmpty(form.getDescription())) +
               (!isWorkflowDriven ?
                       getPropertyChangelog("Mailing type", mailing.getMailingType().name(), form.getMailingType().name()) +
                       getPropertyChangelog("General archive", mailing.getCampaignID(), form.getArchiveId()) +
                       getPropertyChangelog("Archive", mailing.getArchived() != 0, form.isArchived()) +
                       getPropertyChangelog("Target mode", getTargetModeName(mailing.getTargetMode()), getTargetModeName(form.getTargetMode())) +
                       getTargetsChangelog(mailing, form) +
                       getMailinglistChangelog(mailing, form) : EMPTY) +
               getEmailMediatypeChangelog(mailing, form.getEmailMediatype()) +
               getSplitChangelog(mailing, form, newSplitId);
    }

    private String getTargetModeName(int mode) {
        return mode == Mailing.TARGET_MODE_AND ? "AND" : "OR";
    }

    private String getTargetsChangelog(Mailing mailing, MailingSettingsForm form) {
        Collection<Integer> mailingTargets = new ArrayList<>(CollectionUtils.emptyIfNull(mailing.getTargetGroups()));
        Collection<Integer> formTargets = getFormTargets(form);
        if (CollectionUtils.disjunction(mailingTargets, formTargets).isEmpty()) {
            return EMPTY;
        }
        Collection<Integer> intersection = CollectionUtils.intersection(mailingTargets, formTargets);
        Collection<Integer> removed = CollectionUtils.removeAll(mailingTargets, intersection);
        Collection<Integer> added = CollectionUtils.removeAll(formTargets, intersection);
        return "target groups " + "removed " + StringUtils.join(removed, ", ") + " added " + StringUtils.join(added, ", ") + "; ";
    }

    private String getTextTemplateChangelog(Mailing mailing, EmailMediatypeForm form) {
        MailingComponent textComponent = mailing.getTextTemplate();
        if (textComponent == null || Strings.CI.equals(textComponent.getEmmBlock(), form.getTextTemplate())) {
            return EMPTY;
        }
        return "mailing Frame content - Text block was changed; ";
    }

    private String getEmailMediatypeChangelog(Mailing mailing, EmailMediatypeForm form) {
        MediatypeEmail emailMediatype = mailing.getEmailParam();
        if (emailMediatype == null) {
            return EMPTY;
        }
        return getTextTemplateChangelog(mailing, form) +
               getHtmlTemplateChangelog(mailing, form) +
               getPropertyChangelog("Subject", emailMediatype.getSubject(), form.getSubject()) +
               getPropertyChangelog("Pre-Header", emailMediatype.getPreHeader(), form.getPreHeader()) +
               getPropertyChangelog("Format", getMailFormatName(emailMediatype.getMailFormat()), getMailFormatName(form.getMailFormat())) +
               getPropertyChangelog("Sender e-mail", emailMediatype.getFromEmail(), form.getFromEmail()) +
               getPropertyChangelog("Sender full name", trimToEmpty(emailMediatype.getFromFullname()), form.getFromFullname()) +
               getPropertyChangelog("Reply-to e-mail", emailMediatype.getReplyEmail(), form.getReplyEmail()) +
               getPropertyChangelog("Reply-to full name", trimToEmpty(emailMediatype.getReplyFullname()), form.getReplyFullname()) +
               getPropertyChangelog("Character set", emailMediatype.getCharset(), form.getCharset()) +
               getPropertyChangelog("Line break after", emailMediatype.getLinefeed(), form.getLinefeed()) +
               getPropertyChangelog("Measure opening rate", emailMediatype.getOnepixel(), form.getOnepixel());
    }

    private String getMailFormatName(int id) {
        return switch (id) {
            case 0 -> "only Text";
            case 1 -> "Text and HTML";
            case 2 -> "Text, HTML and Offline-HTML";
            default -> "Unknown mail format";
        };
    }


    protected String getPropertyChangelog(String valueName, Object oldValue, Object newValue) {
        return !Objects.equals(oldValue, newValue)
                ? "mailing " + valueName + " from " + oldValue + " to " + newValue + "; "
                : EMPTY;
    }

    private String getHtmlTemplateChangelog(Mailing mailing, EmailMediatypeForm form) {
        MailingComponent htmlComponent = mailing.getHtmlTemplate();
        if (htmlComponent != null && Strings.CI.equals(htmlComponent.getEmmBlock(), form.getHtmlTemplate())) {
            return EMPTY;
        }
        return "mailing Frame content - Html block was changed; ";
    }

    private String getMailinglistChangelog(Mailing mailing, MailingSettingsForm form) {
        if (mailing.getMailinglistID() == form.getMailinglistId()) {
            return EMPTY;
        }
        if (form.getMailinglistId() == 0) {
            //send mail
            String message = "Mailinglist ID in mailing template (" + mailing.getId() + ") was set to 0. Please check if the content still exists!";
            javaMailService.sendEmail(mailing.getCompanyID(), configService.getValue(ConfigValue.Mailaddress_Error), "Mailinglist set to 0", message, HtmlUtils.replaceLineFeedsForHTML(message));
        }
        return getPropertyChangelog("mailing list", mailing.getMailinglistID(), form.getMailinglistId());
    }

    private String getSplitChangelog(Mailing mailing, MailingSettingsForm form, int newSplitId) {
        if (mailing.getSplitID() == newSplitId) {
            return EMPTY;
        }
        if (newSplitId == Mailing.NONE_SPLIT_ID) {
            return "edit list split changed to " + Mailing.NONE_SPLIT + "; ";
        } else {
            return "edit list split changed to " + getFormattedSplitString(form.getSplitSettings().getSplitBase()) + " part #" + form.getSplitSettings().getSplitPart() + "; ";
        }
    }

    /**
     * Format list split string from its name to human readable format.
     * For example converting "050505050575" to "5% / 5% / 5% / 5% / 5% / 75%"
     */
    private String getFormattedSplitString(String splitBase) {
        StringBuilder sb = new StringBuilder(splitBase);
        final int firstSeparatorPlace = 2;
        final int splitWithDelimiterLength = 6;
        for (int i = firstSeparatorPlace, j = 0;
             i < sb.length();
             i += splitWithDelimiterLength, j += splitWithDelimiterLength) {
            sb.insert(i, "% / ");
            if (sb.indexOf("0", j) == j) {
                sb.delete(j, j + 1);
                i--;
                j--;
            }
        }
        return sb.append("%").toString();
    }

    protected Collection<Integer> getFormTargets(MailingSettingsForm form) {
        return new ArrayList<>(CollectionUtils.emptyIfNull(form.getTargetGroupIds()));
    }

    protected boolean workflowDriven(Integer workflowId) {
        return workflowId != null && workflowId > 0;
    }
}
