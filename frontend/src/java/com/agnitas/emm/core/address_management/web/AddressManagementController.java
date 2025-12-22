/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.address_management.web;

import static com.agnitas.util.Const.Mvc.ERROR_MSG;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.address_management.dto.AddressManagementEntry;
import com.agnitas.emm.core.address_management.form.AddressManagementSearchForm;
import com.agnitas.emm.core.address_management.service.AddressManagementService;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.exception.BadRequestException;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredPermission("admin.management.show")
public class AddressManagementController {

    private final AddressManagementService addressManagementService;
    private final CompanyService companyService;

    public AddressManagementController(AddressManagementService addressManagementService, CompanyService companyService) {
        this.addressManagementService = addressManagementService;
        this.companyService = companyService;
    }

    @GetMapping("/address-management.action")
    public String view(@ModelAttribute("searchForm") AddressManagementSearchForm searchForm, Admin admin, Model model) {
        model.addAttribute("categories", addressManagementService.getAvailableCategories());
        if (StringUtils.isNotBlank(searchForm.getEmail())) {
            model.addAttribute("entries", addressManagementService.findEntries(searchForm.getEmail(), admin.getCompanyID()));
            model.addAttribute("availableClients", companyService.getCreatedCompanies(admin.getCompanyID()));
        }
        return "address_management_view";
    }

    @PostMapping("/address-management/entries/delete.action")
    public ResponseEntity<DataResponseDto<List<AddressManagementEntry>>> deleteEntries(@RequestBody List<AddressManagementEntry> entries,
                                                                                     @RequestParam String email, Admin admin, Popups popups) {
        if (CollectionUtils.isEmpty(entries)) {
            popups.defaultError();
            return ResponseEntity.ok(new DataResponseDto<>(popups, false));
        }

        ServiceResult<List<AddressManagementEntry>> result = addressManagementService.deleteEntries(email, entries, admin);
        popups.addPopups(result);

        return ResponseEntity.ok(new DataResponseDto<>(result.getResult(), popups, result.isSuccess()));
    }

    @PostMapping("/address-management/entries/deleteAll.action")
    public String deleteAll(AddressManagementSearchForm searchForm, Admin admin, Popups popups, RedirectAttributes ra) {
        checkEmail(searchForm);

        SimpleServiceResult result = addressManagementService.deleteAll(searchForm.getEmail(), admin);
        popups.addPopups(result);

        ra.addFlashAttribute("searchForm", searchForm);
        return "redirect:/address-management.action";
    }

    @PostMapping("/address-management/entries/replace.action")
    public ResponseEntity<DataResponseDto<List<AddressManagementEntry>>> replaceEmail(@RequestBody List<AddressManagementEntry> entries, @RequestParam String email, @RequestParam String oldEmail,
                                                           Admin admin, Popups popups) {
        if (CollectionUtils.isEmpty(entries)) {
            popups.defaultError();
            return ResponseEntity.ok(new DataResponseDto<>(popups, false));
        }

        if (!AgnUtils.isEmailValid(email)) {
            popups.alert("error.email.invalid");
            return ResponseEntity.ok(new DataResponseDto<>(popups, false));
        }

        ServiceResult<List<AddressManagementEntry>> result = addressManagementService.replaceEmails(oldEmail, email, entries, admin);
        popups.addPopups(result);

        return ResponseEntity.ok(new DataResponseDto<>(result.getResult(), popups, result.isSuccess()));
    }

    @PostMapping("/address-management/entries/replaceAll.action")
    public String replaceAll(AddressManagementSearchForm searchForm, @RequestParam String newEmail, Popups popups, Admin admin, RedirectAttributes ra) {
        checkEmail(searchForm);

        addressManagementService.replaceEmails(searchForm.getEmail(), newEmail, admin);
        popups.changesSaved();

        ra.addFlashAttribute("searchForm", searchForm);
        return "redirect:/address-management.action";
    }

    private static void checkEmail(AddressManagementSearchForm searchForm) {
        if (StringUtils.isBlank(searchForm.getEmail())) {
            throw new BadRequestException(ERROR_MSG);
        }
    }

}
