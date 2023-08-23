/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO job_queue_tbl ( description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted, job_comment)
	VALUES ('DBCleanerWithService', CURRENT_TIMESTAMP, NULL, 0, 'OK', 1, 0, '0000', CURRENT_TIMESTAMP, NULL, 'org.agnitas.util.quartz.DBCleanerJobWorkerWithService', 1, 'DbCleaner running from 0-3am, Do not change intervalpattern Except ISMF has ordered changing!');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.01.328', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
