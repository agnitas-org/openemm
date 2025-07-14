/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE emm_layout_base_tbl SET shortname = 'Light' WHERE shortname LIKE 'default' or shortname LIKE 'Standard';
UPDATE emm_layout_base_tbl SET shortname = 'Dark' WHERE shortname LIKE 'Dark mode';

INSERT INTO emm_layout_base_tbl (layout_base_id, base_url, creation_date, change_date, company_id, shortname, menu_position, theme_type)
SELECT layout_base_id + 1, 'assets/core', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'High-Contrast Light', 1, 2 FROM emm_layout_base_tbl WHERE layout_base_id = (SELECT MAX(tmpt.layout_base_id) FROM emm_layout_base_tbl tmpt);

INSERT INTO emm_layout_base_tbl (layout_base_id, base_url, creation_date, change_date, company_id, shortname, menu_position, theme_type)
SELECT layout_base_id + 1, 'assets/core', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'High-Contrast Dark', 1, 3 FROM emm_layout_base_tbl WHERE layout_base_id = (SELECT MAX(tmpt.layout_base_id) FROM emm_layout_base_tbl tmpt);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('24.07.456', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
