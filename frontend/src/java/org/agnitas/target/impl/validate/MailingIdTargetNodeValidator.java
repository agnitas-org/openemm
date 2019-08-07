/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target.impl.validate;

import java.util.Collection;
import java.util.Vector;

import org.agnitas.beans.Mailing;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.target.TargetError;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetNodeValidator;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link TargetNodeValidator} that validates given mailing ID.
 * It reports an error, if mailing ID is unknown for given company ID.
 */
public class MailingIdTargetNodeValidator implements TargetNodeValidator {

	/** DAO for accessing mailing data. */
	private MailingDao mailingDao;
	
	@Override
	public Collection<TargetError> validate(TargetNode node, @VelocityCheck int companyId) {
		try {
			int mailingId = Integer.parseInt( node.getPrimaryValue());
			
			Mailing mailing = mailingDao.getMailing( mailingId, companyId);
			
			if( mailing != null && mailing.getId() == mailingId)
				return null;
			
			return reportInvalidMailingId();
		} catch( Exception e) {
			return reportInvalidMailingId();
		}
		
	}
	
	/**
	 * Create error result for invalid mailing ID. 
	 * 
	 * @return error result for invalid mailing ID
	 */
	private static Collection<TargetError> reportInvalidMailingId() {
		Collection<TargetError> errors = new Vector<>();
		errors.add( new TargetError( TargetError.ErrorKey.INVALID_MAILING));
		
		return errors;
	}

	// ------------------------------------------------------------- Dependency Injection
	/**
	 * Setter for mailing DAO.
	 * 
	 * @param mailingDao mailing DAo
	 */
	@Required
	public void setMailingDao( MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
}
