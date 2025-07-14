/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.util.quartz.DBCleanerCompanyCleaningSubWorker'
WHERE runclass = 'org.agnitas.util.quartz.DBCleanerCompanyCleaningSubWorker';

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.util.quartz.DBCleanerJobWorker'
WHERE runclass = 'org.agnitas.util.quartz.DBCleanerJobWorker';

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.util.quartz.DBErrorCheckJobWorker'
WHERE runclass = 'org.agnitas.util.quartz.DBErrorCheckJobWorker';

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.util.quartz.FailedTestDeliveryCleanupJobWorker'
WHERE runclass = 'org.agnitas.util.quartz.FailedTestDeliveryCleanupJobWorker';

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.util.quartz.LoginTrackTableCleanerJobWorker'
WHERE runclass = 'org.agnitas.util.quartz.LoginTrackTableCleanerJobWorker';

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.util.quartz.OpenEMMCompanyWorker'
WHERE runclass = 'org.agnitas.util.quartz.OpenEMMCompanyWorker';

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.util.quartz.RecipientChartPreCalculatorJobWorker'
WHERE runclass = 'org.agnitas.util.quartz.RecipientChartPreCalculatorJobWorker';

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.util.quartz.RecipientHstCleanerJobWorker'
WHERE runclass = 'org.agnitas.util.quartz.RecipientHstCleanerJobWorker';

UPDATE job_queue_tbl
SET runclass = 'com.agnitas.util.quartz.WebserviceLoginTrackTableCleanerJobWorker'
WHERE runclass = 'org.agnitas.util.quartz.WebserviceLoginTrackTableCleanerJobWorker';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('25.01.336', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
