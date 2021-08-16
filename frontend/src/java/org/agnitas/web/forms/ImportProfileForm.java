/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import org.agnitas.actions.EmmAction;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.ImportProfileImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.CheckForDuplicates;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.NullValuesAction;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.agnitas.web.ImportProfileAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ImportProcessAction;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.commons.validation.AgnitasEmailValidator;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

/**
 * Form class that incapsulates the data of import profile
 */
public class ImportProfileForm extends StrutsFormBase {
	private static final long serialVersionUID = 3558724976522120933L;

    protected int action;

    protected int profileId;

    protected int defaultProfileId;

    protected ImportProfile profile = new ImportProfileImpl();

    protected String[] allDBColumns;

    protected String addedGender;

    protected int addedGenderInt;

    protected boolean fromListPage;

	protected ImportMode[] availableImportModes;
	
	protected List<ImportProcessAction> importProcessActions;

	protected List<EmmAction> actionsForNewRecipients;

	// Set of mailing lists managed by user manually.
	private Set<Integer> mailinglists = new HashSet<>();
	// Set of mailing lists (read only) taken from EMM Actions (if one is selected).
	private Set<Integer> mailinglistsToShow = Collections.emptySet();

	//Page settings - necessary for properly set up page after validation error
	private List<ProfileField> availableImportProfileFields;
	private List<Mailinglist> availableMailinglists;
	
	// Set of mediatypes managed by user manually.
	private Set<Integer> mediatypes = new HashSet<>();
	
	public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public ImportProfile getProfile() {
        return profile;
    }

    public void setProfile(ImportProfile profile) {
        this.profile = profile;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getDefaultProfileId() {
        return defaultProfileId;
    }

    public void setDefaultProfileId(int defaultProfileId) {
        this.defaultProfileId = defaultProfileId;
    }

    public boolean getFromListPage() {
        return fromListPage;
    }

    public void setFromListPage(boolean fromListPage) {
        this.fromListPage = fromListPage;
    }

    public String getAddedGender() {
        return addedGender;
    }

    public void setAddedGender(String addedGender) {
        this.addedGender = addedGender;
    }

    public int getAddedGenderInt() {
        return addedGenderInt;
    }

    public void setAddedGenderInt(int addedGenderInt) {
        this.addedGenderInt = addedGenderInt;
    }

    public Charset[] getCharsets() {
        return Charset.values();
    }

    public DateFormat[] getDateFormats() {
        return DateFormat.values();
    }

    public TextRecognitionChar[] getDelimiters() {
        return TextRecognitionChar.values();
    }

    public ImportMode[] getImportModes() {
        return availableImportModes;
    }

	public void setImportModes(ImportMode[] importModes) {
		availableImportModes = importModes;
	}

    public NullValuesAction[] getNullValuesActions() {
        return NullValuesAction.values();
    }

    public void setImportProcessActions(List<ImportProcessAction> importProcessActions) {
        this.importProcessActions = importProcessActions;
    }

    public List<ImportProcessAction> getImportProcessActions() {
        return importProcessActions;
    }

    public CheckForDuplicates[] getCheckForDuplicatesValues() {
        return CheckForDuplicates.values();
    }

    public String[] getAllDBColumns() {
        return new String[]{"email", "name", "etc"};
    }

    public List<EmmAction> getActionsForNewRecipients() {
        return actionsForNewRecipients;
    }

    public void setActionsForNewRecipients(List<EmmAction> actionsForNewRecipients) {
        this.actionsForNewRecipients = actionsForNewRecipients;
    }
	
	@Override
	public void reset(ActionMapping map, HttpServletRequest request) {
		super.reset(map, request);
		profile = new ImportProfileImpl();
		mailinglists = new HashSet<>();
		setNumberOfRows(-1);
	}
	
	@Override
	public ActionErrors formSpecificValidate(ActionMapping actionMapping, HttpServletRequest request) {
        ActionErrors actionErrors = new ActionErrors();

        if (action == ImportProfileAction.ACTION_SAVE) {
            if (AgnUtils.parameterNotEmpty(request, "save")) {
                if (profile.getName() == null || profile.getName().length() < 3) {
                    actionErrors.add("shortname", new ActionMessage("error.name.too.short"));
                }
                if (StringUtils.isNotBlank(profile.getMailForReport())) {
                	try {
						for (InternetAddress emailAddress : AgnUtils.getEmailAddressesFromList(profile.getMailForReport())) {
						    if (!AgnitasEmailValidator.getInstance().isValid(emailAddress.getAddress())) {
						    	actionErrors.add("mailForReport", new ActionMessage("error.invalid.email"));
						    	break;
							}
						}
					} catch (Exception e) {
						actionErrors.add("mailForReport", new ActionMessage("error.invalid.email"));
					}
                }
                if (StringUtils.isNotBlank(profile.getMailForError())) {
                	try {
						for (InternetAddress emailAddress : AgnUtils.getEmailAddressesFromList(profile.getMailForError())) {
						    if (!AgnitasEmailValidator.getInstance().isValid(emailAddress.getAddress())) {
						    	actionErrors.add("mailForError", new ActionMessage("error.invalid.email"));
						    	break;
							}
						}
					} catch (Exception e) {
						actionErrors.add("mailForError", new ActionMessage("error.invalid.email"));
					}
                }
            }
        }
        return actionErrors;
    }
	
	@Override
	protected void loadNonFormDataForErrorView(ActionMapping mapping, HttpServletRequest request) {
		ComAdmin admin = AgnUtils.getAdmin(request);
		
		request.setAttribute("isCustomerIdImportNotAllowed", !admin.permissionAllowed(Permission.IMPORT_CUSTOMERID));
		
		List<Integer> genders = new ArrayList<>();
        int maxGenderValue;
        if (admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)) {
            maxGenderValue = ConfigService.MAX_GENDER_VALUE_EXTENDED;
        } else {
            maxGenderValue = ConfigService.MAX_GENDER_VALUE_BASIC;
        }
        
        for (int i = 0; i <= maxGenderValue; i++) {
            genders.add(i);
        }
        
        request.setAttribute("availableGenderIntValues", genders);
	}
	
