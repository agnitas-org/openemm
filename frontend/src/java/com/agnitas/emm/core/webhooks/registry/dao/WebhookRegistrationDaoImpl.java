/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.registry.dao;

import java.util.List;
import java.util.Objects;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.registry.common.WebhookAlreadyRegisteredException;
import com.agnitas.emm.core.webhooks.registry.common.WebhookNotRegisteredException;
import com.agnitas.emm.core.webhooks.registry.common.WebhookRegistryEntry;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link WebhookRegistrationDao} interface.
 */
public class WebhookRegistrationDaoImpl extends BaseDaoImpl implements WebhookRegistrationDao {

	@Transactional
	@Override
	public void registerWebhookUrl(int companyId, WebhookEventType eventType, String url) throws WebhookAlreadyRegisteredException {
		Objects.requireNonNull(eventType, "WebhookEventType is null");
		Objects.requireNonNull(url, "Webhook URL is null");
		
		try {
			// Check if entry exists
			findWebhookEntry(companyId, eventType);
			
			throw new WebhookAlreadyRegisteredException(companyId, eventType);
		} catch (WebhookNotRegisteredException e) {
			final String sql = "INSERT INTO webhook_url_tbl (company_ref, event_type, webhook_url) VALUES (?,?,?)";
			
			this.update(sql, companyId, eventType.getEventCode(), url);
		}
	}

	@Override
	public void unregisterWebhookUrl(int companyId, WebhookEventType eventType) throws WebhookNotRegisteredException {
		Objects.requireNonNull(eventType, "WebhookEventType is null");
		
		// Check that entry exists
		findWebhookEntry(companyId, eventType);
		
		final String sql = "DELETE FROM webhook_url_tbl WHERE company_ref=? AND event_type=?";
		
		update(sql, companyId, eventType.getEventCode());
	}
	
	@Override
	public boolean deleteWebhookUrlByCompany(int companyId)  {
		int touchedLines = update("DELETE FROM webhook_url_tbl WHERE company_ref=?", companyId);
    	if (touchedLines > 0) {
    		return true;
    	}

		int remaining = selectInt("SELECT COUNT(*) FROM webhook_url_tbl WHERE company_ref = ?", companyId);
		return remaining == 0;
	}

	@Override
	public List<WebhookRegistryEntry> listAllRegisteredWebhookUrls(WebhookEventType eventType) {
		final String sql = "SELECT * FROM webhook_url_tbl WHERE event_type=?";
		return select(sql, WebhookRegistrationEntryRowMapper.INSTANCE, eventType.getEventCode());
	}

	@Override
	public WebhookRegistryEntry findWebhookEntry(int companyId, WebhookEventType eventType) throws WebhookNotRegisteredException {
		Objects.requireNonNull(eventType, "WebhookEventType is null");

		final String sql = "SELECT * FROM webhook_url_tbl WHERE company_ref=? AND event_type=?";

		WebhookRegistryEntry entry = selectObjectDefaultNull(sql, WebhookRegistrationEntryRowMapper.INSTANCE, companyId, eventType.getEventCode());

		if (entry == null) {
			throw new WebhookNotRegisteredException(companyId, eventType);
		} 
		
		return entry;
	}

}
