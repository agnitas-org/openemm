/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.agnitas.util.MapUtils;
import org.agnitas.beans.ProfileField;
import org.agnitas.emm.core.recipient.dto.RecipientLightDto;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

public class RecipientUtils {
    
    private static final Logger logger = Logger.getLogger(RecipientUtils.class);
    
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    
    public static final String COLUMN_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_FIRSTNAME = "firstname";
    public static final String COLUMN_LASTNAME = "lastname";
    public static final String COLUMN_EMAIL = "email";
    
    public static final String COLUMN_GENDER = "gender";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_MAILTYPE = "mailtype";
    public static final String COLUMN_LATEST_DATASOURCE_ID = "latest_datasource_id";
    public static final String COLUMN_FREQUENCY_COUNT_DAY = "freq_count_day";
    public static final String COLUMN_FREQUENCY_COUNTER_WEEK = "freq_count_week";
    public static final String COLUMN_FREQUENCY_COUNT_MONTH = "freq_count_month";

    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_CREATION_DATE = "creation_date";
    public static final String COLUMN_LASTOPEN_DATE = "lastopen_date";
    public static final String COLUMN_LASTCLICK_DATE = "lastclick_date";
    public static final String COLUMN_LASTSEND_DATE = "lastsend_date";
    
    public static int COLUMN_FIRST_ORDER = 1;
    public static int COLUMN_SECOND_ORDER = 2;
    public static int COLUMN_THIRD_ORDER = 3;
    public static int COLUMN_OTHER_ORDER = 4;
    
    public static final int MAX_SELECTED_FIELDS_COUNT = 8;

    
    public static String getRecipientDescription(Map<String, Object> data) {
        final int customerId = NumberUtils.toInt((String) data.get(COLUMN_CUSTOMER_ID), 0);
        final String firstName = (String) data.get(COLUMN_FIRSTNAME);
        final String lastName = (String) data.get(COLUMN_LASTNAME);
        final String email = (String) data.get(COLUMN_EMAIL);
        return RecipientUtils.getRecipientDescription(customerId, firstName, lastName, email);
    }
    
    public static String getRecipientDescription(RecipientLightDto recipientDto) {
        return getRecipientDescription(recipientDto.getCustomerId(), recipientDto.getFirstname(), recipientDto.getLastname(), recipientDto.getEmail());
    }
    
    public static String getRecipientDescription(int id, String firstName, String lastName, String email) {
		final boolean hasFirstName = StringUtils.isNotBlank(firstName);
		final boolean hasLastName = StringUtils.isNotBlank(lastName);
		if (hasFirstName || hasLastName) {
			if (hasFirstName && hasLastName) {
				return firstName + " " + lastName + " (" + id + ")";
			} else {
				if (hasFirstName) {
					return firstName + " (" + id + ")";
				} else {
					return lastName + " (" + id + ")";
				}
			}
		} else {
			return email + " (" + id + ")";
		}
	}
 
	/**
     *  If string length is more than 500 characters cut it and add "..." in the end.
     *
     * @param string recipient type letter
     * @return cut string
     */
    public static String cutRecipientDescription(String string){
        try {
            return StringUtils.abbreviate(string, MAX_DESCRIPTION_LENGTH);
        } catch (IllegalArgumentException e) {
            logger.error("RecipientUtils.cutRecipientDescription: " + e, e);
            return string;
        }
    }
    
    public static Comparator<Map.Entry<String, String>> getFieldOrderComparator() {
        return getFieldOrderComparator(false);
    }
    
    public static Comparator<Map.Entry<String, String>> getFieldOrderComparator(boolean isDuplicateList) {
        return (o1, o2) -> {
            //Some columns should always be first
            int firstOrder = columnOrder(o1.getKey(), isDuplicateList);
            int secondOrder = columnOrder(o2.getKey(), isDuplicateList);
    
            if (firstOrder == COLUMN_OTHER_ORDER && secondOrder == COLUMN_OTHER_ORDER) {
                return o1.getValue().compareToIgnoreCase(o2.getValue());
            }
    
            return firstOrder - secondOrder;
        };
    }
    
    public static Comparator<String> getCsvColumnComparator(boolean duplicateList) {
        return (o1, o2) -> {
            //Some columns should always be first
            int firstOrder = columnOrder(o1, duplicateList);
            int secondOrder = columnOrder(o2, duplicateList);
    
            if (firstOrder == COLUMN_OTHER_ORDER && secondOrder == COLUMN_OTHER_ORDER) {
                return o1.compareToIgnoreCase(o2);
            }
    
            return firstOrder - secondOrder;
        };
    }
    
	public static int columnOrder(String columnName, boolean isDuplicateList){
        if (isDuplicateList) {
            if (COLUMN_CUSTOMER_ID.equals(columnName)) {
                return COLUMN_FIRST_ORDER;
            }
            if (COLUMN_EMAIL.equals(columnName)) {
                return COLUMN_SECOND_ORDER;
            }
            if (COLUMN_TIMESTAMP.equals(columnName)) {
                return COLUMN_THIRD_ORDER;
            }
        } else {
            if(COLUMN_GENDER.equals(columnName)){
                return COLUMN_FIRST_ORDER;
            }
            if(COLUMN_FIRSTNAME.equals(columnName)){
                return COLUMN_SECOND_ORDER;
            }
            if(COLUMN_LASTNAME.equals(columnName)){
                return COLUMN_THIRD_ORDER;
            }
        }
        
        return COLUMN_OTHER_ORDER;
    }
    
    public static Map<String, String> sortDuplicateAnalysisFields(CaseInsensitiveMap<String, ProfileField> columnMap) {
		final LinkedHashMap<String, String> fieldsMap = new LinkedHashMap<>();
		
		// we need predefined order for default columns: gender, firstname, lastname.
		fieldsMap.put(COLUMN_GENDER, columnMap.get(COLUMN_GENDER).getShortname());
		fieldsMap.put(COLUMN_FIRSTNAME, columnMap.get(COLUMN_FIRSTNAME).getShortname());
		fieldsMap.put(COLUMN_LASTNAME, columnMap.get(COLUMN_LASTNAME).getShortname());
		fieldsMap.put(COLUMN_TIMESTAMP, columnMap.get(COLUMN_TIMESTAMP).getShortname());
		fieldsMap.put(COLUMN_CUSTOMER_ID, columnMap.get(COLUMN_CUSTOMER_ID).getShortname());
		
		columnMap.remove(COLUMN_GENDER);
		columnMap.remove(COLUMN_FIRSTNAME);
		columnMap.remove(COLUMN_LASTNAME);
		columnMap.remove(COLUMN_TIMESTAMP);
		columnMap.remove(COLUMN_CUSTOMER_ID);
		
		columnMap.forEach((key, value) -> fieldsMap.put(key, value.getShortname()));
		
		MapUtils.reorderLinkedHashMap(fieldsMap, RecipientUtils.getFieldOrderComparator(true));
		
		return fieldsMap;
    }
}
