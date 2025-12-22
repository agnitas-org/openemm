/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.velocity.emmapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

public final class VelocityMailingWrapper implements VelocityMailing {
	
	private static final Logger LOGGER = LogManager.getLogger(VelocityMailingWrapper.class);
	
	private final Mailing mailing;
	private final SendActionbasedMailingService sendActionbasedMailingService;
	
	public VelocityMailingWrapper(final Mailing mailing, final SendActionbasedMailingService sendActionbasedMailingService) {
		this.mailing = Objects.requireNonNull(mailing, "mailing is null");
		this.sendActionbasedMailingService = Objects.requireNonNull(sendActionbasedMailingService, "sendActionbasedMailingService is null");
	}

	@Override
	public final int getId() {
		return this.mailing.getId();
	}

	@Override
	public final void setId(final int id) {
		this.mailing.setId(id);
	}

	@Override
	public final int getMailingID() {
		return this.mailing.getId();
	}

	@Override
	public final void setMailingID(final int id) {
		this.mailing.setId(id);
	}

	@Override
	public final int getMailinglistID() {
		return this.mailing.getMailinglistID();
	}

	@Override
	public final void setMailinglistID(final int id) {
		this.mailing.setMailinglistID(id);
	}

	@Override
	public final String getShortname() {
		return this.mailing.getShortname();
	}

	@Override
	public final void setShortname(final String shortname) {
		this.mailing.setShortname(shortname);
	}

	@Override
	public final int getMailingType() {
		return mailing.getMailingType().getCode();
	}

	public final void setMailingType(final MailingType mailingType) {
		this.mailing.setMailingType(mailingType);
	}

	@Override
	public final void setMailingType(final int mailingTypeCode) {
		this.mailing.setMailingType(MailingType.getByCode(mailingTypeCode));
	}

	@Override
	public final DynamicTag getDynamicTagById(final int dynId) {
		return this.mailing.getDynamicTagById(dynId);
	}

	@Override
	public final Map<String, DynamicTag> getDynTags() {
		return this.mailing.getDynTags();
	}

	@Override
	public final boolean sendEventMailing(final int customerID, final int delayMinutes, final String userStatus, final Map<String, String> overwrite, final ApplicationContext con) {
		final List<Integer> userStatusList = new ArrayList<>();
		userStatusList.add(Integer.valueOf(userStatus));
		
		return sendEventMailing(customerID, delayMinutes, userStatusList, overwrite, con);
	}

	@Override
	public final boolean sendEventMailing(final int customerID, final int delayMinutes, final List<Integer> userStatusList, final Map<String, String> overwrite, final ApplicationContext con) {
		final List<UserStatus> statusList = new ArrayList<>();
		
		final MailgunOptions options = MailgunOptions.from(overwrite);
		
		if(userStatusList != null) {
			for(final int statusCode : userStatusList) {
				if (UserStatus.existsWithId(statusCode)) {
					statusList.add(UserStatus.getByCode(statusCode));
				} else {
					LOGGER.error("Skipping unknown user status code {}", statusCode);
				}
			}
		
			options.withAllowedUserStatus(statusList);
		}
		
		try {
			this.sendActionbasedMailingService.sendActionbasedMailing(
					this.mailing.getCompanyID(),
					this.mailing.getId(),
					customerID,
					delayMinutes,
					options);
			
			return true;
		} catch(final Exception e) {
			return false;
		}
	}

}
