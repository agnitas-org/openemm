/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.upload.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.SafeString;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.upload.FormFile;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.upload.bean.DownloadData;
import com.agnitas.emm.core.upload.bean.UploadData;
import com.agnitas.emm.core.upload.dao.ComUploadDao;
import com.agnitas.emm.core.upload.service.UploadService;
import com.agnitas.emm.core.upload.service.dto.EmailEntry;
import com.agnitas.emm.core.upload.service.dto.PageSetUp;
import com.agnitas.emm.core.upload.service.dto.PageUploadData;
import com.agnitas.emm.core.upload.service.dto.UploadFileDescription;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.web.mvc.Popups;

public class UploadServiceImpl implements UploadService {
	
	/** The logger. */
    private static final transient Logger logger = LogManager.getLogger(UploadServiceImpl.class);

    private ComUploadDao uploadDao;
    private ExtendedConversionService conversionService;
    private ComCompanyDao companyDao;
    private AdminService adminService;
    private JavaMailService javaMailService;
    private ConfigService configService;

    @Override
    public boolean exists(int id) {
        if (id > 0) {
            return uploadDao.exists(id);
        }

        return false;
    }

    @Override
    public PaginatedListImpl<PageUploadData> getPaginatedFilesDescription(PageSetUp pageSetUp) {
        PaginatedListImpl<UploadData> paginatedList = uploadDao.getPaginatedList(pageSetUp.getCompanyId(),
                pageSetUp.getAdminId(), pageSetUp.getSort(),
                pageSetUp.getOrder(), pageSetUp.getPage(),
                pageSetUp.getNumberOfRows());

        PaginatedListImpl<PageUploadData> pageUploadDataPaginatedList = conversionService.convertPaginatedList(paginatedList, UploadData.class, PageUploadData.class);

        int expireRange = configService.getIntegerValue(ConfigValue.ExpireUpload, pageSetUp.getCompanyId());
        pageUploadDataPaginatedList.getList().forEach(data -> calculateExpireDate(expireRange, data));

        return pageUploadDataPaginatedList;
    }

    @Override
    public UploadFileDescription getUploadFileDescription(int id) {
        DownloadData downloadData = uploadDao.getDownloadData(id);
        return conversionService.convert(downloadData, UploadFileDescription.class);
    }

    @Override
    public List<AdminEntry> getUsers(int companyId) {
        return adminService.listAdminsByCompanyID(companyId);
    }

    @Override
    public void deleteFile(int id) {
        uploadDao.deleteData(id);
    }

    @Override
    public boolean hasPermissionForDelete(int id, int adminId, int companyId) {
        return uploadDao.isOwnerOrAdmin(id, adminId, companyId);
    }

    @Override
    public void uploadFiles(UploadFileDescription description, List<MultipartFile> files, Popups popups, final ComAdmin admin) {
    	long uploadSizeBytes = uploadDao.getCurrentUploadOverallSizeBytes(description.getCompanyId());
    	long maximumOverallSizeBytes = configService.getLongValue(ConfigValue.UploadMaximumOverallSizeBytes, description.getCompanyId());
    	if (uploadSizeBytes > maximumOverallSizeBytes) {
    		popups.alert("error.upload.overallsize", AgnUtils.getHumanReadableNumber(maximumOverallSizeBytes, "Byte", false, admin.getLocale(), false));
    		return;
    	}
    	long maximumSingleFileSizeBytes = configService.getLongValue(ConfigValue.UploadMaximumSizeBytes, description.getCompanyId());
    	for (MultipartFile multipartFile : files) {
    		if (multipartFile.getSize() > maximumSingleFileSizeBytes) {
        		popups.alert("error.upload.filesize", AgnUtils.getHumanReadableNumber(maximumSingleFileSizeBytes, "Byte", false, admin.getLocale(), false), multipartFile.getOriginalFilename(), AgnUtils.getHumanReadableNumber(multipartFile.getSize(), "Byte", false, admin.getLocale(), false));
        		return;
        	}
    		uploadSizeBytes += multipartFile.getSize();
    		if (uploadSizeBytes > maximumOverallSizeBytes) {
        		popups.alert("error.upload.overallsize", AgnUtils.getHumanReadableNumber(maximumOverallSizeBytes, "Byte", false, admin.getLocale(), false));
        		return;
        	}
    	}
    	
        if (description.getUploadId() > 0) {
            UploadData data = conversionService.convert(description, UploadData.class);
            uploadDao.updateData(data);
        }

        files.stream()
                .map(file -> saveFile(file, description))
                .forEach(data -> sendNotification(data, admin));
    }

