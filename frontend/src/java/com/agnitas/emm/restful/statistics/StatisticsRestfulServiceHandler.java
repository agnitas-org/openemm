/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.statistics;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.agnitas.beans.Mailinglist;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.exception.UnknownUserStatusException;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils.RequestMethod;

import com.agnitas.beans.Admin;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.restful.BaseRequestResponse;
import com.agnitas.emm.restful.JsonRequestResponse;
import com.agnitas.emm.restful.ResponseType;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.restful.RestfulNoDataFoundException;
import com.agnitas.emm.restful.RestfulServiceHandler;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.reporting.birt.external.beans.factory.MailingSummaryDataSetFactory;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.reporting.birt.external.dataset.MailingBouncesDataSet;
import com.agnitas.reporting.birt.external.dataset.MailingSummaryDataSet;
import com.agnitas.reporting.birt.external.dataset.MailingBouncesDataSet.BounceType;
import com.agnitas.reporting.birt.external.dataset.MailingBouncesDataSet.BouncesRow;
import com.agnitas.reporting.birt.external.dataset.MailingSummaryDataSet.MailingSummaryRow;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This restful service is available at: https://<system.url>/restful/statistics
 */
public class StatisticsRestfulServiceHandler implements RestfulServiceHandler {
	private static final String MAILING = "mailing";
	private static final String CUSTOMERS = "customers";
	private static final String MAILINGLIST = "mailinglist";
	private static final String BOUNCES = "bounces";

	public static final String NAMESPACE = "statistics";

	private UserActivityLogDao userActivityLogDao;
	
	private ComCompanyDao companyDao;
	
	private ComMailingDao mailingDao;
	
	private MailingSummaryDataSetFactory mailingSummaryDataSetFactory;

	private MailinglistDao mailinglistDao;

	public StatisticsRestfulServiceHandler(UserActivityLogDao userActivityLogDao, ComCompanyDao companyDao, ComMailingDao mailingDao, MailingSummaryDataSetFactory mailingSummaryDataSetFactory, MailinglistDao mailinglistDao) {
		this.userActivityLogDao = userActivityLogDao;
		this.companyDao = companyDao;
		this.mailingDao = mailingDao;
		this.mailingSummaryDataSetFactory = mailingSummaryDataSetFactory;
		this.mailinglistDao = mailinglistDao;
	}

	@Override
	public RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception {
		// No redirect needed
		return this;
	}

	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataFile, BaseRequestResponse restfulResponse,
			ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception {
		if (requestMethod == RequestMethod.GET) {
			((JsonRequestResponse) restfulResponse).setJsonResponseData(new JsonNode(getStatisticsData(request, admin)));
		} else {
			throw new RestfulClientException("Invalid http request method");
		}
	}

	/**
	 * Return statistics data sets
	 * 
	 * @param request
	 * @param admin
	 * @return
	 * @throws Exception
	 */
	private Object getStatisticsData(HttpServletRequest request, Admin admin) throws Exception {
		if (!admin.permissionAllowed(Permission.STATS_SHOW)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.STATS_SHOW.toString() + "'");
		}
		
		int companyID = admin.getCompanyID();

		String[] restfulContext = RestfulServiceHandler.getRestfulContext(request, NAMESPACE, 0, 2);

