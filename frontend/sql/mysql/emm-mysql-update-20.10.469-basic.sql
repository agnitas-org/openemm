/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE permission_tbl SET sub_category = NULL, sort_order = 1 WHERE permission_name = 'mailing.show';
UPDATE permission_tbl SET sub_category = NULL, sort_order = 2 WHERE permission_name = 'mailing.change';
UPDATE permission_tbl SET sub_category = NULL, sort_order = 3 WHERE permission_name = 'mailing.delete';
UPDATE permission_tbl SET sub_category = NULL, sort_order = 4 WHERE permission_name = 'mailing.import';
UPDATE permission_tbl SET sub_category = NULL, sort_order = 5 WHERE permission_name = 'mailing.export';
UPDATE permission_tbl SET sub_category = NULL, sort_order = 6 WHERE permission_name = 'mailing.grid.show';

UPDATE permission_tbl SET sub_category = 'Settings', sort_order = 1 WHERE permission_name = 'mailing.show.types';
UPDATE permission_tbl SET sub_category = 'Settings', sort_order = 2 WHERE permission_name = 'mediatype.email';
UPDATE permission_tbl SET sub_category = 'Settings', sort_order = 3 WHERE permission_name = 'mailing.interval';
UPDATE permission_tbl SET sub_category = 'Settings', sort_order = 4 WHERE permission_name = 'mailing.envelope_address';

UPDATE permission_tbl SET sub_category = 'mailing.searchContent', sort_order = 1 WHERE permission_name = 'mailing.content.show';
UPDATE permission_tbl SET sub_category = 'mailing.searchContent', sort_order = 2 WHERE permission_name = 'mailing.text.html';
UPDATE permission_tbl SET sub_category = 'mailing.searchContent', sort_order = 3 WHERE permission_name = 'mailing.components.show';
UPDATE permission_tbl SET sub_category = 'mailing.searchContent', sort_order = 4 WHERE permission_name = 'mailing.components.change';
UPDATE permission_tbl SET sub_category = 'mailing.searchContent', sort_order = 5 WHERE permission_name = 'mailing.attachments.show';
UPDATE permission_tbl SET sub_category = 'mailing.searchContent', sort_order = 6 WHERE permission_name = 'mailing.extend_trackable_links';
UPDATE permission_tbl SET sub_category = 'mailing.searchContent', sort_order = 7 WHERE permission_name = 'use.content.source';
UPDATE permission_tbl SET sub_category = 'mailing.searchContent', sort_order = 8 WHERE permission_name = 'mailing.contentsource.date.limit';
UPDATE permission_tbl SET sub_category = 'mailing.searchContent', sort_order = 9 WHERE permission_name = 'mailing.content.showExcludedTargetgroups';

UPDATE permission_tbl SET sub_category = 'Delivery', sort_order = 1 WHERE permission_name = 'mailing.send.show';
UPDATE permission_tbl SET sub_category = 'Delivery', sort_order = 2 WHERE permission_name = 'mailing.send.world';
UPDATE permission_tbl SET sub_category = 'Delivery', sort_order = 3 WHERE permission_name = 'mailing.setmaxrecipients';
UPDATE permission_tbl SET sub_category = 'Delivery', sort_order = 4 WHERE permission_name = 'mailing.send.admin.target';
UPDATE permission_tbl SET sub_category = 'Delivery', sort_order = 5 WHERE permission_name = 'mailing.send.admin.options';
UPDATE permission_tbl SET sub_category = 'Delivery', sort_order = 6 WHERE permission_name = 'mailing.resume.world';
UPDATE permission_tbl SET sub_category = 'Delivery', sort_order = 7 WHERE permission_name = 'mailing.can_allow';
UPDATE permission_tbl SET sub_category = 'Delivery', sort_order = 8 WHERE permission_name = 'mailing.can_send_always';

UPDATE permission_tbl SET sub_category = 'settings.FormsOfAddress', sort_order = 1 WHERE permission_name = 'salutation.show';
UPDATE permission_tbl SET sub_category = 'settings.FormsOfAddress', sort_order = 2 WHERE permission_name = 'salutation.change';
UPDATE permission_tbl SET sub_category = 'settings.FormsOfAddress', sort_order = 3 WHERE permission_name = 'salutation.delete';
UPDATE permission_tbl SET sub_category = 'settings.FormsOfAddress', sort_order = 4 WHERE permission_name = 'recipient.gender.extended';

UPDATE permission_tbl SET sub_category = 'Campaigns', sort_order = 1 WHERE permission_name = 'campaign.show';
UPDATE permission_tbl SET sub_category = 'Campaigns', sort_order = 2 WHERE permission_name = 'campaign.change';
UPDATE permission_tbl SET sub_category = 'Campaigns', sort_order = 3 WHERE permission_name = 'campaign.delete';
UPDATE permission_tbl SET sub_category = 'Campaigns', sort_order = 4 WHERE permission_name = 'campaign.autoopt';

UPDATE permission_tbl SET sub_category = 'Template', sort_order = 1 WHERE permission_name = 'template.show';
UPDATE permission_tbl SET sub_category = 'Template', sort_order = 2 WHERE permission_name = 'template.change';
UPDATE permission_tbl SET sub_category = 'Template', sort_order = 3 WHERE permission_name = 'template.delete';
	
UPDATE permission_tbl SET sub_category = 'Mediapool', sort_order = 1 WHERE permission_name = 'media.show';
UPDATE permission_tbl SET sub_category = 'Mediapool', sort_order = 2 WHERE permission_name = 'media.change';
UPDATE permission_tbl SET sub_category = 'Mediapool', sort_order = 3 WHERE permission_name = 'media.delete';

UPDATE permission_tbl SET category = 'Premium', sub_category = 'Mailing' WHERE permission_name = 'mailing.attachment.personalize';
	
INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.10.469', CURRENT_USER, CURRENT_TIMESTAMP);
