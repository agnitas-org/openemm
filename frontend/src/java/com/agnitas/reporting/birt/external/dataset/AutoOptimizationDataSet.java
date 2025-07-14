/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import com.agnitas.reporting.birt.external.beans.LightMailing;
import com.agnitas.reporting.birt.external.dao.impl.LightMailingDaoImpl;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AutoOptimizationDataSet extends BIRTDataSet {

	public static class AutoOptimizationData {
		String autoOptimizationName;
		String testGroup1;
		int testGroup1Id;
		String testGroup2;
		int testGroup2Id;
		String testGroup3;
		int testGroup3Id;
		String testGroup4;
		int testGroup4Id;
		String testGroup5;
		int testGroup5Id;
		String resultMailing;
		int resultMailingId;
		int winnerId;

		public String getAutoOptimizationName() {
			return autoOptimizationName;
		}
		public String getTestGroup1() {
			return testGroup1;
		}
		public int getTestGroup1Id() {
			return testGroup1Id;
		}
		public String getTestGroup2() {
			return testGroup2;
		}
		public int getTestGroup2Id() {
			return testGroup2Id;
		}
		public String getTestGroup3() {
			return testGroup3;
		}
		public int getTestGroup3Id() {
			return testGroup3Id;
		}
		public String getTestGroup4() {
			return testGroup4;
		}
		public int getTestGroup4Id() {
			return testGroup4Id;
		}
		public String getTestGroup5() {
			return testGroup5;
		}
		public int getTestGroup5Id() {
			return testGroup5Id;
		}
		public String getResultMailing() {
			return resultMailing;
		}
		public int getResultMailingId() {
			return resultMailingId;
		}
		public int getWinnerId() {return winnerId; }
	}

    public AutoOptimizationData getData(Integer optimizationID, Integer companyID) {
        List<Integer> mailings = getAutoOptimizationMailings(optimizationID, companyID);
        AutoOptimizationData data = new AutoOptimizationData();
        if (CollectionUtils.isNotEmpty(mailings)) {
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
            data.testGroup1Id = mailings.get(0);
            data.testGroup1 = mailingNames.get(0);
			data.testGroup2Id = mailings.get(1);
			data.testGroup2 = mailingNames.get(1);
			data.testGroup3Id = mailings.get(2);
			data.testGroup3 = mailingNames.get(2);
			data.testGroup4Id = mailings.get(3);
			data.testGroup4 = mailingNames.get(3);
			data.testGroup5Id = mailings.get(4);
			data.testGroup5 = mailingNames.get(4);
			data.resultMailingId = mailings.get(5);
			data.resultMailing = mailingNames.get(5);
			data.winnerId = getAutoOptimizationWinnerId(optimizationID, companyID);
        }
        return data;
    }

    public String getAutoOptimizationShortname(int optimizationID, int companyID) {
        String shortname = null;
		String query = "SELECT shortname FROM auto_optimization_tbl WHERE optimization_id=? and company_id=?";
		List<Map<String, Object>> optimizationElements = select(query, optimizationID, companyID);
        if (!optimizationElements.isEmpty()) {
        	Map<String, Object> map = optimizationElements.get(0);
            shortname = (String) map.get("shortname");
        }
        return shortname;
    }

}
