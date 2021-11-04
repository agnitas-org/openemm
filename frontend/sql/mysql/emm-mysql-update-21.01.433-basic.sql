/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO company_info_tbl (company_id, cname, cvalue, description) VALUES
  (0, 'webservice.default_api_call_limits', '864000/PT1D;2400/PT1M', 'Default settings for API rate limit');
  
INSERT INTO company_info_tbl (company_id, cname, cvalue, description) VALUES
  (610, 'development.useNewWebserviceRateLimiting', 'true', 'Enabled webservice rate limit');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.01.433', CURRENT_USER, CURRENT_TIMESTAMP);
