/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.http;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.agnitas.util.NetworkUtil;
import com.agnitas.util.Triple;
import com.agnitas.util.Tuple;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class HttpUtilities {
	private static TrustManager TRUSTALLCERTS_TRUSTMANAGER = new X509TrustManager() {
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
			// nothing to do
		}

		@Override
		public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
			// nothing to do
		}
	};

	private static HostnameVerifier TRUSTALLHOSTNAMES_HOSTNAMEVERIFIER = (hostname, session) -> true;

	/**
	 * Use systems default proxy, if set on JVM start.
	 * To override default proxy usage use "executeHttpRequest(httpRequest, Proxy.NO_PROXY)"
	 *
	 * @param httpRequest
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse executeHttpRequest(final HttpRequest httpRequest, final String secureTransportLayerProtocol) throws Exception {
		return executeHttpRequest(httpRequest, null, secureTransportLayerProtocol);
	}

	public static HttpResponse executeHttpRequest(final HttpRequest httpRequest, final Proxy proxy, final String secureTransportLayerProtocol) throws Exception {
		try {
			String requestedUrl = httpRequest.getUrlWithProtocol();

			// Check for already in URL included GET parameters
			String parametersFromUrl;
			if (requestedUrl.contains("?")) {
				if (requestedUrl.contains("#")) {
					parametersFromUrl = requestedUrl.substring(requestedUrl.indexOf("?") + 1, requestedUrl.indexOf("#"));
					requestedUrl = requestedUrl.substring(0, requestedUrl.indexOf("?"));
				} else {
					parametersFromUrl = requestedUrl.substring(requestedUrl.indexOf("?") + 1);
					requestedUrl = requestedUrl.substring(0, requestedUrl.indexOf("?"));
				}
			} else {
				parametersFromUrl = "";
			}

			// Prepare GET parameters data
			if (httpRequest.getUrlParameters() != null && httpRequest.getUrlParameters().size() > 0) {
				final String getParameterString = convertToParameterString(httpRequest.getUrlParameters(), httpRequest.getEncoding());
				if (parametersFromUrl.length() > 0) {
					requestedUrl += "?" + parametersFromUrl + "&" + getParameterString;
				} else {
					requestedUrl += "?" + getParameterString;
				}
			} else if (parametersFromUrl.length() > 0) {
				requestedUrl += "?" + parametersFromUrl;
			}

			HttpURLConnection urlConnection;
			if (proxy == null) {
				urlConnection = (HttpURLConnection) new URL(requestedUrl).openConnection(NetworkUtil.getProxyFromSystem(requestedUrl));
			} else {
				urlConnection = (HttpURLConnection) new URL(requestedUrl).openConnection(proxy);
			}

			if (httpRequest.getRequestMethod() != null) {
				urlConnection.setRequestMethod(httpRequest.getRequestMethod().name());
			}

			if (requestedUrl.startsWith(HttpRequest.SECURE_HTTP_PROTOCOL_SIGN) && !httpRequest.isCheckSslCertificates()) {
				final SSLContext sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, new TrustManager[] { TRUSTALLCERTS_TRUSTMANAGER }, new java.security.SecureRandom());
				final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
				((HttpsURLConnection) urlConnection).setSSLSocketFactory(sslSocketFactory);
				((HttpsURLConnection) urlConnection).setHostnameVerifier(TRUSTALLHOSTNAMES_HOSTNAMEVERIFIER);
			}

			if (httpRequest.getHeaders() != null && httpRequest.getHeaders().size() > 0) {
				for (final Entry<String, String> headerEntry : httpRequest.getHeaders().entrySet()) {
					urlConnection.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
				}
			}

			if (httpRequest.getCookieData() != null && httpRequest.getCookieData().size() > 0) {
				final StringBuilder cookieValue = new StringBuilder();
				for (final Entry<String, String> cookieEntry : httpRequest.getCookieData().entrySet()) {
					if (cookieValue.length() > 0) {
						cookieValue.append("; ");
					}
					cookieValue.append(encodeForCookie(cookieEntry.getKey()) + "=" + encodeForCookie(cookieEntry.getValue()));
				}

				urlConnection.setRequestProperty(HttpRequest.HEADER_NAME_UPLOAD_COOKIE, cookieValue.toString());
			}

			final String boundary = UUID.randomUUID().toString().replace("-", "");

			String httpRequestBody = null;
			if (httpRequest.getRequestBodyContentStream() != null) {
				urlConnection.setDoOutput(true);
				final OutputStream outputStream = urlConnection.getOutputStream();
				IOUtils.copy(httpRequest.getRequestBodyContentStream(), outputStream);
				outputStream.flush();
			} else if (httpRequest.getRequestBody() != null) {
				urlConnection.setDoOutput(true);
				final OutputStream outputStream = urlConnection.getOutputStream();
				httpRequestBody = httpRequest.getRequestBody();
				outputStream.write(httpRequestBody.getBytes(StandardCharsets.UTF_8));
				outputStream.flush();
			} else if (httpRequest.getUploadFileAttachments() != null && httpRequest.getUploadFileAttachments().size() > 0) {
				urlConnection.setDoOutput(true);
				urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
				final OutputStream outputStream = urlConnection.getOutputStream();

				if (httpRequest.getPostParameters() != null && httpRequest.getPostParameters().size() > 0) {
					for (final Tuple<String, Object> postParameter : httpRequest.getPostParameters()) {
						outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
						outputStream.write(("Content-Disposition: form-data; name=\"" + URLEncoder.encode(postParameter.getFirst(), "UTF-8") + "\"\r\n").getBytes(StandardCharsets.UTF_8));
						outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
						if (postParameter.getSecond() != null) {
							outputStream.write(postParameter.getSecond().toString().getBytes(StandardCharsets.UTF_8));
						}
						outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
					}
				}

				for (final Triple<String, String, byte[]> uploadFileAttachment : httpRequest.getUploadFileAttachments()) {
					outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
					outputStream.write(("Content-Disposition: form-data; name=\"" + uploadFileAttachment.getFirst() + "\"; filename=\"" + uploadFileAttachment.getSecond() + "\"\r\n").getBytes(StandardCharsets.UTF_8));
					outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

					outputStream.write(uploadFileAttachment.getThird());

					outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
				}

				outputStream.write(("--" + boundary + "--" + "\r\n").getBytes(StandardCharsets.UTF_8));
				outputStream.flush();
			} else if (httpRequest.getPostParameters() != null && httpRequest.getPostParameters().size() > 0) {
				urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				httpRequestBody = convertToParameterString(httpRequest.getPostParameters(), null);

				// Send post parameter data
				urlConnection.setDoOutput(true);
				try (OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream(), StringUtils.isBlank(httpRequest.getEncoding()) ? "UTF-8" : httpRequest.getEncoding())) {
					out.write(httpRequestBody);
					out.flush();
				}
			}

			urlConnection.connect();

			final Map<String, String> headers = new LinkedHashMap<>();
			for (final String headerName : urlConnection.getHeaderFields().keySet()) {
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

			Map<String, String> cookiesMap = null;
			if (headers.containsKey(HttpRequest.HEADER_NAME_DOWNLOAD_COOKIE)) {
				final String cookiesData = headers.get(HttpRequest.HEADER_NAME_DOWNLOAD_COOKIE);
				if (cookiesData != null)  {
					cookiesMap = new LinkedHashMap<>();
					for (final String cookie : cookiesData.split(";")) {
						final String[] cookieParts = cookie.split("=");
						if (cookieParts.length == 2) {
							cookiesMap.put(URLDecoder.decode(cookieParts[0].trim(), "UTF-8"), URLDecoder.decode(cookieParts[1].trim(), "UTF-8"));
						}
					}
				}
			}

			final int httpResponseCode = urlConnection.getResponseCode();
			if (httpResponseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
				if (httpRequest.getDownloadStream() != null && 200 <= httpResponseCode && httpResponseCode <= 299) {
					IOUtils.copy(urlConnection.getInputStream(), httpRequest.getDownloadStream());
					return new HttpResponse(httpResponseCode, urlConnection.getResponseMessage(), "File downloaded", urlConnection.getContentType(), headers, cookiesMap);
				} else if (httpRequest.getDownloadFile() != null && 200 <= httpResponseCode && httpResponseCode <= 299) {
					try (FileOutputStream downloadFileOutputStream = new FileOutputStream(httpRequest.getDownloadFile())) {
						IOUtils.copy(urlConnection.getInputStream(), downloadFileOutputStream);
						return new HttpResponse(httpResponseCode, urlConnection.getResponseMessage(), "File downloaded", urlConnection.getContentType(), headers, cookiesMap);
					} catch (final Exception e) {
						if (httpRequest.getDownloadFile().exists()) {
							httpRequest.getDownloadFile().delete();
						}
						throw e;
					}
				} else {
					try (BufferedReader httpResponseContentReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), encoding))) {
						final StringBuilder httpResponseContent = new StringBuilder();
						String httpResponseContentLine;
						while ((httpResponseContentLine = httpResponseContentReader.readLine()) != null) {
							if (httpResponseContent.length() > 0) {
								httpResponseContent.append("\n");
							}
							httpResponseContent.append(httpResponseContentLine);
						}
						return new HttpResponse(httpResponseCode, urlConnection.getResponseMessage(), httpResponseContent.toString(), urlConnection.getContentType(), headers, cookiesMap);
					} catch (final Exception e) {
						return new HttpResponse(httpResponseCode, urlConnection.getResponseMessage(), null, null, headers, cookiesMap);
					}
				}
			} else if ((httpResponseCode == HttpURLConnection.HTTP_MOVED_TEMP || httpResponseCode == HttpURLConnection.HTTP_MOVED_PERM) && httpRequest.isFollowRedirects()) {
				// Optionally follow redirections (HttpCodes 301 and 302)
				final String redirectUrl = urlConnection.getHeaderField("Location");
				if (StringUtils.isNotBlank(redirectUrl)) {
					final HttpRequest redirectedHttpRequest = new HttpRequest(httpRequest.getRequestMethod(), redirectUrl);
					return executeHttpRequest(redirectedHttpRequest, secureTransportLayerProtocol);
				} else {
					throw new Exception("Redirection url was empty");
				}
			} else {
				try (BufferedReader httpResponseContentReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), encoding))) {
					final StringBuilder httpResponseContent = new StringBuilder();
					String httpResponseContentLine;
					while ((httpResponseContentLine = httpResponseContentReader.readLine()) != null) {
						if (httpResponseContent.length() > 0) {
							httpResponseContent.append("\n");
						}
						httpResponseContent.append(httpResponseContentLine);
					}
					return new HttpResponse(httpResponseCode, urlConnection.getResponseMessage(), httpResponseContent.toString(), urlConnection.getContentType(), headers, cookiesMap);
				} catch (final Exception e) {
					return new HttpResponse(httpResponseCode, urlConnection.getResponseMessage(), null, null, headers, cookiesMap);
				}
			}
		} catch (final Exception e) {
			throw e;
		}
	}

	public static String convertToParameterString(final List<Tuple<String, Object>> parameters, String encoding) throws UnsupportedEncodingException {
		if (parameters == null) {
			return null;
		} else {
			if (StringUtils.isBlank(encoding)) {
				encoding = "UTF-8";
			}
			final StringBuilder returnValue = new StringBuilder();
			try {
				for (final Tuple<String, Object> entry : parameters) {
					if (returnValue.length() > 0) {
						returnValue.append("&");
					}
					returnValue.append(URLEncoder.encode(entry.getFirst(), encoding));
					returnValue.append("=");
					if (entry.getSecond() != null) {
						returnValue.append(URLEncoder.encode(entry.getSecond().toString(), encoding));
					}
				}
			} catch (final UnsupportedEncodingException e) {
				throw e;
			}

			return returnValue.toString();
		}
	}

	public static String getDomainFromUrl(String url) {
		if (!url.startsWith("http") && !url.startsWith("https")) {
			url = "http://" + url;
		}
		URL netUrl;
		try {
			netUrl = new URL(url);
		} catch (final MalformedURLException e) {
			return null;
		}
		return netUrl.getHost();
	}

	public static Tuple<String, String> createHtmlFormMimetypeHeader(final String encoding) {
		if (StringUtils.isBlank(encoding)) {
			return new Tuple<>("content-type", "application/x-www-form-urlencoded");
		} else {
			return new Tuple<>("content-type", "application/x-www-form-urlencoded; charset=" + encoding.toLowerCase());
		}
	}

	public static String addUrlParameter(final String url, final String parameterName, final Object parameterValue, final String encodingCharSet) throws UnsupportedEncodingException {
		final StringBuilder escapedParameterNameAndValue = new StringBuilder();

		if (StringUtils.isEmpty(encodingCharSet)) {
			escapedParameterNameAndValue.append(parameterName);
		} else {
			escapedParameterNameAndValue.append(URLEncoder.encode(parameterName, encodingCharSet));
		}

		escapedParameterNameAndValue.append('=');

		if (parameterValue instanceof char[]) {
			if (StringUtils.isEmpty(encodingCharSet)) {
				escapedParameterNameAndValue.append(new String((char[]) parameterValue));
			} else {
				escapedParameterNameAndValue.append(URLEncoder.encode(new String((char[]) parameterValue), encodingCharSet));
			}
		} else if (parameterValue instanceof Object[]) {
			boolean isFirstValue = true;
			for (final Object value : (Object[]) parameterValue) {
				if (!isFirstValue) {
					escapedParameterNameAndValue.append(",");
				}
				if (StringUtils.isEmpty(encodingCharSet)) {
					escapedParameterNameAndValue.append(String.valueOf(value));
				} else {
					escapedParameterNameAndValue.append(URLEncoder.encode(String.valueOf(value), encodingCharSet));
				}
				isFirstValue = false;
			}
		} else {
			if (StringUtils.isEmpty(encodingCharSet)) {
				escapedParameterNameAndValue.append(String.valueOf(parameterValue));
			} else {
				escapedParameterNameAndValue.append(URLEncoder.encode(String.valueOf(parameterValue), encodingCharSet));
			}
		}
		return addUrlParameter(url, escapedParameterNameAndValue.toString());
	}

	public static String addUrlParameter(final String url, final String escapedParameterNameAndValue) {
		final StringBuilder newUrl = new StringBuilder();
		final int insertPosition = url.indexOf('#');

		if (insertPosition < 0) {
			newUrl.append(url);
			newUrl.append(url.indexOf('?') <= -1 ? '?' : '&');
			newUrl.append(escapedParameterNameAndValue);
		} else {
			newUrl.append(url.substring(0, insertPosition));
			newUrl.append(url.indexOf('?') <= -1 ? '?' : '&');
			newUrl.append(escapedParameterNameAndValue);
			newUrl.append(url.substring(insertPosition));
		}

		return newUrl.toString();
	}

	public static String addPathParameter(final String url, final String escapedParameterNameAndValue) {
		final StringBuilder newUrl = new StringBuilder();
		int insertPosition = url.indexOf('?');
		if (insertPosition < 0) {
			insertPosition = url.indexOf('#');
		}

		if (insertPosition < 0) {
			newUrl.append(url);
			newUrl.append(";");
			newUrl.append(escapedParameterNameAndValue);
		} else {
			newUrl.append(url.substring(0, insertPosition));
			newUrl.append(";");
			newUrl.append(escapedParameterNameAndValue);
			newUrl.append(url.substring(insertPosition));
		}

		return newUrl.toString();
	}

	public static String getPlainParameterFromHtml(String htmlText, String parameterName) {
		if (StringUtils.isBlank(htmlText)) {
			return null;
		} else {
			final Pattern parameterPattern = Pattern.compile("\\W" + parameterName + "\\s*=(\\w*)\\W", Pattern.MULTILINE);
			final Matcher parameterMatcher = parameterPattern.matcher(htmlText);
			if (parameterMatcher.find()) {
				return parameterMatcher.group(1).trim();
			} else {
				return null;
			}
		}
	}

	public static String getQuotedParameterFromHtml(String htmlText, String parameterName) {
		if (StringUtils.isBlank(htmlText)) {
			return null;
		} else {
			final Pattern parameterPattern = Pattern.compile("\\W" + parameterName + "\\s*=\\s\"(\\w*)\"\\W", Pattern.MULTILINE);
			final Matcher parameterMatcher = parameterPattern.matcher(htmlText);
			if (parameterMatcher.find()) {
				return parameterMatcher.group(1).trim();
			} else {
				return null;
			}
		}
	}

	public static String getHttpStatusText(final int httpStatusCode) {
		switch (httpStatusCode) {
			case HttpURLConnection.HTTP_OK:
				// 200
				return "OK";
			case HttpURLConnection.HTTP_CREATED:
				// 201
				return "Created";
			case HttpURLConnection.HTTP_ACCEPTED:
				// 202
				return "Accepted";
			case HttpURLConnection.HTTP_NOT_AUTHORITATIVE:
				// 203
				return "Non-Authoritative Information";
			case HttpURLConnection.HTTP_NO_CONTENT:
				// 204
				return "No Content";
			case HttpURLConnection.HTTP_RESET:
				// 205
				return "Reset Content";
			case HttpURLConnection.HTTP_PARTIAL:
				// 206
				return "Partial Content";
			case HttpURLConnection.HTTP_MULT_CHOICE:
				// 300
				return "Multiple Choices";
			case HttpURLConnection.HTTP_MOVED_PERM:
				// 301
				return "Moved Permanently";
			case HttpURLConnection.HTTP_MOVED_TEMP:
				// 302
				return "Temporary Redirect";
			case HttpURLConnection.HTTP_SEE_OTHER:
				// 303
				return "See Other";
			case HttpURLConnection.HTTP_NOT_MODIFIED:
				// 304
				return "Not Modified";
			case HttpURLConnection.HTTP_USE_PROXY:
				// 305
				return "Use Proxy";
			case HttpURLConnection.HTTP_BAD_REQUEST:
				// 400
				return "Bad Request";
			case HttpURLConnection.HTTP_UNAUTHORIZED:
				// 401
				return "Unauthorized";
			case HttpURLConnection.HTTP_PAYMENT_REQUIRED:
				// 402
				return "Payment Required";
			case HttpURLConnection.HTTP_FORBIDDEN:
				// 403
				return "Forbidden";
			case HttpURLConnection.HTTP_NOT_FOUND:
				// 404
				return "Not Found";
			case HttpURLConnection.HTTP_BAD_METHOD:
				// 405
				return "Method Not Allowed";
			case HttpURLConnection.HTTP_NOT_ACCEPTABLE:
				// 406
				return "Not Acceptable";
			case HttpURLConnection.HTTP_PROXY_AUTH:
				// 407
				return "Proxy Authentication Required";
			case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
				// 408
				return "Request Time-Out";
			case HttpURLConnection.HTTP_CONFLICT:
				// 409
				return "Conflict";
			case HttpURLConnection.HTTP_GONE:
				// 410
				return "Gone";
			case HttpURLConnection.HTTP_LENGTH_REQUIRED:
				// 411
				return "Length Required";
			case HttpURLConnection.HTTP_PRECON_FAILED:
				// 412
				return "Precondition Failed";
			case HttpURLConnection.HTTP_ENTITY_TOO_LARGE:
				// 413
				return "Request Entity Too Large";
			case HttpURLConnection.HTTP_REQ_TOO_LONG:
				// 414
				return "Request-URI Too Large";
			case HttpURLConnection.HTTP_UNSUPPORTED_TYPE:
				// 415
				return "Unsupported Media Type";
			case HttpURLConnection.HTTP_INTERNAL_ERROR:
				// 500
				return "Internal Server Error";
			case HttpURLConnection.HTTP_NOT_IMPLEMENTED:
				// 501
				return "Not Implemented";
			case HttpURLConnection.HTTP_BAD_GATEWAY:
				// 502
				return "Bad Gateway";
			case HttpURLConnection.HTTP_UNAVAILABLE:
				// 503
				return "Service Unavailable";
			case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
				// 504
				return "Gateway Timeout";
			case HttpURLConnection.HTTP_VERSION:
				// 505
				return "HTTP Version Not Supported";
			default:
				return "Unknown Http Status Code (" + httpStatusCode + ")";
		}
	}

	public static String createBasicAuthenticationHeaderValue(final String username, final String password) {
		return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
	}

	public static String createBasicAuthenticationHeaderValue(final String username, final char[] password) {
		return "Basic " + Base64.getEncoder().encodeToString((username + ":" + new String(password)).getBytes(StandardCharsets.UTF_8));
	}

	private static String encodeForCookie(final String value) {
		if (value == null) {
			return value;
		} else {
			return value.replace(";", "%3B").replace("=", "%3D");
		}
	}

}
