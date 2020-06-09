/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.dao.impl;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.SafeString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.calendar.beans.ComCalendarComment;
import com.agnitas.emm.core.calendar.beans.ComCalendarCommentRecipient;
import com.agnitas.emm.core.calendar.beans.impl.ComCalendarCommentImpl;
import com.agnitas.emm.core.calendar.beans.impl.ComCalendarCommentRecipientImpl;
import com.agnitas.emm.core.calendar.dao.ComCalendarCommentDao;
import com.agnitas.emm.core.reminder.beans.ComReminder;
import com.agnitas.emm.core.reminder.beans.impl.ComReminderImpl;
import com.agnitas.emm.core.reminder.dao.impl.ComReminderBaseDaoImpl;

public class ComCalendarCommentDaoImpl extends ComReminderBaseDaoImpl implements ComCalendarCommentDao {
	private static final transient Logger logger = Logger.getLogger(ComCalendarCommentDaoImpl.class);

    @Override
	public ComCalendarCommentRecipient createCalendarCommentRecipient() {
        return new ComCalendarCommentRecipientImpl();
    }

    @Override
	@DaoUpdateReturnValueCheck
    public int saveComment(ComCalendarComment calendarComment) {
        if (calendarComment.getCommentId() == 0) {
            List<Object> values = new ArrayList<>();
            int newID;

            values.add(calendarComment.getCompanyId());
            values.add(calendarComment.getAdminId());
            values.add(calendarComment.getComment());
            values.add(calendarComment.getDate());
            values.add(calendarComment.isDeadline() ? 1 : 0);
            values.add(calendarComment.getPlannedSendDate());

            if (isOracleDB()) {
                String newIDQuery = "SELECT calendar_comment_tbl_seq.NEXTVAL FROM dual";
                newID = selectInt(logger, newIDQuery);
                String query = "INSERT INTO calendar_comment_tbl (comment_id, company_id, admin_id, comment_content, comment_date, deadline, planned_send_date)"
                	+ " VALUES (?, ?, ?, ?, ?, ?, ?)";
                values.add(0, newID);
                update(logger, query, values.toArray());
            } else {
                String insertStatement = "INSERT INTO calendar_comment_tbl (company_id, admin_id, comment_content, comment_date, deadline, planned_send_date)"
                	+ " VALUES (" + AgnUtils.repeatString("?", 6, ", ") + ")";
                
                newID = insertIntoAutoincrementMysqlTable(logger, "comment_id", insertStatement, values.toArray());
            }

            calendarComment.setCommentId(newID);
        } else {
            String sqlUpdateComment = "UPDATE calendar_comment_tbl SET comment_content = ?, comment_date = ?, deadline = ?, planned_send_date = ?"
            	+ " WHERE comment_id = ? AND company_id = ?";

            update(logger, sqlUpdateComment, calendarComment.getComment(),
                    calendarComment.getDate(),
                    calendarComment.isDeadline() ? 1 : 0,
                    calendarComment.getPlannedSendDate(),
                    calendarComment.getCommentId(),
                    calendarComment.getCompanyId());

            update(logger, "DELETE FROM calendar_custom_recipients_tbl WHERE comment_id = ? AND company_id = ?", calendarComment.getCommentId(), calendarComment.getCompanyId());
        }

        String sqlInsertRecipients = "INSERT INTO calendar_custom_recipients_tbl (comment_id, company_id, email, admin_id, notified) VALUES(" +
                calendarComment.getCommentId() +
                ", " +
                calendarComment.getCompanyId() +
                ", ?, ?, ?)";

        List<ComCalendarCommentRecipient> recipientsList = calendarComment.getRecipients();
        if (CollectionUtils.isNotEmpty(recipientsList)) {
            List<Object[]> values = new ArrayList<>();

            for (ComCalendarCommentRecipient recipient : recipientsList) {
                values.add(new Object[]{
                        recipient.getAddress(),
                        recipient.getAdminId(),
                        recipient.isNotified()
                });
            }

            batchupdate(logger, sqlInsertRecipients, values);
        }

        return calendarComment.getCommentId();
    }

