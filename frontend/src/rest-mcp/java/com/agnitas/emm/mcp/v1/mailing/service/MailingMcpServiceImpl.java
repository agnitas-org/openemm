/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.service;

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
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.CreateMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.ListMailingsRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.ListTemplatesRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingLightResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.PatchMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.mapper.MailingMcpMapper;
import com.agnitas.emm.mcp.v1.mailing.attachments.service.MailingAttachmentMcpService;
import com.agnitas.emm.mcp.v1.mailing.content.service.MailingContentMcpService;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingFullResponse;
import com.agnitas.emm.mcp.v1.mailing.images.service.MailingImageMcpService;
import com.agnitas.emm.mcp.v1.mailing.links.service.MailingLinkMcpService;
import com.agnitas.emm.mcp.v1.target.service.TargetGroupMcpService;
import com.agnitas.service.ServiceResult;
import org.springframework.stereotype.Service;

@Service
public class MailingMcpServiceImpl implements MailingMcpService {

    protected final MailingMcpMapper mailingMcpMapper;
    protected final MailingService mailingService;
    private final CopyMailingService copyMailingService;
    private final ThumbnailService thumbnailService;
    private final MailingSettingsService mailingSettingsService;
    private final TargetGroupMcpService targetService;
    private final MailingImageMcpService mailingImageService;
    private final MailingAttachmentMcpService attachmentService;
    private final MailingContentMcpService contentService;
    private final MailingLinkMcpService linkService;

    public MailingMcpServiceImpl(
            MailingService mailingService,
            MailingMcpMapper mailingMcpMapper,
            CopyMailingService copyMailingService,
            ThumbnailService thumbnailService,
            MailingSettingsService mailingSettingsService,
            TargetGroupMcpService targetService,
            MailingImageMcpService mailingImageService,
            MailingAttachmentMcpService attachmentService,
            MailingContentMcpService contentService,
            MailingLinkMcpService linkService
    ) {
        this.mailingService = mailingService;
        this.mailingMcpMapper = mailingMcpMapper;
        this.copyMailingService = copyMailingService;
        this.thumbnailService = thumbnailService;
        this.mailingSettingsService = mailingSettingsService;
        this.targetService = targetService;
        this.mailingImageService = mailingImageService;
        this.attachmentService = attachmentService;
        this.contentService = contentService;
        this.linkService = linkService;
    }

    @Override
    public PageResponse<MailingLightResponse> list(ListMailingsRequest request, McpPageable pageable, Admin admin) {
        return list(request, pageable, admin, false);
    }

    @Override
    public PageResponse<MailingLightResponse> listTemplates(
            ListTemplatesRequest request,
            McpPageable pageable,
            Admin admin
    ) {
        return list(mailingMcpMapper.toListRequest(request), pageable, admin, true);
    }

    private PageResponse<MailingLightResponse> list(
            ListMailingsRequest request,
            McpPageable pageable,
            Admin admin,
            boolean isTemplate
    ) {
        MailingsListProperties listProps = mailingMcpMapper.toListProperties(request, pageable);
        listProps.setTemplate(isTemplate);
        PaginatedList<Map<String, Object>> overview = mailingService.getOverview(admin, listProps);
        return mailingMcpMapper.toPageResponse(overview, m -> mailingMcpMapper.toMailingLightResponse(m, isTemplate));
    }

    @Override
    public MailingResponse getById(int id, int companyId) {
        Mailing mailing = mailingService.getMailingOnly(id, companyId);
        return mailingMcpMapper.toResponse(mailing);
    }

    @Override
    public MailingResponse getTemplate(int id, int companyId) {
        Mailing template = mailingService.getTemplate(id, companyId);
        return mailingMcpMapper.toResponse(template);
    }

    @Override
    public MailingFullResponse getMailingFullById(int mailingId, Admin admin) {
        Mailing mailing = mailingService.getMailingOnly(mailingId, admin.getCompanyID());
        return getMailingFullResponse(mailing, admin);
    }

    @Override
    public MailingFullResponse getTemplateFullById(int templateId, Admin admin) {
        Mailing template = mailingService.getTemplate(templateId, admin.getCompanyID());
        return getMailingFullResponse(template, admin);
    }

