/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target;

import java.util.ArrayList;
import java.util.List;

import com.agnitas.emm.core.commons.dto.IntRange;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.target.beans.TargetComplexityGrade;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCode;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCodeProperties;

public class TargetUtils {

    public static int MIN_RECIPIENT_COUNT_CONDITION_THRESHOLD = 50_000;

    public static TargetComplexityGrade getComplexityGrade(int complexityIndex, int recipientsCount) {
        if (complexityIndex < 0) {
            return null;
        }

        int complexity = getComplexity(complexityIndex, recipientsCount);

        if (complexity < TargetComplexityGrade.GREEN.getMaxThreshold()) {
            return TargetComplexityGrade.GREEN;
        } else if (complexity < TargetComplexityGrade.YELLOW.getMaxThreshold()) {
            return TargetComplexityGrade.YELLOW;
        } else {
            return TargetComplexityGrade.RED;
        }
    }

    public static IntRange getComplexityIndexesRange(TargetComplexityGrade complexityGrade) {
        if (complexityGrade == null) {
            return null;
        }

        if (TargetComplexityGrade.GREEN.equals(complexityGrade)) {
            return new IntRange(null, complexityGrade.getMaxThreshold());
        }

        if (TargetComplexityGrade.YELLOW.equals(complexityGrade)) {
            return new IntRange(TargetComplexityGrade.GREEN.getMaxThreshold(), complexityGrade.getMaxThreshold());
        }

        return new IntRange(TargetComplexityGrade.YELLOW.getMaxThreshold(), null);
    }

    public static int getComplexityAdjustment(int recipientsCount) {
        if (recipientsCount >= 3_000_000) {
            return 7;
        } else if (recipientsCount >= 1_000_000) {
            return 5;
        } else if (recipientsCount >= 500_000) {
            return 3;
        } else if (recipientsCount >= 100_000) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int getComplexity(int complexityIndex, int recipientsCount) {
        if (recipientsCount < MIN_RECIPIENT_COUNT_CONDITION_THRESHOLD) {
            return 0;
        }

        return complexityIndex + getComplexityAdjustment(recipientsCount);
    }

	public static List<Integer> getInvolvedTargetIds(String targetExpression) {
		List<Integer> returnList = new ArrayList<>();
		if (StringUtils.isNotBlank(targetExpression)) {
			for (String targetPart : targetExpression.split("&|\\|")) {
				if (StringUtils.isNotBlank(targetPart)) {
					returnList.add(Integer.parseInt(targetPart));
				}
			}
		}
		return returnList;
	}
	
	/**
	 * Checks if target group can be used in content blocks.
	 * 
	 * @param sqlCode SQL code of target group
	 * 
	 * @return <code>true</code> if target group can be used in content blocks
	 */
	public static boolean canBeUsedInContentBlocks(final SqlCode sqlCode) {
		final SqlCodeProperties properties = sqlCode.getCodeProperties();

		/*
		 * Code is backend compatible if,
		 * - no non-profile-tables are used
		 * - no reference tables are used
		 * - generated SQL does not contain sub-selects
		 * - and generated SQL does not use date arithmetics
		 */

		return !properties.isUsingNonCustomerTables() && !properties.isUsingReferenceTables() && !properties.isUsingSubselects() && !properties.isUsingDateArithmetics();
	}

}
