/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.SafeString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.reminder.beans.ComReminder;
import com.agnitas.emm.core.reminder.beans.impl.ComReminderImpl;
import com.agnitas.emm.core.reminder.dao.impl.ComReminderBaseDaoImpl;
import com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus;
import com.agnitas.emm.core.workflow.beans.WorkflowReminder;
import com.agnitas.emm.core.workflow.beans.WorkflowReminderRecipient;
import com.agnitas.emm.core.workflow.dao.ComWorkflowStartStopReminderDao;

public class ComWorkflowStartStopReminderDaoImpl extends ComReminderBaseDaoImpl implements ComWorkflowStartStopReminderDao {
	private static final transient Logger logger = Logger.getLogger(ComWorkflowStartStopReminderDaoImpl.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DateUtilities.YYYY_MM_DD);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DateUtilities.HH_MM);

    private static final String STR_MISSING_START_MESSAGE = "workflow.start.reminder.text.inactive";
    private static final String STR_START_MESSAGE = "workflow.start.reminder.text.active";

    private static final String STR_MISSING_START_TITLE = "workflow.start.reminder.title.inactive";
    private static final String STR_START_TITLE = "workflow.start.reminder.title.active";
    private static final String STR_STOP_TITLE = "workflow.stop.reminder.title.active";

    private final ReminderRowMapper reminderRowMapper = new ReminderRowMapper();

    @Override
    public List<ComReminder> getReminders(Date date) {
        return getReminders(date, new CompaniesConstraints());
    }

    @Override
    public List<ComReminder> getReminders(Date date, CompaniesConstraints constraints) {
        if (date == null) {
            return Collections.emptyList();
        }

        String sqlGetReminders = "SELECT rm.reminder_id, rm.company_id, rcp.email, rm.type, rm.message, rcp.notified, " +
                "rcv.admin_lang AS lang, COALESCE(rcv.admin_timezone, snd.admin_timezone) AS timezone, " +
                "snd.fullname AS sender_name, snd.company_name AS sender_company_name, " +
                "w.shortname AS title, w.general_start_date AS workflow_start_date, w.general_end_date AS workflow_stop_date " +
                "FROM workflow_reminder_tbl rm " +
                "JOIN workflow_reminder_recp_tbl rcp " +
                "ON rcp.reminder_id = rm.reminder_id AND rcp.company_id = rm.company_id " +
                "JOIN workflow_tbl w " +
                "ON w.workflow_id = rm.workflow_id AND w.company_id = rm.company_id " +
                "JOIN admin_tbl snd " +
                "ON snd.admin_id = rm.sender_admin_id AND snd.company_id = rm.company_id " +
                "LEFT JOIN admin_tbl rcv " +
                "ON rcp.admin_id <> 0 AND rcv.admin_id = rcp.admin_id AND rcv.company_id = rm.company_id " +
                "WHERE rm.send_date < ? AND rcp.notified = 0 AND (w.status = ? AND rm.type <> ?) OR (w.status = ? AND rm.type = ?)" +
                DbUtilities.asCondition(" AND %s", constraints, "rm.company_id");

        Object[] sqlParameters = new Object[] {
                date,
                WorkflowStatus.STATUS_ACTIVE.getId(),
                ReminderType.MISSING_START.getId(),
                WorkflowStatus.STATUS_INACTIVE.getId(),
                ReminderType.MISSING_START.getId()
        };

        return select(logger, sqlGetReminders, reminderRowMapper, sqlParameters);
    }

	@Override
    public List<ComReminder> getReminders(int id) {
        String sqlGetReminders = "SELECT rm.reminder_id, rm.company_id, rcp.email, rm.type, rm.message, rcp.notified, " +
                "rcv.admin_lang AS lang, COALESCE(rcv.admin_timezone, snd.admin_timezone) AS timezone, " +
                "snd.fullname AS sender_name, snd.company_name AS sender_company_name, " +
                "w.shortname AS title, w.general_start_date AS workflow_start_date, w.general_end_date AS workflow_stop_date " +
                "FROM workflow_reminder_tbl rm " +
                "JOIN workflow_reminder_recp_tbl rcp " +
                "ON rcp.reminder_id = rm.reminder_id AND rcp.company_id = rm.company_id " +
                "JOIN workflow_tbl w " +
                "ON w.workflow_id = rm.workflow_id AND w.company_id = rm.company_id " +
                "JOIN admin_tbl snd " +
                "ON snd.admin_id = rm.sender_admin_id AND snd.company_id = rm.company_id " +
                "LEFT JOIN admin_tbl rcv " +
                "ON rcp.admin_id <> 0 AND rcv.admin_id = rcp.admin_id AND rcv.company_id = rm.company_id " +
                "WHERE rm.reminder_id = ? AND rcp.notified = 0 AND (w.status = ? AND rm.type <> ?) OR (w.status = ? AND rm.type = ?)";

        Object[] sqlParameters = new Object[] {
                id,
                WorkflowStatus.STATUS_ACTIVE.getId(),
                ReminderType.MISSING_START.getId(),
                WorkflowStatus.STATUS_INACTIVE.getId(),
                ReminderType.MISSING_START.getId()
        };

        return select(logger, sqlGetReminders, reminderRowMapper, sqlParameters);
    }

	@Override
    @DaoUpdateReturnValueCheck
    public void setNotified(List<ComReminder> reminders) {
        List<Object[]> sqlParameters = reminders.stream()
                .filter(ComReminder::isSent)
                .map(r -> new Object[]{r.getId(), r.getCompanyId(), r.getRecipientEmail()})
                .collect(Collectors.toList());

        if (sqlParameters.size() > 0) {
            String sqlSetNotified = "UPDATE workflow_reminder_recp_tbl SET notified = 1 " +
                    "WHERE reminder_id = ? AND company_id = ? AND email = ?";

            batchupdate(logger, sqlSetNotified, sqlParameters);
        }
    }

    @Override
    public void deleteReminders(@VelocityCheck int companyId, int workflowId) {
        // Also removes recipients because of foreign key (ON DELETE CASCADE).
        String sqlDeleteAll = "DELETE FROM workflow_reminder_tbl WHERE company_id = ? AND workflow_id = ?";
        update(logger, sqlDeleteAll, companyId, workflowId);
    }

    @Override
    public void deleteReminders(@VelocityCheck int companyId) {
        // Also removes recipients because of foreign key (ON DELETE CASCADE).
        String sqlDeleteAll = "DELETE FROM workflow_reminder_tbl WHERE company_id = ?";
        update(logger, sqlDeleteAll, companyId);
    }

    @Override
    public void deleteRecipients(@VelocityCheck int companyId) {
        String sqlDeleteAll = "DELETE FROM workflow_reminder_recp_tbl WHERE company_id = ?";
        update(logger, sqlDeleteAll, companyId);
    }

    @Override
    public void setReminders(@VelocityCheck int companyId, int workflowId, List<WorkflowReminder> reminders) {
        deleteReminders(companyId, workflowId);

        if (CollectionUtils.isNotEmpty(reminders)) {
            for (WorkflowReminder reminder : reminders) {
                insertReminder(companyId, workflowId, reminder);
            }
        }
    }

    private void insertReminder(@VelocityCheck int companyId, int workflowId, WorkflowReminder reminder) {
        int reminderId;

        if (isOracleDB()) {
            reminderId = selectInt(logger, "SELECT workflow_reminder_tbl_seq.nextval FROM dual");

            String sqlInsert = "INSERT INTO workflow_reminder_tbl (reminder_id, company_id, workflow_id, sender_admin_id, type, message, send_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            update(logger, sqlInsert, reminderId, companyId, workflowId, reminder.getSenderAdminId(), reminder.getType().getId(), reminder.getMessage(), reminder.getDate());
        } else {
            String sqlInsert = "INSERT INTO workflow_reminder_tbl (company_id, workflow_id, sender_admin_id, type, message, send_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            reminderId = insertIntoAutoincrementMysqlTable(logger, "reminder_id", sqlInsert, companyId, workflowId, reminder.getSenderAdminId(), reminder.getType().getId(), reminder.getMessage(), reminder.getDate());
        }

        insertRecipients(companyId, reminderId, reminder.getRecipients());
    }

    private void insertRecipients(@VelocityCheck int companyId, int reminderId, List<WorkflowReminderRecipient> recipients) {
        String sqlInsert = "INSERT INTO workflow_reminder_recp_tbl (company_id, reminder_id, email, admin_id, notified) " +
                "VALUES (?, ?, ?, ?, 0)";

        List<Object[]> sqlParameters = resolveAdmins(companyId, recipients)
                .stream()
                .map(r -> new Object[]{companyId, reminderId, r.getAddress(), r.getAdminId()})
                .collect(Collectors.toList());

        batchupdate(logger, sqlInsert, sqlParameters);
    }

    private List<WorkflowReminderRecipient> resolveAdmins(@VelocityCheck int companyId, List<WorkflowReminderRecipient> recipients) {
        List<WorkflowReminderRecipient> admins = new ArrayList<>();

        List<Integer> ids = new ArrayList<>();
        List<String> addresses = new ArrayList<>();

        for (WorkflowReminderRecipient recipient : recipients) {
            if (recipient.getAdminId() > 0) {
                if (StringUtils.isNotEmpty(recipient.getAddress())) {
                    admins.add(recipient);
                } else {
                    ids.add(recipient.getAdminId());
                }
            } else {
                addresses.add(recipient.getAddress());
            }
        }

        if (ids.size() > 0) {
            Map<Integer, String> map = resolveAdminAddresses(companyId, ids);

            for (int adminId : ids) {
                String address = map.get(adminId);

                if (address == null) {
                    throw new IllegalArgumentException("Unable to resolve admin #" + adminId);
                }

                admins.add(new WorkflowReminderRecipient(address, adminId));
            }
        }

        if (addresses.size() > 0) {
            Map<String, Integer> map = resolveAdminIds(companyId, addresses);

            for (String address : addresses) {
                Integer adminId = map.get(address);

                if (adminId == null) {
                    admins.add(new WorkflowReminderRecipient(address));
                } else {
                    admins.add(new WorkflowReminderRecipient(address, adminId));
                }
            }
        }

        return admins;
    }

    private Map<Integer, String> resolveAdminAddresses(@VelocityCheck int companyId, List<Integer> ids) {
        Map<Integer, String> map = new HashMap<>();

        String sqlResolveAdmins = "SELECT admin_id, email FROM admin_tbl WHERE admin_id IN (" +
                AgnUtils.repeatString("?", ids.size(), ", ") +
                ") AND company_id = ?";

        List<Object> sqlParameters = new ArrayList<>(ids.size() + 1);
        sqlParameters.addAll(ids);
        sqlParameters.add(companyId);

        query(logger, sqlResolveAdmins, new AdminAddressResolutionCallback(map), sqlParameters.toArray());

        return map;
    }

    private Map<String, Integer> resolveAdminIds(@VelocityCheck int companyId, List<String> addresses) {
        Map<String, Integer> map = new HashMap<>();

        String sqlResolveAdmins = "SELECT email, MAX(admin_id) AS admin_id FROM admin_tbl WHERE email IN (" +
                AgnUtils.repeatString("?", addresses.size(), ", ") +
                ") AND company_id = ? GROUP BY email";

        List<Object> sqlParameters = new ArrayList<>(addresses.size() + 1);
        sqlParameters.addAll(addresses);
        sqlParameters.add(companyId);

        query(logger, sqlResolveAdmins, new AdminIdResolutionCallback(map), sqlParameters.toArray());

        return map;
    }

    private class ReminderRowMapper implements RowMapper<ComReminder> {
        @Override
        public ComReminder mapRow(ResultSet rs, int i) throws SQLException {
            ComReminder reminder = new ComReminderImpl();
            reminder.setId(rs.getInt("reminder_id"));
            reminder.setCompanyId(rs.getInt("company_id"));
            reminder.setRecipientEmail(rs.getString("email"));
            reminder.setSenderName(getSenderName(rs.getString("sender_name"), rs.getString("sender_company_name")));
            reminder.setMessage(rs.getString("message"));
            reminder.setSent(rs.getInt("notified") != 0);
            reminder.setLang(StringUtils.defaultIfEmpty(rs.getString("lang"), DEFAULT_LANGUAGE));

            ReminderType reminderType = ReminderType.fromId(rs.getInt("type"));
            // No content generation available for unknown or corrupted reminders.
            if (reminderType != null) {
                String workflowName = rs.getString("title");

                // Recipient timezone (if available) or sender timezone (otherwise).
                ZoneId zoneId = TimeZone.getTimeZone(rs.getString("timezone")).toZoneId();
                LocalDateTime startDate = DateUtilities.toLocalDateTime(rs.getTimestamp("workflow_start_date"), zoneId);
                LocalDateTime stopDate = DateUtilities.toLocalDateTime(rs.getTimestamp("workflow_stop_date"), zoneId);

                generateContent(reminder, reminderType, workflowName, startDate, stopDate);
            }

            return reminder;
        }

        private void generateContent(ComReminder reminder, ReminderType reminderType, String workflowName, LocalDateTime startDate, LocalDateTime stopDate) {
            Locale locale = new Locale(reminder.getLang());

            switch (reminderType) {
            case MISSING_START:
                if (StringUtils.isEmpty(reminder.getMessage())) {
                    reminder.setMessage(str(STR_MISSING_START_MESSAGE, locale, reminder.getSenderName(), workflowName, startDate));
                }
                reminder.setTitle(str(STR_MISSING_START_TITLE, locale, reminder.getSenderName(), workflowName, startDate));
                break;

            case START:
                if (StringUtils.isEmpty(reminder.getMessage())) {
                    reminder.setMessage(str(STR_START_MESSAGE, locale, reminder.getSenderName(), workflowName, startDate));
                }
                reminder.setTitle(str(STR_START_TITLE, locale, reminder.getSenderName(), workflowName, startDate));
                break;

            case STOP:
                reminder.setTitle(str(STR_STOP_TITLE, locale, reminder.getSenderName(), workflowName, stopDate));
                break;
            }
        }

        private String str(String code, Locale locale, String user, String workflowName, LocalDateTime date) {
            return SafeString.getLocaleString(code, locale)
                .replace("<User>", user)
                .replace("<Name>", workflowName)
                .replace("<Date>", date == null ? "" : DATE_FORMATTER.format(date))
                .replace("<Time>", date == null ? "" : TIME_FORMATTER.format(date));
        }
    }

    private static class AdminIdResolutionCallback implements RowCallbackHandler {
        private final Map<String, Integer> map;

        public AdminIdResolutionCallback(Map<String, Integer> map) {
            this.map = Objects.requireNonNull(map);
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            map.put(rs.getString("email"), rs.getInt("admin_id"));
        }
    }

    private static class AdminAddressResolutionCallback implements RowCallbackHandler {
        private final Map<Integer, String> map;

        public AdminAddressResolutionCallback(Map<Integer, String> map) {
            this.map = Objects.requireNonNull(map);
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            map.put(rs.getInt("admin_id"), rs.getString("email"));
        }
    }
}
