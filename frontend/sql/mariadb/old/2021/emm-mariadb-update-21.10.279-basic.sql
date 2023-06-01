DELETE FROM job_queue_result_tbl WHERE job_id = (SELECT id FROM job_queue_tbl WHERE description = 'DBCleaner');
DELETE FROM job_queue_tbl WHERE description = 'DBCleaner';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.10.279', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
