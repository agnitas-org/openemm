/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;

public class BirtReporUtils {

	private static final String FIGURE_SEPARATOR = ",";

	public enum BirtReportFigure {
		OPENERS_MEASURED("openersMeasured", CommonKeys.OPENERS_MEASURED_INDEX),
		OPENERS_INVISIBLE("openersInvisible", CommonKeys.OPENERS_INVISIBLE_INDEX),
		OPENERS_TOTAL("openersTotal", CommonKeys.OPENERS_TOTAL_INDEX),
		OPENINGS_ANONYMOUS("openingsAnonymous", CommonKeys.OPENINGS_ANONYMOUS_INDEX),
		CLICKERS_TOTAL("clickingRecipients", CommonKeys.CLICKER_INDEX),
		CLICKS_ANONYMOUS("clickingAnonymous", CommonKeys.CLICKS_ANONYMOUS_INDEX),
		OPENERS_PC("openersPcRate", null),
		OPENERS_MOBILE("openersMobile", null),
		OPENERS_MULTIPLE("openersPcAndMobile", null),
		OPENERS_TABLET("openersTablet", null),
        OPENERS_SMARTTV("openersSmartTV", null),
		CLICKERS_PC("clickingRecipientsPcRate", null),
		CLICKERS_MOBILE("clickingRecipientsMobile", null),
		CLICKERS_MULTIPLE("clickingRecipientsPcAndMobile", null),
        CLICKERS_TABLET("clickingTablet", null),
        CLICKERS_SMARTTV("clickingRecipientsSmartTV", null),
		SIGNED_OFF("signedOff", CommonKeys.OPT_OUTS_INDEX),
		HARDBOUNCES("hardbounces", CommonKeys.HARD_BOUNCES_INDEX),
		SOFTBOUNCES("softbounces", CommonKeys.SOFT_BOUNCES_INDEX),
		REVENUE("conversionRate", CommonKeys.REVENUE_SHIFTED_INDEX),
		HTML("html", null),
		TEXT("text", null),
		OFFLINE_HTML("offlineHtml", null),
        ACTIVATE_LINK_STATISTICS("activateLinkStatistics", null),
        RECIPIENT_STATUS("recipientStatus", null),
        RECIPIENT_DEVELOPMENT_DETAILED("recipientDevelopmentDetailed", null),
        RECIPIENT_DEVELOPMENT_NET("recipientDevelopmentNet", null),
        ACTIVITY_ANALYSIS("activityAnalysis", null),
        OPENERS_AFTER_DEVICE("openersAfterDevice", null),
		CLICKERS_AFTER_DEVICE("clickersAfterDevice", null),
		OPENERS_DEVICES("openerDevices", null),
		CLICKER_DEVICES("clickerDevices", null),
        MAILING_TYPE("mailingType", null),
		SENT_MAILS("sentMails", CommonKeys.DELIVERED_EMAILS_INDEX),
        OPENERS("openers", CommonKeys.OPENERS_INDEX),
        RECIPIENT_DOI("recipientDOI", null),
		EMAILS_ACCEPTED("emailsAccepted", CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX),
		DOI("recipientDOI", null),
		MAILING_NAME("mailingName", null),
		MAILING_ID("mailingId", null),
		DESCRIPTION("description", null),
		ARCHIVE("archive", null),
		MAIL_FORMAT("mailFormat", null),
		AVERAGE_SIZE("averageSize", null),
		SUBJECT("subject", null),
		MAILING_LIST("mailingList", null),
		TARGET_GROUPS("targetGroups", null),
		NR_OF_RECIPIENTS_WITH_TRACK_VETO("nrOfRecipientsWithTrackVeto", null),
		SENDING_STARTED("sendingStarted", null),
		SENDING_FINISHED("sendingFinished", null),
		SENDER_INFO("senderInfo", null),
		REPLY_TO_INFO("replyToInfo", null),
		BOUNCE_REASON("bounceReason", null),
		TOP_DOMAINS("topDomains", null);

		private final String value;
		private final Integer categoryIndex;

		BirtReportFigure(String value, Integer categoryIndex) {
			this.value = value;
			this.categoryIndex = categoryIndex;
		}

		public String getValue() {
			return value;
		}

		public Integer getCategoryIndex() {
			return categoryIndex;
		}

		public static BirtReportFigure getFigureForValue(String value) {
			for (BirtReportFigure reportFigure : BirtReportFigure.values()) {
				if (reportFigure.getValue().equals(value)) {
					return reportFigure;
				}
			}
			return null;
		}

		public static BirtReportFigure getFigureForCategoryIndex(int categoryIndex) {
			return Arrays.stream(BirtReportFigure.values())
				.filter(reportFigure -> reportFigure.getCategoryIndex() != null)
				.filter(reportFigure -> reportFigure.getCategoryIndex() == categoryIndex)
				.findFirst()
				.orElse(null);
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

	@SuppressWarnings("unused") // used in .rptdesign
	public static boolean isCategoryAllowed(int categoryIndex, String figuresOptions) {
		BirtReportFigure figure = BirtReportFigure.getFigureForCategoryIndex(categoryIndex);
		if (figure == null) {
			return false;
		}
		return isOptionAllowed(figure, figuresOptions);
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
