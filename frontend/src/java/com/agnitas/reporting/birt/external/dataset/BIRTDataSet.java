/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.dao.MailingStatus;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDaoException;
import org.agnitas.emm.core.mediatypes.dao.impl.MediatypesDaoImpl;
import org.agnitas.emm.core.mediatypes.factory.MediatypeFactoryImpl;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.reporting.birt.external.beans.LightMailingList;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.dao.impl.LightMailingListDaoImpl;
import com.agnitas.reporting.birt.external.dao.impl.LightTargetDaoImpl;
import com.agnitas.util.LongRunningSelectResultCacheDao;

public class BIRTDataSet extends LongRunningSelectResultCacheDao {
	private static final Logger logger = LogManager.getLogger(BIRTDataSet.class);
	
	private static final String TEMPTABLECACHE_TABLENAME = "temp_cache_tbl";

	public static final String DATE_PARAMETER_FORMAT = "yyyy-MM-dd";
	public static final String DATE_PARAMETER_FORMAT_WITH_HOUR = "yyyy-MM-dd:H";
	public static final String DATE_PARAMETER_FORMAT_WITH_BLANK_HOUR = "yyyy-MM-dd H";
	public static final String DATE_PARAMETER_FORMAT_WITH_HOUR2 = "yyyy-MM-dd:HH";
	public static final String DATE_PARAMETER_FORMAT_WITH_SECOND = "yyyy-MM-dd HH:mm:ss.SSS";
	
	private JdbcTemplate embeddedJdbcTemplate;
	private DataSource embeddedDataSource;
 
	public Connection getConnection() throws SQLException {
		return getDataSource().getConnection();
	}

	protected int getCachedTempTable(Logger childClassLogger, String statisticsType, String parameterString) throws Exception {
		try {
			if (!checkEmbeddedTableExists(TEMPTABLECACHE_TABLENAME)) {
				executeEmbedded(childClassLogger, "CREATE TABLE " + TEMPTABLECACHE_TABLENAME + " ("
					+ "type VARCHAR(50),"
					+ " parameter VARCHAR(400),"
					+ " temptableid INTEGER,"
					+ " table_name VARCHAR(50),"
					+ " creation_date TIMESTAMP,"
					+ " validity_date TIMESTAMP,"
					+ " cleanup_date TIMESTAMP"
					+")");
			}

			List<Map<String, Object>> result = selectEmbedded(childClassLogger, "SELECT table_name FROM " + TEMPTABLECACHE_TABLENAME + " WHERE cleanup_date < ?", new Date());
			for (Map<String, Object> row : result) {
				String tableNameToDelete = (String) row.get("table_name");
				updateEmbedded(childClassLogger, "DELETE FROM " + TEMPTABLECACHE_TABLENAME + " WHERE table_name = ?", tableNameToDelete);
				try {
					executeEmbedded(childClassLogger, "DROP TABLE " + tableNameToDelete);
				} catch (Exception e) {
					childClassLogger.error("Cannot drop embedded table " + tableNameToDelete);
				}
			}
			
			boolean blockingEntryExists = true;
			while (blockingEntryExists) {
				int tempTableID = selectEmbeddedIntWithDefault(childClassLogger, "SELECT MAX(temptableid) FROM " + TEMPTABLECACHE_TABLENAME + " WHERE type = ? AND parameter = ? AND validity_date IS NULL AND creation_date <= ?", 0, statisticsType, parameterString, DateUtilities.addMinutesToDate(new Date(), 60));
				blockingEntryExists = tempTableID > 0;
				if (blockingEntryExists) {
					Thread.sleep(10000);
				}
			}
			
			int tempTableID = selectEmbeddedIntWithDefault(childClassLogger, "SELECT MAX(temptableid) FROM " + TEMPTABLECACHE_TABLENAME + " WHERE type = ? AND parameter = ? AND validity_date >= ?", 0, statisticsType, parameterString, new Date());
			List<Map<String, Object>> resultTables = selectEmbedded(childClassLogger, "SELECT table_name FROM " + TEMPTABLECACHE_TABLENAME + " WHERE temptableid = ?", tempTableID);
			if (!resultTables.isEmpty()) {
				String tableName = (String) resultTables.get(0).get("table_name");
				if (checkEmbeddedTableExists(tableName)) {
					return tempTableID;
				} else {
					updateEmbedded(childClassLogger, "DELETE FROM " + TEMPTABLECACHE_TABLENAME + " WHERE table_name = ?", tableName);
					return 0;
				}
			} else {
				updateEmbedded(childClassLogger, "DELETE FROM " + TEMPTABLECACHE_TABLENAME + " WHERE temptableid = ?", tempTableID);
				return 0;
			}
		} catch (Exception e) {
			childClassLogger.error("Cannot getCachedTempTable: " + e.getMessage(), e);
			return 0;
		}
	}

