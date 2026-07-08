CREATE TABLE IF NOT EXISTS webservice_usage_log_tbl
(
    timestamp  TIMESTAMP     NOT NULL,
    endpoint   VARCHAR(1000) NOT NULL,
    company_id INTEGER       NOT NULL,
    username   VARCHAR(200)  NOT NULL,
    client_ip  VARCHAR(100)
);
CREATE INDEX IF NOT EXISTS wsus$cidtsusr$idx ON webservice_usage_log_tbl (company_id, timestamp, username);
COMMENT ON TABLE webservice_usage_log_tbl IS 'Stores usage information on webservices';
COMMENT ON COLUMN webservice_usage_log_tbl.timestamp IS 'Timestamp when webservice was invoked';
COMMENT ON COLUMN webservice_usage_log_tbl.endpoint IS 'Invoked webservice';
COMMENT ON COLUMN webservice_usage_log_tbl.company_id IS 'Company ID of webservice user';
COMMENT ON COLUMN webservice_usage_log_tbl.username IS 'Name of webservice user';
COMMENT ON COLUMN webservice_usage_log_tbl.client_ip IS 'Ip address of the client from which the request was sent';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.04.384', CURRENT_USER, CURRENT_TIMESTAMP);
COMMIT;
