/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.bean.LightweightMailingWithMailingList;
import org.springframework.jdbc.core.RowMapper;

public class LightweightMailingWithMailinglistRowMapper implements RowMapper<LightweightMailingWithMailingList> {

    public static final LightweightMailingWithMailinglistRowMapper INSTANCE = new LightweightMailingWithMailinglistRowMapper();

    private LightweightMailingWithMailinglistRowMapper() {
        // Empty
    }

    @Override
    public LightweightMailingWithMailingList mapRow(ResultSet resultSet, int index) throws SQLException {
        final int companyID = resultSet.getInt("company_id");
        final int mailingID = resultSet.getInt("mailing_id");
        final String shortname = resultSet.getString("shortname") != null ? resultSet.getString("shortname") : "";
        final String description = resultSet.getString("description") != null ? resultSet.getString("description") : "";
        final MailingType mailingType = MailingType.getByCode(resultSet.getInt("mailing_type"));
        final String workStatus = resultSet.getString("work_status");
        final String contentTypeString = resultSet.getString("content_type");

        final MailingContentType contentType = decodeContentType(contentTypeString);

        return new LightweightMailingWithMailingList(companyID, mailingID, shortname, description, mailingType, workStatus, contentType, resultSet.getInt("mailinglist_id"));
    }

    private MailingContentType decodeContentType(final String contentTypeString) {
        try {
            return MailingContentType.getFromString(contentTypeString);
        } catch (Exception e) {
            return MailingContentType.advertising;
        }
    }
}
