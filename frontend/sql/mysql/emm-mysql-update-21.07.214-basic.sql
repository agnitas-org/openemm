/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

-- EMM-8126
-- Execute change after version is available on all systems: 21.01.415
DELETE FROM company_info_tbl WHERE cname = 'development.CopyMailing.measureTiming';
DELETE FROM config_tbl WHERE class = 'development' AND name = 'CopyMailing.measureTiming';

-- EMM-5116
-- Execute change after version is available on all systems: 21.01.415
DELETE FROM company_info_tbl WHERE cname = 'development.com.agnitas.emm.core.mailing.service.impl.SendActionbasedMailingServiceImpl.disableMailgunCache';
DELETE FROM config_tbl WHERE class = 'development' AND name = 'com.agnitas.emm.core.mailing.service.impl.SendActionbasedMailingServiceImpl.disableMailgunCache';

-- EMM-7167
-- Execute change after version is available on all systems: 21.01.431
DELETE FROM tag_tbl WHERE tagname = 'agnAUTOURL';
DELETE FROM tag_tbl WHERE tagname = 'agnITAS';
DELETE FROM tag_tbl WHERE tagname = 'agnCALC2';

-- EMM-8126
-- Execute change after version is available on all system: 21.01.433
DELETE FROM company_info_tbl WHERE company_id = 0 AND cname = 'webservice.default_api_call_limits';

-- EMM-7992
-- Execute change after version is available on all systems: 21.01.446
DELETE FROM company_info_tbl WHERE cname = 'development.use_new_blacklist_wildcards';
DELETE FROM config_tbl WHERE class = 'development' AND name = 'use_new_blacklist_wildcards';

-- EMM-3332
-- Execute change after version is available on all systems: 21.01.469
DELETE FROM company_info_tbl WHERE cname = 'development.use_backend_mailing_preview';
DELETE FROM config_tbl WHERE class = 'development' AND name = 'use_backend_mailing_preview';

-- EMM-7167
-- Execute change after version is available on all systems: 21.01.551
DELETE FROM tag_tbl WHERE tagname = 'agnTITLE_SHORT';
DELETE FROM tag_tbl WHERE tagname = 'agnDBV';
DELETE FROM tag_tbl WHERE tagname = 'agnNULL';

INSERT INTO agn_dbversioninfo_tbl (version_number, updating_user, update_timestamp)
    VALUES ('21.07.214', CURRENT_USER, CURRENT_TIMESTAMP);
