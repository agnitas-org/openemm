/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.dao;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;
import com.agnitas.emm.core.webhooks.messages.common.WebhookBackendData;
import com.agnitas.dao.impl.BaseDaoImpl;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public final class WebhookBackendDataDaoImpl extends BaseDaoImpl implements WebhookBackendDataDao {
	
	@Override
	public final List<WebhookBackendData> listUnprocessedData(final int companyID, final WebhookEventType eventType, final ZonedDateTime fromInclusive, final ZonedDateTime toExclusive) {
		final String sql = "SELECT * FROM webhook_backend_data_tbl WHERE company_id=? AND event_type=? AND creation_date >= ? AND creation_date < ?";
		
		final Date fromTime = Date.from(fromInclusive.toInstant());
		final Date toTime = Date.from(toExclusive.toInstant());

		return select(sql, WebhookBackendDataRowMapper.INSTANCE, companyID, eventType.getEventCode(), fromTime, toTime);
	}

	@Override
	public final void deleteData(final long dataId) {
		final String sql = "DELETE FROM webhook_backend_data_tbl WHERE id=?";
		
		update(sql, dataId);
	}
	
	@Override
	public final boolean deleteDataByCompanyId(final int companyId) {
		int touchedLines = update("DELETE FROM webhook_backend_data_tbl WHERE company_id=?", companyId);
    	if (touchedLines > 0) {
    		return true;
    	} else {
    		int remaining = selectInt("SELECT COUNT(*) FROM webhook_backend_data_tbl WHERE company_id = ?", companyId);
    		return remaining == 0;
    	}
	}

}
