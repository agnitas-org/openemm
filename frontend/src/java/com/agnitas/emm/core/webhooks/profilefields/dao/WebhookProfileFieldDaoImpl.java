/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.profilefields.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.emm.core.webhooks.common.WebhookEventType;

public class WebhookProfileFieldDaoImpl extends BaseDaoImpl implements WebhookProfileFieldDao {

	@Override
	public Set<String> listProfileFieldsForWebhook(int companyID, WebhookEventType eventType) {
		final String sql = "SELECT profile_field FROM webhook_profile_field_tbl WHERE company_ref=? AND event_type=?";
        return new HashSet<>(select(sql, StringRowMapper.INSTANCE, companyID, eventType.getEventCode()));
	}
	
	@Override
	public boolean deleteProfileFieldsForWebhooks(int companyID) {
		final int touchedLines = update("DELETE FROM webhook_profile_field_tbl WHERE company_ref=?", companyID);
		
    	if (touchedLines > 0) {
    		return true;
    	}

		int remaining = selectInt("SELECT COUNT(*) FROM webhook_profile_field_tbl WHERE company_ref = ?", companyID);
		return remaining == 0;
	}
	
	@Override
	public boolean deleteProfileFieldsForWebhook(int companyID, WebhookEventType eventType) {
		final int touchedLines = update("DELETE FROM webhook_profile_field_tbl WHERE company_ref=? AND event_type=?", companyID, eventType.getEventCode());
		
    	if (touchedLines > 0) {
    		return true;
    	}

		int remaining = selectInt("SELECT COUNT(*) FROM webhook_profile_field_tbl WHERE company_ref=? AND event_type=?", companyID, eventType.getEventCode());
		return remaining == 0;
	}
	
	@Override
	public void insertProfileFieldsForWebhook(int companyID, WebhookEventType eventType, Set<String> profileFields) {
		final String sql = "INSERT INTO webhook_profile_field_tbl (company_ref, event_type, profile_field) VALUES (?,?,?)";
		
		final List<Object[]> queryParameters = profileFields
				.stream()
				.map(field -> new Object[] {companyID, eventType.getEventCode(), field} )
				.toList();
		
		this.batchupdate(sql, queryParameters);
	}
	
}
