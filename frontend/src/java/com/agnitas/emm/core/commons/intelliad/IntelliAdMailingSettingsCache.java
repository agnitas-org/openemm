/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.intelliad;

import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * Cache for the IntelliAd mailing settings.
 */
public interface IntelliAdMailingSettingsCache {
	
	/**
	 * Returns the IntelliAd settings for given mailing from cache or (if case of a
	 * cache-miss) from database.
	 * If IntelliAd is not configured for given mailing, then the returned object
	 * is set to &quot;disabled&quot; with unspecified tracking string.
	 * 
	 * @param companyId companyID
	 * @param mailingId mailingID
	 * 
	 * @return IntelliAd settings for given mailing
	 */
	public IntelliAdMailingSettings getIntelliAdSettings( @VelocityCheck int companyId, int mailingId);
}
