/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient;

import static java.util.Map.Entry.comparingByKey;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.agnitas.emm.core.recipient.dto.RecipientLightDto;
import org.agnitas.emm.core.recipient.dto.RecipientOverviewWebStorageEntry;
import org.agnitas.service.WebStorage;
import org.agnitas.service.WebStorageBundle;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ProfileField;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.recipient.forms.RecipientListBaseForm;
import com.agnitas.util.MapUtils;

public class RecipientUtils {
    
    private static final Logger logger = LogManager.getLogger(RecipientUtils.class);
    
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    
    public static final String COLUMN_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_FIRSTNAME = "firstname";
    public static final String COLUMN_LASTNAME = "lastname";
    public static final String COLUMN_EMAIL = "email";
    
    public static final String COLUMN_GENDER = "gender";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_MAILTYPE = "mailtype";
    public static final String COLUMN_DATASOURCE_ID = "datasource_id";
    public static final String COLUMN_LATEST_DATASOURCE_ID = "latest_datasource_id";

    public static final String COLUMN_FREQUENCY_COUNT_DAY = "freq_count_day";
    public static final String COLUMN_FREQUENCY_COUNTER_WEEK = "freq_count_week";
    public static final String COLUMN_FREQUENCY_COUNT_MONTH = "freq_count_month";

    public static final String COLUMN_DO_NOT_TRACK = "sys_tracking_veto";
	public static final String COLUMN_ENCRYPTED_SENDING = "sys_encrypted_sending";

    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_CREATION_DATE = "creation_date";
    public static final String COLUMN_CLEANED_DATE = "cleaned_date";
    public static final String COLUMN_LASTOPEN_DATE = "lastopen_date";
    public static final String COLUMN_LASTCLICK_DATE = "lastclick_date";
    public static final String COLUMN_LASTSEND_DATE = "lastsend_date";
    
    public static int COLUMN_FIRST_ORDER = 1;
    public static int COLUMN_SECOND_ORDER = 2;
    public static int COLUMN_THIRD_ORDER = 3;
    public static int COLUMN_OTHER_ORDER = 4;
    
    public static final int MAX_SELECTED_FIELDS_COUNT = 8;

    public static final List<String> MAIN_COLUMNS = Arrays.asList(
            COLUMN_CUSTOMER_ID,
            COLUMN_EMAIL,
            COLUMN_TITLE,
            COLUMN_GENDER,
            COLUMN_MAILTYPE,
            COLUMN_FIRSTNAME,
            COLUMN_LASTNAME,
            COLUMN_TIMESTAMP,
            COLUMN_CLEANED_DATE,

            COLUMN_LATEST_DATASOURCE_ID,
            COLUMN_DATASOURCE_ID,

            COLUMN_DO_NOT_TRACK,
            ComCompanyDaoImpl.STANDARD_FIELD_BOUNCELOAD,
            COLUMN_ENCRYPTED_SENDING,

            COLUMN_FREQUENCY_COUNT_DAY,
            COLUMN_FREQUENCY_COUNTER_WEEK,
            COLUMN_FREQUENCY_COUNT_MONTH
    );

    
    public static String getRecipientDescription(Map<String, Object> data) {
        final int customerId = NumberUtils.toInt((String) data.get(COLUMN_CUSTOMER_ID), 0);
        final String firstName = (String) data.get(COLUMN_FIRSTNAME);
        final String lastName = (String) data.get(COLUMN_LASTNAME);
        final String email = (String) data.get(COLUMN_EMAIL);
        return RecipientUtils.getRecipientDescription(customerId, firstName, lastName, email);
    }
    
    public static String getRecipientDescription(RecipientLightDto recipientDto) {
        return getRecipientDescription(recipientDto.getCustomerId(), recipientDto.getFirstname(), recipientDto.getLastname(), recipientDto.getEmail());
    }
    
    public static String getRecipientDescription(int id, String firstName, String lastName, String email) {
        return getRecipientDescription(id, StringUtils.trim(firstName + " " + lastName), email);
    }

