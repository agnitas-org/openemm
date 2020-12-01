/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

DELIMITER $$

CREATE PROCEDURE tmp_emm7366_proc() 
BEGIN
	DECLARE done INT DEFAULT FALSE; DECLARE cid_var int; 
	DECLARE cid_cursor CURSOR FOR SELECT company_id FROM company_tbl WHERE status IN ('active');
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
	DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
	BEGIN
		-- Do nothing
	END;
	OPEN cid_cursor;
	read_loop: LOOP
		fetch cid_cursor into cid_var;
	IF done THEN LEAVE read_loop; END IF;
	SET @SQLText = CONCAT('ALTER TABLE hst_customer_', cid_var, '_tbl ADD CONSTRAINT hstc', cid_var, '$chtcoldatecid$pk PRIMARY KEY (customer_id, change_date, name, change_Type)');	
		PREPARE stmt FROM @SQLText;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
	END loop;
	CLOSE cid_cursor;
END$$
DELIMITER ;
CALL tmp_emm7366_proc();

DROP PROCEDURE tmp_emm7366_proc;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.01.570', CURRENT_USER, CURRENT_TIMESTAMP);
