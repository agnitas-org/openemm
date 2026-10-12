/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.dto;

import java.util.List;

import com.agnitas.emm.mcp.v1.mailing.content.MailingContentResponse;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MailingFullResponse(
        String version,
        int companyId,
        int id,
        String shortname,
        String description,
        int mailinglistId,
        String mailinglist_shortname,
        String mailinglist_description,
        String mailingtype,
        String targetExpression,
        String mailing_content_type,
        String creationDate,
        Integer open_action_id,
        Integer click_action_id,
        List<MediatypeResponse> mediatypes,
        List<MailingParameterResponse> parameters,
        List<MailingComponentResponse> components,
        List<MailingContentResponse> contents,
        List<TrackableLinkFullResponse> links,
        List<MailingTargetResponse> targets
) {
}
