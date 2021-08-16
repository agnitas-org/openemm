/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('ImportExport', 'import.mode', 1);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('ImportExport', 'import.settings', 2);

UPDATE permission_tbl SET sub_category = NULL, sort_order = 1 WHERE permission_name = 'wizard.import';
UPDATE permission_tbl SET sub_category = NULL, sort_order = 2 WHERE permission_name = 'wizard.importclassic';
UPDATE permission_tbl SET sub_category = NULL, sort_order = 3 WHERE permission_name = 'recipient.change.bulk';
UPDATE permission_tbl SET sub_category = NULL, sort_order = 4 WHERE permission_name = 'wizard.export';

UPDATE permission_tbl SET sub_category = 'import.mode', sort_order = 1 WHERE permission_name = 'import.mode.add';
UPDATE permission_tbl SET sub_category = 'import.mode', sort_order = 2 WHERE permission_name = 'import.mode.add_update';
UPDATE permission_tbl SET sub_category = 'import.mode', sort_order = 3 WHERE permission_name = 'import.mode.only_update';
UPDATE permission_tbl SET sub_category = 'import.mode', sort_order = 4 WHERE permission_name = 'import.mode.unsubscribe';
UPDATE permission_tbl SET sub_category = 'import.mode', sort_order = 5 WHERE permission_name = 'import.mode.bounce';
UPDATE permission_tbl SET sub_category = 'import.mode', sort_order = 6 WHERE permission_name = 'import.mode.blacklist';

UPDATE permission_tbl SET sub_category = 'import.settings', sort_order = 1 WHERE permission_name = 'import.preprocessing';
UPDATE permission_tbl SET sub_category = 'import.settings', sort_order = 2 WHERE permission_name = 'import.mode.null_values';
UPDATE permission_tbl SET sub_category = 'import.settings', sort_order = 3 WHERE permission_name = 'import.mode.doublechecking';
UPDATE permission_tbl SET sub_category = 'import.settings', sort_order = 4 WHERE permission_name = 'import.mode.duplicates';
UPDATE permission_tbl SET sub_category = 'import.settings', sort_order = 5 WHERE permission_name = 'import.mailinglists.all';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.01.142', CURRENT_USER, CURRENT_TIMESTAMP);
