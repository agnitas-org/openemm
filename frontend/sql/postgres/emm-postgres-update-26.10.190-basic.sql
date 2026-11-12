/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

DROP INDEX IF EXISTS mailing$sname$idx;
DROP INDEX IF EXISTS mailing$descr$idx;

CREATE INDEX mailing$sname$idx ON mailing_tbl USING GIN (to_tsvector('simple', shortname));
CREATE INDEX mailing$descr$idx ON mailing_tbl USING GIN (to_tsvector('simple', description));
CREATE INDEX IF NOT EXISTS component$emmblock$idx ON component_tbl USING GIN (to_tsvector('simple', emmblock));
CREATE INDEX IF NOT EXISTS dyncontent$content$idx ON dyn_content_tbl USING GIN (to_tsvector('simple', dyn_content));
CREATE INDEX IF NOT EXISTS dyntg$sname$idx ON dyn_target_tbl USING GIN (to_tsvector('simple', target_shortname));
CREATE INDEX IF NOT EXISTS dyntg$descr$idx ON dyn_target_tbl USING GIN (to_tsvector('simple', target_description));

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.10.190', CURRENT_USER, CURRENT_TIMESTAMP);
