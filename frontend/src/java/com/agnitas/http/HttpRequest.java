/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.http;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.util.Triple;
import org.agnitas.util.Tuple;
import org.apache.commons.lang3.StringUtils;

public class HttpRequest {
	public static final String SECURE_HTTP_PROTOCOL_SIGN = "https://";
	public static final String HTTP_PROTOCOL_SIGN = "http://";

	public static final String HEADER_NAME_BASIC_AUTHENTICATION = "Authorization";
	public static final String HEADER_NAME_USER_AGENT = "User-Agent";
	public static final String HEADER_NAME_DOWNLOAD_COOKIE = "Set-Cookie";
	public static final String HEADER_NAME_UPLOAD_COOKIE = "Cookie";

	public enum HttpMethod {
		GET,
		HEAD,
		POST,
		PUT,
		DELETE,
		CONNECT,
		OPTIONS,
		TRACE,
		PATCH
	}

	private final HttpMethod requestMethod;
	private final String url;
	private String encoding = "UTF-8";

	private final Map<String, String> headers = new LinkedHashMap<>();
	private final List<Tuple<String, Object>> urlParameters = new ArrayList<>();
	private final List<Tuple<String, Object>> postParameters= new ArrayList<>();
	private String requestBody = null;
	private InputStream requestBodyContentStream = null;
	private final List<Triple<String, String, byte[]>> uploadFileAttachments = new ArrayList<>();
	private OutputStream downloadStream = null;
	private File downloadFile = null;
	private final Map<String, Object> pathParameterData = new LinkedHashMap<>();
	private final Map<String, String> cookieData = new LinkedHashMap<>();

	private boolean checkSslCertificates = true;
	private boolean followRedirects = false;

	/**
	 * Http POST Request
	 *
	 * @param url
	 * @throws Exception
	 */
	public HttpRequest(final String url) throws Exception {
		this(HttpMethod.POST, url);
	}

	public HttpRequest(final HttpMethod requestMethod, final String url) throws Exception {
		if (StringUtils.isBlank(url)) {
			throw new Exception("Invalid empty url");
		}
		this.requestMethod = requestMethod == null ? HttpMethod.GET : requestMethod;
		this.url = url;
	}

	public boolean isCheckSslCertificates() {
		return checkSslCertificates;
	}

	public HttpRequest setCheckSslCertificates(final boolean checkSslCertificates) {
		this.checkSslCertificates = checkSslCertificates;

		return this;
	}

	public HttpMethod getRequestMethod() {
		return requestMethod;
	}

	public String getUrl() {
		return url;
	}

