/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.classic', 'Mailing', null, 4, null);
UPDATE permission_tbl SET sort_order = 5 WHERE permission_name = 'mailing.import';
UPDATE permission_tbl SET sort_order = 6 WHERE permission_name = 'mailing.export';
UPDATE permission_tbl SET sort_order = 7 WHERE permission_name = 'mailing.styles.show';

INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name) (SELECT admin_group_id, 'mailing.classic' FROM admin_group_permission_tbl WHERE permission_name = 'mailing.change' AND admin_group_id NOT IN (SELECT admin_group_id FROM admin_group_permission_tbl WHERE permission_name ='mailing.classic'));
INSERT INTO admin_permission_tbl (admin_id, permission_name) (SELECT admin_id, 'mailing.classic' FROM admin_permission_tbl WHERE permission_name = 'mailing.change' AND admin_id NOT IN (SELECT admin_id FROM admin_permission_tbl WHERE permission_name ='mailing.classic'));

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('22.01.556', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
