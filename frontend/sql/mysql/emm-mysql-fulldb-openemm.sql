DELETE FROM config_tbl;
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('system', 'licence', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('system', 'support_emergency_url', 'http://www.openemm.org/systemstoerung', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('system', 'RdirLandingpage', 'http://www.openemm.org', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('system', '<hostname>[to be defined].IsActive', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('mailloop', 'actionbased_autoresponder_ui', 'enabled', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES ('clean', 'mastercompany', 'true', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');

INSERT INTO company_info_tbl (company_id, cname, cvalue) VALUES (0, 'ImageTrafficMeasuring', 'false');

DELETE FROM job_queue_tbl WHERE description = 'AggregateRdirTrafficStatisticJobWorker';

UPDATE admin_group_tbl SET shortname = 'OpenEMM', description = 'OpenEMM' WHERE company_id = 1 AND admin_group_id = 1;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('18.10.490', CURRENT_USER, CURRENT_TIMESTAMP);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.10.000', CURRENT_USER, CURRENT_TIMESTAMP);
INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.10.404', CURRENT_USER, CURRENT_TIMESTAMP);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.04.450', CURRENT_USER, CURRENT_TIMESTAMP);
	
INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.07.019', CURRENT_USER, CURRENT_TIMESTAMP);
INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.07.383', CURRENT_USER, CURRENT_TIMESTAMP);
