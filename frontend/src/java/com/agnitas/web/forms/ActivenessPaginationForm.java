/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.emm.core.commons.ActivenessStatus;

// TODO: EMMGUI-714 remove after remove of old design
public class ActivenessPaginationForm extends PaginationForm {
    private ActivenessStatus filter = ActivenessStatus.NONE;
	private Map<Integer, Boolean> activeness = new HashMap<>();

	public ActivenessStatus getFilter() {
		return filter;
	}

	public void setFilter(ActivenessStatus filter) {
		this.filter = filter;
	}

	public Map<Integer, Boolean> getActiveness() {
		return activeness;
	}

	public void setActiveness(Map<Integer, Boolean> activeness) {
		this.activeness = activeness;
	}
}
