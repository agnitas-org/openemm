/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.impl.DynamicTagContentImpl;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DynTagNameComparator;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.service.MaildropService;

public class ComMailingContentForm extends StrutsFormBase {
    private static final long serialVersionUID = -2580120975819583381L;

    private static final transient Logger logger = LogManager.getLogger(ComMailingContentForm.class);

    private final Pattern dynamicContentParameterPattern = Pattern.compile("^dynContent\\(\\d+\\)$");

    public static final int TEXTAREA_WIDTH = 85;
    public static final int MAILING_CONTENT_HTML_EDITOR = 1;
    public static final int MAILING_CONTENT_HTML_CODE = 0;

    public static final String PROPERTY_NAME_PREFIX = "propertyName_";
    public static final String PROPERTY_VALUE_PREFIX = "propertyValue_";

    private List<TargetLight> availableTargetGroups;
    private List<ProfileField> availableInterestGroups;
    
    private Map<String, String[]> variabletypes = new HashMap<>();

    /**
     * A content can be sorted by an interestgroup, which the name of a column in customer_field_tbl.
     */

    private int mailingID;
    private String shortname;
    private String description;
    private int action;
    private boolean isTemplate;
    private boolean worldMailingSend;
    private boolean showHTMLEditor;
//    protected boolean noImages;
    private int dynNameID;
    private int newTargetID;
    private String newContent;
    private Map<String, DynamicTag> tags;
    private Map<Integer, DynamicTagContent> content;
    private String contentID;
//    private int previewFormat;
//    private int previewSize;
//    private int previewCustomerID;
    private int mailinglistID;
    private int mailFormat;
    private String dynName;
    private int mailingContentView;
    private String dynInterestGroup;
    private boolean enableTextGeneration;
//    private int previewType = -1;
    private int workflowId;
    private int gridTemplateId;
    private boolean showDateSettings = false;
    private boolean isMailingUndoAvailable;

    /**
     * Is mailing not active {@link MaildropService#isActiveMailing(int, int)}
     * or user has permission {@link Permission#MAILING_CONTENT_CHANGE_ALWAYS}
     */
    @Deprecated // Replace by request attribute
    private boolean mailingEditable = false;
    @Deprecated // Replace by request attribute
    private boolean mailingExclusiveLockingAcquired = false;
    @Deprecated // Replace by request attribute
    private String anotherLockingUserName;

    private List<String> dynTagNames = new ArrayList<>();
	
	private String externalCustomerFieldAdd = "";
	private String externalCustomerFieldRemove = "";
	private String externalReferenceFieldAdd = "";
	private String externalReferenceFieldRemove = "";
	
