/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.complexity.service.impl;

import com.agnitas.beans.ComProfileField;
import com.agnitas.dao.ComProfileFieldDao;
import com.agnitas.emm.core.target.complexity.service.AbstractTargetComplexityAnalyzer;
import com.agnitas.emm.core.target.complexity.service.TargetComplexityCriterion;
import com.agnitas.emm.core.target.complexity.bean.TargetComplexityEvaluationCache;
import com.agnitas.emm.core.target.complexity.bean.TargetComplexityEvaluationState;
import com.agnitas.emm.core.target.complexity.bean.CustomerTableColumnMetadata;
import com.agnitas.emm.core.target.complexity.bean.impl.CustomerTableColumnMetadataImpl;
import com.agnitas.emm.core.target.eql.ast.ProfileFieldAtomEqlNode;
import org.agnitas.util.DbColumnType;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public final class TargetProfileFieldNodeComplexityAnalyzer extends AbstractTargetComplexityAnalyzer<ProfileFieldAtomEqlNode> {
    private static final Logger logger = Logger.getLogger(TargetProfileFieldNodeComplexityAnalyzer.class);

    private ComProfileFieldDao profileFieldDao;

    public TargetProfileFieldNodeComplexityAnalyzer(ComProfileFieldDao profileFieldDao) {
        super(ProfileFieldAtomEqlNode.class);
        this.profileFieldDao = profileFieldDao;
    }

    @Override
    public void analyze(ProfileFieldAtomEqlNode node, boolean negative, TargetComplexityEvaluationState state) {
        analyze(state, node.getName());
    }

    private void analyze(TargetComplexityEvaluationState state, String column) {
        if (!state.isCustomerTableColumnInUse(column)) {
            CustomerTableColumnMetadata metadata = getCustomerTableColumnMetadata(state.getCache(), state.getCompanyId(), column);
            DbColumnType.SimpleDataType type = metadata.getType();

            if (type == DbColumnType.SimpleDataType.Characters) {
                add(state, TargetComplexityCriterion.COLUMN_TEXT);
            } else if (type == DbColumnType.SimpleDataType.Date) {
                add(state, TargetComplexityCriterion.COLUMN_DATE);
            }

            if (!metadata.isIndexed()) {
                add(state, TargetComplexityCriterion.COLUMN_WITHOUT_INDEX);
            }

            state.setCustomerTableColumnInUse(column, true);
        }
    }

    private CustomerTableColumnMetadata getCustomerTableColumnMetadata(TargetComplexityEvaluationCache cache, int companyId, String column) {
        CustomerTableColumnMetadata metadata = cache.getCustomerTableColumnMetadata(column);

        if (metadata == null) {
            metadata = getCustomerTableColumnMetadata(companyId, column);

            if (metadata == null) {
                throw new RuntimeException("Failed to resolve customer table column: `" + column + "`");
            }

            cache.putCustomerTableColumnMetadata(column, metadata);
        }

        return metadata;
    }

    private CustomerTableColumnMetadata getCustomerTableColumnMetadata(int companyId, String column) {
        CustomerTableColumnMetadata metadata = new CustomerTableColumnMetadataImpl();

        try {
            DbColumnType type = profileFieldDao.getColumnType(companyId, column);

            if (type == null) {
                ComProfileField field = profileFieldDao.getProfileFieldByShortname(companyId, column);

                if (field == null) {
                    return null;
                }

                metadata.setType(field.getSimpleDataType());
                metadata.setIndexed(profileFieldDao.isColumnIndexed(companyId, field.getColumn()));
            } else {
                metadata.setType(type.getSimpleDataType());
                metadata.setIndexed(profileFieldDao.isColumnIndexed(companyId, column));
            }
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage(), e);
            return null;
        }

        return metadata;
    }
}
