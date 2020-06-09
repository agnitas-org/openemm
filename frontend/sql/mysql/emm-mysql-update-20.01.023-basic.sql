/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE startup_job_tbl MODIFY id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Job ID';


DROP PROCEDURE IF EXISTS TEMP_PROCEDURE;

DELIMITER ;;
CREATE PROCEDURE TEMP_PROCEDURE()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE companyid INT(11);
  DECLARE company_cursor CURSOR FOR SELECT company_id FROM company_tbl WHERE status IN ('active', 'inactive') ORDER BY company_id;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = true;
  
  OPEN company_cursor;
  
  read_loop: LOOP
    FETCH company_cursor INTO companyid;
    IF done THEN
      LEAVE read_loop;
    END IF;

    SET @ddl1 = CONCAT('INSERT INTO startup_job_tbl (classname, version, company_id, enabled, state) VALUES (''com.agnitas.startuplistener.startupjobs.LoginTrackSettingsMigrationStartupJob'', ''20.01.023'', ', companyid, ', 1, 0)');
    PREPARE stmt1 FROM @ddl1;
    EXECUTE stmt1;
    DEALLOCATE PREPARE stmt1;
  END LOOP;
  
  CLOSE company_cursor;
END;;
DELIMITER ;

CALL TEMP_PROCEDURE;
DROP PROCEDURE TEMP_PROCEDURE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.01.023', CURRENT_USER, CURRENT_TIMESTAMP);
