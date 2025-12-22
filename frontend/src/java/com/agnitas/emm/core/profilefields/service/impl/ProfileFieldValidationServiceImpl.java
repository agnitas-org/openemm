/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ExportPredef;
import com.agnitas.beans.ImportProfile;
import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.export.dao.ExportPredefDao;
import com.agnitas.emm.core.import_profile.dao.ImportProfileDao;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.objectusage.service.ObjectUsageService;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.DbColumnType.SimpleDataType;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.KeywordList;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service("ProfileFieldValidationService")
public class ProfileFieldValidationServiceImpl implements ProfileFieldValidationService {
    
    private static final int MAX_VARCHAR_LENGTH = 4000;

    private final KeywordList databaseKeywordList;
    private final RecipientFieldService recipientFieldService;
    private final ConfigService configService;
    private final ObjectUsageService objectUsageService;
    private final ImportProfileDao importProfileDao;
    private final ExportPredefDao exportPredefDao;
    private final EmmActionService emmActionService;

    public ProfileFieldValidationServiceImpl(KeywordList databaseKeywordList, RecipientFieldService recipientFieldService, ConfigService configService,
                                             ObjectUsageService objectUsageService, ImportProfileDao importProfileDao, ExportPredefDao exportPredefDao,
                                             EmmActionService emmActionService) {
        this.databaseKeywordList = databaseKeywordList;
        this.recipientFieldService = recipientFieldService;
        this.configService = configService;
        this.objectUsageService = objectUsageService;
        this.importProfileDao = importProfileDao;
        this.exportPredefDao = exportPredefDao;
        this.emmActionService = emmActionService;
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
        return recipientFieldService.getRecipientField(companyId, shortName) != null;
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
        return recipientFieldService.checkAllowedDefaultValue(companyId, fieldName, defaultValue);
    }

    @Override
    public boolean isInvalidVarcharField(String fieldType, long fieldLength) {
        return DbColumnType.GENERIC_TYPE_VARCHAR.equals(fieldType)
                && (fieldLength < 0 || fieldLength > MAX_VARCHAR_LENGTH);
    }

    @Override
    public boolean mayAddNewColumn(int companyId) {
        return recipientFieldService.mayAddNewRecipientField(companyId);
    }

    private boolean notContainsInDb(int companyId, String fieldName) {
        return recipientFieldService.getRecipientField(companyId, fieldName) == null;
    }

    protected boolean hasNotAllowedNumberOfEntries(int companyId) {
        int numberOfEntries = recipientFieldService.countCustomerEntries(companyId);

        return numberOfEntries > configService.getIntegerValue(ConfigValue.MaximumNumberOfEntriesForDefaultValueChange, companyId);
    }

    @Override
    public SimpleServiceResult isValidToDelete(String fieldName, Admin admin) {
        final Set<Message> errors = new HashSet<>();
        final int companyId = admin.getCompanyID();

        if (notContainsInDb(companyId, fieldName)) {
            errors.add(Message.of("error.profiledb.NotExists", fieldName));
        }
        
        if (hasNotAllowedNumberOfEntries(companyId)) {
            errors.add(Message.of("error.profiledb.delete.tooMuchRecipients", fieldName));
        }
        
        if (isStandardColumn(fieldName)) {
            errors.add(Message.of("error.profiledb.cannotDropColumn", fieldName));
        }

        for (int importProfileID : importProfileDao.getImportsContainingProfileField(companyId, fieldName)) {
            ImportProfile importProfile = importProfileDao.getImportProfileById(importProfileID);
            errors.add(Message.of("error.profiledb.import.used", fieldName, importProfile.getName(), importProfileID));
        }

        for (int exportProfileID : exportPredefDao.getExportsContainingProfileField(companyId, fieldName)) {
            ExportPredef exportProfile = exportPredefDao.get(exportProfileID, companyId);
            errors.add(Message.of("error.profiledb.export.used", fieldName, exportProfile.getShortname(), exportProfileID));
        }

        checkObjectUsages(fieldName, companyId, admin.getLocale(), errors);
        return new SimpleServiceResult(errors.isEmpty(), errors);
    }

    private void checkObjectUsages(String fieldName, int companyId, Locale locale, Set<Message> errors) {
        ObjectUsages usages = objectUsageService.listUsageOfProfileFieldByDatabaseName(companyId, fieldName);
        if (usages.isEmpty()) {
            return;
        }

        errors.add(usages.toMessage("error.profilefield.used", "error.profilefield.used.withMore", locale));
    }

    protected boolean isStandardColumn(String fieldName) {
        return RecipientStandardField.getAllRecipientStandardFieldColumnNames().contains(fieldName);
    }
    
    @Override
    public ServiceResult<Object> validateNewProfileFieldValue(Admin admin, String fieldName, SimpleDataType newSimpleDataType, String newValue, boolean clearThisField) {
        Locale locale = admin.getLocale();

        RecipientFieldDescription profileField = recipientFieldService.getRecipientField(admin.getCompanyID(), fieldName);

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
        List<RecipientFieldDescription> profileFields = recipientFieldService.getRecipientFields(companyId);

        for (RecipientFieldDescription field : profileFields) {
            boolean isColumnEqualsFieldName = StringUtils.equalsIgnoreCase(field.getColumnName(), fieldName);
            boolean isColumnEqualsShortname = StringUtils.equalsIgnoreCase(field.getColumnName(), shortName);
            boolean isDbShortnameEqualsShortname = StringUtils.equalsIgnoreCase(field.getShortName(), shortName);

            if (!isColumnEqualsFieldName && (isColumnEqualsShortname || isDbShortnameEqualsShortname)) {
                return false;
            }
        }

        return true;
    }

	@Override
	public List<ImportProfile> getImportsContainingProfileField(int companyId, String fieldName) {
	    return importProfileDao.getImportsContainingProfileField(companyId, fieldName)
                .stream()
                .map(importProfileDao::getImportProfileById)
                .toList();
	}

	@Override
	public List<ExportPredef> getExportsContainingProfileField(int companyId, String fieldName) {
		return exportPredefDao.getExportsContainingProfileField(companyId, fieldName)
                .stream()
                .map(profileId -> exportPredefDao.get(profileId, companyId))
                .toList();
	}

    @Override
    public List<EmmAction> getDependentActions(String fieldName, int companyId) {
        return emmActionService.findActionsUsingProfileField(fieldName, companyId)
                .stream()
                .map(id -> emmActionService.getEmmAction(id, companyId))
                .toList();
    }
}
