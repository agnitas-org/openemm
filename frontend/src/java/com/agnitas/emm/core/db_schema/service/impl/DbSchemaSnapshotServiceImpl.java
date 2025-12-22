/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.db_schema.service.impl;

import static com.agnitas.emm.core.service.RecipientStandardField.Bounceload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.db_schema.bean.DbColumnInfo;
import com.agnitas.emm.core.db_schema.bean.DbSchemaCheckResult;
import com.agnitas.emm.core.db_schema.bean.DbSchemaSnapshot;
import com.agnitas.emm.core.db_schema.bean.DbTableInfo;
import com.agnitas.emm.core.db_schema.dao.DbSchemaSnapshotDao;
import com.agnitas.emm.core.db_schema.exception.DbSchemaSnapshotMissingException;
import com.agnitas.emm.core.db_schema.exception.DbSchemaSnapshotReadException;
import com.agnitas.emm.core.db_schema.exception.DbSchemaSnapshotWriteException;
import com.agnitas.emm.core.db_schema.exception.InvalidDbSchemaSnapshotFormatException;
import com.agnitas.emm.core.db_schema.service.DbSchemaSnapshotService;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.Tuple;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DbSchemaSnapshotServiceImpl implements DbSchemaSnapshotService {

    private static final Logger logger = LogManager.getLogger(DbSchemaSnapshotServiceImpl.class);

    private static final List<Pattern> TABLES_TO_EXCLUDE = Stream.of(
                    "hst_customer_[\\d]+_tbl",
                    "mailtrack_process_.{2}_tbl",
                    "tmp_crt_[0-9]+_tbl",
                    "tmp_crt_[A-Z]_[0-9]+_[0-9]+_tbl",
                    "tmp_scratch_.*_[0-9]+_tbl",

                    "recvlimit_[0-9]+_tbl",
                    "ahv_[0-9]+_tbl",
                    "ahv_timestamp_tbl",
                    "ahvencrypt_[0-9]+_tbl",
                    "deliver_[0-9]+_tbl",
                    "mail_skip_tbl",
                    "mail_skip_checkpoint_tbl",
                    "mia_lastrun_tbl",
                    "omg_[0-9]+_tbl",
                    "omg_temp_.{2}_tbl",
                    "omg_timestamp_tbl",
                    "pegi_.{2}_control_tbl",
                    "priority_[0-9]+_tbl",
                    "priority_config_tbl",
                    "provider_delivery_tbl"
            )
            .map(tn -> Pattern.compile(tn, Pattern.CASE_INSENSITIVE))
            .toList();

    private final DbSchemaSnapshotDao snapshotDao;
    private final RecipientFieldService recipientFieldService;
    private final ConfigService configService;
    private final ObjectMapper objectMapper;

    public DbSchemaSnapshotServiceImpl(DbSchemaSnapshotDao snapshotDao, RecipientFieldService recipientFieldService, ConfigService configService) {
        this.snapshotDao = snapshotDao;
        this.recipientFieldService = recipientFieldService;
        this.configService = configService;
        this.objectMapper = new ObjectMapper();
    }

    private static class DbTable {
        final String name;
        final Map<String, DbColumnInfo> columns;

        private DbTable(String name, List<DbColumnInfo> columns) {
            this.name = name;
            this.columns = columns.stream()
                    .collect(Collectors.toMap(DbColumnInfo::getName, Function.identity()));
        }

        public Map<String, DbColumnInfo> getColumns() {
            return columns;
        }
    }

    @Override
    public boolean exists() {
        return snapshotDao.exists();
    }

    private DbSchemaSnapshot readSnapshot() {
        try {
            Tuple<String, String> data = snapshotDao.read();
            List<DbTableInfo> tables = objectMapper.readValue(data.getSecond(), new TypeReference<>() {
            });

            return new DbSchemaSnapshot(data.getFirst(), tables);
        } catch (JsonProcessingException e) {
            throw new InvalidDbSchemaSnapshotFormatException("Snapshot file contains invalid JSON", e);
        }
    }

    @Override
    public DbSchemaSnapshot read(MultipartFile file) {
        return readSnapshotFile(file);
    }

    @Override
    public DbSchemaSnapshot read(File file) {
        return readSnapshotFile(() -> new FileInputStream(file));
    }

    private DbSchemaSnapshot readSnapshotFile(InputStreamSource inputStreamSrc) {
        try (InputStream fileStream = inputStreamSrc.getInputStream()) {
            return objectMapper.readValue(
                    String.join("\n", IOUtils.readLines(fileStream, StandardCharsets.UTF_8)),
                    DbSchemaSnapshot.class
            );
        } catch (JsonParseException | JsonMappingException e) {
            throw new InvalidDbSchemaSnapshotFormatException("Snapshot file contains invalid JSON", e);
        } catch (Exception e) {
            throw new DbSchemaSnapshotReadException("Failed to read DB snapshot! " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String versionNumber) {
        return snapshotDao.exists(versionNumber);
    }

    @Override
    public String generateFileName() {
        return generateFileName(getApplicationVersion());
    }

    private String generateFileName(String version) {
        return "emm-%s-schema-%s.json".formatted(getVendorName(), version);
    }

    @Override
    public String getVendorName() {
        return snapshotDao.getVendorName();
    }

    @Override
    public File create() {
        DbSchemaSnapshot snapshot = createSnapshot();

        try {
            File snapshotFile = File.createTempFile(
                    generateFileName(snapshot.getVersionNumber()),
                    "",
                    AgnUtils.createDirectory(AgnUtils.getTempDir())
            );

            try (FileWriter fileWriter = new FileWriter(snapshotFile)) {
                fileWriter.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot));
            }

            return snapshotFile;
        } catch (IOException e) {
            logger.error("Error when create DB snapshot!", e);
            throw new DbSchemaSnapshotWriteException("Error creating DB schema snapshot file", e);
        }
    }

    @Override
    public void save(DbSchemaSnapshot snapshot) {
        try {
            String json = objectMapper.writeValueAsString(snapshot.getTables());
            snapshotDao.save(snapshot.getVersionNumber(), json);
        } catch (JsonProcessingException e) {
            throw new InvalidDbSchemaSnapshotFormatException("Snapshot file contains invalid JSON", e);
        }
    }

    private DbSchemaSnapshot createSnapshot() {
        String tableNamePattern = configService.getValue(ConfigValue.DB_Snapshot_TableNamingPattern);

        List<DbTableInfo> tables = snapshotDao.getTableNames()
                .stream()
                .filter(n -> n.matches(tableNamePattern) && !isTableExcluded(n))
                .map(n -> new DbTableInfo(n, snapshotDao.getTableColumns(n)))
                .toList();

        excludeClientSpecificFields(tables);

        return new DbSchemaSnapshot(getApplicationVersion(), tables);
    }

    private boolean isTableExcluded(String tableName) {
        return TABLES_TO_EXCLUDE.stream()
                .anyMatch(p -> p.matcher(tableName).matches());
    }

    private void excludeClientSpecificFields(List<DbTableInfo> tables) {
        Optional<DbTableInfo> customerTable = tables.stream()
                .filter(t -> "customer_1_tbl".equalsIgnoreCase(t.getName()))
                .findFirst();

        if (customerTable.isPresent()) {
            Set<String> standardFieldsNames = recipientFieldService.getStandardFieldsNames(1);

            customerTable.get().getColumns().removeIf(c ->
                    !standardFieldsNames.contains(c.getName()) || Bounceload.getColumnName().equals(c.getName())
            );
        }
    }

    @Override
    public DbSchemaCheckResult check() {
        if (!exists()) {
            throw new DbSchemaSnapshotMissingException("DB schema can't be checked: snapshot file is missing");
        }

        Map<String, DbTable> snapshotTables = readSnapshot().getTables()
                .stream()
                .collect(Collectors.toMap(DbTableInfo::getName, t -> new DbTable(t.getName(), t.getColumns())));

        Map<String, DbTable> liveTables = createSnapshot().getTables()
                .stream()
                .collect(Collectors.toMap(DbTableInfo::getName, t -> new DbTable(t.getName(), t.getColumns())));

        return new DbSchemaCheckResult(
                findMissingTables(liveTables, snapshotTables),
                findMissingTables(snapshotTables, liveTables),
                findMissingColumns(snapshotTables, liveTables),
                findColumnsWithMismatchedTypes(snapshotTables, liveTables),
                findColumnsWithMismatchedLength(snapshotTables, liveTables)
        );
    }

    @Override
    public File createDiffFile() {
        DbSchemaCheckResult result = check();
        try {
            return Files.writeString(
                    Files.createTempFile("db-schema-diff", ".json"),
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result)
            ).toFile();
        } catch (IOException e) {
            throw new DbSchemaSnapshotWriteException("Failed to write DB schema diff to file!", e);
        }
    }

    private List<String> findMissingTables(Map<String, DbTable> expectedTables, Map<String, DbTable> tables) {
        return expectedTables.keySet()
                .stream()
                .filter(tn -> !tables.containsKey(tn))
                .toList();
    }

    private Map<String, List<String>> findMissingColumns(Map<String, DbTable> expectedTables, Map<String, DbTable> tables) {
        Map<String, List<String>> result = new HashMap<>();

        for (Map.Entry<String, DbTable> tableEntry : expectedTables.entrySet()) {
            DbTable actualTable = tables.get(tableEntry.getKey());

            if (actualTable != null) {
                List<String> missingColumns = findMissingColumnsInTable(tableEntry.getValue(), actualTable);
                if (!missingColumns.isEmpty()) {
                    result.put(tableEntry.getKey(), missingColumns);
                }
            }
        }

        return result;
    }

    private List<String> findMissingColumnsInTable(DbTable expectedTable, DbTable table) {
        return expectedTable.getColumns()
                .keySet()
                .stream()
                .filter(cn -> !table.getColumns().containsKey(cn))
                .toList();
    }

    private Map<String, List<String>> findColumnsWithMismatchedTypes(Map<String, DbTable> expectedTables, Map<String, DbTable> actualTables) {
        return findColumnsWithMismatches(
                expectedTables,
                actualTables,
                (c1, c2) -> !c1.getDataType().equalsIgnoreCase(c2.getDataType()),
                c -> "%s (%s)".formatted(c.getName(), c.getDataType().toUpperCase())
        );
    }

    private Map<String, List<String>> findColumnsWithMismatchedLength(Map<String, DbTable> expectedTables, Map<String, DbTable> actualTables) {
        return findColumnsWithMismatches(
                expectedTables,
                actualTables,
                (c1, c2) -> !Objects.equals(c1.getLength(), c2.getLength()),
                c -> "%s (%s %d)".formatted(c.getName(), c.getDataType().toUpperCase(), c.getLength())
        );
    }

    private Map<String, List<String>> findColumnsWithMismatches(
            Map<String, DbTable> expectedTables,
            Map<String, DbTable> actualTables,
            BiPredicate<DbColumnInfo, DbColumnInfo> predicate,
            Function<DbColumnInfo, String> formatFunction
    ) {
        return findColumnsWithMismatches(expectedTables, actualTables, predicate)
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().map(formatFunction).toList()
                ));
    }

    private Map<String, List<DbColumnInfo>> findColumnsWithMismatches(
            Map<String, DbTable> expectedTables,
            Map<String, DbTable> actualTables,
            BiPredicate<DbColumnInfo, DbColumnInfo> predicate
    ) {
        Map<String, List<DbColumnInfo>> result = new HashMap<>();

        for (Map.Entry<String, DbTable> tableEntry : expectedTables.entrySet()) {
            DbTable actualTable = actualTables.get(tableEntry.getKey());

            if (actualTable != null) {
                List<DbColumnInfo> foundColumns = findColumnsWithMismatches(tableEntry.getValue(), actualTable, predicate);
                if (!foundColumns.isEmpty()) {
                    result.put(tableEntry.getKey(), foundColumns);
                }
            }
        }

        return result;
    }

    private List<DbColumnInfo> findColumnsWithMismatches(
            DbTable expectedTable,
            DbTable actualTable,
            BiPredicate<DbColumnInfo, DbColumnInfo> predicate
    ) {
        List<DbColumnInfo> result = new ArrayList<>();

        for (Map.Entry<String, DbColumnInfo> columnEntry : expectedTable.getColumns().entrySet()) {
            DbColumnInfo actualColumn = actualTable.getColumns().get(columnEntry.getKey());

            if (actualColumn != null && predicate.test(columnEntry.getValue(), actualColumn)) {
                result.add(actualColumn);
            }
        }

        return result;
    }

    private String getApplicationVersion() {
        return configService.getValue(ConfigValue.ApplicationVersion);
    }

}
