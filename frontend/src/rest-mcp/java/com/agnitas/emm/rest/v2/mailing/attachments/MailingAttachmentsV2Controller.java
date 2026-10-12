/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.attachments;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mailings/{mailingId:\\d+}/attachments")
class MailingAttachmentsV2Controller {

    private final MailingAttachmentRestService restService;

    MailingAttachmentsV2Controller(MailingAttachmentRestService restService) {
        this.restService = restService;
    }

    @GetMapping
    @PermissionAllowed("mailing.attachments.show")
    PageResponse<AttachmentResponse> list(@PathVariable @Positive int mailingId, Pageable pageable, Admin admin) {
        return restService.list(mailingId, pageable, admin);
    }

    @GetMapping("/{attachmentId:\\d+}")
    @PermissionAllowed("mailing.attachments.show")
    public AttachmentResponse find(
            @PathVariable @Positive int attachmentId,
            @PathVariable @Positive int mailingId,
            Admin admin
    ) {
        return restService.find(attachmentId, mailingId, admin);
    }

    @GetMapping("/by-name/{filename}")
    @PermissionAllowed("mailing.attachments.show")
    public AttachmentResponse find(
            @PathVariable @Positive int mailingId,
            @PathVariable @NotBlank String filename,
            Admin admin
    ) {
        return restService.find(filename, mailingId, admin);
    }

    @GetMapping("/{attachmentId:\\d+}/content")
    @PermissionAllowed("mailing.attachments.show")
    public ResponseEntity<InputStreamResource> getContent(
            @PathVariable @Positive int attachmentId,
            @PathVariable @Positive int mailingId,
            Admin admin
    ) {
        return restService.getContent(attachmentId, mailingId, admin);
    }

    @GetMapping("/by-name/{filename}/content")
    @PermissionAllowed("mailing.attachments.show")
    public ResponseEntity<InputStreamResource> getContent(
            @PathVariable @Positive int mailingId,
            @PathVariable @NotBlank String filename,
            Admin admin
    ) {
        return restService.getContent(filename, mailingId, admin);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PermissionAllowed("mailing.attachments.show")
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse create(
            @PathVariable @Positive int mailingId,
            @Valid UploadAttachmentRequest request,
            Admin admin
    ) {
        return restService.create(request, mailingId, admin);
    }

    @DeleteMapping("/{attachmentId:\\d+}")
    @PermissionAllowed("mailing.attachments.show")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive int attachmentId,
            @PathVariable @Positive int mailingId,
            Admin admin
    ) {
        restService.delete(attachmentId, mailingId, admin);
    }

    @DeleteMapping("/by-name/{filename}")
    @PermissionAllowed("mailing.attachments.show")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive int mailingId,
            @PathVariable @NotBlank String filename,
            Admin admin
    ) {
        restService.delete(filename, mailingId, admin);
    }
}
