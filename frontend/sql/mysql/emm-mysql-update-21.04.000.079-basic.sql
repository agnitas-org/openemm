/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE IF NOT EXISTS dkim_key_tbl (
	dkim_id                    INT(11) NOT NULL AUTO_INCREMENT,
	creation_date              TIMESTAMP NULL,
	timestamp                  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
	company_id                 INT(11)DEFAULT 0,
	valid_start                TIMESTAMP NULL,
	valid_end                  TIMESTAMP NULL,
	domain                     VARCHAR(128) DEFAULT '',
	selector                   VARCHAR(250) DEFAULT '',
	domain_key                 VARCHAR(4000) DEFAULT '',
	domain_key_encrypted       VARCHAR(4000),
	PRIMARY KEY (dkim_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.04.000.079', CURRENT_USER, CURRENT_TIMESTAMP);
