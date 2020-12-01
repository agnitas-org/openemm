/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.agnitas.beans.ComTrackableLink;
import org.agnitas.actions.EmmAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.struts.action.ActionMessages;
import org.springframework.context.ApplicationContext;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;

public interface Mailing extends MailingBase {
    int TARGET_MODE_OR = 0;
    int TARGET_MODE_AND = 1;

    enum AccountMailingType {
    	World("W"), // World sent Mailings
    	EventBased("E"),
    	DateBased("R"),
    	OnDemand("D"),
    	Admin("A"),
    	Test("T"),
    	Verfification("V"), // Verification mails only for Litmus checks
    	CopyForEventBased("C"); // Legacy
    	// DB table may also contain some very old legacy values 'Unknown'
    	
    	private String codeValue;
    	
    	private AccountMailingType(String codeValue) {
    		this.codeValue = codeValue;
    	}
    	
    	public String getCodeValue() {
    		return codeValue;
    	}
    }

    String TYPE_FOLLOWUP_NON_OPENER = "non-opener";
    String TYPE_FOLLOWUP_OPENER = "opener";
    String TYPE_FOLLOWUP_NON_CLICKER = "non-clicker";
    String TYPE_FOLLOWUP_CLICKER = "clicker";
    String TYPE_FOLLOWUP_NON_OPENER_WITHOUT_TRACKING_VETO = "non-opener-without-tracking-veto";
    String TYPE_FOLLOWUP_NON_CKLICKER_WITHOUT_TRACKING_VETO = "non-clicker-without-tracking-veto";
    String TYPE_FOLLOWUP_TRACKING_VETO = "tracking-veto";

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

    String getPreview(String input, int inputType, int customerID, boolean overwriteMailtype, ApplicationContext con) throws Exception;

    String getPreview(String input, int inputType, int customerID, ApplicationContext con) throws Exception;

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
     */
    boolean triggerMailing(int maildropStatusId, Map<String, Object> opts, ApplicationContext con);

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

    /**
     * Creates a copy of the mailing.
     *
     * @return Mailingobject.
     */
    Object clone(ApplicationContext con);

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

    boolean replaceDuplicatedLinks(List<String> links, ApplicationContext context) throws Exception;

    boolean hasComplexTargetExpression();

    List<EmmAction> getPossibleActions();
    void setPossibleActions(List<EmmAction> possibleActions);

    boolean isFrequencyCounterDisabled();
    void setFrequencyCounterDisabled(boolean isDisabled);
}
