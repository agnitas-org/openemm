/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE webservice_endpoint_cost_tbl (
    company_ref INT(11) UNSIGNED NOT NULL COMMENT 'Company ID',
	endpoint VARCHAR(200) NOT NULL COMMENT 'Name of endpoint',
	costs INT(3) UNSIGNED NOT NULL COMMENT 'Costs of invocation'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Costs of invocation of endpoint';


INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
  VALUES ('21.01.209', current_user, current_timestamp);
