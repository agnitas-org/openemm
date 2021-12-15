DELETE FROM permission_subcategory_tbl WHERE category_name = 'Statistics' AND subcategory_name = 'Reports';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.10.193', CURRENT_USER, CURRENT_TIMESTAMP);
