/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

CREATE TABLE oauth2_authorization_request_tbl
(
    state         VARCHAR(128) PRIMARY KEY,
    request_json  TEXT         NOT NULL,
    admin_id      INTEGER      NOT NULL,
    company_id    INTEGER      NOT NULL,
    creation_date TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date   TIMESTAMPTZ    NOT NULL
);
COMMENT ON TABLE oauth2_authorization_request_tbl IS 'Temporary store for OAuth2 authorization requests during binding flow';
COMMENT ON COLUMN oauth2_authorization_request_tbl.state IS 'OAuth2 state parameter - used as PK and CSRF protection token';
COMMENT ON COLUMN oauth2_authorization_request_tbl.request_json IS 'Serialized OAuth2 authorization request (JSON)';
COMMENT ON COLUMN oauth2_authorization_request_tbl.admin_id IS 'Admin user initiating the OAuth2 binding flow - ID (admin_tbl)';
COMMENT ON COLUMN oauth2_authorization_request_tbl.company_id IS 'Tenant ID (company_tbl)';
COMMENT ON COLUMN oauth2_authorization_request_tbl.creation_date IS 'Record creation timestamp';
COMMENT ON COLUMN oauth2_authorization_request_tbl.expiry_date IS 'Expiry timestamp - request must be completed before this time';

CREATE TABLE oauth2_external_identity_tbl
(
    id            SERIAL       PRIMARY KEY,
    admin_id      INTEGER      NOT NULL,
    company_id    INTEGER      NOT NULL,
    issuer        VARCHAR(300) NOT NULL,
    subject       VARCHAR(300) NOT NULL,
    creation_date TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (issuer, subject)
);
COMMENT ON TABLE oauth2_external_identity_tbl IS 'Maps EMM user accounts to external OAuth2/OIDC identities';
COMMENT ON COLUMN oauth2_external_identity_tbl.id IS 'Unique record ID';
COMMENT ON COLUMN oauth2_external_identity_tbl.admin_id IS 'EMM admin linked to the external identity - ID (admin_tbl)';
COMMENT ON COLUMN oauth2_external_identity_tbl.company_id IS 'Tenant ID (company_tbl)';
COMMENT ON COLUMN oauth2_external_identity_tbl.issuer IS 'OAuth2/OIDC issuer URL identifying the identity provider';
COMMENT ON COLUMN oauth2_external_identity_tbl.subject IS 'Subject identifier from the identity provider (unique user ID within issuer)';
COMMENT ON COLUMN oauth2_external_identity_tbl.creation_date IS 'Record creation timestamp';

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
