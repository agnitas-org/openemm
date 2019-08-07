/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE mailing_tbl ADD is_text_version_required TINYINT(1) DEFAULT 1 NOT NULL
COMMENT 'If set to 1, mailing must have a text version (otherwise is cannot be sent, see GWUA-3991)';

-- Reset for all existing mailings.
UPDATE mailing_tbl SET is_text_version_required = 0;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
  VALUES ('18.10.263', CURRENT_USER, CURRENT_TIMESTAMP);
