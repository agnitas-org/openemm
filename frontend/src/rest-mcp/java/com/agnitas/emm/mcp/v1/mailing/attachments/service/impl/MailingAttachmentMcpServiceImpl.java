/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.attachments.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.components.dto.MailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingAttachmentDto;
import com.agnitas.emm.core.components.service.MailingComponentAccessTokenService;
import com.agnitas.emm.core.components.service.MailingComponentsService;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailing.attachments.dto.AttachmentLightResponse;
import com.agnitas.emm.mcp.v1.mailing.attachments.dto.AttachmentResponse;
import com.agnitas.emm.mcp.v1.mailing.attachments.dto.UploadAttachmentRequest;
import com.agnitas.emm.mcp.v1.mailing.attachments.mapper.MailingAttachmentMcpMapper;
import com.agnitas.emm.mcp.v1.mailing.attachments.service.MailingAttachmentMcpService;
import com.agnitas.web.forms.PaginationForm;
import org.springframework.stereotype.Service;

@Service
public class MailingAttachmentMcpServiceImpl implements MailingAttachmentMcpService {

    private final MailingAttachmentMcpMapper mapper;
    protected final MailingComponentsService componentsService;
    private final MailingComponentAccessTokenService accessTokenService;

    public MailingAttachmentMcpServiceImpl(
            MailingAttachmentMcpMapper mapper,
            MailingComponentsService componentsService,
            MailingComponentAccessTokenService accessTokenService
    ) {
        this.mapper = mapper;
        this.componentsService = componentsService;
        this.accessTokenService = accessTokenService;
    }

    @Override
    public PageResponse<AttachmentLightResponse> list(int mailingId, McpPageable pageable, Admin admin) {
        PaginationForm filter = new PaginationForm();
        filter.setPage(pageable.page());
        filter.setNumberOfRows(pageable.size());

        PaginatedList<MailingAttachmentDto> attachments = componentsService.getAttachmentsOverview(
                filter,
                mailingId,
                admin.getCompanyID()
        );

        return mapper.toPageResponse(attachments, mapper::toLightResponse);
    }

    @Override
    public AttachmentResponse getById(int attachmentId, int mailingId, Admin admin) {
        MailingAttachmentDto attachment = componentsService.getAttachment(attachmentId, mailingId, admin.getCompanyID());
        return toResponse(attachment, admin);
    }

    @Override
    public AttachmentResponse getByFilename(String filename, int mailingId, Admin admin) {
        MailingAttachmentDto attachment = componentsService.getAttachment(filename, mailingId, admin.getCompanyID());
        return toResponse(attachment, admin);
    }

    private AttachmentResponse toResponse(MailingAttachmentDto attachment, Admin admin) {
        return mapper.toResponse(attachment, getDownloadUrl(attachment, admin));
    }

    private String getDownloadUrl(MailingAttachmentDto attachment, Admin admin) {
        String token = accessTokenService.sign(attachment.getId(), admin.getCompanyID());
        return componentsService.buildDownloadUrl(attachment.getId(), admin, token);
    }

    @Override
    public void deleteById(int attachmentId, int mailingId, Admin admin) {
        componentsService.deleteAttachment(attachmentId, mailingId, admin);
    }

    @Override
    public void deleteByFilename(String filename, int mailingId, Admin admin) {
        componentsService.deleteAttachment(filename, mailingId, admin);
    }

    @Override
    public AttachmentResponse create(UploadAttachmentRequest request, int mailingId, Admin admin) {
        UploadMailingAttachmentDto dto = mapper.toDto(request);
        componentsService.uploadAttachment(mailingId, admin, dto);
        return getByFilename(dto.getName(), mailingId, admin);
    }

}
