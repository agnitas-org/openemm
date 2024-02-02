/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.importvalues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.ImportStatus;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.service.ImportException;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class ImportModeUnsubscribeHandler implements ImportModeHandler {
    
    private ImportRecipientsDao importRecipientsDao;
    
	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}

	@Override
	public void checkPreconditions(ImportProfile importProfile) throws Exception {
		// If customer_id is imported it may only be used for updates, never for imports(ADD)
		// Permission to import customer_id is checked later
		boolean isCustomerIdImported = false;
		for (ColumnMapping mapping : importProfile.getColumnMapping()) {
			if ("customer_id".equalsIgnoreCase(mapping.getDatabaseColumn())) {
				isCustomerIdImported = true;
			}
		}
		
		if (isCustomerIdImported) {
			throw new ImportException(false, "error.import.customerid_insert");
		}
		
		// Check "not nullable" fields
		CaseInsensitiveMap<String, DbColumnType> customerDbFields = importRecipientsDao.getCustomerDbFields(importProfile.getCompanyId());
		for (Entry<String, DbColumnType> columnEntry : customerDbFields.entrySet()) {
			if (!columnEntry.getValue().isNullable()
					&& DbUtilities.getColumnDefaultValue(importRecipientsDao.getDataSource(), "customer_" + importProfile.getCompanyId() + "_tbl", columnEntry.getKey()) == null
					&& !"customer_id".equalsIgnoreCase(columnEntry.getKey())
					&& !"gender".equalsIgnoreCase(columnEntry.getKey())
					&& !"mailtype".equalsIgnoreCase(columnEntry.getKey())
					&& !ComCompanyDaoImpl.STANDARD_FIELD_BOUNCELOAD.equalsIgnoreCase(columnEntry.getKey())) {
				boolean notNullColumnIsSet = false;
				for (ColumnMapping mapping : importProfile.getColumnMapping()) {
					if (columnEntry.getKey().equalsIgnoreCase(mapping.getDatabaseColumn()) && (mapping.getFileColumn() != null || mapping.getDefaultValue() != null)) {
						notNullColumnIsSet = true;
						break;
					}
				}
				if (!notNullColumnIsSet) {
					throw new ImportException(false, "error.import.missingNotNullableColumnInMapping", columnEntry.getKey());
				}
			}
		}
		
		for (ColumnMapping mapping : importProfile.getColumnMapping()) {
			if ("gender".equalsIgnoreCase(mapping.getDatabaseColumn())) {
				if (StringUtils.isBlank(mapping.getFileColumn()) && (StringUtils.isBlank(mapping.getDefaultValue()) || mapping.getDefaultValue().trim().equals("''") || mapping.getDefaultValue().trim().equalsIgnoreCase("null"))) {
					throw new ImportException(false, "error.import.missingNotNullableColumnInMapping", "gender");
				}
			} else if ("mailtype".equalsIgnoreCase(mapping.getDatabaseColumn())) {
				if (StringUtils.isBlank(mapping.getFileColumn()) && (StringUtils.isBlank(mapping.getDefaultValue()) || mapping.getDefaultValue().trim().equals("''") || mapping.getDefaultValue().trim().equalsIgnoreCase("null"))) {
					throw new ImportException(false, "error.import.missingNotNullableColumnInMapping", "mailtype");
				}
			}
		}
	}

	@Override
	public boolean isNullValueAllowedForData(DbColumnType columnType, NullValuesAction nullValuesAction) {
		return columnType.isNullable();
	}
	
	@Override
	public void handlePreProcessing(EmmActionService emmActionService, ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, int datasourceId, List<Integer> mailingListIdsToAssign) throws Exception {
		// Do not remove!!!
	}

	@Override
	public void handleNewCustomers(ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String duplicateIndexColumn, List<String> transferDbColumns, int datasourceId) throws Exception {
		// Do nothing
	}

	@Override
	public void handleExistingCustomers(ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String importIndexColumn, List<String> transferDbColumns, int datasourceId) throws Exception {
		// Update customer data
		if (importProfile.getUpdateAllDuplicates()) {
			// Update all existing customer identified by keycolumns
			int updatedEntries = importRecipientsDao.updateAllExistingCustomersByKeyColumn(importProfile.getCompanyId(), temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl", importProfile.getKeyColumns(), transferDbColumns, importIndexColumn, importProfile.getNullValuesAction(), datasourceId, importProfile.getCompanyId());
			status.setUpdated(updatedEntries);
		} else {
			// Update the first existing customer only
			int updatedEntries = importRecipientsDao.updateFirstExistingCustomers(importProfile.getCompanyId(), temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl", importProfile.getKeyColumns(), transferDbColumns, importIndexColumn, importProfile.getNullValuesAction(), datasourceId, importProfile.getCompanyId());
			status.setUpdated(updatedEntries);
		}
	}

	@Override
	public void handleExistingCustomersImproved(ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String importIndexColumn, List<String> transferDbColumns, int datasourceId) throws Exception {
		// Update customer data
		if (importProfile.getUpdateAllDuplicates()) {
			// Update all existing customer identified by keycolumns
			int updatedEntries = importRecipientsDao.updateAllExistingCustomersByKeyColumnImproved(importProfile.getCompanyId(), temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl", importProfile.getKeyColumns(), transferDbColumns, importIndexColumn, importProfile.getNullValuesAction(), datasourceId, importProfile.getCompanyId());
			status.setUpdated(updatedEntries);
		} else {
			// Update the first existing customer only
			int updatedEntries = importRecipientsDao.updateFirstExistingCustomersImproved(importProfile.getCompanyId(), temporaryImportTableName, "customer_" + importProfile.getCompanyId() + "_tbl", importProfile.getKeyColumns(), transferDbColumns, importIndexColumn, importProfile.getNullValuesAction(), datasourceId, importProfile.getCompanyId());
			status.setUpdated(updatedEntries);
		}
	}

	@Override
	public Map<MediaTypes, Map<Integer, Integer>> handlePostProcessing(EmmActionService emmActionService, ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, int datasourceId, List<Integer> mailingListIdsToAssign, Set<MediaTypes> mediatypes) throws Exception {
		// Mark customers as opt-out in binding table if current status is active
		if (mailingListIdsToAssign != null) {
			Map<MediaTypes, Map<Integer, Integer>> mailinglistAssignStatistics = new HashMap<>();
	    	for (int mailingListId : mailingListIdsToAssign) {
	    		for (MediaTypes mediatype : mediatypes) {
	    			int changed = importRecipientsDao.changeStatusInMailingList(temporaryImportTableName, importProfile.getKeyColumns(), importProfile.getCompanyId(), mailingListId, mediatype, UserStatus.Active.getStatusCode(), UserStatus.AdminOut.getStatusCode(), "Mass Opt-Out by Admin");
	    			if (!mailinglistAssignStatistics.containsKey(mediatype)) {
		    			mailinglistAssignStatistics.put(mediatype, new HashMap<>());
		    		}
		    		mailinglistAssignStatistics.get(mediatype).put(mailingListId, changed);
	    		}
	    	}
			return mailinglistAssignStatistics;
		} else {
			return null;
		}
	}

	@Override
	public int handleBlacklist(ImportProfile importProfile, String temporaryImportTableName) {
		return importRecipientsDao.removeBlacklistedEmails(temporaryImportTableName, importProfile.getCompanyId());
	}
}
