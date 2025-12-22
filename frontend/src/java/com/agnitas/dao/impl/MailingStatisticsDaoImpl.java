/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.util.Collection;
import java.util.Collections;
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
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.emm.common.FollowUpType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.util.Tuple;
import com.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class MailingStatisticsDaoImpl extends BaseDaoImpl implements MailingStatisticsDao {

    private final TargetService targetService;
    
    public MailingStatisticsDaoImpl(TargetService targetService) {
    	this.targetService = Objects.requireNonNull(targetService);
    }
	
	@Override
    public int getFollowUpRecipientsCount(int mailingID, int baseMailing, String followUpType, int companyID) {
        return getFollowUpRecipients(mailingID, baseMailing, followUpType, companyID).size();
    }

    private List<Integer> getFollowUpRecipients(int mailingID, int baseMailing, String followUpType, int companyID) {
        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_CLICKER.getKey())) {
            // TODO NEW click calculation with target groups.
            // the maildrop-id must be the one of the 'base' mailing, which is
            // already sent. The given
            // mailing-id must be the one of the current (that means the
            // follow-up) mailing.
            return getClickers(baseMailing, mailingID, companyID);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_OPENER.getKey())) {
            return getOpeners(baseMailing, mailingID, companyID);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_NON_CLICKER.getKey())) {
            return getNonClickers(baseMailing, mailingID, companyID);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey())) {
            return getNonOpeners(baseMailing, mailingID, companyID);
        }

        return Collections.emptyList();
    }

	@Override
    public int getFollowUpRecipientsCount(int followUpFor, String followUpType, int companyID, String sqlTargetExpression) {
        int resultValue = 0;
        // What kind of followup do we have, choose the appropriate sql call...
        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_CLICKER.getKey())) {
            resultValue = getClickersCount(companyID, followUpFor, sqlTargetExpression);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_OPENER.getKey())) {
            resultValue = getOpenersCount(companyID, followUpFor, sqlTargetExpression);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_NON_CLICKER.getKey())) {
            resultValue = getNonClickersCount(companyID, followUpFor, sqlTargetExpression);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey())) {
            resultValue = getNonOpenersCount(companyID, followUpFor, sqlTargetExpression);
        }

        return resultValue;
    }

    private int getClickersCount(int companyID, int followUpFor, String sqlTargetExpression) {
        String query = getClickersQuery(companyID);
        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            query += " AND (%s)".formatted(sqlTargetExpression);
        }

        return selectInt(wrapIntoCountQuery(query), followUpFor);
    }

    private int getNonClickersCount(int companyID, int followUpFor, String sqlTargetExpression) {
        String query = getNonClickersQuery(companyID);

        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            query += " AND (%s)".formatted(sqlTargetExpression);
        }

        return selectInt(wrapIntoCountQuery(query), followUpFor, followUpFor);
    }

    private int getOpenersCount(int companyID, int followUpFor, String sqlTargetExpression) {
        String query = getOpenersQuery(companyID);

        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            query += " AND (%s)".formatted(sqlTargetExpression);
        }

        return selectInt(wrapIntoCountQuery(query), followUpFor);
    }

    private int getNonOpenersCount(int companyID, int followUpFor, String sqlTargetExpression) {
        String query = getNonOpenersQuery(companyID);
        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            query += " AND (%s)".formatted(sqlTargetExpression);
        }

        return selectInt(wrapIntoCountQuery(query), followUpFor, followUpFor);
    }

    private String wrapIntoCountQuery(String query) {
        return "SELECT COUNT(*) FROM (%s) c".formatted(query);
    }

    private List<Integer> getClickers(int baseMailingId, int followUpMailingId, int companyId) {
        String query = getClickersQuery(companyId) + getTargetAndSplitSqlCondition(followUpMailingId);
        return select(query, IntegerRowMapper.INSTANCE, baseMailingId);
    }

    private String getClickersQuery(int companyId) {
        return "SELECT DISTINCT bind.customer_id FROM customer_" + companyId + "_binding_tbl bind, customer_" + companyId + "_tbl cust" +
                " WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1" +
                " AND EXISTS (SELECT 1 FROM rdirlog_" + companyId + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)";
    }

    private String getTargetAndSplitSqlCondition(int followUpMailingId) {
        String sqlCondition = "";

        String targetString = getTargetIDString(followUpMailingId);
        if (getTargetExpressionIds(targetString).length != 0) {
            String targetSql = createTargetStatementWithoutAnd(targetString);
            if (StringUtils.isNotBlank(targetSql)) {
                sqlCondition += " AND (%s)".formatted(targetSql);
            }
        }

        int listSplitId = getListSplitId(followUpMailingId);
        if (listSplitId != 0) {
            sqlCondition += " AND (%s)".formatted(createTargetStatementWithoutAnd(Integer.toString(listSplitId)));
        }

        return sqlCondition;
    }

    /**
     * returns the non-clickers. maildrop-id is the id of the BASE!
     * Mailing, not the actual followup!
     */
    private List<Integer> getNonClickers(int baseMailingID, int followUpMailingID, int companyID) {
        String query = getNonClickersQuery(companyID) + getTargetAndSplitSqlCondition(followUpMailingID);
        return select(query, IntegerRowMapper.INSTANCE, baseMailingID, baseMailingID);
    }

    private String getNonClickersQuery(int companyID) {
        return "SELECT DISTINCT bind.customer_id FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust" +
                " WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1" +
                " AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)" +
                " AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)";
    }

    private List<Integer> getOpeners(int baseMailingID, int followUpMailingID, int companyID) {
        String query = getOpenersQuery(companyID) + getTargetAndSplitSqlCondition(followUpMailingID);
        return select(query, IntegerRowMapper.INSTANCE, baseMailingID);
    }

    private String getOpenersQuery(int companyID) {
        return "SELECT DISTINCT bind.customer_id FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust" +
                " WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1" +
                " AND EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)";
    }

    private List<Integer> getNonOpeners(int baseMailingID, int followUpMailingID, int companyID) {
        String query = getNonOpenersQuery(companyID) + getTargetAndSplitSqlCondition(followUpMailingID);
        return select(query, IntegerRowMapper.INSTANCE, baseMailingID, baseMailingID);
    }

    private String getNonOpenersQuery(int companyID) {
        return "SELECT DISTINCT bind.customer_id FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust" +
                " WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1" +
                " AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)" +
                " AND NOT EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)";
    }

    private String createTargetStatementWithoutAnd(String targetString) {
        final int[] targetIDs = getTargetExpressionIds(targetString);
        final String operator = getTargetOperator(targetString);

        final StringBuilder targetSql = new StringBuilder();
        for (int targetID : targetIDs) {
            if (!targetSql.isEmpty()) {
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
            logger.warn("Error reading list split ID for mailing %d".formatted(mailingId), e);
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
    public int getRecipientsCount(Mailing mailing) {
        return getRecipientsIds(mailing).size();
    }

    @Override
    public Set<Integer> getRecipientsIds(Mailing mailing) {
        MediatypeEmail mediatype = mailing.getEmailParam();
        if (mediatype != null && StringUtils.isNotBlank(mediatype.getFollowupFor())) {
            return Set.copyOf(getFollowUpRecipients(mailing.getId(), Integer.parseInt(mediatype.getFollowupFor()), mediatype.getFollowUpMethod(), mailing.getCompanyID()));
        }

        Set<Integer> recipientsIds = new HashSet<>();

        Set<MediaTypes> alreadyCountedMediaTypes = new HashSet<>();
        for (MediaTypes mediaType : getActiveMediaTypesSortedByPrio(mailing)) {
            recipientsIds.addAll(getRecipientsByMediaType(mediaType, mailing, alreadyCountedMediaTypes));
            alreadyCountedMediaTypes.add(mediaType);
        }

        return recipientsIds;
    }

    @Override
	public Map<Integer, Integer> getSendStats(Mailing mailing, int companyId) {
		final Map<Integer, Integer> returnMap = new HashMap<>();
		returnMap.put(SEND_STATS_TEXT, 0);
		returnMap.put(SEND_STATS_HTML, 0);
		returnMap.put(SEND_STATS_OFFLINE, 0);
		
		final MediatypeEmail mediatype = mailing.getEmailParam();
		if (mediatype != null && StringUtils.isNotBlank(mediatype.getFollowupFor())) {
			returnMap.put(SEND_STATS_TEXT, getFollowUpRecipientsCount(mailing.getId(), Integer.parseInt(mediatype.getFollowupFor()), mediatype.getFollowUpMethod(), companyId));
			return returnMap;
		}

        Set<MediaTypes> alreadyCountedMediatypes = new HashSet<>();
        for (MediaTypes mediaType : getActiveMediaTypesSortedByPrio(mailing)) {
            if (mediaType == MediaTypes.EMAIL) {
                Map<MailType, Integer> emailRecipientNumbers = getMailingRecipientAmountsForEmail(mailing, alreadyCountedMediatypes);
                returnMap.put(SEND_STATS_TEXT, returnMap.get(SEND_STATS_TEXT) + emailRecipientNumbers.get(MailType.TEXT));
                returnMap.put(SEND_STATS_HTML, returnMap.get(SEND_STATS_HTML) + emailRecipientNumbers.get(MailType.HTML));
                returnMap.put(SEND_STATS_OFFLINE, returnMap.get(SEND_STATS_OFFLINE) + emailRecipientNumbers.get(MailType.HTML_OFFLINE));
            } else {
                Set<Integer> recipients = getRecipientsByMediaType(mediaType, mailing, alreadyCountedMediatypes);

                returnMap.putIfAbsent(mediaType.getMediaCode(), 0);
                returnMap.put(mediaType.getMediaCode(), returnMap.get(mediaType.getMediaCode()) + recipients.size());
            }
            alreadyCountedMediatypes.add(mediaType);
        }
        return returnMap;
	}

    private List<MediaTypes> getActiveMediaTypesSortedByPrio(Mailing mailing) {
        return mailing.getMediatypes().values().stream()
                .filter(x -> x.getStatus() == MediaTypeStatus.Active.getCode())
                .distinct()
                .sorted(Comparator.comparingInt(Mediatype::getPriority))
                .map(Mediatype::getMediaType)
                .toList();
    }

	private Map<MailType, Integer> getMailingRecipientAmountsForEmail(Mailing mailing, Set<MediaTypes> excludingMediaTypes) {
        Map<MailType, Integer> mailTypesCountsMap = getRecipientsByMailTypeMap(mailing, MediaTypes.EMAIL, excludingMediaTypes)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));

        List.of(MailType.TEXT, MailType.HTML, MailType.HTML_OFFLINE)
                .forEach(mt -> mailTypesCountsMap.putIfAbsent(mt, 0));

        MailType mailType = MailType.getFromInt(mailing.getEmailParam().getMailFormat());
        if (mailType == MailType.TEXT) {
            mailTypesCountsMap.put(MailType.TEXT, mailTypesCountsMap.get(MailType.TEXT) + mailTypesCountsMap.get(MailType.HTML) + mailTypesCountsMap.get(MailType.HTML_OFFLINE));
            mailTypesCountsMap.put(MailType.HTML, 0);
            mailTypesCountsMap.put(MailType.HTML_OFFLINE, 0);
        } else if (mailType == MailType.HTML) {
            mailTypesCountsMap.put(MailType.HTML, mailTypesCountsMap.get(MailType.HTML) + mailTypesCountsMap.get(MailType.HTML_OFFLINE));
            mailTypesCountsMap.put(MailType.HTML_OFFLINE, 0);
        }

        return mailTypesCountsMap;
	}

    private Map<MailType, Set<Integer>> getRecipientsByMailTypeMap(Mailing mailing, MediaTypes mediaType, Set<MediaTypes> excludingMediaTypes) {
        int companyID = mailing.getCompanyID();

        String query = "SELECT DISTINCT cust.customer_id, cust.mailtype"
                + " FROM customer_" + companyID + "_tbl cust, " + "customer_" + companyID + "_binding_tbl bind"
                + " WHERE cust.customer_id = bind.customer_id"
                + " AND bind.mailinglist_id = ? AND bind.user_status = ? AND bind.mediatype = ?";

        String targetAndSplitSql = targetService.getSQLFromTargetExpression(
                mailing.getTargetExpression(),
                mailing.getSplitID(),
                mailing.getCompanyID()
        );

        if (StringUtils.isNotBlank(targetAndSplitSql)) {
            query += " AND (" + targetAndSplitSql + ")";
        }

        if (mailing.isEncryptedSend()) {
            query += " AND cust.sys_encrypted_sending = 1";
        }

        if (CollectionUtils.isNotEmpty(excludingMediaTypes)) {
            String joinedMailTypes = excludingMediaTypes.stream()
                    .map(x -> Integer.toString(x.getMediaCode()))
                    .collect(Collectors.joining(", "));

            query += " AND NOT EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind2"
                    + " WHERE cust.customer_id = bind2.customer_id"
                    + " AND bind2.mediatype IN (" + joinedMailTypes + "))";
        }

        query += " ORDER BY cust.mailtype";

        List<Tuple<MailType, Integer>> rows = select(
                query,
                (rs, rowNum) -> Tuple.of(MailType.getFromInt(rs.getInt("mailtype")), rs.getInt("customer_id")),
                mailing.getMailinglistID(),
                UserStatus.Active.getStatusCode(),
                mediaType.getMediaCode()
        );

        return rows.stream().
                collect(Collectors.groupingBy(
                        Tuple::getFirst,
                        Collectors.mapping(Tuple::getSecond, Collectors.toSet())
                ));
    }

    private Set<Integer> getRecipientsByMediaType(MediaTypes mediaType, Mailing mailing, Set<MediaTypes> excludingMediaTypes) {
        return getRecipientsByMailTypeMap(mailing, mediaType, excludingMediaTypes).values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
