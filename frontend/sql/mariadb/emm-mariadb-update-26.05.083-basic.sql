-- remove JobWorker from OpenEMM that was only removed from EMM (emm-mariadb-update-25.04.515-extended.sql)
DELETE FROM job_queue_result_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE job_name = 'DeletedContentBlockCleaner');
DELETE FROM job_queue_parameter_tbl WHERE job_id IN (SELECT id FROM job_queue_tbl WHERE job_name = 'DeletedContentBlockCleaner');
DELETE FROM job_queue_tbl WHERE job_name = 'DeletedContentBlockCleaner';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('26.05.083', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;