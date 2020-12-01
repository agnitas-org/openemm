/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.DynamicTagDao;
import com.agnitas.service.AgnDynTagGroupResolver;
import com.agnitas.service.AgnDynTagGroupResolverFactory;

public class AgnDynTagGroupResolverFactoryImpl implements AgnDynTagGroupResolverFactory {
    private DynamicTagDao dynamicTagDao;

    @Override
    public AgnDynTagGroupResolver create(int companyID, int mailingID) {
        return new AgnDynTagGroupResolverImpl(companyID, mailingID);
    }

    @Required
    public void setDynamicTagDao(DynamicTagDao dynamicTagDao) {
        this.dynamicTagDao = dynamicTagDao;
    }

    private class AgnDynTagGroupResolverImpl implements AgnDynTagGroupResolver {
        private Map<String, Integer> cache = new HashMap<>();
        private int companyID;
        private int mailingID;

        public AgnDynTagGroupResolverImpl(int companyID, int mailingID) {
            this.mailingID = mailingID;
            this.companyID = companyID;
        }

        @Override
        public int resolve(String name) {
            if (companyID <= 0 || mailingID <= 0 || StringUtils.isEmpty(name)) {
                return 0;
            } else {
            	return cache.computeIfAbsent(name, k -> dynamicTagDao.getId(companyID, mailingID, k));
            }
        }
    }
}
