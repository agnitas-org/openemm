/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.agnitas.beans.BaseTrackableLink;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.trackablelinks.dao.FormTrackableLinkDao;
import com.agnitas.emm.core.trackablelinks.dto.BaseTrackableLinkDto;
import com.agnitas.emm.core.trackablelinks.dto.FormTrackableLinkDto;
import com.agnitas.emm.core.trackablelinks.service.FormTrackableLinkService;
import com.agnitas.emm.core.trackablelinks.web.LinkScanResultToMessage;
import com.agnitas.emm.core.userform.dto.ResultSettings;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;
import com.agnitas.userform.trackablelinks.bean.impl.ComTrackableUserFormLinkImpl;
import com.agnitas.util.LinkUtils;


public class FormTrackableLinkServiceImpl implements FormTrackableLinkService {

	private static final transient Logger logger = LogManager.getLogger(FormTrackableLinkServiceImpl.class);

	private FormTrackableLinkDao trackableLinkDao;
	private LinkService linkService;
	private ExtendedConversionService conversionService;

	@Override
	public void saveTrackableLinks(Admin admin, UserFormDto userFormDto, List<Message> errors, final List<Message> warnings) {
		int companyId = admin.getCompanyID();
		int userFormId = userFormDto.getId();

        try {
			if (userFormId > 0) {
				List<ComTrackableUserFormLink> trackableLinks = getValidTrackableLinks(admin, userFormId, userFormDto, errors, warnings);
				if (!trackableLinkDao.existsDummyFormLink(companyId, userFormId)) {
					ComTrackableUserFormLink dummyStatisticLinks = new ComTrackableUserFormLinkImpl();
					dummyStatisticLinks.setFormID(userFormId);
					dummyStatisticLinks.setCompanyID(companyId);
					dummyStatisticLinks.setFullUrl("Form");
					trackableLinks.add(dummyStatisticLinks);
				}
				trackableLinkDao.saveUserFormTrackableLinks(userFormId, companyId, trackableLinks);
			}
		} catch (Exception e) {
			logger.error("User form trackable links could not save", e);
			errors.add(Message.of("Error"));
		}
	}

	@Override
	public List<FormTrackableLinkDto> getFormTrackableLinks(Admin admin, int formId) {
		List<ComTrackableUserFormLink> formTrackableLinks = trackableLinkDao.getUserFormTrackableLinkList(formId, admin.getCompanyID());
		return conversionService.convert(formTrackableLinks, ComTrackableUserFormLink.class, FormTrackableLinkDto.class);
	}

	@Override
	public void bulkUpdateTrackableLinks(Admin admin, int formId, List<FormTrackableLinkDto> links, int trackable, List<LinkProperty> commonExtensions) {
		bulkUpdateTrackableLinks(admin, formId, links, trackable, true, commonExtensions);
	}

	@Override
	public void bulkUpdateTrackableLinksExtensions(Admin admin, int formId, List<LinkProperty> commonExtensions) {
		bulkUpdateTrackableLinks(admin, formId, Collections.emptyList(), LinkUtils.KEEP_UNCHANGED, true, commonExtensions);
	}

	@Override
	public void bulkUpdateTrackableLinksUsage(Admin admin, int formId, int trackable) {
		if (trackable > LinkUtils.KEEP_UNCHANGED) {
			bulkUpdateTrackableLinks(admin, formId, Collections.emptyList(), trackable, false, Collections.emptyList());
		}
	}

	private void bulkUpdateTrackableLinks(Admin admin, int formId, List<FormTrackableLinkDto> links, int trackable, boolean updateProperties, List<LinkProperty> commonExtensions) {
		List<ComTrackableUserFormLink> formTrackableLinks = trackableLinkDao.getUserFormTrackableLinkList(formId, admin.getCompanyID());
		List<LinkProperty> commonProperties = getFormTrackableLinkCommonExtensions(admin, formId);

		Map<Integer, FormTrackableLinkDto> linkMap = links.stream().collect(Collectors.toMap(BaseTrackableLinkDto::getId, Function.identity()));
		for (ComTrackableUserFormLink trackableLink : formTrackableLinks) {
			int id = trackableLink.getId();
			FormTrackableLinkDto linkDto = linkMap.get(id);
			if (linkDto != null) {
				trackableLink.setShortname(linkDto.getShortname());
				trackableLink.setUsage(linkDto.getTrackable());
			}

			if (trackable > LinkUtils.KEEP_UNCHANGED) {
				trackableLink.setUsage(trackable);
			}

			if (updateProperties && admin.permissionAllowed(Permission.MAILING_EXTEND_TRACKABLE_LINKS)) {
				Set<LinkProperty> propertiesSet = new HashSet<>(trackableLink.getProperties());
				propertiesSet.removeAll(commonProperties);
				propertiesSet.addAll(commonExtensions);
				trackableLink.setProperties(new ArrayList<>(propertiesSet));
			}
		}

		trackableLinkDao.saveUserFormTrackableLinks(formId, admin.getCompanyID(), formTrackableLinks);
	}

