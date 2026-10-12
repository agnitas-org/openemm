/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public class LocalDateRange extends BaseRange<LocalDate> {

    private final ZoneId zoneId;

    public LocalDateRange(LocalDate from, LocalDate to,  ZoneId zoneId) {
        super(from, to);
        this.zoneId = zoneId;
    }

    public DateRange toDateRange() {
        Instant startOfDay = getFrom().atStartOfDay(zoneId).toInstant();
        Instant endOfDay = getTo().plusDays(1).atStartOfDay(zoneId).toInstant();
        return new DateRange(Date.from(startOfDay), Date.from(endOfDay));
    }

    public Iterable<LocalDate> iterator() {
        return () -> getFrom().datesUntil(getTo().plusDays(1)).iterator();
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LocalDateRange dateRange)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Objects.equals(zoneId, dateRange.zoneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), zoneId);
    }
}
