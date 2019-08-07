DELETE FROM config_tbl;
INSERT INTO config_tbl (class, name, value) VALUES
	('system', 'licence', 0),
	('system', 'support_emergency_url', 'http://www.openemm.org/systemstoerung'),
	('system', 'RdirLandingpage', 'http://www.openemm.org'),
	('system', '<hostname>[to be defined].IsActive', 1),
	('mailloop', 'actionbased_autoresponder_ui', 'enabled');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('18.10.490', CURRENT_USER, CURRENT_TIMESTAMP);