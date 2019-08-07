/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportRecipientSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.FilterType;
import com.agnitas.emm.core.birtreport.dto.PeriodType;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.messages.I18nString;
import org.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.MAILING_ACTION_BASED;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.MAILING_DATE_BASED;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.MAILING_GENERAL_TYPES_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.MAILING_NORMAL;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.PERIOD_TYPE_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportRecipientSettings.DATE_RANGE_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportRecipientSettings.DATE_RANGE_PREDEFINED;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportRecipientSettings.DATE_RANGE_PREDEFINED_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportRecipientSettings.DATE_RANGE_PREDEFINED_MONTH;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportRecipientSettings.DATE_RANGE_PREDEFINED_THREE_MONTHS;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportRecipientSettings.DATE_RANGE_PREDEFINED_WEEK;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportRecipientSettings.MAILING_LISTS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.ENABLED_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.EXPRESSION_SEPARATOR;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILINGLISTS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILING_TYPE_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.PREDEFINED_MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.TARGETS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.TARGET_GROUPS_KEY;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.ACTIVATE_LINK_STATISTICS;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.ACTIVITY_ANALYSIS;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.CLICKERS_AFTER_DEVICE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.CLICKING_ANONYM;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.CLICKING_RECIPIENT;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.CONVERSION_RATE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.DEVELOPMENT_DETAILED;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.DEVELOPMENT_NET;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.FORMAT_TYPE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.HARD_BOUNCES;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.HTML;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OFFLINE_HTML;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OPENERES_TOTAL;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OPENERS_AFTER_DEVICE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OPENERS_INVISIBLE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OPENERS_MEASURED;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.OPENING_ANONYM;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.RECIPIENT_STATUS;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.SIGNED_OFF;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.SOFT_BOUNCES;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.TEXT;
import static com.agnitas.emm.core.birtreport.dto.PeriodType.DATE_RANGE_CUSTOM;
import static com.agnitas.emm.core.birtreport.dto.PeriodType.DATE_RANGE_DAY;
import static com.agnitas.emm.core.birtreport.dto.PeriodType.DATE_RANGE_MONTH;
import static com.agnitas.emm.core.birtreport.dto.PeriodType.DATE_RANGE_WEEK;

public class BirtReportSettingsUtils {
    
    private static final Logger logger = Logger.getLogger(BirtReportSettingsUtils.class);
    
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
    
    public static final int FILTER_NO_FILTER_VALUE = FilterType.FILTER_NO_FILTER.getKey();
    public static final int FILTER_ARCHIVE_VALUE = FilterType.FILTER_ARCHIVE.getKey();
    public static final int FILTER_MAILINGLIST_VALUE = FilterType.FILTER_MAILINGLIST.getKey();
    public static final int FILTER_MAILING_VALUE = FilterType.FILTER_MAILING.getKey();
    
