/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.reporting.birt.web.forms.ComMailingBIRTStatForm;
import com.agnitas.service.ComWebStorage;
import com.agnitas.service.GridServiceWrapper;
import org.agnitas.beans.impl.SimpleKeyValueBean;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.FulltextSearchQueryException;
import org.agnitas.web.MailingAdditionalColumn;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Required;

public class ComMailingBIRTStatAction extends ComMailingBIRTStatActionBase {
	
	public final static int ACTION_URL_WEEKSTAT = ACTION_LAST + 14 ;
	public final static int ACTION_URL_DAYSTAT = ACTION_LAST +15;
	public final static int ACTION_MAILING_SEND_OPENED = ACTION_LAST +16;
	public final static int ACTION_MAILING_OPTOUT = ACTION_LAST +17;
	public final static int ACTION_MAILING_BOUNCES = ACTION_LAST + 18;
	public static final int ACTION_MAILINGSTAT = ACTION_LAST+1;
	public static final int ACTION_WEEKSTAT = ACTION_LAST+2;
	public static final int ACTION_DAYSTAT = ACTION_LAST+3;

	private ComMailingBaseService mailingBaseService;
	private ComMailinglistService mailingListService;
	private ComTargetService targetGroupService;
	private WebStorage webStorage;
	
	private GridServiceWrapper gridService;

    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
        case ACTION_MAILINGSTAT:
            return "mailingstat";
        case ACTION_WEEKSTAT:
            return "weekstat";
        case ACTION_DAYSTAT:
            return "daystat";
        case ACTION_URL_WEEKSTAT:
            return "url_weekstat";
        case ACTION_URL_DAYSTAT:
            return "url_daystat";
            
