/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.filter;

import com.agnitas.emm.core.imports.web.RecipientImportController;
import com.agnitas.service.ProfileImportWorker;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Cleanup session data when user logs out or session is destroyed (after inactivity timeout or user closed browser)
 */
public class HttpSessionCleanUpListener implements HttpSessionListener {
	
	@Override
	public void sessionCreated(HttpSessionEvent httpSessionEvent) {
		// Do nothing
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
		HttpSession session = httpSessionEvent.getSession();

		// Cleanup waiting interactive imports
		ProfileImportWorker profileImportWorker = (ProfileImportWorker) session.getAttribute(RecipientImportController.SESSION_WORKER_KEY);
		if (profileImportWorker != null && profileImportWorker.isWaitingForInteraction()) {
			profileImportWorker.cleanUp();
		}
	}
}
