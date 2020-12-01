/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

public interface AnonymizeStatisticsDao {

	/**
	 * Anonymizes statistics for all recipients. If <code>anonymizeAll</code> is <code>true</code>, statistics of
	 * all recipients (independently from SYS_TRACKING_VETO value) will be anonymized.
	 * 
	 * @param companyID ID of company
	 * @param anonymizeAll if <code>true</code> statistics of all recipients will be anonymized
	 * 
	 * @throws Exception on errors during processing
	 */
	void anonymizeStatistics(int companyID, final boolean anonymizeAll) throws Exception;
}
