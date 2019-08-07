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

public class CompanyDescriptionDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(CompanyDescriptionDataSet.class);

	public List<String> getCompanyDescription (@VelocityCheck int companyID){
		List<String> companyDescription = new ArrayList<>();
		String query = getCompanyDescriptionQuery(companyID);
    	List<Map<String, Object>> result = select(logger, query);
		if (result.size() > 0){
			companyDescription.add((String) result.get(0).get("company_name"));
		}
		return companyDescription;
	}

	private String getCompanyDescriptionQuery(@VelocityCheck int companyID) {
		return "select shortname company_name from company_tbl where company_id = " + (Integer.toString(companyID)) ;
	}
}
