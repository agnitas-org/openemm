/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingSendStatus;
import org.agnitas.beans.Mediatype;
import org.agnitas.beans.TrackableLink;
import org.agnitas.beans.factory.DynamicTagContentFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.DynamicTagContentDao;
import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.exception.UnknownMailingIdException;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnTagUtils;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DynTagException;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.SafeString;
import org.agnitas.util.Tuple;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComUndoDynContent;
import com.agnitas.beans.ComUndoMailing;
import com.agnitas.beans.ComUndoMailingComponent;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComUndoDynContentDao;
import com.agnitas.dao.ComUndoMailingComponentDao;
import com.agnitas.dao.ComUndoMailingDao;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.bean.MailingRecipientStatRow;
import com.agnitas.emm.core.mailing.dto.CalculationRecipientsConfig;
import com.agnitas.emm.core.mailing.service.CalculationRecipients;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.messages.Message;
import com.agnitas.service.AgnDynTagGroupResolverFactory;
import com.agnitas.service.AgnTagService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.Span;

public class ComMailingBaseServiceImpl implements ComMailingBaseService {
	private static final Logger logger = Logger.getLogger(ComMailingBaseServiceImpl.class);
    
    private static final String AGN_TAG_IMAGE = "agnIMAGE";
    private static final String AGN_TAG_IMGLINK = "agnIMGLINK";

    private static final double QUOTED_PRINTABLE_OVERHEAD_RATIO = 8.f / 7.f;  // Quoted-printable notation uses 7 bit out of 8
    private static final double BASE64_OVERHEAD_RATIO = 8.f / 6.f;  // Base64 uses 6 bit out of 8 (padding is neglectable)

	private ComMailingDao mailingDao;
	private AdminService adminService;
    private GridServiceWrapper gridServiceWrapper;
    protected ComRecipientDao recipientDao;
    protected ExecutorService workerExecutorService;
    private ComUndoMailingDao undoMailingDao;
    private ComUndoMailingComponentDao undoMailingComponentDao;
    private ComUndoDynContentDao undoDynContentDao;
    private DynamicTagDao dynamicTagDao;
    private ComTargetService targetService;
    private MaildropService maildropService;
	private AgnTagService agnTagService;
	private ComMailingComponentsService mailingComponentsService;
	private LinkService linkService;
    private DynamicTagContentFactory dynamicTagContentFactory;
    private DynamicTagContentDao dynamicTagContentDao;
    private AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory;
    private MailinglistApprovalService mailinglistApprovalService;
    
    @Override
    public boolean isMailingExists(int mailingId, @VelocityCheck int companyId) {
        return mailingId > 0 && companyId > 0 && mailingDao.exist(mailingId, companyId);
    }

    @Override
    public boolean isMailingExists(int mailingId, @VelocityCheck int companyId, boolean isTemplate) {
        return mailingId > 0 && companyId > 0 && mailingDao.exist(mailingId, companyId, isTemplate);
    }

    @Override
    public boolean checkUndoAvailable(int mailingId) {
        return undoMailingDao.getLastUndoData(mailingId) != null;
    }

    @Override
    public boolean isTextTemplateExists(ComAdmin admin, int mailingId) {
        MailingComponent mailingTextTemplate = mailingComponentsService.getMailingTextTemplate(mailingId, admin.getCompanyID());
        if (mailingTextTemplate != null) {
            String emmBlock = mailingTextTemplate.getEmmBlock();
            if (StringUtils.isNotBlank(emmBlock)) {
                // check if text template doesn't contain default value that is set by ComMailingBaseService::doTextTemplateFilling
                String placeholder = SafeString.getLocaleString("mailing.textversion.default", admin.getLocale());
                return !StringUtils.equals(emmBlock, placeholder);
            }
        }
        return false;
    }
    
    @Override
    public void bulkDelete(Set<Integer> mailingsIds, @VelocityCheck int companyId) {
        for (int mailingId : mailingsIds) {
            mailingDao.deleteMailing(mailingId, companyId);
        }
    }

    @Override
    public int getWorkflowId(int mailingId, @VelocityCheck int companyId) {
        return mailingDao.getWorkflowId(mailingId, companyId);
    }

    @Override
    public int saveMailingWithUndo(Mailing mailing, int adminId, boolean preserveTrackableLinks) {
        int mailingId = mailing.getId();
        if (mailingId > 0) {
            int gridTemplateId = gridServiceWrapper.getGridTemplateIdByMailingId(mailingId);
            if (gridTemplateId > 0) {
                gridServiceWrapper.saveUndoGridMailing(mailingId, gridTemplateId, adminId);
            } else {
                mailingDao.saveUndoMailing(mailingId, adminId);
                mailingDao.deleteUndoDataOverLimit(mailingId);
            }
        }
        return mailingDao.saveMailing(mailing, preserveTrackableLinks);
    }

