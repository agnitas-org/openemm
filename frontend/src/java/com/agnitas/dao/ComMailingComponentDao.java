/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.emm.core.components.form.MailingImagesOverviewFilter;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.dao.MailingComponentDao;

import com.agnitas.web.CdnImage;

public interface ComMailingComponentDao extends MailingComponentDao {

	Date getComponentTime(int companyID, int mailingID, String name);

	Map<Integer, Date> getImageComponentsTimestamps(int companyID, int mailingID);

	List<MailingComponent> getMailingComponentsByType(MailingComponentType type, int companyID);

    Map<Integer, Integer> getImageSizes(int companyID, int mailingID);

	Map<Integer, String> getImageNames(int companyId, int mailingId, boolean includeExternalImages);

	boolean exists(int mailingID, int companyID, int componentID);

	boolean attachmentExists(int companyId, int mailingId, String name, int targetId);

	boolean deleteMailingComponentsByCompanyID(int companyID);
	
	void deleteMailingComponentsByMailing(int mailingID);

	List<Integer> findTargetDependentMailingsComponents(int targetGroupId, int companyId);

	List<Integer> filterComponentsOfNotSentMailings(List<Integer> components);

	boolean deleteImages(int companyId, int mailingId, Set<Integer> bulkIds);

    int getImageComponent(int companyId, int mailingId, MailingComponentType componentType);

    CdnImage getCdnImage(int companyID, int mailingID, String imageName, boolean mobile);

	MailingComponent getComponentByCdnID(String cdnID);

    /**
     * Method for marking unpresent components of some mailing
     *
     * @param mailingId mailing with components which will be marked as unpresent
     * @param presentComponents components which will not be marked as unpresent
     *
     * @return count of updated rows (unpresent components)
     */
	int setUnPresentComponentsForMailing(int mailingId, List<MailingComponent> presentComponents);

	boolean updateBinBlockBulk(int companyId, Collection<Integer> mailingIds, MailingComponentType componentType, Collection<String> namePatterns, byte[] value) throws Exception;

	List<String> getImagesNames(int mailingId, Set<Integer> bulkIds, int companyID);

	List<MailingComponent> getImagesOverview(int companyID, int mailingID, MailingImagesOverviewFilter filter);
}
