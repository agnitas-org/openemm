/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.spring.security.config;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.emm.core.oauth.dao.OAuth2RegistrationClientDao;
import com.agnitas.emm.core.oauth.model.OAuth2RegistrationClientRecord;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

class OAuth2RegisteredClientRepository implements RegisteredClientRepository {

    private final OAuth2RegistrationClientDao dao;
    private final ObjectMapper objectMapper;

    OAuth2RegisteredClientRepository(OAuth2RegistrationClientDao dao) {
        this.dao = dao;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void save(@NonNull RegisteredClient client) {
        OAuth2RegistrationClientRecord clientRecord = toRecord(client);
        if (dao.findById(client.getId()).isPresent()) {
            dao.update(clientRecord);
        } else {
            dao.insert(clientRecord);
        }
    }

    @Override
    public RegisteredClient findById(@NonNull String id) {
        return dao.findById(id)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(@NonNull String clientId) {
        return dao.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    private OAuth2RegistrationClientRecord toRecord(RegisteredClient client) {
        return new OAuth2RegistrationClientRecord(
                client.getId(),
                client.getClientId(),
                client.getClientIdIssuedAt() != null ? client.getClientIdIssuedAt() : Instant.now(),
                client.getClientSecret(),
                client.getClientSecretExpiresAt(),
                defaultIfBlank(client.getClientName(), client.getClientId()),
                toDelimited(client.getClientAuthenticationMethods(), ClientAuthenticationMethod::getValue),
                toDelimited(client.getAuthorizationGrantTypes(), AuthorizationGrantType::getValue),
                toDelimited(client.getRedirectUris()),
                toDelimited(client.getPostLogoutRedirectUris()),
                StringUtils.defaultString(toDelimited(client.getScopes())),
                serializeClientSettings(client.getClientSettings()),
                serializeTokenSettings(client.getTokenSettings())
        );
    }

    private RegisteredClient toRegisteredClient(OAuth2RegistrationClientRecord clientRecord) {
        RegisteredClient.Builder builder = RegisteredClient
                .withId(clientRecord.id())
                .clientId(clientRecord.clientId())
                .clientName(defaultIfBlank(clientRecord.clientName(), clientRecord.clientId()));

        if (clientRecord.clientIdIssuedAt() != null) {
            builder.clientIdIssuedAt(clientRecord.clientIdIssuedAt());
        }
        if (clientRecord.clientSecret() != null) {
            builder.clientSecret(clientRecord.clientSecret());
        }
        if (clientRecord.clientSecretExpiresAt() != null) {
            builder.clientSecretExpiresAt(clientRecord.clientSecretExpiresAt());
        }

        split(clientRecord.clientAuthenticationMethods())
                .map(ClientAuthenticationMethod::new)
                .forEach(builder::clientAuthenticationMethod);

        split(clientRecord.authorizationGrantTypes())
                .map(AuthorizationGrantType::new)
                .forEach(builder::authorizationGrantType);

        split(clientRecord.redirectUris()).forEach(builder::redirectUri);
        split(clientRecord.postLogoutRedirectUris()).forEach(builder::postLogoutRedirectUri);
        split(clientRecord.scopes()).forEach(builder::scope);

        builder.clientSettings(deserializeClientSettings(clientRecord.clientSettings()));
        builder.tokenSettings(deserializeTokenSettings(clientRecord.tokenSettings()));

        return builder.build();
    }

    private String serializeClientSettings(ClientSettings settings) {
        Map<String, Object> map = new HashMap<>();
        map.put("requireProofKey", settings.isRequireProofKey());
        map.put("requireAuthorizationConsent", settings.isRequireAuthorizationConsent());
        return toJson(map);
    }

    private ClientSettings deserializeClientSettings(String json) {
        Map<String, Object> map = fromJson(json);
        return ClientSettings.builder()
                .requireProofKey(Boolean.TRUE.equals(map.get("requireProofKey")))
                .requireAuthorizationConsent(Boolean.TRUE.equals(map.get("requireAuthorizationConsent")))
                .build();
    }

    private String serializeTokenSettings(TokenSettings settings) {
        Map<String, Object> map = new HashMap<>();
        map.put("reuseRefreshTokens", settings.isReuseRefreshTokens());
        map.put("accessTokenFormat", settings.getAccessTokenFormat().getValue());
        map.put("idTokenSignatureAlgorithm", settings.getIdTokenSignatureAlgorithm().getName());
        map.put("authCodeTtlSeconds", settings.getAuthorizationCodeTimeToLive().getSeconds());
        map.put("accessTokenTtlSeconds", settings.getAccessTokenTimeToLive().getSeconds());
        map.put("refreshTokenTtlSeconds", settings.getRefreshTokenTimeToLive().getSeconds());
        map.put("deviceCodeTtlSeconds", settings.getDeviceCodeTimeToLive().getSeconds());
        return toJson(map);
    }

    private TokenSettings deserializeTokenSettings(String json) {
        Map<String, Object> map = fromJson(json);
        TokenSettings.Builder builder = TokenSettings.builder();
        if (map.get("reuseRefreshTokens") instanceof Boolean b) {
            builder.reuseRefreshTokens(b);
        }
        if (map.get("accessTokenFormat") instanceof String v) {
            builder.accessTokenFormat(new OAuth2TokenFormat(v));
        }
        if (map.get("idTokenSignatureAlgorithm") instanceof String name) {
            SignatureAlgorithm alg = SignatureAlgorithm.from(name);
            if (alg != null) {
                builder.idTokenSignatureAlgorithm(alg);
            }
        }
        if (map.get("authCodeTtlSeconds") instanceof Number n) {
            builder.authorizationCodeTimeToLive(Duration.ofSeconds(n.longValue()));
        }
        if (map.get("accessTokenTtlSeconds") instanceof Number n) {
            builder.accessTokenTimeToLive(Duration.ofSeconds(n.longValue()));
        }
        if (map.get("refreshTokenTtlSeconds") instanceof Number n) {
            builder.refreshTokenTimeToLive(Duration.ofSeconds(n.longValue()));
        }
        if (map.get("deviceCodeTtlSeconds") instanceof Number n) {
            builder.deviceCodeTimeToLive(Duration.ofSeconds(n.longValue()));
        }
        return builder.build();
    }

    private Stream<String> split(String value) {
        if (StringUtils.isBlank(value)) {
            return Stream.empty();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank);
    }

    private String toDelimited(Collection<String> items) {
        return toDelimited(items, s -> s);
    }

    private <T> String toDelimited(Collection<T> items, Function<T, String> mapper) {
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }

        return items.stream()
                .map(mapper)
                .collect(Collectors.joining(","));
    }

    private String toJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    private Map<String, Object> fromJson(String json) {
        if (StringUtils.isBlank(json)) {
            return new HashMap<>();
        }
        return objectMapper.readValue(json, new TypeReference<>() {});
    }

}
