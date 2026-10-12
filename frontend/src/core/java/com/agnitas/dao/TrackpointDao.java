/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Map;

import com.agnitas.beans.TrackpointDef;
import com.agnitas.beans.PaginatedList;

public interface TrackpointDao {

    /**
     * Getter for property trackpoint by trackpoint id and company id.
     *
     * @return Value of property trackpoint.
     */
    TrackpointDef get(int id, int companyID);

    List<TrackpointDef> getAllByPageTags(List<String> pageTags, int companyId);

    int getTrackpointIdByName(int companyID, int type, String pagetag, int mailingID);

    void saveTrackpoint(TrackpointDef track);

    void deleteTrackpoint(TrackpointDef track);

    PaginatedList<TrackpointDef> getAll(int companyID, String sort, String direction, int pageNumber, int pageSize);

    boolean deleteTrackpointsByCompany(int companyId);

    Map<Integer, String> getNamesMap(int companyID);

}
