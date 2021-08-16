/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE job_queue_tbl ADD criticality INTEGER DEFAULT 5 COMMENT '1=Handle error with priority low (may fail for few days), 2=Handle error with priority low, 3=Important error handling on next work day, 4=High priority error handling at day times, 5=Immediate error handling even at night';
ALTER TABLE job_queue_tbl ADD acknowledged INTEGER DEFAULT 0 COMMENT 'Error was acknowledged by humans';

UPDATE job_queue_tbl SET criticality = 5;
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'AccessDataAggregation';;
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'AggregateRdirTrafficStatisticJobWorker';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'AnonymizeStatistics';
UPDATE job_queue_tbl SET criticality = 4 WHERE description = 'AutoExport';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'AutoImport';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'AutoImportExportTelemetry';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'AutomatischerMailinglist-WechselnachAbschlussMailkampagne';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'AutoOptimization';
UPDATE job_queue_tbl SET criticality = 4 WHERE description = 'BirtReports';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'CalendarCommentMailingService';
UPDATE job_queue_tbl SET criticality = 2 WHERE description = 'CheckEmail';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'DBCleaner';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'DBErrorCheck';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'DeletedContentblockCleaner';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'DeleteGridTemplateWorkCopies';
UPDATE job_queue_tbl SET criticality = 1 WHERE description = 'InformationReport';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'IntervalMailings';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'LitmusPolling';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'LoginTrackTableCleaner';
UPDATE job_queue_tbl SET criticality = 2 WHERE description = 'ProviderStatisticsJobWorker';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'PushResponseHandler';
UPDATE job_queue_tbl SET criticality = 4 WHERE description = 'RecipientChart';
UPDATE job_queue_tbl SET criticality = 4 WHERE description = 'RecipientChartPreCalculator';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'RecipientDeletionByTargetGroupJobWorker';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'RecipientHstCleaner';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'SendMessagesToWhatsBroadcast';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'SendPushNotification';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'TriggerMailNoSendCheckJobWorker';
UPDATE job_queue_tbl SET criticality = 2 WHERE description = 'UmlautChecker';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'UndoRelictCleaner';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'VoucherCodeSurveillance';
UPDATE job_queue_tbl SET criticality = 3 WHERE description = 'WebserviceLoginTrackTableCleaner';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'WorkflowReactionHandler';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'WorkflowReminderService';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'WorkflowReportsSender';
UPDATE job_queue_tbl SET criticality = 5 WHERE description = 'WorkflowStopHandler';
	
INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.10.483', CURRENT_USER, CURRENT_TIMESTAMP);
