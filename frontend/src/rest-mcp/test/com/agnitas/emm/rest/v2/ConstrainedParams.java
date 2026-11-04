/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2;

import org.springframework.restdocs.constraints.Constraint;
import org.springframework.restdocs.constraints.ConstraintDescriptionResolver;
import org.springframework.restdocs.constraints.ResourceBundleConstraintDescriptionResolver;
import org.springframework.restdocs.constraints.ValidatorConstraintResolver;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConstrainedParams {
    private final ValidatorConstraintResolver resolver = new ValidatorConstraintResolver();
    private final ConstraintDescriptionResolver descriptionResolver = new ResourceBundleConstraintDescriptionResolver();
    private final Class<?> beanClass;

    public ConstrainedParams(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public ParameterDescriptorWithType with(String name, String baseDescription, Class<?>... activeGroups) {
        List<Constraint> allConstraints = resolver.resolveForProperty(name, beanClass);

        String constraintText = allConstraints.stream()
                .filter(c -> isConstraintInGroup(c, activeGroups))
                .map(descriptionResolver::resolveDescription)
                .filter(desc -> desc != null && !desc.isEmpty())
                .collect(Collectors.joining(". "));

        return parameterWithName(name)
                .description(baseDescription + (constraintText.isEmpty() ? "" : " | Constraints: " + constraintText));
    }

    private boolean isConstraintInGroup(Constraint c, Class<?>[] activeGroups) {
        Object groups = c.getConfiguration().get("groups");
        if (groups == null) {
            return true;
        }

        Class<?>[] constraintGroups = (Class<?>[]) groups;
        if (constraintGroups.length == 0) return true;

        return Arrays.stream(activeGroups).anyMatch(active ->
                Arrays.asList(constraintGroups).contains(active)
        );
    }
}
