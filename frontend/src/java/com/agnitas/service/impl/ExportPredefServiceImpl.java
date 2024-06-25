/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import static org.agnitas.util.Const.Mvc.ERROR_MSG;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.ExportPredef;
import org.agnitas.dao.ExportPredefDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.exception.UnknownUserStatusException;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.FileCompressionType;
import org.agnitas.service.RecipientExportWorker;
import org.agnitas.service.RecipientExportWorkerFactory;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.importvalues.Charset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.messages.Message;
import com.agnitas.service.ExportPredefService;
import com.agnitas.service.ServiceResult;

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
    public int save(ExportPredef src, Admin admin) throws Exception {
        if (!isManageAllowed(src, admin)) {
            throw new UnsupportedOperationException();
        }

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
        worker.setCompressionType(FileCompressionType.ZIP);
        worker.setUsername(admin.getUsername() + " (ID: " + admin.getAdminID() + ")");
        worker.setRemoteFile(new RemoteFile("", tmpExportFile, -1));
        worker.setMaximumExportLineLimit(configService.getIntegerValue(ConfigValue.ProfileRecipientExportMaxRows, admin.getCompanyID()));
        return worker;
    }
    
    private File getTempRecipientExportFile(int companyId) {
        File companyCsvExportDirectory = getExportDirectory(companyId);
        String dateString = new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES).format(new Date());
        File importTempFile = new File(companyCsvExportDirectory, getMandatoryExportTmpFilePrefix(companyId) + dateString + ".csv.zip");
        int duplicateCount = 1;
        while (importTempFile.exists()) {
            importTempFile = new File(companyCsvExportDirectory, getMandatoryExportTmpFilePrefix(companyId) + dateString + "_" + (duplicateCount++) + ".csv.zip");
        }
        return importTempFile;
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
    public Set<Charset> getAvailableCharsetOptionsForDisplay(Admin admin, ExportPredef export) throws Exception {
        Set<Charset> options = getAvailableCharsetOptions(admin);
        if (export != null && !options.contains(Charset.getCharsetByName(export.getCharset()))) {
            options.add(Charset.getCharsetByName(export.getCharset()));
        }

        return options;
    }

    @Override
    public Set<UserStatus> getAvailableUserStatusOptionsForDisplay(Admin admin, ExportPredef export) throws UnknownUserStatusException {
        Set<UserStatus> options = getAvailableUserStatusOptions(admin);
        if (export != null && export.getUserStatus() != 0 && !options.contains(UserStatus.getUserStatusByID(export.getUserStatus()))) {
            options.add(UserStatus.getUserStatusByID(export.getUserStatus()));
        }

        return options;
    }

    @Override
    public EnumSet<BindingEntry.UserType> getAvailableUserTypeOptionsForDisplay(Admin admin, ExportPredef export) throws Exception {
        EnumSet<BindingEntry.UserType> options = getAvailableUserTypeOptions(admin);
        if (export != null && !options.contains(BindingEntry.UserType.getUserTypeByString(export.getUserType()))) {
            options.add(BindingEntry.UserType.getUserTypeByString(export.getUserType()));
        }

        return options;
    }

    @Override
    public boolean isManageAllowed(ExportPredef export, Admin admin) throws Exception {
        if (export == null) {
            return true;
        }

        final Set<Charset> availableCharsetOptions = getAvailableCharsetOptions(admin);
        if (!availableCharsetOptions.contains(Charset.getCharsetByName(export.getCharset()))) {
            return false;
        }

        final Set<UserStatus> availableUserStatusOptions = getAvailableUserStatusOptions(admin);
        if (export.getUserStatus() != 0 && !availableUserStatusOptions.contains(UserStatus.getUserStatusByID(export.getUserStatus()))) {
            return false;
        }

        final EnumSet<BindingEntry.UserType> availableUserTypeOptions = getAvailableUserTypeOptions(admin);
        return availableUserTypeOptions.contains(BindingEntry.UserType.getUserTypeByString(export.getUserType()));
    }

    protected Set<Charset> getAvailableCharsetOptions(Admin admin) {
        final Map<Charset, Permission> charsetPermissions = Map.of(
                Charset.ISO_8859_15, Permission.CHARSET_USE_ISO_8859_15,
                Charset.UTF_8, Permission.CHARSET_USE_UTF_8
        );

        final Set<Charset> charsets = new HashSet<>(Set.of(Charset.values()));
        charsets.removeAll(Set.of(Charset.ISO_8859_1, Charset.CHINESE_SIMPLIFIED, Charset.ISO_2022_JP, Charset.ISO_8859_2));

        return charsets.stream()
                .filter(c -> !charsetPermissions.containsKey(c) || admin.permissionAllowed(charsetPermissions.get(c)))
                .collect(Collectors.toSet());
    }

    private Set<UserStatus> getAvailableUserStatusOptions(Admin admin) {
        final Map<UserStatus, Permission> statusPermissions = Map.of(UserStatus.Blacklisted, Permission.BLACKLIST);

        return Stream.of(UserStatus.values())
                .filter(s -> !statusPermissions.containsKey(s) || admin.permissionAllowed(statusPermissions.get(s)))
                .collect(Collectors.toSet());
    }

    protected EnumSet<BindingEntry.UserType> getAvailableUserTypeOptions(Admin admin) {
        final EnumSet<BindingEntry.UserType> options = EnumSet.allOf(BindingEntry.UserType.class);
        //options.removeAll(BindingEntry.UserType.TestVIP, BindingEntry.UserType.WorldVIP);
        //options.remove(BindingEntry.UserType.WorldVIP);
        options.removeAll(Set.of(BindingEntry.UserType.TestVIP, BindingEntry.UserType.WorldVIP));
        return options;
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
