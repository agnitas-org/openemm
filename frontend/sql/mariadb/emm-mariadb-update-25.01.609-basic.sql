CREATE TABLE IF NOT EXISTS webhook_url_tbl (
     company_ref                INT(11) UNSIGNED NOT NULL COMMENT 'Company ID',
     event_type                 INT(4) UNSIGNED NOT NULL COMMENT 'Event type (see WebserviceEventType enum for encoding)',
     webhook_url                VARCHAR(1000) NOT NULL COMMENT 'Webhook URL',
     PRIMARY KEY(company_ref, event_type),
     CONSTRAINT webhook_url$cid$fk FOREIGN KEY (company_ref) REFERENCES company_tbl (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Configured webhook URLs';

CREATE TABLE IF NOT EXISTS webhook_message_tbl
(
    message_id      BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Unique message ID',
    company_ref     INT(11) UNSIGNED NOT NULL COMMENT 'Company ID',
    event_type      INT(4) UNSIGNED NOT NULL COMMENT 'Event type (see WebserviceEventType enum for encoding)',
    event_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Timestamp of event',
    send_timestamp  TIMESTAMP NULL COMMENT 'Send time of message',
    retry_count     INTEGER(4) NOT NULL COMMENT 'Number of current send attempts',
    status          INT(1) UNSIGNED NOT NULL COMMENT 'Status of message',
    payload         LONGTEXT                            NOT NULL COMMENT 'Payload of message',
    status_note     VARCHAR(1000) COMMENT 'Note on current status',
    PRIMARY KEY (message_id),
    CONSTRAINT webhook_msg$cid$fk FOREIGN KEY (company_ref) REFERENCES company_tbl (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Webhook messages to send';


CREATE TABLE IF NOT EXISTS webhook_process_timestamp_tbl
(
    company_ref  INT(11) UNSIGNED NOT NULL COMMENT 'Company ID',
    process_type INT(4) UNSIGNED NOT NULL COMMENT 'Type of process (1=mail delivery data, 2=bounces)',
    timestamp    TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Timestamp of last processing',
    PRIMARY KEY (company_ref, process_type),
    CONSTRAINT webhook_prcts$cid$fk FOREIGN KEY (company_ref) REFERENCES company_tbl (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Timestamps for webhook processing';

CREATE TABLE IF NOT EXISTS webhook_profile_field_tbl
(
    company_ref   INT(11) UNSIGNED NOT NULL COMMENT 'Company ID',
    event_type    INT(4) UNSIGNED NOT NULL COMMENT 'Event type (see WebserviceEventType enum for encoding)',
    profile_field VARCHAR(100) NOT NULL COMMENT 'Name of profile field',
    CONSTRAINT webhook_prfld$cid$fk FOREIGN KEY (company_ref) REFERENCES company_tbl (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'List of profile fields to include in webhook messages';

CREATE TABLE IF NOT EXISTS webhook_backend_data_tbl
(
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'ID of record',
    creation_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Creation timestamp of record',
    event_timestamp TIMESTAMP NULL COMMENT 'Timestamp of event',
    event_type      INT(4) UNSIGNED NOT NULL COMMENT 'Event type (see WebserviceEventType enum for encoding)',
    company_id      INT(11) UNSIGNED NOT NULL COMMENT 'Company ID',
    mailing_id      INT(11) UNSIGNED COMMENT 'Mailing ID',
    recipient_id    INT(11) UNSIGNED COMMENT 'Recipient ID',
    CONSTRAINT webhook_backdata$cid$fk FOREIGN KEY (company_id) REFERENCES company_tbl (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Raw backend data for webhook messages';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('25.01.609', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
