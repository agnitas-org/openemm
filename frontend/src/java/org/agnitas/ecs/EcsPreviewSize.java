/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs;

public enum EcsPreviewSize {
    DESKTOP(800, 600),
    MOBILE_PORTRAIT(320, 356),
    MOBILE_LANDSCAPE(356, 320),
    TABLET_PORTRAIT(768, 946),
    TABLET_LANDSCAPE(1024, 690);

    private final int width;
    private final int height;

    public static EcsPreviewSize getForId(int id) {
        return getForId(id, DESKTOP);
    }
    
    public static EcsPreviewSize getForIdOrNull(int id) {
        return getForId(id, null);
    }

    public static EcsPreviewSize getForId(int id, EcsPreviewSize defaultSize) {
        switch (id) {
            case 1:
                return DESKTOP;
            case 2:
                return MOBILE_PORTRAIT;
            case 3:
                return MOBILE_LANDSCAPE;
            case 4:
                return TABLET_PORTRAIT;
            case 5:
                return TABLET_LANDSCAPE;
            default:
                return defaultSize;
        }
    }

    EcsPreviewSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getId() {
        return ordinal() + 1;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
