/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.web.ajax;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.apache.struts.util.RequestUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;

public class CalendarAutoOptimizationAjax extends BaseCalendarAjaxServlet {
    private static final long serialVersionUID = -4290284593247306194L;

    private ComOptimizationService optimizationService;

    public static final String DATE_FORMAT_PATTERN = "dd-MM-yyyy";

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Form form = new Form(request);
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
        dateFormat.setTimeZone(AgnUtils.getTimeZone(request));

        int companyId = AgnUtils.getCompanyID(request);
        Date startDate = formatDate(dateFormat, form.getStartDate());
        Date endDate = formatDate(dateFormat, form.getEndDate());

        List<ComOptimization> optimizationsList =  getComOptimizationService().getOptimizationsForCalendar(companyId, startDate, endDate);
        HttpUtils.responseJson(response, writer -> {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setDateFormat(dateFormat);
            objectMapper.writeValue(writer, optimizationsList);
        });
    }

    private Date formatDate(DateFormat format, String date) {
        try {
            return format.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    private ComOptimizationService getComOptimizationService() {
        if (optimizationService == null) {
            optimizationService = getApplicationContext().getBean("optimizationService", ComOptimizationService.class);
        }
        return optimizationService;
    }

    public static class Form {
        private String startDate;
        private String endDate;

        public Form(HttpServletRequest request) throws ServletException {
            RequestUtils.populate(this, request);
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }
}
