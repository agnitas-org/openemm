/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.exception.target.TargetGroupLockedException;
import org.agnitas.dao.exception.target.TargetGroupPersistenceException;
import org.agnitas.dao.exception.target.TargetGroupTooLargeException;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.target.TargetFactory;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.FulltextSearchQueryException;
import org.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.agnitas.beans.ComTarget;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.impl.mapper.TargetLightRowMapper;
import com.agnitas.dao.impl.mapper.TargetRowMapper;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.commons.database.fulltext.FulltextSearchQueryGenerator;
import com.agnitas.emm.core.target.beans.RawTargetGroup;
import com.agnitas.emm.core.target.beans.TargetGroupDependentType;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCode;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCodeProperties;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.phloc.commons.collections.pair.Pair;

/**
 * Implementation of {@link ComTargetDao}.
 */
public class ComTargetDaoImpl extends PaginatedBaseDaoImpl implements ComTargetDao, InitializingBean {

	/** Maximum length of target SQL. */
	public static final int TARGET_GROUP_SQL_MAX_LENGTH = 4000;

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComTargetDaoImpl.class);

    /** Facade for EQL feature */
	private EqlFacade eqlFacade;

	private final RawTargetGroupRowMapper rawTargetGroupRowMapper = new RawTargetGroupRowMapper();

	private FulltextSearchQueryGenerator fulltextSearchQueryGenerator;

	private TargetFactory targetFactory;

	private static final TargetLightRowMapper targetLightRowMapper = new TargetLightRowMapper();

	/** Row Mapper for companies with enabled EQL support. */
	private TargetRowMapper targetRowMapperWithEql;

	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection

	@Required
	public void setTargetFactory(TargetFactory targetFactory) {
		this.targetFactory = targetFactory;
	}


	@Required
	public void setFulltextSearchQueryGenerator(FulltextSearchQueryGenerator fulltextSearchQueryGenerator) {
		this.fulltextSearchQueryGenerator = fulltextSearchQueryGenerator;
	}

	/**
	 * Set facade for EQL feature.
	 *
	 * @param facade facade for EQL feature
	 */
	@Required
	public void setEqlFacade(EqlFacade facade) {
		this.eqlFacade = facade;
	}

	@Override
	public void afterPropertiesSet() {
		targetRowMapperWithEql = new TargetRowMapper(targetFactory);
	}

	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic

	@Override
	public String getTargetName(int targetId, @VelocityCheck int companyId, boolean includeDeleted) {
		String sql = "SELECT target_shortname FROM dyn_target_tbl WHERE company_id = ? AND target_id = ?";
		if (!includeDeleted) {
			sql += " AND deleted <> 1";
		}
		return selectObjectDefaultNull(logger, sql, (rs, index) -> rs.getString("target_shortname"), companyId, targetId);
	}

	@Override
	public boolean isTargetNameInUse(@VelocityCheck int companyId, String targetName, boolean includeDeleted) {
		String sqlGetCount = "SELECT COUNT(*) FROM dyn_target_tbl WHERE company_id = ? AND target_shortname = ?";

		if (!includeDeleted) {
			sqlGetCount += " AND deleted <> 1";
		}

		return selectInt(logger, sqlGetCount, companyId, targetName) > 0;
	}

	@Override
	public List<String> getTargetNamesByIds(@VelocityCheck int companyId, Set<Integer> targetIds) {
		List<String> resultList = new ArrayList<>();
		if (targetIds.size() <= 0) {
			return resultList;
		}
		String sql = "SELECT target_shortname FROM dyn_target_tbl WHERE company_id = ? AND target_id in (" + StringUtils.join(targetIds, ", ") + ")";
		List<Map<String, Object>> list = select(logger, sql, companyId);
		for (Map<String, Object> map : list) {
			String targetName = (String) map.get("target_shortname");
			resultList.add(targetName);
		}
		return resultList;
	}

	@Override
	public List<Integer> getDeletedTargets(@VelocityCheck int companyID) {
		String sql = "SELECT target_id FROM dyn_target_tbl WHERE company_id = ? AND deleted = 1";
		List<Map<String, Object>> list = select(logger, sql, companyID);
		return list.stream()
				.map(row -> ((Number) row.get("target_id")).intValue())
				.collect(Collectors.toList());
	}

	@Override
	public Map<Integer, ComTarget> getAllowedTargets(@VelocityCheck int companyID) {
		Map<Integer, ComTarget> targets = new HashMap<>();
		String sql = "SELECT target_id, target_shortname, target_description, target_sql FROM dyn_target_tbl WHERE company_id = ? ORDER BY target_id";

		try {
			List<Map<String, Object>> list = select(logger, sql, companyID);

			for (Map<String, Object> map : list) {
				int id = ((Number) map.get("target_id")).intValue();
				String shortname = (String) map.get("target_shortname");
				String description = (String) map.get("target_description");
				String targetsql = (String) map.get("target_sql");
				ComTarget target = targetFactory.newTarget();

				target.setCompanyID(companyID);
				target.setId(id);
				if (shortname != null) {
					target.setTargetName(shortname);
				}
				if (description != null) {
					target.setTargetDescription(description);
				}
				if (targetsql != null) {
					target.setTargetSQL(targetsql);
				}
				targets.put(id, target);
			}
		} catch (Exception e) {
			logger.error("getAllowedTargets (sql: " + sql + ")", e);
			return null;
		}
		return targets;
	}

	@Override
	public Map<Integer, TargetLight> getAllowedTargetLights(@VelocityCheck int companyID) {
		Map<Integer, TargetLight> targets = new HashMap<>();
		String sql = "SELECT target_id, company_id, target_shortname, target_description, locked, creation_date, change_date, invalid, deleted, component_hide, complexity, invalid, favorite " + getTargetExtendedColumnsAsString() +
				"FROM dyn_target_tbl WHERE company_id = ? " +
				"ORDER BY target_id";

		List<TargetLight> list = select(logger, sql, getTargetLightRowMapper(), companyID);

		for (TargetLight targetLight : list) {
			targets.put(targetLight.getId(), targetLight);
		}

		return targets;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int saveTarget(ComTarget target) throws TargetGroupPersistenceException {
		return saveTarget(target, false);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int saveHiddenTarget(ComTarget target) throws TargetGroupPersistenceException {
		return saveTarget(target, true);
	}

	private static boolean isCodeBackendCompatible(final SqlCode sqlCode) {
		SqlCodeProperties properties = sqlCode.getCodeProperties();

		/*
		 * Code is backend compatible if,
		 * - no non-profile-tables are used
		 * - no reference tables are used
		 * - and generated SQL does not contain sub-selects
		 */

		return !properties.isUsingNonCustomerTables() && !properties.isUsingReferenceTables() && !properties.isUsingSubselects();
	}

	/**
	 * Saves target group. If {@code hidden} is false, the target group is visible in lists. If value is true,
	 * target group is hidden in lists. {@code hidden} is ignored, if target group already exists.
	 *
	 * @param target target group to save
	 * @param hidden hidden-flag
	 * @return ID of target group
	 * @throws TargetGroupPersistenceException on errors saving target group
	 */
	@DaoUpdateReturnValueCheck
	private int saveTarget(ComTarget target, boolean hidden) throws TargetGroupPersistenceException {
		boolean sqlTooLarge = false;

		if (target == null) {
			return 0;
		} else if (StringUtils.isBlank(target.getTargetName())) {
			throw new RuntimeException("Target is missing target name");
		}

		try {
			String eql = target.getEQL();
			SqlCode sqlCode = eqlFacade.convertEqlToSql(eql, target.getCompanyID());

			// Hide target group, if SQL code is not compatible with backend
			target.setComponentHide(!isCodeBackendCompatible(sqlCode));

			target.setTargetSQL(sqlCode.getSql());
			target.setValid(true);

		} catch(Exception e) {
			logger.error("Error converting target group " + target.getId() + " to EQL", e);

			// In case of an error, make target group selecting no recipients
			target.setTargetSQL("1=0");
			target.setValid(false);
		}

		/*
		 *  This check is only used to check, if SQL got too large and make valid SQL returning no recipients.
		 *  The exception itself is thrown after saving the target group.
		 *  When throwing exception here, neither EQL nor other properties are udpated in / inserted to DB
		 *  so the user will loose any modifications done in UI.
		 *
		 *  This is done to get EQL saved to DB.
		 */
		if(StringUtils.length(target.getTargetSQL()) > TARGET_GROUP_SQL_MAX_LENGTH) {
			logger.warn("Target group is too large: " + target.getId());

			target.setTargetSQL("1=0");
			target.setValid(false);

			sqlTooLarge = true;
		}

		if (isTargetGroupLocked(target.getId(), target.getCompanyID())) {
			throw new TargetGroupLockedException(target.getId());
		}

		try {
			target.setChangeDate(new Date());

			if (target.getId() == 0) {
				if (target.getCreationDate() == null) {
					// some tests set the creation date
					target.setCreationDate(new Date());
				}
                List<Object> params = new ArrayList<>(Arrays.asList(
                    target.getCompanyID(),
                    target.getTargetSQL(),
                    target.getTargetName(),
                    target.getTargetDescription(),
                    target.getCreationDate(),
                    target.getChangeDate(),
                    target.getDeleted(),
                    BooleanUtils.toInteger(target.isAdminTestDelivery()),
                    BooleanUtils.toInteger(target.getComponentHide()),
                    BooleanUtils.toInteger(hidden),
                    target.getEQL(),
                    target.getComplexityIndex(),
                    target.isValid() ? 0 : 1	// Note: Property is "valid", table column is "invalid"!'
                ));
                String additionalColumns = DbUtilities.joinColumnsNames(getAdditionalExtendedColumns(), true);
                params.addAll(getAdditionalExtendedParams(target));

				if (isOracleDB()) {
                    target.setId(selectInt(logger, "SELECT dyn_target_tbl_seq.NEXTVAL FROM DUAL"));
                    params.add(0, target.getId());
					update(logger, "INSERT INTO dyn_target_tbl (target_id, company_id, target_sql, target_shortname, target_description, creation_date, change_date, deleted, admin_test_delivery, component_hide, hidden, eql, complexity, invalid" + additionalColumns
                            + ") VALUES (" + AgnUtils.repeatString("?", params.size(), ", ") + ")", params.toArray());
				} else {
                    int targetID = insertIntoAutoincrementMysqlTable(logger, "target_id", "INSERT INTO dyn_target_tbl (company_id, target_sql, target_shortname, target_description, creation_date, change_date, deleted, admin_test_delivery, component_hide, hidden, eql, complexity, invalid" + additionalColumns
                            + ") VALUES (" + AgnUtils.repeatString("?", params.size(), ", ") + ")", params.toArray());
					target.setId(targetID);
				}
			} else {
				update(logger, "UPDATE dyn_target_tbl SET target_sql = ?, target_shortname = ?, target_description = ?, deleted = ?, change_date = ?, admin_test_delivery = ?, eql=?, complexity = ?, invalid=?, component_hide = ? WHERE target_id = ? AND company_id = ?",
					target.getTargetSQL(),
					target.getTargetName(),
					target.getTargetDescription(),
					target.getDeleted(),
					target.getChangeDate(),
					BooleanUtils.toInteger(target.isAdminTestDelivery()),
					target.getEQL(),
					target.getComplexityIndex(),
					target.isValid() ? 0 : 1,	// Note: Property is "valid", table column is "invalid"!
					BooleanUtils.toInteger(target.getComponentHide()),
					target.getId(),
					target.getCompanyID()
					);
				
				if (!getAdditionalExtendedColumns().isEmpty()) {
					String sqlSetPart = StringUtils.join(getAdditionalExtendedColumns(), " = ?, ") + " = ?";
					List<Object> params = new ArrayList<>(getAdditionalExtendedParams(target));
					params.add(target.getId());
					params.add(target.getCompanyID());
	                update(logger, "UPDATE dyn_target_tbl SET " + sqlSetPart + " WHERE target_id = ? AND company_id = ?", params.toArray(new Object[0]));
				}

			}
		} catch (Exception e) {
			logger.error("Error saving target group " + target.getId(), e);
			target.setId(0);
		}

		// This check is only used to throw the exception, if the target SQL got too large
		if(sqlTooLarge) {
			throw new TargetGroupTooLargeException(target.getId());
		}

		return target.getId();
	}

	@Override
	public ComTarget getTarget(int targetID, @VelocityCheck int companyID) {
		final String sqlGetTarget = "SELECT target_id, company_id, target_description, target_shortname, target_sql, " +
				"deleted, creation_date, change_date, admin_test_delivery, locked, eql, COALESCE(complexity, -1) AS complexity, " +
				"invalid, component_hide " + getTargetExtendedColumnsAsString()
				+ " FROM dyn_target_tbl WHERE target_id = ? AND company_id = ?";

		if (isOracleDB()) {
			return selectObjectDefaultNull(logger, "SELECT * FROM (" + sqlGetTarget + ") WHERE ROWNUM = 1", getTargetRowMapper(), targetID, companyID);
		} else {
			return selectObjectDefaultNull(logger, sqlGetTarget + " LIMIT 1", getTargetRowMapper(), targetID, companyID);
		}
	}

	/**
	 * IMPORTANT NOTE: Target group names are not unique!
	 *
	 * The only exception to this are list split target groups. (Names of these target groups are unique.)
	 * This method MUST NOT be used for other target groups than list split target groups.
	 *
	 * TODO: Replace this method by a new method that takes list split data as parameters to avoid misuse of this method.
	 */
	@Override
	public ComTarget getTargetByName(String targetName, @VelocityCheck int companyID) {
		final StringBuilder sqlQueryBuilder = new StringBuilder();

		final List<String> columns = ListUtils.union(Arrays.asList(
				"target_id",
				"company_id",
				"target_description",
				"target_shortname",
				"target_sql",
				"deleted",
				"creation_date",
				"change_date",
				"admin_test_delivery",
				"locked",
				"invalid",
				"COALESCE(complexity, -1) AS complexity",
				"component_hide",
				"eql"
		), getTargetLightsExtendedColumns());

		if (isOracleDB()) {
			sqlQueryBuilder.append("SELECT * FROM (");
		}

		sqlQueryBuilder.append("SELECT ")
				.append(StringUtils.join(columns, ", "))
				.append(" FROM dyn_target_tbl")
				.append(" WHERE (company_id = ? OR company_id = 0) AND target_shortname = ?")
				// Prefer valid one if available
				.append(" ORDER BY invalid");

		if (isOracleDB()) {
			sqlQueryBuilder.append(") WHERE ROWNUM = 1");
		} else {
			sqlQueryBuilder.append(" LIMIT 1");
		}

		return selectObjectDefaultNull(logger, sqlQueryBuilder.toString(), getTargetRowMapper(), companyID, targetName);
	}

	@Override
	public ComTarget getListSplitTarget(String splitType, int index, @VelocityCheck int companyID) {
		ComTarget target = getListSplitTarget(TargetLight.LIST_SPLIT_PREFIX, splitType, index, companyID);
		if (target == null) {
			target = getListSplitTarget(TargetLight.LIST_SPLIT_CM_PREFIX, splitType, index, companyID);
		}
		return target;
	}

	@Override
	public ComTarget getListSplitTarget(String prefix, String splitType, int index, @VelocityCheck int companyID) {
		if (StringUtils.isEmpty(prefix) || StringUtils.isEmpty(splitType) || index < 0 || companyID <= 0) {
			return null;
		}

		return getTargetByName(prefix + splitType + "_" + index, companyID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteTarget(int targetID, @VelocityCheck int companyID) throws TargetGroupPersistenceException {
		if (isTargetGroupLocked(targetID, companyID)) {
			throw new TargetGroupLockedException(targetID);
		}

		int touchedLines = update(logger, "UPDATE dyn_target_tbl SET deleted = 1, change_date = CURRENT_TIMESTAMP WHERE target_id = ? AND company_id = ? AND deleted = 0", targetID, companyID);

		return touchedLines > 0;
	}

	@Override
	public boolean deleteTargetReally(int targetId, @VelocityCheck int companyId) {
		return update(logger, "DELETE FROM dyn_target_tbl WHERE company_id = ? AND target_id = ?", companyId, targetId) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteTargetsReally(@VelocityCheck int companyID) {
		if (companyID <= 0) {
			return false;
		}
		try {
			update(logger, "DELETE FROM dyn_target_tbl WHERE company_id = ?", companyID);
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
			return false;
		}
		return true;
	}

    @Override
    public boolean deleteWorkflowTargetConditions(@VelocityCheck int companyId, int workflowId) {
		String sql = "DELETE FROM dyn_target_tbl WHERE target_id IN " +
				"(SELECT entity_id FROM workflow_dependency_tbl WHERE company_id = ? AND workflow_id = ? AND type = ?)";

		return update(logger, sql, companyId, workflowId, WorkflowDependencyType.TARGET_GROUP_CONDITION.getId()) > 0;
    }

	@Override
	public void deleteWorkflowTargetConditions(int companyId) {
		if(companyId > 0) {
			String sql = "DELETE FROM dyn_target_tbl WHERE target_id IN " +
				"(SELECT entity_id FROM workflow_dependency_tbl WHERE company_id = ? AND type = ?)";

			update(logger, sql, companyId, WorkflowDependencyType.TARGET_GROUP_CONDITION.getId());
		}
	}

	@Override
	public String getTargetSplitName(int targetId) {
		return select(logger, "SELECT target_shortname FROM dyn_target_tbl WHERE target_id = ?", String.class, targetId);
	}

	@Override
	public int getTargetSplitID(String name) {
		String sqlGetTargetId = "SELECT target_id FROM dyn_target_tbl WHERE target_shortname = ?";
		if (isOracleDB()) {
			return selectIntWithDefaultValue(logger, "SELECT * FROM (" + sqlGetTargetId + ") WHERE ROWNUM = 1", -1, name);
		} else {
			return selectIntWithDefaultValue(logger, sqlGetTargetId + " LIMIT 1", -1, name);
		}
	}

	@Override
	public String getTargetSQL(int targetId, @VelocityCheck int companyId) {
		String sqlSelectSqlCode = "SELECT target_sql FROM dyn_target_tbl WHERE target_id = ? AND company_id = ?";
		return selectWithDefaultValue(logger, sqlSelectSqlCode, String.class, null, targetId, companyId);
	}

	@Override
	public List<String> getSplitNames(@VelocityCheck int companyID) {
		StringBuilder sqlQueryBuilder = new StringBuilder();
		List<Object> sqlParameters = new ArrayList<>();

		sqlQueryBuilder.append("SELECT DISTINCT target_shortname ")
				.append("FROM dyn_target_tbl ")
				.append("WHERE (company_id = ? OR company_id = 0) ");
		sqlParameters.add(companyID);

		sqlQueryBuilder.append("AND (")
				.append(makeShortNameMatchConditionClause(sqlParameters))
				.append(") AND admin_test_delivery = 0 ")
				.append("ORDER BY target_shortname");

		List<Map<String, Object>> result = select(logger, sqlQueryBuilder.toString(), sqlParameters.toArray());

		List<String> splitNames = new ArrayList<>();
		for (Map<String, Object> row : result) {
			splitNames.add((String) row.get("target_shortname"));
		}

		return splitNames;
	}

	@Override
	public int getSplits(@VelocityCheck int companyID, String splitType) {
		StringBuilder sqlQueryBuilder = new StringBuilder();
		List<Object> sqlParameters = new ArrayList<>();

		sqlQueryBuilder.append("SELECT COUNT(target_id) ")
				.append("FROM dyn_target_tbl ")
				.append("WHERE (company_id = ? OR company_id = 0) ");
		sqlParameters.add(companyID);

		sqlQueryBuilder.append("AND (")
				.append(makeShortNameMatchConditionClause(splitType, sqlParameters))
				.append(") AND admin_test_delivery = 0 ")
				.append("ORDER BY target_shortname");

		return selectInt(logger, sqlQueryBuilder.toString(), sqlParameters.toArray());
	}

	@Override
	public List<TargetLight> getTargetLights(@VelocityCheck int companyID) {
		return getTargetLights(companyID, false);
	}

	@Override
	public List<TargetLight> getTargetLights(@VelocityCheck int companyID, boolean includeDeleted) {
		return getTargetLights(companyID, includeDeleted, true, true);
	}

	@Override
	public List<TargetLight> getTargetLights(@VelocityCheck int companyID, boolean includeDeleted, boolean worldDelivery, boolean adminTestDelivery) {
		return getTargetLights(companyID, includeDeleted, worldDelivery, adminTestDelivery, false);
	}

	@Override
	public List<TargetLight> getTargetLights(@VelocityCheck int companyID, boolean includeDeleted, boolean worldDelivery, boolean adminTestDelivery, boolean content) {
		TargetLightsOptions options = TargetLightsOptions.builder()
				.setCompanyId(companyID)
				.setIncludeDeleted(includeDeleted)
				.setWorldDelivery(worldDelivery)
				.setAdminTestDelivery(adminTestDelivery)
				.setContent(content)
				.build();
		return getTargetLightsBySearchParameters(options);
	}

	@Override
	public List<TargetLight> getTargetLightsBySearchParameters(TargetLightsOptions options) {
		try {
			SqlPreparedStatementManager sqlPreparedStatementManager = prepareSelectStatement(options);
			return select(logger, sqlPreparedStatementManager.getPreparedSqlString(), getTargetLightRowMapper(), sqlPreparedStatementManager.getPreparedSqlParameters());
		} catch (Exception e) {
			logger.error("Getting target lights failed: " + e.getMessage());
		}

		return new ArrayList<>();
	}

	protected SqlPreparedStatementManager prepareSelectStatement(TargetLightsOptions options) throws Exception {
		SqlPreparedStatementManager preparedStatementManager =
				new SqlPreparedStatementManager(
						"SELECT target_id, company_id, target_description, target_shortname, " +
								"creation_date, change_date, deleted, locked, invalid, component_hide, complexity, favorite " + getTargetExtendedColumnsAsString() +
								"FROM dyn_target_tbl");

		prepareWhereClauseForTargetsList(preparedStatementManager, options);

		preparedStatementManager.finalizeStatement("ORDER BY favorite DESC, LOWER(target_shortname), target_id");

		return preparedStatementManager;
	}

	protected void prepareWhereClauseForTargetsList(final SqlPreparedStatementManager preparedStatementManager,
													final TargetLightsOptions options) throws Exception {
		preparedStatementManager.addWhereClause("company_id = ?", options.getCompanyId());
		preparedStatementManager.addWhereClause("(hidden IS NULL or hidden = 0)");

		if (!options.isIncludeDeleted()) {
			preparedStatementManager.addWhereClause("deleted = 0");
		}

		if (options.isContent()) {
			preparedStatementManager.addWhereClause("component_hide = 0");
		}

		// If none of worldDelivery and adminTestDelivery is true, we also show all targets even if it would logically mean no items to show
		if (options.isWorldDelivery() && !options.isAdminTestDelivery()) {
			preparedStatementManager.addWhereClause("admin_test_delivery = 0");
		} else if (!options.isWorldDelivery() && options.isAdminTestDelivery()) {
			preparedStatementManager.addWhereClause("admin_test_delivery = 1");
		}

		// Check supported search modes by available db indices
		String searchQuery = options.getSearchText();
		if (StringUtils.isNotEmpty(searchQuery) && isBasicFullTextSearchSupported()) {
			List<String> searchClauses = new ArrayList<>();
			List<java.lang.Object> properties = new ArrayList<>();

			String fullTextSearchClause = searchQuery;
			if (options.isSearchName() || options.isSearchDescription()) {
				try {
					fullTextSearchClause = fulltextSearchQueryGenerator.generateSpecificQuery(searchQuery);
				} catch (final FulltextSearchQueryException e) {
					logger.error("Cannot transform full text search query: " + searchQuery);
				}
			}

			if (options.isSearchName()) {
				searchClauses.add(isOracleDB() ? "CONTAINS(target_shortname, ?) > 0" : "MATCH(target_shortname) AGAINST(? IN BOOLEAN MODE) > 0");
				properties.add(fullTextSearchClause);
			}

			if (options.isSearchDescription()) {
				searchClauses.add(isOracleDB() ? "CONTAINS(target_description, ?) > 0" : "MATCH(target_description) AGAINST(? IN BOOLEAN MODE) > 0");
				properties.add(fullTextSearchClause);
			}

			String searchClause = searchClauses.stream().map(StringUtils::trimToNull).filter(Objects::nonNull).collect(Collectors.joining(" OR "));
			if (StringUtils.isNotBlank(searchClause)) {
				preparedStatementManager.addAndClause();
				preparedStatementManager.appendOpeningParenthesis();
				preparedStatementManager.addWhereClauseSimple(searchClause, properties.toArray());
				preparedStatementManager.appendClosingParenthesis();
			}
		}
	}

	@Override
	public int createSampleTargetGroups(int companyID){
		if(companyID < 2){
			return 0;
		}
		try {
			String sql;
			if (isOracleDB()) {
				sql =
						"INSERT INTO dyn_target_tbl (target_id, company_id, target_shortname, target_sql, target_description, " +
						"                            deleted, change_date, creation_date, admin_test_delivery, " +
						"                            locked, component_hide, eql, invalid, hidden) " +
						"  SELECT " +
						"    dyn_target_tbl_seq.nextval AS target_id, ? AS company_id, " +
						"    target_shortname, target_sql, target_description, deleted, change_date, " +
						"    creation_date, admin_test_delivery, locked, component_hide, eql, invalid, hidden ";
			} else {
				sql =
						"INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, target_description, " +
						"                            deleted, change_date, creation_date, admin_test_delivery, " +
						"                            locked, component_hide, eql, invalid, hidden) " +
						"  SELECT " +
						"    ? AS company_id, " +
						"    target_shortname, target_sql, target_description, deleted, change_date, " +
						"    creation_date, admin_test_delivery, locked, component_hide, eql, invalid, hidden ";
			}
			sql +=
					"  FROM dyn_target_tbl " +
					"  WHERE company_id = 1 " +
					"        AND (hidden IS NULL OR hidden = 0) " +
					"        AND deleted = 0 " +
					"        AND component_hide = 0 " +
					"        AND (LOWER(target_shortname) LIKE '%sample%' " +
					"             OR LOWER(target_shortname) LIKE '%example%' " +
					"             OR LOWER(target_shortname) LIKE '%muster%' " +
					"             OR LOWER(target_shortname) LIKE '%beispiel%' " +
					"             OR LOWER(target_shortname) LIKE '%emm target group%')";
			return update(logger, sql, companyID);
		} catch (Exception e){
			logger.error("Error occurred during creating sample target groups for company: "+companyID, e);
			return 0;
		}
	}

    /**
	 * Checks, if target group is locked (unmodifiable).
	 *
	 * @param targetID
	 *            ID of target group
	 * @param companyID
	 *            ID of company
	 *
	 * @return true if locked, otherwise false
	 */
    @Override
	public boolean isTargetGroupLocked(int targetID, @VelocityCheck int companyID) {
		// New target groups are never locked
		if (targetID == 0) {
			return false;
		}

		int result = selectIntWithDefaultValue(logger, "SELECT COALESCE(locked, 0) AS locked FROM dyn_target_tbl WHERE target_id = ? AND (company_id = ? OR company_id = 0)", -1, targetID, companyID);

		if (result == 1 || result == -1) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void updateTargetLockState(int targetID, @VelocityCheck int companyID, boolean locked) {
		update(logger, "UPDATE dyn_target_tbl SET locked = ? WHERE company_id = ? AND target_id = ?", locked ? 1 : 0, companyID, targetID);
	}

	protected final class RawTargetGroupRowMapper implements RowMapper<RawTargetGroup> {

		@Override
		public final RawTargetGroup mapRow(final ResultSet rs, final int row) throws SQLException {
			final int id = rs.getInt("target_id");
			final String name = rs.getString("target_shortname");
			final int companyId = rs.getInt("company_id");
			final String eql = rs.getString("eql");

			return new RawTargetGroup(id, name, companyId, eql);
		}

	}

	@Override
	public List<TargetLight> getTargetLights(int companyID, Collection<Integer> targetIds, boolean includeDeleted) {
		if (targetIds == null || targetIds.size() <= 0) {
			return new ArrayList<>();
		}

		String deleted = includeDeleted ? "" : " AND deleted = 0";

		return select(logger, "SELECT target_id, company_id, target_description, target_shortname, locked, " +
				" creation_date, change_date, deleted, component_hide, complexity, invalid, favorite " + getTargetExtendedColumnsAsString() +
				"FROM dyn_target_tbl WHERE (company_id = ? OR company_id = 0)" + deleted + " AND target_id IN (" + StringUtils.join(targetIds, ", ") + ") ORDER BY favorite DESC, target_shortname", getTargetLightRowMapper(), companyID);
	}

	@Override
	public List<TargetLight> getUnchoosenTargetLights(int companyID, Collection<Integer> targetIds) {
		if (CollectionUtils.isNotEmpty(targetIds)) {
			String sqlGetTargetsExceptIds = "SELECT target_id, company_id, target_description, " +
					"target_shortname, locked, creation_date, change_date, deleted, component_hide, complexity, invalid, favorite " + getTargetExtendedColumnsAsString() +
					"FROM dyn_target_tbl " +
					"WHERE company_id = ? AND COALESCE(deleted, 0) = 0 AND COALESCE(hidden, 0) = 0 AND admin_test_delivery = 0 " +
					"AND target_id NOT IN (" + StringUtils.join(targetIds, ", ") + ") " +
					"ORDER BY favorite DESC, LOWER(target_shortname)";

			return select(logger, sqlGetTargetsExceptIds, getTargetLightRowMapper(), companyID);
		} else {
			return getTargetLights(companyID, false);
		}
	}

	@Override
	public List<TargetLight> getChoosenTargetLights(String targetExpression, int companyID) {
		if (StringUtils.isNotEmpty(targetExpression)) {
			return select(logger, "SELECT target_id, company_id, target_description, target_shortname, locked, " +
					" creation_date, change_date, deleted, component_hide, complexity, invalid, favorite " + getTargetExtendedColumnsAsString() +
					"FROM dyn_target_tbl WHERE deleted = 0 AND admin_test_delivery = 0 AND target_id IN (" + targetExpression + ") ORDER BY favorite DESC, target_shortname", getTargetLightRowMapper());
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public List<TargetLight> getTestAndAdminTargetLights(int companyId) {
		return getTargetLights(companyId, false, false, true);
	}

	@Override
	public List<TargetLight> getSplitTargetLights(int companyID, String splitType) {
		StringBuilder sqlQueryBuilder = new StringBuilder();
		List<Object> sqlParameters = new ArrayList<>();

		sqlQueryBuilder.append("SELECT target_id, company_id, target_description, target_shortname, locked, creation_date, change_date, deleted, component_hide, complexity, invalid, favorite ")
				.append(getTargetExtendedColumnsAsString())
				.append("FROM dyn_target_tbl ")
				.append("WHERE (company_id = ? OR company_id = 0) AND deleted = 0 ");
		sqlParameters.add(companyID);

		sqlQueryBuilder.append("AND (")
				.append(makeShortNameMatchConditionClause(splitType, sqlParameters))
				.append(") AND admin_test_delivery = 0 ")
				.append("ORDER BY target_shortname");

		return select(logger, sqlQueryBuilder.toString(), getTargetLightRowMapper(), sqlParameters.toArray());
	}

    @Override
    public boolean isBasicFullTextSearchSupported() {
        return checkIndicesAvailable(logger, "dyntg$sname$idx", "dyntg$descr$idx");
    }

    @Override
    public Set<Integer> getInvalidTargets(final int companyId, final Set<Integer> targets) {
        if(CollectionUtils.isEmpty(targets) || companyId <= 0) {
            return new HashSet<>();
        }
        final String query = "SELECT target_id FROM dyn_target_tbl WHERE invalid <> 0 AND target_id IN (:targetIds) AND company_id = :companyId";

        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("targetIds", targets);
        parameters.addValue("companyId", companyId);
        return new HashSet<>(new NamedParameterJdbcTemplate(getDataSource()).query(query, parameters, IntegerRowMapper.INSTANCE));
    }

	private String makeShortNameMatchConditionClause(List<Object> sqlParameters) {
		return makeShortNameMatchConditionClause(null, sqlParameters);
	}

	private String makeShortNameMatchConditionClause(String splitType, List<Object> sqlParameters) {
		return makeShortNameMatchConditionClause(splitType, sqlParameters, TargetLight.LIST_SPLIT_PREFIXES);
	}

	private String makeShortNameMatchConditionClause(String splitType, List<Object> sqlParameters, String ...prefixes) {
		final String[] patterns = makeShortNamePatterns(splitType, prefixes);
		List<String> sqlLikeClauses = new ArrayList<>();

		for (String pattern : patterns) {
			if (isOracleDB()) {
				sqlLikeClauses.add("target_shortname LIKE ? ESCAPE '\\'");
			} else {
				sqlLikeClauses.add("target_shortname LIKE ?");
			}
			sqlParameters.add(pattern.replace("_", "\\_"));
		}
		return StringUtils.join(sqlLikeClauses, " OR ");
	}

	private String[] makeShortNamePatterns(String splitType, String ...prefixes) {
		String[] patterns = new String[prefixes.length];

		if (StringUtils.isNotEmpty(splitType)) {
			for (int i = 0; i < prefixes.length; i++) {
				patterns[i] = prefixes[i] + splitType + "_%";
			}
		} else {
			for (int i = 0; i < prefixes.length; i++) {
				patterns[i] = prefixes[i] + "%";
			}
		}

		return patterns;
	}

	@Override
	public boolean isOracle() {
		return super.isOracleDB();
	}

	@Override
	public final List<RawTargetGroup> listRawTargetGroups(@VelocityCheck int companyId, String ...eqlRawFragments) {
		String sqlGetAll = "SELECT target_id, target_shortname, company_id, eql FROM dyn_target_tbl "
				+ "WHERE company_id = ? AND deleted = 0";

		if (eqlRawFragments.length > 0) {
			List<Object> sqlParameters = new ArrayList<>(eqlRawFragments.length + 1);
			int validFragmentsCount = 0;

			sqlParameters.add(companyId);

			for (String fragment : eqlRawFragments) {
				if (StringUtils.isNotEmpty(fragment)) {
					sqlParameters.add("%" + fragment.toLowerCase() + "%");
					validFragmentsCount++;
				}
			}

			if (validFragmentsCount > 0) {
				String sql = sqlGetAll + " AND " + AgnUtils.repeatString("LOWER(eql) LIKE ?", validFragmentsCount, " AND ");

				return select(logger, sql, rawTargetGroupRowMapper, sqlParameters.toArray());
			}
		}

		return select(logger, sqlGetAll, rawTargetGroupRowMapper, companyId);
	}

	@Override
	public void saveComplexityIndices(@VelocityCheck int companyId, Map<Integer, Integer> complexities) {
		if (MapUtils.isNotEmpty(complexities)) {
			List<Object[]> sqlParameters = complexities.entrySet()
					.stream()
					.map(entry -> new Object[]{entry.getValue(), entry.getKey(), companyId})
					.collect(Collectors.toList());

			batchupdate(logger, "UPDATE dyn_target_tbl SET complexity = ? WHERE target_id = ? AND company_id = ?", sqlParameters);
		}
	}

	@Override
	public List<ComTarget> getTargetByNameAndSQL(int companyId, String targetName, String targetSQL, boolean includeDeleted, boolean worldDelivery, boolean adminTestDelivery) {
		List<Object> selectParameter = new ArrayList<>();
		String selectSql = "SELECT target_id, company_id, target_description, target_shortname, target_sql, deleted, " +
				"creation_date, change_date, admin_test_delivery, locked, eql, invalid, component_hide, COALESCE(complexity, -1) AS complexity " + getTargetExtendedColumnsAsString() +
				" FROM dyn_target_tbl WHERE company_id = ? AND target_shortname = ? AND target_sql = ?" +
				" AND (hidden IS NULL or hidden = 0)";
		selectParameter.add(companyId);
		selectParameter.add(targetName);
		selectParameter.add(targetSQL);

		if (!includeDeleted) {
			selectSql += " AND deleted = 0";
		}

		// If none of worldDelivery and adminTestDelivery is true, we also show all targets even if it would logically mean no items to show
		if (worldDelivery && !adminTestDelivery) {
			selectSql += " AND admin_test_delivery = 0";
		} else if (!worldDelivery && adminTestDelivery) {
			selectSql += " AND admin_test_delivery = 1";
		}

		return select(logger, selectSql, getTargetRowMapper(),  selectParameter.toArray());
	}

	@Override
	public PaginatedListImpl<Dependent<TargetGroupDependentType>> getDependents(@VelocityCheck int companyId, int targetId,
																				Set<TargetGroupDependentType> allowedTypes, int pageNumber,
																				int pageSize, String sortColumn, String order) {
		final boolean isOracle = isOracle();
		final boolean isFilterDisabled = CollectionUtils.isEmpty(allowedTypes);
		final boolean sortAscending = AgnUtils.sortingDirectionToBoolean(order, true);

		List<String> sqlSubQueries = new ArrayList<>();
		List<Object> sqlParameters = new ArrayList<>();

		if (isFilterDisabled || allowedTypes.contains(TargetGroupDependentType.MAILING)) {
			sqlSubQueries.add(getSqlDependentMailings(isOracle));
			sqlParameters.add(TargetGroupDependentType.MAILING.getId());
			sqlParameters.add(companyId);
			sqlParameters.add(targetId);
		}

		if (isFilterDisabled || allowedTypes.contains(TargetGroupDependentType.MAILING_CONTENT)) {
			sqlSubQueries.add(getSqlDependentMailingContents(isOracle));
			sqlParameters.add(TargetGroupDependentType.MAILING_CONTENT.getId());
			sqlParameters.add(companyId);
			sqlParameters.add(targetId);
		}

		if (isFilterDisabled || allowedTypes.contains(TargetGroupDependentType.REPORT)) {
			sqlSubQueries.add(getSqlDependentReports(isOracle));
			sqlParameters.add(TargetGroupDependentType.REPORT.getId());
			sqlParameters.add(companyId);
			sqlParameters.add(targetId);
		}

		if (isFilterDisabled || allowedTypes.contains(TargetGroupDependentType.EXPORT_PROFILE)) {
			sqlSubQueries.add(getSqlDependentExportPredefs());
			sqlParameters.add(TargetGroupDependentType.EXPORT_PROFILE.getId());
			sqlParameters.add(companyId);
			sqlParameters.add(targetId);
		}

		String sqlGetDependents = "SELECT * FROM (" + StringUtils.join(sqlSubQueries, " UNION ALL ") + ") T1";

		return selectPaginatedList(logger, sqlGetDependents, null, StringUtils.trimToNull(sortColumn), sortAscending, pageNumber, pageSize, new DependentMapper(), sqlParameters.toArray());
	}

	@Override
	public Map<Integer, Integer> getTargetComplexityIndices(@VelocityCheck int companyId) {
		String sqlGetComplexityIndices = "SELECT target_id, COALESCE(complexity, -1) AS complexity FROM dyn_target_tbl WHERE company_id = ? AND COALESCE(hidden, 0) = 0";

		Map<Integer, Integer> map = new HashMap<>();
		query(logger, sqlGetComplexityIndices, new ComplexityIndicesCallbackHandler(map), companyId);
		return map;
	}

	@Override
	public Integer getTargetComplexityIndex(@VelocityCheck int companyId, int targetId) {
		String sqlGetComplexityIndex = "SELECT COALESCE(complexity, -1) FROM dyn_target_tbl " +
			"WHERE target_id = ? AND company_id = ? AND COALESCE(hidden, 0) = 0";

		return selectWithDefaultValue(logger, sqlGetComplexityIndex, Integer.class, null, targetId, companyId);
	}

	@Override
	public List<Pair<Integer, String>> getTargetsToInitializeComplexityIndices(@VelocityCheck int companyId) {
		String sqlGetTargets = "SELECT target_id, eql FROM dyn_target_tbl WHERE company_id = ? AND complexity IS NULL";

		return select(logger, sqlGetTargets, new TargetEqlMapper(), companyId);
	}

    @Override
    public boolean isValid(final int companyId, final int targetId) {
        String query = "SELECT invalid FROM dyn_target_tbl WHERE company_id = ? AND target_id = ?";
        return selectInt(logger, query, companyId, targetId) == 0;
    }

    @Override
    public void addToFavorites(final int targetId, final int companyId) {
        manageFavorite(targetId, true, companyId);
    }

    @Override
    public void removeFromFavorites(final int targetId, final int companyId) {
        manageFavorite(targetId, false, companyId);
    }

    private void manageFavorite(final int targetId, final boolean favorite, final int companyId) {
        String query = "UPDATE dyn_target_tbl SET favorite = ? WHERE target_id = ? AND company_id = ?";
        update(logger, query, favorite ? 1 : 0, targetId, companyId);
    }

	public TargetLightRowMapper getTargetLightRowMapper() {
		return targetLightRowMapper;
	}

	public TargetRowMapper getTargetRowMapper() {
		return targetRowMapperWithEql;
	}

	protected String getTargetExtendedColumnsAsString() {
		return getTargetExtendedColumnsAsString(true);
	}

	protected String getTargetExtendedColumnsAsString(final boolean commaBefore) {
		final List<String> extendedColumns = getTargetLightsExtendedColumns();
		if(CollectionUtils.isEmpty(extendedColumns)) {
			return "";
		}

		return (commaBefore ? ", " : "") + StringUtils.join(extendedColumns.toArray());
	}

	protected List<String> getTargetLightsExtendedColumns() {
		return Collections.emptyList();
	}

	protected TargetFactory getTargetFactory() {
		return targetFactory;
	}

	private static String getSqlDependentMailings(boolean isOracle) {
		return "SELECT ? AS type, mailing_id AS id, shortname AS name FROM mailing_tbl " +
				"WHERE company_id = ? AND deleted = 0 AND (" + DbUtilities.createTargetExpressionRestriction(isOracle) + ")";
	}

	private static String getSqlDependentReports(boolean isOracle) {
		return "SELECT DISTINCT ? AS type, rep.report_id AS id, rep.shortname AS name " +
				"FROM birtreport_parameter_tbl param INNER JOIN birtreport_tbl rep ON rep.report_id = param.report_id " +
				"WHERE rep.company_id = ? AND param.parameter_name = 'selectedTargets' " +
				"AND param.parameter_value NOT IN (' ') " +
				(isOracle ?
						"AND INSTR(',' || parameter_value || ',', ',' || ? || ',') <> 0 "
						:
						"AND INSTR(CONCAT(',', parameter_value, ','), CONCAT(',', ?, ',')) <> 0 "
				);
	}

	private static String getSqlDependentExportPredefs() {
		return "SELECT ? AS type, export_predef_id AS id, shortname AS name FROM export_predef_tbl " +
				"WHERE deleted = 0 AND company_id = ? AND target_id = ?";
	}

	private static String getSqlDependentMailingContents(boolean isOracle) {
		String sql;

		if (isOracle) {
			sql = "SELECT DISTINCT ? AS type, m.mailing_id AS id, m.shortname || ' (' || n.dyn_name || ')' AS name ";
		} else {
			sql = "SELECT DISTINCT ? AS type, m.mailing_id AS id, CONCAT(m.shortname, ' (' , n.dyn_name , ')') AS name ";
		}

		return sql + "FROM dyn_content_tbl c " +
				"JOIN mailing_tbl m ON m.deleted = 0 AND c.mailing_id = m.mailing_id " +
				"JOIN dyn_name_tbl n ON n.deleted = 0 AND c.dyn_name_id = n.dyn_name_id " +
				"WHERE c.company_id = ? AND c.target_id = ?";
	}

	private static class DependentMapper implements RowMapper<Dependent<TargetGroupDependentType>> {
		@Override
		public Dependent<TargetGroupDependentType> mapRow(ResultSet rs, int i) throws SQLException {
			return TargetGroupDependentType.fromId(rs.getInt("type"), false)
					.forId(rs.getInt("id"), rs.getString("name"));
		}
	}

	private static class TargetEqlMapper implements RowMapper<Pair<Integer, String>> {
		@Override
		public Pair<Integer, String> mapRow(ResultSet rs, int i) throws SQLException {
			return new Pair<>(rs.getInt("target_id"), rs.getString("eql"));
		}
	}

	private static class ComplexityIndicesCallbackHandler implements RowCallbackHandler {
		private Map<Integer, Integer> map;

		public ComplexityIndicesCallbackHandler(Map<Integer, Integer> map) {
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			map.put(rs.getInt("target_id"), rs.getInt("complexity"));
		}
	}

	@Override
	public boolean isValidEql(int companyID, String eql) {
		try {
			eqlFacade.convertEqlToSql(eql, companyID);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
    @Override
    public boolean isAltg(int targetId) {
        return selectInt(logger, "SELECT is_access_limiting FROM dyn_target_tbl WHERE target_id = ?", targetId) == 1;
	}
	
    protected Collection<String> getAdditionalExtendedColumns() {
  		return CollectionUtils.emptyCollection();
  	}
  
  	protected Collection<Object> getAdditionalExtendedParams(ComTarget target) {
  		return CollectionUtils.emptyCollection();
  	}

	@Override
	public int getAccessLimitingTargetgroupsAmount(int companyId) {
		return 0;
	}
}
