/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE birtreport_tbl ADD intervalpattern VARCHAR(100);
ALTER TABLE birtreport_tbl ADD lasthostname VARCHAR(100);
ALTER TABLE birtreport_tbl ADD laststart TIMESTAMP NULL;
ALTER TABLE birtreport_tbl ADD nextstart TIMESTAMP NULL;
ALTER TABLE birtreport_tbl ADD running INTEGER DEFAULT 0;
ALTER TABLE birtreport_tbl ADD lastresult VARCHAR(512);

CREATE TABLE birtreport_recipient_tbl (
	birtreport_id              INT(10) UNSIGNED COMMENT 'Multiple entries for referenced report_id from birtreport_tbl',
	email                      VARCHAR(100) COMMENT 'Emailadress of report recipient'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Birt report recipient adresses';
ALTER TABLE birtreport_recipient_tbl ADD CONSTRAINT birtrecp$birtrep$fk FOREIGN KEY (birtreport_id) REFERENCES birtreport_tbl (report_id) ON DELETE CASCADE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('19.10.295', CURRENT_USER, CURRENT_TIMESTAMP);
