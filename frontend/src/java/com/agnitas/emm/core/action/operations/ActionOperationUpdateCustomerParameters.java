/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.operations;

import javax.sql.DataSource;

import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.DbUtilities;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.springframework.context.ApplicationContext;

import com.agnitas.beans.ComTrackpointDef;
import com.agnitas.dao.ComTrackpointDao;

public class ActionOperationUpdateCustomerParameters extends AbstractActionOperationParameters {
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
	
	@Override
	public boolean validate(ActionErrors errors, ApplicationContext applicationContext) {
		if (useTrack) {
			DataSource ds = (DataSource) applicationContext.getBean("dataSource");
			DbColumnType dt = null;
			try {
				dt = DbUtilities.getColumnDataType(ds, "customer_"+this.getCompanyId()+"_tbl", columnName);
			} catch (Exception e) {
	            errors.add("trackingPointId", new ActionMessage("error.action.dbAccess"));
				return false;
			}

			if (trackingPointId == -1) {
				if (dt.getSimpleDataType() != SimpleDataType.Numeric) {
		            errors.add("trackingPointId", new ActionMessage("error.action.trackpoint.type", "Numeric", dt.getTypeName()));
					return false;
				}
				return true;
			}
			
			ComTrackpointDao trackpointDao = (ComTrackpointDao) applicationContext.getBean("TrackpointDao");
			if (trackpointDao != null) {
				ComTrackpointDef tp = trackpointDao.get(trackingPointId, this.getCompanyId());
				if (tp == null) {
		            errors.add("trackingPointId", new ActionMessage("error.action.dbAccess"));
					return false;
				}
				
				switch (tp.getType()) {
					case ComTrackpointDef.TYPE_ALPHA:
						if (dt.getSimpleDataType() != SimpleDataType.Characters) {
				            errors.add("trackingPointId", new ActionMessage("error.action.trackpoint.type", "Alphanumeric", dt.getTypeName()));
							return false;
						}
						break;
					case ComTrackpointDef.TYPE_NUM:
						if (dt.getSimpleDataType() != SimpleDataType.Numeric) {
				            errors.add("trackingPointId", new ActionMessage("error.action.trackpoint.type", "Numeric", dt.getTypeName()));
							return false;
						}
						break;
					case ComTrackpointDef.TYPE_SIMPLE:
						if (dt.getSimpleDataType() != SimpleDataType.Numeric) {
				            errors.add("trackingPointId", new ActionMessage("error.action.trackpoint.type", "Simple", dt.getTypeName()));
							return false;
						}
						break;
					default:
			            errors.add("Unkmow TP type and " + dt.getTypeName() + " column", new ActionMessage("error.action.trackpoint.type"));
						return false;
				}
				System.out.println("TP.type is " + tp.getType() + " Colum type is " + dt.toString());
			}
		}
		return true;
	}
}
