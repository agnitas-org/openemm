/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- Switch url-based tracking (mode 2) to no tracking (mode 0)
UPDATE rdir_url_tbl SET deep_tracking=0 WHERE deep_tracking=2;

-- Switch url- and cookie-based tracking (mode 3) to cookie-based tracking (mode 1)
UPDATE rdir_url_tbl SET deep_tracking=1 WHERE deep_tracking=3;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.07.221', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
