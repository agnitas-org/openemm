/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.quartz;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.agnitas.beans.Recipient;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.service.JobWorkerBase;
import com.agnitas.service.exceptions.JobWorkerConfigurationException;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbColumnType.SimpleDataType;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@JobWorker("HyperPersonalization")
public class HyperPersonalizationJobWorker extends JobWorkerBase {

    private static final Logger logger = LogManager.getLogger(HyperPersonalizationJobWorker.class);

    private static final int STEP_SIZE = 5000;
    private static final int TOP_RESULTS_LIMIT = 3;

    private static final String COUNT_FIELDS_PARAM = "hp_count_fields";
    private static final String DATE_FIELDS_PARAM = "hp_date_fields";

    private static final String RESULT_COUNT_FIELD_PREFIX = "hp_count_resultfield";
    private static final String RESULT_DATE_FIELD_PREFIX = "hp_date_resultfield";

    private Pattern countFieldsPattern;
    private Pattern dateFieldsPattern;

    private final List<String> resultCountFieldNames = new ArrayList<>(TOP_RESULTS_LIMIT);
    private final List<String> resultDateFieldNames = new ArrayList<>(TOP_RESULTS_LIMIT);

    @Override
    public String runJob() throws Exception {
        initFieldNamesPatterns();
        initResultFieldNames();

        daoLookupFactory.getBeanCompanyDao()
                .getActiveCompanies(getCompaniesConstraints())
                .forEach(this::processCompany);

        return null;
    }

    private void initFieldNamesPatterns() {
        countFieldsPattern = getFieldNamePatternFromParam(COUNT_FIELDS_PARAM);
        dateFieldsPattern = getFieldNamePatternFromParam(DATE_FIELDS_PARAM);
    }

    private Pattern getFieldNamePatternFromParam(String paramName) {
        String regex = getParam(paramName);
        if (StringUtils.isBlank(regex)) {
            throw new JobWorkerConfigurationException("Parameter '%s' is missing or blank".formatted(paramName));
        }

        Pattern pattern;
        try {
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            throw new JobWorkerConfigurationException(
                    "Parameter '%s' contains an invalid regular expression: %s".formatted(paramName, e.getMessage()), e
            );
        }

        if (pattern.matcher("").groupCount() < 1) {
            throw new JobWorkerConfigurationException(
                    "Regular expression in parameter '%s' must define at least one capturing group".formatted(paramName)
            );
        }

        return pattern;
    }

    private void initResultFieldNames() {
        IntStream.range(1, TOP_RESULTS_LIMIT + 1)
                .forEach(i -> {
                    String countFieldName = getParam(RESULT_COUNT_FIELD_PREFIX + i);
                    if (StringUtils.isNotBlank(countFieldName)) {
                        resultCountFieldNames.add(countFieldName);
                    }

                    String dateFieldName = getParam(RESULT_DATE_FIELD_PREFIX + i);
                    if (StringUtils.isNotBlank(dateFieldName)) {
                        resultDateFieldNames.add(dateFieldName);
                    }
                });

        if (resultCountFieldNames.isEmpty()) {
            throw new JobWorkerConfigurationException(
                    "No result count fields configured. At least one parameter with prefix '"
                            + RESULT_COUNT_FIELD_PREFIX + "' must be set."
            );
        }

        if (resultDateFieldNames.isEmpty()) {
            throw new JobWorkerConfigurationException(
                    "No result date fields configured. At least one parameter with prefix '"
                            + RESULT_DATE_FIELD_PREFIX + "' must be set."
            );
        }
    }

