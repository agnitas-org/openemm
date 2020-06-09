/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.backend.dao;

import java.util.List;

import org.agnitas.ecs.backend.beans.ClickStatColor;
import org.agnitas.ecs.backend.beans.ClickStatInfo;
import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * Dao for accessing <code>click_stat_colors_tbl</code> - the table that
 * stores color values for percent ranges for differnet companies
 */
public interface EmbeddedClickStatDao {

	/**
	 * Method gets collection of color values for company id
	 *
	 * @param companyId id of company
	 * @return collection of {@link ClickStatColor} beans for companyId
	 */
	List<ClickStatColor> getClickStatColors(@VelocityCheck int companyId);

	/**
	 * Gets click statistics for the mailing for certain ecs mode
	 *
	 * @param companyId id of company
	 * @param mailingId id of mailing
	 * @param mode	  ecs-mode
	 * @return click statistics for links of the mailing
	 * @throws Exception 
	 */
	ClickStatInfo getClickStatInfo(@VelocityCheck int companyId, int mailingId, int mode) throws Exception;

	ClickStatInfo getClickStatInfo(@VelocityCheck int companyId, int mailingId, int mode, int deviceClass) throws Exception;
}
