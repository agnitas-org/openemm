CREATE TABLE IF NOT EXISTS mailloop_replies_tbl
(
    id                  INT(11)          NOT NULL AUTO_INCREMENT COMMENT 'Unique entry id',
    mailloop_id         INT(11) UNSIGNED NOT NULL COMMENT 'Referenced bounce filter',
    status              INT(1)    DEFAULT 0 COMMENT 'Reply status: 0 - unread, 1 - read',
    sender_full_name    VARCHAR(200) COMMENT 'Full name of the sender',
    subject             VARCHAR(200) COMMENT 'Subject of the reply',
    sender_email        VARCHAR(200) COMMENT 'Email of the sender',
    response_email      VARCHAR(200) COMMENT 'Email that was responded to',
    timestamp           TIMESTAMP COMMENT 'Timestamp of reply',
    creation_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp of creation',
    change_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp of last change',
    content             LONGTEXT COMMENT 'Content of the reply',
    content_type        INT(1)    DEFAULT 0 COMMENT 'Type of the content: 0 - text/plain, 1 - text/html',
    raw_message         LONGBLOB COMMENT 'Message including original headers',
    customer_id         INTEGER UNSIGNED COMMENT 'ID of original recipient',
    company_id          INT(11) UNSIGNED NOT NULL COMMENT 'tenant - ID (company_tbl)',
    customer_company_id INT(11) UNSIGNED COMMENT 'ID of company of original recipient',
    PRIMARY KEY (id),
    CONSTRAINT mlooprepl$mlid$fk FOREIGN KEY (mailloop_id) REFERENCES mailloop_tbl (rid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_unicode_ci COMMENT 'Replies to mailings without forward address set in bounce filter';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.04.108', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
