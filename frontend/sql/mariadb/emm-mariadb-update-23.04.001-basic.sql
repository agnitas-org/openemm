/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- This update sql checks, whether the EMM version 23.04 was installed before
-- To let this check happen as early as possible, the sql update file is named 23.04.001
DROP PROCEDURE IF EXISTS TEMP_PROCEDURE;

DELIMITER ;;
CREATE PROCEDURE TEMP_PROCEDURE()
BEGIN
	DECLARE is2304Installed INTEGER;
	
	SELECT COUNT(*) INTO is2304Installed FROM agn_dbversioninfo_tbl WHERE version_number = '23.04.000';

	IF is2304Installed <= 0 THEN
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '*** EMM Version 23.04 was not installed. You have to install the former version of EMM before installing version 23.10 ***';
	END IF;
	
	INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
		VALUES ('23.04.001', CURRENT_USER, CURRENT_TIMESTAMP);
END;;
DELIMITER ;

CALL TEMP_PROCEDURE;
DROP PROCEDURE TEMP_PROCEDURE;
	
COMMIT;
