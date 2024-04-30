/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ImportProcessAction;
import com.agnitas.dao.ImportProcessActionDao;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import org.agnitas.actions.EmmAction;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.CheckForDuplicates;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.MailType;
import org.agnitas.util.importvalues.NullValuesAction;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.agnitas.util.UserActivityUtil.addChangedFieldLog;

@Component
public class ImportProfileChangesDetector {

    private static final String NONE = "none";
    private static final String UNKNOWN_ACTION = "<unknown action>";

    private final EmmActionDao emmActionDao;
    private final ImportProcessActionDao importProcessActionDao;

    public ImportProfileChangesDetector(@Autowired(required = false) EmmActionDao emmActionDao, @Autowired(required = false) ImportProcessActionDao importProcessActionDao) {
        this.emmActionDao = emmActionDao;
        this.importProcessActionDao = importProcessActionDao;
    }

    public StringBuilder detectChanges(ImportProfile oldImport, ImportProfile newImport, Admin admin) {
        StringBuilder builder = new StringBuilder();
        builder.append(addChangedFieldLog("Import name", newImport.getName(), oldImport.getName()));

        builder.append(getSeparatorChangesLog(oldImport, newImport))
                .append(getCharsetChangesLog(oldImport, newImport))
                .append(getRecognitionCharacterChangesLog(oldImport, newImport))
                .append(getDateFormatChangesLog(oldImport, newImport));

        builder.append(addChangedFieldLog("Decimal separator",
                String.valueOf(newImport.getDecimalSeparator()),
                String.valueOf(oldImport.getDecimalSeparator())));
        builder.append(addChangedFieldLog("No csv headers", newImport.isNoHeaders(), oldImport.isNoHeaders()));
        builder.append(addChangedFieldLog("Zip password", newImport.getZipPassword(), oldImport.getZipPassword()));

        builder.append(getImportModeChangesLog(oldImport, newImport))
                .append(getNullValuesActionChangesLog(oldImport, newImport));

        builder.append(addChangedFieldLog("First key column", newImport.getFirstKeyColumn(), oldImport.getFirstKeyColumn()));

        builder.append(getDuplicatesCheckChangesLog(oldImport, newImport))
                .append(getDefaultMailingTypeChangesLog(oldImport, newImport));

        builder.append(getPreImportActionLog(oldImport.getImportProcessActionID(), newImport.getImportProcessActionID(), admin.getCompanyID()));
        builder.append(addChangedFieldLog("Action for new recipients",
                getActionForNewRecipientsName(newImport.getActionForNewRecipients(), admin.getCompanyID()),
                getActionForNewRecipientsName(oldImport.getActionForNewRecipients(), admin.getCompanyID())));
        builder.append(addChangedFieldLog("Report mail", newImport.getMailForReport(), oldImport.getMailForReport()))
                .append(addChangedFieldLog("Error mail", newImport.getMailForError(), oldImport.getMailForError()))
                .append(addChangedFieldLog("Handling of duplicates",
                        buildUpdateRecipientMsg(newImport.getUpdateAllDuplicates()),
                        buildUpdateRecipientMsg(oldImport.getUpdateAllDuplicates())))
                .append(addChangedFieldLog("Automatic mapping", newImport.isAutoMapping(), oldImport.isAutoMapping()))
                .append(addChangedFieldLog("Genders", getGendersValue(newImport.getGenderMapping()), getGendersValue(oldImport.getGenderMapping())));

        return builder;
    }

    private String getSeparatorChangesLog(ImportProfile oldImport, ImportProfile newImport) {
        try {
            String newSeparator = String.valueOf(Separator.getSeparatorById(newImport.getSeparator()).getValueChar());
            String oldSeparator = String.valueOf(Separator.getSeparatorById(oldImport.getSeparator()).getValueChar());
            return addChangedFieldLog("Separator", newSeparator, oldSeparator);
        } catch (Exception e) {
            return "";
        }
    }

    private String getCharsetChangesLog(ImportProfile oldImport, ImportProfile newImport) {
        try {
            String newCharset = Charset.getCharsetById(newImport.getCharset()).getCharsetName();
            String oldCharset = Charset.getCharsetById(oldImport.getCharset()).getCharsetName();

            return addChangedFieldLog("Charset", newCharset, oldCharset);
        } catch (Exception e) {
            return "";
        }
    }

