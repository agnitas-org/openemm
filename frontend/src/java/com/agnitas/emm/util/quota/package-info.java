/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

/**
 * <p>
 * Quotas.
 * </p>
 * 
 * <p>
 * This package provides services for quotas to access different types of APIs.
 * </p>
 * 
 * <p>
 *   To check access, a username, a company ID and the name of the requested API service
 *   is needed. For webservice, the API service name is the name of the endpoint.
 * </p>
 * 
 * <h3>Basic usage</h3>
 * <p>
 *   The interface {@link com.agnitas.emm.util.quota.api.QuotaService} provides a single
 *   method {@link com.agnitas.emm.util.quota.api.QuotaService#checkAndTrack(String, int, String)},
 *   which checks, logs and possibly rejects the access.
 * </p>
 * <p>
 *   Simply invoke this method.
 * </p>
 */
package com.agnitas.emm.util.quota;

