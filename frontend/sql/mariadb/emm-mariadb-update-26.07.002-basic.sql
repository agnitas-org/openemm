/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

CREATE TABLE oauth2_reg_client_tbl
(
    id                            VARCHAR(100)  NOT NULL COMMENT 'Client registration UUID',
    client_id                     VARCHAR(100)  NOT NULL COMMENT 'OAuth2 client_id',
    client_id_issued_at           TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Timestamp when client_id was issued',
    client_secret                 VARCHAR(200)  DEFAULT NULL COMMENT 'Hashed client secret (null for public clients)',
    client_secret_expires_at      TIMESTAMP     NULL DEFAULT NULL COMMENT 'Expiry of client secret (null = never)',
    client_name                   VARCHAR(200)  NOT NULL COMMENT 'Human-readable client name',
    client_authentication_methods VARCHAR(1000) NOT NULL COMMENT 'Serialized authentication methods',
    authorization_grant_types     VARCHAR(1000) NOT NULL COMMENT 'Serialized grant types',
    redirect_uris                 VARCHAR(1000) DEFAULT NULL COMMENT 'Serialized redirect URIs',
    post_logout_redirect_uris     VARCHAR(1000) DEFAULT NULL COMMENT 'Serialized post-logout redirect URIs',
    scopes                        VARCHAR(1000) NOT NULL COMMENT 'Serialized scopes',
    client_settings               VARCHAR(2000) NOT NULL COMMENT 'Serialized ClientSettings JSON',
    token_settings                VARCHAR(2000) NOT NULL COMMENT 'Serialized TokenSettings JSON',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'OAuth2 registered client registrations';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.07.002', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
