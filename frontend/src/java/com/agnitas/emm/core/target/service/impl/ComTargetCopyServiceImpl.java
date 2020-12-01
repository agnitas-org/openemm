/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.service.impl;

import com.agnitas.beans.ComTarget;
import com.agnitas.emm.core.target.service.TargetCopyService;

public final class ComTargetCopyServiceImpl implements TargetCopyService {

	@Override
	public final ComTarget copyTargetGroup(final ComTarget source, final ComTarget destination) {
		destination.setAdminTestDelivery(source.isAdminTestDelivery());
		destination.setChangeDate(source.getChangeDate());
		destination.setCompanyID(source.getCompanyID());
		destination.setComponentHide(source.getComponentHide());
		destination.setCreationDate(source.getCreationDate());
		destination.setDeleted(source.getDeleted());
		destination.setEQL(source.getEQL());
		destination.setId(source.getId());
		destination.setLocked(source.isLocked());
		destination.setTargetDescription(source.getTargetDescription());
		destination.setTargetName(source.getTargetName());
		destination.setTargetSQL(source.getTargetSQL());
		destination.setValid(source.isValid());
		
		return destination;
	}

}
