/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.Comparator;

public class RecipientsStatisticRowComparator implements Comparator<RecipientsStatisticCommonRow> {
	@Override
	public int compare(RecipientsStatisticCommonRow row1, RecipientsStatisticCommonRow row2) {
		if (row1 != null && row2 != null) {
			if (row1.getMailingListName() != null && row2.getMailingListName() != null) {
				return row1.getMailingListName().compareTo(row2.getMailingListName());
			}
		}
		return 0;
	}
}
