/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.upselling.form;

import java.util.Map;

import org.agnitas.util.AgnUtils;

/**
 * Represents set of fields for displaying upselling page
 */
public class UpsellingForm {
    private String page;
    private String featureNameKey;
    private String sidemenuActive;
    private String sidemenuSubActive;
    private String navigationKey;
    private String extraParams;

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getFeatureNameKey() {
        return featureNameKey;
    }

    public void setFeatureNameKey(String featureNameKey) {
        this.featureNameKey = featureNameKey;
    }

    public String getSidemenuActive() {
        return sidemenuActive;
    }

    public void setSidemenuActive(String sidemenuActive) {
        this.sidemenuActive = sidemenuActive;
    }

    public String getSidemenuSubActive() {
        return sidemenuSubActive;
    }

    public void setSidemenuSubActive(String sidemenuSubActive) {
        this.sidemenuSubActive = sidemenuSubActive;
    }
    
    public String getNavigationKey() {
        return navigationKey;
    }
    
    public void setNavigationKey(String navigationKey) {
        this.navigationKey = navigationKey;
    }
    
    public String getExtraParams() {
        return extraParams;
    }
    
    public void setExtraParams(String extraParams) {
        this.extraParams = extraParams;
    }
    
    public Map<String, String> getExtraParamsMap() {
        return AgnUtils.getParamsMap(extraParams, "&", "=");
    }
}
