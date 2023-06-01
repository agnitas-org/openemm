/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.filter;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import org.agnitas.emm.core.download.service.DownloadService;
import org.agnitas.service.ProfileImportWorker;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.ProfileImportAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.Admin;
import com.agnitas.emm.grid.grid.service.ComGridTemplateService;
import com.agnitas.util.FutureHolderMap;

/**
 * Cleanup session data when user logs out or session is destroyed (after inactivity timeout or user closed browser)
 */
public class HttpSessionCleanUpListener implements HttpSessionListener {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(HttpSessionCleanUpListener.class);
	
	@Override
	public void sessionCreated(HttpSessionEvent httpSessionEvent) {
		// Do nothing
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
		HttpSession session = httpSessionEvent.getSession();
		String sessionID = session.getId();
		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());
		
		// Cleanup FutureHolder
		FutureHolderMap futureHolder = (FutureHolderMap) applicationContext.getBean("futureHolder");
		List<String> keysToRemove = new ArrayList<>();
		for (String key : futureHolder.keySet()) {
			if (key.endsWith("@" + sessionID) ) {
				keysToRemove.add(key);
			}
		}
		for (String removeMe : keysToRemove) {
			futureHolder.remove(removeMe);
		}
		
		// Remove all download data and associated files
		DownloadService downloadService = (DownloadService) applicationContext.getBean("DownloadService");
		downloadService.removeAllDownloadData(session);
		
		// Cleanup grid recycle bin
		Admin admin = (Admin) session.getAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN);
        if (admin != null) {
            ComGridTemplateService gridTemplateService = applicationContext.getBean("GridTemplateService", ComGridTemplateService.class);
            gridTemplateService.deleteRecycledChildren(admin.getCompanyID());
        }
        
        // Cleanup waiting interactive imports
        ProfileImportWorker profileImportWorker = (ProfileImportWorker) session.getAttribute(ProfileImportAction.PROFILEIMPORTWORKER_SESSIONKEY);
		if (profileImportWorker != null && profileImportWorker.isWaitingForInteraction()) {
			profileImportWorker.cleanUp();
			session.removeAttribute(ProfileImportAction.PROFILEIMPORTWORKER_SESSIONKEY);
			logger.info("Canceled interactively waiting ProfileImport for session: " + sessionID + " " + (admin != null ? "admin: " + admin.getUsername() : ""));
		}
	}
}
