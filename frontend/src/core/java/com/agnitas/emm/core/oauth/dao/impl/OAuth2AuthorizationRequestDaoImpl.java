/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.oauth.dao.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import javax.sql.DataSource;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.oauth.dao.OAuth2AuthorizationRequestDao;
import com.agnitas.emm.core.oauth.model.OAuthRequestRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthorizationRequestDaoImpl extends BaseDaoImpl implements OAuth2AuthorizationRequestDao {

    protected final RowMapper<OAuthRequestRecord> rowMapper = (rs, i) ->
            new OAuthRequestRecord(
                    rs.getString("state"),
                    rs.getString("request_json"),
                    rs.getInt("admin_id"),
                    rs.getInt("company_id")
            );

    protected OAuth2AuthorizationRequestDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            @Autowired(required = false) JavaMailService javaMailService
    ) {
        super(dataSource, javaMailService);
    }

    @Override
    public void save(String state, String requestJson, int adminId, int companyId, Instant expiresAt) {
        String sql = """
                INSERT INTO oauth2_auth_req_tbl (state, request_json, admin_id, company_id, expiry_date)
                VALUES (?, ?, ?, ?, ?)
                """;
        update(sql, state, requestJson, adminId, companyId, Timestamp.from(expiresAt));
    }

    @Override
    public Optional<OAuthRequestRecord> findByState(String state) {
        String sql = """
                SELECT state, request_json, admin_id, company_id
                FROM oauth2_auth_req_tbl
                WHERE state = ?
                  AND expiry_date > CURRENT_TIMESTAMP
                """;

        return findObject(sql, rowMapper, state);
    }

    @Override
    public void deleteByState(String state) {
        update("DELETE FROM oauth2_auth_req_tbl WHERE state = ?", state);
    }

    @Override
    public void deleteExpired() {
        update("DELETE FROM oauth2_auth_req_tbl WHERE expiry_date <= CURRENT_TIMESTAMP");
    }

}
