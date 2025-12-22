/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dyncontent.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.dao.TargetDao;
import com.agnitas.emm.core.dyncontent.service.validation.ContentModelValidator;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.impl.DynamicTagContentImpl;
import com.agnitas.emm.core.dyncontent.dao.DynamicTagContentDao;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.core.dyncontent.entity.ContentModel;
import com.agnitas.emm.core.dyncontent.exception.DynamicTagContentInvalid;
import com.agnitas.emm.core.dyncontent.exception.DynamicTagContentNotExistException;
import com.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import com.agnitas.emm.core.dyncontent.exception.DynamicTagContentWithSameOrderAlreadyExist;
import com.agnitas.emm.core.dyncontent.exception.DynamicTagContentWithSameTargetIdAlreadyExist;
import com.agnitas.emm.core.dynname.exception.DynamicTagNameNotExistException;
import com.agnitas.emm.core.mailing.exception.MailingNotExistException;
import com.agnitas.emm.core.target.exception.TargetNotExistException;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("dynamicTagContentService")
public class DynamicTagContentServiceImpl implements DynamicTagContentService {

	private static final Logger LOGGER = LogManager.getLogger(DynamicTagContentServiceImpl.class);
	
	private final DynamicTagContentDao dynamicTagContentDao;
	private final MailingService mailingService;
	private final TargetDao targetDao;
    private final ContentModelValidator contentModelValidator;
    private final ApplicationContext applicationContext;

	public DynamicTagContentServiceImpl(DynamicTagContentDao dynamicTagContentDao, MailingService mailingService, TargetDao targetDao,
                                        ContentModelValidator contentModelValidator, ApplicationContext applicationContext) {
		this.dynamicTagContentDao = dynamicTagContentDao;
        this.mailingService = mailingService;
		this.targetDao = targetDao;
		this.contentModelValidator = contentModelValidator;
		this.applicationContext = applicationContext;
	}

	protected boolean deleteContentImpl(ContentModel model, List<UserAction> userActions) {
        if (dynamicTagContentDao.deleteContent(model.getCompanyId(), model.getContentId())) {
        	userActions.add(new UserAction("delete textblock", "ID " + model.getContentId()));
            return true;
        }

        return false;
	}

	@Override
	public DynamicTagContent getContent(ContentModel model) {
	    contentModelValidator.assertIsValidToGetOrDelete(model);
		DynamicTagContent content = dynamicTagContentDao.getContent(model.getCompanyId(), model.getContentId());
		if (content == null) {
			throw new DynamicTagContentNotExistException();
		}
		return content;
	}

	@Override
	public List<DynamicTagContent> getContentList(ContentModel model) {
	    contentModelValidator.assertIsValidToGetList(model);
		if (!mailingService.exists(model.getMailingId(), model.getCompanyId())) {
			throw new MailingNotExistException(model.getCompanyId(), model.getMailingId());
		}
		return dynamicTagContentDao.getContentList(model.getCompanyId(), model.getMailingId());
	}

	protected int addContentImpl(ContentModel model, List<UserAction> userActions) {
		Mailing mailing = mailingService.getMailing(model.getCompanyId(), model.getMailingId());
		DynamicTag dynamicTag = mailing.getDynTags().get(model.getBlockName());
		if (dynamicTag == null) {
			throw new DynamicTagNameNotExistException();
		}
		if (model.getTargetId() != 0 && targetDao.getTarget(model.getTargetId(), model.getCompanyId()) == null) {
			throw new TargetNotExistException(model.getTargetId());
		}
        Map<Integer, DynamicTagContent> dContent = dynamicTag.getDynContent();

        if (dContent.containsKey(model.getOrder())) {
        	throw new DynamicTagContentWithSameOrderAlreadyExist();
        }
        
        for (DynamicTagContent aContentTmp : dContent.values()) {
            if(aContentTmp.getTargetID() == model.getTargetId()) {
                throw new DynamicTagContentWithSameTargetIdAlreadyExist();
            }
		}
        
        DynamicTagContent content = new DynamicTagContentImpl();
		content.setDynNameID(dynamicTag.getId());
		content.setId(0);
		content.setDynName(dynamicTag.getDynName());
		content.setMailingID(model.getMailingId());
		content.setCompanyID(model.getCompanyId());
		content.setDynOrder(model.getOrder());
		content.setTargetID(model.getTargetId());
		content.setDynContent(model.getContent());
		dynamicTag.addContent(content);

        try {
        	mailing.buildDependencies(false, applicationContext);
        } catch (Exception e) {
        	LOGGER.error(String.format("Error building dependencies of mailing %d", mailing.getId()), e);
        	
        	throw new DynamicTagContentInvalid(e.getMessage());
        }

		try {
			mailingService.saveMailingWithNewContent(mailing, false, false);
		} catch (Exception e) {
        	LOGGER.error(String.format("Error saving mailing %d", mailing.getId()), e);
        	
			throw new DynamicTagContentInvalid(e.getMessage());
		}

		String description = dynamicTag.getDynName() +
				"(" +
				content.getId() +
				")" +
				" in the " +
				(mailing.isIsTemplate() ? "template " : "mailing ") +
				mailing.getShortname() +
				"(" +
				mailing.getId() +
				")";
		userActions.add(new UserAction("create textblock", description));

		return content.getId();
	}
		
