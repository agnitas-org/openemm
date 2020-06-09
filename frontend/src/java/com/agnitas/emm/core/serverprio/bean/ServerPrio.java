/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverprio.bean;

import java.util.Date;
import java.util.Optional;

public class ServerPrio {

	private final int companyID;
	private final int mailingID;
	private final Optional<Integer> prio;
	private final Optional<Date> startDate;
	private final Optional<Date> endDate;
	
	public ServerPrio(final int companyID, final int mailingID, final Integer prioOrNull, final Date startDateOrNull, final Date endDateOrNull) {
		this.companyID = companyID;
		this.mailingID = mailingID;
		this.prio = Optional.ofNullable(prioOrNull);
		this.startDate = Optional.ofNullable(startDateOrNull);
		this.endDate = Optional.ofNullable(endDateOrNull);
	}

	public final int getCompanyID() {
		return companyID;
	}

	public final int getMailingID() {
		return mailingID;
	}

	public final Optional<Integer> getPrio() {
		return prio;
	}

	public final Optional<Date> getStartDate() {
		return startDate;
	}

	public final Optional<Date >getEndDate() {
		return endDate;
	}
	
}
