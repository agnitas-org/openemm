/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import	java.sql.SQLException;
import	java.util.ArrayList;
import	java.util.Arrays;
import	java.util.List;
import	java.util.Map;
import	org.agnitas.backend.BlockData;
import	org.agnitas.backend.DBase;
import	org.agnitas.backend.Media;
import	org.agnitas.backend.StringOps;
import	org.agnitas.util.Const;
import	org.agnitas.util.Log;

/**
 * Access all informations from the component table
 */
public class ComponentDAO {
	private long	companyID;
	private long	mailingID;
	public ComponentDAO (long forCompanyID, long forMailingID) {
		companyID = forCompanyID;
		mailingID = forMailingID;
	}

	public List <BlockData> retrieve (DBase dbase, int[] componentTypes) throws SQLException {
		List <BlockData>		rc = new ArrayList <> ();
		String				reduceClause = null;
		
		if ((componentTypes != null) && (componentTypes.length > 0)) {
			reduceClause = " AND comptype ";
			if (componentTypes.length > 1) {
				reduceClause += "IN (" + Arrays.stream (componentTypes).boxed ().map (e -> e.toString ()).reduce ((s, e) -> s + "," + e).orElse (null) + ")";
			} else {
				reduceClause += "= " + componentTypes[0];
			}
		}
		
		List <Map <String, Object>>	rq;
		
		try (DBase.With with = dbase.with ()) {
			rq = dbase.query (with.jdbc (),
					  "SELECT component_id, comptype, url_id, compname, mtype, target_id, emmblock, binblock " +
					  "FROM component_tbl " +
					  "WHERE company_id = :companyID AND (mailing_id = :mailingID OR (mailing_id = 0 AND mailtemplate_id = 0 AND comppresent != 0)) " + (reduceClause  != null ? reduceClause + " " : "") +
					  "ORDER BY component_id",
					  "companyID", companyID,
					  "mailingID", mailingID);
			for (int n = 0; n < rq.size (); ++n) {
				Map <String, Object>	row = rq.get (n);
				BlockData		tmp = new BlockData ();
			
				tmp.comptype = dbase.asInt (row.get ("comptype"));
				tmp.cid = dbase.asString (row.get ("compname"));
				tmp.urlID = dbase.asInt (row.get ("url_id"));
				tmp.mime = dbase.asString (row.get ("mtype"));
				tmp.targetID = dbase.asInt (row.get ("target_id"));
				switch (tmp.comptype) {
				case 0:
					tmp.isParseable = true;
					tmp.isText = true;
					if (tmp.cid.equals (Const.Component.NAME_HEADER)) {
						tmp.type = BlockData.HEADER;
						tmp.media = Media.TYPE_EMAIL;
					} else if (tmp.cid.equals (Const.Component.NAME_TEXT)) {
						tmp.type = BlockData.TEXT;
						tmp.media = Media.TYPE_EMAIL;
					} else if (tmp.cid.equals (Const.Component.NAME_HTML)) {
						tmp.type = BlockData.HTML;
						tmp.media = Media.TYPE_EMAIL;
					} else if (tmp.cid.equals (Const.Component.NAME_FAX)) {
						tmp.type = BlockData.PDF;
						tmp.media = Media.TYPE_FAX;
						tmp.isPDF = true;
					} else if (tmp.cid.equals (Const.Component.NAME_PRINT)) {
						tmp.type = BlockData.PDF;
						tmp.media = Media.TYPE_PRINT;
						tmp.isPDF = true;
					} else if (tmp.cid.equals (Const.Component.NAME_MMS)) {
						tmp.type = BlockData.MMS;
						tmp.media = Media.TYPE_MMS;
					} else if (tmp.cid.equals (Const.Component.NAME_SMS)) {
						tmp.type = BlockData.SMS;
						tmp.media = Media.TYPE_SMS;
					} else {
						dbase.logging (Log.WARNING, "component", "Invalid compname " + tmp.cid + " for comptype 0 found");
						tmp = null;
					}
					break;
				case 1:
					tmp.type = BlockData.RELATED_BINARY;
					break;
				case 3:
					tmp.type = BlockData.ATTACHMENT_BINARY;
					tmp.isAttachment = true;
					break;
				case 4:
					tmp.type = BlockData.ATTACHMENT_TEXT;
					tmp.isParseable = true;
					tmp.isText = true;
					tmp.isAttachment = true;
					if ((tmp.mime != null) && tmp.mime.equals ("application/pdf")) {
						tmp.isPDF = true;
					}
					break;
				case 5:
					tmp.type = BlockData.RELATED_BINARY;
					break;
				case 6:
					tmp.type = BlockData.FONT;
					tmp.isFont = true;
					break;
				case 7:
					tmp.type = BlockData.ATTACHMENT_BINARY;
					tmp.isParseable = true;
					tmp.isAttachment = true;
					tmp.isPrecoded = true;
					break;
				default:
					dbase.logging (Log.WARNING, "component", "Invalid comptype " + tmp.comptype + " found");
					tmp = null;
					break;
				}
				if (tmp != null) {
					String	emmblock = dbase.asClob (row.get ("emmblock"));
					byte[]	binary = dbase.asBlob (row.get ("binblock"));

					if (emmblock != null) {
						if (tmp.isParseable || tmp.isPrecoded) {
							tmp.content =  StringOps.convertOld2New (emmblock);
						} else {
							tmp.content = emmblock;
						}
					}
					tmp.binary = binary;
					if (tmp.binary != null) {
						if ((! tmp.isParseable) && (tmp.content != null) && (tmp.content.length () == 0)) {
							tmp.content = null;
						}
					}
					if ((tmp.type == BlockData.RELATED_BINARY) && (tmp.mime != null) && (tmp.mime.toLowerCase ().startsWith ("image/"))) {
						tmp.isImage = true;
					}
					rc.add (tmp);
				}
			}
		}
		return rc;
	}
	
	public void add (DBase dbase, BlockData bd) throws SQLException {
		try (DBase.With with = dbase.with ()) {
			String	query;
			
			if (dbase.isOracle ()) {
				long	cid;

				query = "SELECT component_tbl_seq.nextval FROM dual";
				cid = dbase.queryLong (with.jdbc (), query);
				query = "INSERT INTO component_tbl (component_id, mailing_id, company_id, mtype, comptype, compname, emmblock, binblock, target_id, url_id, timestamp) " +
					"VALUES (:componentID, :mailingID, :companyID, :mtype, 5, :compname, null, :binblock, 0, 0, CURRENT_TIMESTAMP)";
				dbase.update (with.jdbc (), query, "componentID", cid, "mailingID", mailingID, "companyID", companyID, "mtype", bd.mime, "compname", bd.cid, "binblock", bd.binary);
			} else {
				query = "INSERT INTO component_tbl (mailing_id, company_id, mtype, comptype, compname, emmblock, binblock, target_id, url_id, timestamp) " +
					"VALUES (:mailingID, :companyID, :mtype, 5, :compname, null, :binblock, 0, 0, CURRENT_TIMESTAMP)";
				dbase.update (with.jdbc (), query, "mailingID", mailingID, "companyID", companyID, "mtype", bd.mime, "compname", bd.cid, "binblock", bd.binary);
			}
			dbase.logging (Log.DEBUG, "add", "Added new component " + bd.cid);
		} catch (SQLException e) {
			dbase.logging (Log.ERROR, "add", "Failed to add new component " + bd.cid + ": " + e.toString (), e);
		}
	}
}
