/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.service.impl;

import static org.agnitas.beans.BaseTrackableLink.KEEP_UNCHANGED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.agnitas.beans.BaseTrackableLink;
import org.agnitas.beans.TrackableLink;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.beans.TrackableLinkModel;
import com.agnitas.beans.TrackableLinkSettings;
import com.agnitas.beans.impl.MailingImpl;
import com.agnitas.dao.ComTrackableLinkDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.trackablelinks.exceptions.MailingNotSentException;
import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkException;
import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkUnknownLinkIdException;
import com.agnitas.emm.core.trackablelinks.service.ComTrackableLinkService;
import com.agnitas.web.exception.ClearLinkExtensionsException;

/**
 * Service class dealing with trackable links.
 */
public class ComTrackableLinkServiceImpl implements ComTrackableLinkService {

	/** The logger. */
    private static final transient Logger logger = Logger.getLogger(ComTrackableLinkServiceImpl.class);
    
    /** DAO for accessing trackable links. */
    private ComTrackableLinkDao trackableLinkDao;
    
    @Resource(name="MailingDao")
    protected MailingDao mailingDao;
    
    /** Service dealing with mailings. */
    private MailingService mailingService;

    @Override
    public void addExtensions(Mailing aMailing, Set<Integer> bulkIds, List<LinkProperty> extensions, List<UserAction> userActions) {
		Collection<ComTrackableLink> bulkLinks = aMailing.getTrackableLinks().values()
                .stream().filter(l -> bulkIds.contains(l.getId()) && (l.getShortname() == null 
                                || !l.getShortname().startsWith(MailingImpl.LINK_SWYN_PREFIX)))
                .collect(Collectors.toList());
        List<LinkProperty> commonExtensions = getCommonExtensions(aMailing.getId(), aMailing.getCompanyID(), bulkIds);

        for (ComTrackableLink link : bulkLinks) {
            Set<LinkProperty> extensionsToSet = new HashSet<>(link.getProperties());
            extensionsToSet.removeAll(commonExtensions);
            extensionsToSet.addAll(extensions);
            link.setProperties(new ArrayList<>(extensionsToSet));
            userActionLogLinksCreated(userActions, aMailing, extensions, commonExtensions, link);
        }
    }

    @Override
    public void removeLegacyMailingLinkExtension(Mailing aMailing, Set<Integer> bulkLinkIds) {
        for (ComTrackableLink link : aMailing.getTrackableLinks().values()) {
            if (bulkLinkIds.contains(link.getId())) {
                link.setExtendByMailingExtensions(false);
            }
        }
    }

    @Override
    public void setMailingLinkExtension(Mailing aMailing, String linkExtension) {
        ComTrackableLink aLink;
        for (TrackableLink link : aMailing.getTrackableLinks().values()) {
            aLink = (ComTrackableLink) link;
            aLink.setExtendByMailingExtensions(aLink.getProperties() != null && aLink.getProperties().size() > 0);
            aLink.setProperties(null);
        }
    }

    @Override
    public void setLegacyLinkExtensionMarker(Mailing aMailing, Map<Integer, Boolean> linksToExtends) {
        for (TrackableLink link : aMailing.getTrackableLinks().values()) {
            Boolean extendLinkByMailingLinkExtension = linksToExtends.get(link.getId());
            if (extendLinkByMailingLinkExtension != null) {
                ((ComTrackableLink) link).setExtendByMailingExtensions(extendLinkByMailingLinkExtension);
            }
        }
    }

    @Override
    public void setStandardDeeptracking(Mailing aMailing, Set<Integer> bulkLinkIds, int deepTracking, Map<Integer, Integer> getLinkItemsDeepTracking) {
        for (TrackableLink aLink : aMailing.getTrackableLinks().values()) {
            int id = aLink.getId();
            int linkItemDeepTracking = getLinkItemsDeepTracking.getOrDefault(id, 0);
            if (aLink.getDeepTracking() != linkItemDeepTracking) {
                aLink.setDeepTracking(linkItemDeepTracking);
            } else if ((deepTracking != KEEP_UNCHANGED) && bulkLinkIds.contains(id)) {
                aLink.setDeepTracking(deepTracking);
            }
        }
    }

