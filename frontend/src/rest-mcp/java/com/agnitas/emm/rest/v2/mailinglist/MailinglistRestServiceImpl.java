/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailinglist;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.commons.pagination.PaginatedFilter;
import com.agnitas.emm.core.mailinglist.MailinglistSortCriteria;
import com.agnitas.emm.core.mailinglist.dto.CreateMailinglistDTO;
import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;
import com.agnitas.emm.core.mailinglist.dto.PatchMailinglistCommand;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.rest.v2.infrastructure.exception.BadRequestException;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.service.SimpleServiceResult;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
class MailinglistRestServiceImpl implements MailinglistRestService {

    private final MailinglistService mailinglistService;
    private final MailinglistRestMapper mailinglistRestMapper;

    public MailinglistRestServiceImpl(
            MailinglistService mailinglistService,
            MailinglistRestMapper mailinglistRestMapper
    ) {
        this.mailinglistService = mailinglistService;
        this.mailinglistRestMapper = mailinglistRestMapper;
    }

    @Override
    public PageResponse<MailinglistResponse> findAll(Pageable pageable, int companyId) {
        PaginatedFilter<MailinglistSortCriteria> pagination = mailinglistRestMapper.toPaginationFilter(pageable);
        PaginatedList<Mailinglist> mailinglists = mailinglistService.findAll(pagination, companyId);
        return mailinglistRestMapper.toPageResponse(mailinglists, mailinglistRestMapper::toResponse);
    }

    @Override
    public MailinglistResponse getById(int mailinglistId, int companyId) {
        Mailinglist mailinglists = mailinglistService.getMailinglist(mailinglistId, companyId);
        return mailinglistRestMapper.toResponse(mailinglists);
    }

    @Override
    public MailinglistResponse create(CreateMailinglistRequest request, int companyId) {
        CreateMailinglistDTO createDTO = mailinglistRestMapper.toDto(request);
        MailinglistDto dto = mailinglistService.create(createDTO, companyId);
        return mailinglistRestMapper.toResponse(dto);
    }

    @Override
    public MailinglistResponse updatePartially(int mailinglistId, int companyId, PatchMailinglistRequest updateDto) {
        PatchMailinglistCommand patch = mailinglistRestMapper.toPatch(updateDto, mailinglistId, companyId);
        Mailinglist mailinglist = mailinglistService.update(patch);
        return mailinglistRestMapper.toResponse(mailinglist);
    }

    @Override
    public void delete(int mailinglistId, Admin admin) {
        SimpleServiceResult canBeDeleted = mailinglistService.checkCanBeDeleted(mailinglistId, admin);
        if (!canBeDeleted.isSuccess()) {
            throw new BadRequestException(canBeDeleted.getErrorMessages());
        }
        mailinglistService.deleteMailinglist(mailinglistId, admin.getCompanyID());
    }
}
