/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE tag_tbl ADD deprecated INTEGER  DEFAULT 0 COMMENT 'If true(1), the tag is not shown in WYSIWYG editor and should not be used anymore';

UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnALTER';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnALTERCALC';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnBANNER';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnCUSTOMDATE';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnCUSTOMDATE_DE';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnDATEDB';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnDATEDB_DE';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnDATEDB_LANG';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnYEARCALC';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnYEARCALC_F';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnLABEL';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnLINK';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnTEXT';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnAUTOURL';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnTITLE_EXPIRED';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnTITLE_SHORT';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnPID';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnITAS';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnFIRSTNAME';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnLASTNAME';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnCUSTOMERID';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnDBV';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnDAYS_UNTIL';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnNULL';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnCALC';
UPDATE tag_tbl SET deprecated = 1 WHERE tagname = 'agnCALC2';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('19.04.405', CURRENT_USER, CURRENT_TIMESTAMP);
