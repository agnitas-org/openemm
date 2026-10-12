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
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

public record PatchMailingRequest(
        // region General settings
        JsonNullable<@NotBlank @Size(
                min = MailingConstraints.NAME_MIN_LENGTH,
                max = MailingConstraints.NAME_MAX_LENGTH
        ) String> name,
        JsonNullable<@Size(max = MailingConstraints.DESCRIPTION_MAX_LENGTH) String> description,
        JsonNullable<@NotNull MailingContentType> contentType,
        JsonNullable<@NotNull MailingType> mailingType,
        JsonNullable<@Positive Integer> archiveId,
        JsonNullable<@Positive Integer> openTriggerId,
        JsonNullable<@Positive Integer> clickTriggerId,
        JsonNullable<@NotEmpty List<RestMediatype>> mediatypes, // for active and priority
        // endregion

        // region Targeting settings
        JsonNullable<@NotNull @Positive Integer> mailinglistId,
        JsonNullable<String> targetExpression,
        JsonNullable<Boolean> needsTarget,
        // endregion

        // region EMAIL mediatype settings
        JsonNullable<@Size(min = MailingConstraints.SUBJECT_MIN_LENGTH) String> subject,
        JsonNullable<String> preHeader,
        JsonNullable<@NotNull FollowupType> followupType,
        JsonNullable<@Positive Integer> followupForId,

        JsonNullable<@Email String> senderAddress,
        JsonNullable<@Size(max = MailingConstraints.SENDER_FULL_NAME_MAX_LENGTH) String> senderName,
        JsonNullable<@Email String> replyAddress,
        JsonNullable<@Size(max = MailingConstraints.REPLY_FULL_NAME_MAX_LENGTH) String> replyName,

        JsonNullable<String> textTemplate,
        JsonNullable<String> htmlTemplate,

        JsonNullable<@NotNull RestCharset> charset,
        JsonNullable<@NotNull MailFormat> format,
        JsonNullable<@NotNull OnePixelPosition> measureOpeningRate,
        JsonNullable<@Email String> envelopeEmail,
        // endregion

        // region SMS mediatype settings
        JsonNullable<String> smsTemplate,
        JsonNullable<
                @Size(min = MailingConstraints.SMS_ADDRESS_MIN_LENGTH)
                String
        > smsSenderAddress,
        JsonNullable<String> rcsTemplate,
        // endregion

        JsonNullable<List<@Valid UpdateMailingParameterRequest>> parameters
) {

    public boolean hasAnyEmailMediatypeField() {
        return subject.isPresent()
               || preHeader.isPresent()
               || senderAddress.isPresent()
               || senderName.isPresent()
               || replyAddress.isPresent()
               || replyName.isPresent()
               || textTemplate.isPresent()
               || htmlTemplate.isPresent()
               || charset.isPresent()
               || format.isPresent()
               || measureOpeningRate.isPresent()
               || envelopeEmail.isPresent();
    }

    public boolean hasAnySmsMediatypeField() {
        return smsTemplate.isPresent() || smsSenderAddress.isPresent();
    }

    public boolean hasAnyRcsMediatypeField() {
        return rcsTemplate.isPresent();
    }

    @SuppressWarnings("unused")
    @AssertTrue(groups = MailingGroup.class, message = "The 'needsTarget' parameter is only allowed for templates")
    public boolean isNeedsTargetValid() {
        return needsTarget == null || !needsTarget.isPresent();
    }
}
