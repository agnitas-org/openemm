INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order) VALUES ('Statistics', 'Reports', 3);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('24.07.450', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