	protected void createBlockEntryInTempTableCache(Logger childClassLogger, String statisticsType, String parameterString) throws Exception {
		try {
			updateEmbedded(childClassLogger, "INSERT INTO " + TEMPTABLECACHE_TABLENAME + " (type, parameter, creation_date, cleanup_date) VALUES (?, ?, ?, ?)", statisticsType, parameterString, new Date(), DateUtilities.addMinutesToDate(new Date(), 60));
		} catch (Exception e) {
			childClassLogger.error("Cannot createBlockEntryInTempTableCache: " + e.getMessage(), e);
		}
	}

	protected void storeTempTableInCache(Logger childClassLogger, String statisticsType, String parameterString, int tempTableID, String tempTableName) throws Exception {
		try {
			updateEmbedded(childClassLogger, "INSERT INTO " + TEMPTABLECACHE_TABLENAME + " (type, parameter, temptableid, table_name, creation_date, validity_date, cleanup_date) VALUES (?, ?, ?, ?, ?, ?, ?)", statisticsType, parameterString, tempTableID, tempTableName, new Date(), DateUtilities.addMinutesToDate(new Date(), 10), DateUtilities.addMinutesToDate(new Date(), 15));
			updateEmbedded(childClassLogger, "DELETE FROM " + TEMPTABLECACHE_TABLENAME + " WHERE type = ? AND parameter = ? AND validity_date IS NULL", statisticsType, parameterString);
		} catch (Exception e) {
			childClassLogger.error("Cannot storeTempTableInCache: " + e.getMessage(), e);
		}
	}
	
	public static String getOnePixelLogDeviceTableName(int companyID) {
		return "onepixellog_device_" + companyID + "_tbl";
	}
	
	public static String getRdirLogTableName(int companyID) {
		return "rdirlog_" + companyID + "_tbl";
	}
	
	protected static LightTarget getDefaultTarget(LightTarget target) {
		if (target == null) {
			return getAllSubscribersTarget();
		}
		
		return target;
	}
	
	protected static LightTarget getAllSubscribersTarget() {
		LightTarget allSubscribers = new LightTarget();
		allSubscribers.setId(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID);
		allSubscribers.setName(CommonKeys.ALL_SUBSCRIBERS);
		allSubscribers.setTargetSQL("");
		return allSubscribers;
	}
	
