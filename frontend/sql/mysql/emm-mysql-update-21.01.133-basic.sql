/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE target_ref_mailing_tbl (
  target_ref  int(10) unsigned NOT NULL COMMENT 'Target group ID',
  company_ref int(11) unsigned NOT NULL COMMENT 'Company ID',
  mailing_ref int(10) unsigned NOT NULL COMMENT 'ID of referenced mailing'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Mailing referenced by target groups';
  
CREATE TABLE target_ref_link_tbl (
  target_ref  int(10) unsigned NOT NULL COMMENT 'Target group ID',
  company_ref int(11) unsigned NOT NULL COMMENT 'Company ID',
  link_ref int(10) unsigned NOT NULL COMMENT 'ID of referenced link'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Link referenced by target groups';

CREATE TABLE target_ref_autoimport_tbl (
  target_ref  int(10) unsigned NOT NULL COMMENT 'Target group ID',
  company_ref int(11) unsigned NOT NULL COMMENT 'Company ID',
  autoimport_ref int(10) unsigned NOT NULL COMMENT 'ID of referenced auto import'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Auto imports referenced by target groups';

CREATE TABLE target_ref_profilefield_tbl (
  target_ref  int(10) unsigned NOT NULL COMMENT 'Target group ID',
  company_ref int(11) unsigned NOT NULL COMMENT 'Company ID',
  name varchar(200) NOT NULL COMMENT 'Name of referenced profile field'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Profile fields referenced by target groups';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.01.133', CURRENT_USER, CURRENT_TIMESTAMP);
