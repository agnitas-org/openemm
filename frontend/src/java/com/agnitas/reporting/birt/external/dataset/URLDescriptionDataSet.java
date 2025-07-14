/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.List;

public class URLDescriptionDataSet extends BIRTDataSet {

    public List<String> getURLDescription(int urlID) {
        String query = "SELECT full_url FROM rdir_url_tbl WHERE url_id = ?";
        return select(query, (rs, rowNum) -> rs.getString("full_url"), urlID);
    }
}
