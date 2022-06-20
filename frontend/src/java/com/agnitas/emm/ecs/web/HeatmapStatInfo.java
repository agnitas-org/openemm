/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.ecs.web;

import java.util.ArrayList;
import java.util.List;

public class HeatmapStatInfo {

    public static class Entry {
        private String id;
        private String value;
        private String color;

        public Entry(String id, String value, String color) {
            this.id = id;
            this.value = value;
            this.color = color;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

        public String getColor() {
            return color;
        }
    }
    private List<Entry> statEntries = new ArrayList<>();
    private String nullColor;

    public List<Entry> getStatEntries() {
        return statEntries;
    }

    public void setStatEntries(List<Entry> statEntries) {
        this.statEntries = statEntries;
    }

    public void setNullColor(String nullColor) {
        this.nullColor = nullColor;
    }

    public String getNullColor() {
        return nullColor;
    }
}
