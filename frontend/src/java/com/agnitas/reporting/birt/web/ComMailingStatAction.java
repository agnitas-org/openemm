/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.web;

import static com.agnitas.web.forms.ComBirtStatForm.DateMode.SELECT_DAY;
import static com.agnitas.web.forms.ComBirtStatForm.DateMode.SELECT_MONTH;
import static com.agnitas.web.forms.ComBirtStatForm.DateMode.SELECT_PERIOD;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingSendStatus;
import org.agnitas.dao.AdminPreferencesDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComAdminPreferences;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.birtreport.service.ComBirtReportService;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.reporting.birt.external.dataset.BIRTDataSet;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.reporting.birt.util.URLUtils;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.web.forms.ComBirtStatForm;
import com.agnitas.web.forms.ComBirtStatForm.DateMode;

public class ComMailingStatAction extends ComReportBaseAction {
	
	private static final SimpleDateFormat BIRT_PARAMS_DATE_FORMAT = new SimpleDateFormat(BIRTDataSet.DATE_PARAMETER_FORMAT);
	private static final SimpleDateFormat BIRT_PARAMS_DATE_TIME_FORMAT = new SimpleDateFormat(BIRTDataSet.DATE_PARAMETER_FORMAT_WITH_HOUR2);
	
	private static final List<String> ALLOWED_STATISTIC = Arrays.asList(
			"mailing_summary.rptdesign",
			"mailing_linkclicks.rptdesign",
			"mailing_delivery_progress.rptdesign",
			"mailing_net_and_gross_openings_progress.rptdesign",
			"mailing_linkclicks_progress.rptdesign",
			"top_domains.rptdesign",
			"mailing_bounces.rptdesign"
	);
	
	protected static final String DEFAULT_REPORT_NAME = "mailing_summary.rptdesign";

    private ComMailingDao mailingDao;
    protected ComCompanyDao companyDao;
    private AdminPreferencesDao adminPreferencesDao;
	protected ComBirtReportService birtReportService;
    
	protected ConfigService configService;
	private ComMailingBaseService mailingBaseService;
	
	protected BirtStatisticsService birtStatisticsService;
	
