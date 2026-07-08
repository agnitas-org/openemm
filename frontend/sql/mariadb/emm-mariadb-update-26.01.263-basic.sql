/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE rdir_url_tbl ADD COLUMN emm_trigger INT(1) DEFAULT 0 COMMENT 'Flag indicating whether the link originates from a tag with the emm-trigger-name or emm-trigger-id attribute.';

CREATE TABLE hp_clickranking_tbl
(
    url_id          INT(10) UNSIGNED NOT NULL COMMENT 'References rdir_url_tbl.url_id',
    company_id      INT(11) UNSIGNED NOT NULL COMMENT 'ID of company',
    url             TEXT             NOT NULL COMMENT 'References rdir_url_tbl.full_url',
    topic           VARCHAR(50)      NOT NULL COMMENT 'References rdir_action_tbl.shortname. One of the keywords (given in attribute emm-trigger-name or - if former attribute does not exist - emm-trigger-id)',
    count           INTEGER UNSIGNED NOT NULL COMMENT 'Total number of link clicks',
    rank            INTEGER          NOT NULL COMMENT 'Position of the link in the hit list for the respective topic (keyword)',
    first_clickdate TIMESTAMP        NOT NULL COMMENT 'First click on this link'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT 'Stores the number of clicks per topic item to support ranking.';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.01.263', CURRENT_USER, CURRENT_TIMESTAMP);

COMMIT;
