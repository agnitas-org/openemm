/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE `ws_login_track_tbl` (
  `login_track_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'unique ID, use ws_login_track_tbl_seq',
  `ip_address` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '[secret_data] address where login-request came from',
  `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'login-request timestamp',
  `login_status` int(11) DEFAULT NULL COMMENT '10 = successfull, 20 = failed, 30 = unlock blocked IP, 40 = successfull but while IP is locked',
  `username` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '[secret_data] WS-user name used in request',
  PRIMARY KEY (`login_track_id`),
  KEY `wslogtrck$ip_cdate_stat$idx` (`ip_address`,`creation_date`,`login_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT '[secret_data] any WS-login-request, successfull or not, is stored here (for a certain time)';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('19.10.270', CURRENT_USER, CURRENT_TIMESTAMP);
