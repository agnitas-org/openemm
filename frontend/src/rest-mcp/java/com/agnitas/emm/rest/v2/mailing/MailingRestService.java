/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing;

import java.util.EnumMap;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.mailing.dto.PatchMailingSettingsCommand;
import com.agnitas.emm.core.mailing.service.CopyMailingService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.settings.MailingSettingsService;
import com.agnitas.emm.core.mailing.settings.MediaTypePatch;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.mailing.attachments.MailingAttachmentRestService;
import com.agnitas.emm.rest.v2.mailing.content.MailingContentRestService;
import com.agnitas.emm.rest.v2.mailing.dto.CreateMailingRequest;
import com.agnitas.emm.rest.v2.mailing.dto.ListMailingsRequest;
import com.agnitas.emm.rest.v2.mailing.dto.ListTemplatesRequest;
import com.agnitas.emm.rest.v2.mailing.dto.MailingFullResponse;
import com.agnitas.emm.rest.v2.mailing.dto.MailingLightResponse;
import com.agnitas.emm.rest.v2.mailing.dto.MailingResponse;
import com.agnitas.emm.rest.v2.mailing.dto.PatchMailingRequest;
import com.agnitas.emm.rest.v2.mailing.images.MailingImageRestService;
import com.agnitas.emm.rest.v2.mailing.links.MailingLinkRestService;
import com.agnitas.emm.rest.v2.target.TargetRestService;
import com.agnitas.service.ServiceResult;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
class MailingRestService {

    protected final MailingRestMapper mailingMapper;
    protected final MailingService mailingService;
    private final CopyMailingService copyMailingService;
    private final ThumbnailService thumbnailService;
    private final MailingSettingsService mailingSettingsService;
    private final TargetRestService targetService;
    private final MailingImageRestService mailingImageService;
    private final MailingAttachmentRestService attachmentService;
    private final MailingContentRestService contentService;
    private final MailingLinkRestService linkService;

    public MailingRestService(
            MailingService mailingService,
            MailingRestMapper mailingMapper,
            CopyMailingService copyMailingService,
            ThumbnailService thumbnailService,
            MailingSettingsService mailingSettingsService,
            TargetRestService targetService,
            MailingImageRestService mailingImageService,
            MailingAttachmentRestService attachmentService,
            MailingContentRestService contentService,
            MailingLinkRestService linkService
    ) {
        this.mailingService = mailingService;
        this.mailingMapper = mailingMapper;
        this.copyMailingService = copyMailingService;
        this.thumbnailService = thumbnailService;
        this.mailingSettingsService = mailingSettingsService;
        this.targetService = targetService;
        this.mailingImageService = mailingImageService;
        this.attachmentService = attachmentService;
        this.contentService = contentService;
        this.linkService = linkService;
    }

    public PageResponse<MailingLightResponse> listMailings(
            ListMailingsRequest request,
            Pageable pageable,
            Admin admin
    ) {
        return list(request, pageable, admin, false);
    }

    public PageResponse<MailingLightResponse> listTemplates(
            ListTemplatesRequest request,
            Pageable pageable,
            Admin admin
    ) {
        return list(mailingMapper.toListRequest(request), pageable, admin, true);
    }

    private PageResponse<MailingLightResponse> list(
            ListMailingsRequest request,
            Pageable pageable,
            Admin admin,
            boolean isTemplate
    ) {
        MailingsListProperties listProps = mailingMapper.toListProperties(request, pageable);
        listProps.setTemplate(isTemplate);
        PaginatedList<Map<String, Object>> overview = mailingService.getOverview(admin, listProps);
        return mailingMapper.toPageResponse(overview, m -> mailingMapper.toMailingLightResponse(m, isTemplate));
    }

    public void deleteMailing(int id, Admin admin) {
        delete(id, admin, false);
    }

    public void deleteTemplate(int id, Admin admin) {
        delete(id, admin, true);
    }

    private void delete(int id, Admin admin, boolean isTemplate) {
        mailingService.deleteMailing(id, admin, isTemplate);
    }

    public MailingResponse createMailing(CreateMailingRequest request, Admin admin) {
        return create(request, admin, false);
    }

    public MailingResponse createTemplate(CreateMailingRequest request, Admin admin) {
        return create(request, admin, true);
    }

