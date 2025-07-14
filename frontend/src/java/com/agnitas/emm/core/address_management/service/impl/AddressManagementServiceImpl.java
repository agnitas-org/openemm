/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.address_management.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Company;
import com.agnitas.emm.core.address_management.dto.AddressManagementDTOBase;
import com.agnitas.emm.core.address_management.dto.AddressManagementEntry;
import com.agnitas.emm.core.address_management.dto.BirtReportAddressManagementDTO;
import com.agnitas.emm.core.address_management.dto.ClientAddressManagementDTO;
import com.agnitas.emm.core.address_management.dto.ImportProfileAddressManagementDTO;
import com.agnitas.emm.core.address_management.dto.RecipientAddressManagementDTO;
import com.agnitas.emm.core.address_management.dto.UserAddressManagementDTO;
import com.agnitas.emm.core.address_management.enums.AddressManagementCategory;
import com.agnitas.emm.core.address_management.service.AddressManagementService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.ReportEntry;
import com.agnitas.emm.core.birtreport.service.BirtReportService;
import com.agnitas.emm.core.company.bean.CompanyEntry;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.ImportProfile;
import com.agnitas.beans.Recipient;
import org.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.service.ImportProfileService;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static com.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

@Service("AddressManagementService")
public class AddressManagementServiceImpl implements AddressManagementService {

    private static final Logger logger = LogManager.getLogger(AddressManagementServiceImpl.class);
    protected static final int ROOT_COMPANY_ID = 1;
    private static final List<AddressManagementCategory> CATEGORIES = List.of(AddressManagementCategory.values());
    private static final List<AddressManagementCategory> FORBIDDEN_CATEGORIES = List.of(
            AddressManagementCategory.AUTO_EXPORTS,
            AddressManagementCategory.AUTO_IMPORTS,
            AddressManagementCategory.REFERENCE_TABLES,
            AddressManagementCategory.VOUCHER_CODE_TABLES
    );

    private final RecipientService recipientService;
    private final BirtReportService birtReportService;
    private final CompanyService companyService;
    private final AdminService adminService;
    private final ImportProfileService importProfileService;
    protected final ExtendedConversionService conversionService;

    public AddressManagementServiceImpl(RecipientService recipientService, BirtReportService birtReportService, CompanyService companyService, AdminService adminService,
                                        ImportProfileService importProfileService, ExtendedConversionService conversionService) {
        this.recipientService = recipientService;
        this.birtReportService = birtReportService;
        this.companyService = companyService;
        this.adminService = adminService;
        this.importProfileService = importProfileService;
        this.conversionService = conversionService;
    }

    @Override
    public Map<AddressManagementCategory, List<? extends AddressManagementDTOBase>> findEntries(String email, int companyId) {
        return getAvailableCategories().stream()
                .collect(Collectors.toMap(Function.identity(), c -> findEntriesByCategory(c, email, companyId)));
    }

    protected List<? extends AddressManagementDTOBase> findEntriesByCategory(AddressManagementCategory category, String email, int companyId) {
        if (AddressManagementCategory.RECIPIENTS.equals(category)) {
            return findRecipients(email, companyId);
        }

        if (AddressManagementCategory.IMPORT_PROFILES.equals(category)) {
            return findImportProfiles(email, companyId);
        }

        if (AddressManagementCategory.USERS.equals(category)) {
            return findUsers(email, companyId);
        }

        if (AddressManagementCategory.REPORTS.equals(category)) {
            return findReports(email, companyId);
        }

        if (AddressManagementCategory.TECHNICAL_CONTACTS.equals(category)) {
            return findClients(email, companyId);
        }

        return Collections.emptyList();
    }

    private List<RecipientAddressManagementDTO> findRecipients(String email, int companyId) {
        List<Integer> companies = companyId == ROOT_COMPANY_ID
                ? companyService.getCompaniesIds()
                : List.of(companyId);

        List<Recipient> recipients = recipientService.findAllByEmailPart(email, companies);
        return conversionService.convert(recipients, Recipient.class, RecipientAddressManagementDTO.class);
    }

    private List<UserAddressManagementDTO> findUsers(String email, int companyId) {
        List<AdminEntry> admins = companyId == ROOT_COMPANY_ID
                ? adminService.findAllByEmailPart(email)
                : adminService.findAllByEmailPart(email, companyId);

        return conversionService.convert(admins, AdminEntry.class, UserAddressManagementDTO.class);
    }

