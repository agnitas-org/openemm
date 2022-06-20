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
	DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;

	ALTER TABLE customer_1_binding_tbl ADD CONSTRAINT cust1b$mid$fk FOREIGN KEY (mailinglist_id) REFERENCES mailinglist_tbl (mailinglist_id); 
	ALTER TABLE hst_customer_1_binding_tbl ADD CONSTRAINT hcust1b$pk PRIMARY KEY (customer_id, mailinglist_id, mediatype, timestamp_change);
	CREATE INDEX onpx1$mlid_cuid$idx ON onepixellog_1_tbl (mailing_id, customer_id);
END;;
DELIMITER ;

CALL TEMP_PROCEDURE;
DROP PROCEDURE TEMP_PROCEDURE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('22.01.338', CURRENT_USER, CURRENT_TIMESTAMP);
	
COMMIT;
