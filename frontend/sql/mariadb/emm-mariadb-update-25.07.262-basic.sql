/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE ui_message_tbl(
    admin_id      INT(11) COMMENT 'Admin id of admin',
    messages_json TEXT COMMENT 'messages JSON array',
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'time of last change'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT 'Stores messages to be shown on UI';

ALTER TABLE ui_message_tbl ADD CONSTRAINT uimessage$admin$fk FOREIGN KEY (admin_id) REFERENCES admin_tbl (admin_id);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('25.07.262', CURRENT_USER, CURRENT_TIMESTAMP);
COMMIT;
