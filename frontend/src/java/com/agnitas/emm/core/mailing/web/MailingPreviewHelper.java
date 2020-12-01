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
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mediatype;
import org.agnitas.emm.core.mediatypes.factory.MediatypeFactory;
import org.agnitas.preview.Preview;

import com.agnitas.beans.ComMailing;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.web.PreviewForm;
import com.agnitas.web.ShowImageServlet;

/**
 * Helper class to find the proper preselected preview format.
 */
public class MailingPreviewHelper {

	/** Indicates, that the algorithm was not able to detect proper preview format. */
	public static final int UNDEFINED_PREVIEW_FORMAT = -1;
	
	public static final int INPUT_TYPE_TEXT = 0;
    public static final int INPUT_TYPE_HTML = 1;
	
	/**
	 * Updated preview format in preview form.
	 * @param previewForm PreviewForm
	 * @param request PreviewForm
	 * @param mailingId mailing ID
	 * @param companyID company ID
	 * @param dao DAO for accessing mailings
	 */
	public static void updateActiveMailingPreviewFormat(PreviewForm previewForm, HttpServletRequest request, int mailingId, int companyID, ComMailingDao dao) {
		int activeFormat = computeActivePreviewFormat(previewForm.getFormat(), mailingId, companyID, dao);
		
		previewForm.setFormat(activeFormat);
		
		request.setAttribute("PREVIEW_FORMAT", activeFormat);
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
		ComMailing mailing = dao.getMailing(mailingID, companyID);
		
		// No mailing found? 
		if(mailing.getId() == 0) {
			return UNDEFINED_PREVIEW_FORMAT;
		}
		
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
		if(mt != null && mt.getStatus() == Mediatype.STATUS_ACTIVE) {
			return currentFormat;  // If so, keep this format as active
		}
		
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
	
	public static MediaTypes castPreviewFormatToMediaType(int previewFormat, MediatypeFactory mediatypeFactory) {
		int mediaTypeCode = previewFormat == INPUT_TYPE_TEXT ? INPUT_TYPE_TEXT : previewFormat - 1;
		if (mediatypeFactory.isTypeSupported(mediaTypeCode)) {
			Mediatype mediatype = mediatypeFactory.create(mediaTypeCode);
			return mediatype.getMediaType();
		}
		return null;
	}
	
	/**
     * Replacing the standard images with mobile images if present
     * @param components - mailing dynamic components
     * @param previewSize - size of preview choosen by user
     * @param previewAsString - mailing preview  @return - resulting mailing preview.
     */
    public static String replaceImagesWithMobileComponents(Map<String, MailingComponent> components, int previewSize, String previewAsString) {
    	Preview.Size screenSize = Preview.Size.getSizeById(previewSize);
        if (screenSize == Preview.Size.MOBILE_PORTRAIT || screenSize == Preview.Size.MOBILE_LANDSCAPE) {
            final Set<Map.Entry<String, MailingComponent>> componentEntries = components.entrySet();
            for (Map.Entry<String, MailingComponent> component : componentEntries) {
                int componentType = component.getValue().getType();
                if (componentType == MailingComponent.TYPE_HOSTED_IMAGE ||
                        componentType == MailingComponent.TYPE_IMAGE) {
                    final String componentName = component.getKey();
                    final String replacementName = ShowImageServlet.MOBILE_IMAGE_PREFIX + componentName;
                    final MailingComponent replacementComponent = components.get(replacementName);
                    if (replacementComponent != null &&
                            (replacementComponent.getType() == MailingComponent.TYPE_HOSTED_IMAGE ||
									replacementComponent.getType() == MailingComponent.TYPE_IMAGE)){
                        previewAsString = previewAsString.replaceAll(componentName, replacementName);
                    }
                }
            }
        }
        return previewAsString;
    }
}
