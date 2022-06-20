/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;

public class NetworkUtil {
	
	/** Proxy default port (value: {@value #PROXY_DEFAULT_PORT}). */
	public static final int PROXY_DEFAULT_PORT = 8080;
	
	public static final class ProxySettings {
		public final String host;
		public final int port;
		
		public ProxySettings(final String host, final int port) {
			this.host = Objects.requireNonNull(host, "host is null");
			this.port = port;
		}
		
		public final Proxy asProxy() {
			return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(this.host, this.port));
		}
	}
	
	public static List<InetAddress> listLocalInetAddresses() throws SocketException {
		List<InetAddress> list = new Vector<>();
		
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while( interfaces.hasMoreElements()) {
			listInetAddressesForInterface( list, interfaces.nextElement());
		}
		
		return list;
	}
	
	private static void listInetAddressesForInterface( List<InetAddress> list, NetworkInterface iface) {
		Enumeration<InetAddress> addresses = iface.getInetAddresses();
		
		while( addresses.hasMoreElements()) {
			list.add( addresses.nextElement());
		}
	}

	public static byte[] loadUrlData(String url, final String userAgent) throws Exception {
		HttpClient httpClient = new HttpClient();
		setHttpClientProxyFromSystem(httpClient, url);
		GetMethod get = new GetMethod(url);
		
		if(StringUtils.isNotEmpty(userAgent)) {
			get.addRequestHeader("User-Agent", userAgent);
		}
		
		get.setFollowRedirects(true);
		
		try {
			httpClient.getParams().setParameter("http.connection.timeout", 5000);
			int httpReturnCode = httpClient.executeMethod(get);

			if (httpReturnCode == 200) {
				// Don't use get.getResponseBody, it causes a warning in log
				try (InputStream inputStream = get.getResponseBodyAsStream()) {
					return IOUtils.toByteArray(inputStream);
				}
			} else {
				throw new Exception("ERROR: Received httpreturncode " + httpReturnCode);
			}
		} finally {
			get.releaseConnection();
		}
	}
	
	public static void setHttpClientProxyFromSystem(final RequestConfig.Builder configBuilder, final String url) {
		// TODO Change code to use determineProxySettingsFromSystem(String)
		String proxyHost = System.getProperty("http.proxyHost");
		if (StringUtils.isNotBlank(proxyHost)) {
			String proxyPort = System.getProperty("http.proxyPort");
			String nonProxyHosts = System.getProperty("http.nonProxyHosts");
		
			if (StringUtils.isBlank(nonProxyHosts)) {
				if (StringUtils.isNotBlank(proxyHost)) {
					if (StringUtils.isNotBlank(proxyPort) && AgnUtils.isNumber(proxyPort)) {
						configBuilder.setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort)));
					} else {
						configBuilder.setProxy(new HttpHost(proxyHost, 8080));
					}
				}
			} else {
				boolean ignoreProxy = false;
				String urlDomain = getDomainFromUrl(url);
				if (urlDomain != null) {
					urlDomain = urlDomain.trim().toLowerCase();
					for (String nonProxyHost : nonProxyHosts.split("\\||,|;| ")) {
						nonProxyHost = nonProxyHost.trim().toLowerCase();
						if (urlDomain.equals(nonProxyHost) || urlDomain.endsWith("." + nonProxyHost)) {
							ignoreProxy = true;
							break;
						}
					}
					if (!ignoreProxy) {
						if (StringUtils.isNotBlank(proxyHost)) {
							if (StringUtils.isNotBlank(proxyPort) && AgnUtils.isNumber(proxyPort)) {
								configBuilder.setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort)));
							} else {
								configBuilder.setProxy(new HttpHost(proxyHost, 8080));
							}
						}
					}
				}
			}
		}
	}

	public static void setHttpClientProxyFromSystem(HttpClient httpClient, String url) {
		// TODO Change code to use determineProxySettingsFromSystem(String)
		String proxyHost = System.getProperty("http.proxyHost");
		if (StringUtils.isNotBlank(proxyHost)) {
			String nonProxyHosts = System.getProperty("http.nonProxyHosts");
			String proxyPort = System.getProperty("http.proxyPort");

			if (StringUtils.isBlank(nonProxyHosts)) {
				httpClient.getHostConfiguration().setProxy(proxyHost, NumberUtils.toInt(proxyPort, 8080));
			} else {
				boolean ignoreProxy = false;
				String urlDomain = getDomainFromUrl(url);
				if (urlDomain != null) {
					urlDomain = urlDomain.trim().toLowerCase();
					for (String nonProxyHost : nonProxyHosts.split("\\||,|;| ")) {
						nonProxyHost = nonProxyHost.trim().toLowerCase();
						if (urlDomain.equals(nonProxyHost) || urlDomain.endsWith("." + nonProxyHost)) {
							ignoreProxy = true;
							break;
						}
					}

					if (!ignoreProxy) {
						httpClient.getHostConfiguration().setProxy(proxyHost, NumberUtils.toInt(proxyPort, 8080));
					}
				}
			}
		}
	}
	
	public static String getDomainFromUrl(String url) {
		if (!url.startsWith("http") && !url.startsWith("https")) {
			url = "http://" + url;
		}
		URL netUrl;
		try {
			netUrl = new URL(url);
		} catch (MalformedURLException e) {
			return null;
		}
		return netUrl.getHost();
	}

	public static void setHttpClientProxyFromSystem(HttpRequestBase request, String url) {
		// TODO Change code to use determineProxySettingsFromSystem(String)
		String proxyHost = System.getProperty("http.proxyHost");
		if (StringUtils.isNotBlank(proxyHost)) {
			String proxyPort = System.getProperty("http.proxyPort");
			String nonProxyHosts = System.getProperty("http.nonProxyHosts");
			
			if (StringUtils.isBlank(nonProxyHosts)) {
				if (StringUtils.isNotBlank(proxyPort) && AgnUtils.isNumber(proxyPort)) {
					request.setConfig(RequestConfig.custom().setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort), "http")).build());
				} else {
					request.setConfig(RequestConfig.custom().setProxy(new HttpHost(proxyHost, 8080, "http")).build());
				}
			} else {
				boolean ignoreProxy = false;
				String urlDomain = getDomainFromUrl(url);
				if (urlDomain != null) {
					urlDomain = urlDomain.trim().toLowerCase();
					for (String nonProxyHost : nonProxyHosts.split("\\||,|;| ")) {
						nonProxyHost = nonProxyHost.trim().toLowerCase();
						if (urlDomain.equals(nonProxyHost) || urlDomain.endsWith("." + nonProxyHost)) {
							ignoreProxy = true;
							break;
						}
					}

					if (!ignoreProxy) {
						request.setConfig(RequestConfig.custom()
								.setProxy(new HttpHost(proxyHost, NumberUtils.toInt(proxyPort, 8080), "http")).build());
					}
				}
			}
		}
	}
	
	/**
	 * Determines the proxy settings from system properties.
	 * 
	 * @param url target URL
	 * 
	 * @return proxy settings or <code>null</code> if not proxy can be used
	 */
	public static final ProxySettings determineProxySettingsFromSystem(final String url) {
		final String proxyHost = System.getProperty("http.proxyHost");
		
		if (StringUtils.isNotBlank(proxyHost)) {
			return isProxiedHost(url)
					? new ProxySettings(proxyHost, determineProxyPort())
					: null;
		} else {
			return null;
		}
	}
	
	/**
	 * Determines to proxy port. 
	 * If nothing is configured, the default port ({@value #PROXY_DEFAULT_PORT}) is returned.
	 * 
	 * @return proxy port
	 */
	private static final int determineProxyPort() {
		final String proxyPort = System.getProperty("http.proxyPort");
		
		return StringUtils.isNotBlank(proxyPort) && AgnUtils.isNumber(proxyPort)
			? Integer.parseInt(proxyPort)
			: 8080;
	}
	
	/**
	 * Determines whether a proxy can be used or not.
	 * 
	 * @param url url to check
	 * 
	 * @return <code>true</code> if proxy can be used
	 */
	private static final boolean isProxiedHost(final String url) {
		final String urlDomain = getDomainFromUrl(url);
		
		if (urlDomain == null) { 
			return false;
		}
		
		final String nonProxyHosts = System.getProperty("http.nonProxyHosts");

		for (String nonProxyHost : nonProxyHosts.split("\\||,|;| ")) {
			nonProxyHost = nonProxyHost.trim().toLowerCase();
			if (urlDomain.equals(nonProxyHost) || urlDomain.endsWith("." + nonProxyHost)) {
				return false;
			}
		}
		
		return true;
	}

}
