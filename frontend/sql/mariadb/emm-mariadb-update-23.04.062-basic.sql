/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE import_size_tbl (
    company_ref                INTEGER UNSIGNED NOT NULL COMMENT 'company id',
    import_ref                 INTEGER UNSIGNED NOT NULL COMMENT 'import id',
    import_type                VARCHAR(32) NOT NULL COMMENT 'Type of import (see ImportType enum)',
    lines_count                INTEGER UNSIGNED NOT NULL COMMENT 'number of imported lines',
    timestamp                  TIMESTAMP NOT NULL COMMENT 'timestamp of import'
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_unicode_ci COMMENT 'Import sizes';
							
INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('23.04.062', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
