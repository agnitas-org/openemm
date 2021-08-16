/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.MapUtils;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.CheckForDuplicates;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.MailType;
import org.agnitas.util.importvalues.NullValuesAction;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;

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
    protected Map<String, Integer> genderMapping = Collections.synchronizedMap(new HashMap<String, Integer>());
    protected List<ColumnMapping> columnMapping = Collections.synchronizedList(new ArrayList<ColumnMapping>());
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
    private boolean zipped = false;
    private String zipPassword = null;
    private boolean autoMapping = false;
    private List<Integer> mailinglistIds = new ArrayList<>();
    private Set<MediaTypes> mediatypes = new HashSet<>();
    private String datatype = "CSV";
	private boolean mailinglistsAll;
    
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
	public void setCompanyId( @VelocityCheck int companyId) {
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
    	keyColumns.add(keyColumn.toLowerCase());
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
	public boolean isZipped() {
		return zipped;
	}
	@Override

	public void setZipped(boolean zipped) {
		this.zipped = zipped;
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
		try {
			output.append("Charset: " + Charset.getCharsetById(charset).getCharsetName() + "\n");
		} catch (Exception e) {
			output.append("Charset: Invalid (\"" + e.getMessage() + "\")\n");
		}
		output.append("Zipped: " + zipped + "\n");
		output.append("EncryptedZip: " + (zipPassword != null) + "\n");
		output.append("AutoMapping: " + autoMapping + "\n");
		
		if ("CSV".equalsIgnoreCase(datatype)) {
			output.append("NoHeaders: " + noHeaders + "\n");
			try {
				output.append("Separator: " + Separator.getSeparatorById(separator).getValueChar() + "\n");
			} catch (Exception e) {
				output.append("Separator: Invalid (\"" + e.getMessage() + "\")\n");
			}
			try {
				output.append("TextRecognitionChar: " + TextRecognitionChar.getTextRecognitionCharById(textRecognitionChar).name() + "\n");
			} catch (Exception e) {
				output.append("TextRecognitionChar: Invalid (\"" + e.getMessage() + "\")\n");
			}
			output.append("DecimalSeparator: " + decimalSeparator + "\n");
		} else if ("JSON".equalsIgnoreCase(datatype)) {
			output.append("Json data import: true\n");
		} else {
			throw new RuntimeException("Invalid datatype: " + datatype);
		}
		
		try {
			output.append("DateFormat: " + DateFormat.getDateFormatById(dateFormat).getValue() + "\n");
		} catch (Exception e) {
			output.append("DateFormat: Invalid (\"" + e.getMessage() + "\")\n");
		}
		try {
			output.append("ImportMode: " + ImportMode.getFromInt(importMode).getMessageKey() + "\n");
		} catch (Exception e) {
			output.append("ImportMode: Invalid (\"" + e.getMessage() + "\")\n");
		}
		try {
			output.append("CheckForDuplicates: " + CheckForDuplicates.getFromInt(checkForDuplicates).name() + "\n");
		} catch (Exception e) {
			output.append("CheckForDuplicates: Invalid (\"" + e.getMessage() + "\")\n");
		}
		try {
			output.append("NullValuesAction: " + NullValuesAction.getFromInt(nullValuesAction).name() + "\n");
		} catch (Exception e) {
			output.append("NullValuesAction: Invalid (\"" + e.getMessage() + "\")\n");
		}
		try {
			output.append("DefaultMailType: " + MailType.getFromInt(defaultMailType).name() + "\n");
		} catch (Exception e) {
			output.append("DefaultMailType: Invalid (\"" + e.getMessage() + "\")\n");
		}
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
}
