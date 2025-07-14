/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.beans.TrackableLinkModel;
import com.agnitas.beans.TrackableLinkSettings;
import com.agnitas.beans.impl.MailingImpl;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.TrackableLinkDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.trackablelinks.exceptions.MailingNotSentException;
import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkException;
import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkUnknownLinkIdException;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.web.exception.ClearLinkExtensionsException;
import jakarta.annotation.Resource;
import com.agnitas.beans.BaseTrackableLink;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import com.agnitas.emm.core.mediatypes.dao.MediatypesDaoException;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Service class dealing with trackable links.
 */
public class TrackableLinkServiceImpl implements TrackableLinkService {

    public static final String LINK_SWYN_PREFIX = "SWYN: ";

    private static final Logger logger = LogManager.getLogger(TrackableLinkServiceImpl.class);

    /** DAO for accessing trackable links. */
    private TrackableLinkDao trackableLinkDao;

    @Resource(name="MailingDao")
    protected MailingDao mailingDao;

    /** Service dealing with mailings. */
    private MailingService mailingService;
    private ConfigService configService;

    @Override
    public void addExtensions(Mailing aMailing, Set<Integer> bulkIds, List<LinkProperty> extensions, List<UserAction> userActions) {
        Collection<TrackableLink> bulkLinks = getBulkLinks(aMailing.getTrackableLinks().values(), bulkIds);
        List<LinkProperty> commonExtensions = getCommonExtensions(aMailing.getId(), aMailing.getCompanyID(), bulkIds);

        for (TrackableLink link : bulkLinks) {
            Set<LinkProperty> extensionsToSet = new HashSet<>(link.getProperties());
            extensionsToSet.removeAll(commonExtensions);
            extensionsToSet.addAll(extensions);
            link.setProperties(new ArrayList<>(extensionsToSet));
            userActionLogMailingLinksCreated(userActions, aMailing, extensions, commonExtensions, link);
        }
    }

    private Collection<TrackableLink> getBulkLinks(Collection<TrackableLink> allLinks, Set<Integer> bulkIds) {
        return allLinks
                .stream().filter(l -> bulkIds.contains(l.getId()) && (l.getShortname() == null
                        || !l.getShortname().startsWith(MailingImpl.LINK_SWYN_PREFIX)))
                .collect(Collectors.toList());
    }

    @Override
    public void removeLegacyMailingLinkExtension(Mailing aMailing, Set<Integer> bulkLinkIds) {
        for (TrackableLink link : aMailing.getTrackableLinks().values()) {
            if (bulkLinkIds.contains(link.getId())) {
                link.setExtendByMailingExtensions(false);
            }
        }
    }

    @Override
    public Map<Integer, String> getMailingLinks(int mailingId, int companyId) {
        List<TrackableLink> trackableLinks = trackableLinkDao.getTrackableLinks(companyId, mailingId);

        //productive links (A-Z), SWYN links (A-Z), administrative links (A-Z)"
        Comparator<TrackableLink> byAdministrative = (l1, l2) -> Boolean.compare(l1.isAdminLink(), l2.isAdminLink());
        Comparator<TrackableLink> bySWYN = Comparator.comparing(this::isLinkSWYN);
        Comparator<TrackableLink> byUrl = Comparator.comparing(TrackableLink::getFullUrl);
        trackableLinks.sort(byAdministrative.thenComparing(bySWYN).thenComparing(byUrl));

        Map<Integer, String> resultMap = new LinkedHashMap<>();
        for (TrackableLink trackableLink : trackableLinks) {
            resultMap.put(trackableLink.getId(), trackableLink.getFullUrl());
        }
        return resultMap;
    }

    private Boolean isLinkSWYN(TrackableLink link) {
        return StringUtils.startsWith(link.getShortname(), "SWYN");
    }

    @Override
    public JSONObject getMailingLinksJson(int mailingId, int companyId) {
        Map<Integer, String> links = getMailingLinks(mailingId, companyId);
        JSONObject orderedLinks = new JSONObject();
        int index = 0;

        for (Map.Entry<Integer, String> entry : links.entrySet()) {
            JSONObject data = new JSONObject();

            data.put("id", entry.getKey());
            data.put("url", entry.getValue());

            orderedLinks.put(Integer.toString(index++), data);
        }
        return orderedLinks;
    }

    @Override
    public List<TrackableLinkListItem> getTrackableLinkItems(int mailingID, int companyId) {
        checkMailing(mailingID, companyId);
        return trackableLinkDao.listTrackableLinksForMailing(companyId, mailingID);
    }

    @Override
    public int saveTrackableLink(TrackableLink trackableLink) {
        return trackableLinkDao.saveTrackableLink(trackableLink);
    }

    @Override
    public TrackableLink getTrackableLink(int companyId, int linkId) {
        return trackableLinkDao.getTrackableLink(linkId, companyId);
    }

