/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.datasource.enums;

import org.agnitas.dao.SourceGroupType;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public enum DataSourceType {

    USER("settings.Admin", Set.of(SourceGroupType.File, SourceGroupType.SubscriberInterface)),
    API("default.API", Set.of(SourceGroupType.SoapWebservices, SourceGroupType.Facebook, SourceGroupType.RestfulService)),
    FORMS("workflow.panel.forms", Set.of(SourceGroupType.AutoinsertForms, SourceGroupType.Velocity)),
    OTHER("others", Set.of(SourceGroupType.Other, SourceGroupType.Default, SourceGroupType.DataAgent));

    private final String messageKey;
    private final Set<SourceGroupType> sourceGroupTypes;

    DataSourceType(String messageKey, Set<SourceGroupType> sourceGroupTypes) {
        this.messageKey = messageKey;
        this.sourceGroupTypes = sourceGroupTypes;
    }

    public static Optional<DataSourceType> findBySourceGroupType(SourceGroupType type) {
        return Stream.of(DataSourceType.values())
                .filter(t -> t.getSourceGroupTypes().contains(type))
                .findAny();
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Set<SourceGroupType> getSourceGroupTypes() {
        return sourceGroupTypes;
    }
}
