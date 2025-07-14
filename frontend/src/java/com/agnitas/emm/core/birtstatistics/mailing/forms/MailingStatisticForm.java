/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.forms;


import java.time.Month;

import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.enums.StatisticType;
import com.agnitas.web.forms.FormDateTime;
import org.apache.commons.lang3.StringUtils;

public class MailingStatisticForm {
    
    public static final int DEFAULT_MAX_DOMAIN = 5;
    
    private String shortname;

    /**
     * which date selection mode to use? (select month, select period, shortcuts)
     */
    private DateMode dateSelectMode = DateMode.NONE;

    /**
     * Selected month (one month period).
     */
    private int month = -1;
    /**
     * Selected year (one month period).
     */
    private int year;

    private StatisticType statisticType;

    /**
     * list of selected targetids (for checkboxes)
     */
    private String[] selectedTargets;
    /**
     * Selected beginning date of period (arbitrary selected period).
     */
    private FormDateTime startDate = new FormDateTime();
    /**
     * Selected end date of period (arbitrary selected period).
     */
    private FormDateTime endDate = new FormDateTime();

    private boolean topLevelDomain;

    /**
     * Shows percentage value with base of delivered mails instead of sent.
     */
    private boolean showNetto;

    private int maxDomains = DEFAULT_MAX_DOMAIN;

    private int mailingID;

    private int templateId;

    private int sector;
    
    private int urlID;
    private String description;
    private boolean show10HoursTab;
    private boolean ignoreAutoOptSummary;

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public DateMode getDateMode(){
        return this.dateSelectMode;
    }

    public String getDateSelectMode() {
        return (null != dateSelectMode)? dateSelectMode.toString() : null;
    }

    public void setDateMode(DateMode dateSelectMode) {
        this.dateSelectMode = dateSelectMode;
    }

    public void setDateSelectMode(String dateSelectMode) {
        if (StringUtils.isEmpty(dateSelectMode)) {
            this.dateSelectMode = DateMode.NONE;
        } else {
            this.dateSelectMode = DateMode.valueOf(dateSelectMode);
        }
    }
    
    /**
     * @return the month-of-year, from 0 (January) to 11 (December)
     */
    public int getMonth() {
        return month;
    }
    
    /**
     * @param month  the month-of-year to set in the result, from 0 (January) to 11 (December)
     */
    public void setMonth(int month) {
        this.month = month;
    }
    
    
    public void setMonth(Month month) {
        this.month = month.getValue()-1;
    }
    
    public Month getMonthValue() {
        return Month.of(month+1);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public StatisticType getStatisticType() {
        return statisticType;
    }

    public void setStatisticType(StatisticType statisticType) {
        this.statisticType = statisticType;
    }

    public String[] getSelectedTargets() {
        return selectedTargets;
    }

    public void setSelectedTargets(String[] selectedTargets) {
        this.selectedTargets = selectedTargets;
    }

    public FormDateTime getStartDate() {
        return startDate;
    }

    public FormDateTime getEndDate() {
        return endDate;
    }

    public boolean isTopLevelDomain() {
        return topLevelDomain;
    }

    public void setTopLevelDomain(boolean topLevelDomain) {
        this.topLevelDomain = topLevelDomain;
    }

    public boolean isShowNetto() {
        return showNetto;
    }

    public void setShowNetto(boolean showNetto) {
        this.showNetto = showNetto;
    }

    public int getMaxDomains() {
        return maxDomains;
    }

    public void setMaxDomains(int maxDomains) {
        this.maxDomains = maxDomains > 0 ? maxDomains : DEFAULT_MAX_DOMAIN;
    }

    public int getMailingID() {
        return mailingID;
    }

    public void setMailingID(int mailingID) {
        this.mailingID = mailingID;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public int getSector() {
        return sector;
    }

    public void setSector(int sector) {
        this.sector = sector;
    }
    
    public int getUrlID() {
        return urlID;
    }
    
    public void setUrlID(int urlID) {
        this.urlID = urlID;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isShow10HoursTab() {
        return show10HoursTab;
    }

    public void setShow10HoursTab(boolean show10HoursTab) {
        this.show10HoursTab = show10HoursTab;
    }

    public boolean isIgnoreAutoOptSummary() {
        return ignoreAutoOptSummary;
    }

    public void setIgnoreAutoOptSummary(boolean ignoreAutoOptSummary) {
        this.ignoreAutoOptSummary = ignoreAutoOptSummary;
    }
}
