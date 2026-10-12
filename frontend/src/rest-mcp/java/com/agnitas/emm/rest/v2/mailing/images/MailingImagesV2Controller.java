/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.images;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mailings/{mailingId:\\d+}/images")
class MailingImagesV2Controller {

    private final MailingImageRestService restService;

    MailingImagesV2Controller(MailingImageRestService restService) {
        this.restService = restService;
    }

    @GetMapping
    @PermissionAllowed("mailing.content.show")
    PageResponse<ImageResponse> list(@PathVariable @Positive int mailingId, Pageable pageable, Admin admin) {
        return restService.list(mailingId, pageable, admin);
    }

    @GetMapping("/by-name")
    @PermissionAllowed("mailing.content.show")
    public ImageResponse find(
            @PathVariable @Positive int mailingId,
            @RequestParam @NotBlank String filename,
            Admin admin
    ) {
        return restService.find(filename, mailingId, admin);
    }

    @PermissionAllowed("mailing.content.show")
    @GetMapping("/{imageId:\\d+}")
    public ImageResponse find(
            @PathVariable @Positive int imageId,
            @PathVariable @Positive int mailingId,
            Admin admin
    ) {
        return restService.find(imageId, mailingId, admin);
    }

    @GetMapping("/{imageId:\\d+}/content")
    @PermissionAllowed("mailing.content.show")
    public ResponseEntity<byte[]> getContent(
            @PathVariable @Positive int imageId,
            @PathVariable @Positive int mailingId,
            Admin admin,
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        return restService.getContent(imageId, mailingId, admin, req, resp);
    }

    @GetMapping("/by-name/content")
    @PermissionAllowed("mailing.content.show")
    public ResponseEntity<byte[]> getContent(
            @PathVariable @Positive int mailingId,
            @RequestParam @NotBlank String filename,
            Admin admin,
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        return restService.getContent(filename, mailingId, admin, req, resp);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PermissionAllowed("mailing.components.change")
    @ResponseStatus(HttpStatus.CREATED)
    public ImageResponse create(
            @PathVariable @Positive int mailingId,
            @Valid UploadMailingImageRequest request,
            Admin admin
    ) {
        return restService.create(request, mailingId, admin);
    }

    @DeleteMapping("/{imageId:\\d+}")
    @PermissionAllowed("mailing.components.change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive int imageId,
            @PathVariable @Positive int mailingId,
            Admin admin
    ) {
        restService.delete(imageId, mailingId, admin);
    }

    @DeleteMapping("/by-name")
    @PermissionAllowed("mailing.components.change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive int mailingId,
            @RequestParam @NotBlank String filename,
            Admin admin
    ) {
        restService.delete(filename, mailingId, admin);
    }
}
