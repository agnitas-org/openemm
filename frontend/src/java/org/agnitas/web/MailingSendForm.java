/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.web;

import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import com.agnitas.beans.Mailing;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import com.agnitas.beans.DeliveryStat;
import com.agnitas.web.PreviewForm;

public class MailingSendForm extends StrutsFormBase {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(MailingSendForm.class);

    private static final long serialVersionUID = -2753995761202472679L;

    /**
     * Holds value of property mailingID.
     */
    protected int mailingID;

    /**
     * Holds value of property shortname.
     */
    protected String shortname;

    /**
     * Holds value of property description.
     */
    protected String description;

    /**
     * Holds value of property emailFormat.
     */
    protected int emailFormat;

    /**
     * Holds value of property action.
     */
    protected int action;
    

    protected PreviewForm previewForm = new PreviewForm();
    
    /**
     * Holds value of property sendStatText.
     */
    protected int sendStatText;

    /**
     * Holds value of property sendStatHtml.
     */
    protected int sendStatHtml;

    /**
     * Holds value of property sendStatOffline.
     */
    protected int sendStatOffline;

    /**
     * Holds value of property isTemplate.
     */
    protected boolean isTemplate;

    /**
     * Holds value of property deliveryStat.
     */
    protected DeliveryStat deliveryStat;

    /**
     * Indicates, whether the mailing uses deleted target groups or not.
     */
    protected boolean hasDeletedTargetGroups;

	/**
	 * The names of target groups assigned to the mailing
	 */
	protected Collection<String> targetGroupsNames;

	/**
	 * The height for stats-box
	 */
	protected int frameHeight;

	/**
	 * Are there any mailing transmissions running ? ( Test- , Admin-, or Worldmailings which are currently sent ?)
	 */
	private boolean isTransmissionRunning;
	
    /**
     * Holds value of property needsTarget.
     */
    private boolean needsTarget;

    /**
     * Holds value of calculated max size of a mail that the mailing can produce.
     */
    private long approximateMaxSize;

    /**
     * Holds value of calculated max size of a mail (excluding external images) that the mailing can produce.
     */
    private long approximateMaxSizeWithoutExternalImages;

    /**
     * Holds mailing size threshold (in bytes) that triggers a warning message.
     */
    private long sizeWarningThreshold;

    /**
     * Holds mailing size threshold (in bytes) that triggers an error message.
     */
    private long sizeErrorThreshold;

    private boolean isPrioritizationDisallowed;
    
    private Map<Integer, Integer> sendStat = null;
    
    /**
     * Holds value of property mailing.
     */
    private Mailing mailing;
    
    /**
     * Holds value of property mailingtype.
     */
    private int mailingtype;
    
    /**
     * Holds value of property sendHour.
     */
    private int sendHour;
    
    /**
     * Holds value of property sendMinute.
     */
    private int sendMinute;
    
	/**
     * Holds value of property targetGroups.
     */
    private Collection<Integer> targetGroups;
    
    private int stepping = 0;
    
    private int blocksize = 0;
    
