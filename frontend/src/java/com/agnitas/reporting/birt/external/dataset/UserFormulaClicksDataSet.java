/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;

import com.agnitas.reporting.birt.external.beans.UserFormulaURLClickStatRow;

/**
 * BIRT-DataSet for user formula URL clicks statistics
 */
public class UserFormulaClicksDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(UserFormulaClicksDataSet.class);
	
	public List<UserFormulaURLClickStatRow> getClicksPerUrl(@VelocityCheck int formID, int companyID) {
		logger.debug("getClicksPerUrl started - formula id: " + formID);
		
        String query = 
			"SELECT" +
				" COUNT(measured_customer_id) clicks_gross," +
				" COUNT(distinct measured_customer_id) clicks_net," +
				" sum(anonym_clicks) clicks_anonym," +
				" url_id," +
				" " + getIfNull() + "(shortname, full_url) url" +
			" FROM (" +
				"SELECT" +
					" urltbl.url_id," +
					" urltbl.full_url," +
					" urltbl.shortname," +
					" (CASE WHEN (customer_id IS NOT NULL AND customer_id <> 0) THEN customer_id ELSE NULL END) measured_customer_id," +
					" (CASE WHEN (customer_id IS NULL OR customer_id = 0) THEN 1 ELSE 0 END) anonym_clicks" +
				" FROM rdir_url_userform_tbl urltbl JOIN rdirlog_userform_" + companyID + "_tbl logtbl  " + " ON (logtbl.form_id = " + formID + " AND logtbl.url_id = urltbl.url_id ))" + (isOracleDB() ? "" : " subsel") +
			" GROUP BY url_id, full_url, shortname" +
			" ORDER BY clicks_net DESC";
        
		List<UserFormulaURLClickStatRow> urlClickList = new ArrayList<>();
		
		List<Map<String, Object>> result = select(logger, query);
		for (Map<String, Object> row : result) {
			UserFormulaURLClickStatRow tmp = new UserFormulaURLClickStatRow();
			tmp.setUrl((String) row.get("url"));
			tmp.setUrl_id(((Number) row.get("url_id")).intValue());
			tmp.setClicks_gros(((Number) row.get("clicks_gross")).intValue());
			tmp.setClicks_net(((Number) row.get("clicks_net")).intValue());
			tmp.setClicks_unique(0);
            tmp.setClicks_anonymous(((Number) row.get("clicks_anonym")).intValue());
			urlClickList.add(tmp);
		}
		
		logger.debug("getClicksPerUrl finished - formula id: " + formID);
		
		return urlClickList;
	}
		
}
