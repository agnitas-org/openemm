/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.links;

import java.util.Comparator;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.TrackableLink;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.trackablelinks.dto.PatchLinkCommand;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MailingLinkRestService {

    private final MailingLinkRestMapper mapper;
    private final TrackableLinkService linkService;
    private final CompanyTokenService companyTokenService;

    MailingLinkRestService(
            MailingLinkRestMapper mapper,
            TrackableLinkService linkService,
            CompanyTokenService companyTokenService
    ) {
        this.mapper = mapper;
        this.linkService = linkService;
        this.companyTokenService = companyTokenService;
    }

    public PageResponse<LinkResponse> list(int mailingId, Pageable pageable, Admin admin) {
        List<LinkResponse> links = linkService
                .getMailingForLinksOverview(mailingId, admin.getCompanyID(), false)
                .getTrackableLinks().values().stream()
                .sorted(Comparator.comparing(TrackableLink::getId))
                .map(link -> toResponse(link, admin.getCompanyID()))
                .toList();
        return PageResponse.of(links, pageable.getPageNumber(), pageable.getPageSize());
    }

    public LinkResponse find(int linkId, int mailingId, Admin admin) {
        TrackableLink link = linkService.getLink(linkId, mailingId, admin.getCompanyID());
        return toResponse(link, admin.getCompanyID());
    }

    private LinkResponse toResponse(TrackableLink link, int companyId) {
        String companyToken = companyTokenService.getCompanyToken(companyId).orElse("");
        return mapper.toResponse(link, companyToken, companyId);
    }

    LinkResponse patch(int linkId, int mailingId, PatchLinkRequest request, Admin admin) {
        PatchLinkCommand command = mapper.toCommand(request);
        linkService.patch(linkId, mailingId, command, admin);
        return find(linkId, mailingId, admin);
    }

    public List<LinkResponse> getMailingLinks(Mailing mailing) {
        return MapUtils.emptyIfNull(mailing.getTrackableLinks()).values().stream()
                .map(link -> toResponse(link, mailing.getCompanyID()))
                .toList();
    }
}
