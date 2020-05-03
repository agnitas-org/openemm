/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import com.agnitas.beans.ComMailing.MailingContentType;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mailing.dao.ComMailingParameterDao;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.service.AgnTagService;
import com.agnitas.service.ComMailingLightVO;
import com.agnitas.web.ComMailingBaseAction;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.Mediatype;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbUtilities;
import org.agnitas.web.MailingBaseAction;
import org.agnitas.web.forms.MailingBaseForm;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.upload.FormFile;

/**
 * Implementation of <strong>Form</strong> that handles Mailings
 */
public class ComMailingBaseForm extends MailingBaseForm {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingBaseForm.class);

	public final int getCompanyID() {
		return companyID;
	}

	public final void setCompanyID(@VelocityCheck int companyID) {
		this.companyID = companyID;
	}

	/** Serial version UID. */
	private static final long serialVersionUID = -2251490405906769187L;
	
	private int numOfMediaTypes = 0;
	private int defaultMediaType = 0;
	private String followUpMailingType = ""; // contains which followUp we have (if any).
	private int parentMailing;
	private int companyID; // holds the company Id.

	private boolean undoAvailable;
	private boolean dynamicTemplate;

	private String followMailing = "";

	private Set<Integer> bulkIDs = new HashSet<>();

	private List<TargetLight> splitTargetsForSplitBase;

	private List<TargetLight> splitTargets;

	private int gridTemplateId;

    private String gridTemplateName;

	private int fromCalendarPage;

	private String planDate;
	
	private int mailingInfoID = 0;	//increasing number

    private String ownerName = "";

    private String notes = "";

    private boolean isMailingGrid = false;

	private Map<Integer, ComMailingParameter> parameterMap = new HashMap<>();

	private boolean addParameter;

    private int workflowId;
    private int splitId;

	private boolean usedInCampaignManager;
    private boolean usedInActiveWorkflow;
    private String workflowForwardParams;

	private String searchQueryText;
	private boolean searchNameChecked = true;
	private boolean searchDescriptionChecked = true;
	private boolean searchContentChecked = true;

	private Integer[] filterMailingLists;
	private String[] filterStatuses;
	private String[] badgeFilters;
	private String filterSendDateBegin;
	private String filterSendDateEnd;
	private String filterCreationDateBegin;
	private String filterCreationDateEnd;
	private String[] selectedFields = ArrayUtils.EMPTY_STRING_ARRAY;
	private String targetExpression;
	private int scrollTop;
	private boolean changeMailing;

	public boolean isChangeMailing() {
		return changeMailing;
	}

	public void setChangeMailing(boolean changeMailing) {
		this.changeMailing = changeMailing;
	}

	/**
	 * Holds value of property mailingTypeInterval
	 */
	protected boolean mailingTypeInterval;

	private FormFile uploadFile;

	private ComMailingParameterDao.IntervalType intervalType = ComMailingParameterDao.IntervalType.None;

	/**
	 * This array is index with the int values of Calendar days - 1 (Calendar.Sunday=1, Calendar.Saturday=7)
	 */
	private boolean[] intervalDays = new boolean[7];

	private int numberOfMonth;
	
	private int weekdayOrdinal;

	/**
	 * -1 = Ultimo/Last
	 */
	private int intervalDayOfMonth;

	private String intervalTime = null;

	/** Holds value of property showMtypeOptions. */
	private int activeMedia = 0;
	private String[] mediaTypeLabels = { "Email", "Fax", "Print", "MMS", "SMS", "WhatsApp" };	// TODO Remove that from FormBean
	private String[] mediaTypeLabelsLowerCase = null;	// TODO Remove that from FormBean

	private boolean wmSplit;

	private boolean complexTargetExpression;

	private String splitBaseMessage;

	private String splitPartMessage;

	/**
	 * Holds value of property splitBase.
	 */
	private String splitBase;

	/**
	 * Holds value of property splitPart.
	 */
	private String splitPart;

	/**
	 * Holds value of property archived.
	 */
	private boolean archived;

	private boolean locked;

	/**
	 * Holds value of property envelopeEmail.
	 */
	protected String envelopeEmail;

	/**
	 * Holds value of property mailingTypeDate.
	 */
	protected boolean mailingTypeFollowup;

	// Mailing's target expression won't be overwritten unless this flag is set to true.
	private boolean assignTargetGroups;
	
	private MailingContentType mailingContentType;

	@Override
	public void reset(ActionMapping map, HttpServletRequest request) {
		setAction(ComMailingBaseAction.ACTION_LIST);

		dynamicTemplate = false;
		archived = false;
		super.reset(map, request);
		clearBulkIds();

		int actionID = NumberUtils.toInt(request.getParameter("action"));
		if (actionID == ComMailingBaseAction.ACTION_SAVE
			|| actionID == ComMailingBaseAction.ACTION_SAVE_MAILING_GRID) {
			parameterMap.clear();
			addParameter = false;
		}

		intervalType = ComMailingParameterDao.IntervalType.None;
		intervalDays = new boolean[7];

		setNumberOfRows(-1);
		selectedFields = ArrayUtils.EMPTY_STRING_ARRAY;

		uploadFile = null;

		setTargetGroups(Collections.emptyList());
		setTargetExpression(StringUtils.EMPTY);
		assignTargetGroups = false;
	}
	
	/**
     * Clears all data connected with targets for action based mailing form
     *
     */
    public void clearTargetsData () {
        if (mailingType == MailingModel.MailingType.ACTION_BASED.getValue()) {
			setTargetGroupsList(Collections.emptyList());
			setTargetGroups(Collections.emptyList());
            setSplitId(0);
            setSplitBase(null);
            setSplitPart(null);
        }
    }

	public String getFilterCreationDateBegin() {
		return filterCreationDateBegin;
	}

	public int getScrollTop() {
		return scrollTop;
	}

	public void setScrollTop(int scrollTop) {
		this.scrollTop = scrollTop;
	}

	public void setFilterCreationDateBegin(String filterCreationDateBegin) {
		this.filterCreationDateBegin = filterCreationDateBegin;
	}

	public String getFilterCreationDateEnd() {
		return filterCreationDateEnd;
	}

	public void setFilterCreationDateEnd(String filterCreationDateEnd) {
		this.filterCreationDateEnd = filterCreationDateEnd;
	}

	public String getTargetExpression() {
		return targetExpression;
	}

	public void setTargetExpression(String targetExpression) {
		this.targetExpression = targetExpression;
	}

	public String[] getSelectedFields() {
		return selectedFields;
	}

	public void setSelectedFields(String[] selectedFields) {
		this.selectedFields = selectedFields;
	}

	public int getMailingInfoID() {
		return mailingInfoID;
	}

	public void setMailingInfoID(int mailingInfoID) {
		this.mailingInfoID = mailingInfoID;
	}

	public Map<Integer, ComMailingParameter> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<Integer, ComMailingParameter> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public ComMailingParameter getParameter(int index) {
		return parameterMap.computeIfAbsent(index, i -> new ComMailingParameter());
	}

	public boolean isAddParameter() {
		return addParameter;
	}

	public void setAddParameter(boolean addParameter) {
		this.addParameter = addParameter;
	}

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

	/**
	 * Used to store the followUp Mailing Type (if any).
	 */
	public String getFollowUpMailingType() {
		return followUpMailingType;
	}

	public void setFollowUpMailingType(String followUpMailingType) {
		this.followUpMailingType = followUpMailingType;
	}

	// get a sorted list from the Helper Class ComMailingLightVO
	public List<LightweightMailing> getMailings() {
		// get dietMails via Spring :-) Look into applicationContext for mapping of this bean.
		ComMailingLightVO dietMails = (ComMailingLightVO) getWebApplicationContext().getBean("MailingLightService");
		List<LightweightMailing> mailings = dietMails.getSnowflakeMailings(getCompanyID());

		if (parentMailing > 0 && parentMailing != getMailingID()) {
			boolean parentNotInList = true;

			for (LightweightMailing mailing : mailings) {
				if (parentMailing == mailing.getMailingID()) {
					parentNotInList = false;
					break;
				}
			}

			if (parentNotInList) {
				mailings.add(dietMails.getSnowflakeMailing(parentMailing));
			}
		}

		return mailings;
	}

	/**
	 * used for storing the parent of a follow-Up Mailing (just the MailingID).
	 */
	public int getParentMailing() {
		return parentMailing;
	}

	public void setParentMailing(int parentMailing) {
		this.parentMailing = parentMailing;
	}

	public static int getNumOfMediaTypes(HttpServletRequest req) {

		int numOfMediaTypes = 0;

		for(final MediaTypes mt : MediaTypes.values()) {
			if(AgnUtils.allowed(req, mt.getRequiredPermission())) {
				numOfMediaTypes++;
			}
		}

		if (numOfMediaTypes == 0) {
			numOfMediaTypes = 1;
		}

		return numOfMediaTypes;
	}

	public static int getDefaultMediaType(HttpServletRequest req) {
		for(final MediaTypes mt : MediaTypes.valuesSortedByDefaultValuePriority()) {
			if(AgnUtils.allowed(req, mt.getRequiredPermission())) {
				return mt.getMediaCode();
			}
		}

		return MediaTypes.EMAIL.getMediaCode();
	}

	@Override
	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
		// set company ID
		setCompanyID(AgnUtils.getCompanyID(request));

		ActionErrors actionErrors = new ActionErrors();

		if (action == ComMailingBaseAction.ACTION_SAVE || action == ComMailingBaseAction.ACTION_SAVE_MAILING_GRID) {
			if (shortname.length() >= 100) {
				actionErrors.add("shortname", new ActionMessage("error.shortname_too_long"));
			}

			if (shortname.length() < 3) {
				actionErrors.add("shortname", new ActionMessage("error.name.too.short"));
			}

			if (StringUtils.length(getDescription()) > 500) {
				actionErrors.add("description", new ActionMessage("error.description.too.long"));
			}

            Integer workflowId = (Integer) request.getSession().getAttribute(WorkflowParametersHelper.WORKFLOW_ID);
			if (mailinglistID == 0) {
				if (workflowId == null || workflowId == 0) {
					actionErrors.add("global", new ActionMessage("error.mailing.noMailinglist"));
				} else {
					actionErrors.add("global", new ActionMessage("error.mailing.noMailinglistSetWithCampaignEditor"));
				}
			}

			// NEW CODE (to be inserted):
			if (StringUtils.length(emailReplytoFullname) > 255) {
				actionErrors.add("replyFullname", new ActionMessage("error.reply_fullname_too_long"));
			}
			if (StringUtils.length(getSenderFullname()) > 255) {
				actionErrors.add("senderFullname", new ActionMessage("error.sender_fullname_too_long"));
			}
			if (StringUtils.isBlank(emailReplytoFullname)) {
				emailReplytoFullname = getSenderFullname();
			}

			// TODO: move all media-specific validation to controller layer.
			if (getUseMediaType(MediaTypes.EMAIL.getMediaCode())) {
				if (getMediaEmail().getFromEmail().length() < 3) {
					actionErrors.add("email", new ActionMessage("error.invalid.email"));
				}

				if (getEmailSubject().length() < 2) {
					actionErrors.add("subject", new ActionMessage("error.mailing.subject.too_short"));
				}

				try {
					InternetAddress adr = new InternetAddress(getMediaEmail().getFromEmail());
					String email = adr.getAddress();
	                if (!AgnUtils.isEmailValid(email)) {
						actionErrors.add("sender", new ActionMessage("error.mailing.sender_adress"));
					}
				} catch (Exception e) {
					if (!getMediaEmail().getFromEmail().contains("[agn")) {
						actionErrors.add("sender", new ActionMessage("error.mailing.sender_adress"));
					}
				}
				
				try {
					InternetAddress adr = new InternetAddress(getMediaEmail().getReplyEmail());
					String email = adr.getAddress();
	                if (!AgnUtils.isEmailValid(email)) {
						actionErrors.add("sender", new ActionMessage("error.mailing.reply_adress"));
					}
				} catch (Exception exc) {
					if (!getMediaEmail().getReplyEmail().contains("[agn")) {
						actionErrors.add("sender", new ActionMessage("error.mailing.reply_adress"));
					}
				}
				try {
					InternetAddress adr = new InternetAddress(getMediaEmail().getEnvelopeEmail());
					String email = adr.getAddress();
	                if (!AgnUtils.isEmailValid(email)) {
						actionErrors.add("sender", new ActionMessage("error.mailing.envelope_adress"));
					}
				} catch (Exception e) {
					// do nothing
				}
			}

			AgnTagService agnTagService = getWebApplicationContext().getBean("AgnTagService", AgnTagService.class);
			try {
				agnTagService.getDynTags(getEmailSubject());
				if (getUseMediaType(MediaTypes.EMAIL.getMediaCode())) {
					agnTagService.getDynTags(getSenderFullname());
				}
			} catch (Exception e) {
				logger.error("error during validation", e);
				actionErrors.add("subject", new ActionMessage("error.template.dyntags.subject"));
			}

			try {
				agnTagService.resolveTags(getEmailSubject(), AgnUtils.getCompanyID(request), 0, 0, 0);
				if (getUseMediaType(MediaTypes.EMAIL.getMediaCode())) {
					agnTagService.resolveTags(getSenderFullname(), AgnUtils.getCompanyID(request), 0, 0, 0);
				}
			} catch (Exception e) {
				actionErrors.add("subject", new ActionMessage("error.personalization_tag"));
			}

			if (addParameter) {
				ComMailingParameter parameter = parameterMap.get(0);
				if (parameter != null && StringUtils.isEmpty(parameter.getName())) {
					actionErrors.add("global", new ActionMessage("error.mailing.parameter.emptyName"));
				}
			}
		}

        if (action == MailingBaseAction.ACTION_LIST) {
            if (searchNameChecked || searchDescriptionChecked || searchContentChecked) {
				for (String error : DbUtilities.validateFulltextSearchQueryText(searchQueryText)) {
					actionErrors.add("invalid_search_query", new ActionMessage(error));
				}
            }
        }

		// check if we have a follow-Up mailing. If not, reset the follow-Up parameters
		if (mailingType != Mailing.TYPE_FOLLOWUP) {
			parentMailing = 0;
			followUpMailingType = "";
			followMailing = "";
		}

		numOfMediaTypes = getNumOfMediaTypes(request);
		defaultMediaType = getDefaultMediaType(request);
		return actionErrors;
	}

	/**
	 * Getter for property activeMedia.
	 * 
	 * @return Value of property activeMedia.
	 * 
	 */
	public int getActiveMedia() {
		if (getUseMediaType(activeMedia)) {
			return activeMedia;
		}

		for (MediaTypes type : MediaTypes.values()) {
			if (getUseMediaType(type.getMediaCode())) {
				activeMedia = type.getMediaCode();
				return activeMedia;
			}
		}

		activeMedia = -1;

		return activeMedia;
	}

	/**
	 * Setter for property activeMedia.
	 * 
	 * @param activeMedia
	 *            New value of property activeMedia.
	 * 
	 */
	public void setActiveMedia(int activeMedia) {
		this.activeMedia = activeMedia;
	}

	public boolean isWmSplit() {
		return wmSplit;
	}

	public void setWmSplit(boolean wmSplit) {
		this.wmSplit = wmSplit;
	}

	public boolean isComplexTargetExpression() {
		return complexTargetExpression;
	}

	public void setComplexTargetExpression(boolean complexTargetExpression) {
		this.complexTargetExpression = complexTargetExpression;
	}

	public String getSplitBaseMessage() {
		return splitBaseMessage;
	}

	public void setSplitBaseMessage(String splitBaseMessage) {
		this.splitBaseMessage = splitBaseMessage;
	}

	public String getSplitPartMessage() {
		return splitPartMessage;
	}

	public void setSplitPartMessage(String splitPartMessage) {
		this.splitPartMessage = splitPartMessage;
	}

	/**
	 * Getter for property splitBase.
	 * 
	 * @return Value of property splitBase.
	 */
	public String getSplitBase() {
		return splitBase;
	}

	/**
	 * Setter for property splitBase.
	 * 
	 * @param splitBase
	 *            New value of property splitBase.
	 */
	public void setSplitBase(String splitBase) {
		this.splitBase = splitBase;
	}

	/**
	 * Getter for property splitPart.
	 * 
	 * @return Value of property splitPart.
	 */
	public String getSplitPart() {
		return splitPart;
	}

	/**
	 * Setter for property splitPart.
	 * 
	 * @param splitPart
	 *            New value of property splitPart.
	 */
	public void setSplitPart(String splitPart) {
		this.splitPart = splitPart;
	}

	/**
	 * Getter for property archived.
	 * 
	 * @return Value of property archived.
	 */
	@Override
	public boolean isArchived() {
		return archived;
	}

	/**
	 * Setter for property archived.
	 * 
	 * @param archived
	 *            New value of property archived.
	 */
	@Override
	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	/**
	 * Indexed getter for property useMediaType.
	 * 
	 * @param index
	 *            Index of the property.
	 * @return Value of the property at <CODE>index</CODE>.
	 * 
	 */
	public boolean getUseMediaType(int index) {
		Mediatype media = getMediatypes().get(index);

		if (media == null) {
			return false;
		} else {
			return media.getStatus() == Mediatype.STATUS_ACTIVE;
		}
	}

    public boolean[] getUseMediaType() {
        Set<Integer> keys = getMediatypes().keySet();
        boolean[] result = new boolean[Collections.max(keys) + 1];
        for (Integer key: keys) {
            result[key] = getUseMediaType(key);
        }
        return result;
    }

	public boolean getUseMediaEmail() {
		return getUseMediaType(MediaTypes.EMAIL.getMediaCode());
	}

	public String[] getMediaTypeLabelsLowerCase() {
		if (mediaTypeLabelsLowerCase == null) {
			mediaTypeLabelsLowerCase = new String[mediaTypeLabels.length];
			for (int i = 0; i < mediaTypeLabels.length; i++) {
				mediaTypeLabelsLowerCase[i] = mediaTypeLabels[i].toLowerCase();
			}
		}
		return mediaTypeLabelsLowerCase;
	}

	public List<String> getPrioritizedUsedMediaTypes() {
		List<String> result = new ArrayList<>();
		String[] labels = getMediaTypeLabelsLowerCase();
		for (int priority : getPriorities()) {
			if (getUseMediaType(priority)) {
				result.add(labels[priority].toLowerCase());
			}
		}
		return result;
	}

	public void setUseMediaType(int index, boolean how) {
		Mediatype mt = getMediatypes().get(index);

		if (mt == null) {
			if (!how) {
				return;
			}
			mt = getMedia(index);
		}
		if (mt != null) {
			if (how) {
				mt.setStatus(Mediatype.STATUS_ACTIVE);
			} else {
				mt.setStatus(Mediatype.STATUS_INACTIVE);
			}
		}
	}

	public void setMoveMedia(int type, boolean isUp) {
		List<Integer> list = getPriorities();
		Mediatype upper = null, lower = null;
		int index;

		for (index = 0; index < 5; index++) {
			if (list.get(index) == type) {
				break;
			}
		}
		if (index >= 5) {
			return;
		}
		if (isUp) {
			if (index <= 0) {
				return;
			}
			lower = mediatypes.get(list.get(index - 1));
			upper = mediatypes.get(list.get(index));
			// Bugfix priority was always 5 --> index is true priority
			upper.setPriority(index - 1);
			lower.setPriority(index);
		} else {
			if (index >= 4) {
				return;
			}
			lower = mediatypes.get(list.get(index));
			upper = mediatypes.get(list.get(index + 1));
			if (upper == null) {
				return;
			}
			// Bugfix priority was always 5 --> index is true priority
			upper.setPriority(index);
			lower.setPriority(index + 1);
		}
		/*
		 * savePrio=upper.getPriorities(); upper.setPriority(lower.getPriorities()); lower.setPriority(savePrio);
		 */
	}

	public int getNumberOfMediatypes() {
		return numOfMediaTypes;
	}

	public int getDefaultMediatype() {
		return defaultMediaType;
	}

	public ArrayList<Integer> getPriorities() {
		MediaTypes[] mediaTypes = MediaTypes.values();

		ArrayList<Integer> priorities = new ArrayList<>(mediaTypes.length);
		for (MediaTypes type : mediaTypes) {
			priorities.add(type.getMediaCode());
		}

		Map<Integer, Mediatype> map = getMediatypes();
		priorities.sort((c1, c2) -> {
			Mediatype type1 = map.get(c1);
			Mediatype type2 = map.get(c2);

			if (type1 == null || type2 == null) {
				if (type1 == type2) {
					return 0;
				} else {
					return type1 == null ? 1 : -1;
				}
			} else {
				return type1.getPriority() - type2.getPriority();
			}
		});

		return priorities;
	}

	public boolean getLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	/**
	 * Getter for property envelopeEmail.
	 * 
	 * @return Value of property envelopeEmail.
	 */
	public String getEnvelopeEmail() {
		return envelopeEmail;
	}

	/**
	 * Setter for property envelopeEmail.
	 * 
	 * @param envelopeEmail
	 *            New value of property envelopeEmail.
	 */
	public void setEnvelopeEmail(String envelopeEmail) {
		this.envelopeEmail = envelopeEmail;
	}

	public boolean isMailingTypeFollowup() {
		return mailingTypeFollowup;
	}

	public void setMailingTypeFollowup(boolean mailingTypeFollowup) {
		this.mailingTypeFollowup = mailingTypeFollowup;
	}

	public boolean isMailingTypeInterval() {
		return mailingTypeInterval;
	}

	public void setMailingTypeInterval(boolean mailingTypeInterval) {
		this.mailingTypeInterval = mailingTypeInterval;
	}

	/**
	 * Getter for property mailingType.
	 * 
	 * @return Value of property mailingType.
	 */
	@Override
	public String getTypes() {
		types = "";
		if (mailingTypeNormal) {
			types = "0";
		}
		if (mailingTypeEvent) {
			if (!types.equals("")) {
				types = types + ",";
			}
			types = types + "1";
		}
		if (mailingTypeDate) {
			if (!types.equals("")) {
				types = types + ",";
			}
			types = types + "2";
		}
		if (mailingTypeFollowup) {
			if (!types.equals("")) {
				types = types + ",";
			}
			types = types + "3";
		}
		if (mailingTypeInterval) {
			if (!types.equals("")) {
				types = types + ",";
			}
			types = types + "4";
		}
		if (types.equals("")) {
			types = "100";
		}
		return types;
	}

	@Override
	public void clearData() throws Exception {
        clearData(false);
    }

	@Override
	public void clearData(boolean keepContainerVisibilityState) throws Exception {
		super.clearData(keepContainerVisibilityState);
		planDate = "";
        workflowId = 0;
        workflowForwardParams = "";
		setSplitBase("none");
		setSplitPart("1");
		setNotes("");
        setMailingGrid(false);
        parameterMap.clear();
        mailingContentType = null;
	}

	public String getFollowMailing() {
		return followMailing;
	}

	public void setFollowMailing(String followMailing) {
		this.followMailing = followMailing;
	}

	public void setUndoAvailable(boolean undoAvailable) {
		this.undoAvailable = undoAvailable;
	}

	public boolean isUndoAvailable() {
		return undoAvailable;
	}

	@Override
	public void setUseDynamicTemplate(boolean dynamicTemplate) {
		this.dynamicTemplate = dynamicTemplate;
	}

	@Override
	public boolean getUseDynamicTemplate() {
		return dynamicTemplate;
	}

	@Override
	public void setDynamicTemplateString(String dynamicTemplateString) {
		if (dynamicTemplateString == null)
			dynamicTemplate = false;
		else
			dynamicTemplate = dynamicTemplateString.equals("on") || dynamicTemplateString.equals("on") || dynamicTemplateString.equals("true");
	}

	@Override
	public String getDynamicTemplateString() {
		if (dynamicTemplate)
			return "on";
		else
			return "";
	}

	public void setBulkID(int id, String value) {
		if (value != null && (value.equals("on") || value.equals("yes") || value.equals("true")))
			bulkIDs.add(id);
	}

	public String getBulkID(int id) {
		return bulkIDs.contains(id) ? "on" : "";
	}

	public Set<Integer> getBulkIds() {
		return bulkIDs;
	}

	public void clearBulkIds() {
		bulkIDs.clear();
	}

	public List<TargetLight> getSplitTargetsForSplitBase() {
		return splitTargetsForSplitBase;
	}

	public void setSplitTargetsForSplitBase(List<TargetLight> splitTargetsForSplitBase) {
		this.splitTargetsForSplitBase = splitTargetsForSplitBase;
	}

	public List<TargetLight> getSplitTargets() {
		return splitTargets;
	}

	public void setSplitTargets(List<TargetLight> splitTargets) {
		this.splitTargets = splitTargets;
	}

	@Override
	protected boolean isParameterExcludedForUnsafeHtmlTagCheck(String parameterName, HttpServletRequest request) {
		return super.isParameterExcludedForUnsafeHtmlTagCheck(parameterName, request);
	}

	public int getGridTemplateId() {
		return gridTemplateId;
	}

	public void setGridTemplateId(int gridTemplateId) {
		this.gridTemplateId = gridTemplateId;
	}

    public String getGridTemplateName() {
        return gridTemplateName;
    }

    public void setGridTemplateName(String gridTemplateName) {
        this.gridTemplateName = gridTemplateName;
    }

    public int getFromCalendarPage() {
		return fromCalendarPage;
	}

	public void setFromCalendarPage(int fromCalendarPage) {
		this.fromCalendarPage = fromCalendarPage;
	}

	public String getPlanDate() {
		return planDate;
	}

	public void setPlanDate(String planDate) {
		this.planDate = planDate;
	}

	public ComMailingParameterDao.IntervalType getIntervalType() {
		boolean hasActiveDay = false;
		for (boolean intervalDay : intervalDays) {
			if (intervalDay) {
				hasActiveDay = true;
				break;
			}
		}
		if (!hasActiveDay && (intervalType == ComMailingParameterDao.IntervalType.Weekly || intervalType == ComMailingParameterDao.IntervalType.TwoWeekly)) {
			// No Active Day for weekly or twoweekly => No activation
			return ComMailingParameterDao.IntervalType.None;
		} else {
			return intervalType;
		}
	}

	public int getIntervalTypeID() {
		return intervalType.getId();
	}

	public void setIntervalType(ComMailingParameterDao.IntervalType intervalType) {
		this.intervalType = intervalType;
	}

	public void setIntervalTypeID(int id) {
		intervalType = ComMailingParameterDao.IntervalType.fromId(id, false);
	}

	public boolean[] getIntervalDays() {
		return intervalDays;
	}

	public void setIntervalDays(boolean[] intervalDays) {
		this.intervalDays = intervalDays;
	}

	public void setIsIntervalDay(int day, boolean activate) {
		intervalDays[day] = activate;
	}

	public boolean getIsIntervalDay(int day) {
		return intervalDays[day];
	}

	public int getNumberOfMonth() {
		return numberOfMonth;
	}

	public void setNumberOfMonth(int numberOfMonth) {
		this.numberOfMonth = numberOfMonth;
	}

	public int getWeekdayOrdinal() {
		return weekdayOrdinal;
	}

	public void setWeekdayOrdinal(int weekdayOrdinal) {
		this.weekdayOrdinal = weekdayOrdinal;
	}

	public int getIntervalDayOfMonth() {
		return intervalDayOfMonth;
	}

	public void setIntervalDayOfMonth(int intervalDayOfMonth) {
		this.intervalDayOfMonth = intervalDayOfMonth;
	}

	public String getIntervalTime() {
		if (intervalTime != null && !AgnUtils.check24HourTime(intervalTime)) {
			intervalTime = null;
		}
		return intervalTime == null ? "00:00" : intervalTime;
	}

	public void setIntervalTime(String intervalTime) {
		this.intervalTime = intervalTime;
	}

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isIsMailingGrid() {
        return isMailingGrid;
    }

    public void setMailingGrid(boolean mailingGrid) {
        isMailingGrid = mailingGrid;
    }

    public int getSplitId() {
        return splitId;
    }

    public void setSplitId(int splitId) {
        this.splitId = splitId;
        if (splitId == -1) {
            splitBase = "yes";
        }
    }

	public boolean isUsedInActiveWorkflow() {
		return usedInActiveWorkflow;
	}

	public void setUsedInActiveWorkflow(boolean usedInActiveWorkflow) {
		this.usedInActiveWorkflow = usedInActiveWorkflow;
	}

	public boolean isUsedInCampaignManager() {
		return usedInCampaignManager;
	}

	public void setUsedInCampaignManager(boolean usedInCampaignManager) {
		this.usedInCampaignManager = usedInCampaignManager;
	}

    public String getWorkflowForwardParams() {
        return workflowForwardParams;
    }

    public void setWorkflowForwardParams(String workflowForwardParams) {
        this.workflowForwardParams = workflowForwardParams;
    }

	public String getSearchQueryText() {
		return searchQueryText;
	}

	public void setSearchQueryText(String searchQueryText) {
		this.searchQueryText = searchQueryText;
	}

	public boolean isSearchNameChecked() {
		return searchNameChecked;
	}

	public void setSearchNameChecked(boolean searchNameChecked) {
		this.searchNameChecked = searchNameChecked;
	}

	public boolean isSearchDescriptionChecked() {
		return searchDescriptionChecked;
	}

	public void setSearchDescriptionChecked(boolean searchDescriptionChecked) {
		this.searchDescriptionChecked = searchDescriptionChecked;
	}

	public boolean isSearchContentChecked() {
		return searchContentChecked;
	}

	public void setSearchContentChecked(boolean searchContentChecked) {
		this.searchContentChecked = searchContentChecked;
	}

	public Integer[] getFilterMailingList() {
		return filterMailingLists;
	}

	public void setFilterMailingList(Integer[] filterMailingLists) {
		this.filterMailingLists = filterMailingLists;
	}

	public String getFilterSendDateEnd() {
		return filterSendDateEnd;
	}

	public void setFilterSendDateEnd(String filterSendDateEnd) {
		this.filterSendDateEnd = filterSendDateEnd;
	}

	public String getFilterSendDateBegin() {
		return filterSendDateBegin;
	}

	public void setFilterSendDateBegin(String filterSendDateBegin) {
		this.filterSendDateBegin = filterSendDateBegin;
	}

	public String[] getFilterStatus() {
		return filterStatuses;
	}

	public void setFilterStatus(String[] filterStatuses) {
		this.filterStatuses = filterStatuses;
	}

	public String[] getBadgeFilters() {
		return badgeFilters;
	}

	public void setBadgeFilters(String[] badgeFilters) {
		this.badgeFilters = badgeFilters;
	}

	public void setIntervalNumberOfMonth(int numberOfMonth) {
		this.numberOfMonth = numberOfMonth;
	}

	public FormFile getUploadFile() {
		return uploadFile;
	}

	public void setUploadFile(FormFile uploadFile) {
		this.uploadFile = uploadFile;
	}

	public boolean getAssignTargetGroups() {
		return assignTargetGroups;
	}

	public void setAssignTargetGroups(boolean assignTargetGroups) {
		this.assignTargetGroups = assignTargetGroups;
	}

	public MailingContentType getMailingContentType() throws Exception {
		return mailingContentType;
	}

	public void setMailingContentType(MailingContentType mailingContentType) {
		this.mailingContentType = mailingContentType;
	}

	public boolean isMailingContentTypeAdvertising() {
		return mailingContentType == null || mailingContentType == MailingContentType.advertising;
	}

	public void setMailingContentTypeAdvertising(boolean mailingContentTypeAdvertising) {
		mailingContentType = mailingContentTypeAdvertising ? MailingContentType.advertising : MailingContentType.transaction;
	}
}
