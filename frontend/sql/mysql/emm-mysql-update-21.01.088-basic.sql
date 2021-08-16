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
	DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
	
	SELECT LOWER(VERSION()) LIKE '%maria%' INTO @isMariaDB FROM DUAL;
	
	ALTER TABLE admin_group_permission_tbl DROP KEY unique_admin_group_idx;
	ALTER TABLE admin_group_permission_tbl DROP KEY admin_grp_tbl$id_sectkn$uq;
	ALTER TABLE admin_group_permission_tbl ADD UNIQUE INDEX admingrpperm$id_permname$uq (admin_group_id, permission_name);
	ALTER TABLE admin_group_permission_tbl ADD CONSTRAINT admingrpperm$perm$fk FOREIGN KEY (permission_name) REFERENCES permission_tbl (permission_name) ON DELETE CASCADE;
	
	ALTER TABLE admin_permission_tbl DROP KEY admin_tbl$id_sectkn$uq;
	ALTER TABLE admin_permission_tbl ADD UNIQUE INDEX adminperm$id_sectkn$uq (admin_id, permission_name);
	ALTER TABLE admin_permission_tbl ADD CONSTRAINT adminperm$perm$fk FOREIGN KEY (permission_name) REFERENCES permission_tbl (permission_name) ON DELETE CASCADE;
	
	IF isMariaDB > 0 THEN
		-- Use sql in string to hide it from MySQL
		SET @ddl1 = CONCAT('ALTER TABLE prevent_table_drop DROP CONSTRAINT IF EXISTS lock$comppermtbl');
		PREPARE stmt1 FROM @ddl1;
		EXECUTE stmt1;
		DEALLOCATE PREPARE stmt1;
	ELSE
		ALTER TABLE prevent_table_drop DROP FOREIGN KEY lock$comppermtbl;
		ALTER TABLE prevent_table_drop DROP KEY lock$comppermtbl;
	END IF;
	
	ALTER TABLE company_permission_tbl DROP PRIMARY KEY;
	ALTER TABLE company_permission_tbl ADD UNIQUE INDEX compperm$id_permname$uq (company_id, permission_name);
	ALTER TABLE company_permission_tbl ADD CONSTRAINT compperm$id_permname$fk FOREIGN KEY (permission_name) REFERENCES permission_tbl (permission_name);
	ALTER TABLE company_permission_tbl MODIFY security_token VARCHAR(50) COLLATE utf8mb4_unicode_ci NULL;
END;;
DELIMITER ;

CALL TEMP_PROCEDURE;
DROP PROCEDURE TEMP_PROCEDURE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.01.088', CURRENT_USER, CURRENT_TIMESTAMP);
