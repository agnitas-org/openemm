/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('upselling', 'moreInfo.url.de', 'https://www.agnitas.de/e-marketing-manager/funktionsumfang/unterschiede-emminhouse-openemm/', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');

INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('upselling', 'moreInfo.url.en', 'https://www.agnitas.de/en/e-marketing_manager/functions/differences-emminhouse-openemm/', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.10.015', CURRENT_USER, CURRENT_TIMESTAMP);
