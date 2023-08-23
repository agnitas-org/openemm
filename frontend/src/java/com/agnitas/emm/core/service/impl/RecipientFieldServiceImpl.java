/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.core.dao.RecipientFieldDao;
import com.agnitas.emm.core.recipient.service.RecipientProfileHistoryService;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientFieldsCache;

public class RecipientFieldServiceImpl implements RecipientFieldService {
	private RecipientFieldsCache recipientFieldsCache;
	private RecipientFieldDao recipientFieldDao;
	private ProfileFieldDao profileFieldDao;
	private RecipientProfileHistoryService recipientProfileHistoryService;
	
	public RecipientFieldServiceImpl(RecipientFieldsCache recipientFieldsCache, RecipientFieldDao recipientFieldDao, ProfileFieldDao profileFieldDao, RecipientProfileHistoryService recipientProfileHistoryService) {
		this.recipientFieldsCache = Objects.requireNonNull(recipientFieldsCache, "RecipientFieldsCache may not be null");
		this.recipientFieldDao = Objects.requireNonNull(recipientFieldDao, "RecipientFieldDao may not be null");
		this.profileFieldDao = Objects.requireNonNull(profileFieldDao, "ProfileFieldDao may not be null");
		this.recipientProfileHistoryService = Objects.requireNonNull(recipientProfileHistoryService, "RecipientProfileHistoryService may not be null");
	}

	@Override
	public List<RecipientFieldDescription> getRecipientFields(int companyID) throws Exception {
		return getCachedRecipientFieldsData(companyID);
	}

	@Override
	public RecipientFieldDescription getRecipientField(int companyID, String recipientFieldName) throws Exception {
		List<RecipientFieldDescription> recipientFields = getCachedRecipientFieldsData(companyID);
		for (RecipientFieldDescription recipientFieldDescription : recipientFields) {
			if (recipientFieldName.equalsIgnoreCase(recipientFieldDescription.getColumnName())
				|| recipientFieldName.equalsIgnoreCase(recipientFieldDescription.getShortName())) {
				return recipientFieldDescription;
			}
		}
		return null;
	}

	@Override
	public void saveRecipientField(int companyID, RecipientFieldDescription recipientFieldDescription) throws Exception {
		if (RecipientStandardField.Bounceload.getColumnName().equalsIgnoreCase(recipientFieldDescription.getColumnName())) {
			throw new Exception("Recipient field bounceload is unchangeable");
		} else {
			recipientFieldDao.saveRecipientField(companyID, recipientFieldDescription);

			// Clear cached data
			recipientFieldsCache.put(companyID, null);
			profileFieldDao.clearProfileStructureCache(companyID);
			
			recipientProfileHistoryService.afterProfileFieldStructureModification(companyID);
		}
	}

	@Override
	public void deleteRecipientField(int companyID, String recipientFieldName) throws Exception {
		if (RecipientStandardField.getAllRecipientStandardFieldColumnNames().contains(recipientFieldName)) {
			throw new Exception("Cannot delete recipient standard field: " + recipientFieldName);
		} else {
			recipientFieldDao.deleteRecipientField(companyID, recipientFieldName);

			// Clear cached data
			recipientFieldsCache.put(companyID, null);
			profileFieldDao.clearProfileStructureCache(companyID);
			
			recipientProfileHistoryService.afterProfileFieldStructureModification(companyID);
		}
	}

	private List<RecipientFieldDescription> getCachedRecipientFieldsData(int companyID) throws Exception {
		List<RecipientFieldDescription> recipientFields = recipientFieldsCache.get(companyID);
		if (recipientFields == null) {
			recipientFields = recipientFieldDao.getRecipientFields(companyID).stream()
				.filter(recipientField -> !recipientField.getColumnName().equalsIgnoreCase(RecipientStandardField.Bounceload.getColumnName()))
				.collect(Collectors.toList());
			
			recipientFieldsCache.put(companyID, recipientFields);
		}
		return recipientFields;
	}

	@Override
	public boolean isReservedKeyWord(String fieldname) {
		return recipientFieldDao.isReservedKeyWord(fieldname);
	}
}
