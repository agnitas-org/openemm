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
import org.agnitas.emm.core.component.service.ComponentModel;
import com.agnitas.emm.core.components.service.ComponentService;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import com.agnitas.emm.springws.exception.MailingNotEditableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.agnitas.beans.TrackableLink;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;

/**
 * Auxiliary class for webservices to check, that a mailing is editable.
 */

@Component("MailingEditableCheck")
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
	public MailingEditableCheck(final MailingPropertiesRules rules, @Qualifier("componentService") final ComponentService componentService, final DynamicTagContentService dynamicTagContentServices, final TrackableLinkService trackableLinkService) {
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
	public void requireMailingEditable(final int mailingId, final int companyId) throws MailingNotEditableException {
		doCheck(mailingId, companyId);
	}
	
	public void requireMailingForComponentEditable(final int componentId, final int companyId) throws MailingNotEditableException {
		final ComponentModel model = new ComponentModel();
		model.setCompanyId(companyId);
		model.setComponentId(componentId);
		
		final MailingComponent component = this.componentService.getComponent(model);
		
		if(component != null) {
			doCheck(component.getMailingID(), component.getCompanyID());
		}
	}

	public void requireMailingForContentBlockEditable(final int contentId, final int companyId) throws MailingNotEditableException {
		final DynamicTagContent content = this.dynamicTagContentService.getContent(companyId, contentId);
		
		if(content != null) {
			doCheck(content.getMailingID(), content.getCompanyID());
		}
	}
	
	public void requireMailingForTrackableLinkEditable(final int urlId, final int companyId) throws MailingNotEditableException {
		final TrackableLink link = this.trackableLinkService.getTrackableLink(companyId, urlId);
		
		if(link != null) {
			doCheck(link.getMailingID(), link.getCompanyID());
		}
	}

	private void doCheck(final int mailingId, final int companyId) throws MailingNotEditableException {
		if(mailingPropertiesRules.mailingIsWorldSentOrActive(mailingId, companyId)) {
			throw new MailingNotEditableException();
		}
	}
}
