/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE actop_tbl DROP FOREIGN KEY idx_ao_a; 
ALTER TABLE actop_tbl ADD CONSTRAINT idx_ao_a FOREIGN KEY (action_id) REFERENCES rdir_action_tbl (action_id) ON DELETE CASCADE;

ALTER TABLE actop_execute_script_tbl        DROP FOREIGN KEY idx_ao1_ao;
ALTER TABLE actop_update_customer_tbl       DROP FOREIGN KEY idx_ao2_ao;
ALTER TABLE actop_get_customer_tbl          DROP FOREIGN KEY idx_ao3_ao;
ALTER TABLE actop_subscribe_customer_tbl    DROP FOREIGN KEY idx_ao4_ao;
ALTER TABLE actop_send_mailing_tbl          DROP FOREIGN KEY idx_ao5_ao;
ALTER TABLE actop_service_mail_tbl          DROP FOREIGN KEY idx_ao6_ao;
ALTER TABLE actop_get_archive_list_tbl      DROP FOREIGN KEY idx_ao7_ao;
ALTER TABLE actop_get_archive_mailing_tbl   DROP FOREIGN KEY idx_ao8_ao;
ALTER TABLE actop_content_view_tbl          DROP FOREIGN KEY idx_ao9_ao;
ALTER TABLE actop_identify_customer_tbl     DROP FOREIGN KEY idx_a11_ao;

ALTER TABLE actop_execute_script_tbl        ADD CONSTRAINT idx_ao1_ao FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id) ON DELETE CASCADE;
ALTER TABLE actop_update_customer_tbl       ADD CONSTRAINT idx_ao2_ao FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id) ON DELETE CASCADE;
ALTER TABLE actop_get_customer_tbl          ADD CONSTRAINT idx_ao3_ao FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id) ON DELETE CASCADE;
ALTER TABLE actop_subscribe_customer_tbl    ADD CONSTRAINT idx_ao4_ao FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id) ON DELETE CASCADE;
ALTER TABLE actop_send_mailing_tbl          ADD CONSTRAINT idx_ao5_ao FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id) ON DELETE CASCADE;
ALTER TABLE actop_service_mail_tbl          ADD CONSTRAINT idx_ao6_ao FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id) ON DELETE CASCADE;
ALTER TABLE actop_get_archive_list_tbl      ADD CONSTRAINT idx_ao7_ao FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id) ON DELETE CASCADE;
ALTER TABLE actop_get_archive_mailing_tbl   ADD CONSTRAINT idx_ao8_ao FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id) ON DELETE CASCADE;
ALTER TABLE actop_content_view_tbl          ADD CONSTRAINT idx_ao9_ao FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id) ON DELETE CASCADE;
ALTER TABLE actop_identify_customer_tbl     ADD CONSTRAINT idx_a11_ao FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id) ON DELETE CASCADE;

ALTER TABLE actop_activate_doi_tbl          ADD CONSTRAINT idx_a12_ao FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id) ON DELETE CASCADE;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.10.043', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
