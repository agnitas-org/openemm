/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.form;

import com.agnitas.emm.core.import_profile.bean.ImportProfileColumnMapping;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.util.importvalues.ImportMode;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportProfileForm {

    private int id;
    private String name;
    private String datatype = "CSV";
    private char decimalSeparator = '.';
    private int separator;
    private boolean noHeaders;
    private String zipPassword;
    private String firstKeyColumn;
    /*** TODO: replace usages with {@link shouldCheckForDuplicates} after EMMGUI-714 will be finished and old design will be removed */
    private int checkForDuplicates;
    private boolean updateAllDuplicates = true;
    private int defaultMailType;
    private int importProcessActionID;
    private String mailForReport;
    private String mailForError;
    private String reportLocale;
    private String reportTimezone = "Europe/Berlin";
    private boolean autoMapping;
    private int actionForNewRecipients;
    private int textRecognitionChar;
    private int charset;
    private int dateFormat;
    private int nullValuesAction;
    private boolean mailinglistsAll;
    private boolean shouldCheckForDuplicates;
    private Set<Integer> selectedMailinglists = new HashSet<>();
    private Set<MediaTypes> selectedMediatypes = new HashSet<>();
    private int importMode = ImportMode.ADD_AND_UPDATE.getIntValue();
    private final Map<Integer, String> genderMappings = new HashMap<>();
    private final Map<Integer, String> mailinglists = new HashMap<>(); // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    private final Map<Integer, String> mediatypes = new HashMap<>(); // TODO: remove after EMMGUI-714 will be finished and old design will be removed

    private List<ImportProfileColumnMapping> columnsMappings = new ArrayList<>();

    private MultipartFile uploadFile;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public int getSeparator() {
        return separator;
    }

    public void setSeparator(int separator) {
        this.separator = separator;
    }

    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    public void setDecimalSeparator(char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    public boolean isNoHeaders() {
        return noHeaders;
    }

    public void setNoHeaders(boolean noHeaders) {
        this.noHeaders = noHeaders;
    }

    public String getZipPassword() {
        return zipPassword;
    }

    public void setZipPassword(String zipPassword) {
        this.zipPassword = zipPassword;
    }

    public int getImportMode() {
        return importMode;
    }

    public void setImportMode(int importMode) {
        this.importMode = importMode;
    }

    public String getFirstKeyColumn() {
        return firstKeyColumn;
    }

    public void setFirstKeyColumn(String firstKeyColumn) {
        this.firstKeyColumn = firstKeyColumn;
    }

    public int getCheckForDuplicates() {
        return checkForDuplicates;
    }

    public void setCheckForDuplicates(int checkForDuplicates) {
        this.checkForDuplicates = checkForDuplicates;
    }

    public boolean isUpdateAllDuplicates() {
        return updateAllDuplicates;
    }

    public void setUpdateAllDuplicates(boolean updateAllDuplicates) {
        this.updateAllDuplicates = updateAllDuplicates;
    }

    public int getDefaultMailType() {
        return defaultMailType;
    }

    public void setDefaultMailType(int defaultMailType) {
        this.defaultMailType = defaultMailType;
    }

    public int getImportProcessActionID() {
        return importProcessActionID;
    }

    public void setImportProcessActionID(int importProcessActionID) {
        this.importProcessActionID = importProcessActionID;
    }

    public String getMailForReport() {
        return mailForReport;
    }

    public void setMailForReport(String mailForReport) {
        this.mailForReport = mailForReport;
    }

    public String getMailForError() {
        return mailForError;
    }

    public void setMailForError(String mailForError) {
        this.mailForError = mailForError;
    }

    public String getReportLocale() {
        return reportLocale;
    }

    public void setReportLocale(String reportLocale) {
        this.reportLocale = reportLocale;
    }

    public String getReportTimezone() {
        return reportTimezone;
    }

    public void setReportTimezone(String reportTimezone) {
        this.reportTimezone = reportTimezone;
    }

    public boolean isAutoMapping() {
        return autoMapping;
    }

    public void setAutoMapping(boolean autoMapping) {
        this.autoMapping = autoMapping;
    }

    public int getActionForNewRecipients() {
        return actionForNewRecipients;
    }

    public void setActionForNewRecipients(int actionForNewRecipients) {
        this.actionForNewRecipients = actionForNewRecipients;
    }

    public void setMailinglists(Map<Integer, String> map) {
        this.mailinglists.clear();
        this.mailinglists.putAll(map);
    }

    public Map<Integer, String> getMailinglist() {
        return mailinglists;
    }

    public void setMediatypes(Map<Integer, String> map) {
        this.mediatypes.clear();
        this.mediatypes.putAll(map);
    }

    public Map<Integer, String> getMediatype() {
        return mediatypes;
    }

    public int getCharset() {
        return charset;
    }

    public void setCharset(int charset) {
        this.charset = charset;
    }

    public int getTextRecognitionChar() {
        return textRecognitionChar;
    }

    public void setTextRecognitionChar(int textRecognitionChar) {
        this.textRecognitionChar = textRecognitionChar;
    }

    public int getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(int dateFormat) {
        this.dateFormat = dateFormat;
    }

    public int getNullValuesAction() {
        return nullValuesAction;
    }

    public void setNullValuesAction(int nullValuesAction) {
        this.nullValuesAction = nullValuesAction;
    }

    public boolean isMailinglistsAll() {
        return mailinglistsAll;
    }

    public void setMailinglistsAll(boolean mailinglistsAll) {
        this.mailinglistsAll = mailinglistsAll;
    }

    public Map<Integer, String> getGenderMapping() {
        return genderMappings;
    }

    public boolean isShouldCheckForDuplicates() {
        return shouldCheckForDuplicates;
    }

    public void setShouldCheckForDuplicates(boolean shouldCheckForDuplicates) {
        this.shouldCheckForDuplicates = shouldCheckForDuplicates;
    }

    public Set<Integer> getSelectedMailinglists() {
        return selectedMailinglists;
    }

    public void setSelectedMailinglists(Set<Integer> selectedMailinglists) {
        this.selectedMailinglists = selectedMailinglists;
    }

    public Set<MediaTypes> getSelectedMediatypes() {
        return selectedMediatypes;
    }

    public void setSelectedMediatypes(Set<MediaTypes> selectedMediatypes) {
        this.selectedMediatypes = selectedMediatypes;
    }

    public List<ImportProfileColumnMapping> getColumnsMappings() {
        return columnsMappings;
    }

    public MultipartFile getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(MultipartFile uploadFile) {
        this.uploadFile = uploadFile;
    }
}
