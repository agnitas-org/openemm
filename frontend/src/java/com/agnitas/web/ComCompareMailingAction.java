/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Company;
import org.agnitas.beans.MailingBase;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.reporting.birt.external.web.filter.BirtInterceptingFilter;
import com.agnitas.reporting.birt.util.URLUtils;
import com.agnitas.reporting.birt.web.ComReportBaseAction;

/**
 * Implementation of <strong>Action</strong> that validates a user logon.
 */
public final class ComCompareMailingAction extends ComReportBaseAction {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComCompareMailingAction.class);

    public static final int ACTION_COMPARE = ACTION_LAST + 1;

    public static final int DEFAULT_FRAME_HEIGHT = 300;
    public static final int HEIGTH_PER_MAIL = 85;
    public static final int HEIGHT_PER_TARGET = 30;
    public static final int BASE_REPORT_HEIGHT = 60;

	/** DAO accessing target groups. */
    protected ComTargetDao targetDao;
    protected MailingDao mailingDao;
    
	protected ConfigService configService;

	@Required
    public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
        case ACTION_COMPARE:
            return "compare";
        default:
            return super.subActionMethodName(subAction);
        }
    }
    
    @Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        ComCompareMailingForm aForm;
        ActionMessages errors = new ActionMessages();
        ActionForward destination=null;

        if(!AgnUtils.isUserLoggedIn(req)) {
            return mapping.findForward("logon");
        }

        if (form == null) {
            aForm = new ComCompareMailingForm();
        } else {
            aForm = (ComCompareMailingForm) form;
        }

        if (StringUtils.isEmpty(aForm.getReportFormat())) {
    		aForm.setReportFormat("html");
    	}

        removeDummyTarget(aForm);

        if (logger.isInfoEnabled()) {
        	logger.info("Action: "+aForm.getAction());
        }
        try {
            switch (aForm.getAction()) {
                case ACTION_LIST:
                    aForm.setAction(ACTION_COMPARE);
                    List<TargetLight> targetList = targetDao.getTargetLights(AgnUtils.getCompanyID(req), false);
                    List<MailingBase> mailingsForComparation = mailingDao.getMailingsForComparation(AgnUtils.getCompanyID(req));
                    aForm.resetForNewCompare();
                    req.setAttribute("targetGroups", targetList);
                    req.setAttribute("mailings", mailingsForComparation);
                    destination=mapping.findForward("list");
                    break;
                case ACTION_COMPARE:
                    aForm.setAction(ACTION_COMPARE);
                    aForm.setReportUrl(getReportUrl(req, aForm));
                    List<TargetLight> targetGroupsList = targetDao.getTargetLights(AgnUtils.getCompanyID(req), false);
                    req.setAttribute("targetGroups", targetGroupsList);
                    countFrameHeight(aForm, req);
                    if ("html".equals(aForm.getReportFormat()))  {
                        destination = mapping.findForward("compare");
                    } else {
                        res.sendRedirect(aForm.getReportUrl());
                    }

                    List<String> mailingNames = new LinkedList<>();
                    for (Object mailingId : aForm.getMailings()) {
                        ComMailing mailing = (ComMailing) mailingDao.getMailing((Integer) mailingId, AgnUtils.getCompanyID(req));
                        mailingNames.add(mailing.getShortname());
                    }
                    req.setAttribute("mailingNames", mailingNames);
                    break;
                default:
                    destination=mapping.findForward("list");
            }

        } catch (Exception e) {
            logger.error("execute: "+e, e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
        }

        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            saveErrors(req, errors);
            return(new ActionForward(mapping.getInput()));
        }

        return destination;
    }

    private void removeDummyTarget(ComCompareMailingForm aForm) {
        if (aForm.getSelectedTargets() != null && aForm.getSelectedTargets().length > 0) {
            List<String> targets = new LinkedList<>();
            for (String target : aForm.getSelectedTargets()) {
                if (!"0".equals(target)) {
                    targets.add(target);
                }
            }
            aForm.setSelectedTargets(targets.toArray(new String[0]));
        }
    }

    private void countFrameHeight(ComCompareMailingForm aForm, HttpServletRequest req) {
        aForm.setFrameHeight(DEFAULT_FRAME_HEIGHT);
        int mailNum = aForm.getMailings() == null ? 0 : aForm.getMailings().size();
        int targetNum = aForm.getSelectedTargets() == null ? 0 : aForm.getSelectedTargets().length;
        int height = BASE_REPORT_HEIGHT + mailNum * HEIGTH_PER_MAIL + mailNum * targetNum * HEIGHT_PER_TARGET;
        if (height > aForm.getFrameHeight()) {
            aForm.setFrameHeight(height);
        }
    }

    public String getReportUrl(HttpServletRequest request, ComCompareMailingForm aForm) throws Exception {
		String language = "EN";
		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin != null) {
			language = admin.getAdminLang();
		}
		
        String[] sel = aForm.getSelectedTargets();
		String selectedTargets = "";
		if (null != sel && sel.length>0) {
			selectedTargets = StringUtils.join(sel, ",");
		}
        StringBuilder builder = new StringBuilder();
        builder.append(configService.getValue(ConfigValue.BirtUrl)).append("/run?");
		builder.append("__report=mailing_compare.rptdesign");
        builder.append("&companyID=").append(AgnUtils.getCompanyID(request));
        builder.append("&targetID=").append(aForm.getTargetID());
        builder.append("&selectedTargets=").append(selectedTargets);
        builder.append("&recipientsType=").append(aForm.getRecipientType());
        builder.append("&selectedMailings=").append(StringUtils.join(aForm.getMailings(), ","));
        builder.append("&language=").append(language);
		builder.append("&__format=").append(aForm.getReportFormat());
        builder.append("&sec=").append(URLUtils.encodeURL(BirtInterceptingFilter.createSecurityToken(configService, AgnUtils.getAdmin(request).getCompanyID())));
        builder.append("&emmsession=").append(request.getSession(false).getId());
        builder.append("&targetbaseurl=").append(URLUtils.encodeURL(configService.getValue(ConfigValue.BirtDrilldownUrl)));
        Company company = AgnUtils.getCompany(request);
        if (company != null) {
            builder.append("&trackingAllowed=").append(company.getMailtracking() == 1);
        }
		return builder.toString();
    }

	/**
	 * Sets DAO accessing target groups.
	 * 
	 * @param targetDao DAO accessing target groups
	 */
   @Required
   public void setTargetDao(ComTargetDao targetDao) {
        this.targetDao = targetDao;
    }

    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }
}
