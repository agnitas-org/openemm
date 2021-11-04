/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.agnitas.actions.EmmAction;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mediatype;
import org.agnitas.beans.TrackableLink;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.preview.AgnTagError;
import org.apache.struts.action.ActionMessages;
import org.springframework.context.ApplicationContext;

import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;

public interface Mailing extends MailingBase {
    int TARGET_MODE_OR = 0;
    int TARGET_MODE_AND = 1;

    int NONE_SPLIT_ID = 0;
    int YES_SPLIT_ID = -1;

	String NONE_SPLIT = "none";
	String YES_SPLIT = "yes";

    void addAttachment(MailingComponent aComp);

    void addComponent(MailingComponent aComp);

    /**
     * Search for tags and adds then to a vector.
     *
     * @return Vector of added tags.
     * @deprecated use {@link com.agnitas.service.AgnTagService#getDynTags(String, com.agnitas.service.AgnDynTagGroupResolver)}.
     */
    @Deprecated
    Vector<String> findDynTagsInTemplates(String aTemplate, ApplicationContext con) throws Exception;

    MailingComponent getTemplate(String id);
    
    MailingComponent getTemplate(MediaTypes mediaTypes);

    MailingComponent getTextTemplate();

    Map<String, MailingComponent> getComponents();

    Map<String, DynamicTag> getDynTags();

    MailingComponent getHtmlTemplate();

    int getMailTemplateID();

    int getMailingType();

    Date getCreationDate();

    Collection<Integer> getTargetGroups();

    int getTargetID();

    int getTargetMode();

    int getTemplateOK();

    boolean isIsTemplate();

    List<String> cleanupDynTags(Vector<String> keepTags);

    void cleanupTrackableLinks(Vector<String> keepLinks);

    void cleanupMailingComponents(Vector<String> keepComps);

    boolean parseTargetExpression(String tExp);

    Vector<String> scanForLinks(String aText1, String textModuleName, ApplicationContext con, ActionMessages messages, ActionMessages errors) throws Exception;
    Vector<String> scanForLinks(String aText1, String textModuleName, ApplicationContext con, ActionMessages messages, ActionMessages errors, ComAdmin admin) throws Exception;

    /**
     * search for links
     *
     * @return Vector of links.
     */
    Vector<String> scanForLinks(ApplicationContext con) throws Exception;

    /**
     * Sends mailing.
     * @throws Exception
     */
    boolean triggerMailing(int maildropStatusId, Map<String, Object> opts, ApplicationContext con) throws Exception;

    /**
     * Setter for property asciiTemplate.
     *
     * @param asciiTemplate New value of property asciiTemplate.
     */
    void setTextTemplate(MailingComponent asciiTemplate);

    void setComponents(Map<String, MailingComponent> components);

    void setDynTags(Map<String, DynamicTag> dynTags);

    void setHtmlTemplate(MailingComponent htmlTemplate);

    void setIsTemplate(boolean isTemplate);

    void setMailTemplateID(int id);

    void setMailingType(int mailingType);

    void setMailingType(MailingType mailingType);

    void setCreationDate(Date creationDate);

    /**
     * Setter for property targetGroups.
     * This is automatically set when calling {@link #setTargetExpression(String)}.
     *
     * @param targetGroups New value of property targetGroups.
     */
    @Deprecated
    void setTargetGroups(Collection<Integer> targetGroups);

    void setTargetID(int id);

    /**
     * Setter for property targetMode.
     * This is automatically set when {@link #setTargetExpression(String)} is called.
     *
     * @param targetMode New value of property targetMode.
     */
    @Deprecated
    void setTargetMode(int targetMode);

    void setTemplateOK(int templateOK);

    String getTargetExpression();
    void setTargetExpression(String targetExpression);

    Map<Integer, Mediatype> getMediatypes();
    void setMediatypes(Map<Integer, Mediatype> mediatypes);

    MediatypeEmail getEmailParam();

    Map<String, ComTrackableLink> getTrackableLinks();
    void setTrackableLinks(Map<String, ComTrackableLink> trackableLinks);

    void init( @VelocityCheck int companyID, ApplicationContext con);

    DynamicTag getDynamicTagById(int dynId);

    TrackableLink getTrackableLinkById(int urlID);

    /**
     * Search for all dependency
     */
    boolean buildDependencies(boolean scanDynTags, ApplicationContext con) throws Exception;
    boolean buildDependencies(boolean scanDynTags, List<String> dynNamesForDeletion, ApplicationContext con, ActionMessages messages, ActionMessages errors) throws Exception;
    boolean buildDependencies(boolean scanDynTags, List<String> dynNamesForDeletion, ApplicationContext con, ActionMessages messages, ActionMessages errors, ComAdmin admin) throws Exception;
    boolean buildDependencies(boolean scanDynTags, List<String> dynNamesForDeletion, ApplicationContext con) throws Exception;

    Set<MaildropEntry> getMaildropStatus();
    void setMaildropStatus(Set<MaildropEntry> maildropStatus);

    void addDynamicTag(DynamicTag aTag);

    int getDeleted();
    void setDeleted(int deleted);

    Map<Integer, ComTarget> getAllowedTargets(ApplicationContext myContext);

    boolean getNeedsTarget();
    void setNeedsTarget(boolean needsTarget);

    int getLocked();
    void setLocked(int locked);

    int getPriority();
    void setPriority(int priority);

    boolean isPrioritizationAllowed();
    void setPrioritizationAllowed(boolean prioritizationAllowed);

    int getArchived();
    void setArchived(int archived);

    int getOpenActionID();
    void setOpenActionID(int id);

    int getClickActionID();
    void setClickActionID(int id);

    void updateTargetExpression();

    List<String> replaceAndGetMeasuredSeparatelyLinks(List<String> links, ApplicationContext context) throws Exception;

    boolean hasComplexTargetExpression();

    List<EmmAction> getPossibleActions();
    void setPossibleActions(List<EmmAction> possibleActions);

    boolean isFrequencyCounterDisabled();
    void setFrequencyCounterDisabled(boolean isDisabled);

    int getSplitID();
    void setSplitID(int splitID);

    Date getPlanDate();
    void setPlanDate(Date planDate);

    String getStatusmailRecipients();
    void setStatusmailRecipients(String statusmailRecipients);

    boolean isStatusmailOnErrorOnly();
    void setStatusmailOnErrorOnly(boolean statusmailOnErrorOnly);
    
    String getFollowUpType();
	void setFollowUpType(String followUpType);
	
	@Override
	boolean getUseDynamicTemplate();
	@Override
	void setUseDynamicTemplate( boolean useDynamicTemplate);

	/**
	 * Returns a list of link properties with are contained in all links of thi mailing.
	 * Link properties contained in a link but not in all the others are not contained in this list,
	 * but are contained in the link property list of the specific link additionally to the links of this list.
	 * 
	 * @return
	 */
	List<LinkProperty> getCommonLinkExtensions();
	
	List<ComMailingParameter> getParameters();

	void setParameters(List<ComMailingParameter> parameters);
	
	Map<String, List<AgnTagError>> checkAgnTagSyntax(ApplicationContext applicationContext) throws Exception;

	int getPreviewComponentId();
	void setPreviewComponentId(int previewComponentId);
	
	MailingContentType getMailingContentType();
	void setMailingContentType(MailingContentType mailingContentType);
	Set<Integer> getAllReferencedTargetGroups();
}