	public List<String> splitGenderSequence(String genderSequence) {
		List<String> strGenders = new ArrayList<>();
		StringTokenizer stringTokenizerNewGender = new StringTokenizer(genderSequence, ",");
        while(stringTokenizerNewGender.hasMoreTokens()){
           strGenders.add(stringTokenizerNewGender.nextToken().trim());
        }
		return strGenders;
    }

	public void setMailinglist(int id, String value) {
		if (AgnUtils.interpretAsBoolean(value)) {
			mailinglists.add(id);
		} else {
			mailinglists.remove(id);
		}
	}

	public String getMailinglist(int id) {
		return mailinglists.contains(id) ? "on" : "";
	}

	public Set<Integer> getMailinglists() {
		return mailinglists;
	}

	public void setMediatype(int id, String value) {
		if (AgnUtils.interpretAsBoolean(value)) {
			mediatypes.add(id);
		} else {
			mediatypes.remove(id);
		}
	}

	public String getMediatype(int id) {
		return mediatypes.contains(id) ? "on" : "";
	}

	public Set<MediaTypes> getMediatypes() {
		Set<MediaTypes> returnSet = new HashSet<>();
		for (int mediatypeCode : mediatypes) {
			returnSet.add(MediaTypes.getMediaTypeForCode(mediatypeCode));
		}
		return returnSet;
	}

	public void setMediatypes(Set<MediaTypes> mediatypes) {
		this.mediatypes.clear();
		for (MediaTypes mediatype : mediatypes) {
			this.mediatypes.add(mediatype.getMediaCode());
		}
	}

	public void setMailinglistsToShow(Set<Integer> mailinglistsToShow) {
	    if (CollectionUtils.isNotEmpty(mailinglistsToShow)) {
            this.mailinglistsToShow = mailinglistsToShow;
        } else {
	        this.mailinglistsToShow = Collections.emptySet();
        }
	}

	public Set<Integer> getMailinglistsToShow() {
		return mailinglistsToShow;
	}

	public void setAvailableImportProfileFields(List<ProfileField> availableImportProfileFields) {
		this.availableImportProfileFields = availableImportProfileFields;
	}
	
	public List<ProfileField> getAvailableImportProfileFields() {
		return availableImportProfileFields;
	}
	
	public void setAvailableMailinglists(List<Mailinglist> availableMailinglists) {
		this.availableMailinglists = availableMailinglists;
	}
	
	public List<Mailinglist> getAvailableMailinglists() {
		return availableMailinglists;
	}
	
	public void clearLists() {
		this.mailinglists.clear();
	}
}
