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
import com.agnitas.emm.core.mailing.settings.MediatypePatch;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.CreateMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.ListMailingsRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.ListTemplatesRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingFullResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingLightResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.PatchMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.mapper.MailingMcpMapper;
import com.agnitas.service.MailingExporter;
import com.agnitas.service.ServiceResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class MailingMcpServiceImpl implements MailingMcpService {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .build();

    protected final MailingMcpMapper mailingMcpMapper;
    protected final MailingService mailingService;
    private final CopyMailingService copyMailingService;
    private final ThumbnailService thumbnailService;
    private final MailingExporter mailingExporter;
    private final MailingSettingsService mailingSettingsService;

    public MailingMcpServiceImpl(
            MailingService mailingService,
            MailingMcpMapper mailingMcpMapper,
            CopyMailingService copyMailingService,
            ThumbnailService thumbnailService,
            MailingExporter mailingExporter,
            MailingSettingsService mailingSettingsService
    ) {
        this.mailingService = mailingService;
        this.mailingMcpMapper = mailingMcpMapper;
        this.copyMailingService = copyMailingService;
        this.thumbnailService = thumbnailService;
        this.mailingExporter = mailingExporter;
        this.mailingSettingsService = mailingSettingsService;
    }

    @Override
    public PageResponse<MailingLightResponse> list(ListMailingsRequest query, McpPageable pageable, Admin admin) {
        return list(mailingMcpMapper.toListProperties(query, pageable), admin);
    }

    @Override
    public PageResponse<MailingLightResponse> listTemplates(ListTemplatesRequest query, McpPageable pageable, Admin admin) {
        MailingsListProperties listProps = mailingMcpMapper.toListProperties(mailingMcpMapper.toListRequest(query), pageable);
        listProps.setTemplate(true);
        return list(listProps, admin);
    }

    private PageResponse<MailingLightResponse> list(MailingsListProperties listProps, Admin admin) {
        PaginatedList<Map<String, Object>> overview = mailingService.getOverview(admin, listProps);
        return mailingMcpMapper.toPageResponse(overview);
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
    public MailingFullResponse getFull(int id, int companyId) {
        try {
            byte[] bytes = mailingExporter.exportMailingToByteArray(id, companyId);
            return JSON_MAPPER.readValue(bytes, MailingFullResponse.class);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error occurred while trying to export mailing to JSON.",
                    e
            );
        }
    }

    @Override
    public MailingFullResponse getFullTemplate(int id, int companyId) {
        try {
            byte[] bytes = mailingExporter.exportTemplateToByteArray(id, companyId);
            return JSON_MAPPER.readValue(bytes, MailingFullResponse.class);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error occurred while trying to export template to JSON.",
                    e
            );
        }
    }

    @Override
    public MailingResponse create(CreateMailingRequest request, Admin admin) {
        PatchMailingSettingsCommand command = mailingMcpMapper.toPatchCommand(request, buildMediatypePatches(request));
        ServiceResult<Integer> result = mailingSettingsService.createMailing(command, admin);
        thumbnailService.updateMailingThumbnailByWebservice(admin.getCompanyID(), result.getResult());
        return getById(result.getResult(), admin.getCompanyID());
    }

    @Override
    public MailingResponse createTemplate(CreateMailingRequest request, Admin admin) {
        PatchMailingSettingsCommand command = mailingMcpMapper.toPatchCommand(request, buildMediatypePatches(request));
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
        PatchMailingSettingsCommand command = mailingMcpMapper.toPatchCommand(request, buildMediatypePatches(request));
        mailingSettingsService.updateSettings(id, command, admin);
        return getById(id, admin.getCompanyID());
    }

    @Override
    public MailingResponse patchTemplate(int id, PatchMailingRequest request, Admin admin) {
        PatchMailingSettingsCommand command = mailingMcpMapper.toPatchCommand(request, buildMediatypePatches(request));
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
    protected Map<MediaTypes, MediatypePatch> buildMediatypePatches(PatchMailingRequest request) {
        Map<MediaTypes, MediatypePatch> patches = new EnumMap<>(MediaTypes.class);
        if (request.hasAnyEmailMediatypeField()) {
            patches.put(MediaTypes.EMAIL, mailingMcpMapper.toEmailPatch(request));
        }
        return patches;
    }

    protected Map<MediaTypes, MediatypePatch> buildMediatypePatches(CreateMailingRequest request) {
        Map<MediaTypes, MediatypePatch> patches = new EnumMap<>(MediaTypes.class);
        patches.put(MediaTypes.EMAIL, mailingMcpMapper.toEmailPatch(request));
        return patches;
    }

}
