/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.web.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.agnitas.reporting.birt.external.beans.SendStatRow;
import com.agnitas.reporting.birt.external.dataset.MailingSummaryDataSet;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DashboardInfo extends HttpServlet {
	private static final long serialVersionUID = 3250441017268983412L;
	
	private static final transient Logger logger = Logger.getLogger(DashboardInfo.class);

    @Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        try {
			int mailingId = 0;
			if (req.getParameter("mailingId") != null) {
			    mailingId = Integer.parseInt(req.getParameter("mailingId"));
			}

			int companyId = 0;
			if (req.getParameter("companyId") != null) {
			    companyId = Integer.parseInt(req.getParameter("companyId"));
			}

			Map<Integer, Integer> statValues = new HashMap<>();
			if (mailingId != 0 && companyId != 0) {
			    MailingSummaryDataSet mailingSummaryDataSet = new MailingSummaryDataSet();
			    int tempTableID = mailingSummaryDataSet.prepareDashboardForCharts(mailingId, companyId);
			    List<MailingSummaryDataSet.MailingSummaryRow> mailingSummaryData = mailingSummaryDataSet.getSummaryData(tempTableID);

			    for (SendStatRow aMailingSummaryData : mailingSummaryData) {
			        statValues.put(aMailingSummaryData.getCategoryindex(), aMailingSummaryData.getCount());
			    }
			} else {
			    logger.error("Parameters mailingId and companyId are missing");
			}

			ObjectMapper objectMapper = new ObjectMapper();
			String jsonData = objectMapper.writeValueAsString(statValues);
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			PrintWriter writer = res.getWriter();
			writer.write(jsonData);
			// Don't close writer. It is done by the container
		} catch (Exception e) {
			logger.error("Cannot create Dashboard info", e);
		}
    }
}
