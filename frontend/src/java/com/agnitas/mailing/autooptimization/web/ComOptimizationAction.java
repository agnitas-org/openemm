/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.web;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Company;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.impl.MaildropDeleteException;
import org.agnitas.emm.core.commons.util.Constants;
import org.agnitas.emm.core.mailing.MailingAllReadySentException;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.BaseDispatchActionSupport;
import org.agnitas.web.StrutsActionBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.birtstatistics.optimization.dto.OptimizationStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.beans.impl.ComOptimizationImpl;
import com.agnitas.mailing.autooptimization.service.ComOptimizationScheduleService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import com.agnitas.mailing.autooptimization.service.OptimizationIsFinishedException;
import com.agnitas.mailing.autooptimization.web.forms.ComOptimizationForm;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;

public class ComOptimizationAction extends BaseDispatchActionSupport {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComOptimizationAction.class);
	
	private ComOptimizationService optimizationService;
	
	private ComOptimizationScheduleService optimizationScheduleService;
    private MailinglistApprovalService mailinglistApprovalService;
    private BirtStatisticsService birtStatisticsService;

    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }
    
    @Required
    public final void setBirtStatisticsService(final BirtStatisticsService birtStatisticsService) {
    	this.birtStatisticsService = Objects.requireNonNull(birtStatisticsService, "Birt statistics service is null");
    }

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			 {
		if (!checkLogon(request)) {
			return mapping.findForward("logon");
		}

		int companyID = AgnUtils.getCompanyID(request);
		ComOptimizationForm optimizationForm = (ComOptimizationForm) form;
		
		List<ComOptimization> optimizationsList = optimizationService.list(optimizationForm.getCampaignID(), companyID);
		request.setAttribute("optimizations", optimizationsList);

		return mapping.findForward("list");
	}
	
	public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		AgnUtils.setAdminDateTimeFormatPatterns(request);
		ComOptimizationForm optimizationForm = (ComOptimizationForm) form;
		ComAdmin admin = AgnUtils.getAdmin(request);
		
		if (optimizationForm.isNewOptimization() && optimizationForm.getOptimizationID() == 0) { // use case : tried to save a new optimization with some errors, validation failed
			optimizationForm.setNewOptimization(false); // it isn't really 'new' , you have tried to save it !
			return newOptimization(mapping, form, request, response);
		}
		
		int companyID =	AgnUtils.getCompanyID(request);
		ComOptimization optimization = optimizationService.get(optimizationForm.getOptimizationID(), companyID);
        optimizationForm.setTargetExpression(optimization.getTargetExpression());
        optimizationForm.setTargetMode(optimization.getTargetMode());
		
		if (optimization.getThreshold() > 0) {
			optimizationForm.setThresholdString(Integer.toString(optimization.getThreshold()));
		}
		
		if (optimization.getSendDate() != null) {
			optimizationForm.setResultSendDateAsString(admin.getDateTimeFormat().format(optimization.getSendDate()));
		}
		
		if (optimization.getTestMailingsSendDate() != null) {
			optimizationForm.setTestMailingsSendDateAsString(admin.getDateTimeFormat().format(optimization.getTestMailingsSendDate()));
		}

		optimizationForm.setOptimization(optimization);
		setCommonRequestAttributes(request, companyID, optimization, admin);

        optimizationForm.setReportUrl(getReportUrl(request, optimizationForm));
        optimizationForm.setPreviousAction(StrutsActionBase.ACTION_VIEW);
        request.setAttribute("frameHeight", "900");

        writeUserActivityLog(admin, "view Auto-Optimization", getOptimizationDescription(optimization));

        return mapping.findForward("view");
	}

    public String getReportUrl(HttpServletRequest request, ComOptimizationForm optimizationForm) throws Exception {
		ComAdmin admin = AgnUtils.getAdmin(request);
		OptimizationStatisticDto optimizationStatisticDto = new OptimizationStatisticDto();
        optimizationStatisticDto.setFormat("html");
        optimizationStatisticDto.setMailingId(optimizationForm.getGroup1());
        optimizationStatisticDto.setOptimizationId(optimizationForm.getOptimizationID());
        optimizationStatisticDto.setCompanyId(optimizationForm.getCompanyID());
        optimizationStatisticDto.setRecipientType(CommonKeys.TYPE_ALL_SUBSCRIBERS);

        return birtStatisticsService.getOptimizationStatisticUrl(admin, optimizationStatisticDto);
    }

	public ActionForward newOptimization(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
			
			ComAdmin admin = AgnUtils.getAdmin(request);
			int companyID =	AgnUtils.getCompanyID(request);
		
			ComOptimizationForm optimizationForm = (ComOptimizationForm) form;

			ComOptimization optimization = null;
			if (optimizationForm.isNewOptimization()) {
				optimization = new ComOptimizationImpl();
				optimization.setCampaignID(optimizationForm.getCampaignID());
				optimization.setCompanyID(companyID);
				optimizationForm.setOptimization(optimization);
				optimizationForm.setTargetMode(Mailing.TARGET_MODE_AND);
			} else {
				optimization = optimizationForm.getOptimization();
				if (optimization.getThreshold() != 0) {
					optimizationForm.setThresholdString(Integer.toString(optimization.getThreshold()));
				}
			}

			setCommonRequestAttributes(request, companyID, optimization, admin);
			
			return mapping.findForward("view");
	}

	public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		AgnUtils.setAdminDateTimeFormatPatterns(request);
		ActionMessages errors = new ActionMessages();
		ComAdmin admin = AgnUtils.getAdmin(request);
        int companyID =	AgnUtils.getCompanyID(request);

		ComOptimizationForm optimizationForm = (ComOptimizationForm) form;
		ComOptimization optimization = optimizationForm.getOptimization();

		optimization.setCompanyID(companyID);
        optimization.setTargetExpression(optimizationForm.getTargetExpression());
        optimization.setTargetMode(optimizationForm.getTargetMode());

		try {
			String testMailingsSendDateAsString = optimizationForm.getTestMailingsSendDateAsString();
			if (StringUtils.isNotBlank(testMailingsSendDateAsString)) {
				optimization.setTestMailingsSendDate(admin.getDateTimeFormat().parse(testMailingsSendDateAsString));
			}
		} catch (ParseException e) {
			logger.error("Could not parse date : " + optimizationForm.getTestMailingsSendDateAsString(), e);
		}

		try {
			String resultSendDateAsString = optimizationForm.getResultSendDateAsString();
			if (StringUtils.isNotBlank(resultSendDateAsString)) {
				optimization.setSendDate(admin.getDateTimeFormat().parse(resultSendDateAsString));
			}
		} catch (ParseException e) {
			logger.error("Could not parse date : " + optimizationForm.getResultSendDateAsString(), e);
		}

		optimization.setThreshold(NumberUtils.toInt(optimizationForm.getThresholdString(), 0));

		boolean is_new = (optimization.getId() == 0);

        ComOptimization cachedOptimization = null;
        if (!is_new) {
            cachedOptimization = optimizationService.get(optimization.getId(), companyID);
        }

        int newOptimizationID = optimizationService.save(optimization);
		optimization.setId(newOptimizationID);
		setCommonRequestAttributes(request, companyID, optimization, admin);

		ActionMessages actionMessages = new ActionMessages();
		actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
		saveMessages(request, actionMessages);

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}

        if (is_new) {
            writeUserActivityLog(admin, "create Auto-Optimization", getOptimizationDescription(optimization));
        } else if (cachedOptimization != null) {
            writeAutoOptimizationChangesLog(optimization, cachedOptimization, admin);
        } else {
            logger.error("Log Auto-Optimization changes error. No cached optimization");
        }

        return mapping.findForward("view");
	}
	
	public ActionForward confirmDelete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {

		ComOptimizationForm optimizationForm = (ComOptimizationForm) form;
		optimizationForm.setOptimization(optimizationService.get(optimizationForm.getOptimizationID(), optimizationForm.getCompanyID()));
		
		return mapping.findForward("confirmDelete");
	}
	
	
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)  {
	
		ComOptimizationForm optimizationForm = (ComOptimizationForm) form;
		ComOptimization deleteOptimization = optimizationService.get(optimizationForm.getOptimizationID(), optimizationForm.getCompanyID());
		ComAdmin admin = AgnUtils.getAdmin(request);
		ActionErrors errors = new ActionErrors();
		
		try {
			optimizationService.delete(deleteOptimization);
		} catch (MaildropDeleteException e) {
			logger.error("Could not delete optimization with ID: " + optimizationForm.getOptimizationID());
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.delete", optimizationForm.getShortname() ));
		}
		saveErrors(request, errors);

		if (errors.isEmpty()) {
			ActionMessages actionMessages = new ActionMessages();
			actionMessages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("default.selection.deleted"));
			saveMessages(request, actionMessages);
            writeUserActivityLog(admin, "delete Auto-Optimization", getOptimizationDescription(deleteOptimization));
		}
				
		return list(mapping, form, request, response);
	}
	
    //  schedule the optimization. Save the senddates of the testmailings, and the senddate of the final mailing.
	public ActionForward schedule(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		AgnUtils.setAdminDateTimeFormatPatterns(request);
		ActionMessages errors = new ActionMessages();
		ComAdmin admin = AgnUtils.getAdmin(request);
		Company company = admin.getCompany();
				
		ComOptimizationForm scheduleForm =  (ComOptimizationForm) form;
		ComOptimization optimization = optimizationService.get(scheduleForm.getOptimizationID(), company.getId());
		
		try {
			optimization.setTestMailingsSendDate(admin.getDateTimeFormat().parse(scheduleForm.getTestMailingsSendDateAsString()));
			optimization.setSendDate(admin.getDateTimeFormat().parse(scheduleForm.getResultSendDateAsString()));
			optimizationScheduleService.scheduleOptimization(optimization);

            DateFormat formatter = new SimpleDateFormat(Constants.DATE_PATTERN_FULL);
            String testMailingsSendDate = formatter.format(optimization.getTestMailingsSendDate());
            String sendDate = formatter.format(optimization.getSendDate());

            writeUserActivityLog(admin, "do schedule Auto-Optimization", getOptimizationDescription(optimization) +
                    " started. Send date of the test mailings: " + testMailingsSendDate +
                            ", send date of the final mailing: " + sendDate);
		} catch (ParseException e) {
			logger.error("Could not parse date : " + scheduleForm.getResultSendDateAsString() , e);
			errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.resultsenddate", admin.getDateTimeFormat().toPattern()));
			errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.resultsenddate", admin.getDateTimeFormat().toPattern()));
			saveErrors(request, errors);
		} catch (MailingAllReadySentException e) {
			logger.error("Could not schedule optimization. One of the test mailings has been allready sent ! Optimization-ID: " +optimization.getId());
			errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.schedule"));
		} catch (OptimizationIsFinishedException e) {
			logger.error("Could not schedule optimization. Optimization has not the right state. Optimization-ID: " +optimization.getId() + " Status-ID:  " + optimization.getStatus() );
			errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.schedule"));
		} catch (MaildropDeleteException e) {
			logger.error("Could not schedule optimization. Previous unschedule failed ! Optimization-ID: " +optimization.getId() + " Status-ID:  " + optimization.getStatus() );
			errors.add( ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.schedule"));
		}
		saveErrors(request, errors);
				
		if (errors.isEmpty() ) {
			ActionMessages actionMessages = new ActionMessages();
			actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			saveMessages(request, actionMessages);
		}
			
		return view(mapping, form, request, response);
	}

	public ActionForward unSchedule(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

		ActionMessages errors = new ActionMessages();
		ComAdmin admin = AgnUtils.getAdmin(request);

		ComOptimizationForm scheduleForm = (ComOptimizationForm) form;
		ComOptimization optimization = optimizationService.get(scheduleForm.getOptimizationID(), admin.getCompanyID());

		try {
			optimizationScheduleService.unscheduleOptimization(optimization);
			writeUserActivityLog(admin, "do unschedule Auto-Optimization", getOptimizationDescription(optimization) + " stopped");
		} catch (MaildropDeleteException e) {
			logger.error("Could not unschedule optimization." + optimization.getId() + " Status-ID:  " + optimization.getStatus());
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.autooptimization.errors.unschedule"));
		}

		if (errors.isEmpty()) {
			ActionMessages actionMessages = new ActionMessages();
			actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			saveMessages(request, actionMessages);
			scheduleForm.setTestMailingsSendDateAsString(null);
			scheduleForm.setResultSendDateAsString(null);
		} else {
			saveErrors(request, errors);
		}

		return view(mapping, form, request, response);
	}
	
	@Override
	public ActionForward cancelled(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!checkLogon(request)) {
			return mapping.findForward("logon");
		}
        ComOptimizationForm aForm = (ComOptimizationForm) form;
        if (aForm.getPreviousAction() == StrutsActionBase.ACTION_VIEW){
            return view(mapping, form, request, response);
        } else {
            return list(mapping, form, request, response);
        }
	}
	
	@Override
	public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!checkLogon(request)) {
			return mapping.findForward("logon");
		}
		return list(mapping, form, request, response);
	}
	
	private boolean checkLogon(HttpServletRequest req) {
		// Is there a valid user logged on?
		return AgnUtils.getAdmin(req) != null;
	}

	public void setOptimizationService(ComOptimizationService optimizationService) {
		this.optimizationService = optimizationService;
	}
	
	public void setOptimizationScheduleService(ComOptimizationScheduleService optimizationScheduleService) {
		this.optimizationScheduleService = optimizationScheduleService;
	}

	private void setCommonRequestAttributes(HttpServletRequest request, @VelocityCheck int companyID, ComOptimization optimization, ComAdmin admin) {
		request.setAttribute("splitTypes", optimizationService.getSplitTypeList(companyID, optimization.getSplitType(), admin.getAdminLang()));
		request.setAttribute("mailingLists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
		request.setAttribute("targets",optimizationService.getTargets(optimization.getTargetExpression(), companyID));
        request.setAttribute("chosenTargets",optimizationService.getChosenTargets(optimization.getTargetExpression(), companyID));
		request.setAttribute("mailtracking", admin.getCompany().getMailtracking() == 1);
		request.setAttribute("adminTimeZone", admin.getAdminTimezone());
		request.setAttribute("adminDateFormatPattern", admin.getDateFormat().toPattern());

		// handle the 'groups' select(s)
		request.setAttribute("groups", optimizationService.getTestMailingList(optimization));
	}

    /**
     * Compare existed and edited Auto-Optimization and write changes in user log
     *
     * @param admin Admin
     * @param newOptimization the form passed from the jsp
     * @param oldOptimization existed Auto-Optimization data
     */
    private void writeAutoOptimizationChangesLog(ComOptimization newOptimization, ComOptimization oldOptimization, ComAdmin admin) {
        try {
			final String description = getOptimizationDescription(oldOptimization);

            // Log name changes
            if (!StringUtils.equals(oldOptimization.getShortname(), newOptimization.getShortname())) {
                writeUserActivityLog(admin, "edit Auto-Optimization", description + " renamed as " + newOptimization.getShortname());
            }

            // Log description changes
            final String oldDescription = StringUtils.trimToEmpty(oldOptimization.getDescription());
            final String newDescription = StringUtils.trimToEmpty(newOptimization.getDescription());

            if (!oldDescription.equals(newDescription)) {
				String action;
				if (oldDescription.isEmpty()) {
					action = "added";
				} else {
					if (newDescription.isEmpty()) {
						action = "removed";
					} else {
						action = "changed";
					}
				}
				writeUserActivityLog(admin, "edit Auto-Optimization", description + ". Description " + action);
            }

            // Log mailing list changes
            if (oldOptimization.getMailinglistID() != newOptimization.getMailinglistID()) {
                writeUserActivityLog(admin, "edit Auto-Optimization", description + ". Mailing list changed from ID = " +
						oldOptimization.getMailinglistID() + " to ID = " + newOptimization.getMailinglistID());
            }

            // Log target group changes
            final String oldTargetsExpr = StringUtils.trimToEmpty(oldOptimization.getTargetExpression());
            final String newTargetsExpr = StringUtils.trimToEmpty(newOptimization.getTargetExpression());

            if (!oldTargetsExpr.equals(newTargetsExpr)) {
				Set<String> oldTargets = new HashSet<>(Arrays.asList(oldTargetsExpr.split("[,\\s]+")));
				Set<String> newTargets = new HashSet<>(Arrays.asList(oldTargetsExpr.split("[,\\s]+")));

				// Log removed groups
				for (String targetId : oldTargets) {
					if (!newTargets.contains(targetId)) {
						writeUserActivityLog(admin, "edit Auto-Optimization", description + ". Removed target group with ID = " + targetId);
					}
				}

				// Log added groups
				for (String targetId : newTargets) {
					if (!oldTargets.contains(targetId)) {
						writeUserActivityLog(admin, "edit Auto-Optimization", description + ". Added target group with ID = " + targetId);
					}
				}
            }

            // Log decision-criteria changes
            if (oldOptimization.getEvalType() != newOptimization.getEvalType()) {
                writeUserActivityLog(admin, "edit Auto-Optimization", description +
                        ". Decision-Criteria changed from " + getDecisionCriteriaName(oldOptimization.getEvalType()) +
                        " to " + getDecisionCriteriaName(newOptimization.getEvalType()));
            }

            // Log threshold changes
            if (oldOptimization.getThreshold() != newOptimization.getThreshold()) {
                writeUserActivityLog(admin, "edit Auto-Optimization", description +
						". Threshold changed from " + oldOptimization.getThreshold() +
                                " to " + newOptimization.getThreshold());
            }

            // Log "Check for duplicate records in emails" checked/unchecked
            if (oldOptimization.isDoubleCheckingActivated() != newOptimization.isDoubleCheckingActivated()) {
				writeUserActivityLog(admin, "edit Auto-Optimization", description + ". Check for duplicate records in emails " +
						(newOptimization.isDoubleCheckingActivated() ? "checked" : "unchecked"));
            }

            // Log list-split changes
            if (!StringUtils.equals(oldOptimization.getSplitType(), newOptimization.getSplitType())) {
                writeUserActivityLog(admin, "edit Auto-Optimization", description + ". List-Split changed");
            }

            // Log test mailing changes
            if (oldOptimization.getGroup1() != newOptimization.getGroup1()) {
                writeTestGroupChanges(oldOptimization, 1, oldOptimization.getGroup1(), newOptimization.getGroup1(), admin);
            }
            if (oldOptimization.getGroup2() != newOptimization.getGroup2()) {
                writeTestGroupChanges(oldOptimization, 2, oldOptimization.getGroup2(), newOptimization.getGroup2(), admin);
            }
            if (oldOptimization.getGroup3() != newOptimization.getGroup3()) {
                writeTestGroupChanges(oldOptimization, 3, oldOptimization.getGroup3(), newOptimization.getGroup3(), admin);
            }
            if (oldOptimization.getGroup4() != newOptimization.getGroup4()) {
                writeTestGroupChanges(oldOptimization, 4, oldOptimization.getGroup4(), newOptimization.getGroup4(), admin);
            }
            if (oldOptimization.getGroup5() != newOptimization.getGroup5()) {
                writeTestGroupChanges(oldOptimization, 5, oldOptimization.getGroup5(), newOptimization.getGroup5(), admin);
            }

            if (logger.isInfoEnabled()) {
                logger.info("saveAuto-Optimization: save " + description);
            }
        } catch (Exception e) {
            logger.error("Log Auto-Optimization changes error" + e);
        }
    }

	/**
	 * Get a human-readable description of an auto-optimization for a user activity log.
	 * @param optimization an auto-optimization entity to describe.
	 * @return a description.
	 */
	private String getOptimizationDescription(ComOptimization optimization) {
		return optimization.getShortname() + " (" + optimization.getId() + ")";
	}

    /**
     * Compare existed and edited groupId and write changes in user log
     *
     * @param optimization auto-optimization
     * @param groupIndex test-group sequential number
     * @param oldGroupId existed group ID
     * @param newGroupId new group ID
     * @param admin Admin
     */
    private void writeTestGroupChanges(ComOptimization optimization, int groupIndex, int oldGroupId, int newGroupId, ComAdmin admin){
        if (oldGroupId == newGroupId) {
            return;
        }

		final String description = getOptimizationDescription(optimization) + ". Test-Group " + groupIndex;

        if (oldGroupId == 0) {
            writeUserActivityLog(admin, "edit Auto-Optimization", description + " added, ID = " + newGroupId);
        } else {
			if (newGroupId == 0) {
				writeUserActivityLog(admin, "edit Auto-Optimization", description + " removed");
			} else {
				writeUserActivityLog(admin, "edit Auto-Optimization", description +
						" changed from ID = " + oldGroupId + " to ID = " + newGroupId);
			}
		}
    }

    /**
     * Return Decision-Criteria text representation by id
     *
     * @param criteria Decision-Criteria Id
     * @return   Decision-Criteria text representation
     */
    private String getDecisionCriteriaName(WorkflowDecision.WorkflowAutoOptimizationCriteria criteria){
        switch (criteria){
            case AO_CRITERIA_CLICKRATE:
                return "Click rate";
            case AO_CRITERIA_OPENRATE:
                return "Open rate";
            case AO_CRITERIA_REVENUE:
                return "Revenue";
            default:
                return "Unknown Decision-Criteria";
        }
    }
}
