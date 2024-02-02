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
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		SET @full_error = '*** Field size reduction of rdir_url_tbl.shortname failed. PLEASE CONTACT AGNITAS SUPPORT.';
		SELECT @full_error;
	END;

	ALTER TABLE rdir_url_tbl MODIFY shortname VARCHAR(500);
	
	INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
		VALUES ('23.01.379', CURRENT_USER, CURRENT_TIMESTAMP);
END;;
DELIMITER ;

CALL TEMP_PROCEDURE;
DROP PROCEDURE TEMP_PROCEDURE;