    private void processCompany(int companyId) {
        List<RecipientFieldDescription> fields = serviceLookupFactory.getRecipientFieldService()
                .getRecipientFields(companyId);

        if (!checkResultFields(fields, companyId)) {
            return;
        }

        List<RecipientFieldDescription> countFields = filterCountFields(fields, companyId);
        List<RecipientFieldDescription> dateFields = filterDateFields(fields, companyId);
        Set<String> columnNames = getColumnNames(ListUtils.union(countFields, dateFields));

        if (columnNames.isEmpty()) {
            if (!getCompaniesConstraints().isEmpty()) {
                logger.warn("Company {}: no fields matched count/date patterns, skipping", companyId);
            }

            return;
        }

        int lastId = 0;
        List<Recipient> recipients;

        while (!(recipients = getRecipients(lastId, companyId, columnNames)).isEmpty()) {
            processRecipients(countFields, dateFields, recipients, companyId);
            lastId = recipients.getLast().getCustomerID();
        }
    }

    private void processRecipients(
            List<RecipientFieldDescription> countFields,
            List<RecipientFieldDescription> dateFields,
            List<Recipient> recipients,
            int companyId
    ) {
        Map<Integer, Map<String, ? extends Object>> batchUpdate = recipients.stream()
                .collect(Collectors.toMap(
                        Recipient::getCustomerID,
                        r -> getResultForRecipient(countFields, dateFields, r)
                ));

        serviceLookupFactory.getRecipientService().batchUpdateData(batchUpdate, companyId);
    }

    private Map<String, String> getResultForRecipient(
            List<RecipientFieldDescription> countFields,
            List<RecipientFieldDescription> dateFields,
            Recipient recipient
    ) {
        Map<String, String> data = new HashMap<>();

        extractTopCountFieldNames(countFields, recipient)
                .forEach((col, res) -> data.put(col, res.orElse(null)));

        extractTopDateFieldNames(dateFields, recipient)
                .forEach((col, res) -> data.put(col, res.orElse(null)));

        return data;
    }

