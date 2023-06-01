/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE mailing_tbl SET workflow_id = 0 WHERE workflow_id != 0 AND NOT EXISTS (
	SELECT 1 FROM workflow_dependency_tbl WHERE workflow_dependency_tbl.company_id = mailing_tbl.company_id AND workflow_dependency_tbl.entity_id = mailing_tbl.mailing_id AND type = 4);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.07.253', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
