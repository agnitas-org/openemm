/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
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
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.agnitas.emm.core.commons.filter.OriginUriFilter;
import com.agnitas.http.HttpResponse;
import com.agnitas.json.JsonUtilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class HttpUtils {
	public enum RequestMethod {
		GET,
		POST,
		PUT,
		DELETE
	}
	
	private static final Logger logger = LogManager.getLogger(HttpUtils.class);

	public static final String SECURE_HTTP_PROTOCOL_SIGN = "https://";
	public static final String HTTP_PROTOCOL_SIGN = "http://";

	public static final String IMAGE_PATH_NO_PREVIEW = "/assets/core/images/facelift/no_preview.svg";
	public static final String IMAGE_PATH_ERROR_LINK = "/assets/core/images/grid_expire_image.png";

	public static final String CONTENT_ENCODING = "UTF-8";
	public static final String APPLICATION_JSON_UTF8 = "application/json;charset=" + CONTENT_ENCODING;

	public static final String CONTENT_TYPE_CSV = "text/csv";
    private static final String ERROR_OCCURRED = "Error occurred: {}";

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

	public static boolean isAjax(HttpHeaders headers) {
		List<String> values = headers.get("X-Requested-With");

		if (CollectionUtils.isEmpty(values)) {
			return false;
		}

		return values.contains("XMLHttpRequest");
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
				logger.error(ERROR_OCCURRED, e.getMessage(), e);
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
	
	public static String executeHttpPostRequest(String httpUrl, Map<String, Object> httpPostParameter, final String secureTransportLayerProtocol) throws Exception {
		return executeHttpRequest(httpUrl, null, httpPostParameter, secureTransportLayerProtocol);
	}
	
	public static String executeHttpRequest(String httpUrlString, Map<String, Object> httpUrlParameter, Map<String, Object> httpPostParameter, final String secureTransportLayerProtocol) throws Exception {
		return executeHttpRequest(httpUrlString, httpUrlParameter, httpPostParameter, true, secureTransportLayerProtocol);
	}
	
	public static String executeHttpRequest(String httpUrlString, Map<String, Object> httpUrlParameter, Map<String, Object> httpPostParameter, boolean checkSslServerCert, final String secureTransportLayerProtocol) throws Exception {
		return executeHttpRequest(httpUrlString, null, httpUrlParameter, httpPostParameter, checkSslServerCert, secureTransportLayerProtocol);
	}
	
	public static String executeHttpRequest(String httpUrlString, Map<String, String> httpRequestHeaders, Map<String, Object> httpUrlParameter, Map<String, Object> httpPostParameter, boolean checkSslServerCert, final String secureTransportLayerProtocol) throws Exception {
		String httpRequestBody = null;
		if (httpPostParameter != null && httpPostParameter.size() > 0) {
			httpRequestBody = HttpUtils.convertToParameterString(httpPostParameter, null);
		}
		
		HttpResponse response = executeHttpRequest(null, httpUrlString, httpRequestHeaders, httpUrlParameter, httpRequestBody, checkSslServerCert, secureTransportLayerProtocol);
		
		if (response.getHttpCode() == HttpURLConnection.HTTP_OK) {
			return response.getContent();
		} else {
			throw new HttpException(httpUrlString, response.getHttpCode());
		}
	}
	
	public static HttpResponse executeHttpRequest(RequestMethod requestMethod, String httpUrlString, Map<String, String> httpRequestHeaders, Map<String, Object> httpUrlParameter, String httpRequestBody, boolean checkSslServerCert, final String secureTransportLayerProtocol) throws Exception {
		return executeHttpRequest(requestMethod, httpUrlString, httpRequestHeaders, httpUrlParameter, httpRequestBody, null, checkSslServerCert, secureTransportLayerProtocol);
	}
	
	public static HttpResponse executeHttpRequest(RequestMethod requestMethod, String urlString, Map<String, String> httpRequestHeaders, Map<String, Object> httpUrlParameter, String httpRequestBody, String requestEncoding, boolean checkSslServerCert, final String secureTransportLayerProtocol) throws Exception {
		return executeHttpRequest(requestMethod, urlString, httpRequestHeaders,httpUrlParameter, httpRequestBody, requestEncoding, checkSslServerCert, null, secureTransportLayerProtocol);
	}
	 
	public static HttpResponse executeHttpRequest(RequestMethod requestMethod, String urlString, Map<String, String> httpRequestHeaders, Map<String, Object> httpUrlParameter, String httpRequestBody, String requestEncoding, boolean checkSslServerCert, Proxy proxy, final String secureTransportLayerProtocol) throws Exception {
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
				urlConnection = (HttpURLConnection) new URL(urlString).openConnection(NetworkUtil.getProxyFromSystem(urlString));
			} else {
				urlConnection = (HttpURLConnection) new URL(urlString).openConnection(proxy);
			}
			
			if (requestMethod != null) {
				urlConnection.setRequestMethod(requestMethod.name());
			}
			
			if (urlString.startsWith(SECURE_HTTP_PROTOCOL_SIGN) && !checkSslServerCert) {
				SSLContext sslContext = SSLContext.getInstance(secureTransportLayerProtocol);
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
			logger.error(ERROR_OCCURRED, e.getMessage(), e);
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
            if (!"Illegal character in query".equals(e.getReason()) || !AgnTagUtils.containsAnyAgnTag(e.getInput())) {
                logger.error(ERROR_OCCURRED, e.getMessage(), e);
            }
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
			logger.error(ERROR_OCCURRED, e.getMessage(), e);
			return null;
		}
	}

	public static String originUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute(OriginUriFilter.ORIGIN_URI_KEY);
		String queryString = (String) request.getAttribute(OriginUriFilter.ORIGIN_QUERY_STRING_KEY);

		if (StringUtils.isNotEmpty(queryString)) {
			return uri + "?" + queryString;
		}

		return uri;
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
		setDownloadFilenameHeader(response, filename, "UTF-8");
	}
	public static void setDownloadFilenameHeader(HttpServletResponse response, String filename, String charset) {
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, getContentDispositionAttachment(filename, charset));
		response.setCharacterEncoding(charset);
	}

	public static String getContentDispositionAttachment(String filename) {
		return getContentDispositionAttachment(filename, "UTF-8");
	}

	public static String getContentDispositionAttachment(String filename, String charset) {
		String header = "attachment; ";

		if ("US-ASCII".equalsIgnoreCase(charset)) {
			header += "filename=\"" + escapeFileName(filename) + "\";";
		} else {
			String encodedFileName = "";
			try {
				encodedFileName = encodeFilename(filename.replaceAll("[\\s]", "_"), charset);
			} catch (Exception e) {
				logger.error("Cannot encode file name: {}", filename, e);
			}
			header += "filename*=" + encodedFileName;
		}

		return header;
	}

	/**
	 * Encode the given header field param as describe in RFC 5987.
	 * @param filename the header field param
	 * @return the encoded header field param
	 * @see <a href="https://tools.ietf.org/html/rfc5987">RFC 5987</a>
	 */
	private static String encodeFilename(String filename, String charset) throws UnsupportedEncodingException {
		if (StringUtils.isBlank(filename)) {
			return "";
		}
		if ("US-ASCII".equalsIgnoreCase(charset)) {
			logger.info("Does not require encoding for ASCII charset");
			return filename;
		}

		if (!"UTF-8".equalsIgnoreCase(charset) && !"ISO-8859-1".equalsIgnoreCase(charset)) {
			logger.error("Unsupported charset {}", charset);
			throw new UnsupportedEncodingException("Unsupported charset " + charset);
		}

		byte[] source = filename.getBytes(charset);
		int len = source.length;
		StringBuilder sb = new StringBuilder(len << 1);
		sb.append(charset);
		sb.append("''");
		for (byte b : source) {
			if (isRFC5987AttrChar(b)) {
				sb.append((char) b);
			}
			else {
				sb.append('%');
				char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
				char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
				sb.append(hex1);
				sb.append(hex2);
			}
		}
		return sb.toString();
	}

	private static boolean isRFC5987AttrChar(byte c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') ||
				c == '!' || c == '#' || c == '$' || c == '&' || c == '+' || c == '-' || c == '.' ||
				c == '^' || c == '_' || c == '`' || c == '|' || c == '~';
	}

	public static String escapeFileName(String filename) {
		if (StringUtils.isEmpty(filename)) {
			return "";
		} else {
			return filename.replaceAll("[?:;\\*/<>{}|\"'\\\\,\\s]", "_");
		}
	}

	public static String getAuthorizationToken(HttpServletRequest request) {
		String authorizationToken = request.getHeader("Authorization"); // like: "Bearer abc.abc123.signature"
		if (StringUtils.isBlank(authorizationToken) || !authorizationToken.startsWith("Bearer ")) {
			return null;
		} else {
            return authorizationToken.substring(7); // like: "abc.abc123.signature"
		}
	}
	
	public static String getBasicAuthenticationUsername(HttpServletRequest request) {
		try {
			String basicAuthorizationHeader = request.getHeader("Authorization"); // like: "Basic bXl1c2VybmFtZTpteXBhc3N3b3Jk"
			if (StringUtils.isBlank(basicAuthorizationHeader) || !basicAuthorizationHeader.startsWith("Basic ")) {
				return null;
			} else {
				String decodedAuthorization = new String(AgnUtils.decodeBase64(basicAuthorizationHeader.substring(6)), "UTF-8"); // like: "myusername:mypassword"
				if (decodedAuthorization.contains(":")) {
					return decodedAuthorization.substring(0, decodedAuthorization.indexOf(":"));
				} else {
					return decodedAuthorization;
				}
			}
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public static String getBasicAuthenticationPassword(HttpServletRequest request) {
		try {
			String basicAuthorizationHeader = request.getHeader("Authorization"); // like: "Basic bXl1c2VybmFtZTpteXBhc3N3b3Jk"
			if (StringUtils.isBlank(basicAuthorizationHeader) || !basicAuthorizationHeader.startsWith("Basic ")) {
				return null;
			} else {
				String decodedAuthorization = new String(AgnUtils.decodeBase64(basicAuthorizationHeader.substring(6)), "UTF-8"); // like: "myusername:mypassword"
				if (decodedAuthorization.contains(":")) {
					return decodedAuthorization.substring(decodedAuthorization.indexOf(":") + 1);
				} else {
					return null;
				}
			}
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
}
