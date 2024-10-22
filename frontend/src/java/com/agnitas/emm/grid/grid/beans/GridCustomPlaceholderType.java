/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.beans;

import static com.agnitas.emm.grid.grid.util.PlaceholderUtils.PLACEHOLDER_DEFAULT_CONTENT_BIG_TEXT;
import static com.agnitas.emm.grid.grid.util.PlaceholderUtils.PLACEHOLDER_DEFAULT_CONTENT_COLOR;
import static com.agnitas.emm.grid.grid.util.PlaceholderUtils.PLACEHOLDER_DEFAULT_CONTENT_IMAGE_SRC;
import static com.agnitas.emm.grid.grid.util.PlaceholderUtils.PLACEHOLDER_DEFAULT_CONTENT_LINK_HREF;
import static com.agnitas.emm.grid.grid.util.PlaceholderUtils.PLACEHOLDER_DEFAULT_CONTENT_TEXT;

/**
 * Represents a type of custom (user-defined) placeholder (see {@link com.agnitas.emm.grid.grid.beans.GridDivMarkupSpanType#Placeholder}).
 * Attention: these types are persisted in the database as numbers so never change an assigned id values!
 */
public enum GridCustomPlaceholderType {
    Label(0, PLACEHOLDER_DEFAULT_CONTENT_TEXT),
    Text(1, PLACEHOLDER_DEFAULT_CONTENT_BIG_TEXT),
    Image(2, PLACEHOLDER_DEFAULT_CONTENT_IMAGE_SRC),
    Link(3, PLACEHOLDER_DEFAULT_CONTENT_LINK_HREF),
    ImageLink(4, PLACEHOLDER_DEFAULT_CONTENT_IMAGE_SRC),
    Color(5, PLACEHOLDER_DEFAULT_CONTENT_COLOR),
    Select(6, ""),
    Check(7, ""),
    Multi(8, "");

    private final int id;
    private final String stub;

    GridCustomPlaceholderType(int id, String stub) {
        this.id = id;
        this.stub = stub;
    }
    
    public static GridCustomPlaceholderType getPlaceholderTypeFromId(int id) throws Exception {
    	for (GridCustomPlaceholderType value : GridCustomPlaceholderType.values()) {
    		if (value.getId() == id) {
    			return value;
    		}
    	}
    	throw new Exception("Invalid PlaceholderType id: " + id);
    }

    public static GridCustomPlaceholderType getById(int id) {
        for (GridCustomPlaceholderType value : GridCustomPlaceholderType.values()) {
            if (value.getId() == id) {
                return value;
            }
        }

        return null;
    }
    
    public static GridCustomPlaceholderType getByName(String name) {
    	for (GridCustomPlaceholderType value : GridCustomPlaceholderType.values()) {
    		if (value.name().equalsIgnoreCase(name)) {
    			return value;
    		}
    	}
    	return null;
    }
    
    public static GridCustomPlaceholderType getPlaceholderTypeByName(String name) throws Exception {
    	for (GridCustomPlaceholderType value : GridCustomPlaceholderType.values()) {
    		if (value.name().equalsIgnoreCase(name)) {
    			return value;
    		}
    	}
    	throw new Exception("Invalid PlaceholderType name: " + name);
    }

    public int getId() {
        return id;
    }

    public String getStub() {
        return stub;
    }
}
