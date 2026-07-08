/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.dto;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import org.apache.commons.collections4.ListUtils;

public class MailingSummaryStatsOpts {

    private final Admin admin;
    private final Mailing mailing;
    private final DateRange dateRange;
    private final SimpleDateFormat dateFormat;
    private final List<LightTarget> targets;
    private final boolean mailtrackingActive;
    private final boolean mailingExpired;
    private final boolean successStatisticExpired;
    private final boolean onepixelStatisticExpired;

    private MailingSummaryStatsOpts(Builder builder) {
        this.admin = Objects.requireNonNull(builder.admin, "admin");
        this.mailing = Objects.requireNonNull(builder.mailing, "mailing");
        this.dateRange = builder.dateRange;
        this.dateFormat = builder.dateFormat;
        this.targets = ListUtils.emptyIfNull(builder.targets);
        this.mailtrackingActive = builder.mailtrackingActive;
        this.mailingExpired = builder.mailingExpired;
        this.successStatisticExpired = builder.successStatisticExpired;
        this.onepixelStatisticExpired = builder.onepixelStatisticExpired;
    }

    public int getCompanyId() {
        return admin.getCompanyID();
    }

    public Locale getLocale() {
        return admin.getLocale();
    }

    public Mailing getMailing() {
        return mailing;
    }

    public int getMailingId() {
        return mailing.getId();
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public LocalDate getPeriodStart() {
        return LocalDate.ofInstant(getStartDate().toInstant(), admin.getZoneId());
    }

    public Date getStartDate() {
        return dateRange == null ? null : dateRange.getFrom();
    }

    public Date getEndDate() {
        return dateRange == null ? null : dateRange.getTo();
    }

    public boolean hasPeriod() {
        return dateRange != null && dateRange.getFrom() != null && dateRange.getTo() != null;
    }

    public List<LightTarget> getTargets() {
        return targets;
    }

    public String getTargetName(int targetId) {
        return targets.stream()
                .filter(target -> target.getId() == targetId)
                .findFirst()
                .map(LightTarget::getName)
                .orElse(null);
    }

    public SimpleDateFormat getDateFormat() {
        if (dateFormat != null) {
            return dateFormat;
        }
        SimpleDateFormat dtf = (SimpleDateFormat) DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, getLocale());
        dtf.applyPattern(dtf.toPattern().replaceFirst("y+", "yyyy").replaceFirst(", ", " "));
        return dtf;
    }

    public boolean isMailtrackingActive() {
        return mailtrackingActive;
    }

    public boolean isMailingExpired() {
        return mailingExpired;
    }

    public boolean isSuccessStatisticExpired() {
        return successStatisticExpired;
    }

    public boolean isOnepixelStatisticExpired() {
        return onepixelStatisticExpired;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Admin admin;
        private Mailing mailing;
        private DateRange dateRange;
        private List<LightTarget> targets;
        private SimpleDateFormat dateFormat;
        private boolean mailtrackingActive;
        private boolean mailingExpired;
        private boolean successStatisticExpired;
        private boolean onepixelStatisticExpired;

        public Builder admin(Admin admin) {
            this.admin = admin;
            return this;
        }

        public Builder mailing(Mailing mailing) {
            this.mailing = mailing;
            return this;
        }

        public Builder dateRange(DateRange dateRange) {
            this.dateRange = dateRange;
            return this;
        }

        public Builder targets(List<LightTarget> targets) {
            this.targets = targets;
            return this;
        }

        public Builder dateFormat(SimpleDateFormat dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        public Builder mailtrackingActive(boolean mailtrackingActive) {
            this.mailtrackingActive = mailtrackingActive;
            return this;
        }

        public Builder mailingExpired(boolean mailingExpired) {
            this.mailingExpired = mailingExpired;
            return this;
        }

        public Builder successStatisticExpired(boolean successStatisticExpired) {
            this.successStatisticExpired = successStatisticExpired;
            return this;
        }

        public Builder onepixelStatisticExpired(boolean onepixelStatisticExpired) {
            this.onepixelStatisticExpired = onepixelStatisticExpired;
            return this;
        }

        public MailingSummaryStatsOpts build() {
            return new MailingSummaryStatsOpts(this);
        }
    }
}
