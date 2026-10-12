/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

CREATE TABLE mcp_usage_log_tbl (
	timestamp                  TIMESTAMP NOT NULL COMMENT 'Timestamp when MCP tool was invoked',
	tool_name                  VARCHAR(200) NOT NULL COMMENT 'Name of the invoked MCP tool',
	source                     VARCHAR(200) COMMENT 'Self-reported name of the calling MCP client (e.g. Claude, ChatGPT)',
	company_id                 INT(11) UNSIGNED NOT NULL COMMENT 'Company ID of the MCP user',
	username                   VARCHAR(200) NOT NULL COMMENT 'Name of the MCP user'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Stores activity information of MCP users';
CREATE INDEX mcpus$cidtsusr$idx ON mcp_usage_log_tbl(company_id, timestamp, username);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.07.138', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
