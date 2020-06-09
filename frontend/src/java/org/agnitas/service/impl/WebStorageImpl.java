/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.agnitas.beans.WebStorageEntry;
import org.agnitas.service.WebStorage;
import org.agnitas.service.WebStorageBundle;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class WebStorageImpl implements WebStorage {
    private static final Logger logger = Logger.getLogger(WebStorageImpl.class);

    private Map<String, WebStorageEntry> dataMap = new ConcurrentHashMap<>();

    @Override
    public void setup(String dataAsJson) {
        Map<String, WebStorageEntry> newDataMap = new ConcurrentHashMap<>();

        if (StringUtils.isNotBlank(dataAsJson)) {
            try {
                collectDataMap(newDataMap, dataAsJson);
            } catch (Exception e) {
                logger.error("Error occurred: " + e.getMessage(), e);
            }
        }

        this.dataMap = newDataMap;
    }

    @Override
    public void reset() {
        dataMap.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends WebStorageEntry> T get(WebStorageBundle<T> key) {
        Objects.requireNonNull(key, "key == null");

        synchronized (key) {
            try {
                return (T) access(key).clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public <T extends WebStorageEntry> void access(WebStorageBundle<T> key, Consumer<T> consumer) {
        Objects.requireNonNull(key, "key == null");
        Objects.requireNonNull(consumer, "consumer == null");

        synchronized (key) {
            consumer.accept(access(key));
        }
    }

    private <T extends WebStorageEntry> T access(WebStorageBundle<T> key) {
        Class<T> type = key.getType();
        return type.cast(dataMap.computeIfAbsent(key.getName(), name -> instantiate(type)));
    }

    private <T> T instantiate(Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void collectDataMap(Map<String, WebStorageEntry> dataMapToCollectIn, String dataAsJson) throws IOException {
        Map<String, Class<? extends WebStorageEntry>> typeMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

        for (WebStorageBundle<? extends WebStorageEntry> bundle : WebStorageBundle.definitions()) {
            typeMap.put(bundle.getName(), bundle.getType());
        }

        JsonFactory factory = new JsonFactory();

        try (JsonParser parser = factory.createJsonParser(dataAsJson)) {
            parser.setCodec(mapper);

            if (JsonToken.START_OBJECT != parser.nextToken()) {
                throw new IOException("Missing expected `{` token");
            }

            for ( ; ; ) {
                JsonToken token = parser.nextToken();

                if (token == JsonToken.FIELD_NAME) {
                    String name = parser.getCurrentName();
                    Class<? extends WebStorageEntry> type = typeMap.get(name);

                    parser.nextToken();

                    if (type == null) {
                        parser.skipChildren();
                        logger.warn("Missing expected definition for `" + name + "` bundle");
                    } else {
                        try {
                            dataMapToCollectIn.put(name, mapper.readValue(parser.readValueAsTree(), type));
                        } catch (JsonMappingException e) {
                            logger.warn("Failed to deserialize `" + name + "` bundle", e);
                        }
                    }
                } else if (token == JsonToken.END_OBJECT) {
                    return;
                } else {
                    throw new IOException("Unexpected token (field name or `}` were expected)");
                }
            }
        }
    }
}
