/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface FeatureCleanupDao {
	public enum CleanupFeature {
		ProfileFieldHistory,
		Deeptracking,
		WebPush,
		Sms,
		AHV,
		OMG,
		DeliveryInformation,
		Innerclients;
		
		public static CleanupFeature getFeatureByName(String featureName) throws Exception {
			for (CleanupFeature item : CleanupFeature.values()) {
				if (item.name().equalsIgnoreCase(featureName)) {
					return item;
				}
			}
			throw new Exception("Invalid feature name: " + featureName);
		}
	}
	
	public enum CleanupStatus {
		CleanupToDo(0),
		CleanupFinished(1),
		NoMoreCleanup(2);

		private int statusCode;
		
		CleanupStatus(int statusCode) {
			this.statusCode = statusCode;
		}
		
		public int getStatusCode() {
			return statusCode;
		}
	}
	
	List<Map<String, Object>> getCleanupEntries(int expireDays);

    void saveCleanupEntry(@VelocityCheck int companyID, CleanupFeature feature, CleanupStatus status);
    
    boolean deleteCleanupEntriesByCompanyID(int companyID);

    boolean deleteCleanupEntriesByTime(int expireDays);
}
