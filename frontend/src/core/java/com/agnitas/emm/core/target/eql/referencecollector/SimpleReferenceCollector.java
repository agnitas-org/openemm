/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.referencecollector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class SimpleReferenceCollector implements ReferenceCollector, ReferencedItemsCollection {

	private final Set<Integer> autoImportIds;
	private final Set<Integer> mailingIds;
	private final Set<Link> links;
	private final Set<String> profileFields;
	private final Set<RefTableColumn> referenceTablesAndColumns;
	private final Set<String> referenceTables;

	private final Set<Integer> autoImportIdsReadOnly;
	private final Set<Integer> mailingIdsReadOnly;
	private final Set<Link> linksReadOnly;
	private final Set<String> profileFieldsReadOnly;
	private final Set<RefTableColumn> referenceAndColumnsTablesReadOnly;
	private final Set<String> referenceTablesReadOnly;

	public SimpleReferenceCollector() {
		this.autoImportIds = new HashSet<>();
		this.mailingIds = new HashSet<>();
		this.links = new HashSet<>();
		this.profileFields = new HashSet<>();
		this.referenceTablesAndColumns = new HashSet<>();
		this.referenceTables = new HashSet<>();
		
		this.autoImportIdsReadOnly = Collections.unmodifiableSet(this.autoImportIds);
		this.mailingIdsReadOnly = Collections.unmodifiableSet(this.mailingIds);
		this.linksReadOnly = Collections.unmodifiableSet(this.links);
		this.profileFieldsReadOnly = Collections.unmodifiableSet(this.profileFields);
		this.referenceAndColumnsTablesReadOnly = Collections.unmodifiableSet(this.referenceTablesAndColumns);
		this.referenceTablesReadOnly = Collections.unmodifiableSet(this.referenceTables);
	}

	@Override
	public final Set<Integer> getReferencedAutoImportIds() {
		return this.autoImportIdsReadOnly;
	}

	@Override
	public final Set<Integer> getReferencedMailingIds() {
		return this.mailingIdsReadOnly;
	}

	@Override
	public final Set<Link> getReferencedLinkIds() {
		return this.linksReadOnly;
	}

	@Override
	public final Set<String> getReferencedProfileFields() {
		return this.profileFieldsReadOnly;
	}

	@Override
	public final Set<RefTableColumn> getReferencedRefTableColumns() {
		return this.referenceAndColumnsTablesReadOnly;
	}

	@Override
	public final Set<String> getReferencedReferenceTables() {
		return this.referenceTablesReadOnly;
	}
	
	@Override
	public final void addAutoImportReference(final int autoImportId) {
		this.autoImportIds.add(autoImportId);
	}

	@Override
	public final void addMailingReference(final int mailingId) {
		this.mailingIds.add(mailingId);
	}

	@Override
	public final void addLinkReference(final int mailingId, final Integer linkId) {
		this.links.add(new Link(mailingId, linkId));
	}

	@Override
	public final void addProfileFieldReference(final String profileFieldName) {
		this.profileFields.add(profileFieldName);
	}

	@Override
	public final void addReferenceTableReference(final String referenceTableName) {
		this.referenceTables.add(referenceTableName);
	}

	@Override
	public final void addReferenceTableReference(final String referenceTableName, final String columnName) {
		this.addReferenceTableReference(referenceTableName);
		this.referenceTablesAndColumns.add(new RefTableColumn(referenceTableName, columnName));
	}


}
