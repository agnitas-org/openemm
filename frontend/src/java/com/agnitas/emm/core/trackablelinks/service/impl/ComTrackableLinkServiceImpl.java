/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.TrackableLink;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;

import com.agnitas.beans.ComMailing;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.beans.TrackableLinkModel;
import com.agnitas.beans.TrackableLinkSettings;
import com.agnitas.dao.ComTrackableLinkDao;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.trackablelinks.exceptions.MailingNotSentException;
import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkException;
import com.agnitas.emm.core.trackablelinks.exceptions.TrackableLinkUnknownLinkIdException;
import com.agnitas.emm.core.trackablelinks.service.ComTrackableLinkService;

/**
 * Service class dealing with trackable links.
 */
public class ComTrackableLinkServiceImpl implements ComTrackableLinkService {

	/** The logger. */
    private static final transient Logger logger = Logger.getLogger(ComTrackableLinkServiceImpl.class);
    
    public static final int KEEP_UNCHANGED = -1;
    
    /** DAO for accessing trackable links. */
    private ComTrackableLinkDao trackableLinkDao;
    
    @Resource(name="MailingDao")
    protected MailingDao mailingDao;
    
    /** Service dealing with mailings. */
    private MailingService mailingService;


    @Override
    public void addExtensions(ComMailing aMailing, Set<Integer> linksIds, List<LinkProperty> passedLinkProperties) {
        Collection<TrackableLink> trackableLinkList = aMailing.getTrackableLinks().values();
        if ((trackableLinkList.size() > 0) && (linksIds.size() > 0)) {
            if(passedLinkProperties.size() > 0) {
                for (TrackableLink link : trackableLinkList) {
                    if (linksIds.contains(link.getId())) {
                        // Change link properties
                        List<LinkProperty> existingProperties = ((ComTrackableLink) link).getProperties();
                        List<LinkProperty> updatedProperties = new ArrayList<>();
                        // boolean changedProperty = false;
                        for (LinkProperty property : existingProperties) {
                            if (property.getPropertyType() != PropertyType.LinkExtension) {
                                updatedProperties.add(property);
                            }
                        }
                        updatedProperties.addAll(passedLinkProperties);
                        ((ComTrackableLink) link).setProperties(updatedProperties);
                    }
                }
            }
        }
    }

    @Override
    public void replaceCommonExtensions(ComMailing aMailing, List<LinkProperty> passedLinkProperties, Set<Integer> bulkLinkIds, List<UserAction> userActions) {
        List<LinkProperty> commonLinkProperties = aMailing.getCommonLinkExtensions();

        for (TrackableLink link : aMailing.getTrackableLinks().values()) {
            if(bulkLinkIds.contains(link.getId())) {
                ComTrackableLink comLink = (ComTrackableLink) link;
                Set<LinkProperty> linkProperties = new HashSet<>(comLink.getProperties());

                // Remove all old commonLinkProperties
                Set<LinkProperty> linkPropertiesToRemove = new HashSet<>();
                for (LinkProperty commonLinkProperty : commonLinkProperties) {
                    for (LinkProperty linkProperty : linkProperties) {
                        if (linkProperty.equals(commonLinkProperty)) {
                            linkPropertiesToRemove.add(linkProperty);
                        }
                    }
                }
                for (LinkProperty linkProperty : linkPropertiesToRemove) {
                    if (!passedLinkProperties.contains(linkProperty)) {
                        userActionLogLinkDeleted(userActions, aMailing, comLink, linkProperty);
                    }
                    linkProperties.remove(linkProperty);
                }

                userActionLogLinksCreated(userActions, aMailing, passedLinkProperties, commonLinkProperties, comLink);

                linkProperties.addAll(passedLinkProperties);

                comLink.setProperties(new ArrayList<>(linkProperties));
            }
        }
    }

    @Override
    public void removeLegacyMailingLinkExtension(ComMailing aMailing, Set<Integer> bulkLinkIds) {
        for (TrackableLink link : aMailing.getTrackableLinks().values()) {
            if(bulkLinkIds.contains(link.getId())) {
                ((ComTrackableLink) link).setExtendByMailingExtensions(false);
            }
        }
    }

    @Override
    public void setMailingLinkExtension(ComMailing aMailing, String linkExtension) {
        ComTrackableLink aLink;
        for (TrackableLink link : aMailing.getTrackableLinks().values()) {
            aLink = (ComTrackableLink) link;
            aLink.setExtendByMailingExtensions(aLink.getProperties() != null && aLink.getProperties().size() > 0);
            aLink.setProperties(null);
        }
    }

    @Override
    public void setLegacyLinkExtensionMarker(ComMailing aMailing, Map<Integer, Boolean> linksToExtends) {
        for (TrackableLink link : aMailing.getTrackableLinks().values()) {
            Boolean extendLinkByMailingLinkExtension = linksToExtends.get(link.getId());
            if (extendLinkByMailingLinkExtension != null) {
                ((ComTrackableLink) link).setExtendByMailingExtensions(extendLinkByMailingLinkExtension);
            }
        }
    }

