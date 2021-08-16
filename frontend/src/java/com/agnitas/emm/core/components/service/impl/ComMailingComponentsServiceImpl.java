/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import static com.agnitas.util.ImageUtils.makeMobileDescriptionIfNecessary;
import static com.agnitas.util.ImageUtils.makeMobileFilenameIfNecessary;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.beans.factory.MailingComponentFactory;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.Const;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.struts.upload.FormFile;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.commons.StreamHelper;
import com.agnitas.emm.core.components.dto.NewFileDto;
import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ComSftpService;
import com.agnitas.service.MimeTypeService;
import com.agnitas.service.ServiceResult;
import com.agnitas.util.ImageUtils;

public class ComMailingComponentsServiceImpl implements	ComMailingComponentsService {
	private static final transient Logger logger = Logger.getLogger(ComMailingComponentsServiceImpl.class);

	private static final Set<String> SFTP_PERMITTED_EXTENSIONS = ImageUtils.getValidImageFileExtensions();

	private ComMailingDao mailingDao;
	private ComMailingComponentDao mailingComponentDao;
	private MimeTypeService mimeTypeService;
	private ComSftpService sftpService;
	private MailingComponentFactory mailingComponentFactory;
	private ConfigService configService;

    @Override
    public MailingComponent getMailingTextTemplate(int mailingId, int companyID) {
        return mailingComponentDao.getMailingComponentByName(mailingId, companyID, Const.Component.NAME_TEXT);
    }

	@Override
	public Map<Integer, String> getImageSizes(@VelocityCheck int companyId, int mailingId) {
		Map<Integer, String> map = new HashMap<>();

		mailingComponentDao.getImageSizes(companyId, mailingId)
				.forEach((componentId, size) -> map.put(componentId, AgnUtils.bytesToKbStr(size)));

		return map;
	}

	@Override
	public Map<String, Integer> getImageSizeMap(int companyId, int mailingId, boolean includeExternalImages) {
		Map<Integer, Integer> sizeMap = mailingComponentDao.getImageSizes(companyId, mailingId);
		Map<String, Integer> map = new HashMap<>();

		mailingComponentDao.getImageNames(companyId, mailingId, includeExternalImages)
			.forEach((componentId, name) -> map.put(name, sizeMap.getOrDefault(componentId, 0)));

		return map;
	}

	@Override
	public Map<Integer, String> getImageTimestamps(@VelocityCheck int companyId, int mailingId, DateFormat format) {
		Map<Integer, String> map = new HashMap<>();

		mailingComponentDao.getImageComponentsTimestamps(companyId, mailingId)
				.forEach((componentId, timestamp) -> map.put(componentId, format.format(timestamp)));

		return map;
	}
	
	@Override
    public List<MailingComponent> getComponents(@VelocityCheck int companyID, int mailingId, Set<Integer> componentIds) {
        if (companyID <= 0 || mailingId <= 0 || CollectionUtils.isEmpty(componentIds)) {
        	return Collections.emptyList();
		}
  
		return mailingComponentDao.getMailingComponents(companyID, mailingId, componentIds);
    }

    @Override
	public List<MailingComponent> getComponents(@VelocityCheck int companyId, int mailingId, boolean includeContent) {
		if (companyId <= 0 || mailingId <= 0) {
			return Collections.emptyList();
		}

		return mailingComponentDao.getMailingComponents(mailingId, companyId, includeContent);
	}
    
    @Override
    public MailingComponent getComponent(int componentId, @VelocityCheck int companyID) {
		return mailingComponentDao.getMailingComponent(componentId, companyID);
    }
    
    @Override
    public List<MailingComponent> getComponentsByType(@VelocityCheck int companyID, int mailingId, List<MailingComponentType> types) {
		if (CollectionUtils.isEmpty(types)) {
			return mailingComponentDao.getMailingComponents(mailingId, companyID);
		}
		
		return mailingComponentDao.getMailingComponentsByType(companyID, mailingId, types);
    }
	
	@Override
	public void deleteComponent(MailingComponent component) {
		if (component != null) {
			mailingComponentDao.deleteMailingComponent(component);
		}
	}

	@Override
	public boolean deleteImages(@VelocityCheck int companyId, int mailingId, Set<Integer> bulkIds) {
    	return mailingComponentDao.deleteImages(companyId, mailingId, bulkIds);
	}

