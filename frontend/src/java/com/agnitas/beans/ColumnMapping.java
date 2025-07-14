/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

/**
 * Bean contains information about mapping of csv-file column to database column
 * (while importing recipients from csv-file)
 */
public interface ColumnMapping {
    /**
     * Constant for fileColumn field. Indicates that csv-column of that mapping
     * should not be imported into database
     */
    String DO_NOT_IMPORT = "do-not-import-column";

    /**
     * Getter for property id
     *
     * @return the property id for this ColumnMapping
     */
    int getId();

    /**
     * Setter for property id
     *
     * @param id new value for id property
     */
    void setId(int id);

    /**
     * Getter for property profileId (the owner-profile of this mapping)
     *
     * @return the property profileId for this ColumnMapping
     */
    int getProfileId();

    /**
     * Setter for property profileId
     *
     * @param profileId new value for profileId property
     */
    void setProfileId(int profileId);

    /**
     * Getter for property fileColumn (column in csv-file)
     *
     * @return the property fileColumn for this ColumnMapping
     */
    String getFileColumn();

    /**
     * Setter for property fileColumn
     *
     * @param fileColumn new value for fileColumn property
     */
    void setFileColumn(String fileColumn);

    /**
     * Getter for property databaseColumn (column in database)
     *
     * @return the property databaseColumn for this ColumnMapping
     */
    String getDatabaseColumn();

    /**
     * Setter for property databaseColumn
     *
     * @param databaseColumn new value for databaseColumn property
     */
    void setDatabaseColumn(String databaseColumn);

    /**
     * Getter for property mandatory (indicates if the column is required)
     *
     * @return the property mandatory for this ColumnMapping
     */
    boolean isMandatory();

    /**
     * Setter for property mandatory
     *
     * @param mandatory new value for mandatory property
     */
    void setMandatory(boolean mandatory);

    /**
     * Getter for property encrypted
     *
     * @return encrypted property for this ColumnMapping
     */
    boolean isEncrypted();

    /**
     * Setter for property encrypted
     *
     * @param encrypted new value for encrypted property
     */
    void setEncrypted(boolean encrypted);

    /**
     * Getter for property defaultValue
     *
     * @return defaultValue property for this ColumnMapping
     */
    String getDefaultValue();

    /**
     * Setter for property defaultValue
     *
     * @param defaultValue new value for defaultValue property
     */
    void setDefaultValue(String defaultValue);

	void setFormat(String string);
	
	String getFormat();

	void setKeyColumn(boolean keyColumn);
	
	boolean isKeyColumn();
}