	/**
	 * Check for protocol "https://" or "http://" (fallback: "http://")
	 *
	 * @return
	 * @throws Exception
	 */
	public String getUrlWithProtocol() throws Exception {
		if (StringUtils.isBlank(url)) {
			throw new Exception("Invalid empty URL for http request");
		} else if (url.startsWith(SECURE_HTTP_PROTOCOL_SIGN) || url.startsWith(HTTP_PROTOCOL_SIGN)) {
			return url;
		} else {
			return HTTP_PROTOCOL_SIGN + url;
		}
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public HttpRequest addHeader(final String key, final String value) {
		headers.put(key, value);

		return this;
	}

	public HttpRequest addUserAgentHeader(final String userAgent) throws Exception {
		if (headers.containsKey(HEADER_NAME_USER_AGENT)) {
			throw new Exception("Request already contains a UserAgentHeader");
		} else {
			addHeader(HttpRequest.HEADER_NAME_USER_AGENT, userAgent);

			return this;
		}
	}

	public HttpRequest addBasicAuthenticationHeader(final String username, final String password) throws Exception {
		if (headers.containsKey(HEADER_NAME_BASIC_AUTHENTICATION)) {
			throw new Exception("Request already contains a BasicAuthenticationHeader");
		} else {
			addHeader(HttpRequest.HEADER_NAME_BASIC_AUTHENTICATION, HttpUtilities.createBasicAuthenticationHeaderValue(username, password));

			return this;
		}
	}

	public HttpRequest addBasicAuthenticationHeader(final String username, final char[] password) throws Exception {
		if (headers.containsKey(HEADER_NAME_BASIC_AUTHENTICATION)) {
			throw new Exception("Request already contains a BasicAuthenticationHeader");
		} else {
			addHeader(HttpRequest.HEADER_NAME_BASIC_AUTHENTICATION, HttpUtilities.createBasicAuthenticationHeaderValue(username, password));

			return this;
		}
	}

	public List<Tuple<String, Object>> getUrlParameters() {
		return urlParameters;
	}

	public HttpRequest addUrlParameter(final String key, final Object value) {
		urlParameters.add(new Tuple<>(key, value));

		return this;
	}

	public List<Tuple<String, Object>> getPostParameters() {
		return postParameters;
	}

	public HttpRequest addPostParameter(final String key, final Object value) throws Exception {
		if (requestBody != null) {
			throw new Exception("RequestBody is already set. Post parameters cannot be set therefore");
		} else if (requestBodyContentStream != null) {
			throw new Exception("RequestBodyContentStream is already set. Post parameters cannot be set therefore");
		} else {
			postParameters.add(new Tuple<>(key, value));

			return this;
		}
	}

	public List<Triple<String, String, byte[]>> getUploadFileAttachments() {
		return uploadFileAttachments;
	}

	public HttpRequest addUploadFileData(final String htmlInputName, final String fileName, final byte[] data) throws Exception {
		if (requestBody != null) {
			throw new Exception("RequestBody is already set. UploadFileAttachments cannot be set therefore");
		} else if (requestBodyContentStream != null) {
			throw new Exception("RequestBodyContentStream is already set. UploadFileAttachments cannot be set therefore");
		} else {
			uploadFileAttachments.add(new Triple<>(htmlInputName, fileName, data));

			return this;
		}
	}

	public OutputStream getDownloadStream() {
		return downloadStream;
	}

	public HttpRequest setDownloadStream(final OutputStream downloadStream) throws Exception {
		if (downloadFile != null) {
			throw new Exception("DownloadFile is already set. DownloadStream cannot be set therefore");
		} else {
			this.downloadStream = downloadStream;

			return this;
		}
	}

	public File getDownloadFile() {
		return downloadFile;
	}

	public HttpRequest setDownloadFile(final File downloadFile) throws Exception {
		if (downloadStream != null) {
			throw new Exception("DownloadStream is already set. DownloadFile cannot be set therefore");
		} else {
			this.downloadFile = downloadFile;

			return this;
		}
	}

	public Map<String, Object> getPathParameterData() {
		return pathParameterData;
	}

	public HttpRequest addPathParameter(final String key, final Object value) {
		pathParameterData.put(key, value);

		return this;
	}

	public Map<String, String> getCookieData() {
		return cookieData;
	}

	public HttpRequest addCookieData(final String name, final String value) {
		cookieData.put(name, value);

		return this;
	}

	public String getEncoding() {
		return encoding;
	}

	public HttpRequest setEncoding(final String encoding) {
		this.encoding = encoding;

		return this;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public InputStream getRequestBodyContentStream() {
		return requestBodyContentStream;
	}

	public HttpRequest setRequestBody(final String requestBody) throws Exception {
		if (postParameters.size() > 0) {
			throw new Exception("Post parameters are already set. RequestBody cannot be set therefore");
		} else if (uploadFileAttachments.size() > 0) {
			throw new Exception("UploadFileAttachments are already set. RequestBody cannot be set therefore");
		} else if (requestBodyContentStream != null) {
			throw new Exception("RequestBodyContentStream is already set. RequestBody cannot be set therefore");
		} else {
			this.requestBody = requestBody;

			return this;
		}
	}

	public HttpRequest setRequestBodyContentStream(InputStream requestBodyContentStream) throws Exception {
		if (postParameters.size() > 0) {
			throw new Exception("Post parameters are already set. RequestBody cannot be set therefore");
		} else if (uploadFileAttachments.size() > 0) {
			throw new Exception("UploadFileAttachments are already set. RequestBody cannot be set therefore");
		} else if (requestBody != null) {
			throw new Exception("UploadFileAttachments are already set. RequestBody cannot be set therefore");
		} else {
			this.requestBodyContentStream = requestBodyContentStream;

			return this;
		}
	}

	public boolean isFollowRedirects() {
		return followRedirects;
	}

	public HttpRequest setFollowRedirects(final boolean followRedirects) {
		this.followRedirects = followRedirects;

		return this;
	}
}
