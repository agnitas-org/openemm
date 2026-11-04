/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.restdocs.constraints.Constraint;
import org.springframework.restdocs.constraints.ValidatorConstraintResolver;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.snippet.Attributes;

public class ConstrainedFields {

    private final ValidatorConstraintResolver resolver = new ValidatorConstraintResolver();
    private final Class<?> beanClass;

    public ConstrainedFields(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public FieldDescriptor withPath(String path) {
        String propertyName = extractPropertyName(path);

        List<Constraint> constraints = new ArrayList<>(resolver.resolveForProperty(path, beanClass)); // normal resolver for plain fields
        addInnerConstraintsForJsonNullable(propertyName, constraints); // special handling for JsonNullable<@Constraint T>

        List<Constraint> translated = constraints.stream() // adapt for what restdocs-api-spec expects
                .map(this::translateConstraint)
                .toList();

        return fieldWithPath(path)
                .attributes(Attributes.key("validationConstraints").value(translated));
    }

    private void addInnerConstraintsForJsonNullable(String propertyName, List<Constraint> target) {
        try {
            // Java records
            RecordComponent[] components = beanClass.getRecordComponents();
            if (components != null) {
                for (RecordComponent component : components) {
                    if (component.getName().equals(propertyName)) {
                        extractTypeUseConstraints(component.getAnnotatedType(), target);
                        return;
                    }
                }
            }
            // Regular classes
            Field field = beanClass.getDeclaredField(propertyName);
            extractTypeUseConstraints(field.getAnnotatedType(), target);
        } catch (Exception ignored) {
            // Field not found → no inner constraints
        }
    }

    private void extractTypeUseConstraints(AnnotatedType annotatedType, List<Constraint> target) {
        if (!(annotatedType instanceof AnnotatedParameterizedType apt)) {
            return;
        }

        AnnotatedType[] annotatedArgs = apt.getAnnotatedActualTypeArguments();
        if (annotatedArgs.length == 0) {
            return;
        }

        AnnotatedType innerType = annotatedArgs[0];

        for (Annotation ann : innerType.getAnnotations()) {
            String name = ann.annotationType().getName();
            Map<String, Object> config = new HashMap<>();

            if (ann instanceof jakarta.validation.constraints.Size size) {
                config.put("min", size.min());
                config.put("max", size.max());
                target.add(new Constraint(name, config));
            } else if (name.startsWith("jakarta.validation.constraints.")) {
                target.add(new Constraint(name, config));
            }
        }
    }

    private Constraint translateConstraint(Constraint c) {
        String name = c.getName();

        // Jakarta → javax (mandatory for restdocs-api-spec)
        if (name.startsWith("jakarta.validation.constraints.")) {
            name = "javax.validation.constraints." + name.substring("jakarta.validation.constraints.".length());
        }

        // @Size on String → @Length (the ONLY constraint the library maps to minLength/maxLength)
        if ("javax.validation.constraints.Size".equals(name)) {
            name = "org.hibernate.validator.constraints.Length";
        }

        return new Constraint(name, c.getConfiguration());
    }

    private String extractPropertyName(String path) {
        int dot = path.lastIndexOf('.');
        return dot >= 0 ? path.substring(dot + 1) : path;
    }
}
