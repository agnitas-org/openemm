/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.userforms.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.emm.core.userforms.UserformService;
import org.agnitas.exceptions.FormNotFoundException;
import org.agnitas.service.UserFormExporter;
import org.agnitas.service.UserFormImporter;
import org.agnitas.util.Tuple;

import com.agnitas.dao.UserFormDao;
import com.agnitas.userform.bean.UserForm;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link UserformService}.
 */
public class UserformServiceImpl implements UserformService {

	/** Regular expression for validation of form name. */
	private static final Pattern FORM_NAME_PATTERN = Pattern.compile( "^[a-zA-Z0-9\\-_]+$");

	protected UserFormDao userFormDao;
	protected UserFormExporter userFormExporter;
	protected UserFormImporter userFormImporter;

	@Override
	public final boolean isFormNameInUse(String formName, int formId, int companyId) {
		return userFormDao.isFormNameInUse(formName, formId, companyId);
	}

	@Override
	public boolean isFormNameInUse(String formName, int companyId) {
		return isFormNameInUse(formName, 0, companyId);
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

    @Override
    public List<Tuple<Integer, String>> getUserFormNamesByActionID(int companyID, int actionID) {
		if (companyID > 0 && actionID > 0) {
			return userFormDao.getUserFormNamesByActionID(companyID, actionID);
		}

		return new ArrayList<>();
    }

	@Override
	public void copyUserForm(int id, int companyId, int newCompanyId, int mailinglistID, String rdirDomain, Map<Integer, Integer> actionIdReplacements) throws Exception {
		File userFormTempFile = File.createTempFile("UserFormTempFile_", ".json");
		try {
			try (OutputStream userFormOutputStream = new FileOutputStream(userFormTempFile)) {
				userFormExporter.exportUserFormToJson(companyId, id, userFormOutputStream, true);
			}

			replacePlaceholdersInFile(userFormTempFile, newCompanyId, mailinglistID, rdirDomain);

			try (InputStream userFormInputStream = new FileInputStream(userFormTempFile)) {
				userFormImporter.importUserForm(newCompanyId, userFormInputStream, null, actionIdReplacements);
			}
		} catch (Exception e) {
			throw new Exception(String.format("Could not copy user form (%d) for new company (%d): %s", id, newCompanyId, e.getMessage()), e);
		} finally {
			if (userFormTempFile.exists()) {
				userFormTempFile.delete();
			}
		}
	}

	private void replacePlaceholdersInFile(File userFormTempFile, int companyID, int mailinglistID, String rdirDomain) throws IOException {
		String content = FileUtils.readFileToString(userFormTempFile, StandardCharsets.UTF_8);

		String cid = Integer.toString(companyID);
		content = StringUtils.replaceEach(content, new String[]{"<CID>", "<cid>", "[COMPANY_ID]", "[company_id]", "[Company_ID]"},
				new String[]{cid, cid, cid, cid, cid});

		String mlid = Integer.toString(mailinglistID);
		content = StringUtils.replaceEach(content, new String[]{"<MLID>", "<mlid>", "[MAILINGLIST_ID]", "[mailinglist_id]", "[Mailinglist_ID]"},
				new String[]{mlid, mlid, mlid, mlid, mlid});

		content = content.replace("<rdir-domain>", StringUtils.defaultIfBlank(rdirDomain, "RDIR-Domain"));

		FileUtils.writeStringToFile(userFormTempFile, content, StandardCharsets.UTF_8);
	}

	private UserForm doGetUserForm(final int companyID, final String formName) throws FormNotFoundException {
		try {
			return this.userFormDao.getUserFormByName(formName, companyID);
		}catch(final Exception e) {
			throw new FormNotFoundException(companyID, formName, e);
		}
	}

	// ------------------------------------------------------------- Dependency Injection

	public final void setUserFormDao(final UserFormDao dao) {
		this.userFormDao = Objects.requireNonNull(dao, "User form DAO cannot be null");
	}

	public void setUserFormExporter(UserFormExporter userFormExporter) {
		this.userFormExporter = userFormExporter;
	}

	@Required
	public void setUserFormImporter(UserFormImporter userFormImporter) {
		this.userFormImporter = userFormImporter;
	}
}
