/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.layoutbuilder.forms;

public enum TemplatesStyle {
    
    AREA_BACKGROUND(1, "Background color"),
    AREA_BACKGROUND_IN_DIV(2, "Color of building block background"),
    AREA_FONT_COLOR(3, "Font color"),
    AREA_GRID_COLOR(4, "Gutter color");
    
    private int styleKey;
    private String name;
    
    TemplatesStyle(int styleKey, String name) {
        this.styleKey = styleKey;
        this.name = name;
    }
    
    public int getStyleKey() {
        return styleKey;
    }
    
    public String getStringStyleKey() { return Long.toString(styleKey); }
    
    public String getName() { return name; }
}
