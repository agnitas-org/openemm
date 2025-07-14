/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.Permission;
import com.agnitas.messages.Message;
import com.agnitas.service.ExportPredefService;
import com.agnitas.service.ServiceResult;
import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.ExportPredef;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.emm.core.export.dao.ExportPredefDao;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.exception.UnknownUserStatusException;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.FileCompressionType;
import com.agnitas.service.RecipientExportWorker;
import com.agnitas.service.RecipientExportWorkerFactory;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.Const;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.importvalues.Charset;
import com.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.sql.DataSource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExportPredefServiceImpl implements ExportPredefService {

    private static final Logger LOGGER = LogManager.getLogger(ExportPredefServiceImpl.class);
    
    private RecipientExportWorkerFactory recipientExportWorkerFactory;
    private ExportPredefDao exportPredefDao;
    private ConfigService configService;
    private DataSource dataSource;
    private BulkActionValidationService<Integer, ExportPredef> bulkActionValidationService;
    
    @Override
    public ExportPredef get(int id, int companyId) {
        return exportPredefDao.get(id, companyId);
    }

    @Override
    public String findName(int id, int companyId) {
        return exportPredefDao.findName(id, companyId);
    }

    @Override
    public int save(ExportPredef src, Admin admin) {
        if (!isManageAllowed(src, admin)) {
            throw new UnsupportedOperationException();
        }

        return exportPredefDao.save(src);
    }

    @Override
    public List<ExportPredef> getExportProfiles(Admin admin) {
    	return exportPredefDao.getAllExports(admin);
    }

    @Override
    public PaginatedListImpl<ExportPredef> getExportProfilesOverview(PaginationForm form, Admin admin) {
        final List<ExportPredef> exports = getExportProfiles(admin);
        final int page = AgnUtils.getValidPageNumber(exports.size(), form.getPage(), form.getNumberOfRows());

        List<ExportPredef> sortedExports = exports.stream()
                .sorted(getComparator(form))
                .skip((long) (page - 1) * form.getNumberOfRows())
                .limit(form.getNumberOfRows())
                .toList();

        return new PaginatedListImpl<>(sortedExports, exports.size(), form.getNumberOfRows(), page, form.getSort(), form.getOrder());
    }

    private Comparator<ExportPredef> getComparator(PaginationForm form) {
        Comparator<ExportPredef> comparator = (c1, c2) -> 0;

        if (StringUtils.equalsIgnoreCase("shortname", form.getSort())) {
            comparator = Comparator.comparing(c -> StringUtils.trimToEmpty(c.getShortname()));
        } else if (StringUtils.equalsIgnoreCase("description", form.getSort())) {
            comparator = Comparator.comparing(c -> StringUtils.trimToEmpty(c.getDescription()));
        }

        if (!AgnUtils.sortingDirectionToBoolean(form.getOrder(), true)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    @Override
    public List<Integer> getExportProfileIds(Admin admin) {
    	return exportPredefDao.getAllExportIds(admin);
    }
    
    @Override
    public ServiceResult<ExportPredef> getExportForDeletion(int exportId, int companyId) {
        ExportPredef export = get(exportId, companyId);
        if (export == null) {
            return ServiceResult.error(Message.of("error.general.missing"));
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
    public Set<Charset> getAvailableCharsetOptionsForDisplay(Admin admin, ExportPredef export) {
        Set<Charset> options = getAvailableCharsetOptions(admin);
        if (export != null) {
            options.add(Charset.getCharsetByName(export.getCharset()));
        }

        return options;
    }

    @Override
    public Set<UserStatus> getAvailableUserStatusOptionsForDisplay(Admin admin, ExportPredef export) throws UnknownUserStatusException {
        Set<UserStatus> options = getAvailableUserStatusOptions(admin);
        if (export != null && export.getUserStatus() != 0) {
            options.add(UserStatus.getUserStatusByID(export.getUserStatus()));
        }

        return options;
    }

    @Override
    public EnumSet<BindingEntry.UserType> getAvailableUserTypeOptionsForDisplay(Admin admin, ExportPredef export) {
        EnumSet<BindingEntry.UserType> options = getAvailableUserTypeOptions(admin);
        if (export != null && !options.contains(BindingEntry.UserType.getUserTypeByString(export.getUserType()))) {
            options.add(BindingEntry.UserType.getUserTypeByString(export.getUserType()));
        }

        return options;
    }

    @Override
    public ServiceResult<List<ExportPredef>> getAllowedForDeletion(Set<Integer> ids, int companyId) {
        return bulkActionValidationService.checkAllowedForDeletion(ids, id -> getExportForDeletion(id, companyId));
    }

    @Override
    public ServiceResult<UserAction> delete(Set<Integer> ids, Admin admin) {
        List<ExportPredef> exports = ids.stream()
                .map(id -> getExportForDeletion(id, admin.getCompanyID()))
                .filter(ServiceResult::isSuccess)
                .map(ServiceResult::getResult)
                .toList();

        for (ExportPredef export : exports) {
            exportPredefDao.delete(export);
        }

        List<Integer> removedIds = exports.stream().map(ExportPredef::getId).toList();

        return ServiceResult.success(
                new UserAction(
                        "delete export definitions",
                        "deleted export definitions with following ids: " + StringUtils.join(removedIds, ", ")
                ),
                Message.of(Const.Mvc.SELECTION_DELETED_MSG)
        );
    }

    @Override
    public boolean isManageAllowed(ExportPredef export, Admin admin) {
        if (export == null) {
            return true;
        }

        final Set<Charset> availableCharsetOptions = getAvailableCharsetOptions(admin);
        if (!availableCharsetOptions.contains(Charset.getCharsetByName(export.getCharset()))) {
            return false;
        }

        final Set<UserStatus> availableUserStatusOptions = getAvailableUserStatusOptions(admin);
        if (export.getUserStatus() != 0 && !availableUserStatusOptions.contains(UserStatus.findByCode(export.getUserStatus()))) {
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

    public void setExportPredefDao(ExportPredefDao exportPredefDao) {
        this.exportPredefDao = exportPredefDao;
    }
    
    public void setRecipientExportWorkerFactory(RecipientExportWorkerFactory recipientExportWorkerFactory) {
        this.recipientExportWorkerFactory = recipientExportWorkerFactory;
    }
    
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setBulkActionValidationService(BulkActionValidationService<Integer, ExportPredef> bulkActionValidationService) {
        this.bulkActionValidationService = bulkActionValidationService;
    }
}
