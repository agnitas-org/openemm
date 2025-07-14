/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.table.util;

import static org.springframework.web.servlet.support.RequestContextUtils.getLocale;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils;

public class TableTagUtils {

    private static final Logger logger = LogManager.getLogger(TableTagUtils.class);

    private TableTagUtils() {

    }

    public static Comparator<Object> getComparator(String property, boolean reversed) {
        Comparator<Object> comparator = (o1, o2) -> {
            Object v1 = TableTagUtils.getPropertyValue(o1, property);
            Object v2 = TableTagUtils.getPropertyValue(o2, property);

            if (v1 == null && v2 == null) {
                return 0;
            }
            if (v1 == null) {
                return 1;
            }
            if (v2 == null) {
                return -1;
            }

            Class<?> propertyType = TableTagUtils.getPropertyType(property, o1);

            if (propertyType.equals(String.class)) {
                return ((String) v1).compareToIgnoreCase((String) v2);
            }

            if (ArrayUtils.contains(propertyType.getInterfaces(), Comparable.class)) {
                return ((Comparable) v1).compareTo(v2);
            }

            throw new UnsupportedOperationException("Comparator for type '" + propertyType.getName() + "' not supported!");
        };

        if (reversed) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    public static Class<?> getPropertyType(String name, Object item) {
        try {
            return PropertyUtils.getPropertyType(item, name);
        } catch (Exception e) {
            logger.error("Can't read property type of property '{}'", name);
            throw new RuntimeException(e);
        }
    }

    public static Object getPropertyValue(Object bean, String property) {
        if (bean instanceof Map<?, ?> map) {
            return map.get(property);
        }

        try {
            return PropertyUtils.getSimpleProperty(bean, property);
        } catch (Exception e) {
            logger.error("Can't read value of property '{}'", property);
            throw new RuntimeException(e);
        }
    }

    public static String buildUrl(String url, Map<String, String> params) {
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            params.forEach(uriBuilder::addParameter);
            return uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            logger.error("Can't build url! Request URI: {}", url);
            throw new RuntimeException(e);
        }
    }

    public static List<Integer> getPageNumbersToDisplay(int currentPage, int totalPages, int maxPagesCount) {
        int startPage;
        int endPage;

        if (totalPages <= maxPagesCount) {
            startPage = 1;
            endPage = totalPages;
        } else {
            if (currentPage <= Math.ceil(maxPagesCount / 2.0)) {
                startPage = 1;
                endPage = maxPagesCount;
            } else if (currentPage + Math.floorDiv(maxPagesCount, 2) > totalPages) {
                startPage = totalPages - maxPagesCount + 1;
                endPage = totalPages;
            } else {
                startPage = currentPage - Math.floorDiv(maxPagesCount, 2);
                endPage = currentPage + Math.floorDiv(maxPagesCount, 2) - 1;
            }
        }

        List<Integer> pages = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pages.add(i);
        }

        return pages;
    }

    public static String translateMessage(String key, HttpServletRequest req) {
        MessageSource messageSource = RequestContextUtils.findWebApplicationContext(req);
        if (messageSource == null) {
            throw new IllegalStateException("Message source not found!");
        }

        String message = messageSource.getMessage(key, null, null, getLocale(req));
        return message == null ? "???%s???".formatted(key) : message;
    }

    public static int calculateTotalPages(int listSize, int pageSize) {
        return (int) Math.ceil((double) listSize / pageSize);
    }
}
