/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ImportProfile;
import com.agnitas.beans.ImportStatus;
import com.agnitas.beans.Mailinglist;
import com.agnitas.dao.ImportRecipientsDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.auto_import.bean.RemoteFile;
import com.agnitas.emm.core.importquota.service.ImportQuotaCheckService;
import com.agnitas.emm.core.imports.reporter.ProfileImportReporter;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;

public class ProfileImportWorkerFactory {
	private ConfigService configService;
	private MailinglistDao mailinglistDao;
	private ProfileImportReporter profileImportReporter;
	private ImportModeHandlerFactory importModeHandlerFactory;
	private ImportRecipientsDao importRecipientsDao;
	private RecipientFieldService recipientFieldService;
	private ImportQuotaCheckService importQuotaCheckService;
	
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}

	public void setProfileImportReporter(ProfileImportReporter profileImportReporter) {
		this.profileImportReporter = profileImportReporter;
	}

	public void setImportModeHandlerFactory(ImportModeHandlerFactory importModeHandlerFactory) {
		this.importModeHandlerFactory = importModeHandlerFactory;
	}

	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}

	public void setRecipientFieldService(RecipientFieldService recipientFieldService) {
		this.recipientFieldService = recipientFieldService;
	}
	
	public final void setImportQuotaCheckService(final ImportQuotaCheckService service) {
		this.importQuotaCheckService = Objects.requireNonNull(service, "import quota check service");
	}
	
	public ProfileImportWorker getProfileImportWorker(boolean interactiveMode, List<Integer> mailingListIdsToAssign, String sessionId, int companyID, Admin admin, int datasourceId, ImportProfile importProfile, RemoteFile importFile, ImportStatus importStatus) {
		ProfileImportWorker profileImportWorker = new ProfileImportWorker();

		profileImportWorker.setConfigService(configService);
		profileImportWorker.setProfileImportReporter(profileImportReporter);
		profileImportWorker.setImportModeHandlerFactory(importModeHandlerFactory);
		profileImportWorker.setImportRecipientsDao(importRecipientsDao);
		profileImportWorker.setRecipientFieldService(recipientFieldService);

		profileImportWorker.setSessionId(sessionId);
		profileImportWorker.setInteractiveMode(interactiveMode);
		profileImportWorker.setAdmin(admin);
		profileImportWorker.setDatasourceId(datasourceId);
		profileImportWorker.setImportProfile(importProfile);
		profileImportWorker.setImportFile(importFile);
		profileImportWorker.setCustomerImportStatus(importStatus);
		profileImportWorker.setImportQuotaCheckService(importQuotaCheckService);
		profileImportWorker.setCheckHtmlTags(!configService.getBooleanValue(ConfigValue.NoHtmlCheckOnProfileImport, companyID));
		profileImportWorker.setAllowSafeHtmlTags(configService.getBooleanValue(ConfigValue.AllowHtmlTagsInReferenceAndProfileFields, companyID));
		profileImportWorker.setThrottleImportPerBlock(configService.getIntegerValue(ConfigValue.ImportThrottlingSecondsPerBlock, companyID));
		
		if (importProfile.isMailinglistsAll()) {
			profileImportWorker.setMailingListIdsToAssign(mailinglistDao.getMailinglists(companyID)
					.stream()
					.map(Mailinglist::getId)
					.collect(Collectors.toList()));
		} else {
			profileImportWorker.setMailingListIdsToAssign(mailingListIdsToAssign);
		}
		
		if (admin == null || admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)) {
			profileImportWorker.setMaxGenderValue(ConfigService.MAX_GENDER_VALUE_EXTENDED);
		} else {
			profileImportWorker.setMaxGenderValue(ConfigService.MAX_GENDER_VALUE_BASIC);
		}
		
		return profileImportWorker;
	}
}
