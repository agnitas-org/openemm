/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

INSERT INTO permission_tbl (permission_name, category, sub_category, sort_order, feature_package) VALUES ('mailing.trackablelinks.url.change', 'Premium', 'Mailing', 3, 'Mailing');
INSERT INTO admin_permission_tbl (admin_id, security_token) (SELECT admin_id, 'mailing.trackablelinks.url.change' FROM admin_permission_tbl a WHERE security_token = 'mailing.trackablelinks.url.edit' AND NOT EXISTS (SELECT 1 FROM admin_permission_tbl b WHERE security_token = 'mailing.trackablelinks.url.change' AND a.admin_id = b.admin_id));
INSERT INTO admin_group_permission_tbl (admin_group_id, security_token) (SELECT admin_group_id, 'mailing.trackablelinks.url.change' FROM admin_group_permission_tbl a WHERE security_token = 'mailing.trackablelinks.url.edit' AND NOT EXISTS (SELECT 1 FROM admin_group_permission_tbl b WHERE security_token = 'mailing.trackablelinks.url.change' AND a.admin_group_id = b.admin_group_id));
INSERT INTO company_permission_tbl (company_id, security_token) (SELECT company_id, 'mailing.trackablelinks.url.change' FROM company_permission_tbl a WHERE security_token = 'mailing.trackablelinks.url.edit' AND NOT EXISTS (SELECT 1 FROM company_permission_tbl b WHERE security_token = 'mailing.trackablelinks.url.change' AND a.company_id = b.company_id));

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('19.07.325', CURRENT_USER, CURRENT_TIMESTAMP);
