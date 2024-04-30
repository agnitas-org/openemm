/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, creation_date) VALUES ('auto.export.rollback', 'System', 'Migration', 0, CURRENT_TIMESTAMP);
INSERT INTO messages_tbl (message_key, value_default, creation_date) VALUES ('UserRight.auto.export.rollback', 'AutoExport Rollback', CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, creation_date) VALUES ('export.rollback', 'System', 'Migration', 0, CURRENT_TIMESTAMP);
INSERT INTO messages_tbl (message_key, value_default, creation_date) VALUES ('UserRight.export.rollback', 'Export Rollback', CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, creation_date) VALUES ('auto.import.rollback', 'System', 'Migration', 0, CURRENT_TIMESTAMP);
INSERT INTO messages_tbl (message_key, value_default, creation_date) VALUES ('UserRight.auto.import.rollback', 'AutoImport Rollback', CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, creation_date) VALUES ('import.profiles.rollback', 'System', 'Migration', 0, CURRENT_TIMESTAMP);
INSERT INTO messages_tbl (message_key, value_default, creation_date) VALUES ('UserRight.import.profiles.rollback', 'ProfileImport Rollback', CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, creation_date) VALUES ('import.wizard.rollback', 'System', 'Migration', 0, CURRENT_TIMESTAMP);
INSERT INTO messages_tbl (message_key, value_default, creation_date) VALUES ('UserRight.import.wizard.rollback', 'ImportWizard Rollback', CURRENT_TIMESTAMP);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.04.018', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
