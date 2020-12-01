/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE workflow_tbl MODIFY general_end_date TIMESTAMP NULL DEFAULT NULL NULL
comment 'the latest date configured by stop icon(s), set if a workflow is configured to end at specific date (end_type = 2)';

UPDATE workflow_tbl SET general_end_date = NULL WHERE end_type != 2;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.04.295', CURRENT_USER, CURRENT_TIMESTAMP);