	private String salutationTagType = "";
	private String salutationType = "";
	private String genderLanguage = "";
    
    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     * @return errors
     */
    @Override
    public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
        return new ActionErrors();
    }

    @Override
    protected boolean isParameterExcludedForUnsafeHtmlTagCheck(String parameterName, HttpServletRequest request) {
        return parameterName.equals("newContent") || dynamicContentParameterPattern.matcher(parameterName).matches();
    }

    /**
     * Getter for property mailingID.
     *
     * @return Value of property mailingID.
     */
    public int getMailingID() {
        return mailingID;
    }

    /**
     * Setter for property mailingID.
     *
     * @param mailingID New value of property mailingID.
     */
    public void setMailingID(int mailingID) {
        this.mailingID = mailingID;
        if (mailingID == 0) {
            logger.error("Mailing ID is set to 0 for MailingContentForm. That can be a reason of problem with saving of mailing with companyId = 0.");
        }
    }

    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    public String getShortname() {
        return this.shortname;
    }

    /**
     * Setter for property shortname.
     *
     * @param shortname New value of property shortname.
     */
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    /**
     * Getter for property action.
     *
     * @return Value of property action.
     */
    public int getAction() {
        return this.action;
    }

    /**
     * Setter for property action.
     *
     * @param action New value of property action.
     */
    public void setAction(int action) {
        this.action = action;
    }

    /**
     * Getter for property isTemplate.
     *
     * @return Value of property isTemplate.
     */
    public boolean isIsTemplate() {
        return this.isTemplate;
    }

    /**
     * Setter for property isTemplate.
     *
     * @param isTemplate New value of property isTemplate.
     */
    public void setIsTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    /**
     * Getter for property dynNameID.
     *
     * @return Value of property dynNameID.
     */
    public int getDynNameID() {

        return this.dynNameID;
    }

    /**
     * Setter for property dynNameID.
     *
     * @param dynNameID New value of property dynNameID.
     */
    public void setDynNameID(int dynNameID) {

        this.dynNameID = dynNameID;
    }

    /**
     * Getter for property newContent.
     *
     * @return Value of property newContent.
     */
    public String getNewContent() {

        return this.newContent;
    }

    /**
     * Setter for property newContent.
     *
     * @param newContent New value of property newContent.
     */
    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }

    /**
     * Getter for property newTargetID.
     *
     * @return Value of property newTargetID.
     */
    public int getNewTargetID() {

        return this.newTargetID;
    }

    /**
     * Setter for property newTargetID.
     *
     * @param newTargetID New value of property newTargetID.
     */
    public void setNewTargetID(int newTargetID) {

        this.newTargetID = newTargetID;
    }

    /**
     * Getter for property tags.
     *
     * @return Value of property tags.
     */
    public Map<String, DynamicTag> getTags() {
        return this.tags;
    }

    /**
     * Setter for property tags.
     *
     * @param tags New value of property tags.
     */
    public void setTags(Map<String, DynamicTag> tags) {
        this.setTags(tags, false);
    }

    /**
     * Setter for property content. Performs sorting if the corresponding parameter is true
     *
     * @param tags     New value of property Tags.
     * @param sortTags do we need to sort tags?
     */
    public void setTags(Map<String, DynamicTag> tags, boolean sortTags) {
        if (sortTags) {
            this.tags = AgnUtils.sortMap(tags, new DynTagNameComparator());
        } else {
            this.tags = tags;
        }
    }

    /**
     * Getter for property content.
     *
     * @return Value of property content.
     */
    public Map<Integer, DynamicTagContent> getContent() {
        if (content == null) {
            content = new LinkedHashMap<>();
        }
        return this.content;
    }

    /**
     * Get a list of text blocks to be validated. Depending on the {@link #action} value and submitted content there are
     * different text blocks to validate.
     */
    public List<String> getContentForValidation() {
        Map<Integer, DynamicTagContent> contentMap = getContent();

        List<String> contents = new ArrayList<>(contentMap.size() + 1);

        for (DynamicTagContent block : contentMap.values()) {
            contents.add(block.getDynContent());
        }

        contents.add(newContent);

        return contents;
    }

    /**
     * Setter for property content.
     *
     * @param content New value of property content.
     */
    public void setContent(Map<Integer, DynamicTagContent> content) {
        this.setContent(content, false);
    }

    /**
     * Setter for property content. Performs sorting if the corresponding parameter is true
     *
     * @param content     New value of property content.
     * @param sortContent do we need to sort the content?
     */
    public void setContent(Map<Integer, DynamicTagContent> content, boolean sortContent) {
        if (sortContent) {
            this.content = AgnUtils.sortMap(content, Comparator.reverseOrder());
        } else {
            this.content = content;
        }
    }

    /**
     * Getter for property contentID.
     *
     * @return Value of property contentID.
     */
    public String getContentID() {
        return this.contentID;
    }

    /**
     * Setter for property contentID.
     *
     * @param contentID New value of property contentID.
     */
    public void setContentID(String contentID) {
        this.contentID = contentID;
    }



    /**
     * Getter for property mailinglistID.
     *
     * @return Value of property mailinglistID.
     */
    public int getMailinglistID() {
        return this.mailinglistID;
    }

    /**
     * Setter for property mailinglistID.
     *
     * @param mailinglistID New value of property mailinglistID.
     */
    public void setMailinglistID(int mailinglistID) {
        this.mailinglistID = mailinglistID;
    }

    /**
     * Getter for property mailFormat.
     *
     * @return Value of property mailFormat.
     */
    public int getMailFormat() {
        return this.mailFormat;
    }

    /**
     * Setter for property mailFormat.
     *
     * @param mailFormat New value of property mailFormat.
     */
    public void setMailFormat(int mailFormat) {
        this.mailFormat = mailFormat;
    }

    /**
     * Getter for property dynName.
     *
     * @return Value of property dynName.
     */
    public String getDynName() {
        return this.dynName;
    }

    /**
     * Setter for property dynName.
     *
     * @param dynName New value of property dynName.
     */
    public void setDynName(String dynName) {
        this.dynName = dynName;
    }

    /**
     * Getter for property worldMailingSend.
     *
     * @return Value of property worldMailingSend.
     */
    public boolean isWorldMailingSend() {
        return this.worldMailingSend;
    }

    /**
     * Setter for property worldMailingSend.
     *
     * @param worldMailingSend New value of property worldMailingSend.
     */
    public void setWorldMailingSend(boolean worldMailingSend) {
        this.worldMailingSend = worldMailingSend;
    }

    public boolean isShowHTMLEditor() {
        return this.showHTMLEditor;
    }

    public void setShowHTMLEditor(boolean showHTMLEditor) {
        this.showHTMLEditor = showHTMLEditor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMailingContentView() {
        return mailingContentView;
    }

    public void setMailingContentView(int mailingContentView) {
        this.mailingContentView = mailingContentView;
    }

    public int getTargetID(String index) {
        DynamicTagContent tag = getContent().get(NumberUtils.toInt(index));
        if (tag == null) {
            return -1;
        }
        return tag.getTargetID();
    }

    public void setTargetID(String index, int targetID) {
        getContent().computeIfAbsent(NumberUtils.toInt(index), this::createTagContent)
                .setTargetID(targetID);
    }

    public int getDynOrder(String index) {
        DynamicTagContent tag = getContent().get(NumberUtils.toInt(index));
        if (tag == null) {
            return -1;
        }
        return tag.getDynOrder();
    }

    public void setDynOrder(String index, int dynOrder) {
        getContent().computeIfAbsent(NumberUtils.toInt(index), this::createTagContent)
                .setDynOrder(dynOrder);
    }

    public String getDynContent(String index) {
        DynamicTagContent tag = getContent().get(NumberUtils.toInt(index));
        if (tag == null) {
            return null;
        } else {
            return tag.getDynContent();
        }
    }

    public void setDynContent(String index, String dynContent) {
        getContent().computeIfAbsent(NumberUtils.toInt(index), this::createTagContent)
                .setDynContent(dynContent);
    }

    public int getDynContentId(String index) {
        DynamicTagContent tag = getContent().get(NumberUtils.toInt(index));
        if (tag == null) {
            return -1;
        }
        return tag.getId();
    }

    public void setDynContentId(String index, int id) {
        getContent().computeIfAbsent(NumberUtils.toInt(index), this::createTagContent)
                .setId(id);
    }

    public int getDynNameId(String index) {
        DynamicTagContent tag = getContent().get(NumberUtils.toInt(index));
        if (tag == null) {
            return -1;
        }
        return tag.getDynNameID();
    }

    public void setDynNameId(String index, int id) {
        getContent().computeIfAbsent(NumberUtils.toInt(index), this::createTagContent)
                .setDynNameID(id);
    }

    private DynamicTagContent createTagContent(int index) {
        DynamicTagContent dynamicTagContent = new DynamicTagContentImpl();
        dynamicTagContent.setDynOrder(index);
        return dynamicTagContent;
    }

    public List<TargetLight> getAvailableTargetGroups() {
        return availableTargetGroups;
    }

    public void setAvailableTargetGroups(List<TargetLight> availableTargetGroups) {
        this.availableTargetGroups = availableTargetGroups;
    }

    public List<ProfileField> getAvailableInterestGroups() {
        return availableInterestGroups;
    }

    public void setAvailableInterestGroups(List<ProfileField> availableInterestGroups) {
        this.availableInterestGroups = availableInterestGroups;
    }

    public String getDynInterestGroup() {
        return dynInterestGroup;
    }

    public void setDynInterestGroup(String dynInterestGroup) {
        this.dynInterestGroup = dynInterestGroup;
    }

    public boolean isEnableTextGeneration() {
        return enableTextGeneration;
    }

    public void setEnableTextGeneration(boolean enableTextGeneration) {
        this.enableTextGeneration = enableTextGeneration;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        mailingID = 0;
        shortname = "";
        contentID = null;
        newContent = "";
        newTargetID = 0;

        gridTemplateId = 0;
        
		externalCustomerFieldAdd = "";
		externalCustomerFieldRemove = "";
		externalReferenceFieldAdd = "";
		externalReferenceFieldRemove = "";
		
		variabletypes = new HashMap<>();
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public int getGridTemplateId() {
        return gridTemplateId;
    }

    public void setGridTemplateId(int gridTemplateId) {
        this.gridTemplateId = gridTemplateId;
    }

    public boolean isShowDateSettings() {
        return showDateSettings;
    }

    public void setShowDateSettings(boolean showDateSettings) {
        this.showDateSettings = showDateSettings;
    }

    public boolean getIsMailingUndoAvailable() {
        return isMailingUndoAvailable;
    }

    public void setIsMailingUndoAvailable(boolean isMailingUndoAvailable) {
        this.isMailingUndoAvailable = isMailingUndoAvailable;
    }

    @Deprecated // Replace by request attribute
    public boolean isMailingEditable() {
        return mailingEditable;
    }

    @Deprecated // Replace by request attribute
    public void setMailingEditable(boolean mailingEditable) {
        this.mailingEditable = mailingEditable;
    }

    @Deprecated // Replace by request attribute
    public boolean isMailingExclusiveLockingAcquired() {
        return mailingExclusiveLockingAcquired;
    }

    @Deprecated // Replace by request attribute
    public void setMailingExclusiveLockingAcquired(boolean mailingExclusiveLockingAcquired) {
        this.mailingExclusiveLockingAcquired = mailingExclusiveLockingAcquired;
    }

    public void setDynTagNames(List<String> dynTagNames) {
        this.dynTagNames = dynTagNames;
    }
    
    public List<String> getDynTagNames() {
        return dynTagNames;
    }

	public String getExternalCustomerFieldAdd() {
		return externalCustomerFieldAdd;
	}

	public void setExternalCustomerFieldAdd(String externalCustomerFieldAdd) {
		this.externalCustomerFieldAdd = externalCustomerFieldAdd;
	}

	public String getExternalCustomerFieldRemove() {
		return externalCustomerFieldRemove;
	}

	public void setExternalCustomerFieldRemove(String externalCustomerFieldRemove) {
		this.externalCustomerFieldRemove = externalCustomerFieldRemove;
	}

	public String getExternalReferenceFieldAdd() {
		return externalReferenceFieldAdd;
	}

	public void setExternalReferenceFieldAdd(String externalReferenceFieldAdd) {
		this.externalReferenceFieldAdd = externalReferenceFieldAdd;
	}

	public String getExternalReferenceFieldRemove() {
		return externalReferenceFieldRemove;
	}

	public void setExternalReferenceFieldRemove(String externalReferenceFieldRemove) {
		this.externalReferenceFieldRemove = externalReferenceFieldRemove;
	}

	public String getSalutationTagType() {
		return salutationTagType;
	}

	public void setSalutationTagType(String salutationTagType) {
		this.salutationTagType = salutationTagType;
	}

	public String getSalutationType() {
		return salutationType;
	}

	public void setSalutationType(String salutationType) {
		this.salutationType = salutationType;
	}

	public String getGenderLanguage() {
		return genderLanguage;
	}

	public void setGenderLanguage(String genderLanguage) {
		this.genderLanguage = genderLanguage;
	}

	public Map<String, String[]> getVariabletypes() {
		return variabletypes;
	}

    @Deprecated // Replace by request attribute
    public String getAnotherLockingUserName() {
        return anotherLockingUserName;
    }

    @Deprecated // Replace by request attribute
    public void setAnotherLockingUserName(String anotherLockingUserName) {
        this.anotherLockingUserName = anotherLockingUserName;
    }
}
