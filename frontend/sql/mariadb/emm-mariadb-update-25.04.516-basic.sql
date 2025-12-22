/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


DROP TABLE click_stat_colors_tbl;

ALTER TABLE webservice_user_tbl RENAME COLUMN contact_info TO bk_contact_info;

ALTER TABLE company_tbl DROP COLUMN export_notify;

DELETE FROM admin_permission_tbl WHERE permission_name = 'stats.dynamic';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'stats.dynamic';
DELETE FROM company_permission_tbl WHERE permission_name = 'stats.dynamic';

DELETE FROM admin_permission_tbl WHERE permission_name = 'user.xpress';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'user.xpress';
DELETE FROM company_permission_tbl WHERE permission_name = 'user.xpress';
DELETE FROM permission_tbl WHERE permission_name = 'user.xpress';


DELETE FROM admin_permission_tbl WHERE permission_name = 'ui.design.migration';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'ui.design.migration';
DELETE FROM company_permission_tbl WHERE permission_name = 'ui.design.migration';
DELETE FROM permission_tbl WHERE permission_name = 'ui.design.migration';

DELETE FROM admin_permission_tbl WHERE permission_name = 'use.redesigned.ui';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'use.redesigned.ui';
DELETE FROM company_permission_tbl WHERE permission_name = 'use.redesigned.ui';
DELETE FROM permission_tbl WHERE permission_name = 'use.redesigned.ui';

DELETE FROM admin_permission_tbl WHERE permission_name = 'user.activity.actions.extended';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'user.activity.actions.extended';
DELETE FROM company_permission_tbl WHERE permission_name = 'user.activity.actions.extended';
DELETE FROM permission_tbl WHERE permission_name = 'user.activity.actions.extended';

DELETE FROM admin_permission_tbl WHERE permission_name = 'mediatype.fax';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'mediatype.fax';
DELETE FROM company_permission_tbl WHERE permission_name = 'mediatype.fax';
DELETE FROM permission_tbl WHERE permission_name = 'mediatype.fax';

UPDATE recipients_report_tbl SET entity_type = 1 WHERE type = 'IMPORT_REPORT';
UPDATE recipients_report_tbl SET entity_type = 2 WHERE type = 'EXPORT_REPORT';
UPDATE recipients_report_tbl SET entity_execution = 2, entity_id = autoimport_id WHERE entity_type = 1 AND autoimport_id > 0;
UPDATE recipients_report_tbl SET entity_execution = 1 WHERE entity_type = 1 AND autoimport_id <= 0;
ALTER TABLE recipients_report_tbl DROP COLUMN autoimport_id;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('25.04.516', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
