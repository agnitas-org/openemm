/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE http_response_headers_tbl ADD COLUMN query_pattern VARCHAR(1000) COMMENT 'Regular expression for query (if null: not checked)';
ALTER TABLE http_response_headers_tbl ADD COLUMN resp_mimetype_pattern VARCHAR(1000) COMMENT 'Regular expression for response mime type (if null: not checked)';
ALTER TABLE http_response_headers_tbl ADD COLUMN description VARCHAR(1000) COMMENT 'Comment on this configuration';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.04.255', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
