/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

DROP PROCEDURE IF EXISTS TEMP_PROCEDURE;

DELIMITER ;;
CREATE PROCEDURE TEMP_PROCEDURE()
BEGIN
	DECLARE isMariaDB INTEGER;
	DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
	ALTER TABLE birtreport_parameter_tbl DROP COLUMN change_date;
	ALTER TABLE push_retry_tbl CHANGE status state INT(1) DEFAULT NULL;
	ALTER TABLE undo_mailing_tbl MODIFY undo_id INT(11) NOT NULL AUTO_INCREMENT COMMENT 'unique ID';
	
	SELECT LOWER(VERSION()) LIKE '%maria%' INTO isMariaDB FROM DUAL;

	IF isMariaDB > 0 THEN
		-- Use sql in string to hide it from MySQL
		SET @ddl1 = CONCAT('ALTER TABLE company_tbl DROP CONSTRAINT comp$status$fk');
		PREPARE stmt1 FROM @ddl1;
		EXECUTE stmt1;
		DEALLOCATE PREPARE stmt1;
		
		SET @ddl2 = CONCAT('ALTER TABLE company_tbl ADD CONSTRAINT comp$status$fk FOREIGN KEY (status) REFERENCES company_status_desc_tbl (status)');
		PREPARE stmt2 FROM @ddl2;
		EXECUTE stmt2;
		DEALLOCATE PREPARE stmt2;
	ELSE
		ALTER TABLE company_tbl DROP FOREIGN KEY comp$status$fk;
		ALTER TABLE company_tbl DROP KEY comp$status$fk;
		ALTER TABLE company_tbl ADD CONSTRAINT comp$status$fk FOREIGN KEY (status) REFERENCES company_status_desc_tbl (status);
	END IF;
END;;
DELIMITER ;

CALL TEMP_PROCEDURE;
DROP PROCEDURE TEMP_PROCEDURE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.10.181', CURRENT_USER, CURRENT_TIMESTAMP);
