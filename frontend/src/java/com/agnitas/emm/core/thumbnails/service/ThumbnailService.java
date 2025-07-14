/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.thumbnails.service;

/**
 * Service to generate mailing thumbnails.
 */
public interface ThumbnailService {
    
    public static final int MAILING_THUMBNAIL_WIDTH = 300;
    public static final int MAILING_THUMBNAIL_HEIGHT = 300;

	/**
	 * Invoked by webservice to update thumbnails of mailings.
	 * 
	 * @param companyID company ID 
	 * @param mailingID mailing ID
	 * 
	 * @throws Exception on errors updating thumbnail 
	 */
	void updateMailingThumbnailByWebservice(final int companyID, final int mailingID) throws Exception;
	
}
