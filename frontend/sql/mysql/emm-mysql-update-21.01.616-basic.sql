/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE permission_tbl SET sort_order = 1 WHERE permission_name = 'forms.show';
UPDATE permission_tbl SET sort_order = 2 WHERE permission_name = 'forms.change';
UPDATE permission_tbl SET sort_order = 3 WHERE permission_name = 'forms.delete';
UPDATE permission_tbl SET sort_order = 4 WHERE permission_name = 'forms.import';
UPDATE permission_tbl SET sort_order = 5 WHERE permission_name = 'forms.export';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.01.616', CURRENT_USER, CURRENT_TIMESTAMP);
