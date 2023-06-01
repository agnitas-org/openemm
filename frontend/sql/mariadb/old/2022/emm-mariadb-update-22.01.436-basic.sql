UPDATE config_tbl SET hostname = NULL WHERE class = 'jobqueue' AND name = 'execute' AND hostname = '<hostname> [to be defined]';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.01.436', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
