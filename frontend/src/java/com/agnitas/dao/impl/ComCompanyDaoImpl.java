/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.ComCompany;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.impl.ComCompanyImpl;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.company.bean.CompanyEntry;
import com.agnitas.emm.core.company.rowmapper.CompanyEntryRowMapper;
import com.agnitas.emm.core.recipient.dao.BindingHistoryDao;
import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.Company;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.TrackableLink;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.impl.OnepixelDaoImpl;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.Tuple;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

public class ComCompanyDaoImpl extends PaginatedBaseDaoImpl implements ComCompanyDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComCompanyDaoImpl.class);

	private static final int DEFAULT_EXPIRATION_DAYS = 1100;
	
	private static final int DEFAULT_RECIPIENT_EXPIRATION_DAYS = 30;

	private static final String COMPANY_STATUS_ACTIVE = "active";

	/** Configuration service. */
	private ConfigService configService;
	
	private EmmActionService emmActionService;
	
	/** Mailing DAO. */
	private ComMailingDao mailingDao;

	private ComTargetDao targetDao;
	
	private CopyMailingService copyMailingService;
	
	private BindingHistoryDao bindingHistoryDao;
	
	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}

	@Required
	public void setEmmActionService(EmmActionService emmActionService) {
		this.emmActionService = emmActionService;
	}

	@Required
	public void setCopyMailingService(CopyMailingService copyMailingService) {
		this.copyMailingService = copyMailingService;
	}

	@Required
	public void setBindingHistoryDao(BindingHistoryDao bindingHistoryDao) {
		this.bindingHistoryDao = bindingHistoryDao;
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic

	private static final String[] CLICK_STAT_COLORS = {"F4F9FF", "D5E6FF", "E1F7E1", "FEFECC", "FFE4BA", "FFCBC3"};
	private static final int[] CLICK_STAT_RANGES = {5, 5, 5, 5, 5, 75}; // Sum must be 100
	
	// Standard Customer-Table fields
	public static final String STANDARD_FIELD_CUSTOMER_ID = "customer_id";
	public static final String STANDARD_FIELD_EMAIL = "email";
	public static final String STANDARD_FIELD_FIRSTNAME = "firstname";
	public static final String STANDARD_FIELD_LASTNAME = "lastname";
	public static final String STANDARD_FIELD_TITLE = "title";
	public static final String STANDARD_FIELD_GENDER = "gender";
	public static final String STANDARD_FIELD_MAILTYPE = "mailtype";
	public static final String STANDARD_FIELD_TIMESTAMP = "timestamp";
	public static final String STANDARD_FIELD_CREATION_DATE = "creation_date";
	public static final String STANDARD_FIELD_DATASOURCE_ID = "datasource_id";
	public static final String STANDARD_FIELD_LASTOPEN_DATE = "lastopen_date";
	public static final String STANDARD_FIELD_LASTCLICK_DATE = "lastclick_date";
	public static final String STANDARD_FIELD_LASTSEND_DATE = "lastsend_date";
	public static final String STANDARD_FIELD_LATEST_DATASOURCE_ID = "latest_datasource_id";
	public static final String STANDARD_FIELD_DO_NOT_TRACK = "sys_tracking_veto";
	public static final String STANDARD_FIELD_CLEANED_DATE = "cleaned_date";

	private static final String TABLESPACE_CUSTOMER_HISTORY = "customer_history";
	private static final String TABLESPACE_CUSTOMER_HISTORY_INDEX = "index_customer_history";
	private static final String TABLESPACE_DATA_WAREHOUSE = "data_warehouse";
	private static final String TABLESPACE_DATA_WAREHOUSE_INDEX = "index_data_warehouse";
	private static final String TABLESPACE_DATA_SUCCESS = "data_success";
	private static final String TABLESPACE_INDEX_SUCCESS = "index_success";
	private static final String TABLESPACE_DATA_CUSTOMER_TABLE = "data_cust_table";
	private static final String TABLESPACE_INDEX_CUSTOMER = "data_cust_index";
	
	/**
	 * DB field for AGNEMM-1817, AGNEMM-1924 and AGNEMM-1925
	 */
	public static final String STANDARD_FIELD_BOUNCELOAD = "bounceload";
	
	public static final String[] STANDARD_CUSTOMER_FIELDS = new String[]{
		STANDARD_FIELD_CUSTOMER_ID,
		STANDARD_FIELD_EMAIL,
		STANDARD_FIELD_FIRSTNAME,
		STANDARD_FIELD_LASTNAME,
		STANDARD_FIELD_TITLE,
		STANDARD_FIELD_GENDER,
		STANDARD_FIELD_MAILTYPE,
		STANDARD_FIELD_TIMESTAMP,
		STANDARD_FIELD_CREATION_DATE,
		STANDARD_FIELD_DATASOURCE_ID,
		STANDARD_FIELD_LASTOPEN_DATE,
		STANDARD_FIELD_LASTCLICK_DATE,
		STANDARD_FIELD_LASTSEND_DATE,
		STANDARD_FIELD_LATEST_DATASOURCE_ID,
		STANDARD_FIELD_DO_NOT_TRACK,
		STANDARD_FIELD_CLEANED_DATE,
		STANDARD_FIELD_BOUNCELOAD};
	
	/**
	 * Socialmedia fields to be ignored in limit checks for profile field counts until they are removed entirely in all client tables
	 */
	public static final String[] OLD_SOCIAL_MEDIA_FIELDS = new String[]{
		"facebook_status",
		"foursquare_status",
		"google_status",
		"twitter_status",
		"xing_status"};
	
	public static final String[] GUI_BULK_IMMUTABALE_FIELDS = new String[] {
		STANDARD_FIELD_CUSTOMER_ID,
		STANDARD_FIELD_GENDER,
		STANDARD_FIELD_TITLE,
		STANDARD_FIELD_FIRSTNAME,
		STANDARD_FIELD_LASTNAME,
		STANDARD_FIELD_EMAIL,
		STANDARD_FIELD_DATASOURCE_ID,
		STANDARD_FIELD_BOUNCELOAD,
		STANDARD_FIELD_LASTOPEN_DATE,
		STANDARD_FIELD_LASTCLICK_DATE,
		STANDARD_FIELD_LASTSEND_DATE,
		STANDARD_FIELD_LATEST_DATASOURCE_ID,
		STANDARD_FIELD_TIMESTAMP,
		STANDARD_FIELD_CLEANED_DATE,
		STANDARD_FIELD_CREATION_DATE};
	
	public static final String[] CLEAN_IMMUTABALE_FIELDS = new String[] {
		STANDARD_FIELD_CUSTOMER_ID,
		STANDARD_FIELD_EMAIL,
		STANDARD_FIELD_DATASOURCE_ID,
		STANDARD_FIELD_CREATION_DATE,
		STANDARD_FIELD_BOUNCELOAD,
		STANDARD_FIELD_CLEANED_DATE,
		STANDARD_FIELD_TIMESTAMP};
	
	@Override
	public ComCompany getCompany(@VelocityCheck int companyID) {
		if (companyID == 0) {
			return null;
		} else {
			try {
				String sql = "SELECT company_id, creator_company_id, shortname, description, rdir_domain, mailloop_domain, status, mailtracking, expire_stat, stat_admin, expire_onepixel, expire_success, expire_cookie, expire_upload, max_login_fails, login_block_time, secret_key, uid_version, auto_mailing_report_active, sector, business_field, expire_recipient, max_recipients, salutation_extended, enabled_uid_version, export_notify, parent_company_id, expire_bounce, contact_tech FROM company_tbl WHERE company_id = ?";
				List<ComCompany> list = select(logger, sql, new ComCompany_RowMapper(), companyID);
				if (list.size() > 0) {
					return list.get(0);
				} else {
					return null;
				}
			} catch (Exception e) {
				throw new RuntimeException("Cannot read company data for companyid: " + companyID, e);
			}
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void saveCompany(Company company) throws Exception {
		ComCompany comCompany = (ComCompany) company;
		try {
			List<Map<String, Object>> list = select(logger, "SELECT company_id FROM company_tbl WHERE company_id = ?", comCompany.getId());
			
			// Manage default value for ExpireStat
			if (comCompany.getExpireStat() <= 0) {
				comCompany.setExpireStat(configService.getIntegerValue(ConfigValue.ExpireStatisticsDefault));
				if (comCompany.getExpireStat() <= 0) {
					// Fallback if no default is set, just to be sure
					comCompany.setExpireStat(DEFAULT_EXPIRATION_DAYS);
				}
			}

			// Manage default value for ExpireOnePixel
			if (comCompany.getExpireOnePixel() <= 0) {
				comCompany.setExpireOnePixel(configService.getIntegerValue(ConfigValue.ExpireOnePixelDefault));
				if (comCompany.getExpireOnePixel() <= 0) {
					// Fallback if no default is set, just to be sure
					comCompany.setExpireOnePixel(DEFAULT_EXPIRATION_DAYS);
				}
			}
			
			// Manage default value for ExpireSuccess
			if (comCompany.getExpireSuccess() <= 0) {
				comCompany.setExpireSuccess(configService.getIntegerValue(ConfigValue.ExpireSuccessDefault));
				if (comCompany.getExpireSuccess() <= 0) {
					// Fallback if no default is set, just to be sure
					comCompany.setExpireSuccess(DEFAULT_EXPIRATION_DAYS);
				}
			}
			
			// Manage default value for maxFields
			if (comCompany.getMaxFields() <= 0) {
				comCompany.setMaxFields(configService.getIntegerValue(ConfigValue.MaxFields));
			}
			
			// Manage default value for ExpireRecipient
			if (comCompany.getExpireRecipient() <= 0) {
				comCompany.setExpireRecipient(DEFAULT_RECIPIENT_EXPIRATION_DAYS);
			}
			
			if (list.size() > 0) {
				String sql = "UPDATE company_tbl SET creator_company_id = ?, shortname = ?, description = ?, " +
						" rdir_domain = ?, mailloop_domain = ?, status = ?, mailtracking = ?, expire_stat = ?, " +
						" stat_admin = ?, expire_onepixel = ?, expire_success = ?, expire_upload = ?, " +
						" max_login_fails = ?, login_block_time = ?, sector = ?, business_field = ?, max_recipients = ?, " +
						" salutation_extended = ?, export_notify = ?, expire_bounce = ?, contact_tech = ?, expire_recipient = ? " +
						" WHERE company_id = ?";
				update(logger, sql,
						comCompany.getCreatorID(),
						comCompany.getShortname(),
						comCompany.getDescription(),
						comCompany.getRdirDomain(),
						comCompany.getMailloopDomain(),
						comCompany.getStatus(),
						comCompany.getMailtracking(),
						comCompany.getExpireStat(),
						comCompany.getStatAdmin(),
						comCompany.getExpireOnePixel(),
						comCompany.getExpireSuccess(),
						comCompany.getExpireUpload(),
						comCompany.getMaxLoginFails(),
						comCompany.getLoginBlockTime(),
						comCompany.getSector(),
						comCompany.getBusiness(),
						comCompany.getMaxRecipients(),
						comCompany.getSalutationExtended(),
						comCompany.getExportNotifyAdmin(),
						comCompany.getExpireBounce(),
						StringUtils.defaultString(comCompany.getContactTech()),
						comCompany.getExpireRecipient(),
						comCompany.getId());
			} else {
				int defaultDatasourceID = createNewDefaultdatasourceID();
				
				int newCompanyID;
				
				if (isOracleDB()) {
					newCompanyID = selectInt(logger, "SELECT company_tbl_seq.NEXTVAL FROM DUAL");
					List<String> fieldList =
							Arrays.asList("company_id", "creator_company_id", "shortname", "description",
									"rdir_domain", "mailloop_domain", "status",
									"mailtracking", "expire_stat", "stat_admin", "expire_onepixel",
									"expire_success", "max_fields", "expire_cookie", "max_login_fails",
									"login_block_time", "sector", "business_field", "max_recipients", "secret_key",
									"salutation_extended", "enabled_uid_version", "maxadminmails", "export_notify",
									"default_datasource_id", "expire_bounce", "contact_tech", "expire_recipient");

					String sql = "INSERT INTO company_tbl (" + StringUtils.join(fieldList, ", ") + ")"
						+ " VALUES (?" + StringUtils.repeat(", ?", fieldList.size() - 1) + ")";
					
					update(logger, sql,
						newCompanyID,
						comCompany.getCreatorID(),
						comCompany.getShortname(),
						comCompany.getDescription(),
						comCompany.getRdirDomain(),
						comCompany.getMailloopDomain(),
						comCompany.getStatus(),
						comCompany.getMailtracking(),
						comCompany.getExpireStat(),
						comCompany.getStatAdmin(),
						comCompany.getExpireOnePixel(),
						comCompany.getExpireSuccess(),
						comCompany.getMaxFields(),
						comCompany.getExpireCookie(),
						comCompany.getMaxLoginFails(),
						comCompany.getLoginBlockTime(),
						comCompany.getSector(),
						comCompany.getBusiness(),
						comCompany.getMaxRecipients(),
						comCompany.getSecretKey(),
						comCompany.getSalutationExtended(),
						comCompany.getEnabledUIDVersion(),
						comCompany.getMaxAdminMails(),
						comCompany.getExportNotifyAdmin(),
						defaultDatasourceID,
						comCompany.getExpireBounce(),
						StringUtils.defaultString(comCompany.getContactTech()),
						comCompany.getExpireRecipient());
				} else {
					List<String> creationFieldList =
							Arrays.asList("creation_date", "creator_company_id", "shortname", "description",
									"rdir_domain", "mailloop_domain", "status", "mailtracking", "expire_stat",
									"stat_admin", "expire_onepixel", "expire_success", "max_fields", "expire_cookie",
									"max_login_fails","login_block_time", "sector", "business_field",
									"max_recipients", "secret_key", "salutation_extended", "enabled_uid_version",
									"maxadminmails", "export_notify", "default_datasource_id", "expire_bounce",
									"contact_tech", "expire_recipient");
					
					String insertStatementSql = "INSERT INTO company_tbl (" + StringUtils.join(creationFieldList, ", ") + ")"
							+ " VALUES (CURRENT_TIMESTAMP" + StringUtils.repeat(", ?", creationFieldList.size() - 1)+ ")";
					
					newCompanyID = insertIntoAutoincrementMysqlTable(logger, "company_id", insertStatementSql,
						comCompany.getCreatorID(),
						comCompany.getShortname(),
						comCompany.getDescription(),
						comCompany.getRdirDomain(),
						comCompany.getMailloopDomain(),
						comCompany.getStatus(),
						comCompany.getMailtracking(),
						comCompany.getExpireStat(),
						comCompany.getStatAdmin(),
						comCompany.getExpireOnePixel(),
						comCompany.getExpireSuccess(),
						comCompany.getMaxFields(),
						comCompany.getExpireCookie(),
						comCompany.getMaxLoginFails(),
						comCompany.getLoginBlockTime(),
						comCompany.getSector(),
						comCompany.getBusiness(),
						comCompany.getMaxRecipients(),
						comCompany.getSecretKey(),
						comCompany.getSalutationExtended(),
						comCompany.getEnabledUIDVersion(),
						comCompany.getMaxAdminMails(),
						comCompany.getExportNotifyAdmin(),
						defaultDatasourceID,
						comCompany.getExpireBounce(),
						StringUtils.defaultString(comCompany.getContactTech()),
						comCompany.getExpireRecipient());
				}

				comCompany.setId(newCompanyID);
				
				updateDefaultdatasourceIDWithNewCompanyid(defaultDatasourceID, newCompanyID);
			}
		} catch (Exception e) {
			logger.error("Cannot save company data", e);
			throw new Exception("Cannot store company data: " + e.getMessage(), e);
		}
	}

	/**
	 * Because the companyid is not aquired yet we use 1 (always existing company of emm-master) as interim companyid and update this later
	 *
	 * @return
	 */
	@DaoUpdateReturnValueCheck
	private int createNewDefaultdatasourceID() {
		int sourceGroupID = selectInt(logger, "SELECT sourcegroup_id FROM sourcegroup_tbl WHERE sourcegroup_type = ?", "DD");
		
		if (isOracleDB()) {
			int newDatasourceID = selectInt(logger, "SELECT datasource_description_tbl_seq.NEXTVAL FROM DUAL");
			update(logger, "INSERT INTO datasource_description_tbl (datasource_id, description, company_id, sourcegroup_id) VALUES (?, ?, ?, ?)",
				newDatasourceID, "Default Datasource", 1, sourceGroupID);
			return newDatasourceID;
		} else {
			int newDatasourceID = insertIntoAutoincrementMysqlTable(logger, "datasource_id", "INSERT INTO datasource_description_tbl (description, company_id, sourcegroup_id) VALUES (?, ?, ?)",
				"Default Datasource", 1, sourceGroupID);
			return newDatasourceID;
		}
	}

	/**
	 * Because the companyid was not aquired we used -1 as interim companyid and now we update this here with the new companyid
	 */
	@DaoUpdateReturnValueCheck
	private void updateDefaultdatasourceIDWithNewCompanyid(int datasourceID, int companyID) {
		update(logger, "UPDATE datasource_description_tbl SET company_id = ? WHERE datasource_id = ?", companyID, datasourceID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteCompany(Company comp) {
		String sql = "UPDATE company_tbl SET status = ?, timestamp = CURRENT_TIMESTAMP WHERE company_id = ?";
		update(logger, sql, "todelete", comp.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteCompany(int companyID) {
		String sql = "UPDATE company_tbl SET status = ?, timestamp = CURRENT_TIMESTAMP WHERE company_id = ?";
		update(logger, sql, "deleted", companyID);
	}
	
	@Override
	public void updateCompanyStatus(int companyID, String status) {
		String sql = "UPDATE company_tbl SET status = ?, timestamp = CURRENT_TIMESTAMP WHERE company_id = ?";
		update(logger, sql, status, companyID);
	}

	/**
	 * Use {@link ComCompanyDaoImpl#getCompanyListNew(int, String, String, int, int)} instead.
	 */
	@Deprecated
	@Override
	public PaginatedListImpl<CompanyEntry> getCompanyList(@VelocityCheck int companyID, String sortCriterion, String sortDirection, int pageNumber, int pageSize) {
		if (StringUtils.isBlank(sortCriterion)) {
			sortCriterion = "shortname";
		}
		
		String sortColumn = sortCriterion;
		
		boolean sortDirectionAscending = !"desc".equalsIgnoreCase(sortDirection) && !"descending".equalsIgnoreCase(sortDirection);
		
		PaginatedListImpl<CompanyEntry> paginatedList = selectPaginatedList(logger, "SELECT company_id, shortname, description FROM company_tbl WHERE (company_id = ? OR creator_company_id = ?) AND status = 'active'", "company_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, new CompanyEntryRowMapper(), companyID, companyID);
		paginatedList.setSortCriterion(sortCriterion);
		return paginatedList;
	}

	@Override
	public PaginatedListImpl<CompanyEntry> getCompanyListNew(@VelocityCheck int companyID, String sortCriterion, String sortDirection, int pageNumber, int pageSize) {
		if (StringUtils.isBlank(sortCriterion)) {
			sortCriterion = "shortname";
		}

		String sortColumn = sortCriterion;

		boolean sortDirectionAscending = !"desc".equalsIgnoreCase(sortDirection) && !"descending".equalsIgnoreCase(sortDirection);

		PaginatedListImpl<CompanyEntry> paginatedList = selectPaginatedList(logger, "SELECT company_id, shortname, description FROM company_tbl WHERE (company_id = ? OR creator_company_id = ?) AND status = 'active'", "company_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, new CompanyEntryRowMapper(), companyID, companyID);
		paginatedList.setSortCriterion(sortCriterion);
		return paginatedList;
	}
	
	@Override
	public List<CompanyEntry> getActiveCompaniesLight() {
		return select(logger, "SELECT company_id, shortname, description FROM company_tbl WHERE status = 'active' ORDER BY LOWER(shortname)", new CompanyEntryRowMapper());
	}
	
	@Override
	public List<CompanyEntry> getActiveOwnCompaniesLight(@VelocityCheck int companyId) {
		return select(logger, "SELECT company_id, shortname, description FROM company_tbl WHERE status = 'active' AND (company_id = ? OR creator_company_id = ?) ORDER BY LOWER(shortname)", new CompanyEntryRowMapper(), companyId, companyId);
	}

	@Override
	public CompanyEntry getCompanyLight(int id) {
		return select(logger, "SELECT company_id, shortname, description FROM company_tbl WHERE company_id = ?", new CompanyEntryRowMapper(), id).get(0);
	}

	@Override
	public List<ComCompany> getCreatedCompanies(@VelocityCheck int companyId) {
		try {
			return select(logger, "SELECT company_id, creator_company_id, shortname, description, rdir_domain, mailloop_domain, status, mailtracking, expire_stat, stat_admin, expire_onepixel, expire_success, expire_cookie, expire_upload, max_login_fails, login_block_time, secret_key, uid_version, auto_mailing_report_active, sector, business_field, expire_recipient, max_recipients, salutation_extended, enabled_uid_version, export_notify, parent_company_id, expire_bounce, contact_tech FROM company_tbl WHERE (creator_company_id = ? OR company_id = ?) AND status = 'active' ORDER BY LOWER (shortname)", new ComCompany_RowMapper(), companyId, companyId);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean initTables(@VelocityCheck int newCompanyId) {
		if (newCompanyId <= 0) {
			return false;
		}
		
		int mailinglistID = 0;
		String sql = "";

		try {
			if (isOracleDB()) {
				String tablespaceClauseCustomerTable = "";
				if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_DATA_CUSTOMER_TABLE)) {
					tablespaceClauseCustomerTable = " TABLESPACE " + TABLESPACE_DATA_CUSTOMER_TABLE;
				}
				
				String tablespaceClauseDataWarehouse = "";
				if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_DATA_WAREHOUSE)) {
					tablespaceClauseDataWarehouse = " TABLESPACE " + TABLESPACE_DATA_WAREHOUSE;
				}
				
				String tablespaceClauseDataWarehouseIndex = "";
				if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_DATA_WAREHOUSE_INDEX)) {
					tablespaceClauseDataWarehouseIndex = " TABLESPACE " + TABLESPACE_DATA_WAREHOUSE_INDEX;
				}

				String tablespaceClauseDataSuccess = "";
				if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_DATA_SUCCESS)) {
					tablespaceClauseDataSuccess = " TABLESPACE " + TABLESPACE_DATA_SUCCESS;
				}
				
				String tablespaceClauseIndexSuccess = "";
				if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_INDEX_SUCCESS)) {
					tablespaceClauseIndexSuccess = " TABLESPACE " + TABLESPACE_INDEX_SUCCESS;
				}
				
				String tablespaceClauseCustomerBindIndex = "";
				if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_INDEX_CUSTOMER)) {
					tablespaceClauseCustomerBindIndex = " TABLESPACE " + TABLESPACE_INDEX_CUSTOMER;
				}
				
				initCustomerTables(newCompanyId);
				
				sql = "CREATE TABLE interval_track_" + newCompanyId + "_tbl (customer_id NUMBER NOT NULL, mailing_id NUMBER NOT NULL, send_date TIMESTAMP NOT NULL)" + tablespaceClauseCustomerTable;
				execute(logger, sql);
				sql = "CREATE INDEX intervtrack$" + newCompanyId + "mid$idx ON interval_track_" + newCompanyId + "_tbl (mailing_id)" + tablespaceClauseCustomerBindIndex;
				execute(logger, sql);
				
				sql = "CREATE TABLE mailtrack_" + newCompanyId + "_tbl (mailing_id NUMBER, maildrop_status_id NUMBER, customer_id NUMBER, timestamp date default SYSDATE)" + tablespaceClauseCustomerTable;
				execute(logger, sql);
				sql = "ALTER TABLE mailtrack_" + newCompanyId + "_tbl ADD CONSTRAINT mt" + newCompanyId + "$cuid$nn CHECK (customer_id IS NOT NULL)";
				execute(logger, sql);
				sql = "ALTER TABLE mailtrack_" + newCompanyId + "_tbl ADD CONSTRAINT mt" + newCompanyId + "$mdsid$nn CHECK (maildrop_status_id IS NOT NULL)";
				execute(logger, sql);
				sql = "CREATE INDEX mailtr" + newCompanyId + "$cid$idx ON mailtrack_" + newCompanyId + "_tbl (customer_id)" + tablespaceClauseCustomerBindIndex;
				execute(logger, sql);
				sql = "CREATE INDEX mailtr" + newCompanyId + "$mid$idx ON mailtrack_" + newCompanyId + "_tbl (mailing_id)" + tablespaceClauseCustomerBindIndex;
				execute(logger, sql);
				
				sql = "CREATE TABLE " + OnepixelDaoImpl.getOnepixellogTableName(newCompanyId) + " (customer_id NUMBER NOT NULL, mailing_id NUMBER NOT NULL, company_id NUMBER NOT NULL, ip_adr VARCHAR2(15) NOT NULL, timestamp DATE DEFAULT SYSDATE, open_count NUMBER, mobile_count NUMBER, first_open TIMESTAMP, last_open TIMESTAMP)" + tablespaceClauseCustomerTable;
				execute(logger, sql);
				sql = "CREATE INDEX onepix" + newCompanyId + "$mlid_cuid$idx ON " + OnepixelDaoImpl.getOnepixellogTableName(newCompanyId) + " (mailing_id, customer_id)" + tablespaceClauseCustomerBindIndex;
				execute(logger, sql);

				sql = "CREATE TABLE " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (company_id NUMBER NOT NULL, mailing_id NUMBER NOT NULL, customer_id NUMBER NOT NULL, device_class_id NUMBER NOT NULL, device_id NUMBER, creation TIMESTAMP, client_id INTEGER)" + tablespaceClauseDataWarehouse;
				execute(logger, sql);
				sql = "CREATE INDEX onedev" + newCompanyId + "$creat$idx ON " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (creation)" + tablespaceClauseDataWarehouseIndex;
				execute(logger, sql);
				sql = "CREATE INDEX onepixdev" + newCompanyId + "$mlid_cuid$idx ON " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (mailing_id, customer_id)" + tablespaceClauseDataWarehouseIndex;
				execute(logger, sql);
				sql = "CREATE INDEX onedev" + newCompanyId + "$ciddevclidmlid$idx ON " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (customer_id, device_class_id, mailing_id)" + tablespaceClauseDataWarehouseIndex;
				execute(logger, sql);
				
				sql = "CREATE TABLE rdirlog_" + newCompanyId + "_tbl (customer_id NUMBER NOT NULL, url_id NUMBER NOT NULL, company_id NUMBER NOT NULL, timestamp DATE DEFAULT SYSDATE, ip_adr VARCHAR2(15) NOT NULL, mailing_id NUMBER, device_class_id NUMBER NOT NULL, device_id NUMBER, client_id INTEGER)" + tablespaceClauseCustomerTable;
				execute(logger, sql);
				sql = "CREATE INDEX rlog" + newCompanyId + "$mlid_urlid_cuid$idx ON rdirlog_" + newCompanyId + "_tbl (MAILING_ID, URL_ID, CUSTOMER_ID)" + tablespaceClauseCustomerBindIndex;
				execute(logger, sql);
				sql = "CREATE INDEX rlog" + newCompanyId + "$ciddevclidmlid$idx ON rdirlog_" + newCompanyId + "_tbl (customer_id, device_class_id, mailing_id)" + tablespaceClauseCustomerBindIndex;
				execute(logger, sql);
				sql = "CREATE INDEX rlog" + newCompanyId + "$tmst$idx ON rdirlog_" + newCompanyId + "_tbl (timestamp)" + tablespaceClauseCustomerBindIndex;
				execute(logger, sql);
				
				sql = "CREATE TABLE rdirlog_userform_" + newCompanyId + "_tbl (form_id NUMBER, customer_id NUMBER NULL, url_id NUMBER NOT NULL, company_id NUMBER NOT NULL, timestamp DATE DEFAULT SYSDATE, ip_adr VARCHAR2(15), mailing_id NUMBER, device_class_id NUMBER NOT NULL, device_id NUMBER, client_id INTEGER)" + tablespaceClauseCustomerTable;
				execute(logger, sql);
				sql = "CREATE INDEX rlogform" + newCompanyId + "$fid_urlid$idx ON rdirlog_userform_" + newCompanyId + "_tbl (form_id, url_id)" + tablespaceClauseCustomerBindIndex;
				execute(logger, sql);

				// Create success_xxx_tbl for this new company
				sql = "CREATE TABLE success_" + newCompanyId + "_tbl (customer_id NUMBER, mailing_id NUMBER, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)" + tablespaceClauseDataSuccess;
				execute(logger, sql);
				
				sql = "CREATE INDEX suc" + newCompanyId + "$mid$idx ON success_" + newCompanyId + "_tbl (mailing_id)" + tablespaceClauseIndexSuccess;
				execute(logger, sql);
				sql = "CREATE INDEX suc" + newCompanyId + "$cid$idx ON success_" + newCompanyId + "_tbl (customer_id)" + tablespaceClauseIndexSuccess;
				execute(logger, sql);
				sql = "CREATE INDEX suc" + newCompanyId + "$tmst$idx ON success_" + newCompanyId + "_tbl (timestamp)" + tablespaceClauseIndexSuccess;
				execute(logger, sql);
				
				sql = "CREATE TABLE rdir_traffic_amount_" + newCompanyId + "_tbl (mailing_id NUMBER, content_name VARCHAR2(3000), content_size NUMBER, demand_date TIMESTAMP)" + tablespaceClauseCustomerTable;
				execute(logger, sql);
				sql = "CREATE TABLE rdir_traffic_agr_" + newCompanyId + "_tbl (mailing_id NUMBER, content_name VARCHAR2(3000), content_size NUMBER, demand_date TIMESTAMP, amount NUMBER)" + tablespaceClauseCustomerTable;
				execute(logger, sql);
			} else {
				sql = "CREATE TABLE interval_track_" + newCompanyId + "_tbl (customer_id INT(11) NOT NULL, mailing_id INT(11) NOT NULL, send_date TIMESTAMP NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				execute(logger, sql);
				sql = "CREATE INDEX intervtrack$" + newCompanyId + "mid$idx ON interval_track_" + newCompanyId + "_tbl (mailing_id)";
				execute(logger, sql);
				
				initCustomerTables(newCompanyId);
				
				// Watch out: Mysql does not support check constraints
				sql = "CREATE TABLE mailtrack_" + newCompanyId + "_tbl (mailing_id INT(11), maildrop_status_id INT(11) NOT NULL, customer_id INT(11) NOT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				execute(logger, sql);
				sql = "CREATE INDEX mailtr" + newCompanyId + "$cid$idx ON mailtrack_" + newCompanyId + "_tbl (customer_id)";
				execute(logger, sql);
				sql = "CREATE INDEX mailtr" + newCompanyId + "$mid$idx ON mailtrack_" + newCompanyId + "_tbl (mailing_id)";
				execute(logger, sql);
				
				sql = "CREATE TABLE " + OnepixelDaoImpl.getOnepixellogTableName(newCompanyId) + " (customer_id INT(11) NOT NULL, mailing_id INT(11) NOT NULL, company_id INT(11) NOT NULL, ip_adr VARCHAR(15) NOT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, open_count INT(11), mobile_count INT(11), first_open TIMESTAMP NULL, last_open TIMESTAMP NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				execute(logger, sql);
				sql = "CREATE INDEX onepix" + newCompanyId + "$mlid_cuid$idx ON " + OnepixelDaoImpl.getOnepixellogTableName(newCompanyId) + " (mailing_id, customer_id)";
				execute(logger, sql);
				
				sql = "CREATE TABLE " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (company_id INT(11) NOT NULL, mailing_id INT(11) NOT NULL, customer_id INT(11) NOT NULL, device_id INT(11), device_class_id INT(2) NOT NULL, creation TIMESTAMP, client_id INT(11)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				execute(logger, sql);
				sql = "CREATE INDEX onedev" + newCompanyId + "$creat$idx ON " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (creation)";
				execute(logger, sql);
				sql = "CREATE INDEX onepixdev" + newCompanyId + "$mlid_cuid$idx ON " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (mailing_id, customer_id)";
				execute(logger, sql);
				sql = "CREATE INDEX onedev" + newCompanyId + "$ciddevclidmlid$idx ON " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (customer_id, device_class_id, mailing_id)";
				execute(logger, sql);
				
				sql = "CREATE TABLE rdirlog_" + newCompanyId + "_tbl (customer_id INT(11) NOT NULL, url_id INT(11) NOT NULL, company_id INT(11) NOT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, ip_adr VARCHAR(15) NOT NULL, mailing_id INT(11), device_class_id INT(2) NOT NULL, device_id INT(11), client_id INT(11)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				execute(logger, sql);
				sql = "CREATE INDEX rlog" + newCompanyId + "$mlid_urlid_cuid$idx ON rdirlog_" + newCompanyId + "_tbl (mailing_id, url_id, customer_id)";
				execute(logger, sql);
				sql = "CREATE INDEX rlog" + newCompanyId + "$$ciddevclidmlid$idx ON rdirlog_" + newCompanyId + "_tbl (customer_id, device_class_id, mailing_id)";
				execute(logger, sql);
				sql = "CREATE INDEX rlog" + newCompanyId + "$tmst$idx ON rdirlog_" + newCompanyId + "_tbl (timestamp)";
				execute(logger, sql);
				
				sql = "CREATE TABLE rdirlog_userform_" + newCompanyId + "_tbl (form_id INT(11), customer_id INT(11) NULL, url_id INT(11) NOT NULL, company_id INT(11) NOT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, ip_adr VARCHAR(15), mailing_id INT(11), device_class_id INT(2) NOT NULL, device_id INT(11), client_id INT(11)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				execute(logger, sql);
				sql = "CREATE INDEX rlogform" + newCompanyId + "$fid_urlid$idx ON rdirlog_userform_" + newCompanyId + "_tbl (form_id, url_id)";
				execute(logger, sql);
				
				// Create success_tbl_xxx for this new company
				sql = "CREATE TABLE success_" + newCompanyId + "_tbl (customer_id INT(11), mailing_id INT(11), timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				execute(logger, sql);
				sql = "CREATE INDEX suc" + newCompanyId + "$mid$idx ON success_" + newCompanyId + "_tbl (mailing_id)";
				execute(logger, sql);
				sql = "CREATE INDEX suc" + newCompanyId + "$cid$idx ON success_" + newCompanyId + "_tbl (customer_id)";
				execute(logger, sql);
				sql = "CREATE INDEX suc" + newCompanyId + "$tmst$idx ON success_" + newCompanyId + "_tbl (timestamp)";
				execute(logger, sql);
				
				sql = "CREATE TABLE rdir_traffic_amount_" + newCompanyId + "_tbl (mailing_id INTEGER, content_name VARCHAR(3000), content_size INTEGER, demand_date TIMESTAMP NULL)";
				execute(logger, sql);
				sql = "CREATE TABLE rdir_traffic_agr_" + newCompanyId + "_tbl (mailing_id INTEGER, content_name VARCHAR(3000), content_size INTEGER, demand_date TIMESTAMP NULL, amount INTEGER)";
				execute(logger, sql);
			}
			
			createRdirValNumTable(newCompanyId);
			
			//create historyBindingTbl and trigger for new company
			if (!createHistoryTables(newCompanyId)) {
				throw new Exception("Cannot create history tables");
			}

			if (isOracleDB()) {
				//init colors for heatmap
				sql = "INSERT INTO click_stat_colors_tbl (id, company_id, range_start, range_end, color) VALUES (click_stat_colors_tbl_seq.NEXTVAL, ?, ?, ?, ?)";
				int nextRangeStart = 0;
				for (int i = 0; i < CLICK_STAT_COLORS.length; i++) {
					update(logger, sql, newCompanyId, nextRangeStart, nextRangeStart + CLICK_STAT_RANGES[i], CLICK_STAT_COLORS[i]);
					nextRangeStart += CLICK_STAT_RANGES[i];
				}

				// create statistic table for mobile devices.
				String createCustHistoryTblSql = "CREATE TABLE cust_" +newCompanyId+ "_devicehistory_tbl (device_id NUMBER, customer_id NUMBER, mailing_id NUMBER, creation_date TIMESTAMP)";
				String createCustHistoryTblSeq = "CREATE SEQUENCE cust_"+ newCompanyId +"_devicehistory_seq START WITH 1 INCREMENT BY 1";
				try {
					execute(logger, createCustHistoryTblSql);
					execute(logger, createCustHistoryTblSeq);
				} catch (Exception e) {
					logger.error("Error creating devicehistory_tbl or sequence: " + e);
				}

				// Copy mailinglist
				int mailingListToCopyID = selectIntWithDefaultValue(logger, "SELECT MIN(mailinglist_id) FROM mailinglist_tbl WHERE company_id = 1 AND deleted = 0", 0);
				if (mailingListToCopyID > 0) {
					mailinglistID = selectInt(logger, "SELECT mailinglist_tbl_seq.NEXTVAL FROM DUAL");
					update(logger, "INSERT INTO mailinglist_tbl (mailinglist_id, shortname, description, company_id) (SELECT ?, shortname, description, ? FROM mailinglist_tbl WHERE company_id = 1 AND deleted = 0 AND mailinglist_id = ?)", mailinglistID, newCompanyId, mailingListToCopyID);
				}
				
				// Copy customer
				int customerToCopyID = selectIntWithDefaultValue(logger, "SELECT MIN(customer_id) FROM customer_1_tbl", 0);
				int newCustomerID = selectInt(logger, "SELECT customer_" + newCompanyId + "_tbl_seq.NEXTVAL FROM DUAL");
				if (customerToCopyID > 0) {
					sql = "INSERT INTO customer_" + newCompanyId + "_tbl (customer_id, gender, firstname, lastname, email, mailtype) (SELECT " + newCustomerID + ", gender, firstname, lastname, email, mailtype FROM customer_1_tbl WHERE customer_id = " + customerToCopyID + ")";
					update(logger, sql);
				} else {
					update(logger, "INSERT INTO customer_" + newCompanyId + "_tbl (customer_id, gender, bounceload, email, mailtype) VALUES(?, 2, 0, 'tester@agnitas.de', 1)", newCustomerID);
				}
				if (customerToCopyID > 0) {
					// Set binding for new customer
					sql = "INSERT INTO customer_" + newCompanyId + "_binding_tbl (mailinglist_id, mediatype, user_status, user_type, exit_mailing_id, customer_id) VALUES (" + mailinglistID + ", 0, 1, '" + UserType.Admin.getTypeCode() + "', 0, " + newCustomerID + ")";
					update(logger, sql);
				}
				
				// New customer
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (customer_id, gender, bounceload, email, mailtype) VALUES(customer_" + newCompanyId + "_tbl_seq.NEXTVAL, 2, 1, 'adam+" + newCompanyId + "@adamatis.eu', 1)";
				update(logger, sql);
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (customer_id, gender, bounceload, email, mailtype) VALUES(customer_" + newCompanyId + "_tbl_seq.NEXTVAL, 2, 1, 'eva+" + newCompanyId + "@adamatis.eu', 1)";
				update(logger, sql);
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (customer_id, gender, bounceload, email, mailtype) VALUES(customer_" + newCompanyId + "_tbl_seq.NEXTVAL, 2, 1, 'kain+" + newCompanyId + "@adamatis.eu', 1)";
				update(logger, sql);
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (customer_id, gender, bounceload, email, mailtype) VALUES(customer_" + newCompanyId + "_tbl_seq.NEXTVAL, 2, 1, 'abel+" + newCompanyId + "@adamatis.eu', 1)";
				update(logger, sql);

				// Default mediapool categories
				sql = "INSERT INTO grid_mediapool_category_tbl (company_id, category_id, shortname, hidden, description, in_floating_bar, translatable)"
					+ " VALUES (" + newCompanyId + ", grid_category_tbl_seq.nextval, 'grid.mediapool.category.generic', 0, '', 1, 1)";
				update(logger, sql);
				sql = "INSERT INTO grid_mediapool_category_tbl (company_id, category_id, shortname, hidden, description, in_floating_bar, translatable)"
					+ " VALUES (" + newCompanyId + ", grid_category_tbl_seq.nextval, 'grid.mediapool.category.editorial', 0, '', 1, 1)";
				update(logger, sql);
			} else {
				//init colors for heatmap
				sql = "INSERT INTO click_stat_colors_tbl (company_id, range_start, range_end, color) VALUES (?, ?, ?, ?)";
				int nextRangeStart = 0;
				for (int i = 0; i < CLICK_STAT_COLORS.length; i++) {
					update(logger, sql, newCompanyId, nextRangeStart, nextRangeStart + CLICK_STAT_RANGES[i], CLICK_STAT_COLORS[i]);
					nextRangeStart += CLICK_STAT_RANGES[i];
				}
				
				String createCustHistoryTblSql = "CREATE TABLE cust_" + newCompanyId + "_devicehistory_tbl (device_id INT(11) PRIMARY KEY AUTO_INCREMENT NOT NULL, customer_id INT(11), mailing_id INT(11), creation_date TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
				try {
					execute(logger, createCustHistoryTblSql);
				} catch (Exception e) {
					logger.error("Error creating devicehistory_tbl: " + e);
				}
				
				// Copy mailinglist
				int mailingListToCopyID = selectIntWithDefaultValue(logger, "SELECT MIN(mailinglist_id) FROM mailinglist_tbl WHERE company_id = 1 AND deleted = 0", 0);
				if (mailingListToCopyID > 0) {
					mailinglistID = insertIntoAutoincrementMysqlTable(logger, "mailinglist_id", "INSERT INTO mailinglist_tbl (shortname, description, company_id) (SELECT shortname, description, ? FROM mailinglist_tbl WHERE company_id = 1 AND deleted = 0 AND mailinglist_id = ?)", newCompanyId, mailingListToCopyID);
				}
				
				// Copy customer
				int customerToCopyID = selectIntWithDefaultValue(logger, "SELECT MIN(customer_id) FROM customer_1_tbl", 0);
				int newCustomerID = 0;
				if (customerToCopyID > 0) {
					newCustomerID = insertIntoAutoincrementMysqlTable(logger, "customer_id", "INSERT INTO customer_" + newCompanyId + "_tbl (gender, firstname, lastname, email, mailtype) (SELECT gender, firstname, lastname, email, mailtype FROM customer_1_tbl WHERE customer_id = ?)", customerToCopyID);
				} else {
					newCustomerID = insertIntoAutoincrementMysqlTable(logger, "customer_id", "INSERT INTO customer_" + newCompanyId + "_tbl (gender, bounceload, email, mailtype) VALUES (2, 0, 'tester@agnitas.de', 1)");
				}
				if (newCustomerID > 0) {
					// Set binding for new customer
					sql = "INSERT INTO customer_" + newCompanyId + "_binding_tbl (mailinglist_id, mediatype, user_status, user_type, exit_mailing_id, customer_id) VALUES (" + mailinglistID + ", 0, 1, '" + UserType.Admin.getTypeCode() + "', 0, " + newCustomerID + ")";
					update(logger, sql);
				}

				// New customer
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (gender, bounceload, email, mailtype) VALUES(2, 1, 'adam+" + newCompanyId + "@adamatis.eu', 1)";
				update(logger, sql);
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (gender, bounceload, email, mailtype) VALUES(2, 1, 'eva+" + newCompanyId + "@adamatis.eu', 1)";
				update(logger, sql);
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (gender, bounceload, email, mailtype) VALUES(2, 1, 'kain+" + newCompanyId + "@adamatis.eu', 1)";
				update(logger, sql);
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (gender, bounceload, email, mailtype) VALUES(2, 1, 'abel+" + newCompanyId + "@adamatis.eu', 1)";
				update(logger, sql);

				// Default mediapool categories
				sql = "INSERT INTO grid_mediapool_category_tbl (company_id, shortname, hidden, description, in_floating_bar, translatable)"
					+ " VALUES (" + newCompanyId + ", 'grid.mediapool.category.generic', 0, '', 1, 1)";
				update(logger, sql);
				sql = "INSERT INTO grid_mediapool_category_tbl (company_id, shortname, hidden, description, in_floating_bar, translatable)"
					+ " VALUES (" + newCompanyId + ", 'grid.mediapool.category.editorial', 0, '', 1, 1)";
				update(logger, sql);
			}

			// All following actions do not use the db transaction, so it must be closed/commited in forehand to prevent deadlocks
			
			targetDao.createSampleTargetGroups(newCompanyId);

			copySampleMailings(newCompanyId, mailinglistID, getRedirectDomain(newCompanyId));

			// Copy sample form templates (some form actions need the sample mailings)
			for (int sampleFormID : getSampleFormIDs()) {
				try {
					// Copy start action for this form
					int copiedStartActionID = 0;
					int startActionID = selectInt(logger, "SELECT startaction_id FROM userform_tbl WHERE form_id = ?", sampleFormID);
					if (startActionID > 0) {
						Map<String,Object> actionResult = selectSingleRow(logger, "SELECT shortname, description, action_type FROM rdir_action_tbl WHERE action_id = ?", startActionID);
						String shortname = (String) actionResult.get("shortname");
						String description = (String) actionResult.get("description");
						int actiontype = ((Number) actionResult.get("action_type")).intValue();
						
						if (isOracleDB()) {
							copiedStartActionID = selectInt(logger, "SELECT rdir_action_tbl_seq.NEXTVAL FROM DUAL");
							update(logger, "INSERT INTO rdir_action_tbl (action_id, company_id, shortname, description, action_type) VALUES  (?, ?, ?, ?, ?)", copiedStartActionID, newCompanyId, shortname, description, actiontype);
						} else {
							copiedStartActionID = insertIntoAutoincrementMysqlTable(logger, "action_id", "INSERT INTO rdir_action_tbl (company_id, shortname, description, action_type) VALUES (?, ?, ?, ?)", newCompanyId, shortname, description, actiontype);
						}
						
						// Copy operations
						emmActionService.copyActionOperations(1, startActionID, newCompanyId, copiedStartActionID);
					}
	
					// Copy end action for this form
					int copiedEndActionID = 0;
					int endActionID = selectInt(logger, "SELECT endaction_id FROM userform_tbl WHERE form_id = ?", sampleFormID);
					if (endActionID > 0) {
						Map<String,Object> actionResult = selectSingleRow(logger, "SELECT shortname, description, action_type FROM rdir_action_tbl WHERE action_id = ?", endActionID);
						String shortname = (String) actionResult.get("shortname");
						String description = (String) actionResult.get("description");
						int actiontype = ((Number) actionResult.get("action_type")).intValue();
						
						if (isOracleDB()) {
							copiedEndActionID = selectInt(logger, "SELECT rdir_action_tbl_seq.NEXTVAL FROM DUAL");
							update(logger, "INSERT INTO rdir_action_tbl (action_id, company_id, shortname, description, action_type) VALUES (?, ?, ?, ?, ?)", copiedEndActionID, newCompanyId, shortname, description, actiontype);
						} else {
							copiedEndActionID = insertIntoAutoincrementMysqlTable(logger, "action_id", "INSERT INTO rdir_action_tbl (company_id, shortname, description, action_type) VALUES (?, ?, ?, ?)", newCompanyId, shortname, description, actiontype);
						}
						
						// Copy operations
						emmActionService.copyActionOperations(1, endActionID, newCompanyId, copiedEndActionID);
					}
					
					// Copy sample form
					Map<String,Object> formResult = selectSingleRow(logger, "SELECT formname, description, startaction_id, endaction_id, success_template, error_template FROM userform_tbl WHERE form_id = ?", sampleFormID);
					String formname = (String) formResult.get("formname");
					String description = (String) formResult.get("description");
					String successtemplate = (String) formResult.get("success_template");
					if (successtemplate != null) {
						successtemplate = successtemplate.replace("<CID>", Integer.toString(newCompanyId)).replace("<cid>", Integer.toString(newCompanyId));
						successtemplate = successtemplate.replace("[Company_id]", Integer.toString(newCompanyId)).replace("[company_id]", Integer.toString(newCompanyId));
						successtemplate = successtemplate.replace("<MLID>", Integer.toString(mailinglistID)).replace("<mlid>", Integer.toString(mailinglistID));
						successtemplate = successtemplate.replace("[Mailinglist_id]", Integer.toString(mailinglistID)).replace("[mailinglist_id]", Integer.toString(mailinglistID));
					}
					String errortemplate = (String) formResult.get("error_template");
					if (errortemplate != null) {
						errortemplate = errortemplate.replace("<CID>", Integer.toString(newCompanyId)).replace("<cid>", Integer.toString(newCompanyId));
						errortemplate = errortemplate.replace("[Company_id]", Integer.toString(newCompanyId)).replace("[company_id]", Integer.toString(newCompanyId));
						errortemplate = errortemplate.replace("<MLID>", Integer.toString(mailinglistID)).replace("<mlid>", Integer.toString(mailinglistID));
						errortemplate = errortemplate.replace("[Mailinglist_id]", Integer.toString(mailinglistID)).replace("[mailinglist_id]", Integer.toString(mailinglistID));
					}
					
					if (isOracleDB()) {
						update(logger, "INSERT INTO userform_tbl (form_id, company_id, formname, description, startaction_id, endaction_id, success_template, error_template) VALUES (userform_tbl_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)", newCompanyId, formname, description, copiedStartActionID, copiedEndActionID, successtemplate, errortemplate);
					} else {
						insertIntoAutoincrementMysqlTable(logger, "form_id", "INSERT INTO userform_tbl (company_id, formname, description, startaction_id, endaction_id, success_template, error_template) VALUES (?, ?, ?, ?, ?, ?, ?)", newCompanyId, formname, description, copiedStartActionID, copiedEndActionID, successtemplate, errortemplate);
					}
				} catch (Exception e) {
					logger.error("Could not copy user form (" + sampleFormID + ") for new company: " + e.getMessage(), e);
				}
			}

			return true;
		} catch (Exception e) {
			logger.error("initTables: SQL: " + sql + "\n" + e, e);

			return false;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void copySampleMailings(@VelocityCheck int newCompanyId, int mailinglistID, String rdirDomain) throws Exception {
		Map<Integer, Integer> mailingsMapping = new HashMap<>();
		doCopySampleMailings(newCompanyId, mailinglistID, rdirDomain, mailingsMapping);
	}
	
	protected void doCopySampleMailings(@VelocityCheck int newCompanyId, int mailinglistID, String rdirDomain, final Map<Integer, Integer> mailingsMapping) throws Exception {
		// Copy sample mailing templates
		for (int sampleMailingID : mailingDao.getSampleMailingIDs()) {
			int copiedMailingID = copyMailingService.copyMailing(1, sampleMailingID, newCompanyId, null, null);
			Mailing newMailing = mailingDao.getMailing(copiedMailingID, newCompanyId);
			newMailing.setMailinglistID(mailinglistID);
			
			for (MailingComponent component : newMailing.getComponents().values()) {
				String content = component.getEmmBlock();
				if (content != null) {
					content = content.replace("<CID>", Integer.toString(newCompanyId)).replace("<cid>", Integer.toString(newCompanyId));
					content = content.replace("[COMPANY_ID]", Integer.toString(newCompanyId)).replace("[company_id]", Integer.toString(newCompanyId)).replace("[Company_ID]", Integer.toString(newCompanyId));
					content = content.replace("<MID>", Integer.toString(newMailing.getId())).replace("<mid>", Integer.toString(newMailing.getId()));
					content = content.replace("<MLID>", Integer.toString(mailinglistID)).replace("<mlid>", Integer.toString(mailinglistID));
					content = content.replace("[MAILINGLIST_ID]", Integer.toString(mailinglistID)).replace("[mailinglist_id]", Integer.toString(mailinglistID)).replace("[Mailinglist_ID]", Integer.toString(mailinglistID));
					if (StringUtils.isNotBlank(rdirDomain)) {
						content = content.replace("<rdir-domain>", rdirDomain);
					} else {
						content = content.replace("<rdir-domain>", "RDIR-Domain");
					}
					component.setEmmBlock(content, "text/plain");
				}
			}

			for (DynamicTag dynamicTag : newMailing.getDynTags().values()) {
				for (DynamicTagContent dynamicTagContent : dynamicTag.getDynContent().values()) {
					String content = dynamicTagContent.getDynContent();
					if (content != null) {
						content = content.replace("<CID>", Integer.toString(newCompanyId)).replace("<cid>", Integer.toString(newCompanyId));
						content = content.replace("[COMPANY_ID]", Integer.toString(newCompanyId)).replace("[company_id]", Integer.toString(newCompanyId)).replace("[Company_ID]", Integer.toString(newCompanyId));
						content = content.replace("<MID>", Integer.toString(newMailing.getId())).replace("<mid>", Integer.toString(newMailing.getId()));
						content = content.replace("<MLID>", Integer.toString(mailinglistID)).replace("<mlid>", Integer.toString(mailinglistID));
						content = content.replace("[MAILINGLIST_ID]", Integer.toString(mailinglistID)).replace("[mailinglist_id]", Integer.toString(mailinglistID)).replace("[Mailinglist_ID]", Integer.toString(mailinglistID));
						if (StringUtils.isNotBlank(rdirDomain)) {
							content = content.replace("<rdir-domain>", rdirDomain);
						} else {
							content = content.replace("<rdir-domain>", "RDIR-Domain");
						}
						dynamicTagContent.setDynContent(content);
					}
				}
			}
			
			for (TrackableLink trackableLink : newMailing.getTrackableLinks().values()) {
				String content = trackableLink.getFullUrl();
				if (content != null) {
					content = content.replace("<CID>", Integer.toString(newCompanyId)).replace("<cid>", Integer.toString(newCompanyId));
					content = content.replace("[COMPANY_ID]", Integer.toString(newCompanyId)).replace("[company_id]", Integer.toString(newCompanyId)).replace("[Company_ID]", Integer.toString(newCompanyId));
					content = content.replace("<MID>", Integer.toString(newMailing.getId())).replace("<mid>", Integer.toString(newMailing.getId()));
					content = content.replace("<MLID>", Integer.toString(mailinglistID)).replace("<mlid>", Integer.toString(mailinglistID));
					content = content.replace("[MAILINGLIST_ID]", Integer.toString(mailinglistID)).replace("[mailinglist_id]", Integer.toString(mailinglistID)).replace("[Mailinglist_ID]", Integer.toString(mailinglistID));
					if (StringUtils.isNotBlank(rdirDomain)) {
						content = content.replace("<rdir-domain>", rdirDomain);
					} else {
						content = content.replace("<rdir-domain>", "RDIR-Domain");
					}
					trackableLink.setFullUrl(content);
				}
			}
			
			mailingDao.saveMailing(newMailing, false);
			mailingsMapping.put(sampleMailingID, newMailing.getId());
		}
	}

	@Override
	public void createRdirValNumTable(int newCompanyId) throws Exception {
		if (isOracleDB()) {
			String tablespaceClauseDataSuccess = "";
			if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_DATA_SUCCESS)) {
				tablespaceClauseDataSuccess = " TABLESPACE " + TABLESPACE_DATA_SUCCESS;
			}
	
			String tablespaceClauseCustomerBindIndex = "";
			if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_INDEX_CUSTOMER)) {
				tablespaceClauseCustomerBindIndex = " TABLESPACE " + TABLESPACE_INDEX_CUSTOMER;
			}
			
			String sql;
			// Create reveue tracking table for this new company
			sql = "CREATE TABLE rdirlog_" + newCompanyId + "_val_num_tbl (company_id NUMBER, customer_id NUMBER, ip_adr VARCHAR2(15), mailing_id NUMBER, session_id NUMBER, timestamp DATE DEFAULT CURRENT_TIMESTAMP, num_parameter NUMBER, page_tag VARCHAR(30))" + tablespaceClauseDataSuccess;
			execute(logger, sql);
			sql = "ALTER TABLE rdirlog_" + newCompanyId + "_val_num_tbl ADD CONSTRAINT rdvalnum" + newCompanyId + "$coid$nn CHECK (company_id IS NOT NULL)";
			execute(logger, sql);
			sql = "CREATE INDEX rvalnum" + newCompanyId + "$cod_cid_mid$idx ON rdirlog_" + newCompanyId + "_val_num_tbl (company_id, customer_id, mailing_id)" + tablespaceClauseCustomerBindIndex;
			execute(logger, sql);
			sql = "CREATE INDEX rvalnum" + newCompanyId + "$mid_pagetag$idx ON rdirlog_" + newCompanyId + "_val_num_tbl (mailing_id, page_tag)" + tablespaceClauseCustomerBindIndex;
			execute(logger, sql);
		} else {
			String sql;
			// Create reveue tracking table for this new company
			sql = "CREATE TABLE rdirlog_" + newCompanyId + "_val_num_tbl (company_id INT(11) NOT NULL, customer_id INT(11), ip_adr VARCHAR(15), mailing_id INT(11), session_id INT(11), `timestamp` timestamp DEFAULT CURRENT_TIMESTAMP, num_parameter DOUBLE, page_tag VARCHAR(30)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
			execute(logger, sql);
			sql = "ALTER TABLE rdirlog_" + newCompanyId + "_val_num_tbl ADD CONSTRAINT rdvalnum" + newCompanyId + "$coid$nn CHECK (company_id IS NOT NULL)";
			execute(logger, sql);
			sql = "CREATE INDEX rvalnum" + newCompanyId + "$cod_cid_mid$idx ON rdirlog_" + newCompanyId + "_val_num_tbl (company_id, customer_id, mailing_id)";
			execute(logger, sql);
			sql = "CREATE INDEX rvalnum" + newCompanyId + "$mid_pagetag$idx ON rdirlog_" + newCompanyId + "_val_num_tbl (mailing_id, page_tag)";
			execute(logger, sql);
		}
	}

	@Override
	public boolean initCustomerTables(int newCompanyId) throws SQLException {
		try {
			String sql;
			if (isOracleDB()) {
				String tablespaceClauseCustomer = "";
				String tablespaceCustomerContraint = "";
				String tablespaceClauseCustomerTable = "";
				if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_DATA_CUSTOMER_TABLE)) {
					tablespaceClauseCustomer = " TABLESPACE " + TABLESPACE_DATA_CUSTOMER_TABLE;
					tablespaceClauseCustomerTable = " TABLESPACE " + TABLESPACE_DATA_CUSTOMER_TABLE;
				}
				String tablespaceCustomerBindIndexIndividual = configService.getValue(ConfigValue.TablespacenameCustomerIndex);
				if (StringUtils.isBlank(tablespaceCustomerBindIndexIndividual)) {
					tablespaceCustomerBindIndexIndividual = TABLESPACE_INDEX_CUSTOMER;
				}
				String tablespaceClauseCustomerBindIndex = "";
				if (DbUtilities.checkOracleTablespaceExists(getDataSource(), tablespaceCustomerBindIndexIndividual)) {
					tablespaceClauseCustomerBindIndex = " TABLESPACE " + tablespaceCustomerBindIndexIndividual;
					tablespaceCustomerContraint = " USING INDEX TABLESPACE " + tablespaceCustomerBindIndexIndividual;
				}
				sql = "CREATE SEQUENCE customer_" + newCompanyId + "_tbl_seq start WITH 1000 INCREMENT BY 1 NOCACHE";
				execute(logger, sql);
				
				sql = "CREATE TABLE customer_" + newCompanyId + "_tbl ("
					+ STANDARD_FIELD_CUSTOMER_ID + " NUMBER, "
					+ STANDARD_FIELD_EMAIL + " VARCHAR2(100), "
					+ STANDARD_FIELD_FIRSTNAME + " VARCHAR2(100), "
					+ STANDARD_FIELD_LASTNAME + " VARCHAR2(100), "
					+ STANDARD_FIELD_TITLE + " VARCHAR2(100), "
					+ STANDARD_FIELD_GENDER + " NUMBER(1), "
					+ STANDARD_FIELD_MAILTYPE + " NUMBER(1), "
					+ STANDARD_FIELD_TIMESTAMP + " DATE DEFAULT SYSDATE, "
					+ STANDARD_FIELD_CREATION_DATE + " DATE DEFAULT SYSDATE, "
					+ STANDARD_FIELD_DATASOURCE_ID + " NUMBER, "
					+ STANDARD_FIELD_BOUNCELOAD + " NUMBER(1) DEFAULT 0 NOT NULL, "
					+ STANDARD_FIELD_LASTOPEN_DATE + " DATE, "
					+ STANDARD_FIELD_LASTCLICK_DATE + " DATE, "
					+ STANDARD_FIELD_LASTSEND_DATE + " DATE, "
					+ STANDARD_FIELD_LATEST_DATASOURCE_ID + " NUMBER, "
					+ STANDARD_FIELD_DO_NOT_TRACK + " NUMBER(1), "
					+ STANDARD_FIELD_CLEANED_DATE + " DATE "
					+ ")"
					+ tablespaceClauseCustomer;
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_tbl ADD CONSTRAINT cust" + newCompanyId + "$cid$pk PRIMARY KEY (customer_id)" + tablespaceCustomerContraint;
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_tbl ADD CONSTRAINT cust" + newCompanyId + "$email$nn CHECK (email IS NOT NULL)";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_tbl ADD CONSTRAINT cust" + newCompanyId + "$gender$ck CHECK (gender IN (0,1,2,3,4,5))";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_tbl ADD CONSTRAINT cust" + newCompanyId + "$gender$nn CHECK (gender IS NOT NULL)";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_tbl ADD CONSTRAINT cust" + newCompanyId + "$mailtype$ck CHECK (mailtype IN (0,1,2,4))";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_tbl ADD CONSTRAINT cust" + newCompanyId + "$mailtype$nn CHECK (mailtype IS NOT NULL)";
				execute(logger, sql);
				sql = "CREATE INDEX cust" + newCompanyId + "$email$idx ON customer_" + newCompanyId + "_tbl (email)" + tablespaceClauseCustomerBindIndex;
				execute(logger, sql);
				
				sql = "CREATE TABLE customer_" + newCompanyId + "_binding_tbl (customer_id NUMBER, mailinglist_id NUMBER, user_type CHAR(1), user_status NUMBER, user_remark VARCHAR2(150), timestamp DATE DEFAULT SYSDATE, creation_date DATE default SYSDATE, exit_mailing_id NUMBER, mediatype number DEFAULT 0, referrer VARCHAR2(4000))" + tablespaceClauseCustomer;
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$cid$fk FOREIGN KEY (customer_id) REFERENCES customer_" + newCompanyId + "_tbl (customer_id)";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$mid$fk FOREIGN KEY (mailinglist_id) REFERENCES mailinglist_tbl (mailinglist_id)";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$cid_mid_mt$pk PRIMARY KEY (customer_id, mailinglist_id, mediatype)" + tablespaceCustomerContraint;
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$cid$nn CHECK (customer_id IS NOT NULL)";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$mid$nn CHECK (mailinglist_id IS NOT NULL)";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$ustat$nn CHECK (user_status IS NOT NULL)";
				execute(logger, sql);
				sql = "ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$customer_" + newCompanyId + "_binding_tbl FOREIGN KEY (customer_id, mailinglist_id, mediatype) REFERENCES customer_" + newCompanyId + "_binding_tbl (customer_id, mailinglist_id, mediatype)";
				execute(logger, sql);
				sql = "CREATE INDEX cust" + newCompanyId + "b$tmst$idx ON customer_" + newCompanyId + "_binding_tbl (timestamp)" + tablespaceClauseCustomerBindIndex;
				execute(logger, sql);
				sql = "CREATE INDEX cust" + newCompanyId + "b$cuid_utype_mlid$idx ON customer_" + newCompanyId + "_binding_tbl (customer_id, user_type, mailinglist_id)" + tablespaceClauseCustomerBindIndex;
				execute(logger, sql);
				sql = "CREATE INDEX cust" + newCompanyId + "b$cuid_ustat_mlid$idx ON customer_" + newCompanyId + "_binding_tbl (customer_id, user_status, mailinglist_id)" + tablespaceClauseCustomerBindIndex;
				execute(logger, sql);
				
				// Oracle Standard Edition does not support bitmap index. But a simple index might be worse than no index because of the few different values of bounceload.
//				try {
//					sql = "CREATE BITMAP INDEX cust" + newCompanyId + "$bounceload$idx ON customer_" + newCompanyId + "_tbl (bounceload)" + tablespaceClauseCustomerBindIndex;
//					execute(logger, sql);
//				} catch (Exception e) {
//					logger.error("Cannot create bitmap index on bounceload: " + e.getMessage(), e);
//				}
				
	        	if (!DbUtilities.checkIfTableExists(getDataSource(), "cust" + newCompanyId + "_ban_tbl")) {
	        		sql = "CREATE TABLE cust" + newCompanyId + "_ban_tbl (email VARCHAR2(150) PRIMARY KEY, timestamp DATE DEFAULT SYSDATE)" + tablespaceClauseCustomerTable;
	        		execute(logger, sql);
	        		sql = "ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$cust" + newCompanyId + "_ban_tbl FOREIGN KEY (text) REFERENCES cust" + newCompanyId + "_ban_tbl (email)";
	        		execute(logger, sql);
	        	}
			} else {
				// Watch out: Mysql does not support check constraints
				sql = "CREATE TABLE customer_" + newCompanyId + "_tbl ("
					+ STANDARD_FIELD_CUSTOMER_ID + " INT(11) PRIMARY KEY AUTO_INCREMENT, "
					+ STANDARD_FIELD_EMAIL + " VARCHAR(100) NOT NULL, "
					+ STANDARD_FIELD_FIRSTNAME + " VARCHAR(100), "
					+ STANDARD_FIELD_LASTNAME + " VARCHAR(100), "
					+ STANDARD_FIELD_TITLE + " VARCHAR(100), "
					+ STANDARD_FIELD_GENDER + " INT(1) NOT NULL, "
					+ STANDARD_FIELD_MAILTYPE + " INT(1) NOT NULL, "
					+ STANDARD_FIELD_TIMESTAMP + " TIMESTAMP DEFAULT '2000-01-01 01:00:00', "
					+ STANDARD_FIELD_CREATION_DATE + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
					+ STANDARD_FIELD_DATASOURCE_ID + " INT(11), "
					+ STANDARD_FIELD_BOUNCELOAD + " INT(1) NOT NULL DEFAULT 0, "
					+ STANDARD_FIELD_LASTOPEN_DATE + " TIMESTAMP NULL, "
					+ STANDARD_FIELD_LASTCLICK_DATE + " TIMESTAMP NULL, "
					+ STANDARD_FIELD_LASTSEND_DATE + " TIMESTAMP NULL, "
					+ STANDARD_FIELD_LATEST_DATASOURCE_ID + " INT(11), "
					+ STANDARD_FIELD_DO_NOT_TRACK + " INT(1), "
					+ STANDARD_FIELD_CLEANED_DATE + " TIMESTAMP NULL "
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				execute(logger, sql);
				sql = "CREATE INDEX cust" + newCompanyId + "$email$idx ON customer_" + newCompanyId + "_tbl (email)";
				execute(logger, sql);
				
			 // Watch out for collation of user_type in customer_*_binding_tbl
				sql = "CREATE TABLE customer_" + newCompanyId + "_binding_tbl ("
					+ "customer_id INT(11) NOT NULL,"
					+ " mailinglist_id INT(11) UNSIGNED DEFAULT NULL,"
					+ " user_type CHAR(1) COLLATE utf8_bin,"
					+ " user_status INT(11) NOT NULL,"
					+ " user_remark VARCHAR(150),"
					+ " timestamp TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
					+ " creation_date TIMESTAMP NULL DEFAULT NULL,"
					+ " exit_mailing_id INT(11),"
					+ " mediatype INT(11) DEFAULT 0,"
					+ " referrer VARCHAR(4000)"
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$cid$fk FOREIGN KEY (customer_id) REFERENCES customer_" + newCompanyId + "_tbl (customer_id)";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$mid$fk FOREIGN KEY (mailinglist_id) REFERENCES mailinglist_tbl (mailinglist_id)";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$cid_mid_mt$pk PRIMARY KEY (customer_id, mailinglist_id, mediatype)";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$cid$nn CHECK (customer_id IS NOT NULL)";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$mid$nn CHECK (mailinglist_id IS NOT NULL)";
				execute(logger, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$ustat$nn CHECK (user_status IS NOT NULL)";
				execute(logger, sql);
				sql = "CREATE INDEX cust" + newCompanyId + "b$cuid_ustat_mlid$idx ON customer_" + newCompanyId + "_binding_tbl (customer_id, user_status, mailinglist_id)";
				execute(logger, sql);
				
				sql = "CREATE TABLE cust" + newCompanyId + "_ban_tbl (email VARCHAR(150) NOT NULL, timestamp timestamp DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (email)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				execute(logger, sql);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			return false;
		}
		return true;
	}
	
	@Override
	public boolean existTrackingTables(@VelocityCheck int companyID) {
		if (!DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_val_alpha_tbl")) {
			return false;
		} else if (!DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_ext_link_tbl")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * returns true, if mailtracking for this companyID is active.
	 */
	@Override
	public boolean isMailtrackingActive(@VelocityCheck int companyID) {
		try {
			return selectInt(logger, "SELECT mailtracking FROM company_tbl WHERE company_id = ?", companyID) == 1;
		} catch (Exception e) {
			logger.error("isMailtrackingActive goes wrong.", e);
			return false;
		}
	}

	@Override
	public int getSuccessDataExpirePeriod(@VelocityCheck int companyID) {
		boolean expireColumnAvailable = false;
		try {
			if (DbUtilities.checkTableAndColumnsExist(getDataSource(), "company_tbl", "expire_success")) {
				expireColumnAvailable = true;
			}
		} catch (Exception e) {
			// Do nothing
		}

		if (expireColumnAvailable) {
			return selectInt(logger, "SELECT expire_success FROM company_tbl WHERE company_id = ?", companyID);
		} else {
			return DEFAULT_EXPIRATION_DAYS;
		}
	}

	@Override
	public String getRedirectDomain(@VelocityCheck int companyId) {
		String sql = "SELECT rdir_domain FROM company_tbl WHERE company_id = ?";
		return selectObjectDefaultNull(logger, sql, (rs, index) -> rs.getString("rdir_domain"), companyId);
	}
	
	@Override
	public int getMaxAdminMails(@VelocityCheck int companyID) {
		return selectInt(logger, "SELECT maxadminmails FROM company_tbl WHERE company_id = ?", companyID);
	}

	@Override
	public boolean checkDeeptrackingAutoActivate(@VelocityCheck int companyID) {
		return selectInt(logger, "SELECT auto_deeptracking FROM company_tbl WHERE company_id = ?", companyID) == 1;
	}

	@Override
	public int getCompanyDatasource(@VelocityCheck int companyID) {
		return selectIntWithDefaultValue(logger, "SELECT default_datasource_id FROM company_tbl WHERE company_id = ?", -1, companyID);
	}

	/**
	 * This method gets a list with all NOT DELETED companys from our DB.
	 *
	 * @return
	 */
	@Override
	public List<ComCompany> getAllActiveCompaniesWithoutMasterCompany() {
		String sql = "SELECT company_id, creator_company_id, shortname, description, rdir_domain, mailloop_domain, status, mailtracking, expire_stat, stat_admin, expire_onepixel, expire_success, expire_cookie, expire_upload, max_login_fails, login_block_time, secret_key, uid_version, auto_mailing_report_active, sector, business_field, expire_recipient, max_recipients, salutation_extended, enabled_uid_version, export_notify, parent_company_id, expire_bounce, contact_tech FROM company_tbl WHERE status = 'active' AND company_id > 1 ORDER BY company_id";
		try {
			return select(logger, sql, new ComCompany_RowMapper());
		} catch (Exception e) {
			// return an empty list, for no further actions
			return new ArrayList<>();
		}
	}
	
	/**
	 * This method returns a list of all companies, with status ='active' starting from a given companyID
	 */
	@Override
	public List<ComCompany> getActiveCompaniesWithoutMasterCompanyFromStart(int startCompany) {
		String sql = "SELECT company_id, creator_company_id, shortname, description, rdir_domain, mailloop_domain, status, mailtracking, expire_stat, stat_admin, expire_onepixel, expire_success, expire_cookie, expire_upload, max_login_fails, login_block_time, secret_key, uid_version, auto_mailing_report_active, sector, business_field, expire_recipient, max_recipients, salutation_extended, enabled_uid_version, export_notify, parent_company_id, expire_bounce, contact_tech FROM company_tbl WHERE status = 'active' AND company_id >= ? ORDER BY company_id";
		try {
			return select(logger, sql, new ComCompany_RowMapper(), startCompany);
		} catch (Exception e) {
			// return an empty list, for no further actions
			return new ArrayList<>();
		}
	}

	@Override
	public List<Integer> getAllActiveCompaniesIdsWithoutMasterCompany() {
		return select(logger, "SELECT company_id from company_tbl WHERE status = 'active' AND company_id > 1", new IntegerRowMapper());
	}

	@Override
	public boolean createHistoryTables(int companyID) {
		if (!DbUtilities.checkIfTableExists(getDataSource(), "hst_customer_" + companyID + "_binding_tbl")) {

			String sql = "";
			try {
				if (isOracleDB()) {
					String tablespaceClause = "";
					if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_CUSTOMER_HISTORY)) {
						tablespaceClause = " TABLESPACE " + TABLESPACE_CUSTOMER_HISTORY;
					}
					sql = "CREATE TABLE hst_customer_" + companyID + "_binding_tbl "
							+ "("
							+ "customer_id NUMBER, "
							+ "mailinglist_id NUMBER, "
							+ "user_type CHAR(1), "
							+ "user_status NUMBER, "
							+ "user_remark VARCHAR2(150), "
							+ "timestamp DATE, "
							+ "creation_date DATE, "
							+ "exit_mailing_id NUMBER, "
							+ "mediatype NUMBER, "
							+ "change_type NUMBER, "
							+ "timestamp_change DATE, "
							+ "client_info VARCHAR2(150), "
							+ "email VARCHAR2(100), "
							+ "referrer VARCHAR2(4000)"
							+ ")"
							+ tablespaceClause;
					execute(logger, sql);

					String tablespaceClauseIndex = "";
					if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_CUSTOMER_HISTORY_INDEX)) {
						tablespaceClauseIndex = " TABLESPACE " + TABLESPACE_CUSTOMER_HISTORY_INDEX;
					}
					execute(logger, "CREATE INDEX hstcb" + companyID + "$email$idx ON hst_customer_" + companyID + "_binding_tbl (email)" + tablespaceClauseIndex);
					execute(logger, "CREATE INDEX hstcb" + companyID + "$mlidcidl$idx ON hst_customer_" + companyID + "_binding_tbl (mailinglist_id, customer_id)" + tablespaceClauseIndex);
					execute(logger, "CREATE INDEX hstcb" + companyID + "$tsch$idx ON hst_customer_" + companyID + "_binding_tbl (timestamp_change)" + tablespaceClauseIndex);
				} else {
					sql = "CREATE TABLE hst_customer_" + companyID + "_binding_tbl "
							+ "("
							+ "customer_id INT(11), "
							+ "mailinglist_id INT(11), "
							+ "user_type CHAR(1), "
							+ "user_status INT(11), "
							+ "user_remark VARCHAR(150), "
							+ "timestamp TIMESTAMP, "
							+ "creation_date TIMESTAMP, "
							+ "exit_mailing_id INT(11), "
							+ "mediatype INT(11), "
							+ "change_type INT(11), "
							+ "timestamp_change TIMESTAMP, "
							+ "client_info VARCHAR(150), "
							+ "email VARCHAR(100), "
							+ "referrer VARCHAR(4000)"
							+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
					execute(logger, sql);
					
					execute(logger, "CREATE INDEX hstcb" + companyID + "$email$idx ON hst_customer_" + companyID + "_binding_tbl (email)");
					execute(logger, "CREATE INDEX hstcb" + companyID + "$mlidcidl$idx ON hst_customer_" + companyID + "_binding_tbl (mailinglist_id, customer_id)");
					execute(logger, "CREATE INDEX hstcb" + companyID + "$tsch$idx ON hst_customer_" + companyID + "_binding_tbl (timestamp_change)");
				}
				
				bindingHistoryDao.recreateBindingHistoryTrigger(companyID);
				
				return true;
			} catch(Exception e) {
				logger.error("createHistoryTables: SQL: " + sql + "\n" + e, e);
				
				return false;
			}
		} else {
			//tables existing
			return true;
		}
	}

	/**
	 * This method gets a list with all NOT DELETED companies from our DB.
	 *
	 * @return
	 */
	@Override
	public List<ComCompany> getAllActiveCompanies() {
		String sql = "SELECT company_id, creator_company_id, shortname, description, rdir_domain, mailloop_domain, status, mailtracking, expire_stat, stat_admin, expire_onepixel, expire_success, expire_cookie, expire_upload, max_login_fails, login_block_time, secret_key, uid_version, auto_mailing_report_active, sector, business_field, expire_recipient, max_recipients, salutation_extended, enabled_uid_version, export_notify, parent_company_id, expire_bounce, contact_tech FROM company_tbl WHERE status = 'active' ORDER BY company_id DESC";
		try {
			return select(logger, sql, new ComCompany_RowMapper());
		} catch (Exception e) {
			// return an empty list, for no further actions
			return new ArrayList<>();
		}
	}

	private class ComCompany_RowMapper implements RowMapper<ComCompany> {
		@Override
		public ComCompany mapRow(ResultSet resultSet, int row) throws SQLException {
			ComCompany readCompany = new ComCompanyImpl();
			
			readCompany.setId(resultSet.getInt("company_id"));
			readCompany.setCreatorID(resultSet.getInt("creator_company_id"));
			readCompany.setShortname(resultSet.getString("shortname"));
			readCompany.setDescription(resultSet.getString("description"));
			readCompany.setRdirDomain(resultSet.getString("rdir_domain"));
			readCompany.setMailloopDomain(resultSet.getString("mailloop_domain"));
			readCompany.setStatus(resultSet.getString("status"));
			readCompany.setMailtracking(resultSet.getInt("mailtracking"));
			readCompany.setStatAdmin(resultSet.getInt("stat_admin"));
			readCompany.setExpireCookie(resultSet.getInt("expire_cookie"));
			readCompany.setExpireUpload(resultSet.getInt("expire_upload"));
			readCompany.setSector(resultSet.getInt("sector"));
			readCompany.setBusiness(resultSet.getInt("business_field"));
			readCompany.setMaxRecipients(resultSet.getInt("max_recipients"));
			readCompany.setMaxLoginFails(resultSet.getInt("max_login_fails"));
			readCompany.setLoginBlockTime(resultSet.getInt("login_block_time"));
			readCompany.setSecretKey(resultSet.getString("secret_key"));
			readCompany.setMinimumSupportedUIDVersion(resultSet.getInt("uid_version"));
			readCompany.setAutoMailingReportSendActivated(resultSet.getInt("auto_mailing_report_active") == 1);
			readCompany.setExpireRecipient(resultSet.getInt("expire_recipient"));
			readCompany.setSalutationExtended(resultSet.getInt("salutation_extended"));
			readCompany.setEnabledUIDVersion(resultSet.getInt("enabled_uid_version"));
			readCompany.setExportNotifyAdmin(resultSet.getInt("export_notify"));
			readCompany.setParentCompanyId( resultSet.getInt("parent_company_id"));
			readCompany.setExpireStat(resultSet.getInt("expire_stat"));
			readCompany.setExpireOnePixel(resultSet.getInt("expire_onepixel"));
			readCompany.setExpireSuccess(resultSet.getInt("expire_success"));
			readCompany.setExpireBounce(resultSet.getInt("expire_bounce"));
			readCompany.setContactTech(resultSet.getString("contact_tech"));
			
			return readCompany;
		}
	}

	@Override
	public int getNumberOfCompanies() {
		return selectInt(logger, "SELECT COUNT(*) FROM company_tbl WHERE status = ?", COMPANY_STATUS_ACTIVE);
	}

	@Override
	public int getMaximumNumberOfCustomers() {
		int maximumNumberOfCustomers = 0;
		for (Integer companyID : select(logger, "SELECT company_id FROM company_tbl WHERE status = ?", new IntegerRowMapper(), COMPANY_STATUS_ACTIVE)) {
			if (DbUtilities.checkIfTableExists(getDataSource(), "customer_" + companyID + "_tbl")) {
				int numberOfCustomers = selectInt(logger, "SELECT COUNT(*) FROM customer_" + companyID + "_tbl");
				maximumNumberOfCustomers = Math.max(maximumNumberOfCustomers, numberOfCustomers);
			}
		}
		return maximumNumberOfCustomers;
	}

	@Override
	public int getMaximumNumberOfProfileFields() throws Exception {
		int maximumNumberOfProfileFields = 0;
		for (Integer companyID : select(logger, "SELECT company_id FROM company_tbl WHERE status = ?", new IntegerRowMapper(), COMPANY_STATUS_ACTIVE)) {
			if (DbUtilities.checkIfTableExists(getDataSource(), "customer_" + companyID + "_tbl")) {
				int numberOfProfileFields = DbUtilities.getColumnCount(getDataSource(), "customer_" + companyID + "_tbl");
				maximumNumberOfProfileFields = Math.max(maximumNumberOfProfileFields, numberOfProfileFields);
			}
		}
		return maximumNumberOfProfileFields;
	}

	@Override
	public int getNumberOfProfileFields(@VelocityCheck int companyID) throws Exception {
		return DbUtilities.getColumnCount(getDataSource(), "customer_" + companyID + "_tbl");
	}

	@Override
	public List<Tuple<String,String>> getCompanyInfo(int companyID){
		String query = "SELECT cname, cvalue FROM company_info_tbl WHERE company_id IN (0, ?) ORDER BY company_id";
		return select(logger, query, (resultSet, i) -> new Tuple<>(resultSet.getString("cname"), resultSet.getString("cvalue")), companyID);
	}

	@Override
	public Map<String, Object> getCompanySettings(int companyID){
		String query = "SELECT shortname, mailtracking, secret_key, rdir_domain, mailloop_domain, status, mails_per_day FROM company_tbl WHERE company_id = ?";
		return selectSingleRow(logger, query, companyID);
	}

	@Override
	public List<Map<String, Object>> getReferenceTableSettings(int companyID){
		String query = "SELECT name, reftable, refsource, refcolumn, backref, joincondition, order_by, voucher FROM reference_tbl WHERE company_id = ?";
		return select(logger, query, companyID);
	}

	private List<Integer> getSampleFormIDs() {
		return select(logger, "SELECT form_id FROM userform_tbl WHERE company_id = 1 AND (LOWER(formname) LIKE '%sample%' OR LOWER(formname) LIKE '%example%' OR LOWER(formname) LIKE '%muster%' OR LOWER(formname) LIKE '%beispiel%')", new IntegerRowMapper());
	}

	@Override
	public void createCompanyPermission(int companyID, Permission permission) {
		if (companyID >= 0  && !hasCompanyPermission(companyID, permission)) {
			update(logger, "INSERT INTO company_permission_tbl (company_id, security_token) VALUES (?, ?)", companyID, permission.getTokenString());
		}
	}

	@Override
	public boolean hasCompanyPermission(int companyID, Permission permission) {
		return selectInt(logger, "SELECT COUNT(*) FROM company_permission_tbl WHERE (company_id = ? OR company_id = 0) AND security_token = ?", companyID, permission.getTokenString()) > 0;
	}

	@Override
	public Set<Permission> getCompanyPermissions(int companyID) {
		if (configService.getIntegerValue(ConfigValue.System_Licence) == 0) {
			// Only OpenEMM is allowed everything
			Set<Permission> returnSet = new HashSet<>();
			for (Permission permission: Permission.getAllSystemPermissions()) {
				returnSet.add(permission);
			}
			return returnSet;
		} else {
			List<String> result = select(logger, "SELECT DISTINCT security_token FROM company_permission_tbl WHERE company_id = ? OR company_id = 0", new StringRowMapper(), companyID);
			Set<Permission> returnSet = new HashSet<>();
			for (String securityToken: result) {
				returnSet.add(Permission.getPermissionByToken(securityToken));
			}
			return returnSet;
		}
	}

	@Override
	public void deleteCompanyPermission(int companyID, Permission permission) {
		update(logger, "DELETE FROM company_permission_tbl WHERE company_id = ? AND security_token = ?", companyID, permission.getTokenString());
	}
	
	@Override
	public boolean deleteAllCompanyPermission(int companyID) {
		int touchedLines = update(logger, "DELETE FROM company_permission_tbl WHERE company_id = ?", companyID);
		if (touchedLines > 0) {
			return true;
		} else {
			return selectInt(logger, "SELECT COUNT(*) FROM company_permission_tbl WHERE company_id = ?", companyID) == 0;
		}
	}

	@Override
	public void setupPremiumFeaturePermissions(Set<String> allowedPremiumFeatures, Set<String> unAllowedPremiumFeatures) {
		if (allowedPremiumFeatures != null) {
			for (String allowedSecurityToken : allowedPremiumFeatures) {
				Permission grantedPremiumPermission = Permission.getPermissionByToken(allowedSecurityToken);
				if (grantedPremiumPermission != null) {
					createCompanyPermission(0, grantedPremiumPermission);
				} else {
					logger.warn("Found non-existing granted premium permission: " + allowedSecurityToken);
				}
			}
		}
		
		if (unAllowedPremiumFeatures != null && unAllowedPremiumFeatures.size() > 0) {
			Object[] parameters = unAllowedPremiumFeatures.toArray(new String[unAllowedPremiumFeatures.size()]);
			
			List<String> foundUnAllowedPremiumFeatures_Admin = select(logger, "SELECT security_token FROM company_permission_tbl WHERE security_token IN (" + AgnUtils.repeatString("?", unAllowedPremiumFeatures.size(), ", ") + ")", new StringRowMapper(), parameters);
			
			int touchedLines1 = update(logger, "DELETE FROM company_permission_tbl WHERE security_token IN (" + AgnUtils.repeatString("?", unAllowedPremiumFeatures.size(), ", ") + ")", parameters);
			if (touchedLines1 > 0) {
				logger.error("Deleted unallowed premium features for admins: " + touchedLines1);
				logger.error(StringUtils.join(foundUnAllowedPremiumFeatures_Admin, ", "));
			}
		}
	}
	
	@Override
	public boolean addExecutiveAdmin(int companyID, int executiveAdminID) {
		return(update(logger, "UPDATE company_tbl SET stat_admin = ? WHERE company_id = ?", executiveAdminID, companyID)) == 1;
	}

	@Override
	public void changeFeatureRights(String featureName, int companyID, boolean activate) {
		if (DbUtilities.checkIfTableExists(getDataSource(), "feature_permission_tbl")) {
			List<Integer> subCompanyIDs = select(logger, "SELECT company_id FROM company_tbl WHERE parent_company_id = ?", new IntegerRowMapper(), companyID);
			List<String> rightsToSet = select(logger, "SELECT security_token FROM feature_permission_tbl WHERE feature_name = ?", new StringRowMapper(), featureName);
	
			for (String rightToCheck : rightsToSet) {
				if (Permission.getPermissionByToken(rightToCheck) != null) {
					if (activate) {
						createCompanyPermission(companyID, Permission.getPermissionByToken(rightToCheck));
						for (int subCompanyID : subCompanyIDs) {
							createCompanyPermission(subCompanyID, Permission.getPermissionByToken(rightToCheck));
						}
					} else {
						deleteCompanyPermission(companyID, Permission.getPermissionByToken(rightToCheck));
						for (int subCompanyID : subCompanyIDs) {
							deleteCompanyPermission(subCompanyID, Permission.getPermissionByToken(rightToCheck));
						}
					}
				}
			}
		}
	}

	@Override
	public int getPriorityCount(@VelocityCheck int companyId) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void setPriorityCount(@VelocityCheck int companyId, int value) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean isCompanyExist(int companyId) {
        return selectInt(logger, "SELECT count(company_id) FROM company_tbl WHERE company_id = ?", companyId) > 0;
	}

	@Override
	public String getShortName(@VelocityCheck int companyId) {
		String query = "SELECT shortname FROM company_tbl WHERE company_id = ?";
		return selectWithDefaultValue(logger, query, String.class, null, companyId);
	}

	@Override
	public void deactivateExtendedCompanies() {
		update(logger, "UPDATE company_tbl SET status = 'locked', timestamp = CURRENT_TIMESTAMP WHERE company_id IS NULL OR company_id < 1 OR company_id > 1");
	}
	
	@Override
	public boolean isCompanyNameUnique(String shortname) {
		return selectInt(logger, "SELECT count(*) FROM company_tbl WHERE shortname = ?", shortname) == 0;
	}
}
