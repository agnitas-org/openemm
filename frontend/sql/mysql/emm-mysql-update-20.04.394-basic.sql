/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

DROP PROCEDURE IF EXISTS TEMP_PROCEDURE;

DELIMITER ;;
CREATE PROCEDURE TEMP_PROCEDURE()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE tablename VARCHAR(32);
  DECLARE columnname VARCHAR(32);
  DECLARE table_cursor CURSOR FOR SELECT DISTINCT table_name FROM information_schema.tables WHERE table_schema = SCHEMA() AND table_type = 'BASE TABLE' AND (table_collation IS NULL OR table_collation != 'utf8mb4_unicode_ci');
  DECLARE column_char_cursor CURSOR FOR SELECT table_name, column_name FROM information_schema.columns WHERE table_schema = schema() AND data_type = 'char' AND(collation_name IS NOT NULL AND collation_name != 'utf8mb4_unicode_ci' AND collation_name != 'utf8mb4_bin');
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = true;
  
  ALTER TABLE webservice_perm_group_tbl MODIFY name VARCHAR(150) COLLATE utf8mb4_unicode_ci NOT NULL;
  ALTER TABLE webservice_perm_group_perm_tbl MODIFY endpoint VARCHAR(150) COLLATE utf8mb4_unicode_ci NOT NULL;
  ALTER TABLE webservice_permissions_tbl MODIFY endpoint VARCHAR(150) COLLATE utf8mb4_unicode_ci NOT NULL;
  ALTER TABLE webservice_permissions_tbl MODIFY category VARCHAR(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL;
  ALTER TABLE webservice_user_group_tbl MODIFY username VARCHAR(150) COLLATE utf8mb4_unicode_ci NOT NULL;
  
  OPEN table_cursor;
  read_loop: LOOP
    FETCH table_cursor INTO tablename;
    IF done THEN
      LEAVE read_loop;
    END IF;

    IF tablename != 'messages_tbl' THEN
	    SET @ddl1 = CONCAT('ALTER TABLE ', tablename, ' CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci');
	    PREPARE stmt1 FROM @ddl1;
	    EXECUTE stmt1;
	    DEALLOCATE PREPARE stmt1;
    END IF;
  END LOOP;
  CLOSE table_cursor;
  
  SET done = FALSE;
  
  OPEN column_char_cursor;
  read_loop: LOOP
    FETCH column_char_cursor INTO tablename, columnname;
    IF done THEN
      LEAVE read_loop;
    END IF;

    SET @ddl1 = CONCAT('ALTER TABLE ', tablename, ' MODIFY ', columnname, ' CHAR(1) COLLATE utf8mb4_bin');
    PREPARE stmt1 FROM @ddl1;
    EXECUTE stmt1;
    DEALLOCATE PREPARE stmt1;
  END LOOP;
  CLOSE column_char_cursor;
END;;
DELIMITER ;

CALL TEMP_PROCEDURE;
DROP PROCEDURE TEMP_PROCEDURE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.04.394', CURRENT_USER, CURRENT_TIMESTAMP);
