/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE restful_usage_log_tbl
(
    timestamp      TIMESTAMP        NOT NULL COMMENT 'Timestamp when restful service was invoked',
    endpoint       VARCHAR(1000)    NOT NULL COMMENT 'Invoked restful service URL',
    description    VARCHAR(4000) COMMENT 'additional information',
    request_method VARCHAR(7)       NOT NULL COMMENT 'Method of request',
    company_id     int(11) unsigned NOT NULL COMMENT 'Company ID of restful user',
    username       VARCHAR(200)     NOT NULL COMMENT 'Name of restful user'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci COMMENT 'Stores activity information of restful users';

INSERT INTO permission_tbl (permission_name, category, sort_order) VALUES ('user.activity.actions.extended', 'System', 58);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('22.10.310', CURRENT_USER, CURRENT_TIMESTAMP);
