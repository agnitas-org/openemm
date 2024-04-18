DROP TABLE auto_import_used_files_tbl;
DROP TABLE auto_import_mlist_bind_tbl;
DROP TABLE auto_import_result_tbl;
DROP TABLE auto_import_tbl;
DROP TABLE auto_export_result_tbl;
DROP TABLE auto_export_tbl;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.04.381', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
