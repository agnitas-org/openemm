/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.form.BounceFilterListForm;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.mailloop.util.SecurityTokenGenerator;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.beans.Mailloop;
import com.agnitas.beans.MailloopEntry;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.dao.MailloopDao;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.Const;
import com.agnitas.util.DateUtilities;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service("BounceFilterService")
public class BounceFilterServiceImpl implements BounceFilterService {

    private final MailloopDao mailloopDao;
    private final ExtendedConversionService conversionService;
    private final BlacklistService blacklistService;
    private final BulkActionValidationService<Integer, BounceFilterDto> bulkActionValidationService;

    public BounceFilterServiceImpl(MailloopDao mailloopDao, ExtendedConversionService conversionService, BlacklistService blacklistService,
                                   BulkActionValidationService<Integer, BounceFilterDto> bulkActionValidationService) {
        this.mailloopDao = mailloopDao;
        this.conversionService = conversionService;
        this.blacklistService = blacklistService;
        this.bulkActionValidationService = bulkActionValidationService;
    }

	@Override
	public int saveBounceFilter(Admin admin, BounceFilterDto bounceFilter, boolean isNew) throws Exception {
		return saveBounceFilter(admin.getCompanyID(), AgnUtils.getTimeZone(admin), bounceFilter, isNew);
	}

    @Override
    public int saveBounceFilter(int companyId, TimeZone adminTimeZone, BounceFilterDto bounceFilter, boolean isNew) throws Exception {
        bounceFilter.setChangeDate(DateUtilities.midnight(adminTimeZone));
        if (StringUtils.isEmpty(bounceFilter.getSecurityToken())) {
            bounceFilter.setSecurityToken(SecurityTokenGenerator.generateSecurityToken());
        }
        Mailloop mailloop = conversionService.convert(bounceFilter, Mailloop.class);
        mailloop.setCompanyID(companyId);

        if (StringUtils.isNotEmpty(mailloop.getFilterEmail())) {
        	if (blacklistService.blacklistCheck(mailloop.getFilterEmail(), companyId)) {
        		throw new BlacklistedFilterEmailException();
        	}
        	if (isInvalidFilterEmail(companyId, isNew, mailloop)) {
        		throw new EmailInUseException();
        	}
        }

        if (StringUtils.isNotEmpty(mailloop.getForwardEmail()) && blacklistService.blacklistCheck(mailloop.getForwardEmail(), companyId)) {
            throw new BlacklistedForwardEmailException();
        }

        return mailloopDao.saveMailloop(mailloop);
    }

    private boolean isInvalidFilterEmail(int companyId, boolean isNew, Mailloop mailloop) {
        if (isNew) {
            return mailloopDao.isAddressInUse(mailloop.getFilterEmail());
        }
        return isFilterEmailChanged(mailloop, companyId) && mailloopDao.isAddressInUse(mailloop.getFilterEmail());
    }

    private boolean isFilterEmailChanged(Mailloop updatedMailloop, int companyId) {
        Mailloop oldMailloop = mailloopDao.getMailloop(updatedMailloop.getId(), companyId);
        return !StringUtils.equalsIgnoreCase(
                StringUtils.trim(oldMailloop.getFilterEmail()),
                StringUtils.trim(updatedMailloop.getFilterEmail()));
    }

    @Override
    public PaginatedListImpl<BounceFilterDto> getPaginatedBounceFilterList(Admin admin, String sort, String direction, int pageNumber, int pageSize) {
        PaginatedListImpl<MailloopEntry> paginatedListFromDb = mailloopDao.getPaginatedMailloopList(admin.getCompanyID(), sort, direction, pageNumber, pageSize);

        List<BounceFilterDto> convertedList = conversionService.convert(paginatedListFromDb.getList(), MailloopEntry.class, BounceFilterDto.class);

        String companyDomain = admin.getCompany().getMailloopDomain();
        convertedList.forEach(item -> item.setCompanyDomain(companyDomain));

        return transformPaginatedList(paginatedListFromDb, convertedList);
    }

