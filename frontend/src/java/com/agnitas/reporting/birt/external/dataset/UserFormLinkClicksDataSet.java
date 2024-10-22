/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.reporting.birt.external.beans.UserFormLinkClicksStatisticRow;

/**
 * BIRT-DataSet for user form URL clicks statistics
 */
public class UserFormLinkClicksDataSet extends BIRTDataSet {

	private static final Logger logger = LogManager.getLogger(UserFormLinkClicksDataSet.class);

	// used in form_click_statistics.rptdesign
	public List<UserFormLinkClicksStatisticRow> getClicksPerUrl(int formID, int companyID,
																String startDate, String endDate) {
		List<UserFormLinkClicksStatisticRow> urlClickList = new ArrayList<>();
		List<Object> params = new ArrayList<>();
		params.add(formID);
		String periodFilter = getPeriodFilter(new DateFormats(startDate, endDate, false), params);

		String queryMeasured =
			"SELECT"
			+ " COALESCE(formurl.shortname, formurl.full_url) AS url,"
			+ " formurl.url_id,"
			+ " COUNT(*) AS clicks_gross,"
			+ " COUNT(DISTINCT rlog.customer_id) AS clicks_net"
			+ " FROM rdir_url_userform_tbl formurl"
			+ " JOIN rdirlog_userform_" + companyID + "_tbl rlog ON rlog.url_id = formurl.url_id"
			+ " WHERE rlog.form_id = ?" + periodFilter
			+ " AND (rlog.customer_id IS NOT NULL AND rlog.customer_id != 0)"
			+ " GROUP BY formurl.url_id, COALESCE(formurl.shortname, formurl.full_url)";
		
		List<Map<String, Object>> resultMeasured = select(logger, queryMeasured, params.toArray());
		for (Map<String, Object> row : resultMeasured) {
			UserFormLinkClicksStatisticRow statisticRow = new UserFormLinkClicksStatisticRow();
			
			statisticRow.setUrl((String) row.get("url"));
			statisticRow.setUrlId(((Number) row.get("url_id")).intValue());
			statisticRow.setClicksGross(((Number) row.get("clicks_gross")).intValue());
			statisticRow.setClicksNet(((Number) row.get("clicks_net")).intValue());
			statisticRow.setClicksUnique(0);
            
			urlClickList.add(statisticRow);
		}

		String queryAnonymous =
			"SELECT"
			+ " COALESCE(formurl.shortname, formurl.full_url) AS url,"
			+ " formurl.url_id,"
			+ " COUNT(*) AS clicks_anonym"
			+ " FROM rdir_url_userform_tbl formurl"
			+ " JOIN rdirlog_userform_" + companyID + "_tbl rlog ON rlog.url_id = formurl.url_id"
			+ " WHERE rlog.form_id = ?" + periodFilter
			+ " AND (rlog.customer_id IS NULL OR rlog.customer_id = 0)"
			+ " GROUP BY formurl.url_id, COALESCE(formurl.shortname, formurl.full_url)";
		
		List<Map<String, Object>> resultAnonymous = select(logger, queryAnonymous, params.toArray());
		for (Map<String, Object> row : resultAnonymous) {
			int urlID = ((Number) row.get("url_id")).intValue();
			
			UserFormLinkClicksStatisticRow statisticRow = null;
			for (UserFormLinkClicksStatisticRow urlClickListItem : urlClickList) {
				if (urlClickListItem.getUrlId() == urlID) {
					statisticRow = urlClickListItem;
					break;
				}
			}
			
			if (statisticRow != null) {
				// Add anonymous value to existing statistic row
	            statisticRow.setClicksAnonymous(((Number) row.get("clicks_anonym")).intValue());
			} else {
				// Add stand alone statistic row for anonymous value
				statisticRow = new UserFormLinkClicksStatisticRow();
				
				statisticRow.setUrl((String) row.get("url"));
				statisticRow.setUrlId(urlID);
	            statisticRow.setClicksAnonymous(((Number) row.get("clicks_anonym")).intValue());
				statisticRow.setClicksUnique(0);
	            
				urlClickList.add(statisticRow);
			}
		}
		
		return urlClickList;
	}

	private static String getPeriodFilter(DateFormats dateFormats, List<Object> params) {
        if (StringUtils.isBlank(dateFormats.getStartDate())) {
			return "";
		}
		params.add(dateFormats.getStartDateAsDate());
		params.add(dateFormats.getStopDateAsDate());
		return " AND rlog.timestamp >= ? AND rlog.timestamp < ?";
    }
}
