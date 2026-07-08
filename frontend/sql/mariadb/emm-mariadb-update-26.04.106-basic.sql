INSERT INTO company_info_tbl (company_id, cname, cvalue) VALUES (0, 'bavd:inbox-enabled', 'true');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('26.04.106', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