	@Override
	public DataSource getDataSource() {
		DataSource datasource = super.getDataSource();
		
		if (datasource != null) {
			return datasource;
		} else {
			try {
				datasource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/emm_db");
				setDataSource(datasource);
				return datasource;
			} catch (Exception e) {
				logger.error("Cannot find datasource in JNDI context: " + e.getMessage(), e);
				throw new RuntimeException("Cannot find datasource in JNDI context: " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Dependency injection method
	 * @param embeddedDataSource to be used by this dao object for temporary data storage
	 */
	@Required
	public final void setEmbeddedDatasource(DataSource embeddedDataSource) {
		/*
		 * Keep this method final to avoid problems with
		 * overriding in subclasses!
		 */

		this.embeddedDataSource = embeddedDataSource;
	}

	protected DataSource getEmbeddedDatasource() throws Exception {
		if (embeddedDataSource != null) {
			return embeddedDataSource;
		} else {
			try {
				Context initialContext = new InitialContext();
				embeddedDataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/embedded");
				return embeddedDataSource;
			} catch (NamingException e) {
				throw new Exception("Cannot create temporary database connection, check your JNDI-settings: " + e.getMessage());
			} catch (Exception e) {
				throw new Exception("Cannot create temporary database connection: " + e.getMessage());
			}
		}
	}
	
	private JdbcTemplate getEmbeddedJdbcTemplate() throws Exception {
    	if (embeddedJdbcTemplate == null) {
    		embeddedJdbcTemplate = new JdbcTemplate(getEmbeddedDatasource());
		}
		
		return embeddedJdbcTemplate;
    }
    
	protected ConfigService getConfigService() {
		return BIRTDataSetHelper.getInstance().getConfigService(getDataSource());
	}
	
	protected JavaMailService getJavaMailService() {
		return BIRTDataSetHelper.getInstance().getJavaMailService(getDataSource());
	}
	
	
	protected class DateFormats {
		private boolean hourScale;
		private String sqlFormatDate;
		private SimpleDateFormat formater;
		private String startDate;
		private String stopDate;
		private int period;
		
		public DateFormats(String startDate, String stopDate, Boolean hourScale) {
			this.hourScale = hourScale;
			if (isOracleDB()) {
				sqlFormatDate = this.hourScale ? "YYYY-MM-DD:HH24" : "YYYY-MM-DD";
			} else {
				sqlFormatDate = this.hourScale ? "%Y-%m-%d:%H" : "%Y-%m-%d";
			}
			if (this.hourScale) {
				formater = new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR);
			} else {
				formater = new SimpleDateFormat(DATE_PARAMETER_FORMAT);
			}
			if (startDate.contains(":") && !hourScale) {
				this.startDate = startDate.substring(0, startDate.indexOf(":"));
			} else {
				this.startDate = startDate;
			}
			if (stopDate.contains(":") && !hourScale) {
				this.stopDate = stopDate.substring(0, stopDate.indexOf(":"));
			} else {
				this.stopDate = stopDate;
			}
			if (isDateSlice()) {
				calcPeriod();
			}
		}
		
		public DateFormats() {
			this("", "", false);
		}
		
		private void calcPeriod() {
			try {
				Date sdt = formater.parse(startDate);
				Date edt = formater.parse(stopDate);
				if (hourScale) {
					period = 1 + BigDecimal.valueOf(TimeUnit.HOURS.convert(edt.getTime() - sdt.getTime(), TimeUnit.MILLISECONDS)).intValue();
				} else {
					period = 1 + BigDecimal.valueOf(TimeUnit.DAYS.convert(edt.getTime() - sdt.getTime(), TimeUnit.MILLISECONDS)).intValue();
				}
			} catch (ParseException e) {
				logger.error("Error calculating period", e);

				period = 0;
			}
		}
		
		public boolean isHourScale() {
			return hourScale;
		}
		
		public String getSqlFormatDate() {
			return sqlFormatDate;
		}
		
		public SimpleDateFormat getFormater() {
			return formater;
		}
		
		public String getStartDate() {
			return startDate;
		}
		
		public String getStopDate() {
			return stopDate;
		}
		
		public int getPeriod() {
			return period;
		}
		
		public boolean isDateSlice() {
			return !startDate.isEmpty();
		}
		
		public Date getStartDateAsDate() {
			try {
				return formater.parse(startDate);
			} catch (ParseException e) {
				logger.error("Error parsing start date", e);

				return null;
			}
		}
		
		public Date getStopDateAsDate() {
			try {
				return formater.parse(stopDate);
			} catch (ParseException e) {
				logger.error("Error parsing stop date", e);

				return null;
			}
		}
	}
	
	protected List<String> getTargetSql(String selectedTargets, Integer companyId) {
		List<String> targetSql = new ArrayList<>();
		try {
			if (!StringUtils.isEmpty(selectedTargets)) {
				LightTargetDaoImpl lightTargetDao = new LightTargetDaoImpl();
				lightTargetDao.setDataSource(getDataSource());
				
				List<LightTarget> targets = lightTargetDao.getTargets(Arrays.asList(selectedTargets.split(",")), companyId);
				for (LightTarget target : targets) {
					if (!StringUtils.isEmpty(target.getTargetSQL())) {
						targetSql.add(target.getTargetSQL());
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error occured: " + e.getMessage(), e);
		}
		return targetSql;
	}
	
	protected List<LightTarget> getTargets(String selectedTargets, Integer companyID) {
		try {
			if (StringUtils.isNotEmpty(selectedTargets)) {
				LightTargetDaoImpl lightTargetDao = new LightTargetDaoImpl();
				lightTargetDao.setDataSource(getDataSource());
				
				return lightTargetDao.getTargets(Arrays.asList(selectedTargets.split(",")), companyID);
			}
		} catch (Exception e) {
			logger.error("Error occured: " + e.getMessage(), e);
		}
		return new ArrayList<>();
	}
	
	protected List<LightTarget> getTargets(List<String> selectedTargets, Integer companyID) {
		try {
			if (selectedTargets != null && !selectedTargets.isEmpty()) {
				LightTargetDaoImpl lightTargetDao = new LightTargetDaoImpl();
				lightTargetDao.setDataSource(getDataSource());
				
				return lightTargetDao.getTargets(selectedTargets, companyID);
			}
		} catch (Exception e) {
			logger.error("Error occured: " + e.getMessage(), e);
		}
		return new ArrayList<>();
	}
	
	protected LightTarget getTarget(int targetID, int companyID) {
		try {
			LightTargetDaoImpl lightTargetDao = new LightTargetDaoImpl();
			lightTargetDao.setDataSource(getDataSource());
			
			return lightTargetDao.getTarget(targetID, companyID);
		} catch (Exception e) {
			logger.error("Error occured: " + e.getMessage(), e);
		}
		return null;
	}
	
	protected String getMailinglistName(int companyId, int mailinglistId) {
	   try {
           LightMailingList mailingList = new LightMailingListDaoImpl(getDataSource()).getMailingList(mailinglistId, companyId);
           return mailingList != null ? mailingList.getShortname() : "";
		} catch (Exception e) {
			logger.error("Error occured: " + e.getMessage(), e);
		}
		
		return "";
    }
    
    protected String getMailingSubject(int companyId, int mailingId) {
		try {
			MediatypesDaoImpl mediatypesDao = new MediatypesDaoImpl();
			mediatypesDao.setDataSource(getDataSource());
			mediatypesDao.setConfigService(getConfigService());
			mediatypesDao.setMediatypeFactory(new MediatypeFactoryImpl());
			Map<Integer, Mediatype> mediaTypeMap = mediatypesDao.loadMediatypes(mailingId, companyId);
			Mediatype mediatype = mediaTypeMap.get(MediaTypes.EMAIL.getMediaCode());
			if (mediatype != null) {
				MediatypeEmail emailMediaType = (MediatypeEmail) mediatype;
				return StringUtils.trimToEmpty(emailMediaType.getSubject());
			}
			
		} catch (MediatypesDaoException e) {
			logger.error("Error occured: " + e.getMessage(), e);
		}
		return "";
	}
	
	protected String getTargetSqlString(String selectedTargets, Integer companyId) {
		List<String> sqlList = getTargetSql(selectedTargets, companyId);
		if (CollectionUtils.isNotEmpty(sqlList)) {
			return sqlList.stream()
					.map(sql -> "(" + sql + ")")
					.collect(Collectors.joining(" OR ", "(", ")"));
		}
		
		return "";
	}

	protected String joinWhereClause(final String leftClause, final String rightClause) {
		if (StringUtils.isBlank(leftClause) && StringUtils.isBlank(rightClause)) {
			return "";
		}

		if (StringUtils.isBlank(leftClause)) {
			return rightClause;
		}

		if (StringUtils.isBlank(rightClause)) {
			return leftClause;
		}

		return StringUtils.join(leftClause, " AND " , rightClause);
	}
	
	protected int getNextTmpID() throws Exception {
		String statement = "VALUES (NEXT VALUE FOR birt_report_tmp_tbl_seq)";
		try {
			logSqlStatement(logger, "EMBEDDED: " + statement);
			return getEmbeddedJdbcTemplate().queryForObject(statement, Integer.class);
		} catch (Exception e) {
			resetTempDatabase();
			return selectEmbeddedInt(logger, statement);
		}
	}
	
	public void resetTempDatabase() {
		try {
			logger.info("Resetting temporary database (derby)");
			List<String> existingTables = selectEmbedded(logger, "SELECT t.tablename FROM sys.systables t, sys.sysschemas s WHERE t.schemaid = s.schemaid AND t.tabletype = 'T'", StringRowMapper.INSTANCE);
			for (String existingTable : existingTables) {
				executeEmbedded(logger, "DROP TABLE " + existingTable);
			}
			
			try {
				getEmbeddedJdbcTemplate().execute("DROP SEQUENCE birt_report_tmp_tbl_seq RESTRICT");
			} catch (Exception e) {
				// Cannot cleanup old birt_report_tmp_tbl_seq in derby db, because it does not exist. For the first start of EMM statistics this is ok.
			}
			executeEmbedded(logger, "CREATE SEQUENCE birt_report_tmp_tbl_seq AS INT START WITH 1 MAXVALUE 999999 CYCLE");
		} catch (Exception e) {
			logger.error("Could not reset temp database", e);
		}
	}
	
	public boolean isMailingTrackingActivated(int companyID) {
		String query = "SELECT COALESCE(mailtracking, 0) FROM company_tbl WHERE company_id = ?";
		return select(logger, query, Integer.class, companyID) != 0;
	}
	
	public boolean isMailingTrackingDataAvailable(int mailingID, int companyID) throws Exception {
		return isMailingTrackingActivated(companyID) && isTrackingExists(mailingID, companyID);
	}
	
	public boolean isTrackingExists(int mailingID, int companyID) throws Exception {
		StringBuilder queryBuilder = new StringBuilder();
		if (getMailingType(mailingID) == MailingType.INTERVAL) {
			queryBuilder
					.append("SELECT COUNT(*)")
					.append(" FROM interval_track_").append(companyID).append("_tbl mt")
					.append(" WHERE mt.mailing_id = ?");
		} else {
			queryBuilder
					.append("SELECT COUNT(*)")
					.append(" FROM success_").append(companyID).append("_tbl")
					.append(" WHERE mailing_id = ?");
		}
		return select(logger, queryBuilder.toString(), Integer.class, mailingID) > 0;
	}
	
	public boolean isTrackingExistsOfOptimizationMailings(int optimizationId, int companyID) throws Exception {
		List<Integer> mailings = getAutoOptimizationMailings(optimizationId, companyID).stream().filter(id -> id != 0).collect(Collectors.toList());
		for (int mailingId: mailings) {
			if (!isTrackingExists(mailingId, companyID)) {
				return false;
			}
		}
		return !CollectionUtils.isEmpty(mailings);
	}
	
	public boolean isMailingBouncesExpire(int companyId, int mailingId) {
		Map<Integer, Boolean> expireMap = getMailingBouncesExpire(companyId, String.valueOf(mailingId));
		return expireMap.get(mailingId);
	}
	
	public Map<Integer, Boolean> getMailingBouncesExpire(int companyId, String mailingIds) {
		HashMap<Integer, Boolean> expireMap = new HashMap<>();
		int bounceExpire =  getConfigService().getIntegerValue(ConfigValue.ExpireBounce, companyId);

		String sql;
		if (isOracleDB()) {
			sql = "SELECT (sysdate - senddate) mail_age, mailing_id FROM maildrop_status_tbl " + "WHERE company_id = ? AND mailing_id IN (" + mailingIds
					+ ") ORDER BY DECODE(status_field, " + "'W', 1, 'R', 2, 'D', 2, 'E', 3, 'C', 3, 'T', 4, 'A', 4, 5), status_id DESC";
		} else {
			sql = "SELECT TIMESTAMPDIFF(DAY, senddate, CURRENT_TIMESTAMP) mail_age, mailing_id FROM maildrop_status_tbl " + "WHERE company_id = ? AND mailing_id IN ("
					+ mailingIds + ") ORDER BY  CASE status_field WHEN 'W' "
					+ "THEN 1 WHEN 'R' THEN 2 WHEN 'D' THEN 2 WHEN 'E' THEN 3 WHEN 'C' THEN 3 WHEN 'T' THEN 4 WHEN 'A' " + "THEN 4 ELSE 5 END, status_id DESC";
		}
		List<Map<String, Object>> result = select(logger, sql, companyId);
		for (Map<String, Object> row : result) {
			int mailingId = ((Number) row.get("mailing_id")).intValue();
			if (expireMap.get(mailingId) == null) {
				int mailAge = Optional.ofNullable((Number) row.get("mail_age")).orElse(0).intValue();
				expireMap.put(mailingId, bounceExpire < mailAge);
			}
		}
		String[] ids = mailingIds.split(",");
		for (String id : ids) {
			int mailingId = NumberUtils.toInt(StringUtils.trim(id));
			expireMap.putIfAbsent(mailingId, false);
		}
		return expireMap;
	}

	protected String getTemporaryTableName(int tempTableID) {
		return "tmp_report_aggregation_" + tempTableID + "_tbl";
	}
	
	public void dropTempTable(int tempTableID) throws Exception {
		try {
			String dropTableSQL = "DROP TABLE tmp_report_aggregation_" + tempTableID + "_tbl";
			getEmbeddedJdbcTemplate().update(dropTableSQL);
		} catch (NamingException e) {
			logger.error("Could not drop temporary table, check your JNDI-settings", e);
		}
	}
	
	public List<Integer> getAutoOptimizationMailings(int optimizationID, int companyID) {
        List<Integer> result = new ArrayList<>();
		String query = "SELECT group1_id, group2_id, group3_id, group4_id, group5_id, final_mailing_id FROM auto_optimization_tbl WHERE optimization_id=? and company_id=?";
        List<Map<String, Object>> optimizationElements = select(logger, query, optimizationID, companyID);
        if (!optimizationElements.isEmpty()) {
        	Map<String, Object> map = optimizationElements.get(0);
            for (int i = 1; i <= 5; i++ ){
                int groupId = ((Number) map.get("group" + i + "_id")).intValue();
                result.add(groupId);
            }
            result.add(map.get("final_mailing_id") == null ? 0 : ((Number) map.get("final_mailing_id")).intValue());
        }
        return result;
    }
	
    public int getAutoOptimizationWinnerId(int optimizationID, int companyID) {
		String query = "SELECT result_mailing_id FROM auto_optimization_tbl WHERE optimization_id=? and company_id=?";
        return selectInt(logger, query, optimizationID, companyID);
    }
    
    protected List<LightMailingList> getMailingLists(List<Integer> mailingListIds, Integer companyID) {
    	if (mailingListIds != null && !mailingListIds.isEmpty()) {
			return new LightMailingListDaoImpl(getDataSource()).getMailingLists(mailingListIds, companyID);
        }

		return new ArrayList<>();
    }
	
    protected List<Integer> parseCommaSeparatedIds(String stringOfIds) {
        List<Integer> ids = new LinkedList<>();
        try {
            if (StringUtils.isNotEmpty(stringOfIds)) {
                for (String id : stringOfIds.split(",")) {
                    ids.add(Integer.valueOf(id.trim()));
                }
            }
        } catch (Exception e) {
            logger.error("Error occured: " + e.getMessage(), e);
        }
        return ids;
    }
	
    protected MailingType getMailingType(int mailingId) throws Exception {
    	if (mailingId <= 0) {
    		throw new RuntimeException("Invalid mailing id");
    	}

		return MailingType.fromCode(select(logger, "SELECT mailing_type FROM mailing_tbl WHERE mailing_id = ?", Integer.class, mailingId));
    }
	
    protected int getNumberSentMailings(int companyID, int mailingID, String recipientsType, String targetSql, String startDateString, String endDateString) throws Exception {
    	Date startDate = null;
    	Date endDate = null;
    	boolean useTargetGroup = (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1"));
    	
        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
        	startDate = parseStatisticDate(startDateString);
        	endDate = parseStatisticEndDate(endDateString);
		}
        
        MailingType mailingType = getMailingType(mailingID);
        if (mailingType == MailingType.INTERVAL) {
            StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) FROM");
    		List<Object> parameters = new ArrayList<>();
            
            if (targetSql != null && targetSql.contains("cust.")) {
    			queryBuilder.append(" customer_").append(companyID).append("_tbl cust,");
    		}
         
        	queryBuilder.append(" interval_track_").append(companyID).append("_tbl track");
        	queryBuilder.append(" WHERE track.mailing_id = ?");
    		parameters.add(mailingID);
        	if (startDate != null && endDate != null) {
    			queryBuilder.append(" AND (? <= track.send_date AND track.send_date < ?)");
				parameters.add(startDate);
				parameters.add(endDate);
    		}
            
            if (targetSql != null && targetSql.contains("cust.")) {
    			queryBuilder.append(" AND cust.customer_id = track.customer_id");
    		}
    		
    		if (useTargetGroup) {
    			queryBuilder.append(" AND (").append(targetSql).append(")");
    		}
            
            return selectIntWithDefaultValue(logger, queryBuilder.toString(), 0, parameters.toArray(new Object[0]));
        } else {
        	if (DbUtilities.checkIfTableExists(getDataSource(), "mailtrack_" + companyID + "_tbl") && isMailingTrackingActivated(companyID) && !isMailTrackingExpired(companyID, mailingID)) {
                StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) FROM");
        		List<Object> parameters = new ArrayList<>();
                
                if (targetSql != null && targetSql.contains("cust.")) {
        			queryBuilder.append(" customer_").append(companyID).append("_tbl cust,");
        		}
             
            	queryBuilder.append(" mailtrack_").append(companyID).append("_tbl track");
            	queryBuilder.append(" WHERE track.mailing_id = ? AND track.maildrop_status_id <> 0");
        		parameters.add(mailingID);

            	if (startDate != null && endDate != null) {
        			queryBuilder.append(" AND (? <= track.timestamp AND track.timestamp < ?)");
    				parameters.add(startDate);
    				parameters.add(endDate);
        		}
                
                if (targetSql != null && targetSql.contains("cust.")) {
        			queryBuilder.append(" AND cust.customer_id = track.customer_id");
        		}
        		
        		if (useTargetGroup) {
        			queryBuilder.append(" AND (").append(targetSql).append(")");
        		}
                
        		int numberSentMailingsByMailTrack = selectIntWithDefaultValue(logger, queryBuilder.toString(), 0, parameters.toArray(new Object[0]));
        		
        		if (numberSentMailingsByMailTrack == 0 && !useTargetGroup) {
        			// Fallback for newly activated automation package with newly created and therefore empty mailtrack table
					return getNumberOfSentMailingsFromMailingAccount(mailingID, recipientsType, startDate, endDate);
        		} else {
        			return numberSentMailingsByMailTrack;
        		}
        	} else if (!useTargetGroup) {
        		// mailing_account_tbl has no customer ids and therefore cannot be used for target group specific numbers
        		return getNumberOfSentMailingsFromMailingAccount(mailingID, recipientsType, startDate, endDate);
        	} else {
        		return -1;
        	}
        }
	}

	private int getNumberOfSentMailingsFromMailingAccount(int mailingID, String recipientsType, Date startDate, Date endDate) {
		StringBuilder queryBuilder = new StringBuilder("SELECT SUM(no_of_mailings) FROM mailing_account_tbl WHERE mailing_id = ?");
		List<Object> parameters = new ArrayList<>();
		parameters.add(mailingID);

		if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
			queryBuilder.append(" AND status_field IN ('A', 'T')");
		} else {
			queryBuilder.append(" AND status_field NOT IN ('A', 'T', 'V')");
		}

		if (startDate != null && endDate != null) {
			queryBuilder.append(" AND (? <= timestamp AND timestamp < ?)");
			parameters.add(startDate);
			parameters.add(endDate);
		}

		return selectIntWithDefaultValue(logger, queryBuilder.toString(), 0, parameters.toArray(new Object[0]));
	}
	
	private Date parseStatisticDate(String dateString) throws ParseException {
		String format = StringUtils.contains(dateString, ":") ? DATE_PARAMETER_FORMAT_WITH_HOUR : DATE_PARAMETER_FORMAT;
		return new SimpleDateFormat(format).parse(dateString);
	}
	
	private Date parseStatisticEndDate(String dateString) throws ParseException {
		if (StringUtils.contains(dateString, ":")) {
			return DateUtils.addHours(parseStatisticDate(dateString), 1);
		} else {
			return DateUtils.addDays(parseStatisticDate(dateString), 1);
		}
	}
	
	protected int selectNumberOfDeliveredMails(int companyID, int mailingID, String recipientsType, String targetSql, String startDateString, String endDateString) throws Exception {
    	// Do not count by "distinct customer_id", because event based mailings (birthday mailings etc.) might be delivered multiple times
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(s.customer_id) AS counter FROM success_" + companyID + "_tbl s");
		List<Object> parameters = new ArrayList<>();
        
        if (targetSql != null && targetSql.contains("cust.")) {
			queryBuilder.append(", customer_" + companyID + "_tbl cust");
			queryBuilder.append(" WHERE s.mailing_id = ? AND s.customer_id = cust.customer_id");
		} else {
			queryBuilder.append(" WHERE s.mailing_id = ?");
		}
		parameters.add(mailingID);
  
		if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
			queryBuilder.append(" AND (? <= s.timestamp AND s.timestamp < ?)");
			parameters.add(parseStatisticDate(startDateString));
			parameters.add(parseStatisticEndDate(endDateString));
		}
		
    	boolean useTargetGroup = (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1"));
		if (useTargetGroup) {
			queryBuilder.append(" AND (").append(targetSql).append(")");
		}

		if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
			queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND bind.mailinglist_id IN (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = s.mailing_id) AND bind.customer_id = s.customer_id)");
        } else if(CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
        	queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id IN (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = s.mailing_id) AND bind.customer_id = s.customer_id)");
        }

		List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

		return ((Number) result.get(0).get("counter")).intValue();
    }
	
