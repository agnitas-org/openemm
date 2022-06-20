/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.agnitas.actions.EmmAction;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.factory.DynamicTagContentFactory;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailingcontent.dto.DynContentDto;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.service.MailingContentService;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;

@Service
public class MailingContentServiceImpl implements MailingContentService {

    private DynamicTagContentFactory dynamicTagContentFactory;
    private ComMailingBaseService mailingBaseService;
    private ConversionService conversionService;
    private ApplicationContext applicationContext;
    private ComMailingDao mailingDao;
    private EmmActionDao actionDao;
    private DynamicTagDao dynamicTagDao;

    public MailingContentServiceImpl(DynamicTagContentFactory dynamicTagContentFactory,
                                     ComMailingBaseService mailingBaseService,
                                     ConversionService conversionService,
                                     ApplicationContext applicationContext,
                                     ComMailingDao mailingDao,
                                     EmmActionDao actionDao,
                                     DynamicTagDao dynamicTagDao) {
        this.dynamicTagContentFactory = dynamicTagContentFactory;
        this.mailingBaseService = mailingBaseService;
        this.conversionService = conversionService;
        this.applicationContext = applicationContext;
        this.mailingDao = mailingDao;
        this.actionDao = actionDao;
        this.dynamicTagDao = dynamicTagDao;
    }

    @Override
    public ServiceResult<List<UserAction>> updateDynContent(Mailing mailing, DynTagDto dynTagDto, ComAdmin admin) throws Exception {
        DynamicTag oldDynamicTag = mailing.getDynamicTagById(dynTagDto.getId());
        DynamicTag newDynamicTag = convertDynTagDtoToDynamicTag(admin.getCompanyID(), oldDynamicTag, dynTagDto);

        mailing.getDynTags().replace(newDynamicTag.getDynName(), newDynamicTag);
        List<EmmAction> actions = actionDao.getEmmActions(admin.getCompanyID());
        mailing.setPossibleActions(actions);
        final ActionMessages errors = new ActionMessages();
        mailing.buildDependencies(false, null, applicationContext, null, errors, admin);
        if(!errors.isEmpty()) {
            final List<Message> messages = new ArrayList<>();
            @SuppressWarnings("unchecked")
            Iterator<ActionMessage> iterator = errors.get();
            iterator.forEachRemaining(err-> messages.add(Message.of(err.getKey(), err.getValues())));
            return ServiceResult.error(messages);
        }
        boolean hasNoCleanPermission = admin.permissionAllowed(Permission.MAILING_TRACKABLELINKS_NOCLEANUP);
        mailingBaseService.saveMailingWithUndo(mailing, admin.getAdminID(), hasNoCleanPermission);
        mailingDao.updateStatus(mailing.getId(), MailingStatus.EDIT);

        List<UserAction> userActions = getUserActions(oldDynamicTag, newDynamicTag, mailing);
        dynamicTagDao.removeAbsentDynContent(oldDynamicTag, newDynamicTag);
        return ServiceResult.success(userActions);
    }

    @Override
    public DynTagDto getDynTag(@VelocityCheck int companyId, int dynNameId) {
        DynamicTag tag = mailingBaseService.getDynamicTag(companyId, dynNameId);

        if (tag == null) {
            return null;
        }

        return conversionService.convert(tag, DynTagDto.class);
    }

    private List<Integer> getCreatedId(DynamicTag oldDynamicTag, DynamicTag newDynamicTag) {
        Set<Integer> oldIds = oldDynamicTag.getDynContent().values().stream()
                .map(DynamicTagContent::getId)
                .collect(Collectors.toSet());

        Set<Integer> newIds = newDynamicTag.getDynContent().values().stream()
                .map(DynamicTagContent::getId)
                .collect(Collectors.toSet());

        return newIds.stream().filter(oldId -> !oldIds.contains(oldId)).collect(Collectors.toList());
    }

    private List<Integer> getUpdateContentIds(DynamicTag oldDynamicTag, DynamicTag newDynamicTag) {
        return oldDynamicTag.getDynContent().values().stream()
                .filter((oldDynContent) -> {
                    DynamicTagContent newDynContent = newDynamicTag.getDynContentID(oldDynContent.getId());
                    return Objects.nonNull(newDynContent) && !oldDynContent.getDynContent().equals(newDynContent.getDynContent());
                })
                .map(DynamicTagContent::getId)
                .collect(Collectors.toList());
    }

    private List<Integer> getUpdateTargetGroupIds(DynamicTag oldDynamicTag, DynamicTag newDynamicTag) {
        return oldDynamicTag.getDynContent().values().stream()
                .filter((oldDynContent) -> {
                    DynamicTagContent newDynContent = newDynamicTag.getDynContentID(oldDynContent.getId());
                    return Objects.nonNull(newDynContent) && oldDynContent.getTargetID() != newDynContent.getTargetID();
                })
                .map(DynamicTagContent::getId)
                .collect(Collectors.toList());
    }

