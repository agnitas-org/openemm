/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

DELETE FROM webservice_perm_group_perm_tbl WHERE endpoint='ListMailingsInMailinglists';
DELETE FROM webservice_perm_group_perm_tbl WHERE endpoint='ListContentBlock';

INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (2, 'ListSubscribers');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'ListMailingsInMailinglist');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (4, 'ListContentBlocks');
INSERT INTO webservice_perm_group_perm_tbl (group_ref, endpoint) VALUES (3, 'AddSubscribers');

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('22.01.003', CURRENT_USER, CURRENT_TIMESTAMP);
	
COMMIT;
