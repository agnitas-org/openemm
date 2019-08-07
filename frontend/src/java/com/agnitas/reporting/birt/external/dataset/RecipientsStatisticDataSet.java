/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightMailingList;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.utils.BirtReporUtils;

public class RecipientsStatisticDataSet extends BIRTDataSet {
	private static final transient Logger logger = Logger.getLogger(RecipientsStatisticDataSet.class);

	public final static String ACTIVE = "recipient.MailingState1";
	public final static int ACTIVE_INDEX = 1;

	public final static String OPENERS = "report.opens";
	public final static int OPENERS_INDEX = 3;
	public final static String CLICKER = "report.clicker";
	public final static int CLICKER_INDEX = 6;

	public final static String OPENERS_TRACKED = "report.individual.opens.measured";
	public final static int OPENERS_TRACKED_INDEX = 13;
	public final static String OPENERS_PC = "report.individual.opens.pc";
	public final static int OPENERS_PC_INDEX = 14;
	public final static String OPENERS_MOBILE = "report.individual.opens.mobile";
	public final static int OPENERS_MOBILE_INDEX = 15;
	public final static String OPENERS_TABLET = "report.individual.opens.tablet";
	public final static int OPENERS_TABLET_INDEX = 16;
	public final static String OPENERS_SMARTTV = "report.opens.smarttv";
	public final static int OPENERS_SMARTTV_INDEX = 17;
	public final static String OPENERS_MULTIPLE_DEVICES = "report.openers.multiple-devices";
	public final static int OPENERS_MULTIPLE_DEVICES_INDEX = 18;

	public final static String CLICKER_TRACKED = "report.clicker";
	public final static int CLICKER_TRACKED_INDEX = 19;
	public final static String CLICKER_PC = "statistic.clicker.pc";
	public final static int CLICKER_PC_INDEX = 20;
	public final static String CLICKER_MOBILE = "statistic.clicker.mobile";
	public final static int CLICKER_MOBILE_INDEX = 21;
	public final static String CLICKER_TABLET = "statistic.clicker.tablet";
	public final static int CLICKER_TABLET_INDEX = 22;
	public final static String CLICKER_SMARTTV = "statistic.clicker.smarttv";
	public final static int CLICKER_SMARTTV_INDEX = 23;
	public final static String CLICKER_MULTIPLE_DEVICES = "statistic.clicker.multiple";
	public final static int CLICKER_MULTIPLE_DEVICES_INDEX = 24;

	public final static int ACTIVE_START_DATE_INDEX = 25;
	public final static int ACTIVE_END_DATE_INDEX = 26;
	public final static int CLICKER_END_DATE_INDEX = 27;
	public final static int OPENERS_END_DATE_INDEX = 28;
	public final static int CLICKER_TRACKED_END_DATE_INDEX = 29;
	public final static int CLICKER_PC_END_DATE_INDEX = 30;
	public final static int CLICKER_MOBILE_END_DATE_INDEX = 31;
	public final static int CLICKER_TABLET_END_DATE_INDEX = 32;
	public final static int CLICKER_SMARTTV_END_DATE_INDEX = 33;
	public final static int CLICKER_MULTIPLE_DEVICES_END_DATE_INDEX = 34;
	public final static int OPENERS_TRACKED_END_DATE_INDEX = 35;
	public final static int OPENERS_PC_END_DATE_INDEX = 36;
	public final static int OPENERS_MOBILE_END_DATE_INDEX = 37;
	public final static int OPENERS_TABLET_END_DATE_INDEX = 38;
	public final static int OPENERS_SMARTTV_END_DATE_INDEX = 39;
	public final static int OPENERS_MULTIPLE_DEVICES_END_DATE_INDEX = 40;

	// All recipients is a targetgroup
	public final static String ALL_SUBSCRIBERS = "statistic.all_subscribers";
	public final static int ALL_SUBSCRIBERS_TARGETGROUPID = 1;
	public final static int ALL_SUBSCRIBERS_INDEX = 1;

	public final static int DATE_CONSTRAINT_BETWEEN = 0;
	public final static int DATE_CONSTRAINT_LESS_THAN_START = 1;
	public final static int DATE_CONSTRAINT_LESS_THAN_STOP = 2;

	protected Map<Integer, String> mailinglistNamesById = new HashMap<>();
	
	private List<RecipientsStatisticRow> statList = new ArrayList<>();
	private List<RecipientCollectedStatisticRow> collectedStatisticList;

