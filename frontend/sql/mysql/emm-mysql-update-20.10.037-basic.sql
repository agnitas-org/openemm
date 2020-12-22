/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package)
    (SELECT 'mailing.export', 'Mailing', NULL, 13, NULL FROM DUAL
        WHERE NOT EXISTS(SELECT 1 FROM permission_tbl WHERE permission_name = 'mailing.export'));

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package)
    (SELECT 'forms.export', 'Forms', NULL, 3, NULL FROM DUAL
        WHERE NOT EXISTS(SELECT 1 FROM permission_tbl WHERE permission_name = 'forms.export'));

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('20.10.037', CURRENT_USER, CURRENT_TIMESTAMP);