    @Override
    public PaginatedListImpl<BounceFilterDto> overview(BounceFilterListForm filter) {
        return mailloopDao.getPaginatedMailloopList(filter);
    }

    private PaginatedListImpl<BounceFilterDto> transformPaginatedList(PaginatedListImpl<MailloopEntry> paginatedListFromDb, List<BounceFilterDto> convertedList) {
        return new PaginatedListImpl<>(
                convertedList,
                paginatedListFromDb.getFullListSize(),
                paginatedListFromDb.getPageSize(),
                paginatedListFromDb.getPageNumber(),
                paginatedListFromDb.getSortCriterion(),
                paginatedListFromDb.getSortDirection().getName());
    }

    @Override
    public BounceFilterDto getBounceFilter(int companyId, int filterId) {
        Mailloop mailloop = mailloopDao.getMailloop(filterId, companyId);
        return conversionService.convert(mailloop, BounceFilterDto.class);
    }

    @Override
    public boolean deleteBounceFilter(int filterId, int companyId) {
        ServiceResult<List<BounceFilterDto>> result = getAllowedForDeletion(Set.of(filterId), companyId);
        if (result.isSuccess()) {
            deleteMailloop(filterId, companyId);
            return true;
        }

        return false;
    }

    @Override
    public ServiceResult<UserAction> delete(Set<Integer> ids, int companyId) {
        List<Integer> allowedIds = getAllowedForDeletion(ids, companyId)
                .getResult()
                .stream()
                .map(BounceFilterDto::getId)
                .toList();

        allowedIds.forEach(id -> deleteMailloop(id, companyId));

        return ServiceResult.success(
                new UserAction(
                        "delete response processings",
                        "deleted response processings with following ids: " + StringUtils.join(allowedIds, ", ")
                ),
                Message.of(Const.Mvc.SELECTION_DELETED_MSG)
        );
    }

    protected void deleteMailloop(int id, int companyId) {
        mailloopDao.deleteMailloop(id, companyId);
    }

	@Override
	public boolean isMailingUsedInBounceFilterWithActiveAutoResponder(int companyId, int mailingId) {
		if (companyId > 0 && mailingId > 0) {
			return mailloopDao.isMailingUsedInBounceFilterWithActiveAutoResponder(companyId, mailingId);
		}

		return false;
	}

    @Override
    public List<BounceFilterDto> getDependentBounceFiltersWithActiveAutoResponder(int companyId, int mailingId) {
        if (companyId <= 0 || mailingId <= 0) {
            return Collections.emptyList();
        }

        List<MailloopEntry> filters = mailloopDao.getDependentBounceFiltersWithActiveAutoResponder(companyId, mailingId);
        return conversionService.convert(filters, MailloopEntry.class, BounceFilterDto.class);
    }

    @Override
    public String getBounceFilterNames(List<BounceFilterDto> filters) {
        return filters.stream()
                .map(BounceFilterDto::getShortName)
                .collect(Collectors.joining(", "));
    }

    @Override
    public ServiceResult<List<BounceFilterDto>> getAllowedForDeletion(Set<Integer> ids, int companyId) {
        return bulkActionValidationService.checkAllowedForDeletion(
                ids,
                id -> getBounceFilterForDeletion(id, companyId, ids.size() > 1)
        );
    }

    protected ServiceResult<BounceFilterDto> getBounceFilterForDeletion(int id, int companyId, boolean isBulkDeletion) {
        if (isBulkDeletion && containsReply(id)) {
            return ServiceResult.errorKeys("error.mailloop.delete.inbox");
        }

        BounceFilterDto bounceFilter = getBounceFilter(companyId, id);
        if (bounceFilter == null) {
            return ServiceResult.errorKeys("error.general.missing");
        }

        return ServiceResult.success(bounceFilter);
    }

    @Override
    public boolean containsReply(int filterId) {
        return false;
    }
}
