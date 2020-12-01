/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import com.agnitas.emm.core.userform.util.WebFormUtils;
import org.agnitas.beans.Recipient;
import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.mailing.exception.UnknownMailingIdException;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.userforms.UserformService;
import org.agnitas.exceptions.FormNotFoundException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComCompany;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.mailing.service.FullviewException;
import com.agnitas.emm.core.mailing.service.FullviewService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.recipient.UnknownRecipientIdException;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;
import com.agnitas.userform.bean.UserForm;

public final class FullviewServiceImpl implements FullviewService {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(FullviewServiceImpl.class);
	
	private CompanyService companyService;
	private UserformService formService;
	private MailingService mailingService;
	private ConfigService configService; 
	private RecipientService recipientService;
	
	private ExtensibleUIDService uidService;
	
	@Override
	public final String getFullviewUrl(final int companyID, final int mailingID, final int customerID, final String formNameOrNull) throws UnknownCompanyIdException, UnknownMailingIdException, UnknownRecipientIdException, FormNotFoundException, FullviewException {
		if(logger.isInfoEnabled()) {
			logger.info(String.format("Creating personalized fullview URL (company ID %d, mailing ID %d, customer ID %d)", companyID, mailingID, customerID));
		}
		
		final ComCompany company = this.companyService.getCompany(companyID);
		final MailingModel mailingModel = new MailingModel();
		mailingModel.setCompanyId(companyID);
		mailingModel.setMailingId(mailingID);
		
		this.mailingService.getMailing(mailingModel);	// Simply check, if mailing exists

		final String formName = formNameOrNull == null ? configService.getFullviewFormName(companyID) : formNameOrNull;
		
		final UserForm form = this.formService.getUserForm(companyID, formName);
		final int licenseID = this.configService.getLicenseID();
		
		final RecipientModel recipientModel = new RecipientModel();
		recipientModel.setCompanyId(companyID);
		recipientModel.setCustomerId(customerID);
		final Recipient recipient = this.recipientService.getRecipient(recipientModel);
		
		final ComExtensibleUID uid = UIDFactory.from(licenseID, recipient, mailingID);
		
		try {
			final String uidString = uidService.buildUIDString(uid);

			final String urlEncodedFormName = URLEncoder.encode(form.getFormName(), "UTF-8");
			final String urlEncodedUID = URLEncoder.encode(uidString, "UTF-8");

			return WebFormUtils.getFormFullViewLink(company.getRdirDomain(), companyID, urlEncodedFormName, urlEncodedUID);
		} catch(final RequiredInformationMissingException | UIDStringBuilderException e) {
			throw new FullviewException("Cannot build UID", e);
		} catch(final UnsupportedEncodingException e) {
			throw new FullviewException("Cannot url-encode data", e);
		}
	}
	
	@Required
	public final void setCompanyService(final CompanyService service) {
		this.companyService = Objects.requireNonNull(service, "Company service cannot be null");
	}
	
	@Required
	public final void setUserFormService(final UserformService service) {
		this.formService = Objects.requireNonNull(service, "User form service cannot be null");
	}
	
	@Required
	public final void setMailingService(final MailingService service) {
		this.mailingService = Objects.requireNonNull(service, "Mailing service cannot be null");
	}
	
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}
	
	@Required
	public final void setRecipientService(final RecipientService service) {
		this.recipientService = Objects.requireNonNull(service, "Recipient service cannot be null");
	}
	
	@Required
	public final void setExtensibleUidService(final ExtensibleUIDService service) {
		this.uidService = Objects.requireNonNull(service, "Extensible UID service cannot be null");
	}
}
