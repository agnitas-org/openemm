/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.importvalues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.beans.CustomerImportStatus;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.util.DbColumnType;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.action.service.EmmActionService;

public class ImportModeUnsubscribeHandler implements ImportModeHandler {
    @SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ImportModeUnsubscribeHandler.class);
    
    private ImportRecipientsDao importRecipientsDao;
    
	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}

	@Override
	public void checkPreconditions(ImportProfile importProfile) throws Exception {
		// Do nothing
	}

	@Override
	public boolean isNullValueAllowedForData(DbColumnType columnType, NullValuesAction nullValuesAction) {
		return true;
	}
	
	@Override
	public void handlePreProcessing(EmmActionService emmActionService, CustomerImportStatus status, ImportProfile importProfile, String temporaryImportTableName, int datasourceId, List<Integer> mailingListIdsToAssign) throws Exception {
		// Do nothing
	}

	@Override
	public void handleNewCustomers(CustomerImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String duplicateIndexColumn, List<String> transferDbColumns, int datasourceId) throws Exception {
		// Do nothing
	}

	@Override
	public void handleExistingCustomers(CustomerImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String importIndexColumn, List<String> transferDbColumns, int datasourceId) throws Exception {
		// Do nothing
	}

	@Override
	public Map<Integer, Integer> handlePostProcessing(EmmActionService emmActionService, CustomerImportStatus status, ImportProfile importProfile, String temporaryImportTableName, int datasourceId, List<Integer> mailingListIdsToAssign) throws Exception {
		// Mark customers as opt-out in binding table if current status is active
		if (mailingListIdsToAssign != null) {
			Map<Integer, Integer> mailinglistAssignStatistics = new HashMap<>();
	    	for (int mailingListId : mailingListIdsToAssign) {
	    		int changed = importRecipientsDao.changeStatusInMailingList(temporaryImportTableName, importProfile.getKeyColumns(), importProfile.getCompanyId(), mailingListId, UserStatus.Active.getStatusCode(), UserStatus.AdminOut.getStatusCode(), "Mass Opt-Out by Admin");
	    		mailinglistAssignStatistics.put(mailingListId, changed);
	    	}
			return mailinglistAssignStatistics;
		} else {
			return null;
		}
	}
}
