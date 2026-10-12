/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

CREATE TABLE oauth2_reg_client_tbl
(
    id                            VARCHAR(100)                            NOT NULL,
    client_id                     VARCHAR(100)                            NOT NULL,
    client_id_issued_at           TIMESTAMPTZ     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret                 VARCHAR(200)  DEFAULT NULL,
    client_secret_expires_at      TIMESTAMPTZ     DEFAULT NULL,
    client_name                   VARCHAR(200)                            NOT NULL,
    client_authentication_methods VARCHAR(1000)                           NOT NULL,
    authorization_grant_types     VARCHAR(1000)                           NOT NULL,
    redirect_uris                 VARCHAR(1000) DEFAULT NULL,
    post_logout_redirect_uris     VARCHAR(1000) DEFAULT NULL,
    scopes                        VARCHAR(1000)                           NOT NULL,
    client_settings               VARCHAR(2000)                           NOT NULL,
    token_settings                VARCHAR(2000)                           NOT NULL,
    PRIMARY KEY (id)
);
COMMENT ON TABLE  oauth2_reg_client_tbl IS 'OAuth2 registered client registrations';
COMMENT ON COLUMN oauth2_reg_client_tbl.id IS 'Client registration UUID';
COMMENT ON COLUMN oauth2_reg_client_tbl.client_id IS 'OAuth2 client_id';
COMMENT ON COLUMN oauth2_reg_client_tbl.client_id_issued_at IS 'Timestamp when client_id was issued';
COMMENT ON COLUMN oauth2_reg_client_tbl.client_secret IS 'Hashed client secret (null for public clients)';
COMMENT ON COLUMN oauth2_reg_client_tbl.client_secret_expires_at IS 'Expiry of client secret (null = never)';
COMMENT ON COLUMN oauth2_reg_client_tbl.client_name IS 'Human-readable client name';
COMMENT ON COLUMN oauth2_reg_client_tbl.client_authentication_methods IS 'Serialized authentication methods';
COMMENT ON COLUMN oauth2_reg_client_tbl.authorization_grant_types IS 'Serialized grant types';
COMMENT ON COLUMN oauth2_reg_client_tbl.redirect_uris IS 'Serialized redirect URIs';
COMMENT ON COLUMN oauth2_reg_client_tbl.post_logout_redirect_uris IS 'Serialized post-logout redirect URIs';
COMMENT ON COLUMN oauth2_reg_client_tbl.scopes IS 'Serialized scopes';
COMMENT ON COLUMN oauth2_reg_client_tbl.client_settings IS 'Serialized ClientSettings JSON';
COMMENT ON COLUMN oauth2_reg_client_tbl.token_settings IS 'Serialized TokenSettings JSON';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.07.002', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
