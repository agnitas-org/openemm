/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity;

import java.util.Objects;

import org.agnitas.emm.core.velocity.emmapi.CompanyAccessCheck;
import com.agnitas.util.TimeoutLRUMap;
/**
 * Implementation of {@link VelocityWrapperFactory} that caches existing {@link VelocityWrapper}
 * for companies.
 */
public class VelocityWrapperFactoryImpl implements VelocityWrapperFactory {

	@Override
	public VelocityWrapper getWrapper(int companyId) {
		VelocityWrapper wrapper = cache.get(companyId);

		if (wrapper == null) {
			wrapper = createVelocityWrapper(companyId);
			cache.put(companyId, wrapper);
		}

		return wrapper;
	}

	/**
	 * Creates a new {@link VelocityWrapper} instance running in the context of the
	 * given comapny ID.
	 * 
	 * @param companyId company ID
	 * 
	 * @return new {@link VelocityWrapper} instance
	 */
	protected VelocityWrapper createVelocityWrapper( int companyId) {
		return new VelocityWrapperImpl(companyId, companyAccessCheck);
	}
	
    public CompanyAccessCheck getCompanyAccessCheck() {
        return companyAccessCheck;
    }

	// --------------------------------------------------------------------------- Dependency Injection
	
	/** LRU map caching the VelocityWrapper instances. */
	private TimeoutLRUMap<Integer, VelocityWrapper> cache;
	
    private CompanyAccessCheck companyAccessCheck;

	/**
	 * Sets the LRU map for caching.
	 * 
	 * @param cache LRU map
	 */
	public void setVelocityWrapperCache( TimeoutLRUMap<Integer, VelocityWrapper> cache) {
		this.cache = cache;
	}

   	public final void setCompanyAccessCheck(final CompanyAccessCheck check) {
   		this.companyAccessCheck = Objects.requireNonNull(check, "CompanyAccessCheck is null");
   	}
}
