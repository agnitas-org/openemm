/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.reflection;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ClasspathScanner {

    private static final Logger logger = LogManager.getLogger(ClasspathScanner.class);

    public Set<Class<?>> findClasses(String basePackage, Predicate<Class<?>> filter) {
        Set<Class<?>> result = new HashSet<>();

        String path = basePackage.replace('.', '/');
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);

        if (resource == null) {
            throw new IllegalStateException("Could not find resource for: " + path);
        }

        File directory = new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8));
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalStateException("Invalid directory for: " + basePackage);
        }

        find(directory, basePackage, result, filter);
        return result;
    }

    private void find(File dir, String currentPackage, Set<Class<?>> result, Predicate<Class<?>> filter) {
        for (File file : Objects.requireNonNullElse(dir.listFiles(), new File[]{})) {
            if (file.isDirectory()) {
                find(file, currentPackage + "." + file.getName(), result, filter);
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                String className = currentPackage + "." + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (filter.test(clazz)) {
                        result.add(clazz);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    logger.warn("Could not find class {}", className);
                }
            }
        }
    }

}
