/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

-- DATA_ACCOUNTING --> ADMIN*, MAILING*, SERVER*, WS*, ..
CREATE TABLESPACE data_accounting OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/1';

-- DATA_BOUNCE --> BOUNCE tables (BOUNCE_COLLECT_TBL, BOUNCE_TBL, BOUNCE_TRANSLATE_TBL, SOFTBOUNCE_EMAIL_TBL)
CREATE TABLESPACE data_bounce OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/2';

-- DATA_CUST_TABLE --> Accountspecific tables
CREATE TABLESPACE data_cust_table OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/3';

-- DATA_CUST_INDEX --> Indices of tables in DATA_CUST and DATA_CUST_TABLE
CREATE TABLESPACE data_cust_index OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/4';

-- DATA_EMMAUX --> BIRT*, IMPORT*, REPORT*, SWYN*
CREATE TABLESPACE data_emmaux OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/5';

-- DATA_EMMAUX_IDX --> Indices of tables in DATA_EMMAUX
CREATE TABLESPACE data_emmaux_idx OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/6';

-- DATA_EMM_PREDEV --> PREDELIVERY tables (PREDELIVERY_TEST_ITEM_SPAM_TBL, PREDELIVERY_TEST_ITEM_TBL, PREDELIVERY_TEST_TBL)
CREATE TABLESPACE data_emm_predev OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/7';

-- DATA_EMM_UNDO  --> UNDO tables (UNDO_COMPONENT_TBL, UNDO_DYN_CONTENT_TBL, UNDO_MAILING_TBL)
CREATE TABLESPACE data_emm_undo OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/8';

-- DATA_WAREHOUSE --> Accountspecific tables HST_CUSTOMER_<CID>_BINDING_TBL and ONEPIXELLOG_DEVICE_<CID>_TBL
CREATE TABLESPACE data_warehouse OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/9';

-- INDEX_DATA_WAREHOUSE --> Indices of ONEPIXELLOG_DEVICE_<CID>_TBL
CREATE TABLESPACE index_data_warehouse OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/10';

-- DATA_SUCCESS --> Accountspecific table SUCCESS_<CID>_TBL
CREATE TABLESPACE data_success OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/11';

-- INDEX_SUCCESS --> Indices of SUCCESS_<CID>_TBL
CREATE TABLESPACE index_success OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/12';

-- CUSTOMER_HISTORY --> hst_customer_<CID>_binding_tbl
CREATE TABLESPACE customer_history OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/13';

-- CUSTOMER_HISTORY_INDEX --> Indices of tables in CUSTOMER_HISTORY
CREATE TABLESPACE index_customer_history OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/14';

-- DATA_WORKFLOW --> Tables for campaign manager
CREATE TABLESPACE data_workflow OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/15';

-- INDEX_WORKFLOW --> Indices of tables for campaign manager
CREATE TABLESPACE index_workflow OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/16';

-- DATA_TEMP --> TBS for high frequently changing data
CREATE TABLESPACE data_temp OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/17';

CREATE TABLESPACE data_default OWNER agnitas LOCATION '/var/lib/postgresql/tablespace/18';
