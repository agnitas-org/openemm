/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.agnitas.util.CaseInsensitiveSet;

/**
 * Standard Customer-Table fields
 * 
 * Boolean parameters of enums:
 *   1st boolean: readOnly
 *   2nd boolean: hidden
 *   3rd boolean: historize
 */
public enum RecipientStandardField {
	CustomerID("customer_id", "Customer_ID", true, false, false),
		
	/**
	 * Email is historized by default
	 */
	Email("email", "mailing.MediaType.0", false, false, true),
		
	Title("title", "recipient.Title", false, false, false),

	/**
	 * Firstname is historized by default
	 */
	Firstname("firstname", "recipient.Firstname", false, false, true),

	/**
	 * Lastname is historized by default
	 */
	Lastname("lastname", "recipient.Lastname", false, false, true),

	/**
	 * Gender is historized by default
	 */
	Gender("gender", "recipient.Salutation", false, false, true),
		
	/**
	 * Mailtype is historized by default
	 */
	Mailtype("mailtype", "Mailtype", false, false, true),
		
	LastOpenDate("lastopen_date", null, true, false, false),
	LastClickDate("lastclick_date", null, true, false, false),
	LastSendDate("lastsend_date", null, true, false, false),
		
	DatasourceID("datasource_id", "recipient.DatasourceId", true, false, false),
	LatestDatasourceID("latest_datasource_id", null, true, false, false),

	/**
	 * DoNotTrack(sys_tracking_veto) is historized by default
	 */
	DoNotTrack("sys_tracking_veto", "recipient.trackingVeto", false, false, true),
		
	EncryptedSending("sys_encrypted_sending", "recipient.encryptedSending", false, false, false),
		
	ChangeDate("timestamp", null, true, false, false),
	CreationDate("creation_date", null, true, false, false),
	CleanedDate("cleaned_date", null, true, false, false),
	
	/**
	 * DB field for AGNEMM-1817, AGNEMM-1924 and AGNEMM-1925
	 * This is by now the only hidden field in EMM
	 */
	Bounceload("bounceload", null, true, true, false);
		
	private List<String> allRecipientStandardFieldColumnNames = new ArrayList<>();

	public static Set<String> getAllRecipientStandardFieldColumnNames() {
		Set<String> allRecipientStandardFieldColumnNames = new CaseInsensitiveSet();
		for (RecipientStandardField recipientStandardField : RecipientStandardField.values()) {
			allRecipientStandardFieldColumnNames.add(recipientStandardField.getColumnName());
		}
		return allRecipientStandardFieldColumnNames;
	}
		
	public static Set<String> getReadOnlyRecipientStandardFieldColumnNames() {
		Set<String> readOnlyRecipientStandardFieldColumnNames = new CaseInsensitiveSet();
		for (RecipientStandardField recipientStandardField : RecipientStandardField.values()) {
			if (recipientStandardField.isReadOnly()) {
				readOnlyRecipientStandardFieldColumnNames.add(recipientStandardField.getColumnName());
			}
		}
		return readOnlyRecipientStandardFieldColumnNames;
	}
		
	public static Set<String> getHiddenRecipientStandardFieldColumnNames() {
		Set<String> hiddenRecipientStandardFieldColumnNames = new CaseInsensitiveSet();
		for (RecipientStandardField recipientStandardField : RecipientStandardField.values()) {
			if (recipientStandardField.isHidden()) {
				hiddenRecipientStandardFieldColumnNames.add(recipientStandardField.getColumnName());
			}
		}
		return hiddenRecipientStandardFieldColumnNames;
	}
		
	public static Set<String> getHistorizedRecipientStandardFieldColumnNames() {
		Set<String> historizedRecipientStandardFieldColumnNames = new CaseInsensitiveSet();
		for (RecipientStandardField recipientStandardField : RecipientStandardField.values()) {
			if (recipientStandardField.isHistorize()) {
				historizedRecipientStandardFieldColumnNames.add(recipientStandardField.getColumnName());
			}
		}
		return historizedRecipientStandardFieldColumnNames;
	}
		
