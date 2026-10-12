/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.oauth.dao;

import java.time.Instant;
import java.util.Optional;

import com.agnitas.emm.core.oauth.model.OAuthRequestRecord;

public interface OAuth2AuthorizationRequestDao {

    void save(String state, String requestJson, int adminId, int companyId, Instant expiresAt);

    Optional<OAuthRequestRecord> findByState(String state);

    void deleteByState(String state);

    void deleteExpired();

}
