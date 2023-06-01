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
import java.util.Set;

import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.ImportStatus;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.util.DbColumnType;

import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class ImportModeReactivateSuspendedHandler implements ImportModeHandler {
    
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
	public void handlePreProcessing(EmmActionService emmActionService, ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, int datasourceId, List<Integer> mailingListIdsToAssign) throws Exception {
		// Do not remove!!!
	}

	@Override
	public void handleNewCustomers(ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String duplicateIndexColumn, List<String> transferDbColumns, int datasourceId) throws Exception {
		// Do nothing
	}

	@Override
	public void handleExistingCustomers(ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, String importIndexColumn, List<String> transferDbColumns, int datasourceId) throws Exception {
		// Do nothing
	}

	@Override
	public Map<MediaTypes, Map<Integer, Integer>> handlePostProcessing(EmmActionService emmActionService, ImportStatus status, ImportProfile importProfile, String temporaryImportTableName, int datasourceId, List<Integer> mailingListIdsToAssign, Set<MediaTypes> mediatypes) throws Exception {
		// Mark customers suspended in binding table if current status is active
		if (mailingListIdsToAssign != null) {
			Map<MediaTypes, Map<Integer, Integer>> mailinglistAssignStatistics = new HashMap<>();
			mailinglistAssignStatistics = new HashMap<>();
	    	for (int mailingListId : mailingListIdsToAssign) {
	    		for (MediaTypes mediatype : mediatypes) {
	    			int changed = importRecipientsDao.changeStatusInMailingList(temporaryImportTableName, importProfile.getKeyColumns(), importProfile.getCompanyId(), mailingListId, mediatype, UserStatus.Suspend.getStatusCode(), UserStatus.Active.getStatusCode(), "Mass Reactivate-Suspend by Admin");
	    			if (!mailinglistAssignStatistics.containsKey(mediatype)) {
		    			mailinglistAssignStatistics.put(mediatype, new HashMap<Integer, Integer>());
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
