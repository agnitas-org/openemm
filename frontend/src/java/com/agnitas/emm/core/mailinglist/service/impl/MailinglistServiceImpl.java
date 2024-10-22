/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.Mailing;
import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.common.exceptions.ShortnameTooShortException;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.birtreport.bean.ComLightweightBirtReport;
import com.agnitas.emm.core.birtreport.dao.ComBirtReportDao;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.objectusage.common.ObjectUsage;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.objectusage.common.ObjectUserType;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.MailinglistImpl;
import org.agnitas.dao.MailingStatus;
import org.agnitas.dao.MailinglistApprovalDao;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILINGLISTS_KEY;

public class MailinglistServiceImpl implements MailinglistService {

    private MailinglistDao mailinglistDao;

    private MailinglistApprovalDao mailinglistApprovalDao;
    
    private MailingDao mailingDao;
    
    private ComBindingEntryDao bindingEntryDao;
    
    private ComBirtReportDao birtReportDao;
    
    private ComRecipientDao recipientDao;
    
    private ComTargetDao targetDao;
    private BulkActionValidationService<Integer, Mailinglist> bulkActionValidationService;

    @Required
    public void setBindingEntryDao(ComBindingEntryDao bindingEntryDao) {
        this.bindingEntryDao = bindingEntryDao;
    }

    @Required
    public void setBulkActionValidationService(BulkActionValidationService<Integer, Mailinglist> bulkActionValidationService) {
        this.bulkActionValidationService = bulkActionValidationService;
    }

    @Required
    public void setMailinglistDao(MailinglistDao mailinglistDao) {
        this.mailinglistDao = mailinglistDao;
    }

    @Required
    public void setMailinglistApprovalDao(MailinglistApprovalDao mailinglistApprovalDao) {
        this.mailinglistApprovalDao = mailinglistApprovalDao;
    }

    @Required
    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setBirtReportDao(ComBirtReportDao birtReportDao) {
        this.birtReportDao = birtReportDao;
    }
 
	@Required
    public void setRecipientDao(ComRecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }

    @Required
    public void setTargetDao(ComTargetDao targetDao) {
        this.targetDao = targetDao;
    }
    
    @Override
    public void bulkDelete(Collection<Integer> mailinglistIds, int companyId) {
        if(mailinglistIds != null) {
            for (int mailinglistId : mailinglistIds) {
                mailinglistDao.deleteMailinglist(mailinglistId, companyId);
            }
        }
    }

