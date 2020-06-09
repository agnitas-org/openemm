/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.dao.MailingDao;
import org.agnitas.dao.TrackableLinkDao;
import org.agnitas.target.TargetOperator;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.StrutsFormBase;
import org.agnitas.web.forms.helper.EmptyStringFactory;
import org.agnitas.web.forms.helper.ZeroIntegerFactory;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.FactoryUtils;
import org.apache.commons.collections4.list.GrowthList;
import org.apache.commons.collections4.list.LazyList;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.web.ComTargetAction;
import com.agnitas.web.forms.ComTargetForm;

public class TargetForm extends StrutsFormBase {
    
	private static final Factory<String> emptyStringFactory = new EmptyStringFactory();
	private static final Factory<Integer> zeroIntegerFactory = new ZeroIntegerFactory();
	private static final Factory<TargetOperator[]> nullFactory = FactoryUtils.nullFactory();
	private static final Factory<List<TrackableLinkListItem>> nullFactory2 = FactoryUtils.nullFactory();
	
	public static final int COLUMN_TYPE_STRING = 0;
	public static final int COLUMN_TYPE_NUMERIC = 1;
	public static final int COLUMN_TYPE_DATE = 2;
	public static final int COLUMN_TYPE_INTERVAL_MAILING = 3;
	public static final int COLUMN_TYPE_MAILING_RECEIVED = 4;
	public static final int COLUMN_TYPE_MAILING_OPENED = 5;
	public static final int COLUMN_TYPE_MAILING_CLICKED = 6;
	
    private static final long serialVersionUID = 45877020863407141L;
	private String shortname;
    private String description;
    private int targetID;
    private int action;
    private int numOfRecipients;
    
    // defined rules
    private List<String> columnAndTypeList;
    private List<Integer> chainOperatorList;
    private List<Integer> parenthesisOpenedList;
    private List<Integer> primaryOperatorList;
    private List<String> primaryValueList;
    private List<Integer> parenthesisClosedList;
    private List<String> dateFormatList;
    private List<Integer> secondaryOperatorList;
    private List<String> secondaryValueList;
    private List<TargetOperator[]> validTargetOperatorsList;
    private List<String> columnNameList;
    private List<Integer> columnTypeList;
    private List<List<TrackableLinkListItem>> validLinksList;

    // new rules
    private String columnAndTypeNew;
    private int chainOperatorNew;
    private int parenthesisOpenedNew;
    private int primaryOperatorNew;
    private String primaryValueNew;
    private int parenthesisClosedNew;
    private String dateFormatNew;
    private int secondaryOperatorNew;
    private String secondaryValueNew;
    
    private boolean addTargetNode;
    private int targetNodeToRemove;
    
    /**
     * Last action we came from.
     */
    private int previousAction;
    
    /**
     * The list size a user prefers while viewing a table 
     */
    private int preferredListSize = 20; 
    
    /**
     * The list size has been loaded from the admin's properties 
     */
    private boolean preferredListSizeLoaded = true;

    private boolean isShowStatistic = false;
    
