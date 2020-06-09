/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

DELIMITER $$

create procedure tmp_emm6955_proc  () 
Begin
   DECLARE done INT DEFAULT FALSE; DECLARE cid_var int; 
   declare cid_curse cursor for select company_id from company_tbl where status in ('active');
   DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
      GET DIAGNOSTICS CONDITION 1 @msg = MESSAGE_TEXT;
      SELECT concat('FEHLER: ', @msg, 'bei CID ', cid_var);
    END;
    open cid_curse;
     read_loop: LOOP
        fetch cid_curse into cid_var;
	IF done THEN LEAVE read_loop; END IF;
	SET @SQLText = CONCAT('alter table cust', cid_var, '_ban_tbl add reason varchar(500)');    
    	PREPARE stmt FROM @SQLText;
    	EXECUTE stmt;
    	DEALLOCATE PREPARE stmt;
   end loop;
   close cid_curse;
END$$
DELIMITER ;
call tmp_emm6955_proc();

DROP PROCEDURE tmp_emm6955_proc;
INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('19.10.121', CURRENT_USER, CURRENT_TIMESTAMP);
