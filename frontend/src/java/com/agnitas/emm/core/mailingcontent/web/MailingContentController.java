/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.web;

import java.util.List;

import javax.servlet.http.HttpSession;

import com.agnitas.emm.core.mailing.service.MailingService;
import org.agnitas.beans.Mailing;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.service.MailingContentService;
import com.agnitas.emm.core.mailingcontent.validator.DynTagChainValidator;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@PermissionMapping("mailing.content")
@RequestMapping("/mailingcontent")
public class MailingContentController {

    private static final Logger logger = Logger.getLogger(MailingContentController.class);

    private ExtendedConversionService extendedConversionService;
    private UserActivityLogService userActivityLogService;
    private MailingContentService mailingContentService;
    private DynTagChainValidator dynTagChainValidator;
    private PreviewImageService previewImageService;
    private final MailingService mailingService;

    public MailingContentController(ExtendedConversionService extendedConversionService,
                                    UserActivityLogService userActivityLogService,
                                    MailingContentService mailingContentService,
                                    DynTagChainValidator dynTagChainValidator,
                                    PreviewImageService previewImageService,
                                    final MailingService mailingService) {
        this.extendedConversionService = extendedConversionService;
        this.userActivityLogService = userActivityLogService;
        this.mailingContentService = mailingContentService;
        this.dynTagChainValidator = dynTagChainValidator;
        this.previewImageService = previewImageService;
        this.mailingService = mailingService;
    }

    @GetMapping("/name/{id:\\d+}/view.action")
    public ResponseEntity<DynTagDto> view(ComAdmin admin, @PathVariable("id") int dynNameId) {
        DynTagDto dto = mailingContentService.getDynTag(admin.getCompanyID(), dynNameId);

        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/save.action")
    public @ResponseBody
    DataResponseDto<DynTagDto> save(ComAdmin admin, HttpSession session, @RequestBody DynTagDto dynTagDto, Popups popups) {
        if (!dynTagChainValidator.validate(dynTagDto, popups, admin)) {
            return new DataResponseDto<>(popups, false);
        }

        Mailing mailing = mailingService.getMailing(admin.getCompanyID(), dynTagDto.getMailingId());

        try {
            // editing or creating
            ServiceResult<List<UserAction>> serviceResult =
                    mailingContentService.updateDynContent(mailing, dynTagDto, admin);
            if(!serviceResult.isSuccess()) {
                popups.addPopups(serviceResult);
                return new DataResponseDto<>(popups, false);
            } else {
                final List<UserAction> userActions = serviceResult.getResult();
                userActions.forEach(action -> userActivityLogService.writeUserActivityLog(admin, action));
                logger.info(String.format("Content of mailing was changed. mailing-name : %s, mailing-id: %d",
                        mailing.getDescription(), mailing.getId()));
            }
        } catch (Exception e) {
            logger.error(String.format("Error during building dependencies. mailing-name : %s, mailing-id: %d",
                    mailing.getDescription(), mailing.getId()), e);
            popups.alert("error.mailing.save", mailing.getShortname());
            return new DataResponseDto<>(popups, false);
        }

        previewImageService.generateMailingPreview(admin, session.getId(), mailing.getId(), true);
        DynamicTag dynamicTag = mailing.getDynTags().get(dynTagDto.getName());
        DynTagDto dynTagDtoResponse = extendedConversionService.convert(dynamicTag, DynTagDto.class);
        popups.success("default.changes_saved");
        return new DataResponseDto<>(dynTagDtoResponse, popups, true);
    }
}
