/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dao;

import java.util.List;


import com.agnitas.reporting.birt.external.beans.LightMailingList;

/**
 * A light version of the MailingListDao, without application-context stuff, and persisting. Just reading mailingList(s);
 */
public interface LightMailingListDao {
	LightMailingList getMailingList(int mailingListID, int companyID);

	List<LightMailingList> getMailingLists(List<Integer> mailingListIDs, int companyID);
}
