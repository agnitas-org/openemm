/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverprio.server.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.agnitas.emm.core.serverprio.bean.ServerPrio;
import com.agnitas.emm.core.serverprio.dao.ServerPrioDao;
import com.agnitas.emm.core.serverprio.server.ServerPrioService;

public final class ServerPrioServiceImpl implements ServerPrioService {

	/** DAO dealing with ServerPrio. */
	private ServerPrioDao serverPrioDao;
	
	@Override
	public final boolean pauseMailGenerationAndDelivery(final int mailingID) {
		final ServerPrio serverPrio = new ServerPrio(0, mailingID, 0, new Date(), null);

		this.serverPrioDao.insertServerPrio(serverPrio);
		
		return true;
	}

	@Override
	public final boolean resumeMailGenerationAndDelivery(final int mailingID) {
		return this.serverPrioDao.deleteServerPrioByMailingAndCompany(0, mailingID);
	}

	@Override
	public final boolean isMailGenerationAndDeliveryPaused(final int mailingID) {
		final List<ServerPrio> list = this.serverPrioDao.listServerPriosByMailingAndCompany(0, mailingID);
		
		final Optional<Integer> result = list.stream()
				.filter(prio -> prio.getPrio().isPresent())		// Remove all ServerPrios without prio set
				.map(prio -> prio.getPrio().get())				// Replace all ServerPrios by it prio
				.filter(x -> x == 0)							// Remove all prios <> 0
				.findFirst();									// Returns the first (if present)

		return result.isPresent();
	}

	@Override
	public final Date getDeliveryPauseDate(final int companyId, final int mailingId) {
		return serverPrioDao.getDeliveryPauseDate(companyId, mailingId);
	}

	public final void setServerPrioDao(final ServerPrioDao dao) {
		this.serverPrioDao = Objects.requireNonNull(dao, "Server prio DAO is null");
	}
}
