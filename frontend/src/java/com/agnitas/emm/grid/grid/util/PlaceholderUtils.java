/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.agnitas.emm.grid.grid.beans.ComGridPlaceholder;
import com.agnitas.emm.grid.grid.beans.GridCustomPlaceholderType;

public class PlaceholderUtils {
    public static final String PLACEHOLDER_DEFAULT_CONTENT_TEXT = "Lorem ipsum";
    public static final String PLACEHOLDER_DEFAULT_CONTENT_BIG_TEXT = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
    public static final String PLACEHOLDER_DEFAULT_CONTENT_IMAGE_SRC = "assets/core/images/grid_expire_image.png";
    public static final String PLACEHOLDER_DEFAULT_CONTENT_LINK_HREF = "#";
    public static final String PLACEHOLDER_DEFAULT_CONTENT_IMAGE_NAME = "grid_expire_image.png";

    public static final String DELETE_IMAGE_MESSAGE = "__delete_image__";
    public static final String TOC_ITEM_ID_PREFIX = "toc_";

    private static final Map<Integer, String> defaultValues = new HashMap<>();

    static {
        for (GridCustomPlaceholderType type : GridCustomPlaceholderType.values()) {
            defaultValues.put(type.getId(), type.getStub());
        }
    }

    /**
     * Return a stub (default value) for a placeholder of a given type.
     *
     * @param type a type of a placeholder.
     * @return a default value for a placeholder.
     */
    public static String stub(int type) {
        return defaultValues.get(type);
    }

    public static boolean matches(ComGridPlaceholder ph1, ComGridPlaceholder ph2) {
        if (ph1.getPlaceholderType() == ph2.getPlaceholderType()) {
            return StringUtils.equals(ph1.getPlaceholderName(), ph2.getPlaceholderName());
        }

        return false;
    }
}
