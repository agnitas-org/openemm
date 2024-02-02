/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.upload.dao.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.util.AgnUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.DefaultLobHandler;

import com.agnitas.beans.Admin;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.upload.bean.DownloadData;
import com.agnitas.emm.core.upload.bean.UploadData;
import com.agnitas.emm.core.upload.dao.ComUploadDao;
import org.springframework.transaction.annotation.Transactional;

public class ComUploadDaoImpl extends PaginatedBaseDaoImpl implements ComUploadDao {
	
	private static final Logger logger = LogManager.getLogger(ComUploadDaoImpl.class);
	
	// should be inserted by spring...
	protected DefaultLobHandler lobhandler = new DefaultLobHandler();
	
	// Rowmapper for the list view....
	protected class UploadDataRowMapper implements RowMapper<UploadData> {
		@Override
		public UploadData mapRow(ResultSet resultSet, int row) throws SQLException {
			UploadData uploadData = new UploadData();
			uploadData.setAdminID(resultSet.getInt("admin_id"));
			uploadData.setCreation_date(resultSet.getTimestamp("creation_date"));
			uploadData.setUploadID(resultSet.getInt("upload_id"));
			uploadData.setFilename(resultSet.getString("filename"));
			uploadData.setFilesize(resultSet.getInt("filesize"));
			return uploadData;
		}
	}

	// Rowmapper for the detail-view.
	protected class DownloadDataRowMapper implements RowMapper<UploadData> {
		@Override
		public UploadData mapRow(ResultSet resultSet, int i) throws SQLException {
			UploadData uploadData = new UploadData();
			byte[] blobBytes = lobhandler.getBlobAsBytes(resultSet, "payload");
			uploadData.setData(blobBytes);
			uploadData.setAdminID(resultSet.getInt("admin_id"));
			uploadData.setCreation_date(resultSet.getTimestamp("creation_date"));
			uploadData.setUploadID(resultSet.getInt("upload_id"));
			uploadData.setFilename(resultSet.getString("filename"));
			uploadData.setFilesize(resultSet.getInt("filesize"));
			uploadData.setContactFirstname(resultSet.getString("contact_firstname"));
			uploadData.setContactName(resultSet.getString("contact_name"));
			uploadData.setContactMail(resultSet.getString("contact_mail"));
			uploadData.setContactPhone(resultSet.getString("contact_phone"));
			uploadData.setSendtoMail(resultSet.getString("sendto_mail"));
			uploadData.setUploadID(resultSet.getInt("upload_id"));
			return uploadData;
		}
	}

	protected class NewDownloadDataRowMapper implements RowMapper<DownloadData> {
		@Override
		public DownloadData mapRow(ResultSet resultSet, int i) throws SQLException {
			DownloadData data = new DownloadData();

			data.setAdminID(resultSet.getInt("admin_id"));
			data.setCreation_date(resultSet.getTimestamp("creation_date"));
			data.setUploadID(resultSet.getInt("upload_id"));
			data.setFilename(resultSet.getString("filename"));
			data.setFilesize(resultSet.getInt("filesize"));
			data.setContactFirstname(resultSet.getString("contact_firstname"));
			data.setContactName(resultSet.getString("contact_name"));
			data.setContactMail(resultSet.getString("contact_mail"));
			data.setContactPhone(resultSet.getString("contact_phone"));
			data.setSendtoMail(resultSet.getString("sendto_mail"));
			data.setUploadID(resultSet.getInt("upload_id"));
			data.setOwners(getUploadOwners(data.getUploadID()));

			return data;
		}
	}


	// returns a list of admins ids that can access the uploaded file
    private List<Integer> getUploadOwners(int uploadId) {
        return select(logger, "SELECT admin_id FROM admin_upload_list_tbl WHERE upload_id = ?",
                IntegerRowMapper.INSTANCE, uploadId);
    }

