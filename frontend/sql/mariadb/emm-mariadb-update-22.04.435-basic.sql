/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE mailing_tbl SET workflow_id = (SELECT MAX(workflow_id) FROM workflow_dependency_tbl WHERE mailing_id = entity_id AND type = 4)
	WHERE mailing_id IN (SELECT DISTINCT entity_id FROM workflow_dependency_tbl WHERE type = 4);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.04.435', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
