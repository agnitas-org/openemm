/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.userforms.impl;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.emm.core.userforms.UserformService;
import org.agnitas.exceptions.FormNotFoundException;

import com.agnitas.dao.UserFormDao;
import com.agnitas.userform.bean.UserForm;

/**
 * Implementation of {@link UserformService}.
 */
public class UserformServiceImpl implements UserformService {

	// ------------------------------------------------------------- Business Code

	/** Regular expression for validation of form name. */
	private static final transient Pattern FORM_NAME_PATTERN = Pattern.compile( "^[a-zA-Z0-9\\-_]+$"); 
	
	@Override
	public final boolean isFormNameInUse(String formName, int formId, int companyId) {
		return userFormDao.isFormNameInUse(formName, formId, companyId);
	}

	@Override
	public final boolean isValidFormName(String formName) {
		Matcher matcher = FORM_NAME_PATTERN.matcher(formName);
		
		return matcher.matches();
	}

	@Override
	public final UserForm getUserForm(final int companyID, final String formName) throws FormNotFoundException {
		final UserForm form = doGetUserForm(companyID, formName);
		
		if(form == null) {
			throw new FormNotFoundException(companyID, formName);
		}

		return form;
	}
	
	private final UserForm doGetUserForm(final int companyID, final String formName) throws FormNotFoundException {
		try {
			return this.userFormDao.getUserFormByName(formName, companyID);
		}catch(final Exception e) {
			throw new FormNotFoundException(companyID, formName, e);
		}
	}

	// ------------------------------------------------------------- Dependency Injection
	/**
	 * DAO for accessing userform data.
	 */
	protected UserFormDao userFormDao;

	/**
	 * Set DAO for accessing userform data.
	 * 
	 * @param dao DAO for accessing userform data
	 */
	public final void setUserFormDao(final UserFormDao dao) {
		this.userFormDao = Objects.requireNonNull(dao, "User form DAO cannot be null");
	}

}
