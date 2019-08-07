/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.Mailinglist;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.agnitas.beans.ComCompany;
import com.agnitas.emm.core.commons.validation.AgnitasEmailValidator;

public class ComAdminForm extends StrutsFormBase {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComAdminForm.class);

	private static final long serialVersionUID = -5934996047571867787L;
	protected String statEmail;
    protected String companyName;
	protected String initialCompanyName;
    protected String email;
    protected int layoutBaseId;
    protected int startPage;
    protected int dashboardMailingsView;
    protected int navigationLocation;
    protected int mailingSettingsView;
    protected int livePreviewPosition;
    protected int statisticLoadType;
    protected int gender = 2;
    protected String title;
    protected String searchFirstName;
    protected String searchLastName;
    protected String searchEmail;
    protected String searchCompany;
    protected String filterCompanyId;
    protected String filterMailinglistId;
    protected String filterAdminGroupId;
    protected String filterLanguage;
    protected String firstname;
    private Set<Integer> bulkIDs = new HashSet<>();
    private int mailinglistID;
    private List<Mailinglist> mailinglists;
    private Set<Integer> disabledMailinglistsIds = new HashSet<>();
    private List<ComCompany> companies;
    private List<AdminGroup> adminGroups;
    private boolean isOneTimePassword;
    protected int action;
	protected int previousAction;
	protected int adminID = 0;
	protected int companyID = 1;
	protected int customerID;
	protected int layoutID = 0;
	protected int mailingContentView;
	protected String username;
	protected String password;
	protected String fullname;
	protected String adminTimezone;
	private String language;
	private Locale adminLocale;
	private String passwordConfirm;
	private String adminPhone;

	/**
	 * Holds value of property userRights.
	 */
	private Set<String> userRights;

	/**
	 * Holds value of property groupID.
	 */
	private int groupID = 0;
	private ActionMessages messages;
	
	// constructor:
	public ComAdminForm() {
        super();
        for (int i = 0; i < 4; i++) {
			columnwidthsList.add("-1");
		}
        if (this.columnwidthsList == null) {
            this.columnwidthsList = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                columnwidthsList.add("-1");
            }
        }
    }
	
	/**
	 * Reset all properties to their default values.
	 *
	 * @param mapping
	 *            The mapping used to select this instance
	 * @param request
	 *            The servlet request we are processing
	 */
	@Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		setAdminLocale(Locale.GERMANY);
		setAdminTimezone("Europe/Berlin");
		userRights = new HashSet<>();
	}
	
	/**
	 * Validate the properties that have been set from this HTTP request, and
	 * return an <code>ActionMessages</code> object that encapsulates any
	 * validation errors that have been found. If no errors are found, return
	 * <code>null</code> or an <code>ActionMessages</code> object with no
	 * recorded error messages.
	 * 
	 * @param mapping
	 *            The mapping used to select this instance
	 * @param request
	 *            The servlet request we are processing
	 * @return errors
	 */
	@Override
	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors actionErrors = new ActionErrors();
		boolean doNotDelete = request.getParameter("delete") == null || request.getParameter("delete").isEmpty();
		if (doNotDelete && (action == ComAdminAction.ACTION_SAVE || action == ComAdminAction.ACTION_NEW)) {
			if (username.length() < 3) {
				actionErrors.add("username", new ActionMessage("error.username.tooShort"));
			}

			if (!password.equals(passwordConfirm)) {
				actionErrors.add("password", new ActionMessage("error.password.mismatch"));
			}

			if (username.length() > 180) {
				actionErrors.add("username", new ActionMessage("error.username.tooLong"));
			}
			if (StringUtils.isBlank(fullname) || fullname.length() < 2) {
				actionErrors.add("fullname", new ActionMessage("error.name.too.short"));
			} else if (fullname.length() > 100) {
				actionErrors.add("fullname", new ActionMessage("error.username.tooLong"));
			}

			if (StringUtils.isBlank(firstname) || firstname.length() < 2) {
				actionErrors.add("firstname", new ActionMessage("error.name.too.short"));
			} else if (firstname.length() > 100) {
				actionErrors.add("firstname", new ActionMessage("error.username.tooLong"));
			}

			if (StringUtils.isBlank(companyName) || companyName.length() < 2) {
				actionErrors.add("companyName", new ActionMessage("error.company.tooShort"));
			} else if (companyName.length() > 100) {
				actionErrors.add("companyName", new ActionMessage("error.company.tooLong"));
			}

			if (GenericValidator.isBlankOrNull(this.email) || !AgnitasEmailValidator.getInstance().isValid(email)) {
				actionErrors.add("mailForReport", new ActionMessage("error.invalid.email"));
			}
		}

		if (action == ComAdminAction.ACTION_SAVE_RIGHTS) {
			Enumeration<String> aEnum = request.getParameterNames();
			while (aEnum.hasMoreElements()) {
				String paramName = aEnum.nextElement();
				if (paramName.startsWith("user_right")) {
					String value = request.getParameter(paramName);
					if (value != null) {
						if (value.startsWith("user__")) {
							value = value.substring(6);
							userRights.add(value);
						}
					}
				}
			}
		}
		return actionErrors;
	}
	
	@Override
	protected ActionMessages checkForHtmlTags(HttpServletRequest request) {
		if (action != ComAdminAction.ACTION_VIEW_WITHOUT_LOAD) {
			return super.checkForHtmlTags(request);
		}
		return new ActionErrors();
	}

    public boolean isOneTimePassword() {
        return isOneTimePassword;
    }

    public void setOneTimePassword(boolean oneTimePassword) {
        isOneTimePassword = oneTimePassword;
    }

    public String getStatEmail() {
		return statEmail;
	}

    public String getEmail() {
        return this.email;
    }

    public String getCompanyName() {
        return this.companyName;
    }

	public void setStatEmail(String statEmail) {
		this.statEmail = statEmail;
	}

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

	public int getLayoutBaseId() {
		return layoutBaseId;
	}

	public void setLayoutBaseId(int layoutBaseId) {
		this.layoutBaseId = layoutBaseId;
	}

	public String getInitialCompanyName() {
		return initialCompanyName;
	}

	public void setInitialCompanyName(String initialCompanyName) {
		this.initialCompanyName = initialCompanyName;
	}

    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getDashboardMailingsView() {
        return dashboardMailingsView;
    }

    public void setDashboardMailingsView(int dashboardMailingsView) {
        this.dashboardMailingsView = dashboardMailingsView;
    }

    public int getNavigationLocation() {
        return navigationLocation;
    }

    public void setNavigationLocation(int navigationLocation) {
        this.navigationLocation = navigationLocation;
    }

    public int getMailingSettingsView() {
        return mailingSettingsView;
    }

    public void setMailingSettingsView(int mailingSettingsView) {
        this.mailingSettingsView = mailingSettingsView;
    }

    public int getLivePreviewPosition() {
        return livePreviewPosition;
    }

    public void setLivePreviewPosition(int livePreviewPosition) {
        this.livePreviewPosition = livePreviewPosition;
    }

    public int getStatisticLoadType() {
        return statisticLoadType;
    }

    public void setStatisticLoadType(int statisticLoadType) {
        this.statisticLoadType = statisticLoadType;
    }

    public String getSearchFirstName() {
        return searchFirstName;
    }

    public void setSearchFirstName(String searchFirstName) {
        this.searchFirstName = searchFirstName;
    }

    public String getSearchLastName() {
        return searchLastName;
    }

    public void setSearchLastName(String searchLastName) {
        this.searchLastName = searchLastName;
    }

    public String getSearchEmail() {
        return searchEmail;
    }

    public void setSearchEmail(String searchEmail) {
        this.searchEmail = searchEmail;
    }

    public String getSearchCompany() {
        return searchCompany;
    }

    public void setSearchCompany(String searchCompany) {
        this.searchCompany = searchCompany;
    }

    public String getFilterCompanyId() {
        return filterCompanyId;
    }

    public String getFilterMailinglistId() {
        return filterMailinglistId;
    }

    public void setFilterMailinglistId(String filterMailinglistId) {
        this.filterMailinglistId = filterMailinglistId;
    }

    public void setFilterCompanyId(String filterCompanyId) {
        this.filterCompanyId = filterCompanyId;
    }

    public String getFilterAdminGroupId() {
        return filterAdminGroupId;
    }

    public void setFilterAdminGroupId(String filterAdminGroupId) {
        this.filterAdminGroupId = filterAdminGroupId;
    }

    public String getFilterLanguage() {
        return filterLanguage;
    }

    public void setFilterLanguage(String filterLanguage) {
        this.filterLanguage = filterLanguage;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
    public void setBulkID(int id, String value) {
        if (AgnUtils.interpretAsBoolean(value))
            this.bulkIDs.add(id);
    }
    public String getBulkID(int id) {
        return this.bulkIDs.contains(id) ? "on" : "";
    }
    public Set<Integer> getBulkIds() {
        return this.bulkIDs;
    }
    public void clearBulkIds() {
        this.bulkIDs.clear();
    }

    public List<Mailinglist> getMailinglists() {
        return mailinglists;
    }

    public void setMailinglists(List<Mailinglist> mailinglists) {
        this.mailinglists = mailinglists;
    }

    public Set<Integer> getDisabledMailinglistsIds() {
        return disabledMailinglistsIds;
    }

    public void setEnabledMailinglist(int id, String value){
        if (AgnUtils.interpretAsBoolean(value)){
            disabledMailinglistsIds.remove(id);
        } else {
            disabledMailinglistsIds.add(id);
        }
    }

    public String getEnabledMailinglist(int id){
        return disabledMailinglistsIds.contains(id) ? "" : "on";
    }

    public int getMailinglistID() {
        return mailinglistID;
    }

    public void setMailinglistID(int mailinglistID) {
        this.mailinglistID = mailinglistID;
    }

    public List<ComCompany> getCompanies() {
        return companies;
    }

    public void setCompanies(List<ComCompany> companies) {
        this.companies = companies;
    }

    public List<AdminGroup> getAdminGroups() {
        return adminGroups;
    }

    public void setAdminGroups(List<AdminGroup> adminGroups) {
        this.adminGroups = adminGroups;
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
	 * Getter for property adminID.
	 *
	 * @return Value of property adminID.
	 */
	public int getAdminID() {
		return adminID;
	}

	/**
	 * Getter for property username.
	 *
	 * @return Value of property username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Getter for property password.
	 *
	 * @return Value of property password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Getter for property companyID.
	 *
	 * @return Value of property companyID.
	 */
	public int getCompanyID() {
		return companyID;
	}

	/**
	 * Getter for property fullname.
	 *
	 * @return Value of property fullname.
	 */
	public String getFullname() {
		return fullname;
	}

	/**
	 * Getter for property customerID.
	 *
	 * @return Value of property customerID.
	 */
	public int getCustomerID() {
		return customerID;
	}

	/**
	 * Getter for property adminTimezone.
	 *
	 * @return Value of property adminTimezone.
	 */
	public String getAdminTimezone() {
		return adminTimezone;
	}

	/**
	 * Getter for property layoutID.
	 *
	 * @return Value of property layoutID.
	 */
	public int getLayoutID() {
		return layoutID;
	}

	/**
	 * Getter for the preferred mailing content view type.
	 *
	 * @return Value of property mailingContentView.
	 */
	public int getMailingContentView() {
		return mailingContentView;
	}

	/**
	 * Getter for property language.
	 *
	 * @return Value of property language.
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Getter for property adminLocale.
	 *
	 * @return Value of property adminLocale.
	 */
	public Locale getAdminLocale() {
		return adminLocale;
	}

	/**
	 * Getter for property passwordConfirm.
	 *
	 * @return Value of property passwordConfirm.
	 */
	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	/**
	 * Setter for property action.
	 *
	 * @param action
	 *            New value of property action.
	 */
	public void setAction(int action) {
		this.action = action;
	}

	/**
	 * Setter for property adminID.
	 *
	 * @param adminID
	 *            New value of property adminID.
	 */
	public void setAdminID(int adminID) {
		this.adminID = adminID;
	}

	/**
	 * Setter for property username.
	 *
	 * @param username
	 *            New value of property username.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Setter for property password.
	 *
	 * @param password
	 *            New value of property password.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Setter for property companyID.
	 *
	 * @param companyID
	 *            New value of property companyID.
	 */
	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}

	/**
	 * Setter for property fullname.
	 *
	 * @param fullname
	 *            New value of property fullname.
	 */
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	/**
	 * Setter for property customerID.
	 *
	 * @param customerID
	 *            New value of property customerID.
	 */
	public void setCustomerID(int customerID) {
		this.customerID = customerID;
	}

	/**
	 * Setter for property admineTimezone.
	 *
	 * @param timezone
	 *            New value of property adminTimezone.
	 */
	public void setAdminTimezone(String timezone) {
		this.adminTimezone = timezone;
	}

	/**
	 * Setter for property layoutID.
	 *
	 * @param layoutID
	 *            New value of property layoutID.
	 */
	public void setLayoutID(int layoutID) {
		this.layoutID = layoutID;
	}

	/**
	 * Setter for the preferred mailing content view type.
	 *
	 * @param mailingContentView
	 *            New value of property mailingContentView.
	 */
	public void setMailingContentView(int mailingContentView) {
		this.mailingContentView = mailingContentView;
	}
	
	/**
	 * Setter for property language.
	 *
	 * @param language
	 *            New value of property language.
	 */
	public void setLanguage(String language) {
		this.language = language;
		if (language != null) {
			int aPos = language.indexOf('_');
			String lang = language.substring(0, aPos);
			String country = language.substring(aPos + 1);
			if (logger.isInfoEnabled()) {
				logger.info("Got lang: " + lang + " Country: " + country);
			}
			adminLocale = new Locale(lang, country);
		}
	}

	/**
	 * Setter for property adminLocale.
	 *
	 * @param adminLocale
	 *            New value of property adminLocale.
	 */
	public void setAdminLocale(Locale adminLocale) {
		this.adminLocale = adminLocale;
		if (adminLocale != null) {
			language = adminLocale.toString();
		}
	}

	/**
	 * Setter for property passwordConfirm.
	 *
	 * @param passwordConfirm
	 *            New value of property passwordConfirm.
	 */
	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	/**
	 * Getter for property userRights.
	 *
	 * @return Value of property userRights.
	 */
	public Set<String> getUserRights() {
		return userRights;
	}

	/**
	 * Getter for property groupID.
	 *
	 * @return Value of property groupID.
	 */
	public int getGroupID() {
		return groupID;
	}

	/**
	 * Setter for property groupID.
	 *
	 * @param groupID
	 *            New value of property groupID.
	 */
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	public int getPreviousAction() {
		return previousAction;
	}

	public void setPreviousAction(int previousAction) {
		this.previousAction = previousAction;
	}

	public ActionMessages getMessages() {
		return messages;
	}

	public void setMessages(ActionMessages messages) {
		this.messages = messages;
	}

	public String getAdminPhone() {
		return adminPhone;
	}

	public void setAdminPhone(String adminPhone) {
		this.adminPhone = adminPhone;
	}
	
}
