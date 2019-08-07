/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.RecipientForm;
import org.apache.struts.action.ActionMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ComRecipientReaction;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;

public class ComRecipientForm extends RecipientForm {
    private static final long serialVersionUID = -175166723099243720L;

    private Set<Integer> bulkIDs = new HashSet<>();

    private PaginatedListImpl<ComRecipientReaction> recipientReactions;
    private String dateFormatPattern;
    private Map<String, String> bulkChange = new HashMap<>();
    private boolean trackingVeto;

    public final boolean isTrackingVeto() {
		return trackingVeto;
	}

	public final void setTrackingVeto(final boolean trackingVeto) {
		this.trackingVeto = trackingVeto;
	}

	public static final Map<Integer, String> MAILING_TYPE_NAMES;
	static {
		MAILING_TYPE_NAMES = new HashMap<>();
		MAILING_TYPE_NAMES.put(Mailing.TYPE_NORMAL, "mailing.Normal_Mailing");
		MAILING_TYPE_NAMES.put(Mailing.TYPE_DATEBASED, "mailing.Rulebased_Mailing");
		MAILING_TYPE_NAMES.put(Mailing.TYPE_ACTIONBASED, "mailing.action.based.mailing");
		MAILING_TYPE_NAMES.put(Mailing.TYPE_FOLLOWUP, "mailing.Followup_Mailing");
		MAILING_TYPE_NAMES.put(Mailing.TYPE_INTERVAL, "mailing.Interval_Mailing");
	}

	/**
     * Getter for property bindingEntry.
     *
     * @return Value of property bindingEntry.
     */
    private BindingEntry getBindingEntry(int type, int id) {
        Map<Integer, BindingEntry> sub=null;

        sub = mailing.get(new Integer(id)); 
        if(sub == null) {
            sub = new HashMap<>();
            mailing.put(new Integer(id), sub); 
        }

        if(sub.get(new Integer(type)) == null) {
            BindingEntry entry=(BindingEntry) getWebApplicationContext().getBean("BindingEntry");

            entry.setMailinglistID(id);
            entry.setMediaType(type);
            sub.put(new Integer(type), entry); 
        }
        return sub.get(new Integer(type));
    }

    public BindingEntry getEmailEntry(int id) {
        return getBindingEntry(0, id);
    }

    public BindingEntry getFaxEntry(int id) {
        return getBindingEntry(1, id);
    }

    public BindingEntry getPrintEntry(int id) {
        return getBindingEntry(2, id);
    }

    public BindingEntry getMmsEntry(int id) {
        return getBindingEntry(3, id);
    }

    public BindingEntry getSmsEntry(int id) {
        return getBindingEntry(4, id);
    }

	@Override
	protected boolean isParameterExcludedForUnsafeHtmlTagCheck( String parameterName, HttpServletRequest request) {
		if (AgnUtils.allowed(request, Permission.RECIPIENT_PROFILEFIELD_HTML_ALLOWED)) {
			return true;
		} else {
			return super.isParameterExcludedForUnsafeHtmlTagCheck(parameterName, request);
		}
	}

    @Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        clearBulkIds();
        bulkChange = new HashMap<>();
        this.trackingVeto = false;
    }

    public void setBulkID(int id, String value) {
        if (value != null && (value.equals("on") || value.equals("yes") || value.equals("true")))
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

    public PaginatedListImpl<ComRecipientReaction> getRecipientReactions() {
        return recipientReactions;
    }

    public void setRecipientReactions(PaginatedListImpl<ComRecipientReaction> recipientReactions) {
        this.recipientReactions = recipientReactions;
    }

    public String getDateFormatPattern() {
        return dateFormatPattern;
    }

    public void setDateFormatPattern(String dateFormatPattern) {
        this.dateFormatPattern = dateFormatPattern;
    }

	/**
	 * Load additional Data which should be stored in request attributes etc.
	 *
	 * @param mapping
	 * @param request
	 */
    @Override
	protected void loadNonFormDataForErrorView(ActionMapping mapping, HttpServletRequest request) {
        ApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
        request.setAttribute("mailinglists", springContext.getBean("mailinglistApprovalService", MailinglistApprovalService.class).getEnabledMailinglistsForAdmin(AgnUtils.getAdmin(request)));
	}
	
	public Map<String, String> getBulkChange() {
		bulkChange = AgnUtils.repairFormMap(bulkChange);
		return bulkChange;
	}

	public void setBulkChange(String key, String value) {
		bulkChange.put(key, value);
	}
}