    private List<BirtReportAddressManagementDTO> findReports(String email, int companyId) {
        List<ReportEntry> reports = companyId == ROOT_COMPANY_ID
                ? birtReportService.findAllByEmailPart(email)
                : birtReportService.findAllByEmailPart(email, companyId);

        return conversionService.convert(reports, ReportEntry.class, BirtReportAddressManagementDTO.class);
    }

    private List<ImportProfileAddressManagementDTO> findImportProfiles(String email, int companyId) {
        List<ImportProfile> profiles = companyId == ROOT_COMPANY_ID
                ? importProfileService.findAllByEmailPart(email)
                : importProfileService.findAllByEmailPart(email, companyId);

        return conversionService.convert(profiles, ImportProfile.class, ImportProfileAddressManagementDTO.class);
    }

    private List<ClientAddressManagementDTO> findClients(String email, int companyId) {
        List<CompanyEntry> companies = companyId == ROOT_COMPANY_ID
                ? companyService.findAllByEmailPart(email)
                : companyService.findAllByEmailPart(email, companyId);

        return conversionService.convert(companies, CompanyEntry.class, ClientAddressManagementDTO.class);
    }

    @Override
    public List<AddressManagementCategory> getAvailableCategories() {
        List<AddressManagementCategory> categories = new ArrayList<>(CATEGORIES);
        categories.removeAll(getForbiddenCategories());
        return categories;
    }

    protected List<AddressManagementCategory> getForbiddenCategories() {
        return FORBIDDEN_CATEGORIES;
    }

    @Override
    public SimpleServiceResult deleteAll(String emailPart, Admin admin) {
        List<AddressManagementEntry> entries = getEntriesByEmail(emailPart, admin.getCompanyID());
        return SimpleServiceResult.of(deleteEntries(emailPart, entries, admin));
    }

    @Override
    public void replaceEmails(String emailPart, String newEmail, Admin admin) {
        List<AddressManagementEntry> entries = getEntriesByEmail(emailPart, admin.getCompanyID());
        replaceEmails(emailPart, newEmail, entries, admin);
    }

    private List<AddressManagementEntry> getEntriesByEmail(String emailPart, int companyId) {
        List<AddressManagementEntry> entries = new ArrayList<>();
        for (AddressManagementCategory category : getAvailableCategories()) {
            entries.addAll(findEntriesByCategory(category, emailPart, companyId)
                    .stream()
                    .map(e -> new AddressManagementEntry(e.getId(), e.getCompanyId(), category))
                    .toList());
        }

        return entries;
    }

    @Override
    public ServiceResult<List<AddressManagementEntry>> deleteEntries(String emailPart, List<AddressManagementEntry> entries, Admin admin) {
        entries = filterAllowed(entries, admin.getCompanyID());
        return doBulkAction(
                entries,
                e -> removeEntry(emailPart, e, admin),
                "warning.bulkAction.delete",
                SELECTION_DELETED_MSG
        );
    }

    protected SimpleServiceResult removeEntry(String email, AddressManagementEntry entry, Admin admin) {
        if (AddressManagementCategory.RECIPIENTS.equals(entry.getCategory())) {
            return recipientService.delete(entry.getId(), entry.getCompanyId(), admin);
        }

        if (AddressManagementCategory.USERS.equals(entry.getCategory())) {
            return adminService.delete(entry.getId(), entry.getCompanyId());
        }

        return replaceEmail(email, "", entry);
    }

    @Override
    public ServiceResult<List<AddressManagementEntry>> replaceEmails(String emailPart, String newEmail, List<AddressManagementEntry> entries, Admin admin) {
        entries = filterAllowed(entries, admin.getCompanyID());

        return doBulkAction(
                entries,
                e -> replaceEmail(emailPart, newEmail, e),
                "",
                CHANGES_SAVED_MSG
        );
    }

