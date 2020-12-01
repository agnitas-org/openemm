/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.backend.BlockData;
import org.agnitas.backend.DBase;
import org.agnitas.backend.Image;

/**
 * handle access to mediapool images
 */
public class MediapoolDAO {
	private long companyID;
	private Map<String, Image> images;

	public MediapoolDAO(DBase dbase, long forCompanyID) throws SQLException {
		companyID = forCompanyID;
		try (DBase.With with = dbase.with()) {
			images = retrieve(dbase, with, "mediapool_element_id", "grid_mediapool_element_tbl");
		}
	}

	/**
	 * Returns a mapping from mediapool images to real image information
	 *
	 * @return the mapping as a Map
	 */
	public Map<String, Image> images() {
		return images;
	}

	/**
	 * Retrieves the real mediapool image from the database as BlockData
	 * to integrate this as a component for mail generation
	 *
	 * @param dbase the database interface reference
	 * @param image the image to load the content for
	 * @return the image content as a component
	 */
	public BlockData getImage(DBase dbase, Image image) throws SQLException {
		return retrieveContent(dbase, image, "mediapool_element_id", "grid_mediapool_element_tbl");
	}

	private BlockData retrieveContent(DBase dbase, Image image, String idColumn, String table) throws SQLException {
		BlockData rc = null;

		if (dbase.exists (table)) try (DBase.With with = dbase.with ()) {
			Map <String, Object>	row = dbase.querys (with.jdbc (),
								    "SELECT content, mime_type " +
								    "FROM " + table + " " +
								    "WHERE " + idColumn + " = :imageID",
								    "imageID", image.id ());
			if (row != null) {
				rc = new BlockData ();

					rc.comptype = 5;
					rc.cid = image.filename();
					rc.cidEmit = image.link();
					rc.mime = dbase.asString(row.get("mime_type"));
					rc.type = BlockData.RELATED_BINARY;
					rc.binary = dbase.asBlob(row.get("content"));
				}
			}
		return rc;
	}

	private Map<String, Image> retrieve(DBase dbase, DBase.With with, String idColumn, String table) throws SQLException {
		Map<String, Image> rc = new HashMap<>();
		List<Map<String, Object>> rq;

		if (dbase.exists (table)) {
			rq = dbase.query (with.jdbc (),
					  "SELECT " + idColumn + ", filename, mime_type " +
					  "FROM  " + table + " " +
					  "WHERE company_id IN (0, :companyID) AND filename IS NOT NULL " +
					  "ORDER BY company_id",
					  "companyID", companyID);
			for (int n = 0; n < rq.size (); ++n) {
				Map <String, Object>	row = rq.get (n);
				long			id = dbase.asLong (row.get (idColumn));
				String			filename = dbase.asString (row.get ("filename"));
				String			mime = dbase.asString (row.get ("mime"));
				String			ext;
				int			pos;
					
				if ((pos = filename.lastIndexOf ('.')) != -1) {
					ext = filename.substring (pos + 1);
				} else if ((mime != null) && ((pos = mime.indexOf ('/')) != -1)) {
					ext = mime.substring (pos + 1);
				} else {
					ext = "jpg";
				}
				rc.put(filename, new Image(id, filename, String.format("%d.%s", id, ext), null));
			}
		}
		return rc;
	}
}
