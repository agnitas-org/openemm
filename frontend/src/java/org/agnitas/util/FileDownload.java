/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jakartaee.commons.io.IOUtils;

/**
 * Download files from HTTP / HTTPS easily.
 */
public final class FileDownload {

	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(FileDownload.class);

	/**
	 * Downloads given URL as file. An HTTP client with a default configuration is used.
	 * The default logger for this class is used
	 * 
	 * @param url URL to download from
	 * @param file file to store downloaded data
	 * 
	 * @return <code>true</code> if download was successful
	 * 
	 * @throws IOException on errors downloading from URL
	 */
	public static final boolean downloadAsFile(final String url, final File file) throws IOException {
		try(final CloseableHttpClient httpClient = createHttpClientForDownload(url)) {
			return downloadAsFile(url, file, httpClient, LOGGER);
		}
	}
	
	/**
	 * Downloads given URL as file. An HTTP client with a default configuration is used.
	 * 
	 * @param url URL to download from
	 * @param file file to store downloaded data
	 * @param logger logger
	 * 
	 * @return <code>true</code> if download was successful
	 * 
	 * @throws IOException on errors downloading from URL
	 */
	public static final boolean downloadAsFile(final String url, final File file, final Logger logger) throws IOException {
		try(final CloseableHttpClient httpClient = createHttpClientForDownload(url)) {
			return downloadAsFile(url, file, httpClient, logger);
		}
	}

	/**
	 * Downloads given URL as file. Given HTTP client is used. The default logger for this class is used.
	 * 
	 * @param url URL to download from
	 * @param file file to store downloaded data
	 * @param httpClient configured HTTP client#
	 * 
	 * @return <code>true</code> if download was successful
	 * 
	 * @throws IOException on errors downloading from URL
	 */
	public static final boolean downloadAsFile(final String url, final File file, final CloseableHttpClient httpClient) throws IOException {
		return downloadAsFile(url, file, httpClient, LOGGER);
	}

	/**
	 * Downloads given URL as file. Given HTTP client is used.
	 * 
	 * @param url URL to download from
	 * @param file file to store downloaded data
	 * @param httpClient configured HTTP client#
	 * @param logger logger
	 * 
	 * @return <code>true</code> if download was successful
	 * 
	 * @throws IOException on errors downloading from URL
	 */
	public static final boolean downloadAsFile(final String url, final File file, final CloseableHttpClient httpClient, final Logger logger) throws IOException {
		// Create request
		final HttpGet request = new HttpGet(url);
		
		// Execute request
		try(final CloseableHttpResponse response = httpClient.execute(request)) {
			final int statusCode = response.getStatusLine().getStatusCode();
			
			// Check response state
			if(statusCode != HttpStatus.SC_OK) {
				// Response does not indicate success
				logger.warn(String.format("Downloading from url '%s' returned HTTP status %d (%s)", url, statusCode, response.getStatusLine().getReasonPhrase()));
				
				return false;
			} 

			// Resonse indicates success, so copy response body to file
			final HttpEntity responseBody = response.getEntity();
			try(final FileOutputStream out = new FileOutputStream(file)) {
				try(final InputStream in = responseBody.getContent()) {
					IOUtils.copy(in, out);
				}
			}
			
			return true;
		}
	}
	
	/**
	 * Creates the HTTP client to download from given URL.
	 * 
	 * @param url URL to download from
	 *
	 * @return configures HTTP client
	 */
	public static final CloseableHttpClient createHttpClientForDownload(final String url) {
		final RequestConfig requestConfig = createRequestConfig(url);
		
		return HttpClients
				.custom()
				.setDefaultRequestConfig(requestConfig)
				.build();
	}
	
	/**
	 * Creates a request configuration.
	 * 
	 * @param url URL to download from
	 * 
	 * @return request configuration
	 */
	private static final RequestConfig createRequestConfig(final String url) {
		final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

		NetworkUtil.setHttpClientProxyFromSystem(requestConfigBuilder, url);

		return requestConfigBuilder.build();
	}
	
}
