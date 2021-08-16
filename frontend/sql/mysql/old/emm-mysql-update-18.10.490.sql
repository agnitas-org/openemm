DELETE FROM config_tbl;
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('system', 'licence', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('system', 'support_emergency_url', 'http://www.openemm.org/systemstoerung', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('system', 'RdirLandingpage', 'http://www.openemm.org', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('system', '<hostname>[to be defined].IsActive', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('mailloop', 'actionbased_autoresponder_ui', 'enabled', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('18.10.490', CURRENT_USER, CURRENT_TIMESTAMP);