    private String getRecognitionCharacterChangesLog(ImportProfile oldImport, ImportProfile newImport) {
        try {
            String newTextRecognitionChar = TextRecognitionChar.getTextRecognitionCharById(newImport.getTextRecognitionChar()).getValueString();
            String oldTextRecognitionChar = TextRecognitionChar.getTextRecognitionCharById(oldImport.getTextRecognitionChar()).getValueString();
            return addChangedFieldLog("Recognition character", newTextRecognitionChar, oldTextRecognitionChar);
        } catch (Exception e) {
            return "";
        }
    }

    private String getDateFormatChangesLog(ImportProfile oldImport, ImportProfile newImport) {
        try {
            String newDateFormat = DateFormat.getDateFormatById(newImport.getDateFormat()).getValue();
            String oldDateFormat = DateFormat.getDateFormatById(oldImport.getDateFormat()).getValue();

            return addChangedFieldLog("Date format", newDateFormat, oldDateFormat);
        } catch (Exception e) {
            return "";
        }
    }

    private String getImportModeChangesLog(ImportProfile oldImport, ImportProfile newImport) {
        try {
            String newImportMode = ImportMode.getFromInt(newImport.getImportMode()).getMessageKey();
            String oldImportMode = ImportMode.getFromInt(oldImport.getImportMode()).getMessageKey();
            return addChangedFieldLog("Mode", newImportMode, oldImportMode);
        } catch (Exception e) {
            return "";
        }
    }

    private String getNullValuesActionChangesLog(ImportProfile oldImport, ImportProfile newImport) {
        try {
            String newNullValueAction = NullValuesAction.getFromInt(newImport.getNullValuesAction()).getMessageKey();
            String oldNullValueAction = NullValuesAction.getFromInt(oldImport.getNullValuesAction()).getMessageKey();
            return addChangedFieldLog("Null values action", newNullValueAction, oldNullValueAction);
        } catch (Exception e) {
            return "";
        }
    }

    private String getDuplicatesCheckChangesLog(ImportProfile oldImport, ImportProfile newImport) {
        try {
            String newDuplicatesCheck = CheckForDuplicates.getFromInt(newImport.getCheckForDuplicates()).getMessageKey();
            String oldDuplicatesCheck = CheckForDuplicates.getFromInt(oldImport.getCheckForDuplicates()).getMessageKey();
            return addChangedFieldLog("Duplicates check", newDuplicatesCheck, oldDuplicatesCheck);
        } catch (Exception e) {
            return "";
        }
    }

    private String getDefaultMailingTypeChangesLog(ImportProfile oldImport, ImportProfile newImport) {
        try {
            String newDefaultMailingType = MailType.getFromInt(newImport.getDefaultMailType()).getMessageKey();
            String oldDefaultMailingType = MailType.getFromInt(oldImport.getDefaultMailType()).getMessageKey();
            return addChangedFieldLog("Default mailing type", newDefaultMailingType, oldDefaultMailingType);
        } catch (Exception e) {
            return "";
        }
    }

    private String getGendersValue(Map<?, ?> gendersMapping) {
        return gendersMapping.size() != 0 ? gendersMapping.toString() : "\"no mapping\"";
    }

    private String buildUpdateRecipientMsg(boolean value) {
        return value ? "update all recipients" : "update only one recipient";
    }

    private String getActionForNewRecipientsName(int actionId, int companyId) {
        if (actionId == 0) {
            return NONE;
        }

        List<EmmAction> emmActions = emmActionDao.getEmmActionsByOperationType(companyId, false, ActionOperationType.SUBSCRIBE_CUSTOMER, ActionOperationType.SEND_MAILING);
        EmmAction emmAction = emmActions.stream()
                .filter(action -> action.getId() == actionId)
                .findFirst()
                .orElse(null);

        return emmAction != null ? emmAction.getShortname() : UNKNOWN_ACTION;
    }

    private String getPreImportActionLog(int oldId, int newId, int companyId) {
        List<ImportProcessAction> allActions = importProcessActionDao.getAvailableImportProcessActions(companyId);

        return addChangedFieldLog(
                "Pre import action",
                getImportProcessActionName(newId, allActions),
                getImportProcessActionName(oldId, allActions)
        );
    }

    private String getImportProcessActionName(int actionId, List<ImportProcessAction> allActions) {
        if (actionId == 0) {
            return NONE;
        }

        ImportProcessAction importAction = allActions.stream()
                .filter(action -> action.getImportactionID() == actionId)
                .findFirst()
                .orElse(null);

        return importAction != null ? importAction.getName() : UNKNOWN_ACTION;
    }
}
