/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.List;

import com.agnitas.beans.AgnTagAttributeDto;
import com.agnitas.beans.AgnTagTextAttributeDto;
import com.agnitas.beans.ComAdmin;
import com.agnitas.service.AgnTagAttributeResolver;
import com.agnitas.service.AgnTagAttributeResolverRegistry;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AgnTagAttributeResolverRegistry")
public class AgnTagAttributeResolverRegistryImpl implements AgnTagAttributeResolverRegistry {
    /** The logger. */
    private static final transient Logger logger = Logger.getLogger(AgnTagAttributeResolverRegistryImpl.class);

    private List<AgnTagAttributeResolver> resolvers;

    public AgnTagAttributeResolverRegistryImpl(@Autowired List<AgnTagAttributeResolver> resolvers) {
        this.resolvers = resolvers;
    }

    @Override
    public AgnTagAttributeDto resolve(ComAdmin admin, String tag, String attribute) {
        for (AgnTagAttributeResolver resolver : resolvers) {
            try {
                AgnTagAttributeDto dto = resolver.resolve(admin, tag, attribute);

                if (dto != null) {
                    return dto;
                }
            } catch (Exception e) {
                logger.error("Agn-tag attribute resolution failed: " + e.getMessage(), e);
            }
        }

        return new AgnTagTextAttributeDto(attribute);
    }
}
