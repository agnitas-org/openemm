/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE maildrop_status_tbl ADD depends_on_auto_import_id INTEGER COMMENT 'Optional referenced auto_import_id. Errors in AutoImport block mailing delivery start, See auto_import_ok';
ALTER TABLE maildrop_status_tbl ADD auto_import_ok INTEGER COMMENT 'Status of referenced auto_import_id in depends_on_auto_import_id. (0 = Error, 1 = OK)';
ALTER TABLE maildrop_status_tbl ADD CONSTRAINT mds$dependautoimportid$fk FOREIGN KEY (depends_on_auto_import_id) REFERENCES auto_import_tbl (auto_import_id);

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('22.01.386', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