	/**
	 * This method loads ONLY a few fields from the database for given file
	 * extentions. NOT fetched from the db is the payload (=BLOB). In other
	 * words, the file itself is not fetched by this method. The returned List
	 * contains a Map for each row. The key-value for each map are then the
	 * entries.
	 * 
	 * @param admin
	 * @param extentions
	 * @return
	 */
	@Override
	public List<UploadData> getOverviewListByExtention(Admin admin, List<String> extentions) {
		String sqlExtentions = "";
		String sql = "SELECT upload_id, admin_id, creation_date, filename, filesize FROM upload_tbl WHERE";
		String sqlOrder = " ORDER BY upload_id ";
		
		if (extentions != null && extentions.size() > 0) {
			sqlExtentions = " and (filename like '%." + extentions.get(0) + "'";
			for (int i = 1; i < extentions.size(); i++) {
				sqlExtentions += " or filename like '%." + extentions.get(i) + "'";
			}
			sqlExtentions += ")";
		} else {
			return new ArrayList<>();
		}
		
		if (admin.getCompanyID() > 0) {
			sql += " (company_id = ?)" + sqlExtentions + sqlOrder;
			return select(logger, sql, new UploadDataRowMapper(), admin.getCompanyID());
		} else {
			// get normal user-list.
			sql += " (admin_id = ? or from_admin_id = ?)" + sqlExtentions + sqlOrder;
			return select(logger, sql, new UploadDataRowMapper(), admin.getAdminID(), admin.getAdminID());
		}
	}

	/**
	 * This method loads ONLY a few fields from the database. NOT fetched from
	 * the db is the payload (=BLOB). In other words, the file itself is not
	 * fetched by this method. The returned List contains a Map for each row.
	 * The key-value for each map are then the entries.
	 * 
	 * @param admin
	 * @return
	 */
	@Override
	public List<UploadData> getOverviewList(Admin admin) {
		String sql = "SELECT upload_id, admin_id, creation_date, filename, filesize FROM upload_tbl WHERE ";
		String sqlOrder = " ORDER BY upload_id ";
		
		if (admin.getCompanyID() > 0) {
			sql += " company_id = ?" + sqlOrder;
			return select(logger, sql, new UploadDataRowMapper(), admin.getCompanyID());
		} else {
			// get normal user-list.
			sql += " admin_id = ? or from_admin_id = ?" + sqlOrder;
			return select(logger, sql, new UploadDataRowMapper(), admin.getAdminID(), admin.getAdminID());
		}
	}

	@Override
	public PaginatedListImpl<UploadData> getPaginatedList(int companyId, int adminId, String sort, String direction, int pageNumber, int pageSize) {
		PaginatedListImpl<UploadData> paginatedList;

		if (StringUtils.isBlank(sort)) {
			sort = "filename";
		}

		boolean sortAscending = AgnUtils.sortingDirectionToBoolean(direction, true);

		StringBuilder sqlBuilder = new StringBuilder("select upload_id, admin_id, creation_date, filename, filesize from upload_tbl where ");

		if (companyId > 0) {
			sqlBuilder.append("company_id = ?");
			paginatedList = selectPaginatedList(logger, sqlBuilder.toString(), "upload_tbl", sort, sortAscending,
					pageNumber, pageSize, new UploadDataRowMapper(), companyId);
		} else {
			sqlBuilder.append("admin_id = ? or from_admin_id = ?");
			paginatedList = selectPaginatedList(logger, sqlBuilder.toString(), "upload_tbl", sort, sortAscending,
					pageNumber, pageSize, new UploadDataRowMapper(), adminId, adminId);
		}
		
		paginatedList.setSortCriterion(sort);

		return paginatedList;
	}

	/**
	 * This method checks, if the given id belongs to the given adminID,
	 * either by uploading it, or if the adminID is from the upload-admin of
	 * this company.
	 *
	 * @param id id of upload file
	 * @param adminId id of current user
	 * @param companyId id of current company
	 */
	@Override
	public boolean isOwnerOrAdmin(int id, int adminId, int companyId) {
		int returnAdminID = 0;
		int returnFromAdminID = 0;
		int returnCompanyID = 0;
		String sql = "SELECT admin_ID, from_admin_id, company_id FROM upload_tbl WHERE upload_id = ?";
		try {
			List<Map<String, Object>> result = select(logger, sql, id);
			returnAdminID = ((Number) result.get(0).get("admin_id")).intValue();
			returnFromAdminID = ((Number) result.get(0).get("from_admin_id")).intValue();
			returnCompanyID = ((Number) result.get(0).get("company_id")).intValue();
		} catch (Exception e) {
			logger.error("No Admin-ID found for corresponding uploadID");
			logger.error("Admin-ID: " + adminId);
			logger.error("Upload-ID: " + id);
		}

		if (returnAdminID > 0 && adminId == returnAdminID) {
			return true; // found owner
		} else if (returnFromAdminID > 0 && adminId == returnFromAdminID) {
			return true; // found owner
		} else if (adminId > 0 && companyId == returnCompanyID) {
			return true; // found admin for this company
		} else {
			return false;
		}
	}

