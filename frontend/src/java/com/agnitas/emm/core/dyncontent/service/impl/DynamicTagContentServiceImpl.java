/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dyncontent.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.impl.DynamicTagContentImpl;
import org.agnitas.dao.DynamicTagContentDao;
import org.agnitas.emm.core.dyncontent.service.ContentModel;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentInvalid;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentNotExistException;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentWithSameOrderAlreadyExist;
import org.agnitas.emm.core.dyncontent.service.DynamicTagContentWithSameTargetIdAlreadyExist;
import org.agnitas.emm.core.dynname.service.DynamicTagNameNotExistException;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.target.service.TargetNotExistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.validator.annotation.Validate;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.DynamicTag;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;

public class DynamicTagContentServiceImpl implements DynamicTagContentService, ApplicationContextAware {  // TODO: Do we really need to implement ApplicationContextAware? No better ideas here???
	@Resource(name="DynamicTagContentDao")
	private DynamicTagContentDao dynamicTagContentDao;
	@Resource(name="MailingDao")
	private ComMailingDao mailingDao;
	@Resource(name="TargetDao")
	private ComTargetDao targetDao;

	private ApplicationContext applicationContext;

	protected boolean deleteContentImpl(ContentModel model, List<UserAction> userActions) {
        if (dynamicTagContentDao.deleteContent(model.getCompanyId(), model.getContentId())) {
        	userActions.add(new UserAction("delete textblock", "ID " + model.getContentId()));
            return true;
        }

        return false;
	}

	@Override
	@Validate("getContentBlock")
	public DynamicTagContent getContent(ContentModel model) {
		DynamicTagContent content = dynamicTagContentDao.getContent(model.getCompanyId(), model.getContentId());
		if (content == null) {
			throw new DynamicTagContentNotExistException();
		}
		return content;
	}

	@Override
	@Validate("listContentBlocksOrNames")
	public List<DynamicTagContent> getContentList(ContentModel model) {
		if (!mailingDao.exist(model.getMailingId(), model.getCompanyId())) {
			throw new MailingNotExistException();
		}
		return dynamicTagContentDao.getContentList(model.getCompanyId(), model.getMailingId());
	}

	@Validate("addContentBlock")
	protected int addContentImpl(ContentModel model, List<UserAction> userActions) {
		Mailing mailing = mailingDao.getMailing(model.getMailingId(), model.getCompanyId());
		if (mailing == null || mailing.getId() == 0) {
			throw new MailingNotExistException();
		}
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
        
        DynamicTagContent aContent = new DynamicTagContentImpl();
		aContent.setDynNameID(dynamicTag.getId());
		aContent.setId(0);
		aContent.setDynName(dynamicTag.getDynName());
		aContent.setMailingID(model.getMailingId());
		aContent.setCompanyID(model.getCompanyId());
		aContent.setDynOrder(model.getOrder());
		aContent.setTargetID(model.getTargetId());
		aContent.setDynContent(model.getContent());
		dynamicTag.addContent(aContent);

        try {
        	mailing.buildDependencies(false, applicationContext);
        } catch (Exception e) {
        	throw new DynamicTagContentInvalid(e.getMessage());
        }

		try {
			mailingDao.saveMailing(mailing, false, false);
		} catch (Exception e) {
			throw new DynamicTagContentInvalid(e.getMessage());
		}

		String description = dynamicTag.getDynName() +
				"(" +
				aContent.getId() +
				")" +
				" in the " +
				(mailing.isIsTemplate() ? "template " : "mailing ") +
				mailing.getShortname() +
				"(" +
				mailing.getId() +
				")";
		userActions.add(new UserAction("create textblock", description));

		return aContent.getId();
	}
		
	protected void updateContentImpl(ContentModel contentModel, List<UserAction> userActions) {
		// Read existing content item by company_id and content_id to get the mailingid.
		// Do not change this object, because changes will not be stored.
		DynamicTagContent dynamicTagContentForReadOnly = getContent(contentModel);
		
		// Basically check new targetgroup settings
		if (contentModel.getTargetId() != 0 && targetDao.getTarget(contentModel.getTargetId(), contentModel.getCompanyId()) == null) {
			throw new TargetNotExistException(contentModel.getTargetId());
		}
		
		int mailingId = dynamicTagContentForReadOnly.getMailingID();
		String dynName = dynamicTagContentForReadOnly.getDynName();
		int dynOrder = dynamicTagContentForReadOnly.getDynOrder();

		// Read existing mailing data
		Mailing mailing = mailingDao.getMailing(mailingId, contentModel.getCompanyId());
		if (mailing == null) {
			throw new MailingNotExistException();
		}
		
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

		// Set new content text
		boolean textContentWasChanged = false;
		if (!StringUtils.equals(dynamicTagContentToChange.getDynContent(), contentModel.getContent())) {
			dynamicTagContentToChange.setDynContent(contentModel.getContent());
			textContentWasChanged = true;
		}

		// Validate content text and check for new or removed trackable links and images
		try {
        	mailing.buildDependencies(false, applicationContext);
        } catch (Exception e) {
        	throw new DynamicTagContentInvalid(e.getMessage());
        }
		
		// Save changes
		try {
			mailingDao.saveMailing(mailing, false, false);
		} catch (Exception e) {
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
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

	@Override
	@Transactional
	@Validate("addContentBlock")
    public int addContent(ContentModel model, List<UserAction> userActions) {
        int contentId = addContentImpl(model, userActions);
        mailingDao.updateStatus(model.getMailingId(), "edit");
        return contentId;
    }

	@Override
	@Transactional
	@Validate("deleteContentBlock")
	public boolean deleteContent(ContentModel model, List<UserAction> userActions) {
		boolean res = deleteContentImpl(model, userActions);
        mailingDao.updateStatus(model.getMailingId(), "edit");
        return res;
	}

	@Override
	@Transactional
	@Validate("updateContentBlock")
	public void updateContent(ContentModel model, List<UserAction> userActions) {
		updateContentImpl(model, userActions);
        mailingDao.updateStatus(model.getMailingId(), "edit");
	}
}
