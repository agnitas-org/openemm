/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Subscriber-Editor', 'default.extensions', 1);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Subscriber-Editor', 'recipient.fields', 2);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Subscriber-Editor', 'Mailinglist', 3);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Subscriber-Editor', 'recipient.Blacklist', 4);

UPDATE permission_tbl SET sub_category = NULL, sort_order = 1 WHERE permission_name = 'recipient.show';
UPDATE permission_tbl SET sub_category = NULL, sort_order = 2 WHERE permission_name = 'recipient.change';
UPDATE permission_tbl SET sub_category = NULL, sort_order = 3 WHERE permission_name = 'recipient.create';
UPDATE permission_tbl SET sub_category = NULL, sort_order = 4 WHERE permission_name = 'recipient.delete';
UPDATE permission_tbl SET sub_category = NULL, sort_order = 5 WHERE permission_name = 'recipient.tracking.veto';

UPDATE permission_tbl SET sub_category = 'default.extensions', sort_order = 1 WHERE permission_name = 'recipient.history';
UPDATE permission_tbl SET sub_category = 'default.extensions', sort_order = 2 WHERE permission_name = 'recipient.history.device';

UPDATE permission_tbl SET sub_category = 'recipient.fields', sort_order = 1 WHERE permission_name = 'profileField.show';
UPDATE permission_tbl SET sub_category = 'recipient.fields', sort_order = 2 WHERE permission_name = 'profileField.visible';

UPDATE permission_tbl SET sub_category = 'Mailinglist', sort_order = 1 WHERE permission_name = 'mailinglist.show';
UPDATE permission_tbl SET sub_category = 'Mailinglist', sort_order = 2 WHERE permission_name = 'mailinglist.change';
UPDATE permission_tbl SET sub_category = 'Mailinglist', sort_order = 3 WHERE permission_name = 'mailinglist.delete';
UPDATE permission_tbl SET sub_category = 'Mailinglist', sort_order = 4 WHERE permission_name = 'mailinglist.recipients.delete';

UPDATE permission_tbl SET sub_category = 'recipient.Blacklist', sort_order = 1 WHERE permission_name = 'blacklist';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.01.012', CURRENT_USER, CURRENT_TIMESTAMP);
