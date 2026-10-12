/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.links.service;

import java.util.Comparator;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.TrackableLink;
import com.agnitas.emm.core.trackablelinks.dto.PatchLinkCommand;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailing.links.MailingLinkResponse;
import com.agnitas.emm.mcp.v1.mailing.links.dto.PatchMailingLinkRequest;
import com.agnitas.emm.mcp.v1.mailing.links.mapper.MailingLinkMcpMapper;
import org.springframework.stereotype.Service;

@Service
public class MailingLinkMcpServiceImpl implements MailingLinkMcpService {

    private final TrackableLinkService linkService;
    private final MailingLinkMcpMapper mapper;

    public MailingLinkMcpServiceImpl(TrackableLinkService linkService, MailingLinkMcpMapper mapper) {
        this.linkService = linkService;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<MailingLinkResponse> list(int mailingId, McpPageable pageable, int companyId) {
        List<MailingLinkResponse> links = linkService
                .getMailingForLinksOverview(mailingId, companyId, false)
                .getTrackableLinks()
                .values()
                .stream()
                .sorted(Comparator.comparing(TrackableLink::getId))
                .map(mapper::toResponse)
                .toList();

        return PageResponse.of(links, pageable.page(), pageable.size());
    }

    @Override
    public MailingLinkResponse getById(int linkId, int mailingId, int companyId) {
        TrackableLink link = linkService.getLink(linkId, mailingId, companyId);
        return mapper.toResponse(link);
    }

    @Override
    public MailingLinkResponse patch(int linkId, int mailingId, PatchMailingLinkRequest request, Admin admin) {
        PatchLinkCommand command = mapper.toCommand(request);
        linkService.patch(linkId, mailingId, command, admin);
        return getById(linkId, mailingId, admin.getCompanyID());
    }

}
