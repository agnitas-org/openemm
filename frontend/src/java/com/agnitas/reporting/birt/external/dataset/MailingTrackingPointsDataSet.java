/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.TrackingPointDef;
import com.agnitas.reporting.birt.external.beans.TrackingPointStatRow;

public class MailingTrackingPointsDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingTrackingPointsDataSet.class);

	private static final String ALL_SUBSCRIBERS = "statistic.all_subscribers";
	public final static String EXT_LINK_TABLENAMEFRAGMENT = "ext_link";
	public final static String VAL_ALPHA_TABLENAMEFRAGMENT = "val_alpha";
	public final static String VAL_NUM_TABLENAMEFRAGMENT = "val_num";
	public final static int EXT_LINK_TYPE = 0;
	public final static int VAL_ALPHA_TYPE = 2;
	public final static int VAL_NUM_TYPE = 1;
	
	public List<TrackingPointStatRow> getNumTrackingPointsClicks(String mailingID, String companyID, String targetIDsSeparated, String language) throws SQLException {
		// TODO: Company ID is not checkable. Change to "int"
		List<TrackingPointDef> trackingPointsDefList = getTrackingPoints(companyID, getPageTags(mailingID, companyID));
		return getTrackingPointsClicks(mailingID, companyID, targetIDsSeparated, language, trackingPointsDefList,
                VAL_NUM_TYPE, VAL_NUM_TABLENAMEFRAGMENT);
	}

	public List<TrackingPointStatRow> getTrackingPointsClicks(
			String mailingID, String companyID, String targetIDsSeparated, String language,
			List<TrackingPointDef> trackingPointsDefList, int type, String tableFragment) throws SQLException {
		// TODO: Company ID is not checkable. Change to "int"

		List<TrackingPointStatRow> statList = new ArrayList<>();

		Collection<TrackingPointDef> numTrackPoints = extractTrackingPointsDefByType(trackingPointsDefList, type);
		if (numTrackPoints.size() < 1) {
			return statList;
		}

		StringBuilder pagetagsBuilder = new StringBuilder();
		for (TrackingPointDef trackingpoint : numTrackPoints) {
			pagetagsBuilder.append("'"+ trackingpoint.getPagetag() + "'");
			pagetagsBuilder.append(",");
		}
		pagetagsBuilder = pagetagsBuilder.delete(pagetagsBuilder.lastIndexOf(","), pagetagsBuilder.lastIndexOf(",") + 1);

		Connection totalQueryconnection = null;
        Statement totalStatement = null;
        ResultSet totalResultSet = null;
        if (StringUtils.isBlank(language)) {
			language = "EN";
		}
		String totalQuery = getTrackingPointsStatTotalQuery(companyID, tableFragment, type, mailingID, pagetagsBuilder.toString(), language);

		int currentcolumnIndex = 0;
		try {
			totalQueryconnection = getDataSource().getConnection();
			totalStatement = totalQueryconnection.createStatement();
			totalResultSet = totalStatement.executeQuery( totalQuery );
			while (totalResultSet.next()) {
                TrackingPointStatRow row = null;
                if (type == VAL_NUM_TYPE) {
				    row = getTrackingPointStatRowNumeric(totalResultSet,currentcolumnIndex);
                } else if (type == VAL_ALPHA_TYPE) {
				    row = getTrackingPointStatRowAlpha(totalResultSet,currentcolumnIndex);
                } else {
				    row = getTrackingPointStatRowSimple(totalResultSet,currentcolumnIndex);
                }
				statList.add(row);
			}
		} catch (SQLException e) {
			logger.error("SQL Execption while trying to get total stat for tracking points of type num ! ",e);
		} finally {
            DbUtilities.closeQuietly(totalQueryconnection, "could not close total query connection !");
            DbUtilities.closeQuietly(totalStatement, "Could not close DB-statement !");
            DbUtilities.closeQuietly(totalResultSet, "Could not close result set !");
		}

		StringBuilder queryBuilder = new StringBuilder();
		if (targetIDsSeparated != null && !"".equals(targetIDsSeparated.trim())) {
			List<String> targetIDs = new ArrayList<>();
			if (StringUtils.isNotBlank(targetIDsSeparated)) {
			StringTokenizer tokenizer = new StringTokenizer(targetIDsSeparated, ",");
				while (tokenizer.hasMoreTokens()) {
					targetIDs.add(tokenizer.nextToken());
				}
			}

			List<LightTarget> targetList = null;
			Connection targetQueryConnection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
				targetList = getTargets(targetIDs, Integer.parseInt(companyID));
                Map<String, Integer> targetIndexes = new HashMap<>();
                int targetColumnIndex = 1;
				for (LightTarget target : targetList) {
					queryBuilder.append(getTrackingPointsPerTargetQuery(target, Integer.parseInt(companyID),
                            Integer.parseInt(mailingID), tableFragment, type, pagetagsBuilder.toString())) ;
					queryBuilder.append(" UNION ");
                    targetIndexes.put(target.getName(), targetColumnIndex);
                    targetColumnIndex++;
				}

				queryBuilder.delete(queryBuilder.lastIndexOf(" UNION "),queryBuilder.lastIndexOf(" UNION ") +  " UNION ".length());

				targetQueryConnection = getDataSource().getConnection();
				statement = targetQueryConnection.createStatement();
				resultSet = statement.executeQuery(queryBuilder.toString());
				String currentTargetGroup = "__DUMMY__";

				while (resultSet.next()) {
					if (!currentTargetGroup.equals(resultSet.getString("targetgroup"))) {
						currentTargetGroup = resultSet.getString("targetgroup");
						currentcolumnIndex = targetIndexes.get(currentTargetGroup);
					}
                    TrackingPointStatRow row = null;
                    if (type == VAL_NUM_TYPE) {
                        row = getTrackingPointStatRowNumeric(resultSet,currentcolumnIndex);
                    } else if (type == VAL_ALPHA_TYPE) {
                        row = getTrackingPointStatRowAlpha(resultSet,currentcolumnIndex);
                    } else {
                        row = getTrackingPointStatRowSimple(resultSet,currentcolumnIndex);
                    }
                    statList.add(row);
				}
                List<TrackingPointStatRow> rowsToAdd = new ArrayList<>();
                for (TrackingPointDef trackPoint : numTrackPoints) {
                    String allSubsTarget = I18nString.getLocaleString(ALL_SUBSCRIBERS, language);
                    Object allSubsRow = findStatRow(trackPoint, allSubsTarget, statList);
                    if (allSubsRow != null) {
                        for (LightTarget target : targetList) {
                            if (findStatRow(trackPoint, target.getName(), statList) == null) {
                                TrackingPointStatRow row = new TrackingPointStatRow();
                                row.setTrackingPoint(trackPoint.getShortname());
                                row.setTargetGroup(target.getName());
                                row.setClicks_gros(0);
                                row.setClicks_net(0);
                                row.setPagetag(trackPoint.getPagetag());
                                row.setColumn_index(targetIndexes.get(target.getName()));
                                rowsToAdd.add(row);
                            }
                        }
                    }
                }
                statList.addAll(rowsToAdd);
			} catch (NumberFormatException e) {
				logger.error("Check your parameters ! mailingID =" +mailingID +" , companyID=" +companyID , e);
			} finally {
                DbUtilities.closeQuietly(targetQueryConnection, "could not close targetQueryConnection ");
                DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
                DbUtilities.closeQuietly(resultSet, "Could not close result set !");
			}

		}
		return statList;
	}

    private Object findStatRow(TrackingPointDef trackPoint, String targetName, List<TrackingPointStatRow> statList) {
        for (TrackingPointStatRow statRow : statList) {
            if (trackPoint.getPagetag().equals(statRow.getPagetag()) && targetName.equals(statRow.getTargetGroup())) {
                return statRow;
            }
        }
        return null;
    }

    public List<TrackingPointStatRow> getAlphaTrackingPointsClicks(String mailingID, String companyID, String targetIDsSeparated , String language) throws SQLException {		
    	// TODO: Company ID is not checkable. Change to "int"
		List<TrackingPointDef> trackingPointsDefList = getTrackingPoints(companyID, getPageTags(mailingID, companyID));
		return getTrackingPointsClicks(mailingID, companyID, targetIDsSeparated, language, trackingPointsDefList,
                VAL_ALPHA_TYPE, VAL_ALPHA_TABLENAMEFRAGMENT);
	}
	
	public List<TrackingPointStatRow> getSimpleTrackingPointsClicks(String mailingID, String companyID, String targetIDsSeparated , String language) throws SQLException {
    	// TODO: Company ID is not checkable. Change to "int"
		List<TrackingPointDef> trackingPointsDefList = getTrackingPoints(companyID, getPageTags(mailingID, companyID));
		return getTrackingPointsClicks(mailingID, companyID, targetIDsSeparated, language, trackingPointsDefList,
		        EXT_LINK_TYPE, EXT_LINK_TABLENAMEFRAGMENT);
	}

	private TrackingPointStatRow getTrackingPointStatRowNumeric(ResultSet resultSet, int currentcolumnIndex) throws SQLException {
		TrackingPointStatRow row = new TrackingPointStatRow();
		row.setTrackingPoint(resultSet.getString("SHORTNAME"));
		row.setTargetGroup(resultSet.getString("TARGETGROUP"));
		row.setClicks_gros(resultSet.getInt("clicks_gros"));
		row.setClicks_net(resultSet.getInt("clicks_net"));
		row.setPagetag(resultSet.getString("pagetag"));
		row.setColumn_index(currentcolumnIndex);
		row.setNum_value(resultSet.getBigDecimal("num_value").doubleValue());
		row.setCurrency(resultSet.getString("currency"));
		return row;
	}
	
	private TrackingPointStatRow getTrackingPointStatRowAlpha(ResultSet resultSet, int currentcolumnIndex) throws SQLException {
		TrackingPointStatRow row = new TrackingPointStatRow();
		row.setTrackingPoint(resultSet.getString("SHORTNAME"));
		row.setTargetGroup(resultSet.getString("TARGETGROUP"));
		row.setClicks_gros(resultSet.getInt("clicks_gros"));
		row.setClicks_net(resultSet.getInt("clicks_net"));
		row.setPagetag(resultSet.getString("pagetag"));
		row.setAlphaParameter(resultSet.getString("ALPHA_PARAMETER"));
		row.setColumn_index(currentcolumnIndex);
		return row;
	}

    private TrackingPointStatRow getTrackingPointStatRowSimple(ResultSet resultSet, int currentcolumnIndex) throws SQLException {
		TrackingPointStatRow row = new TrackingPointStatRow();
		row.setTrackingPoint(resultSet.getString("SHORTNAME"));
		row.setTargetGroup(resultSet.getString("TARGETGROUP"));
		row.setClicks_gros(resultSet.getInt("clicks_gros"));
		row.setClicks_net(resultSet.getInt("clicks_net"));
        row.setPagetag(resultSet.getString("pagetag"));
		row.setColumn_index(currentcolumnIndex);
		return row;
	}

	public List<TrackingPointDef> getTrackingPoints(String companyID,
			List<String> pageTags) {
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

			Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
				connection = getDataSource().getConnection();
				statement = connection.createStatement();
				resultSet = statement.executeQuery(template);
				while (resultSet.next()) {
					TrackingPointDef trackingPointDef = new TrackingPointDef();
					trackingPointDef.setShortname(resultSet.getString("shortname"));
					trackingPointDef.setPagetag(resultSet.getString("pagetag"));
					trackingPointDef.setType(resultSet.getInt("type"));
					trackingPointDef.setCurrency(resultSet.getString("currency"));
					trackingPoints.add( trackingPointDef );
				}
			} catch (SQLException e) {
				logger.error("Error while trying to get tracking point definitions :" +template, e);
			} finally {
                DbUtilities.closeQuietly(connection, "Couldn't close SQL-connection!");
                DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
                DbUtilities.closeQuietly(resultSet, "Could not close result set !");
			}
		}
		return trackingPoints;
	}

	public String getTrackingPointsPerTargetQuery(LightTarget target, @VelocityCheck int companyID, int mailingID, String tablenameFragment, int trackingPointType, String pagetags) {
		String template = getTrackingPointsStatPerTargetQueryTemplate(trackingPointType);
		template = template.replace("<TARGETGROUP>", target.getName());
		template = template.replace("<COMPANYID>", Integer.toString(companyID));
		template = template.replace("<MAILINGID>", Integer.toString(mailingID));
		template = template.replace("<TARGETSQL>", target.getTargetSQL());
		template = template.replace("<TABLE_NAME_FRAGMENT>", tablenameFragment);
		template = template.replace("<PAGETAGS>", pagetags);
		return template;
	}

	private String getTrackingPointsStatPerTargetQueryTemplate(int trackingPointType) {
		String template = null; 
		if (trackingPointType == VAL_NUM_TYPE) {
			template = "SELECT '<TARGETGROUP>' as TARGETGROUP, PAGETAG, SHORTNAME, count(DISTINCT rdirlog.customer_id) as clicks_net, count(rdirlog.customer_id) as clicks_gros, sum(num_parameter) as num_value, currency "
				+ " from trackpoint_def_tbl trdef join (rdirlog_<COMPANYID>_<TABLE_NAME_FRAGMENT>_tbl rdirlog "
				+ " join customer_<COMPANYID>_tbl cust on (cust.customer_id= rdirlog.customer_id)) on (trdef.pagetag = rdirlog.page_tag and rdirlog.mailing_id=<MAILINGID> ) "
				+ "	where pagetag in (<PAGETAGS>) and (<TARGETSQL>) "
				+ " group by pagetag, shortname , currency";
		}
		
		if (trackingPointType == VAL_ALPHA_TYPE) {
			template = "SELECT '<TARGETGROUP>' as TARGETGROUP, PAGETAG, SHORTNAME, ALPHA_PARAMETER, count(DISTINCT rdirlog.customer_id) as clicks_net, count(rdirlog.customer_id) as clicks_gros "
				+ " from trackpoint_def_tbl trdef join (rdirlog_<COMPANYID>_<TABLE_NAME_FRAGMENT>_tbl rdirlog "
				+ " join customer_<COMPANYID>_tbl cust on (cust.customer_id= rdirlog.customer_id)) on (trdef.pagetag = rdirlog.page_tag and rdirlog.mailing_id=<MAILINGID> ) "
				+ "	where pagetag in (<PAGETAGS>) and (<TARGETSQL>) "
				+ " group by pagetag, shortname , ALPHA_PARAMETER ";
		}
		
		if (trackingPointType == EXT_LINK_TYPE) {
			template = "SELECT '<TARGETGROUP>' as TARGETGROUP, PAGETAG, SHORTNAME, count(DISTINCT  rdirlog.customer_id) as clicks_net, count (rdirlog.customer_id) as clicks_gros "
				+ " from trackpoint_def_tbl trdef join (rdirlog_<COMPANYID>_<TABLE_NAME_FRAGMENT>_tbl rdirlog "
				+ " join customer_<COMPANYID>_tbl cust on (cust.customer_id= rdirlog.customer_id)) on (trdef.pagetag = rdirlog.page_tag and rdirlog.mailing_id=<MAILINGID> ) "
				+ "	where pagetag in (<PAGETAGS>) and (<TARGETSQL>) "
				+ " group by pagetag, shortname ";
		}
		return template;
	}
	
	public String getTrackingPointsStatTotalQuery(String companyID, String tablenameFragment, int trackingPointType, String mailingID, String pagetags, String language) {
		// TODO: Company ID is not checkable. Change to "int"
		String template = getTrackingPointsStatTotalQueryTemplate(trackingPointType);
		template = template.replace("<COMPANYID>", companyID);
		template = template.replace("<TABLE_NAME_FRAGMENT>", tablenameFragment);
		template = template.replace("<MAILINGID>",mailingID);
		template = template.replace("<PAGETAGS>", pagetags);
		template = template.replace("<TOTAL>",I18nString.getLocaleString(ALL_SUBSCRIBERS,language));
		return template;
	}
	
	private String getTrackingPointsStatTotalQueryTemplate(int trackingPointType) {
		String template = null;
		
		if (trackingPointType == VAL_ALPHA_TYPE) {
			template = "SELECT '<TOTAL>' as TARGETGROUP, PAGETAG, SHORTNAME, ALPHA_PARAMETER, count(DISTINCT  customer_id) as clicks_net, count(customer_id) as clicks_gros "  + 
			" from trackpoint_def_tbl trdef join rdirlog_<COMPANYID>_<TABLE_NAME_FRAGMENT>_tbl rdirlog on " +
			" (trdef.pagetag = rdirlog.page_tag and rdirlog.mailing_id=<MAILINGID> ) " +
			" where pagetag in (<PAGETAGS>) " +    
			"	group by pagetag, shortname , ALPHA_PARAMETER";
			
		}
		
		if (trackingPointType == VAL_NUM_TYPE) {
			template = "SELECT '<TOTAL>' as TARGETGROUP, PAGETAG, SHORTNAME, count(DISTINCT  customer_id) as clicks_net, count(customer_id) as clicks_gros, sum(num_parameter) as num_value , currency "  +
			" from trackpoint_def_tbl trdef join rdirlog_<COMPANYID>_<TABLE_NAME_FRAGMENT>_tbl rdirlog on " +
			" (trdef.pagetag = rdirlog.page_tag and rdirlog.mailing_id=<MAILINGID> ) " +
			" where pagetag in (<PAGETAGS>) " +    
			"	group by pagetag, shortname, currency ";
		}

        if (trackingPointType == EXT_LINK_TYPE) {
			template = "SELECT '<TOTAL>' as TARGETGROUP, PAGETAG, SHORTNAME, count(DISTINCT  customer_id) as clicks_net, count(customer_id) as clicks_gros  "  +
			"	from trackpoint_def_tbl trdef join rdirlog_<COMPANYID>_<TABLE_NAME_FRAGMENT>_tbl rdirlog on " +
			"   (trdef.pagetag = rdirlog.page_tag and rdirlog.mailing_id=<MAILINGID>) " +
			"   where pagetag in (<PAGETAGS>) " +
			"	group by pagetag, shortname ";
		}
		return template;
	}
	
	public List<String> getPageTags(String mailingID, String companyID) {
		// TODO: Company ID is not checkable. Change to "int"

		String template = getPageTagsQueryTemplate();
		template = template.replace("<COMPANYID>", companyID);
		template = template.replace("<MAILINGID>", mailingID);

		List<String> pageTags = new ArrayList<>();

		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
			connection = getDataSource().getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(template);

			while (resultSet.next()) {
				pageTags.add(resultSet.getString("page_tag"));
			}
		} catch (SQLException e) {
			logger.error("SQL-Execption while trying to get page-tags for mailingID="+ mailingID + " companyID=" + companyID, e);
		} finally {
            DbUtilities.closeQuietly(connection, "Could not close SQL-Connection");
            DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
            DbUtilities.closeQuietly(resultSet, "Could not close result set !");
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
		Predicate<TrackingPointDef> predicate = new Predicate<TrackingPointDef>() {
			@Override
			public boolean evaluate(TrackingPointDef trackingPointDef) {
				return trackingPointDef.getType() == trackingPointType ? true : false;
			}
		};
		return CollectionUtils.select(inputCollection, predicate);
	}
	
	// a really save check ..
	private boolean trackingPointsEnabled(@VelocityCheck int companyID ) {
        String query = "";
        int numberOfTables = 0;
        if (isOracleDB()){
            query = "select count(table_name) as exist from user_tables where lower(table_name) in "
                     +" (lower('rdirlog_<COMPANYID>_ext_link_tbl') , lower('rdirlog_<COMPANYID>_val_alpha_tbl'), lower('rdirlog_<COMPANYID>_val_num_tbl') )";
            query = query.replace("<COMPANYID>", Integer.toString(companyID));
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                 connection = getDataSource().getConnection();
                 statement = connection.createStatement();
                 resultSet = statement.executeQuery(query);
                 if( resultSet.next()) {
                     numberOfTables = resultSet.getInt("exist");
                 }
            } catch (SQLException e) {
                logger.error("Could not execute statement: " + query , e);
            } finally {
                DbUtilities.closeQuietly(connection, "Could not close DB connection ");
                DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
                DbUtilities.closeQuietly(resultSet, "Could not close result set !");
            }
        } else {
            String dbname_query = "SELECT SCHEMA() dbname";
            String dbname = "";
            query = "show tables where tables_in_<DBNAME> in ('rdirlog_<COMPANYID>_ext_link_tbl', 'rdirlog_<COMPANYID>_val_alpha_tbl', 'rdirlog_<COMPANYID>_val_num_tbl')";
            query = query.replace("<COMPANYID>", Integer.toString(companyID));
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                 connection = getDataSource().getConnection();
                 statement = connection.createStatement();
                 resultSet = statement.executeQuery(dbname_query);
                 if (resultSet.next()) {
                     dbname = resultSet.getString("dbname");
                 }
                 resultSet.close();
                 
                 query = query.replace("<DBNAME>", dbname);
                 resultSet = statement.executeQuery(query);
                 if (resultSet.next()) {
                     resultSet.last();
                     numberOfTables = resultSet.getRow();
                 }
            } catch (SQLException e) {
                logger.error("Could not execute statement: " + query , e);
            } finally {
                DbUtilities.closeQuietly(resultSet, "Could not close result set !");
                DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
                DbUtilities.closeQuietly(connection, "Could not close DB connection ");
            }
        }
		return numberOfTables == 3;
	}
}
