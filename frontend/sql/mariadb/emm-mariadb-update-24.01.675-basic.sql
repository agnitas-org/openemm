/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
	(SELECT MAX(id) + 1, 'MigrationJobWorker', CURRENT_TIMESTAMP, null, 0, 'OK', 0, 0, 'MoTuWeThFr:1500', CURRENT_TIMESTAMP, null, 'com.agnitas.service.job.MigrationJobWorker', 0  FROM job_queue_tbl);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('24.01.675', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;