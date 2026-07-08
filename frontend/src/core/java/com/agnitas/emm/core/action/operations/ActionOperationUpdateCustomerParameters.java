/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.operations;

import java.util.regex.Pattern;

public class ActionOperationUpdateCustomerParameters extends AbstractActionOperationParameters {
	/**
	 * Regexp pattern for date arithmetics.
	 */
	public static final Pattern DATE_ARITHMETICS_PATTERN = Pattern.compile("^\\s*(SYSDATE|CURRENT_TIMESTAMP)\\s*(?:(\\+|-)\\s*(\\d+(?:\\.\\d+)?)\\s*)?$");
	
    public static final int TYPE_INCREMENT_BY = 1;
    public static final int TYPE_DECREMENT_BY = 2;
    public static final int TYPE_SET_VALUE = 3;

	private String columnName;
	private int updateType;
	private String updateValue;
	private int trackingPointId;
	private boolean useTrack;

	public ActionOperationUpdateCustomerParameters() {
		super(ActionOperationType.UPDATE_CUSTOMER);
	}

	public boolean isUseTrack() {
		return useTrack;
	}

	public void setUseTrack(boolean useTrack) {
		this.useTrack = useTrack;
	}

	public int getTrackingPointId() {
		return trackingPointId;
	}

	public void setTrackingPointId(int trackingPointId) {
		this.trackingPointId = trackingPointId;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getUpdateType() {
		return updateType;
	}

	public void setUpdateType(int updateType) {
		this.updateType = updateType;
	}

	public String getUpdateValue() {
		return updateValue;
	}

	public void setUpdateValue(String updateValue) {
		this.updateValue = updateValue;
	}
}