	@Override
	public List<LinkProperty> getFormTrackableLinkCommonExtensions(Admin admin, int formId) {
		if (!admin.permissionAllowed(Permission.MAILING_EXTEND_TRACKABLE_LINKS)) {
			return new ArrayList<>();
		}

		try {
			List<ComTrackableUserFormLink> links = trackableLinkDao.getUserFormTrackableLinkList(formId, admin.getCompanyID());
			Map<Integer, List<LinkProperty>> linksMap = links.stream().map(link -> new Tuple<>(link.getId(), link.getProperties())).collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
			return LinkUtils.collectCommonExtensions(linksMap);
		} catch (Exception e) {
			logger.error("Could not obtain common extensions!");
		}
		return new ArrayList<>();
	}

	@Override
	public FormTrackableLinkDto getFormTrackableLink(Admin admin, int formId, int linkId) {
		ComTrackableUserFormLink userFormTrackableLink = trackableLinkDao.getUserFormTrackableLink(admin.getCompanyID(), formId, linkId);
		if (userFormTrackableLink == null) {
			return null;
		}

		return conversionService.convert(userFormTrackableLink, FormTrackableLinkDto.class);
	}

	@Override
	public boolean updateTrackableLink(Admin admin, int formId, FormTrackableLinkDto trackableLinkDto) {
		if (trackableLinkDto == null || formId < 0) {
			return false;
		}

		ComTrackableUserFormLink userFormLink = trackableLinkDao.getUserFormTrackableLink(admin.getCompanyID(), formId, trackableLinkDto.getId());
		if (userFormLink == null) {
			//don't create new link because all links are obtained while saving user form
			return false;
		}

		//don't change full url
		userFormLink.setId(trackableLinkDto.getId());
		userFormLink.setShortname(trackableLinkDto.getShortname());
		userFormLink.setUsage(trackableLinkDto.getTrackable());
		userFormLink.setProperties(trackableLinkDto.getProperties());

		trackableLinkDao.saveUserFormTrackableLink(formId, admin.getCompanyID(), userFormLink);
		return true;
	}

	private List<ComTrackableUserFormLink> getValidTrackableLinks(Admin admin, int userFormId, UserFormDto userFormDto, List<Message> errors, final List<Message> warnings)
            throws Exception {
        int companyId = admin.getCompanyID();
        LinkService.LinkScanResult successSettingsLinkResult = validateLinks(userFormDto.getSuccessSettings(), admin, errors, warnings);
        LinkService.LinkScanResult errorSettingsLinkResult = validateLinks(userFormDto.getErrorSettings(), admin, errors, warnings);

        Map<String, ComTrackableUserFormLink> existingLinks = trackableLinkDao.getUserFormTrackableLinks(userFormId, companyId);

        List<ComTrackableLink> trackableLinks = successSettingsLinkResult.getTrackableLinks();
        trackableLinks.addAll(errorSettingsLinkResult.getTrackableLinks());

        List<LinkProperty> defaultExtensions = linkService.getDefaultExtensions(companyId);

        //collect objects with new links
        List<ComTrackableUserFormLink> userFormLinks = new ArrayList<>();
        for(ComTrackableLink link: trackableLinks) {
            String url = link.getFullUrl();
            ComTrackableUserFormLink trackableLink = existingLinks.get(url);
            if (trackableLink == null) {
                trackableLink = new ComTrackableUserFormLinkImpl();
                trackableLink.setCompanyID(companyId);
                trackableLink.setFormID(userFormId);
                trackableLink.setUsage(BaseTrackableLink.TRACKABLE_NO);
                trackableLink.setShortname("");
                trackableLink.setFullUrl(url);
            }
            Set<LinkProperty> properties = new HashSet<>(trackableLink.getProperties());
            properties.addAll(defaultExtensions);
            trackableLink.setProperties(new ArrayList<>(properties));

            userFormLinks.add(trackableLink);
        }
        return userFormLinks;
    }

    private LinkService.LinkScanResult validateLinks(ResultSettings settings, Admin admin, List<Message> errors, List<Message> warnings) throws Exception {
		String type = settings.isSuccess() ? "SUCCESS" : "ERROR";
		LinkService.LinkScanResult links = linkService.scanForLinks(settings.getTemplate(), admin.getCompanyID());


		List<String> notTrackableLinks = links.getNotTrackableLinks();
		if (CollectionUtils.isNotEmpty(notTrackableLinks)) {
			errors.add(new Message("warning.mailing.link.agntag",
					new Object[]{ type, StringEscapeUtils.escapeHtml4(notTrackableLinks.get(0)) }));
		}

		List<LinkService.ErroneousLink> erroneousLinks = links.getErroneousLinks();
		if (CollectionUtils.isNotEmpty(erroneousLinks)) {
			LinkService.ErroneousLink firstErroneousLink = erroneousLinks.get(0);
			errors.add(new Message("error.mailing.links",
					new Object[]{ erroneousLinks.size(), type,
							StringEscapeUtils.escapeHtml4(firstErroneousLink.getLinkText()),
							I18nString.getLocaleString(firstErroneousLink.getErrorMessageKey(), admin.getLocale())
					}));
		}

		LinkScanResultToMessage.linkWarningsToMessage(links, warnings);
		
		return links;
	}

	@Required
	public void setTrackableLinkDao(FormTrackableLinkDao trackableLinkDao) {
		this.trackableLinkDao = trackableLinkDao;
	}

	@Required
	public void setLinkService(LinkService linkService) {
		this.linkService = linkService;
	}

	@Required
	public void setConversionService(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}
}
