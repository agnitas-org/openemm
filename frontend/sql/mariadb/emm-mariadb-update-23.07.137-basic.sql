INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted, criticality)
	(SELECT 'WorkflowReminderService', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '***0;***5', CURRENT_TIMESTAMP, NULL, 'com.agnitas.emm.core.workflow.service.ComWorkflowReminderServiceJobWorker', 0, 5 FROM job_queue_tbl WHERE NOT EXISTS (SELECT 1 FROM job_queue_tbl WHERE description = 'WorkflowReminderService') LIMIT 1);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted, criticality)
	(SELECT 'WorkflowReactionHandler', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '***0;***5', CURRENT_TIMESTAMP, NULL, 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowReactionJobWorker', 0, 5 FROM job_queue_tbl WHERE NOT EXISTS (SELECT 1 FROM job_queue_tbl WHERE description = 'WorkflowReactionHandler') LIMIT 1);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted, criticality)
	(SELECT 'WorkflowStopHandler', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '***0;***5', CURRENT_TIMESTAMP, NULL, 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowStopJobWorker', 0, 5 FROM job_queue_tbl WHERE NOT EXISTS (SELECT 1 FROM job_queue_tbl WHERE description = 'WorkflowStopHandler') LIMIT 1);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted, criticality)
	(SELECT 'WorkflowReportsSender', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '***0;***5', CURRENT_TIMESTAMP, NULL, 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowReportSendJobWorker', 0, 5 FROM job_queue_tbl WHERE NOT EXISTS (SELECT 1 FROM job_queue_tbl WHERE description = 'WorkflowReportsSender') LIMIT 1);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.07.137', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