    protected SimpleServiceResult replaceEmail(String oldEmail, String newEmail, AddressManagementEntry entry) {
        if (AddressManagementCategory.RECIPIENTS.equals(entry.getCategory())) {
            recipientService.updateEmail(newEmail, entry.getId(), entry.getCompanyId());
            return SimpleServiceResult.simpleSuccess();
        }

        if (AddressManagementCategory.USERS.equals(entry.getCategory())) {
            adminService.updateEmail(newEmail, entry.getId(), entry.getCompanyId());
            return SimpleServiceResult.simpleSuccess();
        }

        if (AddressManagementCategory.IMPORT_PROFILES.equals(entry.getCategory())) {
            return replaceEmailForImportProfile(oldEmail, newEmail, entry.getId());
        }

        if (AddressManagementCategory.REPORTS.equals(entry.getCategory())) {
            return replaceEmailForBirtReport(oldEmail, newEmail, entry.getId(), entry.getCompanyId());
        }

        if (AddressManagementCategory.TECHNICAL_CONTACTS.equals(entry.getCategory())) {
            return replaceTechnicalContactEmail(oldEmail, newEmail, entry.getId());
        }

        return SimpleServiceResult.simpleError(Message.of(ERROR_MSG));
    }

    private ServiceResult<List<AddressManagementEntry>> doBulkAction(List<AddressManagementEntry> entries, Function<AddressManagementEntry, SimpleServiceResult> function,
                                                                     String warningMessageCode, String successMessageCode) {

        final Map<AddressManagementEntry, SimpleServiceResult> results = entries.stream()
                .collect(Collectors.toMap(Function.identity(), function));

        List<AddressManagementEntry> successfulHandledEntries = results.entrySet().stream()
                .filter(e -> e.getValue().isSuccess())
                .map(Map.Entry::getKey)
                .toList();

        if (successfulHandledEntries.isEmpty()) {
            return ServiceResult.error(results.values().stream().flatMap(r -> r.getErrorMessages().stream()).toList());
        }

        if (successfulHandledEntries.size() != entries.size()) {
            return ServiceResult.warning(
                    successfulHandledEntries,
                    true,
                    Message.of(warningMessageCode, entries.size() - successfulHandledEntries.size())
            );
        }

        return ServiceResult.success(successfulHandledEntries, Message.of(successMessageCode));
    }

    private List<AddressManagementEntry> filterAllowed(Collection<AddressManagementEntry> entries, int companyId) {
        if (companyId == ROOT_COMPANY_ID) {
            return new ArrayList<>(entries);
        }

        return entries.stream()
                .filter(e -> e.getCompanyId() == companyId)
                .toList();
    }

    private SimpleServiceResult replaceTechnicalContactEmail(String oldEmail, String newEmail, int id) {
        Company company = companyService.getCompany(id);
        if (company == null) {
            return SimpleServiceResult.simpleError(Message.of(ERROR_MSG));
        }

        companyService.updateTechnicalContact(
                replaceEmail(company.getContactTech(), oldEmail, newEmail, " "),
                company.getId()
        );
        return SimpleServiceResult.simpleSuccess();
    }

    private SimpleServiceResult replaceEmailForImportProfile(String oldEmail, String newEmail, int id) {
        ImportProfile importProfile = importProfileService.getImportProfileById(id);
        if (importProfile == null) {
            return SimpleServiceResult.simpleError(Message.of(ERROR_MSG));
        }

        importProfileService.updateEmails(
                replaceEmail(importProfile.getMailForError(), oldEmail, newEmail, " "),
                replaceEmail(importProfile.getMailForReport(), oldEmail, newEmail, " "),
                id
        );
        return SimpleServiceResult.simpleSuccess();
    }

    private SimpleServiceResult replaceEmailForBirtReport(String oldEmail, String newEmail, int id, int companyId) {
        BirtReport report = birtReportService.getBirtReport(id, companyId);
        if (report == null) {
            return SimpleServiceResult.simpleError(Message.of(ERROR_MSG));
        }

        List<String> emails = replaceEmail(report.getEmailRecipientList(), oldEmail, newEmail);
        birtReportService.storeBirtReportEmailRecipients(emails, id);
        return SimpleServiceResult.simpleSuccess();
    }

    protected String replaceEmail(String joinedEmails, String oldEmail, String newEmail, CharSequence delimiter) {
        List<String> emails = AgnUtils.splitAndTrimList(joinedEmails);
        return String.join(delimiter, replaceEmail(emails, oldEmail, newEmail));
    }

    protected List<String> replaceEmail(Collection<String> emails, String oldEmail, String newEmail) {
        return emails.stream()
                .map(email -> StringUtils.containsIgnoreCase(email, oldEmail) ? newEmail : email)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

}
