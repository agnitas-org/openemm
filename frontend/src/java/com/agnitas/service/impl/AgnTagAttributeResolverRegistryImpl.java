/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AgnTagAttributeDto;
import com.agnitas.beans.AgnTagTextAttributeDto;
import com.agnitas.service.AgnTagAttributeResolver;
import com.agnitas.service.AgnTagAttributeResolverRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AgnTagAttributeResolverRegistry")
public class AgnTagAttributeResolverRegistryImpl implements AgnTagAttributeResolverRegistry {

    private List<AgnTagAttributeResolver> resolvers;

    public AgnTagAttributeResolverRegistryImpl(@Autowired List<AgnTagAttributeResolver> resolvers) {
        this.resolvers = resolvers;
    }

    @Override
    public AgnTagAttributeDto resolve(Admin admin, String tag, String attribute) {
        for (AgnTagAttributeResolver resolver : resolvers) {
            AgnTagAttributeDto dto = resolver.resolve(admin, tag, attribute);
            if (dto != null) {
                return dto;
            }
        }

        return new AgnTagTextAttributeDto(attribute);
    }
}
