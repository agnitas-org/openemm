/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.log4j.Logger;

import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.TrackingPointDef;
import com.agnitas.reporting.birt.external.beans.TrackingPointStatRow;

public class MailingAlphaTrackingPointsDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingAlphaTrackingPointsDataSet.class);

	private static final String ALL_SUBSCRIBERS = "statistic.all_subscribers";
	public final static String EXT_LINK_TABLENAMEFRAGMENT = "ext_link";
	public final static String VAL_ALPHA_TABLENAMEFRAGMENT = "val_alpha";
	public final static String VAL_NUM_TABLENAMEFRAGMENT = "val_num";
	public final static int EXT_LINK_TYPE = 0;
	public final static int VAL_ALPHA_TYPE = 2;
	public final static int VAL_NUM_TYPE = 1;
	
	public List<TrackingPointStatRow> getNumTrackingPointsClicks(String mailingID, String companyID, String targetIDsSeparated , String language) throws Exception {
		// TODO: Company ID is not checkable. Change to "int"
		List<TrackingPointDef> trackingPointsDefList = getTrackingPoints(companyID, getPageTags(mailingID, companyID));
		return getNumTrackingPointsClicks(mailingID, companyID, targetIDsSeparated, language, trackingPointsDefList);
	}
	
	public List<TrackingPointStatRow> getNumTrackingPointsClicks(String mailingID, String companyID, String targetIDsSeparated, String language, List<TrackingPointDef> trackingPointsDefList) throws Exception {
		// TODO: Company ID is not checkable. Change to "int"
	
		List<TrackingPointStatRow> statList = new ArrayList<>();

		Collection<TrackingPointDef> numTrackPoints = extractTrackingPointsDefByType(trackingPointsDefList, VAL_NUM_TYPE);
		if( numTrackPoints.size() < 1 ) {
			return statList;
		}
				
		StringBuilder pagetagsBuilder = new StringBuilder();
		for (TrackingPointDef trackingpoint : numTrackPoints) {
			pagetagsBuilder.append("'"+ trackingpoint.getPagetag() + "'");
			pagetagsBuilder.append(",");
		}
		pagetagsBuilder = pagetagsBuilder.delete(pagetagsBuilder.lastIndexOf(","), pagetagsBuilder.lastIndexOf(",") + 1);
		
        if( language == null || "".equals(language.trim() )) {
			language = "EN";
		}
		String totalQuery = getTrackingPointsStatTotalQuery(companyID, VAL_NUM_TABLENAMEFRAGMENT, mailingID, pagetagsBuilder.toString(), language);
		
		int currentcolumnIndex = 0;
		List<Map<String, Object>> result = select(logger, totalQuery);
		for (Map<String, Object> resultRow : result) {
			TrackingPointStatRow row = getTrackingPointStatRow(resultRow, currentcolumnIndex);
			statList.add(row);
		}
		
		StringBuilder queryBuilder = new StringBuilder();
		if (targetIDsSeparated != null && !"".equals(targetIDsSeparated.trim())) {
			List<String> targetIDs = new ArrayList<>();
			if (targetIDsSeparated != null
					&& !"".equals(targetIDsSeparated)) {
				StringTokenizer tokenizer = new StringTokenizer(targetIDsSeparated, ",");
					while (tokenizer.hasMoreTokens()) {
						targetIDs.add(tokenizer.nextToken());
					}
				}

				List<LightTarget> targetList = null;
				targetList = getTargets(targetIDs, Integer.parseInt(companyID));
				for (LightTarget target : targetList) {

					queryBuilder.append( getTrackingPointsPerTargetQuery(target, Integer.parseInt(companyID), Integer.parseInt(mailingID),
							VAL_NUM_TABLENAMEFRAGMENT, pagetagsBuilder.toString()));
					queryBuilder.append(" UNION ");
				}

				queryBuilder.delete(queryBuilder.lastIndexOf(" UNION "),queryBuilder.lastIndexOf(" UNION ") +  " UNION ".length());
				
				String currentTargetGroup = "__DUMMY__";
				List<Map<String, Object>> result2 = select(logger, queryBuilder.toString());
				for (Map<String, Object> result2Row : result2) {
					if (!currentTargetGroup.equals(result2Row.get("targetgroup"))) {
						currentTargetGroup = (String) result2Row.get("targetgroup");
						currentcolumnIndex++;
					}
					TrackingPointStatRow row = getTrackingPointStatRow(result2Row, currentcolumnIndex);
					statList.add(row);
				}
			}	
		
		return statList;
	}


	private TrackingPointStatRow getTrackingPointStatRow(Map<String, Object> resultRow, int currentcolumnIndex) throws SQLException {
		TrackingPointStatRow row = new TrackingPointStatRow();
		row.setTrackingPoint((String) resultRow.get("SHORTNAME"));
		row.setTargetGroup((String) resultRow.get("TARGETGROUP"));
		row.setClicks_gros(((Number) resultRow.get("clicks_gros")).intValue());
		row.setClicks_net(((Number) resultRow.get("clicks_net")).intValue());
		row.setPagetag((String) resultRow.get("pagetag"));
		row.setColumn_index(currentcolumnIndex);
		return row;
	}

	public List<TrackingPointDef> getTrackingPoints(String companyID, List<String> pageTags) {
		// TODO: Company ID is not checkable. Change to "int"
		List<TrackingPointDef> trackingPoints = new ArrayList<>();
		
		if (trackingPointsEnabled(Integer.parseInt(companyID)) && !pageTags.isEmpty() ) {
            String template = getTrackingPointsDefQueryTemplate();
            String pageTagsStr = "";
            for (String pageTag : pageTags) {
                pageTagsStr += ("'" + pageTag + "'");
                pageTagsStr += ",";
            }
            pageTagsStr = pageTagsStr.substring(0, pageTagsStr.lastIndexOf(","));

            template = template.replace("<COMPANYID>", companyID);
            template = template.replace("<PAGETAGS>", pageTagsStr);

            List<Map<String, Object>> result = select(logger, template);
            for (Map<String, Object> row : result) {
                TrackingPointDef trackingPointDef = new TrackingPointDef();
                trackingPointDef.setShortname((String) row.get("shortname"));
                trackingPointDef.setPagetag((String) row.get("pagetag"));
                trackingPointDef.setType(((Number) row.get("type")).intValue());
                trackingPointDef.setCurrency((String) row.get("currency"));
                trackingPoints.add( trackingPointDef );
            }
		}
		return trackingPoints;
	}

	public String getTrackingPointsPerTargetQuery(LightTarget target, @VelocityCheck int companyID, int mailingID, String tablenameFragment, String pagetags) throws Exception {
		String template = getTrackingPointsStatPerTargetQueryTemplate();
		template = template.replace("<TARGETGROUP>", target.getName());
		template = template.replace("<COMPANYID>", Integer.toString(companyID));
		template = template.replace("<MAILINGID>", Integer.toString(mailingID));
		template = template.replace("<TARGETSQL>", target.getTargetSQL());
		template = template.replace("<TABLE_NAME_FRAGMENT>", tablenameFragment);
		template = template.replace("<PAGETAGS>", pagetags);
		return template;
	}

	private String getTrackingPointsStatPerTargetQueryTemplate() {
		String template = "SELECT '<TARGETGROUP>' targetgroup, pagetag, shortname, COUNT(DISTINCT rdirlog.customer_id) clicks_net, COUNT(rdirlog.customer_id) clicks_gros"
				+ " FROM trackpoint_def_tbl trdef"
				+ " JOIN (rdirlog_<COMPANYID>_<TABLE_NAME_FRAGMENT>_tbl rdirlog"
					+ " JOIN customer_<COMPANYID>_tbl cust ON (cust.customer_id = rdirlog.customer_id))"
					+ " ON (trdef.pagetag = rdirlog.page_tag  and rdirlog.mailing_id = <MAILINGID>)"
				+ "	WHERE pagetag IN (<PAGETAGS>) AND (<TARGETSQL>)"
				+ " GROUP BY pagetag, shortname ";
		return template;
	}
	
	public String getTrackingPointsStatTotalQuery(String companyID, String tablenameFragment, String mailingID, String pagetags,String language) {
		// TODO: Company ID is not checkable. Change to "int"
		String template = getTrackingPointsStatTotalQueryTemplate();
		template = template.replace("<COMPANYID>", companyID);
		template = template.replace("<TABLE_NAME_FRAGMENT>", tablenameFragment);
		template = template.replace("<MAILINGID>",mailingID);
		template = template.replace("<PAGETAGS>", pagetags);
		template = template.replace("<TOTAL>",I18nString.getLocaleString(ALL_SUBSCRIBERS,language));
		return template;
	}
	
	private String getTrackingPointsStatTotalQueryTemplate() {
		String template = "SELECT '<TOTAL>' TARGETGROUP, PAGETAG, SHORTNAME,  count( DISTINCT  customer_id) clicks_net, count (customer_id) clicks_gros" + 
						  "	from trackpoint_def_tbl trdef join rdirlog_<COMPANYID>_<TABLE_NAME_FRAGMENT>_tbl rdirlog on " +
						  " (trdef.pagetag = rdirlog.page_tag   and rdirlog.mailing_id=<MAILINGID> ) " + 
						  " where pagetag in (<PAGETAGS>) " +    
						  "	group by pagetag, shortname ";
		return template;
	}
	

	public List<String> getPageTags(String mailingID, String companyID) {
		// TODO: Company ID is not checkable. Change to "int"
		String template = getPageTagsQueryTemplate();
		template = template.replace("<COMPANYID>", companyID);
		template = template.replace("<MAILINGID>", mailingID);

		List<String> pageTags = new ArrayList<>();

		List<Map<String, Object>> result = select(logger, template);
		for (Map<String, Object> row : result) {
			pageTags.add((String) row.get("page_tag"));
		}
		return pageTags;
	}

	private String getPageTagsQueryTemplate() {
		return "select distinct page_tag from rdirlog_<COMPANYID>_ext_link_tbl where mailing_id=<MAILINGID> "
				+ "union "
				+ "select distinct page_tag from rdirlog_<COMPANYID>_val_alpha_tbl where mailing_id=<MAILINGID> "
				+ "union "
				+ "select distinct page_tag from rdirlog_<COMPANYID>_val_num_tbl where mailing_id=<MAILINGID>";
	}

	private String getTrackingPointsDefQueryTemplate() {
		return "select pagetag, shortname, type, currency from trackpoint_def_tbl where company_id=<COMPANYID> and pagetag in (<PAGETAGS>)";
	}

	private Collection<TrackingPointDef> extractTrackingPointsDefByType(Collection<TrackingPointDef> inputCollection, final int trackingPointType) {
		Predicate<Object> predicate = new Predicate<Object>() {
			@Override
			public boolean evaluate(Object arg0) {
				TrackingPointDef trackingPointDef = (TrackingPointDef) arg0;
				return trackingPointDef.getType() == trackingPointType ? true : false;
			}
		};
		return CollectionUtils.select(inputCollection, predicate);
	}
	
	// a really save check ..
	private boolean trackingPointsEnabled(@VelocityCheck int companyID ) {
		String query = "SELECT COUNT(table_name) exist FROM user_tables"
			+ " WHERE LOWER(table_name) IN (LOWER('rdirlog_<COMPANYID>_ext_link_tbl'), LOWER('rdirlog_<COMPANYID>_val_alpha_tbl'), LOWER('rdirlog_<COMPANYID>_val_num_tbl'))";
		query = query.replace("<COMPANYID>", Integer.toString(companyID));
		
		int numberOfTables = 0;
		List<Map<String, Object>> result = select(logger, query);
		if (result.size() > 0) {
			numberOfTables = ((Number) result.get(0).get("exist")).intValue();
		}
		return numberOfTables == 3;
	}
}
