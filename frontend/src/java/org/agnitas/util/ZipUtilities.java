/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

public class ZipUtilities {
	/**
	 * Zip a bytearray
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static byte[] zip(byte[] data, String entryFileName) throws IOException {
		try(final ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			try(final ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outStream))) {
				
				if (data != null) {
					addFileDataToOpenZipFileStream(data, entryFileName, zipOutputStream);
				}
				
			}
			
			outStream.flush();
			
			return outStream.toByteArray();
		}
	}
	
	/**
	 * Compress a file or recursively compress all files of a folder.
	 * The compressed file will be placed in the same directory as the data.
	 * 
	 * @param sourceFile
	 * @return
	 * @throws IOException
	 */
	public static File zipFile(File sourceFile) throws IOException {
		File zippedFile = new File(sourceFile.getAbsolutePath() + ".zip");
		zipFile(sourceFile, zippedFile);
		return zippedFile;
	}
	
	/**
	 * Compress a file or recursively compress all files of a folder.
	 * 
	 * @param sourceFile
	 * @param destinationZipFile
	 * @throws IOException
	 */
	public static void zipFile(File sourceFile, File destinationZipFile) throws IOException {
		if (!sourceFile.exists()) {
			throw new IOException("SourceFile does not exist");
		}
			
		if (destinationZipFile.exists()) {
			throw new IOException("DestinationFile already exists");
		}
		
		try(final ZipOutputStream zipOutputStream = openNewZipOutputStream(destinationZipFile)) {
			addFileToOpenZipFileStream(sourceFile, zipOutputStream);
		}
		catch (IOException e) {
			if (destinationZipFile.exists()) {
				destinationZipFile.delete();
			}
			throw e;
		}
	}
	
	/**
	 * Compress a file or recursively compress all files of a folder.
	 * This starts a new relative path.
	 * 
	 * @param sourceFile
	 * @param destinationZipFileSream
	 * @throws IOException
	 */
	public static void addFileToOpenZipFileStream(File sourceFile, ZipOutputStream destinationZipFileSream) throws IOException {
		addFileToOpenZipFileStream(sourceFile, "", destinationZipFileSream);
	}
	
