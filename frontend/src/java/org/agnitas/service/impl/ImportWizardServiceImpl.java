/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.agnitas.beans.impl.CustomerImportStatusImpl;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.service.ImportWizardHelper;
import org.agnitas.service.ImportWizardService;
import org.agnitas.util.Blacklist;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.CsvReader;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.agnitas.util.importvalues.ImportMode;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.web.ComImportWizardForm;

public class ImportWizardServiceImpl implements ImportWizardService {

    /** The logger. */
    private static final transient Logger logger = Logger.getLogger(ImportWizardServiceImpl.class);

    protected BlacklistService blacklistService;

    @Required
    public void setBlacklistService(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @Override
    public ImportWizardHelper createHelper() {
        ImportWizardHelperImpl helper = new ImportWizardHelperImpl();

        helper.setStatus(new CustomerImportStatusImpl());

        return helper;
    }

    @Override
    public void parseFirstLine(ImportWizardHelper helper)  throws ImportWizardContentParseException{
        int colNum = 0;

        if (helper.getFileData().length == 0) {
            throw new ImportWizardContentParseException("error.import.no_file");
        }
        
        if (!(helper.getMode() != ImportMode.ADD.getIntValue() && helper.getMode() != ImportMode.ADD_AND_UPDATE.getIntValue() && helper.getStatus().getKeycolumn().equalsIgnoreCase("customer_id"))) {
            helper.getDbAllColumns().remove("customer_id");
        }

        helper.setCsvAllColumns(new ArrayList<CsvColInfo>());

        try {
            char separator = helper.getStatus().getSeparator();
            Character stringQuote = null;
            if (StringUtils.isNotEmpty(helper.getStatus().getDelimiter())) {
                stringQuote = helper.getStatus().getDelimiter().charAt(0);
            }
            try (CsvReader csvReader = new CsvReader(new ByteArrayInputStream(helper.getFileData()), helper.getStatus().getCharset(), separator, stringQuote)) {
				csvReader.setAlwaysTrim(true);
                List<String> firstlineData = null;
                if ((firstlineData = csvReader.readNextCsvLine()) != null) {
                	String duplicateCsvColumn = CsvReader.checkForDuplicateCsvHeader(firstlineData, false);
        			if (duplicateCsvColumn != null) {
        				throw new ImportWizardContentParseException("error.duplicate.csvcolumn", duplicateCsvColumn);
        			}
        			
                    CsvColInfo aCol = null;
                    List<String> tempList = new ArrayList<>();
                    for (String csvColumnHeader : firstlineData) {
                        //csvColumnHeader = csvColumnHeader.trim().toLowerCase();
                        aCol = new CsvColInfo();
                        aCol.setName(csvColumnHeader);
                        aCol.setActive(false);
                        aCol.setType(CsvColInfo.TYPE_UNKNOWN);

                        // add column to csvAllColumns:
                        if (!tempList.contains(aCol.getName())) {
                            tempList.add(aCol.getName());
                        } else {
                            throw new ImportWizardContentParseException("error.import.column", aCol.getName());
                        }
                        helper.getCsvAllColumns().add(aCol);
                        colNum++;
                        helper.setCsvMaxUsedColumn(colNum);
                    }
                }
            }
        } catch (ImportWizardContentParseException e) {
            logger.error("parseFirstline: " + e.getMessage() + " key: " + e.getErrorMessageKey(), e);
            throw e;
        } catch (Exception e) {
            logger.error("parseFirstline: " + e, e);
            throw new ImportWizardContentParseException("error.import.parse.firstline", e.getMessage());
        }
    }

    @Override
    public void parseContent(ImportWizardHelper helper) throws ImportWizardContentParseException{
        boolean hasGENDER = false;
        boolean hasMAILTYPE = false;
        boolean hasKeyColumn = false;

        helper.setUniqueValues(new HashSet<>());
        helper.setParsedContent(new LinkedList<LinkedList<Object>>());
        helper.setLinesOK(0);

        helper.setDbInsertStatus(0);

        try {
            @SuppressWarnings("unused")
			String charsetCheck = new String(helper.getFileData(), helper.getStatus().getCharset());
        } catch (UnsupportedEncodingException e) {
            throw new ImportWizardContentParseException("error.import.charset",e);
        }

        try {
            Set<String> blacklistEntries = blacklistService.loadBlackList(helper.getCompanyID());
            helper.setBlacklistHelper(new Blacklist());
            for (String blackListEntry : blacklistEntries) {
                helper.getBlacklistHelper().add(blackListEntry, false);
            }
        } catch (Exception e) {
            throw new ImportWizardContentParseException("error.import.blacklist", e);
        }

        // check in the columnMapping for the key column,
        // and eventually for gender and mailtype:
        for (CsvColInfo value : helper.getColumnMapping().values()) {
            if (value.getName().equalsIgnoreCase(ComImportWizardForm.GENDER_KEY) ) {
                hasGENDER = true;
            }

            if (value.getName().equalsIgnoreCase(ComImportWizardForm.MAILTYPE_KEY)) {
                hasMAILTYPE = true;
            }

            if (value.getName().equalsIgnoreCase(helper.getStatus().getKeycolumn())) {
                hasKeyColumn = true;
            }
        }

        if (!hasKeyColumn) {
            throw new ImportWizardContentParseException("error.import.no_keycolumn_mapping");
        }

        if (helper.getMode() == ImportMode.ADD.getIntValue() || helper.getMode() == ImportMode.ADD_AND_UPDATE.getIntValue()) {
            if (!hasGENDER) {
                throw new ImportWizardContentParseException("error.import.column.gender.required");
            }
            if (!hasMAILTYPE) {
                throw new ImportWizardContentParseException("error.import.no_mailtype_mapping");
            }
        }

        try {
            char separator = helper.getStatus().getSeparator();
            Character stringQuote = null;
            if (StringUtils.isNotEmpty(helper.getStatus().getDelimiter())) {
                stringQuote = helper.getStatus().getDelimiter().charAt(0);
            }
            try (CsvReader csvReader = new CsvReader(new ByteArrayInputStream(helper.getFileData()), helper.getStatus().getCharset(), separator, stringQuote)) {
				csvReader.setAlwaysTrim(true);
                // read first csv line again; do not parse (already parsed in parseFirstLine):
                List<String> firstLineData = csvReader.readNextCsvLine();
                String firstLineDataJoined = StringUtils.join(firstLineData, separator);

                // prepare download-files for errors and parsed data
                helper.setErrorData(new HashMap<ImportErrorType, StringBuffer>());
                helper.getErrorData().put(ImportErrorType.DATE_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.EMAIL_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.KEYDOUBLE_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.GENDER_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.MAILTYPE_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.NUMERIC_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.STRUCTURE_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.BLACKLIST_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.setParsedData(new StringBuffer(firstLineDataJoined + '\n'));

                // read the rest of the csv-file:
                helper.setReadlines(0);
                helper.setLinesOK(0);
                List<String> nextLineData;
                // Only read the first block of lines to avoid trouble with heap space
                while ((nextLineData = csvReader.readNextCsvLine()) != null && helper.getLinesOK() < ComImportWizardForm.BLOCK_SIZE) {
                    LinkedList<Object> aLineContent = helper.parseLine(nextLineData, false);
                    if (aLineContent != null) {
                        helper.getParsedContent().add(aLineContent);
                        helper.getParsedData().append(StringUtils.join(nextLineData, separator)).append("\n");
                        helper.setLinesOK(helper.getLinesOK() + 1);
                    }
                    helper.setReadlines(helper.getReadlines() + 1);
                }
            }
        } catch (Exception e) {
            logger.error("parseContent: " + e, e);
            helper.setError(ImportErrorType.STRUCTURE_ERROR, e.getMessage());
        }
    }

    @Override
    public void doParse(ImportWizardHelper helper) throws ImportWizardContentParseException {
        // start at the top of the csv file:
        helper.setPreviewOffset(0);
        // change this to process the column name mapping from --
        // previous action:
        parseContent(helper);
        //dbAllColumns = recipientDao.readDBColumns( companyID);
    }
}
