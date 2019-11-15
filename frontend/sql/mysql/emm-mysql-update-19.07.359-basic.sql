/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE webservice_perm_group_tbl (
	id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'ID of permission group',
	name VARCHAR(200) NOT NULL COMMENT 'Name of permission group'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'List of webservice permission groups';
ALTER TABLE webservice_perm_group_tbl ADD UNIQUE INDEX wsprmgrp$name$uq (name);


CREATE TABLE webservice_perm_group_perm_tbl (
	group_ref INTEGER UNSIGNED NOT NULL COMMENT 'ID of permission group',
	endpoint VARCHAR(200) NOT NULL COMMENT 'Name of granted endpoint'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'List of webservice permission groups';
ALTER TABLE webservice_perm_group_perm_tbl ADD CONSTRAINT wspermgrpperm$grp$fk foreign key (group_ref) references webservice_perm_group_tbl(id);
ALTER TABLE webservice_perm_group_perm_tbl ADD UNIQUE INDEX wspermgrpperm$grpendp$uq (group_ref,endpoint);


CREATE TABLE webservice_user_group_tbl (
	username VARCHAR(200) NOT NULL COMMENT 'Name of webservice user',
	group_ref INTEGER UNSIGNED NOT NULL COMMENT 'ID of permission group'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'List of webservice permission groups';
ALTER TABLE webservice_user_group_tbl ADD CONSTRAINT wsusergrp$grp$fk foreign key (group_ref) references webservice_perm_group_tbl(id);
ALTER TABLE webservice_user_group_tbl ADD UNIQUE INDEX wsusergrp$usrgrp$uq (username, group_ref);


INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.07.359', CURRENT_USER, CURRENT_TIMESTAMP);
