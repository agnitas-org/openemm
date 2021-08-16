/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE import_profile_mediatype_tbl (
	import_profile_id          INT(11) UNSIGNED NOT NULL COMMENT 'references import_profile_tbl.id',
	mediatype                  INT(11) UNSIGNED NOT NULL COMMENT '0=EMAIL,2=POST,4=SMS',
	PRIMARY KEY (import_profile_id, mediatype),
	CONSTRAINT import_profile_media_fk FOREIGN KEY (import_profile_id) REFERENCES import_profile_tbl (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Mediatypes used by an import';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.01.120', CURRENT_USER, CURRENT_TIMESTAMP);
