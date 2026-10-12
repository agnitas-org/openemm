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
import com.agnitas.emm.core.oauth.dao.OAuth2RegistrationClientDao;
import com.agnitas.emm.core.oauth.model.OAuth2RegistrationClientRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class OAuth2RegistrationClientDaoImpl extends BaseDaoImpl implements OAuth2RegistrationClientDao {

    private static final String COLUMNS =
            "id, client_id, client_id_issued_at, client_secret, client_secret_expires_at, " +
            "client_name, client_authentication_methods, authorization_grant_types, " +
            "redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings";
    private static final String SELECT_SQL = "SELECT " + COLUMNS + " FROM oauth2_reg_client_tbl WHERE ";
    private static final String INSERT_SQL = "INSERT INTO oauth2_reg_client_tbl (" + COLUMNS + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_SQL = """
            UPDATE oauth2_reg_client_tbl
            SET client_id                     = ?,
                client_id_issued_at           = ?,
                client_secret                 = ?,
                client_secret_expires_at      = ?,
                client_name                   = ?,
                client_authentication_methods = ?,
                authorization_grant_types     = ?,
                redirect_uris                 = ?,
                post_logout_redirect_uris     = ?,
                scopes                        = ?,
                client_settings               = ?,
                token_settings                = ?
            WHERE id = ?
            """;

    private final RowMapper<OAuth2RegistrationClientRecord> rowMapper = (rs, i) ->
            new OAuth2RegistrationClientRecord(
                    rs.getString("id"),
                    rs.getString("client_id"),
                    toInstant(rs.getTimestamp("client_id_issued_at")),
                    rs.getString("client_secret"),
                    toInstant(rs.getTimestamp("client_secret_expires_at")),
                    rs.getString("client_name"),
                    rs.getString("client_authentication_methods"),
                    rs.getString("authorization_grant_types"),
                    rs.getString("redirect_uris"),
                    rs.getString("post_logout_redirect_uris"),
                    rs.getString("scopes"),
                    rs.getString("client_settings"),
                    rs.getString("token_settings")
            );

    public OAuth2RegistrationClientDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            @Autowired(required = false) JavaMailService javaMailService
    ) {
        super(dataSource, javaMailService);
    }

    @Override
    public Optional<OAuth2RegistrationClientRecord> findById(String id) {
        return findObject(SELECT_SQL + "id = ?", rowMapper, id);
    }

    @Override
    public Optional<OAuth2RegistrationClientRecord> findByClientId(String clientId) {
        return findObject(SELECT_SQL + "client_id = ?", rowMapper, clientId);
    }

    @Override
    public void insert(OAuth2RegistrationClientRecord clientRecord) {
        update(
                INSERT_SQL,
                clientRecord.id(),
                clientRecord.clientId(),
                toTimestamp(clientRecord.clientIdIssuedAt()),
                clientRecord.clientSecret(),
                toTimestamp(clientRecord.clientSecretExpiresAt()),
                clientRecord.clientName(),
                clientRecord.clientAuthenticationMethods(),
                clientRecord.authorizationGrantTypes(),
                clientRecord.redirectUris(),
                clientRecord.postLogoutRedirectUris(),
                clientRecord.scopes(),
                clientRecord.clientSettings(),
                clientRecord.tokenSettings()
        );
    }

    @Override
    public void update(OAuth2RegistrationClientRecord clientRecord) {
        update(
                UPDATE_SQL,
                clientRecord.clientId(),
                toTimestamp(clientRecord.clientIdIssuedAt()),
                clientRecord.clientSecret(),
                toTimestamp(clientRecord.clientSecretExpiresAt()),
                clientRecord.clientName(),
                clientRecord.clientAuthenticationMethods(),
                clientRecord.authorizationGrantTypes(),
                clientRecord.redirectUris(),
                clientRecord.postLogoutRedirectUris(),
                clientRecord.scopes(),
                clientRecord.clientSettings(),
                clientRecord.tokenSettings(),
                clientRecord.id()
        );
    }

    private Instant toInstant(Timestamp ts) {
        return Optional.ofNullable(ts)
                .map(Timestamp::toInstant)
                .orElse(null);
    }

    private Timestamp toTimestamp(Instant instant) {
        return Optional.ofNullable(instant)
                .map(Timestamp::from)
                .orElse(null);
    }

}
