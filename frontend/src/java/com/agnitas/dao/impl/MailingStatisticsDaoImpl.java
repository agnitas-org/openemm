/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.dao.FollowUpType;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.ComTargetService;

public class MailingStatisticsDaoImpl extends BaseDaoImpl implements MailingStatisticsDao {
    /** The logger. */
    private static final transient Logger logger = LogManager.getLogger(MailingStatisticsDaoImpl.class);

	private final ConfigService configService;
    private final ComTargetService targetService;
    
    @Deprecated(forRemoval = true)	// Use TargetService instead
    private final ComTargetDao targetDao;
    
    public MailingStatisticsDaoImpl(final ConfigService configService, final ComTargetService targetService, @Deprecated final ComTargetDao targetDao) {
    	this.configService = Objects.requireNonNull(configService);
    	this.targetService = Objects.requireNonNull(targetService);
    	this.targetDao = Objects.requireNonNull(targetDao);
    }
	
    /**
     * This method returns the Follow-up statistics for the given mailing.
     *
     * @throws Exception
     */
	@Override
    public int getFollowUpStat(final int mailingID, final int baseMailing, final String followUpType, @VelocityCheck final int companyID, final boolean useTargetGroups) throws Exception {
        int resultValue = 0;

        // What kind of followup do we have, choose the appropriate sql call...
        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_CLICKER.getKey())) {

            // TODO NEW click calculation with target groups.
            // the maildrop-id must be the one of the 'base' mailing, which is
            // already sent. The given
            // mailing-id must be the one of the current (that means the
            // follow-up) mailing.
            resultValue = getClicker(companyID, baseMailing, mailingID);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_OPENER.getKey())) {
            resultValue = getOpener(companyID, baseMailing, mailingID);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_NON_CLICKER.getKey())) {
            resultValue = getNonClicker(companyID, baseMailing, mailingID);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey())) {
            resultValue = getNonOpener(companyID, baseMailing, mailingID);
        }

        return resultValue;
    }

	@Override
    public int getFollowUpStat(final int followUpFor, final String followUpType, final int companyID, final String sqlTargetExpression) throws Exception {

        int resultValue = 0;
        // What kind of followup do we have, choose the appropriate sql call...
        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_CLICKER.getKey())) {
            resultValue = getClicker(companyID, followUpFor, sqlTargetExpression);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_OPENER.getKey())) {
            resultValue = getOpener(companyID, followUpFor, sqlTargetExpression);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_NON_CLICKER.getKey())) {
            resultValue = getNonClicker(companyID, followUpFor, sqlTargetExpression);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey())) {
            resultValue = getNonOpener(companyID, followUpFor, sqlTargetExpression);
        }

        return resultValue;
    }

    private int getClicker(final int companyID, final int followUpFor, final String sqlTargetExpression) {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
        sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
        sql.append(" AND EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)");

        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            sql.append(" AND (" + sqlTargetExpression + ")");
        }

        return selectInt(logger, sql.toString(), followUpFor);
    }

    private int getNonClicker(@VelocityCheck final int companyID, final int followUpFor, final String sqlTargetExpression) throws Exception {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
        sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
        sql.append(" AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)");
        sql.append(" AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)");

        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            sql.append(" AND (" + sqlTargetExpression + ")");
        }

        return selectInt(logger, sql.toString(), followUpFor, followUpFor);
    }

    private int getOpener(@VelocityCheck final int companyID, final int followUpFor, final String sqlTargetExpression) throws Exception {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
        sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
        sql.append(" AND EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)");

        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            sql.append(" AND (" + sqlTargetExpression + ")");
        }

        return selectInt(logger, sql.toString(), followUpFor);
    }

    private int getNonOpener(@VelocityCheck final int companyID, final int followUpFor, final String sqlTargetExpression) throws Exception {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
        sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
        sql.append(" AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)");
        sql.append(" AND NOT EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)");

        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            sql.append(" AND (" + sqlTargetExpression + ")");
        }

        return selectInt(logger, sql.toString(), followUpFor, followUpFor);
    }

    private int getClicker(@VelocityCheck final int companyID, final int baseMailingID, final int followUpMailingID) throws Exception {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
        sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
        sql.append(" AND EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)");

        // Add targetgroup sql for followup mailing
        final String targetString = getTargetIDString(followUpMailingID);
        final int[] targetIDs = getTargetExpressionIds(targetString);
        if (targetIDs.length != 0) {
            final String targetSql = createTargetStatementWithoutAnd(targetString);
            if (StringUtils.isNotBlank(targetSql)) {
                sql.append(" AND (" + targetSql + ")");
            }
        }

        // Add listsplit sql for followup mailing
        final int listSplitId = getListSplitId(followUpMailingID);
        if (listSplitId != 0) {
            final String listSplitSql = createTargetStatementWithoutAnd(Integer.toString(listSplitId));
            sql.append(" AND (" + listSplitSql + ")");
        }

        return selectInt(logger, sql.toString(), baseMailingID);
    }

    /**
     * returns the amount of non-clickers. maildrop-id is the id of the BASE!
     * Mailing, not the actual followup!
     *
     * @param companyID
     * @param baseMailingID
     * @param followUpMailingID
     * @return
     * @throws Exception
     */
    private int getNonClicker(@VelocityCheck final int companyID, final int baseMailingID, final int followUpMailingID) throws Exception {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
        sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
        sql.append(" AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)");
        sql.append(" AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)");

        // Add targetgroup sql for followup mailing
        final String targetString = getTargetIDString(followUpMailingID);
        final int[] targetIDs = getTargetExpressionIds(targetString);
        if (targetIDs.length != 0) {
            final String targetSql = createTargetStatementWithoutAnd(targetString);
            if (StringUtils.isNotBlank(targetSql)) {
                sql.append(" AND (" + targetSql + ")");
            }
        }

        // Add listsplit sql for followup mailing
        final int listSplitId = getListSplitId(followUpMailingID);
        if (listSplitId != 0) {
            final String listSplitSql = createTargetStatementWithoutAnd(Integer.toString(listSplitId));
            sql.append(" AND (" + listSplitSql + ")");
        }

        return selectInt(logger, sql.toString(), baseMailingID, baseMailingID);
    }

    /**
     * returns the amount of openers for the given mailingID, companyID and
     * targetID.
     *
     * @return
     * @throws Exception
     */
    private int getOpener(@VelocityCheck final int companyID, final int baseMailingID, final int followUpMailingID) throws Exception {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
        sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
        sql.append(" AND EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)");

        // Add targetgroup sql for followup mailing
        final String targetString = getTargetIDString(followUpMailingID);
        final int[] targetIDs = getTargetExpressionIds(targetString);
        if (targetIDs.length != 0) {
            final String targetSql = createTargetStatementWithoutAnd(targetString);
            if (StringUtils.isNotBlank(targetSql)) {
                sql.append(" AND (" + targetSql + ")");
            }
        }

        // Add listsplit sql for followup mailing
        final int listSplitId = getListSplitId(followUpMailingID);
        if (listSplitId != 0) {
            final String listSplitSql = createTargetStatementWithoutAnd(Integer.toString(listSplitId));
            sql.append(" AND (" + listSplitSql + ")");
        }

        return selectInt(logger, sql.toString(), baseMailingID);
    }

    /**
     * returns the amount of openers for the given mailingID, companyID and
     * targetID.
     *
     * @return
     * @throws Exception
     */
    private int getNonOpener(@VelocityCheck final int companyID, final int baseMailingID, final int followUpMailingID) throws Exception {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
        sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
        sql.append(" AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)");
        sql.append(" AND NOT EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)");

        // Add targetgroup sql for followup mailing
        final String targetString = getTargetIDString(followUpMailingID);
        final int[] targetIDs = getTargetExpressionIds(targetString);
        if (targetIDs.length != 0) {
            final String targetSql = createTargetStatementWithoutAnd(targetString);
            if (StringUtils.isNotBlank(targetSql)) {
                sql.append(" AND (" + targetSql + ")");
            }
        }

        // Add listsplit sql for followup mailing
        final int listSplitId = getListSplitId(followUpMailingID);
        if (listSplitId != 0) {
            final String listSplitSql = createTargetStatementWithoutAnd(Integer.toString(listSplitId));
            sql.append(" AND (" + listSplitSql + ")");
        }

        return selectInt(logger, sql.toString(), baseMailingID, baseMailingID);
    }

    private String createTargetStatementWithoutAnd(final String targetString) throws Exception {
        final int[] targetIDs = getTargetExpressionIds(targetString);
        final String operator = getTargetOperator(targetString);

        final StringBuilder targetSql = new StringBuilder();
        for (final int targetID : targetIDs) {
            if (targetSql.length() > 0) {
                targetSql.append(" ").append(operator).append(" ");
            }

            targetSql.append(getTargetExpressionForId(targetID));
        }

        return targetSql.toString();
    }

    /**
     * This method returns an Array with all Target_Expression IDs from the
     * mailing_tbl.
     */
    private int[] getTargetExpressionIds(final String targetExpression) {
        final Set<Integer> targetIds = TargetExpressionUtils.getTargetIds(targetExpression);
        final int[] targetsArray = ArrayUtils.toPrimitive(targetIds.toArray(new Integer[0]));
        return targetsArray;
    }

    /**
     * This method returns the Operator for the target String. If the
     * targetString contains "|" it returns "OR" and if it contains "&" it
     * returns "AND". WARNING! Mixing up targets will result in an exception!
     *
     * @param targetString
     * @return
     * @throws Exception
     */
    private String getTargetOperator(final String targetString) throws Exception {
        if (targetString.contains("|") && targetString.contains("&")) {
            // Mix-Check. We don't support mixed target-group at this time (2011.02.01)
            throw new Exception("Unsupported Mixed Target Groups: " + targetString);
        } else if (targetString.contains("|")) {
            return "OR";
        } else {
            return "AND"; // this is the less dangerous version (=less recipients).
        }
    }

    /**
     * This method returns the string with all target IDs.
     *
     * @param followUpMailingID
     * @return
     */
    private String getTargetIDString(final int followUpMailingID) {
        try {
            return select(logger, "SELECT target_expression FROM mailing_tbl WHERE mailing_id = ?", String.class, followUpMailingID);
        } catch (final Exception e) {
            logger.error("Database Error getting target-id Strings in ComMailingDaoImpl", e);
            return null;
        }
    }

    /**
     * Retrieve list split ID for mailing ID.
     *
     * @param mailingId mailing ID
     * @return list split ID or 0
     */
    private int getListSplitId(final int mailingId) {
        try {
            return selectInt(logger, "SELECT split_id FROM mailing_tbl WHERE mailing_id = ?", mailingId);
        } catch (final Exception e) {
            logger.warn("Error reading list split ID for mailing " + mailingId, e);
            return 0;
        }
    }
    
    private String getTargetExpressionForId(final int expressionID) {
        try {
            return select(logger, "SELECT target_sql FROM dyn_target_tbl WHERE target_id = ?", String.class, expressionID);
        } catch (final Exception e) {
            logger.error("Error getting target-sql from DB.", e);
            return "";
        }
    }

	@Override
    public Map<Integer, Integer> getSendStats(final Mailing mailing, final int companyId) throws Exception {
        final Map<Integer, Integer> map = new HashMap<>();
        int numText = 0;
        int numHtml = 0;
        int numOffline = 0;

        final MediatypeEmail mediatype = mailing.getEmailParam();

        if (StringUtils.isBlank(mediatype.getFollowupFor())) {
        	final String targetAndSplitSql = targetService.getSQLFromTargetExpression(mailing.getTargetExpression(), mailing.getSplitID(), companyId);

            String sqlStatement = "SELECT count(*) as count, bind.mediatype, cust.mailtype FROM customer_" + companyId + "_tbl cust, " + "customer_" + companyId
                    + "_binding_tbl bind WHERE bind.mailinglist_id = ? AND cust.customer_id = bind.customer_id " + (StringUtils.isNotBlank(targetAndSplitSql) ? " AND (" + targetAndSplitSql + ")" : "")
                    + " AND bind.user_status = ?";

            if (mailing.isEncryptedSend()) {
                sqlStatement += " AND cust.sys_encrypted_sending = 1 ";
            }

            sqlStatement += " GROUP BY bind.mediatype, cust.mailtype";


            if (logger.isInfoEnabled()) {
                logger.info("sql: " + sqlStatement);
            }

            final boolean useUnsharpRecipientQuery = configService.useUnsharpRecipientQuery(companyId);

            try (Connection connection = getDataSource().getConnection()) {
                try {
                    // TODO: IGNORE_BOUNCELOAD_COMPANY_ID is a bad hack for CONRAD-371!!!
                    if (useUnsharpRecipientQuery) {
                        try (Statement stmt = connection.createStatement()) {
                            stmt.execute("ALTER SESSION SET OPTIMIZER_MODE=RULE");
                        }
                    }

                    final SingleConnectionDataSource scds = new SingleConnectionDataSource(connection, true);
                    final JdbcTemplate template = new JdbcTemplate(scds);

                    final List<Map<String, Object>> result = template.queryForList(sqlStatement, mailing.getMailinglistID(), UserStatus.Active.getStatusCode());
                    for (final Map<String, Object> resultRow : result) {
                        MediaTypes customerMediaType = MediaTypes.getMediaTypeForCode(((Number) resultRow.get("mediatype")).intValue());
                        if (mailing.getMediatypes().containsKey(customerMediaType.getMediaCode())
                                && mailing.getMediatypes().get(customerMediaType.getMediaCode()).getStatus() == MediaTypeStatus.Active.getCode()) {
                            if (customerMediaType == MediaTypes.EMAIL) {
                                final MailType mailType = MailType.getFromInt(((Number) resultRow.get("mailtype")).intValue());
                                switch (mailType) {
                                    case TEXT:
                                        numText += ((Number) resultRow.get("count")).intValue();
                                        break;

                                    case HTML:
                                        if (mailing.getEmailParam().getMailFormat() == 0) {
                                            // nur Text-Mailing
                                            numText += ((Number) resultRow.get("count")).intValue();
                                        } else {
                                            numHtml += ((Number) resultRow.get("count")).intValue();
                                        }
                                        break;

                                    case HTML_OFFLINE:
                                        if (mailing.getEmailParam().getMailFormat() == 0) {
                                            // nur Text-Mailing
                                            numText += ((Number) resultRow.get("count")).intValue();
                                        }
                                        if (mailing.getEmailParam().getMailFormat() == 1) {
                                            // nur Text/Online-HTML-Mailing
                                            numHtml += ((Number) resultRow.get("count")).intValue();
                                        }
                                        if (mailing.getEmailParam().getMailFormat() == 2) {
                                            // alle Formate
                                            numOffline += ((Number) resultRow.get("count")).intValue();
                                        }
                                        break;
                                    default:
                                        throw new Exception("Invalid MailType");
                                }
                            } else {
                                if (map.get(customerMediaType.getMediaCode()) != null) {
                                    // add (do not overwrite) new value for same mediatype
                                    int number = map.get(customerMediaType.getMediaCode());
                                    number += ((Number) resultRow.get("count")).intValue();
                                    map.put(customerMediaType.getMediaCode(), number);
                                } else {
                                    map.put(customerMediaType.getMediaCode(), ((Number) resultRow.get("count")).intValue());
                                }
                            }
                        }
                    }
                } finally {
                    // TODO: IGNORE_BOUNCELOAD_COMPANY_ID is a bad hack for CONRAD-371!!!
                    if (useUnsharpRecipientQuery) {
                        try (Statement stmt = connection.createStatement()) {
                            stmt.execute("ALTER SESSION SET OPTIMIZER_MODE=ALL_ROWS");
                        }
                    }

                }
            } catch (final Exception e) {
                logger.error("Error reading send status of mailing " + mailing.getId(), e);

                if (logger.isInfoEnabled()) {
                    logger.error("SQL statement is: " + sqlStatement);
                }

                return null;
            }
        } else {
            numText = getFollowUpStat(mailing.getId(), Integer.parseInt(mediatype.getFollowupFor()), mediatype.getFollowUpMethod(), companyId, true);
        }

        map.put(SEND_STATS_TEXT, numText);
        map.put(SEND_STATS_HTML, numHtml);
        map.put(SEND_STATS_OFFLINE, numOffline);

        return map;
    }
}
