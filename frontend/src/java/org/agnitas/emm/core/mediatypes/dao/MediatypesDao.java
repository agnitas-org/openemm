/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mediatypes.dao;

import java.util.Map;

import org.agnitas.beans.Mediatype;
import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * Interface for accessing media types of mailings.
 */
public interface MediatypesDao {
	void saveMediatypes(int mailingID, Map<Integer, Mediatype> mediatypes) throws Exception;
	
	/**
	 * Read all mediatypes for given mailing.
	 * 
	 * @param mailingId mailing ID
	 * @param companyId company ID
	 * 
	 * @return mapping from media type code to media type
	 * 
	 * @throws MediatypesDaoException on errors during reading media types
	 */
	Map<Integer, Mediatype> loadMediatypes(int mailingId, @VelocityCheck int companyId) throws MediatypesDaoException;
}
