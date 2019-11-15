/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

DELETE FROM admin_permission_tbl WHERE security_token NOT IN (SELECT permission_name FROM permission_tbl);
DELETE FROM admin_group_permission_tbl WHERE security_token NOT IN (SELECT permission_name FROM permission_tbl);
DELETE FROM company_permission_tbl WHERE security_token NOT IN (SELECT permission_name FROM permission_tbl);
	
ALTER TABLE admin_permission_tbl ADD CONSTRAINT adm$perm$fk foreign key (security_token) references permission_tbl(permission_name);
ALTER TABLE admin_group_permission_tbl ADD CONSTRAINT admgrp$perm$fk foreign key (security_token) references permission_tbl(permission_name);
ALTER TABLE company_permission_tbl ADD CONSTRAINT comperm$perm$fk foreign key (security_token) references permission_tbl(permission_name);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.07.217', CURRENT_USER, CURRENT_TIMESTAMP);
