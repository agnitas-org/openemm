/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.attachments;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.components.dto.MailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingAttachmentDto;
import com.agnitas.emm.core.components.service.MailingComponentsService;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.web.forms.PaginationForm;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
class MailingAttachmentRestService {

    private final MailingAttachmentRestMapper mapper;
    protected final MailingComponentsService componentsService;

    MailingAttachmentRestService(
            MailingAttachmentRestMapper mapper,
            MailingComponentsService componentsService
    ) {
        this.mapper = mapper;
        this.componentsService = componentsService;
    }

    public PageResponse<AttachmentResponse> list(int mailingId, Pageable pageable, Admin admin) {
        PaginationForm filter = new PaginationForm();
        filter.setPage(pageable.getPageNumber());
        filter.setNumberOfRows(pageable.getPageSize());
        PaginatedList<MailingAttachmentDto> attachments = componentsService.getAttachmentsOverview(
                filter,
                mailingId,
                admin.getCompanyID()
        );
        return mapper.toPageResponse(attachments, mapper::toResponse);
    }

    public AttachmentResponse find(int attachmentId, int mailingId, Admin admin) {
        MailingAttachmentDto dto = componentsService.getAttachment(attachmentId, mailingId, admin.getCompanyID());
        return mapper.toResponse(dto);
    }

    public AttachmentResponse find(String filename, int mailingId, Admin admin) {
        MailingAttachmentDto attachment = componentsService.getAttachment(filename, mailingId, admin.getCompanyID());
        return mapper.toResponse(attachment);
    }

    public ResponseEntity<InputStreamResource> getContent(int attachmentId, int mailingId, Admin admin) {
        MailingAttachmentDto dto = componentsService.getAttachment(attachmentId, mailingId, admin.getCompanyID());
        return getFileContent(dto, mailingId, admin);
    }

    public ResponseEntity<InputStreamResource> getContent(String filename, int mailingId, Admin admin) {
        MailingAttachmentDto dto = componentsService.getAttachment(filename, mailingId, admin.getCompanyID());
        return getFileContent(dto, mailingId, admin);
    }

    private ResponseEntity<InputStreamResource> getFileContent(
            MailingAttachmentDto attachment,
            int mailingId,
            Admin admin
    ) {
        InputStream in = componentsService.attachmentToStream(attachment.getId(), mailingId, admin.getCompanyID());

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(attachment.getName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .contentLength(attachment.getOriginalSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new InputStreamResource(in));
    }

    public AttachmentResponse create(UploadAttachmentRequest request, int mailingId, Admin admin) {
        UploadMailingAttachmentDto dto = mapper.toDto(request);
        componentsService.uploadAttachment(mailingId, admin, dto);
        return find(dto.getName(), mailingId, admin);
    }

    public void delete(int attachmentId, int mailingId, Admin admin) {
        componentsService.deleteAttachment(attachmentId, mailingId, admin);
    }

    public void delete(String filename, int mailingId, Admin admin) {
        componentsService.deleteAttachment(filename, mailingId, admin);
    }
}
