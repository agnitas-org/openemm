/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- fills entity_type, entity_id, entity_execution column according to old 'type' column
UPDATE recipients_report_tbl SET entity_type = 1 WHERE type = 'IMPORT_REPORT';
UPDATE recipients_report_tbl SET entity_type = 2 WHERE type = 'EXPORT_REPORT';

COMMIT;