    public String getAccountName(String accountId) {
        String accountName = select(logger, "SELECT shortname FROM company_tbl WHERE company_id = ?", String.class, accountId);
        accountName = accountName == null ? "" : accountName.trim();
        return accountName;
    }
	
    public String getReportName(String reportId) {
        String reportName = select(logger, "SELECT shortname FROM birtreport_tbl WHERE report_id = ?", String.class, reportId);
        reportName = reportName == null ? "" : reportName.trim();
        return reportName;
    }
    
    public boolean successTableActivated(int companyId) {
    	return select(logger, "SELECT COALESCE(mailtracking, 0) FROM company_tbl WHERE company_id = ?", Integer.class, companyId) != 0;
    }

    public boolean hasSuccessTableData(int companyID, int mailingID) {
    	return select(logger, "SELECT COUNT(*) FROM success_" + companyID + "_tbl WHERE mailing_id = ?", Integer.class, mailingID) > 0;
	}
	
	@Override
	protected void logSqlError(Exception e, Logger childClassLogger, String statement, Object... parameter) {
		getJavaMailService().sendExceptionMail(0, "SQL: " + statement + "\nParameter: " + getParameterStringList(parameter), e);
    	if (parameter != null && parameter.length > 0) {
    		childClassLogger.error("Error: " + e.getMessage() + "\nSQL:" + statement + "\nParameter: " + getParameterStringList(parameter), e);
    	} else {
    		childClassLogger.error("Error: " + e.getMessage() + "\nSQL:" + statement, e);
    	}
	}
	
