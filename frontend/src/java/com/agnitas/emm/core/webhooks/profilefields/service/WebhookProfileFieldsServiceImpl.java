/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.profilefields.service;

import java.util.Objects;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.profilefields.dao.WebhookProfileFieldDao;

public final class WebhookProfileFieldsServiceImpl implements WebhookProfileFieldsService {

	private WebhookProfileFieldDao profileFieldDao;
	
	@Override
	public final Set<String> listProfileFieldNamesForWebhook(final int companyID, final WebhookEventType eventType) {
		return this.profileFieldDao.listProfileFieldsForWebhook(companyID, eventType);
	}
	
	@Override
	public final boolean deleteProfileFieldSettingsForWebhooks(final int companyID) {
		return profileFieldDao.deleteProfileFieldsForWebhooks(companyID);
	}

	@Transactional
	@Override
	public final void updateIncludedProfileField(final int companyID, final WebhookEventType eventType, final Set<String> profileFields) {
		this.profileFieldDao.deleteProfileFieldsForWebhook(companyID, eventType);
		
		if(profileFields != null) {
			this.profileFieldDao.insertProfileFieldsForWebhook(companyID, eventType, profileFields);
		}
	}

	public final void setWebhookProfileFieldDao(final WebhookProfileFieldDao dao) {
		this.profileFieldDao = Objects.requireNonNull(dao, "WebhookProfileFieldDao is null");
	}
}
