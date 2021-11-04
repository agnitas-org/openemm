/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE restful_quota_tbl (
	admin_id INTEGER(11) NOT NULL COMMENT 'Reference id to admin_tbl',
	quota VARCHAR(100) NOT NULL COMMENT 'Quota specificaions'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Stores admin-specific Restful quotas';

CREATE TABLE restful_api_costs_tbl (
	company_id INT(11) UNSIGNED NOT NULL COMMENT 'Reference to company table',
	name VARCHAR(100) NOT NULL COMMENT 'Name of Restful service',
	costs INTEGER(4) NOT NULL COMMENT 'Costs for invocation'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Stores invocation costs for Restful API';


INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.07.098', CURRENT_USER, CURRENT_TIMESTAMP);
