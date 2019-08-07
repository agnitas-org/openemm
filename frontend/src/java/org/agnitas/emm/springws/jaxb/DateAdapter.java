/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.jaxb;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date>{
	
	private static final SimpleDateFormat AGN_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
	
	@Override
	public String marshal(Date dt) throws Exception {
        return AGN_FORMAT.format(dt);
    }

	@Override
	public Date unmarshal(String s) throws Exception {
		try {
			// Format "YYYY-MM-DDThh:mm:ssZ" / "YYYY-MM-DDThh:mm:ss+hh:mm" / "YYYY-MM-DDThh:mm:ss-hh:mm"
			return Date.from(ZonedDateTime.parse(s).toInstant());
		} catch(Exception e) {
			try {
				// Format "YYYY-MM-DDThh:mm:ss" (uses current timezone of the server)
				final LocalDateTime parsedWithoutTimezone = LocalDateTime.parse(s);
				return Date.from(parsedWithoutTimezone.atZone(ZoneId.systemDefault()).toInstant());
			} catch(Exception e2) {
				return AGN_FORMAT.parse(s);
			}
		}
    }

}
