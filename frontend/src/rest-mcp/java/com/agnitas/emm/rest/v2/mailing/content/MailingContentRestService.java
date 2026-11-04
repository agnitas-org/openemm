/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.mailingcontent.dto.PatchMailingContentCommand;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.service.MailingContentService;
import com.agnitas.util.LinkUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

@Service
public class MailingContentRestService {

    private final MailingContentService contentService;
    private final MailingContentRestMapper mapper;
    private final ThumbnailService thumbnailService;
    private final CompanyTokenService companyTokenService;

    MailingContentRestService(
            MailingContentService contentService,
            MailingContentRestMapper mapper,
            ThumbnailService thumbnailService,
            CompanyTokenService companyTokenService
    ) {
        this.contentService = contentService;
        this.mapper = mapper;
        this.thumbnailService = thumbnailService;
        this.companyTokenService = companyTokenService;
    }

    public List<MailingContentResponse> list(int mailingId, int companyId) {
        List<DynamicTag> dynamicTags = contentService.getDynamicTags(mailingId, companyId);
        putPlaceholders(dynamicTags, companyId);
        return mapper.toResponse(dynamicTags);
    }

    public List<MailingContentResponse> getMailingContentBlocks(Mailing mailing) {
        Collection<DynamicTag> dynamicTags = MapUtils.emptyIfNull(mailing.getDynTags()).values();
        putPlaceholders(dynamicTags, mailing.getCompanyID());
        return mapper.toResponse(new ArrayList<>(dynamicTags));
    }

    private void putPlaceholders(Collection<DynamicTag> tags, int companyId) {
        tags.forEach(tag -> putPlaceholders(tag, companyId));
    }

    private void putPlaceholders(DynamicTag tag, int companyId) {
        String companyToken = companyTokenService.getCompanyToken(companyId).orElse("");
        MapUtils.emptyIfNull(tag.getDynContent()).values()
                .forEach(content -> {
                    String text = LinkUtils.replaceCompanyParamValuesWithPlaceholders(
                            content.getDynContent(), companyToken, companyId
                    );
                    content.setDynContent(text);
                });
    }

    public MailingContentResponse getById(int contentId, int mailingId, int companyId) {
        DynamicTag dynTag = contentService.getDynamicTag(contentId, mailingId, companyId);
        putPlaceholders(dynTag, companyId);
        return mapper.toResponse(dynTag);
    }

    public MailingContentResponse getByName(String contentName, int mailingId, int companyId) {
        int contentId = contentService.getIdByName(contentName, mailingId, companyId);
        return getById(contentId, mailingId, companyId);
    }

    public void delete(int contentId, int mailingId, int companyId) {
        contentService.markDeleted(contentId, mailingId, companyId);
    }

    public void delete(String name, int mailingId, int companyId) {
        contentService.markDeleted(name, mailingId, companyId);
    }

    public MailingContentResponse patch(
            String contentName,
            int mailingId,
            PatchMailingContentRequest request,
            Admin admin
    ) {
        int contentId = contentService.getIdByName(contentName, mailingId, admin.getCompanyID());
        return patch(contentId, mailingId, request, admin);
    }

    public MailingContentResponse patch(
            int contentId,
            int mailingId,
            PatchMailingContentRequest request,
            Admin admin
    ) {
        PatchMailingContentCommand command = mapper.toPatchCommand(request);
        contentService.updateContent(contentId, mailingId, command, admin);
        thumbnailService.updateMailingThumbnailByWebservice(admin.getCompanyID(), mailingId);
        return getById(contentId, mailingId, admin.getCompanyID());
    }
}
