/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.MailinglistImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtreport.bean.ComLightweightBirtReport;
import com.agnitas.emm.core.birtreport.dao.ComBirtReportDao;
import com.agnitas.emm.core.mailinglist.bean.MailinglistEntry;
import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.service.ExtendedConversionService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class ComMailinglistServiceImpl implements ComMailinglistService {

    private MailinglistDao mailinglistDao;
    
    private ComMailingDao mailingDao;
    
    private ComBindingEntryDao bindingEntryDao;
    
    private ComBirtReportDao birtReportDao;
    
    private ComRecipientDao recipientDao;
    
    private ExtendedConversionService conversionService;
    
    private AdminService adminService;
    
    private ComTargetDao targetDao;
    
    @Required
    public void setBindingEntryDao(ComBindingEntryDao bindingEntryDao) {
        this.bindingEntryDao = bindingEntryDao;
    }

    @Required
    public void setMailinglistDao(MailinglistDao mailinglistDao) {
        this.mailinglistDao = mailinglistDao;
    }

    @Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setBirtReportDao(ComBirtReportDao birtReportDao) {
        this.birtReportDao = birtReportDao;
    }
	
    @Required
	public void setConversionService(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}
 
	@Required
    public void setRecipientDao(ComRecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }
    
    @Required
    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    @Required
    public void setTargetDao(ComTargetDao targetDao) {
        this.targetDao = targetDao;
    }
    
    @Override
    public void bulkDelete(Set<Integer> mailinglistIds, @VelocityCheck int companyId) {
        if(mailinglistIds != null) {
            for (int mailinglistId : mailinglistIds) {
                mailinglistDao.deleteMailinglist(mailinglistId, companyId);
            }
        }
    }

    @Override
    public List<Mailing> getAllDependedMailing(Set<Integer> mailinglistIds, @VelocityCheck int companyId) {
        return mailinglistIds
                .stream()
                .flatMap(id->mailingDao.getMailingsForMLID(companyId,id).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRecipientBindings(Set<Integer> mailinglistIds, @VelocityCheck int companyId){
        bindingEntryDao.deleteRecipientBindingsByMailinglistID(mailinglistIds, companyId);
    }

    @Override
    public List<Mailinglist> getMailinglists(@VelocityCheck int companyId) {
        return mailinglistDao.getMailinglists(companyId);
    }

    @Override
    public Mailinglist getMailinglist(int mailinglistId, @VelocityCheck int companyId){
        return mailinglistDao.getMailinglist(mailinglistId, companyId);
    }

    @Override
    public boolean exist(int mailinglistId, @VelocityCheck int companyId){
        return mailinglistDao.exist(mailinglistId, companyId);
    }

    @Override
    public String getMailinglistName(int mailinglistId, @VelocityCheck int companyId) {
        return mailinglistDao.getMailinglistName(mailinglistId, companyId);
    }

    @Override
    public List<Mailinglist> getAllMailingListsNames(@VelocityCheck int companyId) {
        return mailinglistDao.getMailingListsNames(companyId);
    }

    @Override
    public List<ComLightweightBirtReport> getConnectedBirtReportList(int mailinglistId, @VelocityCheck int companyId) {
        List<Map<String, Object>> reportParams = birtReportDao.getReportParamValues(companyId, "selectedMailinglists");
        List<ComLightweightBirtReport> allReportslist = birtReportDao.getLightweightBirtReportList(companyId);
        // Put reports to Map to avoid several iterations on List of all reports;
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

    @Override
    public List<LightweightMailing> getMailingListByType(MailingTypes type, @VelocityCheck int companyId) {
        List<LightweightMailing> mailingLists = null;
        if(type != null) {
            mailingLists = mailingDao.listMailingsByType(type.getCode(), companyId);
        }
        
        if (mailingLists == null) {
            mailingLists = Collections.emptyList();
        }

        return mailingLists;
    }
	
	@Override
	public PaginatedListImpl<MailinglistDto> getMailinglistPaginatedList(ComAdmin admin, String sort, String sortDirection, int page, int rownumber) {
		PaginatedListImpl<MailinglistEntry> mailinglists = mailinglistDao.getMailinglists(admin.getCompanyID(), admin.getAdminID(), sort, sortDirection, page, rownumber);
		
		return conversionService.convertPaginatedList(mailinglists, MailinglistEntry.class, MailinglistDto.class);
	}
	
	@Override
	public int saveMailinglist(@VelocityCheck int companyId, MailinglistDto mailinglist) {
		MailinglistImpl mailinglistForSave = new MailinglistImpl();
		mailinglistForSave.setId(mailinglist.getId());
		mailinglistForSave.setCompanyID(companyId);
		mailinglistForSave.setShortname(mailinglist.getShortname());
		mailinglistForSave.setDescription(mailinglist.getDescription());
		int mailinglistId;
		if(mailinglist.getId() == 0) {
			mailinglistId = mailinglistDao.createMailinglist(companyId, mailinglistForSave);
			int targetId = mailinglist.getTargetId();
            ComTarget target = targetDao.getTarget(targetId, companyId);
            
            // Add recipients from a target group (if specified)
            if (target != null) {
                bindingEntryDao.addTargetsToMailinglist(companyId, mailinglistId, target);
            }
		} else {
			mailinglistId = mailinglistDao.updateMailinglist(companyId, mailinglistForSave);
		}
		return mailinglistId;
	}
	
	@Override
	public boolean isShortnameUnique(String newShortname, int mailinglistId, @VelocityCheck int companyId) {
		Mailinglist mailinglist = mailinglistDao.getMailinglist(mailinglistId, companyId);
		String oldShortname = mailinglist != null ? mailinglist.getShortname() : "";
		
        return StringUtils.equals(oldShortname, newShortname)
                || !mailinglistDao.mailinglistExists(newShortname, companyId);
    }
    
    @Override
    public boolean deleteMailinglist(int mailinglistId, @VelocityCheck int companyId) {
        if(mailinglistId == 0 || mailinglistDao.checkMailinglistInUse(mailinglistId, companyId)) {
            return false;
        }
        
        return mailinglistDao.deleteMailinglist(mailinglistId, companyId);
    }
    
    @Override
    public void deleteMailinglistBindingRecipients(int companyId, int mailinglistId, boolean onlyActiveUsers, boolean noAdminAndTestUsers) {
        String tmpTable = recipientDao.createTmpTableByMailinglistID(companyId, mailinglistId);
        recipientDao.deleteRecipientsBindings(mailinglistId, companyId, onlyActiveUsers, noAdminAndTestUsers);
        recipientDao.deleteAllNoBindings(companyId, tmpTable);
    }

    @Override
    public JSONArray getMailingListsJson(ComAdmin admin) {
        JSONArray mailingListsJson = new JSONArray();

        for (Mailinglist mailinglist : mailinglistDao.getMailinglists(admin.getCompanyID())) {
            JSONObject entry = new JSONObject();

            entry.element("id", mailinglist.getId());
            entry.element("shortname", mailinglist.getShortname());
            entry.element("description", mailinglist.getDescription());
            entry.element("changeDate", DateUtilities.toLong(mailinglist.getChangeDate()));
            entry.element("creationDate", DateUtilities.toLong(mailinglist.getCreationDate()));

            mailingListsJson.element(entry);
        }

        return mailingListsJson;
    }
 }
