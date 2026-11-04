/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.docs;

import static com.agnitas.emm.rest.v2.infrastructure.config.RestConfiguration.API_BASE;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@RestController
public class OpenApiRestV2Controller {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<ObjectNode> template = new AtomicReference<>();

    public OpenApiRestV2Controller(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping(value = "/openapi", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode>  getOpenApi(HttpServletRequest request) throws IOException {
        ObjectNode base = loadTemplate();
        if (base == null) {
            return ResponseEntity.notFound().build();
        }
        ObjectNode spec = base.deepCopy();
        ArrayNode servers = spec.putArray("servers");
        String url = ServletUriComponentsBuilder.fromContextPath(request)
                .path(API_BASE)
                .build()
                .toUriString();
        servers.addObject().put("url", url);

        return ResponseEntity.ok(spec);
    }

    private ObjectNode loadTemplate() throws IOException {
        ObjectNode result = template.get();
        if (result == null) {
            Resource resource = resourceLoader.getResource("/WEB-INF/docs/openapi3.json");
            if (!resource.exists()) {
                return null;
            }
            try (InputStream in = resource.getInputStream()) {
                result = (ObjectNode) objectMapper.readTree(in);
            }
            template.compareAndSet(null, result);
            result = template.get();
        }
        return result;
    }
}
