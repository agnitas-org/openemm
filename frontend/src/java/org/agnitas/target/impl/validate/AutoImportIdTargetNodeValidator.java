/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target.impl.validate;

import java.util.Collection;
import java.util.Vector;

import org.agnitas.emm.core.autoimport.dao.AutoImportDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.target.TargetError;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetNodeValidator;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link org.agnitas.target.TargetNodeValidator} that validates given auto-import ID.
 * It reports an error, if auto-import ID is unknown for given company ID.
 */
public class AutoImportIdTargetNodeValidator implements TargetNodeValidator {
	private AutoImportDao autoImportDao;

	@Override
	public Collection<TargetError> validate(TargetNode node, @VelocityCheck int companyId) {
		try {
			int autoImportId = Integer.parseInt(node.getPrimaryValue());

			if (autoImportDao.exists(autoImportId, companyId)) {
				return null;
			}

			return reportInvalidAutoImportId();
		} catch (Exception e) {
			return reportInvalidAutoImportId();
		}
	}

	/**
	 * Create error result for invalid auto-import ID.
	 * 
	 * @return error result for invalid auto-import ID
	 */
	private static Collection<TargetError> reportInvalidAutoImportId() {
		Collection<TargetError> errors = new Vector<>();
		errors.add(new TargetError(TargetError.ErrorKey.INVALID_AUTO_IMPORT));
		return errors;
	}

	@Required
	public void setAutoImportDao(AutoImportDao autoImportDao) {
		this.autoImportDao = autoImportDao;
	}
}