    @Override
    public List<ComCalendarComment> getComments(Date startDate, Date endDate, @VelocityCheck int companyId) {
        String sqlGetCalendarComment = "SELECT comment_id, company_id, admin_id, comment_content, comment_date, deadline, planned_send_date FROM calendar_comment_tbl"
        	+ " WHERE comment_date >= ? AND comment_date <= ? AND company_id = ?";

		List<Map<String, Object>> commentsList = select(logger, sqlGetCalendarComment, startDate, endDate, companyId);
        List<ComCalendarComment> comments = new ArrayList<>();

		for (Map<String, Object> commentMap : commentsList) {
			ComCalendarComment comment = new ComCalendarCommentImpl();

			comment.setCommentId(((Number) commentMap.get("comment_id")).intValue());
			comment.setCompanyId(((Number) commentMap.get("company_id")).intValue());
			comment.setAdminId(((Number) commentMap.get("admin_id")).intValue());
			comment.setComment((String) commentMap.get("comment_content"));
			comment.setDate((Date) commentMap.get("comment_date"));
			comment.setDeadline((getValueOrDefaultFromNumberField((Number) commentMap.get("deadline"), 0)) == 1);
			comment.setPlannedSendDate((Date) commentMap.get("planned_send_date"));

            List<ComCalendarCommentRecipient> recipients = new ArrayList<>();

            for (Map<String, Object> recipientMap : select(logger, getSelectRecipientsQuery(comment))) {
                String address = (String) recipientMap.get("email");
                int adminId = getValueOrDefaultFromNumberField((Number) recipientMap.get("admin_id"), 0);
                boolean notified = (getValueOrDefaultFromNumberField((Number) recipientMap.get("notified"), 0)) != 0;

                ComCalendarCommentRecipient recipient = createCalendarCommentRecipient();
                recipient.setAddress(address);
                recipient.setAdminId(adminId);
                recipient.setNotified(notified);
                recipients.add(recipient);
            }
            comment.setRecipients(recipients);

			comments.add(comment);
		}
		return comments;
	}

