/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE prevent_table_drop DROP FOREIGN KEY lock$company_info_tbl;
ALTER TABLE company_info_tbl DROP PRIMARY KEY;
ALTER TABLE company_info_tbl ADD UNIQUE INDEX compif$cidcnmhost$pk (company_id, cname, hostname);
ALTER TABLE prevent_table_drop ADD CONSTRAINT lock$company_info_tbl FOREIGN KEY (signed_id) REFERENCES company_info_tbl (company_id);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('19.10.355', CURRENT_USER, CURRENT_TIMESTAMP);