	/**
	 * Gets data from embedded derby database.
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 */
	protected List<Map<String, Object>> selectEmbedded(Logger childClassLogger, String statement, Object... parameter) throws Exception {
		try {
			logSqlStatement(childClassLogger, "EMBEDDED: " + statement, parameter);
			return getEmbeddedJdbcTemplate().queryForList(statement, parameter);
		} catch (DataAccessException e) {
			logSqlError(e, childClassLogger, "EMBEDDED: " + statement, parameter);
			throw e;
		} catch (RuntimeException e) {
			logSqlError(e, childClassLogger, "EMBEDDED: " + statement, parameter);
			throw e;
		}
	}
	
	/**
	 * Gets data from embedded derby database.
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 */
	protected <T> List<T> selectEmbedded(Logger childClassLogger, String statement, RowMapper<T> rowMapper, Object... parameter) throws Exception {
		try {
			logSqlStatement(childClassLogger, "EMBEDDED: " + statement, parameter);
			return getEmbeddedJdbcTemplate().query(statement, rowMapper, parameter);
		} catch (DataAccessException e) {
			logSqlError(e, childClassLogger, "EMBEDDED: " + statement, parameter);
			throw e;
		} catch (RuntimeException e) {
			logSqlError(e, childClassLogger, "EMBEDDED: " + statement, parameter);
			throw e;
		}
	}
	
