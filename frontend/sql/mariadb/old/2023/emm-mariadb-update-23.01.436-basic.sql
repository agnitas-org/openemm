DELETE FROM config_tbl WHERE class = 'security' AND name = 'csrfProtection.enabled';
INSERT INTO config_tbl (class, name, value) VALUES ('security', 'csrfProtection.enabled', 'true');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.01.436', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
