/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.BouncesEmailStatRow;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.SendStatRow;

public class MailingBouncesDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingBouncesDataSet.class);

    public static final int SOFTBOUNCES_UNDELIVERABLE = 33;

    public static final String BOUNCE_REMARK_SIGN = "bounce:";

	public MailingBouncesDataSet() {
		super();
	}
	
	public MailingBouncesDataSet(DataSource dataSource) {
		super();
		setDataSource(dataSource);
	}
	
	public static class BouncesRow {
        Integer mailingId;
		String detailstring;
		Integer detail;
		Integer count;
		Integer countPercent;
		public String getDetailstring() {
			return detailstring;
		}
		public Integer getDetail() {
			return detail;
		}
		public Integer getCount() {
			return count;
		}
		public Integer getCountPercent() {
			return countPercent;
		}
        public Integer getMailingId() {
            return mailingId;
        }
    }

	public enum BounceType {
		SOFTBOUNCES,
		HARDBOUNCES,
		BOTH;
		
		public static BounceType getByName(String typename) {
			return BounceType.valueOf(StringUtils.upperCase(typename));
		}
	}

	public BouncesAvailableContainer getBouncesWithDetailByMailings(int companyID, String mailings, String language, String selectedTargets, BounceType bounceType) throws Exception{
		List<Integer> mailingIDs = parseCommaSeparatedIds(mailings);
		if (!mailingIDs.isEmpty()){
			BouncesAvailableContainer container = new BouncesAvailableContainer();
			for(Integer id : mailingIDs){
				container.addBouncesData(id, getBouncesWithDetail(companyID, id, language, selectedTargets, bounceType));
			}
			return container;
		} else {
			return new BouncesAvailableContainer();
		}
	}

	public List<BouncesRow> getBouncesWithDetail(@VelocityCheck int companyID, int mailingID, String language, String selectedTargets, BounceType bounceType) throws Exception {
		language = StringUtils.defaultIfEmpty(language, "EN");

		// In the mailing_statistic.rptdesign "-1" is a default value if target groups are not present
		if ("-1".equals(selectedTargets)){
			selectedTargets = "";
		}

		List<BouncesRow> returnList = new ArrayList<>();

		if (bounceType == BounceType.SOFTBOUNCES || bounceType == BounceType.BOTH) {
			StringBuilder query = new StringBuilder();
			String targetSql = getTargetSqlString(selectedTargets, companyID);
			query.append("SELECT COUNT(DISTINCT bounce.customer_id) amount, bounce.detail AS detail FROM bounce_tbl bounce");
			if (StringUtils.isNotBlank(targetSql)) {
				query.append(" JOIN customer_" + companyID + "_tbl cust ON (bounce.customer_id = cust.customer_id)");
			}
			query.append(" WHERE bounce.company_id = ? AND bounce.mailing_id = ? AND bounce.detail <= 509");
			if (StringUtils.isNotBlank(targetSql)) {
				query.append(" AND (" + targetSql + ")");
			}
			query.append(" GROUP BY detail ORDER BY detail");

			List<Map<String, Object>> result = select(logger, query.toString(), companyID, mailingID);
			int softbouncesTotal = 0;
			List<BouncesRow> softbouncesList = new ArrayList<>();
			for (Map<String, Object> resultRow : result) {
				int bounceDetailCode = ((Number) resultRow.get("detail")).intValue();
				int bounceCount = ((Number) resultRow.get("amount")).intValue();

				BouncesRow row = new BouncesRow();
				row.mailingId = mailingID;
				row.count = bounceCount;
				row.detail = bounceDetailCode;
				row.detailstring = I18nString.getLocaleString("bounces.detail." + row.detail, language);
				softbouncesList.add(row);
				softbouncesTotal += row.count;
			}

			for (BouncesRow item : softbouncesList) {
				if (softbouncesTotal > 0) {
					item.countPercent = Math.round(item.count * 100f / softbouncesTotal);
				} else {
					item.countPercent = 0;
				}
			}
			returnList.addAll(softbouncesList);
		}

		if (bounceType == BounceType.HARDBOUNCES || bounceType == BounceType.BOTH) {
			String targetSql = getTargetSqlString(selectedTargets, companyID);
			StringBuilder query = new StringBuilder();
			query.append("SELECT bind.user_remark, COUNT(DISTINCT bind.customer_id) AS amount FROM customer_" + companyID + "_binding_tbl bind");
			if (StringUtils.isNotBlank(targetSql)) {
				query.append(" JOIN customer_" + companyID + "_tbl cust ON bind.customer_id = cust.customer_id");
			}
			query.append(" WHERE bind.user_status = ? AND bind.exit_mailing_id = ? AND bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "')");
			if (StringUtils.isNotBlank(targetSql)) {
				query.append(" AND (" + targetSql + ")");
			}
			query.append(" GROUP BY bind.user_remark");

			List<Map<String, Object>> result = select(logger, query.toString(), UserStatus.Bounce.getStatusCode(), mailingID);
			int hardbouncesTotal = 0;
			int standardHardbounceCount = 0; // detail = 510
			List<BouncesRow> hardbouncesList = new ArrayList<>();
			for (Map<String, Object> resultRow : result) {
				String userRemark = (String) resultRow.get("user_remark");
				int bounceCount = ((Number) resultRow.get("amount")).intValue();

				// Userremark may be of format "bounce:<code>"
				int bounceDetailCode = -1;
				if (userRemark != null && userRemark.startsWith(BOUNCE_REMARK_SIGN)) {
					String bounceDetailCodeString = userRemark.substring(BOUNCE_REMARK_SIGN.length()).trim();
					if (AgnUtils.isNumber(bounceDetailCodeString)) {
						bounceDetailCode = Integer.parseInt(bounceDetailCodeString);
					}
				}

				if (bounceDetailCode > 0 && bounceDetailCode != 510) {
					BouncesRow row = new BouncesRow();
					row.mailingId = mailingID;
					row.count = bounceCount;
					row.detail = bounceDetailCode;
					row.detailstring = I18nString.getLocaleString("bounces.detail." + row.detail, language);
					hardbouncesList.add(row);
					hardbouncesTotal += row.count;
				} else {
					// Handle all unparseable userRemarks as 510-Hardbounces
					standardHardbounceCount += bounceCount;
				}
			}
			// Create entry for 510-Hardbounces
			if (standardHardbounceCount > 0) {
				BouncesRow row = new BouncesRow();
				row.mailingId = mailingID;
				row.count = standardHardbounceCount;
				row.detail = 510;
				row.detailstring = I18nString.getLocaleString("bounces.detail." + row.detail, language);
				hardbouncesList.add(row);
				hardbouncesTotal += row.count;
			}

			for (BouncesRow item : hardbouncesList) {
				if (hardbouncesTotal > 0) {
					item.countPercent = Math.round(item.count * 100f / hardbouncesTotal);
				} else {
					item.countPercent = 0;
				}
			}
			returnList.addAll(hardbouncesList);

	        if (successTableActivated(companyID)) {
	            BouncesRow softUndelivered = getSoftbouncesUndelivered(companyID, mailingID, language, hardbouncesTotal);
	            returnList.add(softUndelivered);
	        }
		}

		return returnList;
	}

    private BouncesRow getSoftbouncesUndelivered(int companyID, int mailingID, String language, int hardTotal) throws Exception {
        BouncesRow softUndelivered = new BouncesRow();
        // Row is not used in total count, so percent is 0
        softUndelivered.countPercent = 0;
        softUndelivered.detail = SOFTBOUNCES_UNDELIVERABLE;
        softUndelivered.detailstring = I18nString.getLocaleString("report.softbounces.undeliverable", language);
        int numberSentMailings = getNumberSentMailings(companyID, mailingID, null, null, null, null);
        int numberDeliveredMailings = selectNumberOfDeliveredMails(companyID, mailingID, null, null, null, null);
        softUndelivered.count = numberSentMailings - numberDeliveredMailings - hardTotal;
        return softUndelivered;
    }

	public List<BouncesEmailStatRow> getBouncesWithDetailAndEmailByMailings(int companyID, String mailings, String language, String selectedTargets, boolean showSoftbounces, boolean showHardbounces){
		List<Integer> mailingIDs = parseCommaSeparatedIds(mailings);
		if(!mailingIDs.isEmpty()){
			List<BouncesEmailStatRow> result = new ArrayList<>();
			for(Integer id : mailingIDs){
				result.addAll(getBouncesWithDetailAndEmail(companyID, id, language, selectedTargets, showSoftbounces, showHardbounces));
			}
			return result;
		}
		return new ArrayList<>();
	}

    // For the backward compatibility
    public List<BouncesEmailStatRow> getBouncesWithDetailAndEmail(@VelocityCheck int companyID, int mailingID, String language) {
        return getBouncesWithDetailAndEmail(companyID, mailingID, language, "");
    }

	public List<BouncesEmailStatRow> getBouncesWithDetailAndEmail(@VelocityCheck int companyID, int mailingID, String language, String selectedTargets) {
		return getBouncesWithDetailAndEmail(companyID, mailingID, language, selectedTargets, true, true);
	}

	public List<BouncesEmailStatRow> getBouncesWithDetailAndEmail(int companyID, int mailingID, String language, String selectedTargets, boolean showSoftbounces, boolean showHardbounces){
		language = StringUtils.defaultIfEmpty(language, "EN");

		// In the mailing_statistic_csv.rptdesign "-1" is a default value if target groups are not present
		if ("-1".equals(selectedTargets)) {
			selectedTargets = "";
		}
		
		List<BouncesEmailStatRow> statList = new ArrayList<>();
		
		if (showSoftbounces) {
			StringBuilder query = new StringBuilder();
			String targetSql = getTargetSqlString(selectedTargets, companyID);
			query.append("SELECT cust.email email, cust.gender gender, cust.firstname firstname, cust.lastname lastname, cust.customer_id customer_id, bounce.detail AS detail FROM bounce_tbl bounce");
			query.append(" JOIN customer_" + companyID + "_tbl cust ON (bounce.customer_id = cust.customer_id)");
			query.append(" WHERE bounce.company_id = ? AND bounce.mailing_id = ? AND bounce.detail <= 509");
			if (StringUtils.isNotBlank(targetSql)) {
				query.append(" AND (" + targetSql + ")");
			}

			List<Map<String, Object>> result = select(logger, query.toString(), companyID, mailingID);
			for (Map<String, Object> resultRow : result) {
				int bounceDetailCode = ((Number) resultRow.get("detail")).intValue();
				
				BouncesEmailStatRow row = new BouncesEmailStatRow();
				row.setMailingId(mailingID);
				row.setCustomerID(((Number) resultRow.get("customer_id")).intValue());
				row.setEmail((String) resultRow.get("email"));
				row.setFirstname((String) resultRow.get("firstname"));
				row.setLastname((String) resultRow.get("lastname"));
				row.setGender(I18nString.getLocaleString("recipient.gender." + ((Number) resultRow.get("gender")).intValue() + ".short", language));
				row.setDetail(I18nString.getLocaleString("bounces.detail." + bounceDetailCode, language));
				if (bounceDetailCode == 410) {
					row.setIndex(0);
				} else if (bounceDetailCode == 420) {
					row.setIndex(1);
				} else {
					row.setIndex(2);
				}
				statList.add(row);
			}
		}
		
		if (showHardbounces) {
			String targetSql = getTargetSqlString(selectedTargets, companyID);
			StringBuilder query = new StringBuilder();
			query.append("SELECT cust.email email, cust.gender gender, cust.firstname firstname, cust.lastname lastname, cust.customer_id customer_id, MAX(bind.user_remark) AS user_remark");
			query.append(" FROM customer_" + companyID + "_binding_tbl bind");
			query.append(" JOIN customer_" + companyID + "_tbl cust ON bind.customer_id = cust.customer_id");
			query.append(" WHERE bind.user_status = ? AND bind.exit_mailing_id = ? AND bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "')");
			if (StringUtils.isNotBlank(targetSql)) {
				query.append(" AND (" + targetSql + ")");
			}
			query.append(" GROUP BY cust.email, cust.gender, cust.firstname, cust.lastname, cust.customer_id");

			List<Map<String, Object>> result = select(logger, query.toString(), UserStatus.Bounce.getStatusCode(), mailingID);
			for (Map<String, Object> resultRow : result) {
				String userRemark = (String) resultRow.get("user_remark");

				// Userremark may be of format "bounce:<code>"
				int bounceDetailCode = 510; // 510 is default hardbounce
				if (userRemark != null && userRemark.startsWith(BOUNCE_REMARK_SIGN)) {
					String bounceDetailCodeString = userRemark.substring(BOUNCE_REMARK_SIGN.length()).trim();
					if (AgnUtils.isNumber(bounceDetailCodeString)) {
						bounceDetailCode = Integer.parseInt(bounceDetailCodeString);
					}
				}

				BouncesEmailStatRow row = new BouncesEmailStatRow();
				row.setMailingId(mailingID);
				row.setCustomerID(((Number) resultRow.get("customer_id")).intValue());
				row.setEmail((String) resultRow.get("email"));
				row.setFirstname((String) resultRow.get("firstname"));
				row.setLastname((String) resultRow.get("lastname"));
				row.setGender(I18nString.getLocaleString("recipient.gender." + ((Number) resultRow.get("gender")).intValue() + ".short", language));
				row.setDetail(I18nString.getLocaleString("bounces.detail." + bounceDetailCode, language));
				if (bounceDetailCode == 511) {
					row.setIndex(3);
				} else if (bounceDetailCode == 512) {
					row.setIndex(4);
				} else {
					row.setIndex(5);
				}
				statList.add(row);
			}
		}

		return statList;
	}

	public List<SendStatRow> getTotalBounces(@VelocityCheck int companyID, int mailingID, String useMailTrackingStr, String targetIDs, String language,Boolean includeAdminAndTestMails ) {
		language = StringUtils.defaultIfEmpty(language, "EN");
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT '<TOTAL>' AS targetgroup, COUNT(DISTINCT customer_id) AS bounces FROM customer_" + companyID + "_binding_tbl bind");
		query.append(" WHERE bind.user_status = ? AND bind.exit_mailing_id = ? AND bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "')");

		List<Integer> targetIDList = com.agnitas.reporting.birt.external.utils.StringUtils.buildListFormCommaSeparatedValueString(targetIDs);
		if (useMailTrackingStr == null || "".equals(useMailTrackingStr.trim())) {
			useMailTrackingStr = "false";
		}

		boolean useMailTracking = new Boolean(useMailTrackingStr.toLowerCase());

		if (useMailTracking && !targetIDList.isEmpty()) {
			List<String> targetIDStringList = new ArrayList<>();
			for(Integer id:targetIDList) {
				targetIDStringList.add(id.toString());
			}
			List<LightTarget> targets = getTargets(targetIDStringList, companyID);
			for (LightTarget target : targets ) {
				if (StringUtils.isNotBlank(target.getTargetSQL())) {
					String targetQuery = getTargetgroupBouncesQuery(target.getName(), companyID, mailingID, target.getTargetSQL());
					query.append(" UNION ");
					query.append(targetQuery);
				}

			}
		}

		int totalsend = new MailingSendDataSet(getDataSource()).getTotalSend(mailingID, includeAdminAndTestMails);
		List<SendStatRow> statList = new ArrayList<>();
		List<Map<String, Object>> result = select(logger, query.toString(), UserStatus.Bounce.getStatusCode(), mailingID);
		int targetgroupindex = 1;
        for (Map<String, Object> resultRow : result) {
			SendStatRow row = new SendStatRow();

			String targetgroup = (String) resultRow.get("targetgroup");
			if ("<TOTAL>".equals(targetgroup)) {
				targetgroup = I18nString.getLocaleString("statistic.all_subscribers", language);
				row.setTargetgroupindex(0);
			} else {
				row.setTargetgroupindex(targetgroupindex);
				targetgroupindex++;
			}
			row.setTargetgroup(targetgroup);
			row.setCount(((Number) resultRow.get("bounces")).intValue());
			if (totalsend != 0) {
				row.setRate((row.getCount() * 1.0f) / totalsend);
			}
			statList.add(row);

		}

		return statList;
	}

	private String getTargetgroupBouncesQuery(String targetgroup, @VelocityCheck int companyID, int mailingID, String targetSQL ) {
		String template = getTargetgroupBouncesQueryTemplate();
		template = template.replace("<TARGETGROUP>", targetgroup);
		template = template.replace("<COMPANYID>", Integer.toString(companyID));
		template = template.replace("<MAILINGID>", Integer.toString(mailingID));
		template = template.replace("<TARGETSQL>", targetSQL );
		return template;
	}


	private String getTargetgroupBouncesQueryTemplate() {
		return "SELECT '<TARGETGROUP>' targetgroup, COUNT(DISTINCT (bind.customer_id)) AS bounces FROM customer_<COMPANYID>_binding_tbl bind JOIN customer_<COMPANYID>_tbl cust "
		   + " ON( cust.customer_id = bind.customer_id) "
		   + " WHERE bind.exit_mailing_id = <MAILINGID> AND bind.user_status = 2 AND (<TARGETSQL>)"
		   + " GROUP BY bind.user_status";
	}


	/**
	 * Contains all hard bounce statuses and their names provided by the AGNINTAS.
	 */
	public enum HardBounceType {
		OTHER_HARD_BOUNCES(510),
		UNKNOWN_ADDRESS(511),
		UNKNOWN_DOMAIN_NAME(512);

		private int statusCode;

		HardBounceType(int statusCode) {
			this.statusCode = statusCode;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public static boolean isValidStatusCode(int statusCode) {
			return Stream.of(HardBounceType.values()).anyMatch(value -> value.getStatusCode() == statusCode);
		}
	}

	public static class BouncesAvailableContainer {

		private final Map<Integer, BouncesAvailable> availabilities = new HashMap<>();

		private final List<BouncesRow> allBounces = new ArrayList<>();

		public void addBouncesData(int mailingID, List<BouncesRow> bounces){
			boolean hasSoftBounceData = false;
			boolean hasHardBounceData = false;
			int undeliveredCount = 0;

			for (BouncesRow bounce : bounces) {
				if (bounce.count > 0 && bounce.getMailingId() != null && bounce.getMailingId() == mailingID) {
					// setting flags
					if (!hasHardBounceData && HardBounceType.isValidStatusCode(bounce.getDetail())) {
						hasHardBounceData = true;
					} else if (!hasSoftBounceData) {
						hasSoftBounceData = true;
					}

					// setting undelivered count
					if (bounce.getDetail() == SOFTBOUNCES_UNDELIVERABLE) {
						undeliveredCount = bounce.getCount();
					}
				}
			}

			allBounces.addAll(bounces);
			availabilities.put(mailingID, new BouncesAvailable(hasSoftBounceData, hasHardBounceData, undeliveredCount));
		}

		public boolean hasSoftbounceData(int mailingID) {
			BouncesAvailable bouncesAvailable = availabilities.get(mailingID);
			return bouncesAvailable != null && bouncesAvailable.hasSoftbounceData;
		}

		public boolean hasHardbounceData(int mailingID) {
			BouncesAvailable bouncesAvailable = availabilities.get(mailingID);
			return bouncesAvailable != null && bouncesAvailable.hasHardbounceData;
		}

		public int undeliveredCount(int mailingID){
			BouncesAvailable bouncesAvailable = availabilities.get(mailingID);
			return bouncesAvailable != null ? bouncesAvailable.undeliveredCount : 0;
		}

		public List<BouncesRow> getAllBounces() {
			return allBounces;
		}
	}

	public static class BouncesAvailable {

		private final boolean hasSoftbounceData;
		private final boolean hasHardbounceData;
		private final int undeliveredCount;


		public BouncesAvailable(boolean hasSoftbounceData, boolean hasHardbounceData, int undeliveredCount) {
			this.hasSoftbounceData = hasSoftbounceData;
			this.hasHardbounceData = hasHardbounceData;
			this.undeliveredCount = undeliveredCount;
		}

		public boolean isHasSoftbounceData() {
			return hasSoftbounceData;
		}

		public boolean isHasHardbounceData() {
			return hasHardbounceData;
		}
	}
}
