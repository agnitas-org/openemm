/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingBase', 'base_settings.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingAttachments', 'sending_normal_file_attachment.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingLinks', 'using_trackable_links.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingPreview', 'preview_-_for_in-depth_checkin.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingsCheck', 'sending_out_test_mails.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingGridContent', 'creating_content_by_using_the_.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingGridTextContent', 'fill_in_building_blocks_by_usi.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingGeneralOptions', 'entering_basic_mailing_data1.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingArchive', 'what_are_archives_.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingPrioritisation', 'mailing-priorisierung.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('importGeneral', 'import_dialog_structure.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('statisticMailing', 'mailing_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailinglistCreate', 'creating_a_mailing_list.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('formImages', 'using_images_in_forms.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('formStatistic', 'form_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('clientCreate', 'creating_a_new_client.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('pluginManagerGeneral', 'Plugin_Manager.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('tablesVoucherCreate', 'create_new_voucher_code_table.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('layoutbuilderTemplateContent', 'adding_content.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('layoutbuilderTemplateEditCss', 'editing_css_files.htm');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.07.283', CURRENT_USER, CURRENT_TIMESTAMP);
