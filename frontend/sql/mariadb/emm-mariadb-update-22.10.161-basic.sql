/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

DROP PROCEDURE IF EXISTS bindidx;

DELIMITER $$
CREATE PROCEDURE bindidx ()
BEGIN
	DECLARE done INT DEFAULT FALSE;
	DECLARE cid_var int;
	DECLARE index_exists int;
	DECLARE cid_cursor CURSOR FOR SELECT company_id FROM company_tbl WHERE status IN ('active', 'locked') ORDER BY company_id;
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
	DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
		BEGIN
			GET DIAGNOSTICS CONDITION 1 @msg = MESSAGE_TEXT;
			CALL emm_log_db_errors(@msg, 0, 'mediatype, EMM-9562');
		END;
	OPEN cid_cursor;
	read_loop: LOOP
		fetch cid_cursor INTO cid_var;
		IF done THEN LEAVE read_loop; END IF;
		
		SELECT COUNT(*) INTO index_exists FROM information_schema.statistics WHERE table_schema = schema() AND table_name = CONCAT('mailtrack_', cid_var, '_tbl') AND column_name = 'timestamp';
		IF index_exists = 0 THEN
			BEGIN
				SET @SQLText = CONCAT('CREATE INDEX mailtr', cid_var, '$ts$idx ON mailtrack_', cid_var, '_tbl (timestamp)');
				PREPARE stmt FROM @SQLText;
				EXECUTE stmt;
				DEALLOCATE PREPARE stmt;
			END;
		END IF;
	END LOOP;
	CLOSE cid_cursor;
END$$
DELIMITER ;

CALL bindidx();
DROP PROCEDURE bindidx;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.10.161', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