	protected void updateContentImpl(ContentModel contentModel, List<UserAction> userActions) {
		// Read existing content item by company_id and content_id to get the mailingid.
		// Do not change this object, because changes will not be stored.
		DynamicTagContent dynamicTagContentForReadOnly = getContent(contentModel);

		logTimePoint("updateContentImpl -> before // Basically check new targetgroup settings");
		// Basically check new targetgroup settings
		if (contentModel.getTargetId() != 0 && targetDao.getTarget(contentModel.getTargetId(), contentModel.getCompanyId()) == null) {
			throw new TargetNotExistException(contentModel.getTargetId());
		}
		
		int mailingId = dynamicTagContentForReadOnly.getMailingID();
		String dynName = dynamicTagContentForReadOnly.getDynName();
		int dynOrder = dynamicTagContentForReadOnly.getDynOrder();

		logTimePoint("updateContentImpl -> before // Read existing mailing data");
		// Read existing mailing data
		Mailing mailing = mailingService.getMailing(contentModel.getCompanyId(), mailingId);

		DynamicTag dynamicTag = mailing.getDynTags().get(dynName);
		// Consistency check of dynamic tag
		if (dynamicTag == null) {
			throw new DynamicTagNameNotExistException();
		}
		
		DynamicTagContent dynamicTagContentToChange = null;
		for (DynamicTagContent dynamicTagContent : dynamicTag.getDynContent().values()) {
			if (dynamicTagContent.getId() == contentModel.getContentId()) {
				dynamicTagContentToChange = dynamicTagContent;
				break;
			}
		}
		
		if (dynamicTagContentToChange == null) {
			throw new RuntimeException("dynamicTagContentToChange was not found");
		}

		logTimePoint("updateContentImpl -> before // Re-check new targetgroup settings");
		// Re-check new targetgroup settings for DynamicTagContent loaded via mailing
		if (contentModel.getTargetId() != 0 && targetDao.getTarget(contentModel.getTargetId(), contentModel.getCompanyId()) == null) {
			throw new TargetNotExistException(contentModel.getTargetId());
		}
		
		Map<Integer, DynamicTagContent> dynContentMap = dynamicTag.getDynContent();
		// Check if new target group order is possible
		if (dynamicTagContentToChange.getDynOrder() != contentModel.getOrder() && dynContentMap.containsKey(contentModel.getOrder())) {
			throw new DynamicTagContentWithSameOrderAlreadyExist();
		}

		// Set new dyn content order
		boolean dynOrderWasChanged = false;
		if (dynamicTagContentToChange.getDynOrder() != contentModel.getOrder()) {
			dynamicTagContentToChange.setDynOrder(contentModel.getOrder());
			dynOrderWasChanged = true;
		}
		
		int maxOrder = 0;
		int minOrder = Integer.MAX_VALUE;
		for (DynamicTagContent aContentTmp : dynContentMap.values()) {
			// Check if new targetgroup settings collide with existing ones
			if (dynamicTagContentToChange.getTargetID() != contentModel.getTargetId() && aContentTmp.getTargetID() == contentModel.getTargetId()) {
				throw new DynamicTagContentWithSameTargetIdAlreadyExist();
			}
			
			// Get current max order value
			if (aContentTmp.getDynOrder() > maxOrder) {
				maxOrder = aContentTmp.getDynOrder();
			}
			
			// Get current min order value
			if (aContentTmp.getDynOrder() < minOrder) {
				minOrder = aContentTmp.getDynOrder();
			}
		}

		// Set new target id
		if (dynamicTagContentToChange.getTargetID() != contentModel.getTargetId()) {
			dynamicTagContentToChange.setTargetID(contentModel.getTargetId());
		}

		logTimePoint("updateContentImpl -> before // Set new content text");
		// Set new content text
		boolean textContentWasChanged = false;
		if (!StringUtils.equals(dynamicTagContentToChange.getDynContent(), contentModel.getContent())) {
			dynamicTagContentToChange.setDynContent(contentModel.getContent());
			textContentWasChanged = true;
		}

		logTimePoint("updateContentImpl -> before // Validate content text and check for new or removed trackable links and images");
		// Validate content text and check for new or removed trackable links and images
		try {
        	mailing.buildDependencies(false, applicationContext);
        } catch (Exception e) {
        	LOGGER.error(String.format("Error building dependencies of mailing %d", mailing.getId()), e);
        	
        	throw new DynamicTagContentInvalid(e.getMessage());
        }

		logTimePoint("updateContentImpl -> before // Save changes");
		// Save changes
		try {
			mailingService.saveMailingWithNewContent(mailing, false, false);
		} catch (Exception e) {
        	LOGGER.error(String.format("Error saving mailing %d", mailing.getId()), e);
        	
			throw new DynamicTagContentInvalid(e.getMessage());
		}

		// Write UserActivityLog for executed changes
		String description = dynamicTag.getDynName() + " (" + contentModel.getContentId() + ")" + " in the " +
				(mailing.isIsTemplate() ? "template " : "mailing ") + mailing.getShortname() +
				" (" + mailing.getId() + ")";

		// Log order change
		if (dynOrderWasChanged) {
			if (contentModel.getOrder() > dynOrder) {
				if (contentModel.getOrder() > maxOrder) {
					userActions.add(new UserAction("do move textblock bottom", description));
				} else {
					userActions.add(new UserAction("do move textblock down", description));
				}
			} else {
				if (contentModel.getOrder() < minOrder) {
					userActions.add(new UserAction("do move textblock top", description));
				} else {
					userActions.add(new UserAction("do move textblock up", description));
				}
			}
		}

		logTimePoint("updateContentImpl -> before // Log content change");
		// Log content change
		if (textContentWasChanged) {
			userActions.add(new UserAction("edit textblock content", description));
		}
	}

