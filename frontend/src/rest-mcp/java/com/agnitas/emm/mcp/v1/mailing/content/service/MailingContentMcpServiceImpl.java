/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.content.service;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.emm.core.mailingcontent.dto.PatchMailingContentCommand;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.emm.mcp.v1.mailing.content.MailingContentResponse;
import com.agnitas.emm.mcp.v1.mailing.content.dto.PatchMailingContentRequest;
import com.agnitas.emm.mcp.v1.mailing.content.mapper.MailingContentMcpMapper;
import com.agnitas.service.MailingContentService;
import org.springframework.stereotype.Service;

@Service
public class MailingContentMcpServiceImpl implements MailingContentMcpService {

    private final MailingContentService contentService;
    private final MailingContentMcpMapper mapper;
    private final ThumbnailService thumbnailService;

    public MailingContentMcpServiceImpl(
            MailingContentService contentService,
            MailingContentMcpMapper mapper,
            ThumbnailService thumbnailService
    ) {
        this.contentService = contentService;
        this.mapper = mapper;
        this.thumbnailService = thumbnailService;
    }

    @Override
    public List<MailingContentResponse> list(int mailingId, int companyId) {
        List<DynamicTag> tags = contentService.getDynamicTags(mailingId, companyId);
        return mapper.toResponses(tags);
    }

    @Override
    public MailingContentResponse getById(int contentId, int mailingId, int companyId) {
        DynamicTag tag = contentService.getDynamicTag(contentId, mailingId, companyId);
        return mapper.toResponse(tag);
    }

    @Override
    public MailingContentResponse getByName(String name, int mailingId, int companyId) {
        int contentId = contentService.getIdByName(name, mailingId, companyId);
        return getById(contentId, mailingId, companyId);
    }

    @Override
    public MailingContentResponse patch(int contentId, int mailingId, PatchMailingContentRequest request, Admin admin) {
        PatchMailingContentCommand command = mapper.toPatchCommand(request);
        contentService.updateContent(contentId, mailingId, command, admin);
        thumbnailService.updateMailingThumbnailByWebservice(admin.getCompanyID(), mailingId);
        return getById(contentId, mailingId, admin.getCompanyID());
    }

    @Override
    public MailingContentResponse patch(String name, int mailingId, PatchMailingContentRequest request, Admin admin) {
        int contentId = contentService.getIdByName(name, mailingId, admin.getCompanyID());
        return patch(contentId, mailingId, request, admin);
    }

    @Override
    public void delete(int contentId, int mailingId, int companyId) {
        contentService.markDeleted(contentId, mailingId, companyId);
    }

    @Override
    public void delete(String name, int mailingId, int companyId) {
        contentService.markDeleted(name, mailingId, companyId);
    }
}
