/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.jaxb;


import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.agnitas.emm.springws.exception.DateFormatException;

public class DateISOAdapter extends XmlAdapter<String, Date> {

    @Override
    public Date unmarshal(String s) throws Exception {
        try {
            return Date.from(ZonedDateTime.parse(s).toInstant());
        } catch (Exception e) {
            throw new DateFormatException();
        }
    }

    @Override
    public String marshal(Date dt) throws Exception {
        return ZonedDateTime
                .ofInstant(dt.toInstant(), ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