    public MailingResponse create(CreateMailingRequest request, Admin admin, boolean isTemplate) {
        PatchMailingSettingsCommand command = mailingMapper.toPatchCommand(request, buildMediaTypePatches(request));
        ServiceResult<Integer> result = isTemplate
                ? mailingSettingsService.createStandardTemplate(command, admin)
                : mailingSettingsService.createMailing(command, admin);
        thumbnailService.updateMailingThumbnailByWebservice(admin.getCompanyID(), result.getResult());
        return find(result.getResult(), admin.getCompanyID(), isTemplate);
    }

    public MailingLightResponse createFromTemplate(int templateId, int companyId) {
        int newMailingId = mailingService.createMailingFromTemplate(templateId, companyId);
        return findMailingLight(newMailingId, companyId);
    }

    public MailingLightResponse copyMailing(int id, int companyId) {
        return copy(id, companyId, false);
    }

    public MailingLightResponse copyTemplate(int id, int companyId) {
        return copy(id, companyId, true);
    }

    private MailingLightResponse copy(int copyFromId, int companyId, boolean isTemplate) {
        int createdId = isTemplate
                ? copyMailingService.copyTemplate(copyFromId, companyId)
                : copyMailingService.copyMailing(copyFromId, companyId);
        thumbnailService.updateMailingThumbnailByWebservice(companyId, createdId);
        return isTemplate
                ? findTemplateLight(createdId, companyId)
                : findMailingLight(createdId, companyId);
    }

    public MailingResponse patchMailing(int mailingId, PatchMailingRequest request, Admin admin) {
        PatchMailingSettingsCommand command = mailingMapper.toPatchCommand(request, buildMediaTypePatches(request));
        mailingSettingsService.updateSettings(mailingId, command, admin);
        return findMailing(mailingId, admin.getCompanyID());
    }

    public MailingResponse patchTemplate(int mailingId, PatchMailingRequest request, Admin admin) {
        PatchMailingSettingsCommand command = mailingMapper.toPatchCommand(request, buildMediaTypePatches(request));
        mailingSettingsService.updateSettings(mailingId, command, admin);
        return findTemplate(mailingId, admin.getCompanyID());
    }

    // overridden in extended class
    protected Map<MediaTypes, MediaTypePatch> buildMediaTypePatches(PatchMailingRequest request) {
        Map<MediaTypes, MediaTypePatch> patches = new EnumMap<>(MediaTypes.class);
        if (request.hasAnyEmailMediaTypeField()) {
            patches.put(MediaTypes.EMAIL, mailingMapper.toEmailPatch(request));
        }
        return patches;
    }

    protected Map<MediaTypes, MediaTypePatch> buildMediaTypePatches(CreateMailingRequest request) {
        Map<MediaTypes, MediaTypePatch> patches = new EnumMap<>(MediaTypes.class);
        patches.put(MediaTypes.EMAIL, mailingMapper.toEmailPatch(request));
        return patches;
    }

    public MailingFullResponse findMailingFull(int mailingId, Admin admin) {
        Mailing mailing = mailingService.getMailingOnly(mailingId, admin.getCompanyID());
        return getMailingFullResponse(mailing, admin);
    }

    public MailingFullResponse findTemplateFull(int templateId, Admin admin) {
        Mailing template = mailingService.getTemplate(templateId, admin.getCompanyID());
        return getMailingFullResponse(template, admin);
    }

    private MailingFullResponse getMailingFullResponse(Mailing mailing, Admin admin) {
        return mailingMapper.toFullResponse(
                mailingMapper.toResponse(mailing),
                targetService.getMailingTargets(mailing, admin),
                mailingImageService.getMailingImages(mailing, admin),
                attachmentService.getAttachments(mailing),
                contentService.getMailingContentBlocks(mailing),
                linkService.getMailingLinks(mailing)
        );
    }

    public MailingLightResponse findMailingLight(int mailingId, int companyId) {
        Mailing mailing = mailingService.getMailingOnly(mailingId, companyId);
        return mailingMapper.toLightResponse(mailing);
    }

    public MailingLightResponse findTemplateLight(int templateId, int companyId) {
        Mailing template = mailingService.getTemplate(templateId, companyId);
        return mailingMapper.toLightResponse(template);
    }

    public MailingResponse findMailing(int mailingId, int companyId) {
        return find(mailingId, companyId, false);
    }

    public MailingResponse findTemplate(int mailingId, int companyId) {
        return find(mailingId, companyId, true);
    }

    private MailingResponse find(int mailingId, int companyId, boolean isTemplate) {
        Mailing mailing = isTemplate
                ? mailingService.getTemplate(mailingId, companyId)
                : mailingService.getMailingOnly(mailingId, companyId);
        return mailingMapper.toResponse(mailing);
    }
}
