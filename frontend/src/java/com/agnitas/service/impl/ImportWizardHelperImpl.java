/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.agnitas.beans.ImportStatus;
import com.agnitas.beans.Recipient;
import com.agnitas.emm.core.commons.dto.FileDto;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.messages.Message;
import com.agnitas.service.ImportWizardHelper;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.Blacklist;
import com.agnitas.util.CsvColInfo;
import com.agnitas.util.ImportUtils.ImportErrorType;
import com.agnitas.util.SafeString;
import com.agnitas.util.importvalues.ImportMode;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Holds the parsed data for ImportWizard
 */
public class ImportWizardHelperImpl implements ImportWizardHelper {
	
	private static final Logger logger = LogManager.getLogger(ImportWizardHelperImpl.class);

	private ImportStatus status = null;
	private final GregorianCalendar borderDate = new GregorianCalendar(1000, 0, 1);	// The Date is 01.01.1000

	/**
	 * Holds value of property csvAllColumns.
	 */
	private ArrayList<CsvColInfo> csvAllColumns;

	/**
	 * Holds value of property mailingLists.
	 */
	private Vector<String> mailingLists;
    // TODO leave List<Integer> after import wizard migration successfully tested
	private List<Integer> mailinglists = new ArrayList<>();

	/**
	 * Holds value of property usedColumns.
	 */
	private ArrayList<String> usedColumns;

	/**
	 * Holds value of property parsedContent.
	 */
	private LinkedList<LinkedList<Object>> parsedContent;

	/**
	 * Holds value of property uniqueValues.
	 */
	private Set<String> uniqueValues;

	/**
	 * Holds value of property dbAllColumns.
	 */
	private Map<String, CsvColInfo> dbAllColumns;

	/**
	 * Holds value of property mode.
	 */
	private int mode = 1;

	/**
	 * Holds value of property dateFormat.
	 */
	private String dateFormat = "dd.MM.yyyy HH:mm";

	/**
	 * Holds value of property linesOK.
	 */
	private int linesOK;
	
	/**
	 * Holds value of property dbInsertStatus.
	 */
	private int dbInsertStatus;

	/**
	 * Holds value of property errorData.
	 */
	private Map<ImportErrorType, StringBuffer> errorData = new HashMap<>();

	/**
	 * Holds value of property parsedData.
	 */
	private StringBuffer parsedData;

	/**
	 * Holds value of property downloadName.
	 */
	private String downloadName;

	/**
	 * Holds value of property dbInsertStatusMessagesAndParameters.
	 */
	private List<Message> dbInsertStatusMessagesAndParameters;

	/**
	 * Holds value of property resultMailingListAdded.
	 */
	private Map<MediaTypes, Map<String, String>> resultMailingListAdded;

	/**
	 * Holds value of property columnMapping.
	 */
	private Map<String, CsvColInfo> columnMapping;

	private Blacklist blacklistHelper;

	protected int csvMaxUsedColumn = 0;

	/**
	 * Holds value of property previewOffset.
	 */
	private int previewOffset;
	
	/**
	 * user may choose a default mailing-type in case of no column for mailing-type has been assigned
	 */

	private String manualAssignedMailingType = Integer.toString(Recipient.MAILTYPE_HTML);
	private String manualAssignedGender = Integer.toString(Recipient.GENDER_UNKNOWN);
	
	private boolean mailingTypeMissing = false;
	private boolean genderMissing = false;

	private int companyID;

	private Locale locale;

	private byte[] fileData;
	
    private FileDto file;

    @Override
	public FileDto getFile() {
        return file;
    }

