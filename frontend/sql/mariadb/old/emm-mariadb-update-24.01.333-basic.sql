-- Make sure there is no more WorkflowStopHandler
-- Make sure there is exactly one WorkflowStateHandler

DELETE FROM job_queue_result_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE runclass = 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowStopJobWorker');
DELETE FROM job_queue_parameter_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE runclass = 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowStopJobWorker');
DELETE FROM job_queue_tbl where runclass = 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowStopJobWorker';

DELETE FROM job_queue_result_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE runclass = 'com.agnitas.emm.core.workflow.service.jobs.WorkflowStateTransitionJobWorker');
DELETE FROM job_queue_parameter_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE runclass = 'com.agnitas.emm.core.workflow.service.jobs.WorkflowStateTransitionJobWorker');
DELETE FROM job_queue_tbl where runclass = 'com.agnitas.emm.core.workflow.service.jobs.WorkflowStateTransitionJobWorker';

INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted, criticality)
	VALUES ('WorkflowStateHandler', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '***0;***5', CURRENT_TIMESTAMP, NULL, 'com.agnitas.emm.core.workflow.service.jobs.WorkflowStateTransitionJobWorker', 0, 5);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('24.01.333', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