    @Override
    public void saveGlobalRelevance(ComMailing aMailing, Set<Integer> bulkLinkIds, int globalRelevance, Map<Integer, Integer> linkItemsRelevance) {
        try {
            for (TrackableLink aLink : aMailing.getTrackableLinks().values()) {
                int id = aLink.getId();
                int linkItemRelevance = linkItemsRelevance.getOrDefault(id, 0);
                if (aLink.getRelevance() != linkItemRelevance) {
                    aLink.setRelevance(linkItemRelevance);
                } else if ((globalRelevance != KEEP_UNCHANGED) && bulkLinkIds.contains(id)) {
                    aLink.setRelevance(globalRelevance);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean saveEveryPositionLinks(ComMailing aMailing, ApplicationContext aContext, Set<Integer> bulkLinkIds) throws Exception {
        List<String> links = aMailing.getTrackableLinks().values().stream()
                .filter(Objects::nonNull)
                .filter((link) -> bulkLinkIds.contains(link.getId()))
                .map(TrackableLink::getFullUrl)
                .collect(Collectors.toList());

        boolean modified = aMailing.replaceDuplicatedLinks(links, aContext);

        aMailing.buildDependencies(true, aContext);
        return modified;
    }

    @Override
    public void setStandardDeeptracking(ComMailing aMailing, Set<Integer> bulkLinkIds, int deepTracking, Map<Integer, Integer> getLinkItemsDeepTracking) {
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
    public void setShortname(ComMailing aMailing, Map<Integer, String> linkItemNames) {
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
    public List<TrackableLinkListItem> getTrackableLinks(int mailingID, @VelocityCheck int companyId) {
        checkMailing(mailingID, companyId);
        return trackableLinkDao.listTrackableLinksForMailing(companyId, mailingID);
    }

    @Override
    public TrackableLinkSettings getTrackableLinkSettings(int linkID, @VelocityCheck int companyId) {
        ComTrackableLink trackableLink = (ComTrackableLink) trackableLinkDao.getTrackableLink(linkID, companyId);
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

    private void userActionLogLinkDeleted(List<UserAction> userActions, ComMailing aMailing, ComTrackableLink comLink, LinkProperty linkProperty) {
        String description = "ID = " +
                aMailing.getId() +
                ". " +
                "Trackable link " +
                comLink.getFullUrl() +
                ". Link extension " +
                linkProperty.getPropertyName() +
                "=" +
                linkProperty.getPropertyValue() +
                " deleted.";

        userActions.add(new UserAction("edit mailing links", description));
    }

    private void userActionLogLinksCreated(List<UserAction> userActions, ComMailing aMailing, List<LinkProperty> passedLinkProperties, List<LinkProperty> commonLinkProperties, ComTrackableLink comLink) {
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
    public boolean isUrlEditingAllowed(int mailingID, @VelocityCheck int companyId) {
    	try {
    		checkUrlEditingAllowed(mailingID, companyId);
    		return true;
    	} catch (TrackableLinkException | MailingNotExistException e) {
    		return false;
    	}
    }

	@Override
	public void removeGlobalAndIndividualLinkExtensions(@VelocityCheck int companyId, int mailingId) throws Exception {
		trackableLinkDao.removeGlobalAndIndividualLinkExtensions(companyId, mailingId);
	}

    @Override
    public void updateTrackableLinkSettings(TrackableLinkModel trackableLinkModel) {
        ComTrackableLink mergedTrackableLinkSettingsData = mergeTrackableLinkData(trackableLinkModel);

        trackableLinkDao.saveTrackableLink(mergedTrackableLinkSettingsData);
    }

    private ComTrackableLink mergeTrackableLinkData(TrackableLinkModel trackableLinkModel) {
        ComTrackableLink trackableLink = (ComTrackableLink) trackableLinkDao.getTrackableLink(trackableLinkModel.getId(), trackableLinkModel.getCompanyID());

        if (trackableLink == null) {
            throw new TrackableLinkUnknownLinkIdException(trackableLinkModel.getId());
        }

        // Optional fields
        String fullUrl = getFullUrl(trackableLink, trackableLinkModel);
        int actionId = mergeFields(trackableLink.getActionID(), trackableLinkModel.getActionID());
        String shortname = mergeFields(trackableLink.getShortname(), trackableLinkModel.getShortname());
        int deepTracking = mergeFields(trackableLink.getDeepTracking(), trackableLinkModel.getDeepTracking());
        int relevance = mergeFields(trackableLink.getRelevance(), trackableLinkModel.getRelevance());
        String altText = mergeFields(trackableLink.getAltText(), trackableLinkModel.getAltText());
        String originalUrl = getOriginalUrl(trackableLink, trackableLinkModel);
        boolean isAdminLink = mergeFields(trackableLink.isAdminLink(), trackableLinkModel.getAdminLink());
        int usage = mergeFields(trackableLink.getUsage(), trackableLinkModel.getUsage());
        List<LinkProperty> properties = mergeFields(trackableLink.getProperties(), trackableLinkModel.getLinkProperties());

        trackableLink.setFullUrl(fullUrl);
        trackableLink.setActionID(actionId);
        trackableLink.setShortname(shortname);
        trackableLink.setDeepTracking(deepTracking);
        trackableLink.setRelevance(relevance);
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
