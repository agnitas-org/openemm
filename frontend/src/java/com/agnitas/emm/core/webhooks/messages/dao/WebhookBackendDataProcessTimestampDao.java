/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.dao;

import java.time.ZonedDateTime;
import java.util.Optional;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;

public interface WebhookBackendDataProcessTimestampDao {

	public Optional<ZonedDateTime> findTimestampOfLastRun(final int companyID, final WebhookEventType eventType);
	public void updateTimestampOfLastRun(final int companyID, final WebhookEventType eventType, final ZonedDateTime timestamp);
	public boolean deleteDataByCompany(final int companyID);

}
