/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

public class OpeningStatPerTargetRow {
    private int openings_gross;
    private String targetgroup;
    private int targetgroup_index;
    private int targetgroup_id;

    public int getTargetgroup_index() {
        return targetgroup_index;
    }

    public void setTargetgroup_index(int targetgroup_index) {
        this.targetgroup_index = targetgroup_index;
    }

    public String getTargetgroup() {
        return targetgroup;
    }

    public void setTargetgroup(String targetgroup) {
        this.targetgroup = targetgroup;
    }

    public int getOpenings_gross() {
        return openings_gross;
    }

    public void setOpenings_gross(int openings_gross) {
        this.openings_gross = openings_gross;
    }

    public int getTargetgroup_id() {
        return targetgroup_id;
    }

    public void setTargetgroup_id(int targetgroup_id) {
        this.targetgroup_id = targetgroup_id;
    }
}
