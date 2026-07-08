/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import java.util.Objects;

import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.TrackableLink;
import com.agnitas.emm.core.components.entity.ComponentModel;
import com.agnitas.emm.core.components.service.ComponentService;
import com.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.emm.springws.exception.MailingNotEditableException;
import org.springframework.stereotype.Component;

/**
 * Auxiliary class for webservices to check, that a mailing is editable.
 */
@Component
public class MailingEditableCheck {
	
	/** Rules to determine whether mailing is editable or not. */
	private final MailingPropertiesRules mailingPropertiesRules;

	private final ComponentService componentService;
	private final DynamicTagContentService dynamicTagContentService;
    private final TrackableLinkService trackableLinkService;

	/**
	 * Creates a new instance.
	 * 
	 * @param rules rules to determine whether mailing is editable or not
	 * 
	 * @throws NullPointerException if one of the arguments is <code>null</code> 
	 */
	public MailingEditableCheck(
			MailingPropertiesRules rules,
			ComponentService componentService,
			DynamicTagContentService dynamicTagContentServices,
			TrackableLinkService trackableLinkService
	) {
		this.mailingPropertiesRules = Objects.requireNonNull(rules);
		this.componentService = Objects.requireNonNull(componentService);
		this.dynamicTagContentService = Objects.requireNonNull(dynamicTagContentServices);
		this.trackableLinkService = Objects.requireNonNull(trackableLinkService);
	}
	
	/**
	 * Requires the given mailing to be editable.
	 * If mailing is not editable a {@link MailingNotEditableException} is thrown.
	 *  
	 * @param mailingId ID of mailing
	 * @param companyId company ID of mailing
	 * 
	 * @throws MailingNotEditableException if given mailing is not editable
	 */
	public void requireMailingEditable(int mailingId, int companyId) {
		doCheck(mailingId, companyId);
	}
	
	public void requireMailingForComponentEditable(int componentId, int companyId) {
		final ComponentModel model = new ComponentModel();
		model.setCompanyId(companyId);
		model.setComponentId(componentId);
		
		final MailingComponent component = this.componentService.getComponent(model);
		
		if(component != null) {
			doCheck(component.getMailingID(), component.getCompanyID());
		}
	}

	public void requireMailingForContentBlockEditable(int contentId, int companyId) {
		final DynamicTagContent content = this.dynamicTagContentService.getContent(companyId, contentId);
		
		if(content != null) {
			doCheck(content.getMailingID(), content.getCompanyID());
		}
	}
	
	public void requireMailingForTrackableLinkEditable(int urlId, int companyId) {
		final TrackableLink link = this.trackableLinkService.getTrackableLink(companyId, urlId);
		
		if(link != null) {
			doCheck(link.getMailingID(), link.getCompanyID());
		}
	}

	private void doCheck(int mailingId, int companyId) {
		if(mailingPropertiesRules.mailingIsWorldSentOrActive(mailingId, companyId)) {
			throw new MailingNotEditableException();
		}
	}
}
