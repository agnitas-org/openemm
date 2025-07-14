/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import com.agnitas.messages.I18nString;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class TargetDescriptionDataSet extends BIRTDataSet {

    public List<String> getTargetDescription(String targetID, String language) {
        if (StringUtils.isBlank(targetID)) {
            if (StringUtils.isBlank(language)) {
                language = "EN";
            }

            return List.of(I18nString.getLocaleString(CommonKeys.ALL_SUBSCRIBERS, language));
        }

        String query = "SELECT target_shortname FROM dyn_target_tbl WHERE target_id IN (" + targetID + ")";
        return select(query, (rs, rowNum) -> rs.getString("target_shortname"));
    }
}
