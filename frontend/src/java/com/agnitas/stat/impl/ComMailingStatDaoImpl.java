/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.stat.impl;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.agnitas.beans.BindingEntry.UserType;
import com.agnitas.beans.Mailing;
import org.agnitas.beans.TrackableLink;
import org.agnitas.dao.MailingDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.stat.URLStatEntry;
import org.agnitas.stat.impl.URLStatEntryImpl;
import org.agnitas.util.SafeString;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.DeliveryStat;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.mailing.service.ComMailingDeliveryStatService;
import com.agnitas.stat.ComMailingDeepStatEntry;
import com.agnitas.stat.ComMailingStatDao;
import com.agnitas.stat.ComMailingStatEntry;

public class ComMailingStatDaoImpl extends BaseDaoImpl implements ComMailingStatDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingStatDaoImpl.class);

	/** DAO accessing target groups. */
    private MailingDao mailingDao;
    private ComCompanyDao companyDao;
    private ComMailingDeliveryStatService deliveryStatService;

    @Override
	public String getMailingStatFromDB(Locale aLocale, int companyID, int mailingID, boolean deepTracking) {
		StringBuilder csvDataBuilder = new StringBuilder();
		Map<Integer, TrackableLink> urls = new Hashtable<>();
		Map<Integer, String> urlShortnames = new Hashtable<>();
		Map<Integer, String> urlAltText = new Hashtable<>();
		List<URLStatEntry> clickedUrls = new LinkedList<>();
		List<URLStatEntry> notRelevantUrls = new LinkedList<>();
		List<String> deepLinkTags = new LinkedList<>();
		List<String> deepNumTags = new LinkedList<>();
		List<String> deepAlphaTags = new LinkedList<>();

		// LOAD MAILING
		Mailing aMailing = mailingDao.getMailing(mailingID, companyID);
		if (aMailing == null) {
			return null;
		}

		// LOAD URL NAMES and ALT TEXT
		for (TrackableLink trkLink : aMailing.getTrackableLinks().values()) {
			String url_short;
			if (StringUtils.isNotBlank(trkLink.getShortname())) {
				url_short = trkLink.getShortname();
			} else {
				url_short = "";
			}
			urlShortnames.put(trkLink.getId(), url_short);

			String url_alt;
			if (StringUtils.isNotBlank(((ComTrackableLink) trkLink).getAltText())) {
				url_alt = ((ComTrackableLink) trkLink).getAltText();
			} else {
				url_alt = trkLink.getFullUrl();
			}
			urlAltText.put(trkLink.getId(), url_alt);
			
			urls.put(trkLink.getId(), trkLink);
		}

		// LOAD DEEPTRACKING PAGETAGS
		if (deepTracking) {

			// deep link pagetags
			try {
				List<Map<String, Object>> list = select(logger, "SELECT DISTINCT page_tag FROM rdirlog_" + companyID + "_ext_link_tbl WHERE mailing_id = ?", mailingID);
				for (Map<String, Object> map : list) {
					deepLinkTags.add((String) map.get("page_tag"));
				}
			} catch (Exception e) {
				logger.error("sql Problem: " + e.getMessage(), e);
			}

			// deep numeric pagetags
			try {
				List<Map<String, Object>> list = select(logger, "SELECT DISTINCT page_tag FROM rdirlog_" + companyID + "_val_num_tbl WHERE mailing_id = ?", mailingID);
				for (Map<String, Object> map : list) {
					if (!((String) map.get("PAGE_TAG")).equals("revenue")) {
						deepNumTags.add((String) map.get("page_tag"));
					}
				}
			} catch (Exception e) {
				logger.error("sql Problem: " + e.getMessage(), e);
			}

			// deep alphanumeric pagetags
			try {
				List<Map<String, Object>> list = select(logger, "SELECT DISTINCT page_tag FROM rdirlog_" + companyID + "_val_alpha_tbl WHERE mailing_id = ?", mailingID);
				for (Map<String, Object> map : list) {
					deepAlphaTags.add((String) map.get("page_tag"));
				}
			} catch (Exception e) {
				logger.error("sql Problem: " + e.getMessage(), e);
			}

			// look up mailing shortnames
			Hashtable<String, String> deepShortnames = new Hashtable<>();
			try {
				List<Map<String, Object>> list = select(logger, "SELECT pagetag, shortname FROM trackpoint_def_tbl WHERE company_id = ? AND mailing_id = ?", companyID, mailingID);
				for (Map<String, Object> map : list) {
					deepShortnames.put((String) map.get("pagetag"), (String) map.get("shortname"));
				}
			} catch (Exception e) {
				logger.error("sql Problem: " + e.getMessage(), e);
			}

			// look up additional global shortnames:
			deepShortnames = new Hashtable<>();
			try {
				List<Map<String, Object>> list = select(logger, "SELECT pagetag, shortname FROM trackpoint_def_tbl WHERE company_id = ? AND mailing_id = 0", companyID);
				for (Map<String, Object> map : list) {
					if (!deepShortnames.containsKey(map.get("pagetag"))) {
						deepShortnames.put((String) map.get("pagetag"), (String) map.get("shortname"));
					}
				}
			} catch (Exception e) {
				logger.error("sql Problem: " + e.getMessage(), e);
			}
		}

		// collect csv data
		csvDataBuilder.append("\"Mailing:\";\"");
		csvDataBuilder.append(aMailing.getShortname());
		csvDataBuilder.append("\"\r\n\r\n\""+ SafeString.getLocaleString("statistic.KlickStats", aLocale) + ":\"");
		csvDataBuilder.append("\r\n\r\n\"" + SafeString.getLocaleString("URL", aLocale) + "\";\"" + SafeString.getLocaleString("Description", aLocale) + "\"");
		
		int aTotalClicks = 0;
		int aTotalClicksNetto = 0;

		// write every value in this Hashtable Entry:
		ComMailingStatEntry statValue = new ComMailingStatEntryImpl();

		// LOAD TARGET GROUP
		String targetName = SafeString.getLocaleString("statistic.all_subscribers", aLocale);
		
		statValue.setTargetName(targetName);

		// LOAD URL_CLICKS
		String selectClicks = "SELECT count(rdir.customer_id) AS total, COUNT(DISTINCT rdir.customer_id) AS distotal, rdir.url_id AS urlid"
			+ " FROM rdirlog_" + companyID + "_tbl rdir"
        	+ " WHERE rdir.mailing_id = ?"
        	+ " GROUP BY rdir.url_id ORDER BY distotal DESC";
		// sortierung wird hier komplett ignoriert
		try {
			List<Map<String, Object>> list = select(logger, selectClicks, mailingID);
			// this will become the clickStatValues - Hashtable in the
			// current MailingStatEntry:
			Map<Integer, URLStatEntry> aktClickStatValues = new Hashtable<>();
			for (Map<String, Object> map : list) {
				URLStatEntry aEntry = new URLStatEntryImpl();
				aEntry.setClicks(((Number) map.get("total")).intValue());
				aEntry.setClicksNetto(((Number) map.get("distotal")).intValue());
				aEntry.setUrlID(((Number) map.get("urlid")).intValue());
				TrackableLink trkLink = urls.get(((Number) map.get("urlid")).intValue());
				if (trkLink == null) {
					//dirty hack also count clicks when links not found
					aTotalClicks += ((Number) map.get("total")).intValue();
					aTotalClicksNetto += ((Number) map.get("distotal")).intValue();
					logger.error("no url found for id:" + ((Number) map.get("urlid")).intValue());
					continue;
				}
				aEntry.setUrl(trkLink.getFullUrl());
				aEntry.setShortname(urlShortnames.get(((Number) map.get("urlid")).intValue()));

				aTotalClicks += ((Number) map.get("total")).intValue();
				aTotalClicksNetto += ((Number) map.get("distotal")).intValue();
				
				aktClickStatValues.put(((Number) map.get("urlid")).intValue(), aEntry);
			}

			// put clickStatValues into MailingStatEntry
			statValue.setClickStatValues(aktClickStatValues);
			statValue.setTotalClicks(aTotalClicks);
			statValue.setTotalClicksNetto(aTotalClicksNetto);
		} catch (Exception e) {
			logger.error("getMailingStatFromDB: " + e.getMessage(), e);
		}

		// LOAD DEEP_CLICKS
		if (deepTracking) {
			String selectExtLinkClicks = "SELECT COUNT(customer_id) AS total, COUNT(DISTINCT customer_id) AS distotal, page_tag"
				+ " FROM rdirlog_" + companyID + "_ext_link_tbl"
				+ " WHERE mailing_id = ?"
				+ " GROUP BY page_tag"
				+ " ORDER BY COUNT(page_tag) DESC";

			try {
				List<Map<String, Object>> list = select(logger, selectExtLinkClicks, mailingID);

				// this will become the deeptrackStatValues - Hashtable
				// in the current MailingStatEntry:
				Map<String, URLStatEntry> aktDeeptrackStatValues = new Hashtable<>();
				for (Map<String, Object> map : list) {
					URLStatEntry aEntry = new URLStatEntryImpl();
					aEntry.setClicks(((Number) map.get("total")).intValue());
					aEntry.setClicksNetto(((Number) map.get("distotal")).intValue());
					aEntry.setShortname((String) map.get("page_tag"));

					aktDeeptrackStatValues.put(((String) map.get("page_tag")), aEntry);
				}

				// put clickStatValues into MailingStatEntry
				statValue.setDeeptrackStatValues(aktDeeptrackStatValues);
			} catch (Exception e) {
				logger.error(" allDeepURLQuery Problem: " + e.getMessage(), e);
			}

			// alphanumeric link klicks
			String selectAlphaLinkClicks = "SELECT COUNT(customer_id) AS total, COUNT(DISTINCT customer_id) AS distotal, page_tag"
				+ " FROM rdirlog_" + companyID + "_val_alpha_tbl"
				+ " WHERE mailing_id = ?"
				+ " GROUP BY page_tag"
				+ " ORDER BY COUNT(page_tag) DESC";

			try {
				List<Map<String, Object>> list = select(logger, selectAlphaLinkClicks, mailingID);

				// this will become the deepAlphaValues - Hashtable in
				// the current MailingStatEntry:
				Map<String, ComMailingDeepStatEntry> aktDeepAlphaStatValues = new Hashtable<>();
				for (Map<String, Object> map : list) {
					ComMailingDeepStatEntry aDeepEntry = new ComMailingDeepStatEntryImpl();
					aDeepEntry.setRequests(((Number) map.get("total")).intValue());
					aDeepEntry.setRequestsNetto(((Number) map.get("distotal")).intValue());

					aktDeepAlphaStatValues.put(((String) map.get("page_tag")), aDeepEntry);
				}

				if (logger.isInfoEnabled()) {
					logger.info(aktDeepAlphaStatValues.size() + " values in deepAlphaValues Hashtable");
				}

				// put clickStatValues into MailingStatEntry
				statValue.setDeepAlphaValues(aktDeepAlphaStatValues);
			} catch (Exception e) {
				logger.error(" allDeepAlphaQuery Problem: " + e.getMessage(), e);
			}

			// numeric link klicks
			String selectNumLinkClicks = "SELECT COUNT(customer_id) AS total, COUNT(DISTINCT customer_id) AS distotal, page_tag"
				+ " FROM rdirlog_" + companyID + "_val_num_tbl"
				+ " WHERE mailing_id = ?"
				+ " GROUP BY page_tag"
				+ " ORDER BY COUNT(page_tag) DESC";

			try {
				List<Map<String, Object>> list = select(logger, selectNumLinkClicks, mailingID);

				// this will become the deepNumValues - Hashtable in the
				// current MailingStatEntry:
				Map<String, ComMailingDeepStatEntry> aktDeepNumStatValues = new Hashtable<>();
				for (Map<String, Object> map : list) {
					if (((String) map.get("page_tag")).compareTo("revenue") != 0) {
						ComMailingDeepStatEntry aDeepEntry = new ComMailingDeepStatEntryImpl();
						aDeepEntry.setRequests(((Number) map.get("total")).intValue());
						aDeepEntry.setRequestsNetto(((Number) map.get("distotal")).intValue());

						aktDeepNumStatValues.put(((String) map.get("page_tag")), aDeepEntry);
					}
				}

				if (logger.isInfoEnabled()) {
					logger.info(aktDeepNumStatValues.size() + " values in deepNumValues Hashtable");
				}

				// put clickStatValues into MailingStatEntry
				statValue.setDeepNumValues(aktDeepNumStatValues);

			} catch (Exception e) {
				logger.error("allDeepNumQuery Problem: " + e.getMessage(), e);
			}
		}

		// LOAD REVENUE
		if (deepTracking) {
			try {
				String selectRevenue = "SELECT SUM(deep.num_parameter) AS total from rdirlog_" + companyID + "_val_num_tbl deep"
					+ " WHERE deep.mailing_id = ? AND deep.page_tag = 'revenue'";
				statValue.setRevenue(selectIntWithDefaultValue(logger, selectRevenue, 0, mailingID));
			} catch (Exception e) {
				logger.error(" revenueQuery Problem: " + e.getMessage(), e);
			}
		}

		// CLICK_SUBSCRIBERS
		String selectTotalSuscribers = "SELECT COUNT(DISTINCT rdir.customer_id)"
			+ " FROM rdirlog_" + companyID + "_tbl rdir, rdir_url_tbl url"
			+ " WHERE rdir.mailing_id = ? and rdir.url_id = url.url_id";

		try {
			statValue.setTotalClickSubscribers(selectInt(logger, selectTotalSuscribers, mailingID));
		} catch (Exception e) {
			logger.error("getMailingStatFromDB: " + e);
		}

		// O P E N E D M A I L S
		String selectOpeners = "SELECT COUNT(onepix.customer_id) open_net"
			+ " FROM onepixellog_" + companyID + "_tbl onepix"
			+ " WHERE onepix.mailing_id = ? ";

		try {
			statValue.setOpened(selectInt(logger, selectOpeners, mailingID));
		} catch (Exception e) {
			logger.error("getMailingStatFromDB: " + e);
		}

		// O P T O U T & B O U N C E

		int optOuts = 0;
		int bounces = 0;
        String selectUserStatus = "SELECT COUNT(bind.customer_id) AS total, bind.user_status AS userstatus"
        	+ " FROM customer_" + companyID + "_binding_tbl bind"
			+ " WHERE bind.exit_mailing_id = ?"
			+ " GROUP BY bind.user_status, bind.mailinglist_id";

		try {
			List<Map<String, Object>> list = select(logger, selectUserStatus, mailingID);
			for (Map<String, Object> row : list) {
				switch (UserStatus.getUserStatusByID(((Number) row.get("userstatus")).intValue())) {
					case AdminOut:
					case UserOut:
						optOuts += ((Number) row.get("total")).intValue();
						break;
					case Bounce:
						if ((((Number) row.get("total")).intValue()) > bounces) {
							bounces = ((Number) row.get("total")).intValue();
						}
						break;
					case Active:
						break;
					case Blacklisted:
						break;
					case Suspend:
						break;
					case WaitForConfirm:
						break;
					default:
						break;
				}
			}
			statValue.setOptouts(optOuts);
			statValue.setBounces(bounces);
		} catch (Exception e) {
			logger.error("getMailingStatFromDB: " + e);
		}

		// T O T A L S E N T M A I L S

		// case mail_tracking
		boolean useMailtracking = companyDao.getCompany(companyID).getMailtracking() == 1;
		if (useMailtracking) {
			try {
				int totalMails = selectInt(logger, "SELECT COUNT(DISTINCT customer_id) FROM success_" + companyID + "_tbl WHERE mailing_id = ?", mailingID);
				statValue.setTotalMails(totalMails);
			} catch (Exception e) {
				statValue.setTotalMails(0);
				logger.error("getMailingStatFromDB (mailtracking): " + e);
			}
		} else {
			// case no_mail_tracking, so look for world mailing
			try {
				DeliveryStat deliveryStat = deliveryStatService.getDeliveryStats(companyID, mailingID, aMailing.getMailingType());
				statValue.setTotalMails(deliveryStat.getTotalMails());
			} catch (Exception e) {
				statValue.setTotalMails(0);
				logger.error("getMailingStatFromDB (no mailtracking): " + e);
			}
		}

		// DETERMINE CLICKED URLS AND SORT THEM
		for (URLStatEntry urlStat : statValue.getClickStatValues().values()) {
			int urlID = urlStat.getUrlID();
			TrackableLink trkLink = urls.get(urlID);
			if (trkLink != null) {
				clickedUrls.add(urlStat);
			}
		}

		Collections.sort(clickedUrls);
		Collections.sort(notRelevantUrls);

		DecimalFormat prcFormat = new DecimalFormat("##0.#");
	    prcFormat.setDecimalSeparatorAlwaysShown(false);
	    NumberFormat nf = NumberFormat.getCurrencyInstance(aLocale);
	    
	    //fill all values into csv
		int urlIndex = clickedUrls.size();
		//fill in link clicks
		ComMailingStatEntry aktMailingStatEntry = statValue;
        csvDataBuilder.append(";\"" + aktMailingStatEntry.getTargetName() + " - " + SafeString.getLocaleString("statistic.Clicks", aLocale) + "\";\"" + aktMailingStatEntry.getTargetName() + " - " + SafeString.getLocaleString("statistic.clicker", aLocale) + "\";\"" + SafeString.getLocaleString("ClicksQuote", aLocale) + "\"");

        while (--urlIndex >= 0) {
          	int aktUrlID = clickedUrls.get(urlIndex).getUrlID();
           	String fullUrl = clickedUrls.get(urlIndex).getUrl();
           	String shortname = "";
           	if (StringUtils.isBlank(urlShortnames.get(aktUrlID))) {
           		shortname = "" + urlAltText.get(aktUrlID);
           	} else {
           		shortname = "" + urlShortnames.get(aktUrlID);
           	}
           	csvDataBuilder.append("\r\n\"" + fullUrl + "\";\"" + shortname + "\"");

            Map<Integer, URLStatEntry> aktClickStatValues = aktMailingStatEntry.getClickStatValues();
            URLStatEntry aktURLStatEntry = aktClickStatValues.get(aktUrlID);
        	if (aktURLStatEntry != null) {
                csvDataBuilder.append(";\"" + aktURLStatEntry.getClicks() + "\";\"" + aktURLStatEntry.getClicksNetto() + "\";\"" + prcFormat.format((double)aktURLStatEntry.getClicksNetto()/(double)aktMailingStatEntry.getTotalMails() * 100d) + " %\"");
        	} else {
                csvDataBuilder.append(";\"0\";\"0\";\"0 %\"");
        	}
        }
		csvDataBuilder.append("\r\n\r\n");
		//number of clicking customers
		csvDataBuilder.append("\"" + SafeString.getLocaleString("statistic.TotalClickSubscribers", aLocale) +"\";\"\"");
        csvDataBuilder.append(";\"" + aktMailingStatEntry.getTotalClickSubscribers() + "\";;\"" + prcFormat.format((double)aktMailingStatEntry.getTotalClickSubscribers()/(double)aktMailingStatEntry.getTotalMails() * 100d) + " %\"");

		//number of total clicks
		csvDataBuilder.append("\r\n\"" + SafeString.getLocaleString("statistic.TotalClicks", aLocale) +"\";\"\"");
        csvDataBuilder.append(";\"" + aktMailingStatEntry.getTotalClicks() + "\";\"" + aktMailingStatEntry.getTotalClicksNetto() + "\"");

        csvDataBuilder.append("\r\n\r\n\" " + SafeString.getLocaleString("Delivery_Statistic", aLocale) + " \"\r\n");
        if (deepTracking) {
        	csvDataBuilder.append("\r\n");
        	csvDataBuilder.append("\r\n\"" + SafeString.getLocaleString("deeptracking.checkpoints", aLocale) + "\"\r\n");
        	csvDataBuilder.append(";\"" + SafeString.getLocaleString("deeptracking.views", aLocale) + ", " + aktMailingStatEntry.getTargetName() + "\"");
        	
        	//normal trackpoints
        	csvDataBuilder.append("\r\n\"" + SafeString.getLocaleString("deeptracking.trackpoint", aLocale) + "\";\"\"");
        	for (String aktDeepLinkTag : deepLinkTags) {
        		csvDataBuilder.append("\r\n\"" + aktDeepLinkTag + "\";\"\"");
    			Map<String, URLStatEntry> aktDeepTrackValues = aktMailingStatEntry.getDeeptrackStatValues();
    			URLStatEntry aktDeepStatEntry = aktDeepTrackValues.get(aktDeepLinkTag);
    			if (aktDeepStatEntry != null) {
    				csvDataBuilder.append(";\"" + aktDeepStatEntry.getClicks() + "\";\"" + aktDeepStatEntry.getClicksNetto() + "\";\"" + prcFormat.format((double)aktDeepStatEntry.getClicksNetto()/(double)aktMailingStatEntry.getTotalMails() * 100d) + " %\"");
    			} else {
    				csvDataBuilder.append(";\"0\";\"0\";\"0 %\"");
    			}
        	}
        	//alphanumeric tracklpoints
        	csvDataBuilder.append("\r\n\"" + SafeString.getLocaleString("deeptracking.trackpoints.alpha", aLocale) + ":\"");
        	for (String aktDeepAlphaTag : deepAlphaTags) {
        		csvDataBuilder.append("\r\n\"" + aktDeepAlphaTag + "\";\"\"");
    			ComMailingDeepStatEntry aktDeepStatEntry = null;
    			if (aktMailingStatEntry.getDeepAlphaValues() != null) {
    				Map<String, ComMailingDeepStatEntry> aktDeepAlphaValues = aktMailingStatEntry.getDeepAlphaValues();
    				if (aktDeepAlphaValues.get(aktDeepAlphaTag) != null) {
    					aktDeepStatEntry = aktDeepAlphaValues.get(aktDeepAlphaTag);
    				}
    			}
    			if (aktDeepStatEntry != null) {
    				csvDataBuilder.append(";\"" + aktDeepStatEntry.getRequests() + "\";\"" + aktDeepStatEntry.getRequestsNetto() + "\";\"" + prcFormat.format((double)aktDeepStatEntry.getRequestsNetto()/(double)aktMailingStatEntry.getTotalMails() * 100d) + " %\"");
    			} else {
    				csvDataBuilder.append(";\"0\";\"0\";\"0 %\"");
    			}
        	}
        	//numeric trackpoints
        	csvDataBuilder.append("\r\n\"" + SafeString.getLocaleString("deeptracking.trackpoints.numeric", aLocale) + ":\"");
        	for (String aktDeepNumTag : deepNumTags) {
        		csvDataBuilder.append("\r\n\"" + aktDeepNumTag + "\";\"\"");
    			ComMailingDeepStatEntry aktDeepStatEntry = null;
    			if (aktMailingStatEntry.getDeepNumValues() != null) {
    				Map<String, ComMailingDeepStatEntry> aktDeepNumValues = aktMailingStatEntry.getDeepNumValues();
    				if (aktDeepNumValues.get(aktDeepNumTag) != null) {
    					aktDeepStatEntry = aktDeepNumValues.get(aktDeepNumTag);
    				}
    			}
    			if (aktDeepStatEntry != null) {
    				csvDataBuilder.append(";\"" + aktDeepStatEntry.getRequests() + "\";\"" + aktDeepStatEntry.getRequestsNetto() + "\";\"" + prcFormat.format((double)aktDeepStatEntry.getRequestsNetto()/(double)aktMailingStatEntry.getTotalMails() * 100d) + " %\"");
    			} else {
    				csvDataBuilder.append(";\"0\";\"0\";\"0 %\"");
    			}
        	}
        }
        //number of opended mails
        csvDataBuilder.append("\r\n\" " + SafeString.getLocaleString("statistic.Opened_Mails", aLocale) + " \";\"\"");
        csvDataBuilder.append(";\"" + aktMailingStatEntry.getOpened() + "\";;\"" + prcFormat.format((double)aktMailingStatEntry.getOpened()/(double)aktMailingStatEntry.getTotalMails() * 100d) + " %\"");

        //number of opt_outs
        csvDataBuilder.append("\r\n\" " + SafeString.getLocaleString("statistic.Opt_Outs", aLocale) + " \";\"\"");
        csvDataBuilder.append(";\"" + aktMailingStatEntry.getOptouts() + "\";;\"" + prcFormat.format((double)aktMailingStatEntry.getOptouts()/(double)aktMailingStatEntry.getTotalMails() * 100d) + " %\"");

        //number of bounces
        csvDataBuilder.append("\r\n\" " + SafeString.getLocaleString("statistic.Bounces", aLocale) + " \";\"\"");
        csvDataBuilder.append(";\"" + aktMailingStatEntry.getBounces() + "\";;\"" + prcFormat.format((double)aktMailingStatEntry.getBounces()/(double)aktMailingStatEntry.getTotalMails() * 100d) + " %\"");

        if (deepTracking) {
        	//revenue
        	csvDataBuilder.append("\r\n\" " + SafeString.getLocaleString("statistic.revenue", aLocale) + " \";\"\"");
        	csvDataBuilder.append(";\"" + nf.format(aktMailingStatEntry.getRevenue()) + "\"");
        }
        //number of total recipients
        csvDataBuilder.append("\r\n\" " + SafeString.getLocaleString("Recipients", aLocale) + " \";\"\"");
        csvDataBuilder.append(";\"" + aktMailingStatEntry.getTotalMails() + "\"");

		return csvDataBuilder.toString();
	}

    @Override
	public boolean cleanAdminClicks(int companyID, int mailingID) {
		if (mailingID == 0) { // never delete mailing 0
			return false;
		} else {
			String sqlClicks = "DELETE FROM rdirlog_" + companyID + "_tbl"
				+ " WHERE mailing_id = ?"
					+ " AND customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl"
						+ " WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))";
	
			String sqlOpen = "DELETE FROM onepixellog_" + companyID	+ "_tbl"
				+ " WHERE mailing_id = ?"
					+ " AND customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl"
						+ " WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))";
	
			String sqlOpenDevice = "DELETE FROM onepixellog_device_" + companyID + "_tbl"
				+ " WHERE mailing_id = ?"
					+ " AND customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl"
						+ " WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))";
	
			String sqlBounce = "DELETE FROM bounce_tbl"
				+ " WHERE company_id = ? AND mailing_id = ?"
					+ " AND customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl"
						+ " WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))";
	
			String sqlOptout = "UPDATE customer_" + companyID + "_binding_tbl SET exit_mailing_id = 0"
				+ " WHERE exit_mailing_id = ?"
					+ " AND user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?)";
	
			try {
				update(logger, sqlClicks, mailingID, mailingID);
				update(logger, sqlOpen, mailingID, mailingID);
				update(logger, sqlOpenDevice, mailingID, mailingID);
				update(logger, sqlBounce, companyID, mailingID, mailingID);
				update(logger, sqlOptout, mailingID, mailingID);
				return true;
			} catch (Exception e) {
				logger.error("cleanAdminClicks: " + e.getMessage(), e);
				return false;
			}
		}
    }

	@Required
    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

	@Required
    public void setCompanyDao(ComCompanyDao companyDao) {
        this.companyDao = companyDao;
    }

	@Required
	public void setDeliveryStatService(ComMailingDeliveryStatService deliveryStatService) {
		this.deliveryStatService = deliveryStatService;
	}
}
