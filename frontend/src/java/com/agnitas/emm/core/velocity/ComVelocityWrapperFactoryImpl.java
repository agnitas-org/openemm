/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.velocity;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.emm.core.velocity.VelocityWrapper;
import org.agnitas.emm.core.velocity.VelocityWrapperFactory;
import org.agnitas.emm.core.velocity.VelocityWrapperFactoryImpl;

/**
 * Implementation of {@link VelocityWrapperFactory} creating {@link VelocityWrapper}s
 * with EMM specific features.
 */
public class ComVelocityWrapperFactoryImpl extends VelocityWrapperFactoryImpl {

	@Override
	protected VelocityWrapper createVelocityWrapper(@VelocityCheck int companyId, String velocityLogDir) throws Exception {
		return new ComVelocityWrapperImpl(companyId, getUberspectDelegateTargetFactory(), velocityLogDir);
	}

}
