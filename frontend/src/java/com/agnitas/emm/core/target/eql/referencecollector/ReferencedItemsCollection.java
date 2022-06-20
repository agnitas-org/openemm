/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.referencecollector;

import java.util.Set;

public interface ReferencedItemsCollection {

	public static final class Link {
		private final int mailingId;
		private final int linkId;
		
		public Link(int mailingId, int linkId) {
			this.mailingId = mailingId;
			this.linkId = linkId;
		}

		public int getMailingId() {
			return mailingId;
		}

		public int getLinkId() {
			return linkId;
		}
		
		@Override
		public int hashCode() {
			return mailingId + linkId;
		}

		@Override
		public boolean equals(Object o) {
			if(o == null)
				return false;
			
			if(o instanceof Link) {
				Link link = (Link) o;
				
				return mailingId == link.mailingId && linkId == link.linkId;
			} else {
				return false;
			}
		}
	}
	
	public static final class RefTableColumn {
		private final String table;
		private final String column;
		
		public RefTableColumn(String table, String column) {
			this.table = table;
			this.column = column;
		}

		public String getTable() {
			return table;
		}

		public String getColumn() {
			return column;
		}
		
		@Override
		public int hashCode() {
			return table.hashCode() + column.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if(o == null)
				return false;
			if(o instanceof RefTableColumn) {
				RefTableColumn r = (RefTableColumn) o;
				
				return table.equals(r.table) && column.equals(r.column);
			} else {
				return false;
			}
		}
	}

	Set<Integer> getReferencedAutoImportIds();
	Set<Integer> getReferencedMailingIds();
	Set<Link> getReferencedLinkIds();
	Set<String> getReferencedProfileFields();
	Set<RefTableColumn> getReferencedRefTableColumns();
	Set<String> getReferencedReferenceTables();
}