        case ACTION_MAILING_SEND_OPENED:
            return "mailing_send_opened";
        case ACTION_MAILING_OPTOUT:
            return "mailing_optout";
        case ACTION_MAILING_BOUNCES:
            return "mailing_bounces";
        default:
            return super.subActionMethodName(subAction);
        }
    }
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		ComAdmin admin = AgnUtils.getAdmin(request);
		assert admin != null;

		ComMailingBIRTStatForm aForm = (ComMailingBIRTStatForm) form;
		
        request.setAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(AgnUtils.getAdmin(request), aForm.getMailingID()));
		
		if (!AgnUtils.isUserLoggedIn(request)) {
			return mapping.findForward("logon");
		}
		
		if ( aForm.getAction() == -1 ) {
			aForm.setAction(ACTION_MAILINGSTAT);
		}
		
		// handle switching between mailings
		if ( aForm.getPreviousMailingID() != aForm.getMailingID() ) {
			aForm.setStartDate(null);
			aForm.setSelectedTargets(null);
		}

		final int companyID = AgnUtils.getCompanyID(request);

		aForm.setPreviousMailingID(aForm.getMailingID());
		loadMailing(aForm, request);

		// prepare targets-list for the select in the jsp
		List<SimpleKeyValueBean>  allTargets = birtservice.getAvailableTargetsAsKeyValue(companyID);
		List<SimpleKeyValueBean> selectedTargets = new ArrayList<>();
		
		if( aForm.getAvailableTargets() == null ) {
			List<String> tmp = new ArrayList<>();
			for( SimpleKeyValueBean bean: allTargets ) {
				tmp.add(bean.getKey());
			}
			aForm.setAvailableTargets(tmp.toArray(new String[0]));
		}

		// remove the selected targets from the available targets list
		if( aForm.isTargetSelectsReset()) { // Bug-Fix form is in session, if no values are provided in form or request the values are kept
			aForm.setSelectedTargets(null);
		}

		List<Integer> targetIDs = new ArrayList<>();
		if( aForm.getSelectedTargets() != null ) {
			List<String> tmpAllTargetList = new ArrayList<>();
			List<String> tmpSelectedTargetList = new ArrayList<>();
 			
			CollectionUtils.addAll(tmpAllTargetList, birtservice.extractTargetIDs(allTargets) );
 			CollectionUtils.addAll(tmpSelectedTargetList, aForm.getSelectedTargets());
 			
 			// remove selected id's
 			tmpAllTargetList.removeAll(tmpSelectedTargetList);
 			aForm.setAvailableTargets(tmpAllTargetList.toArray(new String[0]));
 			
 			selectedTargets = birtservice.filterTargetsFromList(allTargets, aForm.getSelectedTargets());
 			allTargets = birtservice.filterTargetsFromList(allTargets, aForm.getAvailableTargets());
 			
 			if( aForm.getSelectedTargets() != null ) { // TODO use a decorator for that lists
				for(String targetID: aForm.getSelectedTargets() ) {
					if (!StringUtils.isEmpty(targetID) && StringUtils.isNumeric(targetID)) {
						targetIDs.add( Integer.parseInt(targetID));
					}
				}
			}
		}

		// choose action
		if (aForm.getAction() == ACTION_LIST) {
			webStorage.access(ComWebStorage.MAILING_SEPARATE_STATS_OVERVIEW, storage -> {
				if (aForm.getNumberOfRows() > 0) {
					storage.setRowsCount(aForm.getNumberOfRows());
					storage.setSelectedFields(Arrays.asList(aForm.getAdditionalFields()));
				} else {
					aForm.setNumberOfRows(storage.getRowsCount());
					aForm.setAdditionalFields(storage.getSelectedFields().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
				}
			});

			// init search properties;
			MailingsListProperties props = getMailingsListProperties(aForm);

			request.setAttribute("dateTimeFormat", admin.getDateTimeFormat());
			request.setAttribute("mailingStatisticList", birtservice.getMailingStats(admin.getCompanyID(), props));
			request.setAttribute("availableAdditionalFields", MailingAdditionalColumn.values());
			request.setAttribute("availableMailingLists", mailingListService.getAllMailingListsNames(admin.getCompanyID()));
			request.setAttribute("availableTargetGroups", targetGroupService.getTargetLights(admin.getCompanyID()));

			if (Objects.isNull(aForm.getColumnwidthsList())) {
				aForm.setColumnwidthsList(getInitializedColumnWidthList(2));
			}

			return mapping.findForward("list");
		}

		if ( aForm.getAction() == ACTION_MAILINGSTAT ) {
			request.setAttribute("weekstataction",ACTION_WEEKSTAT);
			request.setAttribute("urlweekstataction",ACTION_URL_WEEKSTAT);
			request.setAttribute("sendopenedaction", ACTION_MAILING_SEND_OPENED);
			request.setAttribute("optoutaction", ACTION_MAILING_OPTOUT);
			request.setAttribute("bouncesaction", ACTION_MAILING_BOUNCES);
			setCommonRequestAttributes(request, companyID, aForm.getMailingID(), allTargets, selectedTargets, aForm.isIncludeAdminAndTestMails());
			return mapping.findForward("mailing_stat");
		}
		
		if (aForm.getAction() == ACTION_WEEKSTAT || aForm.getAction() == ACTION_URL_WEEKSTAT) {
			throw new UnsupportedOperationException();
		}
		
		if (aForm.getAction() == ACTION_DAYSTAT || aForm.getAction() == ACTION_URL_DAYSTAT) {
			throw new UnsupportedOperationException();
		}
		
		if( aForm.getAction() == ACTION_MAILING_SEND_OPENED ) {
			throw new UnsupportedOperationException();
		}
		
		if (aForm.getAction() == ACTION_MAILING_OPTOUT ) {
			throw new UnsupportedOperationException();
		}
		
		if ( aForm.getAction() == ACTION_MAILING_BOUNCES ) {
			throw new UnsupportedOperationException();
		}
		
		return null;
	}

	private MailingsListProperties getMailingsListProperties(ComMailingBIRTStatForm statForm) throws FulltextSearchQueryException {
		boolean hasTargetGroups = statForm.getAdditionalFieldsSet()
				.contains(MailingAdditionalColumn.TARGET_GROUPS.getSortColumn());

		MailingsListProperties props = new MailingsListProperties();
		props.setSearchQuery(statForm.getSearchQueryText());
		props.setSearchName(statForm.isSearchNameChecked());
		props.setSearchDescription(statForm.isSearchDescriptionChecked());
		props.setTypes("0,1,2,3,4"); // all mailing types
		props.setStatuses(Collections.singletonList("sent")); // just set mailings
		props.setSort(StringUtils.defaultString(statForm.getSort(), "senddate")); // sort by send date by default
		props.setDirection(StringUtils.defaultString(statForm.getDir(), "desc")); // desc order by default
		props.setPage(statForm.getPageNumber());
		props.setRownums(statForm.getNumberOfRows());
		props.setIncludeTargetGroups(hasTargetGroups );
		props.setAdditionalColumns(statForm.getAdditionalFieldsSet());
		props.setMailingLists(statForm.getFilteredMailingListsAsList());
		props.setTargetGroups(statForm.getFilteredTargetGroupsAsList());

		return props;
	}

	protected void loadMailing(ComMailingBIRTStatForm aForm, HttpServletRequest request) {
		final int companyId = AgnUtils.getCompanyID(request);
		final int mailingId = aForm.getMailingID();

		aForm.setShortname(mailingBaseService.getMailingName(mailingId, companyId));
		aForm.setIsMailingUndoAvailable(mailingBaseService.checkUndoAvailable(mailingId));
		aForm.setWorkflowId(mailingBaseService.getWorkflowId(mailingId, companyId));

		final int templateId = gridService.getGridTemplateIdByMailingId(mailingId);

		aForm.setIsMailingGrid(templateId > 0);
		aForm.setTemplateId(templateId);

		// For backward compatibility
		request.setAttribute("isMailingGrid", templateId > 0);
		request.setAttribute("templateId", templateId);
	}

	@Required
	public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
		this.mailingBaseService = mailingBaseService;
	}
	
	@Required
	public void setGridService(GridServiceWrapper gridService) {
		this.gridService = gridService;
	}
	
	@Required
	public void setMailinglistService(ComMailinglistService mailingListService) {
		this.mailingListService = mailingListService;
	}

	@Required
	public void setTargetService(ComTargetService targetGroupService) {
		this.targetGroupService = targetGroupService;
	}

	@Required
	public void setWebStorage(WebStorage webStorage) {
		this.webStorage = webStorage;
	}
}
