/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.datasource.bean.impl;

import com.agnitas.emm.core.datasource.bean.DataSource;
import com.agnitas.emm.core.datasource.enums.SourceGroupType;

import java.util.Date;

public class DataSourceImpl implements DataSource {

    private int id;
    private String description;
    private Date timestamp;
    private SourceGroupType sourceGroupType;
    private String extraData; // stores additional information depending on source group

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public SourceGroupType getSourceGroupType() {
        return sourceGroupType;
    }

    @Override
    public void setSourceGroupType(SourceGroupType sourceGroupType) {
        this.sourceGroupType = sourceGroupType;
    }

    @Override
    public String getExtraData() {
        return extraData;
    }

    @Override
    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}
