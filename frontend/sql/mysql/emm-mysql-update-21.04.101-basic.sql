/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

--	bounce management configuration and management table definitions			-*- no -*-
--
CREATE TABLE bounce_rule_tbl (
	company_id		INT(11) UNSIGNED NOT NULL COMMENT 'reference to company_tbl.company_id, if 0 and rid=0, then this is a global setting',
	rid			INT(11) UNSIGNED NOT NULL COMMENT 'reference to mailloop_tbl.rid, if 0 and company_id=0, then this is a global setting',
	definition		LONGTEXT NOT NULL COMMENT 'the rule definition as a json object',
	creation_date		TIMESTAMP COMMENT 'timestamp of creation',
	change_date		TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'timestamp of last change',
	PRIMARY KEY (company_id, rid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Rule set for delayed bounces, replaces ~/lib/bav.rule';

CREATE TABLE bounce_config_tbl (
	company_id		INT(11) UNSIGNED NOT NULL COMMENT 'reference to company_tbl.company_id, if 0 and rid=0, then this is a global setting',
	rid			INT(11) UNSIGNED NOT NULL COMMENT 'reference to mailloop_tbl.rid, if 0 and company_id=0, then this is a global setting',
	name			VARCHAR(100) NOT NULL COMMENT 'the name of the configuration entry',
	value			LONGTEXT NOT NULL COMMENT 'the value as a json object',
	description		VARCHAR(500) COMMENT 'optional description for this value',
	creation_date		TIMESTAMP COMMENT 'timestamp of creation',
	change_date		TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'timestamp of last change',
	PRIMARY KEY (company_id, rid, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Configuration for bouncemanagement';

CREATE TABLE bounce_ar_lastsent_tbl (
	rid			INT(11) UNSIGNED NOT NULL COMMENT 'reference to mailloop_tbl.rid',
	customer_id		INTEGER UNSIGNED NOT NULL COMMENT 'the customer_id we keep track last sent autoresponder mail',
	lastsent		TIMESTAMP NOT NULL COMMENT 'the timestamp of the last sent autoresponder mail',
	PRIMARY KEY (rid, customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Keep track of last sent timestamp when an autoresponder mail had been sent to a customer';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
       VALUES ('21.04.101', CURRENT_USER, CURRENT_TIMESTAMP);
	
