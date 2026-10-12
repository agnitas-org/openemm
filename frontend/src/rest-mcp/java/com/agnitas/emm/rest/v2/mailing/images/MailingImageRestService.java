/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.images;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.components.dto.MailingImageDto;
import com.agnitas.emm.core.components.dto.UploadMailingImageDto;
import com.agnitas.emm.core.components.form.MailingImagesOverviewFilter;
import com.agnitas.emm.core.components.service.MailingComponentsService;
import com.agnitas.emm.responseheaders.common.UsedFor;
import com.agnitas.emm.responseheaders.web.HttpResponseHeaderApplier;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
class MailingImageRestService {

    private static final Set<String> INLINE_SAFE_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp",
            "image/bmp",
            "image/x-icon"
    );
    private final MailingImageRestMapper mapper;
    private final MailingComponentsService componentsService;
    private final HttpResponseHeaderApplier responseHeaderApplier;

    MailingImageRestService(
            MailingImageRestMapper mapper,
            MailingComponentsService componentsService,
            HttpResponseHeaderApplier responseHeaderApplier
    ) {
        this.mapper = mapper;
        this.componentsService = componentsService;
        this.responseHeaderApplier = responseHeaderApplier;
    }

    public PageResponse<ImageResponse> list(int mailingId, Pageable pageable, Admin admin) {
        MailingImagesOverviewFilter filter = new MailingImagesOverviewFilter();
        filter.setPage(pageable.getPageNumber());
        filter.setNumberOfRows(pageable.getPageSize());
        PaginatedList<MailingImageDto> images = componentsService.getMailingImagesOverview(
                admin.getCompanyID(),
                mailingId,
                filter
        );
        return mapper.toPageResponse(images, mapper::toResponse);
    }

    public ImageResponse find(int imageId, int mailingId, Admin admin) {
        MailingImageDto image = componentsService.getImage(imageId, mailingId, admin.getCompanyID());
        return mapper.toResponse(image);
    }

    public ImageResponse find(String filename, int mailingId, Admin admin) {
        MailingImageDto image = componentsService.getImage(filename, mailingId, admin.getCompanyID());
        return mapper.toResponse(image);
    }

    public ImageResponse create(UploadMailingImageRequest request, int mailingId, Admin admin) {
        UploadMailingImageDto dto = mapper.toDto(request);
        MailingImageDto image = componentsService.uploadImage(dto, mailingId, admin);
        return mapper.toResponse(image);
    }


    public void delete(int imageId, int mailingId, Admin admin) {
        componentsService.deleteImage(imageId, mailingId, admin);
    }

    public void delete(String filename, int mailingId, Admin admin) {
        componentsService.deleteImage(filename, mailingId, admin);
    }

    public ResponseEntity<byte[]> getContent(
            int imageId,
            int mailingId,
            Admin admin,
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        MailingComponent comp = componentsService.getImageCompById(imageId, mailingId, admin.getCompanyID());
        return getFileContent(comp, admin, req, resp);
    }

    public ResponseEntity<byte[]> getContent(
            String filename,
            int mailingId,
            Admin admin,
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        MailingComponent comp = componentsService.getImageCompByFileName(filename, mailingId, admin.getCompanyID());
        return getFileContent(comp, admin, req, resp);
    }

    public ResponseEntity<byte[]> getFileContent(
            MailingComponent comp,
            Admin admin,
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        MediaType declared = parseOrDefault(comp.getMimeType());
        String essence = (declared.getType() + "/" + declared.getSubtype()).toLowerCase(Locale.ROOT);
        boolean inlineSafe = INLINE_SAFE_TYPES.contains(essence);

        MediaType mediaType = inlineSafe
                ? MediaType.parseMediaType(essence)
                : MediaType.APPLICATION_OCTET_STREAM;
        ContentDisposition disposition = (inlineSafe ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(comp.getComponentName(), StandardCharsets.UTF_8)
                .build();

        responseHeaderApplier.applyHeadersToResponse(UsedFor.RESOURCE, admin.getCompanyID(), req, resp);
        resp.setHeader(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Content-Security-Policy", "default-src 'none'; sandbox");
        resp.setHeader(HttpHeaders.CONTENT_TYPE, mediaType.toString());
        resp.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(comp.getBinaryBlock().length));

        return ResponseEntity.ok().body(comp.getBinaryBlock());
    }

    private static MediaType parseOrDefault(String mimeType) {
        if (StringUtils.isBlank(mimeType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(mimeType);
        } catch (InvalidMediaTypeException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
