/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;

public abstract class ComBirtReportSettings {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ComBirtReportSettings.class);
	
    public static final String DUMMY_LIST_FIELD = "00000";

    public static final String EXPRESSION_SEPARATOR = ",";
    public static final String TARGETS_KEY = "selectedTargets";
    public static final String MAILINGS_KEY = "selectedMailings";
    public static final String MAILINGLISTS_KEY = "selectedMailinglists";
    public static final String ENABLED_KEY = "enabled";
    public static final String SORT_BY_KEY = "sortBy";

    public static final int REPORT_SETTINGS_COMPARISON = 1;
    public static final int REPORT_SETTINGS_MAILING = 2;
    public static final int REPORT_SETTINGS_RECIPIENT = 3;

    /**
     * Predefine mailing contains ids only of
     * {@link com.agnitas.emm.core.birtreport.dto.FilterType#FILTER_ARCHIVE}
     *  or {@link com.agnitas.emm.core.birtreport.dto.FilterType#FILTER_MAILINGLIST}
     */
    public static final String PREDEFINED_ID_KEY = "predefineMailing";

    /**
     * Contains info about mailing filter types {@link com.agnitas.emm.core.birtreport.dto.FilterType} to define ids in predefineMailing
     */
    public static final String MAILING_FILTER_KEY = "mailingFilter";

    public static final String MAILING_TYPE_KEY = "mailingType";
    public static final String PREDEFINED_MAILINGS_KEY = "predefinedMailings";

	public static final String TARGET_GROUPS_KEY = "targetGroups";
	public static final String FIGURES_KEY = "figures";
    public static final String SORT_NAME = "name";
    public static final String SORT_DATE = "date";
    public static final String SORT_MAILINGS_KEY = "sortMailing";
    
    public static final String ARCHIVED_ID = "archiveId";
    
    public static final int MAILINGS_PREDEFINED = 1;
    
    protected Map<String, Object> settingsMap = new HashMap<>();

    public ComBirtReportSettings() {
        loadDefaults();
    }
    
    public void loadDefaults() {
        // nothing do
    }

    public Object getReportSetting(String key) {
        return settingsMap.get(key);
    }
    
    public String getReportSettingAsString(String key) {
        return getReportSettingAsString(key, "");
    }
    
    public String getReportSettingAsString(String key, String defaultValue) {
        return Optional.ofNullable(settingsMap.get(key)).map(Object::toString).orElse(defaultValue);
    }
    
    public int getReportSettingAsInt(String key) {
        return Optional.ofNullable(settingsMap.get(key)).map(Object::toString).map(NumberUtils::toInt).orElse(0);
    }
    
    public int getReportSettingAsInt(String key, int defaultValue) {
        return Optional.ofNullable(settingsMap.get(key)).map(Object::toString).map(s -> NumberUtils.toInt(s, defaultValue)).orElse(defaultValue);
    }
    
    public boolean getReportSettingAsBoolean(String key) {
        return Optional.ofNullable(settingsMap.get(key)).map(Object::toString).map(BooleanUtils::toBoolean).orElse(false);
    }
    
    protected List<String> getReportSettingAsList(String key) {
        String value = getReportSettingAsString(key);
        return ListUtils.emptyIfNull(parseExpression(value));
    }
    
    protected String generateExpression(List<String> idsList) {
        List<String> idList = new ArrayList<>();

        for(String id: parseExpressions(idsList)) {
            if (!StringUtils.equals(DUMMY_LIST_FIELD, id) &&
                    StringUtils.isNotEmpty(id) &&
                    NumberUtils.toInt(id) != 0) {
                idList.add(id);
            }
        }
    
        String expression = StringUtils.join(idList, EXPRESSION_SEPARATOR);
        return StringUtils.trimToEmpty(expression);
    }

    private List<String> parseExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return null;
        }
        
        return AgnUtils.splitAndTrimStringlist(expression);
    }
    
    private List<String> parseExpressions(List<String> expression) {
        List<String> result = new LinkedList<>();
        
        for(String expr: ListUtils.emptyIfNull(expression)) {
            List<String> parsed = parseExpression(expr);
            if(parsed != null) {
                result.addAll(parsed);
            }
        }
        return result;
    }
    
    protected List<Integer> parseExpressionAsInt(String expression) {
        return ListUtils.emptyIfNull(parseExpression(expression)).stream()
                .filter(StringUtils::isNotEmpty)
                .map(NumberUtils::toInt)
                .collect(Collectors.toList());
    }

    public void setReportSetting(String key, Object value) {
        if (value != null) {
            String strValue = value.toString();
            if (StringUtils.equals("on", strValue)) {
                settingsMap.put(key, Boolean.TRUE);
            } else if (StringUtils.isNotEmpty(strValue)) {
                settingsMap.put(key, strValue);
            }
        }
    }

    public boolean isEnabled() {
        return getReportSettingAsBoolean(ENABLED_KEY);
    }

    public List<String> getTargetGroups() {
        return parseExpression(getReportSettingAsString(TARGETS_KEY));
    }
    
    public Map<String, Object> getSettingsMap() {
        return settingsMap;
    }
    
    public void setSettingsMap(Map<String, Object> settingsMap) {
        this.settingsMap = settingsMap;
    }
    
    public int getTypeId() {
        return getReportSettingsType().getKey();
    }
    
    public abstract ReportSettingsType getReportSettingsType();
    
    public abstract String getReportName(String reportFormat);
    
    public abstract Map<String, String> getReportUrlParameters();

    /**
     * Get a collection of the names of missing report parameters that should not be omitted. Supposed to be overridden
     * by subclasses in order to provide a specific results for each report type.
     * @param intervalReportType an interval type of the Birt report
     * @return a set of missing parameters or an empty set (if report is ready for sending/evaluation)
     */
    public Set<String> getMissingReportParameters(int intervalReportType) {
        return getMissingReportParameters();
    }
    
    public Set<String> getMissingReportParameters() {
        Map<String, String> parameters = getReportUrlParameters();
        if (MapUtils.isNotEmpty(parameters)) {
            return parameters.entrySet().stream()
                    .filter(e -> StringUtils.isEmpty(e.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    public void setMailings(String... selectedMailings) {
        setReportSetting(MAILINGS_KEY, generateExpression(Arrays.asList(selectedMailings)));
    }

    public List<String> getMailings() {
        return parseExpression(getReportSettingAsString(MAILINGS_KEY));
    }
    
    public List<Integer> getTargetGroupsAsInt() {
        return BirtReportSettingsUtils.convertStringToIntList((String) settingsMap.get(TARGETS_KEY));
    }
    
    public List<Integer> getMailingsAsInt() {
        return BirtReportSettingsUtils.convertStringToIntList((String) settingsMap.get(MAILINGS_KEY));
    }
    
    public List<Integer> getMailinglistsAsInt() {
        return BirtReportSettingsUtils.convertStringToIntList((String) settingsMap.get(MAILINGLISTS_KEY));
    }
}
