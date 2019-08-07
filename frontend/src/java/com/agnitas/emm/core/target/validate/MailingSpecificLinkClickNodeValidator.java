/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.validate;

import java.util.Collection;
import java.util.Vector;

import org.agnitas.beans.TrackableLink;
import org.agnitas.target.TargetError;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetNodeValidator;
import org.agnitas.target.impl.validate.MailingIdTargetNodeValidator;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComTrackableLinkDao;
import com.agnitas.emm.core.target.nodes.TargetNodeMailingClickedOnSpecificLink;

/**
 * Implementation of {@link TargetNodeValidator} that validates given link ID.
 * It reports an error, if link ID is unknown for given mailing ID.
 * 
 * Validation of mailing ID is also done by extended base class {@link MailingIdTargetNodeValidator}.
 */
public class MailingSpecificLinkClickNodeValidator extends MailingIdTargetNodeValidator {

	/**
	 * DAO for accessing trackable links.
	 */
	private ComTrackableLinkDao linkDao;
	
	@Override
	public Collection<TargetError> validate(TargetNode node0, int companyId) {
		Collection<TargetError> error = super.validate(node0, companyId);
		
		if(error == null || error.size() == 0) {
			TargetNodeMailingClickedOnSpecificLink node = (TargetNodeMailingClickedOnSpecificLink) node0;

			int mailingId = Integer.parseInt(node.getPrimaryValue()); // This was already checked by super.validate(), so we can do that without fear.
			
			try {
				int linkId = Integer.parseInt(node.getSecondaryValue());
				TrackableLink link = linkDao.getTrackableLink(linkId, companyId);
				
				// Check, that we found a link, it has expected ID and belongs to given mailing
				if(link == null || link.getId() != linkId || link.getMailingID() != mailingId) {
					return reportInvalidLink();
				}
			} catch(Exception e) {
				return reportInvalidLink();
			}
						
		}
		
		return error;
	}
	
	/**
	 * Create error result for invalid link ID. 
	 * 
	 * @return error result for invalid link ID
	 */
	private static Collection<TargetError> reportInvalidLink() {
		Collection<TargetError> errors = new Vector<>();
		errors.add(new TargetError(TargetError.ErrorKey.INVALID_MAILING));
		
		return errors;
	}

	// ------------------------------------------------------------- Dependency Injection
	/**
	 * Setter for trackable link DAO.
	 * 
	 * @param dao trackable link DAO
	 */
	@Required
	public void setTrackableLinkDao(ComTrackableLinkDao dao) {
		this.linkDao = dao;
	}
	

}
