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
    public void changeTimeWithZone(TimeZone target_zone){
        
        int old_offset = this.get(Calendar.ZONE_OFFSET);
        int old_daylight_offset = this.get(Calendar.DST_OFFSET);
        this.add(Calendar.MILLISECOND, (-1 * (old_offset + old_daylight_offset)) );
        
        this.setTimeZone(target_zone);
        
        int new_offset = this.get(Calendar.ZONE_OFFSET);
        int new_daylight_offset = this.get(Calendar.DST_OFFSET);
        this.add(Calendar.MILLISECOND, (new_offset + new_daylight_offset));
    }
    
    /**
     * returns TimeZoneOffset in milliseconds
     * Calendar has to be set before to originating TimeZone
     */
    public int getTimeZoneOffset(TimeZone target_zone) {
        // this.setTimeZone(TimeZone.getDefault()); // set to default for offset to gmt
        
        int old_offset = this.get(Calendar.ZONE_OFFSET) + this.get(Calendar.DST_OFFSET);
        
        this.setTimeZone(target_zone);
        
        int new_offset = this.get(Calendar.ZONE_OFFSET) + this.get(Calendar.DST_OFFSET);
        
        return (-1*old_offset)+new_offset;
    }
    
    /**
     * returns TimeZoneOffset in hour-format (+1 or -2.5)
     */
    public double getTimeZoneOffsetHours(TimeZone target_zone) {
        int millis=getTimeZoneOffset(target_zone);
        
        return millis/1000.0/3600;
    }
    
    /**
     * returns TimeZoneOffset in hour-format (+1 or -2.5)
     */
    public double getTimeZoneOffsetHours(TimeZone org_zone, TimeZone target_zone) {
        this.setTimeZone(org_zone);
        int millis=getTimeZoneOffset(target_zone);
        
        return millis/1000.0/3600;
    }
}