    @Override
    public void restoreMailingUndo(ApplicationContext ctx, int mailingId, @VelocityCheck int companyId) throws Exception {
        Mailing mailing = mailingDao.getMailingWithDeletedDynTags(mailingId, companyId);
        ComUndoMailing undoMailing = undoMailingDao.getLastUndoData(mailingId);
        List<ComUndoMailingComponent> undoMailingComponentList = undoMailingComponentDao.getAllUndoDataForMailing(mailingId, undoMailing.getUndoId());
        List<ComUndoDynContent> undoDynContentList = undoDynContentDao.getAllUndoDataForMailing(mailingId, undoMailing.getUndoId());

        gridServiceWrapper.restoreGridMailingUndo(undoMailing.getUndoId(), mailing);

        fillMailingWithUndoData(mailing, undoMailingComponentList, undoDynContentList);
        removeContentFromMailingNotListed(mailing, undoDynContentList);
        undoDynContentDao.deleteAddedDynContent(mailingId, undoMailing.getUndoId());

        // Rebuild dependencies to hide unused content blocks
        List<String> dynNamesForDeletion = new Vector<>();
        mailing.buildDependencies(true, dynNamesForDeletion, ctx);

        // Mark names in dynNamesForDeletion as "deleted"
        dynamicTagDao.markNamesAsDeleted(mailing.getId(), dynNamesForDeletion);

        mailingDao.saveMailing(mailing, false);

        undoDynContentDao.deleteUndoData(undoMailing.getUndoId());
        undoMailingComponentDao.deleteUndoData(undoMailing.getUndoId());
        undoMailingDao.deleteUndoData(undoMailing.getUndoId());
    }

    private void removeContentFromMailingNotListed(Mailing mailing, List<ComUndoDynContent> listedContent) {
        // Create a set of content ID's for all listed contents
        Set<Integer> listedContentIds = new HashSet<>();
        for (ComUndoDynContent content : listedContent) {
            listedContentIds.add(content.getId());
        }

        // Search for unlisted content IDs in the mailing
        Collection<DynamicTag> dynamicTags = mailing.getDynTags().values();

        for (DynamicTag dynTag : dynamicTags) {
            Map<Integer, DynamicTagContent> dynContents = dynTag.getDynContent();
            Set<Integer> ordersToRemove = new HashSet<>();

            for (Entry<Integer, DynamicTagContent> entry : dynContents.entrySet()) {
            	Integer order = entry.getKey();
                DynamicTagContent content = entry.getValue();

                if (!listedContentIds.contains(content.getId())) {
                    ordersToRemove.add(order);
                }
            }

            for (Integer entryId : ordersToRemove) {
                dynContents.remove(entryId);
            }
        }
    }

    private void fillMailingWithUndoData(Mailing mailing, List<ComUndoMailingComponent> undoMailingComponentList, List<ComUndoDynContent> undoDynContentList) {
        Map<String, MailingComponent> mailingComponents = mailing.getComponents();
        for (ComUndoMailingComponent undoComponent : undoMailingComponentList) {
            mailingComponents.put(undoComponent.getComponentName(), undoComponent);
        }
        Map<String, DynamicTag> dynTags = mailing.getDynTags();
        for (ComUndoDynContent undoDynContent : undoDynContentList) {
        	DynamicTag dynamicTag = dynTags.get(undoDynContent.getDynName());

            // unused content blocks ("deleted") won't be loaded and therefore be null
            if (dynamicTag != null) {
                dynamicTag.setCompanyID(undoDynContent.getCompanyID());
                dynamicTag.getDynContent().put(undoDynContent.getDynOrder(), undoDynContent);
            }
        }
    }

    @Override
    public boolean setTargetGroups(int mailingId, @VelocityCheck int companyId, Collection<Integer> targetGroupIds, boolean conjunction) throws TooManyTargetGroupsInMailingException {
        if (mailingId <= 0 || companyId <= 0) {
            return false;
        }

        if (mailingDao.usedInCampaignManager(mailingId)) {
            return false;
        }

        String expression = TargetExpressionUtils.makeTargetExpression(targetGroupIds, conjunction);
        return mailingDao.setTargetExpression(mailingId, companyId, expression);
    }

    @Override
    public String getMailingName(int mailingId, @VelocityCheck int companyId) {
        return StringUtils.defaultString(mailingDao.getMailingName(mailingId, companyId));
    }

    @Override
    public PaginatedListImpl<Map<String, Object>> getPaginatedMailingsData(ComAdmin admin, MailingsListProperties props) {
        if (admin == null || Objects.isNull(props)) {
			return new PaginatedListImpl<>();
		}
		return mailingDao.getMailingList(admin.getCompanyID(), admin.getAdminID(), props);
    }
    
    @Override
    public Future<PaginatedListImpl<DynaBean>> getMailingRecipientsLongRunning(int mailingId, @VelocityCheck int companyId, int filterType, int pageNumber, int rowsPerPage, String sortCriterion, boolean sortAscending, List<String> columns, DateFormat dateFormat) {
        throw new UnsupportedOperationException("Get mailing recipients is unsupported.");
    }

    @Override
    public PaginatedListImpl<MailingRecipientStatRow> getMailingRecipients(int mailingId, @VelocityCheck int companyId, int filterType, int pageNumber, int rowsPerPage, String sortCriterion, boolean sortAscending, List<String> columns) throws Exception {
        throw new UnsupportedOperationException("Get mailing recipients is unsupported.");
    }

    @Override
    public List<DynamicTag> getDynamicTags(int mailingId, @VelocityCheck int companyId) {
        return mailingDao.getDynamicTags(mailingId, companyId);
    }

    @Override
    public List<DynamicTag> getDynamicTags(int mailingId, @VelocityCheck int companyId, boolean resetIds) {
        List<DynamicTag> tags = mailingDao.getDynamicTags(mailingId, companyId);
        if (resetIds) {
            for (DynamicTag tag : tags) {
                resetIds(tag, companyId);
            }
        }
        return tags;
    }
    
