/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.dao;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.ZonedDateTimeRowMapper;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public final class WebhookBackendDataProcessTimestampDaoImpl extends BaseDaoImpl implements WebhookBackendDataProcessTimestampDao {

	@Override
	public final Optional<ZonedDateTime> findTimestampOfLastRun(final int companyID, final WebhookEventType eventType) {
		final String sql = "SELECT timestamp FROM webhook_process_timestamp_tbl WHERE company_ref=? AND process_type=?";
		
		final List<ZonedDateTime> list = select(sql, ZonedDateTimeRowMapper.INSTANCE, companyID, eventType.getEventCode());
		
		return list.isEmpty()
				? Optional.empty()
				: Optional.of(list.get(0));
	}

	@Override
	public void updateTimestampOfLastRun(final int companyID, final WebhookEventType eventType, final ZonedDateTime timestamp) {
		final Date date = Date.from(timestamp.toInstant());
		
		if(!tryUpdateTimestamp(companyID, eventType, date)) {
			tryInsertTimestamp(companyID, eventType, date);
		}
	}
	
	private final boolean tryUpdateTimestamp(final int companyID, final WebhookEventType eventType, final Date timestamp) {
		final String sql = "UPDATE webhook_process_timestamp_tbl SET timestamp=? WHERE company_ref=? AND process_type=?";
		
		return update(sql, timestamp, companyID, eventType.getEventCode()) > 0;
	}

	private final void tryInsertTimestamp(final int companyID, final WebhookEventType eventType, final Date timestamp) {
		final String sql = "INSERT INTO webhook_process_timestamp_tbl (company_ref, process_type, timestamp) VALUES (?,?,?)";
		
		update(sql, companyID, eventType.getEventCode(), timestamp);
	}

	@Override
	public boolean deleteDataByCompany(final int companyID) {
		int touchedLines = update("DELETE FROM webhook_process_timestamp_tbl WHERE company_ref = ?", companyID);
    	if (touchedLines > 0) {
    		return true;
    	} else {
    		int remaining = selectInt("SELECT COUNT(*) FROM webhook_process_timestamp_tbl WHERE company_ref = ?", companyID);
    		return remaining == 0;
    	}
	}
}
