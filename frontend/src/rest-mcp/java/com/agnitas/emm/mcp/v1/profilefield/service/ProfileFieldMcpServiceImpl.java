/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.profilefield.service;

import java.util.Comparator;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.mcp.v1.profilefield.dto.ProfileFieldResponse;
import com.agnitas.emm.mcp.v1.profilefield.mapper.ProfileFieldMcpMapper;
import org.springframework.stereotype.Service;

@Service
public class ProfileFieldMcpServiceImpl implements ProfileFieldMcpService {

    private final RecipientFieldService recipientFieldService;
    private final ProfileFieldMcpMapper profileFieldMcpMapper;

    public ProfileFieldMcpServiceImpl(RecipientFieldService recipientFieldService, ProfileFieldMcpMapper profileFieldMcpMapper) {
        this.recipientFieldService = recipientFieldService;
        this.profileFieldMcpMapper = profileFieldMcpMapper;
    }

    @Override
    public List<ProfileFieldResponse> list(Admin admin) {
        return recipientFieldService.getVisibleFields(admin)
                .values()
                .stream()
                .sorted(Comparator.comparing(RecipientFieldDescription::getColumnName, String.CASE_INSENSITIVE_ORDER))
                .map(field -> profileFieldMcpMapper.toResponse(field, admin.getAdminID()))
                .toList();
    }

}