	/**
	 * Gets data from embedded derby database.
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 */
	protected <T> T selectEmbedded(Logger childClassLogger, String statement, Class<T> requiredType, Object... parameter) throws Exception {
		try {
			logSqlStatement(childClassLogger, "EMBEDDED: " + statement, parameter);
			return getEmbeddedJdbcTemplate().queryForObject(statement, requiredType, parameter);
		} catch (DataAccessException e) {
			logSqlError(e, childClassLogger, "EMBEDDED: " + statement, parameter);
			throw e;
		} catch (RuntimeException e) {
			logSqlError(e, childClassLogger, "EMBEDDED: " + statement, parameter);
			throw e;
		}
	}
	
	/**
	 * Gets data from embedded derby database.
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 */
	protected int selectEmbeddedIntWithDefault(Logger childClassLogger, String statement, int defaultValue, Object... parameter) throws Exception {
		try {
			logSqlStatement(childClassLogger, "EMBEDDED: " + statement, parameter);
			Integer value = getEmbeddedJdbcTemplate().queryForObject(statement, Integer.class, parameter);
			return value != null ? value : defaultValue;
		} catch (EmptyResultDataAccessException e) {
			if (childClassLogger.isDebugEnabled()) {
				childClassLogger.debug("Empty result, using default value: " + defaultValue);
			}
			return defaultValue;
		} catch (DataAccessException e) {
			logSqlError(e, childClassLogger, "EMBEDDED: " + statement, parameter);
			throw e;
		} catch (RuntimeException e) {
			logSqlError(e, childClassLogger, "EMBEDDED: " + statement, parameter);
			throw e;
		}
	}
	