    private String bounceFilterNames;
    
    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    @Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
        try {
            TimeZone aZone = AgnUtils.getTimeZone(request);
            GregorianCalendar aDate = new GregorianCalendar(aZone);
            sendHour = aDate.get(GregorianCalendar.HOUR_OF_DAY);
            sendMinute = aDate.get(GregorianCalendar.MINUTE);
            previewForm = new PreviewForm();
            sendStat = new HashMap<>();
        } catch (Exception e) {
            // do nothing
        }
        approximateMaxSize = 0;
        approximateMaxSizeWithoutExternalImages = 0;
        sizeWarningThreshold = 0;
        sizeErrorThreshold = 0;
    }

    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param req The servlet request we are processing
     * @return errors
     */
    @Override
	public ActionErrors formSpecificValidate(ActionMapping mapping,
                                 HttpServletRequest req) {

        ActionErrors errors = new ActionErrors();
        if (action == MailingSendAction.ACTION_CONFIRM_SEND_WORLD) {
            TimeZone aZone = AgnUtils.getTimeZone(req);
            GregorianCalendar currentDate = new GregorianCalendar(aZone);
            GregorianCalendar newsendDate = new GregorianCalendar(aZone);
            newsendDate.set(Integer.parseInt(sendDate.substring(0, 4)), Integer.parseInt(sendDate.substring(4, 6)) - 1, Integer.parseInt(sendDate.substring(6, 8)), sendHour, sendMinute);
            if (currentDate.getTime().getTime() > newsendDate.getTime().getTime()) {
                errors.add("global", new ActionMessage("error.you_choose_a_time_before_the_current_time"));
            }
        }

        return errors;
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
    }

    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    public String getShortname() {
        return shortname;
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
        return action;
    }

    /**
     * Setter for property action.
     *
     * @param action New value of property action.
     */
    public void setAction(int action) {
        this.action = action;
    }

    public Map<Integer, Integer> getSendStats() {
        return sendStat;
    }

    /**
     */
    public int getSendStat(int id) {
        if (sendStat.containsKey(id)) {
            return sendStat.get(id);
        }
        return 0;
    }

    public void setSendStat(int id, int value) {
        sendStat.put(id, value);
    }

    /**
     */
    public int getSendTotal() {
        int total = 0;
        for (Integer value : sendStat.values()) {
            total += value;
        }
        return total;
    }

    /**
     * Getter for property sendStatText.
     *
     * @return Value of property sendStatText.
     */
    public int getSendStatText() {
        return sendStatText;
    }

    /**
     * Setter for property sendStatText.
     *
     * @param sendStatText New value of property sendStatText.
     */
    public void setSendStatText(int sendStatText) {
        this.sendStatText = sendStatText;
    }

    /**
     * Getter for property sendStatHtml.
     *
     * @return Value of property sendStatHtml.
     */
    public int getSendStatHtml() {
        return sendStatHtml;
    }

    /**
     * Setter for property sendStatHtml.
     *
     * @param sendStatHtml New value of property sendStatHtml.
     */
    public void setSendStatHtml(int sendStatHtml) {
        this.sendStatHtml = sendStatHtml;
    }

    /**
     * Getter for property sendStatOffline.
     *
     * @return Value of property sendStatOffline.
     */
    public int getSendStatOffline() {
        return sendStatOffline;
    }

    /**
     * Setter for property sendStatOffline.
     *
     * @param sendStatOffline New value of property sendStatOffline.
     */
    public void setSendStatOffline(int sendStatOffline) {
        this.sendStatOffline = sendStatOffline;
    }

    /**
     * Getter for property sendStatAll.
     *
     * @return Value of property sendStatAll.
     * @deprecated replaced by getSendStat(0)
     */
    @Deprecated
	public int getSendStatAll() {
        return sendStat.get(0);
    }

    /**
     * Setter for property sendStatAll.
     *
     * @param sendStatAll New value of property sendStatAll.
     * @deprecated replaced by setSendStat(0, value)
     */
    @Deprecated
	public void setSendStatAll(int sendStatAll) {
        sendStat.put(0, sendStatAll);
    }

    /**
     * Getter for property isTemplate.
     *
     * @return Value of property isTemplate.
     */
    public boolean isIsTemplate() {
        return isTemplate;
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
     * Getter for property deliveryStat.
     *
     * @return Value of property deliveryStat.
     */
    public DeliveryStat getDeliveryStat() {
        return deliveryStat;
    }

    /**
     * Setter for property deliveryStat.
     *
     * @param deliveryStat New value of property deliveryStat.
     */
    public void setDeliveryStat(DeliveryStat deliveryStat) {
        this.deliveryStat = deliveryStat;
    }

    /**
     * Getter for property mailing.
     *
     * @return Value of property mailing.
     */
    public Mailing getMailing() {
        return mailing;
    }

    /**
     * Setter for property mailing.
     *
     * @param mailing New value of property mailing.
     */
    public void setMailing(Mailing mailing) {
        this.mailing = mailing;
    }

    /**
     * Holds value of property worldMailingSend.
     */
    private boolean worldMailingSend;

    /**
     * Getter for property worldMailingSend.
     *
     * @return Value of property worldMailingSend.
     */
    public boolean isWorldMailingSend() {
        return worldMailingSend;
    }

    /**
     * Setter for property worldMailingSend.
     *
     * @param worldMailingSend New value of property worldMailingSend.
     */
    public void setWorldMailingSend(boolean worldMailingSend) {
        this.worldMailingSend = worldMailingSend;
    }

    /**
     * Getter for property mailingtype.
     *
     * @return Value of property mailingtype.
     */
    public int getMailingtype() {
        return mailingtype;
    }

    /**
     * Setter for property mailingtype.
     *
     * @param mailingtype New value of property mailingtype.
     */
    public void setMailingtype(int mailingtype) {
        this.mailingtype = mailingtype;
    }

    /**
     * Holds value of property sendDate.
     */
    private String sendDate;

    /**
     * Getter for property sendDate.
     *
     * @return Value of property sendDate.
     */
    public String getSendDate() {
        return sendDate;
    }

    /**
     * Setter for property sendDate.
     *
     * @param sendDate New value of property sendDate.
     */
    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    /**
     * Getter for property sendHour.
     *
     * @return Value of property sendHour.
     */
    public int getSendHour() {
        return sendHour;
    }

    /**
     * Setter for property sendHour.
     *
     * @param sendHour New value of property sendHour.
     */
    public void setSendHour(int sendHour) {
        this.sendHour = sendHour;
    }

    /**
     * Getter for property sendMinute.
     *
     * @return Value of property sendMinute.
     */
    public int getSendMinute() {
        return sendMinute;
    }

    /**
     * Setter for property sendMinute.
     *
     * @param sendMinute New value of property sendMinute.
     */
    public void setSendMinute(int sendMinute) {
        this.sendMinute = sendMinute;
    }

	/**
     * Getter for property frameHeight.
     *
     * @return Value of property frameHeight.
     */
	public int getFrameHeight() {
		return frameHeight;
	}

	/**
     * Setter for property frameHeight.
     *
     * @param frameHeight New value of property frameHeight.
     */
	public void setFrameHeight(int frameHeight) {
		this.frameHeight = frameHeight;
	}

	/**
     * Getter for property targetGroups.
     *
     * @return Value of property targetGroupsNames.
     */
	public Collection<String> getTargetGroupsNames() {
		return targetGroupsNames;
	}

	/**
     * Setter for property targetGroupsNames.
     *
     * @param targetGroupsNames New value of property targetGroupsNames.
     */
	public void setTargetGroupsNames(Collection<String> targetGroupsNames) {
		this.targetGroupsNames = targetGroupsNames;
	}

    /**
     * Getter for property targetGroups.
     *
     * @return Value of property targetGroups.
     */
    public Collection<Integer> getTargetGroups() {
        return targetGroups;
    }

    /**
     * Setter for property targetGroups.
     *
     * @param targetGroups New value of property targetGroups.
     */
    public void setTargetGroups(Collection<Integer> targetGroups) {
        this.targetGroups = targetGroups;
    }

    /**
     * Getter for property emailFormat.
     *
     * @return Value of property emailFormat.
     */
    public int getEmailFormat() {
        return emailFormat;
    }

    /**
     * Setter for property emailFormat.
     *
     * @param emailFormat New value of property emailFormat.
     */
    public void setEmailFormat(int emailFormat) {
        this.emailFormat = emailFormat;
    }

    /**
     * Holds value of property mailinglistID.
     */
    private int mailinglistID;

    /**
     * Getter for property mailinglistID.
     *
     * @return Value of property mailinglistID.
     */
    public int getMailinglistID() {
        return mailinglistID;
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
     * Getter for property stepping.
     *
     * @return Value of property stepping.
     */
    public int getStepping() {
        return stepping;
    }
    
    public int getStep() {
    	// Backward compatibility
    	return getStepping();
    }

    /**
     * Setter for property stepping.
     *
     * @param stepping New value of property stepping.
     */
    public void setStepping(int stepping) {
        this.stepping = stepping;
    }
    
    public void setStep(int stepping) {
    	// Backward compatibility
    	setStepping(stepping);
    }
    
    /**
     * Getter for property blocksize.
     *
     * @return Value of property blocksize.
     */
    public int getBlocksize() {
        return blocksize;
    }

    /**
     * Setter for property blocksize.
     *
     * @param blocksize New value of property blocksize.
     */
    public void setBlocksize(int blocksize) {
        this.blocksize = blocksize;
    }

    public boolean isLocked() {
    	// dirty workaround, mailing could be null!
    	if (mailing == null) {
    		return true;
    	}

        return mailing.getLocked() != 0;
    }

    public void setLocked(boolean locked) {
        mailing.setLocked(locked ? 1 : 0);
    }

	public void setHasDeletedTargetGroups(boolean hasDeletedTargetGroups) {
		this.hasDeletedTargetGroups = hasDeletedTargetGroups;
	}

	public boolean getHasDeletedTargetGroups() {
		return hasDeletedTargetGroups;
	}


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public boolean isTransmissionRunning() {
		return isTransmissionRunning;
	}

	public void setTransmissionRunning(boolean isTransmissionRunning) {
		this.isTransmissionRunning = isTransmissionRunning;
	}

    /**
     * Getter for property needsTarget.
     *
     * @return Value of property needsTarget.
     */
    public boolean isNeedsTarget() {
        return needsTarget;
    }

    /**
     * Setter for property needsTarget.
     *
     * @param needsTarget New value of property needsTarget.
     */
    public void setNeedsTarget(boolean needsTarget) {
        this.needsTarget = needsTarget;
    }

    public long getApproximateMaxSize() {
        return approximateMaxSize;
    }

    public void setApproximateMaxSize(long approximateMaxSize) {
        this.approximateMaxSize = approximateMaxSize;
    }

    public long getApproximateMaxSizeWithoutExternalImages() {
        return approximateMaxSizeWithoutExternalImages;
    }

    public void setApproximateMaxSizeWithoutExternalImages(long approximateMaxSizeWithoutExternalImages) {
        this.approximateMaxSizeWithoutExternalImages = approximateMaxSizeWithoutExternalImages;
    }

    public long getSizeWarningThreshold() {
        return sizeWarningThreshold;
    }

    public void setSizeWarningThreshold(long sizeWarningThreshold) {
        this.sizeWarningThreshold = sizeWarningThreshold;
    }

    public long getSizeErrorThreshold() {
        return sizeErrorThreshold;
    }

    public void setSizeErrorThreshold(long sizeErrorThreshold) {
        this.sizeErrorThreshold = sizeErrorThreshold;
    }

    public boolean isPrioritizationDisallowed() {
        return isPrioritizationDisallowed;
    }

    public boolean getIsPrioritizationDisallowed() {
        return isPrioritizationDisallowed;
    }

    public void setPrioritizationDisallowed(boolean prioritizationDisallowed) {
        isPrioritizationDisallowed = prioritizationDisallowed;
    }

    public void setIsPrioritizationDisallowed(boolean isPrioritizationDisallowed) {
        this.isPrioritizationDisallowed = isPrioritizationDisallowed;
    }
    
    public void setBounceFilterNames(String bounceFilterNames) {
        this.bounceFilterNames = bounceFilterNames;
    }
    
    public String getBounceFilterNames() {
        return bounceFilterNames;
    }
    
    public PreviewForm getPreviewForm() {
        return previewForm;
    }
}
