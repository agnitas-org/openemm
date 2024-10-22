INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('ui.design.migration', 'System', 'Migration', 0, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('use.redesigned.ui', 'System', 'Migration', 0, NULL, CURRENT_TIMESTAMP);

INSERT INTO admin_permission_tbl (admin_id, permission_name)
SELECT a.admin_id, 'ui.design.migration'
FROM admin_tbl a
WHERE NOT EXISTS(SELECT 1
                 FROM admin_permission_tbl ap
                 WHERE ap.permission_name = 'ui.design.migration'
                   AND ap.admin_id = a.admin_id);

INSERT INTO admin_permission_tbl (admin_id, permission_name)
SELECT a.admin_id, 'use.redesigned.ui'
FROM admin_tbl a
WHERE NOT EXISTS(SELECT 1
                 FROM admin_permission_tbl ap
                 WHERE ap.permission_name = 'use.redesigned.ui'
                   AND ap.admin_id = a.admin_id);


INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('24.07.449', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
