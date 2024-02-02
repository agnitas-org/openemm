/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE workflow_reaction_tbl MODIFY COLUMN is_legacy_mode INT(1) DEFAULT 0 NOT NULL COMMENT 'whether to use legacy action-based campaign processing (compatible mode for campaigns activated before GWUA-3603), 1 = legacy, 0 = new';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.01.402', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
