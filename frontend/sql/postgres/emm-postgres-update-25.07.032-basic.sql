/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE job_queue_tbl ADD job_name VARCHAR(64);
COMMENT ON COLUMN job_queue_tbl.job_name IS 'Name of the job';

UPDATE job_queue_tbl SET job_name = 'RefreshUserLastLogin' WHERE runClass = 'com.agnitas.service.job.RefreshUserLastLoginJobWorker';
UPDATE job_queue_tbl SET job_name = 'MigrationJobWorker' WHERE runClass = 'com.agnitas.service.job.MigrationJobWorker';
UPDATE job_queue_tbl SET job_name = 'WebhookBackendDataMessageGenerator' WHERE runClass = 'com.agnitas.emm.core.webhooks.jobqueue.WebhookBackendDataMessageGeneratorJobWorker';
UPDATE job_queue_tbl SET job_name = 'LoginTrackTableCleaner' WHERE runClass = 'com.agnitas.util.quartz.LoginTrackTableCleanerJobWorker';
UPDATE job_queue_tbl SET job_name = 'ProviderStatisticsPerCompany' WHERE runClass = 'com.agnitas.service.job.ProviderStatisticsPerCompanyJobWorker';
UPDATE job_queue_tbl SET job_name = 'WorkflowReactionHandler' WHERE runClass = 'com.agnitas.emm.core.workflow.service.jobs.WorkflowReactionJobWorker';
UPDATE job_queue_tbl SET job_name = 'DBErrorCheck' WHERE runClass = 'com.agnitas.util.quartz.DBErrorCheckJobWorker';
UPDATE job_queue_tbl SET job_name = 'InformationReport' WHERE runClass = 'com.agnitas.service.job.InformationReportJobWorker';
UPDATE job_queue_tbl SET job_name = 'VoucherCodeSurveillance' WHERE runClass = 'com.agnitas.service.job.VoucherLimitCheckJobWorker';
UPDATE job_queue_tbl SET job_name = 'TriggerMailNoSendCheck' WHERE runClass = 'com.agnitas.service.job.TriggerMailNoSendCheckJobWorker';
UPDATE job_queue_tbl SET job_name = 'RecipientDistributionAggregator' WHERE runClass = 'com.agnitas.emm.core.birtstatistics.RecipientDistributionJobWorker';
UPDATE job_queue_tbl SET job_name = 'ProviderStatistics' WHERE runClass = 'com.agnitas.service.job.ProviderStatisticsJobWorker';
UPDATE job_queue_tbl SET job_name = 'UndoRelictCleaner' WHERE runClass = 'com.agnitas.service.job.UndoRelictCleanerJobWorker';
UPDATE job_queue_tbl SET job_name = 'BirtReports' WHERE runClass = 'com.agnitas.emm.core.birtreport.service.BirtReportJobWorker';
UPDATE job_queue_tbl SET job_name = 'TriggerdialogDelivery' WHERE runClass = 'com.agnitas.post.TriggerdialogDeliveryJobWorker';
UPDATE job_queue_tbl SET job_name = 'RecipientDeletionByTargetGroup' WHERE runClass = 'com.agnitas.emm.core.target.deletionschedule.jobqueue.RecipientDeletionJobWorker';
UPDATE job_queue_tbl SET job_name = 'AggregateRdirTrafficStatistic' WHERE runClass = 'com.agnitas.service.job.AggregateRdirTrafficStatisticJobWorker';
UPDATE job_queue_tbl SET job_name = 'AutoImport' WHERE runClass = 'com.agnitas.emm.core.auto_import.worker.AutoImportJobWorker';
UPDATE job_queue_tbl SET job_name = 'SendPushNotification' WHERE runClass = 'com.agnitas.emm.pushsend.jobqueue.PushSendJobWorker';
UPDATE job_queue_tbl SET job_name = 'DBCleaner' WHERE runClass = 'com.agnitas.util.quartz.DBCleanerJobWorker';
UPDATE job_queue_tbl SET job_name = 'IntervalMailings' WHERE runClass = 'com.agnitas.service.job.IntervalMailingsJobWorker';
UPDATE job_queue_tbl SET job_name = 'PushResponseHandler' WHERE runClass = 'com.agnitas.emm.pushsend.jobqueue.PostmanPushResponseProcessingJobWorker';
UPDATE job_queue_tbl SET job_name = 'DiskSpaceCheck' WHERE runClass = 'com.agnitas.emm.core.serverstatus.service.job.DiskSpaceCheckJobWorker';
UPDATE job_queue_tbl SET job_name = 'DeletedContentBlockCleaner' WHERE runClass = 'com.agnitas.service.job.DeletedContentblockCleanerJobWorker';
UPDATE job_queue_tbl SET job_name = 'RecipientChart' WHERE runClass = 'com.agnitas.service.RecipientChartJobWorker';
UPDATE job_queue_tbl SET job_name = 'AssignMediapoolImagesDimensions' WHERE runClass = 'com.agnitas.emm.grid.mediapool.service.job.AssignMediapoolImagesDimensionsJobWorker';
UPDATE job_queue_tbl SET job_name = 'WorkflowReminderService' WHERE runClass = 'com.agnitas.emm.core.workflow.service.WorkflowReminderServiceJobWorker';
UPDATE job_queue_tbl SET job_name = 'CalendarCommentMailingService' WHERE runClass = 'com.agnitas.emm.core.calendar.service.CalendarCommentMailingServiceJobWorker';
UPDATE job_queue_tbl SET job_name = 'WebhookMessageDelivery' WHERE runClass = 'com.agnitas.emm.core.webhooks.jobqueue.WebhookMessageSenderJobWorker';
UPDATE job_queue_tbl SET job_name = 'AnonymizeStatistics' WHERE runClass = 'com.agnitas.service.job.AnonymizeStatisticsJobWorker';
UPDATE job_queue_tbl SET job_name = 'RecipientChartPreCalculator' WHERE runClass = 'com.agnitas.util.quartz.RecipientChartPreCalculatorJobWorker';
UPDATE job_queue_tbl SET job_name = 'AccessDataAggregation' WHERE runClass = 'com.agnitas.service.job.AccessDataAggregationJobWorker';
UPDATE job_queue_tbl SET job_name = 'UmlautChecker' WHERE runClass = 'com.agnitas.service.job.UmlautChecker';
UPDATE job_queue_tbl SET job_name = 'AutoExport' WHERE runClass = 'com.agnitas.emm.core.autoexport.worker.AutoExportJobWorker';
UPDATE job_queue_tbl SET job_name = 'LitmusPolling' WHERE runClass = 'com.agnitas.predelivery.litmus.jobqueue.LitmusTestPollingJobWorker';
UPDATE job_queue_tbl SET job_name = 'WebserviceLoginTrackTableCleaner' WHERE runClass = 'com.agnitas.util.quartz.WebserviceLoginTrackTableCleanerJobWorker';
UPDATE job_queue_tbl SET job_name = 'WorkflowStateHandler' WHERE runClass = 'com.agnitas.emm.core.workflow.service.jobs.WorkflowStateTransitionJobWorker';
UPDATE job_queue_tbl SET job_name = 'RecipientHstCleaner' WHERE runClass = 'com.agnitas.util.quartz.RecipientHstCleanerJobWorker';
UPDATE job_queue_tbl SET job_name = 'UpdatePasswordReminder' WHERE runClass = 'com.agnitas.service.job.UpdatePasswordReminderJobWorker';
UPDATE job_queue_tbl SET job_name = 'DbJobWorker' WHERE runClass = 'com.agnitas.service.job.DBJobWorker';
UPDATE job_queue_tbl SET job_name = 'FailedTestDeliveryCleanup' WHERE runClass = 'com.agnitas.util.quartz.FailedTestDeliveryCleanupJobWorker';
UPDATE job_queue_tbl SET job_name = 'AutoOptimization' WHERE runClass = 'com.agnitas.mailing.autooptimization.service.OptimizationJobWorker';
UPDATE job_queue_tbl SET job_name = 'WebhookHardbounceMessageGenerator' WHERE runClass = 'com.agnitas.emm.core.webhooks.jobqueue.WebhookHardbounceMessageGeneratorJobWorker';
UPDATE job_queue_tbl SET job_name = 'WebhookMailingDeliveryMessageGenerator' WHERE runClass = 'com.agnitas.emm.core.webhooks.jobqueue.WebhookMailingDeliveryMessageGeneratorJobWorker';
UPDATE job_queue_tbl SET job_name = 'UndoRelictCleanerExtended' WHERE runClass = 'com.agnitas.service.job.UndoRelictCleanerJobWorkerExtended';
UPDATE job_queue_tbl SET job_name = 'UndoRelictCleaner' WHERE runClass = 'com.agnitas.service.job.UndoRelictCleanerJobWorker';
UPDATE job_queue_tbl SET job_name = 'OpenEMMCompany' WHERE runClass = 'com.agnitas.util.quartz.OpenEMMCompanyWorker';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('25.07.032', CURRENT_USER, CURRENT_TIMESTAMP);
