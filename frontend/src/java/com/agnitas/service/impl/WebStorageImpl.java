/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.agnitas.service.WebStorage;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.agnitas.beans.WebStorageEntry;
import org.agnitas.service.WebStorageBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebStorageImpl implements WebStorage {

    private static final Logger logger = LogManager.getLogger(WebStorageImpl.class);
    private static final String NULL_KEY_MSG = "key == null";

    private Map<String, WebStorageEntry> dataMap = new ConcurrentHashMap<>();

    @Override
    public void setup(String dataAsJson) {
        Map<String, WebStorageEntry> newDataMap = new ConcurrentHashMap<>();

        if (StringUtils.isNotBlank(dataAsJson)) {
            try {
                collectDataMap(newDataMap, dataAsJson);
            } catch (Exception e) {
                logger.error("Error occurred: {}", e.getMessage(), e);
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
        Objects.requireNonNull(key, NULL_KEY_MSG);

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
        Objects.requireNonNull(key, NULL_KEY_MSG);
        Objects.requireNonNull(consumer, "consumer == null");

        synchronized (key) {
            consumer.accept(access(key));
        }
    }

    @Override
    public <T extends WebStorageEntry> boolean isPresented(final WebStorageBundle<T> key) {
        Objects.requireNonNull(key, NULL_KEY_MSG);

        synchronized (key) {
            return dataMap.containsKey(key.getName());
        }
    }

    private <T extends WebStorageEntry> T access(WebStorageBundle<T> key) {
        Class<T> type = key.getType();
        return type.cast(dataMap.computeIfAbsent(key.getName(), name -> instantiate(type)));
    }

    private <T> T instantiate(Class<T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private void collectDataMap(Map<String, WebStorageEntry> dataMapToCollectIn, String dataAsJson) throws IOException {
        Map<String, Class<? extends WebStorageEntry>> typeMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        for (WebStorageBundle<? extends WebStorageEntry> bundle : WebStorageBundle.definitions()) {
            typeMap.put(bundle.getName(), bundle.getType());
        }

        JsonFactory factory = new JsonFactory();

        try (JsonParser parser = factory.createParser(dataAsJson)) {
            parser.setCodec(mapper);

            if (JsonToken.START_OBJECT != parser.nextToken()) {
                throw new IOException("Missing expected `{` token");
            }

            JsonNode node = mapper.readTree(parser);

            Iterator<Map.Entry<String, JsonNode>> nodeIterator = node.fields();
            while (nodeIterator.hasNext()) {
                final Map.Entry<String, JsonNode> entry = nodeIterator.next();
                final String name = entry.getKey();
                final Class<? extends WebStorageEntry> type = typeMap.get(name);

                if(type == null) {
                    logger.warn("Missing expected definition for `{}` bundle", name);
                    continue;
                }

                try {
                    dataMapToCollectIn.put(name, mapper.readValue(entry.getValue().traverse(), type));
                } catch (JsonParseException | JsonMappingException e) {
                    logger.warn("Failed to deserialize `{}` bundle", name, e);
                }
            }
        }
    }
}
