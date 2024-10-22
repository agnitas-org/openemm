/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.service;

import java.text.DateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.agnitas.service.GenericExportWorker;
import org.agnitas.service.RecipientDuplicateSqlOptions;
import org.agnitas.service.RecipientQueryBuilder;
import org.agnitas.util.SqlPreparedStatementManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.messages.I18nString;

public class DuplicatedRecipientsExportWorker extends GenericExportWorker {
    private static final transient Logger LOGGER = LogManager.getLogger(DuplicatedRecipientsExportWorker.class);

    private RecipientQueryBuilder recipientQueryBuilder;
    private Admin admin;
    private RecipientDuplicateSqlOptions sqlOptions;
    private List<String> selectedFields;
    private Map<String, String> fieldsNames;

    public DuplicatedRecipientsExportWorker(RecipientQueryBuilder recipientQueryBuilder, Admin admin, RecipientDuplicateSqlOptions sqlOptions, List<String> selectedFields, Map<String, String> fieldsNames) {
        this.admin = admin;
        this.recipientQueryBuilder = recipientQueryBuilder;
        this.sqlOptions = sqlOptions;
        this.selectedFields = selectedFields;
        this.fieldsNames = fieldsNames;
        
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Created export worker for duplicated recipients for admin with id " + admin.getAdminID());
        }
    }

    @Override
    public GenericExportWorker call() throws Exception {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Started fetching a list of duplicated recipients for admin with id " + admin.getAdminID());
        }
        
        csvFileHeaders = prepareHeaders(selectedFields, fieldsNames, admin.getLocale());
        
        SqlPreparedStatementManager sqlPreparedStatementManager = recipientQueryBuilder.getDuplicateAnalysisSQLStatement(admin, sqlOptions, selectedFields, false);
        selectStatement = sqlPreparedStatementManager.getPreparedSqlString();
        selectParameters = Arrays.asList(sqlPreparedStatementManager.getPreparedSqlParameters());

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Finished fetching a list of duplicated recipients for admin with id " + admin.getAdminID());
        }
        return super.call();
    }

    public static DuplicatedRecipientsExportWorker.Builder getBuilder(DataSource dataSource, RecipientQueryBuilder recipientQueryBuilder) {
        return new DuplicatedRecipientsExportWorker.Builder(dataSource, recipientQueryBuilder);
    }

    private static List<String> prepareHeaders(List<String> selectedColumns, Map<String, String> fieldsNames, Locale locale){
        final List<String> headers = new ArrayList<>();
        for(String columnKey : selectedColumns) {
			if (columnKey.equals(RecipientStandardField.Email.getColumnName())) {
				headers.add(I18nString.getLocaleString("mailing.MediaType.0", locale));
			} else if (columnKey.equals(RecipientStandardField.ChangeDate.getColumnName())) {
				headers.add(I18nString.getLocaleString("recipient.Timestamp", locale));
			} else if (columnKey.equals(RecipientStandardField.Gender.getColumnName())) {
				headers.add(I18nString.getLocaleString("recipient.Salutation", locale));
			} else if (columnKey.equals(RecipientStandardField.Firstname.getColumnName())) {
				headers.add(I18nString.getLocaleString("Firstname", locale));
			} else if (columnKey.equals(RecipientStandardField.Lastname.getColumnName())) {
				headers.add(I18nString.getLocaleString("Lastname", locale));
			} else if (columnKey.equals(RecipientStandardField.CreationDate.getColumnName())) {
				headers.add(I18nString.getLocaleString("default.creationDate", locale));
			} else {
				headers.add(fieldsNames.get(columnKey));
			}
        }
        return headers;
    }

    public static class Builder {
        private String exportFile;
        private Admin admin;
        private DataSource dataSource;
        private RecipientQueryBuilder recipientQueryBuilder;
        private DateFormat dateFormat;
        private DateFormat dateTimeFormat;
        private ZoneId exportTimezone;
        private List<String> selectedColumns;
        private RecipientDuplicateSqlOptions sqlOptions;
        private Map<String, String> fieldsNames;

        public Builder(DataSource dataSource, RecipientQueryBuilder recipientQueryBuilder) {
        	this.dataSource = Objects.requireNonNull(dataSource);
        	this.recipientQueryBuilder = Objects.requireNonNull(recipientQueryBuilder);
        }
    
        public DuplicatedRecipientsExportWorker.Builder setAdmin(Admin admin) {
            this.admin = admin;
            return this;
        }
    
        public DuplicatedRecipientsExportWorker.Builder setSelectedColumns(List<String> selectedColumns) {
            this.selectedColumns = selectedColumns;
            return this;
        }
    
        public DuplicatedRecipientsExportWorker.Builder setSqlOptions(RecipientDuplicateSqlOptions sqlOptions) {
            this.sqlOptions = sqlOptions;
            return this;
        }
    
        public DuplicatedRecipientsExportWorker.Builder setFieldsNames(Map<String, String> fieldsNames) {
            this.fieldsNames = fieldsNames;
            return this;
        }
    
        public DuplicatedRecipientsExportWorker.Builder setExportFile(String exportFile) {
            this.exportFile = exportFile;
            return this;
        }

        public DuplicatedRecipientsExportWorker.Builder setDateFormat(DateFormat dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        public DuplicatedRecipientsExportWorker.Builder setDateTimeFormat(DateFormat dateTimeFormat) {
            this.dateTimeFormat = dateTimeFormat;
            return this;
        }

		public DuplicatedRecipientsExportWorker.Builder setExportTimezone(ZoneId exportTimezone) {
            this.exportTimezone = exportTimezone;
            return this;
		}

        public DuplicatedRecipientsExportWorker build() {
            DuplicatedRecipientsExportWorker worker = new DuplicatedRecipientsExportWorker(recipientQueryBuilder, admin, sqlOptions, selectedColumns, fieldsNames);
            worker.setDataSource(dataSource);
            worker.setExportFile(exportFile);
            worker.setDateFormat(dateFormat);
            worker.setDateTimeFormat(dateTimeFormat);
            worker.setExportTimezone(exportTimezone);
            return worker;
        }
    }
}
