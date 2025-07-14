/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.upselling.form;

import java.util.Map;

import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Represents set of fields for displaying upselling page
 */
// TODO: remove after EMMGUI-714 will be finished and old design will be removed
public class UpsellingForm {
    private String page;
    private String featureNameKey;
    private String sidemenuActive;
    private String sidemenuSubActive;
    private String navigationKey;
    private String extraParams;
    private String navigationLink;

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

    public String getNavigationLink() {
        return navigationLink;
    }

    public void setNavigationLink(String navigationLink) {
        this.navigationLink = navigationLink;
    }

    public int getMailingIdFromSpringUrl() {
        return NumberUtils.toInt(StringUtils.substringBetween(navigationLink, "/mailing/", "/"));
    }
}