    @Override
    public TrackableLinkSettings getTrackableLinkSettings(int linkID, int companyId) {
        TrackableLink trackableLink = trackableLinkDao.getTrackableLink(linkID, companyId);
        if (trackableLink == null) {
            throw new TrackableLinkUnknownLinkIdException(linkID);
        }

        List<LinkProperty> linkProperties = trackableLinkDao.getLinkProperties(trackableLink);

        return new TrackableLinkSettings(trackableLink, linkProperties);
    }

    private void checkMailing(int mailingID, int companyID) {
        Mailing mailing = mailingDao.getMailing(mailingID, companyID);

        if (mailing == null || mailing.getId() == 0) {
            throw new MailingNotExistException(companyID, mailingID);
        }
    }

    @Override
	public void updateLinkTarget(TrackableLink link, String newUrl) throws TrackableLinkException {
        if(logger.isInfoEnabled()) {
            logger.info("Set url of link " + link.getId() + " to " + newUrl);
        }

        checkUrlEditingAllowed(link);

        // Set original URL is full URL has been changed and original URL currently not set.
        if(!newUrl.equals(link.getFullUrl()) && StringUtils.isEmpty(link.getOriginalUrl())) {
            link.setOriginalUrl(link.getFullUrl());
        }

        // Update URL and save changes
        link.setFullUrl(newUrl);

        trackableLinkDao.saveTrackableLink(link);
    }

    /**
     * Checks, if URL editing is allowed for given link.
     *
     * @param link link to check
     *
     * @throws TrackableLinkException if URL editing is not allowed
     */
    private void checkUrlEditingAllowed(TrackableLink link) throws TrackableLinkException {
    	checkUrlEditingAllowed(link.getMailingID(), link.getCompanyID());
    }

    /**
     * Checks, if URL editing is allowed for given mailing.
     *
     * @param mailingID mailing ID
     * @param companyID company ID of mailing
     *
     * @throws TrackableLinkException if URL editing is not allowed
     */
    private void checkUrlEditingAllowed(int mailingID, int companyID) throws TrackableLinkException {
		MailingModel model = new MailingModel();
		model.setMailingId(mailingID);
		model.setCompanyId(companyID);
		Mailing mailing = mailingService.getMailing(model);

		// Check, if mailing is world sent or activated
		if(!mailingService.isMailingWorldSent(mailing.getId(), mailing.getCompanyID()) && !mailingService.isActiveIntervalMailing(mailingID)) {
			if(logger.isInfoEnabled()) {
				logger.info("Mailing " + mailing.getId() + " has not been sent");
			}

			throw new MailingNotSentException(mailing.getId());
		}
    }

    private void userActionLogMailingLinksCreated(List<UserAction> userActions, Mailing aMailing, List<LinkProperty> passedLinkProperties, List<LinkProperty> commonLinkProperties, TrackableLink link) {
        userActionLogLinksCreated(aMailing.getId(), "edit mailing links", userActions, passedLinkProperties, commonLinkProperties, link);
    }

    private void userActionLogLinksCreated(int creatorId, String actionMessage, List<UserAction> userActions, List<LinkProperty> passedLinkProperties, List<LinkProperty> commonLinkProperties, TrackableLink link) {
        StringBuilder description = new StringBuilder();

        for (LinkProperty passedLinkProperty : passedLinkProperties) {
            if (!commonLinkProperties.contains(passedLinkProperty)) {
                description.append("ID = ")
                        .append(creatorId)
                        .append(". ")
                        .append("Trackable link ")
                        .append(link.getFullUrl())
                        .append(". Link extension ")
                        .append(passedLinkProperty.getPropertyName())
                        .append("=")
                        .append(passedLinkProperty.getPropertyValue())
                        .append(" created.");

                userActions.add(new UserAction(actionMessage, description.toString()));
                description.setLength(0);
            }
        }
    }

    @Override
    public boolean isUrlEditingAllowed(Admin admin, int mailingID) {
        if (!admin.permissionAllowed(Permission.MAILING_TRACKABLELINKS_URL_CHANGE)) {
            return false;
        }

        try {
            checkUrlEditingAllowed(mailingID, admin.getCompanyID());
            return true;
        } catch (TrackableLinkException | MailingNotExistException e) {
            return false;
        }
    }

    @Override
    public void bulkClearExtensions(final int mailingId, final int companyId, final Set<Integer> bulkIds)
            throws ClearLinkExtensionsException {
        trackableLinkDao.bulkClearExtensions(mailingId, companyId, bulkIds);
    }

