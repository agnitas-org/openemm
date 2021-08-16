/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.upload.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.upload.bean.DownloadData;
import com.agnitas.emm.core.upload.bean.UploadData;

public interface ComUploadDao {
	/**
	 * This method loads ONLY a few fields from the database. NOT fetched from the db is the
	 * payload (=BLOB). In other words, the file itself is not fetched by this method.
	 * The returned List contains a Map for each row. The key-value for each map are then the entries.
	 * 
	 * If possible, use {@link #getDownloadData(int)} and {@link #sendDataToStream(int, OutputStream)}.
	 * 
	 * @return
	 */
	List<UploadData> getOverviewList(ComAdmin admin);

	/**
        * This method loads ONLY a few fields from the database for given file extentions. NOT fetched from the db is the
        * payload (=BLOB). In other words, the file itself is not fetched by this method.
        * The returned List contains a Map for each row. The key-value for each map are then the entries.
        * 
        * If possible, use {@link #getDownloadData(int)} and {@link #sendDataToStream(int, OutputStream)}.
        * 
        * @param admin
        * @param extentions
        * @return
        */
	List<UploadData> getOverviewListByExtention(ComAdmin admin, List<String> extentions);
	
	/**
     * This method returns the file associated with the given uploadID.
     * This method is dangerous, it loads the file completely into memory!
     * If possible, use {@link #getDownloadData(int)} and {@link #sendDataToStream(int, OutputStream)}.
     * 
	 * @param uploadID
	 * @return
	 */
	@Deprecated
	UploadData loadData(int uploadID);
	
	/**
	 * This method saves or updates the given Upload Data Object and returns the upload-id.
	 * @return
	 * @throws Exception
	 */
	void saveData(UploadData uploadData, InputStream inStream) throws Exception;
	
	/**
	 * This method updates existing data.
	 * @param uploadData
	 */
	void updateData(UploadData uploadData);
	
	/**
	 * This method deletes the row with the given uploadID.
	 * @param uploadID
	 */
	void deleteData(int uploadID);
	
	boolean deleteByCompany(@VelocityCheck int companyID);
	
	/**
	 * This method returns true, if the given uploadID belongs to the given adminID
	 * or if the given adminID is the admin of the company for the uploadID.
	 * @param admin
	 * @param uploadID
	 * @return
	 */
	boolean isOwnerOrAdmin(ComAdmin admin, int uploadID);
	
	List<Map<String, Object>> getUploadIdsToDelete(int daysToHold, @VelocityCheck int companyID);

    /**
     * If possible, use {@link #getDownloadData(int)} and {@link #sendDataToStream(int, OutputStream)}.
     * @param name file name of upload
     * @param companyID ID of company
     * @return UploadData or NULL if nothing found
     */
    UploadData getDataByFileName(String name, @VelocityCheck int companyID);
    
    List<UploadData> getDataByCompanyID(@VelocityCheck int companyID);

    /**
     * Get the data for the given upload ID. This is a safer way than working
     * on {@link UploadData}, because {@link DownloadData} does not contain the
     * content of the file to download.
     * 
     * @param uploadId ID of upload
     * 
     * @return data of upload
     */
	DownloadData getDownloadData(int uploadId);
	
	/**
	 * Sends the content of the file of given upload to given stream.
	 * 
	 * @param uploadId ID of upload
	 * @param stream {@link OutputStream} for sending data
	 * 
	 * @throws Exception on errors during processing
	 */
	void sendDataToStream(int uploadId, OutputStream stream) throws Exception;

	PaginatedListImpl<UploadData> getPaginatedList(int companyId, int adminId, String sort, String direction, int pageNumber, int pageSize);

	boolean isOwnerOrAdmin(int id, int adminId, int companyId);

    boolean exists(int id);

	DownloadData getDownloadData(int companyID, String filename);

	long getCurrentUploadOverallSizeBytes(int companyID);
}
