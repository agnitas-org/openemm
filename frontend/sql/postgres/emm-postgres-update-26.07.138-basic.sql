/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

CREATE TABLE mcp_usage_log_tbl
(
    timestamp  TIMESTAMPTZ    NOT NULL,
    tool_name  VARCHAR(200) NOT NULL,
    source     VARCHAR(200),
    company_id INTEGER      NOT NULL,
    username   VARCHAR(200) NOT NULL
);
CREATE INDEX mcpus$cidtsusr$idx ON mcp_usage_log_tbl (company_id, timestamp, username);
COMMENT ON TABLE mcp_usage_log_tbl IS 'Stores activity information of MCP users';
COMMENT ON COLUMN mcp_usage_log_tbl.timestamp IS 'Timestamp when MCP tool was invoked';
COMMENT ON COLUMN mcp_usage_log_tbl.tool_name IS 'Name of the invoked MCP tool';
COMMENT ON COLUMN mcp_usage_log_tbl.source IS 'Self-reported name of the calling MCP client (e.g. Claude, ChatGPT)';
COMMENT ON COLUMN mcp_usage_log_tbl.company_id IS 'Company ID of the MCP user';
COMMENT ON COLUMN mcp_usage_log_tbl.username IS 'Name of the MCP user';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.07.138', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
