/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO permission_tbl (permission_name, category, sort_order, creation_date) values ('import.change', 'ImportExport', 2, CURRENT_TIMESTAMP); 
INSERT INTO permission_tbl (permission_name, category, sort_order, creation_date) values ('import.delete', 'ImportExport', 3, CURRENT_TIMESTAMP);
UPDATE permission_tbl SET sort_order = 4 WHERE permission_name = 'wizard.importclassic';
UPDATE permission_tbl SET sort_order = 5 WHERE permission_name = 'recipient.change.bulk';
UPDATE permission_tbl SET sort_order = 6 WHERE permission_name = 'wizard.export';
INSERT INTO permission_tbl (permission_name, category, sort_order, creation_date) values ('export.change', 'ImportExport', 7, CURRENT_TIMESTAMP); 
INSERT INTO permission_tbl (permission_name, category, sort_order, creation_date) values ('export.delete', 'ImportExport', 8, CURRENT_TIMESTAMP);
UPDATE permission_tbl SET sort_order = 9 WHERE permission_name = 'recipient.export.currentdate.option';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.04.014', CURRENT_USER, CURRENT_TIMESTAMP);
