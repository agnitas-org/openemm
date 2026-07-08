/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.premium.web;

import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.Strings;

public enum SpecialPremiumFeature {

    /**
     * Automation Package needs company_tbl value mailtracking to be adjusted
     */
    AUTOMATION("Automation Package"),
    
    /**
     * Automation History Package uses ProfileFieldHistory in hst_customer_<cid>_tbl tables
     */
	AUTOMATION_HISTORY("Automation History Package"),
    
    /**
     * Retargeting Package needs additional rdirlog tables in db
     */
    RETARGETING("Retargeting Package"),
    
    /**
     * Webpush Package needs additional push tables in db
     */
    WEB_PUSH("Webpush Package"),
    
    /**
     * SMS Package needs additional profile field
     */
    SMS("SMS Package"),
    
    /**
     * Delivery Package needs additional config values. Contains OMG and AHV
     */
    DELIVERY_PACKAGE("Delivery Package"),
    
    /**
     * Automation Delivery Information FeaturePackage needs additional company_info values
     */
    AUTOMATION_DELIVERY_INFORMATION("Automation Delivery Information"),
	
    /**
     * Innerclients FeaturePackage needs additional company_info values
     */
    INNERCLIENTS("Innerclients Package"),
	
    /**
     * Frequency Counter FeaturePackage needs additional profile fields
     */
	FREQUENCY_COUNTER("Frequency Counter Package"),
	
    /**
     * Analytics Package FeaturePackage needs an Test-VIP recipient
     */
	ANALYTICS_PACKAGE("Analytics Package"),
	
	/**
	* Delivery Encrypted FeaturePackage
	*/
	DELIVERY_ENCRYPTED_PACKAGE("Delivery Encrypted Package"),
	
	/**
	* Webhooks FeaturePackage needs additional company_info value
	*/
	WEBHOOKS_PACKAGE("Webhooks Package", ConfigValue.Webhooks.WebhooksEnabled),
	
	/**
	* AI FeaturePackage
	*/
	AI_PACKAGE("AI Package");
	
    private final String name;
    private final ConfigValue configValue;

    SpecialPremiumFeature(String name) {
        this(name, null);
    }

    SpecialPremiumFeature(String name, ConfigValue configValue) {
        this.name = name;
        this.configValue = configValue;
    }
    
    public static SpecialPremiumFeature getFeatureByName(String featureName) {
        for (SpecialPremiumFeature feature: SpecialPremiumFeature.values()) {
            if (Strings.CI.equals(feature.getName(), featureName)) {
                return feature;
            }
        }
        return null;
    }
    
    public String getName() {
        return name;
    }

    public ConfigValue getConfigValue() {
        return configValue;
    }

}
