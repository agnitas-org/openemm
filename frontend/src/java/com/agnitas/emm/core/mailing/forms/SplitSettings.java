/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.forms;

public class SplitSettings {

    private int splitId;
    private String splitBase;
    private String splitPart;

    public int getSplitId() {
        return splitId;
    }

    public void setSplitId(int splitId) {
        this.splitId = splitId;
    }

    public String getSplitBase() {
        return splitBase;
    }

    public void setSplitBase(String splitBase) {
        this.splitBase = splitBase;
    }

    public String getSplitPart() {
        return splitPart;
    }

    public void setSplitPart(String splitPart) {
        this.splitPart = splitPart;
    }

    public void clear() {
        setSplitBase(null);
        setSplitPart(null);
    }
}
