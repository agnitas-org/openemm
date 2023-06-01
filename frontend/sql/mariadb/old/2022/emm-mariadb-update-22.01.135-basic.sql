INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('NegativePermissions', 12);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.01.135', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
