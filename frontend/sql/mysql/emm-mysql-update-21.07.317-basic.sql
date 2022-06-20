DELETE FROM permission_category_tbl WHERE category_name = 'Premium';

DELETE FROM permission_subcategory_tbl WHERE category_name = 'Mailing' AND subcategory_name = 'Mediapool';
DELETE FROM permission_subcategory_tbl WHERE category_name = 'Administration' AND subcategory_name = 'ContentSource';
DELETE FROM permission_subcategory_tbl WHERE category_name = 'Administration' AND  subcategory_name = 'Company';


INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.07.317', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
