/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.service.impl;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.ProfileField;
import com.agnitas.beans.RecipientHistory;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.core.recipient.ProfileFieldHistoryFeatureNotEnabledException;
import com.agnitas.emm.core.recipient.RecipientProfileHistoryException;
import com.agnitas.emm.core.recipient.dao.RecipientProfileHistoryDao;
import com.agnitas.emm.core.recipient.dao.impl.RecipientProfileHistoryDaoImpl;
import com.agnitas.emm.core.recipient.service.RecipientProfileHistoryService;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientStandardField;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of {@link RecipientProfileHistoryService} interface.
 */
public class RecipientProfileHistoryServiceImpl implements RecipientProfileHistoryService {

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(RecipientProfileHistoryDaoImpl.class);

	/** Service accessing profile history data. */
	protected RecipientProfileHistoryDao profileHistoryDao;
	
	/** Service accessing configuration data. */
	protected ConfigService configService;
	
	/** DAO accessing profile fields data. */
	protected ProfileFieldDao profileFieldDao;
	
	private RecipientFieldService recipientFieldService;

	@Override
	public void afterProfileFieldStructureModification(int companyId) throws RecipientProfileHistoryException {
		try {
			profileFieldDao.clearProfileStructureCache(companyId);
			
			recipientFieldService.clearCachedData(companyId);
			
			List<ProfileField> profileFields = listProfileFieldsForHistory(companyId);
	
			profileHistoryDao.afterProfileFieldStructureModification(companyId, profileFields);
		} catch(Exception e) {
			logger.error(String.format("Error during post-processing for profile field history for company %d", companyId), e);
			
			throw new RecipientProfileHistoryException(String.format("Error during post-processing for profile field history for company %d", companyId), e);
		}
	}
	
	/**
	 * Determine list of profile fields to be included in history.
	 * 
	 * @param companyId company ID
	 * 
	 * @return  list of profile fields to be included in history
	 */
	protected List<ProfileField> listProfileFieldsForHistory(int companyId) {
		List<ProfileField> allFields = profileFieldDao.getComProfileFields(companyId);

		return Optional.ofNullable(allFields).orElse(Collections.emptyList()).stream()
		.filter(field -> field.getHistorize() || RecipientStandardField.getHistorizedRecipientStandardFieldColumnNames().contains(field.getColumn()))
		.peek(field -> {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Included profile field column '%s' in history", field.getColumn()));
			}
		}).collect(Collectors.toList());
	}

	@Override
	public List<Integer> getChangedRecipients(Set<String> fields, ZonedDateTime from, int companyId) {
		return profileHistoryDao.getChangedRecipients(fields, from, companyId);
	}

	@Override
	public boolean isProfileFieldHistoryEnabled(int companyId) {
		return configService.isRecipientProfileHistoryEnabled(companyId);
	}
	
	@Override
	public List<RecipientHistory> listProfileFieldHistory(int recipientID, int companyId) throws RecipientProfileHistoryException {
		if (!isProfileFieldHistoryEnabled(companyId)) {
			logger.error(String.format("Profile field history not enabled for company %d", companyId));
			
			throw new ProfileFieldHistoryFeatureNotEnabledException(companyId);
		}
		
		return profileHistoryDao.listProfileFieldHistory(recipientID, companyId);
	}
	
	
	/**
	 * Set DAO for writing profile field history data.
	 *
	 * @param profileHistoryDao DAO for writing profile field history data
	 */
	public void setRecipientProfileHistoryDao(final RecipientProfileHistoryDao profileHistoryDao) {
		this.profileHistoryDao = profileHistoryDao;
	}

	/**
	 * Set service accessing configuration data.
	 *
	 * @param configService service accessing configuration data
	 */
	public void setConfigService(final ConfigService configService) {
		this.configService = configService;
	}

	/**
	 * Set DAO accessing profile field data.
	 *
	 * @param profileFieldDao DAO accessing profile field data
	 */
	public void setProfileFieldDao(final ProfileFieldDao profileFieldDao) {
		this.profileFieldDao = profileFieldDao;
	}

	public void setRecipientFieldService(RecipientFieldService recipientFieldService) {
		this.recipientFieldService = recipientFieldService;
	}
}
