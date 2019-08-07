/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.agnitas.emm.core.workflow.beans.WorkflowRecipient.WorkflowTargetOption;

public class TargetExpressionUtils {
    private static final Pattern TARGET_IDS_FROM_EXPRESSION_PATTERN = Pattern.compile( "^.*?(\\d+)(.*)$");
    private static final String OPERATOR_AND = "&";
    private static final String OPERATOR_OR = "|";

    public static Set<Integer> getTargetIds(String targetExpression) {
        Set<Integer> targetIds = new HashSet<>();
        if (StringUtils.isNotBlank(targetExpression)) {
            Matcher matcher = TARGET_IDS_FROM_EXPRESSION_PATTERN.matcher(targetExpression);
            while (matcher.matches()) {
                targetIds.add(Integer.parseInt(matcher.group(1)));
                targetExpression = matcher.group(2);
                matcher = TARGET_IDS_FROM_EXPRESSION_PATTERN.matcher(targetExpression);
            }
        }
        return targetIds;
    }

    /**
     * Make a target expression out of {@code targetGroupIds} combined by either conjunction (AND) or disjunction (OR) operator.
     *
     * @param targetGroupIds target groups to be included into a target expression.
     * @param conjunction use conjunction ({@code true}) or disjunction ({@code false}) to combine target groups.
     * @return a composed target expression or an empty string.
     */
    public static String makeTargetExpression(Collection<Integer> targetGroupIds, boolean conjunction) {
        if (CollectionUtils.isEmpty(targetGroupIds)) {
            return "";
        }

        return targetGroupIds.stream()
            .filter(id -> id != null && id != 0)
            .map(Object::toString)
            .collect(Collectors.joining(conjunction ? OPERATOR_AND : OPERATOR_OR));
    }

    public static String makeTargetExpression(Collection<Integer> targetGroupIds, WorkflowTargetOption option) {
        if (CollectionUtils.isEmpty(targetGroupIds) || option == null) {
            return "";
        }

        switch (option) {
            case ALL_TARGETS_REQUIRED:
                return makeTargetExpression(targetGroupIds, true);

            case NOT_IN_TARGETS:
                return negative(makeTargetExpression(targetGroupIds, true));

            case ONE_TARGET_REQUIRED:
                return makeTargetExpression(targetGroupIds, false);

            default:
                throw new UnsupportedOperationException("Missing implementation for target option: " + option);
        }
    }

    private static String negative(String expression) {
        if (StringUtils.isBlank(expression)) {
            return expression;
        }

        return "!(" + expression + ")";
    }
}
