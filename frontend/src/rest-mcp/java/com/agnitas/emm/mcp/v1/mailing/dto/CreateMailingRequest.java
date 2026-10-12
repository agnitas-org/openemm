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
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record CreateMailingRequest(
        // region General settings
        @McpToolParam(description = "Mailing name")
        @NotBlank
        @Size(min = MailingConstraints.NAME_MIN_LENGTH, max = MailingConstraints.NAME_MAX_LENGTH)
        String name,

        @McpToolParam(description = "Description", required = false)
        @Size(max = MailingConstraints.DESCRIPTION_MAX_LENGTH)
        String description,

        @McpToolParam(description = "Content type of the mailing", required = false)
        MailingContentType contentType,

        @McpToolParam(description = "Mailing type")
        @NotNull
        MailingType mailingType,

        @McpToolParam(description = "ID of an existing mailing archive to assign to the mailing", required = false)
        @Positive
        Integer archiveId,

        @McpToolParam(description = "Active mediatypes in priority order. RCS can only be used together with SMS")
        @NotEmpty
        List<MailingMediatype> mediatypes,

        @McpToolParam(description = "Trigger ID to execute on email open", required = false)
        @Positive
        Integer openTriggerId,

        @McpToolParam(description = "Trigger ID to execute on link click", required = false)
        @Positive
        Integer clickTriggerId,
        // endregion

        // region Targeting settings
        @McpToolParam(description = "ID of an existing mailinglist to assign to the mailing")
        @NotNull
        @Positive
        Integer mailinglistId,

        @McpToolParam(description = "Boolean expression of target group IDs to filter recipients, e.g. \"1&2\", \"1|2\", \"!1&(2|3)\". "
                + "Use & for AND, | for OR, ! for NOT, and () for grouping.", required = false)
        String targetExpression,

        @McpToolParam(description = "Whether a target group is required to send a mailing created from this template. Templates only", required = false)
        @Null(groups = MailingGroup.class, message = "The 'needsTarget' parameter is only allowed for templates")
        Boolean needsTarget,
        // endregion

        // region EMAIL mediatype settings
        @McpToolParam(description = "Subject line. Required when the EMAIL mediatype is selected", required = false)
        @Size(min = MailingConstraints.SUBJECT_MIN_LENGTH)
        String subject,

        @McpToolParam(description = "Pre-header text", required = false)
        String preHeader,

        @McpToolParam(description = "Sender email address. Required when the EMAIL mediatype is selected", required = false)
        @Email
        String senderAddress,

        @McpToolParam(description = "Sender name", required = false)
        @Size(max = MailingConstraints.SENDER_FULL_NAME_MAX_LENGTH)
        String senderName,

        @McpToolParam(description = "Reply-to email address. Required when the EMAIL mediatype is selected", required = false)
        @Email
        String replyAddress,

        @McpToolParam(description = "Reply-to name", required = false)
        @Size(max = MailingConstraints.REPLY_FULL_NAME_MAX_LENGTH)
        String replyName,

        @McpToolParam(description = "Follow-up type. Required for FOLLOW_UP mailings and not allowed for other types", required = false)
        FollowupType followupType,

        @McpToolParam(description = "ID of the mailing this follow-up is based on. Required for FOLLOW_UP mailings and not allowed for other types",
                required = false)
        @Positive
        Integer followupForId,

        @McpToolParam(description = "Text version of the mailing: the content itself or a dyn tag such as [agnDYN name=\"Text\" /] to fill later with update_mailing_content", required = false)
        String textTemplate,

        @McpToolParam(description = "HTML version of the mailing: the content itself or a dyn tag such as [agnDYN name=\"HTML-Version\" /] to fill later with update_mailing_content", required = false)
        String htmlTemplate,

        @McpToolParam(description = "Charset of the email", required = false)
        MailingCharset charset,

        @McpToolParam(description = "Mail format", required = false)
        MailFormat format,

        @McpToolParam(description = "Position of the opening-rate measuring pixel", required = false)
        OnePixelPosition measureOpeningRate,

        @McpToolParam(description = "Envelope email address", required = false)
        @Email
        String envelopeEmail,
        // endregion

        // region SMS mediatype settings
        @McpToolParam(description = "SMS version of the mailing: the content itself or a dyn tag such as [agnDYN name=\"SMS-Version\" /] to fill later with update_mailing_content", required = false)
        String smsTemplate,

        @McpToolParam(description = "SMS sender address. Required when the SMS/RCS mediatype is selected", required = false)
        @Size(min = MailingConstraints.SMS_ADDRESS_MIN_LENGTH)
        String smsSenderAddress,

        @McpToolParam(description = "RCS version of the mailing: the content itself or a dyn tag such as [agnDYN name=\"RCS-Version\" /] to fill later with update_mailing_content", required = false)
        String rcsTemplate,
        // endregion

        @McpToolParam(description = "Mailing parameters (key-value pairs)", required = false)
        List<@Valid CreateMailingParameterRequest> parameters
) {

    @SuppressWarnings("unused")
    @AssertTrue(message = MailingConstraints.FOLLOWUP_TYPE_REQUIRED_FOR_FOLLOWUP_MSG)
    public boolean isFollowupTypePresentForFollowUp() {
        return mailingType != MailingType.FOLLOW_UP || followupType != null;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = MailingConstraints.FOLLOWUP_TYPE_NOT_FOR_NON_FOLLOWUP_MSG)
    public boolean isFollowupTypeAbsentForNonFollowUp() {
        return mailingType == MailingType.FOLLOW_UP || followupType == null;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = MailingConstraints.FOLLOWUP_FOR_ID_REQUIRED_FOR_FOLLOWUP_MSG)
    public boolean isFollowupForIdPresentForFollowUp() {
        return mailingType != MailingType.FOLLOW_UP || followupForId != null;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = MailingConstraints.FOLLOWUP_FOR_ID_NOT_FOR_NON_FOLLOWUP_MSG)
    public boolean isFollowupForIdAbsentForNonFollowUp() {
        return mailingType == MailingType.FOLLOW_UP || followupForId == null;
    }
}
