/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE admin_upload_list_tbl (
    admin_id        INT(11) NOT NULL COMMENT 'References admin_tbl.admin_id',
    upload_id       INT(11) NOT NULL COMMENT 'References upload_tbl.upload_id',
    timestamp       TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Entry last change. Can be useful in case of transfer the latest changes from the old design to the new one. Can be removed after EMMGUI-714 redesign tested and upload_tbl.admin_id not in use anymore',
    PRIMARY KEY (admin_id, upload_id)
) COMMENT 'Many to many table for storing uploads available for admin';

ALTER TABLE admin_upload_list_tbl ADD CONSTRAINT adminupload$adminid$fk FOREIGN KEY (admin_id) REFERENCES admin_tbl(admin_id);
ALTER TABLE admin_upload_list_tbl ADD CONSTRAINT adminupload$uploadid$fk FOREIGN KEY (upload_id) REFERENCES upload_tbl(upload_id);

INSERT INTO admin_upload_list_tbl (admin_id, upload_id) SELECT admin_id, upload_id FROM upload_tbl WHERE admin_id IS NOT NULL AND admin_id <> 0;

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('23.07.201', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
