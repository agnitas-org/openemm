/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ecs.backend.service;

import com.agnitas.emm.ecs.web.HeatmapStatInfo;

/**
 * Service class that handles creation of Embedded click statistics
 */
public interface EmbeddedClickStatService {

	/**
	 * Color that will we set to stat-label if color for some percentage
	 * value is not specified in DB
	 */
	String DEFAULT_STAT_LABEL_COLOR = "FFFFFF";

	/**
	 * Method gets mailing HTML-content for the certain recipient
	 *
	 * @param mailingId   id of mailing
	 * @param recipientId id of recipient
	 * @return mailing HTML-content
	 */
	String getMailingContent(int mailingId, int recipientId);

	/**
	 * Method adds click-stat info to mailing content in a form of hidden fields.
	 * These hidden fields will accumulate information about click statistics (clicks
	 * number, clicks percentage, color for each URL) and will be used by javascript to
	 * create click stat labels
	 *
	 * @param content   mailing HTML content
	 * @param mode	  ECS mode
	 * @param companyId id of company
	 * @return mailing HTML content + hidden fields that will be used by ECS-page javascript
	 */
	HeatmapStatInfo getStatsInfo(int viewMode, int mailingId, int companyId, int deviceType);
}
