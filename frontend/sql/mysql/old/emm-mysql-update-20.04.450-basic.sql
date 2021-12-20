CREATE TABLE serverprio_tbl (
	company_id                 INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'tenant - ID (company_tbl)',
	mailing_id                 INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'references mailing (mailing_tbl.mailing_id)',
	priority                   INT(10) COMMENT '0=disable further sending, >0 sending priority in relation to other ready to send mailings (the higher the value the lower the priority)',
	start_date                 TIMESTAMP NULL COMMENT '(optional) entry validation start-date',
	end_date                   TIMESTAMP NULL COMMENT '(optional) entry validation end-date'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'allowes to set higher or lower sending priority per mailing or tenant';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.04.450', CURRENT_USER, CURRENT_TIMESTAMP);