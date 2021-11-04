/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE rulebased_sent_tbl ADD COLUMN creation_date TIMESTAMP NULL COMMENT 'entry creation date';
ALTER TABLE rulebased_sent_tbl ADD COLUMN change_date TIMESTAMP NULL COMMENT 'entry last change';
ALTER TABLE rulebased_sent_tbl ADD COLUMN clearance INT(1) NOT NULL DEFAULT 1 COMMENT 'if set to a value larger than 0, sending of this mailing is allowed, otherwise sending is blocked';
ALTER TABLE rulebased_sent_tbl ADD COLUMN clearance_change TIMESTAMP NULL COMMENT 'last change of clearance flag';
ALTER TABLE rulebased_sent_tbl ADD COLUMN clearance_origin VARCHAR(128) COMMENT 'host which made the last change on clearance';
ALTER TABLE rulebased_sent_tbl ADD CONSTRAINT rulebase$mid$pk PRIMARY KEY (mailing_id);

ALTER TABLE mailing_tbl ADD COLUMN clearance_email VARCHAR(500) COMMENT 'email address(es) to be informed if a rule based mailing exceeds clearance_threshold';
ALTER TABLE mailing_tbl ADD COLUMN clearance_threshold INT(11) COMMENT 'threshold, when rulebased mailing exceeds this value an email is sent to clearance_email and the generated mails are not sent';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
	VALUES ('21.04.454', CURRENT_USER, CURRENT_TIMESTAMP);
