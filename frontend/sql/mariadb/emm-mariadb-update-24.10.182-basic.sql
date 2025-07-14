/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

DELETE FROM config_tbl WHERE class='webpush' AND (name='filesink_basedir' OR name LIKE 'filesink_basedir.%');
DELETE FROM company_info_tbl WHERE cname = 'webpush.filesink_basedir';

DELETE FROM config_tbl WHERE class='webpush' AND (name='result_basedir' OR name LIKE 'result_basedir.%');
DELETE FROM company_info_tbl WHERE cname = 'webpush.result_basedir';

DELETE FROM config_tbl WHERE class='webpush' AND (name='sftp.host' OR name LIKE 'sftp.host.%');
DELETE FROM company_info_tbl WHERE cname = 'webpush.sftp.host';

DELETE FROM config_tbl WHERE class='webpush' AND (name='sftp.user' OR name LIKE 'sftp.user.%');
DELETE FROM company_info_tbl WHERE cname = 'webpush.sftp.user';

DELETE FROM config_tbl WHERE class='webpush' AND (name='sftp.basepath' OR name LIKE 'sftp.basepath.%');
DELETE FROM company_info_tbl WHERE cname = 'webpush.sftp.basepath';

DELETE FROM config_tbl WHERE class='webpush' AND (name='sftp.sshkey.file' OR name LIKE 'sftp.sshkey.file.%');
DELETE FROM company_info_tbl WHERE cname = 'webpush.sftp.sshkey.file';

DELETE FROM config_tbl WHERE class='webpush' AND (name='sftp.sshkey.passphrase_encrypted' OR name LIKE 'sftp.sshkey.passphrase_encrypted.%');
DELETE FROM company_info_tbl WHERE cname = 'webpush.sftp.sshkey.passphrase_encrypted';

DELETE FROM config_tbl WHERE class='development' AND (name='webpush.useNewPnostman' OR name LIKE 'webpush.useNewPnostman.%');
DELETE FROM company_info_tbl WHERE cname = 'development.webpush.useNewPnostman';


INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('24.10.182', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
