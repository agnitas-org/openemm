/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.mailing.dto;

import java.util.List;

import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

public record UpdateMailingRequest(
    JsonNullable<@Size(min = 3) String> shortname,
    JsonNullable<@Size(max = 2000) String> description,
    JsonNullable<@Positive Integer> mailinglist_id,
    JsonNullable<@Size(min = 3) String> mailinglist_shortname,
    @JsonDeserialize(contentUsing = MailingType.NameDeserializer.class)
    JsonNullable<MailingType> mailingtype,
    @JsonDeserialize(contentUsing = MailingContentType.NameDeserializer.class)
    JsonNullable<MailingContentType> mailing_content_type,
    JsonNullable<@Size(min = 2) String> subject,
    JsonNullable<String> preHeader,
    JsonNullable<@Email String> sender_address,
    JsonNullable<@Email String> reply_address,
    JsonNullable<String> target_expression,
    JsonNullable<Boolean> is_template,
    JsonNullable<Integer> open_action_id,
    JsonNullable<Integer> click_action_id,
    JsonNullable<Integer> campaign_id,
    JsonNullable<List<MailingParameterDto>> parameters,
    JsonNullable<List<TrackableLinkDto>> links
) {

    @AssertTrue(message = "Either mailinglistId or mailinglistName can be provided (but not both)")
    private boolean isMailinglist() {
        return !mailinglist_id.isPresent() || !mailinglist_shortname.isPresent();
    }
}