    public static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat(DateUtilities.YYYY_MM_DD);
    public static final SimpleDateFormat REPORT_DATE_FORMAT_FOR_DAY = new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM);
    
    public static final List<BirtReportSettingsUtils.Properties> COMPARISON_GENERAL_GROUP = Arrays.asList(CLICKING_RECIPIENT, CLICKING_ANONYM, HARD_BOUNCES, SIGNED_OFF);
    public static final List<BirtReportSettingsUtils.Properties> COMPARISON_OPENER_GROUP = Arrays.asList(OPENERES_TOTAL, OPENERS_MEASURED, OPENERS_INVISIBLE, OPENING_ANONYM);
    public static final List<BirtReportSettingsUtils.Properties> COMPARISON_DEVICES_GROUP = Arrays.asList(OPENERS_AFTER_DEVICE, CLICKERS_AFTER_DEVICE);
    public static final List<BirtReportSettingsUtils.Properties> COMPARISON_FORMATS_GROUP = Arrays.asList(HTML, TEXT, OFFLINE_HTML);
    
    public static final List<BirtReportSettingsUtils.Properties> MAILING_FORMATS_GROUP = Arrays.asList(HTML, TEXT, OFFLINE_HTML);
    public static final List<BirtReportSettingsUtils.Properties> MAILING_GENERAL_GROUP = Arrays.asList(CLICKING_RECIPIENT, CLICKING_ANONYM, SIGNED_OFF, SOFT_BOUNCES, HARD_BOUNCES, CONVERSION_RATE);
    public static final List<BirtReportSettingsUtils.Properties> MAILING_OPENER_GROUP = Arrays.asList(OPENERS_MEASURED, OPENERS_INVISIBLE, OPENERES_TOTAL, OPENING_ANONYM);
    public static final List<BirtReportSettingsUtils.Properties> MAILING_DEVICES_GROUP = Arrays.asList(OPENERS_AFTER_DEVICE, CLICKERS_AFTER_DEVICE, ACTIVATE_LINK_STATISTICS);
    
    public static final List<BirtReportSettingsUtils.Properties> RECIPIENT_WITHOUT_GROUP = Arrays.asList(RECIPIENT_STATUS, DEVELOPMENT_DETAILED, DEVELOPMENT_NET, FORMAT_TYPE, ACTIVITY_ANALYSIS);
    public static final List<BirtReportSettingsUtils.Properties> RECIPIENT_ANALYSIS_GROUP = Arrays.asList(OPENERS_MEASURED, CLICKING_RECIPIENT, CLICKERS_AFTER_DEVICE);
    
    @SuppressWarnings("unchecked")
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
                result.addAll((Collection<? extends String>) value);
            }
        }
        
        return result;
    }
    
    public static String getSettingsProperty(Map<String, Object> properties, String propertyName) {
        return Optional.ofNullable(properties.get(propertyName)).map(Object::toString).orElse("");
    }
    
    public static boolean getBooleanProperty(Map<String, Object> properties, String propertyName) {
        if(properties == null || properties.isEmpty()) {
            return false;
        }
    
        Object o = properties.getOrDefault(propertyName, new Object());
        String os = o.toString();
        
        return BooleanUtils.toBoolean(os);
    }
    
    public static SimpleDateFormat getReportDateFormatLocalized(ComAdmin admin) {
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
    
    public static Set<String> getMissingProperties(ReportSettingsType type, Map<String, Object> settings) {
        Set<String> missedProperties = new HashSet<>();
        if(type != ReportSettingsType.RECIPIENT && getSettingsPropertyList(settings, MAILINGS_KEY).isEmpty()) {
            missedProperties.add(MAILINGS_KEY);
        }
        
        if(type == ReportSettingsType.RECIPIENT && getSettingsPropertyList(settings, MAILINGLISTS_KEY).isEmpty()) {
            missedProperties.add(MAILING_LISTS_KEY);
        }
        
        if(type == ReportSettingsType.COMPARISON) {
            int mailingTypeValue = NumberUtils.toInt(getSettingsProperty(settings, MAILING_TYPE_KEY));
        
            if (mailingTypeValue != MAILINGS_PREDEFINED && mailingTypeValue != MAILINGS_CUSTOM) {
                List<String> targetGroups = getSettingsPropertyList(settings, TARGETS_KEY);
                Set<Integer> values = targetGroups.stream()
                        .filter(StringUtils::isNotEmpty)
                        .map(NumberUtils::toInt).filter(v -> v != 0)
                        .collect(Collectors.toSet());
                if(values.isEmpty()) {
                    missedProperties.add(TARGET_GROUPS_KEY);
                }
            }
        }
        
        if(type == ReportSettingsType.MAILING
                && !StringUtils.equals(String.valueOf(MAILING_NORMAL), BirtReportSettingsUtils.getSettingsProperty(settings, MAILING_GENERAL_TYPES_KEY))) {
            if(settings.get(START_DATE) == null) {
                missedProperties.add(START_DATE);
            }
            
            if(settings.get(END_DATE) == null) {
                missedProperties.add(END_DATE);
            }
        }
        
        return missedProperties;
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
    
        return settingsMap;
    }
    
    public static String getLocalizedReportName(ComBirtReportSettings reportSettings, Locale locale, String format) {
        ReportSettingsType type = reportSettings.getReportSettingsType();
        return getLocalizedReportName(type, locale, format);
    }
    
    public static String getLocalizedReportName(ReportSettingsType type, Locale locale, String format) {
        return String.format("%s.%s", I18nString.getLocaleString(type.getTypeMsgKey(), locale), format);
    }
    
    public static DateFormat getLocalDateFormat(Locale locale) {
        if (locale != null && Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
			return new SimpleDateFormat("yyyy-MM-dd");
		} else {
			return SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		}
    }
    
    public static boolean updateDateRestrictions(ReportSettingsType type, Map<String, Object> settings) {
        switch (type) {
            case MAILING:
                return !equalParameter(settings, MAILING_NORMAL, MAILING_GENERAL_TYPES_KEY);
            case COMPARISON:
                return equalParameter(settings, MAILINGS_PREDEFINED, MAILING_TYPE_KEY) &&
                        equalParameter(settings, COMPARISON_PREDEFINED_PERIOD, PREDEFINED_MAILINGS_KEY);
            case RECIPIENT:
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
    
    public static void convertReportDate(String startKey, String stopKey, SimpleDateFormat clientDateFormat, Map<String, Object> settingsByType, boolean convertIntoClientFormat) {
        SimpleDateFormat fromFormat = convertIntoClientFormat ? REPORT_DATE_FORMAT : clientDateFormat;
        SimpleDateFormat toFormat = convertIntoClientFormat ? clientDateFormat : REPORT_DATE_FORMAT;
    
        String startDate = convertDateFormats(fromFormat, toFormat, getSettingsProperty(settingsByType, startKey));
        settingsByType.put(startKey, startDate);
        String stopDate = convertDateFormats(fromFormat, toFormat, getSettingsProperty(settingsByType, stopKey));
        settingsByType.put(stopKey, stopDate);
    }
    
    private static String convertDateFormats(final SimpleDateFormat fromFormat, final SimpleDateFormat toFormat, String dateString) {
        String formattedDate = dateString;
        if (StringUtils.isNotEmpty(dateString)) {
            try {
                Date date = fromFormat.parse(dateString);
                formattedDate = toFormat.format(date);
            } catch (ParseException e) {
                logger.warn("convertDateFormats: exception while parsing client date", e);
            }
        }
        return formattedDate;
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
    
        return periodType == DATE_RANGE_WEEK ||
                periodType == DATE_RANGE_MONTH ||
                periodType == DATE_RANGE_CUSTOM ||
                periodType == DATE_RANGE_DAY;
    }
    
    public static boolean validateMailingsDateRange(Map<String, Object> settings) {
        int generalType = NumberUtils.toInt(getSettingsProperty(settings, MAILING_GENERAL_TYPES_KEY), -1);
        if(generalType == MAILING_DATE_BASED || generalType == MAILING_ACTION_BASED) {
            return isPeriodTypeValid(settings);
        }
        
        return true;
    }
    
    public static boolean validateRecipientDateRange(Map<String, Object> settings) {
        int dateRange = NumberUtils.toInt(BirtReportSettingsUtils.getSettingsProperty(settings, DATE_RANGE_KEY));
        
        if (dateRange == DATE_RANGE_PREDEFINED) {
            int dateRangeType = NumberUtils.toInt(BirtReportSettingsUtils.getSettingsProperty(settings,
                    DATE_RANGE_PREDEFINED_KEY));
            return dateRangeType == DATE_RANGE_PREDEFINED_WEEK ||
                    dateRangeType == DATE_RANGE_PREDEFINED_MONTH ||
                    dateRangeType == DATE_RANGE_PREDEFINED_THREE_MONTHS;
        }
        
        return dateRange == ComBirtReportRecipientSettings.DATE_RANGE_CUSTOM;
    }
    
    public static int getMaxTargetGroupNumber(ReportSettingsType settingsType) {
        if(settingsType == ReportSettingsType.RECIPIENT) {
            return MAX_TARGET_GROUPS_FOR_RECIPIENTS;
        }

        return MAX_TARGET_GROUPS;
    }
    
    public enum Properties {
        CLICKING_RECIPIENT("clickingRecipients", "statistic.TotalClickSubscribers.short"),
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
        
        HTML("html", "HTML"),
        TEXT("text", "report.text"),
        OFFLINE_HTML("offlineHtml", "report.offline.html"),
        FORMAT_TYPE("mailingType", "report.recipient.statistics.mailingType.label"),
        
        RECIPIENT_STATUS("recipientStatus", "report.recipient.statistics.recipientStatuses.label"),
        DEVELOPMENT_DETAILED("recipientDevelopmentDetailed", "report.recipient.statistics.recipientDevelopmentDetailed.label"),
        DEVELOPMENT_NET("recipientDevelopmentNet", "report.recipient.statistics.recipientDevelopmentNet.label"),
        ACTIVITY_ANALYSIS("activityAnalysis", "statistic.recipient.activity.analysis");
    
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
