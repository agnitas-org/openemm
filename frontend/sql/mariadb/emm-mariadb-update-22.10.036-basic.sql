/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.send.migration';
INSERT INTO admin_permission_tbl (admin_id, permission_name) SELECT DISTINCT admin_id, 'mailing.send.migration' FROM admin_tbl;
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.send.migration';
INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name) SELECT DISTINCT admin_group_id, 'mailing.send.migration' FROM admin_group_tbl;

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.settings.migration';
INSERT INTO admin_permission_tbl (admin_id, permission_name) SELECT DISTINCT admin_id, 'mailing.settings.migration' FROM admin_tbl;
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.settings.migration';
INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name) SELECT DISTINCT admin_group_id, 'mailing.settings.migration' FROM admin_group_tbl;

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.create.classic.migration';
INSERT INTO admin_permission_tbl (admin_id, permission_name) SELECT DISTINCT admin_id, 'mailing.create.classic.migration' FROM admin_tbl;
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.create.classic.migration';
INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name) SELECT DISTINCT admin_group_id, 'mailing.create.classic.migration' FROM admin_group_tbl;

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.parameter.migration';
INSERT INTO admin_permission_tbl (admin_id, permission_name) SELECT DISTINCT admin_id, 'mailing.parameter.migration' FROM admin_tbl;
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.parameter.migration';
INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name) SELECT DISTINCT admin_group_id, 'mailing.parameter.migration' FROM admin_group_tbl;

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.styles.migration';
INSERT INTO admin_permission_tbl (admin_id, permission_name) SELECT DISTINCT admin_id, 'mailing.styles.migration' FROM admin_tbl;
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.styles.migration';
INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name) SELECT DISTINCT admin_group_id, 'mailing.styles.migration' FROM admin_group_tbl;

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.create.emc.migration';
INSERT INTO admin_permission_tbl (admin_id, permission_name) SELECT DISTINCT admin_id, 'mailing.create.emc.migration' FROM admin_tbl;
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.create.emc.migration';
INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name) SELECT DISTINCT admin_group_id, 'mailing.create.emc.migration' FROM admin_group_tbl;

DELETE FROM admin_permission_tbl WHERE permission_name = 'mailing.content.migration';
INSERT INTO admin_permission_tbl (admin_id, permission_name) SELECT DISTINCT admin_id, 'mailing.content.migration' FROM admin_tbl;
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mailing.content.migration';
INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name) SELECT DISTINCT admin_group_id, 'mailing.content.migration' FROM admin_group_tbl;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.10.036', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
