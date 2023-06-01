/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.agnitas.beans.Mailloop;
import org.agnitas.beans.MailloopEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailloopDao;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.mailloop.util.SecurityTokenGenerator;
import com.agnitas.service.ExtendedConversionService;

@Service("BounceFilterService")
public class BounceFilterServiceImpl implements BounceFilterService {

    private final MailloopDao mailloopDao;
    private final ExtendedConversionService conversionService;
    private final BlacklistService blacklistService;

    public BounceFilterServiceImpl(MailloopDao mailloopDao, ExtendedConversionService conversionService, BlacklistService blacklistService) {
        this.mailloopDao = mailloopDao;
        this.conversionService = conversionService;
        this.blacklistService = blacklistService;
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
        	if (mailloopDao.isAddressInUse(mailloop.getFilterEmail(), isNew)) {
        		throw new EmailInUseException();
        	}
        }
        
        if (StringUtils.isNotEmpty(mailloop.getForwardEmail()) && blacklistService.blacklistCheck(mailloop.getForwardEmail(), companyId)) {
            throw new BlacklistedForwardEmailException();
        }
        
        return mailloopDao.saveMailloop(mailloop);
    }

    @Override
    public PaginatedListImpl<BounceFilterDto> getPaginatedBounceFilterList(Admin admin, String sort, String direction, int pageNumber, int pageSize) {
        PaginatedListImpl<MailloopEntry> paginatedListFromDb = mailloopDao.getPaginatedMailloopList(admin.getCompanyID(), sort, direction, pageNumber, pageSize);
    
        List<BounceFilterDto> convertedList = conversionService.convert(paginatedListFromDb.getList(), MailloopEntry.class, BounceFilterDto.class);

        String companyDomain = admin.getCompany().getMailloopDomain();
        convertedList.forEach(item -> item.setCompanyDomain(companyDomain));

        return transformPaginatedList(paginatedListFromDb, convertedList);
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
    public BounceFilterDto getBounceFilter(@VelocityCheck int companyId, int filterId) {
        Mailloop mailloop = mailloopDao.getMailloop(filterId, companyId);
        return conversionService.convert(mailloop, BounceFilterDto.class);
    }

    @Override
    public boolean deleteBounceFilter(int filterId, @VelocityCheck int companyId) {
        return mailloopDao.deleteMailloop(filterId, companyId);
    }
    
	@Override
	public boolean isMailingUsedInBounceFilterWithActiveAutoResponder(@VelocityCheck int companyId, int mailingId) {
		if (companyId > 0 && mailingId > 0) {
			return mailloopDao.isMailingUsedInBounceFilterWithActiveAutoResponder(companyId, mailingId);
		}

		return false;
	}
    
    @Override
    public List<BounceFilterDto> getDependentBounceFiltersWithActiveAutoResponder(@VelocityCheck int companyId, int mailingId) {
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
}
