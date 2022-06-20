/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.messages.Message;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EqlValidatorServiceImpl implements EqlValidatorService {

    private static final Pattern GENDER_EQUATION_PATTERN = Pattern.compile("(?:cust\\.gender\\s*(?:=|<>|>|>=|<|<=)\\s*(-?\\d+))|(?:mod\\(cust\\.gender,\\s+(\\d+)\\))");
    private final transient Logger logger = LogManager.getLogger(EqlValidatorServiceImpl.class);

    private EqlFacade eqlFacade;

    @Override
    public Collection<Message> validateEql(ComAdmin admin, String eql) {
        String sql;
        try {
            sql = eqlFacade.convertEqlToSql(eql, admin.getCompanyID()).getSql();
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage(), e);
            return Collections.singleton(Message.of("error.target.definition"));
        }

        if (!validateGender(admin, sql)) {
            return Collections.singleton(Message.of("error.gender.invalid", getMaxGenderValue(admin)));
        }

        return Collections.emptyList();
    }

    private boolean validateGender(ComAdmin admin, String targetSql) {
        if (targetSql.contains("cust.gender")) {
            final int maxGenderValue = getMaxGenderValue(admin);

            Matcher matcher = GENDER_EQUATION_PATTERN.matcher(targetSql);
            while (matcher.find()) {
                int genderValue = NumberUtils.toInt(matcher.group(1));
                if (genderValue < 0 || genderValue > maxGenderValue) {
                    return false;
                }

                genderValue = NumberUtils.toInt(matcher.group(2));
                if (genderValue < 0 || genderValue > maxGenderValue) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getMaxGenderValue(ComAdmin admin) {
        if (admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)) {
            return ConfigService.MAX_GENDER_VALUE_EXTENDED;
        } else {
            return ConfigService.MAX_GENDER_VALUE_BASIC;
        }
    }

    @Required
    public void setEqlFacade(EqlFacade eqlFacade) {
        this.eqlFacade = eqlFacade;
    }
}
