/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.beans.Company;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.impl.CompanyImpl;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.archive.service.CampaignService;
import com.agnitas.emm.core.birtreport.service.BirtReportService;
import com.agnitas.emm.core.company.bean.CompanyEntry;
import com.agnitas.emm.core.company.form.CompanyListForm;
import com.agnitas.emm.core.company.rowmapper.CompanyEntryRowMapper;
import com.agnitas.emm.core.recipient.dao.BindingHistoryDao;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.beans.CompaniesConstraints;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.impl.CompanyStatus;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CompanyDaoImpl extends PaginatedBaseDaoImpl implements CompanyDao {

	private ConfigService configService;
	private MailingDao mailingDao;
	private TargetDao targetDao;
	private CopyMailingService copyMailingService;
	private BindingHistoryDao bindingHistoryDao;
	private UserformService userformService;
	private CampaignService campaignService;
	private BirtReportService birtReportService;
	
	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void setTargetDao(TargetDao targetDao) {
		this.targetDao = targetDao;
	}

	public void setCopyMailingService(CopyMailingService copyMailingService) {
		this.copyMailingService = copyMailingService;
	}

	public void setBindingHistoryDao(BindingHistoryDao bindingHistoryDao) {
		this.bindingHistoryDao = bindingHistoryDao;
	}

	public void setUserformService(UserformService userformService) {
		this.userformService = userformService;
	}

	public void setCampaignService(CampaignService campaignService) {
		this.campaignService = campaignService;
	}

	public void setBirtReportService(BirtReportService birtReportService) {
		this.birtReportService = birtReportService;
	}

	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic


	private static final String TABLESPACE_CUSTOMER_HISTORY = "customer_history";
	private static final String TABLESPACE_CUSTOMER_HISTORY_INDEX = "index_customer_history";
	private static final String TABLESPACE_DATA_WAREHOUSE = "data_warehouse";
	private static final String TABLESPACE_DATA_WAREHOUSE_INDEX = "index_data_warehouse";
	private static final String TABLESPACE_DATA_SUCCESS = "data_success";
	private static final String TABLESPACE_INDEX_SUCCESS = "index_success";
	private static final String TABLESPACE_DATA_CUSTOMER_TABLE = "data_cust_table";
	private static final String TABLESPACE_INDEX_CUSTOMER = "data_cust_index";
	
	@Override
	public Company getCompany(int companyID) {
		if (companyID == 0) {
			return null;
		}

		final String sql = "SELECT company_id, creator_company_id, shortname, description, rdir_domain, mailloop_domain, status, mailtracking, stat_admin, secret_key, uid_version, auto_mailing_report_active, sector, business_field, max_recipients, salutation_extended, enabled_uid_version, parent_company_id, contact_tech FROM company_tbl WHERE company_id = ?";
		final List<Company> list = select(sql, new Company_RowMapper(), companyID);
		
		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void saveCompany(Company company) throws Exception {
		try {
			List<Map<String, Object>> list = select("SELECT company_id FROM company_tbl WHERE company_id = ?", company.getId());
			
			if (!list.isEmpty()) {
				String sql = "UPDATE company_tbl SET creator_company_id = ?, shortname = ?, description = ?," +
						" rdir_domain = ?, mailloop_domain = ?, status = ?, mailtracking = ?," +
						" stat_admin = ?," +
						" sector = ?, business_field = ?, max_recipients = ?," +
						" salutation_extended = ?, contact_tech = ?" +
						" WHERE company_id = ?";
				update(sql,
						company.getCreatorID(),
						company.getShortname(),
						company.getDescription(),
						company.getRdirDomain(),
						company.getMailloopDomain(),
						company.getStatus().getDbValue(),
						company.getMailtracking(),
						company.getStatAdmin(),
						company.getSector(),
						company.getBusiness(),
						company.getMaxRecipients(),
						company.getSalutationExtended(),
						StringUtils.defaultString(company.getContactTech()),
						company.getId());
			} else {
				int defaultDatasourceID = createNewDefaultDatasourceID();
				
				int newCompanyID;
				
				if (isOracleDB()) {
					newCompanyID = selectInt("SELECT company_tbl_seq.NEXTVAL FROM DUAL");
					List<String> fieldList =
							Arrays.asList("company_id", "creator_company_id", "shortname", "description",
									"rdir_domain", "mailloop_domain", "status",
									"mailtracking", "stat_admin",
									"sector", "business_field", "max_recipients", "secret_key",
									"salutation_extended", "enabled_uid_version",
									"default_datasource_id", "contact_tech");

					String sql = "INSERT INTO company_tbl (" + StringUtils.join(fieldList, ", ") + ")"
						+ " VALUES (?" + StringUtils.repeat(", ?", fieldList.size() - 1) + ")";
					
					update(sql,
						newCompanyID,
						company.getCreatorID(),
						company.getShortname(),
						company.getDescription(),
						company.getRdirDomain(),
						company.getMailloopDomain(),
						company.getStatus().getDbValue(),
						company.getMailtracking(),
						company.getStatAdmin(),
						company.getSector(),
						company.getBusiness(),
						company.getMaxRecipients(),
						company.getSecretKey(),
						company.getSalutationExtended(),
						company.getEnabledUIDVersion(),
						defaultDatasourceID,
						StringUtils.defaultString(company.getContactTech()));
				} else {
					List<String> creationFieldList =
							Arrays.asList("creation_date", "creator_company_id", "shortname", "description",
									"rdir_domain", "mailloop_domain", "status", "mailtracking",
									"stat_admin", "sector", "business_field",
									"max_recipients", "secret_key", "salutation_extended", "enabled_uid_version",
									"default_datasource_id",
									"contact_tech");
					
					String insertStatementSql = "INSERT INTO company_tbl (" + StringUtils.join(creationFieldList, ", ") + ")"
							+ " VALUES (CURRENT_TIMESTAMP" + StringUtils.repeat(", ?", creationFieldList.size() - 1)+ ")";
					
					newCompanyID = insertIntoAutoincrementMysqlTable("company_id", insertStatementSql,
						company.getCreatorID(),
						company.getShortname(),
						company.getDescription(),
						company.getRdirDomain(),
						company.getMailloopDomain(),
						company.getStatus().getDbValue(),
						company.getMailtracking(),
						company.getStatAdmin(),
						company.getSector(),
						company.getBusiness(),
						company.getMaxRecipients(),
						company.getSecretKey(),
						company.getSalutationExtended(),
						company.getEnabledUIDVersion(),
						defaultDatasourceID,
						StringUtils.defaultString(company.getContactTech()));
				}

				company.setId(newCompanyID);
				
				updateDefaultDatasourceIDWithNewCompanyId(defaultDatasourceID, newCompanyID);
				
				if (company.getCreatorID() != configService.getIntegerValue(ConfigValue.EmmXPressMasterClient)) {
					update("INSERT INTO company_permission_tbl (company_id, permission_name) VALUES(?, ?)", newCompanyID, Permission.CLEANUP_RECIPIENT_DATA.getTokenString());
				}
			}
		} catch (Exception e) {
			logger.error("Cannot save company data", e);
			throw new Exception("Cannot store company data: " + e.getMessage(), e);
		}
	}

	/**
	 * Because the companyid is not aquired yet we use 1 (always existing company of emm-master) as interim companyid and update this later
	 */
	@DaoUpdateReturnValueCheck
	private int createNewDefaultDatasourceID() {
		int sourceGroupID = selectInt("SELECT sourcegroup_id FROM sourcegroup_tbl WHERE sourcegroup_type = ?", "DD");
		
		if (isOracleDB()) {
			int newDatasourceID = selectInt("SELECT datasource_description_tbl_seq.NEXTVAL FROM DUAL");
			update("INSERT INTO datasource_description_tbl (datasource_id, description, company_id, sourcegroup_id) VALUES (?, ?, ?, ?)",
				newDatasourceID, "Default Datasource", 1, sourceGroupID);
			return newDatasourceID;
		} else {
			int newDatasourceID = insertIntoAutoincrementMysqlTable("datasource_id", "INSERT INTO datasource_description_tbl (description, company_id, sourcegroup_id) VALUES (?, ?, ?)",
				"Default Datasource", 1, sourceGroupID);
			return newDatasourceID;
		}
	}

	/**
	 * Because the companyid was not aquired we used -1 as interim companyid and now we update this here with the new companyid
	 */
	@DaoUpdateReturnValueCheck
	private void updateDefaultDatasourceIDWithNewCompanyId(int datasourceID, int companyID) {
		update("UPDATE datasource_description_tbl SET company_id = ? WHERE datasource_id = ?", companyID, datasourceID);
	}
	
	@Override
	public void updateCompanyStatus(int companyID, CompanyStatus status) {
		String sql = "UPDATE company_tbl SET status = ?, timestamp = CURRENT_TIMESTAMP WHERE company_id = ?";
		update(sql, status.getDbValue(), companyID);
	}

	@Override
	public List<Integer> getOpenEMMCompanyForClosing() {
		final Date lifeTimeExpire = DateUtilities.getDateOfDaysAgo(configService.getIntegerValue(ConfigValue.MaximumLifetimeOfTestAccounts));
		final int creatorCompany = configService.getIntegerValue(ConfigValue.System_License_OpenEMMMasterCompany);
		String sql = "SELECT company_id FROM company_tbl WHERE creator_company_id = ? AND status = '" + CompanyStatus.ACTIVE.getDbValue() + "' AND timestamp < ?";
		return select(sql, IntegerRowMapper.INSTANCE, creatorCompany, lifeTimeExpire);
	}
	
	@Override
	public PaginatedListImpl<CompanyEntry> getCompanyList(int companyID, String sortCriterion, String sortDirection, int pageNumber, int pageSize) {
		if (StringUtils.isBlank(sortCriterion)) {
			sortCriterion = "shortname";
		}

		String sortColumn = sortCriterion;

		boolean sortDirectionAscending = !"desc".equalsIgnoreCase(sortDirection) && !"descending".equalsIgnoreCase(sortDirection);
		PaginatedListImpl<CompanyEntry> paginatedList;
		
		if (companyID == 1) {
			paginatedList = selectPaginatedList("SELECT company_id, shortname, description, status, timestamp FROM company_tbl WHERE (status != '" + CompanyStatus.DELETED.getDbValue() + "' AND status != '" + CompanyStatus.TODELETE.getDbValue() + "' AND status != '" + CompanyStatus.DELETION_IN_PROGRESS.getDbValue() + "')", "company_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, new CompanyEntryRowMapper());
		} else {
			paginatedList = selectPaginatedList("SELECT company_id, shortname, description, status, timestamp FROM company_tbl WHERE (company_id = ? OR creator_company_id = ?) AND (status != '" + CompanyStatus.DELETED.getDbValue() + "' AND status != '" + CompanyStatus.TODELETE.getDbValue() + "' AND status != '" + CompanyStatus.DELETION_IN_PROGRESS.getDbValue() + "')", "company_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, new CompanyEntryRowMapper(), companyID, companyID);
		}
		paginatedList.setSortCriterion(sortCriterion);
		return paginatedList;
	}

    @Override
   	public PaginatedListImpl<CompanyEntry> getCompanyList(CompanyListForm filter, int companyId) {
        StringBuilder sql = new StringBuilder(
                "SELECT company_id, shortname, description, status, timestamp FROM company_tbl");
        List<Object> params = applyOverviewFilter(filter, companyId, sql);
		PaginatedListImpl<CompanyEntry> list = selectPaginatedList(sql.toString(), "company_tbl", filter.getSortOrDefault("company_id"), filter.ascending(),
				filter.getPage(), filter.getNumberOfRows(), new CompanyEntryRowMapper(), params.toArray());

		if (filter.isUiFiltersSet()) {
			list.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(companyId));
		}

		return list;
   	}

    private List<Object> applyOverviewFilter(CompanyListForm filter, int companyId, StringBuilder sql) {
		List<Object> params = applyRequiredOverviewFilter(sql, companyId);

		if (filter.getId() != null) {
            sql.append(getPartialSearchFilterWithAnd("company_id", filter.getId(), params));
        }
        if (StringUtils.isNotBlank(filter.getName())) {
            sql.append(getPartialSearchFilterWithAnd("shortname", filter.getName(), params));
        }
        if (StringUtils.isNotBlank(filter.getDescription())) {
            sql.append(getPartialSearchFilterWithAnd("description", filter.getDescription(), params));
        }
        applyOverviewStatusFilter(filter.getStatus(), sql, params);
        return params;
    }

    private void applyOverviewStatusFilter(CompanyStatus status, StringBuilder sql, List<Object> params) {
        if (status == null) {
            return;
        }
        if (status == CompanyStatus.LOCKED) {
            sql.append(" AND status IN (?, ?)");
            params.addAll(List.of(CompanyStatus.LOCKED.getDbValue(), CompanyStatus.TODELETE.getDbValue()));
            return;
        }
        sql.append(" AND status = ?");
        params.add(status.getDbValue());
    }

	private int getTotalUnfilteredCountForOverview(int companyId) {
		StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM company_tbl");
		List<Object> params = applyRequiredOverviewFilter(query, companyId);

		return selectIntWithDefaultValue(query.toString(), 0, params.toArray());
	}

	private List<Object> applyRequiredOverviewFilter(StringBuilder query, int companyId) {
		List<Object> params = new ArrayList<>();

		query.append(" WHERE")
				.append(companyId != 1 ? " (company_id = ? OR creator_company_id = ?) AND" : "")
				.append(" status NOT IN (?, ?, ?)");

		if (companyId != 1) {
			params.addAll(List.of(companyId, companyId));
		}
		params.addAll(List.of(CompanyStatus.DELETED.getDbValue(), CompanyStatus.DELETION_IN_PROGRESS.getDbValue(), CompanyStatus.TODELETE.getDbValue()));

		return params;
	}

    @Override
	public List<CompanyEntry> getActiveCompaniesLight(boolean allowTransitionStatus) {
		if (allowTransitionStatus) {
			return select("SELECT company_id, shortname, description, status, timestamp FROM company_tbl WHERE status != '" + CompanyStatus.DELETED.getDbValue() + "' ORDER BY LOWER(shortname)", new CompanyEntryRowMapper());
		} else {
			return select("SELECT company_id, shortname, description, status, timestamp FROM company_tbl WHERE status = '" + CompanyStatus.ACTIVE.getDbValue() + "' ORDER BY LOWER(shortname)", new CompanyEntryRowMapper());
		}
	}
	
	@Override
	public List<CompanyEntry> getActiveOwnCompaniesLight(int companyId, boolean allowTransitionStatus) {
		if (allowTransitionStatus) {
			if (companyId == 1) {
				return select("SELECT company_id, shortname, description, status, timestamp FROM company_tbl WHERE status != '" + CompanyStatus.DELETED.getDbValue() + "' ORDER BY LOWER(shortname)", new CompanyEntryRowMapper());
			} else {
				return select("SELECT company_id, shortname, description, status, timestamp FROM company_tbl WHERE status != '" + CompanyStatus.DELETED.getDbValue() + "' AND (company_id = ? OR creator_company_id = ?) ORDER BY LOWER(shortname)", new CompanyEntryRowMapper(), companyId, companyId);
			}
			
		} else {
			return select("SELECT company_id, shortname, description, status, timestamp FROM company_tbl WHERE status = '" + CompanyStatus.ACTIVE.getDbValue() + "' AND (company_id = ? OR creator_company_id = ?) ORDER BY LOWER(shortname)", new CompanyEntryRowMapper(), companyId, companyId);
		}
	}

	@Override
	public List<CompanyEntry> findAllByEmailPart(String email, int companyID) {
		String query = "SELECT company_id, shortname, description, status, timestamp FROM company_tbl WHERE company_id = ?"
				+ getPartialSearchFilterWithAnd("contact_tech");
		return select(query, new CompanyEntryRowMapper(), companyID, email);
	}

	@Override
	public List<CompanyEntry> findAllByEmailPart(String email) {
		String query = "SELECT company_id, shortname, description, status, timestamp FROM company_tbl WHERE "
				+ getPartialSearchFilter("contact_tech");
		return select(query, new CompanyEntryRowMapper(), email);
	}

	@Override
	public void updateTechnicalContact(String email, int id) {
		update("UPDATE company_tbl SET contact_tech = ? WHERE company_id = ?", email, id);
	}

	@Override
	public CompanyEntry getCompanyLight(int id) {
		return select("SELECT company_id, shortname, description, status, timestamp FROM company_tbl WHERE company_id = ?", new CompanyEntryRowMapper(), id).get(0);
	}

	@Override
	public List<Company> getCreatedCompanies(int companyId) {
		try {
			if (companyId == 1) {
				return select("SELECT company_id, creator_company_id, shortname, description, rdir_domain, mailloop_domain, status, mailtracking, stat_admin, secret_key, uid_version, auto_mailing_report_active, sector, business_field, max_recipients, salutation_extended, enabled_uid_version, parent_company_id, contact_tech FROM company_tbl WHERE status = '" + CompanyStatus.ACTIVE.getDbValue() + "' ORDER BY LOWER (shortname)", new Company_RowMapper());
			} else {
				return select("SELECT company_id, creator_company_id, shortname, description, rdir_domain, mailloop_domain, status, mailtracking, stat_admin, secret_key, uid_version, auto_mailing_report_active, sector, business_field, max_recipients, salutation_extended, enabled_uid_version, parent_company_id, contact_tech FROM company_tbl WHERE (creator_company_id = ? OR company_id = ?) AND status = '" + CompanyStatus.ACTIVE.getDbValue() + "' ORDER BY LOWER (shortname)", new Company_RowMapper(), companyId, companyId);
			}
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean initTables(int newCompanyId) {
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
				
				if (!initCustomerTables(newCompanyId)) {
					throw new Exception("Cannot create Customer tables for company id: " + newCompanyId);
				}
				
				sql = "CREATE TABLE interval_track_" + newCompanyId + "_tbl (customer_id NUMBER NOT NULL, mailing_id NUMBER NOT NULL, send_date TIMESTAMP NOT NULL)" + tablespaceClauseDataSuccess;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX intervtrack$" + newCompanyId + "mid$idx ON interval_track_" + newCompanyId + "_tbl (mailing_id)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX intervtrack$" + newCompanyId + "$cid$idx ON interval_track_" + newCompanyId + "_tbl (customer_id)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX intervtrack$" + newCompanyId + "$sendd$idx ON interval_track_" + newCompanyId + "_tbl (send_date)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE mailtrack_" + newCompanyId + "_tbl (mailing_id NUMBER, maildrop_status_id NUMBER, customer_id NUMBER, timestamp DATE default SYSDATE, mediatype NUMBER)" + tablespaceClauseDataSuccess;
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE mailtrack_" + newCompanyId + "_tbl ADD CONSTRAINT mt" + newCompanyId + "$cuid$nn CHECK (customer_id IS NOT NULL)";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE mailtrack_" + newCompanyId + "_tbl ADD CONSTRAINT mt" + newCompanyId + "$mdsid$nn CHECK (maildrop_status_id IS NOT NULL)";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX mailtr" + newCompanyId + "$cid$idx ON mailtrack_" + newCompanyId + "_tbl (customer_id)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX mailtr" + newCompanyId + "$mdrstatid$idx ON mailtrack_" + newCompanyId + "_tbl (maildrop_status_id)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX mailtr" + newCompanyId + "$mid$idx ON mailtrack_" + newCompanyId + "_tbl (mailing_id)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX mailtr" + newCompanyId + "$ts$idx ON mailtrack_" + newCompanyId + "_tbl (timestamp)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE " + OnepixelDaoImpl.getOnepixellogTableName(newCompanyId) + " (customer_id NUMBER NOT NULL, mailing_id NUMBER NOT NULL, company_id NUMBER NOT NULL, ip_adr VARCHAR2(50), timestamp DATE DEFAULT SYSDATE, open_count NUMBER, mobile_count NUMBER, first_open TIMESTAMP, last_open TIMESTAMP)" + tablespaceClauseCustomerTable;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX onepix" + newCompanyId + "$mlid_cuid$idx ON " + OnepixelDaoImpl.getOnepixellogTableName(newCompanyId) + " (mailing_id, customer_id)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);

				sql = "CREATE TABLE " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (company_id NUMBER NOT NULL, mailing_id NUMBER NOT NULL, customer_id NUMBER NOT NULL, device_class_id NUMBER NOT NULL, device_id NUMBER, creation TIMESTAMP, client_id INTEGER)" + tablespaceClauseDataWarehouse;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX onedev" + newCompanyId + "$creat$idx ON " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (creation)" + tablespaceClauseDataWarehouseIndex;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX onepixdev" + newCompanyId + "$mlid_cuid$idx ON " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (mailing_id, customer_id)" + tablespaceClauseDataWarehouseIndex;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX onedev" + newCompanyId + "$ciddevclidmlid$idx ON " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (customer_id, device_class_id, mailing_id)" + tablespaceClauseDataWarehouseIndex;
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE rdirlog_" + newCompanyId + "_tbl (customer_id NUMBER NOT NULL, url_id NUMBER NOT NULL, company_id NUMBER NOT NULL, timestamp DATE DEFAULT SYSDATE, ip_adr VARCHAR2(50), mailing_id NUMBER, device_class_id NUMBER NOT NULL, device_id NUMBER, client_id INTEGER)" + tablespaceClauseCustomerTable;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX rlog" + newCompanyId + "$mlid_urlid_cuid$idx ON rdirlog_" + newCompanyId + "_tbl (MAILING_ID, URL_ID, CUSTOMER_ID)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX rlog" + newCompanyId + "$ciddevclidmlid$idx ON rdirlog_" + newCompanyId + "_tbl (customer_id, device_class_id, mailing_id)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX rlog" + newCompanyId + "$tmst$idx ON rdirlog_" + newCompanyId + "_tbl (timestamp)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE rdirlog_userform_" + newCompanyId + "_tbl (form_id NUMBER, customer_id NUMBER NULL, url_id NUMBER NOT NULL, company_id NUMBER NOT NULL, timestamp DATE DEFAULT SYSDATE, ip_adr VARCHAR2(50), mailing_id NUMBER, device_class_id NUMBER NOT NULL, device_id NUMBER, client_id INTEGER)" + tablespaceClauseCustomerTable;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX rlogform" + newCompanyId + "$fid_urlid$idx ON rdirlog_userform_" + newCompanyId + "_tbl (form_id, url_id)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);

				// Create success_xxx_tbl for this new company
				sql = "CREATE TABLE success_" + newCompanyId + "_tbl (customer_id NUMBER, mailing_id NUMBER, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)" + tablespaceClauseDataSuccess;
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE INDEX suc" + newCompanyId + "$mid$idx ON success_" + newCompanyId + "_tbl (mailing_id)" + tablespaceClauseIndexSuccess;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX suc" + newCompanyId + "$cid$idx ON success_" + newCompanyId + "_tbl (customer_id)" + tablespaceClauseIndexSuccess;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX suc" + newCompanyId + "$tmst$idx ON success_" + newCompanyId + "_tbl (timestamp)" + tablespaceClauseIndexSuccess;
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE rdir_traffic_amount_" + newCompanyId + "_tbl (mailing_id NUMBER, content_name VARCHAR2(3000), content_size NUMBER, demand_date TIMESTAMP)" + tablespaceClauseCustomerTable;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE TABLE rdir_traffic_agr_" + newCompanyId + "_tbl (mailing_id NUMBER, content_name VARCHAR2(3000), content_size NUMBER, demand_date TIMESTAMP, amount NUMBER)" + tablespaceClauseCustomerTable;
				executeWithRetry(0, 3, 120, sql);
				
				// Initially create a mailing_tbl entry for UID generation
				update("INSERT INTO mailing_tbl (mailing_id, company_id, deleted) VALUES (mailing_tbl_seq.nextval, 1, 1)");
			} else {
				sql = "CREATE TABLE interval_track_" + newCompanyId + "_tbl (customer_id INTEGER UNSIGNED NOT NULL, mailing_id INT(11) NOT NULL, send_date TIMESTAMP NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX intervtrack$" + newCompanyId + "mid$idx ON interval_track_" + newCompanyId + "_tbl (mailing_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX intervtrack$" + newCompanyId + "$cid$idx ON interval_track_" + newCompanyId + "_tbl (customer_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX intervtrack$" + newCompanyId + "$sendd$idx ON interval_track_" + newCompanyId + "_tbl (send_date)";
				executeWithRetry(0, 3, 120, sql);
				
				initCustomerTables(newCompanyId);
				
				// Watch out: Mysql does not support check constraints
				sql = "CREATE TABLE mailtrack_" + newCompanyId + "_tbl (mailing_id INT(11), maildrop_status_id INT(11) NOT NULL, customer_id INTEGER UNSIGNED NOT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, mediatype INT(10) UNSIGNED) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX mailtr" + newCompanyId + "$cid$idx ON mailtrack_" + newCompanyId + "_tbl (customer_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX mailtr" + newCompanyId + "$mdrstatid$idx ON mailtrack_" + newCompanyId + "_tbl (maildrop_status_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX mailtr" + newCompanyId + "$mid$idx ON mailtrack_" + newCompanyId + "_tbl (mailing_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX mailtr" + newCompanyId + "$ts$idx ON mailtrack_" + newCompanyId + "_tbl (timestamp)";
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE " + OnepixelDaoImpl.getOnepixellogTableName(newCompanyId) + " (customer_id INTEGER UNSIGNED NOT NULL, mailing_id INT(11) NOT NULL, company_id INT(11) NOT NULL, ip_adr VARCHAR(50), timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, open_count INT(11), mobile_count INT(11), first_open TIMESTAMP NULL, last_open TIMESTAMP NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX onepix" + newCompanyId + "$mlid_cuid$idx ON " + OnepixelDaoImpl.getOnepixellogTableName(newCompanyId) + " (mailing_id, customer_id)";
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (company_id INT(11) NOT NULL, mailing_id INT(11) NOT NULL, customer_id INTEGER UNSIGNED NOT NULL, device_id INT(11), device_class_id INT(2) NOT NULL, creation TIMESTAMP, client_id INT(11)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX onedev" + newCompanyId + "$creat$idx ON " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (creation)";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX onepixdev" + newCompanyId + "$mlid_cuid$idx ON " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (mailing_id, customer_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX onedev" + newCompanyId + "$ciddevclidmlid$idx ON " + OnepixelDaoImpl.getOnepixellogDeviceTableName(newCompanyId) + " (customer_id, device_class_id, mailing_id)";
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE rdirlog_" + newCompanyId + "_tbl (customer_id INTEGER UNSIGNED NOT NULL, url_id INT(11) NOT NULL, company_id INT(11) NOT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, ip_adr VARCHAR(50), mailing_id INT(11), device_class_id INT(2) NOT NULL, device_id INT(11), client_id INT(11)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX rlog" + newCompanyId + "$mlid_urlid_cuid$idx ON rdirlog_" + newCompanyId + "_tbl (mailing_id, url_id, customer_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX rlog" + newCompanyId + "$$ciddevclidmlid$idx ON rdirlog_" + newCompanyId + "_tbl (customer_id, device_class_id, mailing_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX rlog" + newCompanyId + "$tmst$idx ON rdirlog_" + newCompanyId + "_tbl (timestamp)";
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE rdirlog_userform_" + newCompanyId + "_tbl (form_id INT(11), customer_id INTEGER UNSIGNED NULL, url_id INT(11) NOT NULL, company_id INT(11) NOT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, ip_adr VARCHAR(50), mailing_id INT(11), device_class_id INT(2) NOT NULL, device_id INT(11), client_id INT(11)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX rlogform" + newCompanyId + "$fid_urlid$idx ON rdirlog_userform_" + newCompanyId + "_tbl (form_id, url_id)";
				executeWithRetry(0, 3, 120, sql);
				
				// Create success_tbl_xxx for this new company
				sql = "CREATE TABLE success_" + newCompanyId + "_tbl (customer_id INTEGER UNSIGNED, mailing_id INT(11), timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX suc" + newCompanyId + "$mid$idx ON success_" + newCompanyId + "_tbl (mailing_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX suc" + newCompanyId + "$cid$idx ON success_" + newCompanyId + "_tbl (customer_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX suc" + newCompanyId + "$tmst$idx ON success_" + newCompanyId + "_tbl (timestamp)";
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE rdir_traffic_amount_" + newCompanyId + "_tbl (mailing_id INTEGER, content_name VARCHAR(3000), content_size INTEGER, demand_date TIMESTAMP NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE TABLE rdir_traffic_agr_" + newCompanyId + "_tbl (mailing_id INTEGER, content_name VARCHAR(3000), content_size INTEGER, demand_date TIMESTAMP NULL, amount INTEGER) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				executeWithRetry(0, 3, 120, sql);
				
				// Initially create a mailing_tbl entry for UID generation
				update("INSERT INTO mailing_tbl (company_id, deleted) VALUES (1, 1)");
			}
			
			createRdirValNumTable(newCompanyId);
			
			//create historyBindingTbl and trigger for new company
			if (!createHistoryTables(newCompanyId)) {
				updateCompanyStatus(newCompanyId, CompanyStatus.LOCKED);
				throw new Exception("Cannot create history tables");
			}

			if (isOracleDB()) {
				// Copy mailinglist
				int mailingListToCopyID = selectIntWithDefaultValue("SELECT MIN(mailinglist_id) FROM mailinglist_tbl WHERE company_id = 1 AND deleted = 0", 0);
				if (mailingListToCopyID > 0) {
					mailinglistID = selectInt("SELECT mailinglist_tbl_seq.NEXTVAL FROM DUAL");
					update("INSERT INTO mailinglist_tbl (mailinglist_id, shortname, description, company_id) (SELECT ?, shortname, description, ? FROM mailinglist_tbl WHERE company_id = 1 AND deleted = 0 AND mailinglist_id = ?)", mailinglistID, newCompanyId, mailingListToCopyID);
				}
				
				// Copy customer
				int customerToCopyID = selectIntWithDefaultValue("SELECT MIN(customer_id) FROM customer_1_tbl", 0);
				int newCustomerID = selectInt("SELECT customer_" + newCompanyId + "_tbl_seq.NEXTVAL FROM DUAL");
				if (customerToCopyID > 0) {
					update("INSERT INTO customer_" + newCompanyId + "_tbl (customer_id, gender, firstname, lastname, email, mailtype, creation_date, timestamp) (SELECT " + newCustomerID + ", gender, firstname, lastname, email, mailtype, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM customer_1_tbl WHERE customer_id = " + customerToCopyID + ")");
				} else {
					update("INSERT INTO customer_" + newCompanyId + "_tbl (customer_id, gender, " + RecipientStandardField.Bounceload.getColumnName() + ", email, mailtype, creation_date, timestamp) VALUES(?, 2, 0, 'test@example.com', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", newCustomerID);
				}
				if (customerToCopyID > 0) {
					// Set binding for new customer
					sql = "INSERT INTO customer_" + newCompanyId + "_binding_tbl (mailinglist_id, mediatype, user_status, user_type, exit_mailing_id, customer_id) VALUES (" + mailinglistID + ", 0, 1, '" + UserType.Admin.getTypeCode() + "', 0, " + newCustomerID + ")";
					update(sql);
				}
				
				// New customer
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (customer_id, gender, " + RecipientStandardField.Bounceload.getColumnName() + ", email, mailtype, creation_date, timestamp) VALUES(customer_" + newCompanyId + "_tbl_seq.NEXTVAL, 2, 1, 'adam+" + newCompanyId + "@adamatis.eu', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
				update(sql);
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (customer_id, gender, " + RecipientStandardField.Bounceload.getColumnName() + ", email, mailtype, creation_date, timestamp) VALUES(customer_" + newCompanyId + "_tbl_seq.NEXTVAL, 2, 1, 'eva+" + newCompanyId + "@adamatis.eu', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
				update(sql);
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (customer_id, gender, " + RecipientStandardField.Bounceload.getColumnName() + ", email, mailtype, creation_date, timestamp) VALUES(customer_" + newCompanyId + "_tbl_seq.NEXTVAL, 2, 1, 'kain+" + newCompanyId + "@adamatis.eu', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
				update(sql);
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (customer_id, gender, " + RecipientStandardField.Bounceload.getColumnName() + ", email, mailtype, creation_date, timestamp) VALUES(customer_" + newCompanyId + "_tbl_seq.NEXTVAL, 2, 1, 'abel+" + newCompanyId + "@adamatis.eu', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
				update(sql);

				// Default mediapool categories
				sql = "INSERT INTO grid_mediapool_category_tbl (company_id, category_id, shortname, description, translatable)"
					+ " VALUES (" + newCompanyId + ", grid_category_tbl_seq.nextval, 'General', '', 1)";
				update(sql);
				sql = "INSERT INTO grid_mediapool_category_tbl (company_id, category_id, shortname, description, translatable)"
					+ " VALUES (" + newCompanyId + ", grid_category_tbl_seq.nextval, 'grid.mediapool.category.editorial', '', 1)";
				update(sql);
			} else {
				// Copy mailinglist
				int mailingListToCopyID = selectIntWithDefaultValue("SELECT MIN(mailinglist_id) FROM mailinglist_tbl WHERE company_id = 1 AND deleted = 0", 0);
				if (mailingListToCopyID > 0) {
					mailinglistID = insertIntoAutoincrementMysqlTable("mailinglist_id", "INSERT INTO mailinglist_tbl (shortname, description, company_id) (SELECT shortname, description, ? FROM mailinglist_tbl WHERE company_id = 1 AND deleted = 0 AND mailinglist_id = ?)", newCompanyId, mailingListToCopyID);
				}
				
				// Copy customer
				int customerToCopyID = selectIntWithDefaultValue("SELECT MIN(customer_id) FROM customer_1_tbl", 0);
				int newCustomerID;
				if (customerToCopyID > 0) {
					newCustomerID = insertIntoAutoincrementMysqlTable("customer_id", "INSERT INTO customer_" + newCompanyId + "_tbl (gender, firstname, lastname, email, mailtype, creation_date, timestamp) (SELECT gender, firstname, lastname, email, mailtype, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM customer_1_tbl WHERE customer_id = ?)", customerToCopyID);
				} else {
					newCustomerID = insertIntoAutoincrementMysqlTable("customer_id", "INSERT INTO customer_" + newCompanyId + "_tbl (gender, " + RecipientStandardField.Bounceload.getColumnName() + ", email, mailtype, creation_date, timestamp) VALUES (2, 0, 'test@example.com', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
				}
				if (newCustomerID > 0) {
					// Set binding for new customer
					sql = "INSERT INTO customer_" + newCompanyId + "_binding_tbl (mailinglist_id, mediatype, user_status, user_type, exit_mailing_id, customer_id) VALUES (" + mailinglistID + ", 0, 1, '" + UserType.Admin.getTypeCode() + "', 0, " + newCustomerID + ")";
					update(sql);
				}

				// New customer
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (gender, " + RecipientStandardField.Bounceload.getColumnName() + ", email, mailtype, creation_date, timestamp) VALUES(2, 1, 'adam+" + newCompanyId + "@adamatis.eu', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
				update(sql);
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (gender, " + RecipientStandardField.Bounceload.getColumnName() + ", email, mailtype, creation_date, timestamp) VALUES(2, 1, 'eva+" + newCompanyId + "@adamatis.eu', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
				update(sql);
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (gender, " + RecipientStandardField.Bounceload.getColumnName() + ", email, mailtype, creation_date, timestamp) VALUES(2, 1, 'kain+" + newCompanyId + "@adamatis.eu', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
				update(sql);
				sql = "INSERT INTO customer_" + newCompanyId + "_tbl (gender, " + RecipientStandardField.Bounceload.getColumnName() + ", email, mailtype, creation_date, timestamp) VALUES(2, 1, 'abel+" + newCompanyId + "@adamatis.eu', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
				update(sql);

				// Default mediapool categories
				sql = "INSERT INTO grid_mediapool_category_tbl (company_id, shortname, description, translatable)"
					+ " VALUES (" + newCompanyId + ", 'General', '', 1)";
				update(sql);
				sql = "INSERT INTO grid_mediapool_category_tbl (company_id, shortname, description, translatable)"
					+ " VALUES (" + newCompanyId + ", 'grid.mediapool.category.editorial', '', 1)";
				update(sql);
			}

			// All following actions do not use the db transaction, so it must be closed/commited in forehand to prevent deadlocks
			
			targetDao.createSampleTargetGroups(newCompanyId);
			
			String rdirDomain = getRedirectDomain(newCompanyId);

			copySampleMailings(newCompanyId, mailinglistID, rdirDomain);

			// Copy sample form templates (some form actions need the sample mailings)
			copySampleUserForms(newCompanyId, mailinglistID, rdirDomain);
			
			copySampleCampaigns(newCompanyId, 1);
			copySampleReports(newCompanyId, 1);
			
			return true;
		} catch (Exception e) {
			logger.error(String.format("initTables: SQL: %s", sql), e);
			updateCompanyStatus(newCompanyId, CompanyStatus.LOCKED);
			return false;
		}
	}

	private void copySampleCampaigns(int newCompanyId, int fromCompanyId) {
		campaignService.copySampleCampaigns(newCompanyId, fromCompanyId);
	}

	private void copySampleReports(int toCompanyId, int fromCompanyId) {
		birtReportService.copySampleReports(toCompanyId, fromCompanyId);
	}

	private void copySampleUserForms(int newCompanyId, int mailinglistID, String rdirDomain) {
		for (int sampleFormID : getSampleFormIDs()) {
			try {
				userformService.copyUserForm(sampleFormID, 1, newCompanyId, mailinglistID, rdirDomain, null);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void copySampleMailings(int newCompanyId, int mailinglistID, String rdirDomain) throws Exception {
		doCopySampleMailings(newCompanyId, mailinglistID, rdirDomain, new HashMap<>());
	}
	
	protected void doCopySampleMailings(int newCompanyId, int mailinglistID, String rdirDomain, final Map<Integer, Integer> mailingsMapping) throws Exception {
		// Copy sample mailing templates
		for (int sampleMailingID : mailingDao.getSampleMailingIDs()) {
			int copiedMailingID = copyMailingService.copyMailing(1, sampleMailingID, newCompanyId, null, null);
			Mailing newMailing = mailingDao.getMailing(copiedMailingID, newCompanyId);
			newMailing.setMailinglistID(mailinglistID);
			
			for (MailingComponent component : newMailing.getComponents().values()) {
				String content = component.getEmmBlock();
				if (content != null) {
					content = replaceContentProperties(content, newCompanyId, newMailing.getId(), mailinglistID, rdirDomain);
					component.setEmmBlock(content, "text/plain");
				}
			}

			for (DynamicTag dynamicTag : newMailing.getDynTags().values()) {
				for (DynamicTagContent dynamicTagContent : dynamicTag.getDynContent().values()) {
					String content = dynamicTagContent.getDynContent();
					if (content != null) {
						content = replaceContentProperties(content, newCompanyId, newMailing.getId(), mailinglistID, rdirDomain);
						dynamicTagContent.setDynContent(content);
					}
				}
			}
			
			for (TrackableLink trackableLink : newMailing.getTrackableLinks().values()) {
				String content = trackableLink.getFullUrl();
				if (content != null) {
					content = replaceContentProperties(content, newCompanyId, newMailing.getId(), mailinglistID, rdirDomain);
					trackableLink.setFullUrl(content);
				}
			}
			
			mailingDao.saveMailing(newMailing, false);
			mailingsMapping.put(sampleMailingID, newMailing.getId());
		}
	}

	private String replaceContentProperties(String content, int companyId, int mailingId, int mailinglistId, String rdirDomain) throws Exception {
		Optional<String> companyTokenOptional = getCompanyToken(companyId);
		String companyToken = companyTokenOptional.isPresent() ? companyTokenOptional.get() : null;
		
		String cid = Integer.toString(companyId);
		content = StringUtils.replaceEach(content, new String[]{"<CID>", "<cid>", "[COMPANY_ID]", "[company_id]", "[Company_ID]"},
					new String[]{cid, cid, cid, cid, cid});

		String mid = Integer.toString(mailingId);
		content = StringUtils.replaceEach(content, new String[]{"<MID>", "<mid>"},
					new String[]{mid, mid});

		String mlid = Integer.toString(mailinglistId);
		content = StringUtils.replaceEach(content, new String[]{"<MLID>", "<mlid>", "[MAILINGLIST_ID]", "[mailinglist_id]", "[Mailinglist_ID]"},
					new String[]{mlid, mlid, mlid, mlid, mlid});
		
		if (StringUtils.isNotBlank(companyToken)) {
			content = content.replace("[CTOKEN]", companyToken);
		} else {
			content = content.replace("agnCTOKEN=[CTOKEN]", "agnCI=" + companyId);
		}

		content = content.replace("<rdir-domain>", StringUtils.defaultIfBlank(rdirDomain, "RDIR-Domain"));
		return content;
	}

	@Override
	public void createRdirValNumTable(int newCompanyId) {
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
			sql = "CREATE TABLE rdirlog_" + newCompanyId + "_val_num_tbl (company_id NUMBER, customer_id NUMBER, ip_adr VARCHAR2(50), mailing_id NUMBER, session_id NUMBER, timestamp DATE DEFAULT CURRENT_TIMESTAMP, num_parameter NUMBER, page_tag VARCHAR(30))" + tablespaceClauseDataSuccess;
			executeWithRetry(0, 3, 120, sql);
			sql = "ALTER TABLE rdirlog_" + newCompanyId + "_val_num_tbl ADD CONSTRAINT rdvalnum" + newCompanyId + "$coid$nn CHECK (company_id IS NOT NULL)";
			executeWithRetry(0, 3, 120, sql);
			sql = "CREATE INDEX rvalnum" + newCompanyId + "$cod_cid_mid$idx ON rdirlog_" + newCompanyId + "_val_num_tbl (company_id, customer_id, mailing_id)" + tablespaceClauseCustomerBindIndex;
			executeWithRetry(0, 3, 120, sql);
			sql = "CREATE INDEX rvalnum" + newCompanyId + "$mid_pagetag$idx ON rdirlog_" + newCompanyId + "_val_num_tbl (mailing_id, page_tag)" + tablespaceClauseCustomerBindIndex;
			executeWithRetry(0, 3, 120, sql);
		} else {
			String sql;
			// Create reveue tracking table for this new company
			sql = "CREATE TABLE rdirlog_" + newCompanyId + "_val_num_tbl (company_id INT(11) NOT NULL, customer_id INTEGER UNSIGNED, ip_adr VARCHAR(50), mailing_id INT(11), session_id INT(11), `timestamp` timestamp DEFAULT CURRENT_TIMESTAMP, num_parameter DOUBLE, page_tag VARCHAR(30)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
			executeWithRetry(0, 3, 120, sql);
			sql = "ALTER TABLE rdirlog_" + newCompanyId + "_val_num_tbl ADD CONSTRAINT rdvalnum" + newCompanyId + "$coid$nn CHECK (company_id IS NOT NULL)";
			executeWithRetry(0, 3, 120, sql);
			sql = "CREATE INDEX rvalnum" + newCompanyId + "$cod_cid_mid$idx ON rdirlog_" + newCompanyId + "_val_num_tbl (company_id, customer_id, mailing_id)";
			executeWithRetry(0, 3, 120, sql);
			sql = "CREATE INDEX rvalnum" + newCompanyId + "$mid_pagetag$idx ON rdirlog_" + newCompanyId + "_val_num_tbl (mailing_id, page_tag)";
			executeWithRetry(0, 3, 120, sql);
		}
	}

	@Override
	public boolean initCustomerTables(int newCompanyId) {
		try {
			boolean createPreventConstraint = true;
			if (!configService.getBooleanValue(ConfigValue.UsePreventTableDropConstraint, newCompanyId)) {
				createPreventConstraint = false;
			}
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
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE customer_" + newCompanyId + "_tbl ("
					+ RecipientStandardField.CustomerID.getColumnName() + " NUMBER, "
					+ RecipientStandardField.Email.getColumnName() + " VARCHAR2(100), "
					+ RecipientStandardField.Firstname.getColumnName() + " VARCHAR2(100), "
					+ RecipientStandardField.Lastname.getColumnName() + " VARCHAR2(100), "
					+ RecipientStandardField.Title.getColumnName() + " VARCHAR2(100), "
					+ RecipientStandardField.Gender.getColumnName() + " NUMBER(1), "
					+ RecipientStandardField.Mailtype.getColumnName() + " NUMBER(1), "
					+ RecipientStandardField.ChangeDate.getColumnName() + " DATE DEFAULT SYSDATE, "
					+ RecipientStandardField.CreationDate.getColumnName() + " DATE DEFAULT SYSDATE, "
					+ RecipientStandardField.DatasourceID.getColumnName() + " NUMBER, "
					+ RecipientStandardField.Bounceload.getColumnName() + " NUMBER(1) DEFAULT 0 NOT NULL, "
					+ RecipientStandardField.LastOpenDate.getColumnName() + " DATE, "
					+ RecipientStandardField.LastClickDate.getColumnName() + " DATE, "
					+ RecipientStandardField.LastSendDate.getColumnName() + " DATE, "
					+ RecipientStandardField.LatestDatasourceID.getColumnName() + " NUMBER, "
					+ RecipientStandardField.DoNotTrack.getColumnName() + " NUMBER(1), "
					+ RecipientStandardField.CleanedDate.getColumnName() + " DATE, "
					+ RecipientStandardField.EncryptedSending.getColumnName() + " NUMBER(1) DEFAULT 1"
					+ ")"
					+ tablespaceClauseCustomer;
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_tbl ADD CONSTRAINT cust" + newCompanyId + "$cid$pk PRIMARY KEY (customer_id)" + tablespaceCustomerContraint;
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_tbl ADD CONSTRAINT cust" + newCompanyId + "$email$nn CHECK (email IS NOT NULL)";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_tbl ADD CONSTRAINT cust" + newCompanyId + "$gender$ck CHECK (gender IN (0,1,2,3,4,5))";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_tbl ADD CONSTRAINT cust" + newCompanyId + "$gender$nn CHECK (gender IS NOT NULL)";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_tbl ADD CONSTRAINT cust" + newCompanyId + "$mailtype$ck CHECK (mailtype IN (0,1,2,4))";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_tbl ADD CONSTRAINT cust" + newCompanyId + "$mailtype$nn CHECK (mailtype IS NOT NULL)";
				executeWithRetry(0, 3, 120, sql);
				if (createPreventConstraint) {
					sql = "ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$customer_" + newCompanyId + "_tbl FOREIGN KEY (customer_id) REFERENCES customer_" + newCompanyId + "_tbl (customer_id)";
					executeWithRetry(0, 3, 120, sql);
				}
				sql = "CREATE INDEX cust" + newCompanyId + "$email$idx ON customer_" + newCompanyId + "_tbl (email)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX cust" + newCompanyId + "$lowemail$idx ON customer_" + newCompanyId + "_tbl (LOWER(email))" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE customer_" + newCompanyId + "_binding_tbl ("
					+ "customer_id NUMBER, "
					+ "mailinglist_id NUMBER, "
					+ "user_type CHAR(1), "
					+ "user_status NUMBER, "
					+ "user_remark VARCHAR2(150), "
					+ "timestamp DATE DEFAULT SYSDATE, "
					+ "creation_date DATE default SYSDATE, "
					+ "exit_mailing_id NUMBER, "
					+ "entry_mailing_id NUMBER, "
					+ "mediatype NUMBER DEFAULT 0, "
					+ "referrer VARCHAR2(4000)"
					+ ")" + tablespaceClauseCustomer;
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$cid$fk FOREIGN KEY (customer_id) REFERENCES customer_" + newCompanyId + "_tbl (customer_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$mid$fk FOREIGN KEY (mailinglist_id) REFERENCES mailinglist_tbl (mailinglist_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$cid_mid_mt$pk PRIMARY KEY (customer_id, mailinglist_id, mediatype)" + tablespaceCustomerContraint;
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$cid$nn CHECK (customer_id IS NOT NULL)";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$mid$nn CHECK (mailinglist_id IS NOT NULL)";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$ustat$nn CHECK (user_status IS NOT NULL)";
				executeWithRetry(0, 3, 120, sql);
				if (createPreventConstraint) {
					sql = "ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$customer_" + newCompanyId + "_binding_tbl FOREIGN KEY (customer_id, mailinglist_id, mediatype) REFERENCES customer_" + newCompanyId + "_binding_tbl (customer_id, mailinglist_id, mediatype)";
					executeWithRetry(0, 3, 120, sql);
				}
				sql = "CREATE INDEX cust" + newCompanyId + "b$tmst$idx ON customer_" + newCompanyId + "_binding_tbl (timestamp)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX cust" + newCompanyId + "b$cuid_utype_mlid$idx ON customer_" + newCompanyId + "_binding_tbl (customer_id, user_type, mailinglist_id)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				sql = "CREATE INDEX cust" + newCompanyId + "b$cuid_ustat_mlid$idx ON customer_" + newCompanyId + "_binding_tbl (customer_id, user_status, mailinglist_id)" + tablespaceClauseCustomerBindIndex;
				executeWithRetry(0, 3, 120, sql);
				
				// Oracle Standard Edition does not support bitmap index. But a simple index might be worse than no index because of the few different values of bounceload.
//				try {
//					sql = "CREATE BITMAP INDEX cust" + newCompanyId + "$bounceload$idx ON customer_" + newCompanyId + "_tbl (bounceload)" + tablespaceClauseCustomerBindIndex;
//					executeWithRetry(0, 3, 120, sql);
//				} catch (Exception e) {
//					logger.error("Cannot create bitmap index on bounceload: " + e.getMessage(), e);
//				}
				
	        	if (!DbUtilities.checkIfTableExists(getDataSource(), "cust" + newCompanyId + "_ban_tbl")) {
	        		sql = "CREATE TABLE cust" + newCompanyId + "_ban_tbl (email VARCHAR2(150) PRIMARY KEY, timestamp DATE DEFAULT SYSDATE, reason VARCHAR2(500))" + tablespaceClauseCustomerTable;
	        		executeWithRetry(0, 3, 120, sql);
	        		if (createPreventConstraint) {
	        			sql = "ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$cust" + newCompanyId + "_ban_tbl FOREIGN KEY (text) REFERENCES cust" + newCompanyId + "_ban_tbl (email)";
	        			executeWithRetry(0, 3, 120, sql);
	        		}
	        	}
			} else {
				// Watch out: Mysql does not support check constraints
				sql = "CREATE TABLE customer_" + newCompanyId + "_tbl ("
					+ RecipientStandardField.CustomerID.getColumnName() + " INT(11) UNSIGNED PRIMARY KEY AUTO_INCREMENT, "
					+ RecipientStandardField.Email.getColumnName() + " VARCHAR(100) NOT NULL, "
					+ RecipientStandardField.Firstname.getColumnName() + " VARCHAR(100), "
					+ RecipientStandardField.Lastname.getColumnName() + " VARCHAR(100), "
					+ RecipientStandardField.Title.getColumnName() + " VARCHAR(100), "
					+ RecipientStandardField.Gender.getColumnName() + " INT(1) NOT NULL, "
					+ RecipientStandardField.Mailtype.getColumnName() + " INT(1) NOT NULL, "
					+ RecipientStandardField.ChangeDate.getColumnName() + " TIMESTAMP NULL, "
					+ RecipientStandardField.CreationDate.getColumnName() + " TIMESTAMP NULL, "
					+ RecipientStandardField.DatasourceID.getColumnName() + " INT(11), "
					+ RecipientStandardField.Bounceload.getColumnName() + " INT(1) NOT NULL DEFAULT 0, "
					+ RecipientStandardField.LastOpenDate.getColumnName() + " TIMESTAMP NULL, "
					+ RecipientStandardField.LastClickDate.getColumnName() + " TIMESTAMP NULL, "
					+ RecipientStandardField.LastSendDate.getColumnName() + " TIMESTAMP NULL, "
					+ RecipientStandardField.LatestDatasourceID.getColumnName() + " INT(11), "
					+ RecipientStandardField.DoNotTrack.getColumnName() + " INT(1), "
					+ RecipientStandardField.CleanedDate.getColumnName() + " TIMESTAMP NULL, "
					+ RecipientStandardField.EncryptedSending.getColumnName() + " INT(1) DEFAULT 1"
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				executeWithRetry(0, 3, 120, sql);
				if (createPreventConstraint) {
					sql = "ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$customer_" + newCompanyId + "_tbl FOREIGN KEY (customer_id) REFERENCES customer_" + newCompanyId + "_tbl (customer_id)";
					executeWithRetry(0, 3, 120, sql);
				}
				sql = "CREATE INDEX cust" + newCompanyId + "$email$idx ON customer_" + newCompanyId + "_tbl (email)";
				executeWithRetry(0, 3, 120, sql);
				
			 // Watch out for collation of user_type in customer_*_binding_tbl
				sql = "CREATE TABLE customer_" + newCompanyId + "_binding_tbl ("
					+ "customer_id INTEGER UNSIGNED NOT NULL,"
					+ " mailinglist_id INT(11) UNSIGNED DEFAULT NULL,"
					+ " user_type CHAR(1) COLLATE utf8mb4_bin,"
					+ " user_status INT(11) NOT NULL,"
					+ " user_remark VARCHAR(150),"
					+ " timestamp TIMESTAMP NULL,"
					+ " creation_date TIMESTAMP NULL,"
					+ " exit_mailing_id INT(11),"
					+ " entry_mailing_id INT(11),"
					+ " mediatype INTEGER UNSIGNED DEFAULT 0,"
					+ " referrer VARCHAR(4000)"
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$cid_mid_mt$pk PRIMARY KEY (customer_id, mailinglist_id, mediatype)";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$cid$fk FOREIGN KEY (customer_id) REFERENCES customer_" + newCompanyId + "_tbl (customer_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$mid$fk FOREIGN KEY (mailinglist_id) REFERENCES mailinglist_tbl (mailinglist_id)";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$cid$nn CHECK (customer_id IS NOT NULL)";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$mid$nn CHECK (mailinglist_id IS NOT NULL)";
				executeWithRetry(0, 3, 120, sql);
				sql = "ALTER TABLE customer_" + newCompanyId + "_binding_tbl ADD CONSTRAINT cust" + newCompanyId + "b$ustat$nn CHECK (user_status IS NOT NULL)";
				executeWithRetry(0, 3, 120, sql);
				if (createPreventConstraint) {
					sql = "ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$customer_" + newCompanyId + "_binding_tbl FOREIGN KEY (customer_id, mailinglist_id, mediatype) REFERENCES customer_" + newCompanyId + "_binding_tbl (customer_id, mailinglist_id, mediatype)";
					executeWithRetry(0, 3, 120, sql);
				}
				sql = "CREATE INDEX cust" + newCompanyId + "b$cuid_ustat_mlid$idx ON customer_" + newCompanyId + "_binding_tbl (customer_id, user_status, mailinglist_id)";
				executeWithRetry(0, 3, 120, sql);
				
				sql = "CREATE TABLE cust" + newCompanyId + "_ban_tbl (email VARCHAR(150) COLLATE utf8mb4_bin NOT NULL, timestamp timestamp DEFAULT CURRENT_TIMESTAMP, reason varchar(500), PRIMARY KEY (email)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
				executeWithRetry(0, 3, 120, sql);
				if (createPreventConstraint) {
					sql = "ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$cust" + newCompanyId + "ban_tbl FOREIGN KEY (email_ban) REFERENCES cust" + newCompanyId + "_ban_tbl (email)";
					executeWithRetry(0, 3, 120, sql);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			return false;
		}
		return true;
	}
	
	@Override
	public boolean existTrackingTables(int companyID) {
		if (!DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_val_alpha_tbl")) {
			return false;
		}

		return DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_ext_link_tbl");
	}

	/**
	 * returns true, if mailtracking for this companyID is active.
	 */
	@Override
	public boolean isMailtrackingActive(int companyID) {
		try {
			return selectInt("SELECT mailtracking FROM company_tbl WHERE company_id = ?", companyID) == 1;
		} catch (Exception e) {
			logger.error("isMailtrackingActive goes wrong.", e);
			return false;
		}
	}

	@Override
	public boolean isCreatorId(int companyId, int creatorId) {
		String sqlGetCount = "SELECT COUNT(*) FROM company_tbl WHERE company_id = ? AND creator_company_id = ?";
		return selectInt(sqlGetCount, companyId, creatorId) > 0;
	}

	@Override
	public String getRedirectDomain(int companyId) {
		final String sql = "SELECT rdir_domain FROM company_tbl WHERE company_id = ?";
		return selectObjectDefaultNull(sql, (rs, index) -> rs.getString("rdir_domain"), companyId);
	}

	@Override
	public boolean checkDeeptrackingAutoActivate(int companyID) {
		return selectInt("SELECT auto_deeptracking FROM company_tbl WHERE company_id = ?", companyID) == 1;
	}
	@Override 
	public void setAutoDeeptracking(int companyID, boolean active) {
		String sql = "UPDATE company_tbl SET auto_deeptracking = ? WHERE company_id = ?";
		update(sql, active ? 1 : 0, companyID);
	}

	@Override
	public int getCompanyDatasource(int companyID) {
		return selectIntWithDefaultValue("SELECT default_datasource_id FROM company_tbl WHERE company_id = ?", -1, companyID);
	}

	/**
	 * This method gets a list with all NOT DELETED companys from our DB.
	 */
	@Override
	public List<Company> getAllActiveCompaniesWithoutMasterCompany() {
		String sql = "SELECT company_id, creator_company_id, shortname, description, rdir_domain, mailloop_domain, status, mailtracking, stat_admin, secret_key, uid_version, auto_mailing_report_active, sector, business_field, max_recipients, salutation_extended, enabled_uid_version, parent_company_id, contact_tech FROM company_tbl WHERE status IN ('" + CompanyStatus.ACTIVE.getDbValue() + "', '" + CompanyStatus.LOCKED.getDbValue() + "') AND company_id > 1 ORDER BY company_id";
		try {
			return select(sql, new Company_RowMapper());
		} catch (Exception e) {
			// return an empty list, for no further actions
			return new ArrayList<>();
		}
	}
	
	/**
	 * This method returns a list of all companies, with status ='active' starting from a given companyID
	 */
	@Override
	public List<Company> getActiveCompaniesWithoutMasterCompanyFromStart(int startCompany) {
		String sql = "SELECT company_id, creator_company_id, shortname, description, rdir_domain, mailloop_domain, status, mailtracking, stat_admin, secret_key, uid_version, auto_mailing_report_active, sector, business_field, max_recipients, salutation_extended, enabled_uid_version, parent_company_id, contact_tech FROM company_tbl WHERE status IN ('" + CompanyStatus.ACTIVE.getDbValue() + "', '" + CompanyStatus.LOCKED.getDbValue() + "')  AND company_id >= ? ORDER BY company_id";
		try {
			return select(sql, new Company_RowMapper(), startCompany);
		} catch (Exception e) {
			// return an empty list, for no further actions
			return new ArrayList<>();
		}
	}

    @Override
   	public List<Integer> getAllActiveCompaniesIds(boolean includeMasterCompany) {
   		return select("SELECT company_id from company_tbl" +
                " WHERE status = '" + CompanyStatus.ACTIVE.getDbValue() + "'"
                + (includeMasterCompany ? "" : " AND company_id > 1"), IntegerRowMapper.INSTANCE);
   	}

	@Override
	public List<Integer> getCompaniesIds() {
		return select("SELECT company_id from company_tbl", IntegerRowMapper.INSTANCE);
	}

	@Override
	public boolean createHistoryTables(int companyID) {
		if (!DbUtilities.checkIfTableExists(getDataSource(), "hst_customer_" + companyID + "_binding_tbl")) {
			boolean createPreventConstraint = true;
			if (!configService.getBooleanValue(ConfigValue.UsePreventTableDropConstraint, companyID)) {
				createPreventConstraint = false;
			}
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
							+ "entry_mailing_id NUMBER, "
							+ "mediatype NUMBER, "
							+ "change_type NUMBER, "
							+ "timestamp_change DATE, "
							+ "client_info VARCHAR2(150), "
							+ "email VARCHAR2(100), "
							+ "referrer VARCHAR2(4000)"
							+ ")"
							+ tablespaceClause;
					executeWithRetry(0, 3, 120, sql);

					String tablespaceClauseIndex = "";
					if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_CUSTOMER_HISTORY_INDEX)) {
						tablespaceClauseIndex = " TABLESPACE " + TABLESPACE_CUSTOMER_HISTORY_INDEX;
					}
					executeWithRetry(0, 3, 120, "CREATE INDEX hstcb" + companyID + "$email$idx ON hst_customer_" + companyID + "_binding_tbl (email)" + tablespaceClauseIndex);
					executeWithRetry(0, 3, 120, "CREATE INDEX hstcb" + companyID + "$mlidcidl$idx ON hst_customer_" + companyID + "_binding_tbl (mailinglist_id, customer_id)" + tablespaceClauseIndex);
					executeWithRetry(0, 3, 120, "CREATE INDEX hstcb" + companyID + "$tsch$idx ON hst_customer_" + companyID + "_binding_tbl (timestamp_change)" + tablespaceClauseIndex);

					sql = "ALTER TABLE hst_customer_" + companyID + "_binding_tbl ADD CONSTRAINT hstcb" + companyID + "$pk PRIMARY KEY (customer_id, mailinglist_id, mediatype, timestamp_change)";
					executeWithRetry(0, 3, 120, sql);
					if (createPreventConstraint) {
						sql = "ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$hcustomer_" + companyID + "_bind FOREIGN KEY (customer_id, mailinglist_id, mediatype, change_date) REFERENCES hst_customer_" + companyID + "_binding_tbl (customer_id, mailinglist_id, mediatype, timestamp_change)";
						executeWithRetry(0, 3, 120, sql);
					}
				} else {
					sql = "CREATE TABLE hst_customer_" + companyID + "_binding_tbl "
							+ "("
							+ "customer_id INTEGER UNSIGNED, "
							+ "mailinglist_id INTEGER UNSIGNED, "
							+ "user_type CHAR(1) COLLATE utf8mb4_bin, "
							+ "user_status INT(11), "
							+ "user_remark VARCHAR(150), "
							+ "timestamp TIMESTAMP NULL, "
							+ "creation_date TIMESTAMP NULL, "
							+ "exit_mailing_id INT(11), "
							+ "entry_mailing_id INT(11), "
							+ "mediatype INTEGER UNSIGNED, "
							+ "change_type INT(11), "
							+ "timestamp_change TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
							+ "client_info VARCHAR(150), "
							+ "email VARCHAR(100), "
							+ "referrer VARCHAR(4000)"
							+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
					executeWithRetry(0, 3, 120, sql);
					
					executeWithRetry(0, 3, 120, "CREATE INDEX hstcb" + companyID + "$email$idx ON hst_customer_" + companyID + "_binding_tbl (email)");
					executeWithRetry(0, 3, 120, "CREATE INDEX hstcb" + companyID + "$mlidcidl$idx ON hst_customer_" + companyID + "_binding_tbl (mailinglist_id, customer_id)");
					executeWithRetry(0, 3, 120, "CREATE INDEX hstcb" + companyID + "$tsch$idx ON hst_customer_" + companyID + "_binding_tbl (timestamp_change)");

					sql = "ALTER TABLE hst_customer_" + companyID + "_binding_tbl ADD CONSTRAINT hstcb" + companyID + "$pk PRIMARY KEY (customer_id, mailinglist_id, mediatype, timestamp_change)";
					executeWithRetry(0, 3, 120, sql);
					if (createPreventConstraint) {
						sql = "ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$hcustomer_" + companyID + "_bind FOREIGN KEY (customer_id, mailinglist_id, mediatype, change_date) REFERENCES hst_customer_" + companyID + "_binding_tbl (customer_id, mailinglist_id, mediatype, timestamp_change)";
						executeWithRetry(0, 3, 120, sql);
					}
				}
				
				bindingHistoryDao.recreateBindingHistoryTrigger(companyID);
				
				return true;
			} catch(Exception e) {
				logger.error(String.format("createHistoryTables: SQL: %s\n%s", sql, e), e);
				
				return false;
			}
		} else {
			//tables existing
			return true;
		}
	}

	/**
	 * This method gets a list with all NOT DELETED companies from our DB.
	 */
	@Override
	public List<Company> getAllActiveCompanies() {
		String sql = "SELECT company_id, creator_company_id, shortname, description, rdir_domain, mailloop_domain, status, mailtracking, stat_admin, secret_key, uid_version, auto_mailing_report_active, sector, business_field, max_recipients, salutation_extended, enabled_uid_version, parent_company_id, contact_tech FROM company_tbl WHERE status = '" + CompanyStatus.ACTIVE.getDbValue() + "' ORDER BY company_id DESC";
		try {
			return select(sql, new Company_RowMapper());
		} catch (Exception e) {
			// return an empty list, for no further actions
			return new ArrayList<>();
		}
	}

	private static class Company_RowMapper implements RowMapper<Company> {
		@Override
		public Company mapRow(ResultSet resultSet, int row) throws SQLException {
			Company readCompany = new CompanyImpl();
			
			readCompany.setId(resultSet.getInt("company_id"));
			readCompany.setCreatorID(resultSet.getInt("creator_company_id"));
			readCompany.setShortname(resultSet.getString("shortname"));
			readCompany.setDescription(resultSet.getString("description"));
			readCompany.setRdirDomain(resultSet.getString("rdir_domain"));
			readCompany.setMailloopDomain(resultSet.getString("mailloop_domain"));
			try {
				readCompany.setStatus(CompanyStatus.getCompanyStatus(resultSet.getString("status")));
			} catch (Exception e) {
				throw new SQLException("Invalid company status value: " + resultSet.getString("status"));
			}
			readCompany.setMailtracking(resultSet.getInt("mailtracking"));
			readCompany.setStatAdmin(resultSet.getInt("stat_admin"));
			readCompany.setSector(resultSet.getInt("sector"));
			readCompany.setBusiness(resultSet.getInt("business_field"));
			readCompany.setMaxRecipients(resultSet.getInt("max_recipients"));
			readCompany.setSecretKey(resultSet.getString("secret_key"));
			readCompany.setMinimumSupportedUIDVersion(resultSet.getInt("uid_version"));
			readCompany.setAutoMailingReportSendActivated(resultSet.getInt("auto_mailing_report_active") == 1);
			readCompany.setSalutationExtended(resultSet.getInt("salutation_extended"));
			readCompany.setEnabledUIDVersion(resultSet.getInt("enabled_uid_version"));
			readCompany.setParentCompanyId( resultSet.getInt("parent_company_id"));
			readCompany.setContactTech(resultSet.getString("contact_tech"));
			
			return readCompany;
		}
	}

	@Override
	public int getNumberOfCompanies() {
		return selectInt("SELECT COUNT(*) FROM company_tbl WHERE status = ?", CompanyStatus.ACTIVE.getDbValue());
	}

	@Override
	public int getNumberOfCustomers(int companyID) {
		return selectInt("SELECT COUNT(*) FROM customer_" + companyID + "_tbl WHERE " + RecipientStandardField.Bounceload.getColumnName() + " = 0");
	}

	@Override
	public int getMaximumNumberOfCustomers() {
		int maximumNumberOfCustomers = 0;
		for (Integer companyID : select("SELECT company_id FROM company_tbl WHERE status = ?", IntegerRowMapper.INSTANCE, CompanyStatus.ACTIVE.getDbValue())) {
			if (DbUtilities.checkIfTableExists(getDataSource(), "customer_" + companyID + "_tbl")) {
				int numberOfCustomers = getNumberOfCustomers(companyID);
				maximumNumberOfCustomers = Math.max(maximumNumberOfCustomers, numberOfCustomers);
			}
		}
		return maximumNumberOfCustomers;
	}

	@Override
	public int getNumberOfProfileFields(int companyID) {
		return DbUtilities.getColumnCount(getDataSource(), "customer_" + companyID + "_tbl");
	}

	@Override
	public List<Tuple<String,String>> getCompanyInfo(int companyID){
		String query = "SELECT cname, cvalue FROM company_info_tbl WHERE company_id IN (0, ?) ORDER BY company_id";
		return select(query, (resultSet, i) -> new Tuple<>(resultSet.getString("cname"), resultSet.getString("cvalue")), companyID);
	}

	@Override
	public Map<String, Object> getCompanySettings(int companyID){
		String query = "SELECT shortname, mailtracking, secret_key, rdir_domain, mailloop_domain, status, mails_per_day FROM company_tbl WHERE company_id = ?";
		return selectSingleRow(query, companyID);
	}

	@Override
	public List<Map<String, Object>> getReferenceTableSettings(int companyID){
		String query = "SELECT name, reftable, refsource, refcolumn, backref, joincondition, order_by, voucher, voucher_renew " +
				" FROM reference_tbl WHERE company_id = ? AND (deleted IS NULL OR deleted = 0)";
		return select(query, companyID);
	}

	private List<Integer> getSampleFormIDs() {
		return select("SELECT form_id FROM userform_tbl WHERE company_id = 1 AND (LOWER(formname) LIKE '%sample%' OR LOWER(formname) LIKE '%example%' OR LOWER(formname) LIKE '%muster%' OR LOWER(formname) LIKE '%beispiel%' OR LOWER(formname) = ?)",
				IntegerRowMapper.INSTANCE,
				configService.getValue(ConfigValue.FullviewFormName));
	}
	
	@Override
	public void createCompanyPermission(int companyID, Permission permission, String comment) {
		if (selectInt("SELECT COUNT(*) FROM permission_tbl WHERE permission_name = ?", permission.getTokenString()) <= 0) {
			logger.warn("Permission to be granted by license does not exist in database: " + permission.getTokenString());
		} else {
			if (companyID >= 0 && !hasCompanyPermission(companyID, permission)) {
				update("INSERT INTO company_permission_tbl (company_id, permission_name, description, creation_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", companyID, permission.getTokenString(), comment);
			}
		}
	}

	@Override
	public boolean hasCompanyPermission(int companyID, Permission permission) {
		return selectInt("SELECT COUNT(*) FROM company_permission_tbl WHERE (company_id = ? OR company_id = 0) AND permission_name = ?", companyID, permission.getTokenString()) > 0;
	}

	@Override
	public Set<Permission> getCompanyPermissions(int companyID) {
		if (configService.getIntegerValue(ConfigValue.System_Licence) == 0) {
			// Only OpenEMM is allowed everything
			return new HashSet<>(Permission.getAllSystemPermissions());
		} else {
			List<String> result = select("SELECT DISTINCT permission_name FROM company_permission_tbl WHERE company_id = ? OR company_id = 0", StringRowMapper.INSTANCE, companyID);
			Set<Permission> returnSet = new HashSet<>();
			for (String securityToken: result) {
				Permission permission = Permission.getPermissionByToken(securityToken);
				if (permission != null) {
					returnSet.add(permission);
				}
			}
			return returnSet;
		}
	}

	@Override
	public void deleteCompanyPermission(int companyID, Permission permission) {
		update("DELETE FROM company_permission_tbl WHERE company_id = ? AND permission_name = ?", companyID, permission.getTokenString());
	}
	
	@Override
	public boolean deleteAllCompanyPermission(int companyID) {
		int touchedLines = update("DELETE FROM company_permission_tbl WHERE company_id = ?", companyID);
		if (touchedLines > 0) {
			return true;
		} else {
			return selectInt("SELECT COUNT(*) FROM company_permission_tbl WHERE company_id = ?", companyID) == 0;
		}
	}

	@Override
	public void setupPremiumFeaturePermissions(Set<String> allowedPremiumFeatures, Set<String> unAllowedPremiumFeatures, String comment, int companyID) {
		if (allowedPremiumFeatures != null) {
			for (String allowedSecurityToken : allowedPremiumFeatures) {
				Permission grantedPremiumPermission = Permission.getPermissionByToken(allowedSecurityToken);
				if (grantedPremiumPermission != null) {
					try {
						if (isPremiumPermissionAllowedOnlyForMasterCompany(grantedPremiumPermission)) {
							deleteCompanyPermission(companyID, grantedPremiumPermission);
							createCompanyPermission(1, grantedPremiumPermission, comment);
						} else {
							createCompanyPermission(companyID, grantedPremiumPermission, comment);
						}
					} catch (Exception e) {
						logger.error(String.format("Cannot activate premium permission for company " + companyID + ": %s", grantedPremiumPermission.getTokenString()));
					}
				} else {
					logger.warn(String.format("Found non-existing granted premium permission for company " + companyID + ": %s", allowedSecurityToken));
				}
			}
		}
		
		if (unAllowedPremiumFeatures != null && !unAllowedPremiumFeatures.isEmpty()) {
			Object[] parameters = unAllowedPremiumFeatures.toArray(new String[0]);
			
			List<String> foundUnAllowedPremiumFeatures_Admin = select("SELECT permission_name FROM company_permission_tbl WHERE company_id = " + companyID + " AND permission_name IN (" + AgnUtils.repeatString("?", unAllowedPremiumFeatures.size(), ", ") + ")", StringRowMapper.INSTANCE, parameters);
			
			int touchedLines1 = update("DELETE FROM company_permission_tbl WHERE company_id = " + companyID + " AND permission_name IN (" + AgnUtils.repeatString("?", unAllowedPremiumFeatures.size(), ", ") + ")", parameters);
			if (touchedLines1 > 0) {
				logger.warn(String.format("Deleted unallowed premium features for company " + companyID + ": %d", touchedLines1));
				logger.warn(StringUtils.join(foundUnAllowedPremiumFeatures_Admin, ", "));
			}
		}
	}

	protected boolean isPremiumPermissionAllowedOnlyForMasterCompany(Permission permission) {
		return false;
	}

	@Override
	public void cleanupPremiumFeaturePermissions(int companyID) {
		update("DELETE FROM company_permission_tbl WHERE company_id = ?", companyID);
	}
	
	@Override
	public boolean addExecutiveAdmin(int companyID, int executiveAdminID) {
		return(update("UPDATE company_tbl SET stat_admin = ? WHERE company_id = ?", executiveAdminID, companyID)) == 1;
	}
	
	@Override
	public void changeFeatureRights(String featureName, int companyID, boolean activate, String comment) {
		List<Integer> subCompanyIDs = select("SELECT company_id FROM company_tbl WHERE parent_company_id = ? AND status = ?", IntegerRowMapper.INSTANCE, companyID, CompanyStatus.ACTIVE.dbValue);
		List<String> rightsToSet = select("SELECT permission_name FROM permission_tbl WHERE feature_package = ?", StringRowMapper.INSTANCE, featureName);

		for (String rightToCheck : rightsToSet) {
			if (Permission.getPermissionByToken(rightToCheck) != null) {
				if (activate) {
					createCompanyPermission(companyID, Permission.getPermissionByToken(rightToCheck), comment);
					for (int subCompanyID : subCompanyIDs) {
						createCompanyPermission(subCompanyID, Permission.getPermissionByToken(rightToCheck), comment);
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

	@Override
	public int getPriorityCount(int companyId) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void setPriorityCount(int companyId, int value) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean isCompanyExist(int companyId) {
        return selectInt("SELECT count(company_id) FROM company_tbl WHERE company_id = ?", companyId) > 0;
	}

	@Override
	public String getShortName(int companyId) {
		String query = "SELECT shortname FROM company_tbl WHERE company_id = ?";
		return selectWithDefaultValue(query, String.class, null, companyId);
	}

	@Override
	public void deactivateExtendedCompanies() {
		update("UPDATE company_tbl SET status = '" + CompanyStatus.LOCKED.getDbValue() + "', timestamp = CURRENT_TIMESTAMP WHERE company_id IS NULL OR company_id <> 1");
	}

	@Override
	public int selectForTestCompany() {
		return selectIntWithDefaultValue("SELECT company_id FROM company_tbl WHERE shortname like 'OpenEMM Test%' AND status = '" + CompanyStatus.LOCKED.getDbValue() + "' LIMIT 1", 0);
	}
	
	@Override
	public int selectNumberOfExistingTestCompanies() {
		return selectIntWithDefaultValue("SELECT COUNT(*) FROM company_tbl WHERE shortname like 'OpenEMM Test%'", 0);
	}
	
	@Override
	public boolean isCompanyNameUnique(String shortname) {
		return selectInt("SELECT count(*) FROM company_tbl WHERE shortname = ?", shortname) == 0;
	}
	
	@Override
	public int getParenCompanyId(int companyId) {
		return selectInt("SELECT COALESCE(parent_company_id, 0) FROM company_tbl WHERE company_id = ?", companyId);
	}
	
	@Override
	public boolean createFrequencyFields(int companyID) {
		try {
			if (isOracleDB()) {
				executeWithRetry(0, 3, 120, "ALTER TABLE customer_" + companyID + "_tbl ADD freq_count_day NUMBER");
				executeWithRetry(0, 3, 120, "ALTER TABLE customer_" + companyID + "_tbl ADD freq_count_week NUMBER");
				executeWithRetry(0, 3, 120, "ALTER TABLE customer_" + companyID + "_tbl ADD freq_count_month NUMBER");
				
				String tablespaceClauseIndex = "";
				if (DbUtilities.checkOracleTablespaceExists(getDataSource(), TABLESPACE_INDEX_CUSTOMER)) {
					tablespaceClauseIndex = " TABLESPACE " + TABLESPACE_INDEX_CUSTOMER;
				}
				executeWithRetry(0, 3, 120, "CREATE INDEX cust" + companyID + "$freq$idx ON customer_" + companyID + "_tbl (freq_count_day, freq_count_week,freq_count_month ) " + tablespaceClauseIndex);
			} else {
				executeWithRetry(0, 3, 120, "ALTER TABLE customer_" + companyID + "_tbl ADD COLUMN freq_count_day INT(11)");
				executeWithRetry(0, 3, 120, "ALTER TABLE customer_" + companyID + "_tbl ADD COLUMN freq_count_week INT(11)");
				executeWithRetry(0, 3, 120, "ALTER TABLE customer_" + companyID + "_tbl ADD COLUMN freq_count_month INT(11)");
				executeWithRetry(0, 3, 120, "CREATE INDEX cust" + companyID + "$freq$idx ON customer_" + companyID + "_tbl (freq_count_day, freq_count_week,freq_count_month )");
			}
		} catch (Exception e) {
			logger.error(String.format("Cannot add missing fields (CID %d): %s", companyID, e.getMessage()), e);
		}
		return true;
	}
	
	@Override
	public Company getCompanyByName(String companyName) {
		if (StringUtils.isBlank(companyName)) {
			return null;
		}

		try {
			String sql = "SELECT company_id, creator_company_id, shortname, description, rdir_domain, mailloop_domain, status, mailtracking, stat_admin, secret_key, uid_version, auto_mailing_report_active, sector, business_field, max_recipients, salutation_extended, enabled_uid_version, parent_company_id, contact_tech FROM company_tbl WHERE shortname = ?";
			List<Company> list = select(sql, new Company_RowMapper(), companyName);
			if (!list.isEmpty()) {
				return list.get(0);
			}

			return null;
		} catch (Exception e) {
			throw new RuntimeException("Cannot read company data for name: " + companyName, e);
		}
	}

	@Override
	public final Optional<String> getCompanyToken(final int companyID) throws UnknownCompanyIdException {
		final List<String> list = select("SELECT company_token FROM company_tbl WHERE company_id = ?", StringRowMapper.INSTANCE, companyID);
		
		if (list.isEmpty()) {
			throw new UnknownCompanyIdException(companyID);
		}
		
		return Optional.ofNullable(list.get(0));
	}

    @Override
   	public List<Integer> getActiveCompanies(CompaniesConstraints constraints) {
        return select("SELECT company_id FROM company_tbl WHERE status = ?" +
                        DbUtilities.asCondition(" AND %s", constraints),
                IntegerRowMapper.INSTANCE, CompanyStatus.ACTIVE.getDbValue());
   	}

	@Override
	public String getSpamCheckAddress(int companyID) {
		String newTestVipEmailAddress = configService.getValue(ConfigValue.InitialTestVipEmailAddress);
		if (StringUtils.isBlank(newTestVipEmailAddress)) {
			newTestVipEmailAddress = select("SELECT email FROM admin_tbl WHERE admin_id = (SELECT stat_admin FROM company_tbl WHERE company_id = ?)", String.class, companyID);
		}
		return newTestVipEmailAddress;
	}

	@Override
	public void createMissingOpenemmPlusPermissions() {
		// do nothing
	}
}