	@Override
	public boolean exists(int id) {
		return selectInt(logger, "SELECT COUNT(*) FROM upload_tbl WHERE upload_id = ?", id) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void saveData(final UploadData uploadData, final InputStream inStream) throws Exception {
		if (isOracleDB()) {
			int uploadID = selectInt(logger, "SELECT upload_tbl_seq.NEXTVAL FROM DUAL");
			
			String insertSql = "INSERT INTO upload_tbl (upload_id, admin_id, creation_date, change_date, contact_name, contact_firstname, contact_phone, contact_mail, sendto_mail, description, filename, filesize, company_id, from_admin_id)"
				+ " VALUES (" + AgnUtils.repeatString("?", 14, ", ") + ")";
			
			update(logger, insertSql,
				uploadID,
				uploadData.getAdminID(),
				new Date(),
				new Date(),
				uploadData.getContact_name(),
				uploadData.getContactFirstname(),
				uploadData.getContactPhone(),
				uploadData.getContactMail(),
				uploadData.getSendtoMail(),
				uploadData.getDescription(),
				getUniqueFilename(uploadData.getFilename(), uploadData.getCompanyID()),
				uploadData.getFilesize(),
				uploadData.getCompanyID(),
				uploadData.getFromAdminID()
			);

			uploadData.setUploadID(uploadID);
		} else {
			String insertSql = "INSERT INTO upload_tbl (admin_id, creation_date, change_date, contact_name, contact_firstname, contact_phone, contact_mail, sendto_mail, description, filename, filesize, company_id, from_admin_id)"
				+ " VALUES (" + AgnUtils.repeatString("?", 13, ", ") + ")";
			
			int uploadID = insertIntoAutoincrementMysqlTable(logger, "upload_id", insertSql,
				uploadData.getAdminID(),
				new Date(),
				new Date(),
				uploadData.getContact_name(),
				uploadData.getContactFirstname(),
				uploadData.getContactPhone(),
				uploadData.getContactMail(),
				uploadData.getSendtoMail(),
				uploadData.getDescription(),
				getUniqueFilename(uploadData.getFilename(), uploadData.getCompanyID()),
				uploadData.getFilesize(),
				uploadData.getCompanyID(),
				uploadData.getFromAdminID()
			);

			uploadData.setUploadID(uploadID);
		}
		
		updateBlob(logger, "UPDATE upload_tbl SET payload = ? WHERE upload_id = ?", inStream, uploadData.getUploadID());
	}

	/**
	 * This method checks, if the given uploadID belongs to the given adminID,
	 * either by uploading it, or if the adminID is from the upload-admin of
	 * this company.
	 * 
	 * @param admin
	 * @param uploadID
	 * @return
	 */
	@Override
	public boolean isOwnerOrAdmin(Admin admin, int uploadID) {
		return isOwnerOrAdmin(uploadID,admin.getAdminID(),admin.getCompanyID());
	}

	@Override
    @Transactional
	@DaoUpdateReturnValueCheck
	public void deleteData(int uploadID) {
		update(logger, "DELETE FROM admin_upload_list_tbl WHERE upload_id = ?", uploadID);
		update(logger, "DELETE FROM upload_tbl WHERE upload_id = ?", uploadID);
	}
	
	@Override
    @Transactional
	public boolean deleteByCompany(int companyID) {
        update(logger, "DELETE FROM admin_upload_list_tbl WHERE admin_id IN (SELECT admin_id FROM admin_tbl WHERE company_id = ?)", companyID);
		int touchedLines = update(logger, "DELETE FROM upload_tbl WHERE company_id = ?", companyID);
		return touchedLines > 0;
	}

	@Override
	public List<Map<String, Object>> getUploadIdsToDelete(int daysToHold, int companyID) {
		if (isOracleDB()) {
			return select(logger, "SELECT upload_id FROM upload_tbl WHERE no_delete = 0 and company_id = ? and change_date < CURRENT_TIMESTAMP - ?", companyID, daysToHold);
		} else {
			return select(logger, "SELECT upload_id FROM upload_tbl WHERE no_delete = 0 and company_id = ? and change_date < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ? DAY)", companyID, daysToHold);
		}
	}
	
	@Override
	public List<UploadData> getDataByCompanyID(int companyID) {
		String sql = "SELECT payload, admin_id, creation_date, upload_id, filename, filesize, contact_firstname, contact_name,"
				+ " contact_mail, contact_phone, sendto_mail, upload_id FROM upload_tbl WHERE company_id = ?";
		List<UploadData> uploadDataList = select(logger, sql, new DownloadDataRowMapper(), companyID);
		return uploadDataList;
	}

	@Override
	public UploadData getDataByFileName(String name, int companyID) {
		String sql = "SELECT payload, admin_id, creation_date, upload_id, filename, filesize, contact_firstname, contact_name,"
			+ " contact_mail, contact_phone, sendto_mail, upload_id FROM upload_tbl WHERE filename LIKE ? AND company_id = ?";
		List<UploadData> uploadDataList = select(logger, sql, new DownloadDataRowMapper(), name, companyID);
		if (uploadDataList != null && uploadDataList.size() >= 1) {
			return uploadDataList.get(0);
		} else {
			return null;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void updateData(UploadData uploadData) {
		String sql = "UPDATE upload_tbl SET contact_name = ?, contact_firstname = ?, contact_phone = ?, contact_mail = ?, sendto_mail = ?, company_id = ?"
			+ ", change_date = ?, admin_id = ?, from_admin_id = ? WHERE upload_id = ?";
		update(logger, sql,
			uploadData.getContact_name(),
			uploadData.getContactFirstname(),
			uploadData.getContactPhone(),
			uploadData.getContactMail(),
			uploadData.getSendtoMail(),
			uploadData.getCompanyID(),
			new Date(),
			uploadData.getAdminID(),
			uploadData.getFromAdminID(),
			uploadData.getUploadID());
	}

	/**
	 * Create an unique storage filename if the given name already exists in database for this company
	 * 
	 * @param filename
	 * @param companyId
	 * @return
	 */
	protected String getUniqueFilename(String filename, int companyId) {
		String sql = "SELECT COUNT(upload_id) FROM upload_tbl WHERE company_id = ? AND filename = ?";
		int count = selectInt(logger, sql, companyId, filename);
		if (count > 0) {
			if (filename.contains(".")) {
				String fileNameWithoutExtension = filename.substring(0, filename.lastIndexOf("."));
				String fileExtension = filename.substring(filename.lastIndexOf("."), filename.length());
				return fileNameWithoutExtension + System.currentTimeMillis() + fileExtension;
			} else {
				return filename + System.currentTimeMillis();
			}
		} else {
			return filename;
		}
	}

	@Override
	public DownloadData getDownloadData(int uploadId) {
		List<DownloadData> data = select(
			logger,
			"SELECT admin_id, creation_date, upload_id, filename, filesize, contact_firstname, contact_name, contact_mail, contact_phone, sendto_mail, upload_id FROM upload_tbl WHERE upload_id = ?",
			new NewDownloadDataRowMapper(),
			uploadId);

		return data.get(0);
	}

	@Override
	public DownloadData getDownloadData(int companyID, String filename) {
		return selectObjectDefaultNull(
			logger,
			"SELECT admin_id, creation_date, upload_id, filename, filesize, contact_firstname, contact_name, contact_mail, contact_phone, sendto_mail, upload_id FROM upload_tbl WHERE company_id = ? AND filename = ?",
			new NewDownloadDataRowMapper(),
			companyID,
			filename);
	}

	@Override
	public void sendDataToStream(int uploadId, OutputStream stream) throws Exception {
		try(final Connection connection = getDataSource().getConnection()) {
			try(final PreparedStatement stmt = connection.prepareStatement("SELECT payload FROM upload_tbl WHERE upload_id = ?")) {
				stmt.setInt(1, uploadId);
		
				try(final ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						final Blob blob = rs.getBlob("payload");
		
						try(final InputStream inStream = blob.getBinaryStream()) {
							IOUtils.copy(inStream, stream);
						}
					} else {
						logger.warn("No data found for upload ID " + uploadId);
						throw new Exception("No data found for upload ID " + uploadId);
					}
				}
			}
		}
	}

    private Object getParentCompanyID(int companyId) {
		return select(logger, "SELECT COALESCE(parent_company_id, 0) FROM company_tbl WHERE company_id = ?", Integer.class, companyId);
	}

	@Override
	public long getCurrentUploadOverallSizeBytes(int companyID) {
    	if (isOracleDB()) {
    		Long mediapoolSize = select(logger, "SELECT COALESCE(SUM(DBMS_LOB.GETLENGTH(payload)), 0) FROM upload_tbl WHERE (company_id = ? OR company_id = ?)", Long.class, companyID, getParentCompanyID(companyID));
    		return (mediapoolSize == null ? 0 : mediapoolSize);
    	} else {
    		Long mediapoolSize =  select(logger, "SELECT COALESCE(SUM(OCTET_LENGTH(payload)), 0) FROM upload_tbl WHERE (company_id = ? OR company_id = ?)", Long.class, companyID, getParentCompanyID(companyID));
    		return (mediapoolSize == null ? 0 : mediapoolSize);
    	}
	}
}
