/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.components.service.impl;

import static com.agnitas.beans.MailingComponentType.HostedImage;
import static com.agnitas.beans.MailingComponentType.Image;
import static com.agnitas.beans.MailingComponentType.ThumbnailImage;
import static com.agnitas.beans.impl.MailingComponentImpl.COMPONENT_NAME_MAX_LENGTH;
import static com.agnitas.emm.core.Permission.MAILING_CONTENT_CHANGE_ALWAYS;
import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.PERMISSION_DENIED_MSG;
import static com.agnitas.util.ImageUtils.makeMobileDescriptionIfNecessary;
import static com.agnitas.util.ImageUtils.makeMobileFilenameIfNecessary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.factory.MailingComponentFactory;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.common.exceptions.ZipDownloadException;
import com.agnitas.emm.common.service.BulkFilesDownloadService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.commons.StreamHelper;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.components.dto.MailingAttachmentDto;
import com.agnitas.emm.core.components.dto.MailingImageDto;
import com.agnitas.emm.core.components.dto.UpdateMailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingImageDto;
import com.agnitas.emm.core.components.exception.AttachmentDownloadException;
import com.agnitas.emm.core.components.exception.AttachmentNotFoundException;
import com.agnitas.emm.core.components.exception.ComponentNotExistException;
import com.agnitas.emm.core.components.exception.MailingImageNotFoundException;
import com.agnitas.emm.core.components.form.AttachmentType;
import com.agnitas.emm.core.components.form.MailingImagesOverviewFilter;
import com.agnitas.emm.core.components.service.ComponentValidationService;
import com.agnitas.emm.core.components.service.MailingComponentsService;
import com.agnitas.emm.core.components.util.ComponentsUtils;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.validator.AttachmentValidationField;
import com.agnitas.emm.core.validator.MailingImageValidationField;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.core.validator.ValidationField;
import com.agnitas.exception.ForbiddenException;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.preview.Page;
import com.agnitas.preview.Preview;
import com.agnitas.preview.PreviewFactory;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.MimeTypeService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.Const;
import com.agnitas.util.ImageUtils;
import com.agnitas.util.Tuple;
import com.agnitas.web.forms.PaginationForm;
import com.agnitas.web.perm.NotAllowedActionException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public class MailingComponentsServiceImpl implements MailingComponentsService {

	private static final Logger logger = LogManager.getLogger(MailingComponentsServiceImpl.class);
    private static final String NOT_SUPPORTED_LOG_MSG = "Not supported. See extended scope";

	private MailingDao mailingDao;
	private MailingService mailingService;
	private MailingComponentDao mailingComponentDao;
	private MimeTypeService mimeTypeService;
	private ComponentValidationService componentValidationService;
	private MailingComponentFactory mailingComponentFactory;
	protected ConfigService configService;
    private ExtendedConversionService conversionService;
	private BulkFilesDownloadService bulkFilesDownloadService;
	private PreviewFactory previewFactory;
	private RecipientService recipientService;
	private TargetService targetService;

    @Override
    public MailingComponent getMailingTextTemplate(int mailingId, int companyID) {
        return mailingComponentDao.getMailingComponentByName(mailingId, companyID, Const.Component.NAME_TEXT);
    }

	@Override
	public Map<String, Integer> getImageSizeMap(int companyId, int mailingId, boolean includeExternalImages) {
    	// hosted on external and emm server
		Map<Integer, Integer> sizeMap = mailingComponentDao.getImageSizes(companyId, mailingId);
		Map<String, Integer> map = new HashMap<>();

		mailingComponentDao.getImageNames(companyId, mailingId, includeExternalImages)
			.forEach((componentId, name) -> map.put(name, sizeMap.getOrDefault(componentId, 0)));

		return map;
	}

	@Override
    public List<MailingComponent> getComponents(int companyID, int mailingId, Set<Integer> componentIds) {
        if (companyID <= 0 || mailingId <= 0 || CollectionUtils.isEmpty(componentIds)) {
        	return Collections.emptyList();
		}

		return mailingComponentDao.getMailingComponents(companyID, mailingId, componentIds);
    }

	@Override
	public MailingComponent getComponentById(int componentId, int mailingId, int companyId) {
		return findComponentById(componentId, mailingId, companyId)
				.orElseThrow(() -> new ComponentNotExistException(componentId, mailingId));
	}

	@Override
	public Optional<MailingComponent> findComponentById(int componentId, int mailingId, int companyId) {
		return mailingComponentDao.findComponentById(componentId, mailingId, companyId);
	}

	private MailingComponent getComponent(int companyId, int mailingId, int componentId) {
		return mailingComponentDao.getMailingComponent(mailingId, componentId, companyId);
    }

	@Override
	public SimpleServiceResult uploadAttachment(int mailingId, Admin admin, UploadMailingAttachmentDto attachment) {
		if (mailingService.isSettingsReadonly(admin, mailingId)) {
			throw new NotAllowedActionException();
		}
		if (!mailingEditable(mailingId, admin)) {
			throw new ValidationException("status_changed");
		}
		if (attachment.getTargetId() > 0 && !targetService.exist(attachment.getTargetId(), admin.getCompanyID())) {
			throw ValidationException.notFound(AttachmentValidationField.TARGET, attachment.getTargetId());
		}
		SimpleServiceResult result = uploadMailingAttachment(admin, mailingId, attachment);
		if (!result.isSuccess()) {
			throw ValidationException.of(result);
		}
		return result;
	}

	private boolean mailingEditable(int mailingId, Admin admin) {
        return admin.permissionAllowed(MAILING_CONTENT_CHANGE_ALWAYS)
				|| !mailingService.isMailingWorldSent(mailingId, admin.getCompanyID());
    }

	@Override
	public SimpleServiceResult uploadMailingAttachment(Admin admin, int mailingId, UploadMailingAttachmentDto attachment) {
		List<Message> errors = new ArrayList<>();
		List<Message> warnings = new ArrayList<>();

		if (StringUtils.isEmpty(attachment.getName())) {
			// get file's name in case if name field is not specified by user
			if (attachment.isUsePdfUpload() && attachment.getUploadId() > 0) {
                attachment.setName(getUploadedFilename(attachment.getUploadId()));
			} else if (attachment.getAttachmentFile() != null) {
				attachment.setName(attachment.getAttachmentFile().getName());
			}
		}

		boolean valid = componentValidationService.validateAttachment(admin.getCompanyID(), mailingId, attachment, errors, warnings);

		if (!valid) {
			return new SimpleServiceResult(false, Collections.emptyList(), warnings, errors);
		}

		if (mailingComponentDao.attachmentExists(admin.getCompanyID(), mailingId, attachment.getName(), attachment.getTargetId())) {
			return SimpleServiceResult.simpleSuccess();
		}

		try {
			MailingComponent component = mailingComponentFactory.newMailingComponent();
			component.setCompanyID(admin.getCompanyID());
			component.setMailingID(mailingId);
			component.setTargetID(attachment.getTargetId());

			String fileName = attachment.getName();
			byte[] content;
			String mimeType;

			if (attachment.isUsePdfUpload()) {
				int uploadId = attachment.getUploadId();
				mimeType = "application/pdf";
				content = getUploadData(uploadId);
			} else {
				content = attachment.getAttachmentFile().getBytes();
				mimeType = attachment.getAttachmentFile().getContentType();
			}

			component.setComponentName(fileName);

			if (AttachmentType.PERSONALIZED == attachment.getType()) {
				component.setType(MailingComponentType.PersonalizedAttachment);
				mimeType = "application/pdf";

				component.setBinaryBlock(attachment.getBackgroundFile().getBytes(), mimeType);
				component.setEmmBlock(new String(attachment.getAttachmentFile().getBytes(), StandardCharsets.UTF_8), mimeType);
			} else {
				component.setType(MailingComponentType.Attachment);
				component.setBinaryBlock(content, mimeType);
			}

			mailingComponentDao.saveMailingComponent(component);

			return SimpleServiceResult.simpleSuccess(Message.of("default.changes_saved"));

		} catch (Exception e) {
			logger.error("Uploading attachment failed for mailing ID: {}", mailingId, e);

			return SimpleServiceResult.simpleError(Message.of("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
		}
	}

	protected byte[] getUploadData(int uploadId) {
		throw new UnsupportedOperationException(NOT_SUPPORTED_LOG_MSG);
	}

    protected String getUploadedFilename(int uploadId) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_LOG_MSG);
    }

    @Override
	@Transactional
    public SimpleServiceResult updateMailingAttachments(Admin admin, int mailingId, Map<Integer, UpdateMailingAttachmentDto> attachments) {
		if (MapUtils.isNotEmpty(attachments)) {
			List<MailingComponent> existingComponents = mailingComponentDao.getPreviewHeaderComponents(mailingId, admin.getCompanyID());

			for (MailingComponent origin : existingComponents) {
				UpdateMailingAttachmentDto attachment = attachments.get(origin.getId());
				if (attachment != null) {
					origin.setTargetID(attachment.getTargetId());
				}
				mailingComponentDao.saveMailingComponent(origin);
			}
		}

		return SimpleServiceResult.simpleSuccess(Message.of(CHANGES_SAVED_MSG));
    }

    @Override
    public PaginatedList<MailingImageDto> getMailingImagesOverview(int companyId, int mailingId, MailingImagesOverviewFilter filter) {
		PaginatedList<MailingComponent> images = mailingComponentDao.getImagesOverview(companyId, mailingId, filter);
		return conversionService.convertPaginatedList(images, MailingComponent.class, MailingImageDto.class);
    }

	@Override
	public List<String> getMailingImagesNamesForMobileAlternative(int mailingId, int companyId) {
		return mailingComponentDao.getMailingImagesNamesForMobileAlternative(mailingId, companyId);
	}

	@Override
	public List<MailingComponent> getPreviewHeaderComponents(int companyId, int mailingId) {
		return mailingComponentDao.getPreviewHeaderComponents(mailingId, companyId);
	}

	@Override
	public PaginatedList<MailingAttachmentDto> getAttachmentsOverview(PaginationForm form, int mailingId, int companyId) {
		List<MailingAttachmentDto> attachments = conversionService.convert(
				getPreviewHeaderComponents(companyId, mailingId),
				MailingComponent.class,
				MailingAttachmentDto.class
		);

		int page = AgnUtils.getValidPageNumber(attachments.size(), form.getPage(), form.getNumberOfRows());

		List<MailingAttachmentDto> sortedAttachments = attachments.stream()
				.sorted(getAttachmentsComparator(form))
				.skip((long) (page - 1) * form.getNumberOfRows())
				.limit(form.getNumberOfRows())
				.collect(Collectors.toList());

		return new PaginatedList<>(sortedAttachments, attachments.size(), form.getNumberOfRows(), page, form.getSort(), form.getOrder());
	}

	private Comparator<MailingAttachmentDto> getAttachmentsComparator(PaginationForm form) {
		Comparator<MailingAttachmentDto> comparator = Comparator
				.comparing(MailingAttachmentDto::getId);

		if (Strings.CI.equals("name", form.getSort())) {
			comparator = Comparator.comparing(a -> StringUtils.lowerCase(a.getName()));
		} else if (Strings.CI.equals("mimeType", form.getSort())) {
			comparator = Comparator.comparing(a -> StringUtils.lowerCase(a.getMimeType()));
		} else if (Strings.CI.equals("targetId", form.getSort())) {
			comparator = Comparator.comparing(MailingAttachmentDto::getTargetId);
		} else if (Strings.CI.equals("originalSize", form.getSort())) {
			comparator = Comparator.comparing(MailingAttachmentDto::getOriginalSize);
		} else if (Strings.CI.equals("emailSize", form.getSort())) {
			comparator = Comparator.comparing(MailingAttachmentDto::getEmailSize);
		}

		if (!form.ascending()) {
			comparator = comparator.reversed();
		}
		return comparator;
	}

	@Override
	public Map<String, String> getUrlsByNamesForEmmImages(Admin admin, int mailingId) {
		String rdirDomain = admin.getCompany().getRdirDomain();
		return mailingComponentDao.getMailingComponents(mailingId, admin.getCompanyID(), MailingComponentType.HostedImage, false)
				.stream()
				.collect(Collectors.toMap(
						MailingComponent::getComponentName,
						e -> ComponentsUtils.getImageUrl(rdirDomain, admin.getCompanyID(), mailingId, e.getComponentName()),
						(x, y) -> x,
						LinkedHashMap::new
				));
	}

	@Override
	public List<MailingComponent> getMailingComponents(int mailingId, int companyId, MailingComponentType componentType, boolean includeContent) {
		return mailingComponentDao.getMailingComponents(mailingId, companyId, componentType, includeContent);
	}

	private void deleteComponent(MailingComponent component) {
		if (component != null) {
			mailingComponentDao.deleteMailingComponent(component);
		}
	}

	@Override
	public void deleteImage(int imageId, int mailingId, Admin admin) {
		MailingImageDto image = getImage(imageId, mailingId, admin.getCompanyID());
		deleteImage(image, mailingId, admin);
	}

	@Override
	public void deleteImage(String filename, int mailingId, Admin admin) {
		MailingImageDto image = getImage(filename, mailingId, admin.getCompanyID());
		deleteImage(image, mailingId, admin);
	}

	private void deleteImage(MailingImageDto image, int mailingId, Admin admin) {
		deleteImages(Set.of(image.getId()), mailingId, admin);
	}

	@Override
	public boolean deleteImages(Set<Integer> bulkIds, int mailingId, Admin admin) {
		validateImagesCanBeDeleted(bulkIds, mailingId, admin);
    	return mailingComponentDao.deleteImages(admin.getCompanyID(), mailingId, bulkIds);
	}

	@Override
	public ServiceResult<Boolean> reloadImage(Admin admin, int mailingId, int componentId) {
    	MailingComponent component = mailingComponentDao.getMailingComponent(mailingId, componentId, admin.getCompanyID());

    	if (component == null || component.getType() != MailingComponentType.Image) {
    		return new ServiceResult<>(false, false, Message.of("Error"));
		}

    	byte[] existingImage = component.getBinaryBlock();

    	if (component.loadContentFromURL()) {
    		// If image is the same then there's no point in saving.
			if (Objects.deepEquals(existingImage, component.getBinaryBlock())) {
				return new ServiceResult<>(false, true);
			} else {
				try {
					mailingComponentDao.saveMailingComponent(component);
					return new ServiceResult<>(true, true);
				} catch (Exception e) {
					logger.error("Failed to save reloaded image");
					return new ServiceResult<>(false, false, Message.of("Error"));
				}
			}
		} else {
			return new ServiceResult<>(false, false, Message.of("Error"));
		}
	}

	@Override
	public boolean updateHostImage(int mailingId, int companyId, int componentId, byte[] imageBytes) {
		MailingComponent component = mailingComponentDao.getMailingComponent(mailingId, componentId, companyId);

		if (component == null || component.getType() != MailingComponentType.HostedImage) {
			return false;
		}

		// If image is the same then there's no point in saving.
		if (Objects.deepEquals(component.getBinaryBlock(), imageBytes)) {
			return false;
		} else {
			mailingComponentDao.updateHostImage(mailingId, companyId, componentId, imageBytes);
			return true;
		}
	}

    @Override
    public MailingImageDto uploadImage(UploadMailingImageDto image, int mailingId, Admin admin) {
		if (Strings.CI.endsWith(image.getFile().getOriginalFilename(), ".zip")) {
			throw ValidationException.exact("zip not supported");
		}
		validateNewImages(List.of(image), mailingId, admin);
		ServiceResult<ImportStatistics> result = uploadImages(admin, mailingId, List.of(image), new ArrayList<>());
		if (!result.isSuccess()) {
			throw new ValidationException(result.getErrorMessages());
		}
		String savedName = makeMobileFilenameIfNecessary(image.getFile().getOriginalFilename(), image.getMobileBase());
		return getImage(savedName, mailingId, admin.getCompanyID());
	}

  	public ServiceResult<ImportStatistics> uploadImages(Admin admin, int mailingId, List<UploadMailingImageDto> images, List<UserAction> userActions) {
      	if (mailingDao.exist(mailingId, admin.getCompanyID())) {
      		if (images.isEmpty()) {
      			return ServiceResult.success(new ImportStats(0, 0));
  			}

            return doImport(admin, mailingId, importer -> {
                for (UploadMailingImageDto image : images) {
                    importer.importFile(image, userActions);
                }
            });
  		} else {
      		return ServiceResult.error(Message.of("Error"));
  		}
  	}

	@Override
	public ServiceResult<ImportStatistics> importImagesFromSftp(Admin admin, int mailingId, String sftpServerAndAuthConfigString, String sftpPrivateKeyString, String sftpFilePath, List<UserAction> userActions) {
		throw new UnsupportedOperationException();
	}

	protected ServiceResult<ImportStatistics> doImport(Admin admin, int mailingId, Consumer<ImageImporter> importerCalls) {
		final int maximumUploadImageSize = configService.getIntegerValue(ConfigValue.MaximumUploadImageSize);
		final int maximumWarningImageSize = configService.getIntegerValue(ConfigValue.MaximumWarningImageSize);
		final String mailingName = mailingDao.getMailingName(mailingId, admin.getCompanyID());

		ImageImporter importer = createImageImporter(mailingId, mailingName, maximumUploadImageSize, maximumWarningImageSize);
		importerCalls.accept(importer);

		if (importer.getCountValid() > 0) {
			saveNewComponents(admin, mailingId, importer.getComponentsMap());
		}

		List<Message> warnings = new ArrayList<>();
		List<Message> errors = new ArrayList<>();

		checkImportResultErrors(importer, admin, warnings, errors);

		ImportStatistics statistics = new ImportStats(importer.getCountOverall(), importer.getCountValid());
		return new ServiceResult<>(statistics, importer.getCountValid() > 0, Collections.emptyList(), warnings, errors);
	}

	protected void checkImportResultErrors(ImageImporter importer, Admin admin, List<Message> warnings, List<Message> errors) {
		List<String> sizeWarningFiles = importer.getSizeWarningFiles();
		if (!sizeWarningFiles.isEmpty()) {
			String message = I18nString.getLocaleString("warning.component.size", admin.getLocale(), FileUtils.byteCountToDisplaySize(importer.getMaximumWarningImageSize())) +
					filenamesToHtml(sizeWarningFiles);

			warnings.add(Message.exact(message));
		}

		List<String> sizeErrorFiles = importer.getSizeErrorFiles();
		if (!sizeErrorFiles.isEmpty()) {
			String message = I18nString.getLocaleString("error.component.size", admin.getLocale(), FileUtils.byteCountToDisplaySize(importer.getMaximumUploadImageSize())) +
					filenamesToHtml(sizeErrorFiles);

			errors.add(Message.exact(message));
		}

		List<String> invalidFiles = importer.getInvalidFiles();
		if (!invalidFiles.isEmpty()) {
			String message = I18nString.getLocaleString("grid.divchild.format.error", admin.getLocale()) + filenamesToHtml(invalidFiles);
			errors.add(Message.exact(message));
		}

		List<String> invalidArchives = importer.getInvalidArchives();
		if (!invalidArchives.isEmpty()) {
			String message = I18nString.getLocaleString("mailing.Graphics_Component.zipUploadFailed", admin.getLocale()) + filenamesToHtml(invalidArchives);
			errors.add(Message.exact(message));
		}
	}

	protected ImageImporter createImageImporter(int mailingId, String mailingName, int maximumUploadImageSize, int maximumWarningImageSize) {
		return new ImageImporter(mailingId, mailingName, maximumUploadImageSize, maximumWarningImageSize);
	}

	private void saveNewComponents(Admin admin, int mailingId, Map<String, MailingComponent> newComponentsMap) {
		Map<String, MailingComponent> existingComponentsMap = getComponentsMap(mailingId, admin.getCompanyID());

		newComponentsMap.forEach((name, newComponent) -> {
			MailingComponent existingComponent = existingComponentsMap.get(name);

			// Prevent overwriting of existing components of other types (other than hosted image).
			if (existingComponent == null || existingComponent.getType() == MailingComponentType.HostedImage) {
				if (existingComponent != null) {
					mailingComponentDao.deleteMailingComponent(existingComponent);
				}

				newComponent.setMailingID(mailingId);
				newComponent.setCompanyID(admin.getCompanyID());

				mailingComponentDao.saveMailingComponent(newComponent);
			}
		});
	}

	private Map<String, MailingComponent> getComponentsMap(int mailingId, int companyId) {
		List<MailingComponent> components = mailingComponentDao.getMailingComponents(mailingId, companyId, false);
		Map<String, MailingComponent> componentMap = new HashMap<>(components.size());

		for (MailingComponent component : components) {
			componentMap.put(component.getComponentName(), component);
		}

		return componentMap;
	}

	private String filenamesToHtml(Collection<String> filenames) {
		StringBuilder sb = new StringBuilder();

		for (String filename : filenames) {
			sb.append("<br><b>").append(StringEscapeUtils.escapeHtml4(filename)).append("</b>");
		}

		return sb.toString();
	}

	@Override
    public Map<Integer, String> getUploadsByExtension(Admin admin) {
        return Collections.emptyMap();
    }

    @Override
    public boolean validatePdfUploadFields(UploadMailingAttachmentDto attachment, List<Message> errors) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_LOG_MSG);
    }

	private static class ImportStats implements ImportStatistics {
		private final int found;
		private final int stored;

		public ImportStats(int found, int stored) {
			this.found = found;
			this.stored = stored;
		}

		@Override
		public int getFound() {
			return found;
		}

		@Override
		public int getStored() {
			return stored;
		}
	}

	protected class ImageImporter {
		protected final int mailingId;
		protected final String mailingName;
		private final int maximumWarningImageSize;
		private final int maximumUploadImageSize;

		private final Map<String, MailingComponent> componentsMap = new HashMap<>();
		private final List<String> invalidFiles = new ArrayList<>();
		private final List<String> sizeErrorFiles = new ArrayList<>();
		private final List<String> sizeWarningFiles = new ArrayList<>();
		private final List<String> invalidArchives = new ArrayList<>();
    	protected int countOverall;
    	protected int countValid;

    	public ImageImporter(int mailingId, String mailingName, int maximumUploadImageSize, int maximumWarningImageSize) {
    		this.mailingId = mailingId;
    		this.mailingName = mailingName;
    		this.maximumUploadImageSize = maximumUploadImageSize;
    		this.maximumWarningImageSize = maximumWarningImageSize;
		}

        private void importFile(UploadMailingImageDto imageFile, List<UserAction> userActions) {
            MultipartFile file = imageFile.getFile();
            String filename = file.getOriginalFilename();

            if (Strings.CI.endsWith(filename, ".zip")) {
                importArchive(file, userActions);
            } else {
                importFile(file, imageFile.getLink(), imageFile.getDescription(), imageFile.getMobileBase(), userActions);
            }
        }

        private void importArchive(MultipartFile file, List<UserAction> userActions) {
        		String archiveFilename = file.getOriginalFilename();

    			try (ZipInputStream zipStream = new ZipInputStream(file.getInputStream())) {
    				int countBefore = countValid;

    				for (ZipEntry entry = zipStream.getNextEntry(); entry != null; entry = zipStream.getNextEntry()) {
    					final String path = entry.getName();
    					final String name = FilenameUtils.getName(path);

    					if (!entry.isDirectory() && ImageUtils.isValidImageFileExtension(FilenameUtils.getExtension(name))) {
    						logger.info("uploadImagesBulk(): found image file '{}' in ZIP stream", path);
    						countOverall++;

    						if (validateFileSize(archiveFilename + "/" + path, entry.getSize())) {
    							byte[] content = StreamHelper.streamToByteArray(zipStream);
    							if (ImageUtils.isValidImage(content)) {
    								importImage(name, content);
    							} else {
    								invalidFiles.add(archiveFilename + "/" + path);
    							}
    						}
    					}

    					zipStream.closeEntry();
    				}

    				// If at least one image imported from archive.
    				if (countValid > countBefore) {
    					userActions.add(new UserAction("upload archive", String.format("%s(%d), uploaded images from archive", mailingName, mailingId)));
    				}
    			} catch (IOException e) {
    				logger.error("uploadImagesBulk(): IO-error on ZIP-file processing", e);
    				invalidArchives.add(archiveFilename);
    			}
    		}

        private void importFile(MultipartFile file, String link, String description, String mobileBase, List<UserAction> userActions) {
            countOverall++;

            try {
                String filename = file.getOriginalFilename();

                if (validateFileSize(filename, file.getSize())) {
                    byte[] content = file.getBytes();

                    if (ImageUtils.isValidImage(content)) {
                        importImage(filename, file.getContentType(), content, link, description, mobileBase);
                        userActions.add(new UserAction("upload mailing component file", String.format("%s(%d), uploaded image `%s`", mailingName, mailingId, filename)));
                    } else {
                        invalidFiles.add(filename);
                    }
                }
            } catch (IOException e) {
                logger.error("uploadImagesBulk(): IO-error on image file processing", e);
            }
        }

		protected void importImage(String filename, byte[] content) {
    		importImage(filename, mimeTypeService.getMimetypeForFile(filename), content, "", "", "");
		}

		private void importImage(String filename, String mimeType, byte[] content, String link, String description, String mobileComponentBase) {
			countValid++;

			MailingComponent component = mailingComponentFactory.newMailingComponent();

			component.setComponentName(makeMobileFilenameIfNecessary(filename, mobileComponentBase));
			component.setType(MailingComponentType.HostedImage);
			component.setDescription(makeMobileDescriptionIfNecessary(description, mobileComponentBase));
			component.setBinaryBlock(content, mimeType);
			component.setLink(link);

			componentsMap.put(component.getComponentName(), component);
		}

		protected boolean validateFileSize(String filename, long size) {
			if (size > maximumUploadImageSize) {
				sizeErrorFiles.add(filename);
				return false;
			} else if (size > maximumWarningImageSize) {
				sizeWarningFiles.add(filename);
			}

			return true;
		}

		public Map<String, MailingComponent> getComponentsMap() {
			return componentsMap;
		}

		public List<String> getInvalidFiles() {
			return invalidFiles;
		}

		public List<String> getSizeErrorFiles() {
			return sizeErrorFiles;
		}

		public List<String> getSizeWarningFiles() {
			return sizeWarningFiles;
		}

		public List<String> getInvalidArchives() {
			return invalidArchives;
		}

		public int getCountOverall() {
			return countOverall;
		}

		public int getCountValid() {
			return countValid;
		}

		public int getMaximumWarningImageSize() {
			return maximumWarningImageSize;
		}

		public int getMaximumUploadImageSize() {
			return maximumUploadImageSize;
		}
	}

    @Override
    public void updateMailingMediapoolImagesReferences(int mailingId, int companyId, Set<String> mediapoolImages) {
        // overridden in extended class
    }

	@Override
	public List<String> getImagesNames(int mailingId, Set<Integer> ids, Admin admin) {
		return mailingComponentDao.getImagesNames(mailingId, ids, admin.getCompanyID());
	}

	@Override
	public List<String> getImagesNames(int mailingId, int companyId) {
		return mailingComponentDao.getImagesNames(mailingId, Collections.emptySet(), companyId);
	}

	@Override
	public List<String> getNames(Set<Integer> ids, int mailingId, Admin admin) {
		return getAttachments(ids, mailingId, admin)
				.stream()
				.map(MailingComponent::getComponentName)
				.toList();
	}

	@Override
	public ServiceResult<UserAction> deleteAttachment(Set<Integer> ids, int mailingId, Admin admin) {
		List<MailingComponent> attachments = getAttachments(ids, mailingId, admin);
		attachments.forEach(this::deleteComponent);

		List<Integer> removedIds = attachments.stream().map(MailingComponent::getId).toList();

		return ServiceResult.success(
				new UserAction(
						"delete attachments",
						String.format("Attachments IDs (%s) from mailing ID: %d", StringUtils.join(removedIds, ", "), mailingId)
				),
				Message.of(Const.Mvc.SELECTION_DELETED_MSG)
		);
	}

	@Override
	public ServiceResult<UserAction> deleteAttachment(String filename, int mailingId, Admin admin) {
		MailingAttachmentDto attachment = getAttachment(filename, mailingId, admin.getCompanyID());
		return deleteAttachment(attachment, mailingId, admin);
	}

	@Override
	public ServiceResult<UserAction> deleteAttachment(int componentId, int mailingId, Admin admin) {
		MailingAttachmentDto attachment = getAttachment(componentId, mailingId, admin.getCompanyID());
		return deleteAttachment(attachment, mailingId, admin);
	}

	private ServiceResult<UserAction> deleteAttachment(MailingAttachmentDto attachment, int mailingId, Admin admin) {
		if (mailingService.isSettingsReadonly(admin, mailingId)) {
			throw new NotAllowedActionException();
		}
		return deleteAttachment(Set.of(attachment.getId()), mailingId, admin);
	}

	private Optional<MailingComponent> findByFilename(String filename, int mailing, int companyId) {
		return mailingComponentDao.findComponentByName(filename, mailing, companyId);
	}

	private MailingComponent getComponentByFilename(String filename, int mailingId, int companyId) {
		return findByFilename(filename, mailingId, companyId)
				.orElseThrow(() -> new ComponentNotExistException(filename, mailingId));
	}

	@Override
	public MailingAttachmentDto getAttachment(int attachmentId, int mailingId, int companyId) {
		MailingComponent component = getComponentById(attachmentId, mailingId, companyId);
		if (component.getType() != MailingComponentType.Attachment) {
			throw new AttachmentNotFoundException(attachmentId, mailingId);
		}
		return conversionService.convert(component, MailingAttachmentDto.class);
	}

	@Override
	public MailingAttachmentDto getAttachment(String filename, int mailingId, int companyId) {
		MailingComponent component = getComponentByFilename(filename, mailingId, companyId);
		if (component.getType() != MailingComponentType.Attachment) {
			throw new AttachmentNotFoundException(filename, mailingId);
		}
		return conversionService.convert(component, MailingAttachmentDto.class);
	}

	@Override
	public MailingImageDto getImage(int imageId, int mailingId, int companyId) {
		MailingComponent component = getImageCompById(imageId, mailingId, companyId);
		return conversionService.convert(component, MailingImageDto.class);
	}

	@Override
	public MailingImageDto getImage(String filename, int mailingId, int companyId) {
		MailingComponent component = getImageCompByFileName(filename, mailingId, companyId);
		return conversionService.convert(component, MailingImageDto.class);
	}

	@Override
	public MailingComponent getImageCompById(int imageId, int mailingId, int companyId) {
		MailingComponent component = getComponentById(imageId, mailingId, companyId);
		if (!EnumSet.of(Image, HostedImage, ThumbnailImage).contains(component.getType())) {
			throw new MailingImageNotFoundException(imageId, mailingId);
		}
		return component;
	}

	@Override
	public MailingComponent getImageCompByFileName(String filename, int mailingId, int companyId) {
		MailingComponent component = getComponentByFilename(filename, mailingId, companyId);
		if (!EnumSet.of(Image, HostedImage, ThumbnailImage).contains(component.getType())) {
			throw new MailingImageNotFoundException(filename, mailingId);
		}
		return component;
	}

	private List<MailingComponent> getAttachments(Set<Integer> ids, int mailingId, Admin admin) {
		return ids.stream()
				.map(id -> getComponent(admin.getCompanyID(), mailingId, id))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	@Override
	public File getZipToDownload(Set<Integer> ids, int mailingId, Admin admin) {
		try {
			return bulkFilesDownloadService.getZipToDownload(ids, "mailing-attachments", id -> {
				MailingComponent component = getComponent(admin.getCompanyID(), mailingId, id);
				if (component != null) {
					return new Tuple<>(component.getComponentName(), component.getBinaryBlock());
				}

				return null;
			});
		} catch (ZipDownloadException e) {
			throw new AttachmentDownloadException(e.getErrors(), mailingId);
		}
	}

	@Override
	public InputStream attachmentToStream(int componentId, int mailingId, int companyId) {
		return attachmentToStream(componentId, mailingId, companyId, null);
	}

	@Override
	public InputStream attachmentToStream(int componentId, int mailingId, int companyId, Integer customerId) {
		MailingComponent comp = mailingComponentDao.getMailingComponent(componentId, companyId);
		if (comp == null) {
			throw new ComponentNotExistException(componentId, mailingId);
		}
		byte[] attachment = comp.getType() == MailingComponentType.PersonalizedAttachment
				? buildPersonalizedAttachment(comp, mailingId, customerId)
				: comp.getBinaryBlock();

		return new ByteArrayInputStream(ArrayUtils.nullToEmpty(attachment));
	}

	private byte[] buildPersonalizedAttachment(MailingComponent comp, int mailingId, Integer customerId) {
		int resolvedCustomerId = customerId != null ? customerId : 0;
		int targetId = resolvedCustomerId == 0 ? comp.getTargetID() : 0;

		if (resolvedCustomerId == 0 && targetId == 0) {
			resolvedCustomerId = recipientService
					.getAdminAndTestRecipients(comp.getCompanyID(), mailingId)
					.getFirst()
					.getId();
		}
		Preview preview = previewFactory.createPreview();
		Page page = preview.makePreview(mailingId, resolvedCustomerId, targetId);
		return page.getAttachment(comp.getComponentName());
	}

	@Override
	public void validateNewImages(List<UploadMailingImageDto> images, int mailingId, Admin admin) {
		List<Message> errors = new ArrayList<>();
		Map<ValidationField, Message> fieldErrors = new HashMap<>();
		if (images.isEmpty()) {
			throw new ValidationException("mailing.errors.no_component_file");
		}
		validateFileExtensions(images, admin.getLocale(), errors);
		validateFileNamesLength(images, errors);
		validateFileLinks(images, errors);
		validatedMobileBase(images, mailingId, admin, fieldErrors);
		if (!errors.isEmpty() || !fieldErrors.isEmpty()) {
			throw ValidationException.of(SimpleServiceResult.of(List.of(), errors, fieldErrors));
		}
	}

	private void validatedMobileBase(
			List<UploadMailingImageDto> images,
			int mailingId,
			Admin admin,
			Map<ValidationField, Message> fieldErrors
	) {
		List<String> desktopFilenames = getMailingImagesNamesForMobileAlternative(mailingId, admin.getCompanyID());
		images.stream()
				.map(UploadMailingImageDto::getMobileBase)
				.filter(StringUtils::isNotBlank)
				.filter(mobileBase -> !desktopFilenames.contains(mobileBase))
				.forEach(mobileBase -> fieldErrors.put(
						MailingImageValidationField.MOBILE_VERSION_OF,
						Message.exact("Image with filename %s not exists".formatted(mobileBase))
				));
	}

	@Override
	public void validateImagesCanBeDeleted(Set<Integer> imageIds, int mailingId, Admin admin) {
		if (CollectionUtils.isEmpty(imageIds)) {
			throw new ValidationException("bulkAction.nothing.image");
		}
		if (!isDeleteAllowed(mailingId, admin)) {
			throw new ForbiddenException(PERMISSION_DENIED_MSG);
		}
		List<Message> errors = getComponents(admin.getCompanyID(), mailingId, new HashSet<>(imageIds)).stream()
				.filter(Objects::nonNull)
				.map(this::getDeletionError)
				.flatMap(Optional::stream)
				.toList();
		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
	}

	private Optional<Message> getDeletionError(MailingComponent image) {
		if (image.getType() == HostedImage || image.getPresent() == 0) {
			return Optional.empty();
		}
		return Optional.of(Message.of("error.mailing.image.delete", image.getComponentName()));
	}

	private boolean isDeleteAllowed(int mailingId, Admin admin) {
		return admin.permissionAllowed(
				mailingService.isTemplate(mailingId, admin.getCompanyID())
						? Permission.TEMPLATE_CHANGE
						: Permission.MAILING_CHANGE);
	}

	private void validateFileNamesLength(List<UploadMailingImageDto> images, List<Message> errors) {
		for (UploadMailingImageDto image : images) {
			String fileName = image.getFile().getOriginalFilename();
			if (StringUtils.length(fileName) > COMPONENT_NAME_MAX_LENGTH) {
				errors.add(Message.of("error.compname.too.long", fileName));
			}
		}
	}

	private void validateFileLinks(List<UploadMailingImageDto> images, List<Message> errors) {
		for (UploadMailingImageDto image : images) {
			String filename = image.getFile().getOriginalFilename();
			String extension = AgnUtils.getFileExtension(filename);

			if (!extension.equalsIgnoreCase("zip")) {
				String link = image.getLink();
				if (StringUtils.isNotEmpty(link) && (link.contains(" ") || link.contains("\"") || link.contains("'"))) {
					errors.add(Message.of("mailing.error.invalidLinkTarget", link));
				}
			}
		}
	}

	private void validateFileExtensions(List<UploadMailingImageDto> images, Locale locale, List<Message> errors) {
		Set<String> invalidFiles = getFileNamesWithInvalidExtension(images);
		if (!invalidFiles.isEmpty()) {
			errors.add(Message.exact(
					I18nString.t("grid.divchild.format.error", locale) + filenamesToHtml(invalidFiles)
			));
		}
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

	public void setConversionService(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public void setMimeTypeService(MimeTypeService mimeTypeService) {
		this.mimeTypeService = mimeTypeService;
	}

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void setMailingComponentDao(MailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}

	public void setMailingComponentFactory(MailingComponentFactory mailingComponentFactory) {
		this.mailingComponentFactory = mailingComponentFactory;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setComponentValidationService(ComponentValidationService componentValidationService) {
		this.componentValidationService = componentValidationService;
	}

	public void setBulkFilesDownloadService(BulkFilesDownloadService bulkFilesDownloadService) {
		this.bulkFilesDownloadService = bulkFilesDownloadService;
	}

	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}

	public void setPreviewFactory(PreviewFactory previewFactory) {
		this.previewFactory = previewFactory;
	}

	public void setRecipientService(RecipientService recipientService) {
		this.recipientService = recipientService;
	}

	public void setTargetService(TargetService targetService) {
		this.targetService = targetService;
	}

	@Override
	public String buildDownloadUrl(int componentId, Admin admin, String token) {
		int companyId = admin.getCompanyID();
		return AgnUtils.getNormalizedRdirDomain(admin.getCompany().getRdirDomain())
				+ "sc?compID=" + componentId + "&companyId=" + companyId + "&token=" + token;
	}
}