	/**
	 * Compress a file or recursively compress all files of a folder.
	 * 
	 * @param sourceFile
	 * @param destinationZipFileSream
	 * @throws IOException
	 */
	public static void addFileToOpenZipFileStream(final File sourceFile, final String relativeDirPath, final ZipOutputStream destinationZipFileSream) throws IOException {
		if (!sourceFile.exists()) {
			throw new IOException("SourceFile does not exist");
		}
			
		if (destinationZipFileSream == null) {
			throw new IOException("DestinationStream is not ready");
		}
		
		if (relativeDirPath == null) {
			throw new IOException("RelativeDirPath is invalid");
		}
		
		try {
			if (!sourceFile.isDirectory()) {
				final ZipEntry entry = new ZipEntry(relativeDirPath + sourceFile.getName());
				entry.setTime(sourceFile.lastModified());
				destinationZipFileSream.putNextEntry(entry);
				
				try(final BufferedInputStream bufferedFileInputStream = new BufferedInputStream(new FileInputStream(sourceFile))) {
					final byte[] bufferArray = new byte[1024];
					int byteBufferFillLength = bufferedFileInputStream.read(bufferArray);
					while (byteBufferFillLength > -1) {
						destinationZipFileSream.write(bufferArray, 0, byteBufferFillLength);
						byteBufferFillLength = bufferedFileInputStream.read(bufferArray);
					}
					destinationZipFileSream.flush();
					destinationZipFileSream.closeEntry();
				}
			}
			else {
				for (File sourceSubFile : sourceFile.listFiles()) {
					addFileToOpenZipFileStream(sourceSubFile, relativeDirPath + sourceFile.getName() + File.separator, destinationZipFileSream);
				}
			}
		}
		catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * Add data to an open ZipOutputStream as a virtual file
	 * 
	 * @param fileData
	 * @param filename
	 * @param destinationZipFileSream
	 * @throws IOException
	 */
	public static void addFileDataToOpenZipFileStream(byte[] fileData, String filename, ZipOutputStream destinationZipFileSream) throws IOException {
		addFileDataToOpenZipFileStream(fileData, "", filename, destinationZipFileSream);
	}
	
	/**
	 * Open new ZipOutputStream based on a file to write into
	 * 
	 * @param destinationZipFile
	 * @return
	 * @throws IOException
	 */
	public static ZipOutputStream openNewZipOutputStream(File destinationZipFile) throws IOException {
		if (destinationZipFile.exists() && destinationZipFile.length() > 0) {
			throw new IOException("DestinationFile already exists: " + destinationZipFile.getAbsolutePath());
		} else if (!destinationZipFile.getParentFile().exists()) {
			throw new IOException("DestinationDirectory does not exist: " + destinationZipFile.getParentFile().getAbsolutePath());
		}
		
		try {
			return new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destinationZipFile)));
		}
		catch (IOException e) {
			if (destinationZipFile.exists()) {
				destinationZipFile.delete();
			}
			throw e;
		}
	}
	
	/**
	 * Open new ZipOutputStream based on a OutputStream to write into
	 * 
	 * @param destinationZipStream
	 * @return
	 * @throws IOException
	 */
	public static ZipOutputStream openNewZipOutputStream(OutputStream destinationZipStream) throws IOException {
		if (destinationZipStream == null) {
			throw new IOException("DestinationStream is missing");
		}
		
		return new ZipOutputStream(new BufferedOutputStream(destinationZipStream));
	}
	
	/**
	 * Close an open ZipOutputStream
	 * 
	 * @param zipOutputStream
	 * @throws IOException
	 */
	public static void closeZipOutputStream(ZipOutputStream zipOutputStream) throws IOException {
		try {
			zipOutputStream.finish();
			zipOutputStream.flush();
			zipOutputStream.close();
			zipOutputStream = null;
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			if (zipOutputStream != null) {
				try {
					zipOutputStream.close();
				} catch (Exception e) {
					// nothing to do
				}
				zipOutputStream = null;
			}
		}
	}
	
	/**
	 * Close an open ZipOutputStream without errormessages
	 * 
	 * @param zipOutputStream
	 * @throws IOException
	 */
	
	/*			TODO: Unused method
	 
	public static void closeZipOutputStreamQuietly(ZipOutputStream zipOutputStream) {
		try {
			zipOutputStream.finish();
			zipOutputStream.flush();
			zipOutputStream.close();
			zipOutputStream = null;
		}
		catch (IOException e) {
		}
		finally {
			if (zipOutputStream != null) {
				try {
					zipOutputStream.close();
				}
				catch (Exception e) {
				}
				zipOutputStream = null;
			}
		}
	}
	*/
	
	/**
	 * Add data to an open ZipOutputStream as a virtual file
	 * 
	 * @param fileData
	 * @param relativeDirPath
	 * @param filename
	 * @param destinationZipFileSream
	 * @throws IOException
	 */
	public static void addFileDataToOpenZipFileStream(byte[] fileData, String relativeDirPath, String filename, ZipOutputStream destinationZipFileSream) throws IOException {
		if (fileData == null) {
			throw new IOException("FileData is missing");
		}
		
		if (StringUtils.isEmpty(filename) || filename.trim().length() == 0) {
			throw new IOException("Filename is missing");
		}
			
		if (destinationZipFileSream == null) {
			throw new IOException("DestinationStream is not ready");
		}
		
		if (relativeDirPath == null
				|| (!relativeDirPath.endsWith("/")
				&& !relativeDirPath.endsWith("\\")
				&& !relativeDirPath.equals(""))) {
			throw new IOException("RelativeDirPath is invalid");
		}
		
		ZipEntry entry = new ZipEntry(relativeDirPath + filename);
		entry.setTime(new Date().getTime());
		destinationZipFileSream.putNextEntry(entry);
		
		destinationZipFileSream.write(fileData);
		
		destinationZipFileSream.flush();
		destinationZipFileSream.closeEntry();
	}
	
	/**
	 * Compress a file or recursively compress all files of a folder and add the zipped data to an existing file.
	 * All existing entries in the zipped file will be copied in the new one.
	 * 
	 * @param sourceFile
	 * @return
	 * @throws IOException
	 */
	public static void addFileToExistingzipFile(File sourceFile, File zipFile) throws IOException {
		try(ZipOutputStream zipOutputStream = openExistingZipFileForExtension(zipFile)) {
			addFileToOpenZipFileStream(sourceFile, zipOutputStream);
		}
	}
	
	/**
	 * Compress a file or recursively compress all files of a folder and add the zipped data to an existing file.
	 * All existing entries in the zipped file will be copied in the new one.
	 * 
	 * @param sourceFile
	 * @return
	 * @throws IOException
	 */
	public static void addFileToExistingzipFile(List<File> sourceFiles, File zipFile) throws IOException {
		try(final ZipOutputStream zipOutputStream = openExistingZipFileForExtension(zipFile)) {
			for (File file : sourceFiles) {
				addFileToOpenZipFileStream(file, zipOutputStream);
			}
		}
	}

    /**
     * Compress all files from a source List and add the zipped data to an empty existing file.
     *
     * @param sourceFile List of source files
     * @throws IOException
     */
	public static void addFileToEmptyZipFile(List<File> sourceFiles, File zipFile) throws IOException {
		try (FileOutputStream outputStream = new FileOutputStream(zipFile)) {
			try (ZipOutputStream zipOutputStream = openNewZipOutputStream(outputStream)) {
				for (File file : sourceFiles) {
					addFileToOpenZipFileStream(file, zipOutputStream);
				}
			}
		}
	}

	/**
	 * Open an existing Zip file for adding new entries or create a new Zip file if it does not exist yet.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static ZipOutputStream openExistingZipFileForExtensionOrCreateNewZipFile(File zipFile) throws IOException {
		if (zipFile.exists()) {
			return ZipUtilities.openExistingZipFileForExtension(zipFile);
		} else {
			return ZipUtilities.openNewZipOutputStream(zipFile);
		}
	}
	
	/**
	 * Open an existing Zip file for adding new entries.
	 * All existing entries in the zipped file will be copied in the new one.
	 * 
	 * @param zipFile
	 * @return
	 * @throws IOException
	 * @throws ZipException
	 */
	public static ZipOutputStream openExistingZipFileForExtension(File zipFile) throws IOException {
		// Rename source Zip file (Attention: the String path and name of the zipFile are preserved
		File originalFileTemp = new File(zipFile.getParentFile().getAbsolutePath() + "/" + String.valueOf(System.currentTimeMillis()));
		zipFile.renameTo(originalFileTemp);
		
		ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
		
		try (ZipFile sourceZipFile = new ZipFile(originalFileTemp)) {
			// copy entries
			Enumeration<? extends ZipEntry> srcEntries = sourceZipFile.entries();
			while (srcEntries.hasMoreElements()) {
				ZipEntry sourceZipFileEntry = srcEntries.nextElement();
				zipOutputStream.putNextEntry(sourceZipFileEntry);
			
				try (BufferedInputStream bufferedInputStream = new BufferedInputStream(sourceZipFile.getInputStream(sourceZipFileEntry))) {
					byte[] bufferArray = new byte[1024];
					int byteBufferFillLength = bufferedInputStream.read(bufferArray);
					while (byteBufferFillLength > -1) {
						zipOutputStream.write(bufferArray, 0, byteBufferFillLength);
						byteBufferFillLength = bufferedInputStream.read(bufferArray);
					}
					
					zipOutputStream.closeEntry();
				}
			}
			
			zipOutputStream.flush();
			sourceZipFile.close();
			originalFileTemp.delete();
			
			return zipOutputStream;
		} catch (IOException e) {
			// delete existing Zip file
			if (zipFile.exists()) {
				if (zipOutputStream != null) {
					try {
						zipOutputStream.close();
					} catch (Exception ex) {
						// nothing to do
					}
					zipOutputStream = null;
				}
				zipFile.delete();
			}
			
			// revert renaming of source Zip file
			originalFileTemp.renameTo(zipFile);
			throw e;
		}
	}
	
	/**
	 * Read of a Zip file
	 * @param zipFile
	 * @return all file entries
	 * @throws IOException
	 */
	public static Map<String, byte[]> readExistingZipFile(File zipFile) throws IOException {
		
		try (final ZipFile sourceZipFile = new ZipFile(zipFile)){
			final Map<String, byte[]> returnMap = new HashMap<>();
			final byte[] bufferArray = new byte[1024];

			
			// readout of all entries
			final Enumeration<? extends ZipEntry> srcEntries = sourceZipFile.entries();
			
			while (srcEntries.hasMoreElements()) {
				final ZipEntry sourceZipFileEntry = srcEntries.nextElement();
			
				
				try(final BufferedInputStream bufferedInputStream = new BufferedInputStream(sourceZipFile.getInputStream(sourceZipFileEntry))) {
					
					try(final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
						int byteBufferFillLength = bufferedInputStream.read(bufferArray);
						while (byteBufferFillLength > -1) {
							byteArrayOutputStream.write(bufferArray, 0, byteBufferFillLength);
							byteBufferFillLength = bufferedInputStream.read(bufferArray);
						}
					
						byteArrayOutputStream.flush();
						returnMap.put(sourceZipFileEntry.getName(), byteArrayOutputStream.toByteArray());
					}
				}
			}
			
			return returnMap;
		}
	}
	
	public static ZipInputStream openZipInputStream(InputStream sourceZipStream) throws IOException {
		if (sourceZipStream == null) {
			throw new IOException("SourceZipStream is missing");
		}

		return new ZipInputStream(new BufferedInputStream(sourceZipStream), Charset.forName("ISO-8859-1"));
	}

	public static void traverseFilesInZipStream(InputStream sourceZipStream, BiConsumer<ZipInputStream, ZipEntry> consumer) throws IOException {
		try (ZipInputStream zip = openZipInputStream(sourceZipStream)) {
			traverseFilesInZipStream(zip, e -> consumer.accept(zip, e));
		}
	}

	public static void traverseFilesInZipStream(ZipInputStream zip, Consumer<ZipEntry> consumer) throws IOException {
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
			if (!entry.isDirectory()) {
				consumer.accept(entry);
			}
			zip.closeEntry();
		}
	}
	
	public static void compressToEncryptedZipFile(File destinationZipFile, File fileToZip, String zipPassword) throws Exception {
		compressToEncryptedZipFile(destinationZipFile, fileToZip, null, zipPassword);
	}
	
	public static void compressToEncryptedZipFile(File destinationZipFile, File fileToZip, String fileNameInZip, String zipPassword) throws Exception {
		net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(destinationZipFile);
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(CompressionMethod.DEFLATE);
		parameters.setCompressionLevel(CompressionLevel.NORMAL);
		parameters.setEncryptFiles(true);
		parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
		if (StringUtils.isNotBlank(fileNameInZip)) {
			parameters.setFileNameInZip(fileNameInZip);
		}
		zipFile.setPassword(zipPassword.toCharArray());
		zipFile.addFile(fileToZip, parameters);
	}
	
	public static void decompressFromEncryptedZipFile(File encryptedZipFile, File decompressToPath, String zipPassword) throws Exception {
		try {
			net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(encryptedZipFile);
			zipFile.setPassword(zipPassword.toCharArray());
			zipFile.extractAll(decompressToPath.getAbsolutePath());
		} catch (Exception e) {
			throw new ZipDataException("Cannot unzip data: " + e.getMessage(), e);
		}
	}
	
	public static void decompress(File zipFileToDecompress, File decompressToPath) throws Exception {
		try {
			net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(zipFileToDecompress);
			zipFile.extractAll(decompressToPath.getAbsolutePath());
		} catch (Exception e) {
			throw new ZipDataException("Cannot unzip data: " + e.getMessage(), e);
		}
	}
	
	public static byte[] zip(byte[] data) throws IOException {
		if (data == null) {
			return null;
		} else {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			try (ZipOutputStream zipOutputStream = new ZipOutputStream(output)) {
			    ZipEntry entry = new ZipEntry("");
			    entry.setSize(data.length);
			    zipOutputStream.putNextEntry(entry);
			    zipOutputStream.write(data);
			    zipOutputStream.closeEntry();
			}
			return output.toByteArray();
		}
	}
	
	public static byte[] unzip(byte[] zippedData) throws IOException {
		if (zippedData == null) {
			return null;
		} else {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zippedData))) {
				zipInputStream.getNextEntry();
				IOUtils.copy(zipInputStream, output);
			}
			return output.toByteArray();
		}
	}
}
