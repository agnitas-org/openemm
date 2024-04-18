/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.scripthelper.service;

import java.util.Map;

import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;

import com.agnitas.dao.ComMailingDao;

public class ScriptHelperService {
	private ComMailingDao mailingDao;
	private PreviewFactory previewFactory;
	
	/**
	 * this method returns the mailingID for last sent mailing for the given company,customer.
	 * ATTENTION! Only already sent World-Mailings will be found!
	 * @param companyID
	 * @param customerID
	 * @return
	 */
	public int getLastSentMailingID(int companyID, int customerID) {
		int returnValue = -1;
		returnValue = mailingDao.getLastSentMailing(companyID, customerID);
		return returnValue;
	}

	/**
	 * This method returns the mailing ID for a already sent world-mailing with the given companyID and
	 * (if given) the mailingListID. If no mailingListID is given (null or "0") it will be ignored.
	 * @param companyID
	 * @param mailingListID
	 * @return
	 */
	public int getLastSentWorldMailingByCompanyAndMailinglist(int companyID, int mailingListID) {
		return mailingDao.getLastSentWorldMailingByCompanyAndMailinglist(companyID, mailingListID);
	}
	
	
	public Map <String, Object> getAnonLastSentMailing(int companyID, int customerID) throws Exception {
		int lastNewsletter = -1;
		lastNewsletter = getLastSentMailingID(companyID, customerID);
		if (lastNewsletter == -1) {
			throw new Exception("No Mailing found with companyID: " + companyID + " and customerID: " + customerID);
		}
		return generateBackEndPreview(lastNewsletter, customerID);
	}
	
	/**
	 * This method returns the HTML Part for the given customerID and the given mailingID.
	 * @param mailingID
	 * @param customerID
	 * @return
	 * @throws Exception
	 */
	public Object getAnonLastSentMailingByMailingID(int mailingID, int customerID) throws Exception {
		Object returnObject = null;
		try {
			returnObject = generateCachedBackEndHTMLPreview(mailingID, customerID);
		} catch (Exception e) {
			throw new Exception("ScriptHelperService failed to generate the html preview: " + e);
		}
		if (returnObject == null) {
			throw new Exception("ScriptHelperService failed to generate the html preview. MailingID: " + mailingID + ", customerID: " + customerID);
		}
		return returnObject;
	}
	
	@Deprecated
	protected Map<String, Object> generateBackEndPreview(int mailingID,int customerID) {
		// PreviewFactory previewFactory = (PreviewFactory) con.getBean("PreviewFactory");
		Preview preview = previewFactory.createPreview();
		// get a anonymized Fullview.
		Map<String,Object> output = preview.createPreview (mailingID,customerID,null,true, false);
		preview.done();
		return output;
	}
	
	/**
	 * generates a html representation of the given mailing (mailingID) with the given Customer.
	 * @param mailingID
	 * @param customerID
	 * @return
	 * @throws Exception
	 */
	protected Object generateCachedBackEndHTMLPreview(int mailingID, int customerID) throws Exception {
		return generateCachedBackEndHTMLPreview(mailingID, customerID, false);
	}
	
	/**
	 * generates a html representation of the given mailing (mailingID) with the given Customer.
	 * if mobile is true, the mobile representation will be returned. BUT if the mobile part is null,
	 * the normal html part will be returned!
	 * @param mailingID
	 * @param customerID
	 * @param mobile
	 * @return
	 * @throws Exception
	 */
	protected Object generateCachedBackEndHTMLPreview(int mailingID, int customerID, boolean mobile) throws Exception {
		Preview preview = previewFactory.createPreview();
		Page page = preview.makePreview(mailingID,customerID,null,true, true);
		if (page.getError() != null)  {
			throw new Exception("ScriptHelperService::generateBackEndHTMLPreview: Error generating preview. mailingID: " + mailingID +
					" customerID: " + customerID + "\n previewError: " + page.getError());
		}
		return page.getHTML();
	}
	
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void setPreviewFactory(PreviewFactory previewFactory) {
		this.previewFactory = previewFactory;
	}
}
