/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.util;

import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportDateRangedSettings.DATE_RANGE_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportDateRangedSettings.DATE_RANGE_PREDEFINED;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.MAILING_ACTION_BASED;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.MAILING_DATE_BASED;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.MAILING_FOLLOW_UP;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.MAILING_GENERAL_TYPES_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.MAILING_INTERVAL_BASED;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.MAILING_NORMAL;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.PERIOD_TYPE_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.ENABLED_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.EXPRESSION_SEPARATOR;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILINGLISTS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILING_FILTER_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILING_TYPE_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.PREDEFINED_ID_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.PREDEFINED_MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.TARGETS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.TARGET_GROUPS_KEY;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.ACTIVATE_LINK_STATISTICS;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.ACTIVITY_ANALYSIS;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.CLICKERS_AFTER_DEVICE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.CLICKER_DEVICES;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.CLICKING_ANONYM;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.CLICKING_RECIPIENT;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.CONVERSION_RATE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.DEVELOPMENT_DETAILED;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.DEVELOPMENT_NET;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.DOI;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.FORMAT_TYPE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.HARD_BOUNCES;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.HTML;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OFFLINE_HTML;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OPENERES_TOTAL;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OPENERS;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OPENERS_AFTER_DEVICE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OPENERS_INVISIBLE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OPENERS_MEASURED;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OPENER_DEVICES;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OPENING_ANONYM;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.RECIPIENT_STATUS;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.SENT_MAILS;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.SIGNED_OFF;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.SOFT_BOUNCES;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.TEXT;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportDateRangedSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportRecipientSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.PeriodType;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.messages.I18nString;

public class BirtReportSettingsUtils {
    
    public static final String WITHOUT_GROUP = "";
    public static final String GENERAL_GROUP = "General";
    public static final String OPENER_GROUP = "statistic.opener";
    public static final String SENDING_OPENER_GROUP = "statistic.sending.opener";
    public static final String DEVICES_GROUP = "statistic.devices";
    public static final String FORMATS_GROUP = "statistic.formats";
    
    public static final int MAILINGS_PREDEFINED = 1;
    public static final int MAILINGS_CUSTOM = 2;
    public static final int MAILINGS_BASED = 3;
    
    public static final int MAILINGS_GENERAL_NORMAL = 3;
    
    public static final int COMPARISON_PREDEFINED_PERIOD = 0;
    
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "stopDate";
    
    private static final int MAX_TARGET_GROUPS_FOR_RECIPIENTS = 10;
    private static final int MAX_TARGET_GROUPS = 5;

    public static final int ACTION_AND_DATEBASED_MAILING_MAX_COUNT = 10;

    public static final String REPORT_DATE_FORMAT = DateUtilities.YYYY_MM_DD;
    public static final String REPORT_DATE_FORMAT_FOR_DAY = DateUtilities.YYYY_MM_DD_HH_MM;
    
    public static final List<BirtReportSettingsUtils.Properties> COMPARISON_GENERAL_GROUP = Arrays.asList(CLICKING_RECIPIENT, CLICKING_ANONYM, HARD_BOUNCES, SIGNED_OFF);
    public static final List<BirtReportSettingsUtils.Properties> COMPARISON_OPENER_GROUP = Arrays.asList(OPENERES_TOTAL, OPENERS_MEASURED, OPENERS_INVISIBLE, OPENING_ANONYM);
    public static final List<BirtReportSettingsUtils.Properties> COMPARISON_DEVICES_GROUP = Arrays.asList(OPENERS_AFTER_DEVICE, CLICKERS_AFTER_DEVICE);
    public static final List<BirtReportSettingsUtils.Properties> COMPARISON_FORMATS_GROUP = Arrays.asList(HTML, TEXT, OFFLINE_HTML);
    
    public static final List<BirtReportSettingsUtils.Properties> MAILING_FORMATS_GROUP = Arrays.asList(HTML, TEXT, OFFLINE_HTML);
    public static final List<BirtReportSettingsUtils.Properties> MAILING_GENERAL_GROUP = Arrays.asList(CLICKING_RECIPIENT, CLICKING_ANONYM, SIGNED_OFF, SOFT_BOUNCES, HARD_BOUNCES, CONVERSION_RATE);
    public static final List<BirtReportSettingsUtils.Properties> MAILING_OPENER_GROUP = Arrays.asList(OPENERS_MEASURED, OPENERS_INVISIBLE, OPENERES_TOTAL, OPENING_ANONYM);
    public static final List<BirtReportSettingsUtils.Properties> MAILING_DEVICES_GROUP = Arrays.asList(OPENERS_AFTER_DEVICE, CLICKERS_AFTER_DEVICE, OPENER_DEVICES, CLICKER_DEVICES, ACTIVATE_LINK_STATISTICS);
    
