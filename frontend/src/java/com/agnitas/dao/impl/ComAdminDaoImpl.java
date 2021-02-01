/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComCompany;
import com.agnitas.beans.impl.ComAdminImpl;
import com.agnitas.dao.ComAdminDao;
import com.agnitas.dao.ComAdminGroupDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.AdminException;
import com.agnitas.emm.core.admin.AdminNameNotFoundException;
import com.agnitas.emm.core.admin.AdminNameNotUniqueException;
import com.agnitas.emm.core.admin.encrypt.PasswordEncryptor;
import com.agnitas.emm.core.news.enums.NewsType;
import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.impl.AdminEntryImpl;
import org.agnitas.beans.impl.CompanyStatus;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailinglistApprovalDao;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.SqlPreparedStatementManager;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

/**
 * DAO handler for ComAdmin-Objects
 * This class is compatible with oracle and mysql datasources and databases
 */
public class ComAdminDaoImpl extends PaginatedBaseDaoImpl implements ComAdminDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComAdminDaoImpl.class);

	private final RowMapper<ComAdmin> adminRowMapper = new ComAdmin_RowMapper();
	
	/** DAO for accessing admin groups. */
    protected ComAdminGroupDao adminGroupDao;
    
    /** DAO for accessing company data. */
	protected ComCompanyDao companyDao;
	
	protected MailinglistApprovalDao mailinglistApprovalDao;
	
	/** Encryptor for passwords. */
	protected PasswordEncryptor passwordEncryptor;
	
	private ConfigService configService;

    /**
     * Set DAO for accessing admin group data.
     * 
     * @param adminGroupDao DAO for accessing admin group data
     */
    @Required
    public void setAdminGroupDao(ComAdminGroupDao adminGroupDao) {
        this.adminGroupDao = adminGroupDao;
    }

    /**
     * Set DAO for accessing company data.
     * 
     * @param companyDao DAO for accessing company data
     */
    @Required
    public void setCompanyDao(ComCompanyDao companyDao) {
        this.companyDao = companyDao;
    }
    
    @Required
    public void setMailinglistApprovalDao(MailinglistApprovalDao mailinglistApprovalDao) {
    	this.mailinglistApprovalDao = mailinglistApprovalDao;
    }
    
    @Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	/**
	 * Sets password encryptor.
	 * 
	 * @param passwordEncryptor password encryptor or null
	 */
	public void setPasswordEncryptor(PasswordEncryptor passwordEncryptor) {
		this.passwordEncryptor = passwordEncryptor;
	}

	@Override
	public ComAdmin getAdminByLogin(String username, String password) {
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			return null;
		}
		
		try {
			ComAdmin admin = getAdminInternal(username);
			return isAdminPassword(admin, password) ? admin : null;
		} catch (AdminException e) {
			return null;
		}
	}
	
	@Override
	public ComAdmin getByNameAndActiveCompany(String username) throws AdminNameNotFoundException, AdminNameNotUniqueException {
		return getAdminInternal(username);
	}

	@Override
	public ComAdmin getAdmin(int adminID, @VelocityCheck int companyID) {
		if (adminID == 0 || companyID == 0) {
			return null;
		} else {
			final String additionalColumns = DbUtilities.joinColumnsNames(getAdditionalExtendedColumns(), true);
			return selectObjectDefaultNull(logger,
					"SELECT admin_id, username, fullname, firstname, company_id, company_name, email, stat_email, secure_password_hash, creation_date, pwdchange_date,"
							+ " admin_country, admin_lang, admin_lang_variant, admin_timezone, layout_base_id, default_import_profile_id, timestamp,"
							+ " gender, title, news_date, message_date, is_one_time_pass, phone_number" + additionalColumns
							+ " FROM admin_tbl WHERE admin_id = ? AND (company_id = ? OR company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ? AND status != '" + CompanyStatus.DELETED.getDbValue() + "'))",
					getAdminRowMapper(), adminID, companyID, companyID);
		}
	}
	@Override
	public String getAdminName(int adminID, @VelocityCheck int companyID) {
		if (adminID == 0 || companyID == 0) {
			return null;
		} else {
			return selectObjectDefaultNull(logger,
					"SELECT username FROM admin_tbl " +
							"WHERE admin_id = ? AND (company_id = ? OR company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ? AND status != '" + CompanyStatus.DELETED.getDbValue() + "'))",
					new StringRowMapper(), adminID, companyID, companyID);
		}
	}

	@Override
	public ComAdmin getAdmin(String username) throws AdminNameNotFoundException, AdminNameNotUniqueException {
		return getAdminInternal(username);
	}
	
	private ComAdmin getAdminInternal(String username) throws AdminNameNotFoundException, AdminNameNotUniqueException {
		if (StringUtils.isBlank(username)) {
			logger.debug("User name if null in getAdminInternal(String username) call." + username);
			throw new AdminNameNotFoundException("");
		}

		String name = username.trim();
		String sql = "SELECT * FROM admin_tbl WHERE username = ? AND "
				+ "company_id" + " IN (SELECT company_id FROM company_tbl WHERE status = '" + CompanyStatus.ACTIVE.getDbValue() + "')";
		List<ComAdmin> adminList = select(logger, sql, getAdminRowMapper(), name);
		if (adminList.size() == 1) {
			return adminList.get(0);
		} else if (adminList.size() == 0) {
			logger.debug("User not found or part of inactive company: " + name);
			throw new AdminNameNotFoundException(name);
		} else {
			logger.debug("Username is not unique: " + name);
			throw new AdminNameNotUniqueException(name);
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void save(ComAdmin admin) throws Exception {
		if (admin == null) {
			throw new Exception("Invalid empty Admin for storage");
		}

		boolean isNewAdmin = (admin.getAdminID() == 0);
		
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
				int newAdminId = selectInt(logger, "SELECT admin_tbl_seq.nextval FROM DUAL");

				final List<Object> params = new ArrayList<>(Arrays.asList(newAdminId,
						admin.getUsername(),
						admin.getFullname(),
						admin.getFirstName(),
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
						admin.getDefaultImportProfileID(),
						new Date(),
						admin.getGender(),
						admin.getTitle(),
						admin.getLastNewsDate(),
						admin.getLastMessageDate(),
						admin.getAdminPhone(),
						admin.isOneTimePassword() ? 1 : 0));
				params.addAll(getAdditionalExtendedParams(admin));

				int touchedLines = update(logger,
					"INSERT INTO admin_tbl (admin_id, username, fullname, firstname, company_id, company_name, email, stat_email, secure_password_hash,"
						+ " creation_date, pwdchange_date, admin_country, admin_lang, admin_lang_variant, admin_timezone, layout_base_id, default_import_profile_id,"
						+ " timestamp, gender, title, news_date, message_date, phone_number, is_one_time_pass" + additionalColumns
						+ ") VALUES (" + AgnUtils.repeatString("?", params.size(), ", ") + ")",
						params.toArray()
					);
				
				if (touchedLines != 1) {
					throw new RuntimeException("Illegal insert result");
				}
		
				// set the new id to refresh the Admin
				admin.setAdminID(newAdminId);
			} else {

				final List<Object> params = new ArrayList<>(Arrays.asList(
						admin.getUsername(),
						admin.getFullname(),
						admin.getFirstName(),
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
						admin.getDefaultImportProfileID(),
						new Date(),
						admin.getGender(),
						admin.getTitle(),
						admin.getLastNewsDate(),
						admin.getLastMessageDate(),
						admin.getAdminPhone(),
						admin.isOneTimePassword() ? 1 : 0));
				params.addAll(getAdditionalExtendedParams(admin));

				int newAdminId = insertIntoAutoincrementMysqlTable(logger, "admin_id",
					"INSERT INTO admin_tbl (username, fullname, firstname, company_id, company_name, email, stat_email, secure_password_hash, creation_date,"
						+ " pwdchange_date, admin_country, admin_lang, admin_lang_variant, admin_timezone, layout_base_id, default_import_profile_id,"
						+ " timestamp, gender, title, news_date, message_date, phone_number, is_one_time_pass" + additionalColumns
						+ ") VALUES (" + AgnUtils.repeatString("?", params.size(), ", ") + ")",
						params.toArray()
				);
				
				// set the new id to refresh the Admin
				admin.setAdminID(newAdminId);
			}

			if (admin.getSecurePasswordHash() == null) {
				admin.setSecurePasswordHash(createSecurePasswordHash(admin.getAdminID(), admin.getCompanyID(), admin.getPasswordForStorage()));
			}
			
			// Remove Password from memory
			admin.setPasswordForStorage(null);
			
			// store new Password data after insert which gave us the admin_id for the first time
			save(admin);
		} else {
			if (getAdmin(admin.getAdminID(), admin.getCompanyID()) == null) {
				throw new RuntimeException("Admin for update does not exist");
			}
			
			if (!StringUtils.isEmpty(admin.getPasswordForStorage())) {
				// Only overwrite when new password is set
				admin.setSecurePasswordHash(createSecurePasswordHash(admin.getAdminID(), admin.getCompanyID(), admin.getPasswordForStorage()));
				admin.setLastPasswordChange(now);
				
				// Remove Password from memory
				admin.setPasswordForStorage(null);
			}

			final List<Object> params = new ArrayList<>(Arrays.asList(admin.getUsername(),
					admin.getFullname(),
					admin.getFirstName(),
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
					admin.getDefaultImportProfileID(),
					new Date(),
					admin.getGender(),
					admin.getTitle(),
					admin.getAdminPhone(),
					admin.isOneTimePassword() ? 1 : 0));

			params.addAll(getAdditionalExtendedParams(admin));
			params.add(admin.getAdminID());

			// Update Admin in DB
			int touchedLines = update(logger,
				"UPDATE admin_tbl SET username = ?, fullname = ?, firstname = ?, company_id = ?,"
					+ " company_name = ?, email = ?, stat_email = ?, secure_password_hash = ?, creation_date = ?,"
					+ " pwdchange_date = ?, admin_country = ?, admin_lang = ?, admin_lang_variant = ?, admin_timezone = ?,"
					+ " layout_base_id = ?, default_import_profile_id = ?, timestamp = ?, gender = ?,"
					+ " title = ?, phone_number = ?, is_one_time_pass = ?"
					+ DbUtilities.joinColumnsNamesForUpdate(getAdditionalExtendedColumns(), true)
					+ " WHERE admin_id = ?",
					params.toArray());
			
			if (touchedLines != 1) {
				throw new RuntimeException("Illegal update result");
			}

            // clear permissions of existing user for new storing afterwards
			update(logger, "DELETE FROM admin_permission_tbl WHERE admin_id = ?", admin.getAdminID());

	        // write permissions
			if (CollectionUtils.isNotEmpty(admin.getAdminPermissions())){
				List<Object[]> parameterList = new ArrayList<>();
	            for (Permission permission : admin.getAdminPermissions()) {
					parameterList.add(new Object[] { admin.getAdminID(), permission.getTokenString(), permission.getTokenString() });
	            }
	            batchupdate(logger, "INSERT INTO admin_permission_tbl (admin_id, security_token, permission_name) VALUES (?, ?, ?)", parameterList);
	        }
			
			// clear group references of existing user for new storing afterwards
			update(logger, "DELETE FROM admin_to_group_tbl WHERE admin_id = ?", admin.getAdminID());
	
	        // write group references
			if (CollectionUtils.isNotEmpty(admin.getGroups())){
				List<Object[]> parameterList = new ArrayList<>();
	            for (AdminGroup group : admin.getGroups()) {
					parameterList.add(new Object[] { admin.getAdminID(), group.getGroupID() });
	            }
	            batchupdate(logger, "INSERT INTO admin_to_group_tbl (admin_id, admin_group_id) VALUES (?, ?)", parameterList);
	        }
			
			// TODO: Remove in future
			// Legacy for interim time: Keep first/single admin group in old field admin_tbl.admin_group_id
			if (!admin.getGroups().isEmpty()) {
				update(logger, "UPDATE admin_tbl SET admin_group_id = ? WHERE admin_id = ?", admin.getGroups().get(0).getGroupID(), admin.getAdminID());
			} else {
				update(logger, "UPDATE admin_tbl SET admin_group_id = 0 WHERE admin_id = ?", admin.getAdminID());
			}
        }
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean delete(ComAdmin admin) {
		if (admin == null) {
			return false;
		} else {
			return delete(admin.getAdminID(), admin.getCompanyID());
		}
	}
	
	@Override
	public final boolean delete(final int adminID, final int companyID) {
		final ComCompany company = companyDao.getCompany(companyID);
		final int companyAdminId = company.getStatAdmin();
		
		if (adminID == companyAdminId && CompanyStatus.ACTIVE == company.getStatus()) {
			return false;
		} else {
			update(logger, "UPDATE auto_import_tbl SET admin_id = ? WHERE company_id = ? AND admin_id = ?", companyAdminId, companyID, adminID);
			update(logger, "UPDATE auto_export_tbl SET admin_id = ? WHERE company_id = ? AND admin_id = ?", companyAdminId, companyID, adminID);
			if (isDisabledMailingListsSupported()) {
				mailinglistApprovalDao.allowAdminToUseAllMailinglists(companyID, adminID);
			}
			update(logger, "DELETE FROM admin_permission_tbl WHERE admin_id = ?", adminID);
			update(logger, "DELETE FROM admin_to_group_tbl WHERE admin_id = ?", adminID);
			int touchedLines = update(logger, "DELETE FROM admin_tbl WHERE admin_id = ? AND (company_id = ? OR company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ? and status != '" + CompanyStatus.DELETED.getDbValue() + "'))", adminID, companyID, companyID);
			return touchedLines == 1;
		}
		
	}

	@Override
	public boolean adminExists(String username) {
		try {
			return selectInt(logger, "SELECT admin_id FROM admin_tbl WHERE username = ?", username) > 0;
		} catch (DataAccessException e) {
			return false;
		}
	}

	@Override
	public boolean isEnabled(ComAdmin admin) {
		try {
			return selectInt(logger, "SELECT COUNT(*) FROM admin_tbl WHERE admin_id = ? AND company_id = ?", admin.getAdminID(), admin.getCompanyID()) > 0;
		} catch (DataAccessException e) {
			return false;
		}
	}

	@Override
    public List<AdminEntry> getAllAdmins() {
		String sql = "SELECT adm.company_id, adm.admin_id, adm.username, adm.fullname, adm.firstname, adm.company_name, adm.email, adm.stat_email, adm.secure_password_hash, adm.creation_date,"
				+ " adm.pwdchange_date, adm.admin_country, adm.admin_lang, adm.admin_lang_variant, adm.admin_timezone, adm.layout_base_id, adm.default_import_profile_id,"
				+ " adm.timestamp, adm.gender, adm.title, adm.news_date, adm.message_date, adm.is_one_time_pass, comp.shortname"
				+ " FROM admin_tbl adm, company_tbl comp WHERE adm.company_id = comp.company_id ORDER BY LOWER(adm.username)";

		return select(logger, sql, new AdminEntry_RowMapper_Shortname());
    }

	@Override
    public List<AdminEntry> getAllAdminsByCompanyIdOnly(@VelocityCheck int companyID) {
        return select(logger, "SELECT company_id, admin_id, username, fullname, firstname, creation_date, timestamp FROM admin_tbl WHERE company_id = ?", new AdminEntry_RowMapper(), companyID);
	}

	@Override
	public List<AdminEntry> getAllAdminsByCompanyIdOnlyHasEmail(@VelocityCheck int companyID) {
        return select(logger, "SELECT company_id, admin_id, username, fullname, firstname, creation_date, timestamp FROM admin_tbl WHERE company_id = ? and email IS NOT NULL", new AdminEntry_RowMapper(), companyID);
	}

	@Override
	public List<AdminEntry> getAllAdminsByCompanyId(@VelocityCheck int companyID) {
        return select(logger, "SELECT adm.company_id, adm.admin_id, adm.username, adm.fullname, adm.firstname, adm.creation_date, adm.timestamp, comp.shortname FROM admin_tbl adm, company_tbl comp WHERE (adm.company_id = ? OR adm.company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ?)) AND status = '" + CompanyStatus.ACTIVE.getDbValue() + "' AND comp.company_ID = adm.company_id ORDER BY adm.username", new AdminEntry_RowMapper_Shortname(), companyID, companyID);
	}

    @Override
	public List<Map<String, Object>> getAdminsNames(@VelocityCheck int companyID, List<Integer> adminsIds) {
        // if the admin list is empty - return empty result list
        if (adminsIds == null || adminsIds.size() <= 0) {
            return new ArrayList<>();
        }
        
        String adminsIdsStr = StringUtils.join(new LinkedHashSet<>(adminsIds).toArray(), ", ");
        String sql = "SELECT admin_id, username FROM admin_tbl WHERE company_id = ? AND admin_id in (" + adminsIdsStr + ")";
        return select(logger, sql, companyID);
	}

	@Override
	public List<Tuple<Integer, String>> getAdminsUsernames(int companyID){
		return select(logger, "SELECT admin_id, username FROM admin_tbl WHERE company_id = ? ORDER BY LOWER(username)",
				(resultSet, i) -> new Tuple<>(resultSet.getInt("admin_id"), resultSet.getString("username")),
				companyID);
	}

	@Override
	public Map<Integer, String> getAdminsNamesMap(@VelocityCheck int companyId) {
		Map<Integer, String> map = new LinkedHashMap<>();

		String sqlGetNames = "SELECT admin_id, username FROM admin_tbl WHERE company_id = ? ORDER BY username";
		query(logger, sqlGetNames, new NameMapCallback(map), companyId);

		return map;
	}

	@Override
	public PaginatedListImpl<AdminEntry> getAdminList(@VelocityCheck int companyID, String searchFirstName, String searchLastName, String searchEmail, String searchCompanyName, Integer filterCompanyId, Integer filterAdminGroupId, Integer filterMailinglistId, String filterLanguage, String sortColumn, String sortDirection, int pageNumber, int pageSize) {
		if (StringUtils.isBlank(sortColumn)) {
			sortColumn = "username";
		} else {
			sortColumn = sortColumn.replaceAll("^adm.", "");
		}
		
		boolean sortDirectionAscending = !"desc".equalsIgnoreCase(sortDirection) && !"descending".equalsIgnoreCase(sortDirection);

        SqlPreparedStatementManager sqlPreparedStatementManager = new SqlPreparedStatementManager(
        	"SELECT adm.company_id, adm.admin_id, adm.username, adm.fullname, adm.firstname, adm.company_name, adm.email, adm.admin_lang, comp.shortname, adm.creation_date, adm.timestamp, logi.last_login, adm.pwdchange_date"
        		+ " FROM ((SELECT username AS loginusername, MAX(creation_date) AS last_login "
				 + " FROM login_track_tbl WHERE login_status = 10 AND username IN"
				 	+ " (SELECT username FROM admin_tbl WHERE company_id = ? OR"
				 	+ " company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ?)"
				 	+ ") GROUP BY username) logi"
				 + " RIGHT OUTER JOIN admin_tbl adm ON (logi.loginusername = adm.username))"
		 	+ " JOIN company_tbl comp ON (comp.company_ID = adm.company_id)"
		 	+ " WHERE (adm.company_id = ? OR adm.company_id IN ("
		 	+ " SELECT company_id FROM company_tbl WHERE creator_company_id = ?)"
			+ ")"
		 + " AND comp.status = '" + CompanyStatus.ACTIVE.getDbValue() + "'", companyID, companyID, companyID, companyID);

        // WHERE clause already in statement
        sqlPreparedStatementManager.setHasAppendedClauses(true);
        
        try {
        	if (searchFirstName != null && !searchFirstName.isEmpty()) {
                if (isOracleDB()) {
                	sqlPreparedStatementManager.addWhereClause("adm.firstname LIKE ('%' || ? || '%')", searchFirstName);
                } else {
                	sqlPreparedStatementManager.addWhereClause("adm.firstname LIKE CONCAT('%', ?, '%')", searchFirstName);
                }
            }

            if (searchLastName != null && !searchLastName.isEmpty()) {
                if (isOracleDB()) {
                	sqlPreparedStatementManager.addWhereClause("adm.fullname LIKE ('%' || ? || '%')", searchLastName);
                } else {
                	sqlPreparedStatementManager.addWhereClause("adm.fullname LIKE CONCAT('%', ?, '%')", searchLastName);
                }
            }

            if (searchEmail != null && !searchEmail.isEmpty()) {
                if (isOracleDB()) {
                	sqlPreparedStatementManager.addWhereClause("adm.email LIKE ('%' || ? || '%')", searchEmail);
                } else {
                	sqlPreparedStatementManager.addWhereClause("adm.email LIKE CONCAT('%', ?, '%')", searchEmail);
                }
            }

            if (searchCompanyName != null && !searchCompanyName.isEmpty()) {
                if (isOracleDB()) {
                	sqlPreparedStatementManager.addWhereClause("adm.company_name LIKE ('%' || ? || '%')", searchCompanyName);
                } else {
                	sqlPreparedStatementManager.addWhereClause("adm.company_name LIKE CONCAT('%', ?, '%')", searchCompanyName);
                }
            }

            if (filterCompanyId != null) {
            	sqlPreparedStatementManager.addWhereClause("adm.company_id = ?", filterCompanyId);
            }

            if (filterAdminGroupId != null) {
            	sqlPreparedStatementManager.addWhereClause("EXISTS (SELECT 1 FROM admin_to_group_tbl grp WHERE grp.admin_id = adm.admin_id AND grp.admin_group_id = ?)", filterAdminGroupId);
            }

            if (filterLanguage != null && !filterLanguage.isEmpty()) {
            	sqlPreparedStatementManager.addWhereClause("adm.admin_lang = ?", filterLanguage);
            }
        } catch (Exception e) {
            logger.error("Invalid filters", e);
        }
        
		if ("last_login".equalsIgnoreCase(sortColumn)) {
			String sortClause = " ORDER BY last_login " + (sortDirectionAscending ? "ASC" : "DESC");
			return selectPaginatedListWithSortClause(logger, sqlPreparedStatementManager.getPreparedSqlString(), sortClause, sortColumn, sortDirectionAscending, pageNumber, pageSize, new ComAdminEntry_RowMapper_LastLogin(), sqlPreparedStatementManager.getPreparedSqlParameters());
		} else {
			return selectPaginatedList(logger, sqlPreparedStatementManager.getPreparedSqlString(), "admin_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, new ComAdminEntry_RowMapper_LastLogin(), sqlPreparedStatementManager.getPreparedSqlParameters());
		}
    }
	
    @Override
	public ComAdmin getAdminForReport(@VelocityCheck int companyID) {
        String sqlGetAdmin;

        if (isOracleDB()) {
            sqlGetAdmin = "SELECT admin_id, company_id, secure_password_hash FROM admin_tbl WHERE company_id = ? AND rownum = 1";
        } else {
            sqlGetAdmin = "SELECT admin_id, company_id, secure_password_hash FROM admin_tbl WHERE company_id = ? LIMIT 1";
        }

        return selectObjectDefaultNull(logger, sqlGetAdmin, new AdminEntry_RowMapper_ForReport(), companyID);
	}
	
    protected class ComAdmin_RowMapper implements RowMapper<ComAdmin> {
		@Override
		public ComAdmin mapRow(ResultSet resultSet, int row) throws SQLException {
			ComAdmin readComAdmin = new ComAdminImpl();

			readComAdmin.setAdminID(resultSet.getBigDecimal("admin_id").intValue());
			readComAdmin.setUsername(resultSet.getString("username"));
			readComAdmin.setFullname(resultSet.getString("fullname"));
			readComAdmin.setFirstName(resultSet.getString("firstname"));
			readComAdmin.setCompanyName(resultSet.getString("company_name"));
			readComAdmin.setEmail(resultSet.getString("email"));
			readComAdmin.setStatEmail(resultSet.getString("stat_email"));
			readComAdmin.setSecurePasswordHash(resultSet.getString("secure_password_hash"));
			readComAdmin.setCreationDate(resultSet.getTimestamp("creation_date"));
			readComAdmin.setLastPasswordChange(resultSet.getTimestamp("pwdchange_date"));
			readComAdmin.setAdminCountry(resultSet.getString("admin_country"));
			readComAdmin.setAdminLang(resultSet.getString("admin_lang"));
			readComAdmin.setAdminLangVariant(resultSet.getString("admin_lang_variant"));
			readComAdmin.setAdminTimezone(resultSet.getString("admin_timezone"));
			readComAdmin.setLayoutBaseID(resultSet.getInt("layout_base_id"));
			readComAdmin.setDefaultImportProfileID(resultSet.getInt("default_import_profile_id"));
			readComAdmin.setGender(resultSet.getInt("gender"));
			readComAdmin.setTitle(resultSet.getString("title"));
			readComAdmin.setLastNewsDate(resultSet.getTimestamp("news_date"));
			readComAdmin.setLastMessageDate(resultSet.getTimestamp("message_date"));
			readComAdmin.setOneTimePassword(resultSet.getInt("is_one_time_pass") == 1);
			readComAdmin.setAdminPhone(resultSet.getString("phone_number"));

			// Read additional data

			List<String> tokens = select(logger, "SELECT security_token FROM admin_permission_tbl WHERE admin_id = ?", new StringRowMapper(), readComAdmin.getAdminID());

			Set<Permission> adminPermissions = Permission.fromTokens(tokens);
			readComAdmin.setAdminPermissions(adminPermissions);
			
			readComAdmin.setCompany(companyDao.getCompany(resultSet.getBigDecimal("company_id").intValue()));

			Set<Permission> companyPermissions = companyDao.getCompanyPermissions(readComAdmin.getCompanyID());
			readComAdmin.setCompanyPermissions(companyPermissions);
			
			List<Integer> adminGroupIds = select(logger, "SELECT admin_group_id FROM admin_to_group_tbl WHERE admin_id = ? ORDER BY admin_group_id", new IntegerRowMapper(), readComAdmin.getAdminID());
			
			List<AdminGroup> adminGroups = new ArrayList<>();
			for (int adminGroupId : adminGroupIds) {
				AdminGroup adminGroup = adminGroupDao.getAdminGroup(adminGroupId, readComAdmin.getCompanyID());
				adminGroups.add(adminGroup);
			}
			
			readComAdmin.setGroups(adminGroups);

			return readComAdmin;
		}
	}
	
    protected class AdminEntry_RowMapper implements RowMapper<AdminEntry> {
		@Override
		public AdminEntry mapRow(ResultSet resultSet, int row) throws SQLException {
            AdminEntry readAdminEntry = new AdminEntryImpl(
            		resultSet.getBigDecimal("company_id").intValue(),
            		resultSet.getBigDecimal("admin_id").intValue(),
            		resultSet.getString("username"),
            		resultSet.getString("fullname"),
            		resultSet.getString("firstname"),
                    null);
				readAdminEntry.setCreationDate(resultSet.getTimestamp("creation_date"));
				readAdminEntry.setChangeDate(resultSet.getTimestamp("timestamp"));
			return readAdminEntry;
		}
	}

	protected class AdminEntry_RowMapper_Shortname implements RowMapper<AdminEntry> {
		@Override
		public AdminEntry mapRow(ResultSet resultSet, int row) throws SQLException {
            AdminEntry readAdminEntry = new AdminEntryImpl(
            		resultSet.getBigDecimal("company_id").intValue(),
            		resultSet.getBigDecimal("admin_id").intValue(),
            		resultSet.getString("username"),
            		resultSet.getString("fullname"),
            		resultSet.getString("firstname"),
            		resultSet.getString("shortname"), // companyname from company_tbl not from admin_tbl
                    null);
				readAdminEntry.setCreationDate(resultSet.getTimestamp("creation_date"));
				readAdminEntry.setChangeDate(resultSet.getTimestamp("timestamp"));
			return readAdminEntry;
		}
	}
	
	protected class ComAdminEntry_RowMapper_LastLogin extends AdminEntry_RowMapper_Shortname {
		@Override
		public AdminEntry mapRow(ResultSet resultSet, int row) throws SQLException {
            AdminEntry readAdminEntry = super.mapRow(resultSet,row);
            readAdminEntry.setLoginDate(resultSet.getTimestamp("last_login"));
            Timestamp pwdChangeDate = resultSet.getTimestamp("pwdchange_date");
            readAdminEntry.setEmail(resultSet.getString("email"));
            boolean passwordExpired;
        	int passwordInvalidAfter = configService.getIntegerValue(ConfigValue.UserPasswordFinalExpirationDays, resultSet.getInt("company_id"));
        	if (pwdChangeDate != null && passwordInvalidAfter > 0) {
        		passwordExpired = pwdChangeDate.before(DateUtilities.getDateOfDaysAgo(passwordInvalidAfter));
        	} else {
        		passwordExpired = false;
        	}
            readAdminEntry.setPasswordExpired(passwordExpired);
			return readAdminEntry;
		}
	}

	protected class AdminEntry_RowMapper_ForReport implements RowMapper<ComAdmin> {
		@Override
		public ComAdmin mapRow(ResultSet resultSet, int row) throws SQLException {
			ComAdmin admin = new ComAdminImpl();
			
			admin.setAdminID(resultSet.getBigDecimal("admin_id").intValue());
			admin.getCompany().setId(resultSet.getBigDecimal("company_id").intValue());
			admin.setSecurePasswordHash(resultSet.getString("secure_password_hash"));
			
			return admin;
		}
	}

	protected static class NameMapCallback implements RowCallbackHandler {
		private Map<Integer, String> map;

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
	public boolean isAdminPassword(final ComAdmin admin, final String password) {
		try {
			if (StringUtils.isEmpty(password)) {
				// No empty passwords allowed
				return false;
			} else {
				// Check SecurePasswordHash
				return this.passwordEncryptor.isAdminPassword(password, admin);
			}
		} catch (Exception e) {
			logger.error("Cannot check admin password: " + e.getMessage(), e);
			return false;
		}
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
	 *
	 * @param adminId
	 * @param password
	 * @return
	 * @throws Exception
	 */
	private String createSecurePasswordHash(int adminId, int companyID, String password) throws Exception {
		if (adminId == 0) {
			// No admin ID? Then we cannot set a secure password hash
			logger.error("illegal adminid for password storage");
			return "";
		}
		else if (StringUtils.isEmpty(password)) {
			logger.error("illegal password for password storage");
			return "";
		}
		else if (passwordEncryptor == null) {
			if (logger.isInfoEnabled()) {
				logger.info("password encryptor not set - using old-style password");
			}
			return "";
		}
		else {
			if (logger.isInfoEnabled()) {
				logger.info("setting secure password hash for admin " + adminId);
			}
			return passwordEncryptor.computeAdminPasswordHash(password, adminId, companyID);
		}
	}

    @Override
	@DaoUpdateReturnValueCheck
    public int saveAdminRights(int adminID, Set<String> userRights) {
    	update(logger, "DELETE FROM admin_permission_tbl WHERE admin_id = ?", adminID);
        List<Object[]> parameterList = new ArrayList<>();
        for (String permission : userRights) {
        	parameterList.add(new Object[] { adminID, permission, permission });
        }

        int[] result =  batchupdate(logger, "INSERT INTO admin_permission_tbl (admin_id, security_token, permission_name) VALUES (?, ?, ?)", parameterList);
        
        int touchedRows = 0;
        for (int rows : result) {
        	if (rows > 0) {
        		touchedRows += rows;
        	}
		}

        return touchedRows;
    }

	@Override
    public List<AdminEntry> getAllWsAdmins() {
        String query = "SELECT wsadm.username, comp.shortname FROM webservice_user_tbl wsadm, company_tbl comp WHERE wsadm.company_id = comp.company_id ORDER BY LOWER(wsadm.username)";
        return select(logger, query, new WsAdminEntry_RowMapper());
    }

	@Override
    public List<AdminEntry> getAllWsAdminsByCompanyId( @VelocityCheck int companyID) {
        String query = "SELECT wsadm.username, comp.shortname FROM webservice_user_tbl wsadm, company_tbl comp WHERE wsadm.company_id = comp.company_id AND wsadm.company_id = ? ORDER BY LOWER(wsadm.username)";
        return select(logger, query, new WsAdminEntry_RowMapper(), companyID);
    }
	
	protected class WsAdminEntry_RowMapper implements RowMapper<AdminEntry> {
		@Override
		public AdminEntry mapRow(ResultSet resultSet, int row) throws SQLException {
			String username = resultSet.getString("username");
            String shortname = resultSet.getString("shortname");
            return new AdminEntryImpl(-1, -1, username, "WS:" + username, "WS:" + username, shortname);
		}
	}
	   
	@Override
	public int getNumberOfAdmins() {
		return selectInt(logger, "SELECT COUNT(*) FROM admin_tbl");
	}
	
	@Override
	public int getNumberOfAdmins(@VelocityCheck int companyID) {
		return selectInt(logger, "SELECT COUNT(*) FROM admin_tbl WHERE company_id = ?", companyID);
	}

	@Override
	public void deleteFeaturePermissions(Set<String> unAllowedPremiumFeatures) {
		if (unAllowedPremiumFeatures != null && unAllowedPremiumFeatures.size() > 0) {
			Object[] parameters = unAllowedPremiumFeatures.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
			
			List<String> foundUnAllowedPremiumFeatures_Admin = select(logger, "SELECT security_token FROM admin_permission_tbl WHERE security_token IN (" + AgnUtils.repeatString("?", unAllowedPremiumFeatures.size(), ", ") + ")", new StringRowMapper(), parameters);
			
			int touchedLines1 = update(logger, "DELETE FROM admin_permission_tbl WHERE security_token IN (" + AgnUtils.repeatString("?", unAllowedPremiumFeatures.size(), ", ") + ")", parameters);
			if (touchedLines1 > 0) {
				logger.error("Deleted unallowed premium features for admins: " + touchedLines1);
				logger.error(StringUtils.join(foundUnAllowedPremiumFeatures_Admin, ", "));
			}
			
			List<String> foundUnAllowedPremiumFeatures_Group = select(logger, "SELECT security_token FROM admin_group_permission_tbl WHERE security_token IN (" + AgnUtils.repeatString("?", unAllowedPremiumFeatures.size(), ", ") + ")", new StringRowMapper(), parameters);
			
			int touchedLines2 = update(logger, "DELETE FROM admin_group_permission_tbl WHERE security_token IN (" + AgnUtils.repeatString("?", unAllowedPremiumFeatures.size(), ", ") + ")", parameters);
			if (touchedLines2 > 0) {
				logger.error("Deleted unallowed premium features for admingroups: " + touchedLines2);
				logger.error(StringUtils.join(foundUnAllowedPremiumFeatures_Group, ", "));
			}
		}
	}

	@Override
	public boolean updateNewsDate(final int adminID, Date newsDate, NewsType type) {
		String columnName = type.name().toLowerCase() + "_date";
		return update(logger, "UPDATE admin_tbl SET " + columnName + " = ? WHERE ADMIN_ID = ?", newsDate, adminID) == 1;
	}

	@Override
	public String getAdminTimezone(int adminId, @VelocityCheck int companyId) {
		String sqlGetAdminTimezone = "SELECT admin_timezone FROM admin_tbl WHERE admin_id = ? AND company_id = ?";
		return selectWithDefaultValue(logger, sqlGetAdminTimezone, String.class, null, adminId, companyId);
	}

	@Override
	public ComAdmin getOldestAdminOfCompany(int companyID) {
		if (companyID == 0) {
			return null;
		} else {
			return selectObjectDefaultNull(logger,
				"SELECT admin_id, username, fullname, firstname, company_id, company_name, email, stat_email, secure_password_hash, creation_date, pwdchange_date,"
					+ " admin_country, admin_lang, admin_lang_variant, admin_timezone, layout_base_id, default_import_profile_id, timestamp,"
					+ " gender, title, news_date, message_date, is_one_time_pass, phone_number"
				+ " FROM admin_tbl WHERE company_id = ? AND admin_id = (SELECT MIN(admin_id) FROM admin_tbl WHERE company_id = ?)",
					getAdminRowMapper(), companyID, companyID);
		}
	}

	@Override
	public boolean checkBlacklistedAdminNames(String username) {
		return selectInt(logger, "SELECT COUNT(*) FROM admin_blacklist_tbl WHERE username = ?", username) > 0;
	}

	protected Collection<String> getAdditionalExtendedColumns() {
		return CollectionUtils.emptyCollection();
	}

	protected Collection<Object> getAdditionalExtendedParams(final ComAdmin admin) {
		return CollectionUtils.emptyCollection();
	}

	protected RowMapper<ComAdmin> getAdminRowMapper() {
		return adminRowMapper;
	}
}
