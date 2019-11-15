/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.agnitas.beans.ProfileField;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.KeywordList;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComProfileField;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComProfileFieldDao;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
import com.agnitas.emm.core.recipient.dto.RecipientFieldDto;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;

public class ProfileFieldValidationServiceImpl implements ProfileFieldValidationService {
    
    private static final Logger logger = Logger.getLogger(ProfileFieldValidationServiceImpl.class);
    
    private static final int MAX_VARCHAR_LENGTH = 4000;

    private KeywordList databaseKeywordList;
    private ComProfileFieldDao profileFieldDao;
    private ComTargetService targetService;
    private ConfigService configService;

    @Required
    public void setDatabaseKeywordList(KeywordList databaseKeywordList) {
        this.databaseKeywordList = databaseKeywordList;
    }

    @Required
    public void setProfileFieldDao(ComProfileFieldDao profileFieldDao) {
        this.profileFieldDao = profileFieldDao;
    }

    @Required
    public void setTargetService(ComTargetService targetService) {
        this.targetService = targetService;
    }

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public boolean isValidDbFieldName(String fieldName) {
        fieldName = StringUtils.trimToNull(fieldName);

        if (fieldName == null || !fieldName.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
            return false;
        }

        if (databaseKeywordList.containsKeyWord(fieldName) || fieldName.startsWith("sys_")) {
            return false;
        }

        for (String standardField : ComCompanyDaoImpl.STANDARD_CUSTOMER_FIELDS) {
            if (standardField.equalsIgnoreCase(fieldName)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isInvalidLengthForDbFieldName(String fieldName) {
        fieldName = StringUtils.trimToNull(fieldName);
        return fieldName == null || fieldName.length() < 3 || fieldName.length() > 30;
    }

    @Override
    public boolean isValidShortname(@VelocityCheck int companyId, String shortName, String fieldName) {
        boolean isValid;

        if (StringUtils.equalsIgnoreCase(shortName, fieldName)) {
            isValid = true;
        } else {
            isValid = fieldNotExistInDb(companyId, shortName, fieldName);
        }

        return isValid;
    }

    @Override
    public boolean isShortnameInDB(@VelocityCheck int companyId, String shortName) {
        try {
            return profileFieldDao.getProfileFieldByShortname(companyId, shortName) != null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isInvalidIntegerField(String fieldType, String fieldDefault) {
        return DbColumnType.GENERIC_TYPE_INTEGER.equals(fieldType)
                && StringUtils.isNotEmpty(fieldDefault)
                && !AgnUtils.isNumber(fieldDefault);
    }

    @Override
    public boolean isAllowedDefaultValue(String fieldType, String defaultValue, SimpleDateFormat dateFormat) {
        return DbUtilities.checkAllowedDefaultValue(fieldType, defaultValue, dateFormat);
    }

    @Override
    public boolean isDefaultValueAllowedInDb(int companyId, String fieldName, String defaultValue) {
        try {
            return profileFieldDao.checkAllowedDefaultValue(companyId, fieldName, defaultValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isInvalidVarcharField(String fieldType, int fieldLength) {
        return DbColumnType.GENERIC_TYPE_VARCHAR.equals(fieldType)
                && (fieldLength < 0 || fieldLength > MAX_VARCHAR_LENGTH);
    }

    @Override
    public boolean mayAddNewColumn(@VelocityCheck int companyId) {
        return profileFieldDao.mayAdd(companyId);
    }

    @Override
    public boolean notContainsInDb(@VelocityCheck int companyId, String fieldName) {
        try {
            return !profileFieldDao.checkProfileFieldExists(companyId, fieldName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNotAllowedNumberOfEntries(@VelocityCheck int companyId) {
        int numberOfEntries = profileFieldDao.countCustomerEntries(companyId);

        return numberOfEntries > configService.getIntegerValue(ConfigValue.MaximumNumberOfEntriesForDefaultValueChange, companyId);
    }

    @Override
    public boolean hasTargetGroups(@VelocityCheck int companyId, String fieldName) {
        List<TargetLight> targetLights = targetService.listTargetGroupsUsingProfileFieldByDatabaseName(fieldName, companyId);

        return targetLights.size() > 0;
    }

    @Override
    public boolean isStandardColumn(String fieldName) {
        return ArrayUtils.contains(ComCompanyDaoImpl.STANDARD_CUSTOMER_FIELDS, fieldName);
    }
    
    @Override
    public ServiceResult<Object> validateNewProfileFieldValue(ComAdmin admin, RecipientFieldDto fieldChange) {
        Locale locale = admin.getLocale();
        String newValue = fieldChange.getNewValue();
        String fieldName = fieldChange.getShortname();

        ComProfileField profileField = null;
        try {
            profileField = profileFieldDao.getProfileField(admin.getCompanyID(), fieldChange.getShortname());
        } catch (Exception e) {
            logger.error("Could find field by column name " + fieldChange.getShortname(), e);
        }
        
        Message message = null;
        Object value = null;

        if (profileField == null) {
            message = getMessageWithFieldName(fieldName, "error.profiledb.notExists", locale);
        } else if (!profileField.getNullable() && fieldChange.isClear()) {
            message = getMessageWithFieldName(fieldName, "error.profiledb.empty", locale);
        } else if (!isAllowedNewValue(profileField.getAllowedValues(), fieldChange.getNewValue())) {
            message = getMessageWithFieldName(fieldName, "error.profiledb.invalidFixedValue", locale);
        } else if (fieldChange.isClear() && StringUtils.isNotBlank(newValue)) {
            message = Message.of("error.bulkAction.empty.clear", fieldName);
        } else if (StringUtils.isBlank(newValue)) {
            value = "";
        } else {
            switch (fieldChange.getType()) {
                case Date:
                    SimpleDateFormat dateFormat = admin.getDateFormat();
                    String dateFormatPattern = dateFormat.toPattern();
                    try {
                        newValue = StringUtils.replace(newValue, dateFormatPattern, "");
                        value = dateFormat.parse(newValue);
                    } catch (Exception e) {
                       message = Message.of("error.bulkAction.datatype", fieldName,
                               I18nString.getLocaleString(DbColumnType.SimpleDataType.Date.getMessageKey(), locale) + "(" + dateFormatPattern + ")");
                    }
                    break;
                case Numeric:
                    String normalizedValue = AgnUtils.normalizeNumber(locale, newValue);
                    int numericPrecision = profileField.getNumericPrecision();
                    int numericScale = profileField.getNumericScale();
                    if (!AgnUtils.isDouble(normalizedValue)) {
                        message = Message.of("error.bulkAction.datatype", fieldName,
                                I18nString.getLocaleString(DbColumnType.SimpleDataType.Numeric.getMessageKey(), locale));
                    } else if (!isValidProfileFieldNumberSize(normalizedValue, numericPrecision, numericScale)) {
                        message = getMessageWithFieldName(fieldName,"error.contentLengthExceedsLimit", locale,
                                String.format("(%d,%d)", numericPrecision, numericScale));
                    } else {
                        value = normalizedValue;
                    }
                    break;
                case Characters:
                    if (StringUtils.length(newValue) > profileField.getDataTypeLength()) {
                        message = getMessageWithFieldName(fieldName, "error.contentLengthExceedsLimit", locale, profileField.getDataTypeLength());
                    } else {
                        value = newValue;
                    }
                    break;
                default:
                    value = newValue;
                    break;
            }
        }
    
        return new ServiceResult<>(value, value != null, message);
    }
    
    private boolean isAllowedNewValue(String[] allowedValues, String newValue) {
        return StringUtils.isBlank(newValue) ||
                allowedValues == null || ArrayUtils.contains(allowedValues, newValue);
    }
    
    private Message getMessageWithFieldName(String fieldName, String messageKey, Locale locale, Object... parameters) {
        return Message.exact(String.format("%s: %s", fieldName, I18nString.getLocaleString(messageKey, locale, parameters)));
    }
    
    private boolean isValidProfileFieldNumberSize(String newValue, int numericPrecision, int numericScale) {
        if (numericPrecision > 0 || numericScale > 0) {
			BigDecimal decimal = new BigDecimal(newValue);
			if (decimal.precision() > numericPrecision || decimal.scale() > numericScale) {
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
    }
    
    private boolean fieldNotExistInDb(int companyId, String shortName, String fieldName) {
        try {
            List<ProfileField> profileFields = profileFieldDao.getProfileFields(companyId);

            for (ProfileField field : profileFields) {
                boolean isColumnEqualsFieldName = StringUtils.equalsIgnoreCase(field.getColumn(), fieldName);
                boolean isColumnEqualsShortname = StringUtils.equalsIgnoreCase(field.getColumn(), shortName);
                boolean isDbShortnameEqualsShortname = StringUtils.equalsIgnoreCase(field.getShortname(), shortName);

                if (!isColumnEqualsFieldName && (isColumnEqualsShortname || isDbShortnameEqualsShortname)) {
                    return false;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

}
