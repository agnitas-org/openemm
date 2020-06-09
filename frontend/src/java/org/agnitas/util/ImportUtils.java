/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.service.ImportException;
import org.agnitas.service.impl.DataType;
import org.apache.log4j.Logger;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.Permission;

public class ImportUtils {
	private static final transient Logger logger = Logger.getLogger(ImportUtils.class);

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
		INVALID_FORMAT_ERROR("invalidFormat");
    	
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
    	String errorneousFieldName = ((String) recipient.get(ImportRecipientsDao.VALIDATOR_RESULT_RESERVED));
		fieldIsValid = errorneousFieldName != null && !currentFieldName.equals(errorneousFieldName);
    	return fieldIsValid;
	}

	public static String dbtype2string(int type) {
		switch (type) {
			case java.sql.Types.BIGINT:
			case java.sql.Types.INTEGER:
			case java.sql.Types.SMALLINT:
				return DataType.INTEGER;

			case java.sql.Types.DECIMAL:
			case java.sql.Types.DOUBLE:
			case java.sql.Types.FLOAT:
			case java.sql.Types.NUMERIC:
			case java.sql.Types.REAL:
				return DataType.DOUBLE;

			case java.sql.Types.CHAR:
				return DataType.CHAR;

			case java.sql.Types.VARCHAR:
			case java.sql.Types.LONGVARCHAR:
			case java.sql.Types.CLOB:
				return DataType.VARCHAR;

			case java.sql.Types.DATE:
			case java.sql.Types.TIMESTAMP:
			case java.sql.Types.TIME:
				return DataType.DATE;
				
			default:
				return "UNKNOWN(" + type + ")";
		}
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

	public static void removeHiddenColumns(Map<String, CsvColInfo> dbColumns, ComAdmin admin) {
		for (String hiddenColumn : getHiddenColumns(admin)) {
			dbColumns.remove(hiddenColumn);
		}
	}

	public static List<String> getHiddenColumns(ComAdmin admin) {
		List<String> hiddenColumns = new ArrayList<>();
		hiddenColumns.add("change_date");
		hiddenColumns.add(ComCompanyDaoImpl.STANDARD_FIELD_TIMESTAMP);
		hiddenColumns.add(ComCompanyDaoImpl.STANDARD_FIELD_CREATION_DATE);
		hiddenColumns.add(ComCompanyDaoImpl.STANDARD_FIELD_DATASOURCE_ID);
		hiddenColumns.add(ComCompanyDaoImpl.STANDARD_FIELD_BOUNCELOAD);
		hiddenColumns.add(ComCompanyDaoImpl.STANDARD_FIELD_LATEST_DATASOURCE_ID);
		hiddenColumns.add(ComCompanyDaoImpl.STANDARD_FIELD_LASTOPEN_DATE);
		hiddenColumns.add(ComCompanyDaoImpl.STANDARD_FIELD_LASTCLICK_DATE);
		hiddenColumns.add(ComCompanyDaoImpl.STANDARD_FIELD_LASTSEND_DATE);
		hiddenColumns.add(ComCompanyDaoImpl.STANDARD_FIELD_CLEANED_DATE);
		if (!admin.permissionAllowed(Permission.IMPORT_CUSTOMERID)) {
			hiddenColumns.add(ComCompanyDaoImpl.STANDARD_FIELD_CUSTOMER_ID);
		}
		if (!admin.permissionAllowed(Permission.RECIPIENT_TRACKING_VETO)) {
			hiddenColumns.add(ComCompanyDaoImpl.STANDARD_FIELD_DO_NOT_TRACK);
		}
		return Collections.unmodifiableList(hiddenColumns);
	}

	public static boolean checkIfImportFileHasData(File importFile, boolean isZipped, String optionalZipPassword) throws IOException {
        if (importFile == null) {
            return false;
        } else if (isZipped) {
			try {
				if (optionalZipPassword == null) {
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
				} else {
					File unzipPath = new File(importFile.getAbsolutePath() + ".unzipped");
					unzipPath.mkdir();
					ZipUtilities.decompressFromEncryptedZipFile(importFile, unzipPath, optionalZipPassword);
					
					// Check if there was only one file within the zip file and use it for import
					String[] filesToImport = unzipPath.list();
					if (filesToImport.length != 1) {
						throw new Exception("Invalid number of files included in zip file");
					} else {
						boolean fileHasData = new File(unzipPath.getAbsolutePath() + "/" + filesToImport[0]).length() > 0;
						try (InputStream dataInputStream = new FileInputStream(unzipPath.getAbsolutePath() + "/" + filesToImport[0])) {
							// do nothing
						}
						return fileHasData;
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
}
