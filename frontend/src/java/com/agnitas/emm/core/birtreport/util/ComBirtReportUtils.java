/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.agnitas.util.AgnUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class ComBirtReportUtils {
	public static final String BIRT_REPORT_TEMP_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "BirtReport";
	
    public static final int HTTP_RESPONSE_CODE_RESOURCE_NOT_FOUND = 404;

    public static HttpClient initializeHttpClient(String birtUrl) throws MalformedURLException {
        final String BIRT_ACTION = "/run";
        final int PORT_IS_NOT_SET = -1;

        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        URL url = new URL(birtUrl + BIRT_ACTION);
        int port = (url.getPort() == PORT_IS_NOT_SET) ? url.getDefaultPort() : url.getPort();
        httpClient.getHostConfiguration().setHost(url.getHost(), port, url.getProtocol());

        return httpClient;
    }
    
    /**
     * Receives report, writes data to temporary file and returns file handle.
     *
     * @param birtReportId ID of BIRT URL to download
     * @param reportUrl URL of BIRT
     * @param httpClient HTTP client to be used for transfer
     * @param logger logger?
     *
     * @return file handle to temporary file containing report
     *
     * @throws Exception on errors reading report data
     */
    public static File getBirtReportBodyAsTemporaryFile(int birtReportId, String reportUrl, HttpClient httpClient, Logger logger) throws Exception {
        final GetMethod method = new GetMethod(reportUrl);
        try {
            int responseCode = httpClient.executeMethod(method);

            if (responseCode != HTTP_RESPONSE_CODE_RESOURCE_NOT_FOUND) {
            	File file = File.createTempFile( "birt-report-" + birtReportId + "-body", ".tmp", AgnUtils.createDirectory(BIRT_REPORT_TEMP_FILE_DIRECTORY));
            	
            	try( InputStream in = method.getResponseBodyAsStream()) {
	            	try( FileOutputStream out = new FileOutputStream( file)) {
	            		IOUtils.copy( in, out);
	            	}
            	}
            	
            	return file;
            } else {
            	logger.error("getBirtReportBodyAsTemporaryFile received http-code " + responseCode + " for url:\n" + reportUrl);
            	return null;
            }
        } catch (HttpException e) {
        	logger.fatal("HttpClient is in trouble ! Running BirtReport = " + birtReportId, e);
            
            throw e;
        } catch (IOException e) {
        	logger.fatal("I/O-Error while reading from stream ! " + birtReportId, e);
            
            throw e;
        } catch (Throwable t) {
        	logger.fatal("Caught Throwable", t);
            
            throw t;
        } finally {
            method.releaseConnection();
        }
    }
}
