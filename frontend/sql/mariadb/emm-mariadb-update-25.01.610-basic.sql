DELETE FROM job_queue_result_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE runclass='com.agnitas.emm.core.webhooks.jobqueue.WebhookMailingDeliveryMessageGeneratorJobWorker');
DELETE FROM job_queue_result_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE runclass='com.agnitas.emm.core.webhooks.jobqueue.WebhookHardbounceMessageGeneratorJobWorker');

DELETE FROM job_queue_tbl WHERE runclass = 'com.agnitas.emm.core.webhooks.jobqueue.WebhookMailingDeliveryMessageGeneratorJobWorker';
DELETE FROM job_queue_tbl WHERE runclass = 'com.agnitas.emm.core.webhooks.jobqueue.WebhookHardbounceMessageGeneratorJobWorker';

INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted, runonlyonhosts)
VALUES ('WebhookBackendDataMessageGenerator', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '****', CURRENT_TIMESTAMP, NULL, 'com.agnitas.emm.core.webhooks.jobqueue.WebhookBackendDataMessageGeneratorJobWorker', 0, null);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('25.01.610', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
