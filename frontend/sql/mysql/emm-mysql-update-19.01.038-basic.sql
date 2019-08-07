/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

DELETE FROM company_info_tbl WHERE cname = 'recipient.profile_history.rebuild_trigger_on_startup';
INSERT INTO company_info_tbl (company_id, cname, cvalue, description, creation_date)
	SELECT c.company_id, 'recipient.profile_history.rebuild_trigger_on_startup', 'true', 'Rebuild history trigger on startup (EMM-6256)', CURRENT_TIMESTAMP
	FROM company_tbl c
	WHERE 
	  EXISTS (
	    SELECT 1 
	    FROM config_tbl cfg 
	    WHERE cfg.class='recipient' 
	      AND cfg.name=concat('profile_history.', c.company_id) 
	      AND value IN ('1', 'yes', 'enabled', 'true'))
	  OR EXISTS (
	    SELECT 1 
	    FROM company_info_tbl ci 
	    WHERE ci.company_id=c.company_id 
	      AND cname='recipient.profile_history' 
	      AND cvalue IN ('1', 'yes', 'enabled', 'true'))
	ORDER BY c.company_id;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.01.038', CURRENT_USER, CURRENT_TIMESTAMP);
