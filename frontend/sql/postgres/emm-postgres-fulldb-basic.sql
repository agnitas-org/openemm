/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE prevent_table_drop
(
    id             INTEGER,
    text           VARCHAR(4000),
    customer_id    INTEGER,
    mailinglist_id INTEGER,
    mediatype      INTEGER,
    change_date    TIMESTAMP
);
COMMENT ON TABLE prevent_table_drop IS 'saves important tables from inadvertently dropping by FK-Constraints';
COMMENT ON COLUMN prevent_table_drop.id IS 'Referenced integer id';
COMMENT ON COLUMN prevent_table_drop.text IS 'Referenced text';
COMMENT ON COLUMN prevent_table_drop.customer_id IS 'Referenced customer_id';
COMMENT ON COLUMN prevent_table_drop.mailinglist_id IS 'Referenced mailing_id';
COMMENT ON COLUMN prevent_table_drop.mediatype IS 'Referenced mediatype integer';

CREATE TABLE admin_group_tbl
(
    admin_group_id SERIAL PRIMARY KEY,
    company_id     INTEGER,
    shortname      VARCHAR(100),
    description    VARCHAR(1000),
    deleted        SMALLINT   DEFAULT 0 NOT NULL,
    change_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_accounting;
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$admin_group_tbl FOREIGN KEY (id) REFERENCES admin_group_tbl (admin_group_id);
COMMENT ON TABLE admin_group_tbl IS 'stores groups for users to handle permissions easier';
COMMENT ON COLUMN admin_group_tbl.admin_group_id IS 'unique group ID, use admin_group_tbl_seq';
COMMENT ON COLUMN admin_group_tbl.company_id IS 'tenant - use company_ID 1 for generic groups';
COMMENT ON COLUMN admin_group_tbl.shortname IS 'name of the admin groups';
COMMENT ON COLUMN admin_group_tbl.description IS 'description of the admin groups';
COMMENT ON COLUMN admin_group_tbl.deleted IS 'deleted? 0 = no, 1 = yes';
COMMENT ON COLUMN admin_group_tbl.change_date IS 'group last change date';

CREATE TABLE admin_group_permission_tbl
(
    admin_group_id  INTEGER,
    permission_name VARCHAR(40)
) TABLESPACE data_accounting;
ALTER TABLE admin_group_permission_tbl ADD CONSTRAINT admin_grp_tbl$id_permname$pk PRIMARY KEY (admin_group_id, permission_name);
ALTER TABLE admin_group_permission_tbl ADD CONSTRAINT admingrpperm$admingrpid$fk FOREIGN KEY (admin_group_id) REFERENCES admin_group_tbl (admin_group_id) ON DELETE CASCADE;
COMMENT ON TABLE admin_group_permission_tbl IS 'assign permissions to admin_groups';
COMMENT ON COLUMN admin_group_permission_tbl.admin_group_id IS 'references to admin_group_tbl (FK, on delete cascade)';
COMMENT ON COLUMN admin_group_permission_tbl.permission_name IS 'name of the permission granted by this entry';

CREATE TABLE admin_tbl
(
    admin_id                  SERIAL PRIMARY KEY,
    username                  VARCHAR(200),
    company_id                INTEGER,
    fullname                  VARCHAR(100),
    admin_country             VARCHAR(10) DEFAULT 'de',
    admin_lang                VARCHAR(10) DEFAULT 'DE',
    admin_lang_variant        VARCHAR(10),
    admin_timezone            VARCHAR(50) DEFAULT 'Europe/Berlin',
    pwdchange_date            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    stat_email                VARCHAR(200),
    default_import_profile_id INTEGER     DEFAULT 0,
    creation_date             TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    timestamp                 TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    layout_base_id            INTEGER     DEFAULT 0,
    company_name              VARCHAR(100),
    email                     VARCHAR(200),
    secure_password_hash      VARCHAR(200),
    firstname                 VARCHAR(100),
    gender                    SMALLINT    DEFAULT 2,
    title                     VARCHAR(300),
    news_date                 TIMESTAMP,
    message_date              TIMESTAMP,
    phone_number              VARCHAR(100),
    limiting_target_id        INTEGER     DEFAULT NULL,
    last_login_date           TIMESTAMP NULL,
    restful                   INTEGER     DEFAULT 0 NOT NULL,
    employee_id               VARCHAR(100),
    totp_secret               VARCHAR(100),
    dashboard_layout          VARCHAR(1000),
    password_reminder         INTEGER     DEFAULT NULL,
    layout_type               SMALLINT    DEFAULT 0 NOT NULL
) TABLESPACE data_accounting;
ALTER TABLE admin_tbl ADD CONSTRAINT admin$admid$nn CHECK (admin_id IS NOT NULL);
ALTER TABLE admin_tbl ADD CONSTRAINT admin$uname$nn CHECK (username IS NOT NULL);
ALTER TABLE admin_tbl ADD CONSTRAINT admin$defimppro$nn CHECK (default_import_profile_id IS NOT NULL);
CREATE UNIQUE INDEX admin_tbl$username$uq ON admin_tbl (username) TABLESPACE data_accounting;
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$admin_tbl FOREIGN KEY (id) REFERENCES admin_tbl (admin_id);
COMMENT ON TABLE admin_tbl IS '[private_data] stores EMM-Users';
COMMENT ON COLUMN admin_tbl.admin_id IS 'unique ID (PK), use admin_tbl_seq';
COMMENT ON COLUMN admin_tbl.username IS '[private_data] EMM-Login';
COMMENT ON COLUMN admin_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN admin_tbl.fullname IS '[private_data] Name of the EMM-User, shown in GUI';
COMMENT ON COLUMN admin_tbl.admin_country IS 'home country of the administrator according to domain notation';
COMMENT ON COLUMN admin_tbl.admin_lang IS 'language of the admin';
COMMENT ON COLUMN admin_tbl.admin_lang_variant IS 'additional information on the language setting';
COMMENT ON COLUMN admin_tbl.admin_timezone IS 'timezone, used for any date / timebased actions to bring in line with users local time';
COMMENT ON COLUMN admin_tbl.pwdchange_date IS 'last password - change to check for expired passwords';
COMMENT ON COLUMN admin_tbl.stat_email IS '[private_data] used for reports like ADV - Report, deledt mails report';
COMMENT ON COLUMN admin_tbl.creation_date IS 'user created';
COMMENT ON COLUMN admin_tbl.timestamp IS 'user last changed';
COMMENT ON COLUMN admin_tbl.company_name IS '[private_data] EMM-tenant name, shown in GUI';
COMMENT ON COLUMN admin_tbl.phone_number IS 'Phone number of admin in case for any queries';
COMMENT ON COLUMN admin_tbl.default_import_profile_id IS 'Preference setting which import profile to be used by default';
COMMENT ON COLUMN admin_tbl.layout_base_id IS 'Used for layout configuration in whitelabel applications';
COMMENT ON COLUMN admin_tbl.email IS 'Email of this admin';
COMMENT ON COLUMN admin_tbl.secure_password_hash IS 'Secured hash of admins password';
COMMENT ON COLUMN admin_tbl.firstname IS 'Admins firstname';
COMMENT ON COLUMN admin_tbl.gender IS 'Admins gender (0=male, 1=female, 0=unknown)';
COMMENT ON COLUMN admin_tbl.title IS 'Admins optional title';
COMMENT ON COLUMN admin_tbl.news_date IS 'Date of last news entry shown to admin';
COMMENT ON COLUMN admin_tbl.message_date IS 'Date of last message entry shown to admin';
COMMENT ON COLUMN admin_tbl.limiting_target_id IS 'Id of access limiting target group.';
COMMENT ON COLUMN admin_tbl.last_login_date IS 'Timestamp of last successful login';
COMMENT ON COLUMN admin_tbl.restful IS 'User may utilize the restful webservices';
COMMENT ON COLUMN admin_tbl.employee_id IS 'Tennant specific employee id';
COMMENT ON COLUMN admin_tbl.totp_secret IS 'Shared secret for TOTP';
COMMENT ON COLUMN admin_tbl.dashboard_layout IS 'Stores dashboard layout settings in JSON that contains info about grid size, selected tiles and their position inside the grid';
COMMENT ON COLUMN admin_tbl.password_reminder IS '0 - not required, 1 - required, 2 - sent';
COMMENT ON COLUMN admin_tbl.layout_type IS 'Defines UI layout mode. 0 - standard, 1 - left-handed';

CREATE TABLE admin_permission_tbl
(
    admin_id        INTEGER,
    permission_name VARCHAR(40)
) TABLESPACE data_accounting;
ALTER TABLE admin_permission_tbl ADD CONSTRAINT adminperm$id_sectkn$uq PRIMARY KEY (admin_id, permission_name);
ALTER TABLE admin_permission_tbl ADD CONSTRAINT adminperm$adminid$fk FOREIGN KEY (admin_id) REFERENCES admin_tbl (admin_id) ON DELETE CASCADE;
COMMENT ON TABLE admin_permission_tbl IS 'assign permissions to users';
COMMENT ON COLUMN admin_permission_tbl.admin_id IS 'references to admin_tbl (FK, on delete cascade)';
COMMENT ON COLUMN admin_permission_tbl.permission_name IS 'name of the permission granted by this entry';

CREATE TABLE auto_optimization_tbl
(
    company_id        INTEGER,
    campaign_id       INTEGER,
    description       VARCHAR(1000),
    shortname         VARCHAR(100),
    eval_type         INTEGER,
    group1_id         INTEGER,
    group2_id         INTEGER,
    group3_id         INTEGER,
    group4_id         INTEGER,
    group5_id         INTEGER,
    mailinglist_id    INTEGER,
    optimization_id   SERIAL PRIMARY KEY,
    result_mailing_id INTEGER,
    result_senddate   TIMESTAMP,
    send_delay        INTEGER,
    split_type        VARCHAR(100),
    status            INTEGER,
    target_id         INTEGER,
    creation_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    target_expression VARCHAR(300),
    target_mode       INTEGER   DEFAULT 1,
    final_mailing_id  INTEGER,
    threshold         INTEGER,
    double_check      INTEGER,
    deleted           SMALLINT   DEFAULT 0,
    test_senddate     TIMESTAMP,
    test_run          INTEGER   DEFAULT 0 NOT NULL,
    workflow_id       INTEGER   DEFAULT 0 NOT NULL
) TABLESPACE data_accounting;
ALTER TABLE auto_optimization_tbl ADD CONSTRAINT autooptimiz$testrun$ck CHECK (test_run IN (0, 1));
ALTER TABLE auto_optimization_tbl ADD CONSTRAINT autooptimiz$deleted$nn CHECK (deleted IS NOT NULL);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$auto_opt_tbl FOREIGN KEY (id) REFERENCES auto_optimization_tbl (optimization_id);
COMMENT ON TABLE auto_optimization_tbl IS 'processing table for auto-optimization';
COMMENT ON COLUMN auto_optimization_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN auto_optimization_tbl.campaign_id IS 'ID of the mailing-archive';
COMMENT ON COLUMN auto_optimization_tbl.description IS 'description on auto-optimization';
COMMENT ON COLUMN auto_optimization_tbl.shortname IS 'auto-optimization name';
COMMENT ON COLUMN auto_optimization_tbl.eval_type IS 'threshold - evaluation type: 1 = open rate, 2 = clicks, 3 = sales volume';
COMMENT ON COLUMN auto_optimization_tbl.group1_id IS '1st basic-mailing-ID';
COMMENT ON COLUMN auto_optimization_tbl.group2_id IS '2nd basic-mailing-ID';
COMMENT ON COLUMN auto_optimization_tbl.mailinglist_id IS 'ID of related mailinglist';
COMMENT ON COLUMN auto_optimization_tbl.optimization_id IS 'unique ID, use AUTO_OPTIMIZATION_TBL_SEQ';
COMMENT ON COLUMN auto_optimization_tbl.result_mailing_id IS 'winner-mailing ID (from GROUP1_ID ... GROUP5_ID)';
COMMENT ON COLUMN auto_optimization_tbl.result_senddate IS 'winner- mailing send date';
COMMENT ON COLUMN auto_optimization_tbl.send_delay IS 'legacy EMM-4955';
COMMENT ON COLUMN auto_optimization_tbl.split_type IS 'defines split-parts for basic / final mailings';
COMMENT ON COLUMN auto_optimization_tbl.status IS 'auto-optimization state: 0=not started yet, 1=test-sending, 2= evaluation in progress, 3 = done, 4 = scheduled';
COMMENT ON COLUMN auto_optimization_tbl.target_id IS 'legacy';
COMMENT ON COLUMN auto_optimization_tbl.creation_date IS 'auto-optimization creation date';
COMMENT ON COLUMN auto_optimization_tbl.change_date IS 'auto-optimization last change date';
COMMENT ON COLUMN auto_optimization_tbl.target_expression IS 'combination of related targetIDs';
COMMENT ON COLUMN auto_optimization_tbl.target_mode IS 'combination_tye of target_groups: 0 = OR, 1 = AND';
COMMENT ON COLUMN auto_optimization_tbl.final_mailing_id IS 'final-mailing_id (auto-generated copy of the winner-mailing)';
COMMENT ON COLUMN auto_optimization_tbl.threshold IS 'threshold - value, reaching threshold value before end of evaluation periode triggers sending';
COMMENT ON COLUMN auto_optimization_tbl.double_check IS 'use double-check: 0 = no, 1 = yes';
COMMENT ON COLUMN auto_optimization_tbl.deleted IS 'deleted? 0 = no, 1 = yes';
COMMENT ON COLUMN auto_optimization_tbl.test_senddate IS 'test scheduled date';
COMMENT ON COLUMN auto_optimization_tbl.test_run IS 'test-run - mode: 0 = no, 1 = yes';
COMMENT ON COLUMN auto_optimization_tbl.workflow_id IS 'references workflow';
COMMENT ON COLUMN auto_optimization_tbl.group3_id IS '3rd basic-mailing-ID';
COMMENT ON COLUMN auto_optimization_tbl.group4_id IS '4th basic-mailing-ID';
COMMENT ON COLUMN auto_optimization_tbl.group5_id IS '5th basic-mailing-ID';

CREATE TABLE bounce_tbl
(
    dsn         INTEGER,
    bounce_id   SERIAL PRIMARY KEY,
    company_id  INTEGER,
    customer_id INTEGER,
    detail      INTEGER,
    mailing_id  INTEGER,
    timestamp   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_bounce;
COMMENT ON TABLE bounce_tbl IS 'processing / handling bounces for all EMM-tenants';
COMMENT ON COLUMN bounce_tbl.dsn IS 'Code reported by mailsystem';
COMMENT ON COLUMN bounce_tbl.bounce_id IS 'legacy';
COMMENT ON COLUMN bounce_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN bounce_tbl.customer_id IS 'ID of the recipient';
COMMENT ON COLUMN bounce_tbl.detail IS 'detail-code for bounce-reason';
COMMENT ON COLUMN bounce_tbl.timestamp IS 'bounce-timestamp';
COMMENT ON COLUMN bounce_tbl.mailing_id IS 'ID of the Mailing';

CREATE TABLE campaign_tbl
(
    campaign_id   SERIAL PRIMARY KEY,
    company_id    INTEGER,
    shortname     VARCHAR(100),
    description   VARCHAR(1000),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_cust_table;
ALTER TABLE campaign_tbl ADD CONSTRAINT campaign$coid$nn CHECK (company_id IS NOT NULL);
COMMENT ON TABLE campaign_tbl IS 'Table for archives, used like a folder to group mailings';
COMMENT ON COLUMN campaign_tbl.campaign_id IS 'unique ID, use campaign_tbl_seq';
COMMENT ON COLUMN campaign_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN campaign_tbl.shortname IS 'archive - name';
COMMENT ON COLUMN campaign_tbl.description IS 'comment on entry';
COMMENT ON COLUMN campaign_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN campaign_tbl.change_date IS 'entry last change';

CREATE TABLE company_tbl
(
    rdir_domain                VARCHAR(100),
    company_id                 SERIAL PRIMARY KEY,
    shortname                  VARCHAR(30),
    description                VARCHAR(300),
    status                     VARCHAR(20),
    mailtracking               INTEGER     DEFAULT 0,
    creator_company_id         INTEGER     DEFAULT 1,
    mailerset                  INTEGER     DEFAULT 0,
    customer_type              VARCHAR(50) DEFAULT 'UNKNOWN',
    pricing_id                 INTEGER,
    send_immediately           INTEGER     DEFAULT 0,
    offpeak                    INTEGER     DEFAULT 0,
    notification_email         VARCHAR(100),
    mailloop_domain            VARCHAR(200),
    stat_admin                 INTEGER,
    creation_date              TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    timestamp                  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    sector                     INTEGER,
    business_field             INTEGER,
    secret_key                 VARCHAR(32),
    uid_version                SMALLINT,
    mails_per_day              VARCHAR(500),
    max_recipients             INTEGER     DEFAULT 0,
    salutation_extended        INTEGER     DEFAULT 0,
    enabled_uid_version        INTEGER NOT NULL,
    parent_company_id          INTEGER,
    auto_mailing_report_active INTEGER     DEFAULT 0,
    auto_deeptracking          INTEGER     DEFAULT 0,
    default_datasource_id      INTEGER,
    priority_count             INTEGER,
    contact_tech               VARCHAR(400),
    company_token              VARCHAR(64)
) TABLESPACE data_accounting;
ALTER TABLE company_tbl ADD CONSTRAINT COMPANY$SKEY$UID$CK CHECK ((secret_key IS NULL AND uid_version IS NULL) OR secret_key IS NOT NULL);
ALTER TABLE company_tbl ADD CONSTRAINT comp$status$val CHECK (status in ('active', 'locked', 'todelete', 'deleted', 'deletion in progress', 'toreset'));
CREATE UNIQUE INDEX comp$name$idx ON company_tbl (shortname);
ALTER TABLE admin_tbl ADD CONSTRAINT admin$coid$fk FOREIGN KEY (company_id) REFERENCES company_tbl (company_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$company_tbl FOREIGN KEY (id) REFERENCES company_tbl (company_id);
COMMENT ON TABLE company_tbl IS '[private_data] stores tenant data and partly tenant settings';
COMMENT ON COLUMN company_tbl.company_id IS 'unique ID (PK), use company_tbl_seq';
COMMENT ON COLUMN company_tbl.status IS 'pls use active / deleted / todelete or inactive for temp. deactivation';
COMMENT ON COLUMN company_tbl.mailtracking IS '1 = mailtrack_<company_id>_tbl will be filled, 0 if not';
COMMENT ON COLUMN company_tbl.sector IS 'benchmark - statistics: 0, NONE, 1, AGENCIES, 2, COMMUNITIES, 3, TOURISM, 4, FINANCE, 5, IT, 6, RETAIL, 7, MANUFACTURING_INDUSTRY, 8, CONSUMER_GOODS, 9, PUBLISHER, 10, NON_PROFIT, 11, EDUCATION';
COMMENT ON COLUMN company_tbl.business_field IS 'benchmark - statistics: 0, NONE, 1, B2B, 2, B2C';
COMMENT ON COLUMN company_tbl.creator_company_id IS 'ID of master - company, needed for administration sub-companies via EMM';
COMMENT ON COLUMN company_tbl.rdir_domain IS 'default rdir-domain';
COMMENT ON COLUMN company_tbl.shortname IS '[private_data] tenant name';
COMMENT ON COLUMN company_tbl.description IS '[private_data] comment on entry';
COMMENT ON COLUMN company_tbl.mailerset IS 'default mailerset - id, references serverset_tbl, 0 = not specified';
COMMENT ON COLUMN company_tbl.customer_type IS 'legacy, PROJ-711';
COMMENT ON COLUMN company_tbl.pricing_id IS 'legacy, PROJ-711';
COMMENT ON COLUMN company_tbl.send_immediately IS 'if set to a value != 0 then sending out of mailings starts immediately during generating and not when the mail generation is finished';
COMMENT ON COLUMN company_tbl.offpeak IS 'legacy, PROJ-711';
COMMENT ON COLUMN company_tbl.notification_email IS '[private_data] mailadress set to get system-notifications from EMM';
COMMENT ON COLUMN company_tbl.mailloop_domain IS 'This is the default domain used to build dynamic filter (mailloop) addresses; it is also used for display these addresses in the GUI';
COMMENT ON COLUMN company_tbl.stat_admin IS 'ID of executive admin for company';
COMMENT ON COLUMN company_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN company_tbl.timestamp IS 'entry last change';
COMMENT ON COLUMN company_tbl.secret_key IS 'the secret key for generating agnUIDs';
COMMENT ON COLUMN company_tbl.uid_version IS 'defines what UID - version at least has to be used: 0=old UID, 1=XUID (actual one)';
COMMENT ON COLUMN company_tbl.mails_per_day IS 'If not NULL, this is an expression to describe the limit of how many mails one recipient of this company may receive per day';
COMMENT ON COLUMN company_tbl.max_recipients IS '?, DEFAULT 0';
COMMENT ON COLUMN company_tbl.salutation_extended IS 'enables gender 3-6 in title_gender_tbl, DEFAULT 0';
COMMENT ON COLUMN company_tbl.enabled_uid_version IS 'currently active agnUID version for this company';
COMMENT ON COLUMN company_tbl.auto_mailing_report_active IS 'Automatically send a report-mail for sent mailings after 24h, 48h and 1 week, DEFAULT 0';
COMMENT ON COLUMN company_tbl.auto_deeptracking IS 'defines if all links should be extended for deeptracking automatically, 0 = false, 1 = true, DEFAULT 0';
COMMENT ON COLUMN company_tbl.default_datasource_id IS 'default datasource, would be set, if no explizit datasource_ID is given. references datasource_description_tbl';
COMMENT ON COLUMN company_tbl.priority_count IS 'max number of mails a recipient may receive per day for mailings with priority';
COMMENT ON COLUMN company_tbl.contact_tech IS 'Emails list separated by [,; ] for technical contact';
COMMENT ON COLUMN company_tbl.parent_company_id IS 'Parent companyid for master/sub client constructs';
COMMENT ON COLUMN company_tbl.company_token IS 'Company token';

CREATE TABLE company_info_tbl
(
    company_id    INTEGER     NOT NULL,
    cname         VARCHAR(64) NOT NULL,
    cvalue        VARCHAR(4000),
    description   VARCHAR(250),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    timestamp     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    hostname      VARCHAR(100)
) TABLESPACE data_cust_table;
CREATE UNIQUE INDEX compinfotbl$clsnamehost$uq ON company_info_tbl (company_id, cname, hostname);
COMMENT ON TABLE company_info_tbl IS 'tenant settings / global settings, e.g. MIA or success-log';
COMMENT ON COLUMN company_info_tbl.company_id IS 'tenant - ID, use 0 for global settings';
COMMENT ON COLUMN company_info_tbl.cname IS 'specify parameter to set e.g. ahv:is-enabled';
COMMENT ON COLUMN company_info_tbl.cvalue IS 'Configuration value';
COMMENT ON COLUMN company_info_tbl.description IS 'Description for a configuration entry';
COMMENT ON COLUMN company_info_tbl.creation_date IS 'Creation date of this configuration entry';
COMMENT ON COLUMN company_info_tbl.timestamp IS 'Change date of this configuration entry';

CREATE TABLE config_tbl
(
    class         VARCHAR(32),
    name          VARCHAR(64),
    value         VARCHAR(4000),
    hostname      VARCHAR(100),
    description   VARCHAR(500),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_accounting;
CREATE UNIQUE INDEX config_tbl$clsnamehost$uq ON config_tbl (class, name, hostname);
COMMENT ON TABLE config_tbl IS 'several global settings';
COMMENT ON COLUMN config_tbl.class IS 'First part of the configuration entries name';
COMMENT ON COLUMN config_tbl.name IS 'Second part of the configuration entries name';
COMMENT ON COLUMN config_tbl.value IS 'Configuration value';
COMMENT ON COLUMN config_tbl.description IS 'detailed description including issue number (if available)';
COMMENT ON COLUMN config_tbl.creation_date IS 'time of creation';
COMMENT ON COLUMN config_tbl.change_date IS 'time of last change';

CREATE TABLE component_tbl
(
    company_id          INTEGER,
    description         VARCHAR(200),
    mailtemplate_id     INTEGER,
    mailing_id          INTEGER,
    component_id        SERIAL PRIMARY KEY,
    mtype               VARCHAR(100),
    required            INTEGER,
    comptype            INTEGER,
    comppresent         INTEGER,
    compname            VARCHAR(500),
    emmblock            TEXT,
    binblock            BYTEA,
    target_id           INTEGER   DEFAULT 0,
    url_id              INTEGER   DEFAULT 0 NOT NULL,
    timestamp           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    validity_start_date TIMESTAMP,
    validity_end_date   TIMESTAMP,
    cdn_id              VARCHAR(32)
) TABLESPACE data_cust_table;
ALTER TABLE component_tbl ADD CONSTRAINT component$coid$nn CHECK (company_id IS NOT NULL);
ALTER TABLE component_tbl ADD CONSTRAINT component$compid$nn CHECK (component_id IS NOT NULL);
ALTER TABLE component_tbl ADD CONSTRAINT component$compname$nn CHECK (compname IS NOT NULL);
CREATE INDEX component$coid_mid$idx ON component_tbl (company_id, mailing_id) TABLESPACE data_cust_index;
CREATE INDEX component$compname$idx ON component_tbl (compname) TABLESPACE data_cust_index;
CREATE INDEX component$mtype$idx ON component_tbl (mtype) TABLESPACE data_cust_index;
CREATE INDEX component$coid$idx ON component_tbl (company_id) TABLESPACE data_cust_index;
CREATE INDEX component$mid$coid$idx ON component_tbl (mailing_id, company_id) TABLESPACE data_cust_index;
CREATE INDEX component$mid$idx ON component_tbl (mailing_id) TABLESPACE data_cust_index;
CREATE UNIQUE INDEX component$cdn_id$uq ON component_tbl (cdn_id) TABLESPACE data_cust_index;
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$component_tbl FOREIGN KEY (id) REFERENCES component_tbl (component_id);
COMMENT ON TABLE component_tbl IS 'stores mailing-components';
COMMENT ON COLUMN component_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN component_tbl.description IS 'component description';
COMMENT ON COLUMN component_tbl.mailtemplate_id IS 'references related template';
COMMENT ON COLUMN component_tbl.mailing_id IS 'references related mailing';
COMMENT ON COLUMN component_tbl.component_id IS 'unique ID (PK), use COMPONENT_TBL_SEQ';
COMMENT ON COLUMN component_tbl.mtype IS 'Mimetype of the component, 0 = Mobile HTML (Deprecated)';
COMMENT ON COLUMN component_tbl.required IS '0 = no, 1 = yes';
COMMENT ON COLUMN component_tbl.comptype IS 'Type of the component: 0: TEMPLATE 1: External image (referenced via URL) 2: Deprecated 3: Attachment 4: Personalized attachment 5: Embedded image 6: Font (company specific font for personalized pdf attachment) 7: Parsable attachment 8: THUMBNAIL_IMAGE';
COMMENT ON COLUMN component_tbl.comppresent IS 'If this is 0, this component is no more in use';
COMMENT ON COLUMN component_tbl.compname IS 'component name';
COMMENT ON COLUMN component_tbl.emmblock IS 'The content of the component';
COMMENT ON COLUMN component_tbl.binblock IS 'Additional binary information used e.g. for crossmedia';
COMMENT ON COLUMN component_tbl.target_id IS 'references component - specific targetGroup';
COMMENT ON COLUMN component_tbl.url_id IS 'auto-generated link for any pictures / graphics';
COMMENT ON COLUMN component_tbl.timestamp IS 'last change on component';
COMMENT ON COLUMN component_tbl.validity_start_date IS 'for compoments, that should be used for a terminated period only';
COMMENT ON COLUMN component_tbl.validity_end_date IS 'for compoments, that should be used for a terminated period only';
COMMENT ON COLUMN component_tbl.cdn_id IS 'Externally used cdn id for cdn networks';

CREATE TABLE customer_1_tbl
(
    customer_id           SERIAL,
    email                 VARCHAR(100),
    firstname             VARCHAR(100),
    lastname              VARCHAR(100),
    gender                SMALLINT,
    mailtype              SMALLINT,
    title                 VARCHAR(100),
    bounceload            INTEGER   DEFAULT 0 NOT NULL,
    datasource_id         INTEGER,
    timestamp             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creation_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lastopen_date         TIMESTAMP,
    lastclick_date        TIMESTAMP,
    latest_datasource_id  INTEGER,
    lastsend_date         TIMESTAMP,
    sys_tracking_veto     SMALLINT,
    cleaned_date          TIMESTAMP,
    sys_encrypted_sending INTEGER   DEFAULT 1
) TABLESPACE data_cust_table;
ALTER TABLE customer_1_tbl ADD CONSTRAINT cust1$cuid$pk PRIMARY KEY (customer_id) USING INDEX TABLESPACE data_cust_index;
ALTER TABLE customer_1_tbl ADD CONSTRAINT cust1$email$nn CHECK (email IS NOT NULL);
ALTER TABLE customer_1_tbl ADD CONSTRAINT cust1$gender$nn CHECK (gender IS NOT NULL);
ALTER TABLE customer_1_tbl ADD CONSTRAINT cust1$mailtype$nn CHECK (mailtype IS NOT NULL);
ALTER TABLE customer_1_tbl ADD CONSTRAINT cust1$mailtype$ck CHECK (mailtype IN (0, 1, 2, 4));
CREATE INDEX cust1$email$idx ON customer_1_tbl (email) TABLESPACE data_cust_index;
CREATE INDEX cust1$lowemail$idx ON customer_1_tbl (LOWER(email)) TABLESPACE data_cust_index;
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$customer_1_tbl FOREIGN KEY (id) REFERENCES customer_1_tbl (customer_id);
COMMENT ON TABLE customer_1_tbl IS '[private_data] stores recipient data, only default columns documented more might be created by EMM-users';
COMMENT ON COLUMN customer_1_tbl.customer_id IS 'unique ID (PK), use customer_1_tbl_seq';
COMMENT ON COLUMN customer_1_tbl.mailtype IS '0-text, 1-html, 2-offline-html';
COMMENT ON COLUMN customer_1_tbl.gender IS '0-male, 1-female, 2-unknown, 5-company';
COMMENT ON COLUMN customer_1_tbl.email IS '[private_data] ';
COMMENT ON COLUMN customer_1_tbl.lastname IS '[private_data] ';
COMMENT ON COLUMN customer_1_tbl.timestamp IS 'entry last change, default: CURRENT_TIMESTAMP';
COMMENT ON COLUMN customer_1_tbl.creation_date IS 'entry creation date, default: CURRENT_TIMESTAMP';
COMMENT ON COLUMN customer_1_tbl.datasource_id IS 'references datasource_description_tbl.datasource_id - marks the origin of any reciepient, should be filled always!';
COMMENT ON COLUMN customer_1_tbl.lastopen_date IS 'last registered opening of this recipient, daily filled / updated by job-queue-based job';
COMMENT ON COLUMN customer_1_tbl.lastclick_date IS 'last registered click of this recipient, daily filled / updated by job-queue-based job';
COMMENT ON COLUMN customer_1_tbl.lastsend_date IS 'latest mailing sent to this recipient, filled / updated during mail-sending';
COMMENT ON COLUMN customer_1_tbl.latest_datasource_id IS 'latest souce of changes on recipient data, should be filled always!';
COMMENT ON COLUMN customer_1_tbl.firstname IS 'recipients first name';
COMMENT ON COLUMN customer_1_tbl.title IS 'title of the receiver';
COMMENT ON COLUMN customer_1_tbl.bounceload IS 'Special value. See AGNEMM-1817, AGNEMM-1924 and AGNEMM-1925';
COMMENT ON COLUMN customer_1_tbl.sys_tracking_veto IS 'DSGVO tracking veto';
COMMENT ON COLUMN customer_1_tbl.cleaned_date IS 'date of anonymization of customer';

CREATE TABLE success_1_tbl
(
    customer_id INTEGER,
    mailing_id  INTEGER,
    timestamp   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_success;
CREATE INDEX suc1$mid$idx ON success_1_tbl (mailing_id) TABLESPACE index_success;
CREATE INDEX suc1$cid$idx ON success_1_tbl (customer_id) TABLESPACE index_success;
CREATE INDEX suc1$tmst$idx ON success_1_tbl (timestamp) TABLESPACE index_success;
COMMENT ON TABLE success_1_tbl IS 'Stores successfully delivered mailings to any specified recipient, filled only if log-success param is set in company_info_tbl (per tenant or instance), storing period';
COMMENT ON COLUMN success_1_tbl.customer_id IS 'Customer reference';
COMMENT ON COLUMN success_1_tbl.mailing_id IS 'Mailing reference';
COMMENT ON COLUMN success_1_tbl.timestamp IS 'Change date of this entry';

CREATE TABLE customer_1_binding_tbl
(
    customer_id      INTEGER,
    mailinglist_id   INTEGER,
    user_type        CHAR(1),
    user_status      INTEGER,
    user_remark      VARCHAR(150),
    timestamp        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    exit_mailing_id  INTEGER,
    creation_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    mediatype        INTEGER   DEFAULT 0,
    referrer         VARCHAR(4000),
    entry_mailing_id INTEGER
) TABLESPACE data_cust_table;
ALTER TABLE customer_1_binding_tbl ADD CONSTRAINT cust1b$cuid_mlid_mt$pk PRIMARY KEY (customer_id, mailinglist_id, mediatype) USING INDEX TABLESPACE data_cust_index;
ALTER TABLE customer_1_binding_tbl ADD CONSTRAINT cust1b$cuid$fk FOREIGN KEY (customer_id) REFERENCES customer_1_tbl (customer_id);
ALTER TABLE customer_1_binding_tbl ADD CONSTRAINT cust1b$cid$nn check (customer_id IS NOT NULL);
ALTER TABLE customer_1_binding_tbl ADD CONSTRAINT cust1b$mid$nn check (mailinglist_id IS NOT NULL);
ALTER TABLE customer_1_binding_tbl ADD CONSTRAINT cust1b$ustat$nn CHECK (user_status IS NOT NULL);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$customer_1_binding_tbl FOREIGN KEY (mediatype, mailinglist_id, customer_id) REFERENCES customer_1_binding_tbl (customer_id, mailinglist_id, mediatype);
CREATE INDEX cust1b$cuid_ustat_mlid$idx ON customer_1_binding_tbl (user_status, mailinglist_id, customer_id) TABLESPACE data_cust_index;
CREATE INDEX cust1b$cuid_uty_mlid$idx ON customer_1_binding_tbl (customer_id, user_type, mailinglist_id);
COMMENT ON TABLE customer_1_binding_tbl IS 'n:m releation between reciepients and mailinglists';
COMMENT ON COLUMN customer_1_binding_tbl.customer_id IS 'reciepientID - references customer_1_tbl';
COMMENT ON COLUMN customer_1_binding_tbl.mailinglist_id IS 'mailinglistID - references mailinglist_tbl';
COMMENT ON COLUMN customer_1_binding_tbl.user_type IS 'reciepient type: W = world-rec. A = Admin-rec, T = Test-rec., w = VIP(!) - world-rec, t = VIP(!) tester-rec, E = deprecated/do not use';
COMMENT ON COLUMN customer_1_binding_tbl.user_status IS 'current state: 1 = active, 2 = bounced, 3 = opt out by admin, 4 = opt out by user, 5 = DOI waiting for confirm, 6 = blacklisted, 7 / 0 = Suspend - customer specific  cases only';
COMMENT ON COLUMN customer_1_binding_tbl.user_remark IS 'comment on last state - change';
COMMENT ON COLUMN customer_1_binding_tbl.timestamp IS 'timestamp of last state - change';
COMMENT ON COLUMN customer_1_binding_tbl.exit_mailing_id IS 'mailingID causing state-change to bounce or opt-out';
COMMENT ON COLUMN customer_1_binding_tbl.creation_date IS 'binding creation date';
COMMENT ON COLUMN customer_1_binding_tbl.mediatype IS 'mediatype: 0 = Email, 1 = Fax, 2 = Print, 4 = SMS';
COMMENT ON COLUMN customer_1_binding_tbl.referrer IS 'Http referrer header value if set in subscription request';
COMMENT ON COLUMN customer_1_binding_tbl.entry_mailing_id IS 'MailingID of subscription';

CREATE TABLE hst_customer_1_binding_tbl
(
    customer_id      INTEGER,
    mailinglist_id   INTEGER,
    user_type        CHAR(1),
    user_status      INTEGER,
    user_remark      VARCHAR(150),
    timestamp        TIMESTAMP,
    creation_date    TIMESTAMP,
    exit_mailing_id  INTEGER,
    mediatype        INTEGER,
    change_type      INTEGER,
    timestamp_change TIMESTAMP,
    client_info      VARCHAR(150),
    email            VARCHAR(100),
    referrer         VARCHAR(4000),
    entry_mailing_id INTEGER
) TABLESPACE customer_history;
ALTER TABLE hst_customer_1_binding_tbl ADD CONSTRAINT hcust1b$pk PRIMARY KEY (customer_id, mailinglist_id, mediatype, timestamp_change);
CREATE INDEX hstcb1$email$idx ON hst_customer_1_binding_tbl (email) TABLESPACE index_customer_history;
CREATE INDEX hstcb1$mlidcidl$idx ON hst_customer_1_binding_tbl (mailinglist_id, customer_id) TABLESPACE index_customer_history;
CREATE INDEX hstcb1$tsch$idx ON hst_customer_1_binding_tbl (timestamp_change) TABLESPACE index_customer_history;
COMMENT ON TABLE hst_customer_1_binding_tbl IS '[private_data] stores history for recipient - mailinglist - relation, any values are stored as a copy of values BEFORE latest change';
COMMENT ON COLUMN hst_customer_1_binding_tbl.customer_id IS 'reciepientID - references customer_1_tbl';
COMMENT ON COLUMN hst_customer_1_binding_tbl.mailinglist_id IS 'mailinglistID - references mailinglist_tbl';
COMMENT ON COLUMN hst_customer_1_binding_tbl.user_type IS 'reciepient type: W = world-rec. A = Admin-rec, T = Test-rec., w = VIP(!) - world-rec, a = VIP (!) admin-rec';
COMMENT ON COLUMN hst_customer_1_binding_tbl.user_status IS 'current state: 1 = active, 2 = bounced, 3 = opt out by admin, 4 = opt out by user, 5 = DOI waiting for confirm, 6 = blacklisted, 7 / 0 = Suspend - customer specific  cases only';
COMMENT ON COLUMN hst_customer_1_binding_tbl.user_remark IS 'comment on last state - change';
COMMENT ON COLUMN hst_customer_1_binding_tbl.timestamp IS 'timestamp of last state - change';
COMMENT ON COLUMN hst_customer_1_binding_tbl.exit_mailing_id IS 'mailingID causing state-change to bounce or opt-out';
COMMENT ON COLUMN hst_customer_1_binding_tbl.creation_date IS 'binding creation date';
COMMENT ON COLUMN hst_customer_1_binding_tbl.mediatype IS 'mediatype: 0 = Email, 1 = Fax, 2 = Print, 3 = MMS, 4 = SMS';
COMMENT ON COLUMN hst_customer_1_binding_tbl.change_type IS '0 = deleted, 1 = updated';
COMMENT ON COLUMN hst_customer_1_binding_tbl.timestamp_change IS 'timestamp of update / delete that is overwriting deleting this dataset';
COMMENT ON COLUMN hst_customer_1_binding_tbl.client_info IS 'client who made this changes, e.g. console (frontend)';
COMMENT ON COLUMN hst_customer_1_binding_tbl.email IS '[private_data] recipients email in order to trace deleted recipients';
COMMENT ON COLUMN hst_customer_1_binding_tbl.entry_mailing_id IS 'MailingID of subscription';

CREATE TABLE interval_track_1_tbl
(
    customer_id INTEGER   NOT NULL,
    mailing_id  INTEGER   NOT NULL,
    send_date   TIMESTAMP NOT NULL
) TABLESPACE data_success;
CREATE INDEX intervtrack$1mid$idx ON interval_track_1_tbl (mailing_id) TABLESPACE data_cust_index;
CREATE INDEX intervtrack$1$cid$idx ON interval_track_1_tbl (customer_id) TABLESPACE data_cust_index;
CREATE INDEX intervtrack$1$sendd$idx ON interval_track_1_tbl (send_date) TABLESPACE data_cust_index;
COMMENT ON TABLE interval_track_1_tbl IS 'stores mailtracking data for interval-mailings';
COMMENT ON COLUMN interval_track_1_tbl.customer_id IS 'Customer reference';
COMMENT ON COLUMN interval_track_1_tbl.mailing_id IS 'Mailing reference';
COMMENT ON COLUMN interval_track_1_tbl.send_date IS 'Send date of this interval mailing to a specific customer';

CREATE TABLE rdir_traffic_amount_1_tbl
(
    mailing_id   INTEGER,
    content_name VARCHAR(3000),
    content_size INTEGER,
    demand_date  TIMESTAMP
) TABLESPACE data_cust_table;
COMMENT ON TABLE rdir_traffic_amount_1_tbl IS 'stores information on rdir traffic data amount';
COMMENT ON COLUMN rdir_traffic_amount_1_tbl.mailing_id IS 'Referenced mailing id';
COMMENT ON COLUMN rdir_traffic_amount_1_tbl.content_name IS 'Name of demanded content';
COMMENT ON COLUMN rdir_traffic_amount_1_tbl.content_size IS 'Size of demanded content';
COMMENT ON COLUMN rdir_traffic_amount_1_tbl.demand_date IS 'Date of demand request';

CREATE TABLE rdir_traffic_agr_1_tbl
(
    mailing_id   INTEGER,
    content_name VARCHAR(3000),
    content_size INTEGER,
    demand_date  TIMESTAMP,
    amount       INTEGER
) TABLESPACE data_cust_table;
COMMENT ON TABLE rdir_traffic_agr_1_tbl IS 'stores information on rdir traffic data amount with a daily aggregation of rdir_traffic_amount_1_tbl';
COMMENT ON COLUMN rdir_traffic_agr_1_tbl.mailing_id IS 'Referenced mailing id';
COMMENT ON COLUMN rdir_traffic_agr_1_tbl.content_name IS 'Name of demanded content';
COMMENT ON COLUMN rdir_traffic_agr_1_tbl.content_size IS 'Size of demanded content';
COMMENT ON COLUMN rdir_traffic_agr_1_tbl.demand_date IS 'Day date of this summed up entry';
COMMENT ON COLUMN rdir_traffic_agr_1_tbl.amount IS 'Amount of times this content was requested';

CREATE TABLE customer_field_tbl
(
    admin_id       INTEGER,
    col_name       VARCHAR(50),
    shortname      VARCHAR(200),
    description    VARCHAR(500),
    mode_edit      INTEGER,
    company_id     INTEGER,
    field_sort     INTEGER   DEFAULT 0 NOT NULL,
    line           INTEGER,
    isinterest     INTEGER   DEFAULT 0,
    creation_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    historize      SMALLINT,
    allowed_values TEXT
) TABLESPACE data_cust_table;
ALTER TABLE customer_field_tbl ADD CONSTRAINT PK_CUSTFIELD PRIMARY KEY (company_id, col_name);
CREATE UNIQUE INDEX custfield$coid$colname ON customer_field_tbl (company_id, UPPER(col_name));
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$customer_field_tbl FOREIGN KEY (id, text) REFERENCES customer_field_tbl (company_id, col_name);
COMMENT ON TABLE customer_field_tbl IS 'configure individual layout for recipient profile fields per EMM - user';
COMMENT ON COLUMN customer_field_tbl.admin_id IS 'referes to EMM-User (admin_tbl)';
COMMENT ON COLUMN customer_field_tbl.col_name IS 'Name of related DataBaseColumn, unique per tenant';
COMMENT ON COLUMN customer_field_tbl.shortname IS 'field name shown in EMM';
COMMENT ON COLUMN customer_field_tbl.description IS 'description for this field';
COMMENT ON COLUMN customer_field_tbl.mode_edit IS '1 = readonly, 2 = hidden, 0 = no restriction';
COMMENT ON COLUMN customer_field_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN customer_field_tbl.field_sort IS 'sorting order in EMM (if applicable: in group)';
COMMENT ON COLUMN customer_field_tbl.line IS '1=horizontal ruler after this field in EMM - Layout';
COMMENT ON COLUMN customer_field_tbl.isinterest IS 'defines interest to order blocks in mailing - dyn_name_tbl.INTEREST_GROUP';
COMMENT ON COLUMN customer_field_tbl.creation_date IS 'setting creation';
COMMENT ON COLUMN customer_field_tbl.change_date IS 'setting last change';
COMMENT ON COLUMN customer_field_tbl.historize IS '1 = history of any changes in this field would be logged (in hst_customer_xxx_tbl) if profile-field-history is active)';
COMMENT ON COLUMN customer_field_tbl.allowed_values IS 'Optional restricting set of values allowed for this field';


CREATE TABLE datasource_description_tbl
(
    datasource_id  SERIAL PRIMARY KEY,
    description    VARCHAR(500),
    company_id     INTEGER,
    sourcegroup_id INTEGER,
    timestamp      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    url            VARCHAR(500),
    desc2          VARCHAR(500)
) TABLESPACE data_accounting;
ALTER TABLE datasource_description_tbl ADD CONSTRAINT datasource_des$desc$nn CHECK (description IS NOT NULL);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$datasource_descr_tbl FOREIGN KEY (id) REFERENCES datasource_description_tbl (datasource_id);
CREATE INDEX datasource_desc$cidsrid$idx ON datasource_description_tbl (company_id, sourcegroup_id) TABLESPACE data_emmaux;
CREATE INDEX datasource_desc$desc$idx ON datasource_description_tbl (description) TABLESPACE data_emmaux;
COMMENT ON TABLE datasource_description_tbl IS 'defines a source for new recipients / recipient updates, FK to company_tbl dropped in order to allow global settings';
COMMENT ON COLUMN datasource_description_tbl.datasource_id IS 'unique ID, use datasource_description_tbl_seq';
COMMENT ON COLUMN datasource_description_tbl.description IS 'data source description';
COMMENT ON COLUMN datasource_description_tbl.company_id IS 'tenant - ID (company_tbl), use 0 for global sources';
COMMENT ON COLUMN datasource_description_tbl.sourcegroup_id IS 'source-group (references source_group_tbl)';
COMMENT ON COLUMN datasource_description_tbl.timestamp IS 'source creation / registration date';
COMMENT ON COLUMN datasource_description_tbl.url IS 'URL e.g. for subscribe forms';
COMMENT ON COLUMN datasource_description_tbl.desc2 IS 'secondary description';

CREATE TABLE dkim_key_tbl
(
    dkim_id              SERIAL PRIMARY KEY,
    creation_date        TIMESTAMP,
    timestamp            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    company_id           INTEGER,
    valid_start          TIMESTAMP,
    valid_end            TIMESTAMP,
    domain               VARCHAR(128),
    selector             VARCHAR(250),
    domain_key           VARCHAR(4000),
    domain_key_encrypted VARCHAR(4000)
) TABLESPACE data_emmaux;
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$dkim_key_tbl FOREIGN KEY (id) REFERENCES dkim_key_tbl (dkim_id);
COMMENT ON TABLE dkim_key_tbl IS '[secret_data] for integration of DKIM - functionality (transact-user and tables) in the EMM';
COMMENT ON COLUMN dkim_key_tbl.dkim_id IS 'unique ID, use dkim_key_tbl_seq';
COMMENT ON COLUMN dkim_key_tbl.creation_date IS 'key entry creation date';
COMMENT ON COLUMN dkim_key_tbl.timestamp IS 'key entry last change';
COMMENT ON COLUMN dkim_key_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN dkim_key_tbl.valid_start IS 'key validation start time, use NULL as: immediatly';
COMMENT ON COLUMN dkim_key_tbl.valid_end IS 'key validation end time, use NULL as: open';
COMMENT ON COLUMN dkim_key_tbl.domain IS '[secret_data] related key domain';
COMMENT ON COLUMN dkim_key_tbl.selector IS 'allows switching keys per domain';
COMMENT ON COLUMN dkim_key_tbl.domain_key IS '[secret_data] the key itself';
COMMENT ON COLUMN dkim_key_tbl.domain_key_encrypted IS '[secret_data] encrypted key version';

CREATE TABLE deliverycheck_tbl
(
    delivery_id   SERIAL PRIMARY KEY,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    company_id    INTEGER,
    sender        VARCHAR(256),
    receiver      VARCHAR(256),
    active        INTEGER,
    startdate     TIMESTAMP,
    enddate       TIMESTAMP,
    remark        VARCHAR(256)
) TABLESPACE data_accounting;
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$deliverycheck_tbl FOREIGN KEY (id) REFERENCES deliverycheck_tbl (delivery_id);
COMMENT ON TABLE deliverycheck_tbl IS '[secret_data] process-tbl for Deliverycheck 2.0';
COMMENT ON COLUMN deliverycheck_tbl.delivery_id IS 'unique ID, use deliverycheck_tbl_seq';
COMMENT ON COLUMN deliverycheck_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN deliverycheck_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN deliverycheck_tbl.sender IS '[secret_data] reg expression, has to match mailing sender, use * as default setting';
COMMENT ON COLUMN deliverycheck_tbl.receiver IS '[secret_data] report - recipient';
COMMENT ON COLUMN deliverycheck_tbl.active IS '0 = entry wont be used';
COMMENT ON COLUMN deliverycheck_tbl.startdate IS 'entry start time, use NULL as: immediatly';
COMMENT ON COLUMN deliverycheck_tbl.enddate IS 'entry end time, use NULL as: open';
COMMENT ON COLUMN deliverycheck_tbl.remark IS 'comment';

CREATE TABLE dyn_content_tbl
(
    dyn_content_id SERIAL,
    dyn_name_id    INTEGER,
    target_id      INTEGER,
    dyn_order      INTEGER,
    dyn_content    TEXT,
    mailing_id     INTEGER,
    company_id     INTEGER,
    creation_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_emmaux;
ALTER TABLE dyn_content_tbl ADD CONSTRAINT dyn_content$dyncontentid$nn CHECK (dyn_content_id IS NOT NULL);
ALTER TABLE dyn_content_tbl ADD CONSTRAINT dyn_content$dyn_name$nn CHECK (dyn_name_id IS NOT NULL);
ALTER TABLE dyn_content_tbl ADD CONSTRAINT dyn_content$coid$nn CHECK (company_id IS NOT NULL);
CREATE INDEX dyn_content$dyn_name_id$idx ON dyn_content_tbl (dyn_name_id) TABLESPACE data_emmaux_idx;
CREATE INDEX dyn_content_tbl$mlidciod$idx ON dyn_content_tbl (mailing_id, company_id) TABLESPACE data_emmaux_idx;
CREATE INDEX dyn_content_tbl$targetid$idx ON dyn_content_tbl (target_id) TABLESPACE data_emmaux_idx;
ALTER TABLE dyn_content_tbl ADD CONSTRAINT dyncon$dynconid$pk PRIMARY KEY (dyn_content_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$dyn_content_tbl FOREIGN KEY (id) REFERENCES dyn_content_tbl (dyn_content_id);
COMMENT ON TABLE dyn_content_tbl IS 'stores different text contents for text - blocks';
COMMENT ON COLUMN dyn_content_tbl.dyn_content_id IS 'unique ID, use dyn_content_tbl_seq';
COMMENT ON COLUMN dyn_content_tbl.dyn_name_id IS 'references dyn_name_tbl';
COMMENT ON COLUMN dyn_content_tbl.target_id IS 'references dyn_target_tbl, allows to generate content for special target groups only';
COMMENT ON COLUMN dyn_content_tbl.dyn_order IS 'orders content within a text-block';
COMMENT ON COLUMN dyn_content_tbl.dyn_content IS 'the content itself';
COMMENT ON COLUMN dyn_content_tbl.mailing_id IS 'related mailing, references mailing_tbl';
COMMENT ON COLUMN dyn_content_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN dyn_content_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN dyn_content_tbl.change_date IS 'entry last change';

CREATE TABLE dyn_name_tbl
(
    dyn_name_id       SERIAL,
    dyn_name          VARCHAR(100),
    mailing_id        INTEGER,
    company_id        INTEGER,
    dyn_group         INTEGER   DEFAULT 0,
    interest_group    VARCHAR(50),
    creation_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted           SMALLINT   DEFAULT 0 NOT NULL,
    deletion_date     TIMESTAMP,
    no_link_extension SMALLINT
) TABLESPACE data_accounting;
ALTER TABLE dyn_name_tbl ADD CONSTRAINT dyn_name$dynnameid$nn CHECK (dyn_name_id IS NOT NULL);
ALTER TABLE dyn_name_tbl ADD CONSTRAINT dyn_name$mailingid$nn CHECK (mailing_id IS NOT NULL);
ALTER TABLE dyn_name_tbl ADD CONSTRAINT dyn_name$compid$nn CHECK (company_id IS NOT NULL);
ALTER TABLE dyn_name_tbl ADD CONSTRAINT dyn_name$dyn_group$nn CHECK (dyn_group IS NOT NULL);
CREATE INDEX dyn_name_tbl$mailingid$idx ON dyn_name_tbl (mailing_id) TABLESPACE data_emmaux_idx;
ALTER TABLE dyn_name_tbl ADD CONSTRAINT dynname$dynnameid$pk PRIMARY KEY (dyn_name_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$dyn_name_tbl FOREIGN KEY (id) REFERENCES dyn_name_tbl (dyn_name_id);
COMMENT ON TABLE dyn_name_tbl IS 'stores names of text - blocks, contents in DYN_CONTENT_TBL';
COMMENT ON COLUMN dyn_name_tbl.dyn_name_id IS 'unique ID, use dyn_name_tbl_seq';
COMMENT ON COLUMN dyn_name_tbl.dyn_name IS 'name like found in agnDYN name=XXX tag';
COMMENT ON COLUMN dyn_name_tbl.mailing_id IS 'related mailing, references mailing_tbl';
COMMENT ON COLUMN dyn_name_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN dyn_name_tbl.dyn_group IS 'group of this tag. Groups are a feature of dynamic content, which allows the contents to be grouped together when displaying them in the content list';
COMMENT ON COLUMN dyn_name_tbl.interest_group IS 'to order blocks by interest, see customer_field_tbl.isinterest';
COMMENT ON COLUMN dyn_name_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN dyn_name_tbl.change_date IS 'entrylast change';
COMMENT ON COLUMN dyn_name_tbl.deleted IS '1=yes';
COMMENT ON COLUMN dyn_name_tbl.deletion_date IS 'deletion date';
COMMENT ON COLUMN dyn_name_tbl.no_link_extension IS 'Flag to disable extending links (SAAS-1250)';

CREATE TABLE dyn_target_tbl
(
    target_id           SERIAL PRIMARY KEY,
    company_id          INTEGER,
    mailinglist_id      INTEGER,
    target_shortname    VARCHAR(100),
    target_sql          VARCHAR(4000),
    target_description  VARCHAR(1000),
    creation_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT  DEFAULT 0,
    admin_test_delivery INTEGER   DEFAULT 0 NOT NULL,
    locked              SMALLINT,
    component_hide      SMALLINT  DEFAULT 0,
    eql                 TEXT,
    invalid             INTEGER,
    hidden              INTEGER,
    complexity          INTEGER   DEFAULT NULL,
    favorite            SMALLINT  DEFAULT 0
) TABLESPACE data_emmaux;
ALTER TABLE dyn_target_tbl ADD CONSTRAINT dyn_target$coid$nn CHECK (company_id IS NOT NULL);
ALTER TABLE dyn_target_tbl ADD CONSTRAINT dyn_target$del$nn CHECK (deleted IS NOT NULL);
CREATE INDEX dyn_target_tbl$coid$idx ON dyn_target_tbl (company_id) TABLESPACE data_emmaux_idx;
ALTER TABLE dyn_target_tbl ADD CONSTRAINT dyntg$shortname$nn CHECK (target_shortname IS NOT NULL);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$dyn_target_tbl FOREIGN KEY (id) REFERENCES dyn_target_tbl (target_id);
ALTER TABLE admin_tbl ADD CONSTRAINT admin$limittarget$fk FOREIGN KEY (limiting_target_id) REFERENCES dyn_target_tbl (target_id);
COMMENT ON TABLE dyn_target_tbl IS 'stores target group information';
COMMENT ON COLUMN dyn_target_tbl.target_id IS 'unique ID, use dyn_target_tbl_seq';
COMMENT ON COLUMN dyn_target_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN dyn_target_tbl.mailinglist_id IS 'related mailinglist, references mailinglist_tbl';
COMMENT ON COLUMN dyn_target_tbl.target_shortname IS 'targetgroup name';
COMMENT ON COLUMN dyn_target_tbl.target_sql IS 'sql - representation of targetgroup definition';
COMMENT ON COLUMN dyn_target_tbl.target_description IS 'comment on targetgroup';
COMMENT ON COLUMN dyn_target_tbl.creation_date IS 'targetgroup creation date';
COMMENT ON COLUMN dyn_target_tbl.change_date IS 'targetgroup last change';
COMMENT ON COLUMN dyn_target_tbl.deleted IS '1 = targetgroup is deleted';
COMMENT ON COLUMN dyn_target_tbl.admin_test_delivery IS '1 = targetgroup is used for test and / or admin delivery';
COMMENT ON COLUMN dyn_target_tbl.locked IS '1 = any changes only via DB possible';
COMMENT ON COLUMN dyn_target_tbl.component_hide IS '1 = target group cant be used for components';
COMMENT ON COLUMN dyn_target_tbl.eql IS 'eql - representation of targetgroup definition';
COMMENT ON COLUMN dyn_target_tbl.invalid IS '1=invalid target_group (might e.g. happen while switching between definition modes in EMM in older versions)';
COMMENT ON COLUMN dyn_target_tbl.hidden IS '1 for hidden target groups like listsplit-definitions';
COMMENT ON COLUMN dyn_target_tbl.complexity IS 'Complexity of target group';
COMMENT ON COLUMN dyn_target_tbl.favorite IS 'whether or not is target group in favourites, 1 = yes, 0 = no';

CREATE TABLE import_profile_tbl
(
    id                        SERIAL PRIMARY KEY,
    company_id                INTEGER      NOT NULL,
    admin_id                  INTEGER      NOT NULL,
    shortname                 VARCHAR(255) NOT NULL,
    column_separator          SMALLINT     NOT NULL,
    text_delimiter            SMALLINT     NOT NULL,
    file_charset              SMALLINT     NOT NULL,
    date_format               SMALLINT     NOT NULL,
    import_mode               SMALLINT     NOT NULL,
    null_values_action        SMALLINT     NOT NULL,
    key_column                VARCHAR(255) NOT NULL,
    report_email              VARCHAR(255),
    check_for_duplicates      SMALLINT     NOT NULL,
    mail_type                 SMALLINT     NOT NULL,
    creation_date             TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    change_date               TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    update_all_duplicates     SMALLINT    DEFAULT 0,
    deleted                   SMALLINT    DEFAULT 0,
    pre_import_action         INTEGER,
    decimal_separator         VARCHAR(1)  DEFAULT '.',
    action_for_new_recipients INTEGER,
    noheaders                 SMALLINT    DEFAULT 0,
    zip_password_encr         VARCHAR(100),
    error_email               VARCHAR(400),
    automapping               INTEGER,
    datatype                  VARCHAR(32) DEFAULT 'CSV',
    mailinglists_all          SMALLINT    DEFAULT 0,
    report_locale_lang        VARCHAR(10) DEFAULT NULL,
    report_locale_country     VARCHAR(10) DEFAULT NULL,
    report_timezone           VARCHAR(50) DEFAULT NULL
) TABLESPACE data_emmaux;
ALTER TABLE import_profile_tbl ADD CONSTRAINT imp_prof$deleted$nn CHECK (deleted IS NOT NULL);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$import_profile_tbl FOREIGN KEY (id) REFERENCES import_profile_tbl (id);
COMMENT ON TABLE import_profile_tbl IS 'profile-import parameters';
COMMENT ON COLUMN import_profile_tbl.id IS 'unique ID, use import_profile_tbl_seq';
COMMENT ON COLUMN import_profile_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN import_profile_tbl.admin_id IS 'references EMM-User (admin_tbl)';
COMMENT ON COLUMN import_profile_tbl.shortname IS 'profile name';
COMMENT ON COLUMN import_profile_tbl.column_separator IS 'seperator used in import-file';
COMMENT ON COLUMN import_profile_tbl.text_delimiter IS 'delimiter used in import-file';
COMMENT ON COLUMN import_profile_tbl.file_charset IS 'charset used in import-file';
COMMENT ON COLUMN import_profile_tbl.date_format IS 'date format used in import-file';
COMMENT ON COLUMN import_profile_tbl.import_mode IS '0=add, 1=add+update, 2=update only, 3=unsubscribe(profile only), 4=bounce(profile only), 5=blacklist(profile only), 6=bouncereactivate(profile only), 7=MARK_SUSPENDED, 8=ADD_AND_UPDATE_FORCED, 9=ADD_AND_UPDATE_EXCLUSIVE, 10=REACTIVATE_SUSPENDED, 11=SPECIAL_4ER_BLOCK, 12=BLACKLIST_EXCLUSIVE';
COMMENT ON COLUMN import_profile_tbl.null_values_action IS '0 = dont_ignore_null_values, 1 = ignore_null_values';
COMMENT ON COLUMN import_profile_tbl.key_column IS 'import keycolumn';
COMMENT ON COLUMN import_profile_tbl.report_email IS '[private_data] report recipient adress(es)';
COMMENT ON COLUMN import_profile_tbl.check_for_duplicates IS '0 = csv only, 1 = full, 2 = none';
COMMENT ON COLUMN import_profile_tbl.mail_type IS 'updates customer_xxx_tbl.mailtype: 0-text, 1-html, 2-offline-html';
COMMENT ON COLUMN import_profile_tbl.creation_date IS 'import-profile creation date';
COMMENT ON COLUMN import_profile_tbl.change_date IS 'import-profile last change';
COMMENT ON COLUMN import_profile_tbl.update_all_duplicates IS '1=all matches in customer_xxx_tbl would be updated';
COMMENT ON COLUMN import_profile_tbl.deleted IS '1 = yes';
COMMENT ON COLUMN import_profile_tbl.pre_import_action IS 'Reference to import_action_tbl for sql scripts to execute before import of data';
COMMENT ON COLUMN import_profile_tbl.decimal_separator IS 'Separator "." or "," for decimals';
COMMENT ON COLUMN import_profile_tbl.action_for_new_recipients IS 'DOI action for change of new customers after import';
COMMENT ON COLUMN import_profile_tbl.noheaders IS 'No Header option for csv files';
COMMENT ON COLUMN import_profile_tbl.zip_password_encr IS 'Optional password for csv import zip files';
COMMENT ON COLUMN import_profile_tbl.error_email IS 'List of email addresses to inform on erroneous imports';
COMMENT ON COLUMN import_profile_tbl.automapping IS 'Use all csv columns as db column, exactly name by name';
COMMENT ON COLUMN import_profile_tbl.datatype IS 'Datatype to import (CSV, JSON)';
COMMENT ON COLUMN import_profile_tbl.mailinglists_all IS 'Import works an all existing mailinglists';
COMMENT ON COLUMN import_profile_tbl.report_locale_lang IS 'Language part of locale used for reports';
COMMENT ON COLUMN import_profile_tbl.report_locale_country IS 'Country part of locale used for reports';
COMMENT ON COLUMN import_profile_tbl.report_timezone IS 'Timezone used for reports';

CREATE TABLE import_column_mapping_tbl
(
    id            SERIAL PRIMARY KEY,
    profile_id    INTEGER      NOT NULL,
    file_column   VARCHAR(255) NULL,
    db_column     VARCHAR(255) NOT NULL,
    mandatory     SMALLINT     NOT NULL,
    default_value VARCHAR(255),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted       SMALLINT  DEFAULT 0,
    encrypted     INTEGER   DEFAULT 0
) TABLESPACE data_emmaux;
ALTER TABLE import_column_mapping_tbl ADD CONSTRAINT imp_col_map$deleted$nn CHECK (deleted IS NOT NULL);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$import_col_map_tbl FOREIGN KEY (id) REFERENCES import_column_mapping_tbl (id);
COMMENT ON TABLE import_column_mapping_tbl IS 'settings saving a mapping on file columns to database-columns in an import-profile';
COMMENT ON COLUMN import_column_mapping_tbl.id IS 'unique ID, use import_column_mapping_tbl_seq';
COMMENT ON COLUMN import_column_mapping_tbl.profile_id IS 'references import_profile_tbl';
COMMENT ON COLUMN import_column_mapping_tbl.file_column IS 'matching column in file';
COMMENT ON COLUMN import_column_mapping_tbl.db_column IS 'matching column in database, use "do-not-import-column" to ignore a column in file for this import';
COMMENT ON COLUMN import_column_mapping_tbl.mandatory IS '1 = NULL is not allowed for this column and this import';
COMMENT ON COLUMN import_column_mapping_tbl.default_value IS 'default (for this column and this import';
COMMENT ON COLUMN import_column_mapping_tbl.creation_date IS 'mapping creation date';
COMMENT ON COLUMN import_column_mapping_tbl.change_date IS 'mapping last change';
COMMENT ON COLUMN import_column_mapping_tbl.deleted IS '1 = yes';
COMMENT ON COLUMN import_column_mapping_tbl.encrypted IS '1 = yes';

CREATE TABLE import_gender_mapping_tbl
(
    id            SERIAL PRIMARY KEY,
    profile_id    INTEGER      NOT NULL,
    int_gender    SMALLINT     NOT NULL,
    string_gender VARCHAR(100) NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted       SMALLINT  DEFAULT 0
) TABLESPACE data_emmaux;
ALTER TABLE import_gender_mapping_tbl ADD CONSTRAINT imp_gender_map$deleted$nn CHECK (deleted IS NOT NULL);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$import_gen_map_tbl FOREIGN KEY (id) REFERENCES import_gender_mapping_tbl (id);
COMMENT ON TABLE import_gender_mapping_tbl IS 'settings saving a mapping on gender-values (like m/Mr. ...) to agn-code (0, 1, 2 ...) in an import-profile';
COMMENT ON COLUMN import_gender_mapping_tbl.id IS 'mapping last change';
COMMENT ON COLUMN import_gender_mapping_tbl.profile_id IS 'references import_profile_tbl';
COMMENT ON COLUMN import_gender_mapping_tbl.int_gender IS 'gender like used in import file';
COMMENT ON COLUMN import_gender_mapping_tbl.string_gender IS '0-male, 1-female, 2-unknown, 5-company';
COMMENT ON COLUMN import_gender_mapping_tbl.creation_date IS 'mapping creation date';
COMMENT ON COLUMN import_gender_mapping_tbl.change_date IS 'mapping last change';
COMMENT ON COLUMN import_gender_mapping_tbl.deleted IS '1 = yes';

CREATE TABLE maildrop_status_tbl
(
    status_id                SERIAL PRIMARY KEY,
    gendate                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    genstatus                SMALLINT  DEFAULT 3,
    genchange                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    blocksize                INTEGER,
    company_id               INTEGER,
    status_field             VARCHAR(10),
    mailing_id               INTEGER,
    senddate                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    step                     INTEGER,
    max_recipients           INTEGER,
    admin_test_target_id     INTEGER,
    optimize_mail_generation VARCHAR(32),
    selected_test_recipients SMALLINT,
    origin                   VARCHAR(64),
    processed_by             VARCHAR(64),
    overwrite_test_recipient INTEGER
) TABLESPACE data_accounting;
ALTER TABLE maildrop_status_tbl ADD CONSTRAINT mdropstat$coid$nn CHECK (company_id IS NOT NULL);
ALTER TABLE maildrop_status_tbl ADD CONSTRAINT mdropstat$statusfield$nn CHECK (status_field IS NOT NULL);
ALTER TABLE maildrop_status_tbl ADD CONSTRAINT mdropstat$mid$nn CHECK (mailing_id IS NOT NULL);
CREATE INDEX mstat$mailing_id$idx ON maildrop_status_tbl (mailing_id) TABLESPACE data_emmaux_idx;
CREATE INDEX mstat$senddate$idx ON maildrop_status_tbl (senddate) TABLESPACE data_emmaux_idx;
CREATE INDEX mstat$status_field$idx ON maildrop_status_tbl (status_field) TABLESPACE data_emmaux_idx;
CREATE INDEX mstat$coid$idx ON maildrop_status_tbl (company_id) TABLESPACE data_emmaux_idx;
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$maildrop_status_tbl FOREIGN KEY (id) REFERENCES maildrop_status_tbl (status_id);
COMMENT ON TABLE maildrop_status_tbl IS 'process-tbl for handling sending and generation of mailings';
COMMENT ON COLUMN maildrop_status_tbl.gendate IS 'timestamp to start generation';
COMMENT ON COLUMN maildrop_status_tbl.genstatus IS '0=to be done, not started yet, 1=ready to start (generation starts with this value only), 2=generation in progress, 3=done, 4=problem was solved manually (genstatus of T- and W-mailings will be set from 0 to 1 by backend code, if gendate < CURRENT_TIMESTAMP)';
COMMENT ON COLUMN maildrop_status_tbl.genchange IS 'last change-timestamp for genstatus (used for monitoring issues)';
COMMENT ON COLUMN maildrop_status_tbl.blocksize IS 'max recipients per generated block';
COMMENT ON COLUMN maildrop_status_tbl.status_id IS 'unique ID, use maildrop_status_tbl_seq';
COMMENT ON COLUMN maildrop_status_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN maildrop_status_tbl.status_field IS 'A=Admin, T=Test, W= World, E=Event (action), R=Rule (date) (C, X - deprecated), V=Verification (inbox preview), P=Preview (webview)';
COMMENT ON COLUMN maildrop_status_tbl.mailing_id IS 'references mailing (mailing_tbl)';
COMMENT ON COLUMN maildrop_status_tbl.senddate IS 'timestamp to send out mailing';
COMMENT ON COLUMN maildrop_status_tbl.step IS 'timeintervall (in min) between sending 2 blocks';
COMMENT ON COLUMN maildrop_status_tbl.max_recipients IS 'limit recipients to this value, if NOT NULL AND >0 during mail generation';
COMMENT ON COLUMN maildrop_status_tbl.admin_test_target_id IS 'This is an optional reference to dyn_target_tbl.target_id for admin and test mailings to restrict the recipients';
COMMENT ON COLUMN maildrop_status_tbl.optimize_mail_generation IS 'NULL=none, day=optimized for current day, 24h=optimized for next 24h';
COMMENT ON COLUMN maildrop_status_tbl.selected_test_recipients IS 'If set to 1, then the recipients for a test mailing are selected from test_recipients_tbl';
COMMENT ON COLUMN maildrop_status_tbl.origin IS 'Hostname of service which created this entry';
COMMENT ON COLUMN maildrop_status_tbl.processed_by IS 'Hostname of service which processed this entry';
COMMENT ON COLUMN maildrop_status_tbl.overwrite_test_recipient IS 'If selected_test_recipients is set and this value is larger than 0, this will be interpreted as the customer_id to sent all test mails to';

CREATE TABLE mailinglist_tbl
(
    mailinglist_id       SERIAL PRIMARY KEY,
    company_id           INTEGER,
    description          VARCHAR(500),
    shortname            VARCHAR(100),
    auto_url             VARCHAR(200),
    remove_data          INTEGER   DEFAULT 0,
    rdir_domain          VARCHAR(200),
    creation_date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted              SMALLINT  DEFAULT 0,
    binding_clean        SMALLINT  DEFAULT 0,
    freq_counter_enabled SMALLINT,
    sender_email         VARCHAR(200),
    reply_email          VARCHAR(200)
) TABLESPACE data_emmaux;
ALTER TABLE mailinglist_tbl ADD CONSTRAINT mlist$mlid$nn CHECK (mailinglist_id IS NOT NULL);
ALTER TABLE mailinglist_tbl ADD CONSTRAINT mlist$shname$nn CHECK (shortname IS NOT NULL);
ALTER TABLE mailinglist_tbl ADD CONSTRAINT mlist$deleted$nn CHECK (deleted IS NOT NULL);
CREATE INDEX mailinglist$coid$idx ON mailinglist_tbl (company_id) TABLESPACE data_emmaux_idx;
ALTER TABLE customer_1_binding_tbl ADD CONSTRAINT cust1b$mid$fk FOREIGN KEY (mailinglist_id) REFERENCES mailinglist_tbl (mailinglist_id);
COMMENT ON TABLE mailinglist_tbl IS 'stores mailinglists';
COMMENT ON COLUMN mailinglist_tbl.mailinglist_id IS 'unique ID, mailinglist_tbl_seq';
COMMENT ON COLUMN mailinglist_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN mailinglist_tbl.description IS 'comment on mailinglist';
COMMENT ON COLUMN mailinglist_tbl.shortname IS 'mailinglist name';
COMMENT ON COLUMN mailinglist_tbl.auto_url IS 'legacy';
COMMENT ON COLUMN mailinglist_tbl.remove_data IS 'legacy?';
COMMENT ON COLUMN mailinglist_tbl.rdir_domain IS '(default) ridir domain for this mailinglist, overwrites company_id.rdir_domain, if NOT NULL';
COMMENT ON COLUMN mailinglist_tbl.creation_date IS 'mailinglist creation date';
COMMENT ON COLUMN mailinglist_tbl.change_date IS 'mailinglist last change';
COMMENT ON COLUMN mailinglist_tbl.deleted IS '1=yes';
COMMENT ON COLUMN mailinglist_tbl.binding_clean IS 'Flag for nightly cleaning of bindings 1=on, 0=off';
COMMENT ON COLUMN mailinglist_tbl.freq_counter_enabled IS 'Frequency counting is enabled for this mailinglist';
COMMENT ON COLUMN mailinglist_tbl.sender_email IS 'Email of sender';
COMMENT ON COLUMN mailinglist_tbl.reply_email IS 'Email for reply';

CREATE TABLE mailing_account_tbl
(
    mailer             VARCHAR(30),
    mailing_id         INTEGER,
    company_id         INTEGER,
    mailinglist_id     INTEGER,
    mailtype           INTEGER,
    no_of_mailings     INTEGER,
    no_of_bytes        INTEGER,
    timestamp          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    maildrop_id        INTEGER,
    mailing_account_id SERIAL,
    status_field       VARCHAR(10),
    blocknr            INTEGER,
    mediatype          INTEGER,
    skip               INTEGER,
    chunks             INTEGER
) TABLESPACE data_accounting;
ALTER TABLE mailing_account_tbl ADD CONSTRAINT mlacc$timestamp$nn CHECK (timestamp IS NOT NULL);
CREATE INDEX macc$mid_noofml$idx ON mailing_account_tbl (mailing_id, no_of_mailings) TABLESPACE data_emmaux_idx;
CREATE INDEX macc$mid_noofml_stat$idx ON mailing_account_tbl (mailing_id, no_of_mailings, status_field) TABLESPACE data_emmaux_idx;
CREATE INDEX macc$tmst$idx ON mailing_account_tbl (timestamp) TABLESPACE data_emmaux_idx;
CREATE INDEX mailingacc$maildropid$idx ON mailing_account_tbl (maildrop_id) TABLESPACE data_emmaux_idx;
CREATE INDEX mailing_acc$coid_mid_mdid$idx ON mailing_account_tbl (company_id, mailing_id, maildrop_id) TABLESPACE data_emmaux_idx;
CREATE INDEX mailing_acc$mltp_nofm$idx ON mailing_account_tbl (mailtype, no_of_mailings) TABLESPACE data_emmaux_idx;
ALTER TABLE mailing_account_tbl ADD CONSTRAINT mailing_acc$maid$pk PRIMARY KEY (mailing_account_id);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$mailing_account_tbl FOREIGN KEY (id) REFERENCES mailing_account_tbl (mailing_account_id);
COMMENT ON TABLE mailing_account_tbl IS 'logs detailinformation about sending-process';
COMMENT ON COLUMN mailing_account_tbl.mailer IS 'name of (package-) processing mailer';
COMMENT ON COLUMN mailing_account_tbl.mailing_id IS 'references mailing (mailing_tbl)';
COMMENT ON COLUMN mailing_account_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN mailing_account_tbl.mailinglist_id IS 'references mailinglist (mailinglist_tbl)';
COMMENT ON COLUMN mailing_account_tbl.mailtype IS '0-text, 1-html, 2-offline-html (from recipient settings)';
COMMENT ON COLUMN mailing_account_tbl.no_of_mailings IS 'number of mailings sent (within this package)';
COMMENT ON COLUMN mailing_account_tbl.no_of_bytes IS 'number of bytes (within this package)';
COMMENT ON COLUMN mailing_account_tbl.timestamp IS 'entry creation / last change';
COMMENT ON COLUMN mailing_account_tbl.maildrop_id IS 'references maildrop-status-entry (maildrop_status_tbl)';
COMMENT ON COLUMN mailing_account_tbl.mailing_account_id IS 'unique ID, mailing_account_tbl_seq';
COMMENT ON COLUMN mailing_account_tbl.status_field IS 'A=Admin, T=Test, W= Wolrd, E=Event, R=rule-based, V=predelivery-test, D=onDemand (C, X - deprecated)';
COMMENT ON COLUMN mailing_account_tbl.blocknr IS 'XML-block number';
COMMENT ON COLUMN mailing_account_tbl.mediatype IS '0 = Email, 1 = Fax, 2 = Print, 3 = MMS, 4 = SMS(from recipient-binding settings)';
COMMENT ON COLUMN mailing_account_tbl.skip IS 'number of messages not generate due to skipping when empty content is detected';
COMMENT ON COLUMN mailing_account_tbl.chunks IS 'if one message is sent out in several chunks, this represents the number of physical sent out chunks (e.g. for SMS)';

CREATE TABLE mailing_errorlog_tbl
(
    company_id     INTEGER,
    mailinglist_id INTEGER,
    mailing_id     INTEGER,
    customer_id    INTEGER,
    missing_tag    VARCHAR(128),
    creation_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_accounting;
ALTER TABLE mailing_errorlog_tbl ADD CONSTRAINT merrlog$coid$nn CHECK (company_id IS NOT NULL);
ALTER TABLE mailing_errorlog_tbl ADD CONSTRAINT merrlog$mlid$nn CHECK (mailinglist_id IS NOT NULL);
ALTER TABLE mailing_errorlog_tbl ADD CONSTRAINT merrlog$mid$nn CHECK (mailing_id IS NOT NULL);
ALTER TABLE mailing_errorlog_tbl ADD CONSTRAINT merrlog$cuid$nn CHECK (customer_id IS NOT NULL);
ALTER TABLE mailing_errorlog_tbl ADD CONSTRAINT merrlog$misstag$nn CHECK (missing_tag IS NOT NULL);
COMMENT ON TABLE mailing_errorlog_tbl IS 'legacy';
COMMENT ON COLUMN mailing_errorlog_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN mailing_errorlog_tbl.mailinglist_id IS 'references mailinglist (mailinglist_tbl)';
COMMENT ON COLUMN mailing_errorlog_tbl.mailing_id IS 'references mailing (mailing_tbl)';
COMMENT ON COLUMN mailing_errorlog_tbl.customer_id IS 'references recipient (customer_xxx_tbl)';
COMMENT ON COLUMN mailing_errorlog_tbl.missing_tag IS '?';
COMMENT ON COLUMN mailing_errorlog_tbl.creation_date IS 'entry creation date';

CREATE TABLE mailing_tbl
(
    archived                  INTEGER   DEFAULT 0,
    is_template               INTEGER   DEFAULT 0,
    target_expression         VARCHAR(1000),
    split_id                  INTEGER   DEFAULT 0,
    needs_target              SMALLINT  DEFAULT 0,
    mailing_id                SERIAL PRIMARY KEY,
    mailtemplate_id           INTEGER,
    mailinglist_id            INTEGER,
    description               VARCHAR(500),
    shortname                 VARCHAR(100),
    company_id                INTEGER,
    auto_url                  VARCHAR(200),
    mailing_type              INTEGER   DEFAULT 0,
    action_id                 INTEGER   DEFAULT 0,
    deleted                   SMALLINT  DEFAULT 0,
    campaign_id               INTEGER,
    test_lock                 INTEGER,
    work_status               VARCHAR(80),
    statmail_recp             VARCHAR(512),
    unique_link_count         INTEGER,
    creation_date             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cms_has_classic_content   INTEGER   DEFAULT 0,
    dynamic_template          INTEGER,
    openaction_id             INTEGER   DEFAULT 0,
    clickaction_id            INTEGER   DEFAULT 0,
    delivered                 INTEGER   DEFAULT 0,
    plan_date                 TIMESTAMP,
    priority                  INTEGER,
    content_type              VARCHAR(20),
    is_prioritization_allowed INTEGER,
    is_text_version_required  SMALLINT  DEFAULT 1 NOT NULL,
    statmail_onerroronly      INTEGER,
    locking_admin_id          INTEGER   DEFAULT NULL,
    locking_expire_timestamp  TIMESTAMP DEFAULT NULL,
    freq_counter_disabled     SMALLINT,
    mailerset                 VARCHAR(100),
    is_grid                   INTEGER   DEFAULT 0,
    clearance_email           VARCHAR(500),
    clearance_threshold       INTEGER,
    send_date                 TIMESTAMP,
    workflow_id               INTEGER   DEFAULT 0,
    is_post                   INTEGER   DEFAULT 0
) TABLESPACE data_accounting;
ALTER TABLE mailing_tbl ADD CONSTRAINT mailing_tbl$coid$nn CHECK (company_id IS NOT NULL);
ALTER TABLE mailing_tbl ADD CONSTRAINT mailing$split_id$nn CHECK (split_id IS NOT NULL);
CREATE INDEX mailingtbl$mlid$idx ON mailing_tbl (mailinglist_id) TABLESPACE data_emmaux_idx;
CREATE INDEX mailing_tbl$mid_mlid$idx ON mailing_tbl (mailing_id, mailinglist_id) TABLESPACE data_emmaux_idx;
CREATE INDEX mailing$coid$mid$mtype$del$idx ON mailing_tbl (company_id, mailing_id, mailing_type, deleted) TABLESPACE data_emmaux_idx;
ALTER TABLE mailing_tbl ADD CONSTRAINT mailing$deleted$nn CHECK (deleted IS NOT NULL);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$mailing_tbl FOREIGN KEY (id) REFERENCES mailing_tbl (mailing_id);
COMMENT ON TABLE mailing_tbl IS 'stores mailing meta-data';
COMMENT ON COLUMN mailing_tbl.archived IS '1 = yes, mailing in online - archive';
COMMENT ON COLUMN mailing_tbl.is_template IS '1 = this is a template, 0 = this is a mailing';
COMMENT ON COLUMN mailing_tbl.target_expression IS '(combination of) Target-ID(s)';
COMMENT ON COLUMN mailing_tbl.split_id IS 'listsplit - TargetID';
COMMENT ON COLUMN mailing_tbl.needs_target IS '1 = targetgroup is mandatory';
COMMENT ON COLUMN mailing_tbl.mailing_id IS 'unique ID, use mailing_tbl_seq';
COMMENT ON COLUMN mailing_tbl.mailtemplate_id IS 'template used for mailing creation (parent reference)';
COMMENT ON COLUMN mailing_tbl.mailinglist_id IS 'references mailinglist (mailinglist_tbl)';
COMMENT ON COLUMN mailing_tbl.description IS 'comment on mailing';
COMMENT ON COLUMN mailing_tbl.shortname IS 'mailing name';
COMMENT ON COLUMN mailing_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN mailing_tbl.auto_url IS 'basic link URL (from mailinglist-settings)';
COMMENT ON COLUMN mailing_tbl.action_id IS 'legacy PROJ-711';
COMMENT ON COLUMN mailing_tbl.deleted IS '1=yes, not shown in EMM, 0=no, 2=mailing data deleted, not shown in EMM';
COMMENT ON COLUMN mailing_tbl.campaign_id IS 'Archive this mailing belongs to';
COMMENT ON COLUMN mailing_tbl.test_lock IS 'locked during test-sending in order to avoid cascade-effects. Used also for approval feature (0: approved, 1: not approved)';
COMMENT ON COLUMN mailing_tbl.work_status IS 'e.g.: mailing.status.scheduled, mailing.status.new, mailing.status.sent (not considered by backend)';
COMMENT ON COLUMN mailing_tbl.statmail_recp IS 'recipient-address for mailing statistics';
COMMENT ON COLUMN mailing_tbl.unique_link_count IS 'number of unique links used in mailing';
COMMENT ON COLUMN mailing_tbl.creation_date IS 'mailing creation date';
COMMENT ON COLUMN mailing_tbl.change_date IS 'mailing last change';
COMMENT ON COLUMN mailing_tbl.cms_has_classic_content IS 'compatibility mode';
COMMENT ON COLUMN mailing_tbl.dynamic_template IS '1 = yes (for templates: modifications of this template will be published to mailings, for mailings: published template modifications will be adapted), 0/NULL = no/off';
COMMENT ON COLUMN mailing_tbl.openaction_id IS 'references action to execute if mailing is opened, e.g. to count up a recipient profile field (interest counter)';
COMMENT ON COLUMN mailing_tbl.clickaction_id IS 'references action to execute if mailing is clicked';
COMMENT ON COLUMN mailing_tbl.delivered IS 'number of sent mailings, aviable after sending';
COMMENT ON COLUMN mailing_tbl.plan_date IS 'send date (for later sendings)';
COMMENT ON COLUMN mailing_tbl.mailing_type IS '0=world-mailing, 1=event-based, 2=date-based, 3=follow up, 4=interval-based';
COMMENT ON COLUMN mailing_tbl.priority IS 'priority for a template, 0/NULL no priority set, otherwise the higher the number, the higher is the priority';
COMMENT ON COLUMN mailing_tbl.content_type IS 'Content type description for this mailing. Allowed values are transaction or advertising';
COMMENT ON COLUMN mailing_tbl.is_prioritization_allowed IS 'If set to 1, then prioritization for this mailing will be applied see mailing priority for more details';
COMMENT ON COLUMN mailing_tbl.is_text_version_required IS 'If set to 1, mailing must have a text version (otherwise is cannot be sent, see GWUA-3991)';
COMMENT ON COLUMN mailing_tbl.statmail_onerroronly IS 'If true(1), the report email on delivery statistics is only sent in erroneous cases';
COMMENT ON COLUMN mailing_tbl.locking_admin_id IS 'if set: references EMM-User (admin_tbl) that was the last one who acquired the locking';
COMMENT ON COLUMN mailing_tbl.locking_expire_timestamp IS 'if set: the timestamp when a locking is not valid anymore';
COMMENT ON COLUMN mailing_tbl.freq_counter_disabled IS 'Disable frequency counting, if it is enabled for the assigned mailinglist';
COMMENT ON COLUMN mailing_tbl.mailerset IS 'per mailing specific mailerset, if differs from default mailerset of company';
COMMENT ON COLUMN mailing_tbl.is_grid IS '0 = classic mailing, 1 = grid mailing';
COMMENT ON COLUMN mailing_tbl.clearance_email IS 'email address(es) to be informed if a rule based mailing exceeds clearance_threshold';
COMMENT ON COLUMN mailing_tbl.clearance_threshold IS 'threshold, when rulebased mailing exceeds this value an email is sent to clearance_email and the generated mails are not sent';
COMMENT ON COLUMN mailing_tbl.send_date IS 'start timestamp of sending a world mailing';
COMMENT ON COLUMN mailing_tbl.workflow_id IS 'workflow id which is the mailing depending on';
INSERT INTO mailing_tbl (company_id, work_status, deleted) VALUES (1, 'mailing.status.new', 1);

CREATE TABLE mailing_mt_tbl
(
    mailing_id    INTEGER,
    mediatype     INTEGER,
    priority      INTEGER,
    status        INTEGER,
    param         VARCHAR(4000),
    mediatype_id  INTEGER,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_accounting;
ALTER TABLE mailing_mt_tbl ADD CONSTRAINT mailing_mt$mid_mt$pk PRIMARY KEY (mailing_id, mediatype);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$mailing_mt_tbl FOREIGN KEY (id, mediatype) REFERENCES mailing_mt_tbl (mailing_id, mediatype);
COMMENT ON TABLE mailing_mt_tbl IS '[private_data] contains mailing parameters per mediatype - e.g. subject, sender-address etc.';
COMMENT ON COLUMN mailing_mt_tbl.mailing_id IS 'references mailing (mailing_tbl)';
COMMENT ON COLUMN mailing_mt_tbl.mediatype IS '0 = Email, 1 = Fax, 2 = Print, 3 = MMS, 4 = SMS';
COMMENT ON COLUMN mailing_mt_tbl.priority IS 'ranking, if there is more than one entry per mailing';
COMMENT ON COLUMN mailing_mt_tbl.status IS '0=not used, 1=not active, 2=active';
COMMENT ON COLUMN mailing_mt_tbl.param IS '[private_data] list of parameters, e.g. remove_dups=true => check for emailduplicates on sending';
COMMENT ON COLUMN mailing_mt_tbl.mediatype_id IS 'legacy';
COMMENT ON COLUMN mailing_mt_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN mailing_mt_tbl.change_date IS 'entry last change';

CREATE TABLE mailing_backend_log_tbl
(
    mailing_id    INTEGER NOT NULL,
    current_mails INTEGER,
    total_mails   INTEGER,
    status_id     INTEGER,
    timestamp     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_accounting;
COMMENT ON TABLE mailing_backend_log_tbl IS 'mailing-generation process-tbl';
COMMENT ON COLUMN mailing_backend_log_tbl.mailing_id IS 'references mailing (mailing_tbl)';
COMMENT ON COLUMN mailing_backend_log_tbl.current_mails IS 'number of mailings alread generated';
COMMENT ON COLUMN mailing_backend_log_tbl.total_mails IS 'number of mailings to generate';
COMMENT ON COLUMN mailing_backend_log_tbl.status_id IS 'references maildrop_status_tbl.status_id';
COMMENT ON COLUMN mailing_backend_log_tbl.timestamp IS 'entry last change';
COMMENT ON COLUMN mailing_backend_log_tbl.creation_date IS 'entry creation date';

CREATE TABLE mailtrack_1_tbl
(
    maildrop_status_id INTEGER,
    customer_id        INTEGER,
    timestamp          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    mailing_id         INTEGER,
    mediatype          INTEGER
) TABLESPACE data_success;
ALTER TABLE mailtrack_1_tbl ADD CONSTRAINT mt1$mdsid$nn CHECK (maildrop_status_id IS NOT NULL);
ALTER TABLE mailtrack_1_tbl ADD CONSTRAINT mt1$cuid$nn CHECK (customer_id IS NOT NULL);
CREATE INDEX mailtr1$cid$idx ON mailtrack_1_tbl (customer_id) TABLESPACE data_cust_index;
CREATE INDEX mailtr1$mdrstatid$idx ON mailtrack_1_tbl (maildrop_status_id) TABLESPACE data_cust_index;
CREATE INDEX mailtr1$mid$idx ON mailtrack_1_tbl (mailing_id) TABLESPACE data_cust_index;
CREATE INDEX mailtr1$ts$idx ON mailtrack_1_tbl (timestamp) TABLESPACE data_cust_index;
COMMENT ON TABLE mailtrack_1_tbl IS '(automation-package) stores sended mailings per recipient, would be deleted after a period (see company_info_tbl), filled during mailGENERATION, so stopping a mailing later (while sending e.g.) has entries for all recipients anyway!';
COMMENT ON COLUMN mailtrack_1_tbl.maildrop_status_id IS 'references maildrop_status_tbl.status_id';
COMMENT ON COLUMN mailtrack_1_tbl.customer_id IS 'references recipient (customer_xxx_tbl.customer_id)';
COMMENT ON COLUMN mailtrack_1_tbl.timestamp IS 'sending timestamp';
COMMENT ON COLUMN mailtrack_1_tbl.mailing_id IS 'Mailing reference';

CREATE TABLE onepixellog_1_tbl
(
    customer_id  INTEGER,
    mailing_id   INTEGER,
    company_id   INTEGER,
    ip_adr       VARCHAR(50),
    timestamp    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    open_count   INTEGER,
    mobile_count INTEGER,
    first_open   TIMESTAMP,
    last_open    TIMESTAMP
) TABLESPACE data_cust_table;
ALTER TABLE onepixellog_1_tbl ADD CONSTRAINT onpx1$cuid$nn CHECK (customer_id IS NOT NULL);
ALTER TABLE onepixellog_1_tbl ADD CONSTRAINT onpx1$mid$nn CHECK (mailing_id IS NOT NULL);
ALTER TABLE onepixellog_1_tbl ADD CONSTRAINT onpx1$coid$nn CHECK (company_id IS NOT NULL);
CREATE INDEX onpx1$coid_mlid_cuid$idx ON onepixellog_1_tbl (company_id, mailing_id, customer_id) TABLESPACE data_cust_index;
CREATE INDEX onpx1$mid$idx ON onepixellog_1_tbl (mailing_id) TABLESPACE data_cust_index;
CREATE INDEX onpx1$mlid_cuid$idx ON onepixellog_1_tbl (mailing_id, customer_id);
COMMENT ON TABLE onepixellog_1_tbl IS 'stores opening-data';
COMMENT ON COLUMN onepixellog_1_tbl.customer_id IS 'references recipient (customer_xxx_tbl.customer_id)';
COMMENT ON COLUMN onepixellog_1_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN onepixellog_1_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN onepixellog_1_tbl.ip_adr IS 'IP where the opening came from';
COMMENT ON COLUMN onepixellog_1_tbl.timestamp IS '(last) open timestamp';
COMMENT ON COLUMN onepixellog_1_tbl.open_count IS 'sum of all openings (per mailing, per recipient)';
COMMENT ON COLUMN onepixellog_1_tbl.mobile_count IS 'sum of all mobile openings (per mailing, per recipient)';
COMMENT ON COLUMN onepixellog_1_tbl.first_open IS 'First time this customer opened the defined mailing';
COMMENT ON COLUMN onepixellog_1_tbl.last_open IS 'Last time this customer opened the defined mailing';

CREATE TABLE rdirlog_1_tbl
(
    customer_id     INTEGER,
    url_id          INTEGER,
    company_id      INTEGER,
    timestamp       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_adr          VARCHAR(50),
    mailing_id      INTEGER,
    device_class_id INTEGER,
    device_id       INTEGER,
    client_id       INTEGER,
    position        INTEGER
) TABLESPACE data_cust_table;
CREATE INDEX rlog1$mid_urlid_cuid$idx ON rdirlog_1_tbl (mailing_id, url_id, customer_id) TABLESPACE data_cust_index;
CREATE INDEX rlog1$ciddevclidmlid$idx ON rdirlog_1_tbl (customer_id, device_class_id, mailing_id) TABLESPACE data_cust_index;
CREATE INDEX rlog1$tmst$idx ON rdirlog_1_tbl (timestamp);
COMMENT ON TABLE rdirlog_1_tbl IS 'stores click-data';
COMMENT ON COLUMN rdirlog_1_tbl.customer_id IS 'references recipient (customer_xxx_tbl.customer_id)';
COMMENT ON COLUMN rdirlog_1_tbl.url_id IS 'references url (rdir_url_tbl.url_id)';
COMMENT ON COLUMN rdirlog_1_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN rdirlog_1_tbl.timestamp IS 'click timestamp';
COMMENT ON COLUMN rdirlog_1_tbl.ip_adr IS 'IP where the click came from';
COMMENT ON COLUMN rdirlog_1_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN rdirlog_1_tbl.device_class_id IS '1=DESKTOP, 2=MOBILE, 3=TABLET, 4=SMARTTV';
COMMENT ON COLUMN rdirlog_1_tbl.device_id IS 'references device (device_tbl.device_id)';
COMMENT ON COLUMN rdirlog_1_tbl.client_id IS 'Reference to client_tbl of client software used for a link click';
COMMENT ON COLUMN rdirlog_1_tbl.position IS 'position of a link click if the link is more than once in the content';

CREATE TABLE rdirlog_1_val_num_tbl
(
    company_id    INTEGER,
    customer_id   INTEGER,
    ip_adr        VARCHAR(50),
    mailing_id    INTEGER,
    session_id    INTEGER,
    timestamp     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    num_parameter DOUBLE PRECISION,
    page_tag      VARCHAR(30)
) TABLESPACE data_cust_table;
ALTER TABLE rdirlog_1_val_num_tbl ADD CONSTRAINT rdvalnum1$coid$nn CHECK (company_id IS NOT NULL);
CREATE INDEX rvalnum1$cod_cid_mid$idx ON rdirlog_1_val_num_tbl (company_id, customer_id, mailing_id) TABLESPACE data_cust_index;
CREATE INDEX rvalnum1$mid_pagetag$idx ON rdirlog_1_val_num_tbl (mailing_id, page_tag) TABLESPACE data_cust_index;
CREATE INDEX rvalnum1$cid_mid$idx ON rdirlog_1_val_num_tbl (customer_id, mailing_id) TABLESPACE data_cust_index;
COMMENT ON TABLE rdirlog_1_val_num_tbl IS 'stores (shop / website) tracking data';
COMMENT ON COLUMN rdirlog_1_val_num_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN rdirlog_1_val_num_tbl.customer_id IS 'references recipient (customer_xxx_tbl.customer_id)';
COMMENT ON COLUMN rdirlog_1_val_num_tbl.ip_adr IS 'IP where the trackingdata came from';
COMMENT ON COLUMN rdirlog_1_val_num_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN rdirlog_1_val_num_tbl.session_id IS 'cookie - sessionID';
COMMENT ON COLUMN rdirlog_1_val_num_tbl.timestamp IS 'entry timestamp';
COMMENT ON COLUMN rdirlog_1_val_num_tbl.num_parameter IS 'pageID for page-view data, sales volume for sales tracking ';
COMMENT ON COLUMN rdirlog_1_val_num_tbl.page_tag IS 'revenue= shop sales tracking, page-view = retargeting (page view) data, more might be defined';

CREATE TABLE rdirlog_userform_1_tbl
(
    form_id         INTEGER,
    customer_id     INTEGER,
    url_id          INTEGER,
    company_id      INTEGER,
    timestamp       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_adr          VARCHAR(50),
    mailing_id      INTEGER,
    device_class_id INTEGER,
    device_id       INTEGER,
    client_id       INTEGER
) TABLESPACE data_cust_table;
CREATE INDEX rlogform1$fid_urlid$idx ON rdirlog_userform_1_tbl (form_id, url_id) TABLESPACE data_cust_index;
COMMENT ON TABLE rdirlog_userform_1_tbl IS 'storing form-statistics';
COMMENT ON COLUMN rdirlog_userform_1_tbl.form_id IS 'references form (userform_tbl.form_id)';
COMMENT ON COLUMN rdirlog_userform_1_tbl.customer_id IS 'references recipient (customer_xxx_tbl.customer_id)';
COMMENT ON COLUMN rdirlog_userform_1_tbl.url_id IS 'references url (rdir_url_tbl.url_id)';
COMMENT ON COLUMN rdirlog_userform_1_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN rdirlog_userform_1_tbl.timestamp IS 'entry last change';
COMMENT ON COLUMN rdirlog_userform_1_tbl.ip_adr IS 'entry IP-address';
COMMENT ON COLUMN rdirlog_userform_1_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN rdirlog_userform_1_tbl.device_class_id IS '1=DESKTOP, 2=MOBILE, 3=TABLET, 4=SMARTTV';
COMMENT ON COLUMN rdirlog_userform_1_tbl.device_id IS 'references device (device_tbl.device_id)';
COMMENT ON COLUMN rdirlog_userform_1_tbl.client_id IS 'Reference to client_tbl of client software used for a userform link click';

CREATE TABLE rdir_action_tbl
(
    action_id     SERIAL PRIMARY KEY,
    company_id    INTEGER,
    shortname     VARCHAR(50),
    description   VARCHAR(1000),
    action_type   INTEGER   DEFAULT 0,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted       SMALLINT  DEFAULT 0,
    active        SMALLINT  DEFAULT 1 NOT NULL,
    advertising   SMALLINT  DEFAULT 0
) TABLESPACE data_emmaux;
ALTER TABLE rdir_action_tbl ADD CONSTRAINT rdiraction$actid$nn CHECK (action_id IS NOT NULL);
ALTER TABLE rdir_action_tbl ADD CONSTRAINT rdiraction$coid$nn CHECK (company_id IS NOT NULL);
ALTER TABLE rdir_action_tbl ADD CONSTRAINT rdiraction$shname$nn CHECK (shortname IS NOT NULL);
ALTER TABLE rdir_action_tbl ADD CONSTRAINT rdiraction$desc$nn CHECK (description IS NOT NULL);
ALTER TABLE rdir_action_tbl ADD CONSTRAINT rdiraction$deleted$nn CHECK (deleted IS NOT NULL);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$rdir_action_tbl FOREIGN KEY (id) REFERENCES rdir_action_tbl (action_id);
COMMENT ON TABLE rdir_action_tbl IS 'stores actions';
COMMENT ON COLUMN rdir_action_tbl.action_id IS 'unique ID, use rdir_action_tbl_seq';
COMMENT ON COLUMN rdir_action_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN rdir_action_tbl.shortname IS 'action name';
COMMENT ON COLUMN rdir_action_tbl.description IS 'comment on action';
COMMENT ON COLUMN rdir_action_tbl.action_type IS 'defines action usage: 0=link only, 1=form only, 9=all';
COMMENT ON COLUMN rdir_action_tbl.creation_date IS 'action creation date';
COMMENT ON COLUMN rdir_action_tbl.change_date IS 'action last change';
COMMENT ON COLUMN rdir_action_tbl.deleted IS '0=no, 1=yes';
COMMENT ON COLUMN rdir_action_tbl.active IS 'If set to 0, then the action should not be available for using by forms and mailings';
COMMENT ON COLUMN rdir_action_tbl.advertising IS 'If set to 1 - will no be executed for the recipient with the tracking veto flag set';

CREATE TABLE rdir_url_tbl
(
    deep_tracking          INTEGER   DEFAULT 0,
    url_id                 SERIAL PRIMARY KEY,
    full_url               VARCHAR(2000),
    mailing_id             INTEGER,
    company_id             INTEGER,
    mailtemplate_id        INTEGER,
    usage                  INTEGER,
    action_id              INTEGER,
    shortname              VARCHAR(500),
    alt_text               VARCHAR(200),
    creation_date          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    extend_url             INTEGER   DEFAULT 0,
    admin_link             INTEGER   DEFAULT 0,
    from_mailing           INTEGER,
    deleted                SMALLINT  DEFAULT 0,
    original_url           VARCHAR(2000),
    static_value           SMALLINT,
    measured_separately    INTEGER   DEFAULT 0 NOT NULL,
    create_substitute_link SMALLINT
) TABLESPACE data_accounting;
ALTER TABLE rdir_url_tbl ADD CONSTRAINT rdirurl$urlid$nn CHECK (url_id IS NOT NULL);
ALTER TABLE rdir_url_tbl ADD CONSTRAINT rdirurl$fullurl$nn CHECK (full_url IS NOT NULL);
ALTER TABLE rdir_url_tbl ADD CONSTRAINT rdirurl$maid$nn CHECK (mailing_id IS NOT NULL);
ALTER TABLE rdir_url_tbl ADD CONSTRAINT rdirurl$exturl$nn CHECK (extend_url IS NOT NULL);
ALTER TABLE rdir_url_tbl ADD CONSTRAINT rdirurl$deleted$nn CHECK (deleted IS NOT NULL);
CREATE INDEX rdir_url$coid_mid$idx ON rdir_url_tbl (company_id, mailing_id) TABLESPACE data_emmaux_idx;
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$rdir_url_tbl FOREIGN KEY (id) REFERENCES rdir_url_tbl (url_id);
COMMENT ON TABLE rdir_url_tbl IS 'stores links in mailings';
COMMENT ON COLUMN rdir_url_tbl.deep_tracking IS '0=not active, 1=using Cookie, 2=using URL-parameter, 3=using Cookie AND URL-parameter';
COMMENT ON COLUMN rdir_url_tbl.url_id IS 'unique ID, use rdir_url_tbl_seq';
COMMENT ON COLUMN rdir_url_tbl.full_url IS 'complete URL';
COMMENT ON COLUMN rdir_url_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN rdir_url_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN rdir_url_tbl.mailtemplate_id IS 'legacy?';
COMMENT ON COLUMN rdir_url_tbl.usage IS '0=not measurable, 1=text-version only, 2=html-version only, 3=always';
COMMENT ON COLUMN rdir_url_tbl.action_id IS 'references action (executed on click) (rdir_action_tbl.action_id)';
COMMENT ON COLUMN rdir_url_tbl.shortname IS 'url-name';
COMMENT ON COLUMN rdir_url_tbl.alt_text IS 'shown alternative Text for a link in statistic';
COMMENT ON COLUMN rdir_url_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN rdir_url_tbl.extend_url IS 'additional parameters';
COMMENT ON COLUMN rdir_url_tbl.admin_link IS '0=no, 1=yes - set e.g. for fullview - links or other administrative links';
COMMENT ON COLUMN rdir_url_tbl.from_mailing IS 'If this is 1, then the URL had been added from the mailing content, otherwise a 3rd party process had added this and should not be removed';
COMMENT ON COLUMN rdir_url_tbl.deleted IS '0=no, 1=yes';
COMMENT ON COLUMN rdir_url_tbl.original_url IS 'If the full_url has modified after sending the original URL is stored here for reference';
COMMENT ON COLUMN rdir_url_tbl.static_value IS 'If this value is 1 then the current value of referenced database columns are passed for redirect requests';
COMMENT ON COLUMN rdir_url_tbl.measured_separately IS 'track link on every position mode activated 1=yes, 0=no';
COMMENT ON COLUMN rdir_url_tbl.create_substitute_link IS '1 = create substitute link on link resolution (see LTS-376)';

CREATE TABLE serverprio_tbl
(
    company_id INTEGER,
    mailing_id INTEGER,
    priority   INTEGER,
    start_date TIMESTAMP,
    end_date   TIMESTAMP
) TABLESPACE data_accounting;
COMMENT ON TABLE serverprio_tbl IS 'allowes to set higher or lower sending priority per mailing or tenant';
COMMENT ON COLUMN serverprio_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN serverprio_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN serverprio_tbl.priority IS '0=disable further sending, >0 sending priority in relation to other ready to send mailings (the higher the value the lower the priority)';
COMMENT ON COLUMN serverprio_tbl.start_date IS '(optional) entry validation start-date';
COMMENT ON COLUMN serverprio_tbl.end_date IS '(optional) entry validation end-date';

CREATE TABLE softbounce_email_tbl
(
    email         VARCHAR(200),
    bnccnt        INTEGER,
    mailing_id    INTEGER,
    creation_date TIMESTAMP,
    timestamp     TIMESTAMP,
    company_id    INTEGER
) TABLESPACE data_bounce;
COMMENT ON TABLE softbounce_email_tbl IS '[secret_data] proccess-tbl for (soft-)bounce-handling';
COMMENT ON COLUMN softbounce_email_tbl.email IS '[secret_data] bounced address';
COMMENT ON COLUMN softbounce_email_tbl.bnccnt IS 'softbounce - counter (per adress, per mailing)';
COMMENT ON COLUMN softbounce_email_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN softbounce_email_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN softbounce_email_tbl.timestamp IS 'entry last change';
COMMENT ON COLUMN softbounce_email_tbl.company_id IS 'tenant - ID (company_tbl)';

CREATE TABLE sourcegroup_tbl
(
    sourcegroup_id   SERIAL PRIMARY KEY,
    sourcegroup_type VARCHAR(2),
    description      VARCHAR(1000),
    timestamp        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creation_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_accounting;
ALTER TABLE sourcegroup_tbl ADD CONSTRAINT sourcegroup_tbl$scrgrid$nn CHECK (sourcegroup_id IS NOT NULL);
ALTER TABLE sourcegroup_tbl ADD CONSTRAINT sourcegroup_tbl$srcgrptp$nn CHECK (sourcegroup_type IS NOT NULL);
ALTER TABLE sourcegroup_tbl ADD CONSTRAINT sourcegroup_tbl$desc$nn CHECK (description IS NOT NULL);
ALTER TABLE datasource_description_tbl ADD CONSTRAINT datasource_desc$srcgrpid$fk FOREIGN KEY (sourcegroup_id) REFERENCES sourcegroup_tbl (sourcegroup_id);
COMMENT ON TABLE sourcegroup_tbl IS 'stores groups for data-sources, e.g. file, DataAgent, ...';
COMMENT ON COLUMN sourcegroup_tbl.sourcegroup_id IS 'unique ID, use sourcegroup_tbl_seq';
COMMENT ON COLUMN sourcegroup_tbl.sourcegroup_type IS 'sourcegroup - code';
COMMENT ON COLUMN sourcegroup_tbl.description IS 'comment on sourcegroup';
COMMENT ON COLUMN sourcegroup_tbl.timestamp IS 'sourcegroup last change';
COMMENT ON COLUMN sourcegroup_tbl.creation_date IS 'sourcegroup creation date';

CREATE TABLE tag_tbl
(
    tag_id      SERIAL PRIMARY KEY,
    tagname     VARCHAR(32),
    selectvalue VARCHAR(500),
    type        VARCHAR(10),
    company_id  INTEGER,
    description VARCHAR(1000),
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deprecated  INTEGER   DEFAULT 0
) TABLESPACE data_accounting;
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$tag_tbl FOREIGN KEY (id) REFERENCES tag_tbl (tag_id);
COMMENT ON TABLE tag_tbl IS 'stores / provides dynamic tags wich could be used in mailings';
COMMENT ON COLUMN tag_tbl.tag_id IS 'unique ID, use tag_tbl_seq';
COMMENT ON COLUMN tag_tbl.tagname IS 'tags name';
COMMENT ON COLUMN tag_tbl.selectvalue IS 'used in SQL request, provides tag - value (except: TYPE = FUNCTION)';
COMMENT ON COLUMN tag_tbl.type IS 'FUNCTION = script based, SIMPLEX = simple tag, COMPLEX=tag using parameters';
COMMENT ON COLUMN tag_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN tag_tbl.description IS 'comment on tag';
COMMENT ON COLUMN tag_tbl.change_date IS 'tag creation / last change date';
COMMENT ON COLUMN tag_tbl.deprecated IS 'If true(1), the tag is not shown in WYSIWYG editor and should not be used anymore';

CREATE TABLE timestamp_tbl
(
    timestamp_id INTEGER,
    description  VARCHAR(250),
    name         VARCHAR(64),
    cur          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    prev         TIMESTAMP,
    temp         TIMESTAMP
) TABLESPACE data_accounting;
COMMENT ON TABLE timestamp_tbl IS 'primary used for dataAgents, stores last run of a special service in order to provide diff - selections since last run';
COMMENT ON COLUMN timestamp_tbl.timestamp_id IS 'unique ID, no sequence set, check max (id)';
COMMENT ON COLUMN timestamp_tbl.description IS '[secret_data] comment (for the service using that entry!)';
COMMENT ON COLUMN timestamp_tbl.name IS '[secret_data] name (of the service using that entry!)';
COMMENT ON COLUMN timestamp_tbl.cur IS 'actuall run - timestamp of this service, set prev = cur at the end of any successful run';
COMMENT ON COLUMN timestamp_tbl.prev IS 'last run - timestamp of this service, set prev = cur at the end of any successful run';
COMMENT ON COLUMN timestamp_tbl.temp IS 'possibility to set another temp timestamp if needed';

CREATE TABLE title_tbl
(
    title_id    SERIAL PRIMARY KEY,
    company_id  INTEGER,
    description VARCHAR(1000)
) TABLESPACE data_cust_table;
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$title_tbl FOREIGN KEY (id) REFERENCES title_tbl (title_id);
COMMENT ON TABLE title_tbl IS 'types for agnTITLE - tags which might be used in mailings';
COMMENT ON COLUMN title_tbl.title_id IS 'unique ID, use title_tbl_seq';
COMMENT ON COLUMN title_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN title_tbl.description IS 'comment on title';

CREATE TABLE title_gender_tbl
(
    title_id INTEGER,
    gender   INTEGER,
    title    VARCHAR(150)
) TABLESPACE data_cust_table;
ALTER TABLE title_gender_tbl ADD CONSTRAINT titgentbl$titid_genid$pk PRIMARY KEY (title_id, gender);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$title_gender_tbl FOREIGN KEY (id, customer_id) REFERENCES title_gender_tbl (title_id, gender);
COMMENT ON TABLE title_gender_tbl IS 'stores specific title values per (company and) title-type and gender';
COMMENT ON COLUMN title_gender_tbl.title_id IS 'title-type (references title_tbl.title_id)';
COMMENT ON COLUMN title_gender_tbl.gender IS 'gender as used in customer_xxx_tbl: 0-male, 1-female, 2-unknown, 5-company';
COMMENT ON COLUMN title_gender_tbl.title IS 'title value for this specific type and gender';

CREATE TABLE trackpoint_def_tbl
(
    company_id    INTEGER,
    pagetag       VARCHAR(2000),
    mailing_id    INTEGER,
    action_id     INTEGER,
    shortname     VARCHAR(2000),
    description   VARCHAR(2000),
    type          INTEGER,
    currency      VARCHAR(200),
    format        INTEGER,
    trackpoint_id SERIAL PRIMARY KEY,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_accounting;
ALTER TABLE trackpoint_def_tbl ADD CONSTRAINT trckpdef$coid$nn CHECK (company_id IS NOT NULL);
ALTER TABLE trackpoint_def_tbl ADD CONSTRAINT trckpdef$pagetag$nn CHECK (pagetag IS NOT NULL);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$trackpoint_def_tbl FOREIGN KEY (id) REFERENCES trackpoint_def_tbl (trackpoint_id);
COMMENT ON TABLE trackpoint_def_tbl IS 'stores details on trackpoints';
COMMENT ON COLUMN trackpoint_def_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN trackpoint_def_tbl.pagetag IS '(unique)trackpoint name (as used in trackpoint url)';
COMMENT ON COLUMN trackpoint_def_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN trackpoint_def_tbl.action_id IS 'references action (rdir_action_tbl.action_id) to be executed on trackpoint call';
COMMENT ON COLUMN trackpoint_def_tbl.shortname IS 'trackpoint shortname';
COMMENT ON COLUMN trackpoint_def_tbl.description IS 'comment on trackpoint';
COMMENT ON COLUMN trackpoint_def_tbl.type IS '0=simple, 1=numeric, 2=alpha numeric';
COMMENT ON COLUMN trackpoint_def_tbl.currency IS '(optional) currency symbol';
COMMENT ON COLUMN trackpoint_def_tbl.format IS 'numeric trackpoints only: 0=float, 1=integer';
COMMENT ON COLUMN trackpoint_def_tbl.trackpoint_id IS 'unique ID, use trackpoint_def_tbl_seq';
COMMENT ON COLUMN trackpoint_def_tbl.creation_date IS '';

CREATE TABLE userform_tbl
(
    form_id              SERIAL PRIMARY KEY,
    company_id           INTEGER,
    formname             VARCHAR(50),
    description          VARCHAR(2000),
    startaction_id       INTEGER,
    endaction_id         INTEGER,
    success_template     TEXT,
    error_template       TEXT,
    creation_date        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    success_url          VARCHAR(500),
    error_url            VARCHAR(500),
    error_use_url        SMALLINT    DEFAULT 0,
    success_use_url      SMALLINT    DEFAULT 0,
    change_date          TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    success_mimetype     VARCHAR(20) DEFAULT '',
    error_mimetype       VARCHAR(20),
    active               SMALLINT    DEFAULT 1 NOT NULL,
    success_builder_json TEXT,
    error_builder_json   TEXT,
    deleted              SMALLINT    DEFAULT 0 NOT NULL
) TABLESPACE data_emmaux;
ALTER TABLE userform_tbl ADD CONSTRAINT userf$coid$nn CHECK (company_id IS NOT NULL);
ALTER TABLE userform_tbl ADD CONSTRAINT userf$formname$nn CHECK (formname IS NOT NULL);
ALTER TABLE userform_tbl ADD CONSTRAINT usrfo$cidname$uq UNIQUE (company_id, formname);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$userform_tbl FOREIGN KEY (id) REFERENCES userform_tbl (form_id);
COMMENT ON TABLE userform_tbl IS 'stores data for form (e.g. opt-out / opt-in forms';
COMMENT ON COLUMN userform_tbl.form_id IS 'unique ID, use userform_tbl_seq';
COMMENT ON COLUMN userform_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN userform_tbl.formname IS 'name of the form';
COMMENT ON COLUMN userform_tbl.description IS 'comment on form';
COMMENT ON COLUMN userform_tbl.startaction_id IS 'references action (rdir_action_tbl.action_id) to be executed on form start';
COMMENT ON COLUMN userform_tbl.endaction_id IS 'references action (rdir_action_tbl.action_id) to be executed on form end';
COMMENT ON COLUMN userform_tbl.success_template IS 'script to execute or site to show on successful startaction execution (velocity)';
COMMENT ON COLUMN userform_tbl.error_template IS 'script to execute or site to show on non-successful startaction execution (velocity)';
COMMENT ON COLUMN userform_tbl.creation_date IS 'form creation date';
COMMENT ON COLUMN userform_tbl.success_url IS 'URL called after successful form execution';
COMMENT ON COLUMN userform_tbl.error_url IS 'URL called after non-successful form execution';
COMMENT ON COLUMN userform_tbl.error_use_url IS '1=use error_url';
COMMENT ON COLUMN userform_tbl.success_use_url IS '1=use success_url';
COMMENT ON COLUMN userform_tbl.change_date IS 'form last change';
COMMENT ON COLUMN userform_tbl.success_mimetype IS 'Mimetype of the success html text';
COMMENT ON COLUMN userform_tbl.error_mimetype IS 'Mimetype of the error html text';
COMMENT ON COLUMN userform_tbl.active IS 'If set to 0, then the form should not be available for using';
COMMENT ON COLUMN userform_tbl.success_builder_json IS 'JSON to store success form builder state';
COMMENT ON COLUMN userform_tbl.error_builder_json IS 'JSON to store error form builder state';
COMMENT ON COLUMN userform_tbl.deleted IS 'deleted? 0 = no, 1 = yes';

CREATE TABLE world_mailing_backend_log_tbl
(
    mailing_id    INTEGER,
    current_mails INTEGER,
    total_mails   INTEGER,
    timestamp     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_accounting;
COMMENT ON TABLE world_mailing_backend_log_tbl IS 'in line with mailing_backend_log_tbl, but world mailings only. filled after mailgeneration is done';
COMMENT ON COLUMN world_mailing_backend_log_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN world_mailing_backend_log_tbl.current_mails IS 'mumber of mails already generated';
COMMENT ON COLUMN world_mailing_backend_log_tbl.total_mails IS 'mumber of mails to send';
COMMENT ON COLUMN world_mailing_backend_log_tbl.timestamp IS 'entry last change';
COMMENT ON COLUMN world_mailing_backend_log_tbl.creation_date IS 'entry creation date';

CREATE TABLE export_predef_tbl
(
    export_predef_id             SERIAL PRIMARY KEY,
    company_id                   INTEGER NOT NULL,
    charset                      VARCHAR(200),
    columns                      TEXT,
    mailinglists                 VARCHAR(1000),
    delimiter_char               VARCHAR(1),
    separator_char               VARCHAR(1),
    target_id                    INTEGER,
    user_status                  INTEGER,
    user_type                    VARCHAR(1),
    shortname                    VARCHAR(400),
    description                  VARCHAR(1000),
    deleted                      SMALLINT    DEFAULT 0,
    mailinglist_id               INTEGER,
    creation_date                TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    timestamp_start              TIMESTAMP,
    timestamp_end                TIMESTAMP,
    creation_date_start          TIMESTAMP,
    creation_date_end            TIMESTAMP,
    mailinglist_bind_start       TIMESTAMP,
    mailinglist_bind_end         TIMESTAMP,
    timestamp_lastdays           INTEGER,
    creation_date_lastdays       INTEGER,
    mailinglist_bind_lastdays    INTEGER,
    always_quote                 INTEGER     DEFAULT 0,
    dateformat                   INTEGER,
    datetimeformat               INTEGER,
    decimalseparator             VARCHAR(1),
    timezone                     VARCHAR(32),
    timestamp_includecurrent     INTEGER,
    creation_date_includecurrent INTEGER,
    ml_bind_includecurrent       INTEGER,
    limits_linked_by_and         INTEGER     DEFAULT 1,
    locale_lang                  VARCHAR(10) DEFAULT NULL,
    locale_country               VARCHAR(10) DEFAULT NULL,
    use_decoded_values           SMALLINT    DEFAULT 0
) TABLESPACE data_accounting;
ALTER TABLE export_predef_tbl ADD CONSTRAINT expredef$deleted$nn CHECK (deleted IS NOT NULL);
COMMENT ON TABLE export_predef_tbl IS 'stores Templates for recipient-export';
COMMENT ON COLUMN export_predef_tbl.export_predef_id IS 'unique ID, use export_predef_tbl_seq';
COMMENT ON COLUMN export_predef_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN export_predef_tbl.charset IS 'charset used in export-file';
COMMENT ON COLUMN export_predef_tbl.columns IS 'list of export-columns';
COMMENT ON COLUMN export_predef_tbl.mailinglists IS 'list of mailinglists to add status of the recipients';
COMMENT ON COLUMN export_predef_tbl.delimiter_char IS '(field)delimiter used in export-file';
COMMENT ON COLUMN export_predef_tbl.separator_char IS 'separator used in export-file';
COMMENT ON COLUMN export_predef_tbl.target_id IS 'target-ID to filter recipients to export';
COMMENT ON COLUMN export_predef_tbl.user_status IS 'user-status to filter recipients to export';
COMMENT ON COLUMN export_predef_tbl.user_type IS 'user-status to filter recipients to export';
COMMENT ON COLUMN export_predef_tbl.shortname IS 'entrys name';
COMMENT ON COLUMN export_predef_tbl.description IS 'comment on entry';
COMMENT ON COLUMN export_predef_tbl.deleted IS '1 = YES';
COMMENT ON COLUMN export_predef_tbl.mailinglist_id IS 'legacy?';
COMMENT ON COLUMN export_predef_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN export_predef_tbl.timestamp_start IS 'if set: filter recipients to export by last profile-change: FROM';
COMMENT ON COLUMN export_predef_tbl.timestamp_end IS 'if set: filter recipients to export by last profile-change: TO';
COMMENT ON COLUMN export_predef_tbl.creation_date_start IS 'if set: filter recipients to export by profile-creation-date: FROM';
COMMENT ON COLUMN export_predef_tbl.creation_date_end IS 'if set: filter recipients to export by profile-creation-date: TO';
COMMENT ON COLUMN export_predef_tbl.mailinglist_bind_start IS 'if set: filter recipients to export by last binding-change: FROM';
COMMENT ON COLUMN export_predef_tbl.mailinglist_bind_end IS 'if set: filter recipients to export by last binding-change: TO';
COMMENT ON COLUMN export_predef_tbl.timestamp_lastdays IS 'if set: filter recipients to export by last profile-change: PERIOD (in days)';
COMMENT ON COLUMN export_predef_tbl.creation_date_lastdays IS 'if set: filter recipients to export by profile-creationdate: PERIOD (in days)';
COMMENT ON COLUMN export_predef_tbl.mailinglist_bind_lastdays IS 'if set: filter recipients to export by last binding-change: PERIOD (in days)';
COMMENT ON COLUMN export_predef_tbl.always_quote IS 'Always use delimiter_char for quotation of strings in csv outout';
COMMENT ON COLUMN export_predef_tbl.dateformat IS 'Format of dates without time, see DateFormat-class';
COMMENT ON COLUMN export_predef_tbl.datetimeformat IS 'Format of dates with time, see DateFormat-class';
COMMENT ON COLUMN export_predef_tbl.decimalseparator IS 'Decimalseparator for float numbers';
COMMENT ON COLUMN export_predef_tbl.timezone IS 'Timezone to export';
COMMENT ON COLUMN export_predef_tbl.timestamp_includecurrent IS 'Include the current day in export data by timestamp';
COMMENT ON COLUMN export_predef_tbl.creation_date_includecurrent IS 'Include the current day in export data by creation date';
COMMENT ON COLUMN export_predef_tbl.ml_bind_includecurrent IS 'Include the current day in export datamailinglist binding';
COMMENT ON COLUMN export_predef_tbl.limits_linked_by_and IS 'Link timelimits by and (1)';
COMMENT ON COLUMN export_predef_tbl.locale_lang IS 'Language part of locale used';
COMMENT ON COLUMN export_predef_tbl.locale_country IS 'Country part of locale used';
COMMENT ON COLUMN export_predef_tbl.use_decoded_values IS 'Determines if values should be exported in decoded format. 0 - No; 1 - Yes';

CREATE TABLE mailloop_tbl
(
    rid                      SERIAL PRIMARY KEY,
    company_id               INTEGER             NOT NULL,
    forward                  VARCHAR(128),
    timestamp                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ar_enable                INTEGER,
    forward_enable           INTEGER,
    shortname                VARCHAR(200)        NOT NULL,
    description              VARCHAR(2000),
    filter_address           VARCHAR(128),
    subscribe_enable         INTEGER   DEFAULT 0 NOT NULL,
    mailinglist_id           INTEGER   DEFAULT 0 NOT NULL,
    form_id                  INTEGER   DEFAULT 0 NOT NULL,
    creation_date            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    spam_required            INTEGER,
    spam_email               VARCHAR(200),
    spam_forward             INTEGER,
    autoresponder_mailing_id INTEGER,
    security_token           VARCHAR(100)
) TABLESPACE data_cust_table;

COMMENT ON TABLE mailloop_tbl IS '[private_data] stores Mailloop-data';
COMMENT ON COLUMN mailloop_tbl.rid IS 'unique ID, use mailloop_tbl_seq';
COMMENT ON COLUMN mailloop_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN mailloop_tbl.forward IS '[private_data] EMailaddress where all not filtered mails would be forwarded';
COMMENT ON COLUMN mailloop_tbl.timestamp IS 'entry last change';
COMMENT ON COLUMN mailloop_tbl.ar_enable IS 'autoresponder is active, if this value is <>0';
COMMENT ON COLUMN mailloop_tbl.forward_enable IS 'forwarding is active, if this value is <>0';
COMMENT ON COLUMN mailloop_tbl.shortname IS 'entrys name';
COMMENT ON COLUMN mailloop_tbl.description IS 'comment on entry';
COMMENT ON COLUMN mailloop_tbl.filter_address IS '[private_data] ';
COMMENT ON COLUMN mailloop_tbl.subscribe_enable IS '1 = opt in by email is active';
COMMENT ON COLUMN mailloop_tbl.mailinglist_id IS 'mailinglistID used for opt in by email';
COMMENT ON COLUMN mailloop_tbl.form_id IS 'form_ID (references user_form_tbl) used for double opt in, 0 = none';
COMMENT ON COLUMN mailloop_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN mailloop_tbl.spam_required IS '1 = yes';
COMMENT ON COLUMN mailloop_tbl.spam_email IS '[private_data]';
COMMENT ON COLUMN mailloop_tbl.spam_forward IS '1 = yes';
COMMENT ON COLUMN mailloop_tbl.autoresponder_mailing_id IS 'Mailing_ID (references mailing_tbl) used as autoresponder';
COMMENT ON COLUMN mailloop_tbl.security_token IS '[secret_data]';

CREATE TABLE mailloop_replies_tbl
(
    id                  SERIAL PRIMARY KEY,
    mailloop_id         INTEGER                             NOT NULL,
    status              SMALLINT  DEFAULT 0                 NOT NULL,
    sender_full_name    VARCHAR(200),
    subject             VARCHAR(200),
    sender_email        VARCHAR(200),
    response_email      VARCHAR(200),
    timestamp           TIMESTAMP,
    creation_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    change_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    content             TEXT,
    content_type        SMALLINT  DEFAULT 0                 NOT NULL,
    raw_message         BYTEA,
    customer_id         INTEGER,
    company_id          INTEGER,
    customer_company_id INTEGER
);
ALTER TABLE mailloop_replies_tbl ADD CONSTRAINT mlooprepl$mlid$fk FOREIGN KEY (mailloop_id) REFERENCES mailloop_tbl (rid);
COMMENT ON TABLE mailloop_replies_tbl IS 'Replies to mailings without forward address set in bounce filter';
COMMENT ON COLUMN mailloop_replies_tbl.id IS 'Unique entry id, use mailloop_replies_tbl_seq';
COMMENT ON COLUMN mailloop_replies_tbl.mailloop_id IS 'Referenced bounce filter';
COMMENT ON COLUMN mailloop_replies_tbl.status IS 'Reply status: 0 - unread, 1 - read';
COMMENT ON COLUMN mailloop_replies_tbl.sender_full_name IS 'Full name of the sender';
COMMENT ON COLUMN mailloop_replies_tbl.subject IS 'Subject of the reply';
COMMENT ON COLUMN mailloop_replies_tbl.sender_email IS 'Email of the sender';
COMMENT ON COLUMN mailloop_replies_tbl.response_email IS 'Email that was responded to';
COMMENT ON COLUMN mailloop_replies_tbl.timestamp IS 'Timestamp of reply';
COMMENT ON COLUMN mailloop_replies_tbl.creation_date IS 'Timestamp of creation';
COMMENT ON COLUMN mailloop_replies_tbl.change_date IS 'Timestamp of last change';
COMMENT ON COLUMN mailloop_replies_tbl.content IS 'Content of the reply';
COMMENT ON COLUMN mailloop_replies_tbl.content_type IS 'Type of the content: 0 - text/plain, 1 - text/html';
COMMENT ON COLUMN mailloop_replies_tbl.raw_message IS 'Message including original headers';
COMMENT ON COLUMN mailloop_replies_tbl.customer_id IS 'ID of original recipient';
COMMENT ON COLUMN mailloop_replies_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN mailloop_replies_tbl.customer_company_id IS 'ID of company of original recipient';

CREATE TABLE rulebased_sent_tbl
(
    mailing_id       INTEGER NOT NULL,
    lastsent         TIMESTAMP,
    creation_date    TIMESTAMP,
    change_date      TIMESTAMP,
    clearance        INTEGER DEFAULT 1,
    clearance_change TIMESTAMP,
    clearance_origin VARCHAR(128),
    clearance_status VARCHAR(128)
) TABLESPACE data_accounting;
ALTER TABLE rulebased_sent_tbl ADD CONSTRAINT rulebase$mid$pk PRIMARY KEY (mailing_id);
ALTER TABLE rulebased_sent_tbl ADD CONSTRAINT rulebase$clrnce$nn CHECK (clearance IS NOT NULL);
COMMENT ON TABLE rulebased_sent_tbl IS 'stores internal management information for sending out date based mailings';
COMMENT ON COLUMN rulebased_sent_tbl.mailing_id IS 'Mailing_ID (references mailing_tbl)';
COMMENT ON COLUMN rulebased_sent_tbl.lastsent IS 'last sending timestamp';
COMMENT ON COLUMN rulebased_sent_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN rulebased_sent_tbl.change_date IS 'entry last change';
COMMENT ON COLUMN rulebased_sent_tbl.clearance IS 'if set to a value larger than 0, sending of this mailing is allowed, otherwise sending is blocked';
COMMENT ON COLUMN rulebased_sent_tbl.clearance_change IS 'last change of clearance flag';
COMMENT ON COLUMN rulebased_sent_tbl.clearance_origin IS 'host which made the last change on clearance';
COMMENT ON COLUMN rulebased_sent_tbl.clearance_status IS 'status why clearance is not granted';

CREATE TABLE import_temporary_tables
(
    session_id           VARCHAR(255),
    temporary_table_name VARCHAR(255),
    host                 VARCHAR(128),
    import_table_name    VARCHAR(255),
    description          VARCHAR(255),
    creation_date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE import_temporary_tables IS 'stores temporary_tables used in a certain session to run imports, also used to lock import in order to prevent rival accesses';
COMMENT ON COLUMN import_temporary_tables.session_id IS 'creating session';
COMMENT ON COLUMN import_temporary_tables.temporary_table_name IS 'name of created table';
COMMENT ON COLUMN import_temporary_tables.host IS 'running host (also used to clean up after hostswitch / restart)';
COMMENT ON COLUMN import_temporary_tables.import_table_name IS 'Db table name block by an running import';
COMMENT ON COLUMN import_temporary_tables.description IS 'Description to inform about the type and user of the import which blocks';
COMMENT ON COLUMN import_temporary_tables.creation_date IS 'Creation date of this blocking entry';

CREATE TABLE login_track_tbl
(
    login_track_id SERIAL,
    ip_address     VARCHAR(50),
    creation_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    login_status   INTEGER,
    username       VARCHAR(200)
) TABLESPACE data_emmaux;
CREATE INDEX logtrck$ip_cdate_stat$idx ON login_track_tbl (ip_address, creation_date, login_status) TABLESPACE data_emmaux_idx;
CREATE INDEX logtrck$user$idx ON login_track_tbl (username) TABLESPACE data_emmaux_idx;
COMMENT ON TABLE login_track_tbl IS '[secret_data] any login-request, successful or not, is stored here (for a certain time)';
COMMENT ON COLUMN login_track_tbl.login_track_id IS 'unique ID, use login_track_tbl_seq';
COMMENT ON COLUMN login_track_tbl.ip_address IS '[secret_data] address where login-request came from';
COMMENT ON COLUMN login_track_tbl.creation_date IS 'login-request timestamp';
COMMENT ON COLUMN login_track_tbl.login_status IS '10 = successful, 20 = failed, 30 = unlock blocked IP, 40 = successful but while IP is locked';
COMMENT ON COLUMN login_track_tbl.username IS '[secret_data] emm-user name used in request';

CREATE TABLE active_subscriber_tbl
(
    company_id         INTEGER,
    active_subscribers INTEGER,
    timestamp          TIMESTAMP
) TABLESPACE data_accounting;
COMMENT ON TABLE active_subscriber_tbl IS 'used by backend - billing scripts for monthly recipient analysis';
COMMENT ON COLUMN active_subscriber_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN active_subscriber_tbl.active_subscribers IS 'number of active subscriber for this company at timestamp';
COMMENT ON COLUMN active_subscriber_tbl.timestamp IS 'timestamp of evaluated the current number of active subscriber';

CREATE TABLE emm_db_errorlog_tbl
(
    company_id    INTEGER,
    errortext     VARCHAR(4000),
    module_name   VARCHAR(200),
    client_info   VARCHAR(500),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_emmaux;
COMMENT ON TABLE emm_db_errorlog_tbl IS 'backend monitoring and error-handling espc. for triggers, catches errors during trigger execution, monitored by backend-script on AGN-Instances';
COMMENT ON COLUMN emm_db_errorlog_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN emm_db_errorlog_tbl.errortext IS 'catched SQL - Error';
COMMENT ON COLUMN emm_db_errorlog_tbl.module_name IS 'Name of e.g. the trigger who caused an error';
COMMENT ON COLUMN emm_db_errorlog_tbl.client_info IS 'OS user and client causing the error';
COMMENT ON COLUMN emm_db_errorlog_tbl.creation_date IS 'entry timestamp';

CREATE TABLE swyn_click_tbl
(
    network_id  VARCHAR(20),
    mailing_id  INTEGER,
    customer_id INTEGER,
    ip_address  VARCHAR(50),
    timestamp   TIMESTAMP,
    selector    VARCHAR(2000)
) TABLESPACE data_emmaux;
COMMENT ON TABLE swyn_click_tbl IS 'This table records all clicks for displaying a shared newsletter on a network.';
COMMENT ON COLUMN swyn_click_tbl.network_id IS 'This is the content of the field "SOURCE" of the SWYN_TBL to identify the network from where the click came.';
COMMENT ON COLUMN swyn_click_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN swyn_click_tbl.customer_id IS 'references recipient who shared the newsletter (customer_xxx_tbl.customer_id)';
COMMENT ON COLUMN swyn_click_tbl.ip_address IS 'The ip-address of the clicker';
COMMENT ON COLUMN swyn_click_tbl.timestamp IS 'The timestamp of the click';
COMMENT ON COLUMN swyn_click_tbl.selector IS 'If the used anon.view is restricted to a part of the newsletter, this contains the selector used to limit the output of the content';

CREATE TABLE swyn_tbl
(
    swyn_id       SERIAL NOT NULL,
    company_id    INTEGER NOT NULL,
    name          VARCHAR(100),
    source        VARCHAR(20),
    charset       VARCHAR(50),
    ordering      INTEGER,
    image         VARCHAR(80),
    icon          BYTEA,
    target        VARCHAR(1000),
    code          VARCHAR(2000),
    creation_date TIMESTAMP,
    timestamp     TIMESTAMP,
    ISIZE         VARCHAR(2000)
) TABLESPACE data_emmaux;
COMMENT ON TABLE swyn_tbl IS 'This table contains the data to be used for the [agnSWYN ...] tag (SWYN = share with your network). This information is used to build a HTML output to simply allow the recipient to share the newsletter via different networks. The link to share this newsletter as well as the icon to be displayed in the newsletter are stored herein';
COMMENT ON COLUMN swyn_tbl.swyn_id IS 'unique ID, use swyn_tbl_seq';
COMMENT ON COLUMN swyn_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN swyn_tbl.name IS 'The name for this entry as selectable by the [agnSWYN ...] parameter networks="...". Special names staring end ending with tow underscores. At the moment these special names are supported: "__prefix__", "__infix__", "__postfix__". These entries are used to build the frame for the output unless the parameter bare="true" is used in the agnSWYN tag';
COMMENT ON COLUMN swyn_tbl.source IS 'A short ID for this network which is used to determinate the source of an anonymous click in the swyn_click_tbl.';
COMMENT ON COLUMN swyn_tbl.charset IS 'The character set to be used to encode free text inserted in the target URL';
COMMENT ON COLUMN swyn_tbl.ordering IS 'This is used to define an order within the display of the networks, if they are not explicit defined';
COMMENT ON COLUMN swyn_tbl.image IS 'The name of the icon.';
COMMENT ON COLUMN swyn_tbl.icon IS 'The icon graphic itself.';
COMMENT ON COLUMN swyn_tbl.target IS 'The URL to call for sharing a newsletter in a social network';
COMMENT ON COLUMN swyn_tbl.code IS 'The HTML code to be inserted in the newsletter using the agnSWYN tag';
COMMENT ON COLUMN swyn_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN swyn_tbl.timestamp IS 'entry last change';
COMMENT ON COLUMN swyn_tbl.ISIZE IS 'Defines the size of this icon with a symbolic name, which can be used as an optional parameter for [agnSWYN ...] as size="....". For the standard size, use the name "default" here';

CREATE TABLE tag_function_tbl
(
    tag_function_id SERIAL NOT NULL,
    company_id      INTEGER      NOT NULL,
    creation_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    timestamp       TIMESTAMP,
    name            VARCHAR(128) NOT NULL,
    lang            VARCHAR(32)  NOT NULL,
    description     VARCHAR(1000),
    code            TEXT
) TABLESPACE data_emmaux;
COMMENT ON TABLE tag_function_tbl IS 'This table contains the definitions for scripted tags.';
COMMENT ON COLUMN tag_function_tbl.tag_function_id IS 'unique ID, use tag_function_tbl_seq';
COMMENT ON COLUMN tag_function_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN tag_function_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN tag_function_tbl.timestamp IS 'entry last change';
COMMENT ON COLUMN tag_function_tbl.name IS 'Name of the function';
COMMENT ON COLUMN tag_function_tbl.lang IS 'Language the function is written in';
COMMENT ON COLUMN tag_function_tbl.description IS 'comment on entry';
COMMENT ON COLUMN tag_function_tbl.code IS 'The function execution code';

CREATE TABLE undo_component_tbl
(
    company_id      INTEGER,
    mailtemplate_id INTEGER,
    mailing_id      INTEGER,
    component_id    INTEGER,
    mtype           VARCHAR(100),
    required        INTEGER,
    comptype        INTEGER,
    comppresent     INTEGER,
    compname        VARCHAR(1000),
    emmblock        TEXT,
    binblock        BYTEA,
    target_id       INTEGER,
    timestamp       TIMESTAMP,
    url_id          INTEGER DEFAULT 0 NOT NULL,
    undo_id         SERIAL            NOT NULL
) TABLESPACE data_emm_undo;
CREATE INDEX undocomponent$mid$idx ON undo_component_tbl (mailing_id) TABLESPACE data_emm_undo;
COMMENT ON TABLE undo_component_tbl IS 'copies entries in component_tbl (plus undo - ID) to provide undo function, for column description see component_tbl';
COMMENT ON COLUMN undo_component_tbl.company_id IS 'Client reference';
COMMENT ON COLUMN undo_component_tbl.mailtemplate_id IS 'Same as cmponent_tbl.mailtemplate_id';
COMMENT ON COLUMN undo_component_tbl.mailing_id IS 'Reference of mailing';
COMMENT ON COLUMN undo_component_tbl.component_id IS 'Reference of component';
COMMENT ON COLUMN undo_component_tbl.mtype IS 'Same as cmponent_tbl.mtype';
COMMENT ON COLUMN undo_component_tbl.required IS 'Is component used in Mailing?';
COMMENT ON COLUMN undo_component_tbl.comptype IS 'Type of component';
COMMENT ON COLUMN undo_component_tbl.comppresent IS 'Same as cmponent_tbl.comppresent';
COMMENT ON COLUMN undo_component_tbl.compname IS 'Original component name';
COMMENT ON COLUMN undo_component_tbl.emmblock IS 'Original text data';
COMMENT ON COLUMN undo_component_tbl.binblock IS 'Original binary data';
COMMENT ON COLUMN undo_component_tbl.target_id IS 'Original targetgroup this component was configured for';
COMMENT ON COLUMN undo_component_tbl.timestamp IS 'Change date of component';
COMMENT ON COLUMN undo_component_tbl.url_id IS 'Referenced url for click on this component';
COMMENT ON COLUMN undo_component_tbl.undo_id IS 'Reference Key';

CREATE TABLE undo_dyn_content_tbl
(
    dyn_content_id INTEGER,
    dyn_name_id    INTEGER,
    target_id      INTEGER,
    dyn_order      INTEGER,
    dyn_content    TEXT,
    mailing_id     INTEGER,
    company_id     INTEGER,
    undo_id        INTEGER NOT NULL,
    change_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_emm_undo;
CREATE INDEX undodyncontent$mid$idx ON undo_dyn_content_tbl (mailing_id) TABLESPACE data_emm_undo;
COMMENT ON TABLE undo_dyn_content_tbl IS 'copies entries in dyn_content_tbl (plus undo - ID) to provide undo function, for column description see dyn_content_tbl';
COMMENT ON COLUMN undo_dyn_content_tbl.dyn_content_id IS 'Original content id';
COMMENT ON COLUMN undo_dyn_content_tbl.dyn_name_id IS 'Original content name';
COMMENT ON COLUMN undo_dyn_content_tbl.target_id IS 'Original targetgroup this content was configured for';
COMMENT ON COLUMN undo_dyn_content_tbl.dyn_order IS 'Original order of overlay contents to display';
COMMENT ON COLUMN undo_dyn_content_tbl.dyn_content IS 'Original content text';
COMMENT ON COLUMN undo_dyn_content_tbl.mailing_id IS 'Referenced mailing';
COMMENT ON COLUMN undo_dyn_content_tbl.company_id IS 'Referenced tennant';
COMMENT ON COLUMN undo_dyn_content_tbl.undo_id IS 'Reference key';
COMMENT ON COLUMN undo_dyn_content_tbl.change_date IS 'Change date of content';
CREATE INDEX undodync$uid$idx ON undo_dyn_content_tbl (undo_id);

CREATE TABLE undo_mailing_tbl
(
    mailing_id         INTEGER,
    undo_id            SERIAL,
    undo_creation_date TIMESTAMP NOT NULL,
    undo_admin_id      INTEGER   NOT NULL
) TABLESPACE data_emm_undo;
CREATE INDEX undomailing$mid$idx ON undo_mailing_tbl (mailing_id) TABLESPACE data_emm_undo;
COMMENT ON TABLE undo_mailing_tbl IS 'copies entries in mailing_tbl (plus undo - ID) to provide undo function, for column description see mailing_tbl';
COMMENT ON COLUMN undo_mailing_tbl.mailing_id IS 'mailing - ID (mailing_tbl)';
COMMENT ON COLUMN undo_mailing_tbl.undo_id IS 'unique ID, use undo_id_seq';
COMMENT ON COLUMN undo_mailing_tbl.undo_creation_date IS 'entry creation date';
COMMENT ON COLUMN undo_mailing_tbl.undo_admin_id IS 'admin who made changes to EMC mailing and caused undo entry creation - ID (admin_tbl)';
CREATE INDEX undomailing$uid$idx ON undo_mailing_tbl (undo_id);

CREATE TABLE cust1_ban_tbl
(
    email     VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason    VARCHAR(500)
) TABLESPACE data_cust_table;
ALTER TABLE cust1_ban_tbl ADD CONSTRAINT ban1$email$pk PRIMARY KEY (email);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$cust1ban_tbl FOREIGN KEY (text) REFERENCES cust1_ban_tbl (email);
COMMENT ON TABLE cust1_ban_tbl IS '[secret_data] stores tenant - blacklist';
COMMENT ON COLUMN cust1_ban_tbl.email IS '[secret_data] blacklisted address or (while using wildcards) domain / pattern';
COMMENT ON COLUMN cust1_ban_tbl.timestamp IS 'blacklisting timestamp';
COMMENT ON COLUMN cust1_ban_tbl.reason IS 'Reason text for this ban entry';

CREATE TABLE rdir_url_userform_tbl
(
    url_id        SERIAL PRIMARY KEY,
    full_url      VARCHAR(2000) NOT NULL,
    form_id       INTEGER       NOT NULL,
    company_id    INTEGER,
    usage         INTEGER,
    action_id     INTEGER,
    shortname     VARCHAR(1000),
    deep_tracking INTEGER   DEFAULT 0,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT rdiruserformurl$urlid$nn CHECK (url_id IS NOT NULL),
    CONSTRAINT rdiruserformurl$fullurl$nn CHECK (full_url IS NOT NULL),
    CONSTRAINT rdiruserformurl$form$nn CHECK (form_id IS NOT NULL)
) TABLESPACE data_emmaux;
CREATE INDEX rdir_userform_url$coid_mid$idx ON rdir_url_userform_tbl (company_id, form_id) TABLESPACE data_emmaux;
COMMENT ON TABLE rdir_url_userform_tbl IS 'stores form details';
COMMENT ON COLUMN rdir_url_userform_tbl.url_id IS 'PK, use RDIR_URL_USERFORM_TBL_SEQ';
COMMENT ON COLUMN rdir_url_userform_tbl.full_url IS 'complete URL used';
COMMENT ON COLUMN rdir_url_userform_tbl.form_id IS 'refernces Form (userform_tbl)';
COMMENT ON COLUMN rdir_url_userform_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN rdir_url_userform_tbl.usage IS 'Whether this link is used in the userform or kept vfor historic reasons';
COMMENT ON COLUMN rdir_url_userform_tbl.action_id IS 'references called action';
COMMENT ON COLUMN rdir_url_userform_tbl.shortname IS 'name of this entry';
COMMENT ON COLUMN rdir_url_userform_tbl.deep_tracking IS '1=enabled';
COMMENT ON COLUMN rdir_url_userform_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN rdir_url_userform_tbl.change_date IS 'entry last change';

CREATE TABLE del_predelivery_view_conf_tbl
(
    device    VARCHAR(100),
    screen    VARCHAR(100),
    container VARCHAR(100)
);
COMMENT ON TABLE del_predelivery_view_conf_tbl IS 'Configuration table of inbox previews layouts';
COMMENT ON COLUMN del_predelivery_view_conf_tbl.device IS 'Type of device';
COMMENT ON COLUMN del_predelivery_view_conf_tbl.screen IS 'Type of viewport';
COMMENT ON COLUMN del_predelivery_view_conf_tbl.container IS 'JavaScript file for rendering';

CREATE TABLE emm_layout_base_tbl
(
    layout_base_id       SERIAL,
    base_url             VARCHAR(200) DEFAULT 'assets/core',
    creation_date        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    change_date          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    company_id           INTEGER,
    shortname            VARCHAR(200),
    menu_position        INTEGER      DEFAULT 0,
    livepreview_position INTEGER      DEFAULT 0,
    layoutdirectory      VARCHAR(32),
    domain               VARCHAR(32),
    theme_type           SMALLINT     DEFAULT 0
);
COMMENT ON TABLE emm_layout_base_tbl IS 'used for whitelabel settings in EMM';
COMMENT ON COLUMN emm_layout_base_tbl.layout_base_id IS 'unique ID, use emm_layout_base_tbl_seq';
COMMENT ON COLUMN emm_layout_base_tbl.base_url IS 'URL where the layout definition is located';
COMMENT ON COLUMN emm_layout_base_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN emm_layout_base_tbl.change_date IS 'entry last change';
COMMENT ON COLUMN emm_layout_base_tbl.company_id IS 'tenant - ID (company_tbl), use 0 for all';
COMMENT ON COLUMN emm_layout_base_tbl.shortname IS 'layout name';
COMMENT ON COLUMN emm_layout_base_tbl.menu_position IS 'defines position of sidemenu (left or top), default is left';
COMMENT ON COLUMN emm_layout_base_tbl.livepreview_position IS 'defines the position of live preview (0 = right, 1= bottom, 2 = deactivated)';
COMMENT ON COLUMN emm_layout_base_tbl.layoutdirectory IS 'Local path to assets to be shown in this layout';
COMMENT ON COLUMN emm_layout_base_tbl.domain IS 'EMM-Domain to switch layouts for login site';
COMMENT ON COLUMN emm_layout_base_tbl.theme_type IS 'Theme types are like dark theme, standard theme. Dark theme - 1, standard - 0.';

CREATE TABLE mailloop_rule_tbl
(
    rid     INTEGER      NOT NULL,
    section VARCHAR(32)  NOT NULL,
    pattern VARCHAR(256) NOT NULL
);
COMMENT ON TABLE mailloop_rule_tbl IS 'Adding rules for the filter (mailloop) service to assign a pattern to a section for a specific filter definition';
COMMENT ON COLUMN mailloop_rule_tbl.rid IS 'This rule is added for the filter mailloop_tbl.rid';
COMMENT ON COLUMN mailloop_rule_tbl.section IS 'This is the target section (systemmail, filter, hard, soft)';
COMMENT ON COLUMN mailloop_rule_tbl.pattern IS 'The pattern as a regular expression to be checked against incoming mails';

CREATE TABLE login_whitelist_tbl
(
    ip_address  VARCHAR(50)  NOT NULL,
    description VARCHAR(200) NOT NULL
);
COMMENT ON TABLE login_whitelist_tbl IS '[secret_data] listed Adresses will never get Login locked';
COMMENT ON COLUMN login_whitelist_tbl.ip_address IS '[secret_data] IP to whitelist';
COMMENT ON COLUMN login_whitelist_tbl.description IS 'reason to whitelist';

CREATE TABLE access_data_tbl
(
    creation_date TIMESTAMP,
    user_agent    VARCHAR(2000),
    xuid          VARCHAR(500),
    ip            VARCHAR(50),
    referer       VARCHAR(2000),
    access_type   VARCHAR(40),
    mailing_id    INTEGER,
    customer_id   INTEGER,
    link_id       INTEGER,
    device_id     INTEGER
) TABLESPACE data_accounting;
COMMENT ON TABLE access_data_tbl IS 'limited Data Buffer for all clicks and openings, would be reworked (to lastopen / lastclick in recipient proilfe) and cleared up daily';
COMMENT ON COLUMN access_data_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN access_data_tbl.user_agent IS 'EMail-client identification string';
COMMENT ON COLUMN access_data_tbl.xuid IS 'xuid for opener / clicker';
COMMENT ON COLUMN access_data_tbl.ip IS 'IP of opener / clicker';
COMMENT ON COLUMN access_data_tbl.referer IS 'referer';
COMMENT ON COLUMN access_data_tbl.access_type IS 'ONEPIXEL = opening, REDIRECT = click';
COMMENT ON COLUMN access_data_tbl.mailing_id IS 'references Mailing (mailing_tbl)';
COMMENT ON COLUMN access_data_tbl.customer_id IS 'references recipient (customer_xxx_tbl)';
COMMENT ON COLUMN access_data_tbl.link_id IS 'references Link (click only) (rdir_url_tbl)';
COMMENT ON COLUMN access_data_tbl.device_id IS 'references device (device_tbl)';

CREATE TABLE doc_mapping_tbl
(
    filename VARCHAR(200),
    pagekey  VARCHAR(50)
);
ALTER TABLE doc_mapping_tbl ADD CONSTRAINT doc_mapping$pagekey$unique UNIQUE (pagekey);
COMMENT ON TABLE doc_mapping_tbl IS 'Maps pagekeys of JSPs to certain files in the online user manual for context sensitive online help .';
COMMENT ON COLUMN doc_mapping_tbl.filename IS 'filename';
COMMENT ON COLUMN doc_mapping_tbl.pagekey IS 'pagekey';

CREATE TABLE date_tbl
(
    type   INTEGER,
    format VARCHAR(100)
) TABLESPACE data_accounting;
COMMENT ON TABLE date_tbl IS 'information to expand agnDATE - Tag';
COMMENT ON COLUMN date_tbl.type IS 'unique ID - no sequence set';
COMMENT ON COLUMN date_tbl.format IS 'Date format (as java SimpleDateFormat)';

CREATE TABLE webservice_user_tbl
(
    username               VARCHAR(200) PRIMARY KEY,
    company_id             INTEGER              NOT NULL,
    default_data_source_id INTEGER              NOT NULL,
    req_rate_limit         DECIMAL(5, 2),
    bulk_size_limit        INTEGER DEFAULT 1000 NOT NULL,
    password_encrypted     VARCHAR(256),
    max_result_list_size   INTEGER DEFAULT 0    NOT NULL,
    active                 SMALLINT,
    contact_email          VARCHAR(400),
    api_call_limits        VARCHAR(100),
    last_login_date        TIMESTAMP NULL
);
ALTER TABLE webservice_user_tbl ADD CONSTRAINT websuser$cid$fk FOREIGN KEY (company_id) REFERENCES company_tbl (company_id);
ALTER TABLE webservice_user_tbl ADD CONSTRAINT websuser$dds$fk FOREIGN KEY (default_data_source_id) REFERENCES datasource_description_tbl (datasource_id);
COMMENT ON TABLE webservice_user_tbl IS 'stores webservice (2.0) - user';
COMMENT ON COLUMN webservice_user_tbl.username IS '(unique) username';
COMMENT ON COLUMN webservice_user_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN webservice_user_tbl.default_data_source_id IS 'created (datasource_ID) or updated (latest_datasource_ID) recipients are marked by this ID if no other datasource_ID is given';
COMMENT ON COLUMN webservice_user_tbl.req_rate_limit IS 'Limit of maximum webservice requests per second';
COMMENT ON COLUMN webservice_user_tbl.bulk_size_limit IS 'possibilty to set limit for bulk updates';
COMMENT ON COLUMN webservice_user_tbl.password_encrypted IS 'authentification password (encrypted)';
COMMENT ON COLUMN webservice_user_tbl.max_result_list_size IS 'possibilty to set limit for requested list length';
COMMENT ON COLUMN webservice_user_tbl.active IS '1 = yes';
COMMENT ON COLUMN webservice_user_tbl.contact_email IS 'Email contact';
COMMENT ON COLUMN webservice_user_tbl.api_call_limits IS 'API call limits (Format: <amount>"/"<duration>)';
COMMENT ON COLUMN webservice_user_tbl.last_login_date IS 'Timestamp of last login';

CREATE TABLE calendar_comment_tbl
(
    comment_id        SERIAL,
    company_id        INTEGER,
    admin_id          INTEGER,
    comment_content   VARCHAR(1000),
    comment_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deadline          SMALLINT  DEFAULT 0,
    planned_send_date TIMESTAMP
);
ALTER TABLE calendar_comment_tbl ADD CONSTRAINT calendar_comment$psd CHECK (planned_send_date IS NOT NULL);
COMMENT ON TABLE calendar_comment_tbl IS 'in EMM - calendar comments to a specified date could be set, this data are stored here';
COMMENT ON COLUMN calendar_comment_tbl.comment_id IS 'unique ID, use calendar_comment_tbl_seq';
COMMENT ON COLUMN calendar_comment_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN calendar_comment_tbl.admin_id IS 'EMM - User, references admin_tbl.admin_ID';
COMMENT ON COLUMN calendar_comment_tbl.comment_content IS 'note content';
COMMENT ON COLUMN calendar_comment_tbl.comment_date IS 'Date (in calendar for this entry)';
COMMENT ON COLUMN calendar_comment_tbl.deadline IS 'Does this date have a final deadline 1= comment should be sent - to...?';
COMMENT ON COLUMN calendar_comment_tbl.planned_send_date IS 'Remind user at this time';

CREATE TABLE customer_field_permission_tbl
(
    company_id  INTEGER     NOT NULL,
    column_name VARCHAR(32) NOT NULL,
    admin_id    INTEGER     NOT NULL,
    mode_edit   INTEGER     NOT NULL,
    PRIMARY KEY (company_id, column_name, admin_id)
);
CREATE UNIQUE INDEX custfieldperm$coid$colname ON customer_field_permission_tbl (company_id, UPPER(column_name), admin_id);
ALTER TABLE customer_field_permission_tbl ADD CONSTRAINT fk_customer_field_permission FOREIGN KEY (company_id, column_name) REFERENCES customer_field_tbl (company_id, col_name);
COMMENT ON TABLE customer_field_permission_tbl IS 'allows to add permissions for EMM-users to update (or not) per profile-field';
COMMENT ON COLUMN customer_field_permission_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN customer_field_permission_tbl.column_name IS '(EMM-) fieldname';
COMMENT ON COLUMN customer_field_permission_tbl.admin_id IS 'references EMM-User (admin_tbl)';
COMMENT ON COLUMN customer_field_permission_tbl.mode_edit IS '0 = no restrictions, 1 = read only, 2 = hidden';

CREATE TABLE mailing_info_tbl
(
    mailing_info_id   SERIAL,
    mailing_id        INTEGER,
    company_id        INTEGER,
    name              VARCHAR(200),
    value             VARCHAR(4000),
    description       VARCHAR(500),
    creation_date     TIMESTAMP,
    change_date       TIMESTAMP,
    creation_admin_id INTEGER,
    change_admin_id   INTEGER
);
CREATE INDEX mainfo$mid$idx ON mailing_info_tbl (mailing_id);
COMMENT ON TABLE mailing_info_tbl IS 'stores mailing-parameters';
COMMENT ON COLUMN mailing_info_tbl.mailing_info_id IS 'unique ID, use mailing_info_tbl_seq';
COMMENT ON COLUMN mailing_info_tbl.mailing_id IS 'references Mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN mailing_info_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN mailing_info_tbl.name IS 'parameter name';
COMMENT ON COLUMN mailing_info_tbl.value IS 'parameter value';
COMMENT ON COLUMN mailing_info_tbl.description IS 'comment on entry';
COMMENT ON COLUMN mailing_info_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN mailing_info_tbl.change_date IS 'entry last change';
COMMENT ON COLUMN mailing_info_tbl.creation_admin_id IS 'references creating EMM-User (admin_tbl)';
COMMENT ON COLUMN mailing_info_tbl.change_admin_id IS 'references changing EMM-User (admin_tbl)';

CREATE TABLE agn_dbversioninfo_tbl
(
    version_number   VARCHAR(15),
    updating_user    VARCHAR(64),
    update_timestamp DATE,
    CONSTRAINT PK_DBVERSION PRIMARY KEY (version_number)
);
COMMENT ON TABLE agn_dbversioninfo_tbl IS 'stores already executed (SQL-) updates to auto-check consistence of data structure';
COMMENT ON COLUMN agn_dbversioninfo_tbl.version_number IS '(EMM-) version of update - script';
COMMENT ON COLUMN agn_dbversioninfo_tbl.updating_user IS 'executing (DB-) user';
COMMENT ON COLUMN agn_dbversioninfo_tbl.update_timestamp IS 'execution timestamp';

CREATE TABLE rdir_url_param_tbl
(
    url_id      INTEGER,
    param_type  VARCHAR(32),
    param_key   VARCHAR(32),
    param_value VARCHAR(2000)
);
ALTER TABLE rdir_url_param_tbl ADD CONSTRAINT FK_RDIR_URL_PARAM_ID FOREIGN KEY (url_id) REFERENCES rdir_url_tbl (url_id);
CREATE INDEX urlparam$urlid$idx ON rdir_url_param_tbl (url_id) TABLESPACE data_cust_index;
COMMENT ON TABLE rdir_url_param_tbl IS 'stores data for link parameters. e.g. LinkExtensions';
COMMENT ON COLUMN rdir_url_param_tbl.url_id IS 'references link (rdir_url_tbl.url_id)';
COMMENT ON COLUMN rdir_url_param_tbl.param_type IS 'type,  e.g. LinkExtension';
COMMENT ON COLUMN rdir_url_param_tbl.param_key IS 'key added to the link';
COMMENT ON COLUMN rdir_url_param_tbl.param_value IS 'value for this key';

CREATE TABLE rdir_url_userform_param_tbl
(
    url_id      INTEGER,
    param_type  VARCHAR(32),
    param_key   VARCHAR(32),
    param_value VARCHAR(2000)
);
ALTER TABLE rdir_url_userform_param_tbl ADD CONSTRAINT FK_RDIR_URL_USERFORM_PARAM_ID FOREIGN KEY (url_id) REFERENCES rdir_url_userform_tbl (url_id);
COMMENT ON TABLE rdir_url_userform_param_tbl IS 'stores data for form parameters. e.g. LinkExtensions';
COMMENT ON COLUMN rdir_url_userform_param_tbl.url_id IS 'references link (rdir_url_tbl.url_id)';
COMMENT ON COLUMN rdir_url_userform_param_tbl.param_type IS 'type,  e.g. LinkExtension';
COMMENT ON COLUMN rdir_url_userform_param_tbl.param_key IS 'key added to the link';
COMMENT ON COLUMN rdir_url_userform_param_tbl.param_value IS 'value for this key';

CREATE TABLE benchmark_mailing_tbl
(
    company_id        INTEGER,
    mailing_id        INTEGER,
    mailinglist_id    INTEGER,
    shortname         VARCHAR(2000),
    subject           VARCHAR(2000),
    senddate          TIMESTAMP,
    sent_text         INTEGER,
    sent_html         INTEGER,
    sent_ohtml        INTEGER,
    mail_size         INTEGER,
    target_expression VARCHAR(2000),
    target_sql        VARCHAR(4000),
    creation_date     TIMESTAMP,
    target_sql2       VARCHAR(4000),
    target_sql3       VARCHAR(4000),
    sector            INTEGER,
    business_field    INTEGER,
    synch_kz          INTEGER
) TABLESPACE data_cust_table;
COMMENT ON TABLE benchmark_mailing_tbl IS 'collects data for benchmark-reports, filled by backend-prozess, AGN hosted instances only, also used as pre-summary-tbl for certain backend-reports';
COMMENT ON COLUMN benchmark_mailing_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN benchmark_mailing_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN benchmark_mailing_tbl.mailinglist_id IS 'mailinglist, on wich sending was based (mailinglist_tbl.mailinglist_id)';
COMMENT ON COLUMN benchmark_mailing_tbl.shortname IS 'mailing-shortname';
COMMENT ON COLUMN benchmark_mailing_tbl.subject IS 'mailing-subject';
COMMENT ON COLUMN benchmark_mailing_tbl.senddate IS 'mailing-sent date ';
COMMENT ON COLUMN benchmark_mailing_tbl.sent_text IS 'number of text-mailings sent';
COMMENT ON COLUMN benchmark_mailing_tbl.sent_html IS 'number of html-mailings sent';
COMMENT ON COLUMN benchmark_mailing_tbl.sent_ohtml IS 'number of offline-html-mailings sent';
COMMENT ON COLUMN benchmark_mailing_tbl.mail_size IS 'mailing-size';
COMMENT ON COLUMN benchmark_mailing_tbl.target_expression IS 'mailing-targetgroups';
COMMENT ON COLUMN benchmark_mailing_tbl.target_sql IS 'mailing-target - sql - part 1 (longer statements are splitted)';
COMMENT ON COLUMN benchmark_mailing_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN benchmark_mailing_tbl.target_sql2 IS 'mailing-target - sql - part 2 (longer statements are splitted)';
COMMENT ON COLUMN benchmark_mailing_tbl.target_sql3 IS 'mailing-target - sql - part 3 (longer statements are splitted)';
COMMENT ON COLUMN benchmark_mailing_tbl.sector IS 'encoded sector: 0-NONE, 1-AGENCIES, 2-COMMUNITIES, 3-TOURISM, 4-FINANCE, 5-IT, 6-RETAIL, 7-MANUFACTURING_INDUSTRY, 8-CONSUMER_GOODS, 9-PUBLISHER, 10-NON_PROFIT, 11-EDUCATION)';
COMMENT ON COLUMN benchmark_mailing_tbl.business_field IS 'encoded business field: 0-NONE, 1-B2B, 2-B2C';
COMMENT ON COLUMN benchmark_mailing_tbl.synch_kz IS 'for synch proces in backend: NULL = nothing done yet, -1=already exported, 1=imported';

CREATE TABLE messages_tbl
(
    message_key   VARCHAR(250) PRIMARY KEY,
    value_default VARCHAR(4000),
    value_de      VARCHAR(4000),
    value_es      VARCHAR(4000),
    value_fr      VARCHAR(4000),
    value_nl      VARCHAR(4000),
    value_pt      VARCHAR(4000),
    value_it      VARCHAR(4000),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted       SMALLINT  DEFAULT 0 NOT NULL
);
COMMENT ON TABLE messages_tbl IS 'stores any messages or identifiers used in EMM-GUI including translations for different languages';
COMMENT ON COLUMN messages_tbl.message_key IS 'key - value referenced in EMM';
COMMENT ON COLUMN messages_tbl.value_default IS 'default message / identifier - english-';
COMMENT ON COLUMN messages_tbl.value_de IS 'german translation';
COMMENT ON COLUMN messages_tbl.value_es IS 'spanish translation';
COMMENT ON COLUMN messages_tbl.value_fr IS 'french translation';
COMMENT ON COLUMN messages_tbl.value_nl IS 'dutch translation';
COMMENT ON COLUMN messages_tbl.value_pt IS 'Portuguese translation';
COMMENT ON COLUMN messages_tbl.value_it IS 'Italian translation';
COMMENT ON COLUMN messages_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN messages_tbl.change_date IS 'entry last change';
COMMENT ON COLUMN messages_tbl.deleted IS '1=yes';

CREATE TABLE job_queue_tbl
(
    id              SERIAL PRIMARY KEY,
    description     VARCHAR(64)  NOT NULL,
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lastStart       TIMESTAMP NULL,
    running         SMALLINT  DEFAULT 0,
    lastresult      VARCHAR(512) NULL,
    startAfterError SMALLINT  DEFAULT 0,
    lastDuration    INTEGER   DEFAULT 0,
    interval        VARCHAR(64)  NOT NULL,
    nextstart       TIMESTAMP NULL,
    hostname        VARCHAR(64) NULL,
    runClass        VARCHAR(128) NOT NULL,
    runOnlyOnHosts  VARCHAR(64) NULL,
    emailOnError    VARCHAR(512) NULL,
    deleted         SMALLINT  DEFAULT 0,
    job_comment     VARCHAR(500),
    criticality     INTEGER   DEFAULT 5,
    acknowledged    INTEGER   DEFAULT 0
);
ALTER TABLE job_queue_tbl ADD CONSTRAINT jobqueue$description$unique UNIQUE (description);
ALTER TABLE job_queue_tbl ADD CONSTRAINT jobqueue$deleted$nn CHECK (deleted IS NOT NULL);
COMMENT ON TABLE job_queue_tbl IS 'stores details and running details for automated jobs - should be monitored external!';
COMMENT ON COLUMN job_queue_tbl.id IS 'unique ID for references';
COMMENT ON COLUMN job_queue_tbl.description IS 'comment on entry';
COMMENT ON COLUMN job_queue_tbl.created IS 'entry creation date';
COMMENT ON COLUMN job_queue_tbl.lastStart IS 'timestamp when last run started';
COMMENT ON COLUMN job_queue_tbl.running IS '0 = no, 1 = yes';
COMMENT ON COLUMN job_queue_tbl.lastresult IS 'last result: OK or Error details';
COMMENT ON COLUMN job_queue_tbl.startAfterError IS '1=yes, try again, 0=no, wait to correct';
COMMENT ON COLUMN job_queue_tbl.lastDuration IS 'time for last run in sec';
COMMENT ON COLUMN job_queue_tbl.interval IS 'time (of the day) and / or day and / or intervall';
COMMENT ON COLUMN job_queue_tbl.nextStart IS 'next execution is sheduled for this time';
COMMENT ON COLUMN job_queue_tbl.hostname IS 'host to run this job on - wont be executed on other hosts with a running EMM - applikation';
COMMENT ON COLUMN job_queue_tbl.runClass IS 'java class to be executed';
COMMENT ON COLUMN job_queue_tbl.runOnlyOnHosts IS 'Optionally restricts execution of this job to a hostname list of servers like beta. If empty, then any server configured by config_tbl to execute jobs, will execute';
COMMENT ON COLUMN job_queue_tbl.emailOnError IS '[private_data] email - recipient for and error-reports';
COMMENT ON COLUMN job_queue_tbl.deleted IS '1 = yes';
COMMENT ON COLUMN job_queue_tbl.job_comment IS 'Comment for job worker';
COMMENT ON COLUMN job_queue_tbl.nextstart IS 'Next date to start this job';
COMMENT ON COLUMN job_queue_tbl.criticality IS '1=Handle error with priority low (may fail for few days), 2=Handle error with priority low, 3=Important error handling on next work day, 4=High priority error handling at day times, 5=Immediate error handling even at night';
COMMENT ON COLUMN job_queue_tbl.acknowledged IS 'Error was acknowledged by humans';

CREATE TABLE job_queue_parameter_tbl
(
    job_id          INTEGER      NOT NULL,
    parameter_name  VARCHAR(64)  NOT NULL,
    parameter_value VARCHAR(128) NOT NULL
);
ALTER TABLE job_queue_parameter_tbl ADD CONSTRAINT FK_JOB_QUE_PAR FOREIGN KEY (job_id) REFERENCES job_queue_tbl (id);
COMMENT ON TABLE job_queue_parameter_tbl IS 'stores additional details for job';
COMMENT ON COLUMN job_queue_parameter_tbl.job_id IS 'references job (job_queue_tbl.id)';
COMMENT ON COLUMN job_queue_parameter_tbl.parameter_name IS 'parameter name, e.g. companyID or tmpFolder';
COMMENT ON COLUMN job_queue_parameter_tbl.parameter_value IS 'matching parameter value';

CREATE TABLE rdirlog_val_num_dupl_tbl
(
    company_id    INTEGER,
    customer_id   INTEGER,
    ip_adr        VARCHAR(50),
    mailing_id    INTEGER,
    session_id    INTEGER,
    timestamp     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    num_parameter INTEGER,
    page_tag      VARCHAR(30)
);
COMMENT ON TABLE rdirlog_val_num_dupl_tbl IS 'to clean up dublicate entries in rdirlog_val_num_tbl if a values is measured multiple times';
COMMENT ON COLUMN rdirlog_val_num_dupl_tbl.company_id IS 'Referenced client';
COMMENT ON COLUMN rdirlog_val_num_dupl_tbl.customer_id IS 'Referenced customer';
COMMENT ON COLUMN rdirlog_val_num_dupl_tbl.ip_adr IS 'IP of customer when creating this data';
COMMENT ON COLUMN rdirlog_val_num_dupl_tbl.mailing_id IS 'Referenced mailing';
COMMENT ON COLUMN rdirlog_val_num_dupl_tbl.session_id IS 'Seesion id of customer when creation this data';
COMMENT ON COLUMN rdirlog_val_num_dupl_tbl.timestamp IS 'Creation date for this data entry';
COMMENT ON COLUMN rdirlog_val_num_dupl_tbl.num_parameter IS 'Numeric value to be measured';
COMMENT ON COLUMN rdirlog_val_num_dupl_tbl.page_tag IS 'Text describing this measure point';

CREATE TABLE bounce_translate_tbl
(
    company_id    INTEGER             NOT NULL,
    dsn           INTEGER             NOT NULL,
    detail        INTEGER             NOT NULL,
    pattern       VARCHAR(150),
    rule_id       SERIAL PRIMARY KEY,
    active        SMALLINT  DEFAULT 1 NOT NULL,
    shortname     VARCHAR(100),
    description   VARCHAR(500),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_bounce;
COMMENT ON TABLE bounce_translate_tbl IS 'translates Delivery Status Notification (DSN) codes to qualified bounce codes';
COMMENT ON COLUMN bounce_translate_tbl.company_id IS '0=global setting, >0 only used for this company_tbl.company_id';
COMMENT ON COLUMN bounce_translate_tbl.dsn IS 'the Delivery Status Notification as determinated from mail delivery';
COMMENT ON COLUMN bounce_translate_tbl.detail IS 'qualified bounce code (200, 400, 410, 420, 510, 511, 512)';
COMMENT ON COLUMN bounce_translate_tbl.pattern IS '(optional) key=value pair, separated by comma, with keyword (e.g. relay, stat) and regular expression to restrict match to these pattern';
COMMENT ON COLUMN bounce_translate_tbl.rule_id IS 'unique ID >0 to reference this rule';
COMMENT ON COLUMN bounce_translate_tbl.active IS 'if this is 1, this rule is active, otherwise inactive';
COMMENT ON COLUMN bounce_translate_tbl.shortname IS 'name of this rule';
COMMENT ON COLUMN bounce_translate_tbl.description IS 'detailed description including issue number (if available)';
COMMENT ON COLUMN bounce_translate_tbl.creation_date IS 'time of creation';
COMMENT ON COLUMN bounce_translate_tbl.change_date IS 'time of last change';

CREATE TABLE bounce_collect_tbl
(
    customer_id INTEGER,
    mailing_id  INTEGER,
    company_id  INTEGER,
    timestamp   TIMESTAMP
) TABLESPACE data_bounce;
CREATE INDEX bnccoll$cust$idx ON bounce_collect_tbl (customer_id) TABLESPACE data_bounce;
CREATE INDEX bnccoll$coid$idx ON bounce_collect_tbl (company_id) TABLESPACE data_bounce;
CREATE INDEX bnccoll$cust_coid$idx ON bounce_collect_tbl (customer_id, company_id) TABLESPACE data_bounce;
COMMENT ON TABLE bounce_collect_tbl IS 'internal used temp. table during softbounce processing';
COMMENT ON COLUMN bounce_collect_tbl.customer_id IS 'customer_xx_tbl.customer_id for whom this bounce has been registrated';
COMMENT ON COLUMN bounce_collect_tbl.mailing_id IS 'mailing_tbl.mailing_id for which mail the bounce had been created';
COMMENT ON COLUMN bounce_collect_tbl.company_id IS 'company_tbl.company_id owner of the mailing';
COMMENT ON COLUMN bounce_collect_tbl.timestamp IS 'timestamp of processing';

CREATE TABLE onepixellog_device_1_tbl
(
    company_id      INTEGER NOT NULL,
    mailing_id      INTEGER NOT NULL,
    customer_id     INTEGER NOT NULL,
    device_class_id INTEGER,
    device_id       INTEGER,
    creation        TIMESTAMP,
    client_id       INTEGER
) TABLESPACE data_warehouse;
CREATE INDEX onepixdev1$mlid_cuid$idx ON onepixellog_device_1_tbl (mailing_id, customer_id) TABLESPACE index_data_warehouse;
CREATE INDEX onedev1$ciddevclidmlid$idx ON onepixellog_device_1_tbl (customer_id, device_class_id, mailing_id) TABLESPACE index_data_warehouse;
CREATE INDEX onedev1$creat$idx ON onepixellog_device_1_tbl (creation) TABLESPACE index_data_warehouse;
COMMENT ON TABLE onepixellog_device_1_tbl IS 'one entry per registered opening, cleaned up regulary (default: 1000d)';
COMMENT ON COLUMN onepixellog_device_1_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN onepixellog_device_1_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN onepixellog_device_1_tbl.customer_id IS 'references recipient (customer_xxx_tbl.customer_id)';
COMMENT ON COLUMN onepixellog_device_1_tbl.device_class_id IS 'specifies matched device-class: 1=DESKTOP, 2=MOBILE, 3=TABLET, 4=SMARTTV, references deviceclass_tbl.id';
COMMENT ON COLUMN onepixellog_device_1_tbl.device_id IS 'specifies matched device (references device_tbl.device_id)';
COMMENT ON COLUMN onepixellog_device_1_tbl.creation IS 'opening timesstamp = entry creation date';
COMMENT ON COLUMN onepixellog_device_1_tbl.client_id IS 'Client id of client_tbl for the software which was used to open a mailing';

CREATE TABLE workflow_tbl
(
    workflow_id            SERIAL PRIMARY KEY,
    company_id             INTEGER             NOT NULL,
    shortname              VARCHAR(100)        NOT NULL,
    description            VARCHAR(1000),
    status                 SMALLINT  DEFAULT 1 NOT NULL,
    general_start_date     TIMESTAMP,
    general_end_date       TIMESTAMP,
    general_start_reaction INTEGER   DEFAULT 0,
    general_start_event    INTEGER   DEFAULT 0,
    created                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_type               SMALLINT  DEFAULT 0,
    workflow_schema        TEXT,
    pause_undo_id          INTEGER   DEFAULT NULL,
    actual_end_date        TIMESTAMP NULL
) TABLESPACE data_workflow;
ALTER TABLE workflow_tbl ADD CONSTRAINT workflow1$status$ck CHECK (status IN (1, 2, 3, 4, 5, 6, 7, 8, 9));
COMMENT ON TABLE workflow_tbl IS 'stores workflow entities and some metadata';
COMMENT ON COLUMN workflow_tbl.workflow_id IS 'unique ID, use workflow_tbl_seq';
COMMENT ON COLUMN workflow_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN workflow_tbl.shortname IS 'shortname of a workflow';
COMMENT ON COLUMN workflow_tbl.description IS 'description of a workflow';
COMMENT ON COLUMN workflow_tbl.status IS 'current state of a workflow, 1 = open, 2 = active, 3 = deactivated, 4 = complete, 5 = testing, 6 = tested';
COMMENT ON COLUMN workflow_tbl.general_start_date IS 'the earliest date configured by start icon(s)';
COMMENT ON COLUMN workflow_tbl.general_end_date IS 'the latest date configured by stop icon(s), set if a workflow is configured to end at specific date';
COMMENT ON COLUMN workflow_tbl.general_start_reaction IS 'reaction (if any) configured by the earliest start icon (or null)';
COMMENT ON COLUMN workflow_tbl.general_start_event IS 'event (if any) configured by the earliest start icon (or null)';
COMMENT ON COLUMN workflow_tbl.created IS 'entry creation date';
COMMENT ON COLUMN workflow_tbl.end_type IS 'type of condition to end active workflow, 1 = automatic, 2 = at specific date';
COMMENT ON COLUMN workflow_tbl.workflow_schema IS 'JSON representation of workflow icons, their properties and connections between them';
COMMENT ON COLUMN workflow_tbl.pause_undo_id IS 'id of the undo_workflow_tbl(undo_id) entry that used to store data while pausing';
COMMENT ON COLUMN workflow_tbl.actual_end_date IS 'Actual end date. Completion date or manual deactivation date';

CREATE TABLE userlog_tbl
(
    logtime         TIMESTAMP NOT NULL,
    username        VARCHAR(200),
    action          VARCHAR(1000),
    description     VARCHAR(4000),
    supervisor_name VARCHAR(100),
    company_id      INTEGER DEFAULT 0
);
CREATE INDEX userlog$logtime$idx ON userlog_tbl (logtime);
CREATE INDEX userlog$username$idx ON userlog_tbl (username);
COMMENT ON TABLE userlog_tbl IS 'stores log of actions an EMM - user had been done via EMM GUI, auto- cleaned up after 180d';
COMMENT ON COLUMN userlog_tbl.logtime IS 'action - time';
COMMENT ON COLUMN userlog_tbl.username IS 'EMM - User';
COMMENT ON COLUMN userlog_tbl.action IS 'name of this action';
COMMENT ON COLUMN userlog_tbl.description IS 'additional information';
COMMENT ON COLUMN userlog_tbl.supervisor_name IS 'if this was a supervisor access it is stored here';

CREATE TABLE birtreport_tbl
(
    report_id         SERIAL PRIMARY KEY,
    company_id        INTEGER,
    shortname         VARCHAR(300),
    description       VARCHAR(1000),
    active            INTEGER   DEFAULT 0,
    report_type       INTEGER   DEFAULT 0,
    format            INTEGER   DEFAULT 0,
    email_subject     VARCHAR(200),
    email_description VARCHAR(1000),
    creation_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activation_date   TIMESTAMP,
    end_date          TIMESTAMP,
    active_tab        SMALLINT  DEFAULT 1 NOT NULL,
    language          VARCHAR(10),
    hidden            SMALLINT  DEFAULT 0 NOT NULL,
    change_date       TIMESTAMP,
    intervalpattern   VARCHAR(100),
    lasthostname      VARCHAR(100),
    laststart         TIMESTAMP NULL,
    nextstart         TIMESTAMP NULL,
    running           INTEGER   DEFAULT 0,
    lastresult        VARCHAR(512),
    deleted           SMALLINT  DEFAULT 0 NOT NULL
);
COMMENT ON TABLE birtreport_tbl IS 'Definition of preconfigured reports to be created by birt and sent via email';
COMMENT ON COLUMN birtreport_tbl.report_id IS 'Reference key';
COMMENT ON COLUMN birtreport_tbl.company_id IS 'Refernced client';
COMMENT ON COLUMN birtreport_tbl.shortname IS 'Shortname for this report';
COMMENT ON COLUMN birtreport_tbl.description IS 'Description for this report';
COMMENT ON COLUMN birtreport_tbl.active IS 'Is this report activated';
COMMENT ON COLUMN birtreport_tbl.report_type IS '0 = DAILY, 1 = WEEKLY, 2 = BIWEEKLY, 3 = MONTHLY_FIRST, 4 = MONTHLY_15TH, 5 = MONTHLY_LAST, 6 = AFTER_MAILING_24HOURS, 7 = AFTER_MAILING_48HOURS, 8 = AFTER_MAILING_WEEK';
COMMENT ON COLUMN birtreport_tbl.format IS 'Data format (0 = pdf, 1 = csv)';
COMMENT ON COLUMN birtreport_tbl.email_subject IS 'Email subject';
COMMENT ON COLUMN birtreport_tbl.email_description IS 'Email text';
COMMENT ON COLUMN birtreport_tbl.creation_date IS 'Creation date of this report definition';
COMMENT ON COLUMN birtreport_tbl.activation_date IS 'Date this report was activated';
COMMENT ON COLUMN birtreport_tbl.end_date IS 'Last date this report should be delivered';
COMMENT ON COLUMN birtreport_tbl.active_tab IS 'default tab to be shown in GUI';
COMMENT ON COLUMN birtreport_tbl.language IS 'Language to create report data in';
COMMENT ON COLUMN birtreport_tbl.hidden IS 'Do not show this report in EMM-GUI';
COMMENT ON COLUMN birtreport_tbl.change_date IS 'Change date of this report definition';
COMMENT ON COLUMN birtreport_tbl.intervalpattern IS 'Pattern for repeated delivery';
COMMENT ON COLUMN birtreport_tbl.lasthostname IS 'Last host which executed this report';
COMMENT ON COLUMN birtreport_tbl.laststart IS 'Last creation start of this report';
COMMENT ON COLUMN birtreport_tbl.nextstart IS 'Next creation start of this report';
COMMENT ON COLUMN birtreport_tbl.running IS 'Currently running report (0 = no, 1 = yes)';
COMMENT ON COLUMN birtreport_tbl.lastresult IS 'Last status result text of this report';
COMMENT ON COLUMN birtreport_tbl.deleted IS 'deleted? 0 = no, 1 = yes';

CREATE TABLE birtreport_parameter_tbl
(
    report_id       INTEGER       NOT NULL,
    parameter_name  VARCHAR(64)   NOT NULL,
    parameter_value VARCHAR(4000) NOT NULL,
    report_type     INTEGER       NOT NULL
);
ALTER TABLE birtreport_parameter_tbl ADD CONSTRAINT FK_BIRTREP_PAR FOREIGN KEY (report_id) REFERENCES birtreport_tbl (report_id);
CREATE INDEX birtrptprm$rid$idx ON birtreport_parameter_tbl (report_id);
COMMENT ON TABLE birtreport_parameter_tbl IS 'Stores additional configuration for birt reports of birtreport_tbl. Watch out: the value kind of predefineMailing (mailinglist or mailing) depends on controlling value of mailingFilter';
COMMENT ON COLUMN birtreport_parameter_tbl.report_id IS 'Referenced birtreport';
COMMENT ON COLUMN birtreport_parameter_tbl.parameter_name IS 'Parameter name';
COMMENT ON COLUMN birtreport_parameter_tbl.parameter_value IS 'Parameter value';
COMMENT ON COLUMN birtreport_parameter_tbl.report_type IS '1 = COMPARISON, 2 = MAILING, 3 = RECIPIENT, 4 = TOP_DOMAIN';

CREATE TABLE mailing_account_sum_tbl
(
    company_id     INTEGER,
    mailing_id     INTEGER,
    status_field   VARCHAR(100),
    no_of_mailings INTEGER,
    no_of_bytes    INTEGER,
    mintime        TIMESTAMP,
    maxtime        TIMESTAMP,
    skip           INTEGER default 0,
    chunks         INTEGER default 0
);
CREATE INDEX maccsum$coid$idx ON mailing_account_sum_tbl (company_id);
CREATE UNIQUE INDEX maccsum$mid_sf$uq ON mailing_account_sum_tbl (mailing_id, status_field);
COMMENT ON TABLE mailing_account_sum_tbl IS 'summary of mailing sending informations, filled by trigger on mailing_account_tbl';
COMMENT ON COLUMN mailing_account_sum_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN mailing_account_sum_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN mailing_account_sum_tbl.status_field IS 'A=Admin, T=Test, W= Wolrd, E=Event, R=rule-based, V=predelivery-test, D=onDemand (C, X - deprecated)';
COMMENT ON COLUMN mailing_account_sum_tbl.no_of_mailings IS 'sum of sended mailings (yet)';
COMMENT ON COLUMN mailing_account_sum_tbl.no_of_bytes IS 'sum of sended bytes (yet)';
COMMENT ON COLUMN mailing_account_sum_tbl.mintime IS 'min sending timestamp';
COMMENT ON COLUMN mailing_account_sum_tbl.maxtime IS 'last sending timestamp';
COMMENT ON COLUMN mailing_account_sum_tbl.SKIP IS 'number of messages not generate due to skipping when empty content is detected';
COMMENT ON COLUMN mailing_account_sum_tbl.CHUNKS IS 'if one message is sent out in several chunks, this represents the number of physical sent out chunks (e.g. for SMS)';

CREATE TABLE birtreport_sent_mailings_tbl
(
    report_id     INTEGER,
    mailing_id    INTEGER,
    company_id    INTEGER,
    delivery_date TIMESTAMP
);
CREATE INDEX birtrptsent$rid$idx ON birtreport_sent_mailings_tbl (report_id);
COMMENT ON TABLE birtreport_sent_mailings_tbl IS 'Delivery statistics of birt reports';
COMMENT ON COLUMN birtreport_sent_mailings_tbl.report_id IS 'Referenced birtreport_tbl';
COMMENT ON COLUMN birtreport_sent_mailings_tbl.mailing_id IS 'Report for mailing';
COMMENT ON COLUMN birtreport_sent_mailings_tbl.company_id IS 'Refernced client';
COMMENT ON COLUMN birtreport_sent_mailings_tbl.delivery_date IS 'Date of delivery per email';

CREATE TABLE actop_tbl
(
    action_operation_id SERIAL PRIMARY KEY,
    company_id          INTEGER      NOT NULL,
    type                VARCHAR(255) NOT NULL,
    action_id           INTEGER      NOT NULL
);
CREATE INDEX actionoperation$actid$idx ON actop_tbl (action_id);
ALTER TABLE actop_tbl ADD CONSTRAINT actionoperation$actid$fk FOREIGN KEY (action_id) REFERENCES rdir_action_tbl (action_id);
COMMENT ON TABLE actop_tbl IS 'splits EMM - action into operation steps (order defined by action_operation_id - order)';
COMMENT ON COLUMN actop_tbl.action_operation_id IS 'unique ID, use actop_tbl_seq';
COMMENT ON COLUMN actop_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN actop_tbl.type IS 'defines type (and sub - table containg detail-data) e.g. GetCustomer => actop_get_customer_tbl';
COMMENT ON COLUMN actop_tbl.action_id IS 'references action metaData (rdir_action_tbl.action_id)';

CREATE TABLE actop_execute_script_tbl
(
    action_operation_id INTEGER PRIMARY KEY,
    script              TEXT NULL
);
ALTER TABLE actop_execute_script_tbl ADD CONSTRAINT actopes$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id);
COMMENT ON TABLE actop_execute_script_tbl IS 'detail info about action-step to execute scripts';
COMMENT ON COLUMN actop_execute_script_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_execute_script_tbl.script IS 'velocity - code';

CREATE TABLE actop_update_customer_tbl
(
    action_operation_id INTEGER PRIMARY KEY,
    column_name         VARCHAR(255) NOT NULL,
    update_type         INTEGER      NOT NULL,
    update_value        VARCHAR(255) NULL,
    trackpoint_id       INTEGER
);
ALTER TABLE actop_update_customer_tbl ADD CONSTRAINT actopuc$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id);
COMMENT ON TABLE actop_update_customer_tbl IS 'allows updates on customer_xxx_tbl by action - step';
COMMENT ON COLUMN actop_update_customer_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_update_customer_tbl.column_name IS 'column name in customer_xxx_tbl to be updated';
COMMENT ON COLUMN actop_update_customer_tbl.update_type IS '1 -> Increment, 2 -> Decrement, 3 -> Set';
COMMENT ON COLUMN actop_update_customer_tbl.update_value IS 'eg. 1 for increment by 1 or sysdate for set = sysdate';
COMMENT ON COLUMN actop_update_customer_tbl.trackpoint_id IS 'if != 0: used for profile-field modification';

CREATE TABLE actop_get_customer_tbl
(
    action_operation_id INTEGER  PRIMARY KEY,
    load_always         SMALLINT NOT NULL
);
ALTER TABLE actop_get_customer_tbl ADD CONSTRAINT actopgc$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id);
COMMENT ON TABLE actop_get_customer_tbl IS 'action-step for loading recipient';
COMMENT ON COLUMN actop_get_customer_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_get_customer_tbl.load_always IS '1= inactive recipients are loaded, too, 0 = loading active recipients only';

CREATE TABLE actop_subscribe_customer_tbl
(
    action_operation_id INTEGER      PRIMARY KEY,
    double_check        SMALLINT     NOT NULL,
    key_column          VARCHAR(255) NOT NULL,
    double_opt_in       SMALLINT     NOT NULL
);
ALTER TABLE actop_subscribe_customer_tbl ADD CONSTRAINT actopsc$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id);
COMMENT ON TABLE actop_subscribe_customer_tbl IS 'details for subscribe action steps';
COMMENT ON COLUMN actop_subscribe_customer_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_subscribe_customer_tbl.double_check IS '1=checks, if recipient already exists, 0=no check';
COMMENT ON COLUMN actop_subscribe_customer_tbl.key_column IS 'column double_check, e.g. email';
COMMENT ON COLUMN actop_subscribe_customer_tbl.double_opt_in IS '1 = state is set to DOI - waiting for confirm (5)';

CREATE TABLE actop_send_mailing_tbl
(
    action_operation_id   INTEGER            PRIMARY KEY,
    mailing_id            INTEGER            NOT NULL,
    delay_minutes         INTEGER            NOT NULL,
    use_as_doi            SMALLINT DEFAULT 0 NOT NULL,
    bcc                   VARCHAR(4000),
    for_active_recipients SMALLINT DEFAULT 1
);
ALTER TABLE actop_send_mailing_tbl ADD CONSTRAINT actopsm$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id);
COMMENT ON TABLE actop_send_mailing_tbl IS 'details for action step sending action based mailing';
COMMENT ON COLUMN actop_send_mailing_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_send_mailing_tbl.mailing_id IS 'references mailing (mailing_tbl.mailing_id)';
COMMENT ON COLUMN actop_send_mailing_tbl.delay_minutes IS 'delay between action is triggered and mail is send';
COMMENT ON COLUMN actop_send_mailing_tbl.use_as_doi IS '1=this is a DOI mailing, send to recipients having user_status = 5';
COMMENT ON COLUMN actop_send_mailing_tbl.bcc IS 'list of bcc email address';
COMMENT ON COLUMN actop_send_mailing_tbl.for_active_recipients IS 'Represents user statuses for sending: 0 - Wait for confirm, 1 - Wait for confirm and Active, 2 - Active';

CREATE TABLE actop_service_mail_tbl
(
    action_operation_id INTEGER PRIMARY KEY,
    text_mail           TEXT NULL,
    subject_line        VARCHAR(255) NULL,
    to_addr             VARCHAR(255) NULL,
    mailtype            INTEGER NOT NULL,
    html_mail           TEXT NULL,
    from_address        VARCHAR(100),
    reply_address       VARCHAR(100)
);
ALTER TABLE actop_service_mail_tbl ADD CONSTRAINT actopsm1$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id);
COMMENT ON TABLE actop_service_mail_tbl IS '[secret_data] (?) action-step to send a service mail to specified recipient(s) (actually customer-specific feature)';
COMMENT ON COLUMN actop_service_mail_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_service_mail_tbl.text_mail IS 'email content -text format';
COMMENT ON COLUMN actop_service_mail_tbl.subject_line IS 'email subject';
COMMENT ON COLUMN actop_service_mail_tbl.to_addr IS '[private_data]recipient';
COMMENT ON COLUMN actop_service_mail_tbl.mailtype IS '1= html and text, 0 = text ';
COMMENT ON COLUMN actop_service_mail_tbl.html_mail IS 'email content -html format';
COMMENT ON COLUMN actop_service_mail_tbl.from_address IS 'Senders email address';
COMMENT ON COLUMN actop_service_mail_tbl.reply_address IS 'Emailaddress to reply to';

CREATE TABLE actop_get_archive_list_tbl
(
    action_operation_id INTEGER PRIMARY KEY,
    campaign_id         INTEGER NOT NULL,
    limit_type          SMALLINT,
    limit_value         INTEGER
);
ALTER TABLE actop_get_archive_list_tbl ADD CONSTRAINT actopgal$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id);
COMMENT ON TABLE actop_get_archive_list_tbl IS 'action-step to list mailings (archived only) e.g. for links "show my prev. mailings"';
COMMENT ON COLUMN actop_get_archive_list_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_get_archive_list_tbl.campaign_id IS 'references campaign_tbl (ARCHIVED Mailings, not WM)';
COMMENT ON COLUMN actop_get_archive_list_tbl.limit_type IS 'Defines type of limit for action. 0 - Days, 1 - Mailings';
COMMENT ON COLUMN actop_get_archive_list_tbl.limit_value IS 'Stores value of limit';

CREATE TABLE actop_get_archive_mailing_tbl
(
    action_operation_id INTEGER PRIMARY KEY,
    expire_day          INTEGER NOT NULL,
    expire_month        INTEGER NOT NULL,
    expire_year         INTEGER NOT NULL
);
ALTER TABLE actop_get_archive_mailing_tbl ADD CONSTRAINT actopgam$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id);
COMMENT ON TABLE actop_get_archive_mailing_tbl IS 'Action-step to load archive mailing';
COMMENT ON COLUMN actop_get_archive_mailing_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_get_archive_mailing_tbl.expire_day IS 'expire - timestamp DD';
COMMENT ON COLUMN actop_get_archive_mailing_tbl.expire_month IS 'expire - timestamp MM';
COMMENT ON COLUMN actop_get_archive_mailing_tbl.expire_year IS 'expire - timestamp YYYY';

CREATE TABLE actop_content_view_tbl
(
    action_operation_id INTEGER PRIMARY KEY,
    tag_name            VARCHAR(255) NULL
);
ALTER TABLE actop_content_view_tbl ADD CONSTRAINT actopcv$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id);
COMMENT ON TABLE actop_content_view_tbl IS 'action-step to load special content';
COMMENT ON COLUMN actop_content_view_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_content_view_tbl.tag_name IS 'content-block name to be shown';

CREATE TABLE actop_identify_customer_tbl
(
    action_operation_id INTEGER      PRIMARY KEY,
    key_column          VARCHAR(255) NOT NULL,
    pass_column         VARCHAR(255) NOT NULL
);
ALTER TABLE actop_identify_customer_tbl ADD CONSTRAINT actopic$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id);
COMMENT ON TABLE actop_identify_customer_tbl IS 'detail for action steps to find a recipient in customer_xxx_tbl';
COMMENT ON COLUMN actop_identify_customer_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_identify_customer_tbl.key_column IS 'matching column (in customer_xxx_tbl) e.g. email';
COMMENT ON COLUMN actop_identify_customer_tbl.pass_column IS 'allows to protect loading customer_data by password, password has to be stored in this customer_xxx_tbl - column';

CREATE TABLE workflow_reaction_tbl
(
    reaction_id        SERIAL PRIMARY KEY,
    company_id         INTEGER,
    workflow_id        INTEGER,
    trigger_mailing_id INTEGER,
    active             SMALLINT    DEFAULT 0,
    once               SMALLINT    DEFAULT 0,
    start_date         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    reaction_type      INTEGER,
    profile_column     VARCHAR(100),
    rules_sql          VARCHAR(2000),
    trigger_link_id    INTEGER     DEFAULT 0,
    admin_timezone     VARCHAR(50) DEFAULT 'Europe/Berlin' NOT NULL,
    mailinglist_id     INTEGER     DEFAULT 0               NOT NULL
) TABLESPACE data_workflow;
COMMENT ON TABLE workflow_reaction_tbl IS 'stores customer reactions to be watched (core mechanism of action-based workflow)';
COMMENT ON COLUMN workflow_reaction_tbl.reaction_id IS 'unique ID, use workflow_reaction_tbl_seq';
COMMENT ON COLUMN workflow_reaction_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN workflow_reaction_tbl.workflow_id IS 'workflow - ID (workflow_tbl)';
COMMENT ON COLUMN workflow_reaction_tbl.trigger_mailing_id IS 'mailing to watch user reaction - ID (mailing_tbl)';
COMMENT ON COLUMN workflow_reaction_tbl.active IS 'whether or not reaction is being watched, 1 = yes, 0 = no';
COMMENT ON COLUMN workflow_reaction_tbl.once IS 'whether or not a second+ event triggered for the same recipient must be ignored, 1 = ignored, 0 = handled';
COMMENT ON COLUMN workflow_reaction_tbl.start_date IS 'a date when watching starts, all the earlier changes are ignored';
COMMENT ON COLUMN workflow_reaction_tbl.reaction_type IS 'type of reactions to be watched, OPENED(1), CLICKED(3), CHANGE_OF_PROFILE(8), WAITING_FOR_CONFIRM(9), OPT_IN(10), CLICKED_LINK(12)';
COMMENT ON COLUMN workflow_reaction_tbl.profile_column IS 'recipient profile field to be watched; ignored unless reaction_type = 8';
COMMENT ON COLUMN workflow_reaction_tbl.rules_sql IS 'an SQL representation of user-defined condition that causes a reaction to be triggered; ignored unless reaction_type = 8';
COMMENT ON COLUMN workflow_reaction_tbl.trigger_link_id IS 'link in mailing - ID (rdir_url_tbl)';
COMMENT ON COLUMN workflow_reaction_tbl.admin_timezone IS 'timezone to be used for deadline calculation (deadline_hours and deadline_minutes columns of workflow_reaction_decl_tbl)';
COMMENT ON COLUMN workflow_reaction_tbl.mailinglist_id IS 'mailinglist that the reaction watching should be limited to - ID (mailinglist_tbl)';

CREATE TABLE hostauth_pending_tbl
(
    admin_id      INTEGER                             NOT NULL,
    host_id       VARCHAR(80)                         NOT NULL,
    security_code VARCHAR(10)                         NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (admin_id, host_id)
);
COMMENT ON TABLE hostauth_pending_tbl IS 'contains pending authentification info used for 2-factor-auth.';
COMMENT ON COLUMN hostauth_pending_tbl.admin_id IS 'references  EMM-User (admin_tbl)';
COMMENT ON COLUMN hostauth_pending_tbl.host_id IS 'defines requesting host';
COMMENT ON COLUMN hostauth_pending_tbl.security_code IS 'security-code for authentification as sent to user';
COMMENT ON COLUMN hostauth_pending_tbl.creation_date IS 'entry creation date';

CREATE TABLE authenticated_hosts_tbl
(
    admin_id      INTEGER                             NOT NULL,
    host_id       VARCHAR(80)                         NOT NULL,
    expire_date   TIMESTAMP                           NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (admin_id, host_id)
);
COMMENT ON TABLE authenticated_hosts_tbl IS 'contains successful authentification info used for 2-factor-auth.';
COMMENT ON COLUMN authenticated_hosts_tbl.admin_id IS 'references  EMM-User (admin_tbl)';
COMMENT ON COLUMN authenticated_hosts_tbl.host_id IS 'defines requesting host';
COMMENT ON COLUMN authenticated_hosts_tbl.expire_date IS 'entry expire date';
COMMENT ON COLUMN authenticated_hosts_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN authenticated_hosts_tbl.change_date IS 'entry last change';

CREATE TABLE user_agent_tbl
(
    user_agent_id SERIAL PRIMARY KEY,
    user_agent    VARCHAR(2200) NOT NULL,
    req_counter   INTEGER       NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX useragent$useragent$uq ON user_agent_tbl (user_agent);
COMMENT ON TABLE user_agent_tbl IS 'stores all client - information about clients used to open / click a mailing, evaluated regulary to keep RegEx for devices and device - classes up to date';
COMMENT ON COLUMN user_agent_tbl.user_agent_id IS 'unique ID, use user_agent_tbl_seq';
COMMENT ON COLUMN user_agent_tbl.user_agent IS 'user-agent-string as given by client';
COMMENT ON COLUMN user_agent_tbl.req_counter IS 'counts all registrations of this string';
COMMENT ON COLUMN user_agent_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN user_agent_tbl.change_date IS 'entry last change (esp. last count)';

CREATE TABLE admin_pref_tbl
(
    admin_id INTEGER    DEFAULT 0   NOT NULL,
    pref     VARCHAR(40)            NOT NULL,
    val      VARCHAR(5) DEFAULT '0' NOT NULL
);
COMMENT ON TABLE admin_pref_tbl IS 'allows to store settings for EMM users such as listsize or startpage';
COMMENT ON COLUMN admin_pref_tbl.admin_id IS 'references EMM-User (admin_tbl)';
COMMENT ON COLUMN admin_pref_tbl.pref IS 'parameter - name, e.g. listsize';
COMMENT ON COLUMN admin_pref_tbl.val IS 'parameter - value, e.g. 50';

CREATE TABLE actop_activate_doi_tbl
(
    action_operation_id INTEGER            NOT NULL,
    for_all_lists       SMALLINT DEFAULT 0 NOT NULL,
    media_type          SMALLINT
);
ALTER TABLE actop_activate_doi_tbl ADD CONSTRAINT actopad$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id);
COMMENT ON TABLE actop_activate_doi_tbl IS 'action-step to confirm DOI';
COMMENT ON COLUMN actop_activate_doi_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_activate_doi_tbl.for_all_lists IS '1= state is changed from 5 (waiting for DOI) to 1 (active) for all mailinglists, 0= given ML only';
COMMENT ON COLUMN actop_activate_doi_tbl.media_type IS 'Media type for DOI confirmation';

CREATE TABLE csv_imexport_description_tbl
(
    company_id          INTEGER     NOT NULL,
    name                VARCHAR(64) NOT NULL,
    delimiter           VARCHAR(1)  NOT NULL,
    encoding            VARCHAR(64) NOT NULL,
    stringquote         VARCHAR(1),
    fullimportonly      SMALLINT    NOT NULL,
    id                  SERIAL PRIMARY KEY,
    for_import          SMALLINT    DEFAULT 0,
    importmethod        VARCHAR(32),
    updatemethod        VARCHAR(32),
    tablename           VARCHAR(200),
    creation_date_from  INTEGER NULL,
    creation_date_till  INTEGER NULL,
    no_headers          INTEGER,
    zipped              INTEGER,
    zippassword         VARCHAR(100),
    checkforduplicates  INTEGER,
    nullvaluesaction    INTEGER,
    report_email        VARCHAR(400),
    error_email         VARCHAR(400),
    automapping         INTEGER,
    always_quote        INTEGER     DEFAULT 0,
    creation_date_field VARCHAR(32) NULL,
    datatype            VARCHAR(32) DEFAULT 'CSV',
    pre_import_action   INTEGER,
    dateformat          INTEGER,
    datetimeformat      INTEGER,
    decimalseparator    VARCHAR(1),
    timezone            VARCHAR(32),
    locale_lang         VARCHAR(10) DEFAULT NULL,
    locale_country      VARCHAR(10) DEFAULT NULL
);
CREATE UNIQUE INDEX csv$cid_name$uq ON csv_imexport_description_tbl (company_id, name);
COMMENT ON TABLE csv_imexport_description_tbl IS 'contains more details about EMM - import or -export - settings';
COMMENT ON COLUMN csv_imexport_description_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN csv_imexport_description_tbl.name IS 'Setting-name';
COMMENT ON COLUMN csv_imexport_description_tbl.delimiter IS 'defines csv column delimters';
COMMENT ON COLUMN csv_imexport_description_tbl.encoding IS 'defines csv encoding';
COMMENT ON COLUMN csv_imexport_description_tbl.stringquote IS 'defines csv String identifying marks';
COMMENT ON COLUMN csv_imexport_description_tbl.fullimportonly IS 'Import all data or nothing at all, if any error is included in data like invalid email';
COMMENT ON COLUMN csv_imexport_description_tbl.id IS 'unique ID, use csv_imexport_descr_tbl_seq';
COMMENT ON COLUMN csv_imexport_description_tbl.for_import IS '1=import, 0=export';
COMMENT ON COLUMN csv_imexport_description_tbl.importmethod IS 'e.g. ClearBeforeInsert = esp for reference-tables, table will be cleared befor import, UpdateAndInsert, UpdateOnly';
COMMENT ON COLUMN csv_imexport_description_tbl.updatemethod IS 'UpdateAll or DontUpdateWithEmptyData = no overwriting, if newvalue is null';
COMMENT ON COLUMN csv_imexport_description_tbl.tablename IS 'null = customer_table, table_name for reference-tables';
COMMENT ON COLUMN csv_imexport_description_tbl.creation_date_from IS 'for exports: only in this period created entries would be exported';
COMMENT ON COLUMN csv_imexport_description_tbl.creation_date_till IS 'for exports: only in this period created entries would be exported';
COMMENT ON COLUMN csv_imexport_description_tbl.no_headers IS 'Csv file without headers';
COMMENT ON COLUMN csv_imexport_description_tbl.zipped IS 'Zipped import file';
COMMENT ON COLUMN csv_imexport_description_tbl.zippassword IS 'Encrypted zip password';
COMMENT ON COLUMN csv_imexport_description_tbl.checkforduplicates IS 'Duplicate check method id';
COMMENT ON COLUMN csv_imexport_description_tbl.always_quote IS 'Always use delimiter for quotation of strings in csv outout';
COMMENT ON COLUMN csv_imexport_description_tbl.nullvaluesaction IS 'Overwrite or keep db values if import value is null';
COMMENT ON COLUMN csv_imexport_description_tbl.report_email IS 'Email for import information';
COMMENT ON COLUMN csv_imexport_description_tbl.error_email IS 'Email for information on import errors';
COMMENT ON COLUMN csv_imexport_description_tbl.automapping IS 'Used db cloumn names for csv columns';
COMMENT ON COLUMN csv_imexport_description_tbl.creation_date_field IS 'Name of the creation date field to be used for special export period limits';
COMMENT ON COLUMN csv_imexport_description_tbl.datatype IS 'Datatype to import (CSV, JSON)';
COMMENT ON COLUMN csv_imexport_description_tbl.pre_import_action IS 'Unique ID of pa re import action';
COMMENT ON COLUMN csv_imexport_description_tbl.dateformat IS 'Format of dates without time, see DateFormat-class';
COMMENT ON COLUMN csv_imexport_description_tbl.datetimeformat IS 'Format of dates with time, see DateFormat-class';
COMMENT ON COLUMN csv_imexport_description_tbl.decimalseparator IS 'Decimalseparator for float numbers';
COMMENT ON COLUMN csv_imexport_description_tbl.timezone IS 'Timezone to export';
COMMENT ON COLUMN csv_imexport_description_tbl.locale_lang IS 'Language part of locale used for reports';
COMMENT ON COLUMN csv_imexport_description_tbl.locale_country IS 'Country part of locale used for reports';

CREATE TABLE job_queue_result_tbl
(
    job_id   INTEGER                             NOT NULL,
    time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    result   VARCHAR(512)                        NOT NULL,
    duration INTEGER   DEFAULT 0,
    hostname VARCHAR(64)                         NOT NULL
);
ALTER TABLE job_queue_result_tbl ADD CONSTRAINT jobresult$jobid$fk FOREIGN KEY (job_id) REFERENCES job_queue_tbl (id);
COMMENT ON TABLE job_queue_result_tbl IS 'history / log for done jobs triggered by job-queue: result and duration for monitoring / tracing issues';
COMMENT ON COLUMN job_queue_result_tbl.job_id IS 'references job_queue_tbl.id';
COMMENT ON COLUMN job_queue_result_tbl.time IS 'execution-timestamp';
COMMENT ON COLUMN job_queue_result_tbl.result IS 'execution result: OK or error-message';
COMMENT ON COLUMN job_queue_result_tbl.duration IS 'execution duration';
COMMENT ON COLUMN job_queue_result_tbl.hostname IS 'execution host';

CREATE TABLE admin_password_reset_tbl
(
    admin_id    INTEGER                             NOT NULL,
    time        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    valid_until TIMESTAMP                           NOT NULL,
    ip_address  VARCHAR(50)                         NOT NULL,
    token_hash  VARCHAR(64)                         NOT NULL,
    error_count INTEGER                             NOT NULL
);
COMMENT ON TABLE admin_password_reset_tbl IS 'stores information about password changes and - expire dates';
COMMENT ON COLUMN admin_password_reset_tbl.admin_id IS 'references EMM-User (admin_tbl)';
COMMENT ON COLUMN admin_password_reset_tbl.time IS 'last pwsd - change';
COMMENT ON COLUMN admin_password_reset_tbl.valid_until IS 'pswd expire date';
COMMENT ON COLUMN admin_password_reset_tbl.ip_address IS '[secret_data] address calling last change';
COMMENT ON COLUMN admin_password_reset_tbl.token_hash IS 'hashed pswd - token';
COMMENT ON COLUMN admin_password_reset_tbl.error_count IS 'fail counter';

CREATE TABLE form_component_tbl
(
    id            SERIAL PRIMARY KEY,
    form_id       INTEGER                             NOT NULL,
    company_id    INTEGER                             NOT NULL,
    name          VARCHAR(64)                         NOT NULL,
    type          INTEGER                             NOT NULL,
    data          BYTEA,
    data_size     INTEGER                             NOT NULL,
    width         INTEGER,
    height        INTEGER,
    mimetype      VARCHAR(32)                         NOT NULL,
    description   VARCHAR(100),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    change_date   TIMESTAMP                           NOT NULL,
    CONSTRAINT FORMCOMP$FIDNMTYPE$UQ UNIQUE (form_id, name, type)
);
COMMENT ON TABLE form_component_tbl IS 'Images or other binary content for userforms';
COMMENT ON COLUMN form_component_tbl.id IS 'refernce key';
COMMENT ON COLUMN form_component_tbl.form_id IS 'Referenced for this content';
COMMENT ON COLUMN form_component_tbl.company_id IS 'Referenced client';
COMMENT ON COLUMN form_component_tbl.name IS 'Component name';
COMMENT ON COLUMN form_component_tbl.type IS 'Component like hosted image or external data';
COMMENT ON COLUMN form_component_tbl.data IS 'Binary data of component';
COMMENT ON COLUMN form_component_tbl.data_size IS 'Data size';
COMMENT ON COLUMN form_component_tbl.width IS 'Image width';
COMMENT ON COLUMN form_component_tbl.height IS 'Image height';
COMMENT ON COLUMN form_component_tbl.mimetype IS 'Mimetype of component';
COMMENT ON COLUMN form_component_tbl.description IS 'Description of component';
COMMENT ON COLUMN form_component_tbl.creation_date IS 'Creation date of component';
COMMENT ON COLUMN form_component_tbl.change_date IS 'Change date of component';

CREATE TABLE mail_notification_buffer_tbl
(
    id                SERIAL PRIMARY KEY,
    recipients        VARCHAR(400)  NOT NULL,
    subject           VARCHAR(400)  NOT NULL,
    text              VARCHAR(1000) NOT NULL,
    send_time         TIMESTAMP     NOT NULL,
    last_request_time TIMESTAMP     NOT NULL,
    request_count     INTEGER       NOT NULL
);
COMMENT ON TABLE mail_notification_buffer_tbl IS 'Buffer for email notification on errors and events to not send spam emails to notified email';
COMMENT ON COLUMN mail_notification_buffer_tbl.id IS 'Reference key';
COMMENT ON COLUMN mail_notification_buffer_tbl.recipients IS 'Recipients email addresses';
COMMENT ON COLUMN mail_notification_buffer_tbl.subject IS 'Email subject';
COMMENT ON COLUMN mail_notification_buffer_tbl.text IS 'Email text';
COMMENT ON COLUMN mail_notification_buffer_tbl.send_time IS 'Last send time of this email notification';
COMMENT ON COLUMN mail_notification_buffer_tbl.last_request_time IS 'Latest request to send exactly this email and its text';
COMMENT ON COLUMN mail_notification_buffer_tbl.request_count IS 'Number of send retries';

CREATE TABLE mailing_statistic_job_tbl
(
    mailing_stat_job_id SERIAL PRIMARY KEY,
    job_status          INTEGER NOT NULL,
    mailing_id          INTEGER NOT NULL,
    target_groups       VARCHAR(40),
    recipients_type     INTEGER NOT NULL,
    creation_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    job_status_descr    VARCHAR(4000)
);
COMMENT ON TABLE mailing_statistic_job_tbl IS 'Webservice statistic jobs to request asynchronously';
COMMENT ON COLUMN mailing_statistic_job_tbl.mailing_stat_job_id IS 'Statistic job id';
COMMENT ON COLUMN mailing_statistic_job_tbl.job_status IS 'STatus of the generation of statistics data';
COMMENT ON COLUMN mailing_statistic_job_tbl.mailing_id IS 'Mailing referenced for statistics';
COMMENT ON COLUMN mailing_statistic_job_tbl.target_groups IS 'Used targetgroups for statistics';
COMMENT ON COLUMN mailing_statistic_job_tbl.recipients_type IS 'Recipient types (World, Testser, ADmin)';
COMMENT ON COLUMN mailing_statistic_job_tbl.creation_date IS 'Creation date of this request';
COMMENT ON COLUMN mailing_statistic_job_tbl.change_date IS 'Change Date of this request';
COMMENT ON COLUMN mailing_statistic_job_tbl.job_status_descr IS 'Status info text';

CREATE TABLE mailing_statistic_tgtgrp_tbl
(
    mailing_stat_tgtgrp_id SERIAL PRIMARY KEY,
    mailing_stat_job_id    INTEGER NOT NULL,
    mailing_id             INTEGER NOT NULL,
    target_group_id        INTEGER NOT NULL,
    creation_date          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    revenue                DECIMAL(5, 2) DEFAULT 0.0
);
COMMENT ON TABLE mailing_statistic_tgtgrp_tbl IS 'Target group lists for MailingSummary webservice';
COMMENT ON COLUMN mailing_statistic_tgtgrp_tbl.mailing_stat_tgtgrp_id IS 'ID of entry';
COMMENT ON COLUMN mailing_statistic_tgtgrp_tbl.mailing_stat_job_id IS 'ID of statistics job';
COMMENT ON COLUMN mailing_statistic_tgtgrp_tbl.mailing_id IS 'ID of mailing for statistics. Reference to mailing_tbl.id';
COMMENT ON COLUMN mailing_statistic_tgtgrp_tbl.target_group_id IS 'ID of target group used in statistics. Reference to dyn_target_tbl.target_id';
COMMENT ON COLUMN mailing_statistic_tgtgrp_tbl.creation_date IS 'Creation date of entry';
COMMENT ON COLUMN mailing_statistic_tgtgrp_tbl.revenue IS 'revenue of mailing for selected target group';

CREATE TABLE mailing_statistic_value_tbl
(
    mailing_stat_tgtgrp_id INTEGER NOT NULL,
    category_index         INTEGER NOT NULL,
    stat_value             INTEGER       DEFAULT 0,
    stat_quotient          DECIMAL(5, 2) DEFAULT 0.0
);
ALTER TABLE mailing_statistic_value_tbl ADD CONSTRAINT mailingstatvalue$jigi$uk UNIQUE (mailing_stat_tgtgrp_id, category_index);
COMMENT ON TABLE mailing_statistic_value_tbl IS 'Holds values for statistics computes by MailingSummary webservice';
COMMENT ON COLUMN mailing_statistic_value_tbl.mailing_stat_tgtgrp_id IS 'Reference to mailing_statistic_tgtgrp_tbl.mailing_stat_tgtgrp_id';
COMMENT ON COLUMN mailing_statistic_value_tbl.category_index IS 'Type of statistics value';
COMMENT ON COLUMN mailing_statistic_value_tbl.stat_value IS 'Statistics vlaue (absolute)';
COMMENT ON COLUMN mailing_statistic_value_tbl.stat_quotient IS 'Statistics value (relative)';

CREATE TABLE server_command_tbl
(
    command        VARCHAR(200),
    server_name    VARCHAR(50),
    execution_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    admin_id       INTEGER,
    description    VARCHAR(2000),
    timestamp      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
COMMENT ON TABLE server_command_tbl IS 'Internal command storage for EMM servers. Can be set by EMM and is executed on RDIRs';
COMMENT ON COLUMN server_command_tbl.command IS 'Command to execute by servers like clear all image chaches';
COMMENT ON COLUMN server_command_tbl.server_name IS 'Server to execute the command';
COMMENT ON COLUMN server_command_tbl.execution_date IS 'Execution date';
COMMENT ON COLUMN server_command_tbl.admin_id IS 'Admin who created this command';
COMMENT ON COLUMN server_command_tbl.description IS 'Decription of command';
COMMENT ON COLUMN server_command_tbl.timestamp IS 'Change date of command';

CREATE TABLE calendar_custom_recipients_tbl
(
    comment_id INTEGER           NOT NULL,
    company_id INTEGER           NOT NULL,
    email      VARCHAR(200),
    admin_id   INTEGER DEFAULT 0 NOT NULL,
    notified   INTEGER DEFAULT 0 NOT NULL
);
COMMENT ON TABLE calendar_custom_recipients_tbl IS 'Send comments on calendar dates to email addresses';
COMMENT ON COLUMN calendar_custom_recipients_tbl.comment_id IS 'Referenced Comment';
COMMENT ON COLUMN calendar_custom_recipients_tbl.company_id IS 'Referenced client';
COMMENT ON COLUMN calendar_custom_recipients_tbl.email IS 'Email of recipient';
COMMENT ON COLUMN calendar_custom_recipients_tbl.admin_id IS 'Admin who created this notification request';
COMMENT ON COLUMN calendar_custom_recipients_tbl.notified IS 'Was recipient notified yet';

CREATE TABLE sessionhijackingprevention_tbl
(
    ip_group INTEGER,
    ip       VARCHAR(50),
    comments VARCHAR(200)
);
COMMENT ON TABLE sessionhijackingprevention_tbl IS 'Definition of groups which may change their request IP on an EMM without being blocked';
COMMENT ON COLUMN sessionhijackingprevention_tbl.ip_group IS 'Reference key';
COMMENT ON COLUMN sessionhijackingprevention_tbl.ip IS 'IP entry for a whitelist group';
COMMENT ON COLUMN sessionhijackingprevention_tbl.comments IS 'Description of this group';

CREATE TABLE landingpage_tbl
(
    domain      VARCHAR(64),
    landingpage VARCHAR(64),
    http_code   SMALLINT
);
COMMENT ON TABLE landingpage_tbl IS 'Landigpage info for Rdir servers';
COMMENT ON COLUMN landingpage_tbl.domain IS 'Domain used in rdir request';
COMMENT ON COLUMN landingpage_tbl.landingpage IS 'Landingpage for redirect of rdir requests without any parameter';
COMMENT ON COLUMN landingpage_tbl.http_code IS 'HTTP redirect code or 0 or null for HTML meta redirect';

CREATE TABLE import_action_tbl
(
    importaction_id SERIAL PRIMARY KEY,
    company_id      INTEGER     NOT NULL,
    name            VARCHAR(128),
    type            VARCHAR(32) NOT NULL,
    action          TEXT        NOT NULL,
    creation_date   TIMESTAMP,
    change_date     TIMESTAMP
);
COMMENT ON TABLE import_action_tbl IS 'Pre import actions';
COMMENT ON COLUMN import_action_tbl.importaction_id IS 'Reference key used by import_profile_tbl';
COMMENT ON COLUMN import_action_tbl.company_id IS 'Referenced client';
COMMENT ON COLUMN import_action_tbl.name IS 'Name of this pre import action';
COMMENT ON COLUMN import_action_tbl.type IS 'Only SQL by now';
COMMENT ON COLUMN import_action_tbl.action IS 'SQL script text to be executed';
COMMENT ON COLUMN import_action_tbl.creation_date IS 'Creation date of this action';
COMMENT ON COLUMN import_action_tbl.change_date IS 'Change date of this action';

CREATE TABLE recipients_report_tbl
(
    recipients_report_id SERIAL PRIMARY KEY,
    report_date          TIMESTAMP           NOT NULL,
    filename             VARCHAR(500),
    datasource_id        INTEGER,
    admin_id             INTEGER             NOT NULL,
    company_id           INTEGER             NOT NULL,
    report               TEXT,
    download_id          INTEGER,
    error                INTEGER,
    content              BYTEA,
    entity_type          INTEGER DEFAULT 0,
    entity_execution     INTEGER DEFAULT 0,
    entity_data          INTEGER DEFAULT 0,
    entity_id            INTEGER
);
CREATE INDEX recrep$cidrepdate$idx ON recipients_report_tbl (report_date, company_id);
CREATE INDEX recrep$cidaidty$idx ON recipients_report_tbl (admin_id, company_id);
CREATE INDEX recrep$cid$idx ON recipients_report_tbl (company_id);
COMMENT ON TABLE recipients_report_tbl IS 'Report on executed recipient imports and exports';
COMMENT ON COLUMN recipients_report_tbl.error IS 'Flag to show if this report contains description of an data error';
COMMENT ON COLUMN recipients_report_tbl.recipients_report_id IS 'Reference key';
COMMENT ON COLUMN recipients_report_tbl.report_date IS 'Date of import or export';
COMMENT ON COLUMN recipients_report_tbl.filename IS 'Imported filename';
COMMENT ON COLUMN recipients_report_tbl.datasource_id IS 'Used datasource id for import';
COMMENT ON COLUMN recipients_report_tbl.admin_id IS 'admin which executed the import or export';
COMMENT ON COLUMN recipients_report_tbl.company_id IS 'Referenced client';
COMMENT ON COLUMN recipients_report_tbl.report IS 'Report text (HTML)';
COMMENT ON COLUMN recipients_report_tbl.download_id IS 'Id of downloadable export file';
COMMENT ON COLUMN recipients_report_tbl.content IS 'Report data as file';
COMMENT ON COLUMN recipients_report_tbl.entity_type IS 'Type of entity. 0 - unknown, 1 - import, 2 - export';
COMMENT ON COLUMN recipients_report_tbl.entity_execution IS 'Type of execution. 0 - unknown, 1 - manual, 2 - automatic';
COMMENT ON COLUMN recipients_report_tbl.entity_data IS 'Determines whence or where the data is going. 0 - unknown, 1 - profile, 2 - reference table';
COMMENT ON COLUMN recipients_report_tbl.entity_id IS 'ID of entity according to type.';

CREATE TABLE scripthelper_email_log_tbl
(
    company_id INTEGER,
    from_email VARCHAR(256),
    to_email   VARCHAR(256),
    cc_email   VARCHAR(256),
    subject    VARCHAR(256),
    send_date  TIMESTAMP,
    mailing_id INTEGER,
    form_id    INTEGER
);
COMMENT ON TABLE scripthelper_email_log_tbl IS 'Logging of sent emails by Velocity scripts';
COMMENT ON COLUMN scripthelper_email_log_tbl.company_id IS 'Referenced client';
COMMENT ON COLUMN scripthelper_email_log_tbl.from_email IS 'Senders email';
COMMENT ON COLUMN scripthelper_email_log_tbl.to_email IS 'Recipients email';
COMMENT ON COLUMN scripthelper_email_log_tbl.cc_email IS 'CC emails';
COMMENT ON COLUMN scripthelper_email_log_tbl.subject IS 'Email subject';
COMMENT ON COLUMN scripthelper_email_log_tbl.send_date IS 'Send date of email';
COMMENT ON COLUMN scripthelper_email_log_tbl.mailing_id IS 'Referenced mailing';
COMMENT ON COLUMN scripthelper_email_log_tbl.form_id IS 'Referenced userform_tbl entry';

CREATE TABLE download_tbl
(
    download_id SERIAL PRIMARY KEY,
    content     BYTEA               NOT NULL
);
COMMENT ON TABLE download_tbl IS 'Downloadable files like exports';
COMMENT ON COLUMN download_tbl.download_id IS 'Reference key';
COMMENT ON COLUMN download_tbl.content IS 'File data';

CREATE TABLE admin_use_tbl
(
    admin_id  INTEGER                             NOT NULL,
    feature   VARCHAR(100)                        NOT NULL,
    use_count INTEGER   DEFAULT 0                 NOT NULL,
    last_use  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
ALTER TABLE admin_use_tbl ADD CONSTRAINT pk_adminuse PRIMARY KEY (admin_id, feature);
COMMENT ON TABLE admin_use_tbl IS 'Logging of admin usage of EMM';
COMMENT ON COLUMN admin_use_tbl.admin_id IS 'Referenced admin';
COMMENT ON COLUMN admin_use_tbl.feature IS 'Feature used by this admin';
COMMENT ON COLUMN admin_use_tbl.use_count IS 'Times of usage';
COMMENT ON COLUMN admin_use_tbl.last_use IS 'Latest usage';

CREATE TABLE device_blacklist_tbl
(
    device_id    INTEGER UNIQUE,
    device_order INTEGER UNIQUE,
    description  VARCHAR(200),
    regex        VARCHAR(200),
    service      INTEGER
);
COMMENT ON TABLE device_blacklist_tbl IS 'Devices (Useragents) to not log like facebook crawlers';
COMMENT ON COLUMN device_blacklist_tbl.device_id IS 'Reference key';
COMMENT ON COLUMN device_blacklist_tbl.device_order IS 'Order to check reg exp';
COMMENT ON COLUMN device_blacklist_tbl.description IS 'Description';
COMMENT ON COLUMN device_blacklist_tbl.regex IS 'Reg Exp for Useragent';
COMMENT ON COLUMN device_blacklist_tbl.service IS 'Activation sign';

CREATE TABLE csv_imexport_mapping_tbl
(
    description_id INTEGER      NOT NULL,
    dbcolumn       VARCHAR(100) NOT NULL,
    filecolumn     VARCHAR(100) NULL,
    defaultvalue   VARCHAR(100),
    format         VARCHAR(100),
    mandatory      INTEGER      NOT NULL,
    encrypted      INTEGER      NOT NULL,
    keycolumn      INTEGER      NOT NULL
) TABLESPACE data_emmaux;
ALTER TABLE csv_imexport_mapping_tbl ADD CONSTRAINT csv$mapping FOREIGN KEY (description_id) REFERENCES csv_imexport_description_tbl (id);
COMMENT ON TABLE csv_imexport_mapping_tbl IS 'Columnmappings for csv imports';
COMMENT ON COLUMN csv_imexport_mapping_tbl.description_id IS 'Foreighn key to csv_imexport_description_tbl';
COMMENT ON COLUMN csv_imexport_mapping_tbl.dbcolumn IS 'DBs column to map to';
COMMENT ON COLUMN csv_imexport_mapping_tbl.filecolumn IS 'Files column to map from';
COMMENT ON COLUMN csv_imexport_mapping_tbl.defaultvalue IS 'Defaultvalue for data';
COMMENT ON COLUMN csv_imexport_mapping_tbl.format IS 'Format for data';
COMMENT ON COLUMN csv_imexport_mapping_tbl.mandatory IS 'Is field mandatory';
COMMENT ON COLUMN csv_imexport_mapping_tbl.encrypted IS 'Is fields data encrypted';
COMMENT ON COLUMN csv_imexport_mapping_tbl.keycolumn IS 'Is this a keycolumn';

CREATE TABLE client_tbl
(
    client_id    INTEGER UNIQUE,
    client_order INTEGER UNIQUE,
    description  VARCHAR(200),
    regex        VARCHAR(200)
);
COMMENT ON TABLE client_tbl IS 'Detection data for email-clients and browsers used for link-clicks';
COMMENT ON COLUMN client_tbl.client_id IS 'Unique client id for reference in onepixellog and rdirlog tables';
COMMENT ON COLUMN client_tbl.client_order IS 'Order of evaluation to allow generic clientd-patterns to be used after specific ones';
COMMENT ON COLUMN client_tbl.description IS 'Description text for client';
COMMENT ON COLUMN client_tbl.regex IS 'Regular expression used for detection on UserAgent strings';

CREATE TABLE user_agent_for_client_tbl
(
    user_agent_id SERIAL PRIMARY KEY,
    user_agent    VARCHAR(2200) NOT NULL,
    req_counter   INTEGER       NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX useragentcl$useragent$uq ON user_agent_for_client_tbl (user_agent);
COMMENT ON TABLE user_agent_for_client_tbl IS 'Interim storage for email-client and browser UserAgent strings for later manually detection of new clients';
COMMENT ON COLUMN user_agent_for_client_tbl.user_agent_id IS 'Unique UserAgent entry id for references (not used by now)';
COMMENT ON COLUMN user_agent_for_client_tbl.user_agent IS 'UserAgent string';
COMMENT ON COLUMN user_agent_for_client_tbl.req_counter IS 'Number of occurences of this UserAgent since measurement started';
COMMENT ON COLUMN user_agent_for_client_tbl.creation_date IS 'First occurence of this UserAgent';
COMMENT ON COLUMN user_agent_for_client_tbl.change_date IS 'Latest occurence of this UserAgent';

CREATE TABLE company_permission_tbl
(
    company_id      INTEGER,
    permission_name VARCHAR(40),
    description     VARCHAR(100),
    creation_date   TIMESTAMP
) TABLESPACE data_accounting;
ALTER TABLE company_permission_tbl ADD CONSTRAINT compperm$id_permname$uq PRIMARY KEY (company_id, permission_name);
COMMENT ON TABLE company_permission_tbl IS 'Store premium permissions for clients which are granted';
COMMENT ON COLUMN company_permission_tbl.company_id IS 'Referenced client (0=all)';
COMMENT ON COLUMN company_permission_tbl.description IS 'Information why this premium permission is granted and by whom';
COMMENT ON COLUMN company_permission_tbl.creation_date IS 'Date of permission grant action';
COMMENT ON COLUMN company_permission_tbl.permission_name IS 'name of the permission granted by this entry';

CREATE TABLE workflow_reaction_decl_tbl
(
    step_id            INTEGER            NOT NULL,
    previous_step_id   INTEGER            NOT NULL,
    reaction_id        INTEGER            NOT NULL,
    company_id         INTEGER            NOT NULL,
    deadline_relative  INTEGER            NOT NULL,
    deadline_hours     INTEGER            NOT NULL,
    deadline_minutes   INTEGER            NOT NULL,
    target_id          INTEGER            NOT NULL,
    is_target_positive SMALLINT DEFAULT 1 NOT NULL,
    mailing_id         INTEGER            NOT NULL
) TABLESPACE data_workflow;
ALTER TABLE workflow_reaction_decl_tbl ADD CONSTRAINT workflow_reac_d$sidridcid$uk UNIQUE (step_id, reaction_id, company_id);
COMMENT ON TABLE workflow_reaction_decl_tbl IS 'contains the generic (declaration) action-based execution plan for campaigns (represents decisions and mailings), more: http://wiki.agnitas.local/doku.php?id=abteilung:technik:entwicklung:workflowmanager_workflow';
COMMENT ON COLUMN workflow_reaction_decl_tbl.step_id IS 'identifies a step of campaign execution plan, unique within each campaign/reaction';
COMMENT ON COLUMN workflow_reaction_decl_tbl.previous_step_id IS 'references previous step (workflow_reaction_decl_tbl), 0 = initial step (start icon event triggered)';
COMMENT ON COLUMN workflow_reaction_decl_tbl.reaction_id IS 'references a reaction (workflow_reaction_tbl)';
COMMENT ON COLUMN workflow_reaction_decl_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN workflow_reaction_decl_tbl.deadline_relative IS 'relative deadline (milliseconds) for this step';
COMMENT ON COLUMN workflow_reaction_decl_tbl.deadline_hours IS 'exact time of day (hours in admin timezone) for a deadline, is only applicable if deadline_relative exceeds single day, ignored if invalid';
COMMENT ON COLUMN workflow_reaction_decl_tbl.deadline_minutes IS 'exact time of day (minutes in admin timezone) for a deadline, is only applicable if deadline_relative exceeds single day, ignored if invalid';
COMMENT ON COLUMN workflow_reaction_decl_tbl.target_id IS 'target group - ID (dyn_target_tbl) to be applied to recipients taken from previous step, 0 = ignored';
COMMENT ON COLUMN workflow_reaction_decl_tbl.is_target_positive IS 'whether or not a target group (target_id) should be inverted, 0 = inverted, 1 = direct';
COMMENT ON COLUMN workflow_reaction_decl_tbl.mailing_id IS 'mailing - ID (mailing_tbl), mailing to be sent at this step, 0 = ignored';

CREATE TABLE workflow_reaction_step_tbl
(
    case_id          INTEGER            NOT NULL,
    step_id          INTEGER            NOT NULL,
    previous_step_id INTEGER            NOT NULL,
    reaction_id      INTEGER            NOT NULL,
    company_id       INTEGER            NOT NULL,
    step_date        TIMESTAMP          NOT NULL,
    done             SMALLINT DEFAULT 0 NOT NULL
) TABLESPACE data_workflow;
ALTER TABLE workflow_reaction_step_tbl ADD CONSTRAINT workflow_reac_step$cisirici$uk UNIQUE (case_id, step_id, reaction_id, company_id);
COMMENT ON TABLE workflow_reaction_step_tbl IS 'contains the specific steps and the completion status (undone/done) of an action-based execution plan for campaigns, generated by a particular trigger event (mail received, mail opened, link clicked, etc.), new records are written by WorkflowReactionHandler and every record (representing the group of recipients who reacted) gets a unique case_id, more: http://wiki.agnitas.local/doku.php?id=abteilung:technik:entwicklung:workflowmanager_workflow';
COMMENT ON COLUMN workflow_reaction_step_tbl.case_id IS 'an identifier used to isolate prepared execution plans (and recipients) generated on different initial trigger events, unique for company_id AND reaction_id';
COMMENT ON COLUMN workflow_reaction_step_tbl.step_id IS 'identifies a step of campaign execution plan (workflow_reaction_step_tbl and workflow_reaction_decl_tbl)';
COMMENT ON COLUMN workflow_reaction_step_tbl.previous_step_id IS 'references previous step to be done first';
COMMENT ON COLUMN workflow_reaction_step_tbl.reaction_id IS 'references a reaction (workflow_reaction_tbl)';
COMMENT ON COLUMN workflow_reaction_step_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN workflow_reaction_step_tbl.step_date IS 'date when a step should be processed and marked as done';
COMMENT ON COLUMN workflow_reaction_step_tbl.done IS 'whether or not a step is done (mailing is sent or recipients are prepared for the next step), 0 = pending, 1 = done';

CREATE TABLE workflow_reaction_out_tbl
(
    case_id     INTEGER   NOT NULL,
    step_id     INTEGER   NOT NULL,
    reaction_id INTEGER   NOT NULL,
    company_id  INTEGER   NOT NULL,
    customer_id INTEGER   NOT NULL,
    step_date   TIMESTAMP NOT NULL
) TABLESPACE data_workflow;
ALTER TABLE workflow_reaction_out_tbl ADD CONSTRAINT workflow_reaction_out$uk UNIQUE (case_id, step_id, reaction_id, company_id, customer_id);
CREATE INDEX wfrot$datecustrea$idx ON workflow_reaction_out_tbl (customer_id, reaction_id, step_date);
COMMENT ON TABLE workflow_reaction_out_tbl IS 'contains those recipients that have reacted to a specific step during campaign execution, entries are written by WorkflowReactionHandler and are related to workflow_reaction_step_tbl by case_id, more: http://wiki.agnitas.local/doku.php?id=abteilung:technik:entwicklung:workflowmanager_workflow';
COMMENT ON COLUMN workflow_reaction_out_tbl.case_id IS 'references (along with step_id) a step (workflow_reaction_step_tbl)';
COMMENT ON COLUMN workflow_reaction_out_tbl.step_id IS 'references (along with case_id) a step (workflow_reaction_step_tbl), 0 = initial step (start icon event triggered)';
COMMENT ON COLUMN workflow_reaction_out_tbl.reaction_id IS 'references a reaction (workflow_reaction_tbl)';
COMMENT ON COLUMN workflow_reaction_out_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN workflow_reaction_out_tbl.customer_id IS 'customer - ID (customer_<cid>_tbl)';
COMMENT ON COLUMN workflow_reaction_out_tbl.step_date IS 'date when a step has been processed and recipients have been logged';

CREATE TABLE workflow_dependency_tbl
(
    company_id  INTEGER NOT NULL,
    workflow_id INTEGER NOT NULL,
    type        INTEGER NOT NULL,
    entity_id   INTEGER NOT NULL,
    entity_name VARCHAR(200)
) TABLESPACE data_workflow;
COMMENT ON TABLE workflow_dependency_tbl IS 'lists the entities (mailings, archives, profile fields, etc) that a workflow depends on';
COMMENT ON COLUMN workflow_dependency_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN workflow_dependency_tbl.workflow_id IS 'workflow - ID (workflow_tbl)';
COMMENT ON COLUMN workflow_dependency_tbl.type IS 'type of entity that a workflow depends on';
COMMENT ON COLUMN workflow_dependency_tbl.entity_id IS 'identifier of an entity that a workflow depends on';
COMMENT ON COLUMN workflow_dependency_tbl.entity_name IS 'name of an entity that a workflow depends on';

CREATE TABLE workflow_reminder_tbl
(
    reminder_id     SERIAL PRIMARY KEY,
    company_id      INTEGER   NOT NULL,
    workflow_id     INTEGER   NOT NULL,
    sender_admin_id INTEGER   NOT NULL,
    type            INTEGER   NOT NULL,
    message         VARCHAR(2000),
    send_date       TIMESTAMP NOT NULL
) TABLESPACE data_workflow;
COMMENT ON TABLE workflow_reminder_tbl IS 'stores scheduled workflow reminders';
COMMENT ON COLUMN workflow_reminder_tbl.reminder_id IS 'a unique ID to be taken from workflow_reminder_tbl_seq';
COMMENT ON COLUMN workflow_reminder_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN workflow_reminder_tbl.workflow_id IS 'workflow - ID (workflow_tbl)';
COMMENT ON COLUMN workflow_reminder_tbl.sender_admin_id IS 'admin to be used as message sender - ID (admin_tbl)';
COMMENT ON COLUMN workflow_reminder_tbl.type IS 'a reminder type, 0 = workflow start, 1 = workflow stop';
COMMENT ON COLUMN workflow_reminder_tbl.message IS 'reminder message to be used, may be generated automatically if omitted';
COMMENT ON COLUMN workflow_reminder_tbl.send_date IS 'a date when a reminder should be delivered';

CREATE TABLE workflow_reminder_recp_tbl
(
    company_id  INTEGER           NOT NULL,
    reminder_id INTEGER           NOT NULL,
    email       VARCHAR(200),
    admin_id    INTEGER DEFAULT 0 NOT NULL,
    notified    INTEGER DEFAULT 0 NOT NULL,
    CONSTRAINT workflow$rem$recp$remid$fk FOREIGN KEY (reminder_id) REFERENCES workflow_reminder_tbl (reminder_id) ON DELETE CASCADE
) TABLESPACE data_workflow;
COMMENT ON TABLE workflow_reminder_recp_tbl IS 'stores recipients for scheduled workflow reminders';
COMMENT ON COLUMN workflow_reminder_recp_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN workflow_reminder_recp_tbl.reminder_id IS 'reminder - ID (workflow_reminder_tbl)';
COMMENT ON COLUMN workflow_reminder_recp_tbl.email IS 'an email address to send reminder to';
COMMENT ON COLUMN workflow_reminder_recp_tbl.admin_id IS 'admin to send reminder to - ID (admin_tbl), optional';
COMMENT ON COLUMN workflow_reminder_recp_tbl.notified IS 'whether or not reminder is delivered to this recipient, 0 = no, 1 = yes';

CREATE TABLE bcc_mailing_account_tbl
(
    mailing_account_id INTEGER PRIMARY KEY,
    no_of_mailings     INTEGER,
    no_of_bytes        INTEGER
) TABLESPACE data_accounting;
COMMENT ON TABLE bcc_mailing_account_tbl IS 'stores accounting information for bcc mails';
COMMENT ON COLUMN bcc_mailing_account_tbl.mailing_account_id IS '-> mailing_account_tbl.mailing_account_id';
COMMENT ON COLUMN bcc_mailing_account_tbl.no_of_mailings IS 'number of mailings sent as bcc mailings';
COMMENT ON COLUMN bcc_mailing_account_tbl.no_of_bytes IS 'number of bytes sent as bcc mailings';

CREATE TABLE cust_temporary_tbl
(
    uuid        VARCHAR(255) NOT NULL,
    customer_id INTEGER      NOT NULL,
    PRIMARY KEY (uuid, customer_id)
);
COMMENT ON TABLE cust_temporary_tbl IS 'Temp table for save customer id and continue use them for lock before update';
COMMENT ON COLUMN cust_temporary_tbl.uuid IS 'Uniq number of transaction';
COMMENT ON COLUMN cust_temporary_tbl.customer_id IS 'Customer id';

CREATE TABLE test_recipients_tbl
(
    maildrop_status_id INTEGER NOT NULL,
    customer_id        INTEGER NOT NULL,
    PRIMARY KEY (maildrop_status_id, customer_id)
) TABLESPACE data_accounting;
COMMENT ON TABLE test_recipients_tbl IS 'If maildrop_status_tbl.selected_test_recipients=1, then the recipients for this test run are taken from this table';
COMMENT ON COLUMN test_recipients_tbl.maildrop_status_id IS 'Reference to maildrop_status_tbl.status_id';
COMMENT ON COLUMN test_recipients_tbl.customer_id IS 'Reference to customer_xx_tbl.customer_id';

CREATE TABLE pending_email_change_tbl
(
    company_ref       INTEGER,
    customer_ref      INTEGER,
    new_email_address VARCHAR(100),
    confirmation_code VARCHAR(100),
    creation_date     TIMESTAMP
) TABLESPACE data_temp;
ALTER TABLE pending_email_change_tbl ADD CONSTRAINT pndemlchg$cid$NN CHECK (company_ref IS NOT NULL);
ALTER TABLE pending_email_change_tbl ADD CONSTRAINT pndemlchg$custid$NN CHECK (customer_ref IS NOT NULL);
ALTER TABLE pending_email_change_tbl ADD CONSTRAINT pndemlchg$neweml$NN CHECK (new_email_address IS NOT NULL);
ALTER TABLE pending_email_change_tbl ADD CONSTRAINT pndemlchg$confcode$NN CHECK (confirmation_code IS NOT NULL);
ALTER TABLE pending_email_change_tbl ADD CONSTRAINT pndemlchg$crdate$NN CHECK (creation_date IS NOT NULL);
COMMENT ON TABLE pending_email_change_tbl IS 'List of unconfirmed email address changes';
COMMENT ON COLUMN pending_email_change_tbl.company_ref IS 'References company';
COMMENT ON COLUMN pending_email_change_tbl.customer_ref IS 'Referencescustomer';
COMMENT ON COLUMN pending_email_change_tbl.new_email_address IS 'New (unconfirmed) email address';
COMMENT ON COLUMN pending_email_change_tbl.confirmation_code IS 'Random code required for confirmation';
COMMENT ON COLUMN pending_email_change_tbl.creation_date IS 'Creation date of record';

CREATE TABLE auto_import_ws_tbl
(
    job_id           SERIAL PRIMARY KEY,
    company_id       INTEGER             NOT NULL,
    auto_import_id   INTEGER             NOT NULL,
    status           INTEGER             NOT NULL,
    expire_timestamp TIMESTAMP,
    report           TEXT
);
COMMENT ON TABLE auto_import_ws_tbl IS 'stores WS-triggered auto-import job state while running and some time after completion';
COMMENT ON COLUMN auto_import_ws_tbl.job_id IS 'unique ID, use auto_import_ws_tbl_seq';
COMMENT ON COLUMN auto_import_ws_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN auto_import_ws_tbl.auto_import_id IS 'auto-import - ID (auto_import_tbl)';
COMMENT ON COLUMN auto_import_ws_tbl.status IS 'current job status; 1=queued, 2=running, 3=transferring, 4=done, 5=failed';
COMMENT ON COLUMN auto_import_ws_tbl.expire_timestamp IS 'an expiration timestamp — once it''s reached a cleanup is permitted';
COMMENT ON COLUMN auto_import_ws_tbl.report IS 'a JSON representation of extra info about job results, to be set once job is completed/failed';

CREATE TABLE import_profile_mlist_bind_tbl
(
    import_profile_id INTEGER NOT NULL,
    mailinglist_id    INTEGER NOT NULL,
    company_id        INTEGER NOT NULL
);
ALTER TABLE import_profile_mlist_bind_tbl ADD CONSTRAINT ip_mlist$ipid_mlid$pk PRIMARY KEY (import_profile_id, mailinglist_id);
ALTER TABLE import_profile_mlist_bind_tbl ADD CONSTRAINT import_profile_mlist_ibfk_1 FOREIGN KEY (import_profile_id) REFERENCES import_profile_tbl (id) ON DELETE CASCADE;
ALTER TABLE import_profile_mlist_bind_tbl ADD CONSTRAINT import_profile_mlist_ibfk_2 FOREIGN KEY (mailinglist_id) REFERENCES mailinglist_tbl (mailinglist_id) ON DELETE CASCADE;
ALTER TABLE import_profile_mlist_bind_tbl ADD CONSTRAINT import_profile_mlist_ibfk_3 FOREIGN KEY (company_id) REFERENCES company_tbl (company_id) ON DELETE CASCADE;
COMMENT ON TABLE import_profile_mlist_bind_tbl IS 'allows n:m relation between import profiles and mailinglists';
COMMENT ON COLUMN import_profile_mlist_bind_tbl.import_profile_id IS 'references import_profile_tbl.id';
COMMENT ON COLUMN import_profile_mlist_bind_tbl.mailinglist_id IS 'references mailinglist_tbl.mailinglist_id';
COMMENT ON COLUMN import_profile_mlist_bind_tbl.company_id IS 'tenant - ID (company_tbl)';

CREATE TABLE mimetype_whitelist_tbl
(
    mimetype      VARCHAR(100),
    description   VARCHAR(100),
    creation_date TIMESTAMP
) TABLESPACE data_accounting;
ALTER TABLE mimetype_whitelist_tbl ADD CONSTRAINT mimewl$mt$NN CHECK (mimetype IS NOT NULL);
ALTER TABLE mimetype_whitelist_tbl ADD CONSTRAINT mimewl$cdate$NN CHECK (creation_date IS NOT NULL);
COMMENT ON TABLE mimetype_whitelist_tbl IS 'Whitelist for Mimetypes supported for uploads';
COMMENT ON COLUMN mimetype_whitelist_tbl.mimetype IS 'Mimetype pattern (can include asterisk)';
COMMENT ON COLUMN mimetype_whitelist_tbl.description IS 'Optional description';
COMMENT ON COLUMN mimetype_whitelist_tbl.creation_date IS 'Timestamp of creating record';

CREATE TABLE license_tbl
(
    name        VARCHAR(100) NOT NULL,
    data        BYTEA,
    change_date TIMESTAMP
);
CREATE UNIQUE INDEX license_tbl$name$uq ON license_tbl (name);
COMMENT ON TABLE license_tbl IS 'License data';
COMMENT ON COLUMN license_tbl.data IS 'License Blob data';
COMMENT ON COLUMN license_tbl.name IS 'License data entry name';
COMMENT ON COLUMN license_tbl.change_date IS 'License data date of creation';

CREATE TABLE permission_tbl
(
    permission_name VARCHAR(64) PRIMARY KEY,
    category        VARCHAR(32) NOT NULL,
    sub_category    VARCHAR(32) DEFAULT NULL,
    sort_order      INTEGER     DEFAULT 0,
    feature_package VARCHAR(32) DEFAULT NULL,
    creation_date   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_accounting;
ALTER TABLE company_permission_tbl ADD CONSTRAINT compperm$id_permname$fk FOREIGN KEY (permission_name) REFERENCES permission_tbl (permission_name);
ALTER TABLE admin_permission_tbl ADD CONSTRAINT adminperm$perm$fk FOREIGN KEY (permission_name) REFERENCES permission_tbl (permission_name) ON DELETE CASCADE;
ALTER TABLE admin_group_permission_tbl ADD CONSTRAINT admingrpperm$perm$fk FOREIGN KEY (permission_name) REFERENCES permission_tbl (permission_name) ON DELETE CASCADE;
COMMENT ON TABLE permission_tbl IS 'All available permissions';
COMMENT ON COLUMN permission_tbl.permission_name IS 'Name of permission, a.k.a security_token';
COMMENT ON COLUMN permission_tbl.category IS 'Category this permission is sorted in';
COMMENT ON COLUMN permission_tbl.sub_category IS 'Sub-Category this permission is sorted in (optional)';
COMMENT ON COLUMN permission_tbl.sort_order IS 'Sorting order of this permission within its category';
COMMENT ON COLUMN permission_tbl.feature_package IS 'Feature package this permission is contained in (optional)';
COMMENT ON COLUMN permission_tbl.creation_date IS 'Date of creation';

CREATE TABLE admin_blacklist_tbl
(
    username VARCHAR(200) PRIMARY KEY
) TABLESPACE data_accounting;
COMMENT ON TABLE admin_blacklist_tbl IS 'Usernames not to be used anymore';
COMMENT ON COLUMN admin_blacklist_tbl.username IS 'Username not to be used anymore';

CREATE TABLE webservice_permission_tbl
(
    username VARCHAR(200) NOT NULL,
    endpoint VARCHAR(200) NOT NULL
);
CREATE INDEX wsperm$username$idx ON webservice_permission_tbl (username);
COMMENT ON TABLE webservice_permission_tbl IS 'Granted webservice endpoints';
COMMENT ON COLUMN webservice_permission_tbl.username IS 'Name of webservice user';
COMMENT ON COLUMN webservice_permission_tbl.endpoint IS 'Name of granted endpoint';

CREATE TABLE webservice_perm_group_tbl
(
    id   INTEGER      NOT NULL PRIMARY KEY,
    name VARCHAR(200) NOT NULL
);
CREATE UNIQUE INDEX wsprmgrp$name$uq ON webservice_perm_group_tbl (name);
COMMENT ON TABLE webservice_perm_group_tbl IS 'List of webservice permission groups';
COMMENT ON COLUMN webservice_perm_group_tbl.id IS 'ID of permission group';
COMMENT ON COLUMN webservice_perm_group_tbl.name IS 'Name of permission group';

INSERT INTO webservice_perm_group_tbl (id, name) VALUES (1, 'recipient_in');
INSERT INTO webservice_perm_group_tbl (id, name) VALUES (2, 'recipient_out');
INSERT INTO webservice_perm_group_tbl (id, name) VALUES (3, 'recipient_in_out_bulk');
INSERT INTO webservice_perm_group_tbl (id, name) VALUES (4, 'content');
INSERT INTO webservice_perm_group_tbl (id, name) VALUES (5, 'web_push');
INSERT INTO webservice_perm_group_tbl (id, name) VALUES (6, 'statistics');
INSERT INTO webservice_perm_group_tbl (id, name) VALUES (7, 'misc');

CREATE TABLE webservice_perm_group_perm_tbl
(
    group_ref INTEGER      NOT NULL,
    endpoint  VARCHAR(200) NOT NULL
);
ALTER TABLE webservice_perm_group_perm_tbl ADD CONSTRAINT wspermgrpperm$grp$fk FOREIGN KEY (group_ref) REFERENCES webservice_perm_group_tbl (id);
CREATE UNIQUE INDEX wspermgrpperm$grpendp$uq ON webservice_perm_group_perm_tbl (group_ref, endpoint);
COMMENT ON TABLE webservice_perm_group_perm_tbl IS 'List of webservice permission groups';
COMMENT ON COLUMN webservice_perm_group_perm_tbl.group_ref IS 'ID of permission group';
COMMENT ON COLUMN webservice_perm_group_perm_tbl.endpoint IS 'Name of granted endpoint';

INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'AddSubscriber');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'UpdateSubscriber');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'SetSubscriberBinding');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'SetSubscriberBindingWithAction');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'StartImport');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'ImportStatus');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'DeleteSubscriber ');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'DeleteSubscriberBulk');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'AddMailinglist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'GetMailinglist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'ListMailinglists');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'UpdateMailinglist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'DeleteMailinglist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'DeleteSubscriberBinding');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'AddBlacklist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'DeleteBlacklist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'CheckBlacklist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'ListTargetgroups');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'AddTargetGroup');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'UpdateTargetGroup');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (1, 'GetBlacklistItems');

INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'FindSubscriber');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'GetSubscriber');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'GetSubscriberBinding');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'ListSubscriberBinding');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'DeleteSubscriber');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'DeleteSubscriberBulk ');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'AddMailinglist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'GetMailinglist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'ListMailinglists');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'UpdateMailinglist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'DeleteMailinglist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'DeleteSubscriberBinding');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'DeleteBlacklist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'ListTargetgroups');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'AddTargetGroup');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'UpdateTargetGroup');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'CheckBlacklist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'GetBlacklistItems');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'StartExport');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'ExportStatus');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'ListSubscribers');

INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (3, 'AddSubscriberBulk');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (3, 'GetSubscriberBulk');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (3, 'UpdateSubscriberBulk');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (3, 'SetSubscriberBindingWithAction');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (3, 'ListSubscriberBindingBulk');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (3, 'DeleteSubscriberBindingBulk');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (3, 'SetSubscriberBindingBulk');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (3, 'AddSubscribers');

INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'AddMailing');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'AddMailingFromTemplate');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'GetMailing');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'ListMailings');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'UpdateMailing');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'DeleteMailing');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'GetMailingContent');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'UpdateMailingContent');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'CopyMailing');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'ListSubscriberMailings');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'AddTemplate');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'GetTemplate');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'ListTemplates');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'UpdateTemplate');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'DeleteTemplate');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'AddContentBlock');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'GetContentBlock');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'DeleteContentBlock');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'UpdateContentBlock');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'ListContentBlockNames');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'ImportMailingContent');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'ListTrackableLinks');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'GetTrackableLinkSettings');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'UpdateTrackableLinkSettings');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'AddAttachment');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'GetAttachment');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'ListAttachment');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'UpdateAttachment');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'DeleteAttachment');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'AddMailingImage');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'ListMailingsInMailinglist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'ListContentBlocks');

INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (5, 'SetPushNotificationPlanDate');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (5, 'SendPushNotificiation');

INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (6, 'MailingSummaryStatisticJob');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (6, 'MailingSummaryStatisticResult');

INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (7, 'SendMailing');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (7, 'SendServiceMail');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (7, 'GetMailingStatus');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (7, 'GetFullviewUrl');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (7, 'DecryptLinkData');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (7, 'CreateDataSource');

CREATE TABLE webservice_user_group_tbl
(
    username  VARCHAR(200) NOT NULL,
    group_ref INTEGER      NOT NULL
);
ALTER TABLE webservice_user_group_tbl ADD CONSTRAINT wsusergrp$grp$fk FOREIGN KEY (group_ref) REFERENCES webservice_perm_group_tbl (id);
CREATE UNIQUE INDEX wsusergrp$usrgrp$uq ON webservice_user_group_tbl (username, group_ref);
COMMENT ON TABLE webservice_user_group_tbl IS 'List of webservice permission groups';
COMMENT ON COLUMN webservice_user_group_tbl.username IS 'Name of webservice user';
COMMENT ON COLUMN webservice_user_group_tbl.group_ref IS 'ID of permission group';

CREATE TABLE webservice_permissions_tbl
(
    endpoint VARCHAR(200) NOT NULL PRIMARY KEY,
    category VARCHAR(200)
);
COMMENT ON TABLE webservice_permissions_tbl IS 'Available webservice permissions and categories';
COMMENT ON COLUMN webservice_permissions_tbl.endpoint IS 'Name of granted endpoint';
COMMENT ON COLUMN webservice_permissions_tbl.category IS 'Category of endpoint (like Subscribers oder Target groups, null if uncategorized)';

CREATE TABLE ws_login_track_tbl
(
    login_track_id SERIAL,
    ip_address     VARCHAR(50),
    creation_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    login_status   INTEGER,
    username       VARCHAR(200)
) TABLESPACE data_emmaux;
CREATE INDEX wslogtrck$ip_cdate_stat$idx ON ws_login_track_tbl (ip_address, creation_date, login_status) TABLESPACE data_emmaux_idx;
CREATE INDEX wslogtrck$user$idx ON ws_login_track_tbl (username) TABLESPACE data_emmaux_idx;
COMMENT ON TABLE ws_login_track_tbl IS '[secret_data] any WS-login-request, successful or not, is stored here (for a certain time)';
COMMENT ON COLUMN ws_login_track_tbl.login_track_id IS 'unique ID, use ws_login_track_tbl_seq';
COMMENT ON COLUMN ws_login_track_tbl.ip_address IS '[secret_data] address where login-request came from';
COMMENT ON COLUMN ws_login_track_tbl.creation_date IS 'login-request timestamp';
COMMENT ON COLUMN ws_login_track_tbl.login_status IS '10 = successful, 20 = failed, 30 = unlock blocked IP, 40 = successful but while IP is locked';
COMMENT ON COLUMN ws_login_track_tbl.username IS '[secret_data] WS-user name used in request';

CREATE TABLE birtreport_recipient_tbl
(
    birtreport_id INTEGER,
    email         VARCHAR(100)
);
ALTER TABLE birtreport_recipient_tbl ADD CONSTRAINT birtrecp$birtrep$fk FOREIGN KEY (birtreport_id) REFERENCES birtreport_tbl (report_id) ON DELETE CASCADE;
COMMENT ON TABLE birtreport_recipient_tbl IS 'Birt report recipient adresses';
COMMENT ON COLUMN birtreport_recipient_tbl.birtreport_id IS 'Multiple entries for referenced report_id from birtreport_tbl';
COMMENT ON COLUMN birtreport_recipient_tbl.email IS 'Emailadress of report recipient';

CREATE TABLE startup_job_tbl
(
    id            SERIAL PRIMARY KEY,
    classname     VARCHAR(1000) NOT NULL,
    version       VARCHAR(20)   NOT NULL,
    company_id    INTEGER       NOT NULL,
    enabled       SMALLINT      NOT NULL,
    state         SMALLINT      NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description   VARCHAR(1000)
) TABLESPACE data_accounting;
COMMENT ON TABLE startup_job_tbl IS 'Stores information about job to be run on context startup';
COMMENT ON COLUMN startup_job_tbl.id IS 'Job ID';
COMMENT ON COLUMN startup_job_tbl.classname IS 'Name of job class';
COMMENT ON COLUMN startup_job_tbl.version IS 'EMM version';
COMMENT ON COLUMN startup_job_tbl.company_id IS 'references company (company_tbl) or 0 if job is independent from company';
COMMENT ON COLUMN startup_job_tbl.enabled IS '1 = job is enabled, 0 = job is disabled, others: unused (=disabled)';
COMMENT ON COLUMN startup_job_tbl.state IS 'State of job (see enum class com.agnitas.startuplistener.common.JobState)';
COMMENT ON COLUMN startup_job_tbl.creation_date IS 'entry creation date';
COMMENT ON COLUMN startup_job_tbl.change_date IS 'entry last change';
COMMENT ON COLUMN startup_job_tbl.description IS 'Optional description';

INSERT INTO startup_job_tbl (classname, version, company_id, enabled, state)
VALUES ('com.agnitas.startuplistener.api.ReferenceTableIdColumnStartupJob', '24.04.272', 0, 1, 0);

CREATE TABLE layout_tbl
(
    company_id INTEGER DEFAULT 0,
    item_name  VARCHAR(32) NOT NULL,
    data       BYTEA
);
COMMENT ON TABLE layout_tbl IS 'Layout data like favicon and logo images';
COMMENT ON COLUMN layout_tbl.company_id IS 'Client id, default value 0 = for all';
COMMENT ON COLUMN layout_tbl.item_name IS 'Item name like favicon or logo';
COMMENT ON COLUMN layout_tbl.data IS 'Layout data';

CREATE TABLE admin_to_group_tbl
(
    admin_id       INTEGER NOT NULL,
    admin_group_id INTEGER NOT NULL
) TABLESPACE data_accounting;
ALTER TABLE admin_to_group_tbl ADD CONSTRAINT admintogrp$admin$fk FOREIGN KEY (admin_id) REFERENCES admin_tbl (admin_id) ON DELETE CASCADE;
ALTER TABLE admin_to_group_tbl ADD CONSTRAINT admintogrp$admingrpid$fk FOREIGN KEY (admin_group_id) REFERENCES admin_group_tbl (admin_group_id) ON DELETE CASCADE;
COMMENT ON TABLE admin_to_group_tbl IS 'Stores group references of admins';
COMMENT ON COLUMN admin_to_group_tbl.admin_id IS 'Reference id to admin_tbl';
COMMENT ON COLUMN admin_to_group_tbl.admin_group_id IS 'Reference id to admin_group_tbl';

CREATE TABLE group_to_group_tbl
(
    admin_group_id           INTEGER NOT NULL,
    member_of_admin_group_id INTEGER NOT NULL
) TABLESPACE data_accounting;
ALTER TABLE group_to_group_tbl ADD CONSTRAINT grptogrp$grpid$fk FOREIGN KEY (admin_group_id) REFERENCES admin_group_tbl (admin_group_id) ON DELETE CASCADE;
ALTER TABLE group_to_group_tbl ADD CONSTRAINT grptogrp$mbrgrpid$fk FOREIGN KEY (member_of_admin_group_id) REFERENCES admin_group_tbl (admin_group_id) ON DELETE CASCADE;
COMMENT ON TABLE group_to_group_tbl IS 'Stores group references of groups';
COMMENT ON COLUMN group_to_group_tbl.admin_group_id IS 'Reference id to admin_group_tbl';
COMMENT ON COLUMN group_to_group_tbl.member_of_admin_group_id IS 'Reference id to admin_group_tbl, which this group is member of';

CREATE TABLE release_log_tbl
(
    host_name         VARCHAR(64),
    application_name  VARCHAR(64),
    version_number    VARCHAR(15),
    startup_timestamp TIMESTAMP,
    build_time        TIMESTAMP,
    build_host        VARCHAR(100),
    build_user        VARCHAR(100)
);
COMMENT ON TABLE release_log_tbl IS 'Stores information on first startups of new versions and rollbacks and rollforwards';
COMMENT ON COLUMN release_log_tbl.host_name IS 'Hostname this application was started on';
COMMENT ON COLUMN release_log_tbl.application_name IS 'Application that was started';
COMMENT ON COLUMN release_log_tbl.version_number IS 'Version of application that was started';
COMMENT ON COLUMN release_log_tbl.startup_timestamp IS 'Time this version was started';
COMMENT ON COLUMN release_log_tbl.build_time IS 'Creation time of this build';
COMMENT ON COLUMN release_log_tbl.build_host IS 'Host this build was created on';
COMMENT ON COLUMN release_log_tbl.build_user IS 'User which executed the build and deployment';

CREATE TABLE ref_import_action_tbl
(
    ref_importaction_id SERIAL PRIMARY KEY,
    company_id          INTEGER     NOT NULL,
    name                VARCHAR(128),
    type                VARCHAR(32) NOT NULL,
    action              TEXT        NOT NULL,
    creation_date       TIMESTAMP,
    change_date         TIMESTAMP
);
COMMENT ON TABLE ref_import_action_tbl IS 'Pre import actions for reference tables';
COMMENT ON COLUMN ref_import_action_tbl.ref_importaction_id IS 'Unique ID of pre import action, use ref_import_action_tbl_seq';
COMMENT ON COLUMN ref_import_action_tbl.company_id IS 'ClientID of the owner of this pre import action';
COMMENT ON COLUMN ref_import_action_tbl.name IS 'Displayname of this pre import action';
COMMENT ON COLUMN ref_import_action_tbl.type IS 'Type of this pre import action. Mostly SQL';
COMMENT ON COLUMN ref_import_action_tbl.action IS 'Content of this pre import action. Mostly SQL script';
COMMENT ON COLUMN ref_import_action_tbl.creation_date IS 'Creation date of this pre import action';
COMMENT ON COLUMN ref_import_action_tbl.change_date IS 'Change date of this pre import action';

CREATE TABLE feature_cleanup_tbl
(
    company_id        INTEGER,
    feature           VARCHAR(100),
    deactivation_date TIMESTAMP,
    cleanup_status    INTEGER
);
COMMENT ON TABLE feature_cleanup_tbl IS 'Storage of data of deactivated features for later DbCleaner actions';
COMMENT ON COLUMN feature_cleanup_tbl.company_id IS 'Client id a feature was deactivated for';
COMMENT ON COLUMN feature_cleanup_tbl.feature IS 'Deactivated feature name';
COMMENT ON COLUMN feature_cleanup_tbl.deactivation_date IS 'Date of deactivation';
COMMENT ON COLUMN feature_cleanup_tbl.cleanup_status IS 'State of deactivation: 0 = clean up to do, 1 = cleanup is finished, 2 = no more cleanup (maybe reactivated)';

CREATE TABLE permission_category_tbl
(
    category_name VARCHAR(32) PRIMARY KEY,
    sort_order    INTEGER   DEFAULT 0,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_accounting;
COMMENT ON TABLE permission_category_tbl IS 'All available permission categories';
COMMENT ON COLUMN permission_category_tbl.category_name IS 'Technical name of this category';
COMMENT ON COLUMN permission_category_tbl.sort_order IS 'Sort order in GUI for this category';
COMMENT ON COLUMN permission_category_tbl.creation_date IS 'Date of creation of this category';

INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('General', 1);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Mailing', 2);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Subscriber-Editor', 5);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('ImportExport', 6);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Target-Groups', 7);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Statistics', 8);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Forms', 9);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Actions', 10);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('Administration', 11);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('NegativePermissions', 12);
INSERT INTO permission_category_tbl (category_name, sort_order) VALUES ('System', 13);

CREATE TABLE permission_subcategory_tbl
(
    category_name    VARCHAR(32),
    subcategory_name VARCHAR(32),
    sort_order       INTEGER   DEFAULT 0,
    creation_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (category_name, subcategory_name)
) TABLESPACE data_accounting;
COMMENT ON TABLE permission_subcategory_tbl IS 'All available permission subcategories';
COMMENT ON COLUMN permission_subcategory_tbl.category_name IS 'Technical name of the parent category of this subcategory';
COMMENT ON COLUMN permission_subcategory_tbl.category_name IS 'Technical name of this subcategory';
COMMENT ON COLUMN permission_subcategory_tbl.sort_order IS 'Sort order in GUI for this subcategory';
COMMENT ON COLUMN permission_subcategory_tbl.creation_date IS 'Date of creation of this subcategory';

INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Mailing', 'Settings', 1, CURRENT_TIMESTAMP);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Mailing', 'mailing.searchContent', 2, CURRENT_TIMESTAMP);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Mailing', 'Delivery', 3, CURRENT_TIMESTAMP);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Mailing', 'settings.FormsOfAddress', 4, CURRENT_TIMESTAMP);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Mailing', 'Campaigns', 5, CURRENT_TIMESTAMP);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Mailing', 'Template', 6, CURRENT_TIMESTAMP);

INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Subscriber-Editor', 'default.extensions', 1, CURRENT_TIMESTAMP);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Subscriber-Editor', 'recipient.fields', 2, CURRENT_TIMESTAMP);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Subscriber-Editor', 'Mailinglist', 3, CURRENT_TIMESTAMP);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Subscriber-Editor', 'recipient.Blacklist', 4, CURRENT_TIMESTAMP);

INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('ImportExport', 'import.mode', 1, CURRENT_TIMESTAMP);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('ImportExport', 'import.settings', 2, CURRENT_TIMESTAMP);

INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Statistics', 'General', 1, CURRENT_TIMESTAMP);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Statistics', 'Mailing', 2, CURRENT_TIMESTAMP);

INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Administration', 'settings.Admin', 1, CURRENT_TIMESTAMP);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Administration', 'settings.Usergroups', 2, CURRENT_TIMESTAMP);
INSERT INTO permission_subcategory_tbl (category_name, subcategory_name, sort_order, creation_date) VALUES ('Administration', 'settings.Mailloop', 4, CURRENT_TIMESTAMP);

ALTER TABLE permission_tbl ADD CONSTRAINT perm$permcat$fk FOREIGN KEY (category) REFERENCES permission_category_tbl (category_name) ON DELETE CASCADE;
ALTER TABLE permission_subcategory_tbl ADD CONSTRAINT permsubcat$permcat$fk FOREIGN KEY (category_name) REFERENCES permission_category_tbl (category_name) ON DELETE CASCADE;

CREATE TABLE import_profile_mediatype_tbl
(
    import_profile_id INTEGER NOT NULL,
    mediatype         INTEGER NOT NULL
);
ALTER TABLE import_profile_mediatype_tbl ADD CONSTRAINT impmedia$profid$pk PRIMARY KEY (import_profile_id, mediatype);
ALTER TABLE import_profile_mediatype_tbl ADD CONSTRAINT import_profile_media_fk FOREIGN KEY (import_profile_id) REFERENCES import_profile_tbl (id) ON DELETE CASCADE;
COMMENT ON TABLE import_profile_mediatype_tbl IS 'Mediatypes used by an import';
COMMENT ON COLUMN import_profile_mediatype_tbl.import_profile_id IS 'references import_profile_tbl.id';
COMMENT ON COLUMN import_profile_mediatype_tbl.mediatype IS '0=EMAIL,2=POST,4=SMS';

CREATE TABLE target_ref_mailing_tbl
(
    target_ref  INTEGER NOT NULL,
    company_ref INTEGER NOT NULL,
    mailing_ref INTEGER NOT NULL
);
COMMENT ON TABLE target_ref_mailing_tbl IS 'Mailing referenced by target groups';
COMMENT ON COLUMN target_ref_mailing_tbl.target_ref IS 'Target group ID';
COMMENT ON COLUMN target_ref_mailing_tbl.company_ref IS 'Company ID';
COMMENT ON COLUMN target_ref_mailing_tbl.mailing_ref IS 'ID of referenced mailing';

CREATE TABLE target_ref_link_tbl
(
    target_ref  INTEGER NOT NULL,
    company_ref INTEGER NOT NULL,
    link_ref    INTEGER NOT NULL
);
COMMENT ON TABLE target_ref_link_tbl IS 'Link referenced by target groups';
COMMENT ON COLUMN target_ref_link_tbl.target_ref IS 'Target group ID';
COMMENT ON COLUMN target_ref_link_tbl.company_ref IS 'Company ID';
COMMENT ON COLUMN target_ref_link_tbl.link_ref IS 'ID of referenced link';

CREATE TABLE target_ref_autoimport_tbl
(
    target_ref     INTEGER NOT NULL,
    company_ref    INTEGER NOT NULL,
    autoimport_ref INTEGER NOT NULL
);
COMMENT ON TABLE target_ref_autoimport_tbl IS 'Auto imports referenced by target groups';
COMMENT ON COLUMN target_ref_autoimport_tbl.target_ref IS 'Target group ID';
COMMENT ON COLUMN target_ref_autoimport_tbl.company_ref IS 'Company ID';
COMMENT ON COLUMN target_ref_autoimport_tbl.autoimport_ref IS 'ID of referenced auto import';

CREATE TABLE target_ref_profilefield_tbl
(
    target_ref  INTEGER      NOT NULL,
    company_ref INTEGER      NOT NULL,
    name        VARCHAR(200) NOT NULL
);
COMMENT ON TABLE target_ref_profilefield_tbl IS 'Profile fields referenced by target groups';
COMMENT ON COLUMN target_ref_profilefield_tbl.target_ref IS 'Target group ID';
COMMENT ON COLUMN target_ref_profilefield_tbl.company_ref IS 'Company ID';
COMMENT ON COLUMN target_ref_profilefield_tbl.name IS 'Name of referenced profile field';

CREATE TABLE webservice_endpoint_cost_tbl
(
    company_ref INTEGER      NOT NULL,
    endpoint    VARCHAR(200) NOT NULL,
    costs       INTEGER      NOT NULL
);
COMMENT ON TABLE webservice_endpoint_cost_tbl IS 'Costs of invocation of endpoint';
COMMENT ON COLUMN webservice_endpoint_cost_tbl.company_ref IS 'Company ID';
COMMENT ON COLUMN webservice_endpoint_cost_tbl.endpoint IS 'Name of endpoint';
COMMENT ON COLUMN webservice_endpoint_cost_tbl.costs IS 'Costs of invocation';

CREATE TABLE webhook_url_tbl
(
    company_ref INTEGER       NOT NULL,
    event_type  SMALLINT      NOT NULL,
    webhook_url VARCHAR(1000) NOT NULL,
    PRIMARY KEY (company_ref, event_type),
    CONSTRAINT webhook_url$cid$fk FOREIGN KEY (company_ref) REFERENCES company_tbl (company_id)
);
COMMENT ON TABLE webhook_url_tbl IS 'Configured webhook URLs';
COMMENT ON COLUMN webhook_url_tbl.company_ref IS 'Company ID';
COMMENT ON COLUMN webhook_url_tbl.event_type IS 'Event type (see WebhookEventType enum for encoding)';
COMMENT ON COLUMN webhook_url_tbl.webhook_url IS 'Webhook URL';

CREATE TABLE webhook_message_tbl
(
    message_id      SERIAL PRIMARY KEY,
    company_ref     INTEGER   NOT NULL,
    event_type      SMALLINT  NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    send_timestamp  TIMESTAMP NOT NULL,
    retry_count     SMALLINT  NOT NULL,
    status          SMALLINT  NOT NULL,
    payload         TEXT      NOT NULL,
    status_note     VARCHAR(1000),
    CONSTRAINT webhook_msg$cid$fk FOREIGN KEY (company_ref) REFERENCES company_tbl (company_id)
) TABLESPACE data_temp;
COMMENT ON TABLE webhook_message_tbl IS 'Webhook messages to send';
COMMENT ON COLUMN webhook_message_tbl.message_id IS 'Unique message ID';
COMMENT ON COLUMN webhook_message_tbl.company_ref IS 'Company ID';
COMMENT ON COLUMN webhook_message_tbl.event_type IS 'Event type (see WebserviceEventType enum for encoding)';
COMMENT ON COLUMN webhook_message_tbl.event_timestamp IS 'Timestamp of event';
COMMENT ON COLUMN webhook_message_tbl.send_timestamp IS 'Send time of message';
COMMENT ON COLUMN webhook_message_tbl.retry_count IS 'Number of current send attempts';
COMMENT ON COLUMN webhook_message_tbl.status IS 'Status of message';
COMMENT ON COLUMN webhook_message_tbl.payload IS 'Payload of message';
COMMENT ON COLUMN webhook_message_tbl.status_note IS 'Note on current status';

CREATE TABLE webhook_process_timestamp_tbl
(
    company_ref  INTEGER   NOT NULL,
    process_type SMALLINT  NOT NULL,
    timestamp    TIMESTAMP NOT NULL,
    PRIMARY KEY (company_ref, process_type),
    CONSTRAINT webhook_prcts$cid$fk FOREIGN KEY (company_ref) REFERENCES company_tbl (company_id)
);
COMMENT ON TABLE webhook_process_timestamp_tbl IS 'Timestamps for webhook processing';
COMMENT ON COLUMN webhook_process_timestamp_tbl.company_ref IS 'Company ID';
COMMENT ON COLUMN webhook_process_timestamp_tbl.process_type IS 'Type of process (1=mail delivery data, 2=bounces)';
COMMENT ON COLUMN webhook_process_timestamp_tbl.timestamp IS 'Timestamp of last processing';

CREATE TABLE webhook_profile_field_tbl
(
    company_ref   INTEGER      NOT NULL,
    event_type    SMALLINT     NOT NULL,
    profile_field VARCHAR(100) NOT NULL,
    CONSTRAINT webhook_prfld$cid$fk FOREIGN KEY (company_ref) REFERENCES company_tbl (company_id)
);
COMMENT ON TABLE webhook_profile_field_tbl IS 'List of profile fields to include in webhook messages';
COMMENT ON COLUMN webhook_profile_field_tbl.company_ref IS 'Company ID';
COMMENT ON COLUMN webhook_profile_field_tbl.event_type IS 'Event type (see WebserviceEventType enum for encoding)';
COMMENT ON COLUMN webhook_profile_field_tbl.profile_field IS 'Name of profile field';

CREATE TABLE webhook_backend_data_tbl
(
    id              SERIAL PRIMARY KEY,
    creation_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    event_timestamp TIMESTAMP                           NOT NULL,
    event_type      SMALLINT                            NOT NULL,
    company_id      INTEGER                             NOT NULL,
    mailing_id      INTEGER,
    recipient_id    INTEGER,
    CONSTRAINT webhook_backdata$cid$fk FOREIGN KEY (company_id) REFERENCES company_tbl (company_id)
) TABLESPACE data_temp;
COMMENT ON TABLE webhook_backend_data_tbl IS 'Raw backend data for webhook messages';
COMMENT ON COLUMN webhook_backend_data_tbl.id IS 'ID of record';
COMMENT ON COLUMN webhook_backend_data_tbl.creation_date IS 'Creation timestamp of record';
COMMENT ON COLUMN webhook_backend_data_tbl.event_timestamp IS 'Timestamp of event';
COMMENT ON COLUMN webhook_backend_data_tbl.event_type IS 'Event type (see WebserviceEventType enum for encoding)';
COMMENT ON COLUMN webhook_backend_data_tbl.company_id IS 'Company ID';
COMMENT ON COLUMN webhook_backend_data_tbl.mailing_id IS 'Mailing ID';
COMMENT ON COLUMN webhook_backend_data_tbl.recipient_id IS 'Recipient ID';

CREATE TABLE actop_unsubscribe_mlist_tbl
(
    action_operation_id INTEGER NOT NULL,
    mailinglist_id      INTEGER NOT NULL
);
ALTER TABLE actop_unsubscribe_mlist_tbl ADD CONSTRAINT actop_unsc_ml$actopid_mlid$pk PRIMARY KEY (action_operation_id, mailinglist_id);
ALTER TABLE actop_unsubscribe_mlist_tbl ADD CONSTRAINT actop_unsc_ml$mlid$fk FOREIGN KEY (mailinglist_id) REFERENCES mailinglist_tbl (mailinglist_id) ON DELETE CASCADE;
COMMENT ON TABLE actop_unsubscribe_mlist_tbl IS 'many to many table for storing selected mailinglists of unsubscribe action operation';
COMMENT ON COLUMN actop_unsubscribe_mlist_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_unsubscribe_mlist_tbl.mailinglist_id IS 'references mailinglist_tbl.mailinglist_id';

CREATE TABLE bounce_rule_tbl
(
    company_id    INTEGER,
    rid           INTEGER,
    definition    TEXT,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_bounce;
ALTER TABLE bounce_rule_tbl ADD CONSTRAINT bncrule$coidrid$pk PRIMARY KEY (company_id, rid);
ALTER TABLE bounce_rule_tbl ADD CONSTRAINT bncrule$coid$nn CHECK (company_id IS NOT NULL);
ALTER TABLE bounce_rule_tbl ADD CONSTRAINT bncrule$rid$nn CHECK (rid IS NOT NULL);
ALTER TABLE bounce_rule_tbl ADD CONSTRAINT bncrule$def$nn CHECK (definition IS NOT NULL);
COMMENT ON TABLE bounce_rule_tbl IS 'Rule set for delayed bounces, replaces ~/lib/bav.rule';
COMMENT ON COLUMN bounce_rule_tbl.company_id IS 'reference to company_tbl.company_id, if 0 and rid=0, then this is a global setting';
COMMENT ON COLUMN bounce_rule_tbl.rid IS 'reference to mailloop_tbl.rid, if 0 and company_id=0, then this is a global setting';
COMMENT ON COLUMN bounce_rule_tbl.definition IS 'the rule definition as a json object';
COMMENT ON COLUMN bounce_rule_tbl.creation_date IS 'timestamp of creation';
COMMENT ON COLUMN bounce_rule_tbl.change_date IS 'timestamp of last change';

CREATE TABLE bounce_config_tbl
(
    company_id    INTEGER,
    rid           INTEGER,
    name          VARCHAR(100),
    value         TEXT,
    description   VARCHAR(500),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE data_bounce;
ALTER TABLE bounce_config_tbl ADD CONSTRAINT bnccfg$coidridname$pk PRIMARY KEY (company_id, rid, name);
ALTER TABLE bounce_config_tbl ADD CONSTRAINT bnccfg$coid$nn CHECK (company_id IS NOT NULL);
ALTER TABLE bounce_config_tbl ADD CONSTRAINT bnccfg$rid$nn CHECK (rid IS NOT NULL);
ALTER TABLE bounce_config_tbl ADD CONSTRAINT bnccfg$name$nn CHECK (name IS NOT NULL);
ALTER TABLE bounce_config_tbl ADD CONSTRAINT bnccfg$value$nn CHECK (value IS NOT NULL);
COMMENT ON TABLE bounce_config_tbl IS 'Configuration for bouncemanagement';
COMMENT ON COLUMN bounce_config_tbl.company_id IS 'reference to company_tbl.company_id, if 0 and rid=0, then this is a global setting';
COMMENT ON COLUMN bounce_config_tbl.rid IS 'reference to mailloop_tbl.rid, if 0 and company_id=0, then this is a global setting';
COMMENT ON COLUMN bounce_config_tbl.name IS 'the name of the configuration entry';
COMMENT ON COLUMN bounce_config_tbl.value IS 'the value as a json object';
COMMENT ON COLUMN bounce_config_tbl.description IS 'optional description for this value';
COMMENT ON COLUMN bounce_config_tbl.creation_date IS 'timestamp of creation';
COMMENT ON COLUMN bounce_config_tbl.change_date IS 'timestamp of last change';

CREATE TABLE bounce_ar_lastsent_tbl
(
    rid         INTEGER,
    customer_id INTEGER,
    lastsent    TIMESTAMP
) TABLESPACE data_bounce;
ALTER TABLE bounce_ar_lastsent_tbl ADD CONSTRAINT bncarl$ridcid$pk PRIMARY KEY (rid, customer_id);
ALTER TABLE bounce_ar_lastsent_tbl ADD CONSTRAINT bncarl$rid$nn CHECK (rid IS NOT NULL);
ALTER TABLE bounce_ar_lastsent_tbl ADD CONSTRAINT bncarl$cid$nn CHECK (customer_id IS NOT NULL);
ALTER TABLE bounce_ar_lastsent_tbl ADD CONSTRAINT bncarl$lastsent$nn CHECK (lastsent IS NOT NULL);
COMMENT ON TABLE bounce_ar_lastsent_tbl IS 'Keep track of last sent timestamp when an autoresponder mail had been sent to a customer';
COMMENT ON COLUMN bounce_ar_lastsent_tbl.rid IS 'reference to mailloop_tbl.rid';
COMMENT ON COLUMN bounce_ar_lastsent_tbl.customer_id IS 'the customer_id we keep track last sent autoresponder mail';
COMMENT ON COLUMN bounce_ar_lastsent_tbl.lastsent IS 'the timestamp of the last sent autoresponder mail';

CREATE TABLE actop_unsubscribe_customer_tbl
(
    action_operation_id       INTEGER  PRIMARY KEY,
    all_mailinglists_selected SMALLINT NOT NULL
);
ALTER TABLE actop_unsubscribe_customer_tbl ADD CONSTRAINT actop_uns_c$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_tbl (action_operation_id) ON DELETE CASCADE;
COMMENT ON TABLE actop_unsubscribe_customer_tbl IS 'details for unsubscribe action steps';
COMMENT ON COLUMN actop_unsubscribe_customer_tbl.action_operation_id IS 'references actop_tbl.action_operation_id';
COMMENT ON COLUMN actop_unsubscribe_customer_tbl.all_mailinglists_selected IS '1 = unsubscribe from all mailinglists, 0 = user can select mailinglists to unsubscribe';

ALTER TABLE actop_unsubscribe_mlist_tbl ADD CONSTRAINT actop_unsc_ml$actopid$fk FOREIGN KEY (action_operation_id) REFERENCES actop_unsubscribe_customer_tbl (action_operation_id) ON DELETE CASCADE;

CREATE TABLE restful_quota_tbl
(
    admin_id INTEGER      NOT NULL,
    quota    VARCHAR(100) NOT NULL
);
COMMENT ON TABLE restful_quota_tbl IS 'Stores admin-specific Restful quotas';
COMMENT ON COLUMN restful_quota_tbl.admin_id IS 'Reference id to admin_tbl';
COMMENT ON COLUMN restful_quota_tbl.quota IS 'Quota specificaions';

CREATE TABLE restful_api_costs_tbl
(
    company_id INTEGER      NOT NULL,
    name       VARCHAR(100) NOT NULL,
    costs      INTEGER      NOT NULL
);
COMMENT ON TABLE restful_api_costs_tbl IS 'Stores invocation costs for Restful API';
COMMENT ON COLUMN restful_api_costs_tbl.company_id IS 'Reference to company table';
COMMENT ON COLUMN restful_api_costs_tbl.name IS 'Name of Restful service';
COMMENT ON COLUMN restful_api_costs_tbl.costs IS 'Costs for invocation';

CREATE TABLE export_column_mapping_tbl
(
    id               SERIAL PRIMARY KEY,
    export_predef_id INTEGER NOT NULL,
    db_column        VARCHAR(255),
    file_column      VARCHAR(255),
    default_value    VARCHAR(255),
    encrypted        INTEGER DEFAULT 0
) TABLESPACE data_emmaux;
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$export_col_map_tbl FOREIGN KEY (id) REFERENCES export_column_mapping_tbl (id);
ALTER TABLE export_column_mapping_tbl ADD CONSTRAINT exportcolmap$exppredef$fk FOREIGN KEY (export_predef_id) REFERENCES export_predef_tbl (export_predef_id);
COMMENT ON TABLE export_column_mapping_tbl IS 'settings saving a mapping on database-columns to file-columns in an export-profile';
COMMENT ON COLUMN export_column_mapping_tbl.id IS 'unique ID, use export_column_mapping_tbl_seq';
COMMENT ON COLUMN export_column_mapping_tbl.export_predef_id IS 'references export_predef_tbl';
COMMENT ON COLUMN export_column_mapping_tbl.file_column IS 'matching column in file';
COMMENT ON COLUMN export_column_mapping_tbl.db_column IS 'matching column in database';
COMMENT ON COLUMN export_column_mapping_tbl.default_value IS 'default (for this column and this export)';
COMMENT ON COLUMN export_column_mapping_tbl.encrypted IS '1 = yes';

CREATE TABLE http_response_headers_tbl
(
    header_name             VARCHAR(100)  NOT NULL,
    header_value            VARCHAR(4000) NOT NULL,
    overwrite               SMALLINT      NOT NULL,
    app_types               VARCHAR(100)  NOT NULL,
    company_ref             INTEGER,
    remote_hostname_pattern VARCHAR(1000),
    used_for                VARCHAR(100),
    query_pattern           VARCHAR(1000),
    description             VARCHAR(1000)
) TABLESPACE data_emmaux;
COMMENT ON TABLE http_response_headers_tbl IS 'List of HTTP response headers to be added to responses';
COMMENT ON COLUMN http_response_headers_tbl.header_name IS 'Name of HTTP response header';
COMMENT ON COLUMN http_response_headers_tbl.header_value IS 'Value of HTTP response header';
COMMENT ON COLUMN http_response_headers_tbl.overwrite IS 'If true, header is added even if header of same name already exists in response';
COMMENT ON COLUMN http_response_headers_tbl.app_types IS 'Comma/semicolon-separated lists of application types, for that this header is emitted (emm, rdir, ws, birt)';
COMMENT ON COLUMN http_response_headers_tbl.company_ref IS 'Company ID. Use 0 for all companies. Not esed when used_for is set to ''filter''';
COMMENT ON COLUMN http_response_headers_tbl.remote_hostname_pattern IS 'Regular expression for requesting host. Set to null to match all hosts.';
COMMENT ON COLUMN http_response_headers_tbl.used_for IS 'Whewn to apply this settings (currently allowed: filter, resource). See class com.agnitas.emm.responseheaders.common.UsedFor';
COMMENT ON COLUMN http_response_headers_tbl.query_pattern IS 'Regular expression for query (if null: not checked)';
COMMENT ON COLUMN http_response_headers_tbl.description IS 'Comment on this configuration';
INSERT INTO http_response_headers_tbl (header_name, header_value, overwrite, app_types) VALUES ('Content-Security-Policy', 'frame-ancestors ''self''', 1, 'emm');
INSERT INTO http_response_headers_tbl (header_name, header_value, overwrite, app_types) VALUES ('Cache-control', 'no-store', 0, 'emm,rdir,ws,birt');
INSERT INTO http_response_headers_tbl (header_name, header_value, overwrite, app_types) VALUES ('Pragma', 'no-cache', 0, 'emm,rdir,ws,birt');

CREATE TABLE mailing_import_lock_tbl
(
    maildrop_status_id INTEGER NOT NULL,
    auto_import_id     INTEGER NOT NULL,
    status_ok          INTEGER,
    change_date        TIMESTAMP,
    mailing_id         INTEGER NOT NULL,
    PRIMARY KEY (mailing_id, auto_import_id)
);
COMMENT ON TABLE mailing_import_lock_tbl IS 'List of AutoImports that block the delivery of mailings';
COMMENT ON COLUMN mailing_import_lock_tbl.maildrop_status_id IS 'Reference id for maildrop_status_tbl';
COMMENT ON COLUMN mailing_import_lock_tbl.auto_import_id IS 'Reference id for auto_import_tbl';
COMMENT ON COLUMN mailing_import_lock_tbl.status_ok IS 'Status of referenced auto_import_id in auto_import_tbl. (0 = Error, 1 = OK)';
COMMENT ON COLUMN mailing_import_lock_tbl.change_date IS 'Date of last status change';

CREATE TABLE migration_tbl
(
    version_number   VARCHAR(15),
    updating_user    VARCHAR(64),
    update_timestamp DATE
);
COMMENT ON TABLE migration_tbl IS 'stores execution times of migration statements';
COMMENT ON COLUMN migration_tbl.version_number IS '(EMM-) version of update - script';
COMMENT ON COLUMN migration_tbl.updating_user IS 'executing (DB-) user';
COMMENT ON COLUMN migration_tbl.update_timestamp IS 'execution timestamp';

CREATE TABLE company_status_desc_tbl
(
    status      VARCHAR(20) PRIMARY KEY,
    description VARCHAR(32)
);
COMMENT ON TABLE company_status_desc_tbl IS 'Company status descriptions';
COMMENT ON COLUMN company_status_desc_tbl.status IS 'Company status name used in company_tbl.status';
COMMENT ON COLUMN company_status_desc_tbl.description IS 'Company status description for humans';
INSERT INTO company_status_desc_tbl (status) VALUES ('active');
INSERT INTO company_status_desc_tbl (status) VALUES ('locked');
INSERT INTO company_status_desc_tbl (status) VALUES ('todelete');
INSERT INTO company_status_desc_tbl (status) VALUES ('toreset');
INSERT INTO company_status_desc_tbl (status) VALUES ('deleted');
INSERT INTO company_status_desc_tbl (status) VALUES ('deletion in progress');
ALTER TABLE company_tbl ADD CONSTRAINT comp$status$fk FOREIGN KEY (status) REFERENCES company_status_desc_tbl (status);

CREATE TABLE restful_usage_log_tbl
(
    timestamp       TIMESTAMP     NOT NULL,
    endpoint        VARCHAR(1000) NOT NULL,
    description     VARCHAR(4000),
    request_method  VARCHAR(7)    NOT NULL,
    company_id      INTEGER       NOT NULL,
    username        VARCHAR(200)  NOT NULL,
    host_name       VARCHAR(64),
    supervisor_name VARCHAR(100)
);
CREATE INDEX restus$cidtsusr$idx ON restful_usage_log_tbl (company_id, timestamp, username);
COMMENT ON TABLE restful_usage_log_tbl IS 'Stores activity information of restful users';
COMMENT ON COLUMN restful_usage_log_tbl.timestamp IS 'Timestamp when restful service was invoked';
COMMENT ON COLUMN restful_usage_log_tbl.endpoint IS 'Invoked restful service URL';
COMMENT ON COLUMN restful_usage_log_tbl.description IS 'additional information';
COMMENT ON COLUMN restful_usage_log_tbl.request_method IS 'Method of request';
COMMENT ON COLUMN restful_usage_log_tbl.company_id IS 'Company ID of restful user';
COMMENT ON COLUMN restful_usage_log_tbl.username IS 'Name of restful user';
COMMENT ON column restful_usage_log_tbl.host_name IS 'stores name of host';
COMMENT ON COLUMN restful_usage_log_tbl.supervisor_name IS 'if this was a supervisor access it is stored here';

CREATE TABLE mailloop_log_tbl
(
    mailloop_log_id SERIAL,
    rid             VARCHAR(32),
    timestamp       TIMESTAMP,
    status          SMALLINT,
    company_id      INTEGER,
    mailing_id      INTEGER,
    customer_id     INTEGER,
    action          VARCHAR(32),
    remark          VARCHAR(1000)
) TABLESPACE data_bounce;
ALTER TABLE mailloop_log_tbl ADD CONSTRAINT mlooplog$id$pk PRIMARY KEY (mailloop_log_id) USING INDEX TABLESPACE data_bounce;
COMMENT ON TABLE mailloop_log_tbl IS 'stores activity for each bounce filter';
COMMENT ON COLUMN mailloop_log_tbl.mailloop_log_id IS 'unique key, use mailloop_log_tbl_seq';
COMMENT ON COLUMN mailloop_log_tbl.rid IS 'either an internal identifier or a numeric value >0 referencing mailloop_tbl.rid';
COMMENT ON COLUMN mailloop_log_tbl.timestamp IS 'timestamp of occuring of this event';
COMMENT ON COLUMN mailloop_log_tbl.status IS '1=processing of incoming mail was successful, 0=failed';
COMMENT ON COLUMN mailloop_log_tbl.company_id IS 'the company_tbl.company_id of the sender, if available';
COMMENT ON COLUMN mailloop_log_tbl.mailing_id IS 'the mailing_tbl.mailing_id of the origin newsletter, if available';
COMMENT ON COLUMN mailloop_log_tbl.customer_id IS 'the customer_<CID>_tbl.customer_id of the origin recipient of the newsletter, if available';
COMMENT ON COLUMN mailloop_log_tbl.action IS 'the resulting action after processing this incoming mail';
COMMENT ON COLUMN mailloop_log_tbl.remark IS 'further information collected during processing';

CREATE TABLE css_tbl
(
    id          SERIAL PRIMARY KEY,
    company_id  INTEGER NOT NULL,
    name        VARCHAR(255),
    value       VARCHAR(255),
    description VARCHAR(1000)
);
COMMENT ON TABLE css_tbl IS 'stores scss parameters for generation of main css';
COMMENT ON COLUMN css_tbl.id IS 'unique key';
COMMENT ON COLUMN css_tbl.company_id IS 'company id';
COMMENT ON COLUMN css_tbl.name IS 'name of scss parameter';
COMMENT ON COLUMN css_tbl.value IS 'value of scss parameter';
COMMENT ON COLUMN css_tbl.description IS 'description of scss parameter';
INSERT INTO css_tbl (id, name, company_id, value, description) VALUES (1, 'c-blue-27', 0, '#0071b9', 'Primary Color, Default: #0071b9');
INSERT INTO css_tbl (id, name, company_id, value, description) VALUES (2, 'c-blue-56', 0, '#004470', 'Secondary Color, Default: #004470');
INSERT INTO css_tbl (id, name, company_id, value, description) VALUES (3, 'c-blue-22', 0, '#338dc7', 'Tertiary Color, Default: #338dc7');

CREATE TABLE undo_workflow_tbl
(
    undo_id         SERIAL PRIMARY KEY,
    admin_id        INTEGER                             NOT NULL,
    workflow_id     INTEGER                             NOT NULL,
    company_id      INTEGER                             NOT NULL,
    workflow_schema TEXT,
    creation_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
ALTER TABLE undo_workflow_tbl ADD CONSTRAINT undoworkflow$cid$fk FOREIGN KEY (company_id) REFERENCES company_tbl (company_id);
ALTER TABLE undo_workflow_tbl ADD CONSTRAINT undoworkflow$adminid$fk FOREIGN KEY (admin_id) REFERENCES admin_tbl (admin_id);
ALTER TABLE undo_workflow_tbl ADD CONSTRAINT undoworkflow$workflowid$fk FOREIGN KEY (workflow_id) REFERENCES workflow_tbl (workflow_id);
COMMENT ON TABLE undo_workflow_tbl IS 'stores workflow_schema to provide undo function (for example, to autorestore the workflow scheme after an expired pause)';
COMMENT ON COLUMN undo_workflow_tbl.undo_id IS 'unique id, use undo_workflow_seq';
COMMENT ON COLUMN undo_workflow_tbl.admin_id IS 'admin who made changes to workflow and caused undo entry creation - ID (admin_tbl)';
COMMENT ON COLUMN undo_workflow_tbl.workflow_id IS 'id of the workflow';
COMMENT ON COLUMN undo_workflow_tbl.company_id IS 'tenant - ID (company_tbl)';
COMMENT ON COLUMN undo_workflow_tbl.workflow_schema IS 'JSON representation of workflow icons, their properties and connections between them';
COMMENT ON COLUMN undo_workflow_tbl.creation_date IS 'entry creation time';

ALTER TABLE workflow_tbl ADD CONSTRAINT workflow$pause$fk FOREIGN KEY (pause_undo_id) REFERENCES undo_workflow_tbl (undo_id);

CREATE TABLE import_size_tbl
(
    company_ref INTEGER     NOT NULL,
    import_ref  INTEGER     NOT NULL,
    import_type VARCHAR(32) NOT NULL,
    lines_count INTEGER     NOT NULL,
    timestamp   TIMESTAMP   NOT NULL
) TABLESPACE data_temp;
COMMENT ON TABLE import_size_tbl IS 'Import sizes';
COMMENT ON COLUMN import_size_tbl.company_ref IS 'company id';
COMMENT ON COLUMN import_size_tbl.import_ref IS 'import id';
COMMENT ON COLUMN import_size_tbl.import_type IS 'Type of import (see ImportType enum)';
COMMENT ON COLUMN import_size_tbl.lines_count IS 'number of imported lines';
COMMENT ON COLUMN import_size_tbl.timestamp IS 'timestamp of import';


INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('system', 'url', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('system', 'defaultRdirDomain', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('system', 'defaultMailloopDomain', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('system', 'licence', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('jobqueue', 'execute', '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('expire', 'SuccessDef', '180', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('birt', 'url', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('birt', 'privatekey', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('birt', 'publickey', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('webservices', 'url', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailaddress', 'bounce', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailaddress', 'error', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailaddress', 'feature_support', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailaddress', 'frontend', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailaddress', 'replyto', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailaddress', 'report_archive', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailaddress', 'sender', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailaddress', 'support', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailaddress', 'upload.database', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailaddress', 'upload.support', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.account_logfile', '${home}/log/account.log', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.blocksize', '1000', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.boundary', 'AGNITAS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.default_charset', 'ISO-8859-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.default_encoding', 'quoted-printable', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.directdir', '${home}/var/spool/DIRECT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.domain', '[to be defined]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.eol', 'LF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.loglevel', 'ERROR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.mailer', 'Agnitas EMM ${ApplicationMajorVersion}.${ApplicationMinorVersion}', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.mail_log_number', '400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.metadir', '${home}/var/spool/META', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.xmlback', '${home}/bin/xmlback', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('mailout', 'ini.xmlvalidate', 'False', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('upselling', 'moreInfo.url.de',
        'https://www.agnitas.de/e-marketing-manager/funktionsumfang/unterschiede-emminhouse-openemm/',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('upselling', 'moreInfo.url.en',
        'https://www.agnitas.de/en/e-marketing_manager/functions/differences-emminhouse-openemm/', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 'initial setting fulldb');
INSERT INTO config_tbl (class, name, value, creation_date, change_date, description)
VALUES ('security', 'csrfProtection.enabled', 'true', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'initial setting fulldb');

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('actions.show', 'Actions', NULL, 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('actions.change', 'Actions', NULL, 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('actions.delete', 'Actions', NULL, 3, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailloop.show', 'Administration', 'settings.Mailloop', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailloop.change', 'Administration', 'settings.Mailloop', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailloop.delete', 'Administration', 'settings.Mailloop', 3, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('role.show', 'Administration', 'settings.Usergroups', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('role.change', 'Administration', 'settings.Usergroups', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('role.delete', 'Administration', 'settings.Usergroups', 3, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('admin.new', 'Administration', 'settings.Admin', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('admin.show', 'Administration', 'settings.Admin', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('admin.change', 'Administration', 'settings.Admin', 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('admin.delete', 'Administration', 'settings.Admin', 4, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('admin.setgroup', 'Administration', 'settings.Admin', 5, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('admin.setpermission', 'Administration', 'settings.Admin', 6, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('admin.sendWelcome', 'Administration', 'settings.Admin', 7, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('adminlog.show', 'Administration', 'settings.Admin', 8, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('campaign.show', 'Mailing', 'Campaigns', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('campaign.change', 'Mailing', 'Campaigns', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('campaign.delete', 'Mailing', 'Campaigns', 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.editor.extended', 'Mailing', NULL, 8, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('forms.show', 'Forms', NULL, 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('forms.change', 'Forms', NULL, 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('forms.delete', 'Forms', NULL, 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('forms.import', 'Forms', NULL, 4, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('forms.export', 'Forms', NULL, 5, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('calendar.show', 'General', NULL, 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('charset.use.iso_8859_15', 'General', NULL, 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('charset.use.utf_8', 'General', NULL, 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('upload.change', 'General', NULL, 7, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('workflow.activate', 'General', NULL, 10, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('workflow.delete', 'General', NULL, 11, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('workflow.change', 'General', NULL, 12, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('workflow.show', 'General', NULL, 13, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('settings.extended', 'General', NULL, 14, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('wizard.import', 'ImportExport', NULL, 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('import.change', 'ImportExport', NULL, 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('import.delete', 'ImportExport', NULL, 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('wizard.importclassic', 'ImportExport', NULL, 4, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('recipient.change.bulk', 'ImportExport', NULL, 5, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('wizard.export', 'ImportExport', NULL, 6, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('export.ownColumns', 'ImportExport', NULL, 6, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('export.change', 'ImportExport', NULL, 7, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('export.delete', 'ImportExport', NULL, 8, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('datasource.show', 'ImportExport', NULL, 10, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('import.preprocessing', 'ImportExport', 'import.settings', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('import.mode.doublechecking', 'ImportExport', 'import.settings', 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('import.mode.duplicates', 'ImportExport', 'import.settings', 4, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('import.mailinglists.all', 'ImportExport', 'import.settings', 5, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('import.mode.add', 'ImportExport', 'import.mode', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('import.mode.add_update', 'ImportExport', 'import.mode', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('import.mode.only_update', 'ImportExport', 'import.mode', 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('import.mode.unsubscribe', 'ImportExport', 'import.mode', 4, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('import.mode.bounce', 'ImportExport', 'import.mode', 5, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('import.mode.blacklist', 'ImportExport', 'import.mode', 6, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailinglist.show', 'Subscriber-Editor', 'Mailinglist', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailinglist.change', 'Subscriber-Editor', 'Mailinglist', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailinglist.delete', 'Subscriber-Editor', 'Mailinglist', 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailinglist.recipients.delete', 'Subscriber-Editor', 'Mailinglist', 4, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.show', 'Mailing', NULL, 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.change', 'Mailing', NULL, 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.delete', 'Mailing', NULL, 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.classic', 'Mailing', NULL, 4, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.import', 'Mailing', NULL, 5, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.export', 'Mailing', NULL, 6, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.content.show', 'Mailing', 'mailing.searchContent', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.components.show', 'Mailing', 'mailing.searchContent', 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.components.change', 'Mailing', 'mailing.searchContent', 4, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.attachments.show', 'Mailing', 'mailing.searchContent', 5, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.extend_trackable_links', 'Mailing', 'mailing.searchContent', 6, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.contentsource.date.limit', 'Mailing', 'mailing.searchContent', 8, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.content.showExcludedTargetgroups', 'Mailing', 'mailing.searchContent', 9, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.send.show', 'Mailing', 'Delivery', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.send.world', 'Mailing', 'Delivery', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.setmaxrecipients', 'Mailing', 'Delivery', 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.send.admin.target', 'Mailing', 'Delivery', 4, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.send.admin.options', 'Mailing', 'Delivery', 5, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.resume.world', 'Mailing', 'Delivery', 6, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.can_allow', 'Mailing', 'Delivery', 7, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.can_send_always', 'Mailing', 'Delivery', 8, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('salutation.show', 'Mailing', 'settings.FormsOfAddress', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('salutation.change', 'Mailing', 'settings.FormsOfAddress', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('salutation.delete', 'Mailing', 'settings.FormsOfAddress', 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('recipient.gender.extended', 'Mailing', 'settings.FormsOfAddress', 4, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.show.types', 'Mailing', 'Settings', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mediatype.email', 'Mailing', 'Settings', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.envelope_address', 'Mailing', 'Settings', 4, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.recipients.show', 'Mailing', 'Settings', 7, 'Automation Package', CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.editor.hide', 'NegativePermissions', NULL, 5, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.editor.hide.html', 'NegativePermissions', NULL, 6, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('stats.show', 'Statistics', NULL, 15, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('stats.mailing', 'Statistics', 'General', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('stats.domains', 'Statistics', 'General', 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('stats.month', 'Statistics', 'General', 4, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('stats.userform', 'Statistics', 'General', 6, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('stats.ecs', 'Statistics', 'Mailing', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('statistic.softbounces.show', 'Statistics', 'Mailing', 5, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('report.birt.show', 'Statistics', 'Reports', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('report.birt.change', 'Statistics', 'Reports', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('report.birt.delete', 'Statistics', 'Reports', 3, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('recipient.show', 'Subscriber-Editor', NULL, 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('recipient.change', 'Subscriber-Editor', NULL, 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('recipient.create', 'Subscriber-Editor', NULL, 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('recipient.delete', 'Subscriber-Editor', NULL, 4, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('recipient.history', 'Subscriber-Editor', 'default.extensions', 1, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('profileField.show', 'Subscriber-Editor', 'recipient.fields', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('profileField.visible', 'Subscriber-Editor', 'recipient.fields', 2, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('blacklist', 'Subscriber-Editor', 'recipient.Blacklist', 1, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('company.settings.intern', 'System', NULL, 54, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('master.permission.migration.show', 'System', NULL, 60, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('targets.access.limit.extended', 'System', NULL, 61, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('restfulUser.show', 'System', 'package.smartdata.restful', 55, 'WebservicesRestful Package', CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('restfulUser.change', 'System', 'package.smartdata.restful', 56, 'WebservicesRestful Package',
        CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('restfulUser.new', 'System', 'package.smartdata.restful', 57, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('restfulUser.delete', 'System', 'package.smartdata.restful', 58, 'WebservicesRestful Package',
        CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('server.status', 'System', 'others', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('masterlog.show', 'System', 'others', 3, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('master.companies.show', 'System', 'Company', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('company.authentication', 'System', 'Company', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('company.default.stepping', 'System', 'Company', 4, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.content.change.always', 'System', 'Mailing', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('mailing.expire', 'System', 'Mailing', 2, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('webservice.user.show', 'System', 'settings.webservice', 1, 'WebservicesSoap Package', CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('webservice.user.create', 'System', 'settings.webservice', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('webservice.user.change', 'System', 'settings.webservice', 3, 'WebservicesSoap Package', CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('master.show', 'System', 'settings.menu.master', 1, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('targets.show', 'Target-Groups', NULL, 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('targets.change', 'Target-Groups', NULL, 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('targets.delete', 'Target-Groups', NULL, 3, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('targets.lock', 'Target-Groups', NULL, 4, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('template.show', 'Mailing', 'Template', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('template.change', 'Mailing', 'Template', 2, NULL, CURRENT_TIMESTAMP);
INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('template.delete', 'Mailing', 'Template', 3, NULL, CURRENT_TIMESTAMP);

INSERT INTO permission_tbl (permission_name, category, sort_order)
values ('admin.management.show', 'Administration', 2);

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package, creation_date) VALUES ('master.dbschema.snapshot.create', 'System', NULL, 60, NULL, CURRENT_TIMESTAMP);

-- Permissions of category "attachment"
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetAttachment', 'attachment');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListAttachments', 'attachment');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('AddAttachment', 'attachment');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('UpdateAttachment', 'attachment');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('DeleteAttachment', 'attachment');

-- Permissions of category "blacklist"
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('AddBlacklist', 'blacklist');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetBlacklistItems', 'blacklist');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('DeleteBlacklist', 'blacklist');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('CheckBlacklist', 'blacklist');

-- Permissions of category "contentBlock"
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetContentBlock', 'contentBlock');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('DeleteContentBlock', 'contentBlock');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListContentBlocks', 'contentBlock');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('UpdateContentBlock', 'contentBlock');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListContentBlockNames', 'contentBlock');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('AddContentBlock', 'contentBlock');

-- Permissions of category "mailing"
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ImportMailingContent', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetMailing', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('AddMailing', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('CopyMailing', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetMailingStatus', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('SendServiceMail', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListMailings', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('SendMailing', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('AddMailingFromTemplate', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('UpdateMailing', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('DeleteMailing', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetFullviewUrl', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetMailingContent', 'mailing');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('UpdateMailingContent', 'mailing');

-- Permissions of category "mailinglist"
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListMailinglists', 'mailinglist');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetMailinglist', 'mailinglist');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListMailingsInMailinglist', 'mailinglist');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('DeleteMailinglist', 'mailinglist');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('UpdateMailinglist', 'mailinglist');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('AddMailinglist', 'mailinglist');

-- Permissions of category "pushNotification"
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('SetPushNotificationPlanDate', 'pushNotification');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('SendPushNotification', 'pushNotification');

-- Permissions of category "statistics"
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('MailingSummaryStatisticJob', 'statistics');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('MailingSummaryStatisticResult', 'statistics');

-- Permissions of category "subscriber"
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('FindSubscriber', 'subscriber');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('AddSubscriberBulk', 'subscriber');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('UpdateSubscriberBulk', 'subscriber');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('UpdateSubscriber', 'subscriber');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('DeleteSubscriberBulk', 'subscriber');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('AddSubscriber', 'subscriber');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListSubscribers', 'subscriber');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('DeleteSubscriber', 'subscriber');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetSubscriberBulk', 'subscriber');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListSubscriberMailings', 'subscriber');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetSubscriber', 'subscriber');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('AddSubscribers', 'subscriber');

-- Permissions of category "subscriberBinding"
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetSubscriberBinding', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('DeleteSubscriberBindingBulk', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('SetSubscriberBinding', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('DeleteSubscriberBinding', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListSubscriberBindingBulk', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('SetSubscriberBindingBulk', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('SetSubscriberBindingWithAction', 'subscriberBinding');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListSubscriberBinding', 'subscriberBinding');

-- Permissions of category "targetgroup"
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('UpdateTargetGroup', 'targetgroup');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('AddTargetGroup', 'targetgroup');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListTargetgroups', 'targetgroup');

-- Permissions of category "template"
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('AddTemplate', 'template');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetTemplate', 'template');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('DeleteTemplate', 'template');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('UpdateTemplate', 'template');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListTemplates', 'template');

-- Permissions of category "trackableLinks"
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('GetTrackableLinkSettings', 'trackableLinks');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('UpdateTrackableLinkSettings', 'trackableLinks');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ListTrackableLinks', 'trackableLinks');
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('DecryptLinkData', 'trackableLinks');

-- Uncategorized permissions
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('CreateDataSource', NULL);
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('ImportStatus', NULL);
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('StartImport', NULL);
INSERT INTO webservice_permissions_tbl (endpoint, category) VALUES ('AddMailingImage', NULL);

INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (0, 'keep-xml-files', 'true', 'Admin-/Testmail XML Files nicht loeschen', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (0, 'use-extended-usertypes', 'true', 'USER_TYPE fuer VIP Verteiler ermoeglichen', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);
INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (0, 'url-default', 'http://UndefinedInCompanyInfoTbl',
        'Bei fehlender RDIR_DOMAIN in der COMPANY_TBL wird diese URL genommen', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (0, 'expire-recv', '90', 'Expiration period for recv_xxx_tbl entries', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (0, 'expire-recipients-report', 90, 'Expiration period for recipients_report_tbl rows', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);
INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (0, 'host_authentication.authentication', 'disabled', 'Two way authentication', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);
INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (0, 'limit-block-operations', '500000', 'Splitting Merger Updates for customer_tbls', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);
INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (0, 'limit-block-operations-max', '5', 'Splitting Merger Updates for customer_tbls', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);
INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (0, 'imagelink-template', '%(rdir-domain)/image/%(licence-id)/%(company-id)/%(mailing-id)/[name]',
        'Path-structure by default (EMM-4603)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (0, 'imagelink-template-no-cache', '%(rdir-domain)/image/nc/%(licence-id)/%(company-id)/%(mailing-id)/[name]',
        'Path-structure by default (EMM-4603)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sourcegroup_tbl (sourcegroup_type, description, timestamp, creation_date) VALUES ('A', 'Subscriber Interface', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO sourcegroup_tbl (sourcegroup_type, description, timestamp, creation_date) VALUES ('D', 'File', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO sourcegroup_tbl (sourcegroup_type, description, timestamp, creation_date) VALUES ('O', 'Other', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO sourcegroup_tbl (sourcegroup_type, description, timestamp, creation_date) VALUES ('FO', 'Autoinsert Forms', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO sourcegroup_tbl (sourcegroup_type, description, timestamp, creation_date) VALUES ('DD', 'default datasource for companies', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO sourcegroup_tbl (sourcegroup_type, description, timestamp, creation_date) VALUES ('WS', 'Webservices 2.0 - Spring', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO sourcegroup_tbl (sourcegroup_type, description, timestamp, creation_date) VALUES ('V', 'Velocity', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO sourcegroup_tbl (sourcegroup_type, description, timestamp, creation_date) VALUES ('RS', 'RestfulService', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO company_tbl (rdir_domain, mailloop_domain, shortname, description, status, secret_key, enabled_uid_version, max_recipients, company_token)
VALUES ('http://[to be defined]', '[to be defined]', 'EMM-Master', 'EMM-Master', 'active', 'SecretKeyToBeDefined', 5,
        10000, (SELECT CONCAT(LOWER(LEFT (MD5(random()::text), 16)), UPPER(LEFT (MD5(random()::text), 16)))));

INSERT INTO datasource_description_tbl (description, company_id, sourcegroup_id)
VALUES ('Default Datasource', 1, (SELECT sourcegroup_id FROM sourcegroup_tbl WHERE sourcegroup_type = 'DD'));

UPDATE company_tbl
SET default_datasource_id = (SELECT datasource_id
                             FROM datasource_description_tbl
                             WHERE company_id = 1 AND description = 'Default Datasource')
WHERE company_id = 1;

INSERT INTO datasource_description_tbl (description, company_id, sourcegroup_id)
VALUES ('Bulk recipient update', 0,
        (SELECT sourcegroup_id FROM sourcegroup_tbl WHERE sourcegroup_type = 'O'));

INSERT INTO datasource_description_tbl (description, company_id, sourcegroup_id)
VALUES ('Velocity', 0,
        (SELECT sourcegroup_id FROM sourcegroup_tbl WHERE sourcegroup_type = 'V'));

INSERT INTO datasource_description_tbl (description, company_id, sourcegroup_id)
VALUES ('RestfulService', 0,
        (SELECT sourcegroup_id FROM sourcegroup_tbl WHERE sourcegroup_type = 'RS'));

INSERT INTO mailinglist_tbl (company_id, description, shortname, auto_url, remove_data, rdir_domain,
                             creation_date, change_date)
VALUES (1, 'Default, please do not delete!', 'Default-Mailinglist', NULL, '0', NULL,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO rdir_action_tbl (company_id, shortname, description, action_type) VALUES (1, 'Web-View_SAMPLE', 'Fullview', 1);
INSERT INTO actop_tbl (company_id, type, action_id)
VALUES (1, 'GetArchiveMailing', (SELECT action_id
                                                           FROM rdir_action_tbl
                                                           WHERE action_type = 1
                                                             AND shortname = 'Web-View_SAMPLE'
                                                             AND company_id = 1));
INSERT INTO actop_get_archive_mailing_tbl (action_operation_id, expire_day, expire_month, expire_year)
VALUES ((SELECT action_operation_id
         FROM actop_tbl
         WHERE company_id = 1
           AND action_id = (SELECT action_id
                            FROM rdir_action_tbl
                            WHERE action_type = 1 AND shortname = 'Web-View_SAMPLE' AND company_id = 1)), 0, 0, 0);

INSERT INTO userform_tbl (company_id, formname, description, startaction_id, endaction_id, success_template,
                          error_template, creation_date, success_url, error_url, error_use_url, success_use_url)
VALUES (1, 'fullview', 'Shows email in browser', (SELECT action_id
                                                                               FROM rdir_action_tbl
                                                                               WHERE action_type = 1
                                                                                 AND shortname = 'Web-View_SAMPLE'
                                                                                 AND company_id = 1), 0, '$archiveHtml',
        'An error occured - we cannot show you the email in the browser.', NULL, NULL, NULL,
        (SELECT action_id FROM rdir_action_tbl WHERE company_id = 1 AND shortname = 'GetArchiveMailing'), 0);

INSERT INTO admin_group_tbl (admin_group_id, company_id, shortname, description)
VALUES (0, 1, 'No permissions', 'Standard');
INSERT INTO admin_group_tbl (admin_group_id, company_id, shortname, description)
VALUES (1, 1, 'Administrator', 'Benutzer mit allen Rechten');
INSERT INTO admin_group_tbl (admin_group_id, company_id, shortname, description)
VALUES (7, 1, 'Leserechte/view only', 'Leserechte/view only');

SELECT SETVAL('admin_group_tbl_admin_group_id_seq', (SELECT MAX(admin_group_id) FROM admin_group_tbl));

INSERT INTO admin_tbl (username, company_id, admin_lang, admin_country, fullname, firstname, email, company_name, creation_date, pwdchange_date)
VALUES ('emm-master', 1, 'en', 'US', 'Master', 'EMM', '[to be defined]', 'EMM Master', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO admin_to_group_tbl (admin_id, admin_group_id)
VALUES ((SELECT admin_id FROM admin_tbl WHERE username = 'emm-master'), 1);

INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnDATE', '{base}', 'SIMPLE', 0, 'Dummy Tag for Preview');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnDB', NULL, 'COMPLEX', 0, 'Selects a generic column from DB');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnDVALUE', 'agnDVALUE textComponent {name}', 'FLOW', 0,
        'agnDVALUE-Tag works only with agnDYN');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnDYN', 'agnDYN textComponent {name}', 'FLOW', 0,
        'agnDYN-Tag works optionally with agnDVALUE');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnEMAIL', NULL, 'SIMPLE', 0, '');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnFORM',
        '''[rdir-domain]/form.action?agnCI=[company-id]' || chr(38) || 'agnFN={name}' || chr(38) ||
        'agnUID=##AGNUID##''', 'COMPLEX', 0, 'create a link to an emm-form');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnIMAGE', NULL, 'COMPLEX', 0, 'Generates URL for hosted images');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnIMGLINK', NULL, 'COMPLEX', 0, '');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnMAILTYPE', 'cust.mailtype', 'SIMPLE', 0, '');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnMESSAGEID', '''''', 'SIMPLE', 0, 'Dummy content replaced by merger');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnPROFILE',
        '''[rdir-domain]/form.action?agnCI=[company-id]' || chr(38) || 'agnFN=profile' || chr(38) ||
        'agnUID=##AGNUID##''', 'COMPLEX', 0, 'create a link to an emm-profile-form');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnSUBSCRIBERCOUNT', NULL, 'SIMPLE', 0, 'Dummy-Tag for Preview');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnTITLE', NULL, 'COMPLEX', 0, 'shows title - print out title, lastname - by tw');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnTITLEFIRST', NULL, 'COMPLEX', 0, 'shows title - print out firstname');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnTITLEFULL', NULL, 'COMPLEX', 0,
        'shows title - print out title, firstname, lastname - by tw');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnUID', '''''', 'SIMPLE', 0, 'agnUID');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnUNSUBSCRIBE',
        '''[rdir-domain]/form.action?agnCI=[company-id]' || chr(38) || 'agnFN=unsubscribe' || chr(38) ||
        'agnUID=##AGNUID##''', 'COMPLEX', 0, 'create a link to an emm-unsubscribe-form');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnITEM', '', 'COMPLEX', 0, NULL);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnFULLVIEW', NULL, 'COMPLEX', 0, 'Generates URL for fullview');
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description) VALUES ('agnWEBVIEW', NULL, 'COMPLEX', 0, 'Generates URL for fullview');

INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnALTER', 'trunc(months_between(sysdate, cust.{column})/12, 0)', 'COMPLEX', 0,
        'Returns years from column value until now', 1);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnALTERCALC', 'trunc(months_between(sysdate, cust.{column})/12, 0) {op} {value}',
        'COMPLEX', 0, 'like agnALTER with operator and value', 1);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnCALC', 'cust.{column} {op} {value}', 'COMPLEX', 0, 'calculate with NUM-Field', 1);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnCUSTOMDATE', 'to_char(sysdate+{offset}, ''{format}'')', 'COMPLEX', 0,
        'Adds an offset in days to the sysdate value and returns the formatted date', 1);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnCUSTOMDATE_DE',
        'to_char(sysdate+{offset}, ''{format}'', ''nls_date_language = german'')', 'COMPLEX', 0,
        'Adds an offset in days to the sysdate value and returns the formatted date in german lang', 1);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnCUSTOMERID', 'cust.customer_id', 'SIMPLE', 0, '', 1);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnDATEDB', 'to_char(cust.{column}, ''{format}'')', 'COMPLEX', 0,
        'returns date value in column custom formatted', 1);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnDATEDB_DE',
        'rtrim(ltrim(to_char(cust.{column}, ''{format}'', ''nls_date_language = german'')))', 'COMPLEX', 0,
        'Returns date in column custom formatted in german lang', 1);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnDATEDB_LANG',
        'to_char(cust.{column}, ''{format}'', ''nls_date_language = {lang}'')', 'COMPLEX', 0,
        'Returns date in column custom formatted in given language', 1);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnDAYS_UNTIL', 'trunc(cust.{column})-trunc(SYSDATE)', 'COMPLEX', 0,
        'Returns days until endday (endday MUST lie in future!!!)', 0);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnFIRSTNAME', 'cust.firstname', 'SIMPLE', 0, '', 1);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnLASTNAME', 'cust.lastname', 'SIMPLE', 0, '', 1);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnYEARCALC', 'to_char (cust.{field}, ''YYYY'') {op} {value}', 'COMPLEX', 0,
        'to calculate with column', 1);
INSERT INTO tag_tbl (tagname, selectvalue, type, company_id, description, deprecated) VALUES ('agnYEARCALC_F', 'to_char (cust.{column}, ''{format}'') {op} {value}', 'COMPLEX', 0,
        'like agnYEARCALC with formating the date', 1);

INSERT INTO swyn_tbl (company_id, name, source, charset, ordering, image, icon, target, code, creation_date, timestamp, isize) VALUES (0, '__prefix__', NULL, NULL, NULL, NULL, NULL, NULL,
        '<table border="0" cellpadding="0" cellspacing="5"><tr><td>', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '');
INSERT INTO swyn_tbl (company_id, name, source, charset, ordering, image, icon, target, code, creation_date, timestamp, isize) VALUES (0, '__infix__', NULL, NULL, NULL, NULL, NULL, NULL, '</td><td>', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, '');
INSERT INTO swyn_tbl (company_id, name, source, charset, ordering, image, icon, target, code, creation_date, timestamp, isize) VALUES (0, '__postfix__', NULL, NULL, NULL, NULL, NULL, NULL, '</td></tr></table>',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '');
INSERT INTO swyn_tbl (company_id, name, source, charset, ordering, image, icon, target, code, creation_date, timestamp, isize) VALUES (0, 'Facebook', 'FB', 'UTF8', 1, 'button_facebook.png', NULL,
        'http://www.facebook.com/sharer.php?u=%(urllink)' || chr(38) || 't=%(urltitle)',
        '<a href="%(target)"><img src="%(rdir-domain)/image?ci=%(company-id)' || chr(38) || 'mi=%(mailing-id)' ||
        chr(38) || 'name=%(urlimage)" alt="share on Facebook" width="120" height="20" border="0"></a>',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '');
INSERT INTO swyn_tbl (company_id, name, source, charset, ordering, image, icon, target, code, creation_date, timestamp, isize) VALUES (0, 'Twitter', 'TW', 'UTF8', 2, 'button_Twitter.png', NULL,
        'http://twitter.com/home?status=%(urltitle:90)+%(urllink)',
        '<a href="%(target)"><img src="%(rdir-domain)/image?ci=%(company-id)' || chr(38) || 'mi=%(mailing-id)' ||
        chr(38) || 'name=%(urlimage)" alt="share on Twitter" width="120" height="20" border="0"></a>',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '');
INSERT INTO swyn_tbl (company_id, name, source, charset, ordering, image, icon, target, code, creation_date, timestamp, isize) VALUES (0, 'StudiVZ', 'VZ', 'UTF8', 3, 'button_StudyVZ.png', NULL,
        'http://www.studivz.net/Suggest/Selection/?u=%(urllink)' || chr(38) || 'desc=%(urltitle)' || chr(38) ||
        'prov=%(urlhost)',
        '<a href="%(target)"><img src="%(rdir-domain)/image?ci=%(company-id)' || chr(38) || 'mi=%(mailing-id)' ||
        chr(38) || 'name=%(urlimage)" alt="share on Study VZ" width="120" height="20" border="0"></a>',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '');
INSERT INTO swyn_tbl (company_id, name, source, charset, ordering, image, icon, target, code, creation_date, timestamp, isize) VALUES (0, 'MySpace', 'MS', 'UTF8', 4, 'button_myspace.png', NULL,
        'http://www.myspace.com/index.cfm?useaction=postto' || chr(38) || 't=%(urltitle:90)' || chr(38) ||
        'c=%(urltitle)' || chr(38) || 'u=%(urllink)' || chr(38) || 'l=2',
        '<a href="%(target)"><img src="%(rdir-domain)/image?ci=%(company-id)' || chr(38) || 'mi=%(mailing-id)' ||
        chr(38) || 'name=%(urlimage)" alt="share on myspace" width="120" height="20" border="0"></a>',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '');
INSERT INTO swyn_tbl (company_id, name, source, charset, ordering, image, icon, target, code, creation_date, timestamp, isize) VALUES (0, 'LinkedIn', 'LI', 'UTF8', 5, 'button_LinkedIn.png', NULL,
        'http://www.linkedin.com/shareArticle?mini=true' || chr(38) || 'url=%(urllink)' || chr(38) ||
        'title=%(urltitle:90)' || chr(38) || 'summary=%(urltitle)' || chr(38) || 'source=%(urlhost)',
        '<a href="%(target)"><img src="%(rdir-domain)/image?ci=%(company-id)' || chr(38) || 'mi=%(mailing-id)' ||
        chr(38) || 'name=%(urlimage)" alt="share on Linked in" width="120" height="20" border="0"></a>',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '');

INSERT INTO del_predelivery_view_conf_tbl (device, screen, container) VALUES ('symbians60', 'symbians60_screen', 'symbians60_container');
INSERT INTO del_predelivery_view_conf_tbl (device, screen, container) VALUES ('iphone3', 'iphone3_screen', 'iphone3_container');
INSERT INTO del_predelivery_view_conf_tbl (device, screen, container) VALUES ('windowsmobile65portrait', 'windowsmobile65portrait_screen', 'windowsmobile65portrait_container');
INSERT INTO del_predelivery_view_conf_tbl (device, screen, container) VALUES ('blackberry8900', 'blackberry8900_screen', 'blackberry8900_container');

INSERT INTO emm_layout_base_tbl (layout_base_id, base_url, creation_date, change_date, company_id, shortname)
VALUES (0, 'assets/core', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'Light');
INSERT INTO emm_layout_base_tbl (layout_base_id, base_url, creation_date, change_date, company_id, shortname,
                                 menu_position, theme_type)
VALUES ((SELECT MAX(layout_base_id) FROM emm_layout_base_tbl) + 1, 'assets/core', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        0, 'Dark', 1, 1);
INSERT INTO emm_layout_base_tbl (layout_base_id, base_url, creation_date, change_date, company_id, shortname,
                                 menu_position, theme_type)
VALUES ((SELECT MAX(layout_base_id) FROM emm_layout_base_tbl) + 1, 'assets/core', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        0, 'High-Contrast Light', 0, 2);
INSERT INTO emm_layout_base_tbl (layout_base_id, base_url, creation_date, change_date, company_id, shortname,
                                 menu_position, theme_type)
VALUES ((SELECT MAX(layout_base_id) FROM emm_layout_base_tbl) + 1, 'assets/core', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        0, 'High-Contrast Dark', 0, 3);

INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('importStep2', 'assigning_the_csv_columns_to_t.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('userRights', 'assigning_user_rights.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('bounceFilter', 'bounce-filter.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('trackableLinkView', 'building_trackable_links_into_.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('calendar', 'calendar_function.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('classicImport', 'classic_import.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('compareMailings', 'comparing_mailings.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('contentSource', 'content_sources.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newArchive', 'create_a_new_archive.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newMailingWizard', 'create_new_mailing_using_the_w.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newRecipient', 'create_new_recipients.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-child-view-setup', 'creating_a_contribution.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-container-view-setup', 'creating_a_div_container.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-view-setup', 'creating_a_grid.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newMailinglist', 'modifying_a_mailing_list.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newAction', 'creating_a_new_action.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newCompany', 'what_are_users_and_clients_.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newImportProfile', 'creating_a_new_import_profile.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('createNewMailing', 'creating_a_new_mailing.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newUser', 'creating_a_new_user.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-list-setup', 'creating_and_managing_a_grid.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-category-list-setup', 'creating_and_managing_categori.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-container-list-setup', 'creating_and_managing_div_cont.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-child-list-setup', 'creating_and_managing_posts.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('targetGroupView', 'creating_and_managing_target_g.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-category-view-setup', 'creating_categories.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newProfileField', 'creating_new_fields.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('template-view-setup', 'creating_templates.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('contentView', 'creating_text_and_html_modules.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('dashboard', 'dashboard.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('archiveView', 'display_and_amend_details.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newTemplateNormal', 'display_and_amend_details.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('domainStatistic', 'domain_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newMailingNormal', 'entering_basic_mailing_data.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newTemplate', 'entering_basic_template_data.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('importStep3', 'errorhandling.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('export', 'export_function_for_recipient_.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('heatmap', 'heatmap.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('contentList', 'further_details_for_creating_t.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('ipStatistic', 'ip_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingList', 'list_existing_mailings.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingStatistic', 'click_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('manageProfile', 'managing_a_profile__deleting_a.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('actionList', 'managing_actions.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('workflow', 'managing_campaigns.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('manageFields', 'managing_fields.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('formList', 'managing_forms.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('profileFieldList', 'managing_profile_fields.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('recipientList', 'managing_recipients.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('template-list-setup', 'managing_templates.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailinglists', 'modifying_a_mailing_list.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('monthlyOverview', 'monthly_overview.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('recipientStatistic', 'recipient_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('sendMailing', 'send_mailing.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('attachments', 'further_details_for_creating_t.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('recipientView', 'show_recipient_profile.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingTestAndSend', 'testing_and_sending_a_mailing.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('importStep1', 'the_import_assistant.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('formView', 'this_is_how_forms_work.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('salutationForms', 'types_of_address.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('upload', 'upload_files.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-background-image-view-setup', 'uploading_a_background_image.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-background-image-list-setup', 'uploading_and_managing_backgro.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-image-list-setup', 'uploading_and_managing_images.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-image-view-setup', 'uploading_images_one-off_uploa.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-image-mass-upload-setup', 'uploading_images_volume_upload.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('userlog', 'user_log.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('user_self-administration', 'user_self-administration.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('pictureComponents', 'further_details_for_creating_t.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('templateList', 'templates_-_re-usable_text_mod.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('targetGroupList', 'what_is_a_traget_group_.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('dashboard_calendar', 'what_is_the_calendar_.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-child-view-new', 'creating_a_contribution.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-container-view-new', 'creating_a_div_container.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-container-view-view', 'creating_a_div_container.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-view-new', 'creating_a_grid.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-list-view', 'creating_and_managing_a_grid.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-category-list-view', 'creating_and_managing_categori.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-container-delete-view', 'creating_and_managing_div_cont.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-container-list-view', 'creating_and_managing_div_cont.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-child-list-view', 'creating_and_managing_posts.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-category-view-new', 'creating_categories.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('template-view-new', 'creating_templates.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('template-view-view', 'creating_templates.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newUserGroup', 'defining_own_user_groups.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('UserGroupDelete', 'defining_own_user_groups.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('UserGroupList', 'defining_own_user_groups.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-category-delete-view', 'editing_a_category.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-category-view-view', 'editing_a_category.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-child-delete-view', 'editing_a_contribution.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-child-view-view', 'editing_a_contribution.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-image-view-view', 'editing_an_image_entry.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-background-image-view-view', 'editing_background_image_entry.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-delete-view', 'editing_grids.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-view-view', 'editing_grids.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Grid', 'grid_cms.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('ipStatistics', 'ip_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingParameter', 'mailing_parameters.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('template-delete-view', 'managing_templates.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('template-list-view', 'managing_templates.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-background-image-view-new', 'uploading_a_background_image.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-background-image-bulk-delete-view', 'uploading_and_managing_backgro.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-background-image-delete-view', 'uploading_and_managing_backgro.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('grid-background-image-list-view', 'uploading_and_managing_backgro.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-image-list-view', 'uploading_and_managing_images.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-image-delete-view', 'uploading_and_managing_images.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-image-bulk-delete-view', 'uploading_and_managing_images.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-image-view-new', 'uploading_images_one-off_uploa.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mediapool-image-mass-upload-view', 'uploading_images_volume_upload.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('topDomainStatistic', 'domain_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('help_workflow_edit', 'opening_a_campaign_in_the_edit.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('blacklist', 'blacklist_-_do_not_mail.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('reports', 'statistics_reports_by_e-mail.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('autoImport', 'auto_import.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('help_workflow_new', 'building_up_a_campaign_in_the_.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('importStep4', 'importing_the_csv-file.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('preview', 'further_details_for_creating_t.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('autoExport', 'auto_export.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('NewGridMailing', 'creating_a_new_grid_mailing.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('autooptimization', 'auto-optimization_of_mailings.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('trackableLinks', 'further_details_for_creating_t.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('inboxPreview', 'automatic_pre-delivery_check_p.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('template-header-info-view', 'setting_delivery_options.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('pluginmanagerList', 'produktname_-_the_basics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('gridTemplate', 'produktname_-_the_basics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('ReferenceTables', 'produktname_-_the_basics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('feedbackAnalysis', 'produktname_-_the_basics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-container-generate-thumbnails-view', 'produktname_-_the_basics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newTable', 'produktname_-_the_basics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('div-child-generate-thumbnails-view', 'produktname_-_the_basics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('supervisors', 'produktname_-_the_basics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('recipientHistory', 'recipient_history.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('recipientMailingHistory', 'mailing_history.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('managingUsers', 'managing_user.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('managingUserGroups', 'defining_own_user_groups.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('graphicsInForms', 'using_graphics_in_forms.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('trackpointsList', 'display_and_amend_details_for_.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('trackpointsEdit', 'creating_shop_tracking_points.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('socialNetworkStatistic', 'click_statistics_for_social_ne.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('openingProgressStatistic', 'opening_rate.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('bounceStatistic', 'bounce_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('revenueStatistic', 'statistical_evaluation2.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('deliveryProgressStatistic', 'delivery_progress.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('benchmarkStatistic', 'benchmark.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('top_domains_statistics', 'top_domains_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('action-based_mailing', 'action-based_mailing.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('searching_for_mailings', 'searching_for_mailings.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('the_tracking_point_revenue', 'the_tracking_point_revenue.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('creating_advanced_target_groups', 'creating_advanced_target_groups.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('manage_tables', 'manage_tables.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('creating_new_reference_tables', 'creating_new_reference_tables.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('defining_fields_tables', 'defining_fields_tables.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('adding_and_deleting_fields_tables', 'adding_and_deleting_fields_tables.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('editing_a_reference_table', 'editing_a_reference_table.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('import_tables', 'import_tables.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('export_tables', 'export_tables.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('retargeting_history', 'retargeting_history.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('E-Mail-Creator', 'e-mail_creator.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('create_a_mailing_E-Mail_Creator', 'create_a_mailing_e-mail-creator.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Content', 'content.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Styles', 'styles.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('File_attachments', 'file_attachments.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Links', 'links.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Preview', 'preview.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Sending_out_test_mails2', 'sending_out_test_mails2.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Mailing_statistic2', 'mailing_statistic2.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('clickstatistic_per_link', 'clickstatistic_per_link.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('progress_of_gross_openings', 'progress_of_gross_openings.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('summary', 'summary.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Progress_of_openings', 'progress_of_openings.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Content_blocks', 'content_blocks.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Heatmap2', 'heatmap2.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Recipients2', 'recipients2.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Using_image_elements', 'using_image_elements.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Create_trackable_and_non-track', 'create_trackable_and_non-track.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Statistics', 'mailing_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Recipients', 'recipients.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('End_device_history', 'end_device_history.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Managing profile fields', 'managing_profile_fields.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Edit_field_content', 'edit_field_content.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Logs', 'logs.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Progress_of_clicks', 'progress_of_clicks.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('top_domains', 'top_domains.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('End_device_statistics', 'end_device_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Domain_overview', 'domain_overview.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('End_device_overview', 'end_device_overview.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Customer_Insights', 'customer_insights.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Approval', 'approval.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Manage_create_send_and_evaluate_push_notifications', 'what_are_web_push_notifications.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Create_a_new_push_notification', 'creating_a_new_push_notification.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Shipping_settings_and_statistics_for_push', 'shipping_settings_and_statistics_for_push_notification.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Push-trigger_notifications', 'push_trigger_notifications.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Push_statistics', 'push_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('User', 'user.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('User_approval', 'user_approval.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Clients', 'clients.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Create_new_voucher_code_table', 'create_new_voucher_code_table.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Create_and_manage_templates', 'create_and_manage_templates.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Create_template', 'create_template.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Edit_template', 'edit_template.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('End_device_statistic2', 'end_device_overview.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('End_device_statistic', 'end_device_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Recipient_Insights', 'insights.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('supervisor-permissions', 'supervisor-permissions.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('push_global_statistics', 'push_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('push_notification_view', 'shipping_settings_and_statistics_for_push_notification.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingBase', 'base_settings.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingAttachments', 'sending_normal_file_attachment.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingLinks', 'using_trackable_links.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingPreview', 'preview_-_for_in-depth_checkin.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingsCheck', 'sending_out_test_mails.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingGridContent', 'creating_content_by_using_the_.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingGridTextContent', 'fill_in_building_blocks_by_usi.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingGeneralOptions', 'entering_basic_mailing_data1.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingArchive', 'what_are_archives_.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailingPrioritisation', 'mailing-priorisierung.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('importGeneral', 'import_dialog_structure.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('statisticMailing', 'mailing_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('mailinglistCreate', 'creating_a_mailing_list.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('formImages', 'using_images_in_forms.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('formStatistic', 'form_statistics.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('clientCreate', 'creating_a_new_client.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('pluginManagerGeneral', 'Plugin_Manager.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('tablesVoucherCreate', 'create_new_voucher_code_table.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('layoutbuilderTemplateContent', 'adding_content.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('layoutbuilderTemplateEditCss', 'editing_css_files.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Server_status', 'system-status.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('dataSourceList', 'datasource-id.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('importing-templates', 'importing-templates2.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('webhooks', '../pdf/AGNITAS_EMM_Webhooks-Documentation.pdf');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('createNewForm', 'creating-a-new-form.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('importElement', 'importing-elements.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('facebookLeadAds', 'facebook-lead-ads.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('newTrigger', 'create_or_change_trigger.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('Domains', 'domains.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('TextGenerator', 'create-ai-content.htm');
INSERT INTO doc_mapping_tbl (pagekey, filename) VALUES ('agnTags', 'appendix_a_agnitas_tags.htm');

INSERT INTO date_tbl (type, format) VALUES (0, 'd.M.yyyy');
INSERT INTO date_tbl (type, format) VALUES (1, 'MM/dd/yyyy');
INSERT INTO date_tbl (type, format) VALUES (2, 'EEEE, d. MMMM yyyy');
INSERT INTO date_tbl (type, format) VALUES (3, 'yyyy-MM-dd');
INSERT INTO date_tbl (type, format) VALUES (4, 'd. MMMM yyyy');
INSERT INTO date_tbl (type, format) VALUES (5, 'EEEE, d MMMM yyyy');
INSERT INTO date_tbl (type, format) VALUES (6, 'dd/MM/yyyy');
INSERT INTO date_tbl (type, format) VALUES (7, 'yyyy/MM/dd');
INSERT INTO date_tbl (type, format) VALUES (8, 'yyyy-MM-dd');

-- Split: 5% 5% 5% 5% 5% 75%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_050505050575_1', 'mod(cust.CUSTOMER_ID, 20) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_050505050575_2', 'mod(cust.CUSTOMER_ID, 20) = 1', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_050505050575_3', 'mod(cust.CUSTOMER_ID, 20) = 2', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_050505050575_4', 'mod(cust.CUSTOMER_ID, 20) = 3', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_050505050575_5', 'mod(cust.CUSTOMER_ID, 20) = 4', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_050505050575_6', 'mod(cust.CUSTOMER_ID, 20) >= 5', 1, 0);
-- Split: 5% 5% 5% 5% 80%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_0505050580_1', 'mod(cust.CUSTOMER_ID, 20) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_0505050580_2', 'mod(cust.CUSTOMER_ID, 20) = 1', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_0505050580_3', 'mod(cust.CUSTOMER_ID, 20) = 2', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_0505050580_4', 'mod(cust.CUSTOMER_ID, 20) = 3', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_0505050580_5', 'mod(cust.CUSTOMER_ID, 20) >= 4', 1, 0);
-- Split: 5% 5% 5% 85%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_05050585_1', 'mod(cust.CUSTOMER_ID, 20) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_05050585_2', 'mod(cust.CUSTOMER_ID, 20) = 1', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_05050585_3', 'mod(cust.CUSTOMER_ID, 20) = 2', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_05050585_4', 'mod(cust.CUSTOMER_ID, 20) >= 3', 1, 0);
-- Split: 10% 10% 10% 10% 10% 50%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_101010101050_1', 'mod(cust.CUSTOMER_ID, 10) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_101010101050_2', 'mod(cust.CUSTOMER_ID, 10) = 1', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_101010101050_3', 'mod(cust.CUSTOMER_ID, 10) = 2', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_101010101050_4', 'mod(cust.CUSTOMER_ID, 10) = 3', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_101010101050_5', 'mod(cust.CUSTOMER_ID, 10) = 4', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_101010101050_6', 'mod(cust.CUSTOMER_ID, 10) >= 5', 1, 0);
-- Split: 10% 10% 10% 10% 60%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_1010101060_1', 'mod(cust.CUSTOMER_ID, 10) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_1010101060_2', 'mod(cust.CUSTOMER_ID, 10) = 1', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_1010101060_3', 'mod(cust.CUSTOMER_ID, 10) = 2', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_1010101060_4', 'mod(cust.CUSTOMER_ID, 10) = 3', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_1010101060_5', 'mod(cust.CUSTOMER_ID, 10) >= 4', 1, 0);
-- Split: 10% 10% 10% 70%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_10101070_1', 'mod(cust.CUSTOMER_ID, 10) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_10101070_2', 'mod(cust.CUSTOMER_ID, 10) = 1', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_10101070_3', 'mod(cust.CUSTOMER_ID, 10) = 2', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_10101070_4', 'mod(cust.CUSTOMER_ID, 10) >= 3', 1, 0);
-- Split: 10% 10% 80%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_101080_1', 'mod(cust.CUSTOMER_ID, 10) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_101080_2', 'mod(cust.CUSTOMER_ID, 10) = 1', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_101080_3', 'mod(cust.CUSTOMER_ID, 10) > 1', 1, 0);
-- Split: 10% 90%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_1090_10', 'mod(cust.CUSTOMER_ID, 10) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_1090_90', 'mod(cust.CUSTOMER_ID, 10) > 0', 1, 0);
-- Split: 15% 15% 70%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_151570_1', 'mod(cust.CUSTOMER_ID, 100) < 15', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_151570_2',
        'mod(cust.CUSTOMER_ID, 100) > 14 AND mod(cust.CUSTOMER_ID, 100) < 30', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_151570_3', 'mod(cust.CUSTOMER_ID, 100) > 29', 1, 0);
-- Split: 20% 80%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_2080_20', 'mod(cust.CUSTOMER_ID, 10) < 2', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_2080_80', 'mod(cust.CUSTOMER_ID, 10) > 1', 1, 0);
-- Split: 25% 25% 25% 25%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_25252525_1', 'mod(cust.CUSTOMER_ID, 4) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_25252525_2', 'mod(cust.CUSTOMER_ID, 4) = 1', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_25252525_3', 'mod(cust.CUSTOMER_ID, 4) = 2', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_25252525_4', 'mod(cust.CUSTOMER_ID, 4) = 3', 1, 0);
-- Split: 25% 25% 50%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_252550_1', 'mod(cust.CUSTOMER_ID, 4) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_252550_2', 'mod(cust.CUSTOMER_ID, 4) = 1', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_252550_3', 'mod(cust.CUSTOMER_ID, 4) > 1', 1, 0);
-- Split: 30% 70%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_3070_30', 'mod(cust.CUSTOMER_ID, 100) < 30', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_3070_70', 'mod(cust.CUSTOMER_ID, 100) > 29', 1, 0);
-- Split: 33% 33% 33%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_333333_1', 'mod(cust.CUSTOMER_ID, 3) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_333333_2', 'mod(cust.CUSTOMER_ID, 3) = 1', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_333333_3', 'mod(cust.CUSTOMER_ID, 3) = 2', 1, 0);
-- Split: 40% 60%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_4060_40', 'mod(cust.CUSTOMER_ID, 10) < 4', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_4060_60', 'mod(cust.CUSTOMER_ID, 10) > 3', 1, 0);
-- Split: 50% 50%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_5050_1', 'mod(cust.CUSTOMER_ID, 2) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_5050_2', 'mod(cust.CUSTOMER_ID, 2) = 1', 1, 0);
-- Split: 5% 5% 90%
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_050590_1', 'mod(cust.CUSTOMER_ID, 20) = 0', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_050590_2', 'mod(cust.CUSTOMER_ID, 20) = 1', 1, 0);
INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, locked, deleted)
VALUES (0, '__listsplit_050590_3', 'mod(cust.CUSTOMER_ID, 20) > 1', 1, 0);

INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, eql, target_description, deleted,
                            change_date, creation_date, admin_test_delivery, locked, component_hide, invalid, hidden)
VALUES (1, 'EMM Target Group: Gender Male', '(cust.GENDER = 0)', '`gender` = 0', NULL, 0,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, 0, 0, 0);

INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, eql, target_description, deleted,
                            change_date, creation_date, admin_test_delivery, locked, component_hide, invalid, hidden)
VALUES (1, 'EMM Target Group: Gender Female', '(cust.GENDER = 1)', '`gender` = 1', NULL,
        0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, 0, 0, 0);

INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, eql, target_description, deleted,
                            change_date, creation_date, admin_test_delivery, locked, component_hide, invalid, hidden)
VALUES (1, 'EMM Target Group: Gender Unknown', '(cust.GENDER = 2)', '`gender` = 2', NULL,
        0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, 0, 0, 0);

INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, eql, target_description, deleted,
                            change_date, creation_date, admin_test_delivery, locked, component_hide, invalid, hidden)
VALUES (1, 'EMM Target Group: Openers and Clickers',
        '((cust.LASTCLICK_DATE IS NOT NULL) AND ((cust.LASTOPEN_DATE IS NOT NULL) AND (cust.LASTSEND_DATE IS NOT NULL)))',
        '`lastclick_date` IS NOT EMPTY OR `lastopen_date` IS NOT EMPTY', NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        0, NULL, 0, 0, 0);

INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, eql, target_description, deleted,
                            change_date, creation_date, admin_test_delivery, locked, component_hide, invalid, hidden)
VALUES (1, 'EMM Target Group: Lead',
        '(((to_char(cust.creation_date, ''YYYYMMDD'') > to_char((SYSDATE) - (91), ''YYYYMMDD'')) AND ((cust.lastopen_date IS NULL) AND (cust.lastclick_date IS NULL))) OR (((cust.sys_tracking_veto IS NULL) OR (cust.sys_tracking_veto <> 1)) OR (to_char(cust.lastsend_date, ''YYYYMMDD'') = to_char((SYSDATE) - (90), ''YYYYMMDD''))))',
        '(`creation_date` > TODAY-91 DATEFORMAT ''YYYYMMDD'' AND `lastopen_date` IS EMPTY AND `lastclick_date` IS EMPTY) OR (`sys_tracking_veto` IS EMPTY OR `sys_tracking_veto` <> 1) OR `lastsend_date` = TODAY-90 DATEFORMAT ''YYYYMMDD''',
        NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, 0, 0, 0);

INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, eql, target_description, deleted,
                            change_date, creation_date, admin_test_delivery, locked, component_hide, invalid, hidden)
VALUES (1, 'EMM Target Group: Newcomer',
        '(to_char(cust.creation_date, ''YYYYMMDD'') > to_char((SYSDATE) - (31), ''YYYYMMDD''))',
        '`creation_date` > TODAY-31 DATEFORMAT ''YYYYMMDD''', NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, 0,
        0, 0);

INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, eql, target_description, deleted,
                            change_date, creation_date, admin_test_delivery, locked, component_hide, invalid, hidden)
VALUES (1, 'EMM Target Group: Opportunity',
        '((to_char(cust.lastopen_date, ''YYYYMMDD'') > to_char((SYSDATE) - (91), ''YYYYMMDD'')) OR ((to_char(cust.lastclick_date, ''YYYYMMDD'') > to_char((SYSDATE) - (91), ''YYYYMMDD'')) OR (cust.sys_tracking_veto = 1)))',
        '`lastopen_date` > TODAY-91 DATEFORMAT ''YYYYMMDD'' OR `lastclick_date` > TODAY-91 DATEFORMAT ''YYYYMMDD'' OR `sys_tracking_veto` = 1',
        NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, 0, 0, 0);

INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, eql, target_description, deleted,
                            change_date, creation_date, admin_test_delivery, locked, component_hide, invalid, hidden)
VALUES (1, 'EMM Target Group: Sleeper',
        '((to_char(cust.creation_date, ''YYYYMMDD'') < to_char((SYSDATE) - (90), ''YYYYMMDD'')) AND ((to_char(cust.lastsend_date, ''YYYYMMDD'') > to_char((SYSDATE) - (91), ''YYYYMMDD'')) AND (((cust.lastopen_date IS NULL) OR (to_char(cust.lastopen_date, ''YYYYMMDD'') < to_char((SYSDATE) - (90), ''YYYYMMDD''))) AND (((cust.lastclick_date IS NULL) OR (to_char(cust.lastclick_date, ''YYYYMMDD'') < to_char((SYSDATE) - (90), ''YYYYMMDD''))) AND ((cust.sys_tracking_veto IS NULL) OR (cust.sys_tracking_veto <> 1))))))',
        '`creation_date` < TODAY-90 DATEFORMAT ''YYYYMMDD'' AND `lastsend_date` > TODAY-91 DATEFORMAT ''YYYYMMDD'' AND (`lastopen_date` IS EMPTY OR `lastopen_date` < TODAY-90 DATEFORMAT ''YYYYMMDD'') AND (`lastclick_date` IS EMPTY OR `lastclick_date` < TODAY-90 DATEFORMAT ''YYYYMMDD'') AND (`sys_tracking_veto` IS EMPTY OR `sys_tracking_veto` <> 1)',
        NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, 0, 0, 0);

INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, eql, target_description, deleted,
                            change_date, creation_date, admin_test_delivery, locked, component_hide, invalid, hidden)
VALUES (1, 'EMM Target Group: Unattended 3 Months',
        '((to_char(cust.creation_date, ''YYYYMMDD'') < to_char((SYSDATE) - (30), ''YYYYMMDD'')) AND ((to_char(cust.lastsend_date, ''YYYYMMDD'') < to_char((SYSDATE) - (90), ''YYYYMMDD'')) OR (cust.lastsend_date IS NULL)))',
        '`creation_date` < TODAY-30 DATEFORMAT ''YYYYMMDD'' AND (`lastsend_date` < TODAY-90 DATEFORMAT ''YYYYMMDD'' OR `lastsend_date` IS EMPTY)',
        NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, 0, 0, 0);

INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, eql, target_description, deleted,
                            change_date, creation_date, admin_test_delivery, locked, component_hide, invalid, hidden)
VALUES (1, 'EMM Target Group: Unattended 6 Months',
        '((to_char(cust.creation_date, ''YYYYMMDD'') < to_char((SYSDATE) - (30), ''YYYYMMDD'')) AND ((to_char(cust.lastsend_date, ''YYYYMMDD'') < to_char((SYSDATE) - (180), ''YYYYMMDD'')) OR (cust.lastsend_date IS NULL)))',
        '`creation_date` < TODAY-30 DATEFORMAT ''YYYYMMDD'' AND (`lastsend_date` < TODAY-180 DATEFORMAT ''YYYYMMDD'' OR `lastsend_date` IS EMPTY)',
        NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, 0, 0, 0);

INSERT INTO dyn_target_tbl (company_id, target_shortname, target_sql, eql, target_description, deleted,
                            change_date, creation_date, admin_test_delivery, locked, component_hide, invalid, hidden)
VALUES (1, 'EMM Target Group: Untouched',
        '(to_char(cust.timestamp, ''YYYYMMDD'') < to_char((SYSDATE) - (180), ''YYYYMMDD''))',
        '`timestamp` < TODAY-180 DATEFORMAT ''YYYYMMDD''', NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, NULL, 0, 0,
        0);

INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, deleted, criticality)
VALUES ('LoginTrackTableCleaner', CURRENT_TIMESTAMP, NULL, '0',
        'OK', '1', '0', '**00', CURRENT_TIMESTAMP, NULL, 'com.agnitas.util.quartz.LoginTrackTableCleanerJobWorker', 0,
        2);
INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value) (SELECT id, 'retentionTime', '14'
                                                                               FROM job_queue_tbl
                                                                               WHERE description = 'LoginTrackTableCleaner');
INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value) (SELECT id, 'deleteBlockSize', '1000'
                                                                               FROM job_queue_tbl
                                                                               WHERE description = 'LoginTrackTableCleaner');

INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, deleted, criticality)
VALUES ('DBCleaner', CURRENT_TIMESTAMP, NULL, '0', 'OK', '0', '0', '0000',
        CURRENT_TIMESTAMP, NULL, 'com.agnitas.util.quartz.DBCleanerJobWorker', 0, 2);
INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value) (SELECT id, 'maxNumberOfCompanyCleaningWorkers', '3'
                                                                               FROM job_queue_tbl
                                                                               WHERE description = 'DBCleaner');

INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, deleted, criticality)
VALUES ('CalendarCommentMailingService', CURRENT_TIMESTAMP, NULL, '0', 'OK',
        '1', '0', '**00', CURRENT_TIMESTAMP, NULL,
        'com.agnitas.emm.core.calendar.service.CalendarCommentMailingServiceJobWorker', 0, 3);

INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, deleted, criticality)
VALUES ('UndoRelictCleaner', CURRENT_TIMESTAMP, NULL, '0', 'OK', '1', '23',
        '0130', CURRENT_TIMESTAMP, NULL, 'com.agnitas.service.job.UndoRelictCleanerJobWorker', 0, 2);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, deleted, criticality)
VALUES ('AutoOptimization', CURRENT_TIMESTAMP, NULL, '0', 'OK', '1', '0',
        '***0;***5', CURRENT_TIMESTAMP, NULL, 'com.agnitas.mailing.autooptimization.service.OptimizationJobWorker', 0,
        5);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, deleted, criticality)
VALUES ('BirtReports', CURRENT_TIMESTAMP, NULL, '0', 'OK', '1', '0',
        '***0;***5', CURRENT_TIMESTAMP, NULL, 'com.agnitas.emm.core.birtreport.service.BirtReportJobWorker', 0, 4);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, deleted, criticality)
VALUES ('WorkflowReminderServiceJobWorker', CURRENT_TIMESTAMP, NULL, '0', 'OK',
        '1', '0', '***0;***5', CURRENT_TIMESTAMP, NULL,
        'com.agnitas.emm.core.workflow.service.WorkflowReminderServiceJobWorker', 0, 4);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, deleted, criticality)
VALUES ('WorkflowReactionHandler', CURRENT_TIMESTAMP, NULL, '0', 'OK', '1',
        '0', '***0;***5', CURRENT_TIMESTAMP, NULL,
        'com.agnitas.emm.core.workflow.service.jobs.WorkflowReactionJobWorker', 0, 5);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, deleted, criticality)
VALUES ('WorkflowStateHandler', CURRENT_TIMESTAMP, NULL, '0', 'OK', '1', '0',
        '***0;***5', CURRENT_TIMESTAMP, NULL,
        'com.agnitas.emm.core.workflow.service.jobs.WorkflowStateTransitionJobWorker', 0, 5);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, deleted, criticality)
VALUES ('DBErrorCheck', CURRENT_TIMESTAMP, NULL, 0, 'OK', 1, 0, '**00',
        CURRENT_TIMESTAMP, NULL, 'com.agnitas.util.quartz.DBErrorCheckJobWorker', 0, 3);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, deleted, criticality)
VALUES ('WebserviceLoginTrackTableCleaner', CURRENT_TIMESTAMP, NULL, 0, 'OK',
        1, 0, '**00', CURRENT_TIMESTAMP, NULL, 'com.agnitas.util.quartz.WebserviceLoginTrackTableCleanerJobWorker', 0,
        2);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, deleted)
VALUES ('RefreshUserLastLogin', CURRENT_TIMESTAMP, NULL, 0, 'OK', 1, 0, '**00',
        CURRENT_TIMESTAMP, NULL, 'com.agnitas.service.job.RefreshUserLastLoginJobWorker', 0);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, criticality, deleted)
VALUES ('AnonymizeStatistics', CURRENT_TIMESTAMP, null, 0, 'OK', 1, 0, '0000',
        CURRENT_TIMESTAMP, null, 'com.agnitas.service.job.AnonymizeStatisticsJobWorker', 1, 0);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, criticality, deleted)
VALUES ('DiskSpaceCheck', CURRENT_TIMESTAMP, null, 0, 'OK', 1, 0, '**00',
        CURRENT_TIMESTAMP, null, 'com.agnitas.emm.core.serverstatus.service.job.DiskSpaceCheckJobWorker', 3, 0);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, criticality, deleted)
VALUES ('MigrationJobWorker', CURRENT_TIMESTAMP, null, 0, 'OK', 1, 0, 'MoTuWeThFr:1500', CURRENT_TIMESTAMP, null, 'com.agnitas.service.job.MigrationJobWorker', 4, 0);
INSERT INTO job_queue_tbl (description, created, laststart, running, lastresult, startaftererror, lastduration,
                           interval, nextstart, hostname, runclass, criticality, deleted)
VALUES ('UpdatePasswordReminder', CURRENT_TIMESTAMP, null, 0, 'OK', 1, 0,
        '0800', CURRENT_TIMESTAMP, null, 'com.agnitas.service.job.UpdatePasswordReminderJobWorker', 1, 0);


INSERT INTO mailing_account_sum_tbl (company_id, mailing_id, status_field, no_of_mailings, no_of_bytes, mintime, maxtime)
SELECT company_id,
       mailing_id,
       status_field,
       SUM(no_of_mailings),
       SUM(no_of_bytes),
       MIN(timestamp),
       MAX(timestamp)
FROM mailing_account_tbl
GROUP BY company_id, mailing_id, status_field;

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'Sehr geehrter Herr/Sehr geehrte Frau/Sehr geehrte Damen und Herren');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0
           AND description = 'Sehr geehrter Herr/Sehr geehrte Frau/Sehr geehrte Damen und Herren'), 0,
        'Sehr geehrter Herr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0
           AND description = 'Sehr geehrter Herr/Sehr geehrte Frau/Sehr geehrte Damen und Herren'), 1,
        'Sehr geehrte Frau');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0
           AND description = 'Sehr geehrter Herr/Sehr geehrte Frau/Sehr geehrte Damen und Herren'), 2,
        'Sehr geehrte Damen und Herren');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'Hallo Herr/Hallo Frau/Hallo lieber Kunde');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Hallo Herr/Hallo Frau/Hallo lieber Kunde'), 0, 'Hallo Herr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Hallo Herr/Hallo Frau/Hallo lieber Kunde'), 1, 'Hallo Frau');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Hallo Herr/Hallo Frau/Hallo lieber Kunde'), 2, 'Hallo lieber Kunde');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'Hallo Herr/Hallo Frau/Hallo lieber Leser');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Hallo Herr/Hallo Frau/Hallo lieber Leser'), 0, 'Hallo Herr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Hallo Herr/Hallo Frau/Hallo lieber Leser'), 1, 'Hallo Frau');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Hallo Herr/Hallo Frau/Hallo lieber Leser'), 2, 'Hallo lieber Leser');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'Lieber Herr/Liebe Frau/Liebe Damen und Herren');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Lieber Herr/Liebe Frau/Liebe Damen und Herren'), 0, 'Lieber Herr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Lieber Herr/Liebe Frau/Liebe Damen und Herren'), 1, 'Liebe Frau');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Lieber Herr/Liebe Frau/Liebe Damen und Herren'), 2,
        'Liebe Damen und Herren');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'Lieber Herr/Liebe Frau/Lieber Kunde');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Lieber Herr/Liebe Frau/Lieber Kunde'), 0, 'Lieber Herr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Lieber Herr/Liebe Frau/Lieber Kunde'), 1, 'Liebe Frau');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Lieber Herr/Liebe Frau/Lieber Kunde'), 2, 'Lieber Kunde');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'Lieber Herr/Liebe Frau/Lieber Leser');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Lieber Herr/Liebe Frau/Lieber Leser'), 0, 'Lieber Herr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Lieber Herr/Liebe Frau/Lieber Leser'), 1, 'Liebe Frau');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Lieber Herr/Liebe Frau/Lieber Leser'), 2, 'Lieber Leser');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'lieber Herr/liebe Frau/liebe Damen und Herren');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'lieber Herr/liebe Frau/liebe Damen und Herren'), 0, 'lieber Herr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'lieber Herr/liebe Frau/liebe Damen und Herren'), 1, 'liebe Frau');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'lieber Herr/liebe Frau/liebe Damen und Herren'), 2,
        'liebe Damen und Herren');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'lieber Herr/liebe Frau/lieber Kunde');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'lieber Herr/liebe Frau/lieber Kunde'), 0, 'lieber Herr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'lieber Herr/liebe Frau/lieber Kunde'), 1, 'liebe Frau');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'lieber Herr/liebe Frau/lieber Kunde'), 2, 'lieber Kunde');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'lieber Herr/liebe Frau/lieber Leser');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'lieber Herr/liebe Frau/lieber Leser'), 0, 'lieber Herr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'lieber Herr/liebe Frau/lieber Leser'), 1, 'liebe Frau');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'lieber Herr/liebe Frau/lieber Leser'), 2, 'lieber Leser');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'Dear Mr/Dear Mrs/Dear Sir or Madam');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Dear Mr/Dear Mrs/Dear Sir or Madam'), 0, 'Dear Mr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Dear Mr/Dear Mrs/Dear Sir or Madam'), 1, 'Dear Mrs');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Dear Mr/Dear Mrs/Dear Sir or Madam'), 2, 'Dear Sir or Madam');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'dear Mr/dear Mrs/dear Sir or Madam');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'dear Mr/dear Mrs/dear Sir or Madam'), 0, 'dear Mr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'dear Mr/dear Mrs/dear Sir or Madam'), 1, 'dear Mrs');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'dear Mr/dear Mrs/dear Sir or Madam'), 2, 'dear Sir or Madam');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'Dear/Dear/Dear Sir or Madam');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id) FROM title_tbl WHERE company_id = 0 AND description = 'Dear/Dear/Dear Sir or Madam'), 0,
        'Dear');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id) FROM title_tbl WHERE company_id = 0 AND description = 'Dear/Dear/Dear Sir or Madam'), 1,
        'Dear');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id) FROM title_tbl WHERE company_id = 0 AND description = 'Dear/Dear/Dear Sir or Madam'), 2,
        'Dear Sir or Madam');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'dear/dear/dear Sir or Madam');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id) FROM title_tbl WHERE company_id = 0 AND description = 'dear/dear/dear Sir or Madam'), 0,
        'dear');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id) FROM title_tbl WHERE company_id = 0 AND description = 'dear/dear/dear Sir or Madam'), 1,
        'dear');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id) FROM title_tbl WHERE company_id = 0 AND description = 'dear/dear/dear Sir or Madam'), 2,
        'dear Sir or Madam');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'Dear Mr/Dear Mrs/Ladies and Gentleman');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Dear Mr/Dear Mrs/Ladies and Gentleman'), 0, 'Dear Mr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Dear Mr/Dear Mrs/Ladies and Gentleman'), 1, 'Dear Mrs');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'Dear Mr/Dear Mrs/Ladies and Gentleman'), 2, 'Ladies and Gentleman');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'dear Mr/dear Mrs/Ladies and Gentleman');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'dear Mr/dear Mrs/Ladies and Gentleman'), 0, 'dear Mr');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'dear Mr/dear Mrs/Ladies and Gentleman'), 1, 'dear Mrs');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id)
         FROM title_tbl
         WHERE company_id = 0 AND description = 'dear Mr/dear Mrs/Ladies and Gentleman'), 2, 'Ladies and Gentleman');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'Hello/Hello/Hello reader');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id) FROM title_tbl WHERE company_id = 0 AND description = 'Hello/Hello/Hello reader'), 0,
        'Hello');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id) FROM title_tbl WHERE company_id = 0 AND description = 'Hello/Hello/Hello reader'), 1,
        'Hello');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id) FROM title_tbl WHERE company_id = 0 AND description = 'Hello/Hello/Hello reader'), 2,
        'Hello reader');

INSERT INTO title_tbl (company_id, description)
VALUES (0, 'Hello/Hello/Hello customer');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id) FROM title_tbl WHERE company_id = 0 AND description = 'Hello/Hello/Hello customer'), 0,
        'Hello');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id) FROM title_tbl WHERE company_id = 0 AND description = 'Hello/Hello/Hello customer'), 1,
        'Hello');
INSERT INTO title_gender_tbl (title_id, gender, title)
VALUES ((SELECT MAX(title_id) FROM title_tbl WHERE company_id = 0 AND description = 'Hello/Hello/Hello customer'), 2,
        'Hello customer');

INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('image/*', NULL, CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('application/pdf', NULL, CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('text/plain', NULL, CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('text/csv', NULL, CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('application/zip', NULL, CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('application/vnd.ms-excel', NULL, CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', NULL, CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('application/x-zip-compressed', NULL, CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('application/msword', NULL, CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('application/vnd.openxmlformats-officedocument.wordprocessingml.document', NULL, CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('text/calendar', 'iCalendar (.ics, .ifb, .iCal, .iFBf)', CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('application/vnd.oasis.opendocument.spreadsheet', '*.ods', CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('application/vnd.oasis.opendocument.text', '*.odt', CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('application/vnd.oasis.opendocument.presentation', '*.odp', CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('text/xml', 'XML, FO (EMM-7703)', CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('text/x-xslfo', 'XSL-FO (EMM-7703)', CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('text/vcard', 'Outlook Kontakt Linux (EMM-7861)', CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('text/x-vcard', 'Outlook Kontakt Windows (EMM-7861)', CURRENT_TIMESTAMP);
INSERT INTO mimetype_whitelist_tbl (mimetype, description, creation_date) VALUES ('application/json', NULL, CURRENT_TIMESTAMP);

CREATE
OR REPLACE FUNCTION emm_log_db_errors(p_error_message TEXT, p_company_id INT, p_module_name TEXT)
    RETURNS VOID AS $$
DECLARE
v_client_info TEXT;
BEGIN
    -- Get client info using PostgreSQL functions
SELECT pg_catalog.current_user || '|' || inet_client_addr() || '|' || current_setting('application_name')
INTO v_client_info;

-- Insert error log into the table
INSERT INTO emm_db_errorlog_tbl (company_id, errortext, module_name, client_info)
VALUES (p_company_id, p_error_message, p_module_name, v_client_info);

COMMIT;
END;
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mailing_account_sum_trigger_function()
RETURNS TRIGGER AS $$
DECLARE
v_mintime    TIMESTAMP;
    v_maxtime    TIMESTAMP;
    v_error_msg  TEXT;
BEGIN
SELECT mintime, maxtime
INTO v_mintime, v_maxtime
FROM mailing_account_sum_tbl
WHERE mailing_id   = NEW.mailing_id
  AND status_field = NEW.status_field;

IF NOT FOUND THEN
        -- Handle the case when no data is found
        INSERT INTO mailing_account_sum_tbl (
            mailing_id, company_id, no_of_mailings, no_of_bytes,
            mintime, maxtime, status_field
        )
        VALUES (
            NEW.mailing_id, NEW.company_id, NEW.no_of_mailings, NEW.no_of_bytes,
            NEW.timestamp, NEW.timestamp, NEW.status_field
        );
RETURN NULL;
END IF;

    -- If the new timestamp is greater than the maxtime, update the maxtime
    IF v_maxtime IS NULL OR NEW.timestamp > v_maxtime THEN
UPDATE mailing_account_sum_tbl
SET maxtime = NEW.timestamp
WHERE mailing_id = NEW.mailing_id
  AND status_field = NEW.status_field;
END IF;

    -- Update the no_of_mailings and no_of_bytes
UPDATE mailing_account_sum_tbl
SET no_of_mailings = no_of_mailings + NEW.no_of_mailings,
    no_of_bytes    = no_of_bytes + NEW.no_of_bytes
WHERE mailing_id = NEW.mailing_id
  AND status_field = NEW.status_field;

RETURN NULL;
EXCEPTION WHEN OTHERS THEN
    -- Capture any error and log it using the error logging procedure
    v_error_msg := SQLERRM;
    PERFORM
emm_log_db_errors(v_error_msg, 0, 'mailing_account_sum_trigger');
RETURN NULL;
END;
$$
LANGUAGE plpgsql;

-- Create the trigger
CREATE TRIGGER mailing_account_sum_trg
    AFTER INSERT OR
UPDATE ON mailing_account_tbl
    FOR EACH ROW
    EXECUTE FUNCTION mailing_account_sum_trigger_function();


DO
$$
BEGIN
        -- Try creating the index on the 'shortname' column
BEGIN
EXECUTE 'CREATE INDEX mailing$sname$idx ON mailing_tbl USING GIN (to_tsvector(''english'', shortname))';
EXCEPTION WHEN OTHERS THEN
            -- Catch any errors (if full-text index creation fails)
            NULL;
END;

        -- Try creating the index on the 'description' column
BEGIN
EXECUTE 'CREATE INDEX mailing$descr$idx ON mailing_tbl USING GIN (to_tsvector(''english'', description))';
EXCEPTION WHEN OTHERS THEN
            -- Catch any errors (if full-text index creation fails)
            NULL;
END;
END;
$$;

INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date, timestamp) VALUES (1, 'recipient.binding_history.rebuild_trigger_on_startup', 'true', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

CREATE TABLE db_schema_snapshot_tbl
(
    version_number VARCHAR(15),
    schema_json    TEXT,
    timestamp      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_DB_SCHEMA_SNAPSHOT PRIMARY KEY (version_number)
);
COMMENT ON TABLE db_schema_snapshot_tbl IS 'Stores DB schema snapshots';
COMMENT ON COLUMN db_schema_snapshot_tbl.version_number IS 'Version of snapshot';
COMMENT ON COLUMN db_schema_snapshot_tbl.schema_json IS 'JSON representation of DB schema (tables, columns, their types)';
COMMENT ON COLUMN db_schema_snapshot_tbl.timestamp IS 'Creation timestamp';

CREATE TABLE system_message_config_tbl
(
    type          SMALLINT     NOT NULL,
    locale        VARCHAR(5)   NOT NULL,
    subject       VARCHAR(200) NOT NULL,
    reply_email   VARCHAR(200) NOT NULL,
    headline      VARCHAR(255),
    subline       VARCHAR(255),
    content       TEXT,
    legal_notice  TEXT         NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT sysmsg$tl$uq UNIQUE (type, locale)
);
COMMENT ON TABLE system_message_config_tbl IS 'Stores configuration data for system messages';
COMMENT ON COLUMN system_message_config_tbl.type IS 'Type of the message. 0 - password expiration, 1 - password changed, 2 - password reset, 3 - mandatory voucher, 4 - email changed, 5 - security code, 6 - admin welcome, 7 - too large target';
COMMENT ON COLUMN system_message_config_tbl.locale IS 'Language code of the system message (e.g., en, de)';
COMMENT ON COLUMN system_message_config_tbl.subject IS 'Subject of system message';
COMMENT ON COLUMN system_message_config_tbl.reply_email IS 'Email for reply';
COMMENT ON COLUMN system_message_config_tbl.headline IS 'Headline content of system message';
COMMENT ON COLUMN system_message_config_tbl.subline IS 'Subline content of system message';
COMMENT ON COLUMN system_message_config_tbl.content IS 'Content of system message';
COMMENT ON COLUMN system_message_config_tbl.legal_notice IS 'Legal notice of system message';
COMMENT ON COLUMN system_message_config_tbl.creation_date IS 'Creation date of the configuration';
COMMENT ON COLUMN system_message_config_tbl.change_date IS 'Change date of configuration';

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (0, 'en', 'Your password expires in {5} days', 'support@agnitas.de', 'Your password is about to expire', '', 'Hello {0} {1},
<br />
<br />
the password for user access {2} in client {3} (ID {4}) expires in {5} days.
<br />
Please update your password so that your access is not deactivated.
<br />
If you were not able to change the password in time, please contact an administrator or AGNITAS Support to reactivate the user access.
<br />
<br />
Sincerely yours
<br />
<br />
Your AGNITAS team', 'AGNITAS AG, Werner-Eckert-Str. 6, D-81829 Munich
<br />
Registry Court Munich, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Contact: <a href="https://www.agnitas.de/en">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Management Board: Martin Aschoff
<br />
Chairman of the Board: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/en/data-protection-statement/" style="color: #444444; " target="_blank">Data Protection Statement</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (0, 'de', 'Ihr Passwort läuft in {5} Tagen ab', 'support@agnitas.de', 'Ihr Passwort läuft bald ab', '', 'Hallo {0} {1},
<br />
<br />
das Passwort für den Benutzerzugang {2} im Mandanten {3} (ID {4}) läuft in {5} Tagen ab.
<br />
Bitte aktualisieren Sie Ihr Passwort, damit Ihr Zugang nicht deaktiviert wird.
<br />
<br />
Falls Sie das Passwort nicht rechtzeitig ändern konnten, wenden Sie sich bitte an einen Administrator oder den AGNITAS Support, um den Benutzerzugang wieder zu aktivieren.
<br />
<br />
Mit freundlichen Gr&uuml;&szlig;en
<br />
<br />
Ihr AGNITAS-Team', 'AGNITAS AG, Werner-Eckert-Str. 6, 81829 M&uuml;nchen
<br />
Registergericht München, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Kontakt: <a href="https://www.agnitas.de">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Vorstand: Martin Aschoff
<br />
Aufsichtsratsvorsitzender: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/datenschutzerklaerung/" style="color: #444444; " target="_blank">Datenschutzerkl&auml;rung</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (1, 'en', 'EMM user password changed', 'support@agnitas.de', 'Password changed', '', 'Dear EMM user,
<br />
<br />
Your password has just been changed. If you made the change yourself or the change was made on your behalf, you do not need to do anything else. If you have not initiated this change, please change the password immediately using the forgotten password function and inform AGNITAS support.
<br />
<br />
Sincerely yours
<br />
<br />
Your AGNITAS team', 'AGNITAS AG, Werner-Eckert-Str. 6, D-81829 Munich
<br />
Registry Court Munich, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Contact: <a href="https://www.agnitas.de/en">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Management Board: Martin Aschoff
<br />
Chairman of the Board: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/en/data-protection-statement/" style="color: #444444; " target="_blank">Data Protection Statement</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (1, 'de', 'EMM-Benutzer-Passwort geändert', 'support@agnitas.de', 'Passwortänderung', '', 'Lieber EMM-Nutzer,
<br />
<br />
Ihr Passwort wurde soeben ge&auml;ndert. Wenn Sie die &Auml;nderung selbst durchgef&uuml;hrt haben oder die &Auml;nderung in Ihrem Auftrag ausgef&uuml;hrt wurde, brauchen Sie nichts weiter zu tun. Sollte diese &Auml;nderung nicht durch Sie angestoßen worden sein, &auml;ndern Sie bitte umgehend über die Passwort-vergessen-Funktion das Passwort und informieren Sie den AGNITAS-Support.
<br />
<br />
Mit freundlichen Gr&uuml;&szlig;en
<br />
<br />
Ihr AGNITAS-Team', 'AGNITAS AG, Werner-Eckert-Str. 6, 81829 M&uuml;nchen
<br />
Registergericht München, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Kontakt: <a href="https://www.agnitas.de">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Vorstand: Martin Aschoff
<br />
Aufsichtsratsvorsitzender: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/datenschutzerklaerung/" style="color: #444444; " target="_blank">Datenschutzerkl&auml;rung</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (2, 'en', 'Your password reset for EMM', 'support@agnitas.de', 'ACCESS DATA', 'Reset password', 'Hello {2} {3},
<br />
<br />
To reset your password simply open the following link in your preferred browser:
<br />
<br />
<span style="margin:0"><a href="{1}" style="color:#0071b9;" target="_blank"><strong>Set password now*</strong></a>
<br />
<br />
* The link is valid for 30 minutes.
<br />
<br />
Your AGNITAS EMM
<br />
<br />
PS: For support please contact <a href="mailto:support@agnitas.de" style="color:#222222;">support@agnitas.de</a>', 'AGNITAS AG, Werner-Eckert-Str. 6, D-81829 Munich
<br />
Registry Court Munich, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Contact: <a href="https://www.agnitas.de/en">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Management Board: Martin Aschoff
<br />
Chairman of the Board: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/en/data-protection-statement/" style="color: #444444; " target="_blank">Data Protection Statement</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (2, 'de', 'Daten zum Zurücksetzen Ihres EMM-Passworts', 'support@agnitas.de', 'ZUGANGSDATEN',
        'Passwort zurücksetzen', 'Hallo {2} {3},
<br />
<br />
zum Zur&uuml;cksetzen Ihres Passwortes &ouml;ffnen Sie einfach folgenden Link in Ihrem bevorzugten Browser:
<br />
<br />
<span style="margin:0"><a href="{1}" style="color:#0071b9;" target="_blank"><strong>Jetzt Passwort vergeben*</strong></a>
<br />
<br />
* Der Link gilt 30 Minuten.
<br />
<br />
Ihr AGNITAS EMM
<br />
<br />
PS: F&uuml;r Unterst&uuml;tzung kontaktieren Sie <a href="mailto:support@agnitas.de" style="color:#222222;">support@agnitas.de</a>', 'AGNITAS AG, Werner-Eckert-Str. 6, 81829 M&uuml;nchen
<br />
Registergericht München, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Kontakt: <a href="https://www.agnitas.de">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Vorstand: Martin Aschoff
<br />
Aufsichtsratsvorsitzender: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/datenschutzerklaerung/" style="color: #444444; " target="_blank">Datenschutzerkl&auml;rung</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (3, 'en', 'Sending stopped for mailing ''{1}''', 'support@agnitas.de', 'Important Message', '', 'Dear EMM-User,
<br />
<br />
The remaining voucher codes in the reference table ''{0}'' for the mailing ''{1}'' are insufficient to cover all recipients of this mailing.
<br />
As a result, the mailing has been stopped.
<br />
Please provide additional voucher codes in the reference table ''{0}'' or reduce the number of recipients via a target group. You can then reactivate the mailing.
<br />
<br />
Your AGNITAS notification service', 'AGNITAS AG, Werner-Eckert-Str. 6, D-81829 Munich
<br />
Registry Court Munich, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Contact: <a href="https://www.agnitas.de/en">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Management Board: Martin Aschoff
<br />
Chairman of the Board: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/en/data-protection-statement/" style="color: #444444; " target="_blank">Data Protection Statement</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (3, 'de', 'Versand gestoppt für Mailing "{1}"', 'support@agnitas.de', 'Wichtige Mitteilung', '', 'Lieber EMM-Nutzer,
<br />
<br />
Die vorhandene Restmenge an Gutscheincodes aus der Referenztabelle "{0}" für das Mailing "{1}" reicht nicht für alle Empfänger dieses Mailings aus.
<br />
Das Mailing wurde daher gestoppt.
<br />
Stellen Sie zusätzliche Gutscheincodes in der Referenztabelle "{0}" bereit oder reduzieren Sie die Empfängermenge mittels einer Zielgruppe. Anschließend können Sie das Mailing wieder aktivieren.
<br />
<br />
Ihr AGNITAS Benachrichtigungsservice', 'AGNITAS AG, Werner-Eckert-Str. 6, 81829 M&uuml;nchen
<br />
Registergericht München, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Kontakt: <a href="https://www.agnitas.de">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Vorstand: Martin Aschoff
<br />
Aufsichtsratsvorsitzender: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/datenschutzerklaerung/" style="color: #444444; " target="_blank">Datenschutzerkl&auml;rung</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (4, 'en', 'EMM user e-mail changed', 'support@agnitas.de', 'EMM USER E-MAIL CHANGED',
        'Your EMM user e-mail has just been changed.', 'Dear {0},
<br />
<br />
Your e-mail has just been changed to {1}.
<br />
If you made the change yourself or the change was made on your behalf, you can safely disregard this email. If you have not initiated this change, please inform AGNITAS support.', 'AGNITAS AG, Werner-Eckert-Str. 6, D-81829 Munich
<br />
Registry Court Munich, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Contact: <a href="https://www.agnitas.de/en">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Management Board: Martin Aschoff
<br />
Chairman of the Board: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/en/data-protection-statement/" style="color: #444444; " target="_blank">Data Protection Statement</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (4, 'de', 'E-Mail-Adresse des EMM Benutzers wurde geändert', 'support@agnitas.de', 'E-MAIL-ADRESSENÄNDERUNG',
        'Die E-Mail-Adresse ihres EMM Benutzers wurde geändert', 'Hallo {0},
<br />
<br />
Ihre E-Mail-Adresse wurde gerade in {1} geändert.
<br />
Wenn Sie die Änderung selbst vorgenommen haben oder die Änderung in Ihrem Namen vorgenommen wurde, brauchen Sie nichts weiter zu tun. Wenn Sie diese Änderung nicht veranlasst haben, informieren Sie bitte den AGNITAS-Support.', 'AGNITAS AG, Werner-Eckert-Str. 6, 81829 M&uuml;nchen
<br />
Registergericht München, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Kontakt: <a href="https://www.agnitas.de">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Vorstand: Martin Aschoff
<br />
Aufsichtsratsvorsitzender: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/datenschutzerklaerung/" style="color: #444444; " target="_blank">Datenschutzerkl&auml;rung</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (5, 'en', 'Security code for EMM login', 'support@agnitas.de', 'EMM LOGIN', 'Security code', 'Hello {2} {3},
<br />
<br />
Use the following security code to log in to EMM.
<br />
The username shows you the authenticity of the email.
<br />
<br />
User name at login: <strong>{0}</strong>
<br />
Your security code: <strong>{1}</strong>
<br />
<br />
For assistance, contact <a href="mailto:support@agnitas.de" style="color:#0071b9;">support@agnitas.de</a>
<br />
<br />
Your AGNITAS EMM<br />
<br />
PS: If you enable "trust this device" and allow cookies, you will not need to authenticate the next time you log in.', 'AGNITAS AG, Werner-Eckert-Str. 6, D-81829 Munich
<br />
Registry Court Munich, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Contact: <a href="https://www.agnitas.de/en">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Management Board: Martin Aschoff
<br />
Chairman of the Board: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/en/data-protection-statement/" style="color: #444444; " target="_blank">Data Protection Statement</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (5, 'de', 'Sicherheitscode für die EMM-Anmeldung', 'support@agnitas.de', 'EMM LOGIN', 'Sicherheitscode', 'Hallo {2} {3},
<br />
<br />
verwenden Sie folgenden Sicherheitscode für die Anmeldung im EMM.
<br />
Der Benutzername zeigt Ihnen die Echtheit der E-Mail an.
<br />
<br />
Benutzername bei der Anmeldung: <strong>{0}</strong>
<br />
Ihr Sicherheitscode: <strong>{1}</strong>
<br />
<br />
F&uuml;r Unterst&uuml;tzung kontaktieren Sie <a href="mailto:support@agnitas.de" style="color:#0071b9;">support@agnitas.de</a>
<br />
<br />
Ihr AGNITAS EMM
<br />
<br />
PS: Wenn Sie "Diesem Gerät vertrauen" aktivieren und Cookies zulassen, müssen Sie sich bei der nächsten Anmeldung nicht authentifizieren.', 'AGNITAS AG, Werner-Eckert-Str. 6, 81829 M&uuml;nchen
<br />
Registergericht München, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Kontakt: <a href="https://www.agnitas.de">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Vorstand: Martin Aschoff
<br />
Aufsichtsratsvorsitzender: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/datenschutzerklaerung/" style="color: #444444; " target="_blank">Datenschutzerkl&auml;rung</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (6, 'en', 'Your access data for EMM', 'support@agnitas.de', 'ACCESS DATA',
        'Your access to the EMM has been activated', 'Hello {2} {3},
<br />
<br />
The login page can be reached via the AGNITAS website in the header. <br>Tip: Save the link as a bookmark.
<br />
<br />
User name: <b>{0}</b>
<br />
<a href="{1}" style="color:#0071b9;" target="_blank"><b>Set password now*</b></a>
<br />
<br />
* The link is valid for 3 days.
<br />
<br />
Your AGNITAS EMM
<br />
<br />
PS: For support please contact <a href="mailto:support@agnitas.de" style="color:#222222;">support@agnitas.de</a>', 'AGNITAS AG, Werner-Eckert-Str. 6, D-81829 Munich
<br />
Registry Court Munich, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Contact: <a href="https://www.agnitas.de/en">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Management Board: Martin Aschoff
<br />
Chairman of the Board: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/en/data-protection-statement/" style="color: #444444; " target="_blank">Data Protection Statement</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (6, 'de', 'Ihre Zugangsdaten zum EMM', 'support@agnitas.de', 'ZUGANGSDATEN',
        'Ihr Zugang zum EMM wurde freigeschaltet', 'Hallo {2} {3},
<br />
<br />
die Login-Seite erreichen Sie über die AGNITAS-Website oben im Header. Tipp: Speichern Sie den Link als Lesezeichen.
<br />
<br />
Benutzername: <b>{0}</b>
<br />
<a href="{1}" style="color:#0071b9;" target="_blank"><b>Jetzt Passwort vergeben*</b></a>
<br />
<br />
* Der Link gilt 3 Tage.
<br />
<br />
Ihr AGNITAS EMM
<br />
<br />
PS: Für Unterstützung kontaktieren Sie <a href="mailto:support@agnitas.de" style="color:#222222;">support@agnitas.de</a>', 'AGNITAS AG, Werner-Eckert-Str. 6, 81829 M&uuml;nchen
<br />
Registergericht München, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Kontakt: <a href="https://www.agnitas.de">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Vorstand: Martin Aschoff
<br />
Aufsichtsratsvorsitzender: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/datenschutzerklaerung/" style="color: #444444; " target="_blank">Datenschutzerkl&auml;rung</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (7, 'en', '<REPLACE_ME>', 'support@agnitas.de', '<REPLACE_ME>', '', '<REPLACE_ME>', 'AGNITAS AG, Werner-Eckert-Str. 6, D-81829 Munich
<br />
Registry Court Munich, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Contact: <a href="https://www.agnitas.de/en">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Management Board: Martin Aschoff
<br />
Chairman of the Board: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/en/data-protection-statement/" style="color: #444444; " target="_blank">Data Protection Statement</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (7, 'de', 'Versand für [Mailingname] gestoppt', 'support@agnitas.de', 'Wichtige Mitteilung', '', 'Lieber EMM-Nutzer,
<br />
<br />
Der angegebene Schwellwert von [xxx] für das Mailing "[Mailingname]" wurde überschritten und der Versand für das Mailing gestoppt.
<br />
<br />
Bitte überprüfen Sie die eingestellte Zielgruppe bevor Sie das Mailing wieder aktivieren.
<br />
<br />
Ihr AGNITAS Benachrichtigungsservice', 'AGNITAS AG, Werner-Eckert-Str. 6, 81829 M&uuml;nchen
<br />
Registergericht München, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Kontakt: <a href="https://www.agnitas.de">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Vorstand: Martin Aschoff
<br />
Aufsichtsratsvorsitzender: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/datenschutzerklaerung/" style="color: #444444; " target="_blank">Datenschutzerkl&auml;rung</a>');
INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (8, 'de', 'Sicherheitscode für EMM-Supervisor-Zugang', 'support@agnitas.de', 'EMM LOGIN', 'Sicherheitscode', 'Hallo {2} {3},
<br>
<br>
verwenden Sie folgenden Sicherheitscode für die Anmeldung im EMM.
<br>
Der Benutzername zeigt Ihnen die Echtheit der E-Mail an.
<br>
<br>
Benutzername bei der Anmeldung: <strong>{0}</strong>
<br>
Ihr Sicherheitscode: <strong>{1}</strong>
<br>
<br>
Für Unterstützung kontaktieren Sie <a href="mailto:support@agnitas.de" style="color:#0071b9;">support@agnitas.de</a>
<br>
<br>
Ihr AGNITAS EMM
<br>
<br>
PS: Wenn Sie "Diesem Gerät vertrauen" aktivieren und Cookies zulassen, müssen Sie sich bei der nächsten Anmeldung nicht authentifizieren.', 'AGNITAS AG, Werner-Eckert-Str. 6, 81829 M&uuml;nchen
<br />
Registergericht München, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Kontakt: <a href="https://www.agnitas.de">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Vorstand: Martin Aschoff
<br />
Aufsichtsratsvorsitzender: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/datenschutzerklaerung/" style="color: #444444; " target="_blank">Datenschutzerkl&auml;rung</a>');

INSERT INTO system_message_config_tbl (type, locale, subject, reply_email, headline, subline, content, legal_notice)
VALUES (8, 'en', 'Security Code for EMM supervisor login', 'support@agnitas.de', 'EMM LOGIN', 'Security code', 'Hello {2} {3},
<br />
<br />
Use the following security code to log in to EMM.
<br />
The username shows you the authenticity of the email.
<br />
<br />
User name at login: <strong>{0}</strong>
<br />
Your security code: <strong>{1}</strong>
<br />
<br />
For assistance, contact <a href="mailto:support@agnitas.de" style="color:#0071b9;">support@agnitas.de</a>
<br />
<br />
Your AGNITAS EMM<br />
<br />
PS: If you enable "trust this device" and allow cookies, you will not need to authenticate the next time you log in.', 'AGNITAS AG, Werner-Eckert-Str. 6, D-81829 Munich
<br />
Registry Court Munich, HRB 126 104, USt-IdNr. DE 201 88 33 28
<br />
Contact: <a href="https://www.agnitas.de/en">https://www.agnitas.de</a>, <a href="mailto:info@agnitas.ag">info@agnitas.ag</a>
<br />
Management Board: Martin Aschoff
<br />
Chairman of the Board: Norbert Stangl
<br />
<br />
<a href="https://www.agnitas.de/en/data-protection-statement/" style="color: #444444; " target="_blank">Data Protection Statement</a>');

COMMIT;
