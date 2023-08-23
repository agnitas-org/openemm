/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

CREATE TABLE mailloop_log_tbl (
	mailloop_log_id	           INTEGER UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'unique key',
	rid		                   VARCHAR(32) COMMENT 'either an internal identifier or a numeric value >0 referencing mailloop_tbl.rid',
	timestamp	               TIMESTAMP COMMENT 'timestamp of occuring of this event',
	status		               INTEGER(1) UNSIGNED COMMENT '1=processing of incoming mail was successful, 0=failed',
	company_id	               INTEGER UNSIGNED COMMENT 'the company_tbl.company_id of the sender, if available',
	mailing_id	               INTEGER UNSIGNED COMMENT 'the mailing_tbl.mailing_id of the origin newsletter, if available',
	customer_id	               INTEGER UNSIGNED COMMENT 'the customer_<CID>_tbl.customer_id of the origin recipient of the newsletter, if available',
	action		               VARCHAR(32) COMMENT 'the resulting action after processing this incoming mail',
	remark		               VARCHAR(1000) COMMENT 'further information collected during processing',
	PRIMARY KEY(mailloop_log_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_unicode_ci COMMENT 'stores activity for each bounce filter';
							
INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('22.10.387', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
