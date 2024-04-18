/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.service.FileCompressionType;
import org.agnitas.service.ImportException;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.service.RecipientFieldService.RecipientStandardField;

import jakarta.servlet.http.HttpServletRequest;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

public class ImportUtils {

	private static final Logger logger = LogManager.getLogger(ImportUtils.class);

	public static final String RECIPIENT_IMPORT_FILE_ATTRIBUTE_NAME = "recipient-import-file";
	public static final String IMPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientImport";

	public static final String MAIL_TYPE_HTML = "html";
	public static final String MAIL_TYPE_TEXT = "text";
	public static final String MAIL_TYPE_TEXT_ALT = "txt";

	public static final String MAIL_TYPE_DB_COLUMN = "mailtype";

	public static final String MAIL_TYPE_UNDEFINED = "0";
	public static final String MAIL_TYPE_DEFINED = "1";
	
    public enum ImportErrorType {
		ALL("all"),
    	STRUCTURE_ERROR("structure"),
    	
		EMAIL_ERROR("email"),
		BLACKLIST_ERROR("blacklist"),
		KEYDOUBLE_ERROR("keyDouble"),
		NUMERIC_ERROR("numeric"),
		MAILTYPE_ERROR("mailtype"),
		GENDER_ERROR("gender"),
		DATE_ERROR("date"),
    	ENCRYPTION_ERROR("encryption"),
    	DBINSERT_ERROR("dbinsert"),
    	ACTION_ERROR("action"),
		VALUE_TOO_LARGE_ERROR("valueTooLarge"),
		NUMBER_TOO_LARGE_ERROR("numberTooLarge"),
		MISSING_MANDATORY_ERROR("missingMandatory"),
		INVALID_FORMAT_ERROR("invalidFormat"),
		
		UNKNOWN_ERROR("unknown");
    	
    	private String idString;
    	
    	private ImportErrorType(String idString) {
    		this.idString = idString;
    	}
		
		public static ImportErrorType fromString(String idString) throws Exception {
			for (ImportErrorType importErrorType : ImportErrorType.values()) {
        		if (importErrorType.getIdString().equalsIgnoreCase(idString)) {
        			return importErrorType;
        		}
        	}
        	throw new Exception("Unknown ImportErrorType: " + idString);
		}
    	
    	public String getIdString() {
    		return idString;
    	}
    	
    	@Override
    	public String toString() {
    		return idString;
    	}
    }
		
	public static String describeMap(Map<String, String> reportMap) {
		StringBuffer description = new StringBuffer();
		for (Entry<String, String> entry : reportMap.entrySet()) {
			description.append(entry.getKey()).append(" = \"").append(entry.getValue()).append("\"\n");
		}
		return description.toString();
	}
	
	public static boolean isFieldValid(String currentFieldName, Map<String, Object> recipient) {
    	boolean fieldIsValid;
    	String erroneousFieldName = ((String) recipient.get(ImportRecipientsDao.VALIDATOR_RESULT_RESERVED));
		fieldIsValid = erroneousFieldName != null && !currentFieldName.equals(erroneousFieldName);
    	return fieldIsValid;
	}

	/**
	 * Some pages have enctype="multipart/form-data" because they need to
	 * upload file. With such enctype the Charset is corrupted. This method
	 * fixes strings and make them UTF-8
	 *
	 * @param sourceStr source corrupted string
	 * @return fixed string
	 */
	public static String fixEncoding(String sourceStr) {
		try {
			return new String(sourceStr.getBytes("iso-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Error during encoding converting", e);
			return sourceStr;
		}
	}

	public static boolean hasNoEmptyParameterStartsWith(HttpServletRequest request, String paramStart) {
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String paramName = parameterNames.nextElement();
			if (paramName.startsWith(paramStart)) {
				boolean notEmpty = AgnUtils.parameterNotEmpty(request, paramName);
				if(notEmpty){
					return true;
				}
			}
		}
		return false;
	}

	public static String getNotEmptyValueFromParameter(HttpServletRequest request, String parameterStart) {
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String paramName = parameterNames.nextElement();
			if (paramName.startsWith(parameterStart)) {
				if (AgnUtils.parameterNotEmpty(request, paramName)) {
					return request.getParameter(paramName);
				}
			}
		}
		return null;
	}

	public static void removeHiddenColumns(Map<String, CsvColInfo> dbColumns, Admin admin) {
		for (String hiddenColumn : RecipientStandardField.getImportChangeNotAllowedColumns(admin.permissionAllowed(Permission.IMPORT_CUSTOMERID))) {
			dbColumns.remove(hiddenColumn);
		}
	}

	public static boolean checkIfImportFileHasData(File importFile, String optionalZipPassword) throws IOException {
        if (importFile == null) {
            return false;
        } else if (AgnUtils.isZipArchiveFile(importFile)) {
			try {
				if (optionalZipPassword != null) {
					try (ZipFile zipFile = new ZipFile(importFile))  {
						zipFile.setPassword(optionalZipPassword.toCharArray());
						List<FileHeader> fileHeaders = zipFile.getFileHeaders();
						// Check if there is only one file within the zip file
						if (fileHeaders == null || fileHeaders.size() != 1) {
							throw new Exception("Invalid number of files included in zip file");
						} else {
							boolean fileHasData = fileHeaders.get(0).getUncompressedSize() > 0;
							try (InputStream dataInputStream = zipFile.getInputStream(fileHeaders.get(0))) {
								// do nothing
							}
							return fileHasData;
						}
					}
				} else {
					try (InputStream dataInputStream = ZipUtilities.openZipInputStream(new FileInputStream(importFile))) {
						ZipEntry zipEntry = ((ZipInputStream) dataInputStream).getNextEntry();
						if (zipEntry == null) {
							throw new ImportException(false, "error.unzip.noEntry");
						} else {
							if (zipEntry.getSize() == -1) {
								return dataInputStream.read(new byte[5]) > -1;
							} else {
								return zipEntry.getSize() > 0;
							}
						}
					}
				}
			} catch (ImportException e) {
				throw e;
			} catch (Exception e) {
				throw new ImportException(false, "error.unzip", e.getMessage());
			}
		} else {
			boolean fileHasData = importFile.length() > 0;
			try (InputStream dataInputStream = new FileInputStream(importFile)) {
				// do nothing
			}
			return fileHasData;
		}
	}

	public static File createTempImportFile(MultipartFile uploadedFile, Admin admin) throws IOException {
		String fileName = String.format("uploaded_recipient_import_file_%d_%d.csv", admin.getCompanyID(), admin.getAdminID());

		String fileExtension = ".tmp";
		if (uploadedFile.getOriginalFilename() != null && uploadedFile.getOriginalFilename().contains(".")) {
			FileCompressionType fileCompressionType = FileCompressionType.getFileCompressionTypeFromFileName(uploadedFile.getOriginalFilename());
			if (fileCompressionType != null) {
				fileExtension = "." + fileCompressionType.getDefaultFileExtension();
			}
		}
		
		Path path = Files.createTempFile(AgnUtils.createDirectory(IMPORT_FILE_DIRECTORY).toPath(), fileName, null);
		Path targetPath = path.resolveSibling(fileName + fileExtension);

		Files.deleteIfExists(targetPath);
		path = Files.move(path, targetPath);

		File importFile = path.toFile();
		try (OutputStream uploadOutputStream = new FileOutputStream(importFile)) {
			IOUtils.copy(uploadedFile.getInputStream(), uploadOutputStream);
		}

		return importFile;
	}
}