    public static final List<BirtReportSettingsUtils.Properties> RECIPIENT_WITHOUT_GROUP = Arrays.asList(RECIPIENT_STATUS, DEVELOPMENT_DETAILED, DEVELOPMENT_NET, FORMAT_TYPE, ACTIVITY_ANALYSIS, DOI);
    public static final List<BirtReportSettingsUtils.Properties> RECIPIENT_ANALYSIS_GROUP = Arrays.asList(OPENERS_MEASURED, CLICKING_RECIPIENT, CLICKERS_AFTER_DEVICE);
    
    public static final List<BirtReportSettingsUtils.Properties> TOP_DOMAIN_WITHOUT_GROUP = Arrays.asList(SENT_MAILS, HARD_BOUNCES, SOFT_BOUNCES, OPENERS, CLICKING_RECIPIENT);
    
    public static List<String> getSettingsPropertyList(Map<String, Object> properties, String propertyName) {
        List<String> result = new ArrayList<>();
        if(properties == null || properties.isEmpty()) {
            return  result;
        }
    
        Object value = properties.get(propertyName);
        if(value != null) {
            if (value instanceof String) {
                result.add((String) value);
            } else if(value instanceof String[]){
                result.addAll(Arrays.asList((String[]) value));
            } else if(value instanceof Collection) {
                Collection<?> objects = (Collection<?>) value;
                for (Object obj : objects) {
                    if (obj instanceof String) {
                        result.add((String) obj);
                    }
                    if (obj instanceof Integer) {
                        result.add(String.valueOf(obj));
                    }
                }
            }
        }
        
        return result;
    }
    
    public static boolean isDateRangedType(ReportSettingsType type, boolean isMailTrackingEnabled) {
        if (type.isDateRanged()) {
            if (type.isMailTrackingRequired()) {
                return isMailTrackingEnabled;
            }
            return true;
        } else {
        	return false;
        }
    }
    
    public static String getSettingsProperty(Map<String, Object> properties, String propertyName) {
        return Optional.ofNullable(properties.get(propertyName)).map(Object::toString).orElse("");
    }
    
    public static int getIntProperty(Map<String, Object> properties, String propertyName) {
        return NumberUtils.toInt(getSettingsProperty(properties, propertyName), 0);
    }

    public static boolean getBooleanProperty(Map<String, Object> properties, String propertyName) {
        if(properties == null || properties.isEmpty()) {
            return false;
        }
    
        Object o = properties.getOrDefault(propertyName, new Object());
        String os = o.toString();
        
        return BooleanUtils.toBoolean(os);
    }
    
    public static SimpleDateFormat getReportDateFormatLocalized(Admin admin) {
        return new SimpleDateFormat(DateUtilities.YYYY_MM_DD, admin.getLocale());
    }
    
    public static Map<String, String> packProperties(ReportSettingsType type, Map<String, Object> settingsByType) {
        if(type == null || settingsByType == null || settingsByType.isEmpty()) {
            return Collections.emptyMap();
        }
    
        List<Properties> properties = new ArrayList<>();
        switch (type) {
            case COMPARISON:
                properties.addAll(COMPARISON_GENERAL_GROUP);
                properties.addAll(COMPARISON_OPENER_GROUP);
                properties.addAll(COMPARISON_DEVICES_GROUP);
                properties.addAll(COMPARISON_FORMATS_GROUP);
                break;
            case MAILING:
                properties.addAll(MAILING_FORMATS_GROUP);
                properties.addAll(MAILING_OPENER_GROUP);
                properties.addAll(MAILING_DEVICES_GROUP);
                properties.addAll(MAILING_GENERAL_GROUP);
                break;
            case RECIPIENT:
                properties.addAll(RECIPIENT_WITHOUT_GROUP);
                properties.addAll(RECIPIENT_ANALYSIS_GROUP);
                break;
            default:
                //nothing do
        }
        
        return properties.stream()
                .map(Properties::getPropName)
                .collect(Collectors.toMap(Function.identity(), prop -> String.valueOf(settingsByType.getOrDefault(prop, false))));
    }
    
