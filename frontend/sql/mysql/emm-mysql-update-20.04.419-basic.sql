/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE mailing_account_tbl ADD COLUMN (
	skip INT COMMENT 'number of messages not generate due to skipping when empty content is detected',
	chunks INT COMMENT 'if one message is sent out in several chunks, this represents the number of physical sent out chunks (e.g. for SMS)'
);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.04.419', CURRENT_USER, CURRENT_TIMESTAMP);

