UPDATE admin_group_tbl SET shortname = 'OpenEMM', description = 'OpenEMM' WHERE company_id = 1 AND admin_group_id = 1;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.07.019', CURRENT_USER, CURRENT_TIMESTAMP);