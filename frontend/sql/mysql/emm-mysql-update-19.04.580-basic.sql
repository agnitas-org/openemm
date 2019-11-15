/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE prevent_table_drop (
	customer_id                INTEGER UNSIGNED COMMENT 'Referenced unsigned integer id for customer_id',
	text                       VARCHAR(100) COMMENT 'Referenced text',
	mailinglist_id             INTEGER UNSIGNED COMMENT 'Referenced unsigned integer id for mailinglist_id',
	id                         INTEGER UNSIGNED COMMENT 'Referenced unsigned integer id',
	signed_id                  INTEGER COMMENT 'Referenced signed integer id, deprecated',
	change_date                TIMESTAMP NULL COMMENT 'Referenced timestamp key',
	mediatype                  INTEGER UNSIGNED COMMENT 'Referenced mediatype integer'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'saves important tables from inadvertently dropping by FK-Constraints';

ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$admin_group_tbl FOREIGN KEY (signed_id) REFERENCES admin_group_tbl (admin_group_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$admin_group_perm_tbl FOREIGN KEY (signed_id) REFERENCES admin_group_permission_tbl (admin_group_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$admin_tbl FOREIGN KEY (signed_id) REFERENCES admin_tbl (admin_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$admin_perm_tbl FOREIGN KEY (signed_id) REFERENCES admin_permission_tbl (admin_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$company_tbl FOREIGN KEY (id) REFERENCES company_tbl (company_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$auto_opt_tbl FOREIGN KEY (signed_id) REFERENCES auto_optimization_tbl (optimization_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$company_info_tbl FOREIGN KEY (signed_id) REFERENCES company_info_tbl (company_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$component_tbl FOREIGN KEY (id) REFERENCES component_tbl (component_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$customer_field_tbl FOREIGN KEY (signed_id) REFERENCES customer_field_tbl (company_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$datasource_descr_tbl FOREIGN KEY (signed_id) REFERENCES datasource_description_tbl (datasource_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$dyn_content_tbl FOREIGN KEY (id) REFERENCES dyn_content_tbl (dyn_content_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$dyn_name_tbl FOREIGN KEY (id) REFERENCES dyn_name_tbl (dyn_name_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$dyn_target_tbl FOREIGN KEY (id) REFERENCES dyn_target_tbl (target_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$import_profile_tbl FOREIGN KEY (id) REFERENCES import_profile_tbl (id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$import_col_map_tbl FOREIGN KEY (id) REFERENCES import_column_mapping_tbl (id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$import_gen_map_tbl FOREIGN KEY (id) REFERENCES import_gender_mapping_tbl (id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$import_log_tbl FOREIGN KEY (id) REFERENCES import_log_tbl (log_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$maildrop_status_tbl FOREIGN KEY (signed_id) REFERENCES maildrop_status_tbl (status_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$mailing_account_tbl FOREIGN KEY (signed_id) REFERENCES mailing_account_tbl (mailing_account_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$mailing_tbl FOREIGN KEY (id) REFERENCES mailing_tbl (mailing_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$mailing_mt_tbl FOREIGN KEY (mailinglist_id) REFERENCES mailing_mt_tbl (mailing_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$rdir_action_tbl FOREIGN KEY (id) REFERENCES rdir_action_tbl (action_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$rdir_url_tbl FOREIGN KEY (id) REFERENCES rdir_url_tbl (url_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$tag_tbl FOREIGN KEY (id) REFERENCES tag_tbl (tag_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$title_tbl FOREIGN KEY (signed_id) REFERENCES title_tbl (title_id);
ALTER TABLE title_gender_tbl MODIFY title_id INTEGER UNSIGNED;
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock2$title_gender_tbl FOREIGN KEY (id, signed_id) REFERENCES title_gender_tbl (title_id, gender);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$trackpoint_def_tbl FOREIGN KEY (signed_id) REFERENCES trackpoint_def_tbl (trackpoint_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$userform_tbl FOREIGN KEY (id) REFERENCES userform_tbl (form_id);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.04.580', CURRENT_USER, CURRENT_TIMESTAMP);