    public static String convertListToString(Map<String, Object> parameters, String propertyName) {
        List<String> list = BirtReportSettingsUtils.getSettingsPropertyList(parameters, propertyName);
        if(CollectionUtils.isEmpty(list)) {
            return " ";
        } else {
            return StringUtils.join(list, EXPRESSION_SEPARATOR);
        }
    }
    
    public static Set<String> getMissingProperties(Admin admin, ReportSettingsType type, Map<String, Object> settings) {
        Set<String> missedProperties = new HashSet<>();
        if (settingsRequireMailings(admin, type)
                && getSettingsPropertyList(settings, MAILINGS_KEY).isEmpty()) {
            missedProperties.add(MAILINGS_KEY);
        }
        
        if (settingsRequireMailinglists(admin, type)
                && getSettingsPropertyList(settings, MAILINGLISTS_KEY).isEmpty()) {
            missedProperties.add(MAILINGLISTS_KEY);
        }
        
        if (settingsRequirePredefinedValue(type, settings) && getIntProperty(settings, PREDEFINED_ID_KEY) == 0) {
            missedProperties.add(PREDEFINED_ID_KEY);
        }

        if (type == ReportSettingsType.COMPARISON) {
            int mailingTypeValue = NumberUtils.toInt(getSettingsProperty(settings, MAILING_TYPE_KEY));
        
            if (mailingTypeValue != MAILINGS_PREDEFINED && mailingTypeValue != MAILINGS_CUSTOM) {
                List<String> targetGroups = getSettingsPropertyList(settings, TARGETS_KEY);
                Set<Integer> values = targetGroups.stream()
                        .filter(StringUtils::isNotEmpty)
                        .map(NumberUtils::toInt).filter(v -> v != 0)
                        .collect(Collectors.toSet());
                if (values.isEmpty()) {
                    missedProperties.add(TARGET_GROUPS_KEY);
                }
            }
        }

        if (type == ReportSettingsType.MAILING
                && !StringUtils.equals(String.valueOf(MAILING_NORMAL),
                BirtReportSettingsUtils.getSettingsProperty(settings, MAILING_GENERAL_TYPES_KEY))) {
            if (settings.get(START_DATE) == null) {
                missedProperties.add(START_DATE);
            }

            if (settings.get(END_DATE) == null) {
                missedProperties.add(END_DATE);
            }
        }
        
        return missedProperties;
    }
    
    private static boolean settingsRequirePredefinedValue(ReportSettingsType type, Map<String, Object> settings) {
        if (type == ReportSettingsType.COMPARISON || type == ReportSettingsType.MAILING) {
            return getIntProperty(settings, MAILING_FILTER_KEY) > 0;
        }
        return false;
    }

    private static boolean settingsRequireMailinglists(Admin admin, ReportSettingsType type) {
        return isDateRangedType(type, AgnUtils.isMailTrackingAvailable(admin));
    }

    private static boolean settingsRequireMailings(Admin admin, ReportSettingsType type) {
        return !isDateRangedType(type, AgnUtils.isMailTrackingAvailable(admin));
    }
    
    public static Map<ReportSettingsType, Map<String, Object>> getDefaultSettings() {
        Map<ReportSettingsType, Map<String, Object>> settingsMap = new HashMap<>();
    
        Map<String, Object> settings = new HashMap<>();
        settings.put(ENABLED_KEY, false);
        settings.put(MAILING_TYPE_KEY, MAILINGS_PREDEFINED);
        settings.put(MAILINGS_KEY, Collections.emptyList());
        settingsMap.put(ReportSettingsType.COMPARISON, settings);
    
        settings = new HashMap<>();
        settings.put(ENABLED_KEY, false);
        settings.put(MAILINGS_KEY, Collections.emptyList());
        settings.put(MAILING_TYPE_KEY, MAILINGS_PREDEFINED);
        settings.put(MAILING_GENERAL_TYPES_KEY, MAILINGS_GENERAL_NORMAL);
        settingsMap.put(ReportSettingsType.MAILING, settings);
    
        settings = new HashMap<>();
        settings.put(ENABLED_KEY, false);
        settings.put(DATE_RANGE_KEY, DATE_RANGE_PREDEFINED);
        settingsMap.put(ReportSettingsType.RECIPIENT, settings);
        
        settings = new HashMap<>();
        settings.put(ENABLED_KEY, false);
        settings.put(DATE_RANGE_KEY, DATE_RANGE_PREDEFINED);
        settingsMap.put(ReportSettingsType.TOP_DOMAIN, settings);
    
        return settingsMap;
    }
    
