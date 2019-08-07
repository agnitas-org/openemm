/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
import org.agnitas.util.DateUtilities;
import org.agnitas.util.SafeString;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.upload.FormFile;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComAdminDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.upload.bean.DownloadData;
import com.agnitas.emm.core.upload.bean.UploadData;
import com.agnitas.emm.core.upload.dao.ComUploadDao;
import com.agnitas.emm.core.upload.service.UploadService;
import com.agnitas.emm.core.upload.service.dto.EmailEntry;
import com.agnitas.emm.core.upload.service.dto.PageSetUp;
import com.agnitas.emm.core.upload.service.dto.PageUploadData;
import com.agnitas.emm.core.upload.service.dto.UploadFileDescription;
import com.agnitas.messages.I18nString;

public class UploadServiceImpl implements UploadService {
    private static final Logger logger = Logger.getLogger(UploadServiceImpl.class);

    private ComUploadDao uploadDao;
    private ConversionService conversionService;
    private ComCompanyDao companyDao;
    private ComAdminDao adminDao;
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

        List<PageUploadData> pageUploadDataList = transformListToPageUploadData(paginatedList.getList());

        pageUploadDataList.forEach(data -> calculateExpireDate(pageSetUp.getCompanyId(), data));

        return new PaginatedListImpl<>(pageUploadDataList,
                paginatedList.getFullListSize(),
                paginatedList.getPageSize(),
                paginatedList.getPageNumber(),
                paginatedList.getSortCriterion(),
                paginatedList.getSortDirection().toString());

    }

    @Override
    public UploadFileDescription getUploadFileDescription(int id) {
        DownloadData downloadData = uploadDao.getDownloadData(id);
        return conversionService.convert(downloadData, UploadFileDescription.class);
    }

    @Override
    public List<AdminEntry> getUsers(int companyId) {
        return adminDao.getAllAdminsByCompanyId(companyId);
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
    public void uploadFiles(UploadFileDescription description, List<MultipartFile> files) {
        if (description.getUploadId() > 0) {
            UploadData data = conversionService.convert(description, UploadData.class);
            uploadDao.updateData(data);
        }

        files.stream()
                .map(file -> saveFile(file, description))
                .forEach(this::sendNotification);
    }

    @Override
    @Transactional
    public File getDataForDownload(int id) {
        try {
            DownloadData downloadData = uploadDao.getDownloadData(id);
            String suffix = downloadData.getFileType();
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

    private void sendNotification(UploadData data) {
        Locale aLoc = data.getLocale();
        int companyID = data.getCompanyID();

        String shortname = companyDao.getCompany(companyID).getShortname();
        String contactMail = data.getContactMail();
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
            javaMailService.sendEmail(data.getContactMail(), null, null, null, null,
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

    private List<PageUploadData> transformListToPageUploadData(List<UploadData> paginatedList) {
        TypeDescriptor source = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(UploadData.class));
        TypeDescriptor target = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(PageUploadData.class));

        @SuppressWarnings("unchecked")
		List<PageUploadData> returnList = (List<PageUploadData>) conversionService.convert(paginatedList, source, target);
        return returnList;
    }

    private PageUploadData calculateExpireDate(int companyId, PageUploadData pageUploadData) {
        int expireRange = companyDao.getCompany(companyId).getExpireUpload();
        LocalDate date = DateUtilities.toLocalDate(pageUploadData.getCreateDate());
        Date deleteDate = DateUtilities.toDate(date.plusDays(expireRange));
        pageUploadData.setDeleteDate(deleteDate);

        return pageUploadData;
    }

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
            public void setFileName(String fileName) {
                // nothing to do
            }

            @Override
            public void setFileSize(int fileSize) {
                // nothing to do
            }
        };
    }
	
    @Required
	public void setUploadDao(ComUploadDao uploadDao) {
		this.uploadDao = uploadDao;
	}
    @Required
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}
    @Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}
    @Required
	public void setAdminDao(ComAdminDao adminDao) {
		this.adminDao = adminDao;
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