	private GridServiceWrapper gridServiceWrapper;
	
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Required
	public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
		this.mailingBaseService = mailingBaseService;
	}
	
	@Required
	public void setBirtStatisticsService(BirtStatisticsService birtStatisticsService) {
		this.birtStatisticsService = birtStatisticsService;
	}
	
	@Required
	public void setGridServiceWrapper(GridServiceWrapper gridServiceWrapper) {
		this.gridServiceWrapper = gridServiceWrapper;
	}
	
	private static final Map<String, DateMode> reportWithPeriods;
    static {
    	reportWithPeriods = new HashMap<>();
    	reportWithPeriods.put("mailing_tracking_point_week_overview.rptdesign", DateMode.LAST_TENHOURS);
    	reportWithPeriods.put("mailing_num_tracking_point_week_overview.rptdesign", DateMode.LAST_TENHOURS);
    	reportWithPeriods.put("mailing_summary.rptdesign", DateMode.SELECT_MONTH);
    }
	
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
    	HttpServletRequest req, HttpServletResponse res) throws Exception {
    	
    	if (!AgnUtils.isUserLoggedIn(req)) {
            return mapping.findForward("logon");
        }
	
		ComBirtStatForm aForm = (ComBirtStatForm) form;
	
		ComAdmin admin = AgnUtils.getAdmin(req);
		int companyID = AgnUtils.getCompanyID(req);
		ComAdminPreferences adminPreferences = adminPreferencesDao.getAdminPreferences(admin.getAdminID());
		
		req.setAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, aForm.getMailingID()));
	
		/* checking is it grid mailing */
		aForm.setIsMailingGrid(gridServiceWrapper.getGridTemplateIdByMailingId(aForm.getMailingID()) > 0);
		aForm.setCompanyID(companyID);
	
		aForm.setReportName(getBirtReportName(aForm.getReportName(), adminPreferences.getStatisticLoadType()));
		
		if (isTopDomainsStatistic(aForm.getReportName())) {
            MailingSendStatus status = mailingDao.getMailingSendStatus(aForm.getMailingID(), companyID);
            
            aForm.setMailtrackingActive(companyDao.isMailtrackingActive(companyID));
            aForm.setEverSent(status.getHasMailtracks());
            
            if (aForm.isEverSent()) {
                if (status.getHasMailtrackData()) {
                    aForm.setMailtrackingExpired(true);
                } else {
                    aForm.setMailtrackingExpired(false);
                    aForm.setMailtrackingExpirationDays(status.getExpirationDays());
                }
            }
		}
	
		if (StringUtils.isEmpty(aForm.getReportFormat())) {
			aForm.setReportFormat("html");
		}
	
		// switch between a 'progress' report and a non 'progress' report
		// TODO: AGNEMM_1580: removed ";" after ")"
		DateMode newDateMode = getDateMode(aForm.getReportName(), aForm.getMailingID());
		if ((null == aForm.getDateMode()) || (DateMode.NONE == newDateMode) || (DateMode.NONE == aForm.getDateMode())) {
			aForm.setDateMode(newDateMode);
		}

		SimpleDateFormat localeFormat = AgnUtils.getDatePickerFormat(AgnUtils.getAdmin(req), true);

		// set default values for year, month, day, startDay, endDay
		Calendar currentDateCalendar = GregorianCalendar.getInstance();
		if (aForm.getDateMode() == SELECT_MONTH && aForm.getMonth() == null) {
			aForm.setMonth(currentDateCalendar.get(Calendar.MONTH));
		}
		if (aForm.getDateMode() == SELECT_MONTH && aForm.getYear() == null) {
			aForm.setYear(currentDateCalendar.get(Calendar.YEAR));
		}
		if (aForm.getDateMode() == SELECT_DAY && aForm.getSelectDay() == null) {
			aForm.setSelectDay(localeFormat.format(currentDateCalendar.getTime()));
		}
		if (aForm.getDateMode() == SELECT_PERIOD && aForm.getStartDate_localized() == null) {
			Calendar cal = GregorianCalendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, 1);
			aForm.setStartDate_localized(localeFormat.format(cal.getTime()));
		}
		if (aForm.getDateMode() == SELECT_PERIOD && aForm.getStopDate_localized() == null) {
			aForm.setStopDate_localized(localeFormat.format(currentDateCalendar.getTime()));
		}
	
		Date mailingStartDate = birtservice.getMailingStartDate(aForm.getMailingID());
	
		final Mailing mailing = mailingDao.getMailing(aForm.getMailingID(), companyID);
		if (mailing != null) {
			aForm.setShortname(mailing.getShortname());
			aForm.setDescription(mailing.getDescription());
		}

        aForm.setReportUrl(getBirtStatisticUrl(req, aForm, mailingStartDate));

        // DB-table rdirlog_xxx_val_num_tbl is available for every company
        aForm.setShowNumStat(birtservice.showNumStat(aForm.getMailingID(), companyID));
        
    	aForm.setDeepTracking(birtservice.deepTracking(admin.getAdminID(), companyID));
        if (aForm.isDeepTracking()){
            aForm.setShowAlphaStat(birtservice.showAlphaStat(aForm.getMailingID(),companyID));
            aForm.setShowSimpleStat(birtservice.showSimpleStat(aForm.getMailingID(), companyID));
        }

    	req.setAttribute("targetlist", getTargetLights(req));
    	req.setAttribute("monthlist", AgnUtils.getMonthList());
    	
    	int startYear = new GregorianCalendar().get(Calendar.YEAR); // fallback is current year
    	if(mailingStartDate != null) {
	 		GregorianCalendar calendar = new GregorianCalendar();
	 		calendar.setTime(mailingStartDate);
	 		startYear = calendar.get(Calendar.YEAR);
    	}
	 		
	 	req.setAttribute("yearlist", AgnUtils.getYearList(startYear));
	 	
	 	req.setAttribute("localDatePattern", localeFormat.toPattern());

        req.setAttribute("frameHeight", "770");
        
        String reportName = aForm.getReportName();
		if (ArrayUtils.getLength(aForm.getSelectedTargets()) > 0 &&
				(isPerLinkStatistic(reportName) || isTotalOpeningStatistic(reportName))) {
			req.setAttribute("frameHeight", "1450");
		}else if(isBounceStatistic(reportName)){
            req.setAttribute("frameHeight", "780");
        } else if(isMailingSummaryStatistic(reportName)){
            req.setAttribute("frameHeight", "980");
        }

        if (isSinglePerLinkStatistic(reportName) && aForm.getDateMode() == DateMode.LAST_MONTH) {
            if (mailingStartDate != null) {
                aForm.setDateMode(DateMode.SELECT_PERIOD);
                Calendar cal = Calendar.getInstance();
                cal.setTime(mailingStartDate);
                cal.add(Calendar.MONTH, 1);
                aForm.setStartDate_localized(localeFormat.format(mailingStartDate));
                aForm.setStopDate_localized(localeFormat.format(cal.getTime()));
            } else {
                aForm.setDateMode(DateMode.SELECT_MONTH);
            }

        }

		writeUserActivityLog(AgnUtils.getAdmin(req), "view statistics", aForm.getShortname() + " (" + aForm.getMailingID() + ")" + " active tab - statistics");

		if (aForm.getShowReportOnly() == 0) {
			req.setAttribute("workflowId", mailingDao.getWorkflowId(aForm.getMailingID()));
			return mapping.findForward("stat");
		} else {
			res.sendRedirect(aForm.getReportUrl());
			return null;
		}
    }
	
    protected String getBirtStatisticUrl(HttpServletRequest request, ComBirtStatForm form, Date mailingStartDate) throws Exception {
    	return generateBirtUrl(request, form, getURlParams(form, request, mailingStartDate));
	}
	
	private String getBirtReportName(String reportNameParam, int statisticLoadType) {
    	if (StringUtils.isNotBlank(reportNameParam) && isAllowedStatistic(reportNameParam)) {
    		return reportNameParam;
		}
  
		if (statisticLoadType == ComAdminPreferences.STATISTIC_LOADTYPE_ON_CLICK) {
			return "";
		}
  
		return DEFAULT_REPORT_NAME;
	}
	
	protected boolean isAllowedStatistic(String reportNameParam) {
    	return ALLOWED_STATISTIC.contains(reportNameParam);
	}
	
	private String generateBirtUrl(HttpServletRequest req, ComBirtStatForm aForm, BirtUrlParams birtParams) throws Exception {
    	String reportName = aForm.getReportName();
		if(StringUtils.isEmpty(reportName)) {
			return null;
		}
		
		if (birtParams == null) {
			ActionErrors errors = new ActionErrors();
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.report.date"));
			saveErrors(req, errors);
			return null;
		}
		
		if (isIndividualMailingStatistic(reportName)) {
			return createIndividualUrl(req, aForm);
		}

		return getReportUrl(req, aForm, birtParams);
	}
	
	public String getReportUrl(HttpServletRequest request, ComBirtStatForm aForm, BirtUrlParams birtParams) throws Exception {
		ComAdmin admin = AgnUtils.getAdmin(request);
		
		assert admin != null;
		
		String language = StringUtils.defaultIfEmpty(admin.getAdminLang(), "EN");
		
		Map<String, Object> params = new HashMap<>();
		
		String reportName = aForm.getReportName();
		params.put("__report", reportName);
		params.put("__svg", true);
		params.put("__format", aForm.getReportFormat());
		params.put("mailingID", aForm.getMailingID());
		params.put("companyID", aForm.getCompanyID());
		params.put("shortname", StringUtils.trimToEmpty(aForm.getShortname()));
		params.put("description", StringUtils.trimToEmpty(aForm.getDescription()));
		params.put("selectedTargets", getSelectedTargetList(aForm));
		params.put("language", language);
		params.put("uid", birtStatisticsService.generateUid(admin));
		params.put("emmsession", request.getSession(false).getId());
		params.put("targetbaseurl", URLUtils.encodeURL(configService.getValue(ConfigValue.BirtDrilldownUrl)));
		params.put("recipientType", CommonKeys.TYPE_ALL_SUBSCRIBERS);
		
		if (DateMode.NONE != aForm.getDateMode()) {
			params.put("startDate", birtParams.reportStartDate);
			params.put("stopDate", birtParams.reportEndDate);
			params.put("hourScale", birtParams.hourScale);
		}
		
		if(isBounceStatistic(reportName)) {
			params.put("bouncetype", aForm.getBouncetype());
		}
		
		if(isSinglePerLinkStatistic(reportName)) {
			params.put("urlID", aForm.getUrlID());
			params.put("viewChart", true);
		}
		
		if(isPerLinkStatistic(reportName)) {
			params.put("viewChart", true);
		}
		
		if(isMailingSummaryStatistic(reportName)) {
			params.put("trackingAllowed", AgnUtils.isMailTrackingAvailable(admin));
			params.put("isAlowedDeeptracking", admin.permissionAllowed(Permission.DEEPTRACKING));
			params.put("showSoftbounces", admin.permissionAllowed(Permission.STATISTIC_SOFTBOUNCES_SHOW));
			params.put("showNet", aForm.isShowNetto());
			params.put("showGross", !aForm.isShowNetto());
		}
		
		if (isTopDomainsStatistic(reportName)) {
			params.put("hideSentStats", !aForm.isMailtrackingActive() || !aForm.isMailtrackingExpired());
			params.put("topLevelDomain", aForm.isTopLevelDomain());
			params.put("maxDomains", StringUtils.defaultString(aForm.getMaxDomains(), "5"));
		}
		
		return birtStatisticsService.generateUrlWithParamsForExternalAccess(params);
	}
	
    private BirtUrlParams getURlParams(ComBirtStatForm form, HttpServletRequest request, Date mailingStartDate)
			throws ParseException {
    	BirtUrlParams birtParams = new BirtUrlParams();
    	Calendar cal = Calendar.getInstance();
    	if( mailingStartDate != null ) {
    		cal.setTime(mailingStartDate);
    	}
		birtParams.hourScale = false;
		DateFormat localeFormat = AgnUtils.getDatePickerFormat(AgnUtils.getAdmin(request), true);
		
        switch (form.getDateMode()) {
            case LAST_TENHOURS:
			birtParams.reportStartDate = BIRT_PARAMS_DATE_TIME_FORMAT.format(cal.getTime());
			cal.add(Calendar.HOUR_OF_DAY, 10);
			birtParams.reportEndDate = BIRT_PARAMS_DATE_TIME_FORMAT.format(cal.getTime());
			birtParams.hourScale = true;
                break;
            case LAST_DAY:
			birtParams.reportStartDate = BIRT_PARAMS_DATE_TIME_FORMAT.format(cal.getTime());
			cal.add(Calendar.DAY_OF_MONTH, 1);
			birtParams.reportEndDate = BIRT_PARAMS_DATE_TIME_FORMAT.format(cal.getTime());
			birtParams.hourScale = true;
                break;
            case LAST_MONTH:
			birtParams.reportStartDate = BIRT_PARAMS_DATE_TIME_FORMAT.format(cal.getTime());
			cal.add(Calendar.MONTH, 1);
			birtParams.reportEndDate = BIRT_PARAMS_DATE_TIME_FORMAT.format(cal.getTime());
                break;
            case SELECT_DAY: // from dusk till dawn :), no from yyyy-mm-dd 00:00:00 to yyyy-mm-dd 23:59:59
                if (StringUtils.isBlank(form.getSelectDay())) {
                	return null;
                }
			String localizedStartDate =  form.getSelectDay(); // take the localized version of the requested date
			Date parsedDate = localeFormat.parse(localizedStartDate); // create a date , so that we can format it in iso
			String startDate = BIRT_PARAMS_DATE_FORMAT.format(parsedDate);
			String endDate = startDate + ":23";
			startDate += ":00";
			birtParams.reportStartDate = startDate;
			birtParams.reportEndDate = endDate;
			birtParams.hourScale = true;
                break;
            case SELECT_MONTH: // 1st of a month , last day of a month
                cal.set(form.getYear(), form.getMonth(), 1);
                birtParams.reportStartDate = BIRT_PARAMS_DATE_FORMAT.format(cal.getTime());
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                birtParams.reportEndDate = BIRT_PARAMS_DATE_FORMAT.format(cal.getTime());
//                int month = form.getMonth();
//                month += 1; // month is 0 based
//                int year = form.getYear();
//                int first = 1;
//                String startDate = year+"-"+month+"-"+first;
//                GregorianCalendar calendar = new GregorianCalendar();
//                calendar.setTime(isoFormat.parse(startDate));
//                int last = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
//                String endDate = year+"-"+month+"-"+last;
//                birtParams.reportStartDate = startDate;
//                birtParams.reportEndDate = endDate;
                break;
            case SELECT_PERIOD: // any startDate and any endDate
            	if (StringUtils.isBlank(form.getStartDate_localized())) {
            		return null;
            	}
                birtParams.reportStartDate = BIRT_PARAMS_DATE_FORMAT.format(localeFormat.parse(form.getStartDate_localized()));
                birtParams.reportEndDate = BIRT_PARAMS_DATE_FORMAT.format(localeFormat.parse(form.getStopDate_localized()));
                break;
		case LAST_FORTNIGHT:
			// $FALL-THROUGH$
			
		case LAST_WEEK:
			// $FALL-THROUGH$

		case NONE:
			// Nothing to do here
			
		default: break;
		}
		
		return birtParams;
	}
	
    private String getSelectedTargetList(ComBirtStatForm form) {
		String[] sel = form.getSelectedTargets();
		String selectedTargets = "";
		
		if (null != sel && sel.length>0) {
			selectedTargets = StringUtils.join(sel,",");
		}
		
		return selectedTargets;
    }

	
	private boolean isBounceStatistic(String reportName) {
		return StringUtils.equals("mailing_bounces.rptdesign", reportName);
	}
	
	private boolean isSinglePerLinkStatistic(String reportName) {
		return StringUtils.equals("mailing_single_linkclicks_progress.rptdesign", reportName);
	}
	
	private boolean isPerLinkStatistic(String reportName) {
		return StringUtils.equals("mailing_linkclicks_progress.rptdesign", reportName);
	}
	
	private boolean isTotalOpeningStatistic(String reportName) {
		return StringUtils.equals("mailing_net_and_gross_openings_progress.rptdesign", reportName);
	}
	
	private boolean isMailingSummaryStatistic(String reportName) {
		return StringUtils.equals("mailing_summary.rptdesign", reportName);
	}

	private boolean isTopDomainsStatistic(String reportName) {
		return StringUtils.equals("top_domains.rptdesign", reportName);
	}
	
	private boolean isIndividualMailingStatistic(String reportName) {
		return StringUtils.equals("individual.mailing", reportName);
	}

	private String createIndividualUrl(HttpServletRequest request, ComBirtStatForm form) throws Exception {
		ComAdmin admin = AgnUtils.getAdmin(request);
		
		assert admin != null;
		
		String language = StringUtils.defaultIfEmpty(admin.getAdminLang(), "EN");
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("mailing_stat_plugins.do");
		sb.append(";jsessionid=").append(request.getSession(false).getId());
		sb.append("?mailingID=").append( form.getMailingID());
		sb.append("&mailinglistID=").append( form.getMailingListID());
		sb.append("&companyID=").append( form.getCompanyID());
		sb.append("&targets=").append( getSelectedTargetList( form));
		sb.append("&language=").append( language);
		sb.append("&uid=").append(birtStatisticsService.generateUid(admin));
		sb.append("&emmsession=").append( request.getSession(false).getId());
		sb.append("&__format=").append( form.getReportFormat());
		sb.append("&__svg=true");
		sb.append("&recipientType=" + CommonKeys.TYPE_ALL_SUBSCRIBERS);
		
		return sb.toString();
	}

    private DateMode getDateMode(String reportName, int mailingId) {
        if (reportWithPeriods.containsKey(reportName)) {
            if (isMailingSummaryStatistic(reportName)) {
                int mailingType = mailingDao.getMailingType(mailingId);
                if ((mailingType != Mailing.TYPE_ACTIONBASED) &&
                        (mailingType != Mailing.TYPE_DATEBASED) &&
                        (mailingType != Mailing.TYPE_INTERVAL)) {
                    return DateMode.NONE;
                }
            }
            return reportWithPeriods.get(reportName);
        } else if (reportName.endsWith("_progress.rptdesign")) {
            return DateMode.LAST_TENHOURS;
        } else {
            return DateMode.NONE;
        }
    }

    public ComMailingDao getMailingDao() {
        return mailingDao;
    }

    @Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    public ComCompanyDao getCompanyDao() {
        return companyDao;
    }

    @Required
    public void setCompanyDao(ComCompanyDao companyDao) {
        this.companyDao = companyDao;
    }

    @Required
    public void setAdminPreferencesDao(AdminPreferencesDao adminPreferencesDao) {
        this.adminPreferencesDao = adminPreferencesDao;
    }

	@Required
	public void setBirtReportService(ComBirtReportService birtReportService) {
		this.birtReportService = birtReportService;
	}
}
