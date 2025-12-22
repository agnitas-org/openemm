/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.util.Objects;
import java.util.Optional;

import com.agnitas.beans.Company;
import com.agnitas.beans.Recipient;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import com.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.mailing.service.FullviewException;
import com.agnitas.emm.core.mailing.service.FullviewService;
import com.agnitas.emm.core.mailing.service.MailingModel;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.recipient.service.RecipientModel;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.emm.core.userform.web.WebFormUrlBuilder;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.emm.core.commons.util.ConfigService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FullviewServiceImpl implements FullviewService {

	private static final Logger logger = LogManager.getLogger(FullviewServiceImpl.class);
	
	private CompanyService companyService;
	private UserformService formService;
	private MailingService mailingService;
	private ConfigService configService; 
	private RecipientService recipientService;
	
	private ExtensibleUIDService uidService;
	private CompanyTokenService companyTokenService;
	
	@Override
	public String getFullviewUrl(int companyID, int mailingID, int customerID, String formNameOrNull) throws FullviewException {
		if(logger.isInfoEnabled()) {
			logger.info(String.format("Creating personalized fullview URL (company ID %d, mailing ID %d, customer ID %d)", companyID, mailingID, customerID));
		}
		
		final Company company = this.companyService.getCompany(companyID);
		final Optional<String> companyToken = this.companyTokenService.getCompanyToken(companyID);
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
		
		final ExtensibleUID uid = UIDFactory.from(licenseID, recipient, mailingID);
		
		try {
			final String uidString = uidService.buildUIDString(uid);
			
			return WebFormUrlBuilder.from(company, form.getFormName())
					.withCompanyToken(companyToken)
					.withResolvedUID(true)
					.withUID(uidString)
					.build();
		} catch(final RequiredInformationMissingException | UIDStringBuilderException e) {
			throw new FullviewException("Cannot build UID", e);
		}
	}
	
	public final void setCompanyService(final CompanyService service) {
		this.companyService = Objects.requireNonNull(service, "Company service cannot be null");
	}
	
	public final void setUserFormService(final UserformService service) {
		this.formService = Objects.requireNonNull(service, "User form service cannot be null");
	}
	
	public final void setMailingService(final MailingService service) {
		this.mailingService = Objects.requireNonNull(service, "Mailing service cannot be null");
	}
	
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}
	
	public final void setRecipientService(final RecipientService service) {
		this.recipientService = Objects.requireNonNull(service, "Recipient service cannot be null");
	}
	
	public final void setExtensibleUidService(final ExtensibleUIDService service) {
		this.uidService = Objects.requireNonNull(service, "Extensible UID service cannot be null");
	}
	
	public final void setCompanyTokenService(final CompanyTokenService service) {
		this.companyTokenService = Objects.requireNonNull(service, "CompanyTokenService is null");
	}
}
