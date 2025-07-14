INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
	VALUES ('WebhookMessageDelivery', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '****', CURRENT_TIMESTAMP, NULL, 'com.agnitas.emm.core.webhooks.jobqueue.WebhookMessageSenderJobWorker', 0);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
	VALUES ('WebhookMailingDeliveryMessageGenerator', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '****', CURRENT_TIMESTAMP, NULL, 'com.agnitas.emm.core.webhooks.jobqueue.WebhookMailingDeliveryMessageGeneratorJobWorker', 0);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
	VALUES ('WebhookHardbounceMessageGenerator', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '****', CURRENT_TIMESTAMP, NULL, 'com.agnitas.emm.core.webhooks.jobqueue.WebhookHardbounceMessageGeneratorJobWorker', 0);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('25.01.064', CURRENT_USER, CURRENT_TIMESTAMP);
