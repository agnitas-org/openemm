UPDATE job_queue_tbl SET runOnlyOnHosts = NULL WHERE description = 'WebhookBackendDataMessageGenerator';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('25.01.611', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