    public static String getRecipientDescription(int id, String fullName, String email) {
        fullName = StringUtils.trim(fullName);
        if (StringUtils.isNotEmpty(fullName)) {
            return fullName + " (" + id + ")";
		} else {
			return email + " (" + id + ")";
		}
	}
 
	/**
     *  If string length is more than 500 characters cut it and add "..." in the end.
     *
     * @param string recipient type letter
     * @return cut string
     */
    public static String cutRecipientDescription(String string){
        try {
            return StringUtils.abbreviate(string, MAX_DESCRIPTION_LENGTH);
        } catch (IllegalArgumentException e) {
            logger.error("RecipientUtils.cutRecipientDescription: " + e, e);
            return string;
        }
    }
    
    public static Comparator<Map.Entry<String, String>> getFieldOrderComparator() {
        return getFieldOrderComparator(false);
    }
    
    public static Comparator<Map.Entry<String, String>> getFieldOrderComparator(boolean isDuplicateList) {
        return (o1, o2) -> {
            //Some columns should always be first
            int firstOrder = columnOrder(o1.getKey(), isDuplicateList);
            int secondOrder = columnOrder(o2.getKey(), isDuplicateList);
    
            if (firstOrder == COLUMN_OTHER_ORDER && secondOrder == COLUMN_OTHER_ORDER) {
                return o1.getValue().compareToIgnoreCase(o2.getValue());
            }
    
            return firstOrder - secondOrder;
        };
    }
    
    public static Comparator<String> getCsvColumnComparator(boolean duplicateList) {
        return (o1, o2) -> {
            //Some columns should always be first
            int firstOrder = columnOrder(o1, duplicateList);
            int secondOrder = columnOrder(o2, duplicateList);
    
            if (firstOrder == COLUMN_OTHER_ORDER && secondOrder == COLUMN_OTHER_ORDER) {
                return o1.compareToIgnoreCase(o2);
            }
    
            return firstOrder - secondOrder;
        };
    }
    
	public static int columnOrder(String columnName, boolean isDuplicateList){
        if (isDuplicateList) {
            if (COLUMN_CUSTOMER_ID.equals(columnName)) {
                return COLUMN_FIRST_ORDER;
            }
            if (COLUMN_EMAIL.equals(columnName)) {
                return COLUMN_SECOND_ORDER;
            }
            if (COLUMN_TIMESTAMP.equals(columnName)) {
                return COLUMN_THIRD_ORDER;
            }
        } else {
            if (COLUMN_GENDER.equals(columnName)){
                return COLUMN_FIRST_ORDER;
            }
            if (COLUMN_FIRSTNAME.equals(columnName)){
                return COLUMN_SECOND_ORDER;
            }
            if (COLUMN_LASTNAME.equals(columnName)){
                return COLUMN_THIRD_ORDER;
            }
        }
        
        return COLUMN_OTHER_ORDER;
    }

    public static Map<String, String> getSortedFieldsMap(CaseInsensitiveMap<String, ProfileField> columnMap) {
		return getSortedFieldsMap(columnMap, false);
    }

    public static Map<String, String> getSortedFieldsMap(CaseInsensitiveMap<String, ProfileField> columnMap, boolean isDuplicateList) {
		final LinkedHashMap<String, String> fieldsMap = new LinkedHashMap<>();
		columnMap.forEach((key, value) -> fieldsMap.put(key, value.getShortname()));
		MapUtils.reorderLinkedHashMap(fieldsMap, RecipientUtils.getFieldOrderComparator(isDuplicateList));
		return fieldsMap;
    }
    
    public static Map<String, String> sortDuplicateAnalysisFields(CaseInsensitiveMap<String, ProfileField> columnMap) {
		return getSortedFieldsMap(columnMap, true);
    }

