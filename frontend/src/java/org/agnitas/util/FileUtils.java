/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;

/**
 * Utility class dealing with files and streams.
 */
public class FileUtils {
	
	/** Logger used by this class. */
	private static final transient Logger logger = Logger.getLogger( FileUtils.class);
	
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
			logger.error("Could not detect file media type of file: " + file.getName());
		}
		
		return null;
	}
	
	/**
	 * Exception indicating, that a given ZIP entry was not found in ZIP file.
	 */
	public static class ZipEntryNotFoundException extends Exception {
		private static final long serialVersionUID = 73446886704139051L;
		
		/** Name of the ZIP entry. */
		private final String zipEntryName;
		
		/**
		 * Creates a new exception.
		 * 
		 * @param zipEntryName name of the ZIP entry
		 */
		public ZipEntryNotFoundException( String zipEntryName) {
			super( "ZIP entry not found: " + zipEntryName);
			
			this.zipEntryName = zipEntryName;
		}
		
		/**
		 * Returns the name of the ZIP entry.
		 * 
		 * @return name of the ZIP entry
		 */
		public String getZipEntryName() {
			return this.zipEntryName;
		}
	}
	
	/**
	 * Extracts a given entry from a ZIP file to a temporary file.
	 * The temporary file must be handled by the caller. No "deleteOnExit()" is called
	 * to that file.
	 * 
	 * @param zipFile ZIP file to use
	 * @param entryName name of the ZIP entry to extract
	 * @param tempFilePrefix prefix for the temporary file
	 * 
	 * @return the temporary file
	 * 
	 * @throws IOException on errors during extraction of ZIP entry
	 * @throws ZipEntryNotFoundException when the ZIP entry does not exist in the ZIP file
	 */
	public static File extractZipEntryToTemporaryFile( ZipFile zipFile, String entryName, String tempFilePrefix) throws IOException, ZipEntryNotFoundException {
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Transferring data from ZIP entry to temporary file");
		}
		
		File file = File.createTempFile( tempFilePrefix, null);
		extractZipEntryToFile( zipFile, entryName, file);
		
		return file;
	}
	
	/**
	 * Extracts an entry of a given ZIP file to a specific destination file.
	 * 
	 * @param zipFile ZIP file to use
	 * @param entryName name of the ZIP entry
	 * @param destinationFile file to extract to
	 * 
	 * @throws ZipEntryNotFoundException when the ZIP entry was not found in the ZIP file
	 * @throws IOException on errors during extraction
	 */
	public static void extractZipEntryToFile( ZipFile zipFile, String entryName, File destinationFile) throws ZipEntryNotFoundException, IOException {
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Transferring ZIP entry " + entryName + " from ZIP file " + zipFile.getName() + " to " + destinationFile.getAbsolutePath());
		}
		
		ZipEntry zipEntry = zipFile.getEntry( entryName);
		
		if( zipEntry == null) {
			logger.info( "ZIP entry not found: " + entryName);
			
			throw new ZipEntryNotFoundException( entryName);
		}
		
		try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
			streamToFile( inputStream, zipEntry.getSize(), destinationFile);
		}
	}
	
	public static void extractZipEntryToStream(  ZipFile zipFile, String entryName, OutputStream outputStream) throws ZipEntryNotFoundException, IOException {
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Transferring ZIP entry " + entryName + " from ZIP file " + zipFile.getName() + " to stream");
		}
		
		ZipEntry zipEntry = zipFile.getEntry( entryName);
		
		if( zipEntry == null) {
			logger.info( "ZIP entry not found: " + entryName);
			
			throw new ZipEntryNotFoundException( entryName);
		}
		
		try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
			streamToStream( inputStream, zipEntry.getSize(), outputStream);
		}
	}
	
	/**		
	 		byte[] buffer = new byte[16384];
			long remaining = lengthOfData;
			int read;
			while( remaining > 0) {
				read = inputStream.read( buffer);
				remaining -= read;
				outputStream.write( buffer, 0, read);
			}

	 * Writes data from an InputStream to a temporary file. The size of data to
	 * be transferred must be known in order to use this method. The caller must handle
	 * the temporary file. No "deleteOnExit()" is called to that file.
	 * The caller also has to handle the InputStream. It is not closed by this method.
	 * 
	 * @param inputStream InputStream to read from
	 * @param lengthOfData number of bytes to be transferred
	 * @param tempFilePrefix prefix of the temporary file
	 * 
	 * @return the temporary file containing the data
	 * 
	 * @throws IOException on errors reading from the stream
	 */
	public static File streamToTemporaryFile( InputStream inputStream, long lengthOfData, String tempFilePrefix) throws IOException {
		if( logger.isDebugEnabled()) {
			logger.debug( "Transferring data from stream to temporary file");
		}
		
		File file = File.createTempFile( tempFilePrefix, null);
		streamToFile( inputStream, lengthOfData, file);
		
		return file;
	}
	
	/**
	 * Writes data from an InputStream to a temporary file. The size of data to
	 * be transferred must be known in order to use this method. 
	 * The caller has to handle the InputStream. It is not closed by this method.
	 * 
	 * @param inputStream InputStream to read from
	 * @param lengthOfData number of bytes to be transferred
	 * @param file file to write data
	 * 
	 * @throws IOException on errors reading from the stream
	 */
	public static void streamToFile( InputStream inputStream, long lengthOfData, File file) throws IOException {
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Transferring " + lengthOfData + " bytes from stream to file " + file.getAbsolutePath());
		}
		
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			streamToStream( inputStream, lengthOfData, outputStream);
		}
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
	
	public static void copyFileToDirectory( File sourceFile, File destinationDirectory) throws IOException {
		if( !destinationDirectory.isDirectory()) 
			throw new IllegalArgumentException( "destination is not a directory: " + destinationDirectory.getAbsolutePath());
		
		File destinationFile = new File( destinationDirectory.getCanonicalPath() + File.separator + sourceFile.getName());
		try (FileInputStream inputStream = new FileInputStream(sourceFile)) {
			streamToFile( inputStream, sourceFile.length(), destinationFile);
		} catch( IOException e) {
			logger.error( "Error copying file  (" + sourceFile.getAbsolutePath() + " to directory " + destinationDirectory.getAbsolutePath() + ")", e);
			throw e;
		}
	}

	public static void createPathToFile( File file) {
		File parent = file.getParentFile();
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Creating directory structure " + parent.getAbsolutePath() + " for file " + file.getAbsolutePath());
		}
		
		parent.mkdirs();
	}
	
	public static void createPath( String path) {
		File file = new File( path);
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Creating directory structure for " + file.getAbsolutePath());
		}
		
		file.mkdirs();
	}
	
	public static String removeTrailingSeparator( String path) {
		String correctedPath = path;
		
		while( correctedPath.endsWith( File.separator))
			correctedPath = correctedPath.substring( 0, correctedPath.lastIndexOf( File.separator));
		
		if( logger.isDebugEnabled()) {
			logger.debug( "Corrected path " + path + " to " + correctedPath);
		}
		
		return correctedPath;
	}

	public static boolean removeRecursively( String name) {
		File file = new File( name);
		
		return removeRecursively( file);
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
     */
    public static File downloadAsTemporaryFile(String prefix, String suffix, String dirName, String birtUrl, String clientInternalUrl) throws Exception {
		HttpClient httpClient = HttpUtils.initializeHttpClient(clientInternalUrl);
		return downloadAsTemporaryFile(prefix, suffix, dirName, birtUrl, httpClient, logger);
	}
	
    public static File downloadAsTemporaryFile(String prefix, String suffix, String dirName, String birtUrl, HttpClient httpClient, Logger loggerParameter) throws Exception {
        final GetMethod method = new GetMethod(birtUrl);
		
		try {
            int responseCode = httpClient.executeMethod(method);

            if (responseCode != HttpStatus.SC_NOT_FOUND) {
            	File file = File.createTempFile(prefix, suffix, AgnUtils.createDirectory(dirName));
            	
            	try( InputStream in = method.getResponseBodyAsStream()) {
	            	try( FileOutputStream out = new FileOutputStream( file)) {
	            		IOUtils.copy( in, out);
	            	}
            	}
            	
            	return file;
            } else {
            	loggerParameter.error("downloadAsTemporaryFile received http-code " + responseCode + " for url:\n" + birtUrl);
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
