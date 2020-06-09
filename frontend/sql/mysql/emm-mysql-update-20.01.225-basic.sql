/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO webservice_perm_group_tbl VALUES (1, 'recipient_in');
INSERT INTO webservice_perm_group_tbl VALUES (2, 'recipient_out');
INSERT INTO webservice_perm_group_tbl VALUES (3, 'recipient_in_out_bulk');
INSERT INTO webservice_perm_group_tbl VALUES (4, 'content');
INSERT INTO webservice_perm_group_tbl VALUES (5, 'web_push');
INSERT INTO webservice_perm_group_tbl VALUES (6, 'statistics');
INSERT INTO webservice_perm_group_tbl VALUES (7, 'misc');

INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'AddSubscriber');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'UpdateSubscriber');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'SetSubscriberBinding');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'SetSubscriberBindingWithAction');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'StartImport');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'ImportStatus');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'DeleteSubscriber ');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'DeleteSubscriberBulk');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'AddMailinglist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'GetMailinglist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'ListMailinglists');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'UpdateMailinglist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'DeleteMailinglist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'DeleteSubscriberBinding');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'AddBlacklist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'DeleteBlacklist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'CheckBlacklist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'ListTargetgroups');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'AddTargetGroup');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'UpdateTargetGroup');
INSERT INTO webservice_perm_group_perm_tbl VALUES (1, 'GetBlacklistItems');

INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'FindSubscriber');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'GetSubscriber');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'GetSubscriberBinding');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'ListSubscriberBinding');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'DeleteSubscriber');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'DeleteSubscriberBulk ');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'AddMailinglist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'GetMailinglist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'ListMailinglists');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'UpdateMailinglist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'DeleteMailinglist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'DeleteSubscriberBinding');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'DeleteBlacklist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'ListTargetgroups');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'AddTargetGroup');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'UpdateTargetGroup');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'CheckBlacklist');
INSERT INTO webservice_perm_group_perm_tbl VALUES (2, 'GetBlacklistItems');

INSERT INTO webservice_perm_group_perm_tbl VALUES (3, 'AddSubscriberBulk');
INSERT INTO webservice_perm_group_perm_tbl VALUES (3, 'GetSubscriberBulk');
INSERT INTO webservice_perm_group_perm_tbl VALUES (3, 'UpdateSubscriberBulk');
INSERT INTO webservice_perm_group_perm_tbl VALUES (3, 'SetSubscriberBindingWithAction');
INSERT INTO webservice_perm_group_perm_tbl VALUES (3, 'ListSubscriberBindingBulk');
INSERT INTO webservice_perm_group_perm_tbl VALUES (3, 'DeleteSubscriberBindingBulk');

INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'AddMailing');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'AddMailingFromTemplate');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'GetMailing');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'ListMailings');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'ListMailingsInMailinglists');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'UpdateMailing');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'DeleteMailing');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'GetMailingContent');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'UpdateMailingContent');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'CopyMailing');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'ListSubscriberMailings');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'AddTemplate');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'GetTemplate');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'ListTemplate');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'UpdateTemplate');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'DeleteTemplate');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'AddContentBlock');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'GetContentBlock');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'ListContentBlock');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'DeleteContentBlock');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'UpdateContentBlock');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'ListContentBlockNames');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'ImportMailingContent');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'ListTrackableLinks');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'GetTrackableLinkSettings');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'UpdateTrackableLinkSettings');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'AddAttachment');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'GetAttachment');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'ListAttachment');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'UpdateAttachment');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'DeleteAttachment');
INSERT INTO webservice_perm_group_perm_tbl VALUES (4, 'AddMailingImage');

INSERT INTO webservice_perm_group_perm_tbl VALUES (5, 'SetPushNotificationPlanDate');
INSERT INTO webservice_perm_group_perm_tbl VALUES (5, 'SendPushNotificiation');

INSERT INTO webservice_perm_group_perm_tbl VALUES (6, 'MailingSummaryStatisticsJob');
INSERT INTO webservice_perm_group_perm_tbl VALUES (6, 'MailingSummaryStatisticsResult');

INSERT INTO webservice_perm_group_perm_tbl VALUES (7, 'SendMailing');
INSERT INTO webservice_perm_group_perm_tbl VALUES (7, 'SendServiceMailing');
INSERT INTO webservice_perm_group_perm_tbl VALUES (7, 'GetMailingStatus');
INSERT INTO webservice_perm_group_perm_tbl VALUES (7, 'GetFullviewUrl');
INSERT INTO webservice_perm_group_perm_tbl VALUES (7, 'DecryptLinkData');
INSERT INTO webservice_perm_group_perm_tbl VALUES (7, 'CreateDataSource');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.01.225', CURRENT_USER, CURRENT_TIMESTAMP);
	
