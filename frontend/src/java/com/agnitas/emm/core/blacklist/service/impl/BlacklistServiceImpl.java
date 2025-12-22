/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.blacklist.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.BlackListEntry;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.binding.service.BindingService;
import com.agnitas.emm.core.blacklist.dao.BlacklistDao;
import com.agnitas.emm.core.blacklist.exception.BlacklistAlreadyExistException;
import com.agnitas.emm.core.blacklist.service.BlacklistModel;
import com.agnitas.emm.core.blacklist.service.BlacklistService;
import com.agnitas.emm.core.blacklist.service.validation.BlacklistModelValidator;
import com.agnitas.emm.core.globalblacklist.beans.BlacklistDto;
import com.agnitas.emm.core.globalblacklist.forms.BlacklistOverviewFilter;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlacklistServiceImpl implements BlacklistService {

	private static final Logger logger = LogManager.getLogger( BlacklistServiceImpl.class);
	
	private BlacklistDao blacklistDao;
	private BlacklistModelValidator blacklistModelValidator;
	
	/** Binding service to update binding status when blacklisting mail address. */
	private BindingService bindingService;

	private ExtendedConversionService conversionService;
	
	@Override
	public boolean insertBlacklist(BlacklistModel model) {
		if (checkBlacklist(model)) {
			throw new BlacklistAlreadyExistException();
		}
		
		boolean result = blacklistDao.insert(model.getCompanyId(), model.getEmail(), model.getReason());
		
		if (result) {
			bindingService.updateBindingStatusByEmailPattern( model.getCompanyId(), model.getEmail(), UserStatus.Blacklisted, "Added to blocklist");
		}

		return result;
	}

	@Override
	public boolean deleteBlacklist(BlacklistModel model) {
		blacklistModelValidator.assertIsValidToCheck(model);
		return blacklistDao.delete(model.getCompanyId(), model.getEmail());
	}

	@Override
	public boolean checkBlacklist(BlacklistModel model) {
		blacklistModelValidator.assertIsValidToCheck(model);
		List<String> list = blacklistDao.getBlacklist(model.getCompanyId());
		for (String regex : list) {
			// Do pattern transformation. It is important to check, that a replacement is not affected by a previous replacement!
			final String regexPattern = regex
					.replace(".", "\\.")	// First escape any "." in local part or domain part
					.replace("*", ".*").replace("?", ".")	// Then replace the convenience wildcards "*" and "?"
					.replace("%", ".*").replace("_", ".")	// At least, replace the SQL wildcards
					.toLowerCase();

			try {
				if (model.getEmail().toLowerCase().matches(regexPattern)) {
					return true;
				}
			} catch(final Exception e) {
				final String msg = String.format("Unable to check against blacklist pattern '%s' ('%s')", regex, regexPattern);
				
				logger.error(msg, e);
			}
		}
		return false;
	}

	@Override
	public List<String> getEmailList(int companyID) {
		final List<String> result = blacklistDao.getBlacklist(companyID);
		
		if(result == null) {
			final String msg = String.format("Error reading blacklist entries for company %d", companyID);
			
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		
		return result;
	}

    protected List<BlackListEntry> getRecipientList(int companyID) {
        return blacklistDao.getBlacklistedRecipients(companyID);
    }

	@Override
	public boolean isAlreadyExist(int companyId, String email) {
		return blacklistDao.exist(companyId, email);
	}

	@Override
	public boolean add(int companyId, int adminId, String email, String reason) {
		boolean isSuccessfullyInserted = blacklistDao.insert(companyId, email, reason);
		if (isSuccessfullyInserted) {
			String remark = "Added to blocklist by " + adminId;
			bindingService.updateBindingStatusByEmailPattern(companyId, email, UserStatus.Blacklisted, remark);
		}

		return isSuccessfullyInserted;
	}

	@Override
	public boolean update(int companyId, String email, String reason) {
		return blacklistDao.update(companyId, email, reason);
	}

	@Override
	public PaginatedList<BlacklistDto> getAll(BlacklistOverviewFilter filter, int companyId) {
		PaginatedList<BlackListEntry> blacklistedRecipients = blacklistDao.getBlacklistedRecipients(filter, companyId);
		return conversionService.convertPaginatedList(blacklistedRecipients, BlackListEntry.class, BlacklistDto.class);
	}

	@Override
	public List<BlacklistDto> getAll(int companyId) {
		List<BlackListEntry> recipientList = getRecipientList(companyId);
		return conversionService.convert(recipientList, BlackListEntry.class, BlacklistDto.class);
	}

	@Override
	public List<Mailinglist> getBindedMailingLists(Set<String> emails, int companyId) {
		return this.blacklistDao.getMailinglistsWithBlacklistedBindings(emails, companyId);
	}

	@Override
	public boolean delete(Set<String> emails, Set<Integer> mailinglistIds, int companyId) {
		if (!delete(emails, companyId)) {
			return false;
		}
		emails.forEach(e -> blacklistDao.updateBlacklistedBindings(companyId, e, mailinglistIds, UserStatus.AdminOut));
		return true;
	}

	private boolean delete(Set<String> emails, int companyId) {
		emails = emails.stream()
				.filter(StringUtils::isNotBlank)
				.map(AgnUtils::normalizeEmail)
				.collect(Collectors.toSet());

		if (emails.isEmpty()) {
			return false;
		}

		return blacklistDao.delete(emails, companyId);
	}

	@Override
	public boolean blacklistCheck(String email, int companyId) {
		return blacklistDao.blacklistCheck(StringUtils.trimToEmpty(email), companyId);
	}
	
	@Override
	public boolean blacklistCheckCompanyOnly(String email, int companyId) {
		return blacklistDao.blacklistCheckCompanyOnly(email, companyId);
	}
	
	@Override
	public Set<String> loadBlackList(int companyId) {
		return blacklistDao.loadBlackList(companyId);
	}
	
	// ------------------------------------------------------------------------------------------ Dependency Injection

	public void setBlacklistDao(BlacklistDao blacklistDao) {
		this.blacklistDao = blacklistDao;
	}

    public void setBindingService(BindingService bindingService) {
    	this.bindingService = bindingService;
    }

	public void setConversionService(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public void setBlacklistModelValidator(final BlacklistModelValidator blacklistModelValidator) {
		this.blacklistModelValidator = blacklistModelValidator;
	}
}