    public TargetForm() {
        columnAndTypeList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), emptyStringFactory));
        chainOperatorList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), zeroIntegerFactory));
        parenthesisOpenedList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), zeroIntegerFactory));
        primaryOperatorList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), zeroIntegerFactory));
        primaryValueList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), emptyStringFactory));
        parenthesisClosedList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), zeroIntegerFactory));
        dateFormatList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), emptyStringFactory));
        secondaryOperatorList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), zeroIntegerFactory));
        secondaryValueList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), emptyStringFactory));
        validTargetOperatorsList = GrowthList.growthList(LazyList.lazyList(new ArrayList<TargetOperator[]>(), nullFactory));
        columnNameList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), emptyStringFactory));
        columnTypeList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), zeroIntegerFactory));
        validLinksList = GrowthList.growthList(LazyList.lazyList(new ArrayList<List<TrackableLinkListItem>>(), nullFactory2));
    }
    
    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);

        this.targetID = 0;
		this.action = StrutsActionBase.ACTION_SAVE;
        Locale aLoc = AgnUtils.getLocale(request);
        
        MessageResources text=(MessageResources)this.getServlet().getServletContext().getAttribute(org.apache.struts.Globals.MESSAGES_KEY);
        //MessageResources text=this.getServlet().getResources();
        
        this.shortname = text.getMessage(aLoc, "default.Name");
        this.description = text.getMessage(aLoc, "default.description");
        this.isShowStatistic = false;
        
        // Reset form fields for new rule
        clearNewRuleData();
    }

    public void clearNewRuleData() {
        columnAndTypeNew = null;
        chainOperatorNew = 0;
        parenthesisOpenedNew = 0;
        primaryOperatorNew = 0;
        primaryValueNew = null;
        parenthesisClosedNew = 0;
        dateFormatNew = null;
        secondaryOperatorNew = 0;
        secondaryValueNew = null;
    }
    
    public void clearRules() {
        columnAndTypeList.clear();
        chainOperatorList.clear();
        parenthesisOpenedList.clear();
        primaryOperatorList.clear();
        primaryValueList.clear();
        parenthesisClosedList.clear();
        dateFormatList.clear();
        secondaryOperatorList.clear();
        secondaryValueList.clear();
        columnNameList.clear();
        columnTypeList.clear();
        validLinksList.clear();
    }

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
        ActionErrors errors = new ActionErrors();

        if (getAction() == ComTargetAction.ACTION_SAVE) {
            boolean ruleAdded = getAddTargetNode();
            boolean ruleRemoved = getTargetNodeToRemove() != -1;

            // Validate balance in the action class when rule added/removed
            if (!ruleAdded && !ruleRemoved && !checkParenthesisBalance()) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.bracketbalance"));
            }

            if (StringUtils.isEmpty(shortname) || shortname.length()<3) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.name.too.short"));
            }

            /* Currently disabled. Due to two parallel editors, empty target groups cannot be detected.
            if(this.getNumTargetNodes() == 0 && !this.getAddTargetNode()) { 
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.norule"));
            }
            */
        }

        return errors;
    }

    public boolean checkParenthesisBalance() {
        int balance = 0;

    	int lastIndex = this.getNumTargetNodes();

    	for (int index = 0; index < lastIndex; index++) {
            balance += this.getParenthesisOpened(index);
            balance -= this.getParenthesisClosed(index);

            if (balance < 0) {
                return false;
            }
    	}

    	return balance == 0;
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
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Getter for property targetID.
     *
     * @return Value of property targetID.
     */
    public int getTargetID() {
        return this.targetID;
    }
    
    /**
     * Setter for property targetID.
     *
     * @param targetID New value of property targetID.
     */
    public void setTargetID(int targetID) {
        this.targetID = targetID;
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
     * Getter for property numOfRecipients.
     *
     * @return Value of property numOfRecipients.
     */
	public int getNumOfRecipients() {
		return numOfRecipients;
	}

	/**
     * Setter for property numOfRecipients.
     *
     * @param numOfRecipients New value of property numOfRecipients.
     */
	public void setNumOfRecipients(int numOfRecipients) {
		this.numOfRecipients = numOfRecipients;
	}

	public int getPreferredListSize() {
		return preferredListSize;
	}

	public void setPreferredListSize(int preferredListSize) {
		this.preferredListSize = preferredListSize;
	}

	public boolean isPreferredListSizeLoaded() {
		return preferredListSizeLoaded;
	}

	public void setPreferredListSizeLoaded(boolean preferredListSizeLoaded) {
		this.preferredListSizeLoaded = preferredListSizeLoaded;
	}

	public int getPreviousAction() {
		return previousAction;
	}

	public void setPreviousAction(int previousAction) {
		this.previousAction = previousAction;
	}
	
	public void removeRule(int index) {
        safeRemove(columnAndTypeList, index);
        safeRemove(chainOperatorList, index);
        safeRemove(parenthesisOpenedList, index);
        safeRemove(primaryOperatorList, index);
        safeRemove(primaryValueList, index);
        safeRemove(parenthesisClosedList, index);
        safeRemove(dateFormatList, index);
        safeRemove(secondaryOperatorList, index);
        safeRemove(secondaryValueList, index);
        safeRemove(columnNameList, index);
        safeRemove(columnTypeList, index);
	}
	
	/**
	 * Removes and index safely from list. If index does not exists, nothing happens.
	 * 
	 * @param list list to remove index from
	 * @param index index to be removed
	 */
	private void safeRemove(List<?> list, int index) {
		if( list.size() > index && index >= 0)
			list.remove(index);
	}
	
	public int getNumTargetNodes() {
		return this.columnAndTypeList.size();
	}
	
	public String getColumnAndType(int index) {
		return this.columnAndTypeList.get(index);
	}
	
	public void setColumnAndType(int index, String value) {
		this.columnAndTypeList.set(index, value);
	}
	
	public int getChainOperator(int index) {
		return this.chainOperatorList.get(index);
	}
	
	public void setChainOperator(int index, int value) {
		this.chainOperatorList.set(index, value);
	}
	
	public int getParenthesisOpened(int index) {
		return this.parenthesisOpenedList.get(index);
	}
	
	public void setParenthesisOpened(int index, int value) {
		this.parenthesisOpenedList.set(index, value);
	}
	
	public int getPrimaryOperator(int index) {
		return this.primaryOperatorList.get(index);
	}
	
	public void setPrimaryOperator(int index, int value) {
		this.primaryOperatorList.set(index, value);
	}
	
	public String getPrimaryValue(int index) {
		return this.primaryValueList.get(index);
	}
	
	public void setPrimaryValue(int index, String value) {
		this.primaryValueList.set(index, value);
	}
	
	public int getParenthesisClosed(int index) {
		return this.parenthesisClosedList.get(index);
	}
	
	public void setParenthesisClosed(int index, int value) {
		this.parenthesisClosedList.set(index, value);
	}
	
	public String getDateFormat(int index) {
		return this.dateFormatList.get(index);
	}
	
	public void setDateFormat(int index, String value) {
		this.dateFormatList.set(index, value);
	}
	
	public int getSecondaryOperator(int index) {
		return this.secondaryOperatorList.get(index);
	}
	
	public void setSecondaryOperator(int index, int value) {
		this.secondaryOperatorList.set(index, value);
	}
	
	public String getSecondaryValue(int index) {
		return this.secondaryValueList.get(index);
	}
	
	public void setSecondaryValue(int index, String value) {
		this.secondaryValueList.set(index, value);
	}
	
	public List<String> getAllColumnsAndTypes() {
		return this.columnAndTypeList;
	}
	
	public void setValidTargetOperators(int index, TargetOperator[] operators) {
		this.validTargetOperatorsList.set(index, operators);
	}
	
	public TargetOperator[] getValidTargetOperators(int index) {
		return this.validTargetOperatorsList.get(index);
	}
	
	public void setColumnName(int index, String value) {
		this.columnNameList.set(index, value);
	}
	
	public String getColumnName(int index) {
		return this.columnNameList.get(index);
	}
	
	public void setColumnType(int index, int type) {
		this.columnTypeList.set(index, type);
	}
	
	public int getColumnType(int index) {
		return this.columnTypeList.get(index);
	}

	public String getColumnAndTypeNew() {
		return columnAndTypeNew;
	}

	public void setColumnAndTypeNew(String columnAndTypeNew) {
		this.columnAndTypeNew = columnAndTypeNew;
	}

	public int getChainOperatorNew() {
		return chainOperatorNew;
	}

	public void setChainOperatorNew(int chainOperatorNew) {
		this.chainOperatorNew = chainOperatorNew;
	}

	public int getParenthesisOpenedNew() {
		return parenthesisOpenedNew;
	}

	public void setParenthesisOpenedNew(int parenthesisOpenedNew) {
		this.parenthesisOpenedNew = parenthesisOpenedNew;
	}

	public int getPrimaryOperatorNew() {
		return primaryOperatorNew;
	}

	public void setPrimaryOperatorNew(int primaryOperatorNew) {
		this.primaryOperatorNew = primaryOperatorNew;
	}

	public String getPrimaryValueNew() {
		return primaryValueNew;
	}

	public void setPrimaryValueNew(String primaryValueNew) {
		this.primaryValueNew = primaryValueNew;
	}

	public int getParenthesisClosedNew() {
		return parenthesisClosedNew;
	}

	public void setParenthesisClosedNew(int parenthesisClosedNew) {
		this.parenthesisClosedNew = parenthesisClosedNew;
	}

	public String getDateFormatNew() {
		return dateFormatNew;
	}

	public void setDateFormatNew(String dateFormatNew) {
		this.dateFormatNew = dateFormatNew;
	}

	public int getSecondaryOperatorNew() {
		return secondaryOperatorNew;
	}

	public void setSecondaryOperatorNew(int secondaryOperatorNew) {
		this.secondaryOperatorNew = secondaryOperatorNew;
	}

	public String getSecondaryValueNew() {
		return secondaryValueNew;
	}

	public void setSecondaryValueNew(String secondaryValueNew) {
		this.secondaryValueNew = secondaryValueNew;
	}
	
	public void setAddTargetNode( boolean addTargetNode) {
		this.addTargetNode = addTargetNode;
	}
	
	public boolean getAddTargetNode() {
		return this.addTargetNode;
	}
	
	public void setTargetNodeToRemove( int targetNodeToRemove) {
		this.targetNodeToRemove = targetNodeToRemove;
	}
	
	public int getTargetNodeToRemove() {
		return this.targetNodeToRemove;
	}
	
	public List<TrackableLinkListItem> getValidLinks(int index) {
		return this.validLinksList.get(index);
	}
	
	public void setValidLinks(int index, List<TrackableLinkListItem> links) {
		this.validLinksList.set(index, links);
	}

    public boolean isShowStatistic() {
        return isShowStatistic;
    }

    public void setIsShowStatistic(boolean isShowStatistic) {
        this.isShowStatistic = isShowStatistic;
    }

	// TODO: What has this method to do with the GUI? FormBeans are for data transport JSP -> Action only!!!
    public void addMailings(HttpServletRequest request, MailingDao mailingDao, TrackableLinkDao trackableLinkDao) {
        int companyId = AgnUtils.getCompanyID(request);

        Map<Integer, List<TrackableLinkListItem>> mailingTrackableLinks = new HashMap<>();

        // Load trackable links for used (referenced from an existing rules) mailings only.
        // The trackable links for the other mailings can be loaded dynamically.
        for (int index = 0; index < getNumTargetNodes(); index++) {
            int columnType = getColumnType(index);
            if (columnType == ComTargetForm.COLUMN_TYPE_MAILING_CLICKED_SPECIFIC_LINK) {
                int mailingID = 0;
                if (StringUtils.isNotBlank(getPrimaryValue(index))) {
                    mailingID = Integer.parseInt(getPrimaryValue(index));
                }
                List<TrackableLinkListItem> mailingLinks = mailingTrackableLinks.get(mailingID);
                if (mailingLinks == null) {
                    mailingLinks = trackableLinkDao.listTrackableLinksForMailing(companyId, mailingID);
                    mailingTrackableLinks.put(mailingID, mailingLinks);
                }
                setValidLinks(index, mailingLinks);
            }
        }

        request.setAttribute("all_mailings", mailingDao.getLightweightMailings(companyId));
        request.setAttribute("interval_mailings", mailingDao.getLightweightIntervalMailings(companyId));
        request.setAttribute("all_mailings_urls", mailingTrackableLinks);
    }

    @Override
    protected void loadNonFormDataForErrorView(ActionMapping mapping, HttpServletRequest request) {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
        addMailings(request, (MailingDao) context.getBean("MailingDao"), (TrackableLinkDao) context.getBean("TrackableLinkDao"));
    }
}
