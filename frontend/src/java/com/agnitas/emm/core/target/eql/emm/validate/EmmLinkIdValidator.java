/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.validate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.beans.TrackableLink;
import com.agnitas.dao.TrackableLinkDao;
import com.agnitas.emm.core.target.eql.codegen.validate.LinkIdValidationException;
import com.agnitas.emm.core.target.eql.codegen.validate.LinkIdValidator;
import com.agnitas.emm.core.target.eql.codegen.validate.UnknownLinkIdValidationException;

/**
 * EMM-specific implementation of {@link LinkIdValidator} interface.
 */
public class EmmLinkIdValidator implements LinkIdValidator {

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(EmmLinkIdValidator.class);
	
	/** DAO used for link ID validation. */
	private TrackableLinkDao linkDao;
	
	@Override
	public void validateLinkId(int mailingId, Integer linkId, int companyId) throws LinkIdValidationException {
		if(linkId != null) {
			if(logger.isInfoEnabled()) {
				logger.info("Validating link ID " + linkId + " for mailing ID " + mailingId);
			}
			
			TrackableLink link = this.linkDao.getTrackableLink(linkId, companyId);
			
			if(link == null || link.getMailingID() != mailingId) {
				if(logger.isInfoEnabled()) {
					logger.info("Validation link ID " + linkId + " for mailing ID " + mailingId + " failed");
				}
				
				throw new UnknownLinkIdValidationException(linkId, mailingId, companyId);
			}
		}
	}
	
	/**
	 * Set DAO for accessing trackable link data.
	 *  
	 * @param dao DAO for accessing trackable link data
	 */
	public void setTrackableLinkDao(TrackableLinkDao dao) {
		this.linkDao = dao;
	}
}
