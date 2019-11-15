/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE webservice_permission_tbl (
	username VARCHAR(200) NOT NULL COMMENT 'Name of webservice user',
	endpoint VARCHAR(200) NOT NULL COMMENT 'Name of granted endpoint'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Granted webservice endpoints';

CREATE INDEX wsperm$username$idx ON webservice_permission_tbl(username);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
  VALUES ('19.07.355', current_user, current_timestamp);
