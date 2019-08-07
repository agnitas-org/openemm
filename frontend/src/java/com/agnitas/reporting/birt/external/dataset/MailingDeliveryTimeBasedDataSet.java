/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import com.agnitas.reporting.birt.external.beans.LightMailing;

public class MailingDeliveryTimeBasedDataSet extends BIRTDataSet {

    private static final SimpleDateFormat DATE_PARAMETER_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat DATE_PARAMETER_FORMAT_2 = new SimpleDateFormat("yyyy-MM-dd:HH");
    private static final SimpleDateFormat DATE_PARAMETER_FORMAT_3 = new SimpleDateFormat("yyyy-MM-dd");

    public List<TimeBasedDeliveryStatRow> getData(int mailingID, @VelocityCheck int companyID, String language, String startDateString, String endDateString) throws Exception {
        String query = "";
        Date startDate;
        try {
            Calendar startDateCalendar = new GregorianCalendar();
            if (startDateString.contains(".") && StringUtils.countOccurrencesOf(startDateString, ":") > 1) {
                startDate = DATE_PARAMETER_FORMAT_1.parse(startDateString);
                startDateCalendar.setTime(startDate);
            } else if (startDateString.contains(":")) {
                startDate = DATE_PARAMETER_FORMAT_2.parse(startDateString);
                startDateCalendar.setTime(startDate);
            } else {
                startDate = DATE_PARAMETER_FORMAT_3.parse(startDateString);
                startDateCalendar.setTime(startDate);
                startDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
            }
            startDateCalendar.set(Calendar.MINUTE, 0);
            startDateCalendar.set(Calendar.SECOND, 0);
            startDateCalendar.set(Calendar.MILLISECOND, 0);
            startDate = startDateCalendar.getTime();
        } catch (ParseException e) {
            throw new Exception("Error while parsing start date: " + startDateString, e);
        }

        Date endDate;
        try {
            Calendar endDateCalendar = new GregorianCalendar();
            if (endDateString.contains(".") && StringUtils.countOccurrencesOf(endDateString, ":") > 1) {
                endDate = DATE_PARAMETER_FORMAT_1.parse(endDateString);
                endDateCalendar.setTime(endDate);
            } else if (startDateString.contains(":")) {
                endDate = DATE_PARAMETER_FORMAT_2.parse(endDateString);
                endDateCalendar.setTime(endDate);
            } else {
                endDate = DATE_PARAMETER_FORMAT_3.parse(endDateString);
                endDateCalendar.setTime(endDate);
                endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
            }
            endDateCalendar.set(Calendar.MINUTE, 59);
            endDateCalendar.set(Calendar.SECOND, 59);
            endDateCalendar.set(Calendar.MILLISECOND, 999);
            endDate = endDateCalendar.getTime();
        } catch (ParseException e) {
            throw new Exception("Error while parsing end date: " + endDateString, e);
        }

        Object[] parameters = {startDate, endDate, mailingID, companyID};
        JdbcTemplate template = new JdbcTemplate(getDataSource());
        String mailtypeQuery = "select mailing_type from mailing_tbl where mailing_id = ?";
        int mailingType = template.queryForObject(mailtypeQuery, Integer.class, mailingID);
        boolean isDateBasedMailing = mailingType == LightMailing.TYPE_DATE_BASED;
        if (isOracleDB()) {
            String sendHourSQL = isDateBasedMailing ?
                    "SUBSTR(to_char(timestamp , 'yyyy-mm-dd hh24:mi'), 1, 10)" :
                    "concat(SUBSTR(to_char(timestamp , 'yyyy-mm-dd hh24:mi'), 1, 15), '0')";

            query = "select sum(no_of_mailings) as mail_num, send_hour, max(send_date) max_send_date from " +
                    "(select timestamp as send_date, no_of_mailings, " + sendHourSQL + " as send_hour from mailing_account_tbl " +
                    "where timestamp BETWEEN ? and ? and mailing_id = ? and company_id = ? and status_field != 'A' and status_field != 'T') group by send_hour order by send_hour";
        } else {
            String sendHourSQL = isDateBasedMailing ?
                    "SUBSTRING(DATE_FORMAT(timestamp , '%Y-%m-%d %H:%i'), 1, 10)" :
                    "CONCAT(SUBSTRING(DATE_FORMAT(timestamp , '%Y-%m-%d %H:%i'), 1, 15), '0')";
            query = "select sum(no_of_mailings) as mail_num, send_hour from " +
                    "(select no_of_mailings, " + sendHourSQL + " as send_hour from mailing_account_tbl " +
                    "sub_res where timestamp BETWEEN ? and ? and mailing_id = ? and company_id = ? and status_field != 'A' and status_field != 'T') res group by send_hour order by send_hour";
        }

        List<Map<String, Object>> deliveryList = template.queryForList(query, parameters);
        List<TimeBasedDeliveryStatRow> resultList = new ArrayList<>();
        for (Map<String, Object> rowAsMap : deliveryList) {
            TimeBasedDeliveryStatRow resultRow = new TimeBasedDeliveryStatRow();
            resultRow.setMailNum(((Number) rowAsMap.get("mail_num")).intValue());
            resultRow.setSendTime((Date) rowAsMap.get("max_send_date"));
            if (isDateBasedMailing) {
                DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale(language));
                resultRow.setSendTimeDisplay(format.format(resultRow.getSendTime()));
            }
            else {
                resultRow.setSendTimeDisplay(((String)rowAsMap.get("send_hour")));
            }
            resultList.add(resultRow);
        }
        return resultList;
    }

    public Date getStartDate(int mailingID, @VelocityCheck int companyID) {
        String sql = "select max(timestamp) as start_date from mailing_account_tbl where mailing_id =? and company_id = ? and status_field='W'";
        JdbcTemplate template = new JdbcTemplate(getDataSource());
		List<Map<String, Object>> list = template.queryForList(sql, new Object[]{mailingID, companyID});
        if (list != null && list.size() == 1) {
            return (Date) list.get(0).get("start_date");
        }
        return null;
    }

}