    public static boolean updateDateRestrictions(ReportSettingsType type, Map<String, Object> settings) {
        switch (type) {
            case MAILING:
                return !equalParameter(settings, MAILING_NORMAL, MAILING_GENERAL_TYPES_KEY);
            case COMPARISON:
                return equalParameter(settings, MAILINGS_PREDEFINED, MAILING_TYPE_KEY) &&
                        equalParameter(settings, COMPARISON_PREDEFINED_PERIOD, PREDEFINED_MAILINGS_KEY);
            case RECIPIENT:
            case TOP_DOMAIN:
                return true;
            default:
                return false;
        }
    }
    
    private static boolean equalParameter(Map<String, Object> settings, int paramValue, String paramName) {
        return StringUtils.equals(String.valueOf(paramValue), BirtReportSettingsUtils.getSettingsProperty(settings, paramName));
    }
    
    public static boolean equalParameter(Object paramValue, Object expectedValue) {
        return StringUtils.equals(String.valueOf(paramValue), String.valueOf(expectedValue));
    }
    
    public static void convertReportDatesIntoClientFormat(Admin admin, Map<ReportSettingsType, Map<String, Object>> settings) {
        for (ReportSettingsType type : ReportSettingsType.values()) {
        	convertReportDateIntoClientFormat(START_DATE, END_DATE, admin, settings.get(type));
        }
    }
    
    public static void convertReportDatesIntoBackendFormat( Admin admin, Map<ReportSettingsType, Map<String, Object>> settings) {
        for (ReportSettingsType type : ReportSettingsType.values()) {
            convertReportDateIntoBackendFormat(START_DATE, END_DATE, admin, settings.get(type));
        }
    }
    
    public static String getDayOfWeekPattern(Set<Map.Entry<Integer, Boolean>> weekDaysSettings) {
        StringBuilder pattern = new StringBuilder();
        for (Map.Entry<Integer, Boolean> entry : weekDaysSettings) {
            if (entry.getValue() != null && entry.getValue()) {
                switch (entry.getKey()) {
                    case Calendar.MONDAY:
                        pattern.append(DateUtilities.getWeekdayShortname(Calendar.MONDAY)); //Mo
                        break;
                    case Calendar.TUESDAY:
                        pattern.append(DateUtilities.getWeekdayShortname(Calendar.TUESDAY)); //Tu
                        break;
                    case Calendar.WEDNESDAY:
                        pattern.append(DateUtilities.getWeekdayShortname(Calendar.WEDNESDAY)); //We
                        break;
                    case Calendar.THURSDAY:
                        pattern.append(DateUtilities.getWeekdayShortname(Calendar.THURSDAY)); //Th
                        break;
                    case Calendar.FRIDAY:
                        pattern.append(DateUtilities.getWeekdayShortname(Calendar.FRIDAY)); //Fr
                        break;
                    case Calendar.SATURDAY:
                        pattern.append(DateUtilities.getWeekdayShortname(Calendar.SATURDAY)); //Sa
                        break;
                    case Calendar.SUNDAY:
                        pattern.append(DateUtilities.getWeekdayShortname(Calendar.SUNDAY)); //Su
                        break;
                    default:
                        break;
                }
            }
        }
        return pattern.toString();
    }
    
    public static boolean isWeekDayActive(String intervalPattern, int weekDay) {
        //important for birt report interval pattern to use only english locale
		return DateUtilities.isWeekDayActive(intervalPattern, weekDay, Locale.ENGLISH);
	}
    
    public static void convertReportDateIntoClientFormat(String startKey, String stopKey, Admin admin, Map<String, Object> settingsByType) {
        convertReportDate(startKey, stopKey, admin, settingsByType, true);
    }
    
    public static void convertReportDateIntoBackendFormat(String startKey, String stopKey, Admin admin, Map<String, Object> settingsByType) {
        convertReportDate(startKey, stopKey, admin, settingsByType, false);
    }
    
