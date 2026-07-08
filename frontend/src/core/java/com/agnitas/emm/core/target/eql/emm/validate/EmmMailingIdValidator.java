/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.validate;

import com.agnitas.emm.core.mailing.bean.LightweightMailing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.target.eql.codegen.validate.MailingIdValidationException;
import com.agnitas.emm.core.target.eql.codegen.validate.MailingIdValidator;

/**
 * EMM-specific implementation of {@link MailingIdValidator} interface.
 */
public class EmmMailingIdValidator implements MailingIdValidator {

	private static final Logger logger = LogManager.getLogger(EmmMailingIdValidator.class);
	
	/** DAO for accessing mailing data. */
	private MailingDao mailingDao;
	
	@Override
	public void validateMailingId(int mailingId, int companyId) throws MailingIdValidationException {
		logger.info("Validating mailing ID {} for company {}", mailingId, companyId);

		LightweightMailing mailing = mailingDao.getLightweightMailing(companyId, mailingId);
		
		if(mailing == null) {
			logger.info("Validation mailing ID {} for company {} failed.", mailingId, companyId);
			throw new MailingIdValidationException("Unknown mailing ID " + mailingId + " (company ID " + companyId + ")");
		}
	}

	
	/**
	 * Sets DAO for accessing mailing data.
	 * 
	 * @param dao DAO for accessing mailing data
	 */
	public void setMailingDao(MailingDao dao) {
		this.mailingDao = dao;
	}
}
