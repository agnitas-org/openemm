/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.common.exceptions.ZipDownloadException;
import com.agnitas.emm.common.service.BulkFilesDownloadService;
import com.agnitas.emm.core.commons.StreamHelper;
import com.agnitas.emm.core.components.dto.MailingAttachmentDto;
import com.agnitas.emm.core.components.dto.MailingImageDto;
import com.agnitas.emm.core.components.dto.UpdateMailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingImageDto;
import com.agnitas.emm.core.components.exception.AttachmentDownloadException;
import com.agnitas.emm.core.components.form.AttachmentType;
import com.agnitas.emm.core.components.form.MailingImagesOverviewFilter;
import com.agnitas.emm.core.components.service.MailingComponentsService;
import com.agnitas.emm.core.components.service.ComponentValidationService;
import com.agnitas.emm.core.components.util.ComponentsUtils;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.MimeTypeService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.ImageUtils;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.factory.MailingComponentFactory;
import com.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.Const;
import com.agnitas.util.Tuple;
import com.agnitas.web.forms.PaginationForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.agnitas.util.ImageUtils.makeMobileDescriptionIfNecessary;
import static com.agnitas.util.ImageUtils.makeMobileFilenameIfNecessary;
import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;

public class MailingComponentsServiceImpl implements MailingComponentsService {

	private static final Logger logger = LogManager.getLogger(MailingComponentsServiceImpl.class);
    private static final String NOT_SUPPORTED_LOG_MSG = "Not supported. See extended scope";

	private MailingDao mailingDao;
	private MailingComponentDao mailingComponentDao;
	private MimeTypeService mimeTypeService;
	private ComponentValidationService componentValidationService;
	private MailingComponentFactory mailingComponentFactory;
	protected ConfigService configService;
    private ExtendedConversionService conversionService;
	private BulkFilesDownloadService bulkFilesDownloadService;

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
    public MailingComponent getComponent(int componentId, int companyID) {
		return mailingComponentDao.getMailingComponent(componentId, companyID);
    }

    @Override
	public MailingComponent getComponent(int companyId, int mailingId, int componentId) {
		return mailingComponentDao.getMailingComponent(mailingId, componentId, companyId);
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

				try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    sendDataToStream(uploadId, os);
                    content = os.toByteArray();
				}
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

    protected void sendDataToStream(int uploadId, ByteArrayOutputStream os) throws Exception {
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
    public PaginatedListImpl<MailingImageDto> getMailingImagesOverview(int companyId, int mailingId, MailingImagesOverviewFilter filter) {
		PaginatedListImpl<MailingComponent> images = mailingComponentDao.getImagesOverview(companyId, mailingId, filter);
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
	public PaginatedListImpl<MailingAttachmentDto> getAttachmentsOverview(PaginationForm form, int mailingId, int companyId) {
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

		return new PaginatedListImpl<>(sortedAttachments, attachments.size(), form.getNumberOfRows(), page, form.getSort(), form.getOrder());
	}

	private Comparator<MailingAttachmentDto> getAttachmentsComparator(PaginationForm form) {
		Comparator<MailingAttachmentDto> comparator = Comparator
				.comparing(MailingAttachmentDto::getId);

		if (StringUtils.equalsIgnoreCase("name", form.getSort())) {
			comparator = Comparator.comparing(a -> StringUtils.lowerCase(a.getName()));
		} else if (StringUtils.equalsIgnoreCase("mimeType", form.getSort())) {
			comparator = Comparator.comparing(a -> StringUtils.lowerCase(a.getMimeType()));
		} else if (StringUtils.equalsIgnoreCase("targetId", form.getSort())) {
			comparator = Comparator.comparing(MailingAttachmentDto::getTargetId);
		} else if (StringUtils.equalsIgnoreCase("originalSize", form.getSort())) {
			comparator = Comparator.comparing(MailingAttachmentDto::getOriginalSize);
		} else if (StringUtils.equalsIgnoreCase("emailSize", form.getSort())) {
			comparator = Comparator.comparing(MailingAttachmentDto::getEmailSize);
		}

		if (!form.ascending()) {
			comparator = comparator.reversed();
		}
		return comparator;
	}

	@Override
	public Map<String, String> getUrlsByNamesForEmmImages(final Admin admin, final int mailingId) {
		final String rdirDomain = admin.getCompany().getRdirDomain();
		final List<MailingComponent> imagesComponents =
				mailingComponentDao.getMailingComponents(mailingId, admin.getCompanyID(), MailingComponentType.HostedImage, false);
		return imagesComponents.stream().collect(Collectors.toMap(
				MailingComponent::getComponentName,
				e -> ComponentsUtils.getImageUrl(rdirDomain, admin.getCompanyID(), mailingId, e.getComponentName()),
				(x, y) -> x, LinkedHashMap::new));
	}

	@Override
	public List<MailingComponent> getMailingComponents(int mailingId, int companyId, MailingComponentType componentType, boolean includeContent) {
		return mailingComponentDao.getMailingComponents(mailingId, companyId, componentType, includeContent);
	}

    @Override
    public void deleteComponent(int companyId, int mailingId, int componentId) {
		MailingComponent component = mailingComponentDao.getMailingComponent(mailingId, componentId, companyId);
		deleteComponent(component);
	}

    @Override
	public void deleteComponent(MailingComponent component) {
		if (component != null) {
			mailingComponentDao.deleteMailingComponent(component);
		}
	}

	@Override
	public boolean deleteImages(int companyId, int mailingId, Set<Integer> bulkIds) {
    	return mailingComponentDao.deleteImages(companyId, mailingId, bulkIds);
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

            if (StringUtils.endsWithIgnoreCase(filename, ".zip")) {
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
    						logger.info("uploadImagesBulk(): found image file '" + path + "' in ZIP stream");
    						countOverall++;

    						if (validateFileSize(archiveFilename + "/" + path, entry.getSize())) {
    							byte[] content = StreamHelper.streamToByteArray(zipStream);
    							if (ImageUtils.isValidImage(content, configService.getBooleanValue(ConfigValue.UseAdvancedFileContentTypeDetection))) {
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

                    if (ImageUtils.isValidImage(content, configService.getBooleanValue(ConfigValue.UseAdvancedFileContentTypeDetection))) {
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
				.collect(Collectors.toList());
	}

	@Override
	public ServiceResult<UserAction> delete(Set<Integer> ids, int mailingId, Admin admin) {
		List<MailingComponent> attachments = getAttachments(ids, mailingId, admin);
		attachments.forEach(this::deleteComponent);

		List<Integer> removedIds = attachments.stream().map(MailingComponent::getId).collect(Collectors.toList());

		return ServiceResult.success(
				new UserAction(
						"delete attachments",
						String.format("Attachments IDs (%s) from mailing ID: %d", StringUtils.join(removedIds, ", "), mailingId)
				),
				Message.of(Const.Mvc.SELECTION_DELETED_MSG)
		);
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
}
