/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('webhooks', '../pdf/AGNITAS_EMM_Webhooks-Documentation.pdf');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('createNewForm', 'creating-a-new-form.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('importElement', 'importing-elements.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('facebookLeadAds', 'facebook-lead-ads.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newTrigger', 'create_or_change_trigger.htm');

UPDATE doc_mapping_tbl set filename='templates_-_re-usable_text_mod.htm' WHERE pagekey='templateList';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.04.434', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
