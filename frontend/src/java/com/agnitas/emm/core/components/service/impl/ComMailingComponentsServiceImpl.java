/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.impl.MailingComponentImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.Const;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.struts.upload.FormFile;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.emm.core.commons.StreamHelper;
import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.service.ComSftpService;
import com.agnitas.service.MimeTypeService;
import com.agnitas.util.ImageUtils;

public class ComMailingComponentsServiceImpl implements	ComMailingComponentsService {
	private static final transient Logger logger = Logger.getLogger(ComMailingComponentsServiceImpl.class);

	private static final Set<String> SFTP_PERMITTED_EXTENSIONS = ImageUtils.getValidImageFileExtensions();

	private ComMailingComponentDao mailingComponentDao;
	private MimeTypeService mimeTypeService;
	private ComSftpService sftpService;

    @Override
    public MailingComponent getMailingTextTemplate(int mailingId, int companyID) {
        return mailingComponentDao.getMailingComponentByName(mailingId, companyID, Const.Component.NAME_TEXT);
    }

    @Override
	public UploadStatistics uploadZipArchive(Mailing mailing, FormFile zipFile) throws Exception {
		UploadStatisticsImpl statistics = new UploadStatisticsImpl();

		try (ZipInputStream zipStream = new ZipInputStream(zipFile.getInputStream())) {
			for (ZipEntry entry = zipStream.getNextEntry(); entry != null; entry = zipStream.getNextEntry()) {
				final String path = entry.getName();
				final String name = FilenameUtils.getName(path);

				if (!entry.isDirectory() && ImageUtils.isValidImageFileExtension(FilenameUtils.getExtension(name))) {
					statistics.newFound();
					logger.info("uploadZipArchive(): found image file '" + path + "' in ZIP stream");
					byte[] content = StreamHelper.streamToByteArray(zipStream);
					if (ImageUtils.isValidImage(content)) {
						addComponent(mailing, content, name);
						statistics.newStored();
					}
				}

				zipStream.closeEntry();
			}
		} catch (IOException e) {
			logger.error("uploadZipArchive()", e);
			throw e;
		}

		return statistics;
	}

	private void addComponent(Mailing mailing, byte[] content, String filename) {
		String contentType = mimeTypeService.getMimetypeForFile(filename);
		addHostedComponent(mailing, content, contentType, filename, "", "");
	}

    protected void addHostedComponent(Mailing mailing, byte[] content, String contentType, String filename, String link, String description) {
    	logger.info("addHostedComponent(): content type is '" + contentType + "'");
    	
        MailingComponent component = mailing.getComponents().get(filename);

        if (component != null && component.getType() == MailingComponent.TYPE_HOSTED_IMAGE) {
        	logger.info("addHostedComponent(): replacing existing component");
            component.setBinaryBlock(content, contentType);
            component.setLink(link);
			component.setDescription(description);
        } else {
        	logger.info("addHostedComponent(): adding new component");
            component = new MailingComponentImpl();
            component.setCompanyID(mailing.getCompanyID());
            component.setMailingID(mailing.getId());
            component.setType(MailingComponent.TYPE_HOSTED_IMAGE);
			component.setDescription(description);
            component.setComponentName(filename);
            component.setBinaryBlock(content, contentType);
            component.setLink(link);
            mailing.addComponent(component);
        }
    }

	@Override
	public UploadStatistics uploadSFTP(Mailing mailing, String fileServerAndAuthConfigString, String fileServerPrivateKeyString, String sftpFilePath) throws Exception {
		final String dir = FilenameUtils.getPath(sftpFilePath);
		final String basename = FilenameUtils.getBaseName(sftpFilePath);
		final String extension = FilenameUtils.getExtension(sftpFilePath).toLowerCase();

		String masks;

		if ("*".equals(extension) || ("*".equals(basename) && extension.isEmpty())) {
			masks = SFTP_PERMITTED_EXTENSIONS.stream()
					.map(e -> basename + "." + e)
					.collect(Collectors.joining("|"));
		} else if (SFTP_PERMITTED_EXTENSIONS.contains(extension)) {
			masks = basename + "." + extension;
		} else {
			throw new Exception("File not found on sftp server");
		}

		UploadStatisticsImpl statistics = new UploadStatisticsImpl();
		sftpService.retrieveFiles(fileServerAndAuthConfigString, fileServerPrivateKeyString, dir, masks, (path, stream) -> {
			final String name = FilenameUtils.getName(path);
			statistics.newFound();
			logger.info("uploadSFTP(): found file '" + path + "' on SFTP server");
			try {
				byte[] content = StreamHelper.streamToByteArray(stream);
				if (ImageUtils.isValidImage(content)) {
					addComponent(mailing, content, name);
					statistics.newStored();
				}
			} catch (IOException e) {
				logger.error("uploadSFTP(): error occurred during a file transfer", e);
			}
		});

		return statistics;
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
        	return new ArrayList<>();
		}
  
		return mailingComponentDao.getMailingComponents(companyID, mailingId, componentIds);
    }
    
    @Override
    public MailingComponent getComponent(int componentId, @VelocityCheck int companyID) {
		return mailingComponentDao.getMailingComponent(componentId, companyID);
    }
    
    @Override
    public List<MailingComponent> getComponentsByType(@VelocityCheck int companyID, int mailingId, List<Integer> types) {
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
	public void deleteComponents(@VelocityCheck int companyID, int mailingID, Set<Integer> bulkIds) {
		List<MailingComponent> components = mailingComponentDao.getMailingComponents(companyID, mailingID, bulkIds);
		mailingComponentDao.deleteMailingComponents(components);
	}
	
	@Override
	public void updateHostImage(int mailingID, int companyID, int componentID, byte[] imageBytes) {
		mailingComponentDao.updateHostImage(mailingID, companyID, componentID, imageBytes);
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
	public void setMailingComponentDao(ComMailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}

	private static class UploadStatisticsImpl implements UploadStatistics {
		private int found;
		private int stored;

		@Override
		public int getFound() {
			return found;
		}

		public void newFound() {
			found++;
		}

		@Override
		public int getStored() {
			return stored;
		}

		public void newStored() {
			stored++;
		}
	}
}
