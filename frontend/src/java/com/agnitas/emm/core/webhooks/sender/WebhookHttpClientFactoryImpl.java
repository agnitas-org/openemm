/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.sender;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.webhooks.config.WebhookConfigService;
import com.agnitas.util.NetworkUtil;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContextBuilder;

/**
 * Implementation of {@link WebhookHttpClientFactory}.
 */
public final class WebhookHttpClientFactoryImpl implements WebhookHttpClientFactory {

	private final WebhookConfigService configService;

	public WebhookHttpClientFactoryImpl(ConfigService configService) {
		this.configService = new WebhookConfigService(configService);
	}

	@Override
	public CloseableHttpAsyncClient createHttpAsyncClient(int companyID) throws WebhookHttpClientFactoryException {
		try {
			return doCreateHttpAsyncClient(companyID);
		} catch (Exception e) {
			throw new WebhookHttpClientFactoryException("Error creating CloseableHttpClient", e);
		}
	}

	private CloseableHttpAsyncClient doCreateHttpAsyncClient(int companyId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder
				.create()
				.setTlsStrategy(new DefaultClientTlsStrategy(createSSLContext(), NoopHostnameVerifier.INSTANCE))
				.build();

		return HttpAsyncClients
				.custom()
				.setConnectionManager(connectionManager)
				.setDefaultRequestConfig(createAsyncRequestConfig(companyId))
				.build();
	}

	@Override
	public CloseableHttpClient createHttpClient(int companyID) throws WebhookHttpClientFactoryException {
		try {
			return doCreateHttpClient(companyID);
		} catch(final Exception e) {
			throw new WebhookHttpClientFactoryException("Error creating CloseableHttpClient", e);
		}
	}

	/**
	 * Creates the HTTP client for sending webhook messages.
	 *
	 * @param companyID company ID
	 *
	 * @return HTTP client
	 *
	 * @throws KeyManagementException on error setting up TLS configuration
	 * @throws NoSuchAlgorithmException on errors setting up TLS configuration
	 * @throws KeyStoreException on errors setting up TLS configuration
	 */
	private CloseableHttpClient doCreateHttpClient(int companyID) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
				.create()
				.setTlsSocketStrategy(new DefaultClientTlsStrategy(createSSLContext(), NoopHostnameVerifier.INSTANCE))
				.build();

		return HttpClientBuilder
				.create()
				.setConnectionManager(connectionManager)
				.setDefaultRequestConfig(createRequestConfig(companyID))
				.build();
	}

	/**
	 * Creates the SSL context for the HTTP client.
	 * The SSL context is configured to accept any server certificate without checking.
	 *
	 * @return SSL context
	 *
	 * @throws KeyManagementException on errors creating the SSL context
	 * @throws NoSuchAlgorithmException on errors creating the SSL context
	 * @throws KeyStoreException on errors creating the SSL context
	 */
	private SSLContext createSSLContext() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		return new SSLContextBuilder()
				.loadTrustMaterial(null, (arg0, arg1) -> true)
				.build();
	}

	/**
	 * Creates the request configuration for the HTTP client.
	 * Configures timeouts according to {@link ConfigService} and HTTP proxy from system.
	 *
	 * @param companyId company ID
	 *
	 * @return request configuration
	 */
	private RequestConfig createRequestConfig(int companyId) {
		final RequestConfig.Builder configBuilder = RequestConfig.copy(RequestConfig.DEFAULT)
				.setConnectionRequestTimeout(configService.getConnectTimeoutMillis(companyId), TimeUnit.MILLISECONDS)
				.setResponseTimeout(configService.getSocketTimeoutMillis(companyId), TimeUnit.MILLISECONDS);

		NetworkUtil.setHttpClientProxyFromSystem(configBuilder, null);

		return configBuilder.build();
	}

	private RequestConfig createAsyncRequestConfig(int companyId) {
		final RequestConfig.Builder configBuilder = RequestConfig.copy(RequestConfig.DEFAULT)
				.setConnectionRequestTimeout(configService.getConnectTimeoutMillis(companyId), TimeUnit.MILLISECONDS)
				.setResponseTimeout(configService.getSocketTimeoutMillis(companyId), TimeUnit.MILLISECONDS);

		NetworkUtil.setHttpClientProxyFromSystem(configBuilder, null);

		return configBuilder.build();
	}

}
