INSERT INTO config_tbl (class, name, value) VALUES ('birt', 'url.intern', 'http://localhost:8080/birt');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('24.04.220', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