		if (restfulContext.length == 0) {
			// Show list of available statistics
			userActivityLogDao.addAdminUseOfFeature(admin, "restful/statistics", new Date());
			userActivityLogDao.writeUserActivityLog(admin, "restful/statistics GET", "ALL");

			JsonObject resultJsonObject = new JsonObject();
			
			JsonArray statisticsJsonArray = new JsonArray();

			statisticsJsonArray.add(MAILING + "/<mailing_id>");
			statisticsJsonArray.add(CUSTOMERS);
			statisticsJsonArray.add(MAILINGLIST + "/<mailinglist_id>");
			statisticsJsonArray.add(BOUNCES + "/<mailing_id>");
			
			resultJsonObject.add("available statistics", statisticsJsonArray);

			return resultJsonObject;
		} else if (MAILING.equalsIgnoreCase(restfulContext[0])) {
			if (restfulContext.length != 2) {
				throw new RestfulClientException("Invalid request data for " + MAILING);
			} else if (!AgnUtils.isNumber(restfulContext[1])) {
				throw new RestfulClientException("Invalid request data for " + MAILING + ": mailing_id must be an Integer");
			} else {
				int mailingID = Integer.parseInt(restfulContext[1]);
				LightweightMailing mailing = mailingDao.getLightweightMailing(companyID, mailingID);
				if (mailing == null) {
					throw new RestfulClientException("Invalid request data for " + MAILING + ": mailing_id does not exist: " + mailingID);
				} else {
					JsonObject resultJsonObject = getMailingStatistics(mailing);
					userActivityLogDao.addAdminUseOfFeature(admin, "restful/statistics/" + MAILING, new Date());
					userActivityLogDao.writeUserActivityLog(admin, "restful/statistics/" + MAILING + " GET", Integer.toString(mailingID));
					return resultJsonObject;
				}
			}
		} else if (CUSTOMERS.equalsIgnoreCase(restfulContext[0])) {
			if (restfulContext.length != 1) {
				throw new RestfulClientException("Invalid request data for " + CUSTOMERS);
			} else {
				JsonObject resultJsonObject = getCustomersStatistics(companyID);
				userActivityLogDao.addAdminUseOfFeature(admin, "restful/statistics/" + CUSTOMERS, new Date());
				userActivityLogDao.writeUserActivityLog(admin, "restful/statistics/" + CUSTOMERS + " GET", "");
				return resultJsonObject;
			}
		} else if (MAILINGLIST.equalsIgnoreCase(restfulContext[0])) {
			if (restfulContext.length != 2) {
				throw new RestfulClientException("Invalid request data for " + MAILINGLIST);
			} else if (!AgnUtils.isNumber(restfulContext[1])) {
				throw new RestfulClientException("Invalid request data for " + MAILINGLIST + ": mailinglist_id must be an Integer");
			} else {
				int mailingslistID = Integer.parseInt(restfulContext[1]);
				if (!mailinglistDao.exist(mailingslistID, companyID)) {
					throw new RestfulClientException("Invalid request data for " + MAILINGLIST + ": mailinglist_id does not exist: " + mailingslistID);
				} else {
					Map<Integer, Integer> results = mailinglistDao.getMailinglistWorldSubscribersStatistics(companyID, mailingslistID);
					JsonObject resultJsonObject = new JsonObject();
					for (Entry<Integer, Integer> row : results.entrySet()) {
						resultJsonObject.add(UserStatus.getUserStatusByID(row.getKey()).name(), row.getValue());
					}

					userActivityLogDao.addAdminUseOfFeature(admin, "restful/statistics/" + MAILINGLIST, new Date());
					userActivityLogDao.writeUserActivityLog(admin, "restful/statistics/" + MAILINGLIST + " GET", Integer.toString(mailingslistID));
					
					return resultJsonObject;
				}
			}
		} else if (BOUNCES.equalsIgnoreCase(restfulContext[0])) {
			if (restfulContext.length != 2) {
				throw new RestfulClientException("Invalid request data for " + BOUNCES);
			} else if (!AgnUtils.isNumber(restfulContext[1])) {
				throw new RestfulClientException("Invalid request data for " + BOUNCES + ": mailing_id must be an Integer");
			} else {
				int mailingID = Integer.parseInt(restfulContext[1]);
				LightweightMailing mailing = mailingDao.getLightweightMailing(companyID, mailingID);
				if (mailing == null) {
					throw new RestfulClientException("Invalid request data for " + BOUNCES + ": mailing_id does not exist: " + mailingID);
				} else {
					JsonObject resultJsonObject = getBouncesStatistics(mailing);
					userActivityLogDao.addAdminUseOfFeature(admin, "restful/statistics/" + BOUNCES, new Date());
					userActivityLogDao.writeUserActivityLog(admin, "restful/statistics/" + BOUNCES + " GET", Integer.toString(mailingID));
					return resultJsonObject;
				}
			}
		} else {
			throw new RestfulNoDataFoundException("Unsupported statistics type: " + restfulContext[0]);
		}
	}

	private JsonObject getMailingStatistics(LightweightMailing mailing) throws Exception {
		MailingSummaryDataSet mailingSummaryDataSet = mailingSummaryDataSetFactory.create();
		int tempTableID = mailingSummaryDataSet.prepareReport(mailing.getMailingID(), mailing.getCompanyID(), null, "W", false, "", "", false);
		List<MailingSummaryRow> results = mailingSummaryDataSet.getSummaryData(tempTableID);
		
		JsonObject resultJsonObject = new JsonObject();
		resultJsonObject.add("mailing_id", mailing.getMailingID());
		resultJsonObject.add("name", mailing.getShortname());

		resultJsonObject.add("mailingsSent", getMailingSummaryRowValue(results, CommonKeys.DELIVERED_EMAILS));
		resultJsonObject.add("openersMeasured", getMailingSummaryRowValue(results, CommonKeys.OPENERS_MEASURED));
		resultJsonObject.add("clickersWithoutOpening", getMailingSummaryRowValue(results, CommonKeys.OPENERS_INVISIBLE));
		resultJsonObject.add("openings", getMailingSummaryRowValue(results, CommonKeys.OPENINGS_GROSS_MEASURED));
		resultJsonObject.add("openingsAnonymous", getMailingSummaryRowValue(results, CommonKeys.OPENINGS_ANONYMOUS));
		resultJsonObject.add("openersExtrapolated", getMailingSummaryRowValue(results, CommonKeys.OPENERS_TOTAL));
		
		JsonObject openersByDeviceClass = new JsonObject();
		openersByDeviceClass.add("desktop", getMailingSummaryRowValue(results, CommonKeys.OPENERS_PC));
		openersByDeviceClass.add("tablet", getMailingSummaryRowValue(results, CommonKeys.OPENERS_TABLET));
		openersByDeviceClass.add("mobile", getMailingSummaryRowValue(results, CommonKeys.OPENERS_MOBILE));
		openersByDeviceClass.add("smarttv", getMailingSummaryRowValue(results, CommonKeys.OPENERS_SMARTTV));
		openersByDeviceClass.add("multiple", getMailingSummaryRowValue(results, CommonKeys.OPENERS_PC_AND_MOBILE));
		resultJsonObject.add("openersByDeviceClass", openersByDeviceClass);
		
		resultJsonObject.add("clickers", getMailingSummaryRowValue(results, CommonKeys.CLICKER));
		resultJsonObject.add("clicks", getMailingSummaryRowValue(results, CommonKeys.CLICKS_GROSS));
		resultJsonObject.add("clicksAnonymous", getMailingSummaryRowValue(results, CommonKeys.CLICKS_ANONYMOUS));
		
		JsonObject clickersByDeviceClass = new JsonObject();
		clickersByDeviceClass.add("desktop", getMailingSummaryRowValue(results, CommonKeys.CLICKER_PC));
		clickersByDeviceClass.add("tablet", getMailingSummaryRowValue(results, CommonKeys.CLICKER_TABLET));
		clickersByDeviceClass.add("mobile", getMailingSummaryRowValue(results, CommonKeys.CLICKER_MOBILE));
		clickersByDeviceClass.add("smarttv", getMailingSummaryRowValue(results, CommonKeys.CLICKER_SMARTTV));
		clickersByDeviceClass.add("multiple", getMailingSummaryRowValue(results, CommonKeys.CLICKER_PC_AND_MOBILE));
		resultJsonObject.add("clickersByDeviceClass", clickersByDeviceClass);
		
		resultJsonObject.add("unsubscribers", getMailingSummaryRowValue(results, CommonKeys.OPT_OUTS));
		resultJsonObject.add("hardBounces", getMailingSummaryRowValue(results, CommonKeys.HARD_BOUNCES));
		resultJsonObject.add("revenue", getMailingSummaryRowValue(results, CommonKeys.REVENUE));
		
		return resultJsonObject;
	}

	private JsonObject getBouncesStatistics(LightweightMailing mailing) throws Exception {
		
		JsonObject resultJsonObject = new JsonObject();
		resultJsonObject.add("mailing_id", mailing.getMailingID());
		resultJsonObject.add("name", mailing.getShortname());
		
		JsonArray bouncesJsonArray = new JsonArray();
		
		List<BouncesRow> softBouncesResult = new MailingBouncesDataSet().getBouncesWithDetail(mailing.getCompanyID(), mailing.getMailingID(), "en", null, BounceType.SOFTBOUNCES);
		for (BouncesRow bouncesRow : softBouncesResult) {
			if (bouncesRow.getCount() > 0) {
				JsonObject bouncesEntry = new JsonObject();
				bouncesEntry.add("dsn", bouncesRow.getDetail());
				bouncesEntry.add("text", bouncesRow.getDetailstring());
				bouncesEntry.add("type", "softbounce");
				bouncesEntry.add("amount", bouncesRow.getCount());
				bouncesJsonArray.add(bouncesEntry);
			}
		}
		
		List<BouncesRow> hardBouncesResult = new MailingBouncesDataSet().getBouncesWithDetail(mailing.getCompanyID(), mailing.getMailingID(), "en", null, BounceType.HARDBOUNCES);
		for (BouncesRow bouncesRow : hardBouncesResult) {
			if (bouncesRow.getCount() > 0) {
				JsonObject bouncesEntry = new JsonObject();
				bouncesEntry.add("dsn", bouncesRow.getDetail());
				bouncesEntry.add("text", bouncesRow.getDetailstring());
				bouncesEntry.add("type", "hardbounce");
				bouncesEntry.add("amount", bouncesRow.getCount());
				bouncesJsonArray.add(bouncesEntry);
			}
		}
		
		resultJsonObject.add("bounces", bouncesJsonArray);
		
		return resultJsonObject;
	}

	private int getMailingSummaryRowValue(List<MailingSummaryRow> rows, String categoryName) {
		for (MailingSummaryRow row : rows) {
			if (categoryName.equals(row.getCategory())) {
				return row.getCount();
			}
		}
		return 0;
	}

	private JsonObject getCustomersStatistics(int companyID) throws UnknownUserStatusException {
		JsonObject resultJsonObject = new JsonObject();
		
		resultJsonObject.add("customersTotal", companyDao.getNumberOfCustomers(companyID));

		JsonArray resultMailinglistsJsonArray = new JsonArray();
		for (Mailinglist mailingslist : mailinglistDao.getMailinglists(companyID)) {
			Map<Integer, Integer> results = mailinglistDao.getMailinglistWorldSubscribersStatistics(companyID, mailingslist.getId());
			JsonObject resultMailinglistJsonObject = new JsonObject();
			resultMailinglistJsonObject.add("mailinglist_id", mailingslist.getId());
			resultMailinglistJsonObject.add("name", mailingslist.getShortname());

			JsonObject resultMailinglistStatusJsonObject = new JsonObject();
			for (Entry<Integer, Integer> row : results.entrySet()) {
				resultMailinglistStatusJsonObject.add(UserStatus.getUserStatusByID(row.getKey()).name(), row.getValue());
			}
			resultMailinglistJsonObject.add("customers", resultMailinglistStatusJsonObject);
			
			resultMailinglistsJsonArray.add(resultMailinglistJsonObject);
		}
		resultJsonObject.add("mailinglists", resultMailinglistsJsonArray);
		
		return resultJsonObject;
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}
}
