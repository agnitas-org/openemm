/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.complexity.bean.impl;

import com.agnitas.emm.core.target.complexity.bean.CustomerTableColumnMetadata;
import com.agnitas.util.DbColumnType;

public class CustomerTableColumnMetadataImpl implements CustomerTableColumnMetadata {
    private DbColumnType.SimpleDataType type;
    private boolean indexed;

    @Override
    public DbColumnType.SimpleDataType getType() {
        return type;
    }

    @Override
    public void setType(DbColumnType.SimpleDataType type) {
        this.type = type;
    }

    @Override
    public boolean isIndexed() {
        return indexed;
    }

    @Override
    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }
}
