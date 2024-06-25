/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.Arrays;
import java.util.List;

import com.agnitas.emm.core.mobile.bean.DeviceClass;

public interface CommonKeys {

	String ACTIVE = "recipient.MailingState1";
	String ACTIVE_STATUS = "default.status.active";
	int ACTIVE_INDEX = 1;
	
	// indices represent the position in the report
    String RECIPIENTS_NUMBER = "Recipients";
    int RECIPIENTS_NUMBER_INDEX = 0;
	String DELIVERED_EMAILS = "statistic.mails.sent";
	int DELIVERED_EMAILS_INDEX = 1;
    String DELIVERED_EMAILS_DELIVERED = "statistic.mails.delivered";
	String PREDELIVERY_SENT = "mailing.status.sent";
	int DELIVERED_EMAILS_DELIVERED_INDEX = 2;
	String OPENERS = "statistic.opener";
	int OPENERS_INDEX = 3;			// 4
	String OPENINGS_ANONYMOUS = "statistic.openings.anonym";
	int OPENINGS_ANONYMOUS_INDEX = 39;
	String OPENERS_MEASURED = "statistic.opener.measure";
	int OPENERS_MEASURED_INDEX = 3;
	String OPENERS_PROXY = "statistic.opener.proxy";
	int OPENERS_PROXY_INDEX = 77;
	String OPENERS_INVISIBLE = "statistic.opener.invisible";
	int OPENERS_INVISIBLE_INDEX = 4;
	String OPENERS_TOTAL = "statistic.opener.total";
	int OPENERS_TOTAL_INDEX = 5;
    String CLICKER = "statistic.clicker";
	int CLICKER_INDEX = 6;			// 5
	String CLICKS_ANONYMOUS = "statistic.clicks.anonym";
	int CLICKS_ANONYMOUS_INDEX = 40;
	String OPT_OUTS = "statistic.Opt_Outs";
	int OPT_OUTS_INDEX = 7;			// 6
	String SOFT_BOUNCES = "statistic.bounces.softbounce";
	int SOFT_BOUNCES_INDEX = 8;     // 2
	String HARD_BOUNCES = "statistic.bounces.hardbounce";
	int HARD_BOUNCES_INDEX = 9;		// 3
    String SOFT_BOUNCES_UNDELIVERABLE = "report.softbounces.undeliverable";
	int SOFT_BOUNCES_UNDELIVERABLE_INDEX = 10;
	String REVENUE = "statistic.revenue";
	int REVENUE_INDEX = 11;
    int REVENUE_SHIFTED_INDEX = 110;
    String BOUNCES = "statistic.Bounces";
    String BOUNCES_STATUS = "recipient.MailingState2";
	int BOUNCES_INDEX = 12;
    String CLICKERS_TO_BUYERS_RATIO = "statistic.revenue.conversion.buyer.clicker";
    int CLICKERS_TO_BUYERS_RATIO_INDEX = 37;
    String RECIPIENTS_TO_BUYERS_RATIO = "statistic.revenue.conversion.buyer.recipients";
    int RECIPIENTS_TO_BUYERS_RATIO_INDEX = 38;
    String WAITING_FOR_CONFIRM = "birt.recipient.notConfirmed";
	int WAITING_FOR_CONFIRM_INDEX = 41;
	String BLACKLISTED = "recipient.MailingState6";
	int BLACKLISTED_INDEX = 42;

    // pc/mobile data
    String OPENERS_TRACKED = "statistic.opener.measure";
	int OPENERS_TRACKED_INDEX = 13;
    String OPENERS_PC = "statistic.opener.pc";
	int OPENERS_PC_INDEX = 14;
	String OPENERS_TABLET = "statistic.opener.tablet";
	int OPENERS_TABLET_INDEX = 15;
    String OPENERS_MOBILE = "statistic.opener.mobile";
	int OPENERS_MOBILE_INDEX = 16;
    String OPENERS_SMARTTV = "statistic.opener.smarttv";
    int OPENERS_SMARTTV_INDEX = 17;
    String OPENERS_PC_AND_MOBILE = "statistic.opener.multiple";
	int OPENERS_PC_AND_MOBILE_INDEX = 18;

