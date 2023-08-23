/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.util.Objects;

import javax.sql.DataSource;

import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.RecipientDuplicateSqlOptions;
import org.agnitas.service.RecipientOptions;
import org.agnitas.service.RecipientQueryBuilder;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.service.ColumnInfoService;

/**
 * Helper-class for building the sql-query in /recipient/list.jsp
 */
public class RecipientQueryBuilderImpl implements RecipientQueryBuilder {

    /** The logger. */
    private static final transient Logger logger = LogManager.getLogger(RecipientQueryBuilderImpl.class);

    /** DAO for target groups. */
    protected ComTargetDao targetDao;

    /** Service for accessing DB column metadata. */
    protected ColumnInfoService columnInfoService;
    protected MailinglistApprovalService mailinglistApprovalService;

    protected DataSource dataSource;

    /** Facade providing full EQL functionality. */
    protected EqlFacade eqlFacade;

    /**
     * Cache variable for the dataSource vendor, so it must not be recalculated everytime.
     * This variable may be uninitialized before the first execution of the isOracleDB method
     */
    protected Boolean isOracleDB = null;


    private ConfigService configService;

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * Set DAO for target groups.
     *
     * @param targetDao DAO for target groups
     */
    @Required
    public void setTargetDao(ComTargetDao targetDao) {
        this.targetDao = targetDao;
    }

    @Required
    public void setMailinglistApprovalService(final MailinglistApprovalService service) {
        this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }

    /**
     * Set service for DB column meta data.
     *
     * @param service ColumnInfoService
     */
    @Required
    public void setColumnInfoService(ColumnInfoService service) {
        this.columnInfoService = service;
    }

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Required
    public void setEqlFacade(EqlFacade eqlFacade) {
        this.eqlFacade = eqlFacade;
    }

    public final ColumnInfoService getColumnInfoService() {
        return this.columnInfoService;
    }

    @Override
    public SqlPreparedStatementManager getDuplicateAnalysisSQLStatement(com.agnitas.beans.Admin admin, RecipientDuplicateSqlOptions options, boolean includeBounceLoad) throws Exception {
        logger.warn("getDuplicateAnalysisSQLStatement is unsupported!");
        return null;
    }

    @Override
    public SqlPreparedStatementManager getDuplicateAnalysisSQLStatement(com.agnitas.beans.Admin admin, RecipientDuplicateSqlOptions options, java.util.List<String> selectedColumns, boolean includeBounceLoad) throws Exception {
        logger.warn("getDuplicateAnalysisSQLStatement is unsupported!");
        return null;
    }

    protected void addBindingCheck(final int companyId, final int adminId, final RecipientOptions options, final SqlPreparedStatementManager mainStatement, boolean isDuplicate) throws Exception {
        SqlPreparedStatementManager sqlCheckBinding = createBindingCheckQuery(companyId, adminId, options);
        // The mailingListId == -1 means "No binding", but ignored ("All" option used instead) in restricted mode (when checkDisabledMailingLists == true).
        if (options.getListId() >= 0) {
            // Binding must be present for customer to pass filters. Except user has ALML (see GWUA-5207)
            String whereClause = asExistsClause(sqlCheckBinding, false);

            if (isDuplicate && options.isUserTypeEmpty()) {
                SqlPreparedStatementManager checkIfUserStatusNotExists = createBindingCheckQuery(companyId, adminId, 0, 0, "");
                whereClause += " OR " + asExistsClause(checkIfUserStatusNotExists, false);
            }
            mainStatement.addWhereClause(whereClause, sqlCheckBinding.getPreparedSqlParameters());
        } else {
            // Binding must be absent for customer to pass.
            mainStatement.addWhereClause(asExistsClause(sqlCheckBinding, false), sqlCheckBinding.getPreparedSqlParameters());
        }
    }

    protected String asExistsClause(SqlPreparedStatementManager statement, boolean isPositive) {
        return String.format((isPositive ? "EXISTS (%s)" : "NOT EXISTS (%s)"), statement.getPreparedSqlString());
    }

    protected SqlPreparedStatementManager createBindingCheckQuery(int companyId, int adminId, RecipientOptions options) throws Exception {
        return createBindingCheckQuery(companyId, adminId, options.getListId(), options.getUserStatus(), options.getUserType());
    }

    protected SqlPreparedStatementManager createBindingCheckQuery(int companyId, int adminId, int mailingListId, int userStatus, String userType) throws Exception {
        SqlPreparedStatementManager sqlCheckBinding;

        sqlCheckBinding = new SqlPreparedStatementManager("SELECT 1 FROM customer_" + companyId + "_binding_tbl bind");

        sqlCheckBinding.addWhereClause("bind.customer_id = cust.customer_id");

        if (mailingListId > 0) {
            sqlCheckBinding.addWhereClause("bind.mailinglist_id = ?", mailingListId);
        }

        // The mailingListId < 0 means "No binding", but this option is ignored in restricted mode (when disabled mailing lists to be checked).
        if (mailingListId >= 0) {
            if (userStatus != 0) {
                // Check for valid UserStatus code
                UserStatus.getUserStatusByID(userStatus);

                sqlCheckBinding.addWhereClause("bind.user_status = ?", userStatus);
            }

            if (StringUtils.isNotBlank(userType)) {
                sqlCheckBinding.addWhereClause("bind.user_type = ?", userType);
            }
        }

        return sqlCheckBinding;
    }

    /**
     *  Checks if a customer binding check (its presence or its absence) to pass a filter (otherwise all unbound customers will be excluded).
     */
    protected boolean isBindingCheckRequired(RecipientOptions options) {
    	return options.getListId() != 0 || options.getUserStatus() != 0 || StringUtils.isNotBlank(options.getUserType());
    }

    protected boolean isOracleDB() {
        if (isOracleDB == null) {
            isOracleDB = DbUtilities.checkDbVendorIsOracle(dataSource);
        }
        return isOracleDB;
    }

    protected void addTargetWhereClause(RecipientOptions options, int companyId, SqlPreparedStatementManager mainStatement) throws Exception {
        if (options.getTargetId() > 0) {
            mainStatement.addWhereClause(targetDao.getTarget(options.getTargetId(), companyId).getTargetSQL());
        }

        addExtendedTargetWhereClause(options, companyId, mainStatement);
    }

    protected void addExtendedTargetWhereClause(RecipientOptions options, int companyId, SqlPreparedStatementManager mainStatement) throws Exception {
        // nothing to do
    }

    protected void addBounceLoad(int companyId, SqlPreparedStatementManager mainStatement) throws Exception {
        mainStatement.addWhereClause(ComCompanyDaoImpl.STANDARD_FIELD_BOUNCELOAD + " = 0");

        boolean respectHideSign = configService.getBooleanValue(ConfigValue.RespectHideDataSign, companyId);
        if (respectHideSign) {
            mainStatement.addWhereClause("(hide <= 0 OR hide IS NULL)");
        }
    }
}