    @Override
	@DaoUpdateReturnValueCheck
    public boolean deleteComment(int commentId, @VelocityCheck int companyId) {
        String sqlDeleteComment = "DELETE FROM calendar_comment_tbl WHERE comment_id = ? AND company_id = ?";

        int result = update(logger, sqlDeleteComment, commentId, companyId);

        if (result > 0) {
            update(logger, "DELETE FROM calendar_custom_recipients_tbl WHERE comment_id = ? AND company_id = ?", commentId, companyId);
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean deleteCommentsByCompanyID(@VelocityCheck int companyId) {
    	try {
    		int result = update(logger, "DELETE FROM calendar_comment_tbl WHERE company_id= ?", companyId);
    		if (result > 0) {
    			update(logger, "DELETE FROM calendar_custom_recipients_tbl WHERE company_id = ?", companyId);
    		}
    		return true;
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    @Override
    public void setNotified(List<ComReminder> reminders) {
        String sqlSetCustomNotified = "UPDATE calendar_custom_recipients_tbl recp SET notified = 1 WHERE company_id = ? AND comment_id = ? AND (email = ? OR (email IS NULL AND ? = (SELECT email FROM admin_tbl adm WHERE adm.company_id = recp.company_id AND adm.admin_id = recp.admin_id)))";

        if (CollectionUtils.isNotEmpty(reminders)) {
            List<Object[]> valuesSetCustomNotified = new ArrayList<>();

            for (ComReminder reminder : reminders) {
                if (reminder.isSent()) {
                    valuesSetCustomNotified.add(new Object[]{
                            reminder.getCompanyId(),
                            reminder.getId(),
                            reminder.getRecipientEmail(),
                            reminder.getRecipientEmail()
                    });
                }
            }

            if (!valuesSetCustomNotified.isEmpty()) {
                batchupdate(logger, sqlSetCustomNotified, valuesSetCustomNotified);
            }
        }
    }

    @Override
    public List<ComReminder> getReminders(Date date) {
        List<ComReminder> reminders = new ArrayList<>();

        if (isOracleDB()) {
            List<Map<String, Object>> rows = select(logger, getRemindersQuery(date));
            for (Map<String, Object> row : rows) {
                reminders.add(createReminder(row));
            }
        } else {
            List<Map<String, Object>> rows = select(logger, getRemindersQuery(date));
            int serverOffsetHours = TimeZone.getDefault().getRawOffset() / 3600000;
            for (Map<String, Object> row : rows) {
                Timestamp plannedSendDate = (Timestamp) row.get("planned_send_date");
                String admin_timezone = (String) row.get("admin_timezone");

                // When a timezone is the same as a server's one
                if ("SYSTEM".equalsIgnoreCase(admin_timezone)) {
                    admin_timezone = TimeZone.getDefault().getID();
                }

                int adminOffsetHours = TimeZone.getTimeZone(admin_timezone).getRawOffset() / 3600000;
                Calendar adminCalendar = Calendar.getInstance();
                adminCalendar.setTime(date);
                adminCalendar.setTimeZone(TimeZone.getTimeZone(admin_timezone));
                adminCalendar.add(Calendar.HOUR_OF_DAY, -serverOffsetHours + adminOffsetHours);

                Date serverDateForClient = adminCalendar.getTime();
                if (serverDateForClient.compareTo(plannedSendDate) > 0) {
                    reminders.add(createReminder(row));
                }
            }
        }

        return reminders;
    }

    @Override
    public List<ComReminder> getReminders(Date date, CompaniesConstraints constraints) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<ComReminder> getReminders(int id) {
        String sqlGetReminderById = "SELECT comm.comment_id AS id, comm.company_id AS company_id, COALESCE(recp.email, radm.email) AS email, comment_content AS message, COALESCE(radm.admin_lang, sadm.admin_lang) AS lang, comm.admin_id AS admin_id, comm.comment_date AS comment_date, 0 AS notified, planned_send_date " +
                "FROM calendar_comment_tbl comm " +
                "JOIN calendar_custom_recipients_tbl recp ON comm.comment_id = recp.comment_id AND comm.company_id = recp.company_id " +
                "JOIN admin_tbl sadm ON sadm.admin_id = comm.admin_id AND sadm.company_id = comm.company_id " +
                "LEFT JOIN admin_tbl radm ON recp.email IS NULL AND recp.admin_id = radm.admin_id AND radm.company_id = comm.company_id " +
                "WHERE COALESCE(recp.email, radm.email) IS NOT NULL AND comm.comment_id = " + id;

        List<Map<String, Object>> results = select(logger, sqlGetReminderById);
        if (CollectionUtils.isNotEmpty(results)) {
            return results.stream().map(this::createReminder).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    protected ComReminder createReminder(Map<String, Object> row) {
        String language = StringUtils.defaultIfEmpty((String) row.get("lang"), DEFAULT_LANGUAGE);
        Locale locale = new Locale(language);

        String title = SafeString.getLocaleString("calendar.notification.email.title", locale);
        Date commentDate = (Date) row.get("comment_date");

        ComReminder reminder = new ComReminderImpl();

        reminder.setId(((Number)row.get("id")).intValue());
        reminder.setCompanyId(((Number)row.get("company_id")).intValue());
        reminder.setRecipientEmail((String) row.get("email"));
        reminder.setLang(language);
        reminder.setMessage(buildMessage((String) row.get("message"), commentDate, locale));
        reminder.setSenderName(getSenderName(((Number) row.get("admin_id")).intValue()));
        reminder.setTitle(title.replace("<User>", reminder.getSenderName()));
        reminder.setSent(1 == ((Number)row.getOrDefault("notified", 0)).intValue());

        return reminder;
    }

    private String getRemindersQuery(Date date) {
        String sqlGetReminders = "SELECT comm.comment_id AS id, " +
                "comm.company_id AS company_id, " +
                "COALESCE(recp.email, radm.email) AS email, " +
                "comment_content AS message, " +
                "sadm.admin_lang AS lang, " +
                "comm.admin_id AS admin_id, " +
                "comm.comment_date AS comment_date, " +
                "0 AS notified, " +
                "planned_send_date, " +
                "COALESCE(radm.admin_timezone, sadm.admin_timezone) AS admin_timezone " +
                "FROM calendar_comment_tbl comm " +
                "JOIN calendar_custom_recipients_tbl recp ON comm.comment_id = recp.comment_id AND comm.company_id = recp.company_id " +
                "JOIN admin_tbl sadm ON sadm.admin_id = comm.admin_id AND sadm.company_id = comm.company_id " +
                "LEFT JOIN admin_tbl radm ON recp.email IS NULL AND recp.admin_id = radm.admin_id AND radm.company_id = comm.company_id " +
                "WHERE comm.deadline = 1 AND recp.notified = 0 AND COALESCE(recp.email, radm.email) IS NOT NULL";

        if (isOracleDB()) {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String serverLocalTime = format.format(date);
            String serverTimeZone = Calendar.getInstance().getTimeZone().getID();

            return sqlGetReminders + " AND " +
                    "TO_DATE(TO_CHAR(TO_TIMESTAMP_TZ(" +
                    "			'" + serverLocalTime + "' || '" + serverTimeZone + "', 'DD-MM-YYYY HH24:MI TZR'" +
                    "		) AT TIME ZONE COALESCE(radm.admin_timezone, sadm.admin_timezone, SESSIONTIMEZONE),'DD-MM-YYYY HH24:MI'" +
                    "	), 'DD-MM-YYYY HH24:MI'" +
                    ") >= planned_send_date";
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateForQuery = format.format(date);
            return sqlGetReminders + " AND planned_send_date >= subdate('" + dateForQuery + "', 1) AND planned_send_date <= adddate('" + dateForQuery + "', 1)";
        }
    }

    private String getSelectRecipientsQuery(ComCalendarComment calendarComment) {
        return "SELECT recp.email AS email, COALESCE(recp.admin_id, 0) AS admin_id, recp.notified AS notified " +
                "FROM calendar_custom_recipients_tbl recp " +
                "WHERE recp.comment_id = " + calendarComment.getCommentId() + " AND recp.company_id = " + calendarComment.getCompanyId();
    }

    private String buildMessage(String message, Date commentDate, Locale locale) {
        StringBuilder messageBuilder = new StringBuilder("");
        DateFormat outputDateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);

        if (StringUtils.isNotEmpty(message)) {
            messageBuilder.append(message);
            if (commentDate != null) {
                messageBuilder.append("\n\n ")
                        .append(SafeString.getLocaleString("calendar.comment.info", locale))
                        .append(" ")
                        .append(outputDateFormat.format(commentDate));
            }
        }

        return messageBuilder.toString();
    }
}
