/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

CREATE TABLE oauth2_authorization_request_tbl
(
    state         VARCHAR(128) PRIMARY KEY COMMENT 'OAuth2 state parameter - used as PK and CSRF protection token',
    request_json  TEXT      NOT NULL COMMENT 'Serialized OAuth2 authorization request (JSON)',
    admin_id      INT       NOT NULL COMMENT 'Admin user initiating the OAuth2 binding flow - ID (admin_tbl)',
    company_id    INT(11) UNSIGNED NOT NULL COMMENT 'Tenant ID (company_tbl)',
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    expiry_date   TIMESTAMP NOT NULL COMMENT 'Expiry timestamp - request must be completed before this time'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='Temporary store for OAuth2 authorization requests during binding flow';

CREATE TABLE oauth2_external_identity_tbl
(
    id            INT          NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique record ID',
    admin_id      INT          NOT NULL COMMENT 'EMM admin linked to the external identity - ID (admin_tbl)',
    company_id    INT(11) UNSIGNED NOT NULL COMMENT 'Tenant ID (company_tbl)',
    issuer        VARCHAR(300) NOT NULL COMMENT 'OAuth2/OIDC issuer URL identifying the identity provider',
    subject       VARCHAR(300) NOT NULL COMMENT 'Subject identifier from the identity provider (unique user ID within issuer)',
    creation_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    UNIQUE (issuer, subject)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='Maps EMM user accounts to external OAuth2/OIDC identities';

ALTER TABLE oauth2_authorization_request_tbl
    ADD CONSTRAINT oauth2authreq$adminid$fk FOREIGN KEY (admin_id) REFERENCES admin_tbl (admin_id) ON DELETE CASCADE;
ALTER TABLE oauth2_authorization_request_tbl
    ADD CONSTRAINT oauth2authreq$cid$fk FOREIGN KEY (company_id) REFERENCES company_tbl (company_id);

ALTER TABLE oauth2_external_identity_tbl
    ADD CONSTRAINT oauth2extid$adminid$fk FOREIGN KEY (admin_id) REFERENCES admin_tbl (admin_id) ON DELETE CASCADE;
ALTER TABLE oauth2_external_identity_tbl
    ADD CONSTRAINT oauth2extid$cid$fk FOREIGN KEY (company_id) REFERENCES company_tbl (company_id);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.04.259', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
