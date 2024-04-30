/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl.mapper;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.RowMapper;

/**
 * Simple {@link RowMapper} for retrieving integer values from ResultSet.
 * 
 * The source column must be at index 1.
 */
public class BlobRowMapper implements RowMapper<byte[]> {
	
	/** Singleton instance. */
	public static final BlobRowMapper INSTANCE = new BlobRowMapper();

	/**
	 * Creates a new instance.
	 * 
	 * @see #INSTANCE 
	 */
	private BlobRowMapper() {
		// Empty
	}
	
	@Override
	public byte[] mapRow(ResultSet resultSet, int index) throws SQLException {
		Blob blob = resultSet.getBlob(1);
		if (blob == null || blob.length() == 0) {
			return null;
		} else {
			try (InputStream dataStream = blob.getBinaryStream()) {
				return IOUtils.toByteArray(dataStream);
			} catch (Exception e) {
				throw new SQLException("Cannot read predelivery data: " + e.getMessage(), e);
			}
		}
	}
}
