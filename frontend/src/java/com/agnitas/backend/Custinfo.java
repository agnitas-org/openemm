/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.emm.core.service.RecipientStandardField;

/**
 * Keeps track of some customer relevant data
 * during mail generation
 */
public class Custinfo {
	/**
	 * The customer ID
	 */
	private long customerID = 0;
	/**
	 * The user type from the binding table
	 */
	private String userType = null;
	/**
	 * a mapping for all available columns
	 */
	private Map<String, String> columns = null;
	/**
	 * sample email address if creating sample copies
	 */
	private String sampleEmail = null;
	/**
	 * provider email address if creating a provider preview
	 */
	private String providerEmail = null;
	/**
	 * values for database retrieved target groups
	 */
	private boolean[] targetGroupValues = null;
	/**
	 * overwritten test recipient columns
	 */
	private Map <String, String> overwrittenTestRecipientColumns = null;

	public Custinfo(Data data) {
		if (data != null) {
			int targetCount = data.targetExpression.resolveByDatabase().size();
			
			if (targetCount > 0) {
				targetGroupValues = new boolean[targetCount];
			}
			overwrittenTestRecipientColumns = data.retrieveOverwrittenTestRecipientColumns ();
		}
	}

	/**
	 * Set information for the next customer
	 *
	 * @param nCustomerID the customer_id of this customer
	 * @param nUserType   his user-type
	 * @param rmap        the data read from the database
	 */
	public void newCustomer(long nCustomerID, String nUserType, Column[] rmap) {
		customerID = nCustomerID;
		userType = nUserType;
		columns = new HashMap<>();
		if (rmap != null) {
			for (int n = 0; n < rmap.length; ++n) {
				columns.put(rmap[n].getQname(), rmap[n].get());
			}
		}
		sampleEmail = null;
		if (targetGroupValues != null) {
			for (int n = 0; n < targetGroupValues.length; ++n) {
				targetGroupValues[n] = false;
			}
		}
	}

	public long getCustomerID() {
		return customerID;
	}

	public void setSampleEmail(String nSampleEmail) {
		sampleEmail = nSampleEmail;
	}

	public void setProviderEmail(String nProviderEmail) {
		providerEmail = nProviderEmail;
	}

	public void setTargetGroupValue(int pos, boolean value) {
		if ((targetGroupValues != null) && (pos >= 0) && (pos < targetGroupValues.length)) {
			targetGroupValues[pos] = value;
		}
	}

	public String getUserType() {
		return userType;
	}
	
	public String getMediaFieldContent(Media m) {
		if (m != null) {
			if (m.type == Media.TYPE_EMAIL) {
				if (sampleEmail != null) {
					return sampleEmail;
				}
				if (providerEmail != null) {
					return providerEmail;
				}
			}
			String profileField = m.profileField();
			
			if (profileField != null) {
				if ((overwrittenTestRecipientColumns != null) && overwrittenTestRecipientColumns.containsKey (profileField)) {
					return overwrittenTestRecipientColumns.get (profileField);
				}
				return columns.get(profileField);
			}
		}
		return null;
	}

	public int getGender() {
		String value = columns.get("gender");

		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				// do nothing
			}
		}
		return 2;
	}

	public String getFirstname() {
		return columns.get("firstname");
	}

	public String getLastname() {
		return columns.get("lastname");
	}

	public String getTitle() {
		return columns.get("title");
	}

	public boolean getTrackingVeto() {
		String value = columns.get(RecipientStandardField.DoNotTrack.getColumnName());

		return ((value != null) && (!"".equals(value)) && (!"0".equals(value)));
	}

	public Map<String, String> getColumns() {
		return columns;
	}

	public String getTargetGroupValue() {
		String rc = null;

		if (targetGroupValues != null) {
			rc = "";
			for (int n = 0; n < targetGroupValues.length; ++n) {
				rc += targetGroupValues[n] ? "1" : "0";
			}
		}
		return rc;
	}
}
