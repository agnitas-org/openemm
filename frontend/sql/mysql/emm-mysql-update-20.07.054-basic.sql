/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE auto_export_tbl ADD retrycount INTEGER DEFAULT 0 COMMENT 'Current retry of errorneous export';
ALTER TABLE auto_export_tbl ADD maximum_retries INTEGER DEFAULT 0 COMMENT 'Maximum number of retries of errorneous export';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.07.054', CURRENT_USER, CURRENT_TIMESTAMP);