	@Override
	public ServiceResult<Boolean> reloadImage(ComAdmin admin, int mailingId, int componentId) {
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
	public ServiceResult<ImportStatistics> importImagesBulk(ComAdmin admin, int mailingId, List<NewFileDto> newFiles, List<UserAction> userActions) {
    	if (mailingDao.exist(mailingId , admin.getCompanyID())) {
    		if (newFiles.isEmpty()) {
    			return ServiceResult.success(new ImportStats(0, 0));
			}

    		return doImport(admin, mailingId, importer -> {
				for (NewFileDto newFile : newFiles) {
					importer.importFile(newFile, userActions);
				}
			});
		} else {
    		return ServiceResult.error(Message.of("Error"));
		}
	}

	@Override
	public ServiceResult<ImportStatistics> importImagesFromSftp(ComAdmin admin, int mailingId, String sftpServerAndAuthConfigString, String sftpPrivateKeyString, String sftpFilePath, List<UserAction> userActions) {
		final String dir = FilenameUtils.getPath(sftpFilePath);
		final String mask = getSftpFileMask(sftpFilePath);

		if (mask == null) {
			logger.error("importImagesFromSftp(): file not found on SFTP server");
			return ServiceResult.error(Message.of("mailing.errors.sftpUploadFailed"));
		}

		return doImport(admin, mailingId, importer -> importer.importSftpDir(sftpServerAndAuthConfigString, sftpPrivateKeyString, dir, mask, userActions));
	}

	private ServiceResult<ImportStatistics> doImport(ComAdmin admin, int mailingId, Consumer<ImageImporter> importerCalls) {
		final int maximumUploadImageSize = configService.getIntegerValue(ConfigValue.MaximumUploadImageSize);
		final int maximumWarningImageSize = configService.getIntegerValue(ConfigValue.MaximumWarningImageSize);
		final String mailingName = mailingDao.getMailingName(mailingId, admin.getCompanyID());

		ImageImporter importer = new ImageImporter(mailingId, mailingName, maximumUploadImageSize, maximumWarningImageSize);

		importerCalls.accept(importer);

		if (importer.getCountValid() > 0) {
			saveNewComponents(admin, mailingId, importer.getComponentsMap());
		}

		List<Message> warnings = new ArrayList<>();
		List<Message> errors = new ArrayList<>();

		List<String> sizeWarningFiles = importer.getSizeWarningFiles();
		if (sizeWarningFiles.size() > 0) {
			String message = I18nString.getLocaleString("warning.component.size", admin.getLocale(), maximumWarningImageSize / 1024f / 1024) +
					filenamesToHtml(sizeWarningFiles);

			warnings.add(Message.exact(message));
		}

		List<String> sizeErrorFiles = importer.getSizeErrorFiles();
		if (sizeErrorFiles.size() > 0) {
			String message = I18nString.getLocaleString("component.size.error", admin.getLocale(), maximumUploadImageSize / 1024f / 1024) +
					filenamesToHtml(sizeErrorFiles);

			errors.add(Message.exact(message));
		}

		List<String> invalidFiles = importer.getInvalidFiles();
		if (invalidFiles.size() > 0) {
			String message = I18nString.getLocaleString("grid.divchild.format.error", admin.getLocale()) + filenamesToHtml(invalidFiles);
			errors.add(Message.exact(message));
		}

		List<String> invalidArchives = importer.getInvalidArchives();
		if (invalidArchives.size() > 0) {
			String message = I18nString.getLocaleString("mailing.Graphics_Component.zipUploadFailed", admin.getLocale()) + filenamesToHtml(invalidArchives);
			errors.add(Message.exact(message));
		}

		List<String> invalidSftpServers = importer.getInvalidSftpServers();
		if (invalidSftpServers.size() > 0) {
			errors.add(Message.of("mailing.errors.sftpUploadFailed"));
		}

		ImportStatistics statistics = new ImportStats(importer.getCountOverall(), importer.getCountValid());
		return new ServiceResult<>(statistics, importer.getCountValid() > 0, Collections.emptyList(), warnings, errors);
	}

	private String getSftpFileMask(String sftpFilePath) {
		final String basename = FilenameUtils.getBaseName(sftpFilePath);
		final String extension = FilenameUtils.getExtension(sftpFilePath).toLowerCase();

		if ("*".equals(extension) || ("*".equals(basename) && extension.isEmpty())) {
			return SFTP_PERMITTED_EXTENSIONS.stream()
					.map(e -> basename + "." + e)
					.collect(Collectors.joining("|"));
		} else if (SFTP_PERMITTED_EXTENSIONS.contains(extension)) {
			return basename + "." + extension;
		} else {
			return null;
		}
	}

	private void saveNewComponents(ComAdmin admin, int mailingId, Map<String, MailingComponent> newComponentsMap) {
		Map<String, MailingComponent> existingComponentsMap = getComponentsMap(mailingId, admin.getCompanyID());

		newComponentsMap.forEach((name, newComponent) -> {
			MailingComponent existingComponent = existingComponentsMap.get(name);

			// Prevent overwriting of existing components of other types (other than hosted image).
			if (existingComponent == null || existingComponent.getType() == MailingComponentType.HostedImage) {
				if (existingComponent != null) {
					mailingComponentDao.deleteMailingComponent(existingComponent);
				}

				try {
					newComponent.setMailingID(mailingId);
					newComponent.setCompanyID(admin.getCompanyID());

					mailingComponentDao.saveMailingComponent(newComponent);
				} catch (Exception e) {
					int size = ArrayUtils.getLength(newComponent.getBinaryBlock());
					logger.error(String.format("Failed to save mailing #%d component `%s` (%d byte(s))", mailingId, name, size), e);
				}
			}
		});
	}

	private Map<String, MailingComponent> getComponentsMap(int mailingId, @VelocityCheck int companyId) {
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

	@Required
	public void setMimeTypeService(MimeTypeService mimeTypeService) {
		this.mimeTypeService = mimeTypeService;
	}

	@Required
	public void setSftpService(ComSftpService sftpService) {
		this.sftpService = sftpService;
	}

	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setMailingComponentDao(ComMailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}

	@Required
	public void setMailingComponentFactory(MailingComponentFactory mailingComponentFactory) {
    	this.mailingComponentFactory = mailingComponentFactory;
	}

	@Required
	public void setConfigService(ConfigService configService) {
    	this.configService = configService;
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

	private class ImageImporter {
		private final int mailingId;
		private final String mailingName;
		private final int maximumWarningImageSize;
		private final int maximumUploadImageSize;

		private final Map<String, MailingComponent> componentsMap = new HashMap<>();
		private final List<String> invalidFiles = new ArrayList<>();
		private final List<String> sizeErrorFiles = new ArrayList<>();
		private final List<String> sizeWarningFiles = new ArrayList<>();
		private final List<String> invalidArchives = new ArrayList<>();
		private final List<String> invalidSftpServers = new ArrayList<>();
    	private int countOverall;
    	private int countValid;

    	public ImageImporter(int mailingId, String mailingName, int maximumUploadImageSize, int maximumWarningImageSize) {
    		this.mailingId = mailingId;
    		this.mailingName = mailingName;
    		this.maximumUploadImageSize = maximumUploadImageSize;
    		this.maximumWarningImageSize = maximumWarningImageSize;
		}

		public void importFile(NewFileDto newFile, List<UserAction> userActions) {
    		FormFile file = newFile.getFile();
    		String filename = file.getFileName();

    		if (StringUtils.endsWithIgnoreCase(filename, ".zip")) {
    			importArchive(file, userActions);
			} else {
    			importFile(file, newFile.getLink(), newFile.getDescription(), newFile.getMobileComponentBase(), userActions);
			}
		}

		private void importArchive(FormFile file, List<UserAction> userActions) {
    		String archiveFilename = file.getFileName();

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

		private void importFile(FormFile file, String link, String description, String mobileComponentBase, List<UserAction> userActions) {
    		countOverall++;

			try {
				String filename = file.getFileName();

				if (validateFileSize(filename, file.getFileSize())) {
					byte[] content = file.getFileData();

					if (ImageUtils.isValidImage(content)) {
						importImage(filename, file.getContentType(), content, link, description, mobileComponentBase);
						userActions.add(new UserAction("upload mailing component file", String.format("%s(%d), uploaded image `%s`", mailingName, mailingId, filename)));
					} else {
						invalidFiles.add(filename);
					}
				}
			} catch (IOException e) {
				logger.error("uploadImagesBulk(): IO-error on image file processing", e);
			}
		}

		private void importSftpDir(String sftpServerAndAuthConfigString, String sftpPrivateKeyString, String dir, String mask, List<UserAction> userActions) {
    		try {
    			int countBefore = countValid;

				sftpService.retrieveFiles(sftpServerAndAuthConfigString, sftpPrivateKeyString, dir, mask, (path, stream) -> {
					final String name = FilenameUtils.getName(path);
					countOverall++;
					logger.info("importImagesFromSftp(): found file '" + path + "' on SFTP server");

					try {
						byte[] content = StreamHelper.streamToByteArray(stream);
						if (validateFileSize(name, content.length) && ImageUtils.isValidImage(content)) {
							importImage(name, content);
						}
					} catch (IOException e) {
						logger.error("importImagesFromSftp(): error occurred during a file transfer", e);
					}
				});

				if (countValid > countBefore) {
					userActions.add(new UserAction("upload from sftp", String.format("%s(%d), uploaded images from sftp", mailingName, mailingId)));
				} else {
					logger.error("importImagesFromSftp(): file(s) not found on SFTP server");
					invalidSftpServers.add(sftpServerAndAuthConfigString);
				}
			} catch (Exception e) {
				logger.error("importImagesFromSftp(): failed to import SFTP dir", e);
				invalidSftpServers.add(sftpServerAndAuthConfigString);
			}
		}

		private void importImage(String filename, byte[] content) {
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

		private boolean validateFileSize(String filename, long size) {
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

		public List<String> getInvalidSftpServers() {
			return invalidSftpServers;
		}

		public int getCountOverall() {
			return countOverall;
		}

		public int getCountValid() {
			return countValid;
		}
	}
}
