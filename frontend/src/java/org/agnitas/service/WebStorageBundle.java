/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.agnitas.beans.WebStorageEntry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class WebStorageBundle<T extends WebStorageEntry> {
    private static Map<String, WebStorageBundle<?>> registry = new HashMap<>();

    public static List<WebStorageBundle<? extends WebStorageEntry>> definitions() {
        return new ArrayList<>(registry.values());
    }

    // Keep in mind to place definitions at the static context and make sure that all the containing classes are initialized.
    public static <X extends WebStorageEntry> WebStorageBundle<X> define(String name, Class<X> type) {
        return new WebStorageBundle<>(name, type);
    }

    private String name;
    private Class<T> type;

    private WebStorageBundle(String name, Class<T> type) {
        this.name = Objects.requireNonNull(StringUtils.trimToNull(name), "name is blank");
        this.type = Objects.requireNonNull(type, "type == null");

        // Use trimmed name, override existing definition if any.
        registry.put(this.name, this);
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("type", type)
                .toString();
    }
}
