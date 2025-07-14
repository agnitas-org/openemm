/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.maildrop.service;

import com.agnitas.beans.Company;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.DkimDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;
import com.agnitas.messages.I18nString;
import com.agnitas.emm.core.maildrop.dao.MaildropStatusDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.HtmlUtils;
import com.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class MaildropServiceImpl implements MaildropService {

	private static final Logger logger = LogManager.getLogger(MaildropServiceImpl.class);

	protected MailingService mailingService;
	protected MaildropStatusDao maildropStatusDao;
	private DkimDao dkimDao;
	private MediaTypesService mediaTypesService;
	private CompanyService companyService;
	private JavaMailService javaMailService;
	private ConfigService configService;
	private AdminService adminService;

	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}

	public void setDkimDao(DkimDao dkimDao) {
		this.dkimDao = dkimDao;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}

	public void setMediaTypesService(MediaTypesService mediaTypesService) {
		this.mediaTypesService = mediaTypesService;
	}

	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}

	public void setMaildropStatusDao(final MaildropStatusDao dao) {
		this.maildropStatusDao = dao;
	}

	@Override
	public final boolean stopWorldMailingBeforeGeneration(final int companyID, final int mailingID) {
		return maildropStatusDao.delete(companyID, mailingID, MaildropStatus.WORLD, MaildropGenerationStatus.SCHEDULED);
	}

	@Override
	public boolean isActiveMailing(final int mailingID, final int companyID) {
		if (hasMaildropStatus(mailingID, companyID, MaildropStatus.ACTION_BASED, MaildropStatus.DATE_BASED, MaildropStatus.WORLD)) {
			return true;
		}
		return mailingService.isActiveIntervalMailing(mailingID, companyID);
	}

	@Override
	public final boolean hasMaildropStatus(final int mailingID, final int companyID, final MaildropStatus... statusList) {
		return !findMaildrops(mailingID, companyID, statusList).isEmpty();
	}

	@Override
	public Optional<MaildropEntry> findMaildrop(int mailingId, int companyId, MaildropStatus... statuses) {
		return findMaildrops(mailingId, companyId, statuses).stream().findAny();
	}

	private List<MaildropEntry> findMaildrops(int mailingId, int companyId, MaildropStatus... statuses) {
		Collection<MaildropEntry> entries = maildropStatusDao.listMaildropStatus(mailingId, companyId);

		return entries.stream()
				.filter(m -> Stream.of(statuses).anyMatch(s -> s.getCode() == m.getStatus()))
				.toList();
	}

	@Override
	@Transactional
	public void selectTestRecipients(int companyId, int maildropStatusId, List<Integer> customerIds) {
		if (companyId > 0 && maildropStatusId > 0 && CollectionUtils.isNotEmpty(customerIds)) {
			if (maildropStatusDao.setSelectedTestRecipients(companyId, maildropStatusId, true)) {
				maildropStatusDao.setTestRecipients(maildropStatusId, customerIds);
			}
		}
	}

	@Override
	public void writeMailingSendStatisticsEntry(int companyID, int mailingID, MaildropStatus maildropStatus, MediaTypes mediaType, MailType mailType, int amount, int dataSize, Date sendDate, String mailerHostname) {
		maildropStatusDao.writeMailingSendStatisticsEntry(companyID, mailingID, maildropStatus, mediaType, mailType, amount, dataSize, sendDate, mailerHostname);
	}

	@Override
	public List<Integer> getMailingsSentBetween(int companyID, Date startDateIncluded, Date endDateExcluded) {
		return maildropStatusDao.getMailingsSentBetween(companyID, startDateIncluded, endDateExcluded);
	}

	@Override
	public MaildropEntry getEntryForStatus(int mailingID, int companyID, char status) {
		return maildropStatusDao.getEntryForStatus(mailingID, companyID, status);
	}

	@Override
	public int saveMaildropEntry(MaildropEntry entry) {
		int mailingID = entry.getMailingID();

		MailingType mailingType = mailingService.getMailingType(mailingID);

		if (mailingType == MailingType.ACTION_BASED || mailingType == MailingType.DATE_BASED) {
			MaildropEntry existingEntry = this.getEntryForStatus(mailingID, entry.getCompanyID(), entry.getStatus());
			if (existingEntry != null) {
				entry.setId(existingEntry.getId());
				logger.error("Trying to activate mailing multiple times: " + mailingID);
			}
		}

		final MaildropEntry existingEntry = entry.getId() > 0
				? getMaildropEntry(mailingID, entry.getCompanyID(), entry.getId())
				: null;

		if (existingEntry == null) {
			int id = maildropStatusDao.insertMaildropEntry(entry);
			checkMissingDkim(mailingID, entry.getCompanyID());

			return id;
		}

		maildropStatusDao.updateMaildropEntry(entry);
		return entry.getId();
	}

	@Override
	public void saveMaildropEntries(int companyId, int mailingId, Set<MaildropEntry> maildropStatusList) {
		List<Integer> existingMaildropIds = maildropStatusDao.getMaildropEntryIds(mailingId, companyId);

		List<MaildropEntry> update = new ArrayList<>();
		List<MaildropEntry> create = new ArrayList<>();

		maildropStatusList.forEach(entry -> {
			entry.setCompanyID(companyId);
			entry.setMailingID(mailingId);
			if (existingMaildropIds.contains(entry.getId())) {
				update.add(entry);
			} else {
				create.add(entry);
			}
		});

		maildropStatusDao.batchInsertMaildropEntries(companyId, mailingId, create);
		maildropStatusDao.batchUpdateMaildropEntries(companyId, mailingId, update);

		if (!create.isEmpty()) {
			checkMissingDkim(mailingId, companyId);
		}
	}

	private void checkMissingDkim(int mailingId, int companyId) {
		Mediatype activeMediaType = mediaTypesService.getActiveMediaType(companyId, mailingId);

		if (!(activeMediaType instanceof MediatypeEmail)) {
			return;
		}

		String senderDomain = AgnUtils.getDomainFromEmail(((MediatypeEmail) activeMediaType).getFromEmail());
		if (dkimDao.existsDkimKeyForDomain(companyId, senderDomain)) {
			return;
		}

		Locale locale = Optional.ofNullable(configService.getValue(ConfigValue.LocaleLanguage, companyId))
				.map(Locale::new)
				.orElse(Locale.UK);

		Company company = companyService.getCompany(companyId);

		AgnUtils.splitAndTrimList(StringUtils.defaultString(company.getContactTech()))
				.stream()
				.filter(StringUtils::isNotBlank)
				.forEach(techEmail -> {
					String salutationPart = Optional.ofNullable(adminService.findByEmail(techEmail, companyId))
							.map(u -> I18nString.getLocaleString("email.dkimKey.missing.salutation", locale, u.getFullname()))
							.orElse(I18nString.getLocaleString("email.dkimKey.missing.salutation.unknown", locale));

					String subject = I18nString.getLocaleString("email.dkimKey.missing.subject", locale, company.getShortname());
					String emailText = I18nString.getLocaleString("email.dkimKey.missing.text", locale, salutationPart, senderDomain);

					javaMailService.sendEmail(companyId, techEmail, subject, emailText, HtmlUtils.replaceLineFeedsForHTML(emailText));
				});
	}

	@Override
	public void cleanupOldEntriesByMailingID(int mailingID, int maximumAgeInDays) {
		maildropStatusDao.cleanupOldEntriesByMailingID(mailingID, maximumAgeInDays);
	}

	@Override
	public int cleanup(Collection<MaildropEntry> entries) {
		return maildropStatusDao.cleanup(entries);
	}

	@Override
	public Map<Integer, List<Integer>> cleanupFailedTestDeliveries() {
		return maildropStatusDao.cleanupFailedTestDeliveries();
	}

	@Override
	public MaildropEntry getMaildropEntry(int mailingId, int companyId, int statusId) {
		return maildropStatusDao.getMaildropEntry(mailingId, companyId, statusId);
	}

	@Override
	public int getLastMaildropEntryId(int mailingId, int companyId) {
		List<Integer> maildropEntryIds = maildropStatusDao.getMaildropEntryIds(mailingId, companyId);

		if (maildropEntryIds.isEmpty()) {
			return 0;
		}

		return maildropEntryIds.get(maildropEntryIds.size() - 1);
	}

	@Override
	public List<MaildropEntry> getMaildropStatusEntriesForMailing(int companyID, int mailingID) {
		return maildropStatusDao.getMaildropStatusEntriesForMailing(companyID, mailingID);
	}
}
