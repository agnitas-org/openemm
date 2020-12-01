/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.operations;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.DbUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.agnitas.beans.ComTrackpointDef;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTrackpointDao;
import com.agnitas.messages.I18nString;

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
	
	@Override
	public boolean validate(ActionMessages errors, Locale locale, ComRecipientDao recipientDao, ComTrackpointDao trackpointDao) throws Exception {
		DbColumnType dataType;
		try {
			dataType = recipientDao.getColumnDataType(getCompanyId(), columnName);
		} catch (Exception e) {
            errors.add("trackingPointId", new ActionMessage("error.action.dbAccess"));
			return false;
		}
		SimpleDataType simpleDataType = dataType.getSimpleDataType();
		
		if (useTrack) {
			if (trackingPointId == -1) {
				if (simpleDataType != SimpleDataType.Numeric && simpleDataType != SimpleDataType.Float) {
		            errors.add("trackingPointId", new ActionMessage("error.action.trackpoint.type", "Numeric", dataType.getTypeName()));
					return false;
				}
				return true;
			}
			
			ComTrackpointDef tp = trackpointDao.get(trackingPointId, getCompanyId());
			if (tp == null) {
	            errors.add("trackingPointId", new ActionMessage("error.action.dbAccess"));
				return false;
			} else {
				switch (tp.getType()) {
					case ComTrackpointDef.TYPE_ALPHA:
						if (simpleDataType != SimpleDataType.Characters) {
				            errors.add("trackingPointId", new ActionMessage("error.action.trackpoint.type", "Alphanumeric", dataType.getTypeName()));
							return false;
						} else {
							return true;
						}
					case ComTrackpointDef.TYPE_NUM:
						if (simpleDataType != SimpleDataType.Numeric && simpleDataType != SimpleDataType.Float) {
				            errors.add("trackingPointId", new ActionMessage("error.action.trackpoint.type", "Numeric", dataType.getTypeName()));
							return false;
						} else {
							return true;
						}
					case ComTrackpointDef.TYPE_SIMPLE:
						if (simpleDataType != SimpleDataType.Numeric && simpleDataType != SimpleDataType.Float) {
				            errors.add("trackingPointId", new ActionMessage("error.action.trackpoint.type", "Simple", dataType.getTypeName()));
							return false;
						} else {
							return true;
						}
					default:
			            errors.add("Unknown TP type " + dataType.getTypeName(), new ActionMessage("error.action.trackpoint.type"));
						return false;
				}
			}
		} else {
	    	if (StringUtils.isBlank(columnName)) {
	    		return true;
	    	} else {
				switch (simpleDataType) {
					case Blob:
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.updatecustomer.invalidFieldType", columnName));
						return false;
					case Date:
					case DateTime:
						if (updateType == ActionOperationUpdateCustomerParameters.TYPE_INCREMENT_BY) {
							try {
								Double.parseDouble(updateValue);
								return true;
							} catch (Exception e) {
								errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.updatecustomer.invalidValueForType", columnName, I18nString.getLocaleString(SimpleDataType.Date.getMessageKey(), locale), updateValue));
								return false;
							}
						} else if (updateType == ActionOperationUpdateCustomerParameters.TYPE_DECREMENT_BY) {
							try {
								Double.parseDouble(updateValue);
								return true;
							} catch (Exception e) {
								errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.updatecustomer.invalidValueForType", columnName, I18nString.getLocaleString(SimpleDataType.Date.getMessageKey(), locale), updateValue));
								return false;
							}
						} else if (updateType == ActionOperationUpdateCustomerParameters.TYPE_SET_VALUE) {
							Matcher matcher = DATE_ARITHMETICS_PATTERN.matcher(updateValue.toUpperCase());
							if (matcher.matches()) {
								if (matcher.group(2) != null) {
									// Is safe, because group 2 must match "+" or "-" according to reg exp.
									try {
										Double.parseDouble(matcher.group(3));
										return true;
									} catch (Exception e) {
										errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.updatecustomer.invalidValueForType", columnName, I18nString.getLocaleString(SimpleDataType.Date.getMessageKey(), locale), updateValue));
										return false;
									}
								} else if (DbUtilities.isNowKeyword(updateValue)) {
									updateValue = "CURRENT_TIMESTAMP";
									return true;
								} else {
									SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
									format.setLenient(false);
									try {
										format.parse(updateValue);
										return true;
									} catch (ParseException e) {
										errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.updatecustomer.invalidValueForType", columnName, I18nString.getLocaleString(SimpleDataType.Date.getMessageKey(), locale), updateValue));
										return false;
									}
								}
							} else {
								try {
									SimpleDateFormat format = new SimpleDateFormat(DateUtilities.YYYYMMDD);
									format.setLenient(false);
									format.parse(updateValue);
									return true;
								} catch (Exception e) {
									errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.updatecustomer.invalidValueForType", columnName, I18nString.getLocaleString(SimpleDataType.Date.getMessageKey(), locale), updateValue));
									return false;
								}
							}
						} else {
							throw new Exception("Invalid update value type");
						}
					case Numeric:
					case Float:
						if (AgnUtils.isDouble(updateValue)) {
							return true;
						} else {
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.updatecustomer.invalidValueForType", columnName, I18nString.getLocaleString(SimpleDataType.Numeric.getMessageKey(), locale), updateValue));
							return false;
						}
					case Characters:
						// No special conditions for characters
						return true;
					default:
						throw new Exception("Unknown db field type");
				}
	        }
		}
	}
}
