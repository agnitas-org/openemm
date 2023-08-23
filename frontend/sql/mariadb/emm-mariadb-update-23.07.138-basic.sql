-- Execute changes after version is available on all systems: 23.04.001. And delete java class ComWorkflowReportSendJobWorker
-- GWUA-5483
DELETE FROM job_queue_result_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE runclass = 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowReportSendJobWorker');
DELETE FROM job_queue_parameter_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE runclass = 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowReportSendJobWorker');
DELETE from job_queue_tbl where runclass = 'com.agnitas.emm.core.workflow.service.jobs.ComWorkflowReportSendJobWorker';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.07.138', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
