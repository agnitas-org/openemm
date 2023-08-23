/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComUndoDynContent;
import com.agnitas.beans.ComUndoMailing;
import com.agnitas.beans.ComUndoMailingComponent;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComUndoDynContentDao;
import com.agnitas.dao.ComUndoMailingComponentDao;
import com.agnitas.dao.ComUndoMailingDao;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.bean.MailingRecipientStatRow;
import com.agnitas.emm.core.mailing.dto.CalculationRecipientsConfig;
import com.agnitas.emm.core.mailing.service.CalculationRecipients;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.messages.Message;
import com.agnitas.service.AgnDynTagGroupResolverFactory;
import com.agnitas.service.AgnTagService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.Span;
import com.agnitas.web.mvc.Popups;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingSendStatus;
import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.beans.TrackableLink;
import org.agnitas.beans.factory.DynamicTagContentFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.DynamicTagContentDao;
import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.exception.UnknownMailingIdException;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.util.DynTagException;
import org.agnitas.util.FulltextSearchInvalidQueryException;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.SafeString;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import java.util.stream.Collectors;

public class ComMailingBaseServiceImpl implements ComMailingBaseService {

    private static final Logger logger = LogManager.getLogger(ComMailingBaseServiceImpl.class);

    private ComMailingDao mailingDao;
    private GridServiceWrapper gridServiceWrapper;
    protected ComRecipientDao recipientDao;
    protected ExecutorService workerExecutorService;
    private ComUndoMailingDao undoMailingDao;
    private ComUndoMailingComponentDao undoMailingComponentDao;
    private ComUndoDynContentDao undoDynContentDao;
    private DynamicTagDao dynamicTagDao;
    protected ComTargetService targetService;
    private MaildropService maildropService;
    private AgnTagService agnTagService;
    private ComMailingComponentsService mailingComponentsService;
    private DynamicTagContentFactory dynamicTagContentFactory;
    private DynamicTagContentDao dynamicTagContentDao;
    private AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory;
    private MailinglistApprovalService mailinglistApprovalService;
    private ConfigService configService;

    @Override
    public boolean isMailingExists(int mailingId, int companyId) {
        return mailingId > 0 && companyId > 0 && mailingDao.exist(mailingId, companyId);
    }

    @Override
    public boolean isMailingExists(int mailingId, int companyId, boolean isTemplate) {
        return mailingId > 0 && companyId > 0 && mailingDao.exist(mailingId, companyId, isTemplate);
    }

    @Override
    public boolean checkUndoAvailable(int mailingId) {
        return undoMailingDao.getLastUndoData(mailingId) != null;
    }

