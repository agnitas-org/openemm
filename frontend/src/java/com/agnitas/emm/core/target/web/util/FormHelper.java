/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web.util;

import com.agnitas.beans.ComTarget;
import com.agnitas.emm.core.target.web.QueryBuilderTargetGroupForm;

/**
 * Helper to transfer data between QueryBuilderTargetGroupForm and ComTarget and vice-versa.
 */
public final class FormHelper {

	public static final void targetGroupFormProperties(final QueryBuilderTargetGroupForm form, final ComTarget target) {
		form.setTargetID(target.getId());
		form.setShortname(target.getTargetName());
		form.setDescription(target.getTargetDescription());
		form.setUseForAdminAndTestDelivery(target.isAdminTestDelivery());
		form.setEql(target.getEQL());
		form.setLocked(target.isLocked());
		form.setSimpleStructure(target.isSimpleStructured());
	}

	public static final void formPropertiesToTargetGroup(final ComTarget target, final QueryBuilderTargetGroupForm form) {
		target.setId(form.getTargetID());
		target.setAdminTestDelivery(form.isUseForAdminAndTestDelivery());
		target.setEQL(form.getEql());
		target.setTargetDescription(form.getDescription());
		target.setTargetName(form.getShortname());
	}
}
