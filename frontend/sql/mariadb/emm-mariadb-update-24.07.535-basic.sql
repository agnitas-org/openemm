INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('use.old.ui', 'System', 'Migration', 0, NULL, CURRENT_TIMESTAMP);

DELETE FROM admin_permission_tbl WHERE permission_name = 'ui.design.migration';
DELETE FROM company_permission_tbl WHERE permission_name = 'ui.design.migration';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'ui.design.migration';
DELETE FROM permission_tbl WHERE permission_name = 'ui.design.migration';

DELETE FROM admin_permission_tbl WHERE permission_name = 'use.redesigned.ui';
DELETE FROM company_permission_tbl WHERE permission_name = 'use.redesigned.ui';
DELETE FROM admin_group_permission_tbl WHERE permission_name = 'use.redesigned.ui';
DELETE FROM permission_tbl WHERE permission_name = 'use.redesigned.ui';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('24.07.535', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
