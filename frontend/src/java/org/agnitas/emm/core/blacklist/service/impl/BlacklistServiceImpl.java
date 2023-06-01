/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.blacklist.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.BlackListEntry;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.binding.service.BindingService;
import org.agnitas.emm.core.binding.service.BindingServiceException;
import org.agnitas.emm.core.blacklist.service.BlacklistAlreadyExistException;
import org.agnitas.emm.core.blacklist.service.BlacklistModel;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.blacklist.service.validation.BlacklistModelValidator;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.blacklist.dao.ComBlacklistDao;
import com.agnitas.emm.core.globalblacklist.beans.BlacklistDto;
import com.agnitas.emm.core.globalblacklist.beans.GlobalBlacklistDto;
import com.agnitas.service.ExtendedConversionService;

public class BlacklistServiceImpl implements BlacklistService {

	/** The logger. */
	private static final Logger logger = LogManager.getLogger( BlacklistServiceImpl.class);
	
	/** DAO for blacklists. */
	private ComBlacklistDao blacklistDao;
	protected ComRecipientDao recipientDao;
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
		
		if( result) {
			try {
				bindingService.updateBindingStatusByEmailPattern( model.getCompanyId(), model.getEmail(), UserStatus.Blacklisted, "Added to blocklist");
			} catch( BindingServiceException e) {
				logger.error( "Unable to update binding status for blacklisted email: " + model.getEmail(), e);
			}
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
	public List<String> getEmailList(@VelocityCheck int companyID) throws Exception {
		final List<String> result = blacklistDao.getBlacklist(companyID);
		
		if(result == null) {
			final String msg = String.format("Error reading blacklist entries for company %d", companyID);
			
			logger.error(msg);
			throw new Exception(msg);
		}
		
		return result;
	}

    @Override
    public List<BlackListEntry> getRecipientList(@VelocityCheck int companyID) throws Exception {
        return blacklistDao.getBlacklistedRecipients(companyID);
    }

	@Override
	public List<Mailinglist> getMailinglistsWithBlacklistedBindings( BlacklistModel model) {
		return this.blacklistDao.getMailinglistsWithBlacklistedBindings( model.getCompanyId(), model.getEmail());
	}

	@Override
	public void updateBlacklistedBindings(BlacklistModel bm, List<Integer> mailinglistIds, UserStatus userStatus) {
		if( mailinglistIds.size() == 0) {
			if( logger.isInfoEnabled()) {
				logger.info( "List of mailinglist IDs is empty - doing nothing");
			}
			
			return;
		}
			
		this.blacklistDao.updateBlacklistedBindings( bm.getCompanyId(), bm.getEmail(), mailinglistIds, userStatus);
	}

	@Override
	public boolean isAlreadyExist(@VelocityCheck int companyId, String email) {
		return blacklistDao.exist(companyId, email);
	}

	@Override
	public boolean add(@VelocityCheck int companyId, int adminId, String email, String reason) throws Exception {
		boolean isSuccessfullyInserted = blacklistDao.insert(companyId, email, reason);
		if (isSuccessfullyInserted) {
			String remark = "Added to blocklist by " + adminId;
			bindingService.updateBindingStatusByEmailPattern(companyId, email, UserStatus.Blacklisted, remark);
		}

		return isSuccessfullyInserted;
	}

	@Override
	public void add(GlobalBlacklistDto globalBlacklistDto) {
		blacklistDao.insertGlobal(globalBlacklistDto.getEmail(), globalBlacklistDto.getReason());
	}

	@Override
	public boolean update(@VelocityCheck int companyId, String email, String reason) {
		return blacklistDao.update(companyId, email, reason);
	}

	@Override
	public PaginatedListImpl<BlacklistDto> getAll(@VelocityCheck int companyId, String sort, String direction, int page,
												  int rowNumber, String likePattern) {

		PaginatedListImpl<BlackListEntry> blacklistedRecipients = blacklistDao.getBlacklistedRecipients(companyId, sort,
				direction, page, rowNumber, likePattern);

		return conversionService.convertPaginatedList(blacklistedRecipients, BlackListEntry.class, BlacklistDto.class);
	}

	@Override
	public List<BlacklistDto> getAll(@VelocityCheck int companyId) throws Exception {
		List<BlackListEntry> recipientList = getRecipientList(companyId);
		return conversionService.convert(recipientList, BlackListEntry.class, BlacklistDto.class);
	}

	@Override
	public List<Mailinglist> getBindedMailingLists(@VelocityCheck int companyId, String email) {
		BlacklistModel blacklistModel = new BlacklistModel();
		blacklistModel.setCompanyId(companyId);
		blacklistModel.setEmail(email);

		return getMailinglistsWithBlacklistedBindings(blacklistModel);
	}

	@Override
	public boolean delete(@VelocityCheck int companyId, String email, Set<Integer> mailinglistIds) {
		boolean isDeleted = blacklistDao.delete(companyId, email);

		if (isDeleted) {
			BlacklistModel blacklistModel = new BlacklistModel();
			blacklistModel.setEmail(email);
			blacklistModel.setCompanyId(companyId);

            List<Integer> filteredMailinglistIds;
			if (CollectionUtils.isNotEmpty(mailinglistIds)) {
                filteredMailinglistIds = mailinglistIds.stream()
                        .filter((id) -> id > 0)
                        .collect(Collectors.toList());
            } else {
				filteredMailinglistIds = Collections.emptyList();
			}

			updateBlacklistedBindings(blacklistModel, filteredMailinglistIds, UserStatus.AdminOut);
			return true;
		}

		return false;
	}
	
	@Override
	public boolean blacklistCheck(String email, @VelocityCheck int companyId) {
		return blacklistDao.blacklistCheck(StringUtils.trimToEmpty(email), companyId);
	}
	
	@Override
	public boolean blacklistCheckCompanyOnly(String email, @VelocityCheck int companyId) {
		return blacklistDao.blacklistCheckCompanyOnly(email, companyId);
	}
	
	@Override
	public Set<String> loadBlackList(@VelocityCheck int companyId) throws Exception {
		return blacklistDao.loadBlackList(companyId);
	}
	
	// ------------------------------------------------------------------------------------------ Dependency Injection

    @Required
	public void setBlacklistDao(ComBlacklistDao blacklistDao) {
		this.blacklistDao = blacklistDao;
	}

    /**
     * Set binding service.
     * 
     * @param bindingService binding service
     */
    @Required
    public void setBindingService(BindingService bindingService) {
    	this.bindingService = bindingService;
    }

	@Required
	public void setConversionService(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}
	
	@Required
	public void setBlacklistModelValidator(final BlacklistModelValidator blacklistModelValidator) {
		this.blacklistModelValidator = blacklistModelValidator;
	}
}
