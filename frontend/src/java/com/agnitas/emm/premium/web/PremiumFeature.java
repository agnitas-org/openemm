/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.premium.web;

import org.apache.commons.lang3.StringUtils;

public enum PremiumFeature {
    
    AUTOMATION("Automation Package"),
    DELIVERY("Delivery Package"),
    ANALYTICS("Analytics Package"),
    RETARGETING("Retargeting Package"),
    LAYOUT_BUILDER("Layout Package"),
    AUIMPORT_AUTOEXPORT("ImportExport"),
    WEBSERVICES("Webservices"),
    WEB_PUSH("Webpush Package"),
    FACEBOOK_LEAD_ADS("FacebookLeadAds");
    
    private final String name;

    PremiumFeature(String name) {
        this.name = name;
    }
    
    public static PremiumFeature getFeatureByName(String featureName) {
        for (PremiumFeature feature: PremiumFeature.values()) {
            if (StringUtils.equalsIgnoreCase(feature.getName(), featureName)) {
                return feature;
            }
        }
        return null;
    }
    
    public String getName() {
        return name;
    }
}
