/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jakartaee.commons.io.IOUtils;

/**
 * Download files from HTTP / HTTPS easily.
 */
public final class FileDownload {

	private static final Logger LOGGER = LogManager.getLogger(FileDownload.class);

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
	public static boolean downloadAsFile(String url, File file, CloseableHttpClient httpClient) throws IOException {
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
	public static boolean downloadAsFile(String url, File file, CloseableHttpClient httpClient, Logger logger) throws IOException {
		// Create request
		final HttpGet request = new HttpGet(url);
		
		// Execute request
		try (CloseableHttpResponse response = httpClient.execute(request)) {
			final int statusCode = response.getCode();
			
			// Check response state
			if(statusCode != HttpStatus.SC_OK) {
				// Response does not indicate success
				logger.warn("Downloading from url '{}' returned HTTP status {} ({})", url, statusCode, response.getReasonPhrase());
				return false;
			} 

			// Resonse indicates success, so copy response body to file
			final HttpEntity responseBody = response.getEntity();
			try (FileOutputStream out = new FileOutputStream(file)) {
				try (InputStream in = responseBody.getContent()) {
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
	public static CloseableHttpClient createHttpClientForDownload(String url) {
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
	private static RequestConfig createRequestConfig(String url) {
		final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

		NetworkUtil.setHttpClientProxyFromSystem(requestConfigBuilder, url);

		return requestConfigBuilder.build();
	}
	
}
