/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target;

import com.agnitas.emm.core.target.beans.TargetComplexityGrade;

public class TargetUtils {
    public static TargetComplexityGrade getComplexityGrade(int complexityIndex, int recipientsCount) {
        if (complexityIndex < 0) {
            return null;
        }

        int complexity = getComplexity(complexityIndex, recipientsCount);

        if (complexity < 10) {
            return TargetComplexityGrade.GREEN;
        } else if (complexity < 18) {
            return TargetComplexityGrade.YELLOW;
        } else {
            return TargetComplexityGrade.RED;
        }
    }

    private static int getComplexity(int complexityIndex, int recipientsCount) {
        if (recipientsCount < 50_000) {
            return 0;
        } else if (recipientsCount >= 3_000_000) {
            return complexityIndex + 7;
        } else if (recipientsCount >= 1_000_000) {
            return complexityIndex + 5;
        } else if (recipientsCount >= 500_000) {
            return complexityIndex + 3;
        } else if (recipientsCount >= 100_000) {
            return complexityIndex + 1;
        } else {
            return complexityIndex;
        }
    }
}