	protected int selectEmbeddedInt(Logger childClassLogger, String statement, Object... parameter) throws Exception {
		return selectEmbeddedIntWithDefault(childClassLogger, statement, 0, parameter);
	}
	
	/**
	 * Updates data in embedded derby database.
	 * Logs the statement and parameter in debug-level, executes update and logs error.
	 */
	protected int updateEmbedded(Logger childClassLogger, String statement, Object... parameter) throws Exception {
		try {
			logSqlStatement(childClassLogger, "EMBEDDED: " + statement, parameter);
			int touchedLines = getEmbeddedJdbcTemplate().update(statement, parameter);
			if (childClassLogger.isDebugEnabled()) {
				childClassLogger.debug("lines changed by update: " + touchedLines);
			}
			return touchedLines;
		} catch (DataAccessException e) {
			logSqlError(e, childClassLogger, "EMBEDDED: " + statement, parameter);
			throw e;
		} catch (RuntimeException e) {
			logSqlError(e, childClassLogger, "EMBEDDED: " + statement, parameter);
			throw e;
		}
	}
	
	/**
	 * Execute ddl-statement in embedded derby database.
	 * Logs the statement and parameter in debug-level, executes a DDL SQL Statement.
	 */
	protected void executeEmbedded(Logger childClassLogger, String statement) throws Exception {
		try {
			logSqlStatement(childClassLogger, "EMBEDDED: " + statement);
			getEmbeddedJdbcTemplate().execute(statement);
		} catch (RuntimeException e) {
			logSqlError(e, childClassLogger, "EMBEDDED: " + statement);
			throw e;
		}
	}

