/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import com.agnitas.emm.common.MailingType;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MailingDeliveryTimeBasedDataSet extends BIRTDataSet {
    
    public static String getDateWithoutTime(String dateString) throws Exception {
        Date deliveryDate = getDeliveryDate(dateString);
        return new SimpleDateFormat(DATE_PARAMETER_FORMAT).format(deliveryDate);
    }
    
    public List<TimeBasedDeliveryStatRow> getData(int mailingID, int companyID, String language, String startDateString, String endDateString) throws Exception {
        String query;
        Date startDate = getDeliveryDate(startDateString);
        Date endDate = getDeliveryDate(endDateString);

        boolean isSingleDayStatistic = true;

        // for include an end day for collecting statistics
        int daysBetween = DateUtilities.getDaysBetween(startDate, endDate);

        if (daysBetween > 1 || daysBetween == 0) {
            endDate = DateUtilities.addDaysToDate(endDate, 1);
            isSingleDayStatistic = false;
        }

        int mailingType = selectInt("SELECT mailing_type FROM mailing_tbl WHERE mailing_id = ?", mailingID);
        boolean isDateBasedMailing = mailingType == MailingType.DATE_BASED.getCode();

        if (isOracleDB()) {
            String sendHourSQL = isDateBasedMailing ?
                    "SUBSTR(to_char(timestamp , 'yyyy-mm-dd hh24:mi'), 1, 10)" :
                    "concat(SUBSTR(to_char(timestamp , 'yyyy-mm-dd hh24:mi'), 1, 15), '0')";

            query = "SELECT sum(no_of_mailings) AS mail_num, send_hour, max(send_date) max_send_date FROM " +
                    "(SELECT timestamp AS send_date, no_of_mailings, " + sendHourSQL + " AS send_hour FROM mailing_account_tbl " +
                    "WHERE ? <= timestamp AND timestamp < ? AND mailing_id = ? AND company_id = ? AND status_field != 'A' AND status_field != 'T') GROUP BY send_hour ORDER BY send_hour";
        } else {
            String sendHourSQL;
            if (isPostgreSQL()) {
                sendHourSQL = isDateBasedMailing ?
                        "SUBSTRING(TO_CHAR(timestamp, 'YYYY-MM-DD HH24:MI'), 1, 10)" :
                        "SUBSTRING(TO_CHAR(timestamp, 'YYYY-MM-DD HH24:MI'), 1, 15) || '0'";
            } else {
                sendHourSQL = isDateBasedMailing ?
                        "SUBSTRING(DATE_FORMAT(timestamp , '%Y-%m-%d %H:%i'), 1, 10)" :
                        "CONCAT(SUBSTRING(DATE_FORMAT(timestamp , '%Y-%m-%d %H:%i'), 1, 15), '0')";
            }

            query = "SELECT sum(no_of_mailings) AS mail_num, send_hour, max(send_date) max_send_date FROM " +
                    "(SELECT timestamp AS send_date, no_of_mailings, " + sendHourSQL + " AS send_hour FROM mailing_account_tbl " +
                    "sub_res WHERE ? <= timestamp AND timestamp < ? AND mailing_id = ? AND company_id = ? AND status_field != 'A' AND status_field != 'T') res GROUP BY send_hour ORDER BY send_hour";
        }
    
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale(language));
        List<TimeBasedDeliveryStatRow> rows = select(query, new TimeBasedDelivery_RowMapper(dateFormat, isDateBasedMailing), startDate, endDate, mailingID, companyID);

        if (isDateBasedMailing && !isSingleDayStatistic) {
            return getStatForAllDays(rows, toLocalDate(startDate), toLocalDate(endDate), dateFormat);
        }

        return rows;
    }

    // Rows that contains no values are missing in query result GWUA-4943,
    // so we need to populate it manually to display them on UI with 0 value 
    private ArrayList<TimeBasedDeliveryStatRow> getStatForAllDays(List<TimeBasedDeliveryStatRow> rows,
                                                                  LocalDate from, LocalDate till, DateFormat format) {
        Map<LocalDate, TimeBasedDeliveryStatRow> days = new HashMap<>();
        rows.forEach(row -> days.put(toLocalDate(row.getSendTime()), row));

        from.datesUntil(till).forEach(day -> {
            if (!days.containsKey(day)) {
                days.put(day, generateBlankStatRow(format, DateUtilities.toDate(day, AgnUtils.getSystemTimeZoneId())));
            }
        });
        return new ArrayList<>(days.values());
    }

    private TimeBasedDeliveryStatRow generateBlankStatRow(DateFormat format, Date sendTime) {
    	TimeBasedDeliveryStatRow row = new TimeBasedDeliveryStatRow();
        row.setMailNum(0);
        row.setSendTime(sendTime);
        row.setSendTimeDisplay(format.format(sendTime));
        return row;
    }

    private LocalDate toLocalDate(Date date) {
        return DateUtilities.toLocalDate(date, AgnUtils.getSystemTimeZoneId());
    }
    
    private static Date getDeliveryDate(String dateString) throws Exception {
        Date startDate;
        try {
            Calendar startDateCalendar = new GregorianCalendar();
            if (dateString.contains(".") && StringUtils.countOccurrencesOf(dateString, ":") > 1) {
                startDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_SECOND).parse(dateString);
                startDateCalendar.setTime(startDate);
            } else if (dateString.contains(":")) {
                startDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(dateString);
                startDateCalendar.setTime(startDate);
            } else {
                startDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(dateString);
                startDateCalendar.setTime(startDate);
                startDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
            }
            startDateCalendar.set(Calendar.MINUTE, 0);
            startDateCalendar.set(Calendar.SECOND, 0);
            startDateCalendar.set(Calendar.MILLISECOND, 0);
            return startDateCalendar.getTime();
        } catch (ParseException e) {
            throw new Exception("Error while parsing start date: " + dateString, e);
        }
    }
    
    private static class TimeBasedDelivery_RowMapper implements RowMapper<TimeBasedDeliveryStatRow> {
        
        private final DateFormat format;
        private final boolean isDateBasedMailing;
    
        public TimeBasedDelivery_RowMapper(DateFormat format, boolean isDateBasedMailing) {
            this.format = format;
            this.isDateBasedMailing = isDateBasedMailing;
        }
    
        @Override
        public TimeBasedDeliveryStatRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            TimeBasedDeliveryStatRow timeBasedDeliveryRow = new TimeBasedDeliveryStatRow();
            
            timeBasedDeliveryRow.setMailNum(rs.getInt("mail_num"));
            
            Date sendTime = rs.getTimestamp("max_send_date");
            timeBasedDeliveryRow.setSendTime(sendTime);
    
            String sendTimeString = isDateBasedMailing ? format.format(sendTime) : rs.getString("send_hour");
            timeBasedDeliveryRow.setSendTimeDisplay(sendTimeString);
            
            return timeBasedDeliveryRow;
        }
    }
}
