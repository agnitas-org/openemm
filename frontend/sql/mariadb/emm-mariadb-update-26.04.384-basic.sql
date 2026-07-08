CREATE TABLE IF NOT EXISTS webservice_usage_log_tbl
(
    timestamp  TIMESTAMP        NOT NULL COMMENT 'Timestamp when webservice was invoked',
    endpoint   VARCHAR(1000)    NOT NULL COMMENT 'Invoked webservice',
    company_id INT(11) UNSIGNED NOT NULL COMMENT 'Company ID of webservice user',
    username   VARCHAR(200)     NOT NULL COMMENT 'Name of webservice user',
    client_ip  VARCHAR(100) COMMENT 'Ip address of the client from which the request was sent',

    INDEX wsus$cidtsusr$idx (company_id, timestamp, username)
) ENGINE = InnoDB  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Stores usage information on webservices';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.04.384', CURRENT_USER, CURRENT_TIMESTAMP);
COMMIT;
