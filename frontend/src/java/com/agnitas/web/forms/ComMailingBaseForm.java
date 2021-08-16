/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.beans.Mediatype;
import org.agnitas.emm.core.mailing.beans.LightweightMailingWithMailingList;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbUtilities;
import org.agnitas.web.MailingBaseAction;
import org.agnitas.web.forms.MailingBaseForm;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;

import com.agnitas.beans.MailingContentType;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mailing.dao.ComMailingParameterDao;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;
import com.agnitas.service.AgnTagService;
import com.agnitas.service.ComMailingLightService;
import com.agnitas.web.ComMailingBaseAction;

/**
 * Implementation of <strong>Form</strong> that handles Mailings
 */
public class ComMailingBaseForm extends MailingBaseForm {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingBaseForm.class);
	private int adminID;

	public final int getCompanyID() {
		return companyID;
	}

	public final void setCompanyID(@VelocityCheck int companyID) {
		this.companyID = companyID;
	}

	/** Serial version UID. */
	private static final long serialVersionUID = -2251490405906769187L;
	
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

    private boolean isMailingGrid = false;

	private Map<Integer, ComMailingParameter> parameterMap = new HashMap<>();

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
	private boolean changeMailing;
	private boolean frequencyCounterDisabled;
	private boolean mailingListFrequencyCountEnabled;

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

	private boolean importTemplateOverwrite = false;

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
		super.reset(map, request);
		clearBulkIds();

		int actionID = NumberUtils.toInt(request.getParameter("action"));
		if (actionID == ComMailingBaseAction.ACTION_SAVE
			|| actionID == ComMailingBaseAction.ACTION_SAVE_MAILING_GRID) {
			parameterMap.clear();
		}

		intervalType = ComMailingParameterDao.IntervalType.None;
		intervalDays = new boolean[7];

		setNumberOfRows(-1);
		selectedFields = ArrayUtils.EMPTY_STRING_ARRAY;

		uploadFile = null;
		
		importTemplateOverwrite = false;

		setTargetGroups(Collections.emptyList());
		setTargetExpression(StringUtils.EMPTY);
		assignTargetGroups = false;
	}
	
	/**
     * Clears all data connected with targets
     *
     */
    public void clearTargetsData() {
		setTargetGroupsList(Collections.emptyList());
		setTargetGroups(Collections.emptyList());
		setSplitId(0);
		setSplitBase(null);
		setSplitPart(null);
    }

	public String getFilterCreationDateBegin() {
		return filterCreationDateBegin;
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
		List<Integer> emptyParams = this.parameterMap.entrySet().stream()
				.filter(pair -> {
					ComMailingParameter param = pair.getValue();
					return StringUtils.isBlank(param.getName()) &&
							StringUtils.isBlank(param.getValue()) &&
							StringUtils.isBlank(param.getDescription());
				})
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());

		emptyParams.forEach(key -> this.parameterMap.remove(key));
		return this.parameterMap;
	}

	public void setParameterMap(Map<Integer, ComMailingParameter> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public ComMailingParameter getParameter(int index) {
		return parameterMap.computeIfAbsent(index, i -> new ComMailingParameter());
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

	public List<LightweightMailingWithMailingList> getMailings() {
		ComMailingLightService dietMails = (ComMailingLightService) getWebApplicationContext().getBean("MailingLightService");
		return dietMails.getLightweightMailings(getCompanyID(), getAdminID(), parentMailing, getMailingID());
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

	@Override
	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
		// set company ID
		setCompanyID(AgnUtils.getCompanyID(request));
		// set admin ID of current user
		setAdminID(AgnUtils.getAdminId(request));

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

            Integer currentWorkflowId = (Integer) request.getSession().getAttribute(WorkflowParametersHelper.WORKFLOW_ID);
			if (mailinglistID == 0) {
				if (currentWorkflowId == null || currentWorkflowId == 0) {
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

			if (MapUtils.isNotEmpty(parameterMap)) {
				Optional<ComMailingParameter> optional = parameterMap.values().stream()
						.filter(param -> {
							if (param != null && StringUtils.isBlank(param.getName())) {
								return StringUtils.isNotBlank(param.getValue()) || StringUtils.isNotBlank(param.getDescription());
							} else {
								return false;
							}
						}).findFirst();

				if (optional.isPresent()) {
					actionErrors.add(ActionMessages.GLOBAL_MESSAGE,  new ActionMessage("error.mailing.parameter.emptyName"));
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
		if (mailingType != MailingTypes.FOLLOW_UP.getCode()) {
			parentMailing = 0;
			followUpMailingType = "";
			followMailing = "";
		}

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
			return media.getStatus() == MediaTypeStatus.Active.getCode();
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

	public void setUseMediaType(int index, boolean active) {
		Mediatype mt = getMediatypes().get(index);

		if (mt == null) {
			if (!active) {
				return;
			}
			mt = getMedia(index);
		}
		if (mt != null) {
			if (active) {
				mt.setStatus(MediaTypeStatus.Active.getCode());
			} else {
				mt.setStatus(MediaTypeStatus.Inactive.getCode());
			}
		}
	}

	public void setMoveMedia(int type, boolean isUp) {
		if (type == -1) {
			return;
		}

		Mediatype media = getMediatypes().get(type);
		if (media == null) {
			return;
		}

		List<MediaTypes> prioritized = getPrioritizedMediatypes();
		OptionalInt optional = IntStream.range(0, prioritized.size())
				.filter(i -> prioritized.get(i).getMediaCode() == type).findFirst();
		if (!optional.isPresent()) {
			return;
		}

		// Bugfix priority was always 5 --> index is true priority
		int priority = optional.getAsInt();
		if (priority > 4) {
			return;
		}

		int newPriority = isUp ? priority - 1 : priority + 1;
		if (newPriority < 0 || newPriority > 4) {
			return;
		}

		int swapMediaType = Optional.ofNullable(prioritized.get(newPriority))
				.map(MediaTypes::getMediaCode).orElse(-1);
		Mediatype nextMedia = getMediatypes().get(swapMediaType);

		if (nextMedia != null) {
			media.setPriority(newPriority);
			nextMedia.setPriority(priority);
		}
	}

	public List<MediaTypes> getPrioritizedMediatypes() {
		List<MediaTypes> priorities = new ArrayList<>(Arrays.asList(MediaTypes.values()));

		Map<Integer, Mediatype> map = getMediatypes();
		priorities.sort((m1, m2) -> {
			//prevent prioritizing of unused media type
			Mediatype type1 = null;
			if (getUseMediaType(m1.getMediaCode())) {
				type1 = map.get(m1.getMediaCode());
			}

			Mediatype type2 = null;
			if (getUseMediaType(m2.getMediaCode())) {
				type2 = map.get(m2.getMediaCode());
			}

			return Objects.equals(type1, type2) ? 0 :
					type1 == null ? 1 :
					type2 == null ? -1 :
							type1.getPriority() - type2.getPriority();
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

	@Override
	protected List<Integer> getTypeList() {
		List<Integer> typeList = super.getTypeList();

		if (mailingTypeFollowup) {
			typeList.add(MailingType.FOLLOW_UP.getCode());
		}
		if (mailingTypeInterval) {
			typeList.add(MailingType.INTERVAL.getCode());
		}

		return typeList;
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
		if (dynamicTemplateString == null) {
			dynamicTemplate = false;
		} else {
			dynamicTemplate = AgnUtils.interpretAsBoolean(dynamicTemplateString);
		}
	}

	@Override
	public String getDynamicTemplateString() {
		if (dynamicTemplate) {
			return "on";
		} else {
			return "";
		}
	}

	public void setBulkID(int id, String value) {
		if (value != null && (value.equals("on") || value.equals("yes") || value.equals("true"))) {
			bulkIDs.add(id);
		}
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

	public boolean isImportTemplateOverwrite() {
		return importTemplateOverwrite;
	}

	public void setImportTemplateOverwrite(boolean importTemplateOverwrite) {
		this.importTemplateOverwrite = importTemplateOverwrite;
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
	
	public void setAdminID(int adminID) {
		this.adminID = adminID;
	}
	
	public int getAdminID() {
		return adminID;
	}

	public boolean isFrequencyCounterDisabled() {
		return frequencyCounterDisabled;
	}

	public void setFrequencyCounterDisabled(boolean frequencyCounterDisabled) {
		this.frequencyCounterDisabled = frequencyCounterDisabled;
	}

	public boolean isMailingListFrequencyCountEnabled() {
		return mailingListFrequencyCountEnabled;
	}

	public void setMailingListFrequencyCountEnabled(boolean mailingListFrequencyCountEnabled) {
		this.mailingListFrequencyCountEnabled = mailingListFrequencyCountEnabled;
	}
}
