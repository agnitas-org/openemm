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
	DECLARE isMariaDB INTEGER;
	
	SELECT LOWER(VERSION()) LIKE '%maria%' INTO isMariaDB FROM DUAL;
	
	CREATE TABLE actop_unsubscribe_customer_tbl(
		action_operation_id        INT(11) NOT NULL COMMENT 'references actop_tbl.action_operation_id',
		all_mailinglists_selected  TINYINT(1) NOT NULL COMMENT '1 = unsubscribe from all mailinglists, 0 = user can select mailinglists to unsubscribe',
		PRIMARY KEY (action_operation_id),
		CONSTRAINT actop_unsubscribe_customer_actopid_fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl(action_operation_id) ON DELETE CASCADE
	) ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_unicode_ci;
	
	IF isMariaDB > 0 THEN
		-- Use sql in string to hide it from MySQL
		SET @ddl1 = CONCAT('ALTER TABLE actop_unsubscribe_mlist_tbl DROP FOREIGN KEY actop_unsubscribe_mlist_tbl_actopid_fk');
		PREPARE stmt1 FROM @ddl1;
		EXECUTE stmt1;
		DEALLOCATE PREPARE stmt1;
		
		-- Use sql in string to hide it from MySQL
		SET @ddl2 = CONCAT('ALTER TABLE actop_unsubscribe_mlist_tbl ADD FOREIGN KEY actop_unsubscribe_mlist_tbl_actopid_fk (action_operation_id) REFERENCES actop_unsubscribe_customer_tbl (action_operation_id) ON DELETE CASCADE');
		PREPARE stmt2 FROM @ddl2;
		EXECUTE stmt2;
		DEALLOCATE PREPARE stmt2;
	ELSE
		-- MySQL ignores the defined FK name and generates its own FK name
		ALTER TABLE actop_unsubscribe_mlist_tbl DROP FOREIGN KEY actop_unsubscribe_mlist_tbl_ibfk_1;
		ALTER TABLE actop_unsubscribe_mlist_tbl ADD FOREIGN KEY actop_unsubscribe_mlist_tbl_actopid_fk (action_operation_id) REFERENCES actop_unsubscribe_customer_tbl (action_operation_id) ON DELETE CASCADE;
	END IF;
END;;
DELIMITER ;

CALL TEMP_PROCEDURE;
DROP PROCEDURE TEMP_PROCEDURE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.04.169', CURRENT_USER, CURRENT_TIMESTAMP);
