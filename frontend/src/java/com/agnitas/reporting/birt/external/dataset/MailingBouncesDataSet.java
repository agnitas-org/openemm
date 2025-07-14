/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;

import com.agnitas.emm.core.bounce.Bounce;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.BouncesEmailStatRow;
import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;

public class MailingBouncesDataSet extends BIRTDataSet {

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

		Bounce bounce;
		int mailingId;
		int count;
		int countPercent;
		private final String lang;

		public BouncesRow(Bounce bounce, int mailingId, int count, int countPercent, String lang) {
			this.bounce = bounce;
			this.mailingId = mailingId;
			this.count = count;
			this.countPercent = countPercent;
			this.lang = lang;
		}

		public String getDetailstring() {
			return bounce.getDetailMsg(lang);
		}
		public int getDetail() {
			return bounce.getCode();
		}
		public int getCount() {
			return count;
		}
		public int getCountPercent() {
			return countPercent;
		}
        public int getMailingId() {
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

	public BouncesAvailableContainer getBouncesWithDetailByMailings(int companyID, String mailings, String lang, String targets, BounceType bounceType, String hiddenTarget) throws Exception {
		BouncesAvailableContainer container = new BouncesAvailableContainer();

		List<Integer> mailingIDs = parseCommaSeparatedIds(mailings);
		if (mailingIDs.isEmpty()) {
			return container;
		}
		for (Integer mailingId : mailingIDs) {
			List<BouncesRow> bounces = getBouncesWithDetail(companyID, mailingId, lang, targets, bounceType, hiddenTarget);
			bounces.addAll(getMissingSoftBounces(bounces, mailingId, lang));
			container.addBouncesData(mailingId, bounces);
		}
		return container;
	}

	private static List<BouncesRow> getMissingSoftBounces(List<BouncesRow> existing, Integer mailingId, String lang) {
		Set<Integer> existingCodes = existing.stream().map(BouncesRow::getDetail).collect(Collectors.toSet());
		return Arrays.stream(Bounce.values())
			.map(Bounce::getCode)
			.filter(code -> !existingCodes.contains(code))
			.map(code -> new BouncesRow(Bounce.from(code), mailingId, 0, 0, lang))
			.toList();
	}

	public List<BouncesRow> getBouncesWithDetail(int companyID, int mailingID, String language, String selectedTargets, BounceType bounceType) throws Exception {
		return getBouncesWithDetail(companyID, mailingID, language, selectedTargets, bounceType, null);
	}

	public List<BouncesRow> getBouncesWithDetail(int companyId, int mailingId, String lang, String targets, BounceType bounceType, String hiddenTarget) throws Exception {
		lang = StringUtils.defaultIfEmpty(lang, "EN");

		// In the mailing_statistic.rptdesign "-1" is a default value if target groups are not present
		if ("-1".equals(targets)){
			targets = "";
		}

		List<BouncesRow> returnList = new ArrayList<>();
		if (bounceType == BounceType.SOFTBOUNCES || bounceType == BounceType.BOTH) {
			returnList.addAll(getSoftBouncesWithDetail(companyId, mailingId, lang, targets, hiddenTarget));
		}
		if (bounceType == BounceType.HARDBOUNCES || bounceType == BounceType.BOTH) {
			returnList.addAll(getHardBouncesWithDetail(companyId, mailingId, lang, targets));
		}
		return returnList;
	}

	private List<BouncesRow> getSoftBouncesWithDetail(int companyId, int mailingId, String lang, String targets, String hiddenTarget) {
		String targetSql = joinWhereClause(getTargetSqlString(targets, companyId), getTargetSqlString(hiddenTarget, companyId));
		String sql = """
        SELECT COUNT(DISTINCT bounce.customer_id) amount, bounce.detail AS detail
        FROM bounce_tbl bounce %s
        WHERE bounce.company_id = ? AND bounce.mailing_id = ? AND bounce.detail <= 509 %s
        GROUP BY detail
        ORDER BY detail""".formatted(
			isNotBlank(targetSql) ? "JOIN customer_%d_tbl cust ON (bounce.customer_id = cust.customer_id)".formatted(companyId) : "",
			isNotBlank(targetSql) ? "AND (" + targetSql + ")" : "");

		List<BouncesRow> result = select(sql, (rs, i) -> getSoftBounceRow(rs, mailingId, lang), companyId, mailingId);
		int total = result.stream().mapToInt(bounce -> bounce.count).sum();
		result.forEach(item -> item.countPercent = total > 0 ? Math.round(item.count * 100f / total) : 0);
		return result;
	}

	private static BouncesRow getSoftBounceRow(ResultSet rs, int mailingId, String lang) throws SQLException {
		return new BouncesRow(Bounce.from(rs.getInt("detail")), mailingId, rs.getInt("amount"), 0, lang);
	}

	private List<BouncesRow> getHardBouncesWithDetail(int companyId, int mailingId, String lang, String targets) throws Exception {
		String targetSql = getTargetSqlString(targets, companyId);
		String sql = """
		SELECT bind.user_remark, COUNT(DISTINCT bind.customer_id) AS amount
		FROM customer_%d_binding_tbl bind %s
		WHERE bind.user_status = ? AND bind.exit_mailing_id = ? AND bind.user_type IN ('%s', '%s') %s
		GROUP BY bind.user_remark
		""".formatted(companyId,
			isNotBlank(targetSql) ? "JOIN customer_%d_tbl cust ON bind.customer_id = cust.customer_id".formatted(companyId) : "",
			UserType.World.getTypeCode(), UserType.WorldVIP.getTypeCode(),
			isNotBlank(targetSql) ? " AND (%s)".formatted(targetSql) : "");

		List<BouncesRow> foundBounces = select(sql,
			(rs, i) -> getHardBounceRow(rs, mailingId, lang),
			UserStatus.Bounce.getStatusCode(), mailingId);

		List<BouncesRow> result = foundBounces.stream().filter(b -> !is510bounce(b)).collect(Collectors.toList());
		int bounce510count = foundBounces.stream().filter(MailingBouncesDataSet::is510bounce).mapToInt(b -> b.count).sum();
		result.add(new BouncesRow(Bounce.OTHER_HARD_BOUNCE, mailingId, bounce510count, 0, lang));

		int total = result.stream().mapToInt(b -> b.count).sum();
		result.forEach(item -> item.countPercent = total > 0 ? Math.round(item.count * 100f / total) : 0);

		if (successTableActivated(companyId)) {
			result.add(getSoftBouncesUndelivered(companyId, mailingId, lang, total));
		}
		return result;
	}

	private static BouncesRow getHardBounceRow(ResultSet rs, int mailingId, String lang) throws SQLException {
		Bounce bounce = getBounceFromRemark(rs.getString("user_remark"));
		return new BouncesRow(bounce, mailingId, rs.getInt("amount"), 0, lang);
	}

	// detail = 510 + all unparseable userRemarks as other hardBounces
	private static boolean is510bounce(BouncesRow bounce) {
		return bounce.getDetail() <= 0 || bounce.getDetail() == Bounce.OTHER_HARD_BOUNCE.getId();
	}

	// Userremark may be of format "bounce:<code>"
	private static Bounce getBounceFromRemark(String userRemark) {
		if (userRemark == null || !userRemark.startsWith(BOUNCE_REMARK_SIGN)) {
			return Bounce.OTHER_HARD_BOUNCE;
		}
		String bounceDetailCodeStr = userRemark.substring(BOUNCE_REMARK_SIGN.length()).trim();
		return AgnUtils.isNumber(bounceDetailCodeStr)
			? Bounce.from(Integer.parseInt(bounceDetailCodeStr))
			: Bounce.OTHER_HARD_BOUNCE;
	}

    private BouncesRow getSoftBouncesUndelivered(int companyID, int mailingID, String language, int hardTotal) throws Exception {
        int numberSentMailings = getNumberSentMailings(companyID, mailingID, null, null, null, null);
        int numberDeliveredMailings = selectNumberOfDeliveredMails(companyID, mailingID, null, null, null, null);
        int count = numberSentMailings - numberDeliveredMailings - hardTotal;

		return new BouncesRow(
			Bounce.UNDELIVERABLE, mailingID,
			count,
			0, // Row is not used in total count, so percent is 0
			language);
    }

	public List<BouncesEmailStatRow> getBouncesWithDetailAndEmail(int companyID, int mailingID, String language, String selectedTargets) {
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
			if (isNotBlank(targetSql)) {
				query.append(" AND (" + targetSql + ")");
			}

			List<Map<String, Object>> result = select(query.toString(), companyID, mailingID);
			for (Map<String, Object> resultRow : result) {
				int bounceDetailCode = toInt(resultRow.get("detail"));
				
				BouncesEmailStatRow row = new BouncesEmailStatRow();
				row.setMailingId(mailingID);
				row.setCustomerID(toInt(resultRow.get("customer_id")));
				row.setEmail((String) resultRow.get("email"));
				row.setFirstname((String) resultRow.get("firstname"));
				row.setLastname((String) resultRow.get("lastname"));
				row.setGender(I18nString.getLocaleString("recipient.gender." + toInt(resultRow.get("gender")) + ".short", language));
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
			if (isNotBlank(targetSql)) {
				query.append(" AND (" + targetSql + ")");
			}
			query.append(" GROUP BY cust.email, cust.gender, cust.firstname, cust.lastname, cust.customer_id");

			List<Map<String, Object>> result = select(query.toString(), UserStatus.Bounce.getStatusCode(), mailingID);
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
				row.setCustomerID(toInt(resultRow.get("customer_id")));
				row.setEmail((String) resultRow.get("email"));
				row.setFirstname((String) resultRow.get("firstname"));
				row.setLastname((String) resultRow.get("lastname"));
				row.setGender(I18nString.getLocaleString("recipient.gender." + toInt(resultRow.get("gender")) + ".short", language));
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

	/**
	 * Contains all hard bounce statuses and their names provided by the AGNINTAS.
	 */
	public enum HardBounceType {
		OTHER_HARD_BOUNCES(510), // also mentioned as [standard, general] in code
		UNKNOWN_ADDRESS(511),
		UNKNOWN_DOMAIN_NAME(512);

		private final int statusCode;

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
				if (bounce.count > 0 && bounce.getMailingId() > 0 && bounce.getMailingId() == mailingID) {
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
