/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.beans;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ComGridDivContainer {
    int getCompanyId();

    void setCompanyId(@VelocityCheck int companyId);

    int getId();

    void setId(int id);

    String getName();

    void setName(String name);

    String getBody();

    void setBody(String body);

    boolean getIsToc();

    void setIsToc(boolean isToc);

    boolean getIsSortable();

    void setIsSortable(boolean isSortable);

    boolean isIsHidden();

    void setIsHidden(boolean hidden);

    byte[] getThumbnail();

    void setThumbnail(byte[] thumbnail);

    /**
     * An isThumbnailCustom flag indicates a user-defined avatar (is referred to as a "custom thumbnail") manually uploaded via UI.
     * It will not be updated after changes made to div container's content.
     */
    boolean getIsThumbnailCustom();

    void setIsThumbnailCustom(boolean isThumbnailCustom);

    ComGridDivContainerUsages getUsageData();

    void setUsageData(ComGridDivContainerUsages usageData);

    boolean equalsWithoutID(ComGridDivContainer otherGridDivContainer);

    boolean isDeleted();

    void setDeleted(boolean deleted);

    String getType();

    void setType(String type);

    String getFilter();

    void setFilter(String filter);
}
