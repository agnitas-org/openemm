/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

/**
 * <p>
 * Implementation of {@link QuotaService} using <a href="https://github.com/vladimir-bukhtoyarov/bucket4j">Bucket4j</a>.
 * </p>
 * 
 * <p>
 * This implementation uses an implementation of the 
 * <a href="https://en.wikipedia.org/wiki/Token_bucket">Token Bucket algorithm</a>
 * and allows multiple stages of rate limiting.
 * </p>
 */
package com.agnitas.emm.springws.throttling.tokenbucket;
