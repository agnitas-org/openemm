/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.ColumnMapping;
import com.agnitas.beans.ImportProfile;
import com.agnitas.emm.core.import_profile.bean.ImportDataType;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.MapUtils;
import com.agnitas.util.importvalues.Charset;
import com.agnitas.util.importvalues.CheckForDuplicates;
import com.agnitas.util.importvalues.DateFormat;
import com.agnitas.util.importvalues.ImportMode;
import com.agnitas.util.importvalues.MailType;
import com.agnitas.util.importvalues.NullValuesAction;
import com.agnitas.util.importvalues.Separator;
import com.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.lang3.StringUtils;

public class ImportProfileImpl implements ImportProfile {
	protected int id;
    protected int adminId;
    protected int companyId;
    protected String name;
    protected int separator;
    protected int textRecognitionChar;
    protected int charset;
    protected int dateFormat;
    protected int importMode = 1;
    protected int checkForDuplicates;
    protected int nullValuesAction = 0;
    protected Map<String, Integer> genderMapping = Collections.synchronizedMap(new HashMap<>());
    protected List<ColumnMapping> columnMapping = Collections.synchronizedList(new ArrayList<>());
    protected String mailForReport;
    protected String mailForError;
    protected int defaultMailType;
    private boolean updateAllDuplicates = true;
	private int importId;
    private List<String> keyColumns;
	private int importProcessActionID;
	private char decimalSeparator = '.';
	private int actionForNewRecipients;
    private boolean noHeaders = false;
    private String zipPassword = null;
    private boolean autoMapping = false;
    private List<Integer> mailinglistIds = new ArrayList<>();
    private Set<MediaTypes> mediatypes = new HashSet<>();
    private String datatype = "CSV";
	private boolean mailinglistsAll;
    // Used to get mapping from form. Try to delete this field or replace
    // Map<String, Integer> genderMapping with Map<Integer, Set<String>> genderMapping while migration to Spring
    protected Map<Integer, String> genderMappingsToSave = new HashMap<>();

	private Locale reportLocale = new Locale("en", "US");
	private String reportTimezone = "Europe/Berlin";
    
