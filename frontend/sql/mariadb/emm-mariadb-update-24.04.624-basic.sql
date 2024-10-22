/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE dyn_target_tbl
SET target_sql = '(((date_format(cust.creation_date, ''%Y%m%d'') > date_format((current_timestamp) - INTERVAL (91) * 86400 SECOND, ''%Y%m%d'')) AND ((cust.lastopen_date IS NULL) AND ((cust.lastclick_date IS NULL) AND ((cust.sys_tracking_veto IS NULL) OR (cust.sys_tracking_veto <> 1))))) AND (date_format(cust.lastsend_date, ''%Y%m%d'') < date_format((current_timestamp) - INTERVAL (90) * 86400 SECOND, ''%Y%m%d'')))'
WHERE target_shortname='EMM Target Group: Lead'
AND target_sql='(((to_char(cust.creation_date, ''YYYYMMDD'') > to_char((SYSDATE) - (91), ''YYYYMMDD'')) AND ((cust.lastopen_date IS NULL) AND (cust.lastclick_date IS NULL))) OR (((cust.sys_tracking_veto IS NULL) OR (cust.sys_tracking_veto <> 1)) OR (to_char(cust.lastsend_date, ''YYYYMMDD'') = to_char((SYSDATE) - (90), ''YYYYMMDD''))))';

UPDATE dyn_target_tbl
SET target_sql='(date_format(cust.creation_date, ''%Y%m%d'') > date_format((current_timestamp) - INTERVAL (31) * 86400 SECOND, ''%Y%m%d''))'
WHERE target_shortname='EMM Target Group: Newcomer'
AND target_sql='(to_char(cust.creation_date, ''YYYYMMDD'') > to_char((SYSDATE) - (31), ''YYYYMMDD''))';

UPDATE dyn_target_tbl
SET target_sql='((date_format(cust.lastopen_date, ''%Y%m%d'') > date_format((current_timestamp) - INTERVAL (91) * 86400 SECOND, ''%Y%m%d'')) OR ((date_format(cust.lastclick_date, ''%Y%m%d'') > date_format((current_timestamp) - INTERVAL (91) * 86400 SECOND, ''%Y%m%d'')) OR (cust.sys_tracking_veto = 1)))'
WHERE target_shortname='EMM Target Group: Opportunity'
AND target_sql='((to_char(cust.lastopen_date, ''YYYYMMDD'') > to_char((SYSDATE) - (91), ''YYYYMMDD'')) OR ((to_char(cust.lastclick_date, ''YYYYMMDD'') > to_char((SYSDATE) - (91), ''YYYYMMDD'')) OR (cust.sys_tracking_veto = 1)))';

UPDATE dyn_target_tbl
SET target_sql='((date_format(cust.creation_date, ''%Y%m%d'') < date_format((current_timestamp) - INTERVAL (90) * 86400 SECOND, ''%Y%m%d'')) AND ((date_format(cust.lastsend_date, ''%Y%m%d'') > date_format((current_timestamp) - INTERVAL (91) * 86400 SECOND, ''%Y%m%d'')) AND (((cust.lastopen_date IS NULL) OR (date_format(cust.lastopen_date, ''%Y%m%d'') < date_format((current_timestamp) - INTERVAL (90) * 86400 SECOND, ''%Y%m%d''))) AND (((cust.lastclick_date IS NULL) OR (date_format(cust.lastclick_date, ''%Y%m%d'') < date_format((current_timestamp) - INTERVAL (90) * 86400 SECOND, ''%Y%m%d''))) AND ((cust.sys_tracking_veto IS NULL) OR (cust.sys_tracking_veto <> 1))))))'
WHERE (target_shortname='EMM Target Group: Sleeper' or target_shortname='EMM Target Group: Sleepers')
AND target_sql='((to_char(cust.creation_date, ''YYYYMMDD'') < to_char((SYSDATE) - (90), ''YYYYMMDD'')) AND ((to_char(cust.lastsend_date, ''YYYYMMDD'') > to_char((SYSDATE) - (91), ''YYYYMMDD'')) AND (((cust.lastopen_date IS NULL) OR (to_char(cust.lastopen_date, ''YYYYMMDD'') < to_char((SYSDATE) - (90), ''YYYYMMDD''))) AND (((cust.lastclick_date IS NULL) OR (to_char(cust.lastclick_date, ''YYYYMMDD'') < to_char((SYSDATE) - (90), ''YYYYMMDD''))) AND ((cust.sys_tracking_veto IS NULL) OR (cust.sys_tracking_veto <> 1))))))';

UPDATE dyn_target_tbl
SET target_sql='(((date_format(cust.lastsend_date, ''%Y%m%d'') < date_format((current_timestamp) - INTERVAL (90) * 86400 SECOND, ''%Y%m%d'')) OR (cust.lastsend_date IS NULL)) AND (date_format(cust.creation_date, ''%Y%m%d'') < date_format((current_timestamp) - INTERVAL (30) * 86400 SECOND, ''%Y%m%d'')))'
WHERE target_shortname='EMM Target Group: Unattended 3 Months'
AND target_sql='((to_char(cust.creation_date, ''YYYYMMDD'') < to_char((SYSDATE) - (30), ''YYYYMMDD'')) AND ((to_char(cust.lastsend_date, ''YYYYMMDD'') < to_char((SYSDATE) - (90), ''YYYYMMDD'')) OR (cust.lastsend_date IS NULL)))';

UPDATE dyn_target_tbl
SET target_sql='(((date_format(cust.lastsend_date, ''%Y%m%d'') < date_format((current_timestamp) - INTERVAL (180) * 86400 SECOND, ''%Y%m%d'')) AND (cust.lastsend_date IS NULL)) AND (date_format(cust.creation_date, ''%Y%m%d'') < date_format((current_timestamp) - INTERVAL (30) * 86400 SECOND, ''%Y%m%d'')))'
WHERE target_shortname='EMM Target Group: Unattended 6 Months'
AND target_sql='((to_char(cust.creation_date, ''YYYYMMDD'') < to_char((SYSDATE) - (30), ''YYYYMMDD'')) AND ((to_char(cust.lastsend_date, ''YYYYMMDD'') < to_char((SYSDATE) - (180), ''YYYYMMDD'')) OR (cust.lastsend_date IS NULL)))';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('24.04.624', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
