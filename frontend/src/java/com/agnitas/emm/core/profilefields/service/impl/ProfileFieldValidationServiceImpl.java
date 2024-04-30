/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.KeywordList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
import com.agnitas.emm.core.recipient.dto.RecipientFieldDto;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;

import static java.text.MessageFormat.format;

public class ProfileFieldValidationServiceImpl implements ProfileFieldValidationService {
    
    private static final Logger logger = LogManager.getLogger(ProfileFieldValidationServiceImpl.class);
    
    private static final int MAX_VARCHAR_LENGTH = 4000;

    private final KeywordList databaseKeywordList;
    private final ProfileFieldDao profileFieldDao;
    private final ComTargetService targetService;
    private final ConfigService configService;

    public ProfileFieldValidationServiceImpl(KeywordList databaseKeywordList, ProfileFieldDao profileFieldDao, ComTargetService targetService, ConfigService configService) {
        this.databaseKeywordList = databaseKeywordList;
        this.profileFieldDao = profileFieldDao;
        this.targetService = targetService;
        this.configService = configService;
    }

    @Override
    public boolean isDbFieldNameContainsSpaces(String fieldName) {
        fieldName = StringUtils.trimToNull(fieldName);
        return fieldName != null && !fieldName.matches("\\S+");
    }

    @Override
    public boolean isValidDbFieldName(String fieldName) {
        fieldName = StringUtils.trimToNull(fieldName);

        if (fieldName == null || !fieldName.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
            return false;
        }

        if (databaseKeywordList.containsKeyWord(fieldName) || fieldName.startsWith("sys_") || fieldName.startsWith("agn_")) {
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
    public boolean isValidShortname(int companyId, String shortName, String fieldName) {
        boolean isValid;

        if (StringUtils.equalsIgnoreCase(shortName, fieldName)) {
            isValid = true;
        } else {
            isValid = fieldNotExistInDb(companyId, shortName, fieldName);
        }

        return isValid;
    }

    @Override
    public boolean isShortnameInDB(int companyId, String shortName) {
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
    public boolean isInvalidFloatField(String fieldType, String fieldDefault, Locale locale) {
        if (!DbColumnType.GENERIC_TYPE_FLOAT.equals(fieldType) || StringUtils.isEmpty(fieldDefault)) {
            return false;
        }
        String normalizedDecimalNumber = AgnUtils.getNormalizedDecimalNumber(fieldDefault, locale);
        if (normalizedDecimalNumber.isEmpty()) {
            return true;
        }
        
        return !AgnUtils.isDouble(normalizedDecimalNumber);
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
    public boolean isInvalidVarcharField(String fieldType, long fieldLength) {
        return DbColumnType.GENERIC_TYPE_VARCHAR.equals(fieldType)
                && (fieldLength < 0 || fieldLength > MAX_VARCHAR_LENGTH);
    }

    @Override
    public boolean mayAddNewColumn(int companyId) {
        return profileFieldDao.mayAdd(companyId);
    }

    @Override
    public boolean notContainsInDb(int companyId, String fieldName) {
        try {
            return !profileFieldDao.checkProfileFieldExists(companyId, fieldName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNotAllowedNumberOfEntries(int companyId) {
        int numberOfEntries = profileFieldDao.countCustomerEntries(companyId);

        return numberOfEntries > configService.getIntegerValue(ConfigValue.MaximumNumberOfEntriesForDefaultValueChange, companyId);
    }

    @Override
    public boolean hasTargetGroups(int companyId, String fieldName) {
        List<TargetLight> targetLights = targetService.listTargetGroupsUsingProfileFieldByDatabaseName(fieldName, companyId);

        return !targetLights.isEmpty();
    }

    @Override
    public boolean isStandardColumn(String fieldName) {
        return ArrayUtils.contains(ComCompanyDaoImpl.STANDARD_CUSTOMER_FIELDS, fieldName);
    }
    
    @Override
    public ServiceResult<Object> validateNewProfileFieldValue(Admin admin, RecipientFieldDto fieldChange) {
        Locale locale = admin.getLocale();
        String newValue = fieldChange.getNewValue();
        String fieldName = fieldChange.getShortname();

        ProfileField profileField = null;
        try {
            profileField = profileFieldDao.getProfileField(admin.getCompanyID(), fieldChange.getShortname());
        } catch (Exception e) {
            logger.error(format("Could find field by column name {0}", fieldChange.getShortname()), e);
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
	            case DateTime:
                    SimpleDateFormat dateTimeFormat = admin.getDateTimeFormat();
                    String dateTimeFormatPattern = dateTimeFormat.toPattern();
                    try {
                        newValue = StringUtils.replace(newValue, dateTimeFormatPattern, "");
                        value = dateTimeFormat.parse(newValue);
                    } catch (Exception e) {
                       message = Message.of("error.bulkAction.datatype", fieldName,
                               I18nString.getLocaleString(DbColumnType.SimpleDataType.DateTime.getMessageKey(), locale) + "(" + dateTimeFormatPattern + ")");
                    }
                    break;
                case Numeric:
                    String normalizedNumericValue = AgnUtils.normalizeNumber(locale, newValue);
                    int numericPrecision = profileField.getNumericPrecision();
                    int numericScale = profileField.getNumericScale();
                    if (!AgnUtils.isDouble(normalizedNumericValue)) {
                        message = Message.of("error.bulkAction.datatype", fieldName,
                                I18nString.getLocaleString(DbColumnType.SimpleDataType.Numeric.getMessageKey(), locale));
                    } else if (!isValidProfileFieldNumberSize(normalizedNumericValue, numericPrecision, numericScale)) {
                        message = getMessageWithFieldName(fieldName,"error.contentLengthExceedsLimit", locale,
                                String.format("(%d,%d)", numericPrecision, numericScale));
                    } else {
                        value = normalizedNumericValue;
                    }
                    break;
                case Float:
                    String normalizedFloatValue = AgnUtils.normalizeNumber(locale, newValue);
                    int floatPrecision = profileField.getNumericPrecision();
                    int floatScale = profileField.getNumericScale();
                    if (!AgnUtils.isDouble(normalizedFloatValue)) {
                        message = Message.of("error.bulkAction.datatype", fieldName,
                                I18nString.getLocaleString(DbColumnType.SimpleDataType.Float.getMessageKey(), locale));
                    } else if (!isValidProfileFieldNumberSize(normalizedFloatValue, floatScale, floatScale)) {
                        message = getMessageWithFieldName(fieldName,"error.contentLengthExceedsLimit", locale,
                                String.format("(%d,%d)", floatPrecision, floatScale));
                    } else {
                        value = normalizedFloatValue;
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
            return decimal.precision() <= numericPrecision && decimal.scale() <= numericScale;
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
