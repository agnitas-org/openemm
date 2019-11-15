/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.agnitas.reporting.birt.external.beans.LightMailing;
import org.agnitas.dao.impl.mapper.DateRowMapper;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

public class MailingDeliveryTimeBasedDataSet extends BIRTDataSet {
    
    private static final Logger logger = Logger.getLogger(MailingDeliveryTimeBasedDataSet.class);
    
    private static final SimpleDateFormat DATE_FORMAT_WITH_SECOND = new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_SECOND);
    private static final SimpleDateFormat DATE_FORMAT_WITH_HOUR = new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR);
    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(DATE_PARAMETER_FORMAT);
    
    public static String getDateWithoutTime(String dateString) throws Exception {
        Date deliveryDate = getDeliveryDate(dateString);
        return DEFAULT_DATE_FORMAT.format(deliveryDate);
    }
    
    public List<TimeBasedDeliveryStatRow> getData(int mailingID, @VelocityCheck int companyID, String language, String startDateString, String endDateString) throws Exception {
        String query;
        Date startDate = getDeliveryDate(startDateString);
        Date endDate = getDeliveryDate(endDateString);

        int mailingType = selectInt(logger, "SELECT mailing_type FROM mailing_tbl WHERE mailing_id = ?", mailingID);
        boolean isDateBasedMailing = mailingType == LightMailing.TYPE_DATE_BASED;
        if (isOracleDB()) {
            String sendHourSQL = isDateBasedMailing ?
                    "SUBSTR(to_char(timestamp , 'yyyy-mm-dd hh24:mi'), 1, 10)" :
                    "concat(SUBSTR(to_char(timestamp , 'yyyy-mm-dd hh24:mi'), 1, 15), '0')";

            query = "SELECT sum(no_of_mailings) AS mail_num, send_hour, max(send_date) max_send_date FROM " +
                    "(SELECT timestamp AS send_date, no_of_mailings, " + sendHourSQL + " AS send_hour FROM mailing_account_tbl " +
                    "WHERE timestamp BETWEEN ? AND ? AND mailing_id = ? AND company_id = ? AND status_field != 'A' AND status_field != 'T') GROUP BY send_hour ORDER BY send_hour";
        } else {
            String sendHourSQL = isDateBasedMailing ?
                    "SUBSTRING(DATE_FORMAT(timestamp , '%Y-%m-%d %H:%i'), 1, 10)" :
                    "CONCAT(SUBSTRING(DATE_FORMAT(timestamp , '%Y-%m-%d %H:%i'), 1, 15), '0')";
            query = "SELECT sum(no_of_mailings) AS mail_num, send_hour, max(send_date) max_send_date FROM " +
                    "(SELECT timestamp AS send_date, no_of_mailings, " + sendHourSQL + " AS send_hour FROM mailing_account_tbl " +
                    "sub_res WHERE timestamp BETWEEN ? AND ? AND mailing_id = ? AND company_id = ? AND status_field != 'A' AND status_field != 'T') res GROUP BY send_hour ORDER BY send_hour";
        }
    
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale(language));
        return select(logger, query, new TimeBasedDelivery_RowMapper(dateFormat, isDateBasedMailing), startDate, endDate, mailingID, companyID);
    }

    private Date getStartDate(int mailingID, @VelocityCheck int companyID) {
        String sql = "SELECT max(timestamp) AS start_date FROM mailing_account_tbl WHERE mailing_id =? AND company_id = ? AND status_field='W'";
        return selectObjectDefaultNull(logger, sql, new DateRowMapper(), mailingID, companyID);
    }
    
    private static Date getDeliveryDate(String dateString) throws Exception {
        Date startDate;
        try {
            Calendar startDateCalendar = new GregorianCalendar();
            if (dateString.contains(".") && StringUtils.countOccurrencesOf(dateString, ":") > 1) {
                startDate = DATE_FORMAT_WITH_SECOND.parse(dateString);
                startDateCalendar.setTime(startDate);
            } else if (dateString.contains(":")) {
                startDate = DATE_FORMAT_WITH_HOUR.parse(dateString);
                startDateCalendar.setTime(startDate);
            } else {
                startDate = DEFAULT_DATE_FORMAT.parse(dateString);
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
    
    private class TimeBasedDelivery_RowMapper implements RowMapper<TimeBasedDeliveryStatRow> {
        
        private DateFormat format;
        private boolean isDateBasedMailing;
    
        public TimeBasedDelivery_RowMapper(DateFormat format, boolean isDateBasedMailing) {
            this.format = format;
            this.isDateBasedMailing = isDateBasedMailing;
        }
    
        @Override
        public TimeBasedDeliveryStatRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            TimeBasedDeliveryStatRow timeBasedDeliveryRow = new TimeBasedDeliveryStatRow();
            
            timeBasedDeliveryRow.setMailNum(rs.getInt("mail_num"));
            
            Date sendTime = rs.getDate("max_send_date");
            timeBasedDeliveryRow.setSendTime(sendTime);
    
            String sendTimeString = isDateBasedMailing ? format.format(sendTime) : rs.getString("send_hour");
            timeBasedDeliveryRow.setSendTimeDisplay(sendTimeString);
            
            return timeBasedDeliveryRow;
        }
    }
}
