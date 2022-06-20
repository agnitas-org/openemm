/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.MailinglistImpl;

public class MailinglistRowMapper extends AbstractBaseRowMapper<Mailinglist> {

    public MailinglistRowMapper() {
        super();
    }

    public MailinglistRowMapper(final String columnPrefix) {
        super(columnPrefix);
    }

    @Override
    public Mailinglist mapRow(final ResultSet resultSet, final int row) throws SQLException {
        final Mailinglist mailinglist = new MailinglistImpl();

        mailinglist.setId(getValue("mailinglist_id", resultSet::getInt));
        mailinglist.setCompanyID(getValue("company_id", resultSet::getInt));
        mailinglist.setShortname(getValue("shortname", resultSet::getString));
        mailinglist.setDescription(getValue("description", resultSet::getString));

        final Set<String> columnNames = getColumnNamesLowerCase(resultSet);

        if(columnNames.contains(getColumnPrefix() + "creation_date")) {
            mailinglist.setCreationDate(getValue("creation_date", resultSet::getTimestamp));
        }
        if(columnNames.contains(getColumnPrefix() + "change_date")) {
            mailinglist.setChangeDate(getValue("change_date", resultSet::getTimestamp));
        }
        if(columnNames.contains(getColumnPrefix() + "deleted")) {
            mailinglist.setRemoved(getValue("deleted", resultSet::getBoolean));
        }

        if(columnNames.contains(getColumnPrefix() + "freq_counter_enabled")) {
            mailinglist.setFrequencyCounterEnabled(getValue("freq_counter_enabled", s -> resultSet.getInt(s) > 0));
        }

        return mailinglist;

    }
}
