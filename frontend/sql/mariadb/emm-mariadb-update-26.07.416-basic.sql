/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

DELETE FROM http_response_headers_tbl WHERE header_name = 'Pragma';
INSERT INTO http_response_headers_tbl (header_name, header_value, overwrite, app_types) VALUES ('Referrer-Policy', 'no-referrer', 1, 'emm');
INSERT INTO http_response_headers_tbl (header_name, header_value, overwrite, app_types) VALUES ('Strict-Transport-Security', 'max-age=15768000', 1, 'emm');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.07.416', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
