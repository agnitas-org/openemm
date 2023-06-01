/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.validators;

import static com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters.DATE_ARITHMETICS_PATTERN;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbUtilities;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComTrackpointDef;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTrackpointDao;
import com.agnitas.emm.core.action.operations.ActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;

@Component
public class UpdateCustomerValidator implements ActionOperationValidator {

    private ComRecipientDao recipientDao;
    private ComTrackpointDao trackpointDao;

    public UpdateCustomerValidator(ComRecipientDao recipientDao, @Autowired(required = false) ComTrackpointDao trackpointDao) {
        this.recipientDao = recipientDao;
        this.trackpointDao = trackpointDao;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ActionOperationUpdateCustomerParameters.class.isAssignableFrom(clazz);
    }

    @Override
    public SimpleServiceResult validate(Admin admin, ActionOperationParameters target) throws Exception {
        ActionOperationUpdateCustomerParameters operation = (ActionOperationUpdateCustomerParameters) target;
        DbColumnType dataType;
        List<Message> errors = new ArrayList<>();
        boolean valid = true;

        try {
            dataType = recipientDao.getColumnDataType(admin.getCompanyID(), operation.getColumnName());
        } catch (Exception e) {
            return new SimpleServiceResult(false, Message.of("error.action.dbAccess"));
        }


        DbColumnType.SimpleDataType simpleDataType = dataType.getSimpleDataType();
        if (operation.isUseTrack()) {
            valid = validateTrackingPoint(admin, operation, simpleDataType, dataType, errors);
        } else {
            valid = validateColumnName(admin, operation, errors, simpleDataType);
        }


        return new SimpleServiceResult(valid, errors);
    }

    private boolean validateColumnName(Admin admin, ActionOperationUpdateCustomerParameters operation, List<Message> errors, DbColumnType.SimpleDataType simpleDataType) throws Exception {
        List<Message> errorMessages = new ArrayList<>();

        String columnName = operation.getColumnName();
        String updateValueStr = operation.getUpdateValue();
        int updateType = operation.getUpdateType();
        if (StringUtils.isNotBlank(operation.getColumnName())) {
            switch (simpleDataType) {
                case Blob:
                    errorMessages.add(Message.of("error.updatecustomer.invalidFieldType", columnName));
                    break;
                case Date:
                case DateTime:
                    boolean valid = true;
                    if (updateType == ActionOperationUpdateCustomerParameters.TYPE_INCREMENT_BY) {
                        try {
                            Double.parseDouble(updateValueStr);
                        } catch (Exception e) {
                            valid = false;
                            errorMessages.add(Message.of("error.updatecustomer.invalidFieldType",
                                    columnName, I18nString.getLocaleString(DbColumnType.SimpleDataType.Date.getMessageKey(), admin.getLocale()), updateValueStr));
                        }
                    } else if (updateType == ActionOperationUpdateCustomerParameters.TYPE_DECREMENT_BY) {
                        try {
                            Double.parseDouble(updateValueStr);
                        } catch (Exception e) {
                            valid = false;
                        }
                    } else if (updateType == ActionOperationUpdateCustomerParameters.TYPE_SET_VALUE) {
                        Matcher matcher = DATE_ARITHMETICS_PATTERN.matcher(updateValueStr.toUpperCase());
                        if (matcher.matches()) {
                            if (matcher.group(2) != null) {
                                // Is safe, because group 2 must match "+" or "-" according to reg exp.
                                try {
                                    Double.parseDouble(matcher.group(3));
                                } catch (Exception e) {
                                    valid = false;
                                }
                            } else if (DbUtilities.isNowKeyword(updateValueStr)) {
                                operation.setUpdateValue("CURRENT_TIMESTAMP");
                            } else {
                                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                                format.setLenient(false);
                                try {
                                    format.parse(updateValueStr);
                                } catch (ParseException e) {
                                    valid = false;
                                }
                            }
                        } else {
                            try {
                                SimpleDateFormat format = new SimpleDateFormat(DateUtilities.YYYYMMDD);
                                format.setLenient(false);
                                format.parse(updateValueStr);
                            } catch (Exception e) {
                                valid = false;
                            }
                        }

                    } else {
                        throw new Exception("Invalid update value type");
                    }

                    if (!valid) {
                        errorMessages.add(Message.of("error.updatecustomer.invalidFieldType",
                                columnName,
                                I18nString.getLocaleString(DbColumnType.SimpleDataType.Date.getMessageKey(), admin.getLocale()),
                                updateValueStr));
                    }
                    break;
                case Numeric:
                case Float:
                    if (!AgnUtils.isDouble(updateValueStr)) {
                        errorMessages.add(Message.of("error.updatecustomer.invalidFieldType",
                                columnName,
                                I18nString.getLocaleString(DbColumnType.SimpleDataType.Numeric.getMessageKey(), admin.getLocale()),
                                updateValueStr));
                    }
                    break;
                case Characters:
                    // No special conditions for characters
                    break;
                default:
                    throw new Exception("Unknown db field type");
            }
        }

        errors.addAll(errorMessages);
        return errorMessages.isEmpty();
    }

    private boolean validateTrackingPoint(Admin admin, ActionOperationUpdateCustomerParameters operation, DbColumnType.SimpleDataType simpleDataType, DbColumnType dataType, List<Message> messages) {
        List<Message> errorMessages = new ArrayList<>();
        if (operation.getTrackingPointId() == -1) {
            if (simpleDataType != DbColumnType.SimpleDataType.Numeric && simpleDataType != DbColumnType.SimpleDataType.Float) {
                errorMessages.add(Message.of("error.action.trackpoint.type", "Numeric", dataType.getTypeName()));
            }
        } else {
            if (trackpointDao != null) {
                ComTrackpointDef trackingPoint = trackpointDao.get(operation.getTrackingPointId(), admin.getCompanyID());
                if (trackingPoint == null) {
                    errorMessages.add(Message.of("error.action.dbAccess"));
                } else {
                    switch (trackingPoint.getType()) {
                        case ComTrackpointDef.TYPE_ALPHA:
                            if (simpleDataType != DbColumnType.SimpleDataType.Characters) {
                                errorMessages.add(Message.of("error.action.trackpoint.type", "Alphanumeric", dataType.getTypeName()));
                            }
                            break;
                        case ComTrackpointDef.TYPE_NUM:
                            if (simpleDataType != DbColumnType.SimpleDataType.Numeric && simpleDataType != DbColumnType.SimpleDataType.Float) {
                                errorMessages.add(Message.of("error.action.trackpoint.type", "Numeric", dataType.getTypeName()));
                            }
                            break;
                        case ComTrackpointDef.TYPE_SIMPLE:
                            if (simpleDataType != DbColumnType.SimpleDataType.Numeric && simpleDataType != DbColumnType.SimpleDataType.Float) {
                                errorMessages.add(Message.of("error.action.trackpoint.type", "Simple", dataType.getTypeName()));
                            }
                            break;
                        default:
                            errorMessages.add(Message.of("error.action.trackpoint.type"));
                    }
                }
            }
        }

        messages.addAll(errorMessages);
        return errorMessages.isEmpty();
    }
}
