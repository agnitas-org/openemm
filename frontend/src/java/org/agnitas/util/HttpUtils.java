/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.agnitas.emm.core.commons.filter.OriginUriFilter;
import com.agnitas.json.JsonUtilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static org.agnitas.util.NetworkUtil.setHttpClientProxyFromSystem;

public class HttpUtils {
	public enum RequestMethod {
		GET,
		POST,
		PUT,
		DELETE
	}
	
	private static final transient Logger logger = Logger.getLogger(HttpUtils.class);

	public static final String CONTENT_TYPE_JAVASCRIPT = "application/javascript";
	public static final String SECURE_HTTP_PROTOCOL_SIGN = "https://";
	public static final String HTTP_PROTOCOL_SIGN = "http://";

	public static final String IMAGE_CONTENT_TYPE = "image";
	public static final String IMAGE_PATH_NO_PREVIEW = "/assets/core/images/facelift/no_preview.png";
	
	public static final String IMAGE_PATH_ERROR_LINK = "/assets/core/images/grid_expire_image.png";

	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String CONTENT_ENCODING = "UTF-8";
	public static final String APPLICATION_JSON_UTF8 = CONTENT_TYPE_JSON + ";charset=" + CONTENT_ENCODING;

	public static final String CONTENT_TYPE_CSV = "text/csv";

