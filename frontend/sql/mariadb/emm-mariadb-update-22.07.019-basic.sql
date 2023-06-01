/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE job_queue_tbl SET job_comment = 'Update customers lastopen_date and lastclick_date as availbe with data from access_data_tbl', startaftererror = 1 WHERE description = 'AccessDataAggregation';
UPDATE job_queue_tbl SET job_comment = 'Aggregate multiple entries for mailing component downloads in rdir_traffic_amount_<companyID>_tbl into single entries per day in rdir_traffic_agr_<companyID>_tbl', startaftererror = 1 WHERE description = 'AggregateRdirTrafficStatisticJobWorker';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('22.07.019', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