    @Override
    public void setShortname(Mailing aMailing, Map<Integer, String> linkItemNames) {
        try {
            for (TrackableLink trackableLink : aMailing.getTrackableLinks().values()) {
                int id = trackableLink.getId();
                trackableLink.setShortname(StringUtils.defaultIfEmpty(linkItemNames.get(id), ""));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public List<TrackableLinkListItem> getMailingLinks(int mailingID, @VelocityCheck int companyId) {
        return trackableLinkDao.listTrackableLinksForMailing(companyId, mailingID);
    }

    @Override
    public List<TrackableLinkListItem> getTrackableLinkItems(int mailingID, @VelocityCheck int companyId) {
        checkMailing(mailingID, companyId);
        return trackableLinkDao.listTrackableLinksForMailing(companyId, mailingID);
    }

    @Override
    public List<ComTrackableLink> getTrackableLinks(int mailingId, @VelocityCheck int companyId) {
        if (companyId < 0) {
            return new ArrayList<>();
        }

        return trackableLinkDao.getTrackableLinks(companyId, mailingId);
    }
    
    @Override
    public List<ComTrackableLink> getTrackableLinks(int companyId, List<Integer> urlIds) {
        if (companyId < 0) {
            return new ArrayList<>();
        }
    
        List<Integer> filteredUrlIds = urlIds.stream().filter(id -> id != 0).collect(Collectors.toList());
        return trackableLinkDao.getTrackableLinks(companyId, filteredUrlIds);
    }

    @Override
    public int saveTrackableLink(ComTrackableLink trackableLink) {
        return trackableLinkDao.saveTrackableLink(trackableLink);
    }

    @Override
    public ComTrackableLink getTrackableLink(@VelocityCheck int companyId, int linkId) {
        return trackableLinkDao.getTrackableLink(linkId, companyId);
    }
    
    @Override
    public TrackableLinkSettings getTrackableLinkSettings(int linkID, @VelocityCheck int companyId) {
        ComTrackableLink trackableLink = trackableLinkDao.getTrackableLink(linkID, companyId);
        if (trackableLink == null) {
            throw new TrackableLinkUnknownLinkIdException(linkID);
        }

        List<LinkProperty> linkProperties = trackableLinkDao.getLinkProperties(trackableLink);

        return new TrackableLinkSettings(trackableLink, linkProperties);
    }

    private void checkMailing(int mailingID, @VelocityCheck int companyID) {
        Mailing mailing = mailingDao.getMailing(mailingID, companyID);

        if (mailing == null || mailing.getId() == 0) {
            throw new MailingNotExistException();
        }
    }

    @Override
	public void updateLinkTarget(ComTrackableLink link, String newUrl) throws TrackableLinkException {
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
    private void checkUrlEditingAllowed(ComTrackableLink link) throws TrackableLinkException {
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
    private void checkUrlEditingAllowed(int mailingID, @VelocityCheck int companyID) throws TrackableLinkException {
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

    private void userActionLogLinksCreated(List<UserAction> userActions, Mailing aMailing, List<LinkProperty> passedLinkProperties, List<LinkProperty> commonLinkProperties, ComTrackableLink comLink) {
        StringBuilder description = new StringBuilder();

        for (LinkProperty passedLinkProperty : passedLinkProperties) {
            if (!commonLinkProperties.contains(passedLinkProperty)) {
                description.append("ID = ")
                        .append(aMailing.getId())
                        .append(". ")
                        .append("Trackable link ")
                        .append(comLink.getFullUrl())
                        .append(". Link extension ")
                        .append(passedLinkProperty.getPropertyName())
                        .append("=")
                        .append(passedLinkProperty.getPropertyValue())
                        .append(" created.");

                userActions.add(new UserAction("edit mailing links", description.toString()));
                description.setLength(0);
            }
        }
    }

    @Override
    public boolean isUrlEditingAllowed(ComAdmin admin, int mailingID) {
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
        if (CollectionUtils.isEmpty(bulkIds)) {
            return new ArrayList<>();
        }
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);
        return mailing.getTrackableLinks().values().stream()
                .filter(comTrackableLink -> bulkIds.contains(comTrackableLink.getId()))
                .map(BaseTrackableLink::getProperties)
                .reduce((p1, p2) -> {
                    p1.retainAll(p2);
                    return p1;
                }).orElseGet(ArrayList::new);
    }

    @Override
    public void updateTrackableLinkSettings(TrackableLinkModel trackableLinkModel) {
        ComTrackableLink mergedTrackableLinkSettingsData = mergeTrackableLinkData(trackableLinkModel);

        trackableLinkDao.saveTrackableLink(mergedTrackableLinkSettingsData);
    }

    @Override
    public boolean isTrackingOnEveryPositionAvailable(@VelocityCheck int companyId, int mailingId) {
        return trackableLinkDao.isTrackingOnEveryPositionAvailable(companyId, mailingId);
    }

    private ComTrackableLink mergeTrackableLinkData(TrackableLinkModel trackableLinkModel) {
        ComTrackableLink trackableLink = trackableLinkDao.getTrackableLink(trackableLinkModel.getId(), trackableLinkModel.getCompanyID());

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

    private String getFullUrl( ComTrackableLink oldTrackableLink, TrackableLinkModel newTrackableLink ) {
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

    private String getOriginalUrl( ComTrackableLink oldTrackableLink, TrackableLinkModel newTrackableLink ) {
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

    /**
	 * Set DAO for accessing trackable links.
	 *
	 * @param trackableLinkDao DAO for accessing trackable links.
	 */
    @Required
	public void setTrackableLinkDao(ComTrackableLinkDao trackableLinkDao) {
		this.trackableLinkDao = trackableLinkDao;
	}

	/**
	 * Set service dealing with mailings.
	 *
	 * @param mailingService service dealing with mailings.
	 */
	@Required
	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}
}
