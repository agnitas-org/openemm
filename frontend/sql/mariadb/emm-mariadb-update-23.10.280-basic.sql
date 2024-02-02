INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package)
	(SELECT 'mailing.recipients.show', 'Mailing', 'Settings', 7, NULL FROM permission_tbl WHERE NOT EXISTS (SELECT 1 FROM permission_tbl WHERE permission_name = 'mailing.recipients.show') LIMIT 1);
UPDATE permission_tbl SET category = 'Mailing', sub_category = 'Settings', sort_order = 7, feature_package = NULL WHERE permission_name = 'mailing.recipients.show';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.10.280', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
