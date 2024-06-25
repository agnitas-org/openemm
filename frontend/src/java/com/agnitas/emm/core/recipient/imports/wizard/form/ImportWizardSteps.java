/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.imports.wizard.form;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.agnitas.messages.Message;
import org.agnitas.service.ImportWizardHelper;
import org.agnitas.util.importvalues.ImportMode;
import org.apache.commons.lang3.StringUtils;

public class ImportWizardSteps {
    
    public enum Step {
        FILE("fileStepView", "file.action"),
        MODE("modeStepView", "mode.action"),
        MAPPING("mappingStepView", "mapping.action"),
        VERIFY_MISSING_FIELDS("verifyMissingFieldsStepView", "verifyMissingFields.action"),
        VERIFY("verifyStepView", "verify.action"),
        PRESCAN("preScanStepView", "preScan.action"),
        MAILING_LISTS("mailinglistsStepView", "mailinglists.action");
        
        private final String controllerMethodName;
        private final String controllerEndpointName;
        
        Step(String controllerMethodName, String controllerEndpointName) {
       		this.controllerMethodName = controllerMethodName;
       		this.controllerEndpointName = controllerEndpointName;
       	}

        public String getControllerMethodName() {
            return controllerMethodName;
        }

        public String getControllerEndpointName() {
            return controllerEndpointName;
        }

        public static Step fromControllerMethodName(String controllerMethodName) {
            return Arrays.stream(values())
                    .filter(step -> StringUtils.equals(controllerMethodName, step.getControllerMethodName()))
                    .findFirst().orElse(FILE);
       	}
    }

    private Step currentStep = Step.FILE;
    private ImportWizardFileStepForm fileStep = new ImportWizardFileStepForm();
    private ImportWizardModeStepForm modeStep = new ImportWizardModeStepForm();
    private ImportWizardMappingStepForm mappingStep = new ImportWizardMappingStepForm();
    private boolean importRunning;
    private String importUID;

    protected ImportWizardHelper helper;
    
    public void nextStep(Step step) {
        currentStep = step;
    }
    
    public ImportWizardHelper getHelper() {
        return helper;
    }

    public void setHelper(ImportWizardHelper helper) {
        this.helper = helper;
    }

    public Step getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Step currentStep) {
        this.currentStep = currentStep;
    }

    public ImportWizardFileStepForm getFileStep() {
        return fileStep;
    }

    public void setFileStep(ImportWizardFileStepForm fileStep) {
        this.fileStep = fileStep;
    }

    public ImportWizardModeStepForm getModeStep() {
        return modeStep;
    }

    public void setModeStep(ImportWizardModeStepForm modeStep) {
        this.modeStep = modeStep;
    }

    public ImportWizardMappingStepForm getMappingStep() {
        return mappingStep;
    }

    public void setMappingStep(ImportWizardMappingStepForm mappingStep) {
        this.mappingStep = mappingStep;
    }

    public boolean isImportRunning() {
        return importRunning;
    }

    public void setImportRunning(boolean importRunning) {
        this.importRunning = importRunning;
    }

    public String getImportUID() {
        return importUID;
    }

    public void setImportUID(String importUID) {
        this.importUID = importUID;
    }
    
    public boolean isMissingFieldsStepNeeded() {
        int mode = helper.getMode();
        return (mode == ImportMode.ADD.getIntValue()
                || mode == ImportMode.ADD_AND_UPDATE.getIntValue()
                || mode == ImportMode.UPDATE.getIntValue())
                && (helper.isGenderMissing() || helper.isMailingTypeMissing());
    }

    /**
     * Getter for jsp page for displaying status messages (we need to give the copy of list to avoid concurrent
     * modification problem)
     *
     * @return copy of dbInsertStatusMessages list
     */
    public List<Message> getDbInsertStatusMessagesAndParameters() {
        return new LinkedList<>(helper.getDbInsertStatusMessagesAndParameters());
    }
}
