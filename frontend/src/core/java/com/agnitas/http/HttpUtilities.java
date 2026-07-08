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

	private HttpUtilities() {

	}

	private static final TrustManager TRUSTALLCERTS_TRUSTMANAGER = new X509TrustManager() {
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
			if (httpRequest.getUrlParameters() != null && !httpRequest.getUrlParameters().isEmpty()) {
				final String getParameterString = convertToParameterString(httpRequest.getUrlParameters(), httpRequest.getEncoding());
				if (!parametersFromUrl.isEmpty()) {
					requestedUrl += "?" + parametersFromUrl + "&" + getParameterString;
				} else {
					requestedUrl += "?" + getParameterString;
				}
			} else if (!parametersFromUrl.isEmpty()) {
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

			if (httpRequest.getHeaders() != null && !httpRequest.getHeaders().isEmpty()) {
				for (final Entry<String, String> headerEntry : httpRequest.getHeaders().entrySet()) {
					urlConnection.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
				}
			}

			if (httpRequest.getCookieData() != null && !httpRequest.getCookieData().isEmpty()) {
				final StringBuilder cookieValue = new StringBuilder();
				for (final Entry<String, String> cookieEntry : httpRequest.getCookieData().entrySet()) {
					if (!cookieValue.isEmpty()) {
						cookieValue.append("; ");
					}
					cookieValue.append(encodeForCookie(cookieEntry.getKey())).append("=").append(encodeForCookie(cookieEntry.getValue()));
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
			} else if (httpRequest.getUploadFileAttachments() != null && !httpRequest.getUploadFileAttachments().isEmpty()) {
				urlConnection.setDoOutput(true);
				urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
				final OutputStream outputStream = urlConnection.getOutputStream();

				if (httpRequest.getPostParameters() != null && !httpRequest.getPostParameters().isEmpty()) {
					for (final Tuple<String, Object> postParameter : httpRequest.getPostParameters()) {
						outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
						outputStream.write(("Content-Disposition: form-data; name=\"" + URLEncoder.encode(postParameter.getFirst(), StandardCharsets.UTF_8) + "\"\r\n").getBytes(StandardCharsets.UTF_8));
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
			} else if (httpRequest.getPostParameters() != null && !httpRequest.getPostParameters().isEmpty()) {
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
							cookiesMap.put(URLDecoder.decode(cookieParts[0].trim(), StandardCharsets.UTF_8), URLDecoder.decode(cookieParts[1].trim(), StandardCharsets.UTF_8));
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
							if (!httpResponseContent.isEmpty()) {
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
						if (!httpResponseContent.isEmpty()) {
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
			for (final Tuple<String, Object> entry : parameters) {
				if (!returnValue.isEmpty()) {
					returnValue.append("&");
				}
				returnValue.append(URLEncoder.encode(entry.getFirst(), encoding));
				returnValue.append("=");
				if (entry.getSecond() != null) {
					returnValue.append(URLEncoder.encode(entry.getSecond().toString(), encoding));
				}
			}

			return returnValue.toString();
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
					escapedParameterNameAndValue.append(value);
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
