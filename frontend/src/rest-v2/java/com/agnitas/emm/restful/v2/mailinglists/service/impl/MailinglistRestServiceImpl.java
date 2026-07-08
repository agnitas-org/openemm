/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.mailinglists.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.restful.v2.infrastructure.exception.BadRequestException;
import com.agnitas.emm.restful.v2.infrastructure.search.mapper.PaginationMapper;
import com.agnitas.emm.restful.v2.mailinglists.dto.CreateMailinglistRequest;
import com.agnitas.emm.restful.v2.mailinglists.dto.MailinglistResponse;
import com.agnitas.emm.restful.v2.mailinglists.dto.MailinglistsPage;
import com.agnitas.emm.restful.v2.mailinglists.dto.UpdateMailinglistRequest;
import com.agnitas.emm.restful.v2.mailinglists.exception.MailinglistNotFoundException;
import com.agnitas.emm.restful.v2.mailinglists.mapper.MailinglistRestMapper;
import com.agnitas.emm.restful.v2.mailinglists.service.MailinglistRestService;
import com.agnitas.service.ServiceResult;
import com.agnitas.web.forms.PaginationForm;
import org.springframework.stereotype.Service;

@Service
public class MailinglistRestServiceImpl implements MailinglistRestService {

    private final MailinglistService mailinglistService;
    private final MailinglistRestMapper mailinglistRestMapper;
    private final PaginationMapper paginationMapper;

    public MailinglistRestServiceImpl(MailinglistService mailinglistService,
                                      MailinglistRestMapper mailinglistRestMapper,
                                      PaginationMapper paginationMapper) {
        this.mailinglistService = mailinglistService;
        this.mailinglistRestMapper = mailinglistRestMapper;
        this.paginationMapper = paginationMapper;
    }

    @Override
    public PaginatedList<MailinglistResponse> findAll(MailinglistsPage pageForm, int companyId) {
        PaginationForm paginationForm = paginationMapper.toPaginationForm(pageForm);
        PaginatedList<Mailinglist> mailinglists = mailinglistService.findAll(paginationForm, companyId);
        return mailinglistRestMapper.toResponseList(mailinglists);
    }

    @Override
    public MailinglistResponse getById(int mailinglistId, int companyId) {
        return Optional.ofNullable(mailinglistService.getOrNull(mailinglistId, companyId))
            .map(mailinglistRestMapper::toResponse)
            .orElseThrow(() -> new MailinglistNotFoundException(mailinglistId));
    }

    @Override
    public MailinglistResponse create(CreateMailinglistRequest createDto, int companyId) {
        MailinglistDto dto = mailinglistRestMapper.createRequestToDto(createDto);
        int id = mailinglistService.saveMailinglist(companyId, dto);
        dto.setId(id);
        return mailinglistRestMapper.dtoToRestResponse(dto);
    }

    @Override
    public MailinglistResponse updatePartially(int mailinglistId, int companyId,
                                               UpdateMailinglistRequest updateDto) {
        MailinglistDto dto = Optional.ofNullable(mailinglistService.getOrNull(mailinglistId, companyId))
            .map(mailinglistRestMapper::toDto)
            .orElseThrow(() -> new MailinglistNotFoundException(mailinglistId));

        updateDto.name().ifPresent(dto::setShortname);
        updateDto.description().ifPresent(dto::setDescription);

        mailinglistService.saveMailinglist(companyId, dto);
        return mailinglistRestMapper.dtoToRestResponse(dto);
    }

    @Override
    public void delete(int mailinglistId, Admin admin) {
        ServiceResult<List<Mailinglist>> allowed = mailinglistService.getAllowedForDeletion(Set.of(mailinglistId), admin);
        if (!allowed.isSuccess()) {
            throw new BadRequestException(new HashSet<>(allowed.getErrorMessages()));
        }
        mailinglistService.deleteMailinglist(mailinglistId, admin.getCompanyID());
    }
}
