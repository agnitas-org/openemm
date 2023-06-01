/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.premium.bean;

public class FeaturePackage {
    
    private String name;
    
    private boolean active;
    
    private int componentsQuantity;
    
    private String messageKey;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public void setComponentsQuantity(int componentsQuantity) {
        this.componentsQuantity = componentsQuantity;
    }
    
    public int getComponentsQuantity() {
        return componentsQuantity;
    }
    
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }
    
    public String getMessageKey() {
        return messageKey;
    }
}
