INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date)
VALUES ('inbox.show', 'Administration', 'settings.Mailloop', 4, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date)
VALUES ('inbox.delete', 'Administration', 'settings.Mailloop', 5, NULL, CURRENT_TIMESTAMP);

INSERT INTO company_info_tbl (company_id, cname, cvalue) VALUES (0, 'bavd:inbox-enabled', 'true');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.04.106', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
