/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtilities {

	public static List<String> getZipFileEntries(final File file) throws ZipException, IOException {
		try (ZipFile zipFile = new ZipFile(file)) {
			final Enumeration<? extends ZipEntry> entries = zipFile.entries();
			final List<String> entryList = new ArrayList<>();
			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();
				entryList.add(entry.getName());
			}
			return entryList;
		}
	}
	
	public static List<String> getZipFileEntries(final File file, final char[] zipPassword) throws ZipException, IOException {
		final List<String> entries = new ArrayList<>();
		try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(file, zipPassword)) {
			final List<FileHeader> fileHeaders = zipFile.getFileHeaders();
			if (fileHeaders != null) {
				for (final FileHeader fileHeader : fileHeaders) {
					entries.add(fileHeader.getFileName());
				}
			}
		}
		return entries;
	}
	
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
				} catch (@SuppressWarnings("unused") Exception e) {
					// nothing to do
				}
				zipOutputStream = null;
			}
		}
	}
	
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
		try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(destinationZipFile)) {
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
	}
	
	public static void decompress(File zipFileToDecompress, File decompressToPath) throws Exception {
		try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(zipFileToDecompress)) {
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

	public static InputStream openSingleFileZipInputStream(File singleFileZipFile) throws Exception {
		InputStream dataInputStream = ZipUtilities.openZipInputStream(new FileInputStream(singleFileZipFile));
		try {
			ZipEntry zipEntry = ((ZipInputStream) dataInputStream).getNextEntry();
			if (zipEntry == null) {
				dataInputStream.close();
				dataInputStream = null;
				return null;
			} else {
				return dataInputStream;
			}
		} catch (Exception e) {
			if (dataInputStream != null) {
				dataInputStream.close();
				dataInputStream = null;
			}
			throw e;
		}
	}

	public static long getDataSizeUncompressed(final File zippedFile) throws ZipException, IOException {
		try (final ZipFile zipFile = new ZipFile(zippedFile)) {
			long uncompressedSize = 0;
			final Enumeration<? extends ZipEntry> e = zipFile.entries();
			while (e.hasMoreElements()) {
				final ZipEntry entry = e.nextElement();
				final long originalSize = entry.getSize();
				if (originalSize >= 0) {
					uncompressedSize += originalSize;
				} else {
					// -1 indicates, that size is unknown
					return originalSize;
				}
			}
			return uncompressedSize;
		}
	}

	public static long getUncompressedSize(final File zipFilePath, final char[] zipPassword) throws IOException {
		try (final net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(zipFilePath, zipPassword)) {
			final List<FileHeader> fileHeaders = zipFile.getFileHeaders();
			long uncompressedSize = 0;
			for (final FileHeader fileHeader : fileHeaders) {
				final long originalSize = fileHeader.getUncompressedSize();
				if (originalSize >= 0) {
					uncompressedSize += originalSize;
				} else {
					// -1 indicates, that size is unknown
					uncompressedSize = originalSize;
					break;
				}
			}
			return uncompressedSize;
		}
	}

	public static InputStreamWithOtherItemsToClose openPasswordSecuredZipFile(final String importFilePathOrData, final char[] zipPassword) throws Exception {
		return openPasswordSecuredZipFile(importFilePathOrData, zipPassword, null);
	}

	@SuppressWarnings("resource")
	public static InputStreamWithOtherItemsToClose openPasswordSecuredZipFile(final String importFilePathOrData, final char[] zipPassword, final String zippedFilePathAndName) throws Exception {
		net.lingala.zip4j.ZipFile zipFile = null;
		try {
			zipFile = new net.lingala.zip4j.ZipFile(importFilePathOrData, zipPassword);
			final List<FileHeader> fileHeaders = zipFile.getFileHeaders();
			FileHeader selectedFileHeader = null;

			if (fileHeaders != null && fileHeaders.size() >= 0) {
				if (StringUtils.isBlank(zippedFilePathAndName)) {
					if (fileHeaders.size() == 1 && !fileHeaders.get(0).isDirectory()) {
						return new InputStreamWithOtherItemsToClose(zipFile.getInputStream(fileHeaders.get(0)), fileHeaders.get(0).getFileName(), zipFile);
					} else {
						throw new Exception("Zip file '" + importFilePathOrData + "' contains more than one file");
					}
				} else {
					for (final FileHeader fileHeader : fileHeaders) {
						if (!fileHeader.isDirectory() && fileHeader.getFileName().equals(zippedFilePathAndName)) {
							selectedFileHeader = fileHeader;
							break;
						}
					}
					if (selectedFileHeader != null) {
						return new InputStreamWithOtherItemsToClose(zipFile.getInputStream(selectedFileHeader), selectedFileHeader.getFileName(), zipFile);
					} else {
						throw new Exception("Zip file '" + importFilePathOrData + "' does not include defined zipped file '" + zippedFilePathAndName + "'");
					}
				}
			} else {
				throw new Exception("Zip file '" + importFilePathOrData + "' is empty");
			}
		} catch (final Exception e) {
			try {
				if (zipFile != null) {
					zipFile.close();
				}
			} catch (@SuppressWarnings("unused") final IOException e1) {
				// Do nothing
			}
			throw e;
		}
	}
	
	public static ZipOutputStream openNewZipOutputStream(final File destinationZipFile, Charset fileNameEncodingCharset)
			throws IOException {
		if (destinationZipFile.exists()) {
			throw new IOException("DestinationFile already exists");
		} else if (!destinationZipFile.getParentFile().exists()) {
			throw new IOException("DestinationDirectory does not exist");
		}

		if (fileNameEncodingCharset == null) {
			fileNameEncodingCharset = Charset.forName("Cp437");
		}

		try {
			return new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destinationZipFile)));
		} catch (final IOException e) {
			if (destinationZipFile.exists()) {
				destinationZipFile.delete();
			}
			throw e;
		}
	}

	public static InputStreamWithOtherItemsToClose openZipFile(final String importFilePathOrData) throws Exception {
		return openZipFile(importFilePathOrData, null);
	}

	@SuppressWarnings("resource")
	public static InputStreamWithOtherItemsToClose openZipFile(final String importFilePathOrData, final String zippedFilePathAndName) throws Exception {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(importFilePathOrData);
			final List<? extends ZipEntry> fileHeaders = Collections.list(zipFile.entries());
			ZipEntry selectedFileHeader = null;

			if (fileHeaders != null && fileHeaders.size() >= 0) {
				if (StringUtils.isBlank(zippedFilePathAndName)) {
					if (fileHeaders.size() == 1 && !fileHeaders.get(0).isDirectory()) {
						return new InputStreamWithOtherItemsToClose(zipFile.getInputStream(fileHeaders.get(0)), fileHeaders.get(0).getName(), zipFile);
					} else {
						throw new Exception("Zip file '" + importFilePathOrData + "' contains more than one file");
					}
				} else {
					for (final ZipEntry fileHeader : fileHeaders) {
						if (!fileHeader.isDirectory() && fileHeader.getName().equals(zippedFilePathAndName)) {
							selectedFileHeader = fileHeader;
							break;
						}
					}
					if (selectedFileHeader != null) {
						return new InputStreamWithOtherItemsToClose(zipFile.getInputStream(selectedFileHeader), selectedFileHeader.getName(), zipFile);
					} else {
						throw new Exception("Zip file '" + importFilePathOrData + "' does not include defined zipped file '" + zippedFilePathAndName + "'");
					}
				}
			} else {
				throw new Exception("Zip file '" + importFilePathOrData + "' is empty");
			}
		} catch (final Exception e) {
			try {
				if (zipFile != null) {
					zipFile.close();
				}
			} catch (@SuppressWarnings("unused") final IOException e1) {
				// Do nothing
			}
			throw e;
		}
	}
}
