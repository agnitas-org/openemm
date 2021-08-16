/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

UPDATE rdir_url_tbl SET full_url = REPLACE(full_url, '/form.do?', '/form.action?') WHERE full_url LIKE '%/form.do?%';
UPDATE component_tbl SET emmblock = REPLACE(emmblock, '/form.do?', '/form.action?') WHERE emmblock LIKE '%/form.do?%';
UPDATE dyn_content_tbl SET dyn_content = REPLACE(dyn_content, '/form.do?', '/form.action?') WHERE dyn_content LIKE '%/form.do?%';
UPDATE userform_tbl SET success_template = REPLACE(success_template, '/form.do?', '/form.action?') WHERE success_template LIKE '%/form.do?%';
UPDATE userform_tbl SET error_template = REPLACE(error_template, '/form.do?', '/form.action?') WHERE error_template LIKE '%/form.do?%';
UPDATE actop_execute_script_tbl SET script = REPLACE(script, '/form.do?', '/form.action?') WHERE script LIKE '%/form.do?%';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.10.385', CURRENT_USER, CURRENT_TIMESTAMP);