    @Override
    public List<String> getDynamicTagNames(Mailing mailing) {
        if (mailing ==  null || mailing.getHtmlTemplate() == null) {
			return Collections.emptyList();
        }
    
        try {
            MailingComponent template = mailing.getHtmlTemplate();
            List<DynamicTag> dynTags = agnTagService.getDynTags(template.getEmmBlock(), agnDynTagGroupResolverFactory.create(mailing.getCompanyID(), mailing.getId()));
            return dynTags.stream().map(DynamicTag::getDynName).collect(Collectors.toList());
        } catch (DynTagException e) {
            logger.error("Error occurred: " + e.getMessage(), e);
        }
        
        return Collections.emptyList();
    }

    @Override
    public DynamicTag getDynamicTag(@VelocityCheck int companyId, int dynNameId) {
        return mailingDao.getDynamicTag(dynNameId, companyId);
    }

    private void resetIds(DynamicTag tag, int companyId) {
        tag.setId(0);
        tag.setCompanyID(companyId);
        tag.setMailingID(0);

        Map<Integer, DynamicTagContent> contentMap = tag.getDynContent();
        if (MapUtils.isNotEmpty(contentMap)) {
            for (DynamicTagContent content : contentMap.values()) {
                resetIds(content, companyId);
            }
        }
    }

    private void resetIds(DynamicTagContent content, int companyId) {
        content.setId(0);
        content.setCompanyID(companyId);
        content.setMailingID(0);
        content.setDynNameID(0);
    }

    @Override
	public int calculateRecipients(CalculationRecipientsConfig config) throws Exception {
        CalculationRecipients<CalculationRecipientsConfig> result = getDefaultCalculation();

        if (config.getMailingId() <= 0 || config.isChangeMailing()) {
            result = getForUnsavedChanges();
        }

        return result.calculate(config);
    }
    
	@Override
	public boolean isAdvertisingContentType(@VelocityCheck int companyId, int mailingId) {
		return mailingDao.isAdvertisingContentType(companyId, mailingId);
	}

	@Override
	public boolean isLimitedRecipientOverview(ComAdmin admin, int mailingId) {
    	int companyId = admin.getCompanyID();
		Mailing mailing = mailingDao.getMailing(mailingId, companyId);
		
		boolean isSentStatus;
		
		if (mailing.getMailingType() == MailingTypes.INTERVAL.getCode()) {
			String workStatus = mailingDao.getWorkStatus(companyId, mailingId);
            isSentStatus = StringUtils.equals(workStatus, MailingStatus.ACTIVE.getDbKey());
		} else {
            isSentStatus = maildropService.isActiveMailing(mailingId, companyId);
		}
		
		return isSentStatus && !mailinglistApprovalService.isAdminHaveAccess(admin, mailing.getMailinglistID());
	}

    @Lookup
    public UnsavedChanges getForUnsavedChanges(){
        return null;
    }

    @Lookup
    public DefaultCalculation getDefaultCalculation(){
        return null;
    }

    @Override
    public int calculateRecipients(@VelocityCheck int companyId, int mailingListId, int splitId, Collection<Integer> targetGroupIds, boolean conjunction) throws Exception {
        String sqlCondition = targetService.getSQLFromTargetExpression(TargetExpressionUtils.makeTargetExpression(targetGroupIds, conjunction), splitId, companyId);
        return recipientDao.getNumberOfRecipients(companyId, mailingListId, sqlCondition);
    }

    @Override
    public int calculateRecipients(@VelocityCheck int companyId, int mailingId, int mailingListId, int splitId) throws Exception {
        String sqlCondition = targetService.getSQLFromTargetExpression(mailingDao.getTargetExpression(mailingId, companyId), splitId, companyId);
        return recipientDao.getNumberOfRecipients(companyId, mailingListId, sqlCondition);
    }

    @Override
    public int calculateRecipients(@VelocityCheck int companyId, int mailingId) throws Exception {
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);

        if (mailing == null || mailing.getId() <= 0) {
            throw new UnknownMailingIdException("Mailing #" + mailingId + " doesn't exist");
        }

