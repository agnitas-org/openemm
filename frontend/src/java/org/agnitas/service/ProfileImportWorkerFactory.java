/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.List;
import java.util.stream.Collectors;

import org.agnitas.beans.ImportStatus;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.web.ProfileImportReporter;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.service.ColumnInfoService;

public class ProfileImportWorkerFactory {
	private ConfigService configService;
	private MailinglistDao mailinglistDao;
	private ProfileImportReporter profileImportReporter;
	private ImportModeHandlerFactory importModeHandlerFactory;
	private ImportRecipientsDao importRecipientsDao;
	private ColumnInfoService columnInfoService;
	
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Required
	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}

	@Required
	public void setProfileImportReporter(ProfileImportReporter profileImportReporter) {
		this.profileImportReporter = profileImportReporter;
	}

	@Required
	public void setImportModeHandlerFactory(ImportModeHandlerFactory importModeHandlerFactory) {
		this.importModeHandlerFactory = importModeHandlerFactory;
	}

	@Required
	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}

	@Required
	public void setColumnInfoService(ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}
	
	public ProfileImportWorker getProfileImportWorker(boolean interactiveMode, List<Integer> mailingListIdsToAssign, String sessionId, Admin admin, int datasourceId, ImportProfile importProfile, RemoteFile importFile, ImportStatus importStatus) throws Exception {
		ProfileImportWorker profileImportWorker = new ProfileImportWorker();

		profileImportWorker.setConfigService(configService);
		profileImportWorker.setProfileImportReporter(profileImportReporter);
		profileImportWorker.setImportModeHandlerFactory(importModeHandlerFactory);
		profileImportWorker.setImportRecipientsDao(importRecipientsDao);
		profileImportWorker.setColumnInfoService(columnInfoService);

		profileImportWorker.setSessionId(sessionId);
		profileImportWorker.setInteractiveMode(interactiveMode);
		profileImportWorker.setAdmin(admin);
		profileImportWorker.setDatasourceId(datasourceId);
		profileImportWorker.setImportProfile(importProfile);
		profileImportWorker.setImportFile(importFile);
		profileImportWorker.setCustomerImportStatus(importStatus);
		
		if (importProfile.isMailinglistsAll()) {
			profileImportWorker.setMailingListIdsToAssign(mailinglistDao.getMailinglists(importProfile.getCompanyId()).stream().map(mailinglist -> mailinglist.getId()).collect(Collectors.toList()));
		} else {
			profileImportWorker.setMailingListIdsToAssign(mailingListIdsToAssign);
		}
		
		if (admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)) {
			profileImportWorker.setMaxGenderValue(ConfigService.MAX_GENDER_VALUE_EXTENDED);
		} else {
			profileImportWorker.setMaxGenderValue(ConfigService.MAX_GENDER_VALUE_BASIC);
		}
		
		return profileImportWorker;
	}
}
