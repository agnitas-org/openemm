/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.forms;


import com.agnitas.emm.core.birtstatistics.DateMode;
import org.agnitas.web.forms.FormDate;
import org.apache.commons.lang.StringUtils;

public class BirtStatForm {

    private String shortname;

    /**
     * which date selection mode to use? (select month, select period, shortcuts)
     */
    private DateMode dateSelectMode;

    /**
     * Selected month (one month period).
     */
    private Integer month;
    /**
     * Selected year (one month period).
     */
    private Integer year;
    /**
     * name of the report that should be rendered (*.rptdesign)
     */
    private String reportName;

    /**
     * list of selected targetids (for checkboxes)
     */
    private String[] selectedTargets;
    /**
     * Selected beginning date of period (arbitrary selected period).
     */
    private FormDate startDate = new FormDate();
    /**
     * Selected end date of period (arbitrary selected period).
     */
    private FormDate endDate = new FormDate();

    private FormDate selectDay = new FormDate();

    private boolean topLevelDomain;

    /**
     * Shows percentage value with base of delivered mails instead of sent.
     */
    private boolean showNetto;

    private int maxDomains;

    private int mailingID;

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
        if (StringUtils.isEmpty(dateSelectMode))
            this.dateSelectMode = DateMode.SELECT_MONTH;
        else
            this.dateSelectMode = DateMode.valueOf(dateSelectMode);
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String[] getSelectedTargets() {
        return selectedTargets;
    }

    public void setSelectedTargets(String[] selectedTargets) {
        this.selectedTargets = selectedTargets;
    }

    public FormDate getStartDate() {
        return startDate;
    }

    public void setStartDate(FormDate startDate) {
        if(startDate == null){
            this.startDate = new FormDate();
        } else {
            this.startDate = startDate;
        }
    }

    public FormDate getEndDate() {
        return endDate;
    }

    public void setEndDate(FormDate endDate) {
        if(endDate == null){
            this.endDate = new FormDate();
        } else {
            this.endDate = endDate;
        }
    }

    public FormDate getSelectDay() {
        return selectDay;
    }

    public void setSelectDay(FormDate selectDay) {
        if(selectDay == null){
            this.selectDay = new FormDate();
        } else {
            this.selectDay = selectDay;
        }
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
        this.maxDomains = maxDomains;
    }

    public int getMailingID() {
        return mailingID;
    }

    public void setMailingID(int mailingID) {
        this.mailingID = mailingID;
    }

}
