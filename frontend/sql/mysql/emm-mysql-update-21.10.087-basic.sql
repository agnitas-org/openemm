/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- EMM-8184
-- Execute change after version is available on all systems: 21.04.012
-- rename unused tables

DROP PROCEDURE IF EXISTS TEMP_PROCEDURE;

DELIMITER ;;
CREATE PROCEDURE TEMP_PROCEDURE()
BEGIN
	DECLARE done INT DEFAULT FALSE;
	DECLARE v_tabelle VARCHAR(50);
	DECLARE v_umbenannt VARCHAR(50);
	DECLARE stmt VARCHAR(500);
	DECLARE tbl_cursor CURSOR for SELECT table_name FROM information_schema.tables WHERE table_schema = 'emm' AND table_name LIKE 'cust%devicehistory_tbl';
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = true;
	DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;

	OPEN tbl_cursor;

	read_loop: LOOP
		FETCH tbl_cursor INTO v_tabelle;
		IF done THEN
			LEAVE read_loop;
		END IF;

		SET @ddl = CONCAT('RENAME TABLE ', v_tabelle, ' TO ', REPLACE(v_tabelle, 'cust', 'del'));
		PREPARE stmt FROM @ddl;
        EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
	END LOOP;

	CLOSE tbl_cursor;
END;;
DELIMITER ;

CALL TEMP_PROCEDURE;
DROP PROCEDURE TEMP_PROCEDURE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.10.087', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