	private static TrustManager TRUSTALLCERTS_TRUSTMANAGER = new X509TrustManager() {
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			// nothing to do
		}

		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			// nothing to do
		}
	};
	
	private static HostnameVerifier TRUSTALLHOSTNAMES_HOSTNAMEVERIFIER = (hostname, session) -> true;

	public static boolean isAjax(HttpServletRequest request) {
		return StringUtils.equals(request.getHeader("X-Requested-With"), "XMLHttpRequest");
	}

	public static boolean isAjax(HttpHeaders headers) {
		List<String> values = headers.get("X-Requested-With");

		if (CollectionUtils.isEmpty(values)) {
			return false;
		}

		return values.contains("XMLHttpRequest");
	}

	public static int getResponseStatusCode(String urlString) {
		try {
			HttpClient httpClient = new HttpClient();
			setHttpClientProxyFromSystem(httpClient, urlString);

			GetMethod method = new GetMethod(urlString);
			method.setFollowRedirects(true);

			return httpClient.executeMethod(method);
		} catch (IOException e) {
			logger.error("Could not instantiate connection", e);
		}
		return -1;
	}

	public static String convertToParameterString(Map<String, Object> parameterMap, String encoding) {
		if (parameterMap == null) {
			return null;
		} else {
			if (StringUtils.isBlank(encoding)) {
				encoding = "UTF-8";
			}
			StringBuilder returnValue = new StringBuilder();
			try {
				for (Entry<String, Object> entry : parameterMap.entrySet()) {
					if (returnValue.length() > 0) {
						returnValue.append("&");
					}
					returnValue.append(URLEncoder.encode(entry.getKey(), encoding));
					returnValue.append("=");
					if (entry.getValue() != null) {
						returnValue.append(URLEncoder.encode(entry.getValue().toString(), encoding));
					}
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("Error occured: " + e.getMessage(), e);
			}
			
			return returnValue.toString();
		}
	}
	
	public static Tuple<String, String> createHtmlFormMimetypeHeader(String encoding) {
		if (StringUtils.isBlank(encoding)) {
			return new Tuple<>("content-type", "application/x-www-form-urlencoded");
		} else {
			return new Tuple<>("content-type", "application/x-www-form-urlencoded; charset=" + encoding.toLowerCase());
		}
	}
	
	public static String executeHttpGetRequest(String httpUrl) throws Exception {
		return executeHttpRequest(httpUrl, null, null);
	}
	
	public static String executeHttpGetRequest(String httpUrl, Map<String, Object> httpUrlParameter) throws Exception {
		return executeHttpRequest(httpUrl, httpUrlParameter, null);
	}
	
	public static String executeHttpGetRequest(String httpUrl, Map<String, Object> httpUrlParameter, boolean checkSslServerCert) throws Exception {
		return executeHttpRequest(httpUrl, httpUrlParameter, null, checkSslServerCert);
	}
	
	public static String executeHttpPostRequest(String httpUrl, Map<String, Object> httpPostParameter) throws Exception {
		return executeHttpRequest(httpUrl, null, httpPostParameter);
	}
	
	public static String executeHttpPostRequest(String httpUrl, Map<String, Object> httpPostParameter, boolean checkSslServerCert) throws Exception {
		return executeHttpRequest(httpUrl, null, httpPostParameter, checkSslServerCert);
	}
	
	public static String executeHttpRequest(String httpUrlString, Map<String, Object> httpUrlParameter, Map<String, Object> httpPostParameter) throws Exception {
		return executeHttpRequest(httpUrlString, httpUrlParameter, httpPostParameter, true);
	}
	
	public static String executeHttpRequest(String httpUrlString, Map<String, Object> httpUrlParameter, Map<String, Object> httpPostParameter, boolean checkSslServerCert) throws Exception {
		return executeHttpRequest(httpUrlString, null, httpUrlParameter, httpPostParameter, checkSslServerCert);
	}
	
	public static String executeHttpRequest(String httpUrlString, Map<String, String> httpRequestHeaders, Map<String, Object> httpUrlParameter, Map<String, Object> httpPostParameter, boolean checkSslServerCert) throws Exception {
		String httpRequestBody = null;
		if (httpPostParameter != null && httpPostParameter.size() > 0) {
			httpRequestBody = HttpUtils.convertToParameterString(httpPostParameter, null);
		}
		
		HttpResponse response = executeHttpRequest(null, httpUrlString, httpRequestHeaders, httpUrlParameter, httpRequestBody, checkSslServerCert);
		
		if (response.getHttpCode() == HttpURLConnection.HTTP_OK) {
			return response.getContent();
		} else {
			throw new HttpException(httpUrlString, response.getHttpCode());
		}
	}
	
	public static HttpResponse executeHttpRequest(RequestMethod requestMethod, String httpUrlString, Map<String, String> httpRequestHeaders, Map<String, Object> httpUrlParameter, String httpRequestBody, boolean checkSslServerCert) throws Exception {
		return executeHttpRequest(requestMethod, httpUrlString, httpRequestHeaders, httpUrlParameter, httpRequestBody, null, checkSslServerCert);
	}
	
	public static HttpResponse executeHttpRequest(RequestMethod requestMethod, String urlString, Map<String, String> httpRequestHeaders, Map<String, Object> httpUrlParameter, String httpRequestBody, String requestEncoding, boolean checkSslServerCert) throws Exception {
		return executeHttpRequest(requestMethod, urlString, httpRequestHeaders,httpUrlParameter, httpRequestBody, requestEncoding, checkSslServerCert, null);
	}
	 
	public static HttpResponse executeHttpRequest(RequestMethod requestMethod, String urlString, Map<String, String> httpRequestHeaders, Map<String, Object> httpUrlParameter, String httpRequestBody, String requestEncoding, boolean checkSslServerCert, Proxy proxy) throws Exception {
		if (StringUtils.isBlank(urlString)) {
			throw new RuntimeException("Invalid empty URL for http request");
		}
		
		// Check for protocol "https://" or "http://" (fallback: "http://")
		if (!urlString.startsWith(SECURE_HTTP_PROTOCOL_SIGN) && !urlString.startsWith(HTTP_PROTOCOL_SIGN)) {
			urlString = HTTP_PROTOCOL_SIGN + urlString;
		}
			
		try {
			if (httpUrlParameter != null && httpUrlParameter.size() > 0) {
				// Prepare Get parameter data
				String getParameterString = convertToParameterString(httpUrlParameter, requestEncoding);
				urlString += "?" + getParameterString;
			}

			HttpURLConnection urlConnection;
			if (proxy == null) {
				// Use systems default proxy, if set on JVM start. To override default proxy usage use "Proxy.NO_PROXY"
				urlConnection = (HttpURLConnection) new URL(urlString).openConnection(getProxyFromSystem(urlString));
			} else {
				urlConnection = (HttpURLConnection) new URL(urlString).openConnection(proxy);
			}
			
			if (requestMethod != null) {
				urlConnection.setRequestMethod(requestMethod.name());
			}
			
			if (urlString.startsWith(SECURE_HTTP_PROTOCOL_SIGN) && !checkSslServerCert) {
				SSLContext sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, new TrustManager[] { TRUSTALLCERTS_TRUSTMANAGER }, new java.security.SecureRandom());
				SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
				((HttpsURLConnection) urlConnection).setSSLSocketFactory(sslSocketFactory);
				((HttpsURLConnection) urlConnection).setHostnameVerifier(TRUSTALLHOSTNAMES_HOSTNAMEVERIFIER);
			}
			
			if (httpRequestHeaders != null) {
				for (Entry<String, String> headerEntry : httpRequestHeaders.entrySet()) {
					urlConnection.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
				}
			}
			
			if (httpRequestBody != null) {
				// Send post parameter data
				urlConnection.setDoOutput(true);
				if (StringUtils.isBlank(requestEncoding)) {
					requestEncoding = "UTF-8";
				}
			    try (OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream(), requestEncoding)) {
				    out.write(httpRequestBody);
				    out.flush();
			    }
			}
			
			urlConnection.connect();
			
			int httpResponseCode = urlConnection.getResponseCode();
			if (httpResponseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
				Map<String, String> headers = new CaseInsensitiveMap<>();
				for (String headerName : urlConnection.getHeaderFields().keySet()) {
					headers.put(headerName, urlConnection.getHeaderField(headerName));
				}
				
				String encoding = "UTF-8";
				if (headers.containsKey("content-type")) {
					String contentType = headers.get("content-type");
					if (contentType != null && contentType.toLowerCase().contains("charset="))  {
						contentType = contentType.toLowerCase();
						encoding = contentType.substring(contentType.indexOf("charset=") + 8).trim();
					}
				}
				
				try (BufferedReader httpResponseContentReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), encoding))) {
					StringBuilder httpResponseContent = new StringBuilder();
					String httpResponseContentLine;
					while ((httpResponseContentLine = httpResponseContentReader.readLine()) != null) {
						httpResponseContent.append(httpResponseContentLine);
						httpResponseContent.append("\n");
					}
					return new HttpResponse(httpResponseCode, httpResponseContent.toString(), urlConnection.getContentType(), headers);
				} catch (Exception e) {
					return new HttpResponse(httpResponseCode, null, null, null);
				}
			} else {
				Map<String, String> headers = new CaseInsensitiveMap<>();
				for (String headerName : urlConnection.getHeaderFields().keySet()) {
					headers.put(headerName, urlConnection.getHeaderField(headerName));
				}
				
				String encoding = "UTF-8";
				if (headers.containsKey("content-type")) {
					String contentType = headers.get("content-type");
					if (contentType != null && contentType.toLowerCase().contains("charset="))  {
						contentType = contentType.toLowerCase();
						encoding = contentType.substring(contentType.indexOf("charset=") + 8).trim();
					}
				}
				
				try (BufferedReader httpResponseContentReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), encoding))) {
					StringBuilder httpResponseContent = new StringBuilder();
					String httpResponseContentLine;
					while ((httpResponseContentLine = httpResponseContentReader.readLine()) != null) {
						httpResponseContent.append(httpResponseContentLine);
						httpResponseContent.append("\n");
					}
					return new HttpResponse(httpResponseCode, httpResponseContent.toString(), urlConnection.getContentType(), headers);
				} catch (Exception e) {
					return new HttpResponse(httpResponseCode, null, null, null);
				}
			}
		} catch (Exception e) {
			logger.error("Error occured: " + e.getMessage(), e);
			throw e;
		}
	}

	public static String resolveRelativeUri(URL base, final String value0) {
		final String value = value0.replaceAll("^\\s+", "");

		try {
			final URI uri = new URI(value);
			final UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
			UriComponents components = null;

			if (uri.getScheme() == null) {
				builder.scheme(base.getProtocol());
			}

			if (uri.getHost() == null && uri.getPath() != null) {
				builder.host(base.getHost()).port(base.getPort());

				String path;

				if (value.startsWith(base.getHost() + "/")) {
					// Special case when URI starts with a hostname but has no schema
					//  so that hostname is treated as a leading part of a path.
					// It is possible to resolve that ambiguity when a base URL has the
					//  same hostname.
					path = uri.getPath().substring(base.getHost().length() - 1);
				} else {
					if (value.startsWith("/")) {
						// Base path is ignored when a URI starts with a slash
						path = uri.getPath();
					} else {
						if (base.getPath() != null) {
							path = base.getPath() + "/" + uri.getPath();
						} else {
							path = uri.getPath();
						}
					}
				}

				Deque<String> segments = new ArrayDeque<>();
				for (String segment : path.split("/")) {
					switch (segment) {
						case "":
						case ".":
							// Remove duplicating slashes and redundant "." operator
							break;

						case "..":
							// Remove previous segment if possible or append another ".." operator
							String last = segments.peekLast();
							if (last != null && !last.equals("..")) {
								segments.removeLast();
							} else {
								segments.addLast(segment);
							}
							break;

						default:
							segments.addLast(segment);
							break;
					}
				}
				components = builder.replacePath("/" + StringUtils.join(segments, "/")).build();
			}

			if (components != null) {
				return components.toString();
			}
		} catch (URISyntaxException e) {
			logger.error("Error occurred: " + e.getMessage(), e);
		}
		return value;
	}

	public static void redirection(HttpServletRequest request, HttpServletResponse response, String path, Map<String, Object> queryParams) throws IOException {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(request.getContextPath() + path);
		for (Map.Entry<String, Object> queryParam : queryParams.entrySet()) {
			uriBuilder.queryParam(queryParam.getKey(), queryParam.getValue());
		}
		response.sendRedirect(response.encodeRedirectURL(uriBuilder.build().encode().toString()));
	}

	public static void sendImage(byte[] imageData, HttpServletResponse response) throws IOException {
		try (OutputStream stream = response.getOutputStream()) {
			response.setContentType(IMAGE_CONTENT_TYPE);
			stream.write(imageData);
			stream.flush();
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
		}
	}

	public static void sendPreviewImage(byte[] imageData, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (imageData == null) {
			redirection(request, response, IMAGE_PATH_NO_PREVIEW, Collections.emptyMap());
		} else {
			sendImage(imageData, response);
		}
	}

	public static String escapeFileName(String filename) {
		if (StringUtils.isEmpty(filename)) {
			return "";
		} else {
			return filename.replaceAll("[?:\\*/<>|\"\\\\]", "_");
		}
	}

	/**
	 * Checks FTP/SFTP connection
	 * @param path starts with ftp:// or sftp://, can be null
	 * @param privateKey only for sftp:// if need
	 * @param allowUnknownHostKeys only for sftp:// if need
	 * @return true if path is correct and connection is successful
	 */
	public static boolean checkFTPConnection(String path, String privateKey, boolean allowUnknownHostKeys){
		if(StringUtils.isNotBlank(path)) {
			if (path.toLowerCase().startsWith("ftp://")) {
				try (FtpHelper ftpHelper = new FtpHelper(path)) {
					ftpHelper.connect();
					return true;
				} catch (Exception e) {
					logger.error("Error while connecting to ftp", e);
				}
			} else {
				// default: send via SFTP
				try (SFtpHelper sftpHelper = new SFtpHelper(path)) {
					if (StringUtils.isNotBlank(privateKey)) {
						sftpHelper.setPrivateSshKeyData(privateKey);
					}
					if (allowUnknownHostKeys) {
						sftpHelper.setAllowUnknownHostKeys(true);
					}
					sftpHelper.connect();
					return true;
				} catch (Exception e) {
					logger.error("Error while connecting to sftp", e);
				}
			}
		}
		return false;
	}

	public static String encodeUriComponent(String component) {
		try {
			return URLEncoder.encode(component, "US-ASCII");
		} catch (final UnsupportedEncodingException e) {
			logger.error("Unsupported character encoding US-ASCII", e);
			return null;
		}
	}

	public static String toJson(Object data) {
		ObjectMapper mapper = JsonUtilities.getObjectMapper(DateUtilities.UTC);
		try {
			return mapper.writeValueAsString(data);
		} catch (IOException e) {
			logger.error("Error occurred: " + e.getMessage(), e);
			return null;
		}
	}

	public static boolean responseJson(HttpServletResponse response, String content) {
		return responseJson(response, w -> w.write(content));
	}

	public static boolean responseJson(HttpServletResponse response, JSON content) {
		return responseJson(response, content::write);
	}

	public static <T> boolean responseJson(HttpServletResponse response, ObjectMapper objectMapper, T object) {
		return responseJson(response, w -> w.write(objectMapper.writeValueAsString(object)));
	}

	public static boolean responseJson(HttpServletResponse response, ResponseWriter responseWriter) {
		response.setContentType(CONTENT_TYPE_JSON);
		response.setCharacterEncoding(CONTENT_ENCODING);

		try (PrintWriter writer = response.getWriter()) {
			responseWriter.write(writer);
			writer.close();
			return true;
		} catch (IOException e) {
			logger.error("Error occurred: " + e.getMessage());
		}

		return false;
	}

	public static String originUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute(OriginUriFilter.ORIGIN_URI_KEY);
		String queryString = (String) request.getAttribute(OriginUriFilter.ORIGIN_QUERY_STRING_KEY);

		if (StringUtils.isNotEmpty(queryString)) {
			return uri + "?" + queryString;
		}

		return uri;
	}

	public interface ResponseWriter {
		void write(Writer writer) throws IOException;
	}
	
	/**
	 * This proxy will be used as default proxy.
	 * To override default proxy usage use "Proxy.NO_PROXY"
	 * 
	 * It is set via JVM properties on startup:
	 * java ... -Dhttp.proxyHost=proxy.url.local -Dhttp.proxyPort=8080 -Dhttp.nonProxyHosts='127.0.0.1|localhost'
	 */
	public static Proxy getProxyFromSystem(String url) {
		String proxyHost = System.getProperty("http.proxyHost");
		if (StringUtils.isNotBlank(proxyHost)) {
			String proxyPort = System.getProperty("http.proxyPort");
			String nonProxyHosts = System.getProperty("http.nonProxyHosts");
			
			if (StringUtils.isBlank(nonProxyHosts)) {
				if (StringUtils.isNotBlank(proxyHost)) {
					if (StringUtils.isNotBlank(proxyPort) && AgnUtils.isNumber(proxyPort)) {
						return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
					} else {
						return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, 8080));
					}
				}
			} else {
				boolean ignoreProxy = false;
				String urlDomain = getDomainFromUrl(url);
				for (String nonProxyHost : nonProxyHosts.split("\\|")) {
					nonProxyHost = nonProxyHost.trim();
					if (urlDomain == null || urlDomain.equalsIgnoreCase(url)) {
						ignoreProxy = true;
						break;
					}
				}
				if (!ignoreProxy) {
					if (StringUtils.isNotBlank(proxyHost)) {
						if (StringUtils.isNotBlank(proxyPort) && AgnUtils.isNumber(proxyPort)) {
							return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
						} else {
							return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, 8080));
						}
					}
				}
			}
		}
		
		return Proxy.NO_PROXY;
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
	
	public static String getReferrer(HttpServletRequest request) {
		if (request == null) {
			return null;
		} else {
			for (String headerName : Collections.list(request.getHeaderNames())) {
				if ("referer".equalsIgnoreCase(headerName)) {
					return request.getHeader(headerName);
				}
			}
			return null;
		}
	}

	public static void setDownloadFilenameHeader(HttpServletResponse response, String filename) {
		String encodedFilename = escapeFileName(filename);
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\";");
	}

	public static String getContentDispositionHeaderContent(String filename) {
		String encodedFilename = escapeFileName(filename);
		return "attachment; filename=\"" + encodedFilename + "\";";
	}
	
	/**
	 * Method that initialize http client
	 *
	 * note: this method is used to export birt statistics files
	 * @param internalURL
	 * @return
	 * @throws MalformedURLException
	 */
	public static HttpClient initializeHttpClient(String internalURL) throws MalformedURLException {
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        URL url = new URL(internalURL);
        int port = (url.getPort() == -1) ? url.getDefaultPort() : url.getPort();
        
        httpClient.getHostConfiguration()
				.setHost(url.getHost(), port, url.getProtocol());

        return httpClient;
    }
}
