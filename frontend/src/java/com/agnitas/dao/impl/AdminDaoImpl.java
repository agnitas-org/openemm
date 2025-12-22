/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.AdminGroup;
import com.agnitas.beans.Company;
import com.agnitas.beans.IntEnum;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.impl.AdminEntryImpl;
import com.agnitas.beans.impl.AdminImpl;
import com.agnitas.beans.impl.CompanyStatus;
import com.agnitas.dao.AdminDao;
import com.agnitas.dao.AdminGroupDao;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.AdminException;
import com.agnitas.emm.core.admin.AdminNameNotFoundException;
import com.agnitas.emm.core.admin.AdminNameNotUniqueException;
import com.agnitas.emm.core.admin.encrypt.PasswordEncryptor;
import com.agnitas.emm.core.admin.enums.UiLayoutType;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.commons.password.PasswordReminderState;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.mailinglist.dao.MailinglistApprovalDao;
import com.agnitas.emm.core.news.enums.NewsType;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

/**
 * DAO handler for Admin-Objects
 * This class is compatible with oracle and mysql datasources and databases
 */
public class AdminDaoImpl extends PaginatedBaseDaoImpl implements AdminDao {

	private final RowMapper<Admin> adminRowMapper = new Admin_RowMapper();

    private AdminGroupDao adminGroupDao;
	private CompanyDao companyDao;
	private MailinglistApprovalDao mailinglistApprovalDao;
	private PasswordEncryptor passwordEncryptor;
	private ConfigService configService;

    public void setAdminGroupDao(AdminGroupDao adminGroupDao) {
        this.adminGroupDao = adminGroupDao;
    }

    public void setCompanyDao(CompanyDao companyDao) {
        this.companyDao = companyDao;
    }

    public void setMailinglistApprovalDao(MailinglistApprovalDao mailinglistApprovalDao) {
    	this.mailinglistApprovalDao = mailinglistApprovalDao;
    }

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setPasswordEncryptor(PasswordEncryptor passwordEncryptor) {
		this.passwordEncryptor = passwordEncryptor;
	}

	@Override
	public Admin getAdminByLogin(String username, String password) {
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			return null;
		}