    private boolean checkResultFields(List<RecipientFieldDescription> fields, int companyId) {
        Map<String, RecipientFieldDescription> fieldsByName = fields.stream()
                .collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity()));

        return resultCountFieldNames.stream().allMatch(fn -> checkResultField(fn, fieldsByName, companyId))
                && resultDateFieldNames.stream().allMatch(fn -> checkResultField(fn, fieldsByName, companyId));
    }

    private boolean checkResultField(String fieldName, Map<String, RecipientFieldDescription> fields, int companyId) {
        RecipientFieldDescription field = fields.get(fieldName);

        if (field == null) {
            logger.warn("Company {}: result field '{}' does not exist", companyId, fieldName);
            return false;
        }

        if (field.getSimpleDataType() != SimpleDataType.Characters) {
            logger.warn("Company {}: result field '{}' has invalid type {} (expected Alphanumeric)",
                    companyId, fieldName, field.getSimpleDataType());
            return false;
        }

        return true;
    }

    private List<RecipientFieldDescription> filterCountFields(List<RecipientFieldDescription> fields, int companyId) {
        return filterFields(fields, countFieldsPattern, companyId, SimpleDataType.Numeric, SimpleDataType.Float);
    }

    private List<RecipientFieldDescription> filterDateFields(List<RecipientFieldDescription> fields, int companyId) {
        return filterFields(fields, dateFieldsPattern, companyId, SimpleDataType.Date, SimpleDataType.DateTime);
    }

    private List<RecipientFieldDescription> filterFields(List<RecipientFieldDescription> fields, Pattern namePattern, int companyId, SimpleDataType ... types) {
        List<RecipientFieldDescription> filteredFields = fields.stream()
                .filter(f -> namePattern.asMatchPredicate().test(f.getColumnName()))
                .toList();

        if (filteredFields.isEmpty()) {
            return Collections.emptyList();
        }

        Set<SimpleDataType> expectedTypes = EnumSet.copyOf(Arrays.asList(types));

        List<String> fieldsWithInvalidType = filteredFields.stream()
                .filter(f -> !expectedTypes.contains(f.getSimpleDataType()))
                .map(RecipientFieldDescription::getColumnName)
                .toList();

        if (!fieldsWithInvalidType.isEmpty()) {
            throw new JobWorkerConfigurationException(
                    "Company %d: fields %s have invalid data type. Expected one of %s.".formatted(
                            companyId,
                            fieldsWithInvalidType,
                            Arrays.toString(types)
                    )
            );
        }

        return filteredFields;
    }

    private Set<String> getColumnNames(List<RecipientFieldDescription> fields) {
        return fields.stream()
                .map(RecipientFieldDescription::getColumnName)
                .collect(Collectors.toSet());
    }

    private List<Recipient> getRecipients(int lastId, int companyId, Set<String> columnNames) {
        Set<String> columns = new HashSet<>(columnNames);
        columns.add(RecipientStandardField.CustomerID.getColumnName());

        return serviceLookupFactory.getRecipientService().getRecipients(STEP_SIZE, lastId, companyId, columns);
    }

    private Map<String, Optional<String>> extractTopCountFieldNames(
            List<RecipientFieldDescription> numericFields,
            Recipient recipient
    ) {
        return getFieldResults(
                getTopNumericFields(numericFields, recipient),
                countFieldsPattern,
                resultCountFieldNames
        );
    }

    private Map<String, Optional<String>> extractTopDateFieldNames(
            List<RecipientFieldDescription> dateFields,
            Recipient recipient
    ) {
        return getFieldResults(
                getTopDateFields(dateFields, recipient),
                dateFieldsPattern,
                resultDateFieldNames
        );
    }

    private <T> Map<String, Optional<String>> getFieldResults(
            SequencedMap<String, T> topResults,
            Pattern fieldNamePattern,
            List<String> resultFieldNames
    ) {
        Iterator<Map.Entry<String, T>> iterator = topResults.entrySet().iterator();

        return resultFieldNames.stream()
                .collect(Collectors.toMap(Function.identity(), fn -> {
                    if (iterator.hasNext()) {
                        Matcher matcher = fieldNamePattern.matcher(iterator.next().getKey());
                        if (matcher.matches()) {
                            return Optional.ofNullable(matcher.group(1));
                        }
                    }

                    return Optional.empty();
                }));
    }

    private SequencedMap<String, Number> getTopNumericFields(List<RecipientFieldDescription> fields, Recipient recipient) {
        return getTopFieldsByValue(
                fields,
                f -> isValidNumericField(recipient, f.getColumnName()),
                f -> AgnUtils.parseNumber(recipient.getCustParameters().get(f.getColumnName()).toString()),
                Comparator.comparingDouble(Number::doubleValue)
        );
    }

    private SequencedMap<String, Date> getTopDateFields(List<RecipientFieldDescription> fields, Recipient recipient) {
        return getTopFieldsByValue(
                fields,
                f -> isValidDateField(recipient, f.getColumnName()),
                f -> getDateValue(recipient, f.getColumnName()),
                Comparator.comparingLong(Date::getTime)
        );
    }

    private <T> SequencedMap<String, T> getTopFieldsByValue(
            List<RecipientFieldDescription> fields,
            Predicate<RecipientFieldDescription> predicate,
            Function<RecipientFieldDescription, T> valueExtractor,
            Comparator<T> comparator
    ) {
        return fields.stream()
                .filter(predicate)
                .map(f -> Map.entry(f.getColumnName(), valueExtractor.apply(f)))
                .sorted(Map.Entry.comparingByValue(comparator.reversed()))
                .limit(TOP_RESULTS_LIMIT)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private boolean isValidNumericField(Recipient recipient, String columnName) {
        Object value = recipient.getCustParameters().get(columnName);
        return value != null && (value instanceof Number || AgnUtils.isDouble(value.toString()));
    }

    private boolean isValidDateField(Recipient recipient, String columnName) {
        Object value = recipient.getCustParameters().get(columnName);
        if (value == null) {
            return false;
        }

        if (value instanceof Date) {
            return true;
        }

        try {
            recipient.getDateFormat().parse(value.toString());
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private Date getDateValue(Recipient recipient, String columnName) {
        Object value = recipient.getCustParameters().get(columnName);
        if (value instanceof Date date) {
            return date;
        }

        try {
            return recipient.getDateFormat().parse(value.toString());
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

}
