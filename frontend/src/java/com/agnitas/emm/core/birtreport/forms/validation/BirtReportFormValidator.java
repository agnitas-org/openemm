/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.forms.validation;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.emm.core.birtreport.forms.BirtReportForm;
import com.agnitas.emm.core.birtreport.service.ComBirtReportService;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.web.mvc.Popups;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.ENABLED_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILINGLISTS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.TARGETS_KEY;
import static com.agnitas.emm.core.birtreport.service.impl.ComBirtReportServiceImpl.KEY_END_DATE;
import static com.agnitas.emm.core.birtreport.service.impl.ComBirtReportServiceImpl.KEY_START_DATE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.ACTION_AND_DATEBASED_MAILING_MAX_COUNT;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.END_DATE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.START_DATE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.getSettingsPropertyList;

public class BirtReportFormValidator {

	private final ConfigService configService;
	private final ComBirtReportService birtReportService;
	private final ExtendedConversionService conversionService;

	public BirtReportFormValidator(ConfigService configService, ComBirtReportService birtReportService,
			ExtendedConversionService conversionService) {
		this.configService = configService;
		this.birtReportService = birtReportService;
		this.conversionService = conversionService;
	}

	public boolean validateBeforeSave(BirtReportForm form, Admin admin, Popups popups) {
        if (!isValidCommonSettings(form, popups)) {
            return false;
		}
        boolean isValid = true;
        for (ReportSettingsType settingsType : ReportSettingsType.values()) {
            // It has to be ALWAYS possible to deactivate a report
            if (isDeliveryActivated(form, settingsType)) {
                isValid &= validateBirtReport(settingsType, form, popups, admin);
            }
        }
		return isValid;
	}

    public boolean isValidToEvaluate(BirtReportForm form, Admin admin, Popups popups) {
        ReportSettingsType activeType = ReportSettingsType.getTypeByCode(form.getActiveTab());
        return isValidCommonSettings(form, popups)
                && validateBirtReport(activeType, form, popups, admin);
    }

    private boolean isValidCommonSettings(BirtReportForm form, Popups popups) {
        validateShortname(form.getShortname(), popups);
        validateDescription(form.getDescription(), popups);
        validateEmailAddresses(form.getEmailAddresses(), popups);
        validateEmailSubject(form.getEmailSubject(), popups);
        validateEmailDescription(form.getEmailDescription(), popups);
        validateType(form, popups);
        validateSendDate(form, popups);
        return !popups.hasAlertPopups();
    }

    private boolean isDeliveryActivated(BirtReportForm form, ReportSettingsType settingsType) {
        final Map<String, Object> settingsByType = form.getSettingsByType(settingsType);
        return BooleanUtils.toBoolean((String) settingsByType.get(ENABLED_KEY));
    }

	private void validateShortname(String shortname, Popups popups) {
        String error = getShortnameError(shortname);
        if (StringUtils.isNotBlank(error)) {
            popups.field("shortname", error);
        }
    }

	private String getShortnameError(String shortname) {
		if (StringUtils.trimToNull(shortname) == null) {
			return "error.name.is.empty";
		}
		if (StringUtils.trimToEmpty(shortname).length() < 3) {
			return "error.name.too.short";
		}
		if (StringUtils.length(shortname) > 90) {
			return "error.name.too.long";
		}
		return null;
	}

	private void validateDescription(String description, Popups popups) {
		if (StringUtils.length(description) > 1000) {
			popups.field("description", "error.description.too.long");
		}
	}

    private void validateEmailAddresses(String emailAddresses, Popups popups) {
        if (StringUtils.isEmpty(emailAddresses) || !AgnUtils.isValidEmailAddresses(emailAddresses)) {
            popups.field("emailAddresses", "error.invalid.email");
        }
    }

    private void validateEmailSubject(String emailSubject, Popups popups) {
        if (StringUtils.length(emailSubject) < 3) {
            popups.field("emailSubject", "error.subjectToShort");
        }
    }

    private void validateEmailDescription(String emailDescription, Popups popups) {
        if (StringUtils.length(emailDescription) > 1000) {
            popups.field("emailDescription", "error.description.too.long");
        }
    }

	private void validateType(BirtReportForm form, Popups popups) {
		final BirtReportType type = BirtReportType.getTypeByCode(form.getType());
		if ((type == BirtReportType.TYPE_WEEKLY || type == BirtReportType.TYPE_BIWEEKLY) && !form.daysIsChecked()) {
			popups.field("type", "error.day.of.week");
		}
	}

	private void validateSendDate(BirtReportForm form, Popups popups) {
		if (BirtReportType.getTypeByCode(form.getType()) != BirtReportType.TYPE_AFTER_MAILING_24HOURS
                && form.getSendDate().get() == null) {
			popups.field("sendDate", "error.starttime.empty");
		}
	}

