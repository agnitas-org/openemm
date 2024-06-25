/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.post;

import org.agnitas.emm.core.commons.util.ConfigValue;

public enum PostalField {

    COUNTRY("company.settings.post.country", ConfigValue.DefaultCountryField),
    CITY("company.settings.post.city", ConfigValue.DefaultCityField),
    STREET("company.settings.post.street", ConfigValue.DefaultStreetField),
    CODE("company.settings.post.code", ConfigValue.DefaultPostalCodeField);

    private final ConfigValue configValue;
    private final String messageKey;

    PostalField(String messageKey, ConfigValue configValue) {
        this.messageKey = messageKey;
        this.configValue = configValue;
    }

    public ConfigValue getConfigValue() {
        return configValue;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