	public static Set<String> getBulkImmutableRecipientStandardFieldColumnNames() {
		Set<String> bulkImmutableRecipientStandardFieldColumnNames = new CaseInsensitiveSet();
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.CustomerID.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.Gender.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.Title.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.Firstname.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.Lastname.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.Email.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.DatasourceID.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.Bounceload.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.LastOpenDate.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.LastClickDate.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.LastSendDate.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.LatestDatasourceID.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.ChangeDate.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.CleanedDate.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.CreationDate.getColumnName());
		bulkImmutableRecipientStandardFieldColumnNames.add(RecipientStandardField.EncryptedSending.getColumnName());
		return bulkImmutableRecipientStandardFieldColumnNames;
	}
		
	public static Set<String> getDbCleanerImmutableRecipientStandardFieldColumnNames() {
		Set<String> dbCleanerRecipientStandardFieldColumnNames = new CaseInsensitiveSet();
		dbCleanerRecipientStandardFieldColumnNames.add(RecipientStandardField.CustomerID.getColumnName());
		dbCleanerRecipientStandardFieldColumnNames.add(RecipientStandardField.Email.getColumnName());
		dbCleanerRecipientStandardFieldColumnNames.add(RecipientStandardField.DatasourceID.getColumnName());
		dbCleanerRecipientStandardFieldColumnNames.add(RecipientStandardField.CreationDate.getColumnName());
		dbCleanerRecipientStandardFieldColumnNames.add(RecipientStandardField.Bounceload.getColumnName());
		dbCleanerRecipientStandardFieldColumnNames.add(RecipientStandardField.CleanedDate.getColumnName());
		dbCleanerRecipientStandardFieldColumnNames.add(RecipientStandardField.ChangeDate.getColumnName());
		dbCleanerRecipientStandardFieldColumnNames.add(RecipientStandardField.EncryptedSending.getColumnName());
		return dbCleanerRecipientStandardFieldColumnNames;
	}

	public static List<String> getImportChangeNotAllowedColumns(boolean mayImportCustomerID) {
		List<String> hiddenColumns = new ArrayList<>();
		hiddenColumns.add("change_date");
		hiddenColumns.add(RecipientStandardField.ChangeDate.getColumnName());
		hiddenColumns.add(RecipientStandardField.CreationDate.getColumnName());
		hiddenColumns.add(RecipientStandardField.DatasourceID.getColumnName());
		hiddenColumns.add(RecipientStandardField.Bounceload.getColumnName());
		hiddenColumns.add(RecipientStandardField.LatestDatasourceID.getColumnName());
		hiddenColumns.add(RecipientStandardField.LastOpenDate.getColumnName());
		hiddenColumns.add(RecipientStandardField.LastClickDate.getColumnName());
		hiddenColumns.add(RecipientStandardField.LastSendDate.getColumnName());
		hiddenColumns.add(RecipientStandardField.CleanedDate.getColumnName());
		hiddenColumns.add(RecipientStandardField.EncryptedSending.getColumnName());
		if (!mayImportCustomerID) {
			hiddenColumns.add(RecipientStandardField.CustomerID.getColumnName());
		}
		return Collections.unmodifiableList(hiddenColumns);
	}
		
	private String columnName;
	private String messageKey;
	private boolean readOnly;
	private boolean hidden;
	private boolean historize;
		
	private RecipientStandardField(String columnName, String messageKey, boolean readOnly, boolean hidden, boolean historize) {
		this.columnName = columnName.toLowerCase();
		this.messageKey = messageKey;
		this.readOnly = readOnly;
		this.hidden = hidden;
		this.historize = historize;
		if (allRecipientStandardFieldColumnNames.contains(this.columnName)) {
			throw new RuntimeException("Invalid column name for RecipientStandardField: " + this.columnName);
		} else {
			allRecipientStandardFieldColumnNames.add(this.columnName);
		}
	}
		
	public String getColumnName() {
		return columnName;
	}
		
	public String getMessageKey() {
		return messageKey;
	}
		
	public boolean isReadOnly() {
		return readOnly;
	}
		
	public boolean isHidden() {
		return hidden;
	}
		
	public boolean isHistorize() {
		return historize;
	}
}
