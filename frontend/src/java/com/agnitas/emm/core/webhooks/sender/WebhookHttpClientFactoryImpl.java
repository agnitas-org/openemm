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
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.util.NetworkUtil;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import com.agnitas.emm.core.webhooks.config.WebhookConfigService;

/**
 * Implementation of {@link WebhookHttpClientFactory}.
 */
public final class WebhookHttpClientFactoryImpl implements WebhookHttpClientFactory {
	
	/** Configuration service. */
	private WebhookConfigService configService;
	
	@Override
	public final CloseableHttpClient createHttpClient(final int companyID) throws WebhookHttpClientFactoryException {
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
	private final CloseableHttpClient doCreateHttpClient(final int companyID) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		final HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
		final SSLContext sslContext = createSSLContext();
		final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
		final Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.register("https", sslSocketFactory)
				.build();
		
		final HttpClientBuilder builder = HttpClientBuilder.create();
	    builder.setSSLContext(sslContext);
	    builder.setConnectionManager(new PoolingHttpClientConnectionManager(socketFactoryRegistry));
	    builder.setDefaultRequestConfig(createRequestConfig(companyID));
	    
	    return builder.build();
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
	private final SSLContext createSSLContext() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		final SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
	        @Override
			public boolean isTrusted(X509Certificate[] arg0, String arg1) {
	            return true;
	        }
	    }).build();
		
		return sslContext;
	}

	/**
	 * Creates the request configuration for the HTTP client.
	 * Configures timeouts according to {@link ConfigService} and HTTP proxy from system.
	 * 
	 * @param companyId company ID
	 * 
	 * @return request configuration
	 */
	private final RequestConfig createRequestConfig(final int companyId) {
	    final RequestConfig.Builder configBuilder = RequestConfig.copy(RequestConfig.DEFAULT);
	    configBuilder.setConnectTimeout(this.configService.getConnectTimeoutMillis(companyId));
	    configBuilder.setSocketTimeout(this.configService.getSocketTimeoutMillis(companyId));
		NetworkUtil.setHttpClientProxyFromSystem(configBuilder, null);

	    return configBuilder.build();
	}

	/**
	 * Set configuration service.
	 * 
	 * @param service configuration service
	 */
	public final void setConfigService(final ConfigService service) {
		this.configService = new WebhookConfigService(service);
	}
}
