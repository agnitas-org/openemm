/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE USER agnitas IDENTIFIED BY '<password>';

-- MySQL 5.6:
-- SET old_passwords = 0;
-- CREATE USER agnitas IDENTIFIED WITH mysql_native_password;
-- SET PASSWORD FOR agnitas = PASSWORD('secret');
-- CREATE USER agnitas@localhost IDENTIFIED WITH mysql_native_password;
-- SET PASSWORD FOR agnitas@localhost = PASSWORD('secret');
-- CREATE USER agnitas@127.0.0.1 IDENTIFIED WITH mysql_native_password;
-- SET PASSWORD FOR agnitas@127.0.0.1 = PASSWORD('secret');

GRANT ALL PRIVILEGES ON emm.* TO agnitas;
GRANT SUPER ON *.* TO agnitas;
FLUSH PRIVILEGES;
