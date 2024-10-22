/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Utility class dealing with files and streams.
 */
public class FileUtils {
	
	/** Logger used by this class. */
	private static final Logger logger = LogManager.getLogger( FileUtils.class);
	
	private static final String INVALID_FILENAME_PATTERN = "^.*[\\,%\\&/\\?\\*#:].*$";
	
	public static final String JSON_EXTENSION = ".json";
	
	public static boolean isValidFileName(String originalFilename) {
		if(StringUtils.isEmpty(originalFilename)) {
			return false;
		}
		
		return !Pattern.compile(INVALID_FILENAME_PATTERN).matcher(originalFilename).matches();
	}
	
	public static MediaType getMediaType(File file) {
		if (file == null) {
			logger.error("Could not detect file media type");
			return null;
		}
		
		try {
			String contentType = Files.probeContentType(file.toPath());
			return MediaType.parseMediaType(contentType);
		} catch (IOException e) {
			logger.error("Could not detect file media type of file: {}", file.getName());
		}
		
		return null;
	}
	
	public static void streamToStream( InputStream inputStream, long lengthOfData, OutputStream outputStream) throws IOException {
		byte[] buffer = new byte[16384];		// Data buffer
		long remaining = lengthOfData;			// remaining bytes to read
		int read;								// bytes read in iteration
		
		while( remaining > 0) {
			read = inputStream.read( buffer);
			remaining -= read;
			outputStream.write( buffer, 0, read);
		}
	}
	
	public static boolean removeRecursively( File file) {
		if( file.isDirectory()) {
			File[] containedFiles = file.listFiles();
			
			if(containedFiles != null) {
				for( File containedFile : containedFiles) {
					if( !removeRecursively( containedFile))
						return false;
				}
			}
		}
		
		return file.delete();
	}
	
	public static void deleteFilesByWildcards(File directory, String fileNameWithWildcards) {
		final Pattern filePattern = Pattern.compile(fileNameWithWildcards.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*").replaceAll("\\?", "."));
		File[] filesToBeDeleted = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && filePattern.matcher(file.getName()).matches();
			}
		});

		if(filesToBeDeleted != null) {
			for (File deletableFile : filesToBeDeleted) {
				deletableFile.delete();
			}
		}
	}

	public static String getUniqueFileName(String filename, Predicate<String> isInUse) {
		if (isInUse.test(filename)) {
			String base = FilenameUtils.getBaseName(filename);
			String extension = FilenameUtils.getExtension(filename);

			// Honour file names without extension (just in case)
			if (filename.endsWith(".") || !extension.isEmpty()) {
				extension = "." + extension;
			}

			int index = 1;
			do {
				filename = base + "_" + index + extension;
				index++;
			} while (isInUse.test(filename));
		}
		return filename;
	}
	
	/**
     * Receives files of Birt statistic, writes data to temporary file and returns file handle.
     *
     * @param prefix name for temporary file
     * @param suffix extension for temporary file
     * @param dirName name for temporary directory that will be created
     * @param birtUrl URL of BIRT
     * @param clientInternalUrl internal url to initialize http client to be used for transfer
     *
     * @return file handle to temporary file containing report
     *
     * @throws Exception on errors reading report data
     * 
     * @see FileDownload#downloadAsFile(String, File)
     */
	@Deprecated 
    public static File downloadAsTemporaryFile(String prefix, String suffix, String dirName, String birtUrl, String clientInternalUrl) throws Exception {
		HttpClient httpClient = HttpUtils.initializeHttpClient(clientInternalUrl);
		NetworkUtil.setHttpClientProxyFromSystem(httpClient, birtUrl);
		return downloadAsTemporaryFile(prefix, suffix, dirName, birtUrl, httpClient, logger);
	}

	/**
	 * @see FileDownload#downloadAsFile(String, File, org.apache.http.impl.client.CloseableHttpClient)
	 */
	@Deprecated
    public static File downloadAsTemporaryFile(String prefix, String suffix, String dirName, String birtUrl, HttpClient httpClient, Logger loggerParameter) throws Exception {
		final GetMethod method = new GetMethod(birtUrl);
		method.setFollowRedirects(true);

		try {
            int responseCode = httpClient.executeMethod(method);

            if (responseCode == HttpStatus.SC_OK) {
				File file = File.createTempFile(prefix, suffix, AgnUtils.createDirectory(dirName));

				try (InputStream in = method.getResponseBodyAsStream()) {
	            	try (FileOutputStream out = new FileOutputStream(file)) {
	            		IOUtils.copy(in, out);
	            	}
            	}

				return file;
			} else {
            	loggerParameter.error("downloadAsTemporaryFile received http-code {} for url:\n{}", responseCode, birtUrl);
            	return null;
            }
        } catch (HttpException e) {
        	loggerParameter.fatal("HttpClient is in trouble ! Running URL of BIRT = " + birtUrl, e);
            
            throw e;
        } catch (IOException e) {
        	loggerParameter.fatal("I/O-Error while reading from stream ! " + birtUrl, e);
            
            throw e;
        } catch (Throwable t) {
        	loggerParameter.fatal("Caught Throwable", t);
            
            throw t;
        } finally {
            method.releaseConnection();
        }
    }
}
