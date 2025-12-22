/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.quartz.component;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.service.JobWorkerBase;
import com.agnitas.util.quartz.JobWorker;
import com.agnitas.util.reflection.ClasspathScanner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class JobWorkerScanner {

    private final ClasspathScanner classpathScanner;

    public JobWorkerScanner(ClasspathScanner classpathScanner) {
        this.classpathScanner = classpathScanner;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Class<? extends JobWorkerBase>> scan(String basePackage) {
        return classpathScanner.findClasses(basePackage, this::isJobWorkerClass)
                .stream()
                .map(clazz -> (Class<? extends JobWorkerBase>) clazz)
                .collect(Collectors.toMap(this::getJobWorkerName, Function.identity()));
    }

    private boolean isJobWorkerClass(Class<?> clazz) {
        return JobWorkerBase.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers());
    }

    private String getJobWorkerName(Class<?> clazz) {
        String workerName = null;
        if (clazz.isAnnotationPresent(JobWorker.class)) {
            workerName = clazz.getAnnotation(JobWorker.class).value();
        }

        if (StringUtils.isBlank(workerName)) {
            throw new IllegalStateException("JobWorker class " + clazz.getName() + " has no @JobWorker annotation or job name is blank!");
        }

        return workerName.trim();
    }

}
