/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.backend.dao;

import org.agnitas.ecs.backend.beans.ClickStatInfo;


public interface EmbeddedClickStatDao {

	/**
	 * Gets click statistics for the mailing for certain ecs mode
	 *
	 * @param companyId id of company
	 * @param mailingId id of mailing
	 * @param mode	  ecs-mode
	 * @return click statistics for links of the mailing
	 * @throws Exception 
	 */
	ClickStatInfo getClickStatInfo(int companyId, int mailingId, int mode, int deviceClass) throws Exception;
}