    public static Map<String, String> getFieldsForDuplicateAnalysis(Map<String, String> columnMap) {
        return columnMap.entrySet().stream()
                .sorted(comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, HashMap::new));
    }

    public static final String SUPPLEMENTAL_DATECOLUMN_SUFFIX_FORMAT = "_FORMAT";

    public static final String SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY = "_DAY_DATE";
    public static final String SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH = "_MONTH_DATE";
    public static final String SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR = "_YEAR_DATE";
    public static final String SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR = "_HOUR_DATE";
    public static final String SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE = "_MINUTE_DATE";
    public static final String SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND = "_SECOND_DATE";

    private static final List<String> SUPPLEMENTAL_SUFFIXES = Arrays.asList(
        SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY,
        SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH,
        SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR,
        SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR,
        SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE,
        SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND);

    public static String removeColumnSupplementalSuffix(String column) {
        for (String suffix : SUPPLEMENTAL_SUFFIXES) {
            if (StringUtils.upperCase(column).endsWith(suffix)) {
                return StringUtils.removeEndIgnoreCase(column, suffix);
            }
        }
        return column;
    }

    public static boolean hasSupplementalSuffix(String column) {
        for (String suffix : SUPPLEMENTAL_SUFFIXES) {
            if (StringUtils.upperCase(column).endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static String formatRecipientDateValue(ComAdmin admin, String value) throws Exception {
        if (DbUtilities.isNowKeyword(value)) {
            return LocalDate.now(AgnUtils.getZoneId(admin)).format(admin.getDateFormatter());
        }

        if (StringUtils.isNotEmpty(value)) {
            return formatRecipientDateValue(admin, parseUnknownDateFormat(value));
        }

        return "";
    }

    public static String formatRecipientDateTimeValue(ComAdmin admin, String value) throws Exception {
        if (DbUtilities.isNowKeyword(value)) {
            return LocalDateTime.now(AgnUtils.getZoneId(admin)).format(admin.getDateTimeFormatter());
        }

        if (StringUtils.isNotEmpty(value)) {
            return formatRecipientDateTimeValue(admin, parseUnknownDateFormat(value));
        }

        return "";
    }

    public static Date parseUnknownDateFormat(String value) throws Exception {
        Date dateValue = null;
        try {
            dateValue = new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT).parse(value);
        } catch (Exception e1) {
            dateValue = DateUtilities.parseUnknownDateFormat(value);
        }
        return dateValue;
    }

    public static String formatRecipientDateValue(ComAdmin admin, Date dateValue) {
        return StringUtils.defaultString(DateUtilities.format(dateValue, admin.getDateFormat()));
    }

    public static String formatRecipientDateTimeValue(ComAdmin admin, Date dateTimeValue) {
        return StringUtils.defaultString(DateUtilities.format(dateTimeValue, admin.getDateTimeFormat()));
    }

    public static String formatRecipientDoubleValue(ComAdmin admin, String value) {
        return formatRecipientDoubleValue(admin, NumberUtils.toDouble(value));
    }

    public static String formatRecipientDoubleValue(ComAdmin admin, double value) {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(admin.getLocale());
        DecimalFormat floatFormat = new DecimalFormat("###0.###", decimalFormatSymbols);
        return floatFormat.format(value);
    }

    public static String getSalutationByGenderId(int gender) {
        switch (gender) {
            case 0:
                return "Mr.";
            case 1:
                return "Mrs.";
            case 2:
                return "Unknown";
            case 3:
                return "Miss";
            case 4:
                return "Practice";
            case 5:
                return "Company";
            default:
                return "not set";
        }
    }

    public static void syncSelectedFields(WebStorage webStorage, WebStorageBundle<RecipientOverviewWebStorageEntry> bundle, RecipientListBaseForm form) {
        webStorage.access(bundle, storage -> {
            int columnsCount = form.getSelectedFields().size();
            if (columnsCount < 1 || columnsCount > MAX_SELECTED_FIELDS_COUNT) {
				form.setSelectedFields(storage.getSelectedFields());
            } else {
				storage.setSelectedFields(form.getSelectedFields());
            }
        });
    }
}
