/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.userforms;

import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.exceptions.FormNotFoundException;
import org.agnitas.util.Tuple;

import com.agnitas.userform.bean.UserForm;

/**
 * Service layer for forms.
 */
public interface UserformService {

	/**
	 * Checks, if there is another form with same name.
	 *
	 * @param formName  name of form
	 * @param formId    ID of current form
	 * @param companyId company ID
	 * @return true, if form name is already in use
	 */
	boolean isFormNameInUse(final String formName, final int formId, final int companyId);

	boolean isFormNameInUse(final String formName, final int companyId);

	/**
	 * Checks, if the form name does not contain invalid characters.
	 *
	 * @param formName form name to check
	 * @return true, if form name is valid
	 */
	boolean isValidFormName(final String formName);

	/**
	 * Load user form for given form name.
	 *
	 * @param companyID company ID
	 * @param formName  form name
	 * @return user form
	 * @throws FormNotFoundException if given form name is unknown
	 */
	UserForm getUserForm(final int companyID, final String formName) throws FormNotFoundException;

	List<Tuple<Integer, String>> getUserFormNamesByActionID(@VelocityCheck int companyID, int actionID);

}
