/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.validation;

import com.agnitas.beans.Admin;
import com.agnitas.mailing.autooptimization.form.OptimizationForm;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.web.mvc.Popups;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OptimizationFormValidator {

    private static final Logger logger = LogManager.getLogger(OptimizationFormValidator.class);

    private final int MIN_SELECTED_GROUPS_COUNT = 2;
    private final Pattern NAME_PATTERN = Pattern.compile("[0-9A-Za-z]{3,}");
    private final Pattern THRESHOLD_PATTERN = Pattern.compile("[0-9]*");

    public boolean validateName(OptimizationForm form, Popups popups) {
        Matcher matcher = NAME_PATTERN.matcher(form.getOptimization().getShortname());

        if (!matcher.find()) {
            popups.alert("mailing.autooptimization.errors.noshortname");
            return false;
        }

        return true;
    }

    public boolean validateGroups(OptimizationForm form, Popups popups) {
        boolean isValid = true;

        ComOptimization optimization = form.getOptimization();
        int splitSize = form.getSplitSize();
        int[] groups = {optimization.getGroup1(), optimization.getGroup2(), optimization.getGroup3(),
                optimization.getGroup4(), optimization.getGroup5()};

        if (existsUnsetGroups(groups, splitSize)) {
            popups.alert("mailing.autooptimization.errors.unsetgroup");
            isValid = false;
        }

        if (calculateSelectedGroups(groups, splitSize) < MIN_SELECTED_GROUPS_COUNT) {
            popups.alert("mailing.autooptimization.errors.numberofgroups");
            isValid = false;
        }

        if (!containsOnlyUniqueGroups(groups, splitSize)) {
            popups.alert("mailing.autooptimization.errors.groupsareidentically");
            isValid = false;
        }

        return isValid;
    }

    public boolean validateThreshold(OptimizationForm form, Popups popups) {
        String threshold = StringUtils.defaultString(form.getThresholdString());
        Matcher matcher = THRESHOLD_PATTERN.matcher(threshold);

        if (!matcher.matches()) {
            popups.alert("mailing.autooptimization.errors.threshold");
            return false;
        }

        return true;
    }

    public boolean validateSchedule(Admin admin, OptimizationForm form, Popups popups) {
        String testMailingsSendDateAsString = form.getTestMailingsSendDateAsString();
        String resultSendDateAsString = form.getResultSendDateAsString();

        boolean dateValidationResult = validateTestMailingsSendDate(testMailingsSendDateAsString, popups, admin.getDateTimeFormat().toPattern());
        dateValidationResult &= validateResultSendDate(resultSendDateAsString, popups, admin.getDateTimeFormat().toPattern());

        if (!dateValidationResult) {
            return false;
        }

        Date testmailingsSendDate = null;
        Date resultSendDate = null;
        try {
            testmailingsSendDate = admin.getDateTimeFormat().parse(testMailingsSendDateAsString);
            resultSendDate = admin.getDateTimeFormat().parse(resultSendDateAsString);
        } catch (ParseException e) {
            logger.error("Error occurred: " + e.getMessage(), e);
        }

        Date now = new Date();

        if (resultSendDate == null) {
            throw new RuntimeException("resultSendDate was null");
        }

        if (!resultSendDate.after(testmailingsSendDate)) {
            popups.alert("mailing.autooptimization.errors.result_is_not_after_test");
            dateValidationResult = false;
        }

        if (now.after(resultSendDate)) {
            popups.alert("mailing.autooptimization.errors.resultsenddate_is_not_in_future");
            dateValidationResult = false;
        }

        if (now.after(testmailingsSendDate)) {
            popups.alert("mailing.autooptimization.errors.testmailingssenddate_is_not_infuture");
            dateValidationResult = false;
        }

        return dateValidationResult;
    }

    public boolean validateTestMailingsSendDate(String sendDateAsString, Popups popups, String datePattern) {
        return validateDate(sendDateAsString, datePattern, popups,
                "mailing.autooptimization.errors.testmailingssenddate.empty",
                "mailing.autooptimization.errors.testmailingssenddate");
    }

    public boolean validateResultSendDate(String sendDateAsString, Popups popups, String datePattern) {
        return validateDate(sendDateAsString, datePattern, popups,
                "mailing.autooptimization.errors.resultsenddate.empty",
                "mailing.autooptimization.errors.resultsenddate");
    }

    private boolean validateDate(String sendDateAsString, String datePattern, Popups popups, String errorCodeForEmpty, String errorCodeForInvalid) {
        if (StringUtils.isBlank(sendDateAsString)) {
            popups.alert(errorCodeForEmpty, datePattern);
            return false;
        }

        if (!AgnUtils.isDateValid(sendDateAsString, datePattern)) {
            popups.alert(errorCodeForInvalid, datePattern);
            return false;
        }

        return true;
    }

    private boolean existsUnsetGroups(int[] groups, int splitSize) {
        return Arrays.stream(groups)
                .limit(splitSize)
                .anyMatch(g -> g <= 0);
    }

    private long calculateSelectedGroups(int[] groups, int splitSize) {
        return Arrays.stream(groups)
                .limit(splitSize)
                .filter(g -> g > 0)
                .count();
    }

    private boolean containsOnlyUniqueGroups(int[] groups, int splitSize) {
        Set<Integer> groupsSet = new HashSet<>();

        return Arrays.stream(groups)
                .limit(splitSize)
                .allMatch(groupsSet::add);
    }
}