    @Override
    public boolean isTextTemplateExists(Admin admin, int mailingId) {
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
    public void bulkDelete(Set<Integer> mailingsIds, int companyId) {
        for (int mailingId : mailingsIds) {
            deleteMailing(mailingId, companyId);
        }
    }

    @Override
    public boolean deleteMailing(int mailingId, int companyId) {
        if (mailingDao.markAsDeleted(mailingId, companyId)) {
            undoDynContentDao.deleteUndoDataForMailing(mailingId);
            undoMailingComponentDao.deleteUndoDataForMailing(mailingId);
            undoMailingDao.deleteUndoDataForMailing(mailingId);

            return true;
        }

        return false;
    }

    @Override
    public int getWorkflowId(int mailingId, int companyId) {
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
                deleteUndoDataOverLimit(mailingId);
            }
        }
        return mailingDao.saveMailing(mailing, preserveTrackableLinks);
    }

    private void deleteUndoDataOverLimit(int mailingId) {
        final int undoId = undoMailingDao.getUndoIdOverLimit(mailingId, configService.getIntegerValue(ConfigValue.MailingUndoLimit));
        if (undoId != 0) {
            undoMailingComponentDao.deleteUndoDataOverLimit(mailingId, undoId);
            undoDynContentDao.deleteUndoDataOverLimit(mailingId, undoId);
            undoMailingDao.deleteUndoDataOverLimit(mailingId, undoId);
        }
    }

    @Override
    public void restoreMailingUndo(ApplicationContext ctx, int mailingId, int companyId) throws Exception {
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
    public boolean setTargetGroups(int mailingId, int companyId, Collection<Integer> targetGroupIds, boolean conjunction) throws TooManyTargetGroupsInMailingException {
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
    public String getMailingName(int mailingId, int companyId) {
        return StringUtils.defaultString(mailingDao.getMailingName(mailingId, companyId));
    }

    @Override
    public PaginatedListImpl<Map<String, Object>> getPaginatedMailingsData(Admin admin, MailingsListProperties props) {
        if (admin == null || Objects.isNull(props)) {
            return new PaginatedListImpl<>();
        }

        try {
            return mailingDao.getMailingList(admin, props);
        } catch (FulltextSearchInvalidQueryException e) {
            e.printStackTrace();
        }

        return new PaginatedListImpl<>(new ArrayList<>(), 0, props.getRownums(), props.getPage(), "senddate", true);
    }

    @Override
    public PaginatedListImpl<MailingRecipientStatRow> getMailingRecipients(int mailingId, int companyId, int filterType, int pageNumber, int rowsPerPage, String sortCriterion, boolean sortAscending, List<String> columns) throws Exception {
        throw new UnsupportedOperationException("Get mailing recipients is unsupported.");
    }

    @Override
    public List<DynamicTag> getDynamicTags(int mailingId, int companyId) {
        return dynamicTagDao.getDynamicTags(mailingId, companyId, false);
    }

    @Override
    public List<DynamicTag> getDynamicTags(int mailingId, int companyId, boolean resetIds) {
        List<DynamicTag> tags = dynamicTagDao.getDynamicTags(mailingId, companyId, false);
        if (resetIds) {
            for (DynamicTag tag : tags) {
                resetIds(tag, companyId);
            }
        }
        return tags;
    }

    @Override
    public List<String> getDynamicTagNames(Mailing mailing) {
        if (mailing == null || mailing.getHtmlTemplate() == null) {
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
    public DynamicTag getDynamicTag(int companyId, int dynNameId) {
        return dynamicTagDao.getDynamicTag(dynNameId, companyId);
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
    public boolean isAdvertisingContentType(int companyId, int mailingId) {
        return mailingDao.isAdvertisingContentType(companyId, mailingId);
    }

    @Override
    public boolean isLimitedRecipientOverview(Admin admin, int mailingId) {
        try {
            int companyId = admin.getCompanyID();
            Mailing mailing = mailingDao.getMailing(mailingId, companyId);

            boolean isSentStatus;

            if (mailing.getMailingType() == MailingType.INTERVAL) {
                String workStatus = mailingDao.getWorkStatus(companyId, mailingId);
                isSentStatus = StringUtils.equals(workStatus, MailingStatus.ACTIVE.getDbKey());
            } else {
                isSentStatus = maildropService.isActiveMailing(mailingId, companyId);
            }
            return isSentStatus && !mailinglistApprovalService.isAdminHaveAccess(admin, mailing.getMailinglistID());
        } catch (MailingNotExistException ex) {
            return false;
        }
    }

    @Lookup
    public UnsavedChanges getForUnsavedChanges() {
        return null;
    }

    @Lookup
    public DefaultCalculation getDefaultCalculation() {
        return null;
    }

    @Override
    public int calculateRecipients(int companyId, int mailingListId, int splitId, Collection<Integer> altgIds, Collection<Integer> targetGroupIds, boolean conjunction) throws Exception {
        String sqlCondition = getSqlConditionToCalculateRecipients(altgIds, targetGroupIds, conjunction, splitId, companyId);
        return recipientDao.getNumberOfRecipients(companyId, mailingListId, sqlCondition);
    }

    protected String getSqlConditionToCalculateRecipients(Collection<Integer> altgIds, Collection<Integer> targetGroupIds, boolean conjunction, int splitId, int companyId) {
        return targetService.getSQLFromTargetExpression(TargetExpressionUtils.makeTargetExpression(targetGroupIds, conjunction), splitId, companyId);
    }

    @Override
    public int calculateRecipients(int companyId, int mailingId, int mailingListId, int splitId) throws Exception {
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);
        
        String sqlCondition = targetService.getSQLFromTargetExpression(mailingDao.getTargetExpression(mailingId, companyId), splitId, companyId);

		List<MediaTypes> activeMediaTypes = mailing.getMediatypes().values().stream()
			.filter(x -> x.getStatus() == MediaTypeStatus.Active.getCode())
			.map(x -> x.getMediaType())
			.collect(Collectors.toList());
		
        return recipientDao.getNumberOfRecipients(companyId, mailingListId, activeMediaTypes, sqlCondition);
    }

    @Override
    public int calculateRecipients(int companyId, int mailingId) throws Exception {
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);

        if (mailing == null || mailing.getId() <= 0) {
            throw new UnknownMailingIdException("Mailing #" + mailingId + " doesn't exist");
        }

        String sqlCondition = targetService.getSQLFromTargetExpression(mailing, true);
        
		List<MediaTypes> activeMediaTypes = mailing.getMediatypes().values().stream()
			.filter(x -> x.getStatus() == MediaTypeStatus.Active.getCode())
			.map(x -> x.getMediaType())
			.collect(Collectors.toList());
        
        return recipientDao.getNumberOfRecipients(companyId, mailing.getMailinglistID(), activeMediaTypes, sqlCondition);
    }

    @Override
    public boolean isContentBlank(String content, Map<String, DynamicTag> contentMap) {
        if (StringUtils.isBlank(content)) {
            return true;
        }

        return isContentBlank(content, contentMap, new ArrayDeque<>());
    }

    @Override
    public void doTextTemplateFilling(Mailing mailing, Admin admin, Popups popups) {
        doTextTemplateFilling(mailing, admin, null, popups);
    }

    @Override
    public void doTextTemplateFilling(Mailing mailing, Admin admin, ActionMessages messages) {
        doTextTemplateFilling(mailing, admin, messages, null);
    }

    public void doTextTemplateFilling(Mailing mailing, Admin admin, ActionMessages messages, Popups popups) {
        MailingComponent componentTextTemplate = mailing.getTextTemplate();
        if (Objects.isNull(componentTextTemplate)) {
            logger.error("Text template is absent. mailingId: " + mailing.getId());
            return;
        }

        // text template substitution in case of template completely empty
        String textTemplate = componentTextTemplate.getEmmBlock();
        if (StringUtils.isBlank(textTemplate)) {
            componentTextTemplate.setEmmBlock(SafeString.getLocaleString("mailing.textversion.default", admin.getLocale()), "text/plain");
            if (popups != null) {
                popups.warning("mailing.textversion.empty");
            } else {
                messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("mailing.textversion.empty"));
            }
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
                if (popups != null) {
                    popups.warning("mailing.textversion.empty");
                } else {
                    messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("mailing.textversion.empty"));
                }
                return;
            }
        }
    }

    @Override
    public Mailing getMailing(int companyId, int mailingId) {
        if (mailingId > 0 && companyId > 0) {
            return mailingDao.getMailing(mailingId, companyId);
        }

        return null;
    }

    @Override
    public List<MailingBase> getMailingsForComparison(Admin admin) {
        return mailingDao.getMailingsForComparation(admin);
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

            if (tag.getEndTagStart() != tag.getEndTagEnd()) {
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

    @Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
        this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
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

    @Override
    public List<LightweightMailing> getMailingsByType(MailingType type, int companyId) {
        return getMailingsByType(type, companyId, true);
    }

    @Override
    public List<LightweightMailing> getMailingsByType(MailingType type, int companyId, boolean includeInactive) {
        List<LightweightMailing> mailingLists = null;
        if (type != null) {
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
    public MailingType getMailingType(int mailingId) throws Exception {
        return mailingDao.getMailingType(mailingId);
    }

    @Override
    public Date getMailingLastSendDate(int mailingId) {
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
    public void activateTrackingLinksOnEveryPosition(Admin admin, Mailing mailing, ApplicationContext context) throws Exception {
        List<String> links = mailing.getTrackableLinks().values().stream()
                .filter(Objects::nonNull)
                .map(TrackableLink::getFullUrl)
                .collect(Collectors.toList());

        List<String> measuredSeparatelyLinks = mailing.replaceAndGetMeasuredSeparatelyLinks(links, context);
        mailing.buildDependencies(true, context);
        mailing.getTrackableLinks().values().forEach(link -> {
            if (measuredSeparatelyLinks.contains(link.getFullUrl()) || links.contains(link.getFullUrl())) {
                link.setMeasureSeparately(true);
            }
        });
        saveMailingWithUndo(mailing, admin.getAdminID(), false);
    }

	@Override
	public List<Integer> getMailingsSentBetween(int companyID, Date startDateIncluded, Date endDateExcluded) {
		return maildropService.getMailingsSentBetween(companyID, startDateIncluded, endDateExcluded);
	}
}
