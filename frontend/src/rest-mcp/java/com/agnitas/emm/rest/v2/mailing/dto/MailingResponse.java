/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.dto;

import java.time.Instant;
import java.util.List;

import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingType;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.annotation.JsonSerialize;

public record MailingResponse(
        // region General settings
        int id,

        String name,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String description,

        MailingContentType contentType,

        MailingType mailingType,

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        int archiveId,

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        int openTriggerId,

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        int clickTriggerId,

        List<RestMediatype> mediatypes,
        // endregion

        // region Targeting settings
        int mailinglistId,

        String targetExpression,

        boolean needsTarget,
        // endregion

        // region EMAIL mediatype settings
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String subject,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String preHeader,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        FollowupType followupType,

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        int followupForId,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String senderAddress,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String senderName,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String replyAddress,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String replyName,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String textTemplate,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String htmlTemplate,

        @JsonSerialize(using = RestCharset.RestCharsetSerializer.class)
        RestCharset charset,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        MailFormat format,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        OnePixelPosition measureOpeningRate,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        String envelopeEmail,
        // endregion

        // region SMS mediatype settings
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String smsTemplate,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String smsSenderAddress,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String rcsTemplate,
        // endregion

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Instant sendDate,

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        long sentEmails,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<MailingParameterResponse> parameters
) {
}
