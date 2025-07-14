/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date)
VALUES ('master.dbschema.snapshot.create', 'System', NULL, 60, NULL, CURRENT_TIMESTAMP);

DELETE FROM startup_job_tbl WHERE classname = 'com.agnitas.startuplistener.api.DbSchemaSnapshotStartupJob';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('25.01.249', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
