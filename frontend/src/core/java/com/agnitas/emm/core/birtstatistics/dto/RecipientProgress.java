/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.dto;

public class RecipientProgress {

    private int optIns;
    private int optOuts;
    private int bounced;
    private int doubleOptIn;
    private int blocklisted;

    public int getOptIns() {
        return optIns;
    }

    public void setOptIns(int optIns) {
        this.optIns = optIns;
    }

    public int getBounced() {
        return bounced;
    }

    public void setBounced(int bounced) {
        this.bounced = bounced;
    }

    public int getOptOuts() {
        return optOuts;
    }

    public void setOptOuts(int optOuts) {
        this.optOuts = optOuts;
    }

    public int getBlocklisted() {
        return blocklisted;
    }

    public void setBlocklisted(int blocklisted) {
        this.blocklisted = blocklisted;
    }

    public int getDoubleOptIn() {
        return doubleOptIn;
    }

    public void setDoubleOptIn(int doubleOptIn) {
        this.doubleOptIn = doubleOptIn;
    }
}
