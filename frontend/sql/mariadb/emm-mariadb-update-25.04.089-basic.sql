INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, creation_date) VALUES ('webhooks.admin', 'ImportExport', 'Webhooks', 1, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, creation_date) VALUES ('webhooks.enable', 'ImportExport', 'Webhooks', 2, CURRENT_TIMESTAMP);

INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name) (SELECT admin_group_id, 'webhooks.admin' FROM admin_group_tbl WHERE shortname = 'OpenEMM');
INSERT INTO admin_group_permission_tbl (admin_group_id, permission_name) (SELECT admin_group_id, 'webhooks.enable' FROM admin_group_tbl WHERE shortname = 'OpenEMM');

INSERT INTO admin_permission_tbl (admin_id, permission_name) VALUES (1, 'webhooks.admin');
INSERT INTO admin_permission_tbl (admin_id, permission_name) VALUES (1, 'webhooks.enable');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('25.04.089', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
