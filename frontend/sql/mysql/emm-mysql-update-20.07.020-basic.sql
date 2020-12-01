/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE admin_to_group_tbl (
	admin_id                   INT(11) NOT NULL COMMENT 'Reference id to admin_tbl',
	admin_group_id             INT(11) NOT NULL COMMENT 'Reference id to admin_group_tbl'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Stores group references of admins';
ALTER TABLE admin_to_group_tbl ADD CONSTRAINT admintogrp$admin$fk FOREIGN KEY (admin_id) REFERENCES admin_tbl (admin_id) ON DELETE CASCADE;
ALTER TABLE admin_to_group_tbl ADD CONSTRAINT admintogrp$admingrpid$fk FOREIGN KEY (admin_group_id) REFERENCES admin_group_tbl (admin_group_id) ON DELETE CASCADE;

INSERT INTO admin_to_group_tbl (admin_id, admin_group_id) (SELECT admin_id, admin_group_id from admin_tbl);

CREATE TABLE group_to_group_tbl (
	admin_group_id             INT(11) NOT NULL COMMENT 'Reference id to admin_group_tbl',
	member_of_admin_group_id   INT(11) NOT NULL COMMENT 'Reference id to admin_group_tbl, which this group is member of'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Stores group references of groups';
ALTER TABLE group_to_group_tbl ADD CONSTRAINT grptogrp$grpid$fk FOREIGN KEY (admin_group_id) REFERENCES admin_group_tbl (admin_group_id) ON DELETE CASCADE;
ALTER TABLE group_to_group_tbl ADD CONSTRAINT grptogrp$mbrgrpid$fk FOREIGN KEY (member_of_admin_group_id) REFERENCES admin_group_tbl (admin_group_id) ON DELETE CASCADE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.07.020', CURRENT_USER, CURRENT_TIMESTAMP);
