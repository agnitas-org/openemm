DELETE FROM job_queue_result_tbl WHERE job_id = (SELECT id FROM job_queue_tbl WHERE description = 'AggregateRdirTrafficStatisticJobWorker');
DELETE FROM job_queue_tbl WHERE description = 'AggregateRdirTrafficStatisticJobWorker';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.10.000', CURRENT_USER, CURRENT_TIMESTAMP);