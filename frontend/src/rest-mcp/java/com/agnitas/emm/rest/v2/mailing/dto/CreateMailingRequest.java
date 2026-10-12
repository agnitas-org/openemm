/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.dto;

import java.util.List;

import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.validation.MailingConstraints;
import com.agnitas.emm.rest.v2.mailing.MailingGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateMailingRequest(
        // region General settings
        @NotBlank
        @Size(min = MailingConstraints.NAME_MIN_LENGTH, max = MailingConstraints.NAME_MAX_LENGTH)
        String name,

        @Size(max = MailingConstraints.DESCRIPTION_MAX_LENGTH)
        String description,

        MailingContentType contentType,

        @NotNull
        MailingType mailingType,

        @Positive
        Integer archiveId,

        @NotEmpty
        List<RestMediatype> mediatypes,

        @Positive
        Integer openTriggerId,
        @Positive
        Integer clickTriggerId,
        // endregion

        // region Targeting settings
        @NotNull
        @Positive
        Integer mailinglistId,

        String targetExpression,

        @Null(groups = MailingGroup.class, message = "The 'needsTarget' parameter is only allowed for templates")
        Boolean needsTarget,
        // endregion

        // region EMAIL mediatype settings
        @Size(min = MailingConstraints.SUBJECT_MIN_LENGTH)
        String subject,
        String preHeader,

        @Email
        String senderAddress,
        @Size(max = MailingConstraints.SENDER_FULL_NAME_MAX_LENGTH)
        String senderName,
        @Email
        String replyAddress,
        @Size(max = MailingConstraints.REPLY_FULL_NAME_MAX_LENGTH)
        String replyName,

        FollowupType followupType,
        @Positive
        Integer followupForId,

        String textTemplate,
        String htmlTemplate,

        RestCharset charset,
        MailFormat format,
        OnePixelPosition measureOpeningRate,

        @Email String envelopeEmail,
        // endregion

        // region SMS mediatype settings
        String smsTemplate,

        @Size(min = MailingConstraints.SMS_ADDRESS_MIN_LENGTH)
        String smsSenderAddress,

        String rcsTemplate,
        // endregion

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
