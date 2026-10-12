/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.images.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.components.dto.MailingImageDto;
import com.agnitas.emm.core.components.dto.UploadMailingImageDto;
import com.agnitas.emm.core.components.form.MailingImagesOverviewFilter;
import com.agnitas.emm.core.components.service.MailingComponentAccessTokenService;
import com.agnitas.emm.core.components.service.MailingComponentsService;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailing.images.dto.ImageLightResponse;
import com.agnitas.emm.mcp.v1.mailing.images.dto.ImageResponse;
import com.agnitas.emm.mcp.v1.mailing.images.dto.UploadMailingImageRequest;
import com.agnitas.emm.mcp.v1.mailing.images.mapper.MailingImageMcpMapper;
import com.agnitas.emm.mcp.v1.mailing.images.service.MailingImageMcpService;
import org.springframework.stereotype.Service;

@Service
public class MailingImageMcpServiceImpl implements MailingImageMcpService {

    private final MailingImageMcpMapper mapper;
    private final MailingComponentsService componentsService;
    private final MailingComponentAccessTokenService accessTokenService;

    public MailingImageMcpServiceImpl(
            MailingImageMcpMapper mapper,
            MailingComponentsService componentsService,
            MailingComponentAccessTokenService accessTokenService
    ) {
        this.mapper = mapper;
        this.componentsService = componentsService;
        this.accessTokenService = accessTokenService;
    }

    @Override
    public PageResponse<ImageLightResponse> list(int mailingId, McpPageable pageable, Admin admin) {
        MailingImagesOverviewFilter filter = new MailingImagesOverviewFilter();
        filter.setPage(pageable.page());
        filter.setNumberOfRows(pageable.size());

        PaginatedList<MailingImageDto> images = componentsService.getMailingImagesOverview(
                admin.getCompanyID(),
                mailingId,
                filter
        );

        return mapper.toPageResponse(images, mapper::toLightResponse);
    }

    @Override
    public ImageResponse getById(int imageId, int mailingId, Admin admin) {
        MailingImageDto image = componentsService.getImage(imageId, mailingId, admin.getCompanyID());
        return toResponse(image, admin);
    }

    @Override
    public ImageResponse getByFilename(String filename, int mailingId, Admin admin) {
        MailingImageDto image = componentsService.getImage(filename, mailingId, admin.getCompanyID());
        return toResponse(image, admin);
    }

    private ImageResponse toResponse(MailingImageDto image, Admin admin) {
        return mapper.toResponse(image, getDownloadUrl(image, admin));
    }

    private String getDownloadUrl(MailingImageDto image, Admin admin) {
        String token = accessTokenService.sign(image.getId(), admin.getCompanyID());
        return componentsService.buildDownloadUrl(image.getId(), admin, token);
    }

    @Override
    public void deleteById(int imageId, int mailingId, Admin admin) {
        componentsService.deleteImage(imageId, mailingId, admin);
    }

    @Override
    public void deleteByFilename(String filename, int mailingId, Admin admin) {
        componentsService.deleteImage(filename, mailingId, admin);
    }

    @Override
    public ImageResponse create(UploadMailingImageRequest request, int mailingId, Admin admin) {
        UploadMailingImageDto dto = mapper.toDto(request);
        MailingImageDto image = componentsService.uploadImage(dto, mailingId, admin);
        return toResponse(image, admin);
    }

}