    @Override
    public List<LinkProperty> getCommonExtensions(final int mailingId, final int companyId, final Set<Integer> bulkIds) {
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);
        return getCommonExtensions(mailing.getTrackableLinks().values(), bulkIds);
    }

    @Override
    public Mailing getMailingForLinksOverview(int mailingId, int companyId, boolean includeDeleted) throws MediatypesDaoException {
        Mailing mailing = mailingService.getMailing(mailingId, companyId, false);
        mailing.setMediatypes(mailingService.getMediatypes(mailingId, companyId));
        mailing.setTrackableLinks(trackableLinkDao.getTrackableLinksMap(mailingId, companyId, includeDeleted));

        if (configService.isAutoDeeptracking(companyId)) {
            mailing.getTrackableLinks().values().forEach(tl -> tl.setDeepTracking(BooleanUtils.toInteger(true)));
        }

        return mailing;
    }

    private List<LinkProperty> getCommonExtensions(final Collection<TrackableLink> links, final Set<Integer> bulkIds) {
        if (CollectionUtils.isEmpty(bulkIds)) {
            return new ArrayList<>();
        }
        return links.stream()
                .filter(trackableLink -> bulkIds.contains(trackableLink.getId()))
                .filter(trackableLink -> !trackableLink.isDeleted())
                .map(BaseTrackableLink::getProperties)
                .reduce((p1, p2) -> {
                    p1.retainAll(p2);
                    return p1;
                }).orElseGet(ArrayList::new);
    }

    @Override
    public void updateTrackableLinkSettings(TrackableLinkModel trackableLinkModel) {
        TrackableLink mergedTrackableLinkSettingsData = mergeTrackableLinkData(trackableLinkModel);

        trackableLinkDao.saveTrackableLink(mergedTrackableLinkSettingsData);
    }

    @Override
    public boolean isTrackingOnEveryPositionAvailable(int companyId, int mailingId) {
        return trackableLinkDao.isTrackingOnEveryPositionAvailable(companyId, mailingId);
    }

    private TrackableLink mergeTrackableLinkData(TrackableLinkModel trackableLinkModel) {
        TrackableLink trackableLink = trackableLinkDao.getTrackableLink(trackableLinkModel.getId(), trackableLinkModel.getCompanyID());

        if (trackableLink == null) {
            throw new TrackableLinkUnknownLinkIdException(trackableLinkModel.getId());
        }

        // Optional fields
        String fullUrl = getFullUrl(trackableLink, trackableLinkModel);
        int actionId = mergeFields(trackableLink.getActionID(), trackableLinkModel.getActionID());
        String shortname = mergeFields(trackableLink.getShortname(), trackableLinkModel.getShortname());
        int deepTracking = mergeFields(trackableLink.getDeepTracking(), trackableLinkModel.getDeepTracking());
        String altText = mergeFields(trackableLink.getAltText(), trackableLinkModel.getAltText());
        String originalUrl = getOriginalUrl(trackableLink, trackableLinkModel);
        boolean isAdminLink = mergeFields(trackableLink.isAdminLink(), trackableLinkModel.getAdminLink());
        int usage = mergeFields(trackableLink.getUsage(), trackableLinkModel.getUsage());
        List<LinkProperty> properties = mergeFields(trackableLink.getProperties(), trackableLinkModel.getLinkProperties());

        trackableLink.setFullUrl(fullUrl);
        trackableLink.setActionID(actionId);
        trackableLink.setShortname(shortname);
        trackableLink.setDeepTracking(deepTracking);
        trackableLink.setAltText(altText);
        trackableLink.setOriginalUrl(originalUrl);
        trackableLink.setAdminLink(isAdminLink);
        trackableLink.setUsage(usage);
        trackableLink.setProperties(properties);

        return trackableLink;
    }

    private <T> T mergeFields(T oldField, T newField) {
        if (newField == null) {
            return oldField;
        }
        return  newField;
    }

    private String getFullUrl( TrackableLink oldTrackableLink, TrackableLinkModel newTrackableLink ) {
        String newFullUrl = newTrackableLink.getFullUrl();
        String oldFullUrl = oldTrackableLink.getFullUrl();
        String oldOriginalUrl = oldTrackableLink.getOriginalUrl();

        if (newFullUrl != null) {
            if (StringUtils.isNotEmpty(newFullUrl.trim())) {
                return newFullUrl;
            }
            return oldOriginalUrl;
        }

        return oldFullUrl;
    }

    private String getOriginalUrl( TrackableLink oldTrackableLink, TrackableLinkModel newTrackableLink ) {
        String newFullUrl = newTrackableLink.getFullUrl();
        String oldOriginalUrl = oldTrackableLink.getOriginalUrl();

        if (newFullUrl != null) {
            if (StringUtils.isNotEmpty(newFullUrl.trim()) && oldOriginalUrl == null) {
                return newFullUrl;
            }

            return oldOriginalUrl;
        }

        return oldOriginalUrl;
    }

	public void setTrackableLinkDao(TrackableLinkDao trackableLinkDao) {
		this.trackableLinkDao = trackableLinkDao;
	}

	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