    private MailingFullResponse getMailingFullResponse(Mailing mailing, Admin admin) {
        return mailingMcpMapper.toFullResponse(
                mailingMcpMapper.toResponse(mailing),
                targetService.getMailingTargets(mailing, admin),
                mailingImageService.getMailingImages(mailing, admin),
                attachmentService.getAttachments(mailing, admin),
                contentService.getMailingContentBlocks(mailing),
                linkService.getMailingLinks(mailing)
        );
    }

    @Override
    public MailingResponse create(CreateMailingRequest request, Admin admin) {
        PatchMailingSettingsCommand command = mailingMcpMapper.toPatchCommand(request, buildMediaTypePatches(request));
        ServiceResult<Integer> result = mailingSettingsService.createMailing(command, admin);
        thumbnailService.updateMailingThumbnailByWebservice(admin.getCompanyID(), result.getResult());
        return getById(result.getResult(), admin.getCompanyID());
    }

    @Override
    public MailingResponse createTemplate(CreateMailingRequest request, Admin admin) {
        PatchMailingSettingsCommand command = mailingMcpMapper.toPatchCommand(request, buildMediaTypePatches(request));
        ServiceResult<Integer> result = mailingSettingsService.createStandardTemplate(command, admin);
        thumbnailService.updateMailingThumbnailByWebservice(admin.getCompanyID(), result.getResult());
        return getTemplate(result.getResult(), admin.getCompanyID());
    }

    @Override
    public MailingLightResponse createFromTemplate(int templateId, int companyId) {
        int newMailingId = mailingService.createMailingFromTemplate(templateId, companyId);
        return findMailingLight(newMailingId, companyId);
    }

    @Override
    public MailingLightResponse copy(int copyFromId, int companyId) {
        int newMailingId = copyMailingService.copyMailing(copyFromId, companyId);
        thumbnailService.updateMailingThumbnailByWebservice(companyId, newMailingId);
        return findMailingLight(newMailingId, companyId);
    }

    @Override
    public MailingLightResponse copyTemplate(int copyFromId, int companyId) {
        int newTemplateId = copyMailingService.copyTemplate(copyFromId, companyId);
        thumbnailService.updateMailingThumbnailByWebservice(companyId, newTemplateId);
        return findTemplateLight(newTemplateId, companyId);
    }

    @Override
    public MailingResponse patch(int id, PatchMailingRequest request, Admin admin) {
        PatchMailingSettingsCommand command = mailingMcpMapper.toPatchCommand(request, buildMediaTypePatches(request));
        mailingSettingsService.updateSettings(id, command, admin);
        return getById(id, admin.getCompanyID());
    }

    @Override
    public MailingResponse patchTemplate(int id, PatchMailingRequest request, Admin admin) {
        PatchMailingSettingsCommand command = mailingMcpMapper.toPatchCommand(request, buildMediaTypePatches(request));
        mailingSettingsService.updateSettings(id, command, admin);
        return getTemplate(id, admin.getCompanyID());
    }

    @Override
    public void delete(int id, Admin admin) {
        mailingService.deleteMailing(id, admin, false);
    }

    @Override
    public void deleteTemplate(int id, Admin admin) {
        mailingService.deleteMailing(id, admin, true);
    }

    public MailingLightResponse findMailingLight(int mailingId, int companyId) {
        Mailing mailing = mailingService.getMailingOnly(mailingId, companyId);
        return mailingMcpMapper.toLightResponse(mailing);
    }

    public MailingLightResponse findTemplateLight(int templateId, int companyId) {
        Mailing template = mailingService.getTemplate(templateId, companyId);
        return mailingMcpMapper.toLightResponse(template);
    }

    // overridden in extended class
    protected Map<MediaTypes, MediaTypePatch> buildMediaTypePatches(PatchMailingRequest request) {
        Map<MediaTypes, MediaTypePatch> patches = new EnumMap<>(MediaTypes.class);
        if (request.hasAnyEmailMediaTypeField()) {
            patches.put(MediaTypes.EMAIL, mailingMcpMapper.toEmailPatch(request));
        }
        return patches;
    }

    protected Map<MediaTypes, MediaTypePatch> buildMediaTypePatches(CreateMailingRequest request) {
        Map<MediaTypes, MediaTypePatch> patches = new EnumMap<>(MediaTypes.class);
        patches.put(MediaTypes.EMAIL, mailingMcpMapper.toEmailPatch(request));
        return patches;
    }

}
