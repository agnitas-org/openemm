DELETE FROM admin_group_tbl WHERE admin_group_id = 1;
INSERT INTO admin_group_tbl (admin_group_id, company_id, shortname, description) VALUES (1, 1, 'OpenEMM', 'OpenEMM');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.07.019', CURRENT_USER, CURRENT_TIMESTAMP);