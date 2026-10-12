/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.blocklist;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blocklist")
public class BlocklistV2Controller {

    private final BlocklistRestService blocklistRestService;

    public BlocklistV2Controller(BlocklistRestService blocklistRestService) {
        this.blocklistRestService = blocklistRestService;
    }

    @GetMapping
    @PermissionAllowed("blacklist")
    public PageResponse<BlocklistEntryResponse> list(Pageable pageable, Admin admin) {
        return blocklistRestService.find(pageable, admin.getCompanyID());
    }

    @GetMapping("/{email}")
    @PermissionAllowed("blacklist")
    public BlocklistEntryResponse getByEmail(@PathVariable @NotBlank String email, Admin admin) {
        return blocklistRestService.find(email, admin.getCompanyID());
    }

    @PostMapping
    @PermissionAllowed("blacklist")
    @ResponseStatus(HttpStatus.CREATED)
    public BlocklistEntryResponse addEntry(@RequestBody @Valid CreateBlocklistEntryRequest request, Admin admin) {
        return blocklistRestService.addEntry(request, admin);
    }

    @PatchMapping("/{email}")
    @PermissionAllowed("blacklist")
    public BlocklistEntryResponse updateEntry(
            @PathVariable @NotBlank String email,
            @RequestBody @Valid PatchBlocklistEntryRequest request,
            Admin admin
    ) {
        return blocklistRestService.updateEntry(email, request, admin);
    }

    @DeleteMapping("/{email}")
    @PermissionAllowed("recipient.delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @NotBlank String email, Admin admin) {
        blocklistRestService.delete(email, admin.getCompanyID());
    }
}
