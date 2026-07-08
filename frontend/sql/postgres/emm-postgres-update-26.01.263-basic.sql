/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

ALTER TABLE rdir_url_tbl ADD emm_trigger SMALLINT DEFAULT 0;
COMMENT ON COLUMN rdir_url_tbl.emm_trigger IS 'Flag indicating whether the link originates from a tag with the emm-trigger-name or emm-trigger-id attribute.';

CREATE TABLE hp_clickranking_tbl
(
    url_id          INTEGER       NOT NULL,
    company_id      INTEGER       NOT NULL,
    url             VARCHAR(2000) NOT NULL,
    topic           VARCHAR(50)   NOT NULL,
    count           INTEGER       NOT NULL,
    rank            INTEGER       NOT NULL,
    first_clickdate TIMESTAMP     NOT NULL
);
COMMENT ON TABLE hp_clickranking_tbl IS 'Stores the number of clicks per topic item to support ranking.';
COMMENT ON COLUMN hp_clickranking_tbl.url_id IS 'References rdir_url_tbl.url_id';
COMMENT ON COLUMN hp_clickranking_tbl.company_id IS 'ID of company';
COMMENT ON COLUMN hp_clickranking_tbl.url IS 'References rdir_url_tbl.full_url';
COMMENT ON COLUMN hp_clickranking_tbl.topic IS 'References rdir_action_tbl.shortname. One of the keywords (given in attribute emm-trigger-name or - if former attribute does not exist - emm-trigger-id)';
COMMENT ON COLUMN hp_clickranking_tbl.count IS 'Total number of link clicks';
COMMENT ON COLUMN hp_clickranking_tbl.rank IS 'Position of the link in the hit list for the respective topic (keyword)';
COMMENT ON COLUMN hp_clickranking_tbl.first_clickdate IS 'First click on this link';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
VALUES ('26.01.263', CURRENT_USER, CURRENT_TIMESTAMP);
