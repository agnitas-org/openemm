/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.recipients.service.impl;

import static com.agnitas.emm.core.commons.util.ConfigValue.AllowHtmlTagsInReferenceAndProfileFields;
import static com.agnitas.util.AgnUtils.normalizeEmail;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.DatasourceDescription;
import com.agnitas.beans.ImportProfile;
import com.agnitas.beans.ImportStatus;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.Recipient;
import com.agnitas.beans.impl.BindingEntryImpl;
import com.agnitas.beans.impl.DatasourceDescriptionImpl;
import com.agnitas.beans.impl.ImportProfileImpl;
import com.agnitas.beans.impl.ImportStatusImpl;
import com.agnitas.beans.impl.RecipientImpl;
import com.agnitas.dao.RecipientDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.auto_import.bean.RemoteFile;
import com.agnitas.emm.core.binding.service.BindingService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.datasource.enums.SourceGroupType;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipient.dto.RecipientLightDto;
import com.agnitas.emm.core.recipient.exception.SubscriberLimitExceededException;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.recipient.service.SubscriberLimitCheck;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.restful.v2.infrastructure.bulk.BulkDeleteResult;
import com.agnitas.emm.restful.v2.infrastructure.exception.BadRequestException;
import com.agnitas.emm.restful.v2.infrastructure.search.dto.PageForm;
import com.agnitas.emm.restful.v2.recipients.dto.BulkUpsertResponse;
import com.agnitas.emm.restful.v2.recipients.dto.RecipientSubscribeDto;
import com.agnitas.emm.restful.v2.recipients.exception.RecipientNotFoundException;
import com.agnitas.emm.restful.v2.recipients.service.RecipientRestService;
import com.agnitas.emm.util.html.HtmlChecker;
import com.agnitas.emm.util.html.HtmlCheckerException;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.ProfileImportWorker;
import com.agnitas.service.ProfileImportWorkerFactory;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbColumnType.SimpleDataType;
import com.agnitas.util.importvalues.Charset;
import com.agnitas.util.importvalues.CheckForDuplicates;
import com.agnitas.util.importvalues.DateFormat;
import com.agnitas.util.importvalues.Gender;
import com.agnitas.util.importvalues.ImportMode;
import com.agnitas.util.importvalues.MailType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class RecipientRestServiceImpl implements RecipientRestService {

    private static final Logger logger = LogManager.getLogger(RecipientRestServiceImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String IMPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientImport";
    private static final String CUSTOMER_ID_COL = "customer_id";
    private static final String EMAIL_COL = "email";

    private final RecipientService recipientService;
    private final RecipientFieldService recipientFieldService;
    private final ConfigService configService;
    private final SubscriberLimitCheck subscriberLimitCheck;
    private final DataSourceService dataSourceService;
    private final MailinglistService mailinglistService;
    private final BindingService bindingService;
    private final ProfileImportWorkerFactory profileImportWorkerFactory;

    public RecipientRestServiceImpl(RecipientService recipientService,
                                    RecipientFieldService recipientFieldService,
                                    ConfigService configService,
                                    SubscriberLimitCheck subscriberLimitCheck,
                                    DataSourceService dataSourceService,
                                    MailinglistService mailinglistService, BindingService bindingService,
                                    ProfileImportWorkerFactory profileImportWorkerFactory) {
        this.recipientService = recipientService;
        this.recipientFieldService = recipientFieldService;
        this.configService = configService;
        this.subscriberLimitCheck = subscriberLimitCheck;
        this.dataSourceService = dataSourceService;
        this.mailinglistService = mailinglistService;
        this.bindingService = bindingService;
        this.profileImportWorkerFactory = profileImportWorkerFactory;
    }

    @Override
    public Map<String, Object> getById(int recipientId, Admin admin) {
        Map<String, Object> fields = recipientService.getFields(recipientId, admin.getCompanyID());

        if (MapUtils.isEmpty(fields)) {
            throw new RecipientNotFoundException(recipientId);
        }
        Map<String, RecipientFieldDescription> visibleFields = recipientFieldService.getVisibleFields(
                admin.getCompanyID(), admin.getAdminID()
        );
        return getFormattedFieldsMap(admin, fields, visibleFields);
    }

    private static Object getFieldValue(RecipientFieldDescription field, Object value, DateTimeFormatter formatter) {
        if (field.getSimpleDataType() == SimpleDataType.Date && value instanceof Date date) {
            return new SimpleDateFormat(DateUtilities.ISO_8601_DATE_FORMAT_NO_TIMEZONE).format(date);
        }
        if (field.getSimpleDataType() == SimpleDataType.DateTime && value instanceof Date dateTime) {
            ZonedDateTime zdt = ZonedDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());
            return formatter.format(zdt);
        }
        return value;
    }

    @Override
    public SimpleServiceResult delete(int id, Admin admin) {
        RecipientLightDto recipient = recipientService.getRecipientLightDto(admin.getCompanyID(), id);
        if (recipient == null) {
            throw new RecipientNotFoundException(id);
        }
        return recipientService.delete(id, admin.getCompanyID(), admin);
    }

    @Override
    public List<BulkDeleteResult> delete(List<Integer> ids, Admin admin) {
        return ids.stream()
            .map(id -> deleteRecipient(id, admin))
            .toList();
    }

    private BulkDeleteResult deleteRecipient(int id, Admin admin) {
        try {
            SimpleServiceResult serviceResult = delete(id, admin);
            if (!serviceResult.isSuccess()) {
                return BulkDeleteResult.failed(id, serviceResult.getErrorMessages().iterator().next());
            }
            return BulkDeleteResult.deleted(id);
        } catch (RecipientNotFoundException e) {
            return BulkDeleteResult.notFound(id);
        }
    }

    @Override
    public Map<String, Object> create(Map<String, Object> newFields, RecipientSubscribeDto subscribeDto, Admin admin) {
        validateForCreate(newFields, subscribeDto, admin);

        RecipientImpl recipient = new RecipientImpl();
        recipient.setCompanyID(admin.getCompanyID());



        return createOrUpdate(recipient, newFields, subscribeDto, admin);
    }

    @Override
    public Map<String, Object> updatePartially(
        int id,
        Map<String, Object> fields,
        RecipientSubscribeDto subscribeDto,
        Admin admin
    ) {
        int companyID = admin.getCompanyID();
        validateForChange(fields, subscribeDto, admin);

        RecipientImpl recipient = new RecipientImpl();
        recipient.setCompanyID(companyID);
        recipient.setCustParameters(recipientService.getCustomerDataFromDb(companyID, id, recipient.getDateFormat()));

        return createOrUpdate(recipient, fields, subscribeDto, admin);
    }

    private Map<String, Object> createOrUpdate(
        Recipient recipient,
        Map<String, Object> fields,
        RecipientSubscribeDto subscribeDto,
        Admin admin
    ) {
        fillRecipient(recipient, fields, admin);
        tryUpdateRecipient(recipient);
        subscribe(recipient, subscribeDto, admin);
        return getById(recipient.getCustomerID(), admin);
    }

    private void subscribe(Recipient recipient, RecipientSubscribeDto subscribeDto, Admin admin) {
        boolean onlyUpdateExistingBindings = "*".equals(subscribeDto.getMailinglist());
        List<Integer> mailinglistsToSubscribe = detectMailinglistsToSubscribe(subscribeDto.getMailinglist(), admin);
        for (int mailinglistId : mailinglistsToSubscribe) {
            BindingEntry binding = createBindingEntry(recipient.getCustomerID(), mailinglistId, subscribeDto, admin);
            int updatedBindings = bindingService.updateBindings(admin.getCompanyID(), binding);
            if (updatedBindings != 1 && !onlyUpdateExistingBindings) {
                bindingService.insertBindings(admin.getCompanyID(), binding);
            }
        }
    }

    private List<Integer> detectMailinglistsToSubscribe(String mailinglistIdStr, Admin admin) {
        if ("*".equals(mailinglistIdStr)) {
            return mailinglistService.getMailinglistIds(admin.getCompanyID());
        }
        if (AgnUtils.isNumber(mailinglistIdStr)) {
            return List.of(Integer.parseInt(mailinglistIdStr));
        }
        return emptyList();
    }

    private static BindingEntry createBindingEntry(
            int customerId, int mailinglistId, RecipientSubscribeDto subscribeDto, Admin admin
    ) {
        BindingEntry bindingEntry = new BindingEntryImpl();
        bindingEntry.setCustomerID(customerId);
        bindingEntry.setMailinglistID(mailinglistId);
        bindingEntry.setUserStatus(subscribeDto.getStatus().getStatusCode());
        bindingEntry.setUserType(BindingEntry.UserType.World.getTypeCode());
        bindingEntry.setMediaType(subscribeDto.getMediatype().getMediaCode());
        bindingEntry.setUserRemark("Set by " + admin.getUsername() + " via restful");
        return bindingEntry;
    }

    private void fillRecipient(Recipient recipient, Map<String, Object> fields, Admin admin) {
        Map<String, Object> recipientParameters = recipient.getCustParameters();
        removeTripleDateEntries(recipientParameters);

        if (configService.getBooleanValue(ConfigValue.AnonymizeAllRecipients, admin.getCompanyID())) {
            recipientParameters.put(RecipientStandardField.DoNotTrack.getColumnName(), 1);
        }
        recipientParameters.putAll(fields);
        if (!recipientParameters.containsKey("gender")) {
            recipientParameters.put("gender", Gender.UNKNOWN.getStorageValue());
        }
        if (!recipientParameters.containsKey("mailtype")) {
            recipientParameters.put("mailtype", MailType.HTML.getIntValue());
        }
        if (!configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, admin.getCompanyID())) {
            recipientParameters.put(EMAIL_COL, normalizeEmail((String) recipientParameters.get(EMAIL_COL)));
        }
        setDatasourceFields(admin, recipient);
    }

    private void setDatasourceFields(Admin admin, Recipient recipient) {
        DatasourceDescription datasource = dataSourceService.getByDescription(
                SourceGroupType.RestfulService, "RestfulService", admin.getCompanyID()
        );
        if (datasource == null) {
            // Use fallback datasource for companyId 0
            datasource = dataSourceService.getByDescription(SourceGroupType.RestfulService, "RestfulService", 0);
        }
        if (datasource != null) {
            recipient.getCustParameters().put("datasource_id", datasource.getId());
            recipient.getCustParameters().put("latest_datasource_id", datasource.getId());
        }
    }

    private void tryUpdateRecipient(Recipient recipient) {
        try {
            recipient.setChangeFlag(true);
            if (!recipientService.update(recipient)) {
                throw new BadRequestException("Invalid data for recipient");
            }
        } catch (Exception e) {
            throw new BadRequestException("Invalid data for recipient");
        }
    }

    private void removeTripleDateEntries(Map<String, Object> params) {
        params.entrySet()
                .removeIf(e -> Stream.of(RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY,
                                RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH,
                                RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR,
                                RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR,
                                RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE,
                                RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND)
                        .anyMatch(suf -> Strings.CI.endsWith(e.getKey(), suf)));
    }

    private void validateForCreate(Map<String, Object> fields, RecipientSubscribeDto subscribeDto, Admin admin) {
        if (fields.containsKey(CUSTOMER_ID_COL)) {
            throw new BadRequestException(Map.of(CUSTOMER_ID_COL, "Internal field is included"));
        }
        checkLimit(admin);
        validateForChange(fields, subscribeDto, admin);
    }

    private void checkLimit(Admin admin) {
        try {
            subscriberLimitCheck.checkSubscriberLimit(admin.getCompanyID(), 1);
        } catch (SubscriberLimitExceededException e) {
            throw new BadRequestException("""
                Number of customer entries allowed is going to be exceeded.
                Number of existing customers would be '%s'.
                Maximum customer number limit is '%s'.""".formatted(e.getActual(), e.getMaximum())
            );
        }
    }

    private void validateForChange(Map<String, Object> fields, RecipientSubscribeDto subscribeDto, Admin admin) {
        if (!isValidMailinglist(subscribeDto.getMailinglist(), admin.getCompanyID())) {
            throw new BadRequestException("Invalid mailinglist: '%s'".formatted(subscribeDto.getMailinglist()));
        }

        checkInnerColumns(fields, admin);

        int companyId = admin.getCompanyID();
        int adminId = admin.getAdminID();
        boolean allowHtmlTags = configService.getBooleanValue(AllowHtmlTagsInReferenceAndProfileFields, companyId);
        Set<String> changeableFields
                = new HashSet<>(recipientFieldService.getNamesOfChangeableFields(companyId, adminId));
        changeableFields.addAll(List.of(CUSTOMER_ID_COL, EMAIL_COL));

        fields.forEach((field, value) -> {
            if (!changeableFields.contains(field)) {
                throw new BadRequestException(Map.of(
                    field,
                    "Allowed values: [%s]".formatted(StringUtils.join(changeableFields, "', '"))
                ));
            }
            if (value instanceof String stringValue) {
                validateFieldHtml(field, stringValue, allowHtmlTags);
            }
        });
    }

    private boolean isValidMailinglist(String mailinglistStr, int companyId) {
        if (AgnUtils.isNumber(mailinglistStr)) {
            return mailinglistService.exist(Integer.parseInt(mailinglistStr), companyId);
        }
        return isEmpty(mailinglistStr) || "*".equals(mailinglistStr);
    }

    private static void checkInnerColumns(Map<String, Object> fields, Admin admin) {
        List<String> hiddenColumns = RecipientStandardField
            .getImportChangeNotAllowedColumns(admin.permissionAllowed(Permission.IMPORT_CUSTOMERID));
        fields.keySet().stream()
            .filter(field -> hiddenColumns.contains(field.toLowerCase()))
            .findFirst()
            .ifPresent(field -> {
                throw new BadRequestException(Map.of(field, "Internal field is included"));
            });
    }

    private static void validateFieldHtml(String field, String value, boolean allowHtmlTags) {
        try {
            HtmlChecker.checkForUnallowedHtmlTags(value, allowHtmlTags);
        } catch (HtmlCheckerException e) {
            throw new BadRequestException("Invalid recipient data containing HTML for field: %s".formatted(field));
        }
    }

    private Map<String, Object> getFormattedFieldsMap(
            Admin admin,
            Map<String, Object> toFormat,
            Map<String, RecipientFieldDescription> visibleFields
    ) {
        DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern(DateUtilities.ISO_8601_DATETIME_FORMAT)
            .withZone(TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId());

        Map<String, Object> map = LinkedHashMap.newLinkedHashMap(toFormat.size());
        AgnUtils
            .sortCollectionWithItemsFirst(toFormat.keySet(), CUSTOMER_ID_COL, EMAIL_COL, "firstname", "lastname")
            .stream()
            .filter(name -> visibleFields.get(name) != null)
            .map(String::toLowerCase)
            .forEach(name -> map.put(name, getFieldValue(visibleFields.get(name), toFormat.get(name), formatter)));
        return map;
    }

    @Override
    public boolean isAllowedToUseHtml(String columnName, Admin admin) {
        if (recipientService.getEditableColumns(admin).get(columnName) != null) {
            return configService.allowHtmlInReferenceAndProfileFields(admin.getCompanyID());
        }
        return true;
    }

    @Override
    public BulkUpsertResponse create(
            List<Map<String, Object>> recipients,
            RecipientSubscribeDto subscribeDto,
            Admin admin
    ) throws IOException {
        recipients.forEach(recipient -> validateForCreate(recipient, subscribeDto, admin));

        ImportStatus status = importRecipients(recipients, subscribeDto, admin, ImportMode.ADD, null);

        return getBulkUpsertResponse(recipients, status);
    }

    private static BulkUpsertResponse getBulkUpsertResponse(List<Map<String, Object>> recipients, ImportStatus status) {
        Map<String, Integer> errors = status.getErrors().entrySet().stream().collect(Collectors.toMap(
                s -> s.getKey().name(),
                Map.Entry::getValue
        ));
        int errorsCount = status.getErrors().values().stream().mapToInt(i -> i).sum();
        return new BulkUpsertResponse(recipients.size(), recipients.size() - errorsCount, errorsCount, errors);
    }

    @Override
    public BulkUpsertResponse insertOrUpdatePartially(
            List<Map<String, Object>> recipients,
            RecipientSubscribeDto subscribeDto,
            Admin admin
    ) throws IOException {
        recipients.forEach(recipient -> validateForChange(recipient, subscribeDto, admin));

        ImportStatus status = importRecipients(recipients, subscribeDto, admin, ImportMode.ADD_AND_UPDATE, EMAIL_COL);

        return getBulkUpsertResponse(recipients, status);
    }

    @Override
    public PaginatedList<Map<String, Object>> list(PageForm pageReq, Map<String, String> fieldFilters, Admin admin) {
        CaseInsensitiveMap<String, RecipientFieldDescription> visibleFields = recipientFieldService.getVisibleFields(
                admin.getCompanyID(), admin.getAdminID()
        );
        List<String> unknownFields = fieldFilters.keySet()
                .stream()
                .filter(field -> !visibleFields.containsKey(field))
                .toList();

        if (isNotEmpty(unknownFields)) {
            throw new BadRequestException("Invalid recipient filter by unknown recipient fields: %s"
                    .formatted(unknownFields.stream().collect(Collectors.joining(", ", "[", "]"))));
        }
        List<Integer> recipientIds = recipientService.getFilteredRecipientIDs(
                admin.getCompanyID(), visibleFields, fieldFilters
        );
        if (recipientIds.isEmpty()) {
            return PaginatedList.empty();
        }

        List<Map<String, Object>> fullList = recipientService.getFields(recipientIds, admin.getCompanyID())
                .stream()
                .map(fields -> getFormattedFieldsMap(admin, fields, visibleFields)).toList();

        return PaginatedList.of(fullList, pageReq.getPage(), pageReq.getPageSize());
    }

    private ImportStatus importRecipients(
            List<Map<String, Object>> recipients,
            RecipientSubscribeDto subscribeDto,
            Admin admin,
            ImportMode importMode,
            String keyColumn
    ) throws IOException {
        String uuid = AgnUtils.generateNewUUID().toString().replace("-", "").toUpperCase();
        File importTempFile = createImportTempFile(admin, uuid);

        mapper.writeValue(importTempFile, recipients);

        ProfileImportWorker profileImportWorker = profileImportWorkerFactory.getProfileImportWorker(
                false, // Not interactive mode, because there is no error edit GUI
                detectMailinglistsToSubscribe(subscribeDto.getMailinglist(), admin),
                uuid,
                admin.getCompanyID(),
                admin,
                saveImportDatasource(admin, importTempFile).getId(),
                prepareImportProfile(admin, importMode, keyColumn, subscribeDto.getMediatype()),
                new RemoteFile(importTempFile.getName(), importTempFile, -1),
                new ImportStatusImpl());
        profileImportWorker.setMaxGenderValue(admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)
                ? ConfigService.MAX_GENDER_VALUE_EXTENDED
                : ConfigService.MAX_GENDER_VALUE_BASIC);

        profileImportWorker.call();

        return getWorkerResultStatus(profileImportWorker);
    }

    private static ImportStatus getWorkerResultStatus(ProfileImportWorker profileImportWorker) {
        if (profileImportWorker.getError() != null) {
            logger.error(profileImportWorker.getError().getMessage());
            throw new BadRequestException("Error occurred during recipients import. Please contact support team.");
        }
        ImportStatus status = profileImportWorker.getStatus();
        if (status.getFatalError() != null) {
            throw new BadRequestException(status.getFatalError());
        }
        return status;
    }

    private static ImportProfile prepareImportProfile(
            Admin admin, ImportMode importMode, String keyColumn, MediaTypes mediaType
    ) {
        ImportProfile importProfile = new ImportProfileImpl();
        importProfile.setCompanyId(admin.getCompanyID());
        importProfile.setAdminId(admin.getAdminID());
        importProfile.setUpdateAllDuplicates(true);
        importProfile.setDateFormat(DateFormat.ISO8601.getIntValue());
        importProfile.setImportMode(importMode.getIntValue());
        importProfile.setCheckForDuplicates(CheckForDuplicates.COMPLETE.getIntValue());
        importProfile.setNullValuesAction(0);
        importProfile.setKeyColumn(keyColumn);
        importProfile.setAutoMapping(true);
        importProfile.setDefaultMailType(MailType.HTML.getIntValue());
        importProfile.setDatatype("JSON"); // use JSON Import
        importProfile.setCharset(Charset.UTF_8.getIntValue());
        importProfile.setMediatypes(Set.of(mediaType));
        importProfile.setReportLocale(admin.getLocale());
        importProfile.setReportTimezone(admin.getAdminTimezone());
        return importProfile;
    }

    private DatasourceDescription saveImportDatasource(Admin admin, File importTempFile) {
        DatasourceDescription datasource = new DatasourceDescriptionImpl();
        datasource.setCompanyID(admin.getCompanyID());
        datasource.setSourceGroupType(SourceGroupType.File);
        datasource.setCreationDate(new Date());
        datasource.setDescription(importTempFile.getName());
        datasource.setDescription2("Restful-Import");
        dataSourceService.save(datasource);
        return datasource;
    }

    private static File createImportTempFile(Admin admin, String requestUUID) {
        Path importDir = Path.of(IMPORT_FILE_DIRECTORY, String.valueOf(admin.getCompanyID()));
        try {
            Files.createDirectories(importDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create directory: " + importDir, e);
        }

        return importDir.resolve("Recipientimportjson_" + requestUUID + ".json").toFile();
    }
}
