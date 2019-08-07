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
  DECLARE table_cursor CURSOR FOR SELECT DISTINCT table_name FROM information_schema.tables WHERE table_schema = SCHEMA() AND table_collation = 'utf8_unicode_ci';
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = true;
  
  ALTER TABLE admin_tbl MODIFY username VARCHAR(100);
  ALTER TABLE cust_temporary_tbl MODIFY uuid VARCHAR(100);
  DELETE FROM messages_tbl;
  ALTER TABLE messages_tbl MODIFY message_key VARCHAR(150);
  ALTER TABLE component_tbl MODIFY compname VARCHAR(150);
  ALTER TABLE customer_field_permission_tbl DROP FOREIGN KEY customer_field_permission_tbl_ibfk_1;
  
  ALTER TABLE messages_tbl CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
  
  OPEN table_cursor;
  
  read_loop: LOOP
    FETCH table_cursor INTO tablename;
    IF done THEN
      LEAVE read_loop;
    END IF;

    SET @ddl1 = CONCAT('ALTER TABLE ', tablename, ' CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci');
    PREPARE stmt1 FROM @ddl1;
    EXECUTE stmt1;
    DEALLOCATE PREPARE stmt1;
    
    IF tablename LIKE 'customer_%_binding_tbl' THEN
      SET @ddl1 = CONCAT('ALTER TABLE ', tablename, ' MODIFY user_type CHAR(1) COLLATE utf8mb4_bin');
      PREPARE stmt1 FROM @ddl1;
      EXECUTE stmt1;
      DEALLOCATE PREPARE stmt1;
    END IF;
  END LOOP;
  
  ALTER TABLE customer_field_permission_tbl ADD CONSTRAINT customer_field_permission_tbl_ibfk_1 FOREIGN KEY (company_id, column_name) REFERENCES customer_field_tbl (company_id, col_name);
  
  CLOSE table_cursor;
END;;
DELIMITER ;

CALL TEMP_PROCEDURE;
DROP PROCEDURE TEMP_PROCEDURE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.01.155', CURRENT_USER, CURRENT_TIMESTAMP);
