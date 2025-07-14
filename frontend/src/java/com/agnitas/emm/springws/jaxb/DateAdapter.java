/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.jaxb;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import com.agnitas.emm.springws.exception.DateFormatException;

public class DateAdapter extends XmlAdapter<String, Date> {
    private static final String AGN_FORMAT = "yyyy-MM-dd HH:mm:ss zzz";

    @Override
    public String marshal(Date dt) {
        return new SimpleDateFormat(AGN_FORMAT).format(dt);
    }

    @Override
    public Date unmarshal(String s) {
        try {
            // Format "YYYY-MM-DDThh:mm:ssZ" / "YYYY-MM-DDThh:mm:ss+hh:mm" / "YYYY-MM-DDThh:mm:ss-hh:mm"
            return Date.from(ZonedDateTime.parse(s).toInstant());
        } catch (Exception e1) {
            try {
                // Format "YYYY-MM-DDThh:mm:ss" (uses current timezone of the server)
                final LocalDateTime parsedWithoutTimezone = LocalDateTime.parse(s);
                return Date.from(parsedWithoutTimezone.atZone(ZoneId.systemDefault()).toInstant());
            } catch (Exception e2) {
            	try {
					return new SimpleDateFormat(AGN_FORMAT).parse(s);
				} catch (Exception e3) {
            	    throw new DateFormatException();
				}
            }
        }
    }
}
