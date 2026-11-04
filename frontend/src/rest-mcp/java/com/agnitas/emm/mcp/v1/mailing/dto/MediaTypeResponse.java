/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MediaTypeResponse.Email.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = MediaTypeResponse.Sms.class, name = "SMS"),
        @JsonSubTypes.Type(value = MediaTypeResponse.Rcs.class, name = "RCS")
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed interface MediaTypeResponse {

    record Email(
            int priority,
            String status,
            String subject,
            String preHeader,
            @JsonAlias("from_address") String senderAddress,
            @JsonAlias("from_fullname") String senderName,
            @JsonAlias("reply_address") String replyAddress,
            @JsonAlias("reply_fullname") String replyName,
            @JsonAlias("envelope") String envelopeEmail,
            String charset,
            @JsonAlias("mailformat") String format,
            @JsonAlias("onepixel") String measureOpeningRate,
            Integer linefeed,
            @JsonAlias("followup_for") Integer followupForId,
            @JsonAlias("followup_method") String followupType
    ) implements MediaTypeResponse {}

    record Sms(
            int priority,
            String status,
            String param
    ) implements MediaTypeResponse {}

    record Rcs(
            int priority,
            String status
    ) implements MediaTypeResponse {}
}
