/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

-- Execute changes after version is available on all systems: 26.01.313
-- EMMGUI-722
-- Step 2: Delete column 'bk_type' (to be done)
ALTER TABLE recipients_report_tbl DROP COLUMN bk_type;

-- Execute changes after version is available on all systems: 26.01.313
-- GWUA-6469
-- Step 2: Delete columns 'bk_runClass' (to be done)
ALTER TABLE job_queue_tbl DROP COLUMN bk_runClass;

-- Execute changes after version is available on all systems: 26.01.095
-- GWUA-6892
-- Step 1: Rename column 'campaign_id' to 'bk_campaign_id' (to be done)
ALTER TABLE auto_optimization_tbl RENAME COLUMN campaign_id TO bk_campaign_id;

-- Execute changes after version is available on all systems: 26.01.348
-- EMM-10033
DELETE FROM company_info_tbl WHERE cname LIKE '%honeypot.cidr%';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.04.389', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
