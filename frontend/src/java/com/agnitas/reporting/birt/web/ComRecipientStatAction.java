/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Company;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComCompany;
import com.agnitas.reporting.birt.external.web.filter.BirtInterceptingFilter;
import com.agnitas.reporting.birt.util.URLUtils;
import com.agnitas.web.forms.ComBirtStatForm;
import com.agnitas.web.forms.ComBirtStatForm.DateMode;

public  class ComRecipientStatAction  extends ComReportBaseAction {
    /**
     * cache mediatypes.
     */
    final List<String[]> mediatypeList = new ArrayList<>();
	{
		mediatypeList.add(new String[]{"0","mailing.MediaType.0"});//"email"
		mediatypeList.add(new String[]{"1","mailing.MediaType.1"});//"fax"
		mediatypeList.add(new String[]{"2","mailing.MediaType.2"});//"print"
		mediatypeList.add(new String[]{"3","mailing.MediaType.3"});//"mms"
		mediatypeList.add(new String[]{"4","mailing.MediaType.4"});//"sms"
	}

	protected ConfigService configService;
	
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
   
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
    	HttpServletRequest req, HttpServletResponse res) throws Exception {

        if(!AgnUtils.isUserLoggedIn(req)) {
            return mapping.findForward("logon");
        }
        
        ComBirtStatForm aForm =  (ComBirtStatForm) form;
        ActionMessages errors = new ActionMessages();

    	if (StringUtils.isEmpty(aForm.getReportName())) {
    		aForm.setReportName("recipient_progress.rptdesign");
    	}
    	
    	if (StringUtils.isEmpty(aForm.getReportFormat())) {
    		aForm.setReportFormat("html");
    	}
    	
    	BirtUrlParams birtParams=new BirtUrlParams();
        try {
    	    setDate(aForm, birtParams, req);
        }
        catch (ParseException e) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.date.format"));
			saveErrors(req, errors);
            req.setAttribute("noDateAvailable", true);
        }
    	aForm.setCompanyID(AgnUtils.getCompanyID(req));
    	aForm.setReportUrl(getReportUrl(req, aForm, birtParams));
    	
    	req.setAttribute("targetlist", getTargetLights(req));
		req.setAttribute("mailinglists", getMailingListsNames(req));
		req.setAttribute("mediatype", mediatypeList);
		req.setAttribute("monthlist", AgnUtils.getMonthList());

	 	Company company = AgnUtils.getCompany(req);
	 	
	 	GregorianCalendar startDate = new GregorianCalendar();
        Date creationDate = ((ComCompany) company).getCreationDate();

        if(creationDate == null) {
          creationDate = new Date();
        }
        
        startDate.setTime(creationDate);
	 	
		int startYear =  startDate.get(Calendar.YEAR) - 1;
	 	
		req.setAttribute("yearlist",AgnUtils.getYearList(startYear));
    	
		
		if (aForm.getShowReportOnly() == 0) {
			return mapping.findForward("stat");
		} else {
			res.sendRedirect( aForm.getReportUrl());
			return null;
		}
    }
    
    public String getReportUrl(HttpServletRequest request, ComBirtStatForm aForm, BirtUrlParams birtParams) throws Exception {
		String language = "EN";
		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin != null) {
			language = admin.getAdminLang();
		}
 		
		StringBuffer br = new StringBuffer();
		br.append(configService.getValue(ConfigValue.BirtUrl)).append("/run?");
		br.append("__report=").append(aForm.getReportName());
		br.append("&__svg=true");
		br.append("&companyID=").append(AgnUtils.getCompanyID(request));
		br.append("&mediaType=").append(aForm.getMediaType());
		br.append("&startDate=").append(birtParams.reportStartDate);
		br.append("&stopDate=").append(birtParams.reportEndDate);
		br.append("&targetID=").append(aForm.getTargetID());
		br.append("&mailinglistID=").append(aForm.getMailingListID());
		br.append("&hourScale=").append(birtParams.hourScale);
		br.append("&language=").append(language);
		br.append("&__format=").append(aForm.getReportFormat());
		br.append("&sec=").append(URLUtils.encodeURL(BirtInterceptingFilter.createSecurityToken(configService, admin.getCompanyID())));
		br.append("&emmsession=").append(request.getSession(false).getId());
		br.append("&targetbaseurl=").append(URLUtils.encodeURL(configService.getValue(ConfigValue.BirtDrilldownUrl)));
		return br.toString();
    }
    
    private void setDate(ComBirtStatForm form, BirtUrlParams birtParams, HttpServletRequest request) throws ParseException {
    	SimpleDateFormat reportDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat inputDateFormat = AgnUtils.getLocaleDateFormatSpecific(request);

		request.setAttribute("localeDatePattern", inputDateFormat.toPattern());

		Calendar cal = Calendar.getInstance();
 		
 		if (form.getDateSelectMode()==null) {
 			form.setDateMode(DateMode.SELECT_MONTH);
 		}
 		if (form.getMonth()==null) {
 			form.setMonth(cal.get(Calendar.MONTH));
 		}
 		if (form.getYear()==null) {
 			form.setYear(cal.get(Calendar.YEAR));
 		}
 		
 		if (null== form.getEndDay())  {
			form.setEndDay(inputDateFormat.format(Calendar.getInstance().getTime()));
		}
		if (null== form.getStartDay())  {
			cal.setTime(inputDateFormat.parse(form.getEndDay()));
			cal.add(Calendar.MONTH, -1);
			form.setStartDay(inputDateFormat.format(cal.getTime()));
		}
		if (null== form.getSelectDay())  {
			form.setSelectDay(inputDateFormat.format(cal.getTime()));
		}

		birtParams.hourScale=false;

        cal = Calendar.getInstance();

 		switch (form.getDateMode()) {
   		case LAST_DAY:
   			//date stays same;
   			birtParams.reportEndDate = reportDateFormat.format(cal.getTime());
   			birtParams.reportStartDate = reportDateFormat.format(cal.getTime());
   			birtParams.hourScale=true;
   			break;
   		case LAST_WEEK:
   			birtParams.reportEndDate = reportDateFormat.format(cal.getTime());
	   		cal.add(Calendar.DAY_OF_MONTH, -7);
	   		birtParams.reportStartDate = reportDateFormat.format(cal.getTime());
   			break;
   		case LAST_FORTNIGHT:
   			birtParams.reportEndDate = reportDateFormat.format(cal.getTime());
	   		cal.add(Calendar.DAY_OF_MONTH, -14);
	   		birtParams.reportStartDate = reportDateFormat.format(cal.getTime());
   			break;
   		case LAST_MONTH:
   			birtParams.reportEndDate = reportDateFormat.format(cal.getTime());
	   		cal.add(Calendar.MONTH, -1);
	   		birtParams.reportStartDate = reportDateFormat.format(cal.getTime());
   			break;
   		case SELECT_DAY:
   			birtParams.reportStartDate = reportDateFormat.format(inputDateFormat.parse(form.getSelectDay()));
   			birtParams.hourScale=true;
   			break;
   		case SELECT_MONTH:
   			cal.set(Calendar.DAY_OF_MONTH, 1);
    		cal.set(Calendar.MONTH, form.getMonth());
    		cal.set(Calendar.YEAR,form.getYear());
    		birtParams.reportStartDate = reportDateFormat.format(cal.getTime());
    		
    		cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    		birtParams.reportEndDate = reportDateFormat.format(cal.getTime());
    		break;
   		case SELECT_PERIOD:
   			birtParams.reportStartDate = reportDateFormat.format(inputDateFormat.parse(form.getStartDay()));
   			birtParams.reportEndDate = reportDateFormat.format(inputDateFormat.parse(form.getEndDay()));
			break;
			
   		case NONE:
   			// $FALL-THROUGH$
   			
   		case LAST_TENHOURS:
   			// Do nothing here
 		}
    }
}
