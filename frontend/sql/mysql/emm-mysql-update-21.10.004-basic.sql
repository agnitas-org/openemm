DELETE FROM company_permission_tbl WHERE permission_name IN (SELECT permission_name FROM permission_tbl WHERE category = 'NegativePermissions');
DELETE FROM admin_permission_tbl WHERE permission_name IN (SELECT permission_name FROM permission_tbl WHERE category = 'NegativePermissions');
DELETE FROM admin_group_permission_tbl WHERE permission_name IN (SELECT permission_name FROM permission_tbl WHERE category = 'NegativePermissions');
DELETE FROM permission_tbl WHERE category = 'NegativePermissions';
DELETE FROM permission_category_tbl WHERE category_name = 'NegativePermissions';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.10.004', CURRENT_USER, CURRENT_TIMESTAMP);
