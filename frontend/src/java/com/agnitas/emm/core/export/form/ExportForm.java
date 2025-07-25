/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.export.form;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.agnitas.beans.ExportColumnMapping;
import com.agnitas.util.importvalues.DateFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class ExportForm {

    private String shortname = "";
    private String description;
    private int targetId;
    private int mailinglistId;                             // GWUA-5878 openemm field 
    private Set<Integer> mailinglistIds = new HashSet<>(); // GWUA-5878 extended scope field
    private String userType;
    private int userStatus;
    private String separator;
    private String delimiter;
    private String charset = "UTF-8";
    private int[] mailinglists;                            // mailinglists for recipient status
    private boolean alwaysQuote;
    private long exportStartTime;
    private DateFormat dateFormat = DateFormat.ddMMyyyy;
    private DateFormat dateTimeFormat = DateFormat.ddMMyyyyHHmmss;
    private String timezone = "Europe/Berlin";
    private String decimalSeparator = ",";
    private boolean useDecodedValues;
    private Locale locale = new Locale("en", "US");
    private String timestampStart;
    private String timestampEnd;
    private int timestampLastDays;
    private boolean timestampIncludeCurrentDay;
    private String creationDateStart;
    private String creationDateEnd;
    private int creationDateLastDays;
    private boolean creationDateIncludeCurrentDay;
    private boolean timeLimitsLinkedByAnd;
    private String mailinglistBindStart;
    private String mailinglistBindEnd;
    private int mailinglistBindLastDays;
	private boolean mailinglistBindIncludeCurrentDay;
    private String[] userColumns;
    private List<ExportColumnMapping> customColumns = new ArrayList<>();
    private boolean inProgress;

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getMailinglistId() {
        return mailinglistId;
    }

    public void setMailinglistId(int mailinglistId) {
        this.mailinglistId = mailinglistId;
    }

    public Set<Integer> getMailinglistIds() {
        return mailinglistIds;
    }

    public void setMailinglistIds(Set<Integer> mailinglistIds) {
        this.mailinglistIds = mailinglistIds;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    public String[] getUserColumns() {
        return userColumns;
    }

    public void setUserColumns(String[] userColumns) {
        this.userColumns = userColumns;
    }

    public int[] getMailinglists() {
        return mailinglists;
    }

    public void setMailinglists(int[] mailinglists) {
        this.mailinglists = mailinglists;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public boolean isAlwaysQuote() {
        return alwaysQuote;
    }

    public void setAlwaysQuote(boolean alwaysQuote) {
        this.alwaysQuote = alwaysQuote;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public DateFormat getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(DateFormat dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public String getDecimalSeparator() {
        return decimalSeparator;
    }

    public void setDecimalSeparator(String decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public long getExportStartTime() {
        return exportStartTime;
    }

    public void setExportStartTime(long exportStartTime) {
        this.exportStartTime = exportStartTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = "t".equals(separator) ? "\t" : separator;
    }

    public void setCreationDateLastDays(int creationDateLastDays) {
        this.creationDateLastDays = creationDateLastDays;
    }
    
    public int getCreationDateLastDays() {
    	return creationDateLastDays;
    }
    
    public void setCreationDateLastDaysStr(String creationDateLastDays) {
        this.creationDateLastDays = StringUtils.isBlank(creationDateLastDays)
                ? 0
                : NumberUtils.toInt(creationDateLastDays);
    }
    
    public String getCreationDateLastDaysStr() {
        return String.valueOf(this.creationDateLastDays);
    }
    
    public String getTimestampStart() {
        return timestampStart;
    }
    
    public void setTimestampStart(String timestampStart) {
        this.timestampStart = timestampStart;
    }

    public String getTimestampEnd() {
        return timestampEnd;
    }

    public void setTimestampEnd(String timestampEnd) {
        this.timestampEnd = timestampEnd;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

	public void setLocaleStr(String localeStr) {
		String[] localeStringParts = localeStr.split("_");
		locale = new Locale(localeStringParts[0], localeStringParts[1]);
	}

	public String getLocaleStr() {
		return locale.getLanguage() + "_" + locale.getCountry();
	}

    public boolean isTimestampIncludeCurrentDay() {
        return timestampIncludeCurrentDay;
    }

    public void setTimestampIncludeCurrentDay(boolean timestampIncludeCurrentDay) {
        this.timestampIncludeCurrentDay = timestampIncludeCurrentDay;
    }

    public int getTimestampLastDays() {
        return timestampLastDays;
    }

    public void setTimestampLastDays(int timestampLastDays) {
        this.timestampLastDays = timestampLastDays;
    }
    
    public void setTimestampLastDaysStr(String timestampLastDays) {
        this.timestampLastDays = StringUtils.isBlank(timestampLastDays)
                ? 0
                : NumberUtils.toInt(timestampLastDays);
    }
    
    public String getTimestampLastDaysStr() {
        return String.valueOf(this.timestampLastDays);
    }

    public String getCreationDateStart() {
        return creationDateStart;
    }

    public void setCreationDateStart(String creationDateStart) {
        this.creationDateStart = creationDateStart;
    }

    public String getCreationDateEnd() {
        return creationDateEnd;
    }

    public void setCreationDateEnd(String creationDateEnd) {
        this.creationDateEnd = creationDateEnd;
    }

    public boolean isCreationDateIncludeCurrentDay() {
        return creationDateIncludeCurrentDay;
    }

    public void setCreationDateIncludeCurrentDay(boolean creationDateIncludeCurrentDay) {
        this.creationDateIncludeCurrentDay = creationDateIncludeCurrentDay;
    }

    public boolean isTimeLimitsLinkedByAnd() {
        return timeLimitsLinkedByAnd;
    }

    public void setTimeLimitsLinkedByAnd(boolean timeLimitsLinkedByAnd) {
        this.timeLimitsLinkedByAnd = timeLimitsLinkedByAnd;
    }

    public String getMailinglistBindStart() {
        return mailinglistBindStart;
    }

    public void setMailinglistBindStart(String mailinglistBindStart) {
        this.mailinglistBindStart = mailinglistBindStart;
    }

    public String getMailinglistBindEnd() {
        return mailinglistBindEnd;
    }

    public void setMailinglistBindEnd(String mailinglistBindEnd) {
        this.mailinglistBindEnd = mailinglistBindEnd;
    }
    
    public int getMailinglistBindLastDays() {
    	return mailinglistBindLastDays;
    }

    public void setMailinglistBindLastDays(int mailinglistBindLastDays) {
        this.mailinglistBindLastDays = mailinglistBindLastDays;
    }

    public void setMailinglistBindLastDaysStr(String mailinglistBindLastDays) {
        this.mailinglistBindLastDays = StringUtils.isBlank(mailinglistBindLastDays)
                ? 0
                : NumberUtils.toInt(mailinglistBindLastDays);
    }
    
    public String getMailinglistBindLastDaysStr() {
        return String.valueOf(this.mailinglistBindLastDays);
    }

    public boolean isMailinglistBindIncludeCurrentDay() {
        return mailinglistBindIncludeCurrentDay;
    }

    public void setMailinglistBindIncludeCurrentDay(boolean mailinglistBindIncludeCurrentDay) {
        this.mailinglistBindIncludeCurrentDay = mailinglistBindIncludeCurrentDay;
    }

    public List<ExportColumnMapping> getCustomColumns() {
        return customColumns;
    }

    public void setCustomColumns(List<ExportColumnMapping> customColumns) {
        this.customColumns = customColumns;
    }

    public boolean isUseDecodedValues() {
        return useDecodedValues;
    }

    public void setUseDecodedValues(boolean useDecodedValues) {
        this.useDecodedValues = useDecodedValues;
    }

    // in case of modification, adapt export-progress.jsp
    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }
}
