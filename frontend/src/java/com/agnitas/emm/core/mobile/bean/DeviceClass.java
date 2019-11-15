/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mobile.bean;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum DeviceClass {
    DESKTOP(1),
    MOBILE(2),
    TABLET(3),
    SMARTTV(4),
    UNKNOWN_DESKTOP(5),
    UNKNOWN_MOBILE(6),
    UNKNOWN_TABLET(7),
    UNKNOWN_SMARTTV(8);

    private final int id;

    private DeviceClass(int id) {
        this.id = id;
    }

    public static DeviceClass fromString(String value) {
        for (DeviceClass deviceClass : DeviceClass.values()) {
            if (deviceClass.toString().equalsIgnoreCase(value)) {
                return deviceClass;
            }
        }
        return UNKNOWN_DESKTOP;
    }

    public static DeviceClass fromId(int id) {
        return fromIdWithDefault(id, UNKNOWN_DESKTOP);
    }
    
    public static DeviceClass fromIdWithDefault(int id, DeviceClass desktop) {
        if (id > 0) {
            for (DeviceClass deviceClass : DeviceClass.values()) {
                if (deviceClass.getId() == id) {
                    return deviceClass;
                }
            }
        }
        return desktop;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return toString();
    }
    
    public static List<DeviceClass> getOnlyKnownDeviceClasses() {
        return Arrays.stream(DeviceClass.values())
                .filter(item -> !item.getName().startsWith("UNKNOWN_"))
                .collect(Collectors.toList());
    }
}
