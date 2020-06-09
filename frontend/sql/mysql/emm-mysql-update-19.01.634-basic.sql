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
    SET @dbname = DATABASE();
	SET @tablename = "component_tbl";
	SET @columnname = "cdn_id";
	SET @preparedStatement = (SELECT IF(
	  (
	    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
	    WHERE
	      (table_name = @tablename)
	      AND (table_schema = @dbname)
	      AND (column_name = @columnname)
	  ) > 0,
	  "SELECT 1",
	  CONCAT("ALTER TABLE ", @tablename, " ADD ", @columnname, " VARCHAR(32);")
	));
	PREPARE alterIfNotExists FROM @preparedStatement;
	EXECUTE alterIfNotExists;
	DEALLOCATE PREPARE alterIfNotExists;
	
	SET @preparedStatementIndex = (SELECT IF(
	  (
	    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
	    WHERE
	      (table_name = @tablename)
	      AND (table_schema = @dbname)
	      AND (column_name = @columnname)
	  ) > 0,
	  "SELECT 1",
	  "CREATE UNIQUE INDEX component$cdn_id$uq ON component_tbl (cdn_id);"
	));
	PREPARE alterIfNotExists FROM @preparedStatementIndex;
	EXECUTE alterIfNotExists;
	DEALLOCATE PREPARE alterIfNotExists;
END;
;;

DELIMITER ;
CALL TEMP_PROCEDURE();
DROP PROCEDURE TEMP_PROCEDURE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.01.634', CURRENT_USER, CURRENT_TIMESTAMP);