	private boolean validateBirtReport(ReportSettingsType settingsType, BirtReportForm form, final Popups popups, final Admin admin) {
		final Map<String, Object> settingsByType = form.getSettingsByType(settingsType);
		if (!validateTargetGroupSize(settingsType, settingsByType, popups, admin.getLocale())) {
			return false;
		}

		final Locale locale = admin.getLocale();

		if (!validateDateRangeType(settingsType, settingsByType)) {
			popups.alert("error.report.date.range.invalid", getSettingsTypeMsg(settingsType, locale));
			return false;
		}

		final Map<String, LocalDate> dateRange = birtReportService.getDatesRestrictionMap(admin, settingsType, BirtReportSettingsUtils
				.getReportDateFormatLocalized(admin), settingsByType);
		birtReportService.preloadMailingsByRestriction(admin, settingsType, settingsByType, dateRange);

		if (!validateMailingsCount(admin.getCompanyID(), settingsType, settingsByType, popups, locale)) {
			return false;
		}

		if (!validateMailingListsCount(admin, settingsType, settingsByType, popups)) {
			return false;
		}

		//set date restrictions to settings just
		if (BirtReportSettingsUtils.updateDateRestrictions(settingsType, settingsByType)) {
			settingsByType.put(START_DATE, dateRange.get(KEY_START_DATE));
			settingsByType.put(END_DATE, dateRange.get(KEY_END_DATE));
		}

		// Check whether all the required parameters are there
		final Set<String> missingParameters = BirtReportSettingsUtils.getMissingProperties(admin, settingsType, settingsByType);
		if (!missingParameters.isEmpty()) {
			final String typeMsg = getSettingsTypeMsg(settingsType, locale);
			final String parameter = missingParameters.iterator().next();
			if (parameter.equals(MAILINGS_KEY)) {
				settingsByType.put(MAILINGS_KEY, Collections.emptyList());
				popups.alert("error.report.evaluate.sending.missing", getSettingsTypeMsg(settingsType, locale));
			} else {
				String warningMsg = String.format(I18nString.getLocaleString("error.report.evaluate.parameter.missing", locale, settingsType.getTypeMsgKey()),
						BirtReportSettingsUtils.getParameterTranslation(parameter, locale));
				popups.exactWarning(String.format("%s: %s", typeMsg, warningMsg));
			}
			return false;
		}

		return true;
	}

	private boolean validateDateRangeType(ReportSettingsType type, Map<String, Object> settings) {
		switch (type) {
		case COMPARISON:
			return BirtReportSettingsUtils.validateComparisonDateRange(settings);
		case MAILING:
			return BirtReportSettingsUtils.validateMailingsDateRange(settings);
		case RECIPIENT:
		case TOP_DOMAIN:
			return BirtReportSettingsUtils.validateDateRangedSettings(settings);
		default:
			//nothing do
		}
		return true;
	}

	private boolean validateTargetGroupSize(ReportSettingsType type, Map<String, Object> settingsByType, Popups popups, Locale locale) {
		List<String> targetGroups = getSettingsPropertyList(settingsByType, TARGETS_KEY);

		if (CollectionUtils.isEmpty(targetGroups)) {
			return true;
		}

		int maxSize = BirtReportSettingsUtils.getMaxTargetGroupNumber(type);

		Set<Integer> values = new HashSet<>(conversionService.convert(targetGroups, String.class, Integer.class));
		int actualSize = CollectionUtils.size(values);

		if (actualSize > maxSize) {
			popups.alert("error.report.target.max", getSettingsTypeMsg(type, locale), maxSize);
			return false;
		}
		return true;
	}

	private boolean validateMailingsCount(final int companyId, ReportSettingsType activeSettingsType, final Map<String, Object> settingsByType, final Popups popups, Locale locale) {
		final List<String> mailings = BirtReportSettingsUtils.getSettingsPropertyList(settingsByType, MAILINGS_KEY);

        if ((BirtReportSettingsUtils.isMailingActionBased(activeSettingsType, settingsByType) ||
                BirtReportSettingsUtils.isMailingDateBased(activeSettingsType, settingsByType) ||
				BirtReportSettingsUtils.isMailingFollowUp(activeSettingsType, settingsByType) ||
				BirtReportSettingsUtils.isMailingIntervalBased(activeSettingsType, settingsByType))
                && mailings.size() > ACTION_AND_DATEBASED_MAILING_MAX_COUNT) {
            popups.alert("error.report.mailing.action.max", getSettingsTypeMsg(activeSettingsType, locale), ACTION_AND_DATEBASED_MAILING_MAX_COUNT, mailings.size());
            return false;
        }

		final int maxMailings = configService.getIntegerValue(ConfigValue.MaximumMailingsPerReport, companyId);
		if (mailings.size() > maxMailings) {
			popups.alert("error.report.mailing.compare.max", getSettingsTypeMsg(activeSettingsType, locale), mailings.size(), maxMailings);
			return false;
		}
		return true;
	}

    private boolean validateMailingListsCount(final Admin admin, ReportSettingsType settingsType, final Map<String, Object> settingsByType, final Popups popups) {
		final List<String> mailingLists = BirtReportSettingsUtils.getSettingsPropertyList(settingsByType, MAILINGLISTS_KEY);
		final int maxMailingLists = configService.getIntegerValue(ConfigValue.MaximumMailinglistsPerReport, admin.getCompanyID());
		if (mailingLists.size() > maxMailingLists) {
			popups.alert("error.report.mailinglist.max", getSettingsTypeMsg(settingsType, admin.getLocale()), mailingLists.size(), maxMailingLists);
			return false;
		}
		return true;
	}

    private String getSettingsTypeMsg(ReportSettingsType activeSettingsType, Locale locale) {
        return I18nString.getLocaleString(activeSettingsType.getTypeMsgKey(), locale);
    }
}