    private DynamicTag convertDynTagDtoToDynamicTag(int companyId, DynamicTag oldDynamicTag, DynTagDto dynTagDto) {
        DynamicTag clonedDynTag = oldDynamicTag.clone();
        clonedDynTag.setId(dynTagDto.getId());
        clonedDynTag.setDynName(dynTagDto.getName());
        clonedDynTag.setMailingID(dynTagDto.getMailingId());
        clonedDynTag.setDynInterestGroup(dynTagDto.getInterestGroup());
        clonedDynTag.setCompanyID(companyId);

        Map<Integer, DynamicTagContent> dynamicTagContentMap = dynTagDto.getContentBlocks().stream()
                .map(content -> {
                    DynamicTagContent oldContent = oldDynamicTag.getDynContentID(content.getId());
                    return convertDynContentDtoToDynamicTagContent(companyId, oldContent, content, dynTagDto);
                })
                .collect(Collectors.toMap(DynamicTagContent::getDynOrder, Function.identity()));

        clonedDynTag.setDynContent(dynamicTagContentMap);
        return clonedDynTag;
    }

    private List<UserAction> getUserActions(DynamicTag oldDynamicTag, DynamicTag newDynamicTag, Mailing mailing) {
        ArrayList<UserAction> userActions = new ArrayList<>();
        final String formatPattern = "%s (%d) %s %s %s (%d)";

        String mailingEntityName = mailing.isIsTemplate() ? "template" : "mailing";
        String mailingShortname = mailing.getShortname();
        int mailingId = mailing.getId();

        List<Integer> idForRemoving = getIdForRemoving(oldDynamicTag, newDynamicTag);
        idForRemoving.forEach((removedId) -> {
            final Object[] formatParameter = new Object[] {null, null, null, mailingEntityName, mailingShortname, mailingId};
            formatParameter[0] = oldDynamicTag.getDynName();
            formatParameter[1] = removedId;
            formatParameter[2] = "from";
            userActions.add(new UserAction("delete textblock", String.format(formatPattern, formatParameter)));
        });

        List<Integer> updateContentIds = getUpdateContentIds(oldDynamicTag, newDynamicTag);
        updateContentIds.forEach((updatedContentId) -> {
            final Object[] formatParameter = new Object[] {null, null, null, mailingEntityName, mailingShortname, mailingId};
            formatParameter[0] = oldDynamicTag.getDynName();
            formatParameter[1] = updatedContentId;
            formatParameter[2] = "from";
            userActions.add(new UserAction("edit textblock content", String.format(formatPattern, formatParameter)));
        });

        List<Integer> updateTargetGroupIds = getUpdateTargetGroupIds(oldDynamicTag, newDynamicTag);
        updateTargetGroupIds.forEach((updatedTargetId) -> {
            final Object[] formatParameter = new Object[] {null, null, null, mailingEntityName, mailingShortname, mailingId};
            formatParameter[0] = oldDynamicTag.getDynName();
            formatParameter[1] = updatedTargetId;
            formatParameter[2] = "from";
            userActions.add(new UserAction("edit textblock target group", String.format(formatPattern, formatParameter)));
        });

        List<Integer> createdIds = getCreatedId(oldDynamicTag, newDynamicTag);
        createdIds.forEach((createdId) -> {
            final Object[] formatParameter = new Object[] {null, null, null, mailingEntityName, mailingShortname, mailingId};
            formatParameter[0] = newDynamicTag.getDynName();
            formatParameter[1] = createdId;
            formatParameter[2] = "in the";
            userActions.add(new UserAction("create textblock", String.format(formatPattern, formatParameter)));
        });

        return userActions;
    }

    private List<Integer> getIdForRemoving(DynamicTag oldDynamicTag, DynamicTag newDynamicTag) {
        Set<Integer> oldIds = oldDynamicTag.getDynContent().values().stream()
                .map(DynamicTagContent::getId)
                .collect(Collectors.toSet());

        Set<Integer> newIds = newDynamicTag.getDynContent().values().stream()
                .map(DynamicTagContent::getId)
                .collect(Collectors.toSet());

        return oldIds.stream().filter(oldId -> !newIds.contains(oldId)).collect(Collectors.toList());
    }

    private DynamicTagContent convertDynContentDtoToDynamicTagContent(int companyId, DynamicTagContent oldDynContent, DynContentDto dynContentDto, DynTagDto dynTagDto) {
        DynamicTagContent clonedDynContent;
        if (Objects.nonNull(oldDynContent)) {
            clonedDynContent = oldDynContent.clone();
        } else {
            clonedDynContent  = dynamicTagContentFactory.newDynamicTagContent();
        }

        clonedDynContent.setId(dynContentDto.getId());
        clonedDynContent.setDynOrder(dynContentDto.getIndex());
        clonedDynContent.setDynContent(dynContentDto.getContent());
        clonedDynContent.setTargetID(dynContentDto.getTargetId());

        clonedDynContent.setCompanyID(companyId);
        clonedDynContent.setDynName(dynTagDto.getName());
        clonedDynContent.setDynNameID(dynTagDto.getId());
        clonedDynContent.setMailingID(dynTagDto.getMailingId());

        return clonedDynContent;
    }
}