    public ImportProfileImpl() {
    	keyColumns = new ArrayList<>();
    	keyColumns.add("email");
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
	public int getAdminId() {
        return adminId;
    }

    @Override
	public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    @Override
	public int getCompanyId() {
        return companyId;
    }

    @Override
	public void setCompanyId( int companyId) {
        this.companyId = companyId;
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public void setName(String name) {
        this.name = name;
    }

    @Override
	public int getSeparator() {
        return separator;
    }

    @Override
	public void setSeparator(int separator) {
        this.separator = separator;
    }

    @Override
	public int getTextRecognitionChar() {
        return textRecognitionChar;
    }

    @Override
	public void setTextRecognitionChar(int textRecognitionChar) {
        this.textRecognitionChar = textRecognitionChar;
    }

    @Override
	public int getCharset() {
        return charset;
    }

    @Override
	public void setCharset(int charset) {
        this.charset = charset;
    }

    @Override
	public int getDateFormat() {
        return dateFormat;
    }

    @Override
	public void setDateFormat(int dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
	public int getImportMode() {
        return importMode;
    }

    @Override
	public void setImportMode(int importMode) {
        this.importMode = importMode;
    }

    @Override
	public int getCheckForDuplicates() {
        return checkForDuplicates;
    }

    @Override
	public void setCheckForDuplicates(int checkForDuplicates) {
        this.checkForDuplicates = checkForDuplicates;
    }

    @Override
	public int getNullValuesAction() {
        return nullValuesAction;
    }

    @Override
	public void setNullValuesAction(int nullValuesAction) {
        this.nullValuesAction = nullValuesAction;
    }

    @Override
	public Map<String, Integer> getGenderMapping() {
        return genderMapping;
    }

    @Override
    public Map<Integer, String> getGenderMappingsToSave() {
        return genderMappingsToSave;
    }

    @Override
	public void setGenderMappingsToSave(Map<Integer, String> genderMappingsToSave) {
        this.genderMappingsToSave = genderMappingsToSave;
    }

    @Override
	public void setGenderMapping(Map<String, Integer> genderMapping) {
        this.genderMapping = genderMapping;
    }

    @Override
	public List<ColumnMapping> getColumnMapping() {
        return columnMapping;
    }

    @Override
	public void setColumnMapping(List<ColumnMapping> columnMapping) {
        this.columnMapping = columnMapping;
    }

    @Override
    public String getMailForReport() {
        return mailForReport;
    }

    @Override
    public void setMailForReport(String mailForReport) {
        this.mailForReport = mailForReport;
    }

    @Override
    public String getMailForError() {
        return mailForError;
    }

    @Override
    public void setMailForError(String mailForError) {
        this.mailForError = mailForError;
    }

    @Override
    public int getDefaultMailType() {
        return defaultMailType;
    }

    @Override
    public void setDefaultMailType(int defaultMailType) {
        this.defaultMailType = defaultMailType;
    }

    @Override
    public boolean isAutoMapping() {
        return autoMapping;
    }

    @Override
    public void setAutoMapping(boolean autoMapping) {
        this.autoMapping = autoMapping;
    }

    @Override
	public Integer getGenderValueByFieldValue(String fieldValue) {
        if (isParsableToInt(fieldValue)) {
        	Integer gender = genderMapping.get(fieldValue);
        	if (gender == null) {
        		return Integer.valueOf(fieldValue);
        	} else {
        		return gender;
        	}
        } else {
            return genderMapping.get(fieldValue);
        }
    }

    private boolean isParsableToInt(String i) {
        try {
            Integer.parseInt(i);
            return true;
        }
        catch (NumberFormatException nfe) {
            return false;
        }
    }

    @Override
	public boolean getUpdateAllDuplicates() {
        return updateAllDuplicates;
    }

    @Override
	public void setUpdateAllDuplicates(boolean updateAllDuplicates) {
        this.updateAllDuplicates = updateAllDuplicates;
    }

	@Override
	public int getImportId() {
		return importId;
	}

	@Override
	public void setImportId(int importId) {
		this.importId = importId;
	}
	
	@Override
    public Map<String, Integer> getGenderMappingJoined() {
    	return MapUtils.sortByValues(MapUtils.joinStringKeysByValue(genderMapping, ", "));
    }

    @Override
    public List<String> getKeyColumns() {
        return keyColumns;
    }

    @Override
	public void setKeyColumn(String keyColumn) {
    	keyColumns.clear();
    	if (StringUtils.isNotBlank(keyColumn)) {
    		keyColumns.add(keyColumn.toLowerCase());
    	}
    }

    @Override
    public String getFirstKeyColumn() {
        return keyColumns.get(0);
    }

    @Override
	public void setFirstKeyColumn(String keyColumn) {
    	keyColumns.set(0, keyColumn.toLowerCase());
    }

    @Override
    public void setKeyColumns(List<String> newKeyColumns) {
        keyColumns.clear();
        for (String keyColumn : newKeyColumns) {
        	keyColumns.add(keyColumn.toLowerCase());
        }
    }

    @Override
	public boolean keyColumnsContainsCustomerId() {
        for (String column : keyColumns) {
            if ("customer_id".equalsIgnoreCase(column)) {
                return true;
            }
        }
        return false;
    }

	@Override
	public Set<String> getEncryptedColumns() {
		Set<String> encryptedColumns = new HashSet<>();
		for (ColumnMapping columnMappingEntry : getColumnMapping()) {
			if (columnMappingEntry.isEncrypted()) {
				encryptedColumns.add(columnMappingEntry.getDatabaseColumn());
			}
		}
		return encryptedColumns;
	}

    @Override
	public void setImportProcessActionID(int importProcessActionID) {
    	this.importProcessActionID = importProcessActionID;
    }

	@Override
	public int getImportProcessActionID() {
		return importProcessActionID;
	}

    @Override
	public void setActionForNewRecipients(int actionForNewRecipients) {
    	this.actionForNewRecipients = actionForNewRecipients;
    }

	@Override
	public int getActionForNewRecipients() {
		return actionForNewRecipients;
	}

	@Override
	public void setDecimalSeparator(char decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
	}

	@Override
	public char getDecimalSeparator() {
		return decimalSeparator;
	}

	@Override
	public boolean isNoHeaders() {
		return noHeaders;
	}

	@Override
	public void setNoHeaders(boolean noHeaders) {
		this.noHeaders = noHeaders;
	}

	@Override
	public String getZipPassword() {
		return zipPassword;
	}

	@Override
	public void setZipPassword(String zipPassword) {
		this.zipPassword = zipPassword;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder("\"" + name + "\" (CID " + companyId + " / ID " + id + ")\n");
		output.append("Charset: " + Charset.getCharsetById(charset).getCharsetName() + "\n");

		output.append("EncryptedZip: " + (zipPassword != null) + "\n");
		output.append("AutoMapping: " + autoMapping + "\n");
		
		output.append("Data type: " + datatype + "\n");
		ImportDataType importDataType = ImportDataType.getImportDataTypeForName(datatype);
		if (importDataType == ImportDataType.CSV) {
			output.append("NoHeaders: " + noHeaders + "\n");
			output.append("Separator: " + Separator.getSeparatorById(separator).getValueChar() + "\n");

			try {
				output.append("TextRecognitionChar: " + TextRecognitionChar.getTextRecognitionCharById(textRecognitionChar).name() + "\n");
			} catch (Exception e) {
				output.append("TextRecognitionChar: Invalid (\"" + e.getMessage() + "\")\n");
			}
			output.append("DecimalSeparator: " + decimalSeparator + "\n");
		} else if (importDataType == ImportDataType.Excel) {
			output.append("NoHeaders: " + noHeaders + "\n");
		} else if (importDataType == ImportDataType.ODS) {
			output.append("NoHeaders: " + noHeaders + "\n");
		}
		
		try {
			output.append("DateFormat: " + DateFormat.getDateFormatById(dateFormat).getValue() + "\n");
		} catch (Exception e) {
			output.append("DateFormat: Invalid (\"" + e.getMessage() + "\")\n");
		}

		output.append("ImportMode: " + ImportMode.getFromInt(importMode).getMessageKey() + "\n");
		output.append("CheckForDuplicates: " + CheckForDuplicates.getFromInt(checkForDuplicates).name() + "\n");

		output.append("NullValuesAction: " + NullValuesAction.getFromInt(nullValuesAction).name() + "\n");

		output.append("DefaultMailType: " + MailType.getFromInt(defaultMailType).name() + "\n");
		output.append("UpdateAllDuplicates: " + updateAllDuplicates + "\n");
		output.append("ImportProcessActionID: " + importProcessActionID + "\n");
		output.append("ActionForNewRecipients: " + actionForNewRecipients + "\n");
		output.append("MailForReport: " + mailForReport + "\n");
		output.append("MailForError: " + mailForError + "\n");
		
		output.append("GenderMapping: ");
		if (genderMapping != null && genderMapping.size() > 0) {
			output.append("\n\t" + AgnUtils.mapToString(genderMapping).replace("\n", "\n\t") + "\n");
		} else {
			output.append("NONE\n");
		}
		
		output.append("ColumnMapping: \n");
		for (ColumnMapping mapping : columnMapping) {
			output.append("\t" + mapping.getDatabaseColumn() + " = " + mapping.getFileColumn() + (mapping.isEncrypted() ? " encrypted" : "") + "\n");
		}
		
		output.append("KeyColumns: " + StringUtils.join(keyColumns, ", "));
		
		return output.toString();
	}

	@Override
	public ColumnMapping getMappingByDbColumn(String dbColumn) {
		for (ColumnMapping mapping : columnMapping) {
			if (mapping.getDatabaseColumn().equalsIgnoreCase(dbColumn)) {
				return mapping;
			}
		}
		return null;
	}

	@Override
	public List<Integer> getMailinglistIds() {
		return mailinglistIds;
	}

	@Override
	public void setMailinglists(List<Integer> mailinglistIds) {
		this.mailinglistIds = mailinglistIds;
	}

	@Override
	public Set<MediaTypes> getMediatypes() {
		if (mediatypes == null || mediatypes.size() == 0) {
			mediatypes = new HashSet<>();
			mediatypes.add(MediaTypes.EMAIL);
		}
		return mediatypes;
	}

	@Override
	public void setMediatypes(Set<MediaTypes> mediatypes) {
		this.mediatypes = mediatypes;
	}

	@Override
	public String getDatatype() {
		return datatype;
	}

	@Override
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	@Override
	public void setMailinglistsAll(boolean mailinglistsAll) {
		this.mailinglistsAll = mailinglistsAll;
	}

	@Override
	public boolean isMailinglistsAll() {
		return mailinglistsAll;
	}

	@Override
	public void setReportLocale(Locale reportLocale) {
		this.reportLocale = reportLocale;
	}

	@Override
	public Locale getReportLocale() {
		if (reportLocale == null) {
			reportLocale = new Locale("en", "US");
		}
		
		return reportLocale;
	}

	@Override
	public void setReportTimezone(String reportTimezone) {
		this.reportTimezone = reportTimezone;
	}

	@Override
	public String getReportTimezone() {
		if (StringUtils.isBlank(reportTimezone)) {
			reportTimezone = "Europe/Berlin";
		}
		
		return reportTimezone;
	}

	@Override
	public DateTimeFormatter getReportDateTimeFormatter() {
		return AgnUtils.getDateTimeFormatter(getReportTimezone(), getReportLocale());
	}
}