	public Map<String, Integer> initRecipientsStatistic(@VelocityCheck int companyId, String selectedMailingListsAsString, String selectedTargets, String startDateString, String stopDateString, String figuresOptions) throws Exception {
		List<Integer> mailingListIds = new ArrayList<>();
		for (String mailingListIdString : Arrays.asList(selectedMailingListsAsString.split(","))) {
			mailingListIds.add(Integer.parseInt(mailingListIdString));
		}
	
		List<BirtReporUtils.BirtReportFigure> figures = BirtReporUtils.unpackFigures(figuresOptions);
		int tempTableId = createRecipientsStatisticTempTable();

		populateSelectedMailinglists(companyId, mailingListIds);

		insertEmptyRowsIntoTempTable(companyId, tempTableId, mailingListIds, selectedTargets);

		if (figures.contains(BirtReporUtils.BirtReportFigure.MAILING_TYPE)) {
			addRecipientStatMailtype(companyId, tempTableId, mailingListIds, selectedTargets, startDateString, stopDateString);
		}

		// this method calculate recipients count for chart "Recipient analysis by
		// target groups" which is shown always
		addRecipientStatUserStatus(companyId, tempTableId, mailingListIds, selectedTargets, startDateString, stopDateString, false);
		
		 // we need active in the end of period for crossTab
		if (figures.contains(BirtReporUtils.BirtReportFigure.RECIPIENT_STATUS)
				|| figures.contains(BirtReporUtils.BirtReportFigure.MAILING_TYPE)
				|| figures.contains(BirtReporUtils.BirtReportFigure.RECIPIENT_DEVELOPMENT_NET)
				|| figures.contains(BirtReporUtils.BirtReportFigure.ACTIVITY_ANALYSIS)
				|| figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_MEASURED)
				|| figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_TOTAL)) {
			addRecipientStatUserStatus(companyId, tempTableId, mailingListIds, selectedTargets, startDateString, stopDateString, true);
		}

		updateNumberOfTargetGroups(tempTableId, selectedTargets.split(",").length);

		int tempTableInfoInColumnId = prepareReport(companyId, selectedTargets, selectedMailingListsAsString, startDateString, stopDateString, figuresOptions, tempTableId);

		Map<String, Integer> tempTableIds = new HashMap<>();
		tempTableIds.put("tempTableInfoInRowId", tempTableId);
		tempTableIds.put("tempTableInfoInColumnId", tempTableInfoInColumnId);
		return tempTableIds;
	}

	/**
	 * This method returns prepared data by temporary table id Data consists values
	 * groped by mailinglists and target groups
	 *
	 * @param tempTableId
	 * @return
	 * @throws Exception 
	 * @throws  
	 */
	public List<RecipientsStatisticRow> getRecipientsStatistic(int tempTableId) throws Exception {
		if (statList.size() == 0) {
			String selectMailinglistSsql = "SELECT mailinglist_id, mailinglist_group_id, targetgroup_id, mailinglist_name, targetgroup_name, count_type_text, count_type_html,"
					+ " count_type_offline_html, count_active, count_active_for_period, count_waiting_for_confirm, count_blacklisted, count_optout, count_bounced, count_gender_male,"
					+ " count_gender_female, count_gender_unknown, count_recipient, count_target_group, count_active_as_of, count_blacklisted_as_of, count_optout_as_of, count_bounced_as_of,"
					+ " count_waiting_for_confirm_as_of, count_recipient_as_of"
				+ " FROM " + getTempReportTableName(tempTableId)
				+ " ORDER BY mailinglist_id, targetgroup_id";

			statList = selectEmbedded(logger, selectMailinglistSsql, new RecipientsStatisticRowMapper(mailinglistNamesById));

			ArrayList<RecipientsStatisticRow> statisticRows = new ArrayList<>();
			statisticRows.addAll(statList);
			Collections.sort(statisticRows, new RecipientsStatisticRowComparator());

			int previousMailingListId = -1;
			int currentMailingListGroupId = -1;
			for (RecipientsStatisticRow row : statisticRows) {
				if (previousMailingListId != row.getMailingListId()) {
					currentMailingListGroupId++;
					previousMailingListId = row.getMailingListId();
				}
				row.setMailingListGroupId(currentMailingListGroupId);
			}
		}
		return statList;
	}

	/**
	 * Prepare report by collecting the values from different queries in one table
	 * 
	 * @return id of the created temporary table
	 * @throws Exception 
	 **/
	public int prepareReport(@VelocityCheck int companyId, String selectedTargetsAsString, String selectedMailingListsAsString, String startDateString, String endDateString, String figuresOptions, int tempTableInfoInRowId) throws Exception {
		List<Integer> mailingListIds = new ArrayList<>();
		for (String mailingListIdString : Arrays.asList(selectedMailingListsAsString.split(","))) {
			mailingListIds.add(Integer.parseInt(mailingListIdString));
		}
		
		LightTarget allSubscribers = new LightTarget();
		allSubscribers.setId(ALL_SUBSCRIBERS_TARGETGROUPID);
		allSubscribers.setName(ALL_SUBSCRIBERS);
		allSubscribers.setTargetSQL("");
		List<LightTarget> targetGroups = new ArrayList<>();
		targetGroups.add(allSubscribers);
		targetGroups.addAll(getTargets(selectedTargetsAsString, companyId));
		
		Date startDate = null;
		if (StringUtils.isNotBlank(startDateString)) {
			startDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString);
		}
		
		Date endDate = null;
		if (StringUtils.isNotBlank(endDateString)) {
			endDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString);
		}
		
		List<BirtReporUtils.BirtReportFigure> figures = BirtReporUtils.unpackFigures(figuresOptions);
		int tempTableId = createTempTable();

		// add active recipients
		addActiveRecipients(companyId, targetGroups, mailingListIds, startDateString, endDateString, tempTableId, tempTableInfoInRowId, figures.contains(BirtReporUtils.BirtReportFigure.RECIPIENT_DEVELOPMENT_NET));

		if (figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_TOTAL) || figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_AFTER_DEVICE)) {
			insertClickersIntoTempTable(tempTableId, companyId, targetGroups, mailingListIds, startDate, endDate, figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_AFTER_DEVICE));
		}

		if (figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_MEASURED) || figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_AFTER_DEVICE)) {
			Map<String, Integer> indexes = new HashMap<>();
			indexes.put(OPENERS, OPENERS_END_DATE_INDEX);
			indexes.put(OPENERS_TRACKED, OPENERS_TRACKED_END_DATE_INDEX);
			indexes.put(OPENERS_PC, OPENERS_PC_END_DATE_INDEX);
			indexes.put(OPENERS_MOBILE, OPENERS_MOBILE_END_DATE_INDEX);
			indexes.put(OPENERS_TABLET, OPENERS_TABLET_END_DATE_INDEX);
			indexes.put(OPENERS_SMARTTV, OPENERS_SMARTTV_END_DATE_INDEX);
			indexes.put(OPENERS_MULTIPLE_DEVICES, OPENERS_MULTIPLE_DEVICES_END_DATE_INDEX);
			insertOpenersIntoTempTable(tempTableId, companyId, targetGroups, mailingListIds, startDate, endDate, indexes, DATE_CONSTRAINT_LESS_THAN_STOP, figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_AFTER_DEVICE));
		}

		updateAllRates(tempTableId, companyId, targetGroups, figures);

		populateSelectedMailinglists(companyId, mailingListIds);

		return tempTableId;
	}

	/**
	 * Get collected values by temporary table id
	 * 
	 * @param tempTableId
	 * @return list
	 * @throws Exception 
	 * @throws DataAccessException 
	 */
	public List<RecipientCollectedStatisticRow> getData(int tempTableId) throws Exception {
		if (collectedStatisticList == null) {
			String query = "SELECT mailinglist_id, category, category_index, targetgroup_id, targetgroup, targetgroup_index, value, rate"
				+ " FROM " + getTempReportTableName(tempTableId)
				+ " ORDER BY mailinglist_id, category_index, targetgroup_index";

			collectedStatisticList = selectEmbedded(logger, query, new RecipientCollectedStatisticRowMapper(mailinglistNamesById));

			int previousMailingListId = -1;
			int currentMailingListGroupId = -1;
			for (RecipientCollectedStatisticRow row : collectedStatisticList) {
				if (previousMailingListId != row.getMailingListId()) {
					currentMailingListGroupId++;
					previousMailingListId = row.getMailingListId();
				}
				row.setMailingListGroupId(currentMailingListGroupId);
			}
		}
		return collectedStatisticList;
	}

	/**
	 * create a temporary table to collect the values from different queries for the
	 * report in one table
	 * 
	 * @return id of the create temporary table
	 * @throws Exception 
	 * @throws  
	 **/
	private int createTempTable() throws Exception {
		int tempTableId = getNextTmpID();

		String createTableSQL = "CREATE TABLE " + getTempReportTableName(tempTableId)
			+ " (mailinglist_id INTEGER,"
			+ " category VARCHAR(200),"
			+ " category_index INTEGER,"
			+ " targetgroup_id INTEGER,"
			+ " targetgroup VARCHAR(200),"
			+ " targetgroup_index INTEGER,"
			+ " value INTEGER,"
			+ " rate DOUBLE)";

		executeEmbedded(logger, createTableSQL);
		
		return tempTableId;
	}

	/**
	 * Create a temporary table to collect the values from different queries for the
	 * report in one table
	 * 
	 * @return id of the create temporary table
	 * @throws Exception - trouble during executing statements.
	 **/
	private int createRecipientsStatisticTempTable() throws Exception {
		int tempTableId = getNextTmpID();

		String createTableSQL = "CREATE TABLE " + getTempReportTableName(tempTableId)
			+ " (mailinglist_id INTEGER,"
			+ " mailinglist_group_id INTEGER,"
			+ " targetgroup_id INTEGER,"
			+ " mailinglist_name VARCHAR(200),"
			+ " targetgroup_name VARCHAR(200),"
			+ " count_type_text INTEGER,"
			+ " count_type_html INTEGER,"
			+ " count_type_offline_html INTEGER,"
			+ " count_active INTEGER,"
			+ " count_active_for_period INTEGER,"
			+ " count_waiting_for_confirm INTEGER,"
			+ " count_blacklisted INTEGER,"
			+ " count_optout INTEGER,"
			+ " count_bounced INTEGER,"
			+ " count_gender_male INTEGER,"
			+ " count_gender_female INTEGER,"
			+ " count_gender_unknown INTEGER,"
			+ " count_recipient INTEGER,"
			+ " count_target_group INTEGER,"
			+ " count_active_as_of INTEGER,"
			+ " count_blacklisted_as_of INTEGER,"
			+ " count_optout_as_of INTEGER,"
			+ " count_bounced_as_of INTEGER,"
			+ " count_waiting_for_confirm_as_of INTEGER,"
			+ " count_recipient_as_of INTEGER)";

		executeEmbedded(logger, createTableSQL);

		return tempTableId;
	}

	/**
	 * Collect clickers recipients values
	 *
	 * @param tempTableId
	 * @param companyId
	 * @param targetGroups
	 * @param mailingListIds
	 * @param startDate
	 * @param endDate
	 * @param calculateDeviceClasses
	 * @throws Exception
	 */
	private void insertClickersIntoTempTable(int tempTableId, @VelocityCheck int companyId, List<LightTarget> targetGroups, List<Integer> mailingListIds, Date startDate, Date endDate, boolean calculateDeviceClasses) throws Exception {
		String insertEmbedded = getTempInsertQuery(tempTableId);

		for (int mailingListId : mailingListIds) {
			int targetgroupIndex = ALL_SUBSCRIBERS_INDEX;
			for (LightTarget target : targetGroups) {
				String targetSql = target.getTargetSQL();
				boolean useTargetSql = false;
				if (StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
					useTargetSql = true;
				}

				// Overall clickers
		        StringBuilder queryOverallClickers = new StringBuilder("SELECT COUNT(DISTINCT r.customer_id) AS counter FROM rdirlog_" + companyId + "_tbl r");
		        List<Object> queryOverallClickersParameters = new ArrayList<>();

		        if (useTargetSql && targetSql.contains("cust.")) {
		        	queryOverallClickers.append(" JOIN customer_" + companyId + "_tbl cust ON r.customer_id = cust.customer_id");
				}
		        
		        queryOverallClickers.append(" WHERE r.mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND mailinglist_id = ?)");
		        queryOverallClickersParameters.add(companyId);
		        queryOverallClickersParameters.add(mailingListId);
		        
		        queryOverallClickers.append(" AND r.customer_id != 0");

				if (startDate != null) {
					queryOverallClickers.append(" AND ? <= r.timestamp");
					queryOverallClickersParameters.add(startDate);
		        }
				
				if (endDate != null) {
					queryOverallClickers.append(" AND r.timestamp <= ?");
					queryOverallClickersParameters.add(endDate);
		        }
				
				if (useTargetSql) {
					queryOverallClickers.append(" AND (").append(targetSql).append(")");
				}

				List<Map<String, Object>> resultOverallClickers = selectLongRunning(logger, queryOverallClickers.toString(), queryOverallClickersParameters.toArray(new Object[0]));
				int overallClickers = ((Number) resultOverallClickers.get(0).get("counter")).intValue();
				
				// Unique deviceclass clickers
		        StringBuilder queryClickersPerUniqueDevices = new StringBuilder("SELECT r.device_class_id AS deviceClassId, COUNT(DISTINCT r.customer_id) AS counter FROM rdirlog_" + companyId + "_tbl r");
		        List<Object> queryClickersPerUniqueDevicesParameters = new ArrayList<>();
		        
		        if (useTargetSql && targetSql.contains("cust.")) {
					queryClickersPerUniqueDevices.append(" JOIN customer_" + companyId + "_tbl cust ON r.customer_id = cust.customer_id");
				}
		        
				queryClickersPerUniqueDevices.append(" WHERE r.mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND mailinglist_id = ?)");
		        queryClickersPerUniqueDevicesParameters.add(companyId);
		        queryClickersPerUniqueDevicesParameters.add(mailingListId);
		        
		        queryClickersPerUniqueDevices.append(" AND r.customer_id != 0");
		        
		        // Exclude clicks of clickers with combinations of deviceclasses
		        queryClickersPerUniqueDevices.append(" AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyId + "_tbl rdir WHERE rdir.device_class_id != r.device_class_id AND rdir.customer_id = r.customer_id)");

				if (startDate != null) {
					queryClickersPerUniqueDevices.append(" AND ? <= r.timestamp");
					queryClickersPerUniqueDevicesParameters.add(startDate);
		        }
				
				if (endDate != null) {
					queryClickersPerUniqueDevices.append(" AND r.timestamp <= ?");
					queryClickersPerUniqueDevicesParameters.add(endDate);
		        }
				
				if (useTargetSql) {
					queryClickersPerUniqueDevices.append(" AND (").append(targetSql).append(")");
				}
				
				queryClickersPerUniqueDevices.append(" GROUP BY r.device_class_id");

				List<Map<String, Object>> result = selectLongRunning(logger, queryClickersPerUniqueDevices.toString(), queryClickersPerUniqueDevicesParameters.toArray(new Object[0]));
				
				Map<DeviceClass, Integer> uniqueDeviceClassClickers = new HashMap<>();
				// Initialize default values 0 for no clickers at all
				for (DeviceClass deviceClass : CommonKeys.AVAILABLE_DEVICECLASSES) {
					uniqueDeviceClassClickers.put(deviceClass, 0);
				}
				for (Map<String, Object> row : result) {
					DeviceClass deviceClass;
					if (row.get("deviceClassId") == null) {
						// Some old entries dont't have a deviceclassid, those are desktop values
						deviceClass = DeviceClass.DESKTOP;
					} else {
						deviceClass = DeviceClass.fromId(((Number) row.get("deviceClassId")).intValue());
					}
					uniqueDeviceClassClickers.put(deviceClass, ((Number) row.get("counter")).intValue() + uniqueDeviceClassClickers.get(deviceClass));
				}
				
				// Others
				int multipleDeviceClassClickers = overallClickers;
				for (int deviceClassClickers : uniqueDeviceClassClickers.values()) {
					multipleDeviceClassClickers = multipleDeviceClassClickers - deviceClassClickers;
				}

				if (calculateDeviceClasses) {
					updateEmbedded(logger, insertEmbedded, mailingListId, CLICKER_PC, CLICKER_PC_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, uniqueDeviceClassClickers.get(DeviceClass.DESKTOP));
					updateEmbedded(logger, insertEmbedded, mailingListId, CLICKER_MOBILE, CLICKER_MOBILE_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, uniqueDeviceClassClickers.get(DeviceClass.MOBILE));
					updateEmbedded(logger, insertEmbedded, mailingListId, CLICKER_TABLET, CLICKER_TABLET_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, uniqueDeviceClassClickers.get(DeviceClass.TABLET));
					updateEmbedded(logger, insertEmbedded, mailingListId, CLICKER_SMARTTV, CLICKER_SMARTTV_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, uniqueDeviceClassClickers.get(DeviceClass.SMARTTV));
					updateEmbedded(logger, insertEmbedded, mailingListId, CLICKER_MULTIPLE_DEVICES, CLICKER_MULTIPLE_DEVICES_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, multipleDeviceClassClickers);
				}

				updateEmbedded(logger, insertEmbedded, mailingListId, CLICKER, CLICKER_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, overallClickers);
				updateEmbedded(logger, insertEmbedded, mailingListId, CLICKER_TRACKED, CLICKER_TRACKED_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, overallClickers);

				targetgroupIndex++;
			}
		}
	}

	private void insertOpenersIntoTempTable(int tempTableId, @VelocityCheck int companyId, List<LightTarget> targetGroups, List<Integer> mailingListIds, Date startDate, Date endDate, Map<String, Integer> indexes, int dateConstraintType, boolean calculateDeviceClasses) throws Exception {
		String insertEmbedded = getTempInsertQuery(tempTableId);

		for (int mailingListId : mailingListIds) {
			int targetgroupIndex = ALL_SUBSCRIBERS_INDEX;
			for (LightTarget target : targetGroups) {
				String targetSql = target.getTargetSQL();
				boolean useTargetSql = false;
				if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
					useTargetSql = true;
				}

				// Overall openers
		        StringBuilder queryOverallOpeners = new StringBuilder("SELECT COUNT(DISTINCT o.customer_id) AS counter FROM onepixellog_device_" + companyId + "_tbl o");
		        List<Object> queryOverallOpenersParameters = new ArrayList<>();

		        if (useTargetSql && targetSql.contains("cust.")) {
		        	queryOverallOpeners.append(" JOIN customer_" + companyId + "_tbl cust ON o.customer_id = cust.customer_id");
				}
		        
		        queryOverallOpeners.append(" WHERE o.mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND mailinglist_id = ?)");
		        queryOverallOpenersParameters.add(companyId);
		        queryOverallOpenersParameters.add(mailingListId);
		        
		        queryOverallOpeners.append(" AND o.customer_id != 0");

				if (startDate != null) {
					queryOverallOpeners.append(" AND ? <= o.creation");
					queryOverallOpenersParameters.add(startDate);
		        }
				
				if (endDate != null) {
					queryOverallOpeners.append(" AND o.creation <= ?");
					queryOverallOpenersParameters.add(endDate);
		        }
				
				if (useTargetSql) {
					queryOverallOpeners.append(" AND (").append(targetSql).append(")");
				}

				List<Map<String, Object>> resultOverallOpeners = selectLongRunning(logger, queryOverallOpeners.toString(), queryOverallOpenersParameters.toArray(new Object[0]));
				int overallOpeners = ((Number) resultOverallOpeners.get(0).get("counter")).intValue();
				
				// Unique deviceclass openers
		        StringBuilder queryOpenersPerUniqueDevices = new StringBuilder("SELECT o.device_class_id AS deviceClassId, COUNT(DISTINCT o.customer_id) AS counter FROM onepixellog_device_" + companyId + "_tbl o");
		        List<Object> queryOpenersPerUniqueDevicesParameters = new ArrayList<>();
		        
		        if (useTargetSql && targetSql.contains("cust.")) {
					queryOpenersPerUniqueDevices.append(" JOIN customer_" + companyId + "_tbl cust ON o.customer_id = cust.customer_id");
				}
		        
				queryOpenersPerUniqueDevices.append(" WHERE o.mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND mailinglist_id = ?)");
		        queryOpenersPerUniqueDevicesParameters.add(companyId);
		        queryOpenersPerUniqueDevicesParameters.add(mailingListId);
		        
		        queryOpenersPerUniqueDevices.append(" AND o.customer_id != 0");
		        
		        // Exclude openings of openers with combinations of deviceclasses
		        queryOpenersPerUniqueDevices.append(" AND NOT EXISTS (SELECT 1 FROM onepixellog_device_" + companyId + "_tbl opl WHERE opl.device_class_id != o.device_class_id AND opl.customer_id = o.customer_id)");

				if (startDate != null) {
					queryOpenersPerUniqueDevices.append(" AND ? <= o.creation");
					queryOpenersPerUniqueDevicesParameters.add(startDate);
		        }
				
				if (endDate != null) {
					queryOpenersPerUniqueDevices.append(" AND o.creation <= ?");
					queryOpenersPerUniqueDevicesParameters.add(endDate);
		        }
				
				if (useTargetSql) {
					queryOpenersPerUniqueDevices.append(" AND (").append(targetSql).append(")");
				}
				
				queryOpenersPerUniqueDevices.append(" GROUP BY o.device_class_id");

				List<Map<String, Object>> result = selectLongRunning(logger, queryOpenersPerUniqueDevices.toString(), queryOpenersPerUniqueDevicesParameters.toArray(new Object[0]));
				
				Map<DeviceClass, Integer> uniqueDeviceClassOpeners = new HashMap<>();
				// Initialize default values 0 for no openers at all
				for (DeviceClass deviceClass : CommonKeys.AVAILABLE_DEVICECLASSES) {
					uniqueDeviceClassOpeners.put(deviceClass, 0);
				}
				for (Map<String, Object> row : result) {
					DeviceClass deviceClass;
					if (row.get("deviceClassId") == null) {
						// Some old entries dont't have a deviceclassid, those are desktop values
						deviceClass = DeviceClass.DESKTOP;
					} else {
						deviceClass = DeviceClass.fromId(((Number) row.get("deviceClassId")).intValue());
					}
					uniqueDeviceClassOpeners.put(deviceClass, ((Number) row.get("counter")).intValue() + uniqueDeviceClassOpeners.get(deviceClass));
				}
				
				// Others
				int multipleDeviceClassOpeners = overallOpeners;
				for (int deviceClassOpeners : uniqueDeviceClassOpeners.values()) {
					multipleDeviceClassOpeners = multipleDeviceClassOpeners - deviceClassOpeners;
				}
				
				if (calculateDeviceClasses) {
					updateEmbedded(logger, insertEmbedded, mailingListId, OPENERS_TRACKED, indexes.get(OPENERS_TRACKED), target.getName(), target.getId(), targetgroupIndex, overallOpeners);
					updateEmbedded(logger, insertEmbedded, mailingListId, OPENERS_PC, indexes.get(OPENERS_PC), target.getName(), target.getId(), targetgroupIndex, uniqueDeviceClassOpeners.get(DeviceClass.DESKTOP));
					updateEmbedded(logger, insertEmbedded, mailingListId, OPENERS_MOBILE, indexes.get(OPENERS_MOBILE), target.getName(), target.getId(), targetgroupIndex, uniqueDeviceClassOpeners.get(DeviceClass.MOBILE));
					updateEmbedded(logger, insertEmbedded, mailingListId, OPENERS_TABLET, indexes.get(OPENERS_TABLET), target.getName(), target.getId(), targetgroupIndex, uniqueDeviceClassOpeners.get(DeviceClass.TABLET));
					updateEmbedded(logger, insertEmbedded, mailingListId, OPENERS_SMARTTV, indexes.get(OPENERS_SMARTTV), target.getName(), target.getId(), targetgroupIndex, uniqueDeviceClassOpeners.get(DeviceClass.SMARTTV));
					updateEmbedded(logger, insertEmbedded, mailingListId, OPENERS_MULTIPLE_DEVICES, indexes.get(OPENERS_MULTIPLE_DEVICES), target.getName(), target.getId(), targetgroupIndex, multipleDeviceClassOpeners);
				}

				updateEmbedded(logger, insertEmbedded, mailingListId, OPENERS, indexes.get(OPENERS), target.getName(), target.getId(), targetgroupIndex, overallOpeners);

				targetgroupIndex++;
			}
		}
	}

	/**
	 * Calculate rates for collected calues
	 *
	 * @param tempTableId
	 * @param companyId
	 * @param targetGroups list of selected target groups
	 * @param figures report checkbox parameters
	 * @throws Exception
	 */
	private void updateAllRates(int tempTableId, @VelocityCheck int companyId, List<LightTarget> targetGroups, List<BirtReporUtils.BirtReportFigure> figures) throws Exception {
		if (figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_TOTAL)
				|| figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_AFTER_DEVICE)) {
			updateRates(tempTableId, companyId, targetGroups,
				new Object[] { ALL_SUBSCRIBERS_INDEX, CLICKER_TRACKED_END_DATE_INDEX },
				new Integer[] { CLICKER_TRACKED_END_DATE_INDEX, CLICKER_PC_END_DATE_INDEX,
						CLICKER_MOBILE_END_DATE_INDEX, CLICKER_TABLET_END_DATE_INDEX,
						CLICKER_SMARTTV_END_DATE_INDEX, CLICKER_MULTIPLE_DEVICES_END_DATE_INDEX });
		}

		if (figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_MEASURED)
				|| figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_AFTER_DEVICE)) {
			updateRates(tempTableId, companyId, targetGroups,
				new Object[] { ALL_SUBSCRIBERS_INDEX, OPENERS_TRACKED_END_DATE_INDEX },
				new Integer[] { OPENERS_TRACKED_END_DATE_INDEX, OPENERS_PC_END_DATE_INDEX,
						OPENERS_MOBILE_END_DATE_INDEX, OPENERS_TABLET_END_DATE_INDEX,
						OPENERS_SMARTTV_END_DATE_INDEX, OPENERS_MULTIPLE_DEVICES_END_DATE_INDEX });
		}

		updateRates(tempTableId, companyId, targetGroups,
			new Object[] { ALL_SUBSCRIBERS_INDEX, ACTIVE_INDEX },
			new Integer[] { ACTIVE_INDEX, OPENERS_INDEX, CLICKER_INDEX });

		updateRates(tempTableId, companyId, targetGroups,
			new Object[] { ALL_SUBSCRIBERS_INDEX, ACTIVE_END_DATE_INDEX },
			new Integer[] { ACTIVE_END_DATE_INDEX, OPENERS_END_DATE_INDEX, CLICKER_END_DATE_INDEX });
	}
	
	private void updateRates(int tempTableId, @VelocityCheck int companyId, List<LightTarget> targetGroups, Object[] mainIndexes, Integer[] indexes) throws Exception {
		String queryTotal = "SELECT mailinglist_id, value FROM " + getTempReportTableName(tempTableId) + " WHERE targetgroup_index = ? AND category_index = ?";
		List<Map<String, Object>> results = selectEmbedded(logger, queryTotal, mainIndexes);
		for (Map<String, Object> row : results) {
			Integer mailingListId = row.get("mailinglist_id") != null ? ((Number) row.get("mailinglist_id")).intValue() : 0;
			Integer total = row.get("value") != null ? ((Number) row.get("value")).intValue() : 0;
			if (total > 0) {
				StringBuilder query = new StringBuilder("UPDATE ").append(getTempReportTableName(tempTableId))
					.append(" SET rate = (value * 1.0) / ?  WHERE targetgroup_index = ? AND mailinglist_id = ? AND category_index IN (")
					.append(StringUtils.join(indexes, ", "))
					.append(")");
				String queryUpdateRate = query.toString();
				updateEmbedded(logger, queryUpdateRate, total, ALL_SUBSCRIBERS_INDEX, mailingListId);
				
				if (!CollectionUtils.isEmpty(targetGroups) && isMailingTrackingActivated(companyId)) {
					int targetGroupIndex = ALL_SUBSCRIBERS_INDEX;
					for(int index = 0; index < targetGroups.size(); index++) {
						try {
							++targetGroupIndex;
							updateEmbedded(logger, queryUpdateRate, total, targetGroupIndex, mailingListId);
						} catch (DataAccessException e) {
							logger.error("No target group data");
						}
					}
				}
			}
		}
	}
	
	private void addRecipientStatMailtype(@VelocityCheck int companyId, int tempTableId, List<Integer> mailingListIds, String selectedTargets, String startDate, String stopDate) throws Exception {
		for (int mailinglistId : mailingListIds) {
			// All subscribers
			addRecipientStatMailtype(companyId, tempTableId, mailinglistId, null, startDate, stopDate);

			// insert data for each target group
			List<LightTarget> targets = getTargets(selectedTargets, companyId);
			if (targets != null) {
				for (LightTarget target : targets) {
					addRecipientStatMailtype(companyId, tempTableId, mailinglistId, target, startDate, stopDate);
				}
			}
		}
	}

	private void addRecipientStatMailtype(@VelocityCheck int companyId, int tempTableId, int mailinglistId, LightTarget target, String startDate, String stopDate) throws Exception {
		StringBuilder recipientStatMailtypeTempate = new StringBuilder("SELECT COUNT(DISTINCT cust.customer_id) AS mailtype_count, cust.mailtype AS mailtype")
			.append(" FROM customer_" + companyId + "_binding_tbl bind")
			.append(" JOIN customer_" + companyId + "_tbl cust ON (bind.customer_id = cust.customer_id)")
			.append(" WHERE bind.user_status = ").append(UserStatus.Active.getStatusCode())
			.append(" AND bind.mailinglist_id = " + mailinglistId);
		
		if (target != null && StringUtils.isNotBlank(target.getTargetSQL())) {
			recipientStatMailtypeTempate.append(" AND (" + target.getTargetSQL() + ")");
		}
		
		// add date constraint
		recipientStatMailtypeTempate.append(getDateConstraint("bind.timestamp", startDate, stopDate, DATE_CONSTRAINT_LESS_THAN_STOP));
		
		recipientStatMailtypeTempate.append(" GROUP BY cust.mailtype");
		
		List<Map<String, Object>> result = select(logger, recipientStatMailtypeTempate.toString());
		int countTypeText = 0;
		int countTypeHtml = 0;
		int countTypeOfflineHtml = 0;
		for (Map<String, Object> row : result) {
			int count = ((Number) row.get("mailtype_count")).intValue();
			switch (MailType.getFromInt(((Number) row.get("mailtype")).intValue())) {
				case TEXT:
					countTypeText = count;
					break;
				case HTML:
					countTypeHtml = count;
					break;
				case HTML_OFFLINE:
					countTypeOfflineHtml = count;
					break;
			}
		}

		String updateQuery = new StringBuilder("UPDATE ").append(getTempReportTableName(tempTableId))
			.append(" SET count_type_text = ?, count_type_html = ?, count_type_offline_html = ?")
			.append(" WHERE mailinglist_id = ? AND targetgroup_id = ?").toString();
		updateEmbedded(logger, updateQuery, countTypeText, countTypeHtml, countTypeOfflineHtml, mailinglistId, (target != null ? target.getId() : 0));
	}
	
	private void addRecipientStatUserStatus(@VelocityCheck int companyId, int tempTableId, List<Integer> mailingListIds, String selectedTargets, String startDate, String stopDate, boolean isAsOf) throws Exception {
		for (int mailinglistId : mailingListIds) {
			// All subscribers
			insertRecipientStatUserStatus(companyId, tempTableId, mailinglistId, null, startDate, stopDate, isAsOf);

			// insert data for each target group
			List<LightTarget> targets = getTargets(selectedTargets, companyId);
			if (targets != null) {
				for (LightTarget target : targets) {
					insertRecipientStatUserStatus(companyId, tempTableId, mailinglistId, target, startDate, stopDate, isAsOf);
				}
			}
		}
	}

	private void insertRecipientStatUserStatus(@VelocityCheck int companyId, int tempTableId, int mailinglistId, LightTarget target, String startDate, String stopDate, boolean isAsOf) throws Exception {
		StringBuilder recipientStatUserStatusTempate = new StringBuilder("SELECT COUNT(DISTINCT cust.customer_id) AS status_count, bind.user_status AS user_status")
			.append(" FROM customer_" + companyId + "_binding_tbl bind")
			.append(" JOIN customer_" + companyId + "_tbl cust ON (bind.customer_id = cust.customer_id)")
			.append(" WHERE bind.mailinglist_id = " + mailinglistId);
		
		if (target != null && StringUtils.isNotBlank(target.getTargetSQL())) {
			recipientStatUserStatusTempate.append(" AND (" + target.getTargetSQL() + ")");
		}
		
		// add date constraint
		recipientStatUserStatusTempate.append(getDateConstraint("bind.timestamp", startDate, stopDate, (isAsOf ? DATE_CONSTRAINT_LESS_THAN_STOP : DATE_CONSTRAINT_BETWEEN)));
		
		recipientStatUserStatusTempate.append(" GROUP BY bind.user_status");

		int activeCountForPeriod = 0;
		int waitingForConfirmCount = 0;
		int blacklistedCount = 0;
		int optoutCount = 0;
		int bouncedCount = 0;
		int recipientCount = 0;

		List<Map<String, Object>> result = select(logger, recipientStatUserStatusTempate.toString());
		for (Map<String, Object> row : result) {
			int amount = ((Number) row.get("status_count")).intValue();
			int status = ((Number) row.get("user_status")).intValue();

			switch (UserStatus.getUserStatusByID(status)) {
				case Active:
					activeCountForPeriod = amount;
					break;

				case Bounce:
					bouncedCount = amount;
					break;

				case Blacklisted:
					blacklistedCount = amount;
					break;

				case AdminOut:
				case UserOut:
					optoutCount += amount;
					break;

				case WaitForConfirm:
					waitingForConfirmCount = amount;
					break;
					
				default:
					// do nothing
			}

			recipientCount += amount;
		}
		StringBuilder update = new StringBuilder("UPDATE ").append(getTempReportTableName(tempTableId));
		if (isAsOf) {
			update.append(" SET count_active_as_of = ?, count_waiting_for_confirm_as_of = ?, count_blacklisted_as_of = ?, count_optout_as_of = ?, count_bounced_as_of = ?, count_recipient_as_of = ?");
		} else {
			update.append(" SET count_active_for_period = ?, count_waiting_for_confirm = ?, count_blacklisted = ?, count_optout = ?, count_bounced = ?, count_recipient = ?");
		}
		update.append(" WHERE mailinglist_id = ? AND targetgroup_id = ?");

		updateEmbedded(logger, update.toString(), activeCountForPeriod, waitingForConfirmCount, blacklistedCount, optoutCount,
			bouncedCount, recipientCount, mailinglistId, (target != null ? target.getId() : 0));
	}

	private void populateSelectedMailinglists(int companyId, List<Integer> mailingListIds) {
		if (mailinglistNamesById.size() == 0) {
			List<LightMailingList> mailingLists = getMailingLists(mailingListIds, companyId);
			for (LightMailingList mailingList : mailingLists) {
				mailinglistNamesById.put(mailingList.getMailingListId(), mailingList.getShortname());
			}
		}
	}

	private String getDateConstraint(String fieldName, String startDate, String stopDate, int constraintType) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			// validate date format
			switch (constraintType) {
				case DATE_CONSTRAINT_BETWEEN:
					format.parse(startDate);
					format.parse(stopDate);
					break;
				case DATE_CONSTRAINT_LESS_THAN_START:
					format.parse(startDate);
					break;
				case DATE_CONSTRAINT_LESS_THAN_STOP:
					format.parse(stopDate);
					break;
				default:
					//should never happen
					throw new Exception("Can not parse startDate(" + startDate + ") or endDate(" + stopDate + ")");
			}
		} catch (ParseException e) {
			throw new Exception("Can not parse startDate(" + startDate + ") or endDate(" + stopDate + ")", e);
		}

		if (isOracleDB()) {
			switch (constraintType) {
				case DATE_CONSTRAINT_BETWEEN:
					return new StringBuilder(" AND ").append(fieldName).append(" BETWEEN TO_DATE('").append(startDate).append("', 'yyyy-mm-dd')")
						.append(" AND (TO_DATE('").append(stopDate).append("', 'yyyy-mm-dd') + 1) ").toString();
				case DATE_CONSTRAINT_LESS_THAN_START:
					return new StringBuilder(" AND ").append(fieldName).append(" < (TO_DATE('").append(startDate).append("', 'yyyy-mm-dd') + 1)").toString();
				case DATE_CONSTRAINT_LESS_THAN_STOP:
					return new StringBuilder(" AND ").append(fieldName).append(" < (TO_DATE('").append(stopDate).append("', 'yyyy-mm-dd') + 1)").toString();
				default:
					throw new Exception("Can not create DateConstraint");
			}
		} else {
			switch (constraintType) {
				case DATE_CONSTRAINT_BETWEEN:
					return new StringBuilder(" AND ").append(fieldName).append(" BETWEEN STR_TO_DATE('").append(startDate).append("', '%Y-%m-%d')")
						.append(" AND STR_TO_DATE('").append(stopDate).append("', '%Y-%m-%d') + INTERVAL 1 DAY ").toString();
				case DATE_CONSTRAINT_LESS_THAN_START:
					return new StringBuilder(" AND ").append(fieldName).append(" < STR_TO_DATE('").append(startDate).append("', '%Y-%m-%d') + INTERVAL 1 DAY").toString();
				case DATE_CONSTRAINT_LESS_THAN_STOP:
					return new StringBuilder(" AND ").append(fieldName).append(" < STR_TO_DATE('").append(stopDate).append("', '%Y-%m-%d') + INTERVAL 1 DAY").toString();
				default:
					throw new Exception("Can not create DateConstraint");
			}
		}
	}

	private void insertEmptyRowsIntoTempTable(@VelocityCheck int companyId, int tempTableId, List<Integer> mailingListIds, String selectedTargets) throws Exception {
		String tempRecipientsStatRowInsert = new StringBuilder("INSERT INTO ").append(getTempReportTableName(tempTableId))
			.append(" (mailinglist_id, targetgroup_id, mailinglist_name, targetgroup_name)")
			.append(" VALUES (?, ?, ?, ?)").toString();
		
		for (int mailinglistId : mailingListIds) {
			// All subscribers
			updateEmbedded(logger, tempRecipientsStatRowInsert,
				mailinglistId,
				0,
				mailinglistNamesById.get(mailinglistId),
				"");

			// insert data for each target group
			// we need just insert empty rows
			List<LightTarget> targets = getTargets(selectedTargets, companyId);
			if (targets != null) {
				for (LightTarget target : targets) {
					int mailinglistId1 = mailinglistId;
					updateEmbedded(logger, tempRecipientsStatRowInsert,
						mailinglistId1,
						(target != null ? target.getId() : 0),
						mailinglistNamesById.get(mailinglistId1),
						(target != null ? target.getName() : ""));
				}
			}
		}
	}

	private void addActiveRecipients(@VelocityCheck int companyId, List<LightTarget> targetGroups, List<Integer> mailingListIds, String startDateString, String endDateString, int tempTableId, int tempTableInfoInRowId, boolean calculateRecipientDevelopmentNet) throws Exception {
		// we expect that list has already sorted by mailinglistId and targetgroupId
		List<RecipientsStatisticRow> statisticRows = getRecipientsStatistic(tempTableInfoInRowId);
		int targetGroupIndex = ALL_SUBSCRIBERS_INDEX;
		int previousMailinglistId = -1;
		for (int i = 0; i < statisticRows.size(); i++) {
			RecipientsStatisticRow row = statisticRows.get(i);

			if (row.getMailingListId() != previousMailinglistId) {
				previousMailinglistId = row.getMailingListId();
				targetGroupIndex = ALL_SUBSCRIBERS_INDEX;
			}
			
			int targetGroupId = ALL_SUBSCRIBERS_TARGETGROUPID;
			String targetGroupName = ALL_SUBSCRIBERS;
			
			if(row.getTargetGroupId() > 0) {
				targetGroupId = row.getTargetGroupId();
				targetGroupName = row.getTargetGroupName();
			}

			// active recipients for period
			updateEmbedded(logger, getTempInsertQuery(tempTableId),
					row.getMailingListId(), ACTIVE, ACTIVE_INDEX,
					targetGroupName, targetGroupId,
					targetGroupIndex, row.getCountActiveForPeriod());

			// active recipients in the end of period
			updateEmbedded(logger, getTempInsertQuery(tempTableId),
					row.getMailingListId(), endDateString, ACTIVE_END_DATE_INDEX,
					targetGroupName, targetGroupId,
					targetGroupIndex, row.getCountActiveAsOf());

			if (calculateRecipientDevelopmentNet) {
				// active recipients in the start of period
				for (int mailinglistId : mailingListIds) {
					// All subscribers
					insertRecipientStat(companyId, tempTableId, mailinglistId, null, ALL_SUBSCRIBERS_INDEX, startDateString, endDateString);
				
					// insert data for each target group
					for (LightTarget target : targetGroups) {
						insertRecipientStat(companyId, tempTableId, mailinglistId, target, targetGroupIndex, startDateString, endDateString);
					}
				}
			}

			targetGroupIndex++;
		}
	}

	private void insertRecipientStat(@VelocityCheck int companyId, int tempTableId, int mailinglistId, LightTarget target, int targetGroupIndex, String startDateString, String endDateString) throws Exception {
		StringBuilder recipientStatActiveTemplate = new StringBuilder("SELECT COUNT(*) AS active_count")
			.append(" FROM customer_" + companyId + "_binding_tbl bind")
			.append(" JOIN customer_" + companyId + "_tbl cust ON (bind.customer_id = cust.customer_id)")
			.append(" WHERE bind.user_status = ").append(UserStatus.Active.getStatusCode())
			.append(" AND bind.mailinglist_id = " + mailinglistId);
			
		if (target != null && StringUtils.isNotBlank(target.getTargetSQL())) {
			recipientStatActiveTemplate.append(" AND (" + target.getTargetSQL() + ")");
		}

		// add date constraint
		recipientStatActiveTemplate.append(getDateConstraint("bind.timestamp", startDateString, endDateString, DATE_CONSTRAINT_LESS_THAN_START));

		int recipientCount = selectInt(logger, recipientStatActiveTemplate.toString());

		updateEmbedded(logger, getTempInsertQuery(tempTableId),
			mailinglistId,
			startDateString,
			ACTIVE_START_DATE_INDEX,
			(targetGroupIndex == ALL_SUBSCRIBERS_INDEX ? ALL_SUBSCRIBERS : (target != null ? target.getName() : "")),
			(target != null ? target.getId() : ALL_SUBSCRIBERS_TARGETGROUPID),
			targetGroupIndex,
			recipientCount);
	}

	/**
	 * @param tempTableId
	 * @return
	 */
	private String getTempReportTableName(int tempTableId) {
		return "tmp_report_aggregation_" + tempTableId + "_tbl";
	}

	/**
	 * Returns query for inserting collected values into temporary table
	 *
	 * @param tempTableId
	 * @return
	 */
	private String getTempInsertQuery(int tempTableId) {
		return new StringBuilder("INSERT INTO ").append(getTempReportTableName(tempTableId))
			.append(" (mailinglist_id, category, category_index, targetgroup, targetgroup_id, targetgroup_index, value)")
			.append(" VALUES (?, ?, ?, ?, ?, ?, ?)").toString();
	}

	private void updateNumberOfTargetGroups(int tempTableId, int numberOfTargetGroups) throws Exception {
		updateEmbedded(logger, "UPDATE " + getTempReportTableName(tempTableId) + " SET count_target_group = ?", numberOfTargetGroups);
	}
}
