/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;

public class BirtReporUtils {

	private static final String FIGURE_SEPARATOR = ",";

	public enum BirtReportFigure {
		OPENERS_MEASURED("openersMeasured"),
		OPENERS_INVISIBLE("openersInvisible"),
		OPENERS_TOTAL("openersTotal"),
		OPENINGS_ANONYMOUS("openingsAnonymous"),
		CLICKERS_TOTAL("clickingRecipients"),
		CLICKS_ANONYMOUS("clickingAnonymous"),
		OPENERS_PC("openersPcRate"),
		OPENERS_MOBILE("openersMobile"),
		OPENERS_MULTIPLE("openersPcAndMobile"),
		OPENERS_TABLET("openersTablet"),
        OPENERS_SMARTTV("openersSmartTV"),
		CLICKERS_PC("clickingRecipientsPcRate"),
		CLICKERS_MOBILE("clickingRecipientsMobile"),
		CLICKERS_MULTIPLE("clickingRecipientsPcAndMobile"),
        CLICKERS_TABLET("clickingTablet"),
        CLICKERS_SMARTTV("clickingRecipientsSmartTV"),
		SIGNED_OFF("signedOff"),
		HARDBOUNCES("hardbounces"),
		SOFTBOUNCES("softbounces"),
		REVENUE("conversionRate"),
		HTML("html"),
		TEXT("text"),
		OFFLINE_HTML("offlineHtml"),
        ACTIVATE_LINK_STATISTICS("activateLinkStatistics"),
        RECIPIENT_STATUS("recipientStatus"),
        RECIPIENT_DEVELOPMENT_DETAILED("recipientDevelopmentDetailed"),
        RECIPIENT_DEVELOPMENT_NET("recipientDevelopmentNet"),
        ACTIVITY_ANALYSIS("activityAnalysis"),
        OPENERS_AFTER_DEVICE("openersAfterDevice"),
		CLICKERS_AFTER_DEVICE("clickersAfterDevice"),
		OPENERS_DEVICES("openerDevices"),
		CLICKER_DEVICES("clickerDevices"),
        MAILING_TYPE("mailingType"),
		SENT_MAILS("sentMails"),
        OPENERS("openers");

		private final String value;

		private BirtReportFigure(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static BirtReportFigure getFigureForValue(String value) {
			for (BirtReportFigure reportFigure : BirtReportFigure.values()) {
				if (reportFigure.getValue().equals(value)) {
					return reportFigure;
				}
			}
			return null;
		}
	}

	public static String packFigures(Map<String, Object> options) {
		List<Boolean> packedFigures = new ArrayList<>();
		
		for (BirtReportFigure reportFigure : BirtReportFigure.values()) {
			String value = BirtReportSettingsUtils.getSettingsProperty(options, reportFigure.getValue());
			packedFigures.add(BooleanUtils.toBoolean(value));
		}
		return StringUtils.join(packedFigures, FIGURE_SEPARATOR);
	}

	public static List<BirtReportFigure> unpackFigures(String packedFigures) {
		ArrayList<BirtReportFigure> resultList = new ArrayList<>();
		String[] figuresArray = StringUtils.split(packedFigures, FIGURE_SEPARATOR);
		
		for (BirtReportFigure reportFigure : BirtReportFigure.values()) {
			if (BooleanUtils.toBoolean(figuresArray[reportFigure.ordinal()])) {
				resultList.add(reportFigure);
			}
		}
		return resultList;
	}

    public static boolean isOptionAllowed(String optionName, String figuresOptions) {
       return isOptionAllowed(BirtReportFigure.getFigureForValue(optionName), figuresOptions);
    }
    
    public static boolean isOptionAllowed(BirtReportFigure option, String figuresOptions) {
        List<BirtReporUtils.BirtReportFigure> figures = BirtReporUtils.unpackFigures(figuresOptions);
        return figures.contains(option);
    }

    public static boolean isAnyOptionAllowed(String[] options, String figuresOptions) {
        List<BirtReporUtils.BirtReportFigure> figures = BirtReporUtils.unpackFigures(figuresOptions);
        for (String option : options) {
            if (figures.contains(BirtReportFigure.getFigureForValue(option))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAllOptionsAllowed(String[] options, String figuresOptions) {
        List<BirtReporUtils.BirtReportFigure> figures = BirtReporUtils.unpackFigures(figuresOptions);
        for (String option : options) {
            if (!figures.contains(BirtReportFigure.getFigureForValue(option))) {
                return false;
            }
        }
        return true;
    }
}
