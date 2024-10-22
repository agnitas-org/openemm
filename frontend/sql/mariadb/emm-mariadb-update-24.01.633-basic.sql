/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE job_queue_tbl SET startaftererror = 1 WHERE description in (
	'AnonymizeStatistics',
	'AutoOptimization',
	'CalendarCommentMailingService ',
	'CheckEmailJobWorker',
	'DBErrorCheck',
	'DeleteGridTemplateWorkCopies ',
	'DeletedContentblockCleaner',
	'IntervalMailings',
	'LitmusPolling',
	'LoginTrackTableCleaner',
	'PushResponseHandler',
	'RecipientChart',
	'RecipientChartPreCalculator',
	'RecipientHstCleaner',
	'SendPushNotification',
	'TriggerMailNoSendCheckJobWorker',
	'UmlautChecker',
	'UndoRelictCleaner',
	'VoucherCodeSurveillance',
	'WebhookBackendDataMessageGenerator',
	'WebhookMessageDelivery',
	'WebserviceLoginTrackTableCleaner ',
	'WorkflowReactionHandler',
	'WorkflowReminderService',
	'WorkflowReportsSender',
	'WorkflowStopHandler',
	'RefreshUserLastLogin'
);

UPDATE job_queue_tbl SET `interval` = '**00' WHERE description in (
	'LoginTrackTableCleaner',
	'WebserviceLoginTrackTableCleaner' 
);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('24.01.633', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
