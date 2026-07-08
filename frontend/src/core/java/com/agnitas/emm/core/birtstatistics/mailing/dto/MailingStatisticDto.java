/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.dto;

import static java.util.Collections.emptyList;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.enums.StatisticType;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.commons.dto.DateTimeRange;

public class MailingStatisticDto {
    private int mailingId;
    private String shortname;
    private String description;
    private String[] selectedTargets;
    private DateTimeRange dateTimeRange;
    private DateMode dateMode;
    private StatisticType type;
    private boolean showNetto;
    private boolean mailtrackingActive;
    private boolean mailtrackingExpired;
    private boolean topLevelDomain;
    private int maxDomains;
    private Date mailingStartDate;
    private int linkId;
    private int optimizationId;

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

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

    public String[] getSelectedTargets() {
        return selectedTargets;
    }

    public void setSelectedTargets(String[] selectedTargets) {
        this.selectedTargets = selectedTargets;
    }

    public List<Integer> selectedTargetsIds() {
        return selectedTargets == null
                ? emptyList()
                : Arrays.stream(selectedTargets).map(Integer::valueOf).toList();
    }

    public DateRange getDateRange(ZoneId zoneId) {
        if (this.dateTimeRange == null) {
            return null;
        }
        ZonedDateTime fromZoned = Optional.ofNullable(this.dateTimeRange.getFrom())
                .map(ldt -> ldt.atZone(zoneId))
                .orElse(null);
        ZonedDateTime toZoned = Optional.ofNullable(this.dateTimeRange.getTo())
                .map(ldt -> ldt.atZone(zoneId))
                .orElse(null);
        return new DateRange(fromZoned, toZoned);
    }

    public boolean isDateRangeExists() {
        return getDateTimeRange() != null && getDateTimeRange().getFrom() != null;
    }

    public DateTimeRange getDateTimeRange() {
        return dateTimeRange;
    }

    public void setDateTimeRange(DateTimeRange dateTimeRange) {
        this.dateTimeRange = dateTimeRange;
    }

    public boolean isHourScale() {
        return dateMode == DateMode.LAST_TENHOURS || dateMode == DateMode.SELECT_DAY;
    }

    public DateMode getDateMode() {
        return dateMode;
    }

    public void setDateMode(DateMode dateMode) {
        this.dateMode = dateMode;
    }

    public StatisticType getType() {
        return type;
    }

    public void setType(StatisticType type) {
        this.type = type;
    }

    public boolean isShowNetto() {
        return showNetto;
    }

    public void setShowNetto(boolean showNetto) {
        this.showNetto = showNetto;
    }

    public boolean isMailtrackingActive() {
        return mailtrackingActive;
    }

    public void setMailtrackingActive(boolean mailtrackingActive) {
        this.mailtrackingActive = mailtrackingActive;
    }

    public boolean isMailtrackingExpired() {
        return mailtrackingExpired;
    }

    public void setMailtrackingExpired(boolean mailtrackingExpired) {
        this.mailtrackingExpired = mailtrackingExpired;
    }

    public boolean isTopLevelDomain() {
        return topLevelDomain;
    }

    public void setTopLevelDomain(boolean topLevelDomain) {
        this.topLevelDomain = topLevelDomain;
    }

    public int getMaxDomains() {
        return maxDomains;
    }

    public void setMaxDomains(int maxDomains) {
        this.maxDomains = maxDomains;
    }

    public Date getMailingStartDate() {
        return mailingStartDate;
    }

    public void setMailingStartDate(Date mailingStartDate) {
        this.mailingStartDate = mailingStartDate;
    }
    
    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }
    
    public int getLinkId() {
        return linkId;
    }
    
    public int getOptimizationId() {
        return optimizationId;
    }
    
    public void setOptimizationId(int optimizationId) {
        this.optimizationId = optimizationId;
    }
}
