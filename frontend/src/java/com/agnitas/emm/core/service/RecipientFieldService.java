/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface RecipientFieldService {
	/**
	 * Standard Customer-Table fields
	 */
	public enum RecipientStandardField {
		CustomerID("customer_id", "Customer_ID", true),
		
		Email("email", "mailing.MediaType.0", false),
		Title("title", "recipient.Title", false),
		Firstname("firstname", "recipient.Firstname", false),
		Lastname("lastname", "recipient.Lastname", false),
		Gender("gender", "recipient.Salutation", false),
		Mailtype("mailtype", "Mailtype", false),
		
		LastOpenDate("lastopen_date", null, true),
		LastClickDate("lastclick_date", null, true),
		LastSendDate("lastsend_date", null, true),
		
		DatasourceID("datasource_id", "recipient.DatasourceId", true),
		LatestDatasourceID("latest_datasource_id", null, true),
		
		DoNotTrack("sys_tracking_veto", "recipient.trackingVeto", false),
		EncryptedSending("sys_encrypted_sending", "recipient.encryptedSending", false),
		
		ChangeDate("timestamp", null, true),
		CreationDate("creation_date", null, true),
		CleanedDate("cleaned_date", null, true),
	
		/**
		 * DB field for AGNEMM-1817, AGNEMM-1924 and AGNEMM-1925
		 */
		Bounceload("bounceload", null, true);
		
		private List<String> allRecipientStandardFieldColumnNames = new ArrayList<>();

		public static List<String> getAllRecipientStandardFieldColumnNames() {
			List<String> allRecipientStandardFieldColumnNames = new ArrayList<>();
			for (RecipientStandardField recipientStandardField : RecipientStandardField.values()) {
				allRecipientStandardFieldColumnNames.add(recipientStandardField.getColumnName());
			}
			return allRecipientStandardFieldColumnNames;
		}
		
		public static List<String> getReadOnlyRecipientStandardFieldColumnNames() {
			List<String> readOnlyRecipientStandardFieldColumnNames = new ArrayList<>();
			for (RecipientStandardField recipientStandardField : RecipientStandardField.values()) {
				if (recipientStandardField.isReadOnly()) {
					readOnlyRecipientStandardFieldColumnNames.add(recipientStandardField.getColumnName());
				}
			}
			return readOnlyRecipientStandardFieldColumnNames;
		}
		
		private String columnName;
		private String messageKey;
		private boolean readOnly;
		
		private RecipientStandardField(String columnName, String messageKey, boolean readOnly) {
			this.columnName = columnName.toLowerCase();
			this.messageKey = messageKey;
			this.readOnly = readOnly;
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
	}
	
	List<RecipientFieldDescription> getRecipientFields(int companyID) throws Exception;
	Map<String, String> getRecipientDBStructure(int companyID);
	RecipientFieldDescription getRecipientField(int companyID, String recipientFieldName) throws Exception;
	void saveRecipientField(int companyID, RecipientFieldDescription recipientFieldDescription) throws Exception;
	void deleteRecipientField(int companyID, String recipientFieldName) throws Exception;
	boolean isReservedKeyWord(String fieldname);
	void clearCachedData(int companyID);
}
