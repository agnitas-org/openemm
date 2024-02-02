INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.content.readonly', 'NegativePermissions', NULL, 3, NULL);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.10.503', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
