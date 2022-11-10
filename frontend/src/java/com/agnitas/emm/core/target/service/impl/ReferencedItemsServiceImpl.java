/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.target.dao.ReferencedItemsDao;
import com.agnitas.emm.core.target.eql.referencecollector.ReferencedItemsCollection;
import com.agnitas.emm.core.target.service.ReferencedItemsService;

/**
 * Implementation of {@link ReferencedItemsService} interface.
 */
public class ReferencedItemsServiceImpl implements ReferencedItemsService {

	private ReferencedItemsDao referencedItemsDao;
	
	final ReferencedItemsDao getReferencedItemsDao() {
		return this.referencedItemsDao;
	}
	
	@Transactional
	@Override
	public void saveReferencedItems(final ReferencedItemsCollection referencedObject, final int companyID, final int targetID) {
		removeReferencedItems(companyID, targetID);

		this.referencedItemsDao.saveReferencedProfileFields(companyID, targetID, toReferencedProfileFieldNameList(referencedObject));
		this.referencedItemsDao.saveReferencedMailings(companyID, targetID, toReferencedMailingIdList(referencedObject));
		this.referencedItemsDao.saveReferencedLinks(companyID, targetID, toReferencedLinkIdList(referencedObject));
		this.referencedItemsDao.saveReferencedAutoImports(companyID, targetID, toReferencedAutoImportIdsList(referencedObject));
	}

	@Override
	public final void removeReferencedItems(final int companyID, final int targetID) {
		this.referencedItemsDao.removeAllReferencedObjects(companyID, targetID);
	}

	private static List<String> toReferencedProfileFieldNameList(final ReferencedItemsCollection items) {
		return new ArrayList<>(items.getReferencedProfileFields());
	}
	
	private static List<Integer> toReferencedMailingIdList(final ReferencedItemsCollection items) {
		return new ArrayList<>(items.getReferencedMailingIds());
	}
	
	private static List<Integer> toReferencedLinkIdList(final ReferencedItemsCollection items) {
		return items.getReferencedLinkIds()
				.stream()
				.map(ReferencedItemsCollection.Link::getLinkId)
				.collect(Collectors.toList());
	}
	
	private static List<Integer> toReferencedAutoImportIdsList(final ReferencedItemsCollection items) {
		return new ArrayList<>(items.getReferencedAutoImportIds());
	}
	
	@Override
	public final List<TargetLight> listTargetGroupsReferencingProfileFieldByVisibleName(final int companyID, final String visibleShortname) {
		return referencedItemsDao.listTargetGroupsReferencingProfileField(companyID, visibleShortname);
	}

	@Override
	public final List<TargetLight> listTargetGroupsReferencingMailing(final int companyID, final int mailingID) {
		return referencedItemsDao.listTargetGroupsReferencingMailing(companyID, mailingID);
	}

	@Override
	public final List<TargetLight> listTargetGroupsReferencingLink(final int companyID, final int linkID) {
		return referencedItemsDao.listTargetGroupsReferencingLink(companyID, linkID);
	}

	@Override
	public final List<TargetLight> listTargetGroupsReferencingAutoImport(final int companyID, final int autoImportID) {
		return referencedItemsDao.listTargetGroupsReferencingAutoImport(companyID, autoImportID);
	}

	@Override
	public List<TargetLight> listTargetGroupsReferencingReferenceTable(final int companyID, final int tableID) {
		return Collections.emptyList();
	}

	@Override
	public List<TargetLight> listTargetGroupsReferencingReferenceTableColumn(final int companyID, final int tableID, final String columnName) {
		return Collections.emptyList();
	}

	/**
	 * Set DAO persisting information on referenced items.
	 * 
	 * @param dao DAO persisting information on referenced items
	 */
	@Required
	public final void setReferencedItemsDao(final ReferencedItemsDao dao) {
		this.referencedItemsDao = Objects.requireNonNull(dao, "ReferencedItemsDao is null");
	}
}
