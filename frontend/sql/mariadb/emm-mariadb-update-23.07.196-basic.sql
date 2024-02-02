/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE mailinglist_tbl ADD COLUMN sender_email VARCHAR(200) COMMENT 'Email of sender';
ALTER TABLE mailinglist_tbl ADD COLUMN reply_email VARCHAR(200) COMMENT 'Email for reply';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('23.07.196', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
