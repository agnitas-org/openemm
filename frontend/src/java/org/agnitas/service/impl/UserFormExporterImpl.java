/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;

import com.agnitas.beans.LinkProperty;
import com.agnitas.dao.UserFormDao;
import com.agnitas.json.JsonWriter;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;
import org.agnitas.service.UserFormExporter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class UserFormExporterImpl extends ActionExporter implements UserFormExporter {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(UserFormExporterImpl.class);
	
	@Resource(name="UserFormDao")
	protected UserFormDao userFormDao;

	@Override
	public void exportUserFormToJson(int companyID, int formID, OutputStream output) throws Exception {
		UserForm userForm = userFormDao.getUserForm(formID, companyID);
		Set<Integer> actionIDs = new HashSet<>();
		
		try (JsonWriter writer = new JsonWriter(output, "UTF-8")) {
			writer.openJsonObject();
			
			writeJsonObjectAttribute(writer, "version", EXPORT_JSON_VERSION);
			
			writeJsonObjectAttribute(writer, "company_id", userForm.getCompanyID());
			
			writeJsonObjectAttribute(writer, "id", userForm.getId());
			
			writeJsonObjectAttributeWhenNotNullOrBlank(writer, "formname", userForm.getFormName());
			
			if (StringUtils.isNotBlank(userForm.getDescription())) {
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "description", userForm.getDescription());
			}
			
			writeJsonObjectAttribute(writer, "creation_date", userForm.getCreationDate());
			writeJsonObjectAttribute(writer, "change_date", userForm.getChangeDate());
			
			writeJsonObjectAttribute(writer, "active", userForm.isActive());
			
			if (userForm.getStartActionID() > 0) {
				writeJsonObjectAttribute(writer, "startaction", userForm.getStartActionID());
				actionIDs.add(userForm.getStartActionID());
			}
			if (userForm.getEndActionID() > 0) {
				writeJsonObjectAttribute(writer, "endaction", userForm.getEndActionID());
				actionIDs.add(userForm.getEndActionID());
			}

			writeJsonObjectAttribute(writer, "success_template", userForm.getSuccessTemplate());
			writeJsonObjectAttributeWhenNotNullOrBlank(writer, "success_mimetype", userForm.getSuccessMimetype());
			if (StringUtils.isNotBlank(userForm.getSuccessUrl())) {
				writeJsonObjectAttribute(writer, "success_url", userForm.getSuccessUrl());
				writeJsonObjectAttribute(writer, "success_use_url", userForm.isSuccessUseUrl());
			}

			writeJsonObjectAttribute(writer, "error_template", userForm.getErrorTemplate());
			writeJsonObjectAttributeWhenNotNullOrBlank(writer, "error_mimetype", userForm.getErrorMimetype());
			if (StringUtils.isNotBlank(userForm.getErrorUrl())) {
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "error_url", userForm.getErrorUrl());
				writeJsonObjectAttribute(writer, "error_use_url", userForm.isErrorUseUrl());
			}

			if (userForm.getTrackableLinks() != null && userForm.getTrackableLinks().size() > 0) {
				writer.openJsonObjectProperty("links");
				writer.openJsonArray();
				for (ComTrackableUserFormLink trackableLink : userForm.getTrackableLinks().values()) {
					writer.openJsonObject();
					
					writeJsonObjectAttribute(writer, "id", trackableLink.getId());
					writeJsonObjectAttributeWhenNotNullOrBlank(writer, "name", trackableLink.getShortname());
					writeJsonObjectAttributeWhenNotNullOrBlank(writer, "url", trackableLink.getFullUrl());

					if (trackableLink.getActionID() > 0) {
						writeJsonObjectAttribute(writer, "action_id", trackableLink.getActionID());
						actionIDs.add(trackableLink.getActionID());
					}
					
					if (trackableLink.getDeepTracking() > 0) {
						writeJsonObjectAttribute(writer, "deep_tracking", trackableLink.getDeepTracking());
					}
					
					if (trackableLink.getRelevance() > 0) {
						writeJsonObjectAttribute(writer, "relevance", trackableLink.getRelevance());
					}
					
					if (trackableLink.getUsage() > 0) {
						writeJsonObjectAttribute(writer, "usage", trackableLink.getUsage());
					}
					
					List<LinkProperty> linkProperties = trackableLink.getProperties();
					exportProperties(writer, linkProperties);
					
					writer.closeJsonObject();
				}
				writer.closeJsonArray();
			}
			
			exportActions(writer, companyID, actionIDs);
			
			
			writer.closeJsonObject();
		} catch (Exception e) {
			logger.error("Error in mailing export: " + e.getMessage(), e);
			throw e;
		}
	}
}
