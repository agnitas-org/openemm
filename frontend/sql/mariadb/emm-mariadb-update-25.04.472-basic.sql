UPDATE permission_tbl SET sub_category="webhooks" WHERE permission_name = 'webhooks.enable';
UPDATE permission_tbl SET sub_category="webhooks" WHERE permission_name = 'webhooks.admin';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('25.04.472', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
