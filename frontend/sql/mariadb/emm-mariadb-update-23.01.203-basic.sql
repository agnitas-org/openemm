/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE dyn_target_tbl SET eql='`lastclick_date` IS NOT EMPTY OR `lastopen_date` IS NOT EMPTY' WHERE target_shortname = 'EMM Target Group: Openers and Clickers' AND company_id=1;
UPDATE dyn_target_tbl SET eql='`gender` = 1' WHERE target_shortname = 'EMM Target Group: Gender Female' AND company_id=1;
UPDATE dyn_target_tbl SET eql='`gender` = 0' WHERE target_shortname = 'EMM Target Group: Gender Male' AND company_id=1;
UPDATE dyn_target_tbl SET eql='`gender` = 2' WHERE target_shortname = 'EMM Target Group: Gender Unknown' AND company_id=1;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.01.203', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
