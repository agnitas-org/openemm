/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE startup_job_tbl (
    id                         INT(11) UNSIGNED NOT NULL COMMENT 'Job ID',
	classname                  VARCHAR(1000) NOT NULL COMMENT 'Name of job class',
	version                    VARCHAR(20) NOT NULL COMMENT 'EMM version',
	company_id                 INT(11) UNSIGNED NOT NULL COMMENT 'references company (company_tbl) or 0 if job is independent from company',
	enabled                    INT(1) UNSIGNED NOT NULL COMMENT '1 = job is enabled, 0 = job is disabled, others: unused (=disabled)',
	state                      INT(1) UNSIGNED NOT NULL COMMENT 'State of job (see enum class com.agnitas.startuplistener.common.JobState)',
	creation_date              TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'entry creation date',
	change_date                TIMESTAMP NULL COMMENT 'entry last change',
	description                VARCHAR(1000) COMMENT 'Optional description',
	PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Stores information about job to be run on context startup';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('20.01.018', CURRENT_USER, CURRENT_TIMESTAMP);
