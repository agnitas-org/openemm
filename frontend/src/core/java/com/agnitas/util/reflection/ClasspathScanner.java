/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.reflection;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ClasspathScanner {

    private static final Logger logger = LogManager.getLogger(ClasspathScanner.class);

    public Set<Class<?>> findClasses(String basePackage, Predicate<Class<?>> filter) {
        Set<Class<?>> result = new HashSet<>();
        String path = basePackage.replace('.', '/');

        try {
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    File directory = new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8));
                    if (directory.exists() && directory.isDirectory()) {
                        findInDirectory(directory, basePackage, result, filter);
                    }
                } else if ("jar".equals(protocol)) {
                    findInJar(resource, path, result, filter);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan classpath for " + basePackage, e);
        }

        return result;
    }

    private void findInDirectory(File dir, String currentPackage, Set<Class<?>> result, Predicate<Class<?>> filter) {
        for (File file : Objects.requireNonNullElse(dir.listFiles(), new File[0])) {
            if (file.isDirectory()) {
                findInDirectory(file, currentPackage + "." + file.getName(), result, filter);
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                String className = currentPackage + "." + file.getName().replace(".class", "");
                tryAddClass(className, result, filter);
            }
        }
    }

    private void findInJar(URL resource, String path, Set<Class<?>> result, Predicate<Class<?>> filter) {
        try {
            JarURLConnection conn = (JarURLConnection) resource.openConnection();
            try (JarFile jarFile = conn.getJarFile()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith(path) && name.endsWith(".class") && !name.contains("$")) {
                        String className = name.replace('/', '.').replace(".class", "");
                        tryAddClass(className, result, filter);
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to scan JAR: {}", resource, e);
        }
    }

    private void tryAddClass(String className, Set<Class<?>> result, Predicate<Class<?>> filter) {
        try {
            Class<?> clazz = Class.forName(className);
            if (filter.test(clazz)) {
                result.add(clazz);
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            logger.warn("Could not load class {}", className);
        }
    }

}
