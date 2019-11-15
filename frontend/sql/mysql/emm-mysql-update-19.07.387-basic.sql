/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE webservice_permissions_tbl (
	endpoint VARCHAR(200) NOT NULL PRIMARY KEY COMMENT 'Name of granted endpoint',
	category VARCHAR(200) COMMENT 'Category of endpoint (like Subscribers oder Target groups, null if uncategorized)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Available webservice permissions and categories';

-- Permissions of category "attachment"
INSERT INTO webservice_permissions_tbl VALUES ('GetAttachment', 'attachment');
INSERT INTO webservice_permissions_tbl VALUES ('ListAttachments', 'attachment');
INSERT INTO webservice_permissions_tbl VALUES ('AddAttachment', 'attachment');
INSERT INTO webservice_permissions_tbl VALUES ('UpdateAttachment', 'attachment');
INSERT INTO webservice_permissions_tbl VALUES ('DeleteAttachment', 'attachment');

-- Permissions of category "blacklist"
INSERT INTO webservice_permissions_tbl VALUES ('AddBlacklist', 'blacklist');
INSERT INTO webservice_permissions_tbl VALUES ('GetBlacklistItems', 'blacklist');
INSERT INTO webservice_permissions_tbl VALUES ('DeleteBlacklist', 'blacklist');
INSERT INTO webservice_permissions_tbl VALUES ('CheckBlacklist', 'blacklist');

-- Permissions of category "contentBlock"
INSERT INTO webservice_permissions_tbl VALUES ('GetContentBlock', 'contentBlock');
INSERT INTO webservice_permissions_tbl VALUES ('DeleteContentBlock', 'contentBlock');
INSERT INTO webservice_permissions_tbl VALUES ('ListContentBlocks', 'contentBlock');
INSERT INTO webservice_permissions_tbl VALUES ('UpdateContentBlock', 'contentBlock');
INSERT INTO webservice_permissions_tbl VALUES ('ListContentBlockNames', 'contentBlock');
INSERT INTO webservice_permissions_tbl VALUES ('AddContentBlock', 'contentBlock');


-- Permissions of category "mailing"
INSERT INTO webservice_permissions_tbl VALUES ('ImportMailingContent', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('GetMailing', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('AddMailing', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('CopyMailing', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('GetMailingStatus', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('SendServiceMail', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('ListMailings', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('SendMailing', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('AddMailingFromTemplate', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('UpdateMailing', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('DeleteMailing', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('GetFullviewUrl', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('GetMailingContent', 'mailing');
INSERT INTO webservice_permissions_tbl VALUES ('UpdateMailingContent', 'mailing');

-- Permissions of category "mailinglist"
INSERT INTO webservice_permissions_tbl VALUES ('ListMailinglists', 'mailinglist');
INSERT INTO webservice_permissions_tbl VALUES ('GetMailinglist', 'mailinglist');
INSERT INTO webservice_permissions_tbl VALUES ('ListMailingsInMailinglist', 'mailinglist');
INSERT INTO webservice_permissions_tbl VALUES ('DeleteMailinglist', 'mailinglist');
INSERT INTO webservice_permissions_tbl VALUES ('UpdateMailinglist', 'mailinglist');
INSERT INTO webservice_permissions_tbl VALUES ('AddMailinglist', 'mailinglist');

-- Permissions of category "pushNotification"
INSERT INTO webservice_permissions_tbl VALUES ('SetPushNotificationPlanDate', 'pushNotification');
INSERT INTO webservice_permissions_tbl VALUES ('SendPushNotification', 'pushNotification');

-- Permissions of category "statistics"
INSERT INTO webservice_permissions_tbl VALUES ('MailingSummaryStatisticJob', 'statistics');
INSERT INTO webservice_permissions_tbl VALUES ('MailingSummaryStatisticResult', 'statistics');


-- Permissions of category "subscriber"
INSERT INTO webservice_permissions_tbl VALUES ('FindSubscriber', 'subscriber');
INSERT INTO webservice_permissions_tbl VALUES ('AddSubscriberBulk', 'subscriber');
INSERT INTO webservice_permissions_tbl VALUES ('UpdateSubscriberBulk', 'subscriber');
INSERT INTO webservice_permissions_tbl VALUES ('UpdateSubscriber', 'subscriber');
INSERT INTO webservice_permissions_tbl VALUES ('DeleteSubscriberBulk', 'subscriber');
INSERT INTO webservice_permissions_tbl VALUES ('AddSubscriber', 'subscriber');
INSERT INTO webservice_permissions_tbl VALUES ('ListSubscribers', 'subscriber');
INSERT INTO webservice_permissions_tbl VALUES ('DeleteSubscriber', 'subscriber');
INSERT INTO webservice_permissions_tbl VALUES ('GetSubscriberBulk', 'subscriber');
INSERT INTO webservice_permissions_tbl VALUES ('ListSubscriberMailings', 'subscriber');
INSERT INTO webservice_permissions_tbl VALUES ('GetSubscriber', 'subscriber');

-- Permissions of category "subscriberBinding"
INSERT INTO webservice_permissions_tbl VALUES ('GetSubscriberBinding', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl VALUES ('DeleteSubscriberBindingBulk', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl VALUES ('SetSubscriberBinding', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl VALUES ('DeleteSubscriberBinding', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl VALUES ('ListSubscriberBindingBulk', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl VALUES ('SetSubscriberBindingBulk', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl VALUES ('SetSubscriberBindingWithAction', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl VALUES ('ListSubscriberBinding', 'subscriberBinding');

-- Permissions of category "targetgroup"
INSERT INTO webservice_permissions_tbl VALUES ('UpdateTargetGroup', 'targetgroup');
INSERT INTO webservice_permissions_tbl VALUES ('AddTargetGroup', 'targetgroup');
INSERT INTO webservice_permissions_tbl VALUES ('ListTargetgroups', 'targetgroup');


-- Permissions of category "template"
INSERT INTO webservice_permissions_tbl VALUES ('AddTemplate', 'template');
INSERT INTO webservice_permissions_tbl VALUES ('GetTemplate', 'template');
INSERT INTO webservice_permissions_tbl VALUES ('DeleteTemplate', 'template');
INSERT INTO webservice_permissions_tbl VALUES ('UpdateTemplate', 'template');
INSERT INTO webservice_permissions_tbl VALUES ('ListTemplates', 'template');

-- Permissions of category "trackableLinks"
INSERT INTO webservice_permissions_tbl VALUES ('GetTrackableLinkSettings', 'trackableLinks');
INSERT INTO webservice_permissions_tbl VALUES ('UpdateTrackableLinkSettings', 'trackableLinks');
INSERT INTO webservice_permissions_tbl VALUES ('ListTrackableLinks', 'trackableLinks');
INSERT INTO webservice_permissions_tbl VALUES ('DecryptLinkData', 'trackableLinks');



-- Uncategorized permissions
INSERT INTO webservice_permissions_tbl VALUES ('CreateDataSource', null);
INSERT INTO webservice_permissions_tbl VALUES ('ImportStatus', null);
INSERT INTO webservice_permissions_tbl VALUES ('StartImport', null);
INSERT INTO webservice_permissions_tbl VALUES ('AddMailingImage', null);





INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
  VALUES ('19.07.387', current_user, current_timestamp);