    private static void convertReportDate(String startKey, String stopKey, Admin admin, Map<String, Object> settingsByType, boolean convertIntoClientFormat) {
    	if (settingsByType != null) {
	        final DateTimeFormatter backendFormatter = DateTimeFormatter.ofPattern(REPORT_DATE_FORMAT);
	        final DateTimeFormatter userFormatter = admin.getDateFormatter();
	    
	        DateTimeFormatter fromFormatter = backendFormatter;
	        DateTimeFormatter intoFormatter = userFormatter;
	        if (!convertIntoClientFormat) {
	            fromFormatter = userFormatter;
	            intoFormatter = backendFormatter;
	        }
	        
	        String startDate = getSettingsProperty(settingsByType, startKey);
	        String stopDate = getSettingsProperty(settingsByType, stopKey);
	    
	        LocalDate localStartDate = DateUtilities.parseDate(startDate, fromFormatter);
	        startDate = DateUtilities.format(localStartDate, intoFormatter);
	
	        LocalDate localStopDate = DateUtilities.parseDate(stopDate, fromFormatter);
	        stopDate = DateUtilities.format(localStopDate, intoFormatter);
	
	        settingsByType.put(startKey, startDate);
	        settingsByType.put(stopKey, stopDate);
    	}
    }

    public static boolean validateComparisonDateRange(Map<String, Object> settings) {
        int mailingType = NumberUtils.toInt(getSettingsProperty(settings, MAILING_TYPE_KEY), -1);
        if(mailingType == 0) {
            return isPeriodTypeValid(settings);
        }
        return true;
    }
    
    private static boolean isPeriodTypeValid(Map<String, Object> settings) {
        PeriodType periodType = PeriodType.getTypeByStringKey(
                BirtReportSettingsUtils.getSettingsProperty(settings, PERIOD_TYPE_KEY));
    
        return  periodType == PeriodType.DATE_RANGE_DAY ||
                periodType == PeriodType.DATE_RANGE_WEEK ||
                periodType == PeriodType.DATE_RANGE_30DAYS ||
                periodType == PeriodType.DATE_RANGE_LAST_MONTH ||
                periodType == PeriodType.DATE_RANGE_CUSTOM;
    }
    
    public static boolean validateMailingsDateRange(Map<String, Object> settings) {
        int generalType = NumberUtils.toInt(getSettingsProperty(settings, MAILING_GENERAL_TYPES_KEY), -1);
        if (generalType == MAILING_DATE_BASED || generalType == MAILING_ACTION_BASED || generalType == MAILING_INTERVAL_BASED || generalType == MAILING_FOLLOW_UP) {
            return isPeriodTypeValid(settings);
        }
        
        return true;
    }
    
    public static boolean validateDateRangedSettings(Map<String, Object> settings) {
        int dateRange = NumberUtils.toInt(BirtReportSettingsUtils.getSettingsProperty(settings, DATE_RANGE_KEY));
        
        if (dateRange == DATE_RANGE_PREDEFINED) {
            int dateRangeType = NumberUtils.toInt(BirtReportSettingsUtils.getSettingsProperty(settings,
                    DATE_RANGE_PREDEFINED_KEY));
            return dateRangeType == ComBirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_WEEK ||
                    dateRangeType == ComBirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_30_DAYS ||
                    dateRangeType == ComBirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_LAST_MONTH ||
                    dateRangeType == ComBirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_THREE_MONTHS;
        }
        
        return dateRange == ComBirtReportRecipientSettings.DATE_RANGE_CUSTOM;
    }
    
    public static int getMaxTargetGroupNumber(ReportSettingsType settingsType) {
        if(settingsType == ReportSettingsType.RECIPIENT) {
            return MAX_TARGET_GROUPS_FOR_RECIPIENTS;
        }

        return MAX_TARGET_GROUPS;
    }
    
     public static List<Integer> convertStringToIntList(String value) {
        String targets = StringUtils.trimToEmpty(value);
    
        return Arrays.stream(targets.split(ComBirtReportSettings.EXPRESSION_SEPARATOR))
                .map(NumberUtils::toInt)
                .filter(v -> v != 0)
                .collect(Collectors.toList());
    }
    
    public static String getParameterTranslation(String parameter, Locale locale) {
        if (PREDEFINED_ID_KEY.equals(parameter)) {
            return I18nString.getLocaleString("report.mailing.filter", locale);
        }
    
        if (MAILINGLISTS_KEY.equals(parameter)) {
            return I18nString.getLocaleString("report.mailinglists", locale);
        }
        
        if (TARGET_GROUPS_KEY.equals(parameter)) {
            return I18nString.getLocaleString("Target-Groups", locale);
        }
        
        if (START_DATE.equals(parameter) || END_DATE.equals(parameter)) {
            return I18nString.getLocaleString("report.recipient.period", locale);
        }
        
        return parameter;
    }

