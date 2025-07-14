/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.web;

import static com.agnitas.beans.MailingComponentType.HostedImage;
import static com.agnitas.beans.impl.MailingComponentImpl.COMPONENT_NAME_MAX_LENGTH;
import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.PERMISSION_DENIED_MSG;
import static com.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipOutputStream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.components.dto.UploadMailingImageDto;
import com.agnitas.emm.core.components.form.MailingImagesFormSearchParams;
import com.agnitas.emm.core.components.form.MailingImagesOverviewFilter;
import com.agnitas.emm.core.components.form.UploadMailingImagesForm;
import com.agnitas.emm.core.components.service.MailingComponentsService;
import com.agnitas.emm.core.components.service.MailingComponentsService.ImportStatistics;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.util.html.xssprevention.ForbiddenTagError;
import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.WebStorage;
import com.agnitas.util.ImageUtils;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpSession;
import com.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.util.ZipUtilities;
import com.agnitas.web.forms.BulkActionForm;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.forms.SimpleActionForm;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public class MailingImagesController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(MailingImagesController.class);

    protected static final String REDIRECT_TO_LIST_STR = "redirect:/mailing/%d/images/list.action?restoreSort=true";

    protected final MailingComponentsService mailingComponentsService;
    protected final PreviewImageService previewImageService;
    protected final ConfigService configService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final UserActivityLogService userActivityLogService;
    private final MailingBaseService mailingBaseService;
    private final MaildropService maildropService;
    private final WebStorage webStorage;

    public MailingImagesController(MailingBaseService mailingBaseService, PreviewImageService previewImageService,
                                   MaildropService maildropService, UserActivityLogService userActivityLogService,
                                   MailingComponentsService mailingComponentsService, ConfigService configService,
                                   MailinglistApprovalService mailinglistApprovalService, WebStorage webStorage) {
        this.configService = configService;
        this.maildropService = maildropService;
        this.mailingBaseService = mailingBaseService;
        this.previewImageService = previewImageService;
        this.userActivityLogService = userActivityLogService;
        this.mailingComponentsService = mailingComponentsService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.webStorage = webStorage;
    }

    @ExceptionHandler(XSSHtmlException.class)
    public String onXSSHtmlException(XSSHtmlException e, Popups popups) {
        for (HtmlCheckError error : e.getErrors()) {
            if (error instanceof ForbiddenTagError) {
                popups.alert("error.html.forbiddenTag.extended");
            } else {
                popups.alert(error.toMessage());
            }
        }
        return MESSAGES_VIEW;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder, Admin admin) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
    }

    @ModelAttribute
    public MailingImagesFormSearchParams getSearchParams() {
        return new MailingImagesFormSearchParams();
    }

    @GetMapping("/list.action")
    public String list(@PathVariable int mailingId, @RequestParam(required = false) Boolean restoreSort, @ModelAttribute("filter") MailingImagesOverviewFilter filter,
                       @ModelAttribute MailingImagesFormSearchParams searchParams, Admin admin, Model model) {
        if (admin.isRedesignedUiUsed()) {
            FormUtils.syncSearchParams(searchParams, filter, true);
        }
        FormUtils.syncPaginationData(webStorage, WebStorage.MAILING_IMAGES_OVERVIEW, filter, restoreSort);
        addModelAttrs(mailingId, filter, admin, model);

        writeUserActivityLog(admin, "images list", "active tab - images");
        return "mailing_images_list";
    }

    protected void addModelAttrs(int mailingId, MailingImagesOverviewFilter filter, Admin admin, Model model) {
        setMailingModelAttrs(mailingId, admin, model);
        model.addAttribute("images", mailingComponentsService.getMailingImagesOverview(admin.getCompanyID(), mailingId, filter));
        model.addAttribute("imagesForMobileAlternative", mailingComponentsService.getMailingImagesNamesForMobileAlternative(mailingId, admin.getCompanyID()));
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("adminDateTimeFormat", admin.getDateTimeFormat());
            model.addAttribute("imagesMimetypes", ImageUtils.ALLOWED_MIMETYPES);
        } else {
            AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        }
    }

    @GetMapping("/search.action")
    public String search(@PathVariable int mailingId, @ModelAttribute MailingImagesOverviewFilter filter, @ModelAttribute MailingImagesFormSearchParams searchParams) {
        FormUtils.syncSearchParams(searchParams, filter, false);
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    private void setMailingModelAttrs(int mailingId, Admin admin, Model model) {
        int companyId = admin.getCompanyID();
        Mailing mailing = mailingBaseService.getMailing(companyId, mailingId);
        model.addAttribute("mailingShortname", mailing.getShortname());
        model.addAttribute("isTemplate", mailing.isIsTemplate());
        model.addAttribute("isMailingUndoAvailable", mailingId > 0 && mailingBaseService.checkUndoAvailable(mailingId));
        model.addAttribute("workflowId", mailingId > 0 ? mailingBaseService.getWorkflowId(mailingId, companyId) : 0);
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("isActiveMailing", maildropService.isActiveMailing(mailingId, companyId));
            model.addAttribute("mailinglistDisabled", !mailinglistApprovalService.isAdminHaveAccess(admin, mailing.getMailinglistID()));
        } else {
            model.addAttribute("limitedRecipientOverview", maildropService.isActiveMailing(mailing.getId(), companyId)
                    && !mailinglistApprovalService.isAdminHaveAccess(admin, mailing.getMailinglistID()));
        }
    }

    @GetMapping("/{imageId:\\d+}/confirmDelete.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    @Deprecated(forRemoval = true)
    public String confirmDelete(Admin admin, @PathVariable int mailingId, @PathVariable int imageId,
                                Model model, @ModelAttribute("simpleActionForm") SimpleActionForm form, Popups popups) {
        MailingComponent image = mailingComponentsService.getComponent(imageId, admin.getCompanyID());
        if (isImageCantBeDeleted(image, popups)) {
            return MESSAGES_VIEW;
        }

        model.addAttribute("mailingId", mailingId);
        form.setId(imageId);
        form.setShortname(image.getComponentName());
        return "mailing_images_delete";
    }

    @RequestMapping(value = "/{imageId:\\d+}/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    // TODO: EMMGUI-714: remove when old design will be removed
    @Deprecated(forRemoval = true)
    public String delete(@PathVariable int mailingId, @PathVariable int imageId, Admin admin, Popups popups, HttpSession session) {
        MailingComponent image = mailingComponentsService.getComponent(imageId, admin.getCompanyID());
        if (isImageCantBeDeleted(image, popups)) {
            logger.warn("Could not find image (ID: {}) to delete for mailing (ID: {})", imageId, mailingId);
            return MESSAGES_VIEW;
        }
        mailingComponentsService.deleteComponent(image);
        previewImageService.generateMailingPreview(admin, session.getId(), mailingId, true);
        logDeletion(image, admin);
        popups.success("default.selection.deleted");
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    @GetMapping(value = "/deleteRedesigned.action")
    @PermissionMapping("confirmDelete")
    public String confirmDeleteRedesigned(@PathVariable int mailingId, @RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Model model, Popups popups) {
        if (CollectionUtils.isEmpty(bulkIds)) {
            popups.alert("bulkAction.nothing.image");
            return MESSAGES_VIEW;
        }
        if (isImagesCantBeDeleted(bulkIds, mailingId, admin, popups)) {
            return MESSAGES_VIEW;
        }

        List<String> items = mailingComponentsService.getImagesNames(mailingId, bulkIds, admin);
        MvcUtils.addDeleteAttrs(model, items,
                "mailing.Graphics_Component.delete", "image.delete.question",
                "bulkAction.delete.image", "bulkAction.delete.image.question");
        return DELETE_VIEW;
    }

    @RequestMapping(value = "/deleteRedesigned.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @PermissionMapping("delete")
    public String deleteRedesigned(@PathVariable int mailingId, @RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups, HttpSession session) {
        if (isImagesCantBeDeleted(bulkIds, mailingId, admin, popups)) {
            return MESSAGES_VIEW;
        }

        if (mailingComponentsService.deleteImages(admin.getCompanyID(), mailingId, bulkIds)) {
            previewImageService.generateMailingPreview(admin, session.getId(), mailingId, true);
        }

        writeUserActivityLog(admin, "delete mailing images", String.format("%s(%d), deleted images (IDS: %s)",
                mailingBaseService.getMailingName(mailingId, admin.getCompanyID()), mailingId, StringUtils.join(bulkIds, ", ")));
        popups.success(SELECTION_DELETED_MSG);
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    private boolean isImageCantBeDeleted(MailingComponent image, Popups popups) {
        if (image != null && (image.getType() == HostedImage || image.getPresent() == 0)) {
            return false;
        }
        popups.alert("error.mailing.image.delete", image == null ? "" : image.getComponentName());
        return true;
    }

    private boolean isImagesCantBeDeleted(Set<Integer> imageIds, int mailingId, Admin admin, Popups popups) {
        if (!isDeleteAllowed(mailingId, admin)) {
            popups.alert(PERMISSION_DENIED_MSG);
        }
        return mailingComponentsService.getComponents(admin.getCompanyID(), mailingId, new HashSet<>(imageIds))
                .stream().anyMatch(image -> isImageCantBeDeleted(image, popups));
    }

    private boolean isDeleteAllowed(int mailingId, Admin admin) {
        return admin.permissionAllowed(
            mailingBaseService.isTemplate(admin.getCompanyID(), mailingId)
                ? Permission.TEMPLATE_CHANGE
                : Permission.MAILING_CHANGE);
    }

    private void logDeletion(MailingComponent image, Admin admin) {
        int mailingId = image.getMailingID();
        writeUserActivityLog(admin, "delete mailing image", String.format("%s(%d), deleted image (%d, %s)",
                mailingBaseService.getMailingName(mailingId, admin.getCompanyID()), mailingId, image.getId(),
                image.getComponentName() != null ? "'" + image.getComponentName() + "'" : "unnamed"));
    }

    @GetMapping("/confirmBulkDelete.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    @Deprecated(forRemoval = true)
    public String confirmBulkDelete(@PathVariable int mailingId, BulkActionForm form, Admin admin, Popups popups) {
        if (CollectionUtils.isEmpty(form.getBulkIds())) {
            popups.alert("bulkAction.nothing.image");
            return MESSAGES_VIEW;
        }
        if (isImagesCantBeDeleted(new HashSet<>(form.getBulkIds()), mailingId, admin, popups)) {
            return MESSAGES_VIEW;
        }
        return "mailing_images_bulk_delete";
    }

    @RequestMapping(value = "/bulkDelete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    // TODO: EMMGUI-714: remove when old design will be removed
    @Deprecated(forRemoval = true)
    public String bulkDelete(@PathVariable int mailingId, BulkActionForm form, Admin admin, Popups popups, HttpSession session) {
        if (isImagesCantBeDeleted(new HashSet<>(form.getBulkIds()), mailingId, admin, popups)) {
            return MESSAGES_VIEW;
        }
        if (mailingComponentsService.deleteImages(admin.getCompanyID(), mailingId, new HashSet<>(form.getBulkIds()))) {
            previewImageService.generateMailingPreview(admin, session.getId(), mailingId, true);
        }

        writeUserActivityLog(admin, "delete mailing images", String.format("%s(%d), deleted images (IDS: %s)",
                mailingBaseService.getMailingName(mailingId, admin.getCompanyID()), mailingId, StringUtils.join(form.getBulkIds(), ", ")));
        popups.success("default.selection.deleted");
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    @GetMapping("/bulkDownload.action")
    public Object bulkDownload(Admin admin, @PathVariable int mailingId, BulkActionForm form, Popups popups) {
        if (CollectionUtils.isEmpty(form.getBulkIds())) {
            popups.alert("bulkAction.nothing.image");
            return String.format(REDIRECT_TO_LIST_STR, mailingId);
        }
        File zip = createImagesZipFile(admin.getCompanyID(), mailingId, new HashSet<>(form.getBulkIds()));
        if (zip == null) {
            popups.alert("mailing.Graphics_Component.NoImage");
            return String.format(REDIRECT_TO_LIST_STR, mailingId);
        }

        writeUserActivityLog(admin, "do bulk download mailing image", "MailingID: " + mailingId);
        return ResponseEntity.ok()
                .contentLength(zip.length())
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment(getZipFilename(admin, mailingId)))
                .body(new DeleteFileAfterSuccessReadResource(zip));
    }

    private File createImagesZipFile(int companyId, int mailingId, Set<Integer> imageIds) {
        List<MailingComponent> images = mailingComponentsService.getComponents(companyId, mailingId, imageIds);
        if (CollectionUtils.isEmpty(images)) {
            return null;
        }
        try {
            File zip = File.createTempFile("GraphicComponents_" + companyId + "_" + mailingId + "_", ".zip", AgnUtils.createDirectory(AgnUtils.getTempDir()));
            try (ZipOutputStream zipOutputStream = ZipUtilities.openNewZipOutputStream(zip)) {
                writeImagesToZip(mailingId, companyId, zipOutputStream, imageIds);
            }
            return zip;
        } catch (Exception e) {
            logger.error("Can't create tempZipFile file", e);
            return null;
        }
    }

    private void writeImagesToZip(int mailingId, int companyId, ZipOutputStream zipOutputStream, Set<Integer> imageIds) throws IOException {
        List<MailingComponent> images = mailingComponentsService.getComponents(companyId, mailingId, imageIds);
        List<String> writtenFilenames = new ArrayList<>();
        for (MailingComponent image : images) {
            writeImageToZip(mailingId, zipOutputStream, writtenFilenames, image);
        }
    }

    private void writeImageToZip(int mailingId, ZipOutputStream zipOutputStream, List<String> writtenFilenames, MailingComponent image) throws IOException {
        String fileExtension = AgnUtils.getFileExtension(image.getComponentName());
        if (ImageUtils.isValidImageFileExtension(fileExtension)) {
            String fileName = FilenameUtils.removeExtension(image.getComponentName());
            byte[] bytes = image.getBinaryBlock();
            if (bytes != null) {
                writtenFilenames.add(addFileDataToZip(zipOutputStream, writtenFilenames, fileName, fileExtension, bytes));
            } else {
                logger.warn("Cannot add mailings (ID: {}) image {} to zip: fileData is missing",
                        mailingId, image.getComponentName());
            }
        }
    }

    private String addFileDataToZip(ZipOutputStream zipOutputStream, List<String> writtenFiles, String name,
                                    String extension, byte[] bytes) throws IOException {
        String filename = name + "." + extension;
        int index = 0;
        while (writtenFiles.contains(filename) && index < 100) {
            filename = name + "_" + (++index) + "." + extension;
        }
        ZipUtilities.addFileDataToOpenZipFileStream(bytes, filename, zipOutputStream);
        return filename;
    }

    private String getZipFilename(Admin admin, int mailingId) {
        SimpleDateFormat dateFormat = DateUtilities.getFormat(DateUtilities.YYYYMD, TimeZone.getTimeZone(admin.getAdminTimezone()));
        return "Uploaded_images_mailingId_" + mailingId + "_" + dateFormat.format(new Date()) + ".zip";
    }

    @GetMapping(value = "/{imageId:\\d+}/reload.action")
    public String reload(@PathVariable int mailingId, @PathVariable int imageId, Admin admin, Popups popups, HttpSession session) {
        ServiceResult<Boolean> result = mailingComponentsService.reloadImage(admin, mailingId, imageId);
        if (result.isSuccess()) {
            if (Boolean.TRUE.equals(result.getResult())) {
                previewImageService.generateMailingPreview(admin, session.getId(), mailingId, true);
            }
            popups.success(CHANGES_SAVED_MSG);
            writeUserActivityLog(admin, "update mailing image", String.format("%s(%d), reloaded image (%d)",
                    mailingBaseService.getMailingName(mailingId, admin.getCompanyID()), mailingId, imageId));
        } else {
            popups.addPopups(result);
        }
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    @PostMapping("/{imageId:\\d+}/edit.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String edit(@PathVariable int mailingId, @PathVariable int imageId, Admin admin, Popups popups,
                       @RequestParam(required = false) String imgBase64, HttpSession session) {
        return editImage(imgBase64, mailingId, imageId, admin, session, popups);
    }

    @PostMapping("/{imageId:\\d+}/editRedesigned.action")
    @PermissionMapping("edit")
    public String editRedesigned(@PathVariable int mailingId, @PathVariable int imageId, Admin admin, Popups popups,
                                 MultipartFile image, HttpSession session) throws IOException {
        if (editImage(mailingId, imageId, admin, image)) {
            previewImageService.generateMailingPreview(admin, session.getId(), mailingId, true);
            writeUserActivityLog(admin, "update mailing image", String.format("%s(%d), edited image (%d)",
                mailingBaseService.getMailingName(mailingId, admin.getCompanyID()), mailingId, imageId));
            popups.success(CHANGES_SAVED_MSG);
        }
        return REDIRECT_TO_LIST_STR.formatted(mailingId);
    }

    private boolean editImage(int mailingId, int imageId, Admin admin, MultipartFile image) throws IOException {
        return image != null
               && !image.isEmpty()
               && mailingComponentsService.updateHostImage(mailingId, admin.getCompanyID(), imageId, image.getBytes());
    }

    private String editImage(String endodedFile, int mailingId, int imageId, Admin admin, HttpSession session, Popups popups) {
        if (StringUtils.isBlank(endodedFile)) {
            return String.format(REDIRECT_TO_LIST_STR, mailingId);
        }
        if (mailingComponentsService.updateHostImage(mailingId, admin.getCompanyID(), imageId, Base64.decodeBase64(endodedFile))) {
            previewImageService.generateMailingPreview(admin, session.getId(), mailingId, true);
            writeUserActivityLog(admin, "update mailing image", String.format("%s(%d), edited image (%d)",
                    mailingBaseService.getMailingName(mailingId, admin.getCompanyID()), mailingId, imageId));
            popups.success(CHANGES_SAVED_MSG);
        }
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    @PostMapping(value = "/upload.action", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@PathVariable int mailingId, UploadMailingImagesForm form, Admin admin, Popups popups, HttpSession session) {
        List<UploadMailingImageDto> images = new ArrayList<>(form.getImages().values());
        validateNewImages(images, admin.getLocale(), popups);
        if (!popups.hasAlertPopups()) {
            uploadImages(images, mailingId, admin, popups, session.getId());
        }
        return String.format(REDIRECT_TO_LIST_STR, mailingId);
    }

    private void uploadImages(List<UploadMailingImageDto> images, int mailingId, Admin admin, Popups popups, String sessionId) {
        List<UserAction> userActions = new ArrayList<>();
        ServiceResult<ImportStatistics> result = mailingComponentsService.uploadImages(admin, mailingId, images, userActions);
        if (result.isSuccess()) {
            previewImageService.generateMailingPreview(admin, sessionId, mailingId, true);
            ImportStatistics statistic = result.getResult();
            if (statistic.getFound() > 1 || images.stream().anyMatch(img ->
                    StringUtils.endsWithIgnoreCase(img.getFile().getOriginalFilename(), ".zip"))) {
                popups.success("items_saved", statistic.getStored(), statistic.getFound());
            } else {
                popups.success(CHANGES_SAVED_MSG);
            }
            userActions.forEach(userAction -> writeUserActivityLog(admin, userAction));
        } else {
            popups.addPopups(result);
        }
    }

    private void validateNewImages(List<UploadMailingImageDto> images, Locale locale, Popups popups) {
        if (images.isEmpty()) {
            popups.alert("mailing.errors.no_component_file");
        }
        validateFileExtensions(images, locale, popups);
        validateFileNamesLength(images, popups);
        validateFileLinks(images, popups);
    }

    private void validateFileNamesLength(List<UploadMailingImageDto> images, Popups popups) {
        for (UploadMailingImageDto image : images) {
            String fileName = image.getFile().getOriginalFilename();
            if (StringUtils.length(fileName) > COMPONENT_NAME_MAX_LENGTH) {
                popups.alert("error.compname.too.long", fileName);
            }
        }
    }

    private void validateFileLinks(List<UploadMailingImageDto> images, Popups popups) {
        for (UploadMailingImageDto image : images) {
            String filename = image.getFile().getOriginalFilename();
            String extension = AgnUtils.getFileExtension(filename);

            if (!extension.equalsIgnoreCase("zip")) {
                String link = image.getLink();
                if (StringUtils.isNotEmpty(link) && (link.contains(" ") || link.contains("\"") || link.contains("'"))) {
                    popups.alert("mailing.error.invalidLinkTarget", link);
                }
            }
        }
    }

    private void validateFileExtensions(List<UploadMailingImageDto> images, Locale locale, Popups popups) {
        Set<String> invalidFiles = getFileNamesWithInvalidExtension(images);
        if (!invalidFiles.isEmpty()) {
            popups.alert(Message.exact(I18nString.getLocaleString("grid.divchild.format.error", locale) + fileNamesToHtml(invalidFiles)));
        }
    }

    private String fileNamesToHtml(Collection<String> filenames) {
        StringBuilder sb = new StringBuilder();
        filenames.forEach(name -> sb.append("<br><b>").append(StringEscapeUtils.escapeHtml4(name)).append("</b>"));
        return sb.toString();
    }

    private Set<String> getFileNamesWithInvalidExtension(List<UploadMailingImageDto> images) {
        Set<String> invalidFiles = new LinkedHashSet<>();
        for (UploadMailingImageDto image : images) {
            String name = image.getFile().getOriginalFilename();
            if (StringUtils.isEmpty(name)) {
                invalidFiles.add("<blank>");
            } else {
                String extension = AgnUtils.getFileExtension(name);
                if (!ImageUtils.isValidImageFileExtension(extension) && !"zip".equalsIgnoreCase(extension)) {
                    invalidFiles.add(name);
                }
            }
        }
        return invalidFiles;
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        writeUserActivityLog(admin, new UserAction(action, description));
    }

    protected void writeUserActivityLog(Admin admin, UserAction userAction) {
        if (Objects.nonNull(userActivityLogService)) {
            userActivityLogService.writeUserActivityLog(admin, userAction, logger);
        } else {
            logger.error("Missing userActivityLogService in {}", this.getClass().getSimpleName());
            logger.info("Userlog: {} {} {}", admin.getUsername(), userAction.getAction(), userAction.getDescription());
        }
    }
}
