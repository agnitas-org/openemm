/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.MailingClickStatsPerTargetRow;
import com.agnitas.reporting.birt.external.utils.FormatTools;

public class MailingURLClicksPerTargetGroupDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingURLClicksPerTargetGroupDataSet.class);

	public static final String DATE_PATTERN = "YYYYMMDD";

	public List<MailingClickStatsPerTargetRow> getGrosNetClicksForTargets(
			List<Integer> targetIDs, int mailingID, @VelocityCheck int companyID , Boolean includeAdminAndTestMails) {
		List<MailingClickStatsPerTargetRow> statrowsList = new ArrayList<>();
		String query = getSelectGrosNettoClicksforTargets(targetIDs, mailingID, companyID);
		int totalsent = new MailingSendDataSet().getTotalSend(mailingID, includeAdminAndTestMails);
		String currentTargetGroup = "__DUMMY__";
		int currentColumnIndex = -1;
		
		List<Map<String, Object>> result = select(logger, query);
		for (Map<String, Object> resultRow : result) {
			MailingClickStatsPerTargetRow row = new MailingClickStatsPerTargetRow();
			if (!currentTargetGroup.equals(resultRow.get("targetgroup"))) {
				currentTargetGroup = (String) resultRow.get("targetgroup");
				currentColumnIndex++;
			}
			row.setUrl((String) resultRow.get("url"));
			row.setUrl_id(((Number) resultRow.get("url_id")).intValue());
			row.setTargetgroup((String) resultRow.get("targetgroup"));
			row.setClicks_gross(((Number) resultRow.get("clicks_gros")).intValue());
			row.setClicks_net(((Number) resultRow.get("clicks_net")).intValue());
			row.setColumn_index(currentColumnIndex);

			float net_percent = 0;
			float gros_percent = 0;
			if (totalsent != 0) {
				gros_percent = ((Number) resultRow.get("clicks_gros")).floatValue() / totalsent;
				net_percent = ((Number) resultRow.get("clicks_net")).floatValue() / totalsent;
			}
			row.setClicks_gross_percent((float) (FormatTools.roundDecimal(gros_percent, 1)));
			row.setClicks_net_percent((float) (FormatTools.roundDecimal(net_percent, 1)));
			statrowsList.add(row);
		}
		return statrowsList;
	}

	public String getSelectGrosNettoClicksforTargets(List<Integer> targetIDs, int mailingID, @VelocityCheck int companyID) {
		return "SELECT  url, url_id, targetgroup, clicks_gros, clicks_net  FROM (" + getSubSelectsGrosNettoClicksforTargets(targetIDs, mailingID, companyID) + ")";
	}

	// gros/netto clicks total ( mailing_stat.rptdesign )
	/**
	 * The click-statistic should be able to analyze the clicks in dependency of
	 * a target group. Problem is the target_sql which can't be easily appended
	 * to the statement. So for every target-group an own statement must be
	 * created.
	 * 
	 * @param targetID
	 * @param companyID
	 * @param mailingID
	 * @return a single subselect which will be used for the target
	 */
	public String getSubSelectGrosNettoClicksperTarget(int targetID, int mailingID, @VelocityCheck int companyID) {
		String template = null;
		LightTarget target = getTarget(targetID, companyID);
		if (StringUtils.isNotBlank(target.getTargetSQL())) {
			template = getSubSelectTemplateBruttoNettoClicksperTarget();
			template = template.replace("<TARGETGROUP>", target.getName());
			template = template.replace("<COMPANYID>", Integer.toString(companyID));
			template = template.replace("<MAILINGID>", Integer.toString(mailingID));
			template = template.replace("<TARGETSQL>", target.getTargetSQL());
		}
		return template;
	}

	/**
	 * The single subselects joined with 'union'.
	 * 
	 * @param targetIDs
	 * @param companyID
	 * @param mailingID
	 * @return
	 */
	public String getSubSelectsGrosNettoClicksforTargets(List<Integer> targetIDs, int mailingID, @VelocityCheck int companyID) {
		StringBuffer selectBuffer = new StringBuffer();
		for (Integer targetID : targetIDs) {
			String query = getSubSelectGrosNettoClicksperTarget(targetID, mailingID, companyID);
			if (query != null) {
				selectBuffer.append(query);
				selectBuffer.append(" UNION ");
			}
		}
		String unionSelect = selectBuffer.delete(selectBuffer.lastIndexOf("UNION"), selectBuffer.length()).toString();
		return unionSelect;
	}

	private String getSubSelectTemplateBruttoNettoClicksperTarget() {
		return "(SELECT '<TARGETGROUP>' as targetgroup, "
				+ " DECODE(urltbl.shortname, NULL, urltbl.full_url, urltbl.shortname) as url,"
				+ " urltbl.url_id as url_id, "
				+ " count(logtbl.customer_id) as clicks_gros, "
				+ " count(distinct logtbl.customer_id) as clicks_net "
				+ " FROM rdir_url_tbl urltbl join rdirlog_<COMPANYID>_tbl" 
				+ " logtbl on (urltbl.mailing_id=<MAILINGID> " 
		        + " AND (relevance <> 2 OR relevance IS NULL) "
				+ " and logtbl.url_id = urltbl.url_id ) "
				+ " join customer_<COMPANYID>_tbl cust on (cust.customer_id= logtbl.customer_id) "
				+ " where <TARGETSQL> "
				+ " GROUP BY urltbl.full_url,urltbl.url_id, urltbl.shortname ) ";
	}

	// clicks per day( clicks_week_overview.rptdesign )

	public String getSubSelectsClicksPerWeek(List<Integer> targetIDs, int mailingID, @VelocityCheck int companyID, 
			int urlID, String startdate, String enddate, String pattern) {
		StringBuffer selectBuffer = new StringBuffer();
		for (Integer targetID : targetIDs) {
			selectBuffer.append(getSubSelectClicksPerWeek(targetID, companyID, mailingID, urlID, startdate, enddate, pattern));
			selectBuffer.append(" UNION ");
		}
		String unionSelect = selectBuffer.delete(selectBuffer.lastIndexOf("UNION"), selectBuffer.length()).toString();
		return unionSelect;
	}

	public String getSubSelectClicksPerWeek(int targetID, int mailingID, @VelocityCheck int companyID, int urlID, 
			String startdate, String enddate, String pattern) {
		LightTarget target = getTarget(targetID, companyID);
		String targetgroup = target.getName();
		String targetSQL = target.getTargetSQL();
		StringBuffer templateBuffer = new StringBuffer();
        String ifNull = getIfNull();
        templateBuffer.append("(select TO_CHAR(myday,'<PATTERN>') as day, "+ifNull+"(clicksperday.netto,0) as netto, "
        		+ifNull+"(clicksperday.brutto,0) as brutto, '<TARGETGROUP>' as targetgroup from ");
		templateBuffer.append(getSelect7Days());
		templateBuffer.append("left join ( ");
		templateBuffer.append(getSubSelectTemplateClicksPerWeek());
		templateBuffer.append(" ON (to_char(dummydays.myday,'<PATTERN>') = clicksperday.mydate))");

		String targetSubSelect = templateBuffer.toString();
		if (StringUtils.isNotBlank(target.getTargetSQL())) {
			targetSubSelect = targetSubSelect.replaceAll("<PATTERN>", pattern);
			targetSubSelect = targetSubSelect.replaceAll("<TARGETGROUP>", targetgroup);
			targetSubSelect = targetSubSelect.replaceAll("<COMPANYID>", Integer.toString(companyID));
			targetSubSelect = targetSubSelect.replaceAll("<TARGETSQL>", targetSQL);
			targetSubSelect = targetSubSelect.replaceAll("<MAILINGID>", Integer.toString(mailingID));
			targetSubSelect = targetSubSelect.replaceAll("<URLID>", Integer.toString(urlID));
			targetSubSelect = targetSubSelect.replaceAll("<DATE>", startdate);
			targetSubSelect = targetSubSelect.replaceAll("<STARTDATE>", startdate);
			targetSubSelect = targetSubSelect.replaceAll("<ENDDATE>", enddate);
			return targetSubSelect;
		}
		return null;
	}

	private String getSubSelectTemplateClicksPerWeek() {
		return "select to_char(rdir.timestamp, '<PATTERN>') as mydate "
				+ ",count(rdir.customer_id ) as brutto, count(distinct rdir.customer_id ) as netto from rdirlog_<COMPANYID>_tbl rdir "
				+ "join customer_<COMPANYID>_tbl cust on (rdir.customer_id = cust.customer_id and <TARGETSQL> ) "
				+ "where rdir.mailing_id=<MAILINGID> and rdir.url_id=<URLID> "
				+ "and (rdir.timestamp >= TO_DATE('<STARTDATE>', '<PATTERN>') and rdir.timestamp <= TO_DATE('<ENDDATE>', '<PATTERN>') ) "
				+ "group by to_char(rdir.timestamp, '<PATTERN>')) clicksperday";
	}

	private String getSelect7Days() {
		return " (SELECT (to_date('<DATE>','<PATTERN>')  + LEVEL  -1) as myday " + " FROM DUAL " + " CONNECT BY LEVEL <= 7 ) dummydays ";
	}

	// clicks per hour ( clicks_day_overview.rptdesign )

	public String getSubSelectsClicksPerHourTargets(List<Integer> targetIDs,
			int mailingID, @VelocityCheck int companyID, int urlID, String startdate, String pattern) {
		StringBuffer templateBuffer = new StringBuffer();
        String ifNull = getIfNull();
        for (Integer targetID : targetIDs) {
			templateBuffer.append(" select hour, "+ifNull+"(brutto ,0) as brutto, "+ifNull+"(netto,0) as netto, targetgroup from ( ");
			templateBuffer.append("( ");
			templateBuffer.append(getSelectNumbers0_to_24());
			templateBuffer.append(" ) hours ");
			templateBuffer.append(" LEFT JOIN ( ");
			templateBuffer.append(getSubSelectClicksPerHourTarget(targetID, companyID, mailingID, urlID, startdate, pattern));
			templateBuffer.append(" ON (hours.hour = clicks.time)) ");
			templateBuffer.append(" UNION ");
		}
		String unionSelect = templateBuffer.delete(templateBuffer.lastIndexOf("UNION"), templateBuffer.length()).toString();
		return unionSelect;
	}

	public String getSubSelectClicksPerHourTarget(Integer targetID, int mailingID, @VelocityCheck int companyID, 
			int urlID, String startdate, String pattern) {
		String template = null;
		LightTarget target = getTarget(targetID, companyID);
		template = getSubSelectTemplateClicksPerHour();
		template = template.replaceAll("<TARGETGROUP>", target.getName());
		template = template.replaceAll("<COMPANYID>", Integer.toString(companyID));
		template = template.replaceAll("<TARGETSQL>", target.getTargetSQL());
		template = template.replaceAll("<MAILINGID>", Integer.toString(mailingID));
		template = template.replaceAll("<PATTERN>", pattern);
		template = template.replaceAll("<STARTDATE>", startdate);
		template = template.replaceAll("<URLID>", Integer.toString(urlID));
		return template;
	}

	private String getSubSelectTemplateClicksPerHour() {
		return "select to_char(rdir.timestamp, 'HH24') as time "
				+ " , count(rdir.customer_id) as brutto, count(distinct rdir.customer_id) as netto, '<TARGETGROUP>' as targetgroup "
				+ " from rdirlog_<COMPANYID>_tbl rdir join customer_<COMPANYID>_tbl cust ON (<TARGETSQL> and cust.customer_id = rdir.customer_id) "
				+ " where rdir.mailing_id= <MAILINGID> "
				+ " and rdir.url_id=<URLID> "
				+ " and to_char( rdir.timestamp, '<PATTERN>') = '<STARTDATE>' group by to_char(rdir.timestamp, 'HH24')) clicks ";
	}

	public String getSelectNumbers0_to_24() {
		return "SELECT LEVEL -1 AS HOUR " + " FROM DUAL " + " CONNECT BY LEVEL <= 24 ";
	}
}