    @Override
    public DynamicTagContent getContent(int companyID, int contentID) {
        return dynamicTagContentDao.getContent(companyID,contentID);
    }

	@Override
	@Transactional
    public int addContent(ContentModel model, List<UserAction> userActions) {
	    contentModelValidator.assertIsValidToAdd(model);
        int contentId = addContentImpl(model, userActions);
		mailingService.updateStatus(model.getCompanyId(), model.getMailingId(), MailingStatus.EDIT);
        return contentId;
    }

	@Override
	@Transactional
	public boolean deleteContent(ContentModel model, List<UserAction> userActions) {
	    contentModelValidator.assertIsValidToGetOrDelete(model);
		boolean res = deleteContentImpl(model, userActions);
		mailingService.updateStatus(model.getCompanyId(), model.getMailingId(), MailingStatus.EDIT);
        return res;
	}

	@Override
	@Transactional
	public void updateContent(ContentModel model, List<UserAction> userActions) {
		logTimePoint("updateContent -> before assertIsValidToUpdate");
	    contentModelValidator.assertIsValidToUpdate(model);
		logTimePoint("updateContent -> before updateContentImpl");
		updateContentImpl(model, userActions);
		logTimePoint("updateContent -> before updateStatus");
		mailingService.updateStatus(model.getCompanyId(), model.getMailingId(), MailingStatus.EDIT);
	}

	@Override
	public List<Integer> findTargetDependentMailingsContents(int targetGroupId, int companyId) {
		return dynamicTagContentDao.findTargetDependentMailingsContents(targetGroupId, companyId);
	}

	@Override
	public List<Integer> filterContentsOfNotSentMailings(List<Integer> dependencies) {
		if (dependencies.isEmpty()) {
			return Collections.emptyList();
		}

		return dynamicTagContentDao.filterContentsOfNotSentMailings(dependencies);
	}

	// GWUA-5995
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	public static void logTimePoint(String name) {
		LocalDateTime now = LocalDateTime.now();
		LOGGER.error("GWUA-5995: " + name + ": " + formatter.format(now));
	}
}
