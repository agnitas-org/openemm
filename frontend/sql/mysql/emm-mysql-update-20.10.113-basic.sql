/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE feature_cleanup_tbl (
	company_id                 INT(11) COMMENT 'Client id a feature was deactivated for',
	feature                    VARCHAR(100) COMMENT 'Deactivated feature name',
	deactivation_date          TIMESTAMP COMMENT 'Date of deactivation',
	cleanup_status             INTEGER COMMENT 'State of deactivation: 0 = clean up to do, 1 = cleanup is finished, 2 = no more cleanup (maybe reactivated)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT 'Storage of data of deactivated features for later DbCleaner actions';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.10.113', CURRENT_USER, CURRENT_TIMESTAMP);
