/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.reporting.birt.external.beans;

public class RecipientMaxValues {
    
    private int maxActive;
    private int maxBounced;
    private int maxDoubleOptIn;
    private int maxBlacklisted;
    private int maxAllOut;
    
    public int getMaxActive() {
        return maxActive;
    }
    
    public void setMaxActive(int maxActive) {
        this.maxActive = Math.max(maxActive, this.maxActive);
    }
    
    public int getMaxBounced() {
        return maxBounced;
    }
    
    public void setMaxBounced(int maxBounced) {
        this.maxBounced = Math.max(maxBounced, this.maxBounced);
    }
    
    public int getMaxDoubleOptIn() {
        return maxDoubleOptIn;
    }
    
    public void setMaxDoubleOptIn(int maxDoubleOptIn) {
        this.maxDoubleOptIn =  Math.max(maxDoubleOptIn, this.maxDoubleOptIn);
    }
    
    public int getMaxBlacklisted() {
        return maxBlacklisted;
    }
    
    public void setMaxBlacklisted(int maxBlacklisted) {
        this.maxBlacklisted =  Math.max(maxBlacklisted, this.maxBlacklisted);
    }
    
    public int getMaxAllOut() {
        return maxAllOut;
    }
    
    public void setMaxAllOut(int maxAllOut) {
        this.maxAllOut =  Math.max(maxAllOut, this.maxAllOut);
    }
}
