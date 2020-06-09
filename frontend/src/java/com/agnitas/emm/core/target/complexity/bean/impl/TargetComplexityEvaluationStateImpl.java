/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.complexity.bean.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.agnitas.emm.core.target.complexity.bean.TargetComplexityEvaluationCache;
import com.agnitas.emm.core.target.complexity.bean.TargetComplexityEvaluationState;

public class TargetComplexityEvaluationStateImpl implements TargetComplexityEvaluationState {
    private final int companyId;
    private final TargetComplexityEvaluationCache cache;
    private int complexityIndex;
    private int logicOperatorCount;
    private Set<String> customerTableColumns = new HashSet<>();

    public TargetComplexityEvaluationStateImpl(int companyId, TargetComplexityEvaluationCache cache) {
        this.companyId = companyId;
        this.cache = Objects.requireNonNull(cache, "cache == null");
    }

    @Override
    public int getCompanyId() {
        return companyId;
    }

    @Override
    public TargetComplexityEvaluationCache getCache() {
        return cache;
    }

    @Override
    public int getComplexityIndex() {
        return complexityIndex;
    }

    @Override
    public void setComplexityIndex(int complexityIndex) {
        this.complexityIndex = complexityIndex;
    }

    @Override
    public int getLogicOperatorCount() {
        return logicOperatorCount;
    }

    @Override
    public void setLogicOperatorCount(int logicOperatorCount) {
        this.logicOperatorCount = logicOperatorCount;
    }

    @Override
    public boolean isCustomerTableColumnInUse(String column) {
        return customerTableColumns.contains(column);
    }

    @Override
    public void setCustomerTableColumnInUse(String column, boolean inUse) {
        if (inUse) {
            customerTableColumns.add(column);
        } else {
            customerTableColumns.remove(column);
        }
    }
}