    @Override
	public void setFile(FileDto file) {
        this.file = file;
    }

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getDatasourceID()
	 */
	@Override
	public int getDatasourceID() {
		return status.getDatasourceID();
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setError(java.lang.String, java.lang.String)
	 */
	@Override
	public void setError(ImportErrorType id, String desc) {
		status.addError(id);
		if (!errorData.containsKey(id)) {
			errorData.put(id, new StringBuffer());
		}
		errorData.get(id).append(desc);
        if (desc != null && !desc.endsWith("\n")) {
            errorData.get(id).append("\n");
        }
		status.addError(ImportErrorType.ALL);
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getError(java.lang.String)
	 */
	@Override
	public StringBuffer getError(ImportErrorType id) {
		return errorData.get(id);
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getErrorData()
	 */
	@Override
	public Map<ImportErrorType, StringBuffer> getErrorData() {
		return errorData;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setErrorData(java.util.HashMap)
	 */
	@Override
	public void setErrorData(Map<ImportErrorType, StringBuffer> errorData) {
		this.errorData = errorData;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getStatus()
	 */
	@Override
	public ImportStatus getStatus() {
		return status;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setStatus(com.agnitas.beans.CustomerImportStatus)
	 */
	@Override
	public void setStatus(ImportStatus status) {
		this.status = status;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getCsvAllColumns()
	 */
	@Override
	public ArrayList<CsvColInfo> getCsvAllColumns() {
		return csvAllColumns;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setCsvAllColumns(java.util.ArrayList)
	 */
	@Override
	public void setCsvAllColumns(ArrayList<CsvColInfo> csvAllColumns) {
		this.csvAllColumns = csvAllColumns;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getAllMailingListsNames()
	 */
	@Override
	public Vector<String> getMailingLists() {
		return mailingLists;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setMailingLists(java.util.Vector)
	 */
	@Override
	public void setMailingLists(Vector<String> mailingLists) {
		this.mailingLists = mailingLists;
	}

	@Override
    public List<Integer> getMailinglists() {
        return mailinglists;
    }

    @Override
    public void setMailinglists(List<Integer> mailinglists) {
        this.mailinglists = mailinglists;
    }

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getUsedColumns()
	 */
	@Override
	public ArrayList<String> getUsedColumns() {
		return usedColumns;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setUsedColumns(java.util.ArrayList)
	 */
	@Override
	public void setUsedColumns(ArrayList<String> usedColumns) {
		this.usedColumns = usedColumns;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getParsedContent()
	 */
	@Override
	public LinkedList<LinkedList<Object>> getParsedContent() {
		return parsedContent;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setParsedContent(java.util.LinkedList)
	 */
	@Override
	public void setParsedContent(LinkedList<LinkedList<Object>> parsedContent) {
		this.parsedContent = parsedContent;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getUniqueValues()
	 */
	@Override
	public Set<String> getUniqueValues() {
		return uniqueValues;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setUniqueValues(java.util.HashSet)
	 */
	@Override
	public void setUniqueValues(Set<String> uniqueValues) {
		this.uniqueValues = uniqueValues;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getDbAllColumns()
	 */
	@Override
	public Map<String, CsvColInfo> getDbAllColumns() {
		return dbAllColumns;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setDbAllColumns(java.util.Hashtable)
	 */
	@Override
	public void setDbAllColumns(Map<String, CsvColInfo> dbAllColumns) {
		this.dbAllColumns = dbAllColumns;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getMode()
	 */
	@Override
	public int getMode() {
		return mode;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setMode(int)
	 */
	@Override
	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public void setCsvMaxUsedColumn(int csvMaxUsedColumn) {
		this.csvMaxUsedColumn = csvMaxUsedColumn;
	}

	@Override
	public void setBlacklistHelper(Blacklist blacklistHelper) {
		this.blacklistHelper = blacklistHelper;
	}

	@Override
	public Blacklist getBlacklistHelper() {
		return blacklistHelper;
	}

	/**
	 * This method checks if the given Date is greater than 01.01.1000. Reason is the way java
	 * is treating with DateFormats. A Format like mm.dd.yyyy and a parsed date "01.01.77" will result
	 * in "01.01.77" NOT "01.01.1977" or "01.01.2077" and it will throw NO error!
	 * @param inDate
	 * @return
	 */
	private boolean checkDateHas4Digits(Date inDate) {
		if (inDate.before(borderDate.getTime())) {
			return false;
		}
		return true;
	}

	@Override
	public LinkedList<Object> parseLine(List<String> inputData, boolean addErrors) {
		CsvColInfo aInfo = null;
		LinkedList<Object> valueList = new LinkedList<>();
		int tmp = 0;

		if (dateFormat == null || dateFormat.trim().length() == 0) {
			dateFormat = "dd.MM.yyyy HH:mm";
		}

		SimpleDateFormat aDateFormat = new SimpleDateFormat(dateFormat);
		
		try {
			boolean addedGenderDummyValue = false;
			boolean addedMailtypeDummyValue = false;
			
			if (inputData.size() > csvAllColumns.size()) {
				if (addErrors){
				    setError(ImportErrorType.STRUCTURE_ERROR, StringUtils.join(inputData, " ") + "\n");
	            }
				logger.info("Structure error, missing header info: {}", StringUtils.join(inputData, " "));		// That's not ERROR level. We detected it, we reported it, we skipped that line => write at INFO level!
				return null;
			} else if (inputData.size() != csvMaxUsedColumn) {
	            if (addErrors) {
				    setError(ImportErrorType.STRUCTURE_ERROR, StringUtils.join(inputData, " ") + "\n");
	            }
				logger.error("MaxUsedColumn: {}, {}", csvMaxUsedColumn, inputData.size());
				return null;
			}
			
			for (int columnIndex = 0; columnIndex < inputData.size(); columnIndex++) {
				String aValue = inputData.get(columnIndex);
				CsvColInfo aCsvInfo = csvAllColumns.get(columnIndex);

				// only when mapping for this column is found:
				if (getColumnMapping().containsKey(aCsvInfo.getName())) {
					// get real CsvColInfo object:
					aInfo = getColumnMapping().get(aCsvInfo.getName());
					aValue = aValue.trim();
					// do this before eventual duplicate check on Col Email
					if (aInfo.getName().equalsIgnoreCase("email")) {
						aValue = aValue.toLowerCase();
					}
					if (status.getDoubleCheck() != ImportStatus.DOUBLECHECK_NONE
							&& status.getKeycolumn().equalsIgnoreCase(aInfo.getName())) {
						if (uniqueValues.add(aValue) == false) {
                            if (addErrors) {
							    setError(ImportErrorType.KEYDOUBLE_ERROR, StringUtils.join(inputData, ";") + "\n");
                            }
							logger.info("Duplicate email: {}", StringUtils.join(inputData, ";"));		// That's not ERROR level. We detected it, we reported it, we skipped that line => write at INFO level!
							return null;
						}
					}
					if (aInfo.getName().equalsIgnoreCase("email")) {
						if (aValue.length() == 0) {
                            if(addErrors) {
								setError(ImportErrorType.EMAIL_ERROR, StringUtils.join(inputData, " ") + "\n");
							}
							logger.info("Empty email: {}", StringUtils.join(inputData, " "));			// That's not ERROR level. We detected it, we reported it, we skipped that line => write at INFO level!
							return null;
						}
						if (aValue.indexOf('@') == -1) {
                            if(addErrors) {
								setError(ImportErrorType.EMAIL_ERROR, StringUtils.join(inputData, " ") + "\n");
							}
							logger.info("No @ in email: {}", StringUtils.join(inputData, " "));			// That's not ERROR level. We detected it, we reported it, we skipped that line => write at INFO level!
							return null;
						}

						try {
							if (!AgnUtils.isEmailValid(aValue)) {
								throw new IllegalArgumentException("Invalid email address");
							}
						} catch (Exception e) {
                            if(addErrors) {
							    setError(ImportErrorType.EMAIL_ERROR, StringUtils.join(inputData, " ") + "\n");
                            }
                            if(logger.isInfoEnabled()) {
                            	logger.info("InternetAddress error: {}", StringUtils.join(inputData, " "));			// That's not ERROR level. We detected it, we reported it, we skipped that line => write at INFO level!
                            }
							return null;
						}
						// check blacklist
						if( blacklistHelper.isBlackListed(aValue) != null ) {
                            if(addErrors) {
							    setError(ImportErrorType.BLACKLIST_ERROR, StringUtils.join(inputData, " ") + "\n");
                            }
							if(logger.isInfoEnabled()) {
								logger.info("Blacklisted: {}", StringUtils.join(inputData, " "));			// That's not ERROR level. We detected it, we reported it, we skipped that line => write at INFO level!
							}
							return null;
						}
					} else if (aInfo.getName().equalsIgnoreCase(MAILTYPE_KEY)) {
						try {
							tmp = Integer.parseInt(aValue);
							if (tmp < 0 || tmp > 2) {
								throw new Exception("Invalid mailtype");
							}
						} catch (Exception e) {
							if (aInfo.getName().equalsIgnoreCase(MAILTYPE_KEY)) {
								if (!aValue.equalsIgnoreCase("text")
										&& !aValue.equalsIgnoreCase("txt")
										&& !aValue.equalsIgnoreCase("html")
										&& !aValue.equalsIgnoreCase("offline")) {
                                    if(addErrors) {
									    setError(ImportErrorType.MAILTYPE_ERROR, StringUtils.join(inputData, " ") + "\n");
                                    }
                                    if(logger.isInfoEnabled()) {
                                    	logger.info("Invalid mailtype: {}", StringUtils.join(inputData, " "));		// That's not ERROR level. We detected it, we reported it, we skipped that line => write at INFO level!
                                    }
									return null;
								}
							}
						}
					} else if (aInfo.getName().equalsIgnoreCase(GENDER_KEY)) {
						try {
							tmp = Integer.parseInt(aValue);
							if (tmp < 0 || tmp > 5) {
								throw new IllegalArgumentException("Invalid gender");
							}
						} catch (Exception e) {
							if (aInfo.getName().equalsIgnoreCase(GENDER_KEY)) {
								if (!aValue.equalsIgnoreCase("Herr")
										&& !aValue.equalsIgnoreCase("Herrn")
										&& !aValue.equalsIgnoreCase("m")
										&& !aValue.equalsIgnoreCase("Frau")
										&& !aValue.equalsIgnoreCase("w")) {
                                    if(addErrors) {
                                        setError(ImportErrorType.GENDER_ERROR, StringUtils.join(inputData, " ") + ";" + SafeString.getLocaleString("import.error.GenderFormat",locale) + aInfo.getName() + "\n");
                                    }
									logger.info("Invalid gender: {}", aValue); // That's not ERROR level. We detected it, we reported it, we skipped that line => write at INFO level!
									return null;
								}
							}
						}
					}
					if (aInfo != null && aInfo.isActive()) {
						if (aValue.length() == 0) { // is null value
							valueList.add(null);
						} else {
							switch (aInfo.getType()) {
								case CsvColInfo.TYPE_CHAR:
									valueList.add(SafeString.cutByteLength(aValue,aInfo.getLength()));
									break;
	
								case CsvColInfo.TYPE_NUMERIC:
									try {
										valueList.add(Double.valueOf(aValue));
									} catch (Exception e) {
										if (aInfo.getName().equalsIgnoreCase(GENDER_KEY) && !columnMapping.containsKey(GENDER_KEY+"_dummy")) {
											if (aValue.equalsIgnoreCase("Herr")	|| aValue.equalsIgnoreCase("Herrn") || aValue.equalsIgnoreCase("m")) {
												valueList.add(Double.valueOf(0));
											} else if (aValue.equalsIgnoreCase("Frau") || aValue.equalsIgnoreCase("w")) {
												valueList.add(Double.valueOf(1));
											} else {
												valueList.add(Double.valueOf(2));
											}
										} else if (aInfo.getName().equalsIgnoreCase(MAILTYPE_KEY) && !columnMapping.containsKey(MAILTYPE_KEY+"_dummy")) {
											if (aValue.equalsIgnoreCase("text")	|| aValue.equalsIgnoreCase("txt")) {
												valueList.add(Double.valueOf(0));
											} else if (aValue.equalsIgnoreCase("html")) {
												valueList.add(Double.valueOf(1));
											} else if (aValue.equalsIgnoreCase("offline")) {
												valueList.add(Double.valueOf(2));
											}
										} else {
	                                        if(addErrors) {
	                                            setError(ImportErrorType.NUMERIC_ERROR,	StringUtils.join(inputData, " ") + ";"+ SafeString.getLocaleString("import.error.NumberFormat",locale)
	                                                            + aInfo.getName() + "\n");
	                                        }
	                                        if(logger.isInfoEnabled()) {
	                                        	logger.info("Numberformat error: {}", StringUtils.join(inputData, " "));			// That's not ERROR level. We detected it, we reported it, we skipped that line => write at INFO level!
	                                        }
											return null;
										}
									}
									break;
	
								case CsvColInfo.TYPE_DATE:
									try {
										Date tmpDate = aDateFormat.parse(aValue);
										if (!checkDateHas4Digits(tmpDate)) {
											throw new Exception("Incorrect Date Format. The Date must have 4 Digits.");
										}
										valueList.add(tmpDate);
									} catch (Exception e) {
	                                    if(addErrors) {
	                                        setError(ImportErrorType.DATE_ERROR, StringUtils.join(inputData, " ") + ";" + SafeString.getLocaleString("import.error.DateFormat", locale) + aInfo.getName() + "\n");
	                                    }
	                                    if(logger.isInfoEnabled()) {
	                                    	logger.info("Dateformat error: {}", StringUtils.join(inputData, " "));						// That's not ERROR level. We detected it, we reported it, we skipped that line => write at INFO level!
	                                    }
										return null;
									}
									break;
									
								default:
									break;
							}
						}
					}
				}
			}
			if (getColumnMapping().containsKey(GENDER_KEY+"_dummy" ) && !addedGenderDummyValue ) {
				valueList.add(getManualAssignedGender());
				addedGenderDummyValue = true;
			}
			if (getColumnMapping().containsKey(MAILTYPE_KEY+"_dummy" ) && !addedMailtypeDummyValue ) {
				valueList.add(getManualAssignedMailingType());
				addedMailtypeDummyValue = true;
			}
		} catch (Exception e) {
            if(addErrors){
			    setError(ImportErrorType.STRUCTURE_ERROR, StringUtils.join(inputData, " ") + "\n");
            }
			logger.error("parseLine: " + e, e);
			return null;
		}
		
		return valueList;
	}


	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#parseLine(java.lang.String)
	 */
	@Override
    public LinkedList<Object> parseLine(List<String> inputData) {
        return parseLine(inputData, true);
    }

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#mapColumns(java.util.Map)
	 */
	@Override
	public void mapColumns(Map<String, String> mapParameters) {
		int i = 1;
		CsvColInfo aCol = null;
		// initialize columnMapping hashtable:
		columnMapping = new HashMap<>();

		for (i = 1; i < (csvAllColumns.size() + 1); i++) {
			String pName = "map_" + i;
			if (mapParameters.get(pName) != null) {
				aCol = csvAllColumns.get(i - 1);
				if (mapParameters.get(pName) == null) {
					continue;
				}

				if (!"NOOP".equals(mapParameters.get(pName))) {
					CsvColInfo aInfo = dbAllColumns.get(mapParameters.get(pName));
					columnMapping.put(aCol.getName(), aInfo);

					aInfo.setActive(true);
					// write db column (set active now) back to dbAllColums:
					dbAllColumns.put(mapParameters.get(pName), aInfo);

					// adjust & write back csvAllColumns hashtable entry:
					aCol.setActive(true);
					aCol.setLength(aInfo.getLength());
					aCol.setType(aInfo.getType());
					csvAllColumns.set(i - 1, aCol);
				} else {
					aCol.setActive(false);
				}
			}
		}
		
		if (mode == ImportMode.ADD.getIntValue() || mode == ImportMode.ADD_AND_UPDATE.getIntValue()) {
			if (columnIsMapped(GENDER_KEY)) {
				if (isGenderMissing()) {
					// remove the former added dummy field, because the user corrected the mapping now
					columnMapping.remove(GENDER_KEY + "_dummy");
				}
				setGenderMissing(false);
			} else {
				CsvColInfo genderCol = new CsvColInfo();
				genderCol.setName(GENDER_KEY);
				genderCol.setType(CsvColInfo.TYPE_CHAR);
				columnMapping.put(GENDER_KEY + "_dummy", genderCol);
				setGenderMissing(true);
			}

			if (columnIsMapped(MAILTYPE_KEY)) {
				if (isMailingTypeMissing()) {
					// remove the former added dummy field, because the user corrected the mapping now
					columnMapping.remove(MAILTYPE_KEY + "_dummy");
				}
				setMailingTypeMissing(false);
			} else {
				CsvColInfo mailtypeCol = new CsvColInfo();
				mailtypeCol.setName(MAILTYPE_KEY);
				mailtypeCol.setType(CsvColInfo.TYPE_CHAR);
				columnMapping.put(MAILTYPE_KEY + "_dummy", mailtypeCol);
				setMailingTypeMissing(true);
			}
		}
		
		// check if the mailtype/ gender is allready in columnmapping , if we find only a dummy -> add a dummy to csvAllColumns too
		if (getColumnMapping().containsKey(GENDER_KEY + "_dummy") && !csvAllColumnsContainsMapping(GENDER_KEY + "_dummy")) {
			CsvColInfo mailtypeDummy = new CsvColInfo();
			mailtypeDummy.setName(GENDER_KEY+"_dummy");
			mailtypeDummy.setActive(true);
			mailtypeDummy.setType(CsvColInfo.TYPE_CHAR);
			csvAllColumns.add(mailtypeDummy);
		}
		
		if (getColumnMapping().containsKey(MAILTYPE_KEY + "_dummy") && !csvAllColumnsContainsMapping(MAILTYPE_KEY + "_dummy")) {
			CsvColInfo mailtypeDummy = new CsvColInfo();
			mailtypeDummy.setName(MAILTYPE_KEY+"_dummy");
			mailtypeDummy.setActive(true);
			mailtypeDummy.setType(CsvColInfo.TYPE_CHAR);
			csvAllColumns.add(mailtypeDummy);
		}
	}

	private boolean columnIsMapped(String key) {
		for (CsvColInfo value : columnMapping.values()) {
			if(key.equalsIgnoreCase(value.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
    public void clearDummyColumnsMappings(){
        Iterator<CsvColInfo> csvColInfoIterator = csvAllColumns.iterator();
        while(csvColInfoIterator.hasNext()){
            CsvColInfo colInfo = csvColInfoIterator.next();
            String name = colInfo.getName();
            if(isGenderMissing() && (GENDER_KEY+"_dummy").equals(name)){
                csvColInfoIterator.remove();
                continue;
            }
            if(isMailingTypeMissing() && (MAILTYPE_KEY+"_dummy").equals(name)){
                csvColInfoIterator.remove();
            }
        }
    }

    private boolean csvAllColumnsContainsMapping(String mappingKey){
        for(CsvColInfo columnInfo : csvAllColumns){
            String columnInfoName = columnInfo.getName();
            if(mappingKey.equals(columnInfoName)){
                return true;
            }
        }
        return false;
    }

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getLinesOK()
	 */
	@Override
	public int getLinesOK() {
		return linesOK;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setLinesOK(int)
	 */
	@Override
	public void setLinesOK(int linesOK) {
		this.linesOK = linesOK;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getDbInsertStatus()
	 */
	@Override
	public int getDbInsertStatus() {
		return dbInsertStatus;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setDbInsertStatus(int)
	 */
	@Override
	public void setDbInsertStatus(int dbInsertStatus) {
		this.dbInsertStatus = dbInsertStatus;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getParsedData()
	 */
	@Override
	public StringBuffer getParsedData() {
		return parsedData;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setParsedData(java.lang.StringBuffer)
	 */
	@Override
	public void setParsedData(StringBuffer parsedData) {
		this.parsedData = parsedData;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getDownloadName()
	 */
	@Override
	public String getDownloadName() {
		return downloadName;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setDownloadName(java.lang.String)
	 */
	@Override
	public void setDownloadName(String downloadName) {
		this.downloadName = downloadName;
	}
	
	@Override
	public void clearDbInsertStatusMessagesAndParameters() {
		if (dbInsertStatusMessagesAndParameters == null) {
			dbInsertStatusMessagesAndParameters = new LinkedList<>();
		}
		dbInsertStatusMessagesAndParameters.clear();
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getDbInsertStatusMessagesAndParameters()
	 */
	@Override
	public List<Message> getDbInsertStatusMessagesAndParameters() {
		// Avoid concurrent modification problems while import is still running
		return new LinkedList<>(dbInsertStatusMessagesAndParameters);
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#addDbInsertStatusMessageAndParameters(java.lang.String)
	 */
	@Override
	public void addDbInsertStatusMessageAndParameters(String messageKey, Object... additionalParameters) {
		if (dbInsertStatusMessagesAndParameters == null) {
			dbInsertStatusMessagesAndParameters = new LinkedList<>();
		}
		dbInsertStatusMessagesAndParameters.add(Message.of(messageKey, additionalParameters));
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getResultMailingListAdded()
	 */
	@Override
	public Map<MediaTypes, Map<String, String>> getResultMailingListAdded() {
		return resultMailingListAdded;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setResultMailingListAdded(java.util.Hashtable)
	 */
	@Override
	public void setResultMailingListAdded(Map<MediaTypes, Map<String, String>> resultMailingListAdded) {
		this.resultMailingListAdded = resultMailingListAdded;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getPreviewOffset()
	 */
	@Override
	public int getPreviewOffset() {
		return previewOffset;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setPreviewOffset(int)
	 */
	@Override
	public void setPreviewOffset(int previewOffset) {
		this.previewOffset = previewOffset;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getDateFormat()
	 */
	@Override
	public String getDateFormat() {
		return dateFormat;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setDateFormat(java.lang.String)
	 */
	@Override
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getColumnMapping()
	 */
	@Override
	public Map<String, CsvColInfo> getColumnMapping() {
		return columnMapping;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setColumnMapping(java.util.Hashtable)
	 */
	@Override
	public void setColumnMapping(Map<String, CsvColInfo> columnMapping) {
		this.columnMapping = columnMapping;
	}

	private String errorId = null;

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getErrorId()
	 */
	@Override
	public String getErrorId() {
		return errorId;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setErrorId(java.lang.String)
	 */
	@Override
	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getManualAssignedMailingType()
	 */
	@Override
	public String getManualAssignedMailingType() {
		return manualAssignedMailingType;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setManualAssignedMailingType(java.lang.String)
	 */
	@Override
	public void setManualAssignedMailingType(String manualAssignedMailingType) {
		this.manualAssignedMailingType = manualAssignedMailingType;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getManualAssignedGender()
	 */
	@Override
	public String getManualAssignedGender() {
		return manualAssignedGender;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setManualAssignedGender(java.lang.String)
	 */
	@Override
	public void setManualAssignedGender(String manualAssignedGender) {
		this.manualAssignedGender = manualAssignedGender;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#isMailingTypeMissing()
	 */
	@Override
	public boolean isMailingTypeMissing() {
		return mailingTypeMissing;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setMailingTypeMissing(boolean)
	 */
	@Override
	public void setMailingTypeMissing(boolean mailingTypeMissing) {
		this.mailingTypeMissing = mailingTypeMissing;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#isGenderMissing()
	 */
	@Override
	public boolean isGenderMissing() {
		return genderMissing;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setGenderMissing(boolean)
	 */
	@Override
	public void setGenderMissing(boolean genderMissing) {
		this.genderMissing = genderMissing;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getLocale()
	 */
	@Override
	public Locale getLocale() {
		return locale;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setLocale(java.util.Locale)
	 */
	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getFileData()
	 */
	@Override
	public byte[] getFileData() {
		return fileData;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setFileData(byte[])
	 */
	@Override
	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getCompanyID()
	 */
	@Override
	public int getCompanyID() {
		return companyID;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#setCompanyID(int)
	 */
	@Override
	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.service.impl.ImportWizardHelper#getKeyColumn()
	 */
	@Override
	public String getKeyColumn() {
		return status.getKeycolumn();
	}

}