    @Override
    public List<Mailing> getAllDependedMailing(Set<Integer> mailinglistIds, int companyId) {
        return mailinglistIds
                .stream()
                .flatMap(id->mailingDao.getMailingsForMLID(companyId,id).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRecipientBindings(Set<Integer> mailinglistIds, int companyId){
        bindingEntryDao.deleteRecipientBindingsByMailinglistID(mailinglistIds, companyId);
    }

    @Override
    public List<Mailinglist> getMailinglists(int companyId) {
        return mailinglistDao.getMailinglists(companyId);
    }

    @Override
    public Mailinglist getMailinglist(int mailinglistId, int companyId){
    	// TODO Checking validity of mailinglist ID should be done here
    	
        return mailinglistDao.getMailinglist(mailinglistId, companyId);
    }

    @Override
    public boolean exist(int mailinglistId, int companyId){
        return mailinglistDao.exist(mailinglistId, companyId);
    }

    @Override
    public boolean existAndEnabled(Admin admin, int mailingListId) {
        if (mailingListId <= 0) {
            return false;
        }

        if (mailinglistDao.exist(mailingListId, admin.getCompanyID())) {
            return mailinglistApprovalDao.isAdminHaveAccess(admin.getCompanyID(), admin.getAdminID(), mailingListId);
        }

        return false;
    }

    @Override
    public boolean isFrequencyCounterEnabled(Admin admin, int mailingListId) {
        if (mailinglistApprovalDao.isAdminHaveAccess(admin.getCompanyID(), admin.getAdminID(), mailingListId)) {
            Mailinglist mailinglist = mailinglistDao.getMailinglist(mailingListId, admin.getCompanyID());
            if (mailinglist != null) {
                return mailinglist.isFrequencyCounterEnabled();
            }
        }

        return false;
    }

    @Override
    public String getMailinglistName(int mailinglistId, int companyId) {
        return mailinglistDao.getMailinglistName(mailinglistId, companyId);
    }

    @Override
    public ServiceResult<List<Mailinglist>> getAllowedForDeletion(Set<Integer> ids, Admin admin) {
        ServiceResult<List<Mailinglist>> result =
                bulkActionValidationService.checkAllowedForDeletion(ids, id -> getMailinglistForDeletion(id, admin));

        validateDeleteOfLastMailinglist(result.getResult(), admin.getCompanyID());
        return result;
    }

    @Override
    public List<Integer> delete(Set<Integer> ids, Admin admin) {
        List<Integer> allowedIds = ids.stream()
                .map(id -> getMailinglistForDeletion(id, admin))
                .filter(ServiceResult::isSuccess)
                .map(r -> r.getResult().getId())
                .collect(Collectors.toList());

        bulkDelete(allowedIds, admin.getCompanyID());

        return allowedIds;
    }
    
    private ServiceResult<Mailinglist> getMailinglistForDeletion(int id, Admin admin) {
        Mailinglist mailinglist = getMailinglist(id, admin.getCompanyID());
        if (mailinglist == null) {
            return ServiceResult.errorKeys("error.general.missing");
        }

        ObjectUsages usages = collectObjectUsages(id, admin.getCompanyID());
        if (!usages.isEmpty()) {
            return ServiceResult.error(usages.toMessage("error.mailinglist.cannot_delete_mailinglists", admin.getLocale()));
        }
        
        return ServiceResult.success(mailinglist);
    }

    private void validateDeleteOfLastMailinglist(Collection<?> items, int companyId) {
        int mailinglistsCount = mailinglistDao.getCountOfMailinglists(companyId);
        if (mailinglistsCount <= CollectionUtils.size(items)) {
            throw new RequestErrorException("error.mailinglist.delete.last");
        }
    }

    private ObjectUsages collectObjectUsages(int id, int companyId) {
        List<Mailing> affectedMailings = getUsedMailings(Set.of(id), companyId);

        List<ObjectUsage> usages = affectedMailings.stream()
                .map(m -> new ObjectUsage(ObjectUserType.MAILING, m.getId(), m.getShortname()))
                .collect(Collectors.toList());

        return new ObjectUsages(usages);
    }

    @Override
    public List<Mailinglist> getAllMailingListsNames(int companyId) {
        return mailinglistDao.getMailingListsNames(companyId);
    }

    @Override
    public List<ComLightweightBirtReport> getConnectedBirtReportList(int mailinglistId, int companyId) {
        List<Map<String, Object>> reportParams = birtReportDao.getReportParamValues(companyId, "selectedMailinglists");
        List<ComLightweightBirtReport> allReportslist = birtReportDao.getLightweightBirtReportList(companyId);
        // Put reports to Map to avoid several iterations on List of all reports
        Map<Integer, ComLightweightBirtReport> allReportsMap = new HashMap<>();
        for (ComLightweightBirtReport report: allReportslist) {
            allReportsMap.put(report.getId(), report);
        }
        List<ComLightweightBirtReport> connectedReports = new ArrayList<>();
        for (Map<String, Object> paramRow : reportParams) {
            String value = (String) paramRow.get("parameter_value");
            List<String> mailinglistIds = AgnUtils.splitAndTrimStringlist(value);
            for (String mailinglistIdParam: mailinglistIds) {
                try {
                    if (mailinglistId == Integer.parseInt(mailinglistIdParam)) {
                        int reportIDFromParam = ((Number) paramRow.get("report_id")).intValue();
                        connectedReports.add(allReportsMap.get(reportIDFromParam));
                        break;
                    }
                } catch (NumberFormatException e) {
                    break;
                }
            }
        }
        return connectedReports;
    }

    private void deleteMailinglistFromReports(int mailinglistIdToDelete, int companyId) {
        List<Map<String, Object>> reportParams = birtReportDao.getReportParamValues(companyId, MAILINGLISTS_KEY);
        for (Map<String, Object> paramRow : reportParams) {
            List<Integer> mailinglistIds = BirtReportSettingsUtils.convertStringToIntList((String) paramRow.get("parameter_value"));

            if (mailinglistIds.contains(mailinglistIdToDelete)) {
                mailinglistIds.removeIf(m -> m == mailinglistIdToDelete);
                int reportId = ((Number) paramRow.get("report_id")).intValue();
                int reportType = ((Number) paramRow.get("report_type")).intValue();
                birtReportDao.updateReportMailinglists(reportId, reportType, mailinglistIds);
            }
        }
    }
	
	@Override
	public int saveMailinglist(int companyId, MailinglistDto mailinglist) throws ShortnameTooShortException {
        validateBeforeSave(mailinglist);

		MailinglistImpl mailinglistForSave = new MailinglistImpl();
		mailinglistForSave.setId(mailinglist.getId());
		mailinglistForSave.setCompanyID(companyId);
		mailinglistForSave.setShortname(mailinglist.getShortname());
		mailinglistForSave.setDescription(mailinglist.getDescription());
		mailinglistForSave.setFrequencyCounterEnabled(mailinglist.isFrequencyCounterEnabled());
        mailinglistForSave.setSenderEmail(mailinglist.getSenderEmail());
        mailinglistForSave.setReplyEmail(mailinglist.getReplyEmail());

		int mailinglistId;
		if (mailinglist.getId() == 0) {
			mailinglistId = mailinglistDao.createMailinglist(companyId, mailinglistForSave);
			int targetId = mailinglist.getTargetId();
            ComTarget target = targetDao.getTarget(targetId, companyId);
            
            // Add recipients from a target group (if specified)
            if (target != null) {
                bindingEntryDao.addTargetsToMailinglist(companyId, mailinglistId, target, mailinglist.getMediatypes());
            }
		} else {
			mailinglistId = mailinglistDao.updateMailinglist(companyId, mailinglistForSave);
		}
		return mailinglistId;
	}

    private void validateBeforeSave(MailinglistDto mailinglist) throws ShortnameTooShortException {
        if (mailinglist.getShortname() == null || mailinglist.getShortname().length() < 3) {
            throw new ShortnameTooShortException(mailinglist.getShortname());
        }

        Map<String, Message> validationErrors = new HashMap<>();

        if (StringUtils.isNotBlank(mailinglist.getSenderEmail()) && !AgnUtils.isEmailValid(mailinglist.getSenderEmail())) {
            validationErrors.put("senderEmail", Message.of("error.invalid.email"));
        }

        if (StringUtils.isNotBlank(mailinglist.getReplyEmail()) && !AgnUtils.isEmailValid(mailinglist.getReplyEmail())) {
            validationErrors.put("replyEmail", Message.of("error.invalid.email"));
        }

        if (!validationErrors.isEmpty()) {
            throw new RequestErrorException(validationErrors);
        }
    }
	
	@Override
	public boolean isShortnameUnique(String newShortname, int mailinglistId, int companyId) {
		Mailinglist mailinglist = mailinglistDao.getMailinglist(mailinglistId, companyId);
		String oldShortname = mailinglist != null ? mailinglist.getShortname() : "";
		
        return StringUtils.equals(oldShortname, newShortname)
                || !mailinglistDao.mailinglistExists(newShortname, companyId);
    }
    
    @Override
    @Transactional
    public boolean deleteMailinglist(int mailinglistId, int companyId) {
        if (mailinglistId <= 0 || companyId <= 0) {
            return false;
        }

        List<Mailing> dependentMailings = getUsedMailings(Set.of(mailinglistId), companyId);
        if (!dependentMailings.isEmpty()) {
            return false;
        }

        deleteMailinglistFromReports(mailinglistId, companyId);
        return mailinglistDao.deleteMailinglist(mailinglistId, companyId);
    }
    
    @Override
    public void deleteMailinglistBindingRecipients(int companyId, int mailinglistId, boolean onlyActiveUsers, boolean noAdminAndTestUsers) {
        String tmpTable = recipientDao.createTmpTableByMailinglistID(companyId, mailinglistId);
        recipientDao.deleteRecipientsBindings(mailinglistId, companyId, onlyActiveUsers, noAdminAndTestUsers);
        recipientDao.deleteAllNoBindings(companyId, tmpTable);
    }

    @Override
    public JSONArray getMailingListsJson(Admin admin) {
        JSONArray mailingListsJson = new JSONArray();

        for (Mailinglist mailinglist : mailinglistDao.getMailinglists(admin.getCompanyID(), admin.getAdminID())) {
            JSONObject entry = new JSONObject();

            entry.element("id", mailinglist.getId());
            entry.element("shortname", mailinglist.getShortname());
            entry.element("description", mailinglist.getDescription());
            entry.element("changeDate", DateUtilities.toLong(mailinglist.getChangeDate()));
            entry.element("creationDate", DateUtilities.toLong(mailinglist.getCreationDate()));
            entry.element("isFrequencyCounterEnabled", mailinglist.isFrequencyCounterEnabled());
            if (admin.isRedesignedUiUsed()) {
                entry.element("restrictedForSomeAdmins", mailinglist.isRestrictedForSomeAdmins());
            }

            mailingListsJson.element(entry);
        }

        return mailingListsJson;
    }

    @Override
    public boolean mailinglistDeleted(int mailinglistId, int companyId) {
        return mailinglistDao.mailinglistDeleted(mailinglistId, companyId);
    }

    @Override
    public Mailinglist getDeletedMailinglist(int mailinglistId, int companyId) {
        return mailinglistDao.getDeletedMailinglist(mailinglistId, companyId);
    }

    @Override
    public List<Mailing> getUsedMailings(Set<Integer> mailinglistIds, int companyId) {
        return getAllDependedMailing(mailinglistIds, companyId).stream()
                .filter(mail -> !mail.isIsTemplate())
                .filter(mail -> !MailingStatus.SENT.equals(mailingDao.getStatus(companyId, mail.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public int getSentMailingsCount(int mailinglistId, int companyId) {
        return getAllDependedMailing(Collections.singleton(mailinglistId), companyId).stream()
                .filter(m -> MailingStatus.SENT.equals(mailingDao.getStatus(companyId, m.getId())))
                .mapToInt(i -> 1).sum();
    }

    @Override
    public int getAffectedReportsCount(int mailinglistId, int companyId) {
        return birtReportDao.getReportParamValues(companyId, MAILINGLISTS_KEY)
                .stream().filter(param ->
                        BirtReportSettingsUtils.convertStringToIntList((String) param.get("parameter_value"))
                                .contains(mailinglistId))
                .map(param -> param.get("report_id")).distinct().mapToInt(i -> 1).sum();
    }
    
    @Override
	public Map<Integer, Integer> getMailinglistWorldSubscribersStatistics(int companyId, int mailinglistID) {
    	return mailinglistDao.getMailinglistWorldSubscribersStatistics(companyId, mailinglistID);
    }

	@Override
	public int saveMailinglist(Mailinglist mailinglist) {
    	return mailinglistDao.saveMailinglist(mailinglist);
	}
}