		try {
			Admin admin = getAdminInternal(username);
			if (isAdminPassword(admin, password)) {
				return admin;
			}

			logger.warn("Invalid credentials for {}", username);
			return null;
		} catch (AdminNameNotFoundException e) {
			logger.warn("Invalid username: {}", username);
			return null;
		} catch (AdminException e) {
			return null;
		}
	}

	@Override
	public Admin getByNameAndActiveCompany(String username) throws AdminNameNotFoundException, AdminNameNotUniqueException {
		return getAdminInternal(username);
	}

	@Override
	public Admin getAdmin(int adminID, int companyID) {
		if (adminID == 0 || companyID == 0) {
			return null;
		} else if (companyID == 1) {
			final String additionalColumns = DbUtilities.joinColumnsNames(getAdditionalExtendedColumns(), true);
			return selectObjectDefaultNull(
					"SELECT admin_id, username, fullname, firstname, employee_id, company_id, company_name, email, stat_email, secure_password_hash, creation_date, pwdchange_date,"
							+ " admin_country, admin_lang, admin_lang_variant, admin_timezone, layout_base_id, layout_type, default_import_profile_id, timestamp,"
							+ " last_login_date, gender, title, news_date, message_date, phone_number, restful, password_reminder" + additionalColumns
							+ " FROM admin_tbl WHERE admin_id = ?",
					getAdminRowMapper(), adminID);
		} else {
			final String additionalColumns = DbUtilities.joinColumnsNames(getAdditionalExtendedColumns(), true);
			return selectObjectDefaultNull(
					"SELECT admin_id, username, fullname, firstname, employee_id, company_id, company_name, email, stat_email, secure_password_hash, creation_date, pwdchange_date,"
							+ " admin_country, admin_lang, admin_lang_variant, admin_timezone, layout_base_id, layout_type, default_import_profile_id, timestamp,"
							+ " last_login_date, gender, title, news_date, message_date, phone_number, restful, password_reminder" + additionalColumns
							+ " FROM admin_tbl WHERE admin_id = ? AND (company_id = ? OR company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ? AND status != '" + CompanyStatus.DELETED.getDbValue() + "'))",
					getAdminRowMapper(), adminID, companyID, companyID);
		}
	}

	@Override
	public Admin getAdmin(String username) throws AdminNameNotFoundException, AdminNameNotUniqueException {
		return getAdminInternal(username);
	}

	@Override
	public Admin getByEmail(String email) {
		String query = "SELECT * FROM admin_tbl WHERE email = ? AND company_id IN (SELECT company_id FROM company_tbl WHERE status = ?)";
		return selectObjectDefaultNull(query, getAdminRowMapper(), email, CompanyStatus.ACTIVE.getDbValue());
	}

	private Admin getAdminInternal(String username) throws AdminNameNotFoundException, AdminNameNotUniqueException {
		if (StringUtils.isBlank(username)) {
			logger.debug("User name if null in getAdminInternal(String username) call.{}", username);
			throw new AdminNameNotFoundException("");
		}

		String name = username.trim();
		//Using "SELECT * ...", because of huge number of needed fields
		String sql = "SELECT * FROM admin_tbl WHERE username = ? AND company_id"
				+ " IN (SELECT company_id FROM company_tbl WHERE status = '" + CompanyStatus.ACTIVE.getDbValue() + "')";
		List<Admin> adminList = select(sql, getAdminRowMapper(), name);
		if (adminList.size() == 1) {
			return adminList.get(0);
		} else if (adminList.isEmpty()) {
			logger.debug("User not found or part of inactive company: {}", name);
			throw new AdminNameNotFoundException(name);
		} else {
			logger.debug("Username is not unique: {}", name);
			throw new AdminNameNotUniqueException(name);
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void save(Admin admin) {
		if (admin == null) {
			throw new IllegalArgumentException("Invalid empty Admin for storage");
		}

		boolean isNewAdmin = admin.getAdminID() == 0;

		Date now = DateUtilities.getNowWithoutMilliseconds().getTime();

		if (isNewAdmin) {
			if (admin.getCreationDate() == null) {
				admin.setCreationDate(now);
			}
			admin.setLastPasswordChange(now);

			final String additionalColumns = DbUtilities.joinColumnsNames(getAdditionalExtendedColumns(), true);

			// store new Admin and set its AdminID
			if (isOracleDB()) {
				// Insert new Admin into DB
				int newAdminId = selectInt("SELECT admin_tbl_seq.NEXTVAL FROM DUAL");

				final List<Object> params = new ArrayList<>(Arrays.asList(newAdminId,
						admin.getUsername(),
						admin.getFullname(),
						admin.getFirstName(),
						admin.getEmployeeID(),
						admin.getCompanyID(),
						admin.getCompanyName(),
						admin.getEmail(),
						admin.getStatEmail(),
						admin.getSecurePasswordHash(),
						admin.getCreationDate(),
						admin.getLastPasswordChange(),
						admin.getAdminCountry(),
						admin.getAdminLang(),
						admin.getAdminLangVariant(),
						admin.getAdminTimezone(),
						admin.getLayoutBaseID(),
						admin.getLayoutType().getId(),
						admin.getDefaultImportProfileID(),
						new Date(),
						admin.getGender(),
						admin.getTitle(),
						admin.getLastNewsDate(),
						admin.getLastMessageDate(),
						admin.getAdminPhone(),
                        admin.getPasswordReminderState() == null ? null : admin.getPasswordReminderState().ordinal()));
				params.addAll(getAdditionalExtendedParams(admin));

				int touchedLines = update(
					"INSERT INTO admin_tbl (admin_id, username, fullname, firstname, employee_id, company_id, company_name, email, stat_email, secure_password_hash,"
						+ " creation_date, pwdchange_date, admin_country, admin_lang, admin_lang_variant, admin_timezone, layout_base_id, layout_type, default_import_profile_id,"
						+ " timestamp, gender, title, news_date, message_date, phone_number, password_reminder" + additionalColumns
						+ ") VALUES (" + AgnUtils.repeatString("?", params.size(), ", ") + ")",
						params.toArray()
					);

				if (touchedLines != 1) {
					throw new IllegalStateException("Illegal insert result");
				}

				// set the new id to refresh the Admin
				admin.setAdminID(newAdminId);
			} else {
				final List<Object> params = new ArrayList<>(Arrays.asList(
						admin.getUsername(),
						admin.getFullname(),
						admin.getFirstName(),
						admin.getEmployeeID(),
						admin.getCompanyID(),
						admin.getCompanyName(),
						admin.getEmail(),
						admin.getStatEmail(),
						admin.getSecurePasswordHash(),
						admin.getCreationDate(),
						admin.getLastPasswordChange(),
						admin.getAdminCountry(),
						admin.getAdminLang(),
						admin.getAdminLangVariant(),
						admin.getAdminTimezone(),
						admin.getLayoutBaseID(),
						admin.getLayoutType().getId(),
						admin.getDefaultImportProfileID(),
						new Date(),
						admin.getGender(),
						admin.getTitle(),
						admin.getLastNewsDate(),
						admin.getLastMessageDate(),
						admin.getAdminPhone(),
                        admin.getPasswordReminderState() == null ? null : admin.getPasswordReminderState().ordinal()));
				params.addAll(getAdditionalExtendedParams(admin));

				int newAdminId = insert("admin_id",
					"INSERT INTO admin_tbl (username, fullname, firstname, employee_id, company_id, company_name, email, stat_email, secure_password_hash, creation_date,"
						+ " pwdchange_date, admin_country, admin_lang, admin_lang_variant, admin_timezone, layout_base_id, layout_type, default_import_profile_id,"
						+ " timestamp, gender, title, news_date, message_date, phone_number, password_reminder" + additionalColumns
						+ ") VALUES (" + AgnUtils.repeatString("?", params.size(), ", ") + ")",
						params.toArray()
				);

				// set the new id to refresh the Admin
				admin.setAdminID(newAdminId);
			}

			if (admin.getSecurePasswordHash() == null) {
				admin.setSecurePasswordHash(createSecurePasswordHash(admin.getAdminID(), admin.getPasswordForStorage()));
			}

			// Remove Password from memory
			admin.setPasswordForStorage(null);

			// store new Password data after insert which gave us the admin_id for the first time
			save(admin);
		} else {
			if (getAdmin(admin.getAdminID(), admin.getCompanyID()) == null) {
				throw new IllegalArgumentException("Admin for update does not exist! ID: " + admin.getAdminID());
			}

			if (!StringUtils.isEmpty(admin.getPasswordForStorage())) {
				// Only overwrite when new password is set
				admin.setSecurePasswordHash(createSecurePasswordHash(admin.getAdminID(), admin.getPasswordForStorage()));
                setReminderStateForSave(admin);
				admin.setLastPasswordChange(now);

				// Remove Password from memory
				admin.setPasswordForStorage(null);
			}

			final List<Object> params = new ArrayList<>(Arrays.asList(admin.getUsername(),
					admin.getFullname(),
					admin.getFirstName(),
					admin.getEmployeeID(),
					admin.getCompanyID(),
					admin.getCompanyName(),
					admin.getEmail(),
					admin.getStatEmail(),
					admin.getSecurePasswordHash(),
					admin.getCreationDate(),
					admin.getLastPasswordChange(),
					admin.getAdminCountry(),
					admin.getAdminLang(),
					admin.getAdminLangVariant(),
					admin.getAdminTimezone(),
					admin.getLayoutBaseID(),
					admin.getLayoutType().getId(),
					admin.getDefaultImportProfileID(),
					new Date(),
					admin.getGender(),
					admin.getTitle(),
					admin.getAdminPhone(),
                    admin.getPasswordReminderState() == null ? null : admin.getPasswordReminderState().ordinal(),
					admin.isRestful() ? 1 : 0));

			params.addAll(getAdditionalExtendedParams(admin));
			params.add(admin.getAdminID());

			// Update Admin in DB
			int touchedLines = update(
				"UPDATE admin_tbl SET username = ?, fullname = ?, firstname = ?, employee_id = ?, company_id = ?,"
					+ " company_name = ?, email = ?, stat_email = ?, secure_password_hash = ?, creation_date = ?,"
					+ " pwdchange_date = ?, admin_country = ?, admin_lang = ?, admin_lang_variant = ?, admin_timezone = ?,"
					+ " layout_base_id = ?, layout_type = ?, default_import_profile_id = ?, timestamp = ?, gender = ?,"
					+ " title = ?, phone_number = ?, password_reminder = ?, restful = ?"
					+ DbUtilities.joinColumnsNamesForUpdate(getAdditionalExtendedColumns(), true)
					+ " WHERE admin_id = ?",
					params.toArray());

			if (touchedLines != 1) {
				throw new IllegalStateException("Illegal update result");
			}

            // clear permissions of existing user for new storing afterwards
			update("DELETE FROM admin_permission_tbl WHERE admin_id = ?", admin.getAdminID());

	        // write permissions
			if (CollectionUtils.isNotEmpty(admin.getAdminPermissions())){
				List<Object[]> parameterList = new ArrayList<>();
	            for (Permission permission : admin.getAdminPermissions()) {
					parameterList.add(new Object[] { admin.getAdminID(), permission.getTokenString()});
	            }
	            batchupdate("INSERT INTO admin_permission_tbl (admin_id, permission_name) VALUES (?, ?)", parameterList);
	        }

			// clear group references of existing user for new storing afterwards
			update("DELETE FROM admin_to_group_tbl WHERE admin_id = ?", admin.getAdminID());

	        // write group references
			if (CollectionUtils.isNotEmpty(admin.getGroups())){
				List<Object[]> parameterList = new ArrayList<>();
	            for (AdminGroup group : admin.getGroups()) {
					parameterList.add(new Object[] { admin.getAdminID(), group.getGroupID() });
	            }
	            batchupdate("INSERT INTO admin_to_group_tbl (admin_id, admin_group_id) VALUES (?, ?)", parameterList);
	        }
            saveExtendedProperties(admin);
        }
	}

    private void setReminderStateForSave(Admin admin) {
        if (admin.getPasswordReminderState() != PasswordReminderState.NOT_REQUIRED) {
            admin.setPasswordReminderState(PasswordReminderState.REQUIRED);
        }
    }

    protected void saveExtendedProperties(Admin admin) {
	    // nothing to do
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean delete(Admin admin) {
		if (admin == null) {
			return false;
		} else {
			return delete(admin.getAdminID(), admin.getCompanyID());
		}
	}

	@Override
	public final boolean delete(final int adminID, final int companyID) {
		final Company company = companyDao.getCompany(companyID);
		final int companyAdminId = company.getStatAdmin();

		if (adminID == companyAdminId && CompanyStatus.ACTIVE == company.getStatus()) {
			return false;
		} else {
			if (configService.isDisabledMailingListsSupported()) {
				mailinglistApprovalDao.allowAdminToUseAllMailinglists(companyID, adminID);
			}

			deleteAdminDependentData(adminID);
			int touchedLines = update("DELETE FROM admin_tbl WHERE admin_id = ? AND (company_id = ? OR company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ? and status != '" + CompanyStatus.DELETED.getDbValue() + "'))", adminID, companyID, companyID);
			return touchedLines == 1;
		}
	}

	protected void deleteAdminDependentData(int adminID) {
		update("DELETE FROM admin_permission_tbl WHERE admin_id = ?", adminID);
		update("DELETE FROM admin_to_group_tbl WHERE admin_id = ?", adminID);
		update("DELETE FROM undo_workflow_tbl WHERE admin_id = ?", adminID);
		update("DELETE FROM ui_message_tbl WHERE admin_id = ?", adminID);
	}

	@Override
	public boolean adminExists(String username) {
		return selectInt("SELECT admin_id FROM admin_tbl WHERE username = ?", username) > 0;
	}

	@Override
	public boolean isEnabled(Admin admin) {
		return selectInt("SELECT COUNT(*) FROM admin_tbl WHERE admin_id = ? AND company_id = ?", admin.getAdminID(), admin.getCompanyID()) > 0;
	}

	@Override
	public List<String> getUsernames(boolean restful) {
		String query = "SELECT adm.username FROM admin_tbl adm, company_tbl comp WHERE adm.company_id = comp.company_id AND restful = ?";
		return select(query, StringRowMapper.INSTANCE, BooleanUtils.toInteger(restful));
	}

	@Override
	public List<String> getUsernames(boolean restful, int companyId) {
		String query = "SELECT adm.username FROM admin_tbl adm, company_tbl comp WHERE (adm.company_id = ? OR adm.company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ?)) " +
				"AND status = ? AND comp.company_ID = adm.company_id AND restful = ? ORDER BY adm.username";

		return select(query, StringRowMapper.INSTANCE, companyId, companyId, CompanyStatus.ACTIVE.getDbValue(), BooleanUtils.toInteger(restful));
	}

	@Override
    public List<AdminEntry> getAllAdminsByCompanyIdOnly(int companyID) {
        return select("SELECT company_id, admin_id, username, last_login_date, fullname, firstname, creation_date, timestamp FROM admin_tbl WHERE company_id = ?", new AdminEntry_RowMapper(), companyID);
	}

	@Override
	public List<AdminEntry> getAllAdminsByCompanyIdOnlyHasEmail(int companyID) {
        return select("SELECT company_id, admin_id, username, last_login_date, fullname, firstname, creation_date, timestamp FROM admin_tbl WHERE company_id = ? and email IS NOT NULL", new AdminEntry_RowMapper(), companyID);
	}

	@Override
	public Map<Integer, String> getAdminsNamesMap(int companyId) {
		Map<Integer, String> map = new LinkedHashMap<>();

		String sqlGetNames = "SELECT admin_id, username FROM admin_tbl WHERE company_id = ? ORDER BY username";
		query(sqlGetNames, new NameMapCallback(map), companyId);

		return map;
	}

	@Override
	public PaginatedList<AdminEntry> getList(int companyId, String sort, String dir, int pageNumber, int pageSize) {
		String query = "SELECT company_id, admin_id, username, last_login_date, fullname, firstname, creation_date, timestamp FROM admin_tbl WHERE company_id = ?";
		return selectPaginatedList(query, "admin_tbl", sort, AgnUtils.sortingDirectionToBoolean(dir), pageNumber, pageSize, new AdminEntry_RowMapper(), companyId);
	}

	@Override
	public List<AdminEntry> findAllByEmailPart(String email, int companyID) {
		String query = "SELECT company_id, admin_id, username, last_login_date, fullname, firstname, creation_date, timestamp FROM admin_tbl WHERE company_id = ? AND "
				+ getPartialSearchFilter("email");
		return select(query, new AdminEntry_RowMapper(), companyID, email);
	}

	@Override
	public List<AdminEntry> findAllByEmailPart(String email) {
		String query = "SELECT company_id, admin_id, username, last_login_date, fullname, firstname, creation_date, timestamp FROM admin_tbl WHERE "
				+ getPartialSearchFilter("email");

		return select(query, new AdminEntry_RowMapper(), email);
	}

	@Override
	public AdminEntry findByEmail(String email, int companyId) {
		String query = """
				SELECT company_id,
				       admin_id,
				       username,
				       last_login_date,
				       fullname,
				       firstname,
				       creation_date,
				       timestamp,
				       admin_lang,
				       admin_country
				FROM admin_tbl
				WHERE company_id = ? AND email = ?
				""";

		return select(query, new AdminEntry_RowMapper(), companyId, email)
				.stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public void updateEmail(String email, int id, int companyId) {
		update("UPDATE admin_tbl SET email = ? WHERE admin_id = ? AND company_id = ?", email, id, companyId);
	}

	@Override
	public PaginatedList<AdminEntry> getAdminList(int companyID, String searchFirstName, String searchLastName, String searchEmail, String searchCompanyName, Integer filterCompanyId, Integer filterAdminGroupId, Integer filterMailinglistId, String filterLanguage, DateRange creationDate,
													  DateRange lastLoginDate, String username, String sortColumn, String sortDirection, int pageNumber, int pageSize, boolean showRestfulUsers) {
		if (StringUtils.isBlank(sortColumn)) {
			sortColumn = "username";
		} else {
			sortColumn = sortColumn.replaceAll("^adm.", "");
		}

		boolean sortDirectionAscending = !"desc".equalsIgnoreCase(sortDirection) && !"descending".equalsIgnoreCase(sortDirection);

		SqlPreparedStatementManager sqlPreparedStatementManager = createBaseOverviewQuery(showRestfulUsers, companyID, false);

		addAdminListFilters(
				searchFirstName,
				searchLastName,
				searchEmail,
				searchCompanyName,
				filterCompanyId,
				filterAdminGroupId,
				filterMailinglistId,
				filterLanguage,
				creationDate,
				lastLoginDate,
				username,
				sqlPreparedStatementManager
		);

		PaginatedList<AdminEntry> list = selectPaginatedList(sqlPreparedStatementManager.getPreparedSqlString(), "admin_tbl", sortColumn,
				sortDirectionAscending, pageNumber, pageSize, new AdminEntry_RowMapper_Email(), sqlPreparedStatementManager.getPreparedSqlParameters());

		if (isNotBlank(searchFirstName) || isNotBlank(searchLastName) || isNotBlank(searchEmail) || isNotBlank(searchCompanyName)
				|| filterCompanyId != null || filterAdminGroupId != null || filterMailinglistId != null || isNotBlank(filterLanguage)
				|| creationDate.isPresent() || lastLoginDate.isPresent() || isNotBlank(username)) {
			SqlPreparedStatementManager totalCountStatement = createBaseOverviewQuery(showRestfulUsers, companyID, true);
			list.setNotFilteredFullListSize(selectInt(totalCountStatement.getPreparedSqlString(), totalCountStatement.getPreparedSqlParameters()));
		}

		return list;
    }

	private SqlPreparedStatementManager createBaseOverviewQuery(boolean showRestfulUsers, int companyID, boolean useCountQuery) {
        StringBuilder query = new StringBuilder().append("SELECT ")
				.append(useCountQuery ? "COUNT(*)" : "adm.admin_id, adm.username, adm.fullname, adm.firstname, adm.last_login_date, adm.company_name, adm.email, adm.admin_lang, comp.shortname, adm.company_id, adm.creation_date, adm.timestamp, adm.pwdchange_date")
				.append(" FROM admin_tbl adm JOIN company_tbl comp ON (comp.company_ID = adm.company_id) WHERE comp.status = ? AND restful = ?");

		SqlPreparedStatementManager sqlPreparedStatementManager = new SqlPreparedStatementManager(
                query.toString(),
				CompanyStatus.ACTIVE.getDbValue(),
				showRestfulUsers ? 1 : 0
		);

		// WHERE clause already in statement
		sqlPreparedStatementManager.setHasAppendedClauses(true);

		if (companyID > 1) {
			sqlPreparedStatementManager.addWhereClause("adm.company_id = ? OR adm.company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ?)", companyID, companyID);
		}

		return sqlPreparedStatementManager;
	}

    protected void addAdminListFilters(String searchFirstName, String searchLastName, String searchEmail, String searchCompanyName, Integer filterCompanyId, Integer filterAdminGroupId, Integer filterMailinglistId, String filterLanguage, DateRange creationDate,
									   DateRange lastLoginDate, String username, SqlPreparedStatementManager statementManager) {
		if (searchFirstName != null && !searchFirstName.isEmpty()) {
			if (isOracleDB()) {
				statementManager.addWhereClause("UPPER(adm.firstname) LIKE ('%' || UPPER(?) || '%')", searchFirstName);
			} else {
				statementManager.addWhereClause("adm.firstname LIKE CONCAT('%', ?, '%')", searchFirstName);
			}
		}

		if (searchLastName != null && !searchLastName.isEmpty()) {
			if (isOracleDB()) {
				statementManager.addWhereClause("UPPER(adm.fullname) LIKE ('%' || UPPER(?) || '%')", searchLastName);
			} else {
				statementManager.addWhereClause("adm.fullname LIKE CONCAT('%', ?, '%')", searchLastName);
			}
		}

		if (searchEmail != null && !searchEmail.isEmpty()) {
			if (isOracleDB()) {
				statementManager.addWhereClause("UPPER(adm.email) LIKE ('%' || UPPER(?) || '%')", searchEmail);
			} else {
				statementManager.addWhereClause("adm.email LIKE CONCAT('%', ?, '%')", searchEmail);
			}
		}

		if (searchCompanyName != null && !searchCompanyName.isEmpty()) {
			if (isOracleDB()) {
				statementManager.addWhereClause("UPPER(adm.company_name) LIKE ('%' || UPPER(?) || '%')", searchCompanyName);
			} else {
				statementManager.addWhereClause("adm.company_name LIKE CONCAT('%', ?, '%')", searchCompanyName);
			}
		}

		if (filterCompanyId != null) {
			statementManager.addWhereClause("adm.company_id = ?", filterCompanyId);
		}

		if (filterAdminGroupId != null) {
			statementManager.addWhereClause("EXISTS (SELECT 1 FROM admin_to_group_tbl grp WHERE grp.admin_id = adm.admin_id AND grp.admin_group_id = ?)", filterAdminGroupId);
		}

		if (filterLanguage != null && !filterLanguage.isEmpty()) {
			statementManager.addWhereClause("adm.admin_lang = ?", filterLanguage);
		}

		if (creationDate.getFrom() != null) {
			statementManager.addWhereClause("adm.creation_date >= ?", creationDate.getFrom());
		}
		if (creationDate.getTo() != null) {
			statementManager.addWhereClause("adm.creation_date < ?", DateUtilities.addDaysToDate(creationDate.getTo(), 1));
		}

		if (lastLoginDate.getFrom() != null) {
			statementManager.addWhereClause("adm.last_login_date >= ?", lastLoginDate.getFrom());
		}
		if (lastLoginDate.getTo() != null) {
			statementManager.addWhereClause("adm.last_login_date < ?", DateUtilities.addDaysToDate(lastLoginDate.getTo(), 1));
		}

		if (isNotBlank(username)) {
			statementManager.addWhereClause(getPartialSearchFilter("adm.username"), username);
		}
	}

    protected class Admin_RowMapper implements RowMapper<Admin> {
		@Override
		public Admin mapRow(ResultSet resultSet, int row) throws SQLException {
			Admin readAdmin = new AdminImpl();

			readAdmin.setAdminID(resultSet.getBigDecimal("admin_id").intValue());
			readAdmin.setUsername(resultSet.getString("username"));
			readAdmin.setFullname(resultSet.getString("fullname"));
			readAdmin.setFirstName(resultSet.getString("firstname"));
			readAdmin.setEmployeeID(resultSet.getString("employee_id"));
			readAdmin.setCompanyName(resultSet.getString("company_name"));
			readAdmin.setEmail(resultSet.getString("email"));
			readAdmin.setStatEmail(resultSet.getString("stat_email"));
			readAdmin.setSecurePasswordHash(resultSet.getString("secure_password_hash"));
			readAdmin.setCreationDate(resultSet.getTimestamp("creation_date"));
			readAdmin.setLastPasswordChange(resultSet.getTimestamp("pwdchange_date"));
			readAdmin.setAdminCountry(resultSet.getString("admin_country"));
			readAdmin.setAdminLang(resultSet.getString("admin_lang"));
			readAdmin.setAdminLangVariant(resultSet.getString("admin_lang_variant"));
			readAdmin.setAdminTimezone(resultSet.getString("admin_timezone"));
			readAdmin.setLayoutBaseID(resultSet.getInt("layout_base_id"));
			readAdmin.setLayoutType(IntEnum.fromId(UiLayoutType.class, resultSet.getInt("layout_type")));
			readAdmin.setDefaultImportProfileID(resultSet.getInt("default_import_profile_id"));
			readAdmin.setGender(resultSet.getInt("gender"));
			readAdmin.setTitle(resultSet.getString("title"));
			readAdmin.setLastNewsDate(resultSet.getTimestamp("news_date"));
			readAdmin.setLastMessageDate(resultSet.getTimestamp("message_date"));
			readAdmin.setAdminPhone(resultSet.getString("phone_number"));
			readAdmin.setLastLoginDate(resultSet.getTimestamp("last_login_date"));
			readAdmin.setRestful(resultSet.getInt("restful") > 0);
            readAdmin.setPasswordReminderState(PasswordReminderState.fromCode(resultSet.getInt("password_reminder")));

			// Read additional data

			List<String> tokens = select("SELECT permission_name FROM admin_permission_tbl WHERE admin_id = ?", StringRowMapper.INSTANCE, readAdmin.getAdminID());

			Set<Permission> adminPermissions = Permission.fromTokens(tokens);
			readAdmin.setAdminPermissions(adminPermissions);

			readAdmin.setCompany(companyDao.getCompany(resultSet.getBigDecimal("company_id").intValue()));

			Set<Permission> companyPermissions = companyDao.getCompanyPermissions(readAdmin.getCompanyID());
			readAdmin.setCompanyPermissions(companyPermissions);

			List<Integer> adminGroupIds = select("SELECT admin_group_id FROM admin_to_group_tbl WHERE admin_id = ? ORDER BY admin_group_id", IntegerRowMapper.INSTANCE, readAdmin.getAdminID());

			List<AdminGroup> adminGroups = new ArrayList<>();
			for (int adminGroupId : adminGroupIds) {
				AdminGroup adminGroup = adminGroupDao.getAdminGroup(adminGroupId, readAdmin.getCompanyID());
				adminGroups.add(adminGroup);
			}

			readAdmin.setGroups(adminGroups);

			return readAdmin;
		}
	}

    protected static class AdminEntry_RowMapper implements RowMapper<AdminEntry> {
		@Override
		public AdminEntry mapRow(ResultSet rs, int row) throws SQLException {
            AdminEntry readAdminEntry = new AdminEntryImpl(
            		rs.getBigDecimal("company_id").intValue(),
            		rs.getBigDecimal("admin_id").intValue(),
            		rs.getString("username"),
            		rs.getString("fullname"),
            		rs.getString("firstname"),
                    null,
                    DbUtilities.getStringOrNull(rs, "admin_lang"),
                    DbUtilities.getStringOrNull(rs, "admin_country")
			);

            readAdminEntry.setCreationDate(rs.getTimestamp("creation_date"));
            readAdminEntry.setChangeDate(rs.getTimestamp("timestamp"));
            readAdminEntry.setLoginDate(rs.getTimestamp("last_login_date"));

            return readAdminEntry;
		}
	}

	protected class AdminEntry_RowMapper_Email implements RowMapper<AdminEntry> {
		@Override
		public AdminEntry mapRow(ResultSet rs, int row) throws SQLException {
			int companyID = rs.getBigDecimal("company_id").intValue();

            AdminEntry readAdminEntry = new AdminEntryImpl(
            		companyID,
            		rs.getBigDecimal("admin_id").intValue(),
            		rs.getString("username"),
            		rs.getString("fullname"),
            		rs.getString("firstname"),
            		null,
                    rs.getString("email"),
					rs.getString("company_name"),
					DbUtilities.getStringOrNull(rs, "admin_lang"),
					DbUtilities.getStringOrNull(rs, "admin_country")
			);

			readAdminEntry.setCreationDate(rs.getTimestamp("creation_date"));
			readAdminEntry.setChangeDate(rs.getTimestamp("timestamp"));
			readAdminEntry.setLoginDate(rs.getTimestamp("last_login_date"));

			boolean passwordExpired;
			int expirationDays = configService.getIntegerValue(ConfigValue.UserPasswordExpireDays, companyID);
			if (expirationDays <= 0) {
				// Expiration is disabled for company.
				passwordExpired = false;
			} else {
				Timestamp pwdChangeDate = rs.getTimestamp("pwdchange_date");
				Date expirationDate = DateUtils.addDays(pwdChangeDate, expirationDays);
				if (DateUtilities.isPast(expirationDate)) {
					int expirationLockDays = configService.getIntegerValue(ConfigValue.UserPasswordFinalExpirationDays, companyID);
					if (expirationLockDays <= 0) {
						passwordExpired = false;
					} else {
						Date expirationLockDate = DateUtils.addDays(expirationDate, expirationLockDays);
						if (DateUtilities.isPast(expirationLockDate)) {
							passwordExpired = true;
						} else {
							passwordExpired = false;
						}
					}
				} else {
					passwordExpired = false;
				}
			}
        	readAdminEntry.setPasswordExpired(passwordExpired);

			return readAdminEntry;
		}
	}

	protected static class NameMapCallback implements RowCallbackHandler {

		private final Map<Integer, String> map;

		public NameMapCallback(Map<Integer, String> map) {
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			map.put(rs.getInt("admin_id"), rs.getString("username"));
		}
	}

	/**
	 * Check if password matches admin password. If secure password hash is set,
	 * this hash is used. Otherwise the old hash is used.
	 * 
	 * @param admin
	 *            the Admin
	 * @param password
	 *            the password
	 * @return true, if password matches admin password
	 */
	@Override
	public boolean isAdminPassword(Admin admin, String password) {
		if (StringUtils.isEmpty(password) || admin == null) {
			return false;
		}

		return this.passwordEncryptor.isAdminPassword(password, admin);
	}

	/**
	 * Rules for setting the secure password hash instead of plaintext
	 * password:
	 * 
	 * 1. Password encrypt must be set in application context (therefore, a
	 * salt file exists and is specified in application context)
	 * 
	 * 2. The user record in admin_tbl has the secure_password_enabled set to 1
	 * 
	 * If one of these conditions is not met, the password is stored as
	 * plain text.
	 */
	private String createSecurePasswordHash(int adminId, String password) {
		if (adminId == 0) {
			// No admin ID? Then we cannot set a secure password hash
			logger.error("illegal adminid for password storage");
			return "";
		}
		if (StringUtils.isEmpty(password)) {
			logger.error("illegal password for password storage");
			return "";
		}

		if (passwordEncryptor == null) {
			logger.info("password encryptor not set - using old-style password");
			return "";
		}

		logger.info("setting secure password hash for admin {}", adminId);
		return passwordEncryptor.computeAdminPasswordHash(password, adminId);
	}

	@Override
	@DaoUpdateReturnValueCheck
    public int saveAdminRights(int adminID, Set<String> userRights) {
    	update("DELETE FROM admin_permission_tbl WHERE admin_id = ?", adminID);
        List<Object[]> parameterList = new ArrayList<>();
        for (String permission : userRights) {
        	parameterList.add(new Object[] { adminID, permission });
        }

        int[] result =  batchupdate("INSERT INTO admin_permission_tbl (admin_id, permission_name) VALUES (?, ?)", parameterList);

        int touchedRows = 0;
        for (int rows : result) {
        	if (rows > 0) {
        		touchedRows += rows;
        	}
		}

        return touchedRows;
    }

	@Override
	public int getNumberOfGuiAdmins(int companyID) {
		if (companyID > 0) {
			return selectInt("SELECT COUNT(*) FROM admin_tbl WHERE restful = 0 AND company_id = ?", companyID);
		} else {
			return selectInt("SELECT COUNT(*) FROM admin_tbl WHERE restful = 0");
		}
	}

	@Override
	public boolean updateNewsDate(final int adminID, Date newsDate, NewsType type) {
		String columnName = type.name().toLowerCase() + "_date";
		return update("UPDATE admin_tbl SET " + columnName + " = ? WHERE admin_id = ?", newsDate, adminID) == 1;
	}

	@Override
	public Admin getOldestAdminOfCompany(int companyID) {
		if (companyID == 0) {
			return null;
		} else {
			final String additionalColumns = DbUtilities.joinColumnsNames(getAdditionalExtendedColumns(), true);
			return selectObjectDefaultNull(
				"SELECT admin_id, username, fullname, firstname, employee_id, company_id, company_name, email, stat_email, secure_password_hash, creation_date, pwdchange_date,"
					+ " admin_country, admin_lang, admin_lang_variant, admin_timezone, layout_base_id, layout_type, default_import_profile_id, timestamp,"
					+ " gender, title, news_date, message_date, phone_number, last_login_date, limiting_target_id, restful, password_reminder" + additionalColumns
				+ " FROM admin_tbl WHERE company_id = ? AND admin_id = (SELECT MIN(admin_id) FROM admin_tbl WHERE company_id = ?)",
					getAdminRowMapper(), companyID, companyID);
		}
	}

	@Override
	public boolean checkBlacklistedAdminNames(String username) {
		return selectInt("SELECT COUNT(*) FROM admin_blacklist_tbl WHERE username = ?", username) > 0;
	}

	protected Collection<String> getAdditionalExtendedColumns() {
		return CollectionUtils.emptyCollection();
	}

	protected Collection<Object> getAdditionalExtendedParams(final Admin admin) {
		return CollectionUtils.emptyCollection();
	}

	protected RowMapper<Admin> getAdminRowMapper() {
		return adminRowMapper;
	}

    @Override
   	public int getOpenEmmDemoAccountWaitingMailingID(String language) {
   		return selectIntWithDefaultValue("SELECT mailing_id FROM mailing_tbl WHERE company_id = 1 AND shortname = ?", -1, "OpenEmmDemoAccountWaitingMail_" + language.toUpperCase());
   	}

    @Override
   	public int getOpenEmmDemoAccountDataMailingID(String language) {
   		return selectIntWithDefaultValue("SELECT mailing_id FROM mailing_tbl WHERE company_id = 1 AND shortname = ?", -1, "OpenEmmDemoAccountDataMail_" + language.toUpperCase());
   	}

    @Override
    public void setPasswordReminderState(int adminId, PasswordReminderState state) {
        update("UPDATE admin_tbl SET password_reminder = ? WHERE admin_id = ?", state.ordinal(), adminId);
    }

	@Override
	public List<Admin> getAdmins(int companyID, boolean restful) {
		if (companyID == 0) {
			return Collections.emptyList();
		}

		final String additionalColumns = DbUtilities.joinColumnsNames(getAdditionalExtendedColumns(), true);

		String query = "SELECT admin_id, username, fullname, firstname, employee_id, company_id, company_name, email, stat_email, secure_password_hash, creation_date, pwdchange_date,"
				+ " admin_country, admin_lang, admin_lang_variant, admin_timezone, layout_base_id, layout_type, default_import_profile_id, timestamp,"
				+ " last_login_date, gender, title, news_date, message_date, phone_number, restful, password_reminder" + additionalColumns
				+ " FROM admin_tbl WHERE restful = ?";
		List<Object> params = new ArrayList<>();
		params.add(restful ? 1 : 0);

		if (companyID != 1) {
			query += " AND (company_id = ? OR company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ? AND status != ?))";
			params.add(companyID);
			params.add(companyID);
			params.add(CompanyStatus.DELETED.getDbValue());
		}

		return select(query, getAdminRowMapper(), params.toArray());
	}

	@Override
	public List<Integer> getAccessLimitingAdmins(int accessLimitingTargetGroupID) {
		return Collections.emptyList();
	}

	@Override
	public int getNumberOfRestfulUsers(int companyID) {
		if (companyID > 0) {
			return selectInt("SELECT COUNT(*) FROM admin_tbl WHERE restful = 1 AND company_id = ?", companyID);
		} else {
			return selectInt("SELECT COUNT(*) FROM admin_tbl WHERE restful = 1");
		}
	}

	@Override
	public void deleteAdminPermissionsForCompany(int companyID) {
		update("UPDATE admin_tbl SET limiting_target_id = NULL WHERE limiting_target_id IS NOT NULL AND company_id = ?", companyID);
		update("DELETE FROM disabled_mailinglist_tbl WHERE company_id = ?", companyID);
	}

	@Override
	public List<Map<String, Object>> getAdminsLight(int companyID, boolean restful) {
		return select("SELECT admin_id, username, fullname, firstname, employee_id, email FROM admin_tbl WHERE company_id = ? AND restful = ? ORDER BY LOWER(username)", companyID, restful ? 1 : 0);
	}

	@Override
	public void saveDashboardLayout(String layout, int adminId) {
		update("UPDATE admin_tbl SET dashboard_layout = ? WHERE admin_id = ?", layout, adminId);
	}

	@Override
	public String getDashboardLayout(int adminId) {
		return selectWithDefaultValue("SELECT dashboard_layout FROM admin_tbl WHERE admin_id = ?", String.class, "", adminId);
	}
}
