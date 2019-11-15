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

import com.agnitas.reporting.birt.external.beans.LightMailing;
import org.apache.log4j.Logger;

import com.agnitas.reporting.birt.external.dao.impl.LightMailingDaoImpl;

public class AutoOptimizationDataSet extends BIRTDataSet{
	private static final transient Logger logger = Logger.getLogger(AutoOptimizationDataSet.class);

	public static class AutoOptimizationData {
		String autoOptimizationName;
		String testGroup1;
		String testGroup2;
		String testGroup3;
		String testGroup4;
		String testGroup5;
		String resultMailing;

		public String getAutoOptimizationName() {
			return autoOptimizationName;
		}
		public String getTestGroup1() {
			return testGroup1;
		}
		public String getTestGroup2() {
			return testGroup2;
		}
		public String getTestGroup3() {
			return testGroup3;
		}
		public String getTestGroup4() {
			return testGroup4;
		}
		public String getTestGroup5() {
			return testGroup5;
		}

		public String getResultMailing() {
			return resultMailing;
		}

	}

    public AutoOptimizationData getData(Integer optimizationID, Integer companyID) {
        List<Integer> mailings = getAutoOptimizationMailings(optimizationID, companyID);
        AutoOptimizationData data = new AutoOptimizationData();
        if ((mailings != null) && (mailings.size() > 0)) {
            List<String> mailingNames = new ArrayList<>();
			LightMailingDaoImpl lightMailingDao = new LightMailingDaoImpl(getDataSource());
			for (Integer mailingID : mailings) {
				LightMailing mailing = lightMailingDao.getMailing(mailingID, companyID);
				if (mailing != null) {
                    mailingNames.add(mailing.getShortname());
                } else {
                    mailingNames.add(null);
                }
            }
            data.autoOptimizationName = getAutoOptimizationShortname(optimizationID, companyID);
            data.testGroup1 = mailingNames.get(0);
            data.testGroup2 = mailingNames.get(1);
            data.testGroup3 = mailingNames.get(2);
            data.testGroup4 = mailingNames.get(3);
            data.testGroup5 = mailingNames.get(4);
            data.resultMailing = mailingNames.get(5);
        }
        return data;
    }

    public String getAutoOptimizationShortname(int optimizationID, int companyID) {
        String shortname = null;
		String query = "SELECT shortname FROM auto_optimization_tbl WHERE optimization_id=? and company_id=?";
		List<Map<String, Object>> optimizationElements = select(logger, query, optimizationID, companyID);
        if (optimizationElements.size() > 0) {
        	Map<String, Object> map = optimizationElements.get(0);
            shortname = (String) map.get("shortname");
        }
        return shortname;
    }

}
