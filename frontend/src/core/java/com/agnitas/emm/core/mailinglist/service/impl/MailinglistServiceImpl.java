/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.service.impl;

import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.MAILINGLISTS_KEY;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.Target;
import com.agnitas.beans.impl.MailinglistImpl;
import com.agnitas.dao.BindingEntryDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.exceptions.ShortnameTooShortException;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.birtreport.dao.BirtReportDao;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.emm.core.mailinglist.dao.MailinglistApprovalDao;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;
import com.agnitas.emm.core.mailinglist.exception.MailinglistNotExistException;
import com.agnitas.emm.core.mailinglist.mapper.MailinglistMapper;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.objectusage.common.ObjectUsage;
import com.agnitas.emm.core.objectusage.common.ObjectUsageType;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.exception.BadRequestException;
import com.agnitas.exception.ForbiddenException;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.web.forms.PaginationForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("mailinglistService")
public class MailinglistServiceImpl implements MailinglistService {

    protected final MailinglistDao mailinglistDao;
    private final MailinglistApprovalDao mailinglistApprovalDao;
    private final MailingDao mailingDao;
    private final BindingEntryDao bindingEntryDao;
    private final BirtReportDao birtReportDao;
    private final RecipientDao recipientDao;
    private final TargetDao targetDao;
    private final BulkActionValidationService<Integer, Mailinglist> bulkActionValidationService;
    private final MailinglistMapper mailinglistMapper;

    public MailinglistServiceImpl(MailinglistDao mailinglistDao,
                                  MailinglistApprovalDao mailinglistApprovalDao,
                                  MailingDao mailingDao,
                                  BindingEntryDao bindingEntryDao,
                                  BirtReportDao birtReportDao,
                                  RecipientDao recipientDao,
                                  TargetDao targetDao,
                                  BulkActionValidationService<Integer, Mailinglist> bulkActionValidationService,
                                  MailinglistMapper mailinglistMapper) {
        this.mailinglistDao = mailinglistDao;
        this.mailinglistApprovalDao = mailinglistApprovalDao;
        this.mailingDao = mailingDao;
        this.bindingEntryDao = bindingEntryDao;
        this.birtReportDao = birtReportDao;
        this.recipientDao = recipientDao;
        this.targetDao = targetDao;
        this.bulkActionValidationService = bulkActionValidationService;
        this.mailinglistMapper = mailinglistMapper;
    }

    protected void bulkDelete(Collection<Integer> mailinglistIds, int companyId) {
        if(mailinglistIds != null) {
            for (int mailinglistId : mailinglistIds) {
                mailinglistDao.deleteMailinglist(mailinglistId, companyId);
            }
        }
    }

    private List<Mailing> getAllDependedMailing(Set<Integer> mailinglistIds, int companyId) {
        return mailinglistIds
                .stream()
                .flatMap(id->mailingDao.getMailingsForMLID(companyId,id).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Mailinglist> getMailinglists(int companyId) {
        return mailinglistDao.getMailinglists(companyId);
    }

    @Override
    public PaginatedList<Mailinglist> findAll(PaginationForm form, int companyId) {
        return mailinglistDao.findAll(form, companyId);
    }

    @Override
    public Mailinglist getMailinglist(int id, int companyId){
        return Optional.ofNullable(getOrNull(id, companyId))
                .orElseThrow(() -> new MailinglistNotExistException(id, companyId));
    }

    @Override
    public Mailinglist getOrNull(int id, int companyId) {
        return mailinglistDao.getMailinglist(id, companyId);
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
                .toList();

        bulkDelete(allowedIds, admin.getCompanyID());

        return allowedIds;
    }
    
    private ServiceResult<Mailinglist> getMailinglistForDeletion(int id, Admin admin) {
        Mailinglist mailinglist = getOrNull(id, admin.getCompanyID());
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
            throw new ForbiddenException("error.mailinglist.delete.last");
        }
    }

    private ObjectUsages collectObjectUsages(int id, int companyId) {
        List<Mailing> affectedMailings = getUsedMailings(Set.of(id), companyId);

        List<ObjectUsage> usages = affectedMailings.stream()
                .map(m -> new ObjectUsage(ObjectUsageType.MAILING, m.getId(), m.getShortname()))
                .toList();

        return new ObjectUsages(usages);
    }

    @Override
    public List<Mailinglist> getAllMailingListsNames(int companyId) {
        return mailinglistDao.getMailingListsNames(companyId);
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
	public int saveMailinglist(int companyId, MailinglistDto dto) {
        validateBeforeSave(dto, companyId);

		MailinglistImpl mailinglist = mailinglistMapper.toEntity(dto);
        mailinglist.setCompanyID(companyId);

		if (dto.getId() > 0) {
            return mailinglistDao.updateMailinglist(companyId, mailinglist);
        }

        int mailinglistId = mailinglistDao.createMailinglist(companyId, mailinglist);
        Target target = targetDao.getTarget(dto.getTargetId(), companyId);
        // Add recipients from a target group (if specified)
        if (target != null) {
            bindingEntryDao.addTargetsToMailinglist(companyId, mailinglistId, target, dto.getMediatypes());
        }
		return mailinglistId;
	}

    private void validateBeforeSave(MailinglistDto mailinglist, int companyId) {
        if (mailinglist.getShortname() == null || mailinglist.getShortname().length() < 3) {
            throw new ShortnameTooShortException(mailinglist.getShortname());
        }

        Map<String, Message> validationErrors = new HashMap<>();

        if (!isShortnameUnique(mailinglist.getShortname(), mailinglist.getId(), companyId)) {
            validationErrors.put("shortname", Message.of("error.mailinglist.duplicate", mailinglist.getShortname()  ));
        }

        if (StringUtils.isNotBlank(mailinglist.getSenderEmail()) && !AgnUtils.isEmailValid(mailinglist.getSenderEmail())) {
            validationErrors.put("senderEmail", Message.of("error.invalid.email"));
        }

        if (StringUtils.isNotBlank(mailinglist.getReplyEmail()) && !AgnUtils.isEmailValid(mailinglist.getReplyEmail())) {
            validationErrors.put("replyEmail", Message.of("error.invalid.email"));
        }

        if (!validationErrors.isEmpty()) {
            throw new BadRequestException(validationErrors);
        }
    }
	
	@Override
	public boolean isShortnameUnique(String newShortname, int mailinglistId, int companyId) {
		Mailinglist mailinglist = mailinglistDao.getMailinglist(mailinglistId, companyId);
		String oldShortname = mailinglist != null ? mailinglist.getShortname() : "";
		
        return Strings.CS.equals(oldShortname, newShortname)
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

            entry.put("id", mailinglist.getId());
            entry.put("shortname", mailinglist.getShortname());
            entry.put("description", mailinglist.getDescription());
            entry.put("changeDate", DateUtilities.toLong(mailinglist.getChangeDate()));
            entry.put("creationDate", DateUtilities.toLong(mailinglist.getCreationDate()));
            entry.put("isFrequencyCounterEnabled", mailinglist.isFrequencyCounterEnabled());
            entry.put("restrictedForSomeAdmins", mailinglist.isRestrictedForSomeAdmins());

            mailingListsJson.put(entry);
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

    @Override
    public List<Integer> getMailinglistIds(int companyId) {
        return mailinglistDao.getMailinglistIds(companyId);
    }

    @Override
    public String[] getApprovalEmails(int mailinglistId) {
        return mailinglistDao.getApprovalEmails(mailinglistId);
    }

    @Override
    public int getMailinglistIdByName(String name, int companyId) {
        return mailinglistDao.getMailinglistIdByName(name, companyId);
    }
}
