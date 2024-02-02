/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

alter table mailing_account_sum_tbl add (
skip int(11) DEFAULT 0 COMMENT 'number of messages not generate due to skipping when empty content is detected', 
chunks int(11) DEFAULT 0 COMMENT 'if one message is sent out in several chunks, this represents the number of physical sent out chunks (e.g. for SMS)');                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  

DELIMITER $$
create or replace TRIGGER mailing_account_sum_trg AFTER INSERT ON mailing_account_tbl FOR EACH ROW 
BEGIN
	DECLARE v_mintime TIMESTAMP; DECLARE v_maxtime TIMESTAMP;
	DECLARE v_mailing_id   INT(11);
	DECLARE v_status_field VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
	DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
	BEGIN
		GET DIAGNOSTICS CONDITION 1 @msg = MESSAGE_TEXT;
		CALL emm_log_db_errors(@msg, 0, 'mailing_account_sum_trg');
	END;

	SET v_mailing_id = new.mailing_id;
	SET v_status_field = new.status_field COLLATE utf8mb4_unicode_ci;

	IF ((SELECT mintime FROM mailing_account_sum_tbl WHERE mailing_id = v_mailing_id AND status_field = v_status_field) IS NULL) THEN
		INSERT INTO mailing_account_sum_tbl (mailing_id, company_id, no_of_mailings, no_of_bytes, mintime, maxtime, status_field, CHUNKS, skip)
		VALUES (new.mailing_id, new.company_id, new.no_of_mailings, new.no_of_bytes, new.timestamp, new.timestamp, new.status_field, new.CHUNKS, new.skip);
	ELSE
		SET v_mintime = (SELECT mintime FROM mailing_account_sum_tbl WHERE mailing_id = v_mailing_id AND status_field = v_status_field);
		SET v_maxtime = (SELECT maxtime FROM mailing_account_sum_tbl WHERE mailing_id = v_mailing_id AND status_field = v_status_field);

		IF (new.timestamp > v_maxtime) THEN
			UPDATE mailing_account_sum_tbl SET maxtime = new.timestamp WHERE mailing_id = new.mailing_id AND status_field = v_status_field;
		END IF;

		UPDATE mailing_account_sum_tbl
			SET no_of_mailings = no_of_mailings + new.no_of_mailings, no_of_bytes = no_of_bytes + new.no_of_bytes,
			skip = skip + new.skip, chunks = chunks + new.chunks WHERE mailing_id = new.mailing_id AND status_field = new.status_field;
	END IF;
END$$
DELIMITER ;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.04.386', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
