/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.objectusage.service;

import com.agnitas.emm.core.objectusage.common.ObjectUsages;

/**
 * Service interface to detect EMM objects using other EMM objects.
 */
public interface ObjectUsageService {

	/**
	 * Lists all objects of company referencing given auto import.
	 * 
	 * @param companyID company ID
	 * @param autoImportID ID of auto import
	 * 
	 * @return list of objects referencing given auto import
	 */
	public ObjectUsages listUsageOfAutoImport(final int companyID, final int autoImportID);
	
	/**
	 * Lists all objects of company referencing given mailing.
	 * 
	 * @param companyID company ID
	 * @param mailingID ID of mailing
	 * 
	 * @return list of objects referencing given mailing
	 */
	public ObjectUsages listUsageOfMailing(final int companyID, final int mailingID);
	
	/**
	 * Lists all objects of company referencing profile fields by given visible name.
	 * 
	 * @param companyID company ID
	 * @param visibleName visible name of profile field
	 * 
	 * @return list of objects referencing given profile field
	 */
	public ObjectUsages listUsageOfProfileFieldByVisibleName(final int companyID, final String visibleName);
	
	/**
	 * Lists all objects of company referencing profile fields by given database name.
	 * 
	 * @param companyID company ID
	 * @param databaseName database name of profile field
	 * 
	 * @return list of objects referencing given profile field
	 */
	public ObjectUsages listUsageOfProfileFieldByDatabaseName(final int companyID, final String databaseName);
	
	/**
	 * Lists all objects of company referencing given link.
	 * 
	 * @param companyID company ID
	 * @param linkID ID of link
	 * 
	 * @return list of objects referencing given link
	 */
	public ObjectUsages listUsageOfLink(final int companyID, final int linkID);
	
	/**
	 * Lists all objects of company referencing given reference table.
	 * 
	 * @param companyID company ID
	 * @param tableID ID of reference table
	 * 
	 * @return list of objects referencing given reference table
	 */
	public ObjectUsages listUsageOfReferenceTable(final int companyID, final int tableID);

	/**
	 * Lists all objects of company referencing given reference table column.
	 * 
	 * @param companyID company ID
	 * @param tableID ID of reference table
	 * @param columnName name of column
	 * 
	 * @return list of objects referencing given reference table column
	 */
	public ObjectUsages listUsageOfReferenceTableColumn(final int companyID, final int tableID, final String columnName);
}