        String sqlCondition = targetService.getSQLFromTargetExpression(mailing, true);
        return recipientDao.getNumberOfRecipients(companyId, mailing.getMailinglistID(), sqlCondition);
    }

    @Override
    public Tuple<Long, Long> calculateMaxSize(Mailing mailing) {
        return new MailingSizeCalculation(mailing).calculate();
    }

    @Override
    public boolean isContentBlank(String content, Map<String, DynamicTag> contentMap) {
        if (StringUtils.isBlank(content)) {
            return true;
        }

        return isContentBlank(content, contentMap, new ArrayDeque<>());
    }

    @Override
    public void doTextTemplateFilling(Mailing mailing, ComAdmin admin, ActionMessages messages) {
        MailingComponent componentTextTemplate = mailing.getTextTemplate();
        if (Objects.isNull(componentTextTemplate)) {
            logger.error("Text template is absent. mailingId: " + mailing.getId());
            return;
        }

        // text template substitution in case of template completely empty
        String textTemplate = componentTextTemplate.getEmmBlock();
        if (StringUtils.isBlank(textTemplate)) {
            componentTextTemplate.setEmmBlock(SafeString.getLocaleString("mailing.textversion.default", admin.getLocale()), "text/plain");
            messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("mailing.textversion.empty"));
            return;
        }

        // checks if template contains some text except agn tags
        if (agnTagService.isContainsThirdPartyText(textTemplate)) {
            return;
        }

        // retrieves agn tags from template text
        List<DynamicTag> textTemplateDynTags;
        try {
            textTemplateDynTags = agnTagService.getDynTags(textTemplate);
            if (textTemplateDynTags.isEmpty()) {
                return;
            }
        } catch (DynTagException e) {
            logger.error("Error during retrieving dyn tags form text template. name: " + textTemplate);
            return;
        }

        // checks if text template contains at least one filled agn tag content
        Iterator<DynamicTag> dynamicTags = textTemplateDynTags.iterator();
        while (dynamicTags.hasNext()) {
            DynamicTag textTag = mailing.getDynTags().get(dynamicTags.next().getDynName());
            if (textTag.getId() <= 0) {
                int tagId = dynamicTagDao.getId(textTag.getCompanyID(),
                        textTag.getMailingID(),
                        textTag.getDynName());

                boolean isContentValueNotEmpty = dynamicTagContentDao.isContentValueNotEmpty(mailing.getCompanyID(),
                        mailing.getId(),
                        tagId);
                if (isContentValueNotEmpty) {
                    return;
                }
            } else {
                for (DynamicTagContent tagContent : textTag.getDynContent().values()) {
                    if (StringUtils.isNotBlank(tagContent.getDynContent())) {
                        return;
                    }
                }
            }

            // text module substitution
            if (!dynamicTags.hasNext()) {
                DynamicTagContent block = dynamicTagContentFactory.newDynamicTagContent();
                block.setCompanyID(admin.getCompanyID());
                block.setDynContent(SafeString.getLocaleString("mailing.textversion.default", admin.getLocale()));
                block.setDynOrder(textTag.getMaxOrder() + 1);
                block.setDynNameID(textTag.getId());
                block.setMailingID(textTag.getMailingID());

                textTag.addContent(block);
                messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("mailing.textversion.empty"));
                return;
            }
        }
    }
    
    @Override
    public Mailing getMailing(@VelocityCheck int companyId, int mailingId) {
        if (mailingId > 0 && companyId > 0) {
            return mailingDao.getMailing(mailingId, companyId);
        }
        
        return null;
    }
    
    @Override
    public List<MailingBase> getMailingsForComparison(ComAdmin admin) {
        return mailingDao.getMailingsForComparation(admin.getCompanyID(), admin.getAdminID(), adminService.getAccessLimitTargetId(admin));
    }
    
    @Override
    public Map<Integer, String> getMailingNames(List<Integer> mailingIds, int companyId) {
        if (companyId <= 0 || CollectionUtils.isEmpty(mailingIds)) {
            return new HashMap<>();
        }
        
        return mailingDao.getMailingNames(mailingIds, companyId);
    }
    
    private boolean isContentBlank(String content, Map<String, DynamicTag> contentMap, Deque<String> visitedDynBlocks) {
        if (StringUtils.isBlank(content)) {
            return true;
        }

        List<DynamicTag> tags = getDynamicTags(content);

        // Check if there's at least one non-whitespace character between (!) dyn-tags.
        if (!isOwnContentBlank(content, tags)) {
            return false;
        }

        // Check if at least one dyn-tag can be resolved as non-blank content.
        for (DynamicTag tag : tags) {
            // Ignore dyn-tag if the cyclic dependency is detected.
            if (visitedDynBlocks.contains(tag.getDynName())) {
                continue;
            }

            DynamicTag dynamicTag = contentMap.get(tag.getDynName());

            if (dynamicTag != null) {
                List<DynamicTagContent> entries = new ArrayList<>(dynamicTag.getDynContent().values());

                // Make sure that entries are sorted by designed order — to ignore unreachable ones.
                entries.sort(Comparator.comparingInt(DynamicTagContent::getDynOrder));

                visitedDynBlocks.push(tag.getDynName());
                for (DynamicTagContent entry : entries) {
                    // Check if this entry can be resolved as non-blank content.
                    if (!isContentBlank(entry.getDynContent(), contentMap, visitedDynBlocks)) {
                        visitedDynBlocks.pop();
                        return false;
                    }

                    // Ignore unreachable entries (placed below "All recipients" entry).
                    if (entry.getTargetID() <= 0) {
                        break;
                    }
                }
                visitedDynBlocks.pop();
            }
        }

        return true;
    }

    private boolean isOwnContentBlank(String content, List<DynamicTag> tags) {
        int begin = 0;

        for (Span span : getTagSpans(tags)) {
            for (int i = begin; i < span.getBegin(); i++) {
                if (!Character.isWhitespace(content.charAt(i))) {
                    return false;
                }
            }
            begin = span.getEnd();
        }

        for (int i = begin; i < content.length(); i++) {
            if (!Character.isWhitespace(content.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private List<Span> getTagSpans(List<DynamicTag> tags) {
        List<Span> spans = new ArrayList<>();

        for (DynamicTag tag : tags) {
            spans.add(new Span(tag.getStartTagStart(), tag.getStartTagEnd()));

            if (tag.getValueTagStart() != tag.getValueTagEnd()) {
                spans.add(new Span(tag.getValueTagStart(), tag.getValueTagEnd()));
            }

            if (tag.getEndTagStart() != tag.getEndTagEnd()){
                spans.add(new Span(tag.getEndTagStart(), tag.getEndTagEnd()));
            }
        }

        spans.sort(Comparator.comparingInt(Span::getBegin));

        return spans;
    }

    private List<DynamicTag> getDynamicTags(String content) {
        try {
            return agnTagService.getDynTags(content);
        } catch (DynTagException e) {
            logger.error("Error occurred: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<ImageLinkSpan> getImageLinks(String content) {
        List<ImageLinkSpan> imageLinks = new ArrayList<>();

        try {
            linkService.findAllLinks(content, (begin, end) -> {
                String url = content.substring(begin, end);

                if (AgnUtils.checkPreviousTextEquals(content, begin, "src=", ' ', '\'', '"', '\n', '\r', '\t')
                        || AgnUtils.checkPreviousTextEquals(content, begin, "background=", ' ', '\'', '"', '\n', '\r', '\t')
                        || AgnUtils.checkPreviousTextEquals(content, begin, "url(", ' ', '\'', '"', '\n', '\r', '\t')) {
                    imageLinks.add(new ImageLinkSpan(url, begin, end));
                }
            });
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage(), e);
        }

        try {
            agnTagService.collectTags(content, tag -> AGN_TAG_IMAGE.equals(tag.getTagName()) || AGN_TAG_IMGLINK.equals(tag.getTagName()))
                    .forEach(tag -> imageLinks.add(new ImageLinkSpan(tag.getName(), tag.getStartPos(), tag.getEndPos())));
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage(), e);
        }

        return imageLinks;
    }

    @Required
    public void setMailingDao( ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }

    @Required
    public void setRecipientDao(ComRecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }

    @Required
    public void setWorkerExecutorService(ExecutorService workerExecutorService) {
        this.workerExecutorService = workerExecutorService;
    }

    @Required
    public void setUndoMailingDao(ComUndoMailingDao undoMailingDao) {
        this.undoMailingDao = undoMailingDao;
    }

    @Required
    public void setUndoMailingComponentDao(ComUndoMailingComponentDao undoMailingComponentDao) {
        this.undoMailingComponentDao = undoMailingComponentDao;
    }

    @Required
    public void setUndoDynContentDao(ComUndoDynContentDao undoDynContentDao) {
        this.undoDynContentDao = undoDynContentDao;
    }

    @Required
    public void setDynamicTagDao(DynamicTagDao dynamicTagDao) {
        this.dynamicTagDao = dynamicTagDao;
    }

    @Required
    public void setTargetService(ComTargetService targetService) {
        this.targetService = targetService;
    }

    @Required
	public void setMaildropService(MaildropService maildropService) {
		this.maildropService = maildropService;
	}

	@Required
    public void setAgnTagService(AgnTagService agnTagService) {
        this.agnTagService = agnTagService;
    }

    @Required
    public void setMailingComponentsService(ComMailingComponentsService mailingComponentsService) {
        this.mailingComponentsService = mailingComponentsService;
    }

    @Required
    public void setLinkService(LinkService linkService) {
        this.linkService = linkService;
    }

    @Required
    public void setDynamicTagContentFactory(DynamicTagContentFactory dynamicTagContentFactory) {
        this.dynamicTagContentFactory = dynamicTagContentFactory;
    }

    @Required
    public void setDynamicTagContentDao(DynamicTagContentDao dynamicTagContentDao) {
        this.dynamicTagContentDao = dynamicTagContentDao;
    }

    @Override
    public DataSource getDataSource() {
        return recipientDao.getDataSource();
    }
    
    @Required
    public void setAgnDynTagGroupResolverFactory(AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory) {
        this.agnDynTagGroupResolverFactory = agnDynTagGroupResolverFactory;
    }
    
    @Required
    public void setGridServiceWrapper(GridServiceWrapper gridServiceWrapper) {
        this.gridServiceWrapper = gridServiceWrapper;
    }
    
    public static class Block {
        private String name;
        private String content;
        private Markup markup;

        public Block(String name, String content) {
            this.name = name;
            this.content = content;
        }

        public String getName() {
            return name;
        }

        public String getContent() {
            return content;
        }

        public Markup getMarkup() {
            return markup;
        }

        public void setMarkup(Markup markup) {
            this.markup = markup;
        }

        @Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((content == null) ? 0 : content.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Block other = (Block) obj;
			
			if (content == null) {
				if (other.content != null) {
					return false;
				}
			} else if (!content.equals(other.content)) {
				return false;
			}
			
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			
			return true;
		}
    }

    private static class Markup {
        private long ownSize;
        private Set<String> ownImages;
        private List<String> tags;

        private Markup() {
            this(0, Collections.emptySet(), Collections.emptyList());
        }

        private Markup(long ownSize, Set<String> ownImages, List<String> tags) {
            this.ownSize = ownSize;
            this.ownImages = ownImages;
            this.tags = tags;
        }

        public long getOwnSize() {
            return ownSize;
        }

        public Set<String> getOwnImages() {
            return ownImages;
        }

        public List<String> getTags() {
            return tags;
        }
    }

    private static class ImageLinkSpan extends Span {
        private final String link;

        public ImageLinkSpan(String link, int begin, int end) {
            super(begin, end);
            this.link = link;
        }

        public String getLink() {
            return link;
        }
    }

    private class MailingSizeCalculation {
        private Mailing mailing;
        private Map<String, List<Block>> blockMap;
        private Map<String, Integer> imageSizeMap;

        public MailingSizeCalculation(Mailing mailing) {
            this.mailing = mailing;
            this.blockMap = getBlockMap(mailing.getDynTags().values());
        }

        public Tuple<Long, Long> calculate() {
            // Parse markup for each content block.
            for (List<Block> blocks : blockMap.values()) {
                for (Block block : blocks) {
                    if (StringUtils.isEmpty(block.getContent())) {
                        block.setMarkup(new Markup());
                    } else {
                        block.setMarkup(getMarkup(block.getContent()));
                    }
                }
            }

            Markup textMarkup = getMarkup(getContent(mailing.getTextTemplate()));
            Markup htmlMarkup = getMarkup(getContent(mailing.getHtmlTemplate()));

            Map<String, Integer> map1 = mailingComponentsService.getImageSizeMap(mailing.getCompanyID(), mailing.getId(), false);
            Map<String, Integer> map2 = mailingComponentsService.getImageSizeMap(mailing.getCompanyID(), mailing.getId(), true);

            // Without external images
            imageSizeMap = map1;
            long size1 = Math.max(calculate(textMarkup), calculate(htmlMarkup));

            if (map2.size() == map1.size()) {
                return new Tuple<>(size1, size1);
            }

            // With external images
            imageSizeMap = map2;
            long size2 = Math.max(calculate(textMarkup), calculate(htmlMarkup));

            return new Tuple<>(size1, size2);
        }

        private long calculate(Markup root) {
            Deque<String> tagStack = new ArrayDeque<>();
            Map<String, Block> selection = new HashMap<>();
            Map<String, Integer> imageStack = new HashMap<>();

            long contentSize = evaluate(root, tagStack, selection, imageStack);
            long imagesSize = 0;

            for (String image : root.getOwnImages()) {
                imagesSize += imageSizeMap.getOrDefault(image, 0);
            }

            for (Block block : selection.values()) {
                for (String image : block.getMarkup().getOwnImages()) {
                    imagesSize += imageSizeMap.getOrDefault(image, 0);
                }
            }

            return Math.round(contentSize * QUOTED_PRINTABLE_OVERHEAD_RATIO) + Math.round(imagesSize * BASE64_OVERHEAD_RATIO);
        }

        private long evaluate(Markup markup, Deque<String> tagStack, Map<String, Block> selection, Map<String, Integer> imageStack) {
            long size = markup.getOwnSize();

            for (String image : markup.getOwnImages()) {
                increase(imageStack, image);
            }

            for (String tag : markup.getTags()) {
                Block block = selection.get(tag);

                // Handle cyclic dependency by ignoring tag.
                if (!tagStack.contains(tag)) {
                    tagStack.push(tag);
                    if (block == null) {
                        long maxBlockSize = 0;

                        // Evaluate size and select the biggest content block.
                        for (Block candidate : blockMap.getOrDefault(tag, Collections.emptyList())) {
                            long blockSize = evaluate(candidate.getMarkup(), tagStack, selection, imageStack);
                            if (blockSize > maxBlockSize || block == null) {
                                maxBlockSize = blockSize;
                                block = candidate;
                            }
                        }

                        // Remember the choice.
                        if (block != null) {
                            selection.put(tag, block);
                            size += maxBlockSize;
                        }
                    } else {
                        size += evaluate(block.getMarkup(), tagStack, selection, imageStack);
                    }
                    tagStack.pop();
                }
            }

            for (String image : markup.getOwnImages()) {
                decrease(imageStack, image);
            }

            return size;
        }

        private String getContent(MailingComponent root) {
            if (root == null || StringUtils.isEmpty(root.getEmmBlock())) {
                return null;
            }

            return root.getEmmBlock();
        }

        private Markup getMarkup(String content) {
            if (StringUtils.isEmpty(content)) {
                return new Markup();
            }

            content = AgnTagUtils.unescapeAgnTags(content);

            // Collect agn-tags representing dyn-tags (agnDYN/agnDVALUE) and image tags (agnIMAGE).
            List<DynamicTag> dynamicTags = getDynamicTags(content);
            List<ImageLinkSpan> imageLinks = getImageLinks(content);

            // Own content size (to be reduced by number of characters taken by tag lexemes).
            long ownSize = AgnUtils.countBytes(content);

            // Erase content (simple text/html or other agn-tags) embraced with disabled (not defined by user) dynamic tags (if any).
            if (dynamicTags.size() > 0) {
                List<Span> excludedSpans = collectExcludedSpans(dynamicTags);
                Iterator<DynamicTag> iterator = dynamicTags.iterator();

                if (excludedSpans.isEmpty()) {
                    // Reduce own content size by number of characters taken by tag lexemes.
                    while (iterator.hasNext()) {
                        DynamicTag tag = iterator.next();

                        ownSize -= AgnUtils.countBytes(content, tag.getStartTagStart(), tag.getStartTagEnd());  // Opening agnDYN
                        ownSize -= AgnUtils.countBytes(content, tag.getValueTagStart(), tag.getValueTagEnd());  // agnDVALUE
                        ownSize -= AgnUtils.countBytes(content, tag.getEndTagStart(), tag.getEndTagEnd());  // Closing agnDYN

                        // Case when there are opening and closing agn-dyn tags but agnDVALUE tag is missing.
                        if (!tag.isStandaloneTag() && tag.getValueTagStart() == tag.getValueTagEnd()) {
                            // Missing agnDVALUE tag means that block content is not inserted
                            // and should be ignored when calculating content size.
                            iterator.remove();
                        }
                    }
                } else {
                    Comparator<Span> comparator = getSubSpanComparator();

                    while (iterator.hasNext()) {
                        DynamicTag tag = iterator.next();

                        if (tag.isStandaloneTag()) {
                            Span span = new Span(tag.getStartTagStart(), tag.getStartTagEnd());

                            // Check if current standalone tag is placed within excluded span.
                            if (Collections.binarySearch(excludedSpans, span, comparator) >= 0) {
                                iterator.remove();
                            }
                        } else {
                            Span valueTagSpan = new Span(tag.getValueTagStart(), tag.getValueTagEnd());
                            Span embracingSpan = new Span(tag.getStartTagStart(), tag.getEndTagEnd());

                            // Case when there are opening and closing agn-dyn tags but agnDVALUE tag is missing.
                            if (valueTagSpan.getBegin() == valueTagSpan.getEnd()) {
                                // Missing agnDVALUE tag means that block content is not inserted
                                // and should be ignored when calculating content size.
                                iterator.remove();

                                // Check if opening and closing agn-dyn tags are NOT placed withing excluded span.
                                if (Collections.binarySearch(excludedSpans, embracingSpan, comparator) < 0) {
                                    ownSize -= AgnUtils.countBytes(content, tag.getStartTagStart(), tag.getStartTagEnd());  // Opening agnDYN
                                    ownSize -= AgnUtils.countBytes(content, tag.getEndTagStart(), tag.getEndTagEnd());  // Closing agnDYN
                                }
                            } else {
                                int index = Collections.binarySearch(excludedSpans, valueTagSpan, comparator);

                                // Check if agnDVALUE tag is placed withing excluded span.
                                if (index >= 0) {
                                    Span excludedSpan = excludedSpans.get(index);

                                    // Check if opening and closing agn-dyn tags are NOT placed within excluded span.
                                    if (!excludedSpan.contains(embracingSpan)) {
                                        ownSize -= AgnUtils.countBytes(content, tag.getStartTagStart(), tag.getStartTagEnd());  // Opening agnDYN
                                        ownSize -= AgnUtils.countBytes(content, tag.getEndTagStart(), tag.getEndTagEnd());  // Closing agnDYN
                                    }

                                    iterator.remove();
                                }
                            }
                        }
                    }

                    // Reduce own content size by number of characters taken by disabled tags.
                    for (Span span : excludedSpans) {
                        ownSize -= AgnUtils.countBytes(content, span.getBegin(), span.getEnd());
                    }

                    // Reduce own content size by number of characters taken by tag lexemes (outside of excluded spans).
                    for (DynamicTag tag : dynamicTags) {
                        ownSize -= AgnUtils.countBytes(content, tag.getStartTagStart(), tag.getStartTagEnd());  // Opening agnDYN
                        ownSize -= AgnUtils.countBytes(content, tag.getValueTagStart(), tag.getValueTagEnd());  // agnDVALUE
                        ownSize -= AgnUtils.countBytes(content, tag.getEndTagStart(), tag.getEndTagEnd());  // Closing agnDYN
                    }

                    imageLinks.removeIf(span -> Collections.binarySearch(excludedSpans, span, comparator) >= 0);
                }
            }

            return new Markup(ownSize, getUniqueImageNames(imageLinks), getDynNames(dynamicTags));
        }

        private Map<String, List<Block>> getBlockMap(Collection<DynamicTag> tags) {
            Map<String, List<Block>> newBlockMap = new HashMap<>();

            for (DynamicTag tag : tags) {
                List<DynamicTagContent> contents = new ArrayList<>(tag.getDynContent().values());

                if (contents.size() > 0) {
                    List<Block> blocks = new ArrayList<>(contents.size());

                    contents.sort(Comparator.comparingInt(DynamicTagContent::getDynOrder));

                    for (DynamicTagContent content : contents) {
                        blocks.add(new Block(tag.getDynName(), content.getDynContent()));

                        // Exclude all unreachable blocks (placed below "All recipients" block).
                        if (content.getTargetID() <= 0) {
                            break;
                        }
                    }

                    newBlockMap.put(tag.getDynName(), blocks);
                }
            }

            return newBlockMap;
        }

        private List<Span> collectExcludedSpans(List<DynamicTag> dynamicTags) {
            List<Span> excludedSpans = new ArrayList<>(dynamicTags.size());

            // Collect content spans to be erased (if any).
            for (DynamicTag tag : dynamicTags) {
                if (!blockMap.containsKey(tag.getDynName())) {
                    Span span = new Span(tag.getStartTagStart(), tag.isStandaloneTag() ? tag.getStartTagEnd() : tag.getEndTagEnd());

                    if (excludedSpans.isEmpty()) {
                        excludedSpans.add(span);
                    } else {
                        Span previousSpan = excludedSpans.get(excludedSpans.size() - 1);
                        // Ignore sub-spans of excluded spans.
                        if (previousSpan.getEnd() <= span.getBegin()) {
                            excludedSpans.add(span);
                        }
                    }
                }
            }

            return excludedSpans;
        }

        private Set<String> getUniqueImageNames(List<ImageLinkSpan> imageLinks) {
            return imageLinks.stream().map(ImageLinkSpan::getLink)
                    .collect(Collectors.toSet());
        }

        private List<String> getDynNames(List<DynamicTag> tags) {
            return tags.stream().map(DynamicTag::getDynName)
                    .collect(Collectors.toList());
        }

        private Comparator<Span> getSubSpanComparator() {
            // This comparator is going to find a span that contains or matches a searched span.
            return (superSpan, subSpan) -> {
                if (subSpan.getBegin() < superSpan.getBegin()) {
                    return +1;
                }

                if (subSpan.getEnd() > superSpan.getEnd()) {
                    return -1;
                }

                return 0;
            };
        }

        private void increase(Map<String, Integer> map, String key) {
            map.merge(key, 1, Integer::sum);
        }

        private void decrease(Map<String, Integer> map, String key) {
            Integer value = map.get(key);

            if (value != null) {
                if (value > 1) {
                    map.put(key, value - 1);
                } else {
                    map.remove(key);
                }
            }
        }
    }
    
	@Override
    public List<LightweightMailing> getMailingsByType(MailingTypes type, @VelocityCheck int companyId) {
        return getMailingsByType(type, companyId, true);
    }
    
    @Override
    public List<LightweightMailing> getMailingsByType(MailingTypes type, int companyId, boolean includeInactive) {
        List<LightweightMailing> mailingLists = null;
        if(type != null) {
            mailingLists = mailingDao.getMailingsByType(type.getCode(), companyId, includeInactive);
        }
        
        if (mailingLists == null) {
            mailingLists = Collections.emptyList();
        }

        return mailingLists;
    }
    
    @Override
    public MailingSendStatus getMailingSendStatus(int mailingId, int companyId) {
        return mailingDao.getMailingSendStatus(mailingId, companyId);
    }
    
    @Override
    public int getMailingType(int mailingId) {
        return mailingDao.getMailingType(mailingId);
    }
    
    @Override
    public Timestamp getMailingLastSendDate(int mailingId) {
        return mailingDao.getLastSendDate(mailingId);
    }

    @Override
    public SimpleServiceResult checkContentNotBlank(Mailing mailing) {
        MediatypeEmail emailMediaType = mailing.getEmailParam();

        // Check if email media type is selected at all.
        if (Mediatype.isActive(emailMediaType)) {
            // If "text only" format selected then text version is required.
            if (MailingModel.Format.TEXT.getCode() == emailMediaType.getMailFormat()) {
                if (isContentBlank(mailing.getTextTemplate(), mailing.getDynTags())) {
                    return SimpleServiceResult.simpleError(Message.of("error.mailing.no_text_version"));
                } else {
                    return SimpleServiceResult.simpleSuccess();
                }
            } else {
                List<Message> warnings = new ArrayList<>();
                List<Message> errors = new ArrayList<>();

                // Check text version.
                if (isContentBlank(mailing.getTextTemplate(), mailing.getDynTags())) {
                    // Text version is required for all new mailings (see GWUA-3991).
                    if (mailingDao.isTextVersionRequired(mailing.getCompanyID(), mailing.getId())) {
                        errors.add(Message.of("error.mailing.no_text_version"));
                    } else {
                        warnings.add(Message.of("error.mailing.no_text_version"));
                    }
                }

                // Check HTML version.
                if (isContentBlank(mailing.getHtmlTemplate(), mailing.getDynTags())) {
                    errors.add(Message.of("error.mailing.no_html_version"));
                }

                return new SimpleServiceResult(errors.isEmpty(), Collections.emptyList(), warnings, errors);
            }
        } else {
            // Check if no media type is selected at all.
            if (mailing.getMediatypes().values().stream().noneMatch(Mediatype::isActive)) {
                return SimpleServiceResult.simpleError(Message.of("error.mailing.mediatype.none"));
            } else {
                return SimpleServiceResult.simpleSuccess();
            }
        }
    }

    private boolean isContentBlank(MailingComponent template, Map<String, DynamicTag> contentMap) {
        if (template == null) {
            return true;
        }

        return isContentBlank(template.getEmmBlock(), contentMap);
    }

    @Override
    public boolean activateTrackingLinksOnEveryPosition(ComAdmin admin, Mailing mailing, Set<Integer> bulkLinkIds, ApplicationContext context) throws Exception {
        List<String> links = mailing.getTrackableLinks().values().stream()
                .filter(Objects::nonNull)
                .filter((link) -> bulkLinkIds.contains(link.getId()))
                .map(TrackableLink::getFullUrl)
                .collect(Collectors.toList());

        List<String> measuredSeparatelyLinks = mailing.replaceAndGetMeasuredSeparatelyLinks(links, context);
        mailing.buildDependencies(true, context);
        AtomicBoolean changed = new AtomicBoolean(false);
        mailing.getTrackableLinks().forEach((key, link) -> {
            if (measuredSeparatelyLinks.contains(link.getFullUrl()) ||
                    links.contains(link.getFullUrl())) {
                link.setMeasureSeparately(true);
                changed.set(true);
            }
        });
        saveMailingWithUndo(mailing, admin.getAdminID(), false);

        return changed.get();
    }
}
