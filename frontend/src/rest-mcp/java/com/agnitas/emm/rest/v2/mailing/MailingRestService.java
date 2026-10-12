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
import com.agnitas.emm.core.mailing.settings.MediatypePatch;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.mailing.dto.CreateMailingRequest;
import com.agnitas.emm.rest.v2.mailing.dto.ListMailingsRequest;
import com.agnitas.emm.rest.v2.mailing.dto.ListTemplatesRequest;
import com.agnitas.emm.rest.v2.mailing.dto.MailingFullResponse;
import com.agnitas.emm.rest.v2.mailing.dto.MailingLightResponse;
import com.agnitas.emm.rest.v2.mailing.dto.MailingResponse;
import com.agnitas.emm.rest.v2.mailing.dto.PatchMailingRequest;
import com.agnitas.service.MailingExporter;
import com.agnitas.service.ServiceResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
class MailingRestService {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .build();

    protected final MailingRestMapper mapper;
    protected final MailingService mailingService;
    private final MailingExporter mailingExporter;
    private final CopyMailingService copyMailingService;
    private final ThumbnailService thumbnailService;
    private final MailingSettingsService mailingSettingsService;

    public MailingRestService(
            MailingExporter mailingExporter,
            MailingService mailingService,
            MailingRestMapper mapper,
            CopyMailingService copyMailingService,
            ThumbnailService thumbnailService,
            MailingSettingsService mailingSettingsService
    ) {
        this.mailingExporter = mailingExporter;
        this.mailingService = mailingService;
        this.mapper = mapper;
        this.copyMailingService = copyMailingService;
        this.thumbnailService = thumbnailService;
        this.mailingSettingsService = mailingSettingsService;
    }

    public PageResponse<MailingLightResponse> listMailings(
            ListMailingsRequest request,
            Pageable pageable,
            Admin admin
    ) {
        MailingsListProperties listProps = mapper.toListProperties(request, pageable);
        return list(listProps, admin);
    }

    public PageResponse<MailingLightResponse> listTemplates(
            ListTemplatesRequest request,
            Pageable pageable,
            Admin admin
    ) {
        MailingsListProperties listProps = mapper.toListProperties(mapper.toListRequest(request), pageable);
        listProps.setTemplate(true);
        return list(listProps, admin);
    }

    private PageResponse<MailingLightResponse> list(MailingsListProperties listProps, Admin admin) {
        PaginatedList<Map<String, Object>> overview = mailingService.getOverview(admin, listProps);
        return mapper.toPageResponse(overview, mapper::toMailingLightResponse);
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
        PatchMailingSettingsCommand command = mapper.toPatchCommand(request, buildMediatypePatches(request));
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
        PatchMailingSettingsCommand command = mapper.toPatchCommand(request, buildMediatypePatches(request));
        mailingSettingsService.updateSettings(mailingId, command, admin);
        return findMailing(mailingId, admin.getCompanyID());
    }

    public MailingResponse patchTemplate(int mailingId, PatchMailingRequest request, Admin admin) {
        PatchMailingSettingsCommand command = mapper.toPatchCommand(request, buildMediatypePatches(request));
        mailingSettingsService.updateSettings(mailingId, command, admin);
        return findTemplate(mailingId, admin.getCompanyID());
    }

    // overridden in extended class
    protected Map<MediaTypes, MediatypePatch> buildMediatypePatches(PatchMailingRequest request) {
        Map<MediaTypes, MediatypePatch> patches = new EnumMap<>(MediaTypes.class);
        if (request.hasAnyEmailMediatypeField()) {
            patches.put(MediaTypes.EMAIL, mapper.toEmailPatch(request));
        }
        return patches;
    }

    protected Map<MediaTypes, MediatypePatch> buildMediatypePatches(CreateMailingRequest request) {
        Map<MediaTypes, MediatypePatch> patches = new EnumMap<>(MediaTypes.class);
        patches.put(MediaTypes.EMAIL, mapper.toEmailPatch(request));
        return patches;
    }

    public MailingFullResponse findMailingFull(int mailingId, int companyId) {
        byte[] bytes = mailingExporter.exportMailingToByteArray(mailingId, companyId);
        return JSON_MAPPER.readValue(bytes, MailingFullResponse.class);
    }

    public MailingFullResponse findTemplateFull(int templateId, int companyId) {
        byte[] bytes = mailingExporter.exportTemplateToByteArray(templateId, companyId);
        return JSON_MAPPER.readValue(bytes, MailingFullResponse.class);
    }

    public MailingLightResponse findMailingLight(int mailingId, int companyId) {
        Mailing mailing = mailingService.getMailingOnly(mailingId, companyId);
        return mapper.toLightResponse(mailing);
    }

    public MailingLightResponse findTemplateLight(int templateId, int companyId) {
        Mailing template = mailingService.getTemplate(templateId, companyId);
        return mapper.toLightResponse(template);
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
        return mapper.toResponse(mailing);
    }
}
