/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.agnitas.emm.core.linkcheck.beans.LinkReachability;
import org.agnitas.util.NetworkUtil;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;

import com.agnitas.util.OneOf;

/**
 * Working for checking availability of link URL.
 */
public class LinkcheckWorker implements Runnable {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger( LinkcheckWorker.class);
	
	/** Connection time out. 0 = no timeout. */
	private final int timeout;
	
	/** URL to check. */
	private final String linkToCheck;
	
	/** List to add result of check. */ 
	private final List<LinkReachability> resultList;
	
	/** User agent string. */
	private final String userAgentString;
	
	/** 
	 * @param timeout timeout value for connection
	 * @param linkToCheck URL to check
	 * @param resultList list to add result of link check 
	 */
	public LinkcheckWorker(final int timeout, final String linkToCheck, final List<LinkReachability> resultList, final String userAgentString) {
		this.timeout = timeout;
		this.linkToCheck = Objects.requireNonNull(linkToCheck, "URL to check is null");
		this.resultList = Objects.requireNonNull(resultList, "Result list is null");
		this.userAgentString = Objects.requireNonNull(userAgentString, "User agent string is null");
	}
	
	
	@Override
	public void run() {
		// check if the link has dynamic content.
		boolean dynamic = dynamicLinkCheck();
		
		if (dynamic) {
			if( logger.isInfoEnabled()) {
				logger.info( "Link is dynamic - no checking for: " + linkToCheck);
			}
			
			resultList.add(new LinkReachability(this.linkToCheck, LinkReachability.Reachability.OK));
		} else {
			LinkReachability availability = netBasedTest();
			
			if(logger.isInfoEnabled()) {
				logger.info("Result of checking link '" + this.linkToCheck + "': " + availability.getReachability());
			}
			
			this.resultList.add(availability);
		}
	}
	
	/**
	 * this method checks, if the given link contains dynamic content like ##AGNUID##
	 * if thats the case, we wont check the link anymore.

	 * @return true, the link is dynamic
	 */
	private boolean dynamicLinkCheck() {
		boolean dynamic = false;
		Pattern pattern = Pattern.compile ("##([^#]+)##");
		Matcher aMatch = pattern.matcher(linkToCheck);
		if (aMatch.find() ) {
			// found dynamic content
			return true;
		} 
		return dynamic;		
	}
	
	/**
	 * this method checks, if the given link works. It gets a real connection
	 * to the given server and tries to fetch some answers.

	 * @return availability of checked link
	 */
	private LinkReachability netBasedTest() {
		try {						
			if( logger.isInfoEnabled()) {
				logger.info( "Checking link: " + linkToCheck);
			}
			
			@SuppressWarnings("unused")
			final URL url = new URL(linkToCheck);	// just for checking, we could use the plain String...
			
			try(final CloseableHttpClient httpClient = createHttpClient()) {
				final HttpGet request = new HttpGet(linkToCheck);
				
				// Set user agent, because some servers don't respond without a mandatory set useragent and return http-error 404 or so
				request.addHeader("User-Agent", this.userAgentString);

				// Execute request
				try(final CloseableHttpResponse response = httpClient.execute(request)) {
					final int statusCode = response.getStatusLine().getStatusCode();
					
					if (OneOf.oneIntOf(statusCode, HttpURLConnection.HTTP_NOT_FOUND, HttpURLConnection.HTTP_GONE))  {
						return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.NOT_FOUND);
					}			
					
					return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.OK);
				}
			}
		} catch (final MalformedURLException e) {
			// This is no "real error", this is a test result for the link. So we can log this at INFO level
			if( logger.isInfoEnabled()) {
				logger.info( "Link URL malformed: " + linkToCheck);				
			}
			
			return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.NOT_FOUND);
		} catch (final UnknownHostException e) {
			// This is no "real error", this is a test result for the link. So we can log this at INFO level
			if( logger.isInfoEnabled()) {
				logger.info( "Unknown host: " + linkToCheck);					
			}
			
			return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.NOT_FOUND);
		} catch (final SocketTimeoutException | ConnectTimeoutException e) {
			// This is no "real error", this is a test result for the link. So we can log this at INFO level
			if(logger.isInfoEnabled()) {
				logger.info("Timed out: " + linkToCheck, e);
			}
			
			return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.TIMED_OUT);
		} catch (final IOException e) {
 			// This is no "real error", this is a test result for the link. Since this could be any IO problem, let us report this at WARN level
			logger.warn( "I/O error testing URL: " + linkToCheck, e);

			return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.NOT_FOUND);
		} catch (final KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			logger.error("Cannot create HTTP client", e);
			
			return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.NOT_FOUND);
		}
	}	
	
	/**
	 * Creates a fully configured HTTP client.
	 * 
	 * @return CloseableHttpClient
	 * 
	 * @throws KeyManagementException on errors setting up SSL context
	 * @throws NoSuchAlgorithmException on errors setting up SSL context
	 * @throws KeyStoreException on errors setting up SSL context
	 */
	private final CloseableHttpClient createHttpClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
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
	    builder.setDefaultRequestConfig(createRequestConfig());
	    
	    return builder.build();
	}

	/**
	 * Configures request settings.
	 * 
	 * <ul>
	 *   <li>Socket timeout</li>
	 *   <li>Connection timeout</li>
	 *   <li>Proxy</li>
	 * </ul>
	 * 
	 * @return request settings
	 */
	private final RequestConfig createRequestConfig() {
	    final RequestConfig.Builder configBuilder = RequestConfig.copy(RequestConfig.DEFAULT);
	    configBuilder.setConnectTimeout(timeout);
	    configBuilder.setSocketTimeout(timeout);
		NetworkUtil.setHttpClientProxyFromSystem(configBuilder, linkToCheck);

	    return configBuilder.build();
	}
	
	/**
	 * Creates a SSL context configured to ignore insecure / invalid SSL certificates.
	 * 
	 * @return SSL context
	 * 
	 * @throws KeyManagementException on errors setting up SSL context
	 * @throws NoSuchAlgorithmException on errors setting up SSL context
	 * @throws KeyStoreException on errors setting up SSL context
	 */
	private final SSLContext createSSLContext() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		final SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
	        @Override
			public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
	            return true;
	        }
	    }).build();
		
		return sslContext;
	}

}