    @Override
    @Transactional
    public File getDataForDownload(int id) {
        try {
            DownloadData downloadData = uploadDao.getDownloadData(id);
            String suffix = downloadData.getFileType();
            if (!suffix.startsWith(".")) {
            	suffix = "." + suffix;
            }
        	File tempFile = Files.createTempFile(null, suffix).toFile();
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                uploadDao.sendDataToStream(id, outputStream);
            }
            return tempFile;
        } catch (Exception e) {
            logger.warn(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<EmailEntry> getDefaultEmails(String email, Locale locale) {
        List<EmailEntry> emails = new ArrayList<>();

        String localeSupportName = SafeString.getLocaleString("upload.agnitas.support", locale);
        String supportEmail = configService.getValue(ConfigValue.MailAddress_UploadSupport);
        String localeDatebaseName = SafeString.getLocaleString("upload.agnitas.database", locale);
        String datebaseEmail = configService.getValue(ConfigValue.MailAddress_UploadDatabase);
        String localeUserName = SafeString.getLocaleString("upload.account.user", locale);

        EmailEntry entry = new EmailEntry(localeSupportName, supportEmail);
        emails.add(entry);
        entry = new EmailEntry(localeDatebaseName, datebaseEmail);
        emails.add(entry);
        entry = new EmailEntry(localeUserName, email);
        emails.add(entry);

        return emails;
    }

    private void sendNotification(UploadData data, final ComAdmin admin) {
        Locale aLoc = data.getLocale();
        int companyID = data.getCompanyID();

        String shortname = companyDao.getCompany(companyID).getShortname();
        String contactMail = data.getContactMail() != null ? data.getContactMail() : admin.getEmail();
        String filename = data.getFilename();
        String sendToEmail = data.getSendtoMail();

        String subject = I18nString.getLocaleString("upload.mail.subject", aLoc, companyID);
        StringBuilder message = new StringBuilder();
        message.append(I18nString.getLocaleString("upload.mail.text", aLoc, shortname, contactMail))
                .append(data.getDescription())
                .append("<br> ")
                .append(I18nString.getLocaleString("upload.mail.greeting", aLoc))
                .append("<br><br> ")
                .append(I18nString.getLocaleString("upload.mail.file", aLoc, filename));

        if (StringUtils.isNotBlank(sendToEmail)) {
            javaMailService.sendEmail(companyID, contactMail, null, contactMail, null, null,
                    data.getSendtoMail(), null, subject, message.toString(), message.toString(), "UTF-8");
        }
    }

    private UploadData saveFile(MultipartFile file, UploadFileDescription description) {
        UploadData data = conversionService.convert(description, UploadData.class);
        data.setFilename(file.getOriginalFilename());
        data.setFilesize(Long.valueOf(file.getSize()).intValue());
        data.setFileType(file.getContentType());

        try {
            uploadDao.saveData(data, file.getInputStream());
        } catch (Exception e) {
            logger.warn(e);
            throw new RuntimeException(e);
        }

        return data;
    }

    private void calculateExpireDate(int expireRange, PageUploadData pageUploadData) {
        LocalDate date = DateUtilities.toLocalDate(pageUploadData.getCreateDate());
        Date deleteDate = DateUtilities.toDate(date.plusDays(expireRange));
        pageUploadData.setDeleteDate(deleteDate);
    }

    // TODO: GWUA-4801 replace magic "pdf" or "csv" extension string by constant or enum
    @Override
    public List<UploadData> getUploadsByExtension(ComAdmin admin, String extension) {
        return uploadDao.getOverviewListByExtention(admin, Collections.singletonList(Objects.requireNonNull(extension)));
    }

    @Override
    @Deprecated
    public FormFile getFormFileByUploadId(int uploadID, String mime){
        // TODO: The byteArray should not be kept in memory, because it might be quite huge. Better use a Inputstream or temporary local file
        final UploadData uploadData = uploadDao.loadData(uploadID);
        final String fileName = uploadData.getFilename();
        final byte[] fileData = uploadData.getData();
        final Integer fileSize = uploadData.getFilesize();
        final String mimeType = mime;

        return new FormFile() {

            @Override
            public void destroy() {
                // nothing to do
            }

            @Override
            public String getContentType() {
                return mimeType;
            }

            @Override
            public byte[] getFileData() throws FileNotFoundException, IOException {
                return fileData;
            }

            @Override
            public String getFileName() {
                return fileName;
            }

            @Override
            public int getFileSize() {
                return fileSize;
            }

            @Override
            public InputStream getInputStream() throws FileNotFoundException, IOException {
                return new ByteArrayInputStream(fileData);
            }

            @Override
            public void setContentType(String contentType) {
                // nothing to do
            }

            @Override
            public void setFileName(String unusedFileName) {
                // nothing to do
            }

            @Override
            public void setFileSize(int unusedFileSize) {
                // nothing to do
            }
        };
    }
	
    @Required
	public void setUploadDao(ComUploadDao uploadDao) {
		this.uploadDao = uploadDao;
	}
    
    @Required
	public void setConversionService(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}

    @Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}
    
    @Required
	public void setAdminService(AdminService service) {
		this.adminService = Objects.requireNonNull(service, "Admin service is null");
	}
    
    @Required
	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}
    
    @Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
}
