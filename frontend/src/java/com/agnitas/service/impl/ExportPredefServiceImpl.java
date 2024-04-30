/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;

import com.agnitas.beans.Admin;
import com.agnitas.messages.Message;
import com.agnitas.service.ExportPredefService;
import com.agnitas.service.ServiceResult;
import org.agnitas.beans.ExportPredef;
import org.agnitas.dao.ExportPredefDao;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.RecipientExportWorker;
import org.agnitas.service.RecipientExportWorkerFactory;
import org.agnitas.util.DateUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import static org.agnitas.util.Const.Mvc.ERROR_MSG;

public class ExportPredefServiceImpl implements ExportPredefService {

    private static final Logger LOGGER = LogManager.getLogger(ExportPredefServiceImpl.class);
    
    private RecipientExportWorkerFactory recipientExportWorkerFactory;
    private ExportPredefDao exportPredefDao;
    private ConfigService configService;
    private DataSource dataSource;
    
    @Override
    public ExportPredef get(int id, int companyId) {
        return exportPredefDao.get(id, companyId);
    }

    @Override
    public String findName(int id, int companyId) {
        return exportPredefDao.findName(id, companyId);
    }

    @Override
    public ExportPredef create(int companyId) {
        return exportPredefDao.create(companyId);
    }

    @Override
    public int save(ExportPredef src) {
        return exportPredefDao.save(src);
    }

    @Override
    public List<ExportPredef> getExportProfiles(Admin admin) {
    	return exportPredefDao.getAllByCompany(admin.getCompanyID());
    }

    @Override
    public List<Integer> getExportProfileIds(Admin admin) {
    	return exportPredefDao.getAllIdsByCompany(admin.getCompanyID());
    }
    
    @Override
    public ServiceResult<ExportPredef> getExportForDeletion(int exportId, int companyId) {
        ExportPredef export = get(exportId, companyId);
        if (export == null) {
            return ServiceResult.error(Message.of(ERROR_MSG));
        }
        return ServiceResult.success(export);
    }

    @Override
    public ServiceResult<ExportPredef> delete(int exportId, int companyId) {
        ServiceResult<ExportPredef> exportForDeletion = getExportForDeletion(exportId, companyId);
        if (!exportForDeletion.isSuccess()) {
            return ServiceResult.error(exportForDeletion.getErrorMessages());
        }
        ExportPredef export = exportForDeletion.getResult();
        return new ServiceResult<>(export, exportPredefDao.delete(export));
    }
    
    @Override
    public RecipientExportWorker getRecipientsToZipWorker(ExportPredef export, Admin admin) throws Exception {
        File tmpExportFile = getTempRecipientExportFile(admin.getCompanyID());
        
        RecipientExportWorker worker = recipientExportWorkerFactory.newWorker(export, admin);
        worker.setDataSource(dataSource);
        worker.setExportFile(tmpExportFile.getAbsolutePath());
        worker.setZipped(true);
        worker.setZippedFileName(getExportDownloadZipName(admin));
        worker.setUsername(admin.getUsername() + " (ID: " + admin.getAdminID() + ")");
        worker.setRemoteFile(new RemoteFile("", tmpExportFile, -1));
        worker.setMaximumExportLineLimit(configService.getIntegerValue(ConfigValue.ProfileRecipientExportMaxRows, admin.getCompanyID()));
        return worker;
    }
    
    private File getTempRecipientExportFile(int companyId) {
        File companyCsvExportDirectory = getExportDirectory(companyId);
        String dateString = new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES).format(new Date());
        File importTempFile = new File(companyCsvExportDirectory, getMandatoryExportTmpFilePrefix(companyId) + dateString + ".zip");
        int duplicateCount = 1;
        while (importTempFile.exists()) {
            importTempFile = new File(companyCsvExportDirectory, getMandatoryExportTmpFilePrefix(companyId) + dateString + "_" + (duplicateCount++) + ".zip");
        }
        return importTempFile;
    }
    
    /**
     * Generates a filename to be used for the Download.
     * This name appear in the download window and is NOT the the real name within the webserver's filesystem.
     *
     * @param admin current user
     * @return filename
     */
    @Override
    public String getExportDownloadZipName(Admin admin) {
        return admin.getCompany().getShortname() + "_"
                + new SimpleDateFormat(DateUtilities.YYYYMD).format(new Date())
                + ".zip";
    }
    
    private File getExportDirectory(int companyId) {
        File companyCsvExportDirectory = new File(RecipientExportWorker.EXPORT_FILE_DIRECTORY + File.separator + companyId);
        if (!companyCsvExportDirectory.exists()) {
            companyCsvExportDirectory.mkdirs();
        }
        return companyCsvExportDirectory;
    }

    public final ExportPredefDao getExportPredefDao() {
    	return this.exportPredefDao;
    }

    @Override
    public ServiceResult<File> getExportFileToDownload(String tmpFileName, Admin admin) {
        if (isInvalidDownloadFileName(tmpFileName, admin.getCompanyID())) {
            LOGGER.error("Illegal temp file for export: {}", tmpFileName);
            return ServiceResult.error(Message.of("error.permissionDenied"));
        }
        File exportedFile = new File(getExportDirectory(admin.getCompanyID()), tmpFileName);
        if (!exportedFile.exists()) {
            return ServiceResult.error(Message.of("error.export.file_not_ready"));
        }
        return ServiceResult.success(exportedFile);
    }
    
    private boolean isInvalidDownloadFileName(String fileName, int companyId) {
        return !fileName.startsWith(getMandatoryExportTmpFilePrefix(companyId))
                || fileName.contains("..")
                || fileName.contains("/")
                || fileName.contains("\\");
    }

    private String getMandatoryExportTmpFilePrefix(int companyId) {
        return "RecipientExport_" + companyId + "_";
    }

    @Required
    public void setExportPredefDao(ExportPredefDao exportPredefDao) {
        this.exportPredefDao = exportPredefDao;
    }
    
    @Required
    public void setRecipientExportWorkerFactory(RecipientExportWorkerFactory recipientExportWorkerFactory) {
        this.recipientExportWorkerFactory = recipientExportWorkerFactory;
    }
    
    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
    
    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
