/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.agnitas.beans.impl.MediatypeEmailImpl;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import org.agnitas.emm.core.mailing.beans.MailingArchiveEntry;
import org.springframework.jdbc.core.RowMapper;

public class MailingArchiveEntryRowMapper implements RowMapper<MailingArchiveEntry> {

    public static final MailingArchiveEntryRowMapper INSTANCE = new MailingArchiveEntryRowMapper();

    private MailingArchiveEntryRowMapper() {

    }

    @Override
    public MailingArchiveEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (rs.getInt("mediatype") != MediaTypes.EMAIL.getMediaCode()) {
            return null;
        }

        MediatypeEmailImpl mediatype = new MediatypeEmailImpl();
        mediatype.setParam(rs.getString("param"));

        return new MailingArchiveEntry(
                rs.getInt("mailing_id"),
                rs.getString("shortname"),
                mediatype.getSubject()
        );
    }
}
