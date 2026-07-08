/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.Calendar;
import java.util.TimeZone;

public class EmmCalendar extends java.util.GregorianCalendar {
    
    private static final long serialVersionUID = 7579558923002723690L;

	public EmmCalendar(TimeZone zone) {
        this.setTimeZone(zone);
    }
    
    /**
     * changes the actual Time by changing the TimeZone
     */
    public void changeTimeWithZone(TimeZone targetZone){
        int oldOffset = this.get(Calendar.ZONE_OFFSET);
        int oldDaylightOffset = this.get(Calendar.DST_OFFSET);
        this.add(Calendar.MILLISECOND, (-1 * (oldOffset + oldDaylightOffset)) );
        
        this.setTimeZone(targetZone);
        
        int newOffset = this.get(Calendar.ZONE_OFFSET);
        int newDaylightOffset = this.get(Calendar.DST_OFFSET);

        this.add(Calendar.MILLISECOND, (newOffset + newDaylightOffset));
    }
    
    /**
     * returns TimeZoneOffset in milliseconds
     * Calendar has to be set before to originating TimeZone
     */
    public int getTimeZoneOffset(TimeZone targetZone) {
        int oldOffset = this.get(Calendar.ZONE_OFFSET) + this.get(Calendar.DST_OFFSET);
        this.setTimeZone(targetZone);
        int newOffset = this.get(Calendar.ZONE_OFFSET) + this.get(Calendar.DST_OFFSET);
        
        return (-1 * oldOffset) + newOffset;
    }

}

