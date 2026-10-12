/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.dto;

import java.util.List;

import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.validation.MailingConstraints;
import com.agnitas.emm.mcp.v1.mailing.MailingGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record PatchMailingRequest(
        // region General settings
        @McpToolParam(description = "Mailing name", required = false)
        JsonNullable<@NotBlank @Size(min = MailingConstraints.NAME_MIN_LENGTH, max = MailingConstraints.NAME_MAX_LENGTH) String> name,

        @McpToolParam(description = "Description", required = false)
        JsonNullable<@Size(max = MailingConstraints.DESCRIPTION_MAX_LENGTH) String> description,

        @McpToolParam(description = "Content type of the mailing", required = false)
        JsonNullable<@NotNull MailingContentType> contentType,

        @McpToolParam(description = "Mailing type", required = false)
        JsonNullable<@NotNull MailingType> mailingType,

        @McpToolParam(description = "ID of a mailing archive to assign to the mailing. Use null to clear.", required = false)
        JsonNullable<@Positive Integer> archiveId,

        @McpToolParam(description = "Trigger ID to execute on email open. Use null to clear.", required = false)
        JsonNullable<@Positive Integer> openTriggerId,

        @McpToolParam(description = "Trigger ID to execute on link click. Use null to clear.", required = false)
        JsonNullable<@Positive Integer> clickTriggerId,

        @McpToolParam(description = "Active mediatypes in priority order. RCS can only be used together with SMS. When EMAIL is activated, subject, senderAddress and replyAddress must be set; for SMS, smsSenderAddress", required = false)
        JsonNullable<@NotEmpty List<MailingMediatype>> mediatypes,
        // endregion

        // region Targeting settings
        @McpToolParam(description = "ID of a mailinglist to assign the mailing to", required = false)
        JsonNullable<@NotNull @Positive Integer> mailinglistId,

        @McpToolParam(description = "Boolean expression of target group IDs to filter recipients, e.g. \"1&2\", \"1|2\", \"!1&(2|3)\". "
                + "Use & for AND, | for OR, ! for NOT, and () for grouping.", required = false)
        JsonNullable<String> targetExpression,

        @McpToolParam(description = "Whether a target group is required to send a mailing created from this template. Templates only", required = false)
        JsonNullable<Boolean> needsTarget,
        // endregion

        // region EMAIL mediatype settings
        @McpToolParam(description = "Subject line", required = false)
        JsonNullable<@Size(min = MailingConstraints.SUBJECT_MIN_LENGTH) String> subject,

        @McpToolParam(description = "Pre-header text", required = false)
        JsonNullable<String> preHeader,

        @McpToolParam(description = "Follow-up type", required = false)
        JsonNullable<@NotNull FollowupType> followupType,

        @McpToolParam(description = "ID of the mailing this follow-up is based on", required = false)
        JsonNullable<@Positive Integer> followupForId,

        @McpToolParam(description = "Sender email address", required = false)
        JsonNullable<@Email String> senderAddress,

        @McpToolParam(description = "Sender name", required = false)
        JsonNullable<@Size(max = MailingConstraints.SENDER_FULL_NAME_MAX_LENGTH) String> senderName,

        @McpToolParam(description = "Reply-to email address", required = false)
        JsonNullable<@Email String> replyAddress,

        @McpToolParam(description = "Reply-to name", required = false)
        JsonNullable<@Size(max = MailingConstraints.REPLY_FULL_NAME_MAX_LENGTH) String> replyName,

        @McpToolParam(description = "Text version of the mailing: the content itself or a dyn tag such as [agnDYN name=\"Text\" /] to fill later with update_mailing_content", required = false)
        JsonNullable<String> textTemplate,

        @McpToolParam(description = "HTML version of the mailing: the content itself or a dyn tag such as [agnDYN name=\"HTML-Version\" /] to fill later with update_mailing_content", required = false)
        JsonNullable<String> htmlTemplate,

        @McpToolParam(description = "Charset of the email", required = false)
        JsonNullable<@NotNull MailingCharset> charset,

        @McpToolParam(description = "Mail format", required = false)
        JsonNullable<@NotNull MailFormat> format,

        @McpToolParam(description = "Position of the opening-rate measuring pixel", required = false)
        JsonNullable<@NotNull OnePixelPosition> measureOpeningRate,

        @McpToolParam(description = "Envelope email address", required = false)
        JsonNullable<@Email String> envelopeEmail,
        // endregion

        // region SMS mediatype settings
        @McpToolParam(description = "SMS version of the mailing: the content itself or a dyn tag such as [agnDYN name=\"SMS-Version\" /] to fill later with update_mailing_content", required = false)
        JsonNullable<String> smsTemplate,

        @McpToolParam(description = "SMS sender address", required = false)
        JsonNullable<@Size(min = MailingConstraints.SMS_ADDRESS_MIN_LENGTH) String> smsSenderAddress,

        @McpToolParam(description = "RCS version of the mailing: the content itself or a dyn tag such as [agnDYN name=\"RCS-Version\" /] to fill later with update_mailing_content", required = false)
        JsonNullable<String> rcsTemplate,
        // endregion

        @McpToolParam(
                description = "Mailing parameters (key-value pairs). All existing parameters are replaced with the provided list.",
                required = false
        )
        JsonNullable<List<@Valid UpdateMailingParameterRequest>> parameters
) {

    public boolean hasAnyEmailMediatypeField() {
        return isPresent(subject)
               || isPresent(preHeader)
               || isPresent(followupType)
               || isPresent(followupForId)
               || isPresent(senderAddress)
               || isPresent(senderName)
               || isPresent(replyAddress)
               || isPresent(replyName)
               || isPresent(textTemplate)
               || isPresent(htmlTemplate)
               || isPresent(charset)
               || isPresent(format)
               || isPresent(measureOpeningRate)
               || isPresent(envelopeEmail);
    }

    public boolean hasAnySmsMediatypeField() {
        return isPresent(smsTemplate) || isPresent(smsSenderAddress);
    }

    public boolean hasAnyRcsMediatypeField() {
        return isPresent(rcsTemplate);
    }

    @SuppressWarnings("unused")
    @AssertTrue(groups = MailingGroup.class, message = "The 'needsTarget' parameter is only allowed for templates")
    public boolean isNeedsTargetValid() {
        return !isPresent(needsTarget);
    }

    private static boolean isPresent(JsonNullable<?> field) {
        return field != null && field.isPresent();
    }
}
