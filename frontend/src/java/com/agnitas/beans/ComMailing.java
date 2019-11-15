/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.Mailing;
import org.agnitas.preview.AgnTagError;
import org.springframework.context.ApplicationContext;

import com.agnitas.emm.core.mailing.bean.ComMailingParameter;

public interface ComMailing extends Mailing {
	enum MailingContentType {
		advertising,
		transaction;
		
		public static MailingContentType getFromString(String mailingContentTypeString) throws Exception {
			for (MailingContentType value : MailingContentType.values()) {
				if (value.name().equalsIgnoreCase(mailingContentTypeString)) {
					return value;
				}
			}
			throw new Exception("Invalid MailingContentType: " + mailingContentTypeString);
		}
	}
	
	String MAILINGPARAMETER_NAME_DOI_MAILING = "DOI-Mailing";

    int NONE_SPLIT_ID = 0;
    int YES_SPLIT_ID = -1;

	String NONE_SPLIT = "none";
	String YES_SPLIT = "yes";

    int getSplitID();
    void setSplitID(int splitID);

    Date getPlanDate();
    void setPlanDate(Date planDate);

    String getStatusmailRecipients();
    void setStatusmailRecipients(String statusmailRecipients);

    boolean isStatusmailOnErrorOnly();
    void setStatusmailOnErrorOnly(boolean statusmailOnErrorOnly);
    
    String getFollowUpType();
	void setFollowUpType(String followUpType);
	
	@Override
	boolean getUseDynamicTemplate();
	@Override
	void setUseDynamicTemplate( boolean useDynamicTemplate);

	/**
	 * Returns a list of link properties with are contained in all links of thi mailing.
	 * Link properties contained in a link but not in all the others are not contained in this list,
	 * but are contained in the link property list of the specific link additionally to the links of this list.
	 * 
	 * @return
	 */
	List<LinkProperty> getCommonLinkExtensions();
	
	List<ComMailingParameter> getParameters();

	void setParameters(List<ComMailingParameter> parameters);
	
	Map<String, List<AgnTagError>> checkAgnTagSyntax(ApplicationContext applicationContext) throws Exception;

	int getPreviewComponentId();
	void setPreviewComponentId(int previewComponentId);
	
	MailingContentType getMailingContentType();
	void setMailingContentType(MailingContentType mailingContentType);
	public Set<Integer> getAllReferencedTargetGroups();
}