    String CLICKER_TRACKED = "statistic.clicker";
	int CLICKER_TRACKED_INDEX = 19;
    String CLICKER_PC = "statistic.clicker.pc";
	int CLICKER_PC_INDEX = 20;
	String CLICKER_TABLET = "statistic.clicker.tablet";
	int CLICKER_TABLET_INDEX = 21;
    String CLICKER_MOBILE = "statistic.clicker.mobile";
	int CLICKER_MOBILE_INDEX = 22;
    String CLICKER_SMARTTV = "statistic.clicker.smarttv";
	int CLICKER_SMARTTV_INDEX = 23;
    String CLICKER_PC_AND_MOBILE = "statistic.clicker.multiple";
    int CLICKER_PC_AND_MOBILE_INDEX = 24;

    String REVENUE_PC = "statistic.revenue.pc";
    int REVENUE_PC_INDEX = 32;
    String REVENUE_MOBILE = "statistic.revenue.mobile";
    int REVENUE_MOBILE_INDEX = 33;
    String REVENUE_TABLET = "statistic.revenue.tablet";
    int REVENUE_TABLET_INDEX = 34;
    String REVENUE_SMARTTV = "statistic.revenue.smarttv";
    int REVENUE_SMARTTV_INDEX = 35;
    String REVENUE_PC_AND_MOBILE = "statistic.revenue.multiple";
    int REVENUE_PC_AND_MOBILE_INDEX = 36;

	String SENT_HTML = "HTML";
	int SENT_HTML_INDEX = 25;
	String SENT_TEXT = "recipient.mailingtype.text";
	int SENT_TEXT_INDEX = 26;
	String SENT_OFFILE_HTML = "recipient.mailingtype.htmloffline";
	int SENT_OFFLINE_HTML_INDEX = 28;

	// response table
	String OPENINGS_GROSS_MEASURED = "statistic.MeasuredOpeningsGross";
	int OPENINGS_GROSS_MEASURED_INDEX = 29;
	String CLICKS_GROSS = "statistic.Clicks";
	int CLICKS_GROSS_INDEX = 30;
	String ACTIVITY_RATE = "statistic.ActivityRate";
	int ACTIVITY_RATE_INDEX = 31;

	// All recipients is a targetgroup
	String ALL_SUBSCRIBERS = "statistic.all_subscribers";
	int ALL_SUBSCRIBERS_TARGETGROUPID = 1;
	int ALL_SUBSCRIBERS_INDEX = 1;

	// recipient types
	// hint: TYPE_WORLDMAILING exludes admin and test recipients even after sending of the world mailing
	String TYPE_WORLDMAILING = "WORLDMAILING";
	String TYPE_ALL_SUBSCRIBERS = "ALL_SUBSCRIBERS";
	String TYPE_ADMIN_AND_TEST = "ADMIN_AND_TEST";

    // user statuses in mailing lists (can also be found in BindingEntry class)
    int USER_STATUS_BOUNCED = 2;

    int SEND_DATE_INDEX = 1000;
    String SEND_DATE = "mailing.senddate";
    int TARGET_CATEGORY_INDEX = 1001;
    String TARGET_CATEGORY = "Target";
	int SCHEDULED_SEND_TIME_INDEX = 1002;
	String SCHEDULED_SEND_TIME = "statistic.ScheduledSendTime";

	int CONFIRMED_DOI_INDEX = 78;
	String CONFIRMED_DOI = "statistic.recipient.status.confirmed";

	int NOT_CONFIRMED_DOI_INDEX = 79;
	String NOT_CONFIRMED_DOI = "birt.recipient.notConfirmed";
	int NOT_CONFIRMED_AND_DELETED_DOI_INDEX = 80;
	String NOT_CONFIRMED_AND_DELETED_DOI = "statistic.recipient.status.unconfirmed.deleted";
	int CONFIRMED_AND_NOT_ACTIVE_DOI_INDEX = 81;
	String CONFIRMED_AND_NOT_ACTIVE_DOI = "statistic.recipient.status.confirmed.inactive";
	int TOTAL_DOI_INDEX = 82;
	String TOTAL_DOI = "recipient.DOI";

	String ALL_MAILINGLISTS = "statistic.All_Mailinglists";

    List<DeviceClass> AVAILABLE_DEVICECLASSES = Arrays.asList(DeviceClass.DESKTOP, DeviceClass.MOBILE, DeviceClass.TABLET, DeviceClass.SMARTTV);
    
    int OPTIMIZATION_SUMMARY_GROUP_ID = -1;
    int OPTIMIZATION_WINNER_GROUP_ID = 0;
}
