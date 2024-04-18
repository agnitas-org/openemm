/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service.impl;

import static java.text.MessageFormat.format;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.agnitas.beans.ExportPredef;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.ExportPredefDao;
import org.agnitas.dao.ImportProfileDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.KeywordList;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.Admin;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.objectusage.service.ObjectUsageService;
import com.agnitas.emm.core.objectusage.web.ObjectUsagesToPopups;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientFieldService.RecipientStandardField;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.web.mvc.Popups;

public class ProfileFieldValidationServiceImpl implements ProfileFieldValidationService {
    
    private static final Logger logger = LogManager.getLogger(ProfileFieldValidationServiceImpl.class);
    
    private static final int MAX_VARCHAR_LENGTH = 4000;

    private final KeywordList databaseKeywordList;
    private final RecipientFieldService recipientFieldService;
    private final ComTargetService targetService;
    private final ConfigService configService;
    private final ObjectUsageService objectUsageService;
    private final ImportProfileDao importProfileDao;
    private final ExportPredefDao exportPredefDao;

    public ProfileFieldValidationServiceImpl(KeywordList databaseKeywordList, RecipientFieldService recipientFieldService, ComTargetService targetService, ConfigService configService, ObjectUsageService objectUsageService,
    		ImportProfileDao importProfileDao, ExportPredefDao exportPredefDao) {
        this.databaseKeywordList = databaseKeywordList;
        this.recipientFieldService = recipientFieldService;
        this.targetService = targetService;
        this.configService = configService;
        this.objectUsageService = objectUsageService;
        this.importProfileDao = importProfileDao;
        this.exportPredefDao = exportPredefDao;
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

        for (String standardField : RecipientStandardField.getAllRecipientStandardFieldColumnNames()) {
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
            return recipientFieldService.getRecipientField(companyId, shortName) != null;
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
            return recipientFieldService.checkAllowedDefaultValue(companyId, fieldName, defaultValue);
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
    public boolean mayAddNewColumn(int companyId) throws Exception {
        return recipientFieldService.mayAddNewRecipientField(companyId);
    }

    @Override
    public boolean notContainsInDb(int companyId, String fieldName) {
        try {
            return recipientFieldService.getRecipientField(companyId, fieldName) == null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNotAllowedNumberOfEntries(int companyId) {
        int numberOfEntries = recipientFieldService.countCustomerEntries(companyId);

        return numberOfEntries > configService.getIntegerValue(ConfigValue.MaximumNumberOfEntriesForDefaultValueChange, companyId);
    }

    @Override
    public boolean hasTargetGroups(int companyId, String fieldName) {
        List<TargetLight> targetLights = targetService.listTargetGroupsUsingProfileFieldByDatabaseName(fieldName, companyId);

        return !targetLights.isEmpty();
    }

    @Override
    public boolean isValidToDelete(String fieldName, int companyId, Locale locale, Popups popups) {
        if (notContainsInDb(companyId, fieldName)) {
            popups.alert("error.profiledb.NotExists", fieldName);
        }
        
        if (hasNotAllowedNumberOfEntries(companyId)) {
            popups.alert("error.profiledb.delete.tooMuchRecipients", fieldName);
        }
        
        if (isStandardColumn(fieldName)) {
            popups.alert("error.profiledb.cannotDropColumn", fieldName);
        }
        
        List<Integer> importProfileIds = importProfileDao.getImportsContainingProfileField(companyId, fieldName);
        if (importProfileIds != null) {
        	for (int importProfileID : importProfileIds) {
        		ImportProfile importProfile = importProfileDao.getImportProfileById(importProfileID);
        		popups.alert("error.profiledb.import.used", fieldName, importProfile.getName(), importProfileID);
        	}
        }
        
        List<Integer> exportProfileIds = exportPredefDao.getExportsContainingProfileField(companyId, fieldName);
        if (exportProfileIds != null) {
        	for (int exportProfileID : exportProfileIds) {
        		ExportPredef exportProfile = exportPredefDao.get(exportProfileID, companyId);
        		popups.alert("error.profiledb.export.used", fieldName, exportProfile.getShortname(), exportProfileID);
        	}
        }
        
        checkObjectUsages(fieldName, companyId, locale, popups);
        return !popups.hasAlertPopups();
    }

    private void checkObjectUsages(String fieldName, int companyId, Locale locale, Popups popups) {
        ObjectUsages usages = objectUsageService.listUsageOfProfileFieldByDatabaseName(companyId, fieldName);
        if (usages.isEmpty()) {
            return;
        }
        ObjectUsagesToPopups.objectUsagesToPopups(
                "error.profilefield.used", "error.profilefield.used.withMore",
                usages, popups, locale);
    }

    @Override
    public boolean isStandardColumn(String fieldName) {
        return RecipientStandardField.getAllRecipientStandardFieldColumnNames().contains(fieldName);
    }
    
    @Override
    public ServiceResult<Object> validateNewProfileFieldValue(Admin admin, String fieldName, SimpleDataType newSimpleDataType, String newValue, boolean clearThisField) {
        Locale locale = admin.getLocale();

        RecipientFieldDescription profileField = null;
        try {
            profileField = recipientFieldService.getRecipientField(admin.getCompanyID(), fieldName);
        } catch (Exception e) {
            logger.error(format("Could find field by column name {0}", fieldName), e);
        }
        
        Message message = null;
        Object value = null;

        if (profileField == null) {
            message = getMessageWithFieldName(fieldName, "error.profiledb.notExists", locale);
        } else if (!profileField.isNullable() && clearThisField) {
            message = getMessageWithFieldName(fieldName, "error.profiledb.empty", locale);
        } else if (!isAllowedNewValue(profileField.getAllowedValues(), newValue)) {
            message = getMessageWithFieldName(fieldName, "error.profiledb.invalidFixedValue", locale);
        } else if (clearThisField && StringUtils.isNotBlank(newValue)) {
            message = Message.of("error.bulkAction.empty.clear", fieldName);
        } else if (StringUtils.isBlank(newValue)) {
            value = "";
        } else {
            switch (newSimpleDataType) {
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
                    if (StringUtils.length(newValue) > profileField.getCharacterLength()) {
                        message = getMessageWithFieldName(fieldName, "error.contentLengthExceedsLimit", locale, profileField.getCharacterLength());
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
    
    private boolean isAllowedNewValue(List<String> allowedValues, String newValue) {
        return StringUtils.isBlank(newValue) || allowedValues == null || allowedValues.contains(newValue);
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
            List<RecipientFieldDescription> profileFields = recipientFieldService.getRecipientFields(companyId);

            for (RecipientFieldDescription field : profileFields) {
                boolean isColumnEqualsFieldName = StringUtils.equalsIgnoreCase(field.getColumnName(), fieldName);
                boolean isColumnEqualsShortname = StringUtils.equalsIgnoreCase(field.getColumnName(), shortName);
                boolean isDbShortnameEqualsShortname = StringUtils.equalsIgnoreCase(field.getShortName(), shortName);

                if (!isColumnEqualsFieldName && (isColumnEqualsShortname || isDbShortnameEqualsShortname)) {
                    return false;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

	@Override
	public List<Integer> getImportsContainingProfileField(int companyId, String fieldName) {
	    return importProfileDao.getImportsContainingProfileField(companyId, fieldName);
	}

	@Override
	public List<Integer> getExportsContainingProfileField(int companyId, String fieldName) {
		return exportPredefDao.getExportsContainingProfileField(companyId, fieldName);
	}

	@Override
	public ImportProfile getImportProfile(int importProfileID) {
		return importProfileDao.getImportProfileById(importProfileID);
	}

	@Override
	public ExportPredef getExportProfile(int companyID, int exportProfileID) {
		return exportPredefDao.get(exportProfileID, companyID);
	}
}
