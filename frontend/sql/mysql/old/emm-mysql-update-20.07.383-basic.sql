INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('clean', 'mastercompany', 'true', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.07.383', CURRENT_USER, CURRENT_TIMESTAMP);