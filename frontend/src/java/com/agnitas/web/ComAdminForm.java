/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class ComAdminForm extends StrutsFormBase {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComAdminForm.class);

	private static final long serialVersionUID = -5934996047571867787L;
	private String statEmail;
	private String companyName;
	private String initialCompanyName;
	private String email;
	private int layoutBaseId;
	private int dashboardMailingsView;
    private int mailingSettingsView;
    private int livePreviewPosition;
	private int statisticLoadType;
    private int gender = 2;
	protected String title;
    private String firstname;
    private String action;
	private int adminID = 0;
	private int companyID = 1;
	private int mailingContentView;
	private String username;
	private String password;
	private String fullname;
	private String adminTimezone;
	private String language;
	private Locale adminLocale;
	private String passwordConfirm;
	private Set<String> userRights;
	private String[] groupIDs = new String[]{};

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
		groupIDs = new String[]{};
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
		if (doNotDelete && "save".equals(action)) {
			if (StringUtils.trimToNull(username) == null || StringUtils.trimToNull(username).length() < 3) {
				actionErrors.add("username", new ActionMessage("error.username.tooShort"));
			} else if (StringUtils.trimToNull(username).length() > 180) {
				actionErrors.add("username", new ActionMessage("error.username.tooLong"));
			}

			if (!StringUtils.equals(password, passwordConfirm)) {
				actionErrors.add("password", new ActionMessage("error.password.mismatch"));
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

			if (StringUtils.isBlank(StringUtils.trimToNull(StringUtils.lowerCase(email))) || !AgnUtils.isEmailValid(StringUtils.trimToNull(StringUtils.lowerCase(email)))) {
				actionErrors.add("mailForReport", new ActionMessage("error.invalid.email"));
			}

			statEmail = StringUtils.defaultIfEmpty(statEmail, "");
			if (StringUtils.isNotBlank(StringUtils.trimToNull(StringUtils.lowerCase(statEmail))) && !AgnUtils.isEmailValid(StringUtils.trimToNull(StringUtils.lowerCase(statEmail)))) {
				actionErrors.add("statEmail", new ActionMessage("error.invalid.email.statistics"));
			}
		}
		return actionErrors;
	}

    public String getStatEmail() {
		return StringUtils.trimToNull(StringUtils.lowerCase(statEmail));
	}

    public String getEmail() {
        return StringUtils.trimToNull(StringUtils.lowerCase(email));
    }

    public String getCompanyName() {
        return this.companyName;
    }

	public void setStatEmail(String statEmail) {
		this.statEmail = StringUtils.trimToNull(StringUtils.lowerCase(statEmail));
	}

    public void setEmail(String email) {
        this.email = StringUtils.trimToNull(StringUtils.lowerCase(email));
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

    public int getDashboardMailingsView() {
        return dashboardMailingsView;
    }

    public void setDashboardMailingsView(int dashboardMailingsView) {
        this.dashboardMailingsView = dashboardMailingsView;
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
	/**
	 * Getter for property action.
	 *
	 * @return Value of property action.
	 */
	public String getAction() {
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
		return StringUtils.trimToNull(username);
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
	 * Getter for property adminTimezone.
	 *
	 * @return Value of property adminTimezone.
	 */
	public String getAdminTimezone() {
		return adminTimezone;
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
	public void setAction(String action) {
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
		this.username = StringUtils.trimToNull(username);
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
	 * Setter for property admineTimezone.
	 *
	 * @param timezone
	 *            New value of property adminTimezone.
	 */
	public void setAdminTimezone(String timezone) {
		this.adminTimezone = timezone;
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
	 * Getter for property groupIDs.
	 *
	 * @return Value of property groupID.
	 */
	public String[] getGroupIDs() {
		return groupIDs;
	}

	/**
	 * Setter for property groupIDs.
	 *
	 * @param groupID
	 *            New value of property groupIDs.
	 */
	public void setGroupIDs(String[] groupIDs) {
		this.groupIDs = groupIDs;
	}
}
