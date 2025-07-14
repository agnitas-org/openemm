/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediaTypeStatus;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.emm.common.FollowUpType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class MailingStatisticsDaoImpl extends BaseDaoImpl implements MailingStatisticsDao {

    private final TargetService targetService;
    
    public MailingStatisticsDaoImpl(TargetService targetService) {
    	this.targetService = Objects.requireNonNull(targetService);
    }
	
    /**
     * This method returns the Follow-up statistics for the given mailing.
     */
	@Override
    public int getFollowUpStat(int mailingID, int baseMailing, String followUpType, int companyID, boolean useTargetGroups) {
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
    public int getFollowUpStat(int followUpFor, String followUpType, int companyID, String sqlTargetExpression) {
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

    private int getClicker(int companyID, int followUpFor, String sqlTargetExpression) {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
        sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
        sql.append(" AND EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)");

        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            sql.append(" AND (" + sqlTargetExpression + ")");
        }

        return selectInt(sql.toString(), followUpFor);
    }

    private int getNonClicker(int companyID, int followUpFor, String sqlTargetExpression) {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
        sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
        sql.append(" AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)");
        sql.append(" AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)");

        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            sql.append(" AND (" + sqlTargetExpression + ")");
        }

        return selectInt(sql.toString(), followUpFor, followUpFor);
    }

    private int getOpener(int companyID, int followUpFor, String sqlTargetExpression) {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
        sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
        sql.append(" AND EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)");

        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            sql.append(" AND (" + sqlTargetExpression + ")");
        }

        return selectInt(sql.toString(), followUpFor);
    }

    private int getNonOpener(int companyID, int followUpFor, String sqlTargetExpression) {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
        sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
        sql.append(" AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)");
        sql.append(" AND NOT EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)");

        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            sql.append(" AND (" + sqlTargetExpression + ")");
        }

        return selectInt(sql.toString(), followUpFor, followUpFor);
    }

    private int getClicker(int companyID, int baseMailingID, int followUpMailingID) {
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

        return selectInt(sql.toString(), baseMailingID);
    }

    /**
     * returns the amount of non-clickers. maildrop-id is the id of the BASE!
     * Mailing, not the actual followup!
     */
    private int getNonClicker(final int companyID, final int baseMailingID, final int followUpMailingID) {
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

        return selectInt(sql.toString(), baseMailingID, baseMailingID);
    }

    /**
     * returns the amount of openers for the given mailingID, companyID and
     * targetID.
     */
    private int getOpener(int companyID, int baseMailingID, int followUpMailingID) {
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

        return selectInt(sql.toString(), baseMailingID);
    }

    /**
     * returns the amount of openers for the given mailingID, companyID and
     * targetID.
     */
    private int getNonOpener(int companyID, int baseMailingID, int followUpMailingID) {
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

        return selectInt(sql.toString(), baseMailingID, baseMailingID);
    }

    private String createTargetStatementWithoutAnd(String targetString) {
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
    private int[] getTargetExpressionIds(String targetExpression) {
        final Set<Integer> targetIds = TargetExpressionUtils.getTargetIds(targetExpression);
        return ArrayUtils.toPrimitive(targetIds.toArray(new Integer[0]));
    }

    /**
     * This method returns the Operator for the target String. If the
     * targetString contains "|" it returns "OR" and if it contains "&" it
     * returns "AND". WARNING! Mixing up targets will result in an exception!
     */
    private String getTargetOperator(String targetString) {
        if (targetString.contains("|") && targetString.contains("&")) {
            // Mix-Check. We don't support mixed target-group at this time (2011.02.01)
            throw new IllegalArgumentException("Unsupported Mixed Target Groups: " + targetString);
        } else if (targetString.contains("|")) {
            return "OR";
        } else {
            return "AND"; // this is the less dangerous version (=less recipients).
        }
    }

    /**
     * This method returns the string with all target IDs.
     */
    private String getTargetIDString(int followUpMailingID) {
        try {
            return select("SELECT target_expression FROM mailing_tbl WHERE mailing_id = ?", String.class, followUpMailingID);
        } catch (Exception e) {
            logger.error("Database Error getting target-id Strings in MailingDaoImpl", e);
            return null;
        }
    }

    /**
     * Retrieve list split ID for mailing ID.
     *
     * @param mailingId mailing ID
     * @return list split ID or 0
     */
    private int getListSplitId(int mailingId) {
        try {
            return selectInt("SELECT split_id FROM mailing_tbl WHERE mailing_id = ?", mailingId);
        } catch (Exception e) {
            logger.warn("Error reading list split ID for mailing " + mailingId, e);
            return 0;
        }
    }
    
    private String getTargetExpressionForId(int expressionID) {
        try {
            return select("SELECT target_sql FROM dyn_target_tbl WHERE target_id = ?", String.class, expressionID);
        } catch (Exception e) {
            logger.error("Error getting target-sql from DB.", e);
            return "";
        }
    }

	@Override
	public Map<Integer, Integer> getSendStats(Mailing mailing, int companyId) {
		final Map<Integer, Integer> returnMap = new HashMap<>();
		returnMap.put(SEND_STATS_TEXT, 0);
		returnMap.put(SEND_STATS_HTML, 0);
		returnMap.put(SEND_STATS_OFFLINE, 0);
		
		final MediatypeEmail mediatype = mailing.getEmailParam();
		if (mediatype != null && StringUtils.isNotBlank(mediatype.getFollowupFor())) {
			returnMap.put(SEND_STATS_TEXT, getFollowUpStat(mailing.getId(), Integer.parseInt(mediatype.getFollowupFor()), mediatype.getFollowUpMethod(), companyId, true));
			return returnMap;
		}

        List<MediaTypes> activeMediaTypesSortedByPrio = mailing.getMediatypes().values().stream()
            .filter(x -> x.getStatus() == MediaTypeStatus.Active.getCode())
            .sorted(Comparator.comparingInt(Mediatype::getPriority))
            .map(Mediatype::getMediaType)
            .toList();

        Set<MediaTypes> alreadyCountedMediatypes = new HashSet<>();
        for (MediaTypes mediaType : activeMediaTypesSortedByPrio) {
            if (mediaType == MediaTypes.EMAIL) {
                int code = mailing.getEmailParam().getMailFormat();
                Map<MailType, Integer> emailRecipientNumbers = getMailingRecipientAmountsForEmail(companyId, mailing.getMailinglistID(), MailType.getFromInt(code), mailing.getTargetExpression(), mailing.getSplitID(), mailing.isEncryptedSend(), alreadyCountedMediatypes);
                returnMap.put(SEND_STATS_TEXT, returnMap.get(SEND_STATS_TEXT) + emailRecipientNumbers.get(MailType.TEXT));
                returnMap.put(SEND_STATS_HTML, returnMap.get(SEND_STATS_HTML) + emailRecipientNumbers.get(MailType.HTML));
                returnMap.put(SEND_STATS_OFFLINE, returnMap.get(SEND_STATS_OFFLINE) + emailRecipientNumbers.get(MailType.HTML_OFFLINE));
            } else {
                returnMap.putIfAbsent(mediaType.getMediaCode(), 0);
                returnMap.put(mediaType.getMediaCode(), returnMap.get(mediaType.getMediaCode()) + getMailingRecipientAmountForNonEmailMediaType(companyId, mailing.getMailinglistID(), mediaType, mailing.getTargetExpression(), mailing.getSplitID(), mailing.isEncryptedSend(), alreadyCountedMediatypes));
            }
            alreadyCountedMediatypes.add(mediaType);
        }
        return returnMap;
	}

	private Map<MailType, Integer> getMailingRecipientAmountsForEmail(int companyID, int mailinglistID, MailType mailingMailType, String targetExpression, int splitID, boolean encryptedSend, Set<MediaTypes> excludeRecipientsOfMediatypes) {
        String sqlStatement = "SELECT cust.mailtype, COUNT(DISTINCT cust.customer_id) AS count"
                + " FROM customer_" + companyID + "_tbl cust, " + "customer_" + companyID + "_binding_tbl bind"
                + " WHERE cust.customer_id = bind.customer_id"
                + " AND bind.mailinglist_id = ?"
                + " AND bind.user_status = ?"
                + " AND bind.mediatype = ?";

        final String targetAndSplitSql = targetService.getSQLFromTargetExpression(targetExpression, splitID, companyID);
        if (StringUtils.isNotBlank(targetAndSplitSql)) {
            sqlStatement += " AND (" + targetAndSplitSql + ")";
        }

        if (encryptedSend) {
            sqlStatement += " AND cust.sys_encrypted_sending = 1";
        }

        if (CollectionUtils.isNotEmpty(excludeRecipientsOfMediatypes)) {
            sqlStatement += " AND NOT EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind2"
                + " WHERE cust.customer_id = bind2.customer_id"
                + " AND bind2.mediatype IN (" + excludeRecipientsOfMediatypes.stream().map(x -> Integer.toString(x.getMediaCode())).collect(Collectors.joining(", ")) + "))";
        }

        sqlStatement += " GROUP BY cust.mailtype ORDER BY cust.mailtype";

        List<Map<String, Object>> result = select(sqlStatement, mailinglistID, UserStatus.Active.getStatusCode(), MediaTypes.EMAIL.getMediaCode());

        Map<MailType, Integer> returnMap = new HashMap<>();
        returnMap.put(MailType.TEXT, 0);
        returnMap.put(MailType.HTML, 0);
        returnMap.put(MailType.HTML_OFFLINE, 0);
        for (Map<String, Object> row : result) {
            int code = ((Number) row.get("mailtype")).intValue();
            MailType recipientMailType = MailType.getFromInt(code);
            int count = ((Number) row.get("count")).intValue();
            returnMap.put(recipientMailType, returnMap.get(recipientMailType) + count);
        }

        if (mailingMailType == MailType.TEXT) {
            returnMap.put(MailType.TEXT, returnMap.get(MailType.TEXT) + returnMap.get(MailType.HTML) + returnMap.get(MailType.HTML_OFFLINE));
            returnMap.put(MailType.HTML, 0);
            returnMap.put(MailType.HTML_OFFLINE, 0);
        } else if (mailingMailType == MailType.HTML) {
            returnMap.put(MailType.HTML, returnMap.get(MailType.HTML) + returnMap.get(MailType.HTML_OFFLINE));
            returnMap.put(MailType.HTML_OFFLINE, 0);
        }

        return returnMap;
	}

	private Integer getMailingRecipientAmountForNonEmailMediaType(int companyID, int mailinglistID, MediaTypes mediaType, String targetExpression, int splitID, boolean encryptedSend, Collection<MediaTypes> excludeRecipientsOfMediatypes) {
		if (mediaType == MediaTypes.EMAIL) {
			throw new IllegalArgumentException("Invalid mediatype '" + mediaType.name() + "' for getMailingRecipientAmountForNonEmailMediaType. Use getMailingRecipientAmountsForEmail instead");
		}

        String sqlStatement = "SELECT COUNT(DISTINCT cust.customer_id)"
                + " FROM customer_" + companyID + "_tbl cust, " + "customer_" + companyID + "_binding_tbl bind"
                + " WHERE cust.customer_id = bind.customer_id"
                + " AND bind.mailinglist_id = ?"
                + " AND bind.user_status = ?"
                + " AND bind.mediatype = ?";

        final String targetAndSplitSql = targetService.getSQLFromTargetExpression(targetExpression, splitID, companyID);
        if (StringUtils.isNotBlank(targetAndSplitSql)) {
            sqlStatement += " AND (" + targetAndSplitSql + ")";
        }

        if (encryptedSend) {
            sqlStatement += " AND cust.sys_encrypted_sending = 1";
        }

        if (CollectionUtils.isNotEmpty(excludeRecipientsOfMediatypes)) {
            sqlStatement += " AND NOT EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind2"
                + " WHERE cust.customer_id = bind2.customer_id"
                + " AND bind2.mediatype IN (" + excludeRecipientsOfMediatypes.stream().map(x -> Integer.toString(x.getMediaCode())).collect(Collectors.joining(", ")) + "))";
        }

        return selectInt(sqlStatement, mailinglistID, UserStatus.Active.getStatusCode(), mediaType.getMediaCode());
	}
}
