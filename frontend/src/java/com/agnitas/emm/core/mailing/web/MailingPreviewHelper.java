/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.Mediatype;

import com.agnitas.beans.ComMailing;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.web.ComMailingContentForm;
import com.agnitas.web.ComMailingSendForm;

/**
 * Helper class to find the proper preselected preview format.
 */
public class MailingPreviewHelper {

	/** Indicates, that the algorithm was not able to detect proper preview format. */
	public static final int UNDEFINED_PREVIEW_FORMAT = -1;
	
	/**
	 * Updated preview format for mailing contents.
	 * 
	 * @param form ComMailingContentForm bean
	 * @param companyID company ID
	 * @param dao DAO for accessing mailings
	 */
	public static void updateActiveMailingPreviewFormat(ComMailingContentForm form, HttpServletRequest req, int companyID, ComMailingDao dao) {
		int activeFormat = computeActivePreviewFormat(form.getPreviewFormat(), form.getMailingID(), companyID, dao);
		
		form.setPreviewFormat(activeFormat);
		
		req.setAttribute("PREVIEW_FORMAT", activeFormat);
	}
	
	/**
	 * Updated preview format for mailing send tab.
	 * 
	 * @param form ComMailingSendForm bean
	 * @param companyID company ID
	 * @param dao DAO for accessing mailings
	 */
	public static void updateActiveMailingPreviewFormat(ComMailingSendForm form, HttpServletRequest req, int companyID, ComMailingDao dao) {
		int activeFormat = computeActivePreviewFormat(form.getPreviewFormat(), form.getMailingID(), companyID, dao);
		
		form.setPreviewFormat(activeFormat);
		
		req.setAttribute("PREVIEW_FORMAT", activeFormat);
	}
	
	/**
	 * Computes proper preview format depending on used media types and currently selected preview format.
	 * 
	 * @param currentFormat current preview format
	 * @param mailingID mailing ID
	 * @param companyID company ID
	 * @param dao DAO for accessing mailings
	 * 
	 * @return preview format to select
	 */
	private static int computeActivePreviewFormat(int currentFormat, int mailingID, int companyID, ComMailingDao dao) {
		ComMailing mailing = (ComMailing)dao.getMailing(mailingID, companyID);
		
		// No mailing found? 
		if(mailing.getId() == 0)
			return UNDEFINED_PREVIEW_FORMAT;
		
		// Get media types of mailing
		Map<Integer, Mediatype> mediaTypes = mailing.getMediatypes();
		List<Integer> orderedTypeCodes = new ArrayList<>(mediaTypes.keySet());
		
		/*
		 * Here, we have to do some mapping.
		 * 
		 * Preview format HTML and text (0 and 1) are both used by media type 0
		 * preview format 2 is used for media type 1,
		 * preview format 3 is used for media type 2, and so on.
		 * 
		 * We have to use the media type later
		 */
		int currentMediaType = currentFormat >= 2 ? currentFormat - 1 : 0;
		
		// Get media type of current select preview format
		Mediatype mt = mediaTypes.get(currentMediaType);
		
		// Check, that mailing has this media type and media type is used
		if(mt != null && mt.getStatus() == Mediatype.STATUS_ACTIVE)
			return currentFormat;  // If so, keep this format as active
		
		Collections.sort(orderedTypeCodes);
		for(int code : orderedTypeCodes) {
			if(mediaTypes.get(code).getStatus() == Mediatype.STATUS_ACTIVE) {
				/*
				 * Here, we have to do same mapping as above, but reverse now:
				 * 
				 * Media type 0 maps to preview format 0, 
				 * media type 1 maps to preview format 2,
				 * media type 2 maps to preview format 3,
				 * and so on
				 */
				if(code == 0) {
					return code;
				} else {
					return code + 1;
				}
			}
		}
		
		return UNDEFINED_PREVIEW_FORMAT;
	}
	
}
