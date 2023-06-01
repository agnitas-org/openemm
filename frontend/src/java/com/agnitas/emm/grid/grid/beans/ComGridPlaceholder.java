/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.beans;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ComGridPlaceholder {
    int getCompanyId();
    void setCompanyId(@VelocityCheck int companyId);

    int getPlaceholderId();
    void setPlaceholderId(int placeholderId);

    int getDivContainerId();
    void setDivContainerId(int divContainerId);

    int getPlaceholderType();
    void setPlaceholderType(int placeholderType);

    String getPlaceholderName();
    void setPlaceholderName(String placeholderName);

    @Override
	boolean equals(Object placeholder);

    int getNameAndTypeHashCode();

    boolean getIsTocItem();
    void setIsTocItem(boolean isTocItem);
    
    String getOptions();
    void setOptions(String options);
        
    String getHelp();
    void setHelp(String help);
}