    public static boolean isMailingSettings(ReportSettingsType activeSettingsType) {
        return activeSettingsType == ReportSettingsType.MAILING;
    }

    public static boolean isMailingActionBased(ReportSettingsType type, Map<String, Object> settings) {
        if (isMailingSettings(type)) {
            int subType = getIntProperty(settings, MAILING_GENERAL_TYPES_KEY);
            return subType == ComBirtReportMailingSettings.MAILING_ACTION_BASED;
        }

        return false;
    }

    public static boolean isMailingIntervalBased(ReportSettingsType type, Map<String, Object> settings) {
        if (isMailingSettings(type)) {
            int subType = getIntProperty(settings, MAILING_GENERAL_TYPES_KEY);
            return subType == ComBirtReportMailingSettings.MAILING_INTERVAL_BASED;
        }

        return false;
    }

    public static boolean isMailingFollowUp(ReportSettingsType type, Map<String, Object> settings) {
        if (isMailingSettings(type)) {
            int subType = getIntProperty(settings, MAILING_GENERAL_TYPES_KEY);
            return subType == ComBirtReportMailingSettings.MAILING_FOLLOW_UP;
        }

        return false;
    }

    public static boolean isMailingDateBased(ReportSettingsType type, Map<String, Object> settings) {
        if (isMailingSettings(type)) {
            int subType = getIntProperty(settings, MAILING_GENERAL_TYPES_KEY);
            return subType == ComBirtReportMailingSettings.MAILING_DATE_BASED;
        }

        return false;
    }


    public enum Properties {
        CLICKING_RECIPIENT("clickingRecipients", "statistic.clicker"),
        CLICKING_ANONYM("clickingAnonymous", "statistic.clicks.anonym"),
        SOFT_BOUNCES("softbounces", "report.softbounces"),
        HARD_BOUNCES("hardbounces", "statistic.bounces.hardbounce"),
        SIGNED_OFF("signedOff", "statistic.Opt_Outs"),
        CONVERSION_RATE("conversionRate", "statistic.revenue"),
        
        OPENERES_TOTAL("openersTotal", "report.openers.total"),
        OPENERS_MEASURED("openersMeasured", "report.openers.measured"),
        OPENERS_INVISIBLE("openersInvisible", "report.openers.invisible"),
        OPENING_ANONYM("openingsAnonymous", "statistic.openings.anonym"),
        
        OPENERS_AFTER_DEVICE("openersAfterDevice", "report.mailing.openersByDevices"),
        CLICKERS_AFTER_DEVICE("clickersAfterDevice", "report.mailing.clickersByDevices"),
        ACTIVATE_LINK_STATISTICS("activateLinkStatistics", "report.activate.link.statistics"),
        OPENER_DEVICES("openerDevices", "report.devices.opener"),
        CLICKER_DEVICES("clickerDevices", "report.devices.clicker"),

        HTML("html", "HTML"),
        TEXT("text", "report.text"),
        OFFLINE_HTML("offlineHtml", "report.offline.html"),
        FORMAT_TYPE("mailingType", "report.recipient.statistics.mailingType.label"),
        
        RECIPIENT_STATUS("recipientStatus", "report.recipient.statistics.recipientStatuses.label"),
        DEVELOPMENT_DETAILED("recipientDevelopmentDetailed", "report.recipient.statistics.recipientDevelopmentDetailed.label"),
        DEVELOPMENT_NET("recipientDevelopmentNet", "report.recipient.statistics.recipientDevelopmentNet.label"),
        ACTIVITY_ANALYSIS("activityAnalysis", "statistic.recipient.activity.analysis"),
        
        SENT_MAILS("sentMails", "statistic.mails.sent"),
        OPENERS("openers", "birt.report.opens"),
        DOI("recipientDOI", "recipient.DOI");

        private final String propName;
        private final String labelCode;
    
        Properties(String propName, String labelCode) {
            this.propName = propName;
            this.labelCode = labelCode;
        }
    
        public String getPropName() {
            return propName;
        }
    
        public String getLabelCode() {
            return labelCode;
        }
    }
}