	/**
	 * Method to update multiple data entries at once.<br />
	 * Logs the statement and parameter in debug-level, executes update and logs error.<br />
	 * Watch out: Oracle returns value -2 (= Statement.SUCCESS_NO_INFO) per line for success with no "lines touched" info<br />
	 */
	public int[] batchupdateEmbedded(Logger childClassLogger, String statement, List<Object[]> values) throws Exception {
		try {
			logSqlStatement(childClassLogger, "EMBEDDED: " + statement, "BatchUpdateParameterList(Size: " + values.size() + ")");
			int[] touchedLines = getEmbeddedJdbcTemplate().batchUpdate(statement, values);
			if (childClassLogger.isDebugEnabled()) {
				childClassLogger.debug("lines changed by update: " + Arrays.toString(touchedLines));
			}
			return touchedLines;
		} catch (RuntimeException e) {
			logSqlError(e, childClassLogger, "EMBEDDED: " + statement, "BatchUpdateParameterList(Size: " + values.size() + ")");
			throw e;
		}
	}
    
	public boolean isMailTrackingExpired(int companyID, int mailingID) {
        int periodicallySendEntries = selectInt(logger, "SELECT COUNT(mst.mailing_id) AS count FROM maildrop_status_tbl mst JOIN mailing_tbl mt ON mst.mailing_id = mt.mailing_id WHERE mst.mailing_id = ? AND mst.status_field IN ('C', 'E', 'R', 'D') AND mt.work_status = '" + MailingStatus.ACTIVE.getDbKey() + "' AND mst.senddate < CURRENT_TIMESTAMP", mailingID);
        if (periodicallySendEntries > 0) {
        	return false;
        }

		int expirePeriod = getConfigService().getIntegerValue(ConfigValue.ExpireSuccess, companyID);
		if (expirePeriod <= 0) {
			return false;
		}

		int countOfOnceSending = selectInt(logger, "SELECT COUNT(mst.mailing_id) AS count FROM maildrop_status_tbl mst WHERE mst.mailing_id = ? AND mst.status_field IN ('W') AND mst.senddate < CURRENT_TIMESTAMP AND mst.senddate >= ?", mailingID, DateUtilities.getDateOfDaysAgo(expirePeriod));
		return countOfOnceSending <= 0;
    }
	
	public boolean checkEmbeddedTableExists(String tableName) throws Exception {
		try (Connection connection = getEmbeddedDatasource().getConnection()) {
			try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM " + tableName + " WHERE 1 = 0")) {
				return true;
			} catch (Exception e) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
}